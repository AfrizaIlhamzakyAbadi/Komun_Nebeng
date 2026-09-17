/*
 ** Copyright 2011, The Android Open Source Project
 **
 ** Licensed under the Apache License, Version 2.0 (the "License");
 ** you may not use this file except in compliance with the License.
 ** You may obtain a copy of the License at
 **
 **     http://www.apache.org/licenses/LICENSE-2.0
 **
 ** Unless required by applicable law or agreed to in writing, software
 ** distributed under the License is distributed on an "AS IS" BASIS,
 ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 ** See the License for the specific language governing permissions and
 ** limitations under the License.
 */

package android.view.accessibility;

import static android.accessibilityservice.AccessibilityTrace.FLAGS_ACCESSIBILITY_INTERACTION_CLIENT;
import static android.accessibilityservice.AccessibilityTrace.FLAGS_ACCESSIBILITY_INTERACTION_CONNECTION_CALLBACK;
import static android.os.Build.VERSION_CODES.S;
import static android.view.accessibility.AccessibilityNodeInfo.FLAG_PREFETCH_DESCENDANTS_MASK;
import static android.view.accessibility.AccessibilityNodeInfo.FLAG_PREFETCH_MASK;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.IAccessibilityServiceConnection;
import android.annotation.CallbackExecutor;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresNoPermission;
import android.annotation.SuppressLint;
import android.compat.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Log;
import android.util.LongSparseArray;
import android.util.Pair;
import android.util.SparseArray;
import android.util.SparseLongArray;
import android.view.Display;
import android.view.SurfaceControl;
import android.view.ViewConfiguration;
import android.window.ScreenCaptureInternal;

import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.ArrayUtils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

/**
 * This class is a singleton that performs accessibility interaction
 * which is it queries remote view hierarchies about snapshots of their
 * views as well requests from these hierarchies to perform certain
 * actions on their views.
 *
 * Rationale: The content retrieval APIs are synchronous from a client's
 *     perspective but internally they are asynchronous. The client thread
 *     calls into the system requesting an action and providing a callback
 *     to receive the result after which it waits up to a timeout for that
 *     result. The system enforces security and the delegates the request
 *     to a given view hierarchy where a message is posted (from a binder
 *     thread) describing what to be performed by the main UI thread the
 *     result of which it delivered via the mentioned callback. However,
 *     the blocked client thread and the main UI thread of the target view
 *     hierarchy can be the same thread, for example an accessibility service
 *     and an activity run in the same process, thus they are executed on the
 *     same main thread. In such a case the retrieval will fail since the UI
 *     thread that has to process the message describing the work to be done
 *     is blocked waiting for a result is has to compute! To avoid this scenario
 *     when making a call the client also passes its process and thread ids so
 *     the accessed view hierarchy can detect if the client making the request
 *     is running in its main UI thread. In such a case the view hierarchy,
 *     specifically the binder thread performing the IPC to it, does not post a
 *     message to be run on the UI thread but passes it to the singleton
 *     interaction client through which all interactions occur and the latter is
 *     responsible to execute the message before starting to wait for the
 *     asynchronous result delivered via the callback. In this case the expected
 *     result is already received so no waiting is performed.
 */
@Hide
public final class AccessibilityInteractionClient
        extends IAccessibilityInteractionConnectionCallback.Stub {

    public static final int NO_ID = -1;

    public static final String CALL_STACK = "call_stack";
    public static final String IGNORE_CALL_STACK = "ignore_call_stack";

    private static final String LOG_TAG = "AccessibilityInteractionClient";

    private static final boolean DEBUG = false;

    private static final boolean CHECK_INTEGRITY = true;

    private static final long TIMEOUT_INTERACTION_MILLIS = 5000;

    private static final long DISABLE_PREFETCHING_FOR_SCROLLING_MILLIS =
            (long) (ViewConfiguration.getSendRecurringAccessibilityEventsInterval() * 1.5);

    private static final Object sStaticLock = new Object();

    private static final LongSparseArray<AccessibilityInteractionClient> sClients =
        new LongSparseArray<>();

    private static final SparseArray<IAccessibilityServiceConnection> sConnectionCache =
            new SparseArray<>();

    // Used to generate connection ids for direct app-process connections. Start sufficiently far
    // enough from the connection ids generated by AccessibilityManagerService.
    private static int sDirectConnectionIdCounter = 1 << 30;
    private static int sDirectConnectionCount = 0;

    /** List of timestamps which indicate the latest time an a11y service receives a scroll event
        from a window, mapping from windowId -> timestamp. */
    private static final SparseLongArray sScrollingWindows = new SparseLongArray();

    private static SparseArray<AccessibilityCache> sCaches = new SparseArray<>();

    private final AtomicInteger mInteractionIdCounter = new AtomicInteger();

    private final Object mInstanceLock = new Object();

    private final AccessibilityManager mAccessibilityManager;

    // SparseArray of interaction ID -> screenshot executor+callback.
    private final SparseArray<Pair<Executor, AccessibilityService.TakeScreenshotCallback>>
            mTakeScreenshotOfWindowCallbacks = new SparseArray<>();
    private Message mSameThreadMessage;
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    // SparseArray of interaction ID -> status and result state for interaction.
    @GuardedBy("mInstanceLock")
    private final SparseArray<InteractionState> mActiveInteractions = new SparseArray<>();
    // Interaction stored to wait for prefetch results.
    @GuardedBy("mInstanceLock")
    private InteractionState mPrefetchInteraction = null;

    /**
     * @return The client for the current thread.
     */
    @UnsupportedAppUsage()
    public static AccessibilityInteractionClient getInstance() {
        final long threadId = Thread.currentThread().getId();
        return getInstanceForThread(threadId);
    }

    /**
     * <strong>Note:</strong> We keep one instance per interrogating thread since
     * the instance contains state which can lead to undesired thread interleavings.
     * We do not have a thread local variable since other threads should be able to
     * look up the correct client knowing a thread id. See ViewRootImpl for details.
     *
     * @return The client for a given <code>threadId</code>.
     */
    public static AccessibilityInteractionClient getInstanceForThread(long threadId) {
        synchronized (sStaticLock) {
            AccessibilityInteractionClient client = sClients.get(threadId);
            if (client == null) {
                client = new AccessibilityInteractionClient();
                sClients.put(threadId, client);
            }
            return client;
        }
    }

    /**
     * @return The client for the current thread.
     */
    public static AccessibilityInteractionClient getInstance(Context context) {
        final long threadId = Thread.currentThread().getId();
        if (context != null) {
            return getInstanceForThread(threadId, context);
        }
        return getInstanceForThread(threadId);
    }

    /**
     * <strong>Note:</strong> We keep one instance per interrogating thread since
     * the instance contains state which can lead to undesired thread interleavings.
     * We do not have a thread local variable since other threads should be able to
     * look up the correct client knowing a thread id. See ViewRootImpl for details.
     *
     * @return The client for a given <code>threadId</code>.
     */
    public static AccessibilityInteractionClient getInstanceForThread(long threadId,
            Context context) {
        synchronized (sStaticLock) {
            AccessibilityInteractionClient client = sClients.get(threadId);
            if (client == null) {
                client = new AccessibilityInteractionClient(context);
                sClients.put(threadId, client);
            }
            return client;
        }
    }

    /**
     * Gets a cached accessibility service connection.
     *
     * @param connectionId The connection id.
     * @return The cached connection if such.
     */
    public static IAccessibilityServiceConnection getConnection(int connectionId) {
        synchronized (sConnectionCache) {
            return sConnectionCache.get(connectionId);
        }
    }

    /**
     * Adds a cached accessibility service connection.
     *
     * Adds a cache if {@code initializeCache} is true
     * @param connectionId The connection id.
     * @param connection The connection.
     * @param initializeCache whether to initialize a cache
     */
    public static void addConnection(int connectionId, IAccessibilityServiceConnection connection,
            boolean initializeCache) {
        if (connectionId == NO_ID) {
            return;
        }
        synchronized (sConnectionCache) {
            IAccessibilityServiceConnection existingConnection = getConnection(connectionId);
            if (existingConnection instanceof DirectAccessibilityConnection) {
                throw new IllegalArgumentException(
                        "Cannot add service connection with id " + connectionId
                                + " which conflicts with existing direct connection.");
            }
            sConnectionCache.put(connectionId, connection);
            if (!initializeCache) {
                return;
            }
            sCaches.put(connectionId, new AccessibilityCache(
                        new AccessibilityCache.AccessibilityNodeRefresher()));
        }
    }

    /**
     * Adds a new {@link DirectAccessibilityConnection} using the provided
     * {@link IAccessibilityInteractionConnection} to create a direct connection between
     * this client and the {@link android.view.ViewRootImpl} for queries inside the app process.
     *
     * <p>
     * See {@link DirectAccessibilityConnection} for supported methods.
     * </p>
     *
     * @param connection The ViewRootImpl's {@link IAccessibilityInteractionConnection}.
     */
    public static int addDirectConnection(IAccessibilityInteractionConnection connection,
            AccessibilityManager accessibilityManager) {
        synchronized (sConnectionCache) {
            int connectionId = sDirectConnectionIdCounter++;
            if (getConnection(connectionId) != null) {
                throw new IllegalArgumentException(
                        "Cannot add direct connection with existing id " + connectionId);
            }
            DirectAccessibilityConnection directAccessibilityConnection =
                    new DirectAccessibilityConnection(connection, accessibilityManager);
            sConnectionCache.put(connectionId, directAccessibilityConnection);
            sDirectConnectionCount++;
            // Do not use AccessibilityCache for this connection, since there is no corresponding
            // AccessibilityService to handle cache invalidation events.
            return connectionId;
        }
    }

    /** Check if any {@link DirectAccessibilityConnection} is currently in the connection cache. */
    public static boolean hasAnyDirectConnection() {
        return sDirectConnectionCount > 0;
    }

    /**
     * Gets a cached associated with the connection id if available.
     *
     */
    public static AccessibilityCache getCache(int connectionId) {
        synchronized (sConnectionCache) {
            return sCaches.get(connectionId);
        }
    }

    /**
     * Removes a cached accessibility service connection.
     *
     * @param connectionId The connection id.
     */
    public static void removeConnection(int connectionId) {
        synchronized (sConnectionCache) {
            if (getConnection(connectionId) instanceof DirectAccessibilityConnection) {
                sDirectConnectionCount--;
            }
            sConnectionCache.remove(connectionId);
            sCaches.remove(connectionId);
        }
    }

    /**
     * This method is only for testing. Replacing the cache is a generally terrible idea, but
     * tests need to be able to verify this class's interactions with the cache
     */
    @VisibleForTesting
    public static void setCache(int connectionId, AccessibilityCache cache) {
        synchronized (sConnectionCache) {
            sCaches.put(connectionId, cache);
        }
    }

    private AccessibilityInteractionClient() {
        /* reducing constructor visibility */
        mAccessibilityManager = null;
    }

    private AccessibilityInteractionClient(Context context) {
        mAccessibilityManager = context.getSystemService(AccessibilityManager.class);
    }

    /**
     * Sets the message to be processed if the interacted view hierarchy
     * and the interacting client are running in the same thread.
     *
     * @param message The message.
     */
    @UnsupportedAppUsage(maxTargetSdk = Build.VERSION_CODES.R, trackingBug = 170729553)
    public void setSameThreadMessage(Message message) {
        synchronized (mInstanceLock) {
            mSameThreadMessage = message;
            mInstanceLock.notifyAll();
        }
    }

    /**
     * Gets the root {@link AccessibilityNodeInfo} in the currently active window.
     *
     * @param connectionId The id of a connection for interacting with the system.
     * @return The root {@link AccessibilityNodeInfo} if found, null otherwise.
     */
    public AccessibilityNodeInfo getRootInActiveWindow(int connectionId,
            @AccessibilityNodeInfo.PrefetchingStrategy int strategy) {
        return findAccessibilityNodeInfoByAccessibilityId(connectionId,
                AccessibilityWindowInfo.ACTIVE_WINDOW_ID, AccessibilityNodeInfo.ROOT_NODE_ID,
                false, strategy, null);
    }

    /**
     * Gets the info for a window.
     *
     * @param connectionId The id of a connection for interacting with the system.
     * @param accessibilityWindowId A unique window id. Use
     *     {@link android.view.accessibility.AccessibilityWindowInfo#ACTIVE_WINDOW_ID}
     *     to query the currently active window.
     * @return The {@link AccessibilityWindowInfo}.
     */
    public AccessibilityWindowInfo getWindow(int connectionId, int accessibilityWindowId) {
        return getWindow(connectionId, accessibilityWindowId, /* bypassCache */ false);
    }

    /**
     * Gets the info for a window.
     *
     * @param connectionId The id of a connection for interacting with the system.
     * @param accessibilityWindowId A unique window id. Use
     *     {@link AccessibilityWindowInfo#ACTIVE_WINDOW_ID}
     *     to query the currently active window.
     * @param bypassCache Whether to bypass the cache.
     * @return The {@link AccessibilityWindowInfo}.
     */
    public AccessibilityWindowInfo getWindow(int connectionId, int accessibilityWindowId,
            boolean bypassCache) {
        final String loggingMethod = "getWindow";

        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection != null) {
                AccessibilityWindowInfo window;
                AccessibilityCache cache = getCache(connectionId);
                if (cache != null) {
                    if (!bypassCache) {
                        window = cache.getWindow(accessibilityWindowId);
                        if (window != null) {
                            if (DEBUG) {
                                Log.i(LOG_TAG, "Window cache hit");
                            }
                            if (shouldTraceClient()) {
                                logTraceClient(connection, loggingMethod + " cache",
                                        "connectionId=" + connectionId + ";accessibilityWindowId="
                                                + accessibilityWindowId + ";bypassCache=false");
                            }
                            return window;
                        }
                        if (DEBUG) {
                            Log.i(LOG_TAG, "Window cache miss");
                        }
                    }
                } else {
                    if (DEBUG) {
                        Log.w(LOG_TAG, "Cache is null for connection id: " + connectionId);
                    }
                }

                final long identityToken = Binder.clearCallingIdentity();
                try {
                    window = connection.getWindow(accessibilityWindowId);
                } finally {
                    Binder.restoreCallingIdentity(identityToken);
                }
                if (shouldTraceClient()) {
                    logTraceClient(connection, loggingMethod, "connectionId=" + connectionId
                            + ";accessibilityWindowId=" + accessibilityWindowId + ";bypassCache="
                            + bypassCache);
                }

                if (window != null) {
                    if (!bypassCache && cache != null) {
                        cache.addWindow(window);
                    }
                    return window;
                }
            } else {
                if (DEBUG) {
                    Log.w(LOG_TAG, "No connection for connection id: " + connectionId);
                }
            }
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while calling remote getWindow", re);
        }
        return null;
    }

    /**
     * Gets the info for all windows of the default display.
     *
     * @param connectionId The id of a connection for interacting with the system.
     * @return The {@link AccessibilityWindowInfo} list.
     */
    public List<AccessibilityWindowInfo> getWindows(int connectionId) {
        return getWindowsOnDisplay(connectionId, Display.DEFAULT_DISPLAY);
    }

    /**
     * Gets the info for all windows of the specified display.
     *
     * @param connectionId The id of a connection for interacting with the system.
     * @return The {@link AccessibilityWindowInfo} list belonging to {@code displayId}.
     */
    public List<AccessibilityWindowInfo> getWindowsOnDisplay(int connectionId, int displayId) {
        final SparseArray<List<AccessibilityWindowInfo>> windows =
                getWindowsOnAllDisplays(connectionId);
        return windows.get(displayId, Collections.emptyList());
    }
    /**
     * Gets the info for all windows of all displays.
     *
     * @param connectionId The id of a connection for interacting with the system.
     * @return The SparseArray of {@link AccessibilityWindowInfo} list.
     *         The key of SparseArray is display ID.
     */
    public SparseArray<List<AccessibilityWindowInfo>> getWindowsOnAllDisplays(int connectionId) {
        final String loggingMethod = "getWindowsOnAllDisplays";

        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection != null) {
                SparseArray<List<AccessibilityWindowInfo>> windows;
                AccessibilityCache cache = getCache(connectionId);
                if (cache != null) {
                    windows = cache.getWindowsOnAllDisplays();
                    if (windows != null) {
                        if (DEBUG) {
                            Log.i(LOG_TAG, "Windows cache hit");
                        }
                        if (shouldTraceClient()) {
                            logTraceClient(
                                    connection, loggingMethod + " cache",
                                    "connectionId=" + connectionId);
                        }
                        return windows;
                    }
                    if (DEBUG) {
                        Log.i(LOG_TAG, "Windows cache miss");
                    }
                } else {
                    if (DEBUG) {
                        Log.w(LOG_TAG, "Cache is null for connection id: " + connectionId);
                    }
                }

                long populationTimeStamp;
                final long identityToken = Binder.clearCallingIdentity();
                try {
                    populationTimeStamp = SystemClock.uptimeMillis();
                    windows = connection.getWindows();
                } finally {
                    Binder.restoreCallingIdentity(identityToken);
                }
                if (shouldTraceClient()) {
                    logTraceClient(connection, loggingMethod, "connectionId=" + connectionId);
                }
                if (windows != null) {
                    if (cache != null) {
                        cache.setWindowsOnAllDisplays(windows, populationTimeStamp);
                    }
                    return windows;
                }
            } else {
                if (DEBUG) {
                    Log.w(LOG_TAG, "No connection for connection id: " + connectionId);
                }
            }
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while calling remote getWindowsOnAllDisplays", re);
        }

        final SparseArray<List<AccessibilityWindowInfo>> emptyWindows = new SparseArray<>();
        return emptyWindows;
    }


    /**
     * Finds an {@link AccessibilityNodeInfo} by accessibility id and given leash token instead of
     * window id. This method is used to find the leashed node on the embedded view hierarchy.
     *
     * @param connectionId The id of a connection for interacting with the system.
     * @param leashToken The token of the embedded hierarchy.
     * @param accessibilityNodeId A unique view id or virtual descendant id from
     *     where to start the search. Use
     *     {@link android.view.accessibility.AccessibilityNodeInfo#ROOT_NODE_ID}
     *     to start from the root.
     * @param bypassCache Whether to bypass the cache while looking for the node.
     * @param prefetchFlags flags to guide prefetching.
     * @param arguments Optional action arguments.
     * @return An {@link AccessibilityNodeInfo} if found, null otherwise.
     */
    public @Nullable AccessibilityNodeInfo findAccessibilityNodeInfoByAccessibilityId(
            int connectionId, @NonNull IBinder leashToken, long accessibilityNodeId,
            boolean bypassCache, int prefetchFlags, Bundle arguments) {
        if (leashToken == null) {
            return null;
        }
        int windowId = -1;
        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection != null) {
                windowId = connection.getWindowIdForLeashToken(leashToken);
            } else {
                if (DEBUG) {
                    Log.w(LOG_TAG, "No connection for connection id: " + connectionId);
                }
            }
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while calling remote getWindowIdForLeashToken", re);
        }
        if (windowId == -1) {
            return null;
        }
        return findAccessibilityNodeInfoByAccessibilityId(connectionId, windowId,
                accessibilityNodeId, bypassCache, prefetchFlags, arguments);
    }

    /**
     * Finds an {@link AccessibilityNodeInfo} by accessibility id.
     *
     * @param connectionId The id of a connection for interacting with the system.
     * @param accessibilityWindowId A unique window id. Use
     *     {@link android.view.accessibility.AccessibilityWindowInfo#ACTIVE_WINDOW_ID}
     *     to query the currently active window.
     * @param accessibilityNodeId A unique view id or virtual descendant id from
     *     where to start the search. Use
     *     {@link android.view.accessibility.AccessibilityNodeInfo#ROOT_NODE_ID}
     *     to start from the root.
     * @param bypassCache Whether to bypass the cache while looking for the node.
     * @param prefetchFlags flags to guide prefetching.
     * @return An {@link AccessibilityNodeInfo} if found, null otherwise.
     */
    public @Nullable AccessibilityNodeInfo findAccessibilityNodeInfoByAccessibilityId(
            int connectionId, int accessibilityWindowId, long accessibilityNodeId,
            boolean bypassCache, int prefetchFlags, Bundle arguments) {
        final String loggingMethod = "findAccessibilityNodeInfoByAccessibilityId";
        InteractionState state = null;

        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection != null) {
                if (!bypassCache) {
                    AccessibilityCache cache = getCache(connectionId);
                    if (cache != null) {
                        AccessibilityNodeInfo cachedInfo = cache.getNode(
                                accessibilityWindowId, accessibilityNodeId);
                        if (cachedInfo != null) {
                            if (DEBUG) {
                                Log.i(LOG_TAG, "Node cache hit for "
                                        + idToString(accessibilityWindowId, accessibilityNodeId));
                            }
                            if (shouldTraceClient()) {
                                logTraceClient(connection,
                                        loggingMethod + " cache",
                                        "connectionId=" + connectionId + ";accessibilityWindowId="
                                                + accessibilityWindowId + ";accessibilityNodeId="
                                                + accessibilityNodeId + ";bypassCache="
                                                + bypassCache + ";prefetchFlags=" + prefetchFlags
                                                + ";arguments=" + arguments);
                            }
                            return cachedInfo;
                        }
                        if (!cache.isEnabled()) {
                            // Skip prefetching if cache is disabled.
                            prefetchFlags &= ~FLAG_PREFETCH_MASK;
                        }
                        if (DEBUG) {
                            Log.i(LOG_TAG, "Node cache miss for "
                                    + idToString(accessibilityWindowId, accessibilityNodeId));
                        }
                    } else {
                        if (DEBUG) {
                            Log.w(LOG_TAG, "Cache is null for connection id: " + connectionId);
                        }
                    }
                } else {
                    // No need to prefech nodes in bypass cache case.
                    prefetchFlags &= ~FLAG_PREFETCH_MASK;
                }
                // Skip prefetching if window is scrolling.
                if ((prefetchFlags & FLAG_PREFETCH_MASK) != 0
                        && isWindowScrolling(accessibilityWindowId)) {
                    prefetchFlags &= ~FLAG_PREFETCH_MASK;
                }

                final int descendantPrefetchFlags = prefetchFlags & FLAG_PREFETCH_DESCENDANTS_MASK;
                if ((descendantPrefetchFlags & (descendantPrefetchFlags - 1)) != 0) {
                    throw new IllegalArgumentException("There can be no more than one descendant"
                            + " prefetching strategy");
                }
                state = startNewInteraction(connectionId, connection, loggingMethod, bypassCache);
                if (shouldTraceClient()) {
                    logTraceClient(connection, loggingMethod,
                            "InteractionId:" + state.mInteractionId + "connectionId=" + connectionId
                            + ";accessibilityWindowId=" + accessibilityWindowId
                            + ";accessibilityNodeId=" + accessibilityNodeId + ";bypassCache="
                            + bypassCache + ";prefetchFlags=" + prefetchFlags + ";arguments="
                            + arguments);
                }
                final String[] packageNames;
                final long identityToken = Binder.clearCallingIdentity();
                try {
                    packageNames = connection.findAccessibilityNodeInfoByAccessibilityId(
                            accessibilityWindowId, accessibilityNodeId, state.mInteractionId,
                            this, prefetchFlags, Thread.currentThread().getId(), arguments);
                } finally {
                    Binder.restoreCallingIdentity(identityToken);
                }
                if (packageNames != null) {
                    state.configureForFindAccessibilityNodeInfoResult(packageNames);

                    if ((prefetchFlags
                            & AccessibilityNodeInfo.FLAG_PREFETCH_UNINTERRUPTIBLE) != 0) {
                        List<AccessibilityNodeInfo> infos =
                                getFindAccessibilityNodeInfosResultAndClear(state);
                        if (infos != null && !infos.isEmpty()) {
                            return infos.get(0);
                        }
                    } else {
                        boolean shouldPrefetch = (prefetchFlags & FLAG_PREFETCH_MASK) != 0;
                        return getFindAccessibilityNodeInfoResultAndClear(state, shouldPrefetch);
                    }
                }
            } else {
                if (DEBUG) {
                    Log.w(LOG_TAG, "No connection for connection id: " + connectionId);
                }
            }
        } catch (RemoteException re) {
            Log.e(LOG_TAG, "Error while calling remote"
                    + " findAccessibilityNodeInfoByAccessibilityId", re);
        } finally {
            clearInteraction(state);
        }
        return null;
    }

    private static String idToString(int accessibilityWindowId, long accessibilityNodeId) {
        return accessibilityWindowId + "/"
                + AccessibilityNodeInfo.idToString(accessibilityNodeId);
    }

    /**
     * Finds an {@link AccessibilityNodeInfo} by View id. The search is performed in
     * the window whose id is specified and starts from the node whose accessibility
     * id is specified.
     *
     * @param connectionId The id of a connection for interacting with the system.
     * @param accessibilityWindowId A unique window id. Use
     *     {@link android.view.accessibility.AccessibilityWindowInfo#ACTIVE_WINDOW_ID}
     *     to query the currently active window.
     * @param accessibilityNodeId A unique view id or virtual descendant id from
     *     where to start the search. Use
     *     {@link android.view.accessibility.AccessibilityNodeInfo#ROOT_NODE_ID}
     *     to start from the root.
     * @param viewId The fully qualified resource name of the view id to find.
     * @return An list of {@link AccessibilityNodeInfo} if found, empty list otherwise.
     */
    public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByViewId(int connectionId,
            int accessibilityWindowId, long accessibilityNodeId, String viewId) {
        final String loggingMethod = "findAccessibilityNodeInfosByViewId";
        InteractionState state = null;

        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection != null) {
                state = startNewInteraction(connectionId, connection, loggingMethod, false);
                final String[] packageNames;
                final long identityToken = Binder.clearCallingIdentity();
                try {
                    if (shouldTraceClient()) {
                        logTraceClient(connection, loggingMethod,
                                "InteractionId=" + state.mInteractionId + ";connectionId="
                                + connectionId + ";accessibilityWindowId=" + accessibilityWindowId
                                + ";accessibilityNodeId=" + accessibilityNodeId + ";viewId="
                                + viewId);
                    }

                    packageNames = connection.findAccessibilityNodeInfosByViewId(
                            accessibilityWindowId, accessibilityNodeId, viewId,
                            state.mInteractionId, this, Thread.currentThread().getId());
                } finally {
                    Binder.restoreCallingIdentity(identityToken);
                }

                if (packageNames != null) {
                    state.configureForFindAccessibilityNodeInfoResult(packageNames);
                    return getFindAccessibilityNodeInfosResultAndClear(state);
                }
            } else {
                if (DEBUG) {
                    Log.w(LOG_TAG, "No connection for connection id: " + connectionId);
                }
            }
        } catch (RemoteException re) {
            Log.w(LOG_TAG, "Error while calling remote"
                    + " findAccessibilityNodeInfoByViewIdInActiveWindow", re);
        } finally {
            clearInteraction(state);
        }
        return Collections.emptyList();
    }

    /**
     * Takes a screenshot of the window with the provided {@code accessibilityWindowId} and
     * returns the answer asynchronously. This async behavior is similar to {@link
     * AccessibilityService#takeScreenshot} but unlike other methods in this class which perform
     * synchronous waiting in the AccessibilityService client.
     *
     * @see AccessibilityService#takeScreenshotOfWindow
     */
    public void takeScreenshotOfWindow(int connectionId, int accessibilityWindowId,
            @NonNull @CallbackExecutor Executor executor,
            @NonNull AccessibilityService.TakeScreenshotCallback callback) {
        InteractionState state = null;

        synchronized (mInstanceLock) {
            try {
                IAccessibilityServiceConnection connection = getConnection(connectionId);
                if (connection == null) {
                    executor.execute(() -> callback.onFailure(
                            AccessibilityService.ERROR_TAKE_SCREENSHOT_INTERNAL_ERROR));
                    return;
                }
                final long identityToken = Binder.clearCallingIdentity();
                try {
                    state = startNewInteraction(connectionId, connection,
                            "takeScreenshotOfWindow", false);
                    final int interactionId = state.mInteractionId;
                    state.mTakeScreenshotCallback = Pair.create(executor, callback);
                    // Create a ScreenCaptureListener to receive the screenshot directly from
                    // SurfaceFlinger instead of requiring an extra IPC from the app:
                    //   A11yService -> App -> SurfaceFlinger -> A11yService
                    ScreenCaptureInternal.ScreenCaptureListener listener =
                            new ScreenCaptureInternal.ScreenCaptureListener(
                                    (screenshot, status) -> {
                                        if (status != 0) {
                                            sendTakeScreenshotOfWindowError(
                                                    AccessibilityService
                                                            .ERROR_TAKE_SCREENSHOT_INTERNAL_ERROR,
                                                    interactionId);
                                        } else {
                                            sendWindowScreenshotSuccess(screenshot, interactionId);
                                        }
                                    });
                    connection.takeScreenshotOfWindow(
                            accessibilityWindowId, interactionId, listener, this);
                    mMainHandler.postDelayed(() -> {
                        sendTakeScreenshotOfWindowError(
                                AccessibilityService.ERROR_TAKE_SCREENSHOT_INTERNAL_ERROR,
                                interactionId);
                    }, TIMEOUT_INTERACTION_MILLIS);
                } finally {
                    Binder.restoreCallingIdentity(identityToken);
                }
            } catch (RemoteException re) {
                clearInteraction(state);
                executor.execute(() -> callback.onFailure(
                        AccessibilityService.ERROR_TAKE_SCREENSHOT_INTERNAL_ERROR));
            }
        }
    }

    /**
     * Finds {@link AccessibilityNodeInfo}s by View text. The match is case
     * insensitive containment. The search is performed in the window whose
     * id is specified and starts from the node whose accessibility id is
     * specified.
     *
     * @param connectionId The id of a connection for interacting with the system.
     * @param accessibilityWindowId A unique window id. Use
     *     {@link android.view.accessibility.AccessibilityWindowInfo#ACTIVE_WINDOW_ID}
     *     to query the currently active window.
     * @param accessibilityNodeId A unique view id or virtual descendant id from
     *     where to start the search. Use
     *     {@link android.view.accessibility.AccessibilityNodeInfo#ROOT_NODE_ID}
     *     to start from the root.
     * @param text The searched text.
     * @return A list of found {@link AccessibilityNodeInfo}s.
     */
    public List<AccessibilityNodeInfo> findAccessibilityNodeInfosByText(int connectionId,
            int accessibilityWindowId, long accessibilityNodeId, String text) {
        final String loggingMethod = "findAccessibilityNodeInfosByText";
        InteractionState state = null;

        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection != null) {
                state = startNewInteraction(connectionId, connection, loggingMethod, false);
                if (shouldTraceClient()) {
                    logTraceClient(connection, loggingMethod,
                            "InteractionId:" + state.mInteractionId + "connectionId="
                            + connectionId + ";accessibilityWindowId=" + accessibilityWindowId
                            + ";accessibilityNodeId=" + accessibilityNodeId + ";text=" + text);
                }
                final String[] packageNames;
                final long identityToken = Binder.clearCallingIdentity();
                try {
                    packageNames = connection.findAccessibilityNodeInfosByText(
                            accessibilityWindowId, accessibilityNodeId, text, state.mInteractionId,
                            this, Thread.currentThread().getId());
                } finally {
                    Binder.restoreCallingIdentity(identityToken);
                }

                if (packageNames != null) {
                    state.configureForFindAccessibilityNodeInfoResult(packageNames);
                    return getFindAccessibilityNodeInfosResultAndClear(state);
                }
            } else {
                if (DEBUG) {
                    Log.w(LOG_TAG, "No connection for connection id: " + connectionId);
                }
            }
        } catch (RemoteException re) {
            Log.w(LOG_TAG, "Error while calling remote"
                    + " findAccessibilityNodeInfosByViewText", re);
        } finally {
            clearInteraction(state);
        }
        return Collections.emptyList();
    }

    /**
     * Finds the {@link AccessibilityNodeInfo} that has the
     * specified focus type. The search is performed in the window whose id is specified
     * and starts from the node whose accessibility id is specified.
     *
     * @param connectionId The id of a connection for interacting with the system.
     * @param accessibilityWindowId A unique window id. Use
     *     {@link AccessibilityWindowInfo#ANY_WINDOW_ID} to query all
     *     windows
     * @param accessibilityNodeId A unique view id or virtual descendant id from
     *     where to start the search. Use
     *     {@link AccessibilityNodeInfo#ROOT_NODE_ID}
     *     to start from the root.
     * @param focusType The focus type.
     * @return The accessibility focused {@link AccessibilityNodeInfo}.
     */
    @SuppressLint("LongLogTag")
    public AccessibilityNodeInfo findFocus(int connectionId, int accessibilityWindowId,
            long accessibilityNodeId, int focusType) {
        final String loggingMethod = "findFocus";
        InteractionState state = null;

        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection != null) {
                AccessibilityCache cache = getCache(connectionId);
                if (cache != null) {
                    AccessibilityNodeInfo cachedInfo = cache.getFocus(focusType,
                            accessibilityNodeId, accessibilityWindowId);
                    if (cachedInfo != null) {
                        if (DEBUG) {
                            Log.i(LOG_TAG, "Focused node cache hit retrieved"
                                    + idToString(cachedInfo.getRealWindowId(),
                                    cachedInfo.getSourceNodeId()));
                        }
                        return cachedInfo;
                    }
                    if (DEBUG) {
                        Log.i(LOG_TAG, "Focused node cache miss with "
                                + idToString(accessibilityWindowId, accessibilityNodeId));
                    }
                } else {
                    if (DEBUG) {
                        Log.w(LOG_TAG, "Cache is null for connection id: " + connectionId);
                    }
                }
                state = startNewInteraction(connectionId, connection, loggingMethod, false);
                if (shouldTraceClient()) {
                    logTraceClient(connection, loggingMethod,
                            "InteractionId:" + state.mInteractionId + "connectionId="
                            + connectionId + ";accessibilityWindowId=" + accessibilityWindowId
                            + ";accessibilityNodeId=" + accessibilityNodeId + ";focusType="
                            + focusType);
                }
                final String[] packageNames;
                final long identityToken = Binder.clearCallingIdentity();
                try {
                    packageNames = connection.findFocus(accessibilityWindowId,
                            accessibilityNodeId, focusType, state.mInteractionId, this,
                            Thread.currentThread().getId());
                } finally {
                    Binder.restoreCallingIdentity(identityToken);
                }

                if (packageNames != null) {
                    state.configureForFindAccessibilityNodeInfoResult(packageNames);
                    return getFindAccessibilityNodeInfoResultAndClear(state, false);
                }
            } else {
                if (DEBUG) {
                    Log.w(LOG_TAG, "No connection for connection id: " + connectionId);
                }
            }
        } catch (RemoteException re) {
            Log.w(LOG_TAG, "Error while calling remote findFocus", re);
        } finally {
            clearInteraction(state);
        }
        return null;
    }

    /**
     * Finds the accessibility focused {@link android.view.accessibility.AccessibilityNodeInfo}.
     * The search is performed in the window whose id is specified and starts from the
     * node whose accessibility id is specified.
     *
     * @param connectionId The id of a connection for interacting with the system.
     * @param accessibilityWindowId A unique window id. Use
     *     {@link android.view.accessibility.AccessibilityWindowInfo#ACTIVE_WINDOW_ID}
     *     to query the currently active window.
     * @param accessibilityNodeId A unique view id or virtual descendant id from
     *     where to start the search. Use
     *     {@link android.view.accessibility.AccessibilityNodeInfo#ROOT_NODE_ID}
     *     to start from the root.
     * @param direction The direction in which to search for focusable.
     * @return The accessibility focused {@link AccessibilityNodeInfo}.
     */
    public AccessibilityNodeInfo focusSearch(int connectionId, int accessibilityWindowId,
            long accessibilityNodeId, int direction) {
        final String loggingMethod = "focusSearch";
        InteractionState state = null;

        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection != null) {
                state = startNewInteraction(connectionId, connection, loggingMethod, false);
                if (shouldTraceClient()) {
                    logTraceClient(connection, loggingMethod,
                            "InteractionId:" + state.mInteractionId + "connectionId="
                            + connectionId + ";accessibilityWindowId=" + accessibilityWindowId
                            + ";accessibilityNodeId=" + accessibilityNodeId + ";direction="
                            + direction);
                }
                final String[] packageNames;
                final long identityToken = Binder.clearCallingIdentity();
                try {
                    packageNames = connection.focusSearch(accessibilityWindowId,
                            accessibilityNodeId, direction, state.mInteractionId, this,
                            Thread.currentThread().getId());
                } finally {
                    Binder.restoreCallingIdentity(identityToken);
                }

                if (packageNames != null) {
                    state.configureForFindAccessibilityNodeInfoResult(packageNames);
                    return getFindAccessibilityNodeInfoResultAndClear(state, false);
                }
            } else {
                if (DEBUG) {
                    Log.w(LOG_TAG, "No connection for connection id: " + connectionId);
                }
            }
        } catch (RemoteException re) {
            Log.w(LOG_TAG, "Error while calling remote accessibilityFocusSearch", re);
        } finally {
            clearInteraction(state);
        }
        return null;
    }

    /**
     * Performs an accessibility action on an {@link AccessibilityNodeInfo}.
     *
     * @param connectionId The id of a connection for interacting with the system.
     * @param accessibilityWindowId A unique window id. Use
     *     {@link android.view.accessibility.AccessibilityWindowInfo#ACTIVE_WINDOW_ID}
     *     to query the currently active window.
     * @param accessibilityNodeId A unique view id or virtual descendant id from
     *     where to start the search. Use
     *     {@link android.view.accessibility.AccessibilityNodeInfo#ROOT_NODE_ID}
     *     to start from the root.
     * @param action The action to perform.
     * @param arguments Optional action arguments.
     * @return Whether the action was performed.
     */
    public boolean performAccessibilityAction(int connectionId, int accessibilityWindowId,
            long accessibilityNodeId, int action, Bundle arguments) {
        final String loggingMethod = "performAccessibilityAction";
        InteractionState state = null;

        try {
            IAccessibilityServiceConnection connection = getConnection(connectionId);
            if (connection != null) {
                state = startNewInteraction(connectionId, connection, loggingMethod, false);
                if (shouldTraceClient()) {
                    logTraceClient(connection, loggingMethod,
                            "InteractionId:" + state.mInteractionId + "connectionId="
                            + connectionId + ";accessibilityWindowId=" + accessibilityWindowId
                            + ";accessibilityNodeId=" + accessibilityNodeId + ";action=" + action
                            + ";arguments=" + arguments);
                }
                final boolean success;
                final long identityToken = Binder.clearCallingIdentity();
                try {
                    success = connection.performAccessibilityAction(
                            accessibilityWindowId, accessibilityNodeId, action, arguments,
                            state.mInteractionId, this, Thread.currentThread().getId());
                } finally {
                    Binder.restoreCallingIdentity(identityToken);
                }

                if (success) {
                    return getPerformAccessibilityActionResultAndClear(state);
                }
            } else {
                if (DEBUG) {
                    Log.w(LOG_TAG, "No connection for connection id: " + connectionId);
                }
            }
        } catch (RemoteException re) {
            Log.w(LOG_TAG, "Error while calling remote performAccessibilityAction", re);
        } finally {
            clearInteraction(state);
        }
        return false;
    }

    /**
     * Clears the cache associated with {@code connectionId}
     * @param connectionId the connection id
     */
    @UnsupportedAppUsage(maxTargetSdk = S, publicAlternatives =
            "{@link android.accessibilityservice.AccessibilityService#clearCache()}")
    public void clearCache(int connectionId) {
        AccessibilityCache cache = getCache(connectionId);
        if (cache == null) {
            return;
        }
        cache.clear();
    }

    /**
     * Informs the cache associated with {@code connectionId} of {@code event}
     * @param event the event
     * @param connectionId the connection id
     */
    public void onAccessibilityEvent(AccessibilityEvent event, int connectionId) {
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                updateScrollingWindow(event.getRealWindowId(), SystemClock.uptimeMillis());
                break;
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                if (event.getWindowChanges() == AccessibilityEvent.WINDOWS_CHANGE_REMOVED) {
                    deleteScrollingWindow(event.getRealWindowId());
                }
                break;
            default:
                break;
        }
        AccessibilityCache cache = getCache(connectionId);
        if (cache == null) {
            if (DEBUG) {
                Log.w(LOG_TAG, "Cache is null for connection id: " + connectionId);
            }
            return;
        }
        cache.onAccessibilityEvent(event);
    }

    /**
     * Gets the result of an async request that returns an {@link AccessibilityNodeInfo}.
     *
     * @param state The interaction state of the request, to match with the results.
     * @param setUpForPrefetchResult Whether the interaction state should be used
     *                               to wait for incoming prefetch results.
     * @return The result {@link AccessibilityNodeInfo}.
     */
    private AccessibilityNodeInfo getFindAccessibilityNodeInfoResultAndClear(
            InteractionState state, boolean setUpForPrefetchResult) {
        AccessibilityNodeInfo result;
        boolean hasPrefetchResults = false;
        synchronized (mInstanceLock) {
            final boolean success = waitForResultTimedLocked(state.mInteractionId);
            result = success ? state.mFindA11yNodeInfoResult : null;

            // Consider prefetch results for this interaction
            // only if the current prefetch interaction is older (or there is none).
            if (setUpForPrefetchResult
                    && isCurrentPrefetchInteractionOlderLocked(state.mInteractionId)) {
                if (state.mPrefetchResult != null && !state.mPrefetchResult.isEmpty()) {
                    // It's possible for prefetch results to arrive while we were waiting.
                    // If so, we'll process them later on in this function.
                    hasPrefetchResults = true;
                } else if (result != null) {
                    // Otherwise, store this state to be referenced later
                    mPrefetchInteraction = state;
                }
            }
        }

        if (shouldTraceCallback()) {
            logTraceCallback(state, String.valueOf(result));
        }

        finalizeAndCacheAccessibilityNodeInfo(
                result, state.mConnectionId, state.mBypassCache, state.mPackageNames);
        if (hasPrefetchResults) {
            processPrefetchAccessibilityNodeInfoResult(state);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RequiresNoPermission
    public void setFindAccessibilityNodeInfoResult(AccessibilityNodeInfo info,
                int interactionId) {
        synchronized (mInstanceLock) {
            InteractionState state = mActiveInteractions.get(interactionId);
            if (state != null) {
                state.mFindA11yNodeInfoResult = info;
                state.mIsCompleted = true;
                state.mCallingUid = Binder.getCallingUid();
                state.mCallStackOfCallback = new ArrayList<>(
                        Arrays.asList(Thread.currentThread().getStackTrace()));
            }
            mInstanceLock.notifyAll();
        }
    }

    /**
     * Gets the result of an async request that returns {@link AccessibilityNodeInfo}s.
     *
     * @param state The interaction state of the request, to match with the results.
     * @return The result {@link AccessibilityNodeInfo}s.
     */
    private List<AccessibilityNodeInfo> getFindAccessibilityNodeInfosResultAndClear(
            InteractionState state) {
        List<AccessibilityNodeInfo> result = Collections.emptyList();
        synchronized (mInstanceLock) {
            final boolean success = waitForResultTimedLocked(state.mInteractionId);
            if (success) {
                result = state.mFindA11yNodeInfosResult;
            }
        }

        if (shouldTraceCallback()) {
            logTraceCallback(state, result.toString());
        }

        if (Build.IS_DEBUGGABLE && CHECK_INTEGRITY) {
            checkFindAccessibilityNodeInfoResultIntegrity(result);
        }

        finalizeAndCacheAccessibilityNodeInfos(result, state.mConnectionId,
                state.mBypassCache, state.mPackageNames);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RequiresNoPermission
    public void setFindAccessibilityNodeInfosResult(List<AccessibilityNodeInfo> infos,
                int interactionId) {
        synchronized (mInstanceLock) {
            InteractionState state = mActiveInteractions.get(interactionId);
            if (state != null) {
                if (infos != null) {
                    // If the call is not an IPC, i.e. it is made from the same process, we need to
                    // instantiate new result list to avoid passing internal instances to clients.
                    final boolean isIpcCall = (Binder.getCallingPid() != Process.myPid());
                    if (!isIpcCall) {
                        state.mFindA11yNodeInfosResult = new ArrayList<>(infos);
                    } else {
                        state.mFindA11yNodeInfosResult = infos;
                    }
                } else {
                    state.mFindA11yNodeInfosResult = Collections.emptyList();
                }
                state.mIsCompleted = true;
                state.mCallingUid = Binder.getCallingUid();
                state.mCallStackOfCallback = new ArrayList<StackTraceElement>(
                    Arrays.asList(Thread.currentThread().getStackTrace()));
            }
            mInstanceLock.notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RequiresNoPermission
    public void setPrefetchAccessibilityNodeInfoResult(@NonNull List<AccessibilityNodeInfo> infos,
                                                       int interactionId) {
        if (infos.isEmpty()) {
            return;
        }

        synchronized (mInstanceLock) {
            if (mPrefetchInteraction != null
                    && mPrefetchInteraction.mInteractionId == interactionId) {
                // We can process the results now!
                InteractionState prefetchState = mPrefetchInteraction;
                prefetchState.mPrefetchResult = infos;
                processPrefetchAccessibilityNodeInfoResult(prefetchState);
                mPrefetchInteraction = null;
            } else {
                // The results came early.
                // Store them in the corresponding active interaction if possible.
                // When that interaction's results are processed,
                // the prefetch results will also be processed
                // (but only if the interaction was expecting prefetch results).
                InteractionState state = mActiveInteractions.get(interactionId);
                if (state != null) {
                    state.mPrefetchResult = List.copyOf(infos);
                } else if (DEBUG) {
                    // If there's no active interaction corresponding to the id,
                    // then the prefetch results are truly lost.
                    Log.w(LOG_TAG, "Prefetching for interaction with id " + interactionId
                            + " dropped " + infos.size() + " nodes");
                }
            }
        }
    }

    @GuardedBy("mInstanceLock")
    private boolean isCurrentPrefetchInteractionOlderLocked(int interactionId) {
        return mPrefetchInteraction == null || mPrefetchInteraction.mInteractionId < interactionId;
    }

    private void processPrefetchAccessibilityNodeInfoResult(InteractionState state) {
        finalizeAndCacheAccessibilityNodeInfos(
                state.mPrefetchResult, state.mConnectionId, false,
                state.mPackageNames);
        if (shouldTraceCallback()) {
            logTrace(state.mConnection,
                    "setPrefetchAccessibilityNodeInfoResult",
                    "InteractionId:" + state.mInteractionId + ";connectionId="
                            + state.mConnectionId + ";Result: " + state.mPrefetchResult,
                    Binder.getCallingUid(),
                    Arrays.asList(Thread.currentThread().getStackTrace()),
                    new HashSet<>(Collections.singletonList("getStackTrace")),
                    FLAGS_ACCESSIBILITY_INTERACTION_CONNECTION_CALLBACK);
        }
    }

    /**
     * Gets the result of a request to perform an accessibility action.
     *
     * @param state The interaction state of the request, to match with the results.
     * @return Whether the action was performed.
     */
    private boolean getPerformAccessibilityActionResultAndClear(InteractionState state) {
        boolean result;
        synchronized (mInstanceLock) {
            final boolean success = waitForResultTimedLocked(state.mInteractionId);
            result = success && state.mPerformA11yActionResult;
        }

        if (shouldTraceCallback()) {
            logTraceCallback(state, String.valueOf(result));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @RequiresNoPermission
    public void setPerformAccessibilityActionResult(boolean succeeded, int interactionId) {
        synchronized (mInstanceLock) {
            InteractionState state = mActiveInteractions.get(interactionId);
            if (state != null) {
                state.mPerformA11yActionResult = succeeded;
                state.mIsCompleted = true;
                state.mCallingUid = Binder.getCallingUid();
                state.mCallStackOfCallback = new ArrayList<StackTraceElement>(
                    Arrays.asList(Thread.currentThread().getStackTrace()));
            }
            mInstanceLock.notifyAll();
        }
    }

    /**
     * Sends the result of a window screenshot request to the requesting client.
     *
     * <p>{@link #takeScreenshotOfWindow} does not perform synchronous waiting, so this method does
     * not notify any wait lock.
     */
    private void sendWindowScreenshotSuccess(
            ScreenCaptureInternal.ScreenshotHardwareBuffer screenshot, int interactionId) {
        if (screenshot == null) {
            sendTakeScreenshotOfWindowError(
                    AccessibilityService.ERROR_TAKE_SCREENSHOT_INTERNAL_ERROR, interactionId);
            return;
        }
        synchronized (mInstanceLock) {
            if (mActiveInteractions.contains(interactionId)) {
                final AccessibilityService.ScreenshotResult result =
                        new AccessibilityService.ScreenshotResult(screenshot.getHardwareBuffer(),
                                screenshot.getColorSpace(), SystemClock.uptimeMillis());
                final Pair<Executor, AccessibilityService.TakeScreenshotCallback> pair =
                        mActiveInteractions.get(interactionId).mTakeScreenshotCallback;
                if (pair == null) {
                    return;
                }
                final Executor executor = pair.first;
                final AccessibilityService.TakeScreenshotCallback callback = pair.second;
                executor.execute(() -> callback.onSuccess(result));
                clearInteraction(interactionId);
            }
        }
    }

    /**
     * Sends an error code for a window screenshot request to the requesting client.
     *
     * @param errorCode The error code from {@link AccessibilityService.ScreenshotErrorCode}.
     * @param interactionId The interaction id of the request.
     */
    @Override
    @RequiresNoPermission
    public void sendTakeScreenshotOfWindowError(
            @AccessibilityService.ScreenshotErrorCode int errorCode, int interactionId) {
        synchronized (mInstanceLock) {
            if (mActiveInteractions.contains(interactionId)) {
                final Pair<Executor, AccessibilityService.TakeScreenshotCallback> pair =
                        mActiveInteractions.get(interactionId).mTakeScreenshotCallback;
                if (pair == null) {
                    return;
                }
                final Executor executor = pair.first;
                final AccessibilityService.TakeScreenshotCallback callback = pair.second;
                executor.execute(() -> callback.onFailure(errorCode));
                clearInteraction(interactionId);
            }
        }
    }

    /**
     * Waits up to a given bound for a result of a request and returns it.
     *
     * @param interactionId The interaction id to match the result with the request.
     * @return Whether the result was received.
     */
    @GuardedBy("mInstanceLock")
    private boolean waitForResultTimedLocked(int interactionId) {
        long waitTimeMillis = TIMEOUT_INTERACTION_MILLIS;
        final long startTimeMillis = SystemClock.uptimeMillis();
        while (true) {
            try {
                Message sameProcessMessage = getSameProcessMessageAndClear();
                if (sameProcessMessage != null) {
                    sameProcessMessage.getTarget().handleMessage(sameProcessMessage);
                }

                InteractionState state = mActiveInteractions.get(interactionId);
                if (state == null) {
                    return false; // Interaction was cleared or otherwise replaced.
                }
                if (state.mIsCompleted) {
                    return true;
                }

                final long elapsedTimeMillis = SystemClock.uptimeMillis() - startTimeMillis;
                waitTimeMillis = TIMEOUT_INTERACTION_MILLIS - elapsedTimeMillis;
                if (waitTimeMillis <= 0) {
                    return false;
                }
                mInstanceLock.wait(waitTimeMillis);
            } catch (InterruptedException ie) {
                /* ignore */
            }
        }
    }

    /**
     * Finalize an {@link AccessibilityNodeInfo} before passing it to the client.
     *
     * @param info The info.
     * @param connectionId The id of the connection to the system.
     * @param bypassCache Whether or not to bypass the cache. The node is added to the cache if
     *                    this value is {@code false}
     * @param packageNames The valid package names a node can come from.
     */
    private void finalizeAndCacheAccessibilityNodeInfo(AccessibilityNodeInfo info,
            int connectionId, boolean bypassCache, String[] packageNames) {
        if (info == null) {
            return;
        }

        info.setConnectionId(connectionId);
        // Empty array means any package name is Okay
        if (!ArrayUtils.isEmpty(packageNames)) {
            CharSequence packageName = info.getPackageName();
            if (packageName == null
                    || !ArrayUtils.contains(packageNames, packageName.toString())) {
                // If the node package not one of the valid ones, pick the top one - this
                // is one of the packages running in the introspected UID.
                info.setPackageName(packageNames[0]);
            }
        }
        info.setSealed(true);
        if (!bypassCache) {
            AccessibilityCache cache = getCache(connectionId);
            if (cache == null) {
                if (DEBUG) {
                    Log.w(LOG_TAG, "Cache is null for connection id: " + connectionId);
                }
                return;
            }
            cache.add(info);
        }
    }

    /**
     * Finalize {@link AccessibilityNodeInfo}s before passing them to the client.
     *
     * @param infos The {@link AccessibilityNodeInfo}s.
     * @param connectionId The id of the connection to the system.
     * @param bypassCache Whether or not to bypass the cache. The nodes are added to the cache if
     *                    this value is {@code false}
     * @param packageNames The valid package names a node can come from.
     */
    private void finalizeAndCacheAccessibilityNodeInfos(List<AccessibilityNodeInfo> infos,
            int connectionId, boolean bypassCache, String[] packageNames) {
        if (infos != null) {
            final int infosCount = infos.size();
            for (int i = 0; i < infosCount; i++) {
                AccessibilityNodeInfo info = infos.get(i);
                finalizeAndCacheAccessibilityNodeInfo(info, connectionId,
                        bypassCache, packageNames);
            }
        }
    }

    /**
     * Gets the message stored if the interacted and interacting
     * threads are the same.
     *
     * @return The message.
     */
    private Message getSameProcessMessageAndClear() {
        synchronized (mInstanceLock) {
            Message result = mSameThreadMessage;
            mSameThreadMessage = null;
            return result;
        }
    }

    /**
     * Checks whether the infos are a fully connected tree with no duplicates.
     * This does not perform any operations other than error logging.
     *
     * @param infos The result list to check.
     */
    private void checkFindAccessibilityNodeInfoResultIntegrity(List<AccessibilityNodeInfo> infos) {
        if (infos.size() == 0) {
            return;
        }
        // Find the root node.
        AccessibilityNodeInfo root = infos.get(0);
        final int infoCount = infos.size();
        for (int i = 1; i < infoCount; i++) {
            for (int j = i; j < infoCount; j++) {
                AccessibilityNodeInfo candidate = infos.get(j);
                if (root.getParentNodeId() == candidate.getSourceNodeId()) {
                    root = candidate;
                    break;
                }
            }
        }
        if (root == null) {
            Log.e(LOG_TAG, "No root.");
        }
        // Check for duplicates.
        HashSet<AccessibilityNodeInfo> seen = new HashSet<>();
        Queue<AccessibilityNodeInfo> fringe = new ArrayDeque<>();
        fringe.add(root);
        while (!fringe.isEmpty()) {
            AccessibilityNodeInfo current = fringe.poll();
            if (!seen.add(current)) {
                Log.e(LOG_TAG, "Duplicate node.");
                return;
            }
            final int childCount = current.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final long childId = current.getChildId(i);
                for (int j = 0; j < infoCount; j++) {
                    AccessibilityNodeInfo child = infos.get(j);
                    if (child.getSourceNodeId() == childId) {
                        fringe.add(child);
                    }
                }
            }
        }
        final int disconnectedCount = infos.size() - seen.size();
        if (disconnectedCount > 0) {
            Log.e(LOG_TAG, disconnectedCount + " Disconnected nodes.");
        }
    }

    /**
     * Update scroll event timestamp of a given window.
     *
     * @param windowId The window id.
     * @param uptimeMillis Device uptime millis.
     */
    private void updateScrollingWindow(int windowId, long uptimeMillis) {
        synchronized (sScrollingWindows) {
            sScrollingWindows.put(windowId, uptimeMillis);
        }
    }

    /**
     * Remove a window from the scrolling windows list.
     *
     * @param windowId The window id.
     */
    private void deleteScrollingWindow(int windowId) {
        synchronized (sScrollingWindows) {
            sScrollingWindows.delete(windowId);
        }
    }

    /**
     * Whether or not the window is scrolling.
     *
     * @param windowId
     * @return true if it's scrolling.
     */
    private boolean isWindowScrolling(int windowId) {
        synchronized (sScrollingWindows) {
            final long latestScrollingTime = sScrollingWindows.get(windowId);
            if (latestScrollingTime == 0) {
                return false;
            }
            final long currentUptime = SystemClock.uptimeMillis();
            if (currentUptime > (latestScrollingTime + DISABLE_PREFETCHING_FOR_SCROLLING_MILLIS)) {
                sScrollingWindows.delete(windowId);
                return false;
            }
        }
        return true;
    }

    private boolean shouldTraceClient() {
        return (mAccessibilityManager != null)
                && mAccessibilityManager.isA11yInteractionClientTraceEnabled();
    }

    private boolean shouldTraceCallback() {
        return (mAccessibilityManager != null)
                && mAccessibilityManager.isA11yInteractionConnectionCBTraceEnabled();
    }

    private void logTrace(
            IAccessibilityServiceConnection connection, String method, String params,
            int callingUid, List<StackTraceElement> callStack, HashSet<String> ignoreSet,
            long logTypes) {
        try {
            Bundle b = new Bundle();
            b.putSerializable(CALL_STACK, new ArrayList<StackTraceElement>(callStack));
            if (ignoreSet != null) {
                b.putSerializable(IGNORE_CALL_STACK, ignoreSet);
            }
            connection.logTrace(SystemClock.elapsedRealtimeNanos(),
                    LOG_TAG + "." + method,
                    logTypes, params, Process.myPid(), Thread.currentThread().getId(),
                    callingUid, b);
        } catch (RemoteException e) {
            Log.e(LOG_TAG, "Failed to log trace. " + e);
        }
    }

    private void logTraceCallback(InteractionState state, String result) {
        String params = "InteractionId:" + state.mInteractionId + ";connectionId="
                + state.mConnectionId + ";Result: " + result;

        logTrace(state.mConnection, state.mLoggingMethod + " callback", params,
                state.mCallingUid, state.mCallStackOfCallback,
                new HashSet<String>(Arrays.asList("getStackTrace")),
                FLAGS_ACCESSIBILITY_INTERACTION_CONNECTION_CALLBACK);
    }

    private void logTraceClient(
            IAccessibilityServiceConnection connection, String method, String params) {
        logTrace(
                connection,
                method,
                params,
                Binder.getCallingUid(),
                Arrays.asList(Thread.currentThread().getStackTrace()),
                new HashSet<String>(Arrays.asList("getStackTrace", "logTraceClient")),
                FLAGS_ACCESSIBILITY_INTERACTION_CLIENT);
    }

    /** Attaches an accessibility overlay to the specified window. */
    public void attachAccessibilityOverlayToWindow(
            int connectionId,
            int accessibilityWindowId,
            SurfaceControl sc,
            @NonNull @CallbackExecutor Executor executor,
            @NonNull IntConsumer callback) {
        InteractionState state = null;

        synchronized (mInstanceLock) {
            try {
                IAccessibilityServiceConnection connection = getConnection(connectionId);
                if (connection == null) {
                    executor.execute(
                            () ->
                                    callback.accept(
                                            AccessibilityService.OVERLAY_RESULT_INTERNAL_ERROR));
                    return;
                }
                state = startNewInteraction(
                        connectionId, connection, "attachOverlayToWindow", false);
                final int interactionId = state.mInteractionId;
                state.mAttachA11yOverlayCallback = Pair.create(executor, callback);
                connection.attachAccessibilityOverlayToWindow(
                        interactionId, accessibilityWindowId, sc, this);
                mMainHandler.postDelayed(
                        () -> {
                            // Notify failure if we still haven't sent a response after timeout.
                            sendAttachOverlayResult(
                                    AccessibilityService.OVERLAY_RESULT_INTERNAL_ERROR,
                                    interactionId);
                        },
                        TIMEOUT_INTERACTION_MILLIS);
            } catch (RemoteException re) {
                clearInteraction(state);
                re.rethrowFromSystemServer();
            }
        }
    }

    /** Attaches an accessibility overlay to the specified display. */
    public void attachAccessibilityOverlayToDisplay(
            int connectionId,
            int displayId,
            SurfaceControl sc,
            @NonNull @CallbackExecutor Executor executor,
            @NonNull IntConsumer callback) {
        synchronized (mInstanceLock) {
            InteractionState state = null;

            try {
                IAccessibilityServiceConnection connection = getConnection(connectionId);
                if (connection == null) {
                    executor.execute(
                            () ->
                                    callback.accept(
                                            AccessibilityService.OVERLAY_RESULT_INTERNAL_ERROR));
                    return;
                }
                state = startNewInteraction(
                        connectionId, connection, "attachOverlayToDisplay", false);
                final int interactionId = state.mInteractionId;
                state.mAttachA11yOverlayCallback = Pair.create(executor, callback);
                connection.attachAccessibilityOverlayToDisplay(
                        interactionId, displayId, sc, this);
                mMainHandler.postDelayed(
                        () -> {
                            // Notify failure if we still haven't sent a response after timeout.
                            sendAttachOverlayResult(
                                    AccessibilityService.OVERLAY_RESULT_INTERNAL_ERROR,
                                    interactionId);
                        },
                        TIMEOUT_INTERACTION_MILLIS);
            } catch (RemoteException re) {
                clearInteraction(state);
                re.rethrowFromSystemServer();
            }
        }
    }

    /**
     * Sends a result code for an attach window overlay request to the requesting client.
     *
     * @param result The result code from {@link AccessibilityService.OverlayResult}.
     * @param interactionId The interaction id of the request.
     */
    @Override
    @RequiresNoPermission
    public void sendAttachOverlayResult(
            @AccessibilityService.AttachOverlayResult int result, int interactionId) {
        synchronized (mInstanceLock) {
            if (mActiveInteractions.contains(interactionId)) {
                final Pair<Executor, IntConsumer> pair =
                        mActiveInteractions.get(interactionId).mAttachA11yOverlayCallback;
                if (pair == null) {
                    return;
                }

                // If pair is not null but callback flag is off,
                // we still need to clear the interaction before stopping.
                if (!Flags.a11yOverlayCallbacks()) {
                    clearInteraction(interactionId);
                    return;
                }

                final Executor executor = pair.first;
                final IntConsumer callback = pair.second;
                if (executor == null || callback == null) {
                    return;
                }
                executor.execute(() -> callback.accept(result));
                clearInteraction(interactionId);
            }
        }
    }

    /**
     * Starts a new active interaction to be tracked.
     * Make sure that any code path that starts an interaction eventually calls
     * {@link AccessibilityInteractionClient#clearInteraction}, otherwise it will leak memory.
     * This should include if the interaction was completed normally, if it was not completed,
     * and if an exception was thrown.
     */
    private InteractionState startNewInteraction(
            int connectionId, IAccessibilityServiceConnection connection, String loggingMethod,
            boolean bypassCache) {
        int interactionId = mInteractionIdCounter.getAndIncrement();
        InteractionState state = new InteractionState(
                interactionId, connectionId, connection, loggingMethod, bypassCache);
        synchronized (mInstanceLock) {
            mActiveInteractions.put(interactionId, state);
        }
        return state;
    }

    /**
     * Frees the provided {@link InteractionState} from the tracked active interactions.
     * The state can still be read from as long as a scope retains a local reference to it.
     */
    private void clearInteraction(InteractionState state) {
        if (state == null) {
            return;
        }
        clearInteraction(state.mInteractionId);
    }

    /**
     * Frees the state corresponding to {@code interactionId} from the tracked active interactions.
     * The state can still be read from as long as a scope retains a local reference to it.
     */
    private void clearInteraction(int interactionId) {
        synchronized (mInstanceLock) {
            mActiveInteractions.remove(interactionId);
        }
    }

    /**
     * Data object containing most of the information needed to perform an interaction
     * and process its results.
     * <aside class="note">
     * <b>Note:</b> For {@code find} interactions, call
     * {@link InteractionState#configureForFindAccessibilityNodeInfoResult(String[])}
     * once the {@code packageNames} for the interaction are known. </aside>
     */
    private static class InteractionState {
        final int mInteractionId;
        final int mConnectionId;
        final IAccessibilityServiceConnection mConnection;
        String[] mPackageNames;
        final boolean mBypassCache;
        final String mLoggingMethod;
        Pair<Executor, IntConsumer> mAttachA11yOverlayCallback;
        Pair<Executor, AccessibilityService.TakeScreenshotCallback> mTakeScreenshotCallback;

        AccessibilityNodeInfo mFindA11yNodeInfoResult;
        List<AccessibilityNodeInfo> mFindA11yNodeInfosResult;
        List<AccessibilityNodeInfo> mPrefetchResult;
        boolean mPerformA11yActionResult;
        boolean mIsCompleted;

        int mCallingUid = Process.INVALID_UID;
        // call stack for IAccessibilityInteractionConnectionCallback APIs. These callback APIs are
        // shared by multiple requests APIs in IAccessibilityServiceConnection. To correctly log the
        // request API which triggers the callback, we log trace entries for callback after the
        // request API thread waiting for the callback returns. To log the correct callback stack in
        // the request API thread, we save the callback stack in this member variables.
        List<StackTraceElement> mCallStackOfCallback = List.of();

        InteractionState(int interactionId, int connectionId,
                IAccessibilityServiceConnection connection,
                String loggingMethod, boolean bypassCache) {
            mInteractionId = interactionId;
            mConnectionId = connectionId;
            mConnection = connection;
            mLoggingMethod = loggingMethod;
            mBypassCache = bypassCache;
        }

        /**
         * Provides extra information needed for logging and caching nodes
         * related to a {@code find} interaction.
         */
        void configureForFindAccessibilityNodeInfoResult(String[] packageNames) {
            mPackageNames = packageNames;
        }
    }
}
