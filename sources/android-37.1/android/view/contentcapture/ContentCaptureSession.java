/*
 * Copyright (C) 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.view.contentcapture;

import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static android.view.contentcapture.ContentCaptureHelper.sDebug;
import static android.view.contentcapture.ContentCaptureHelper.sVerbose;
import static android.view.contentcapture.ContentCaptureManager.NO_SESSION_ID;
import static android.view.contentcapture.flags.Flags.FLAG_CCAPI_BAKLAVA_ENABLED;
import static android.view.contentcapture.flags.Flags.FLAG_CCAPI_NEXT_ENABLED;
import static android.view.contentcapture.flags.Flags.FLAG_CONTENT_INTERACTION_API_ENABLED;

import android.annotation.CallSuper;
import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.app.compat.CompatChanges;
import android.compat.annotation.ChangeId;
import android.compat.annotation.EnabledSince;
import android.content.ComponentName;
import android.graphics.Insets;
import android.graphics.Rect;
import android.os.IBinder;
import android.util.DebugUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewStructure;
import android.view.autofill.AutofillId;
import android.view.contentcapture.ViewNode.ViewStructureImpl;

import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.IResultReceiver;
import com.android.internal.util.ArrayUtils;
import com.android.internal.util.Preconditions;

import java.io.PrintWriter;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Session used when notifying the Android system about events associated with views.
 */
public abstract class ContentCaptureSession implements AutoCloseable {

    private static final String TAG = ContentCaptureSession.class.getSimpleName();

    // TODO(b/158778794): to make the session ids truly globally unique across
    //  processes, we may need to explore other options.
    private static final SecureRandom ID_GENERATOR = new SecureRandom();

    /**
     * Content Capture Version 1.
     *
     * <p>The first Content Capture version is an information rich, event based system that
     * attempts to transfer the current view hierarchy from the application process to a system
     * intelligence service through periodic event dumps, e.g. {@link #flush()}.
     */
    @FlaggedApi(FLAG_CCAPI_NEXT_ENABLED)
    public static final int VERSION_1 = 1;

    /**
     * Content Capture Version 2.
     *
     * <p>The second and newer Content Capture takes a more focused and leaner approach to passing
     * the View hierarchy from the application to a system intelligence service. The required
     * information is mostly limited to text on screen and bounding boxes of these text nodes.
     * This version of Content Capture is also significantly more efficient at resource usage
     * and recommended to be the default one for various system intelligence services to use.
     */
    @FlaggedApi(FLAG_CCAPI_NEXT_ENABLED)
    public static final int VERSION_2 = 2;

    @Hide
    @IntDef(prefix = {"VERSION_"}, value = {
            VERSION_1,
            VERSION_2
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ContentCaptureVersion {}

    /**
     * Name of the {@link IResultReceiver} extra used to pass the binder interface to the service.
     */
    @Hide
    public static final String EXTRA_BINDER = "binder";

    /**
     * Name of the {@link IResultReceiver} extra used to pass the content capture enabled state.
     */
    @Hide
    public static final String EXTRA_ENABLED_STATE = "enabled";

    /**
     * Initial state, when there is no session.
     */
    @Hide
    // NOTE: not prefixed by STATE_ so it's not printed on getStateAsString()
    public static final int UNKNOWN_STATE = 0x0;

    /**
     * Service's startSession() was called, but server didn't confirm it was created yet.
     */
    @Hide
    public static final int STATE_WAITING_FOR_SERVER = 0x1;

    /**
     * Session is active.
     */
    @Hide
    public static final int STATE_ACTIVE = 0x2;

    /**
     * Session is disabled because there is no service for this user.
     */
    @Hide
    public static final int STATE_DISABLED = 0x4;

    /**
     * Session is disabled because its id already existed on server.
     */
    @Hide
    public static final int STATE_DUPLICATED_ID = 0x8;

    /**
     * Session is disabled because service is not set for user.
     */
    @Hide
    public static final int STATE_NO_SERVICE = 0x10;

    /**
     * Session is disabled by FLAG_SECURE
     */
    @Hide
    public static final int STATE_FLAG_SECURE = 0x20;

    /**
     * Session is disabled manually by the specific app
     * (through {@link ContentCaptureManager#setContentCaptureEnabled(boolean)}).
     */
    @Hide
    public static final int STATE_BY_APP = 0x40;

    /**
     * Session is disabled because session start was never replied.
     */
    @Hide
    public static final int STATE_NO_RESPONSE = 0x80;

    /**
     * Session is disabled because an internal error.
     */
    @Hide
    public static final int STATE_INTERNAL_ERROR = 0x100;

    /**
     * Session is disabled because service didn't allowlist package or activity.
     */
    @Hide
    public static final int STATE_NOT_WHITELISTED = 0x200;

    /**
     * Session is disabled because the service died.
     */
    @Hide
    public static final int STATE_SERVICE_DIED = 0x400;

    /**
     * Session is disabled because the service package is being udpated.
     */
    @Hide
    public static final int STATE_SERVICE_UPDATING = 0x800;

    /**
     * Session is enabled, after the service died and came back to live.
     */
    @Hide
    public static final int STATE_SERVICE_RESURRECTED = 0x1000;

    private static final int INITIAL_CHILDREN_CAPACITY = 5;

    @Hide
    public static final int FLUSH_REASON_FULL = 1;

    @Hide
    public static final int FLUSH_REASON_VIEW_ROOT_ENTERED = 2;

    @Hide
    public static final int FLUSH_REASON_SESSION_STARTED = 3;

    @Hide
    public static final int FLUSH_REASON_SESSION_FINISHED = 4;

    @Hide
    public static final int FLUSH_REASON_IDLE_TIMEOUT = 5;

    @Hide
    public static final int FLUSH_REASON_TEXT_CHANGE_TIMEOUT = 6;

    @Hide
    public static final int FLUSH_REASON_SESSION_CONNECTED = 7;

    @Hide
    public static final int FLUSH_REASON_FORCE_FLUSH = 8;

    @Hide
    public static final int FLUSH_REASON_VIEW_TREE_APPEARING = 9;

    @Hide
    public static final int FLUSH_REASON_VIEW_TREE_APPEARED = 10;

    /**
     * After {@link android.os.Build.VERSION_CODES#UPSIDE_DOWN_CAKE},
     * {@link #notifyViewsDisappeared(AutofillId, long[])} wraps
     * the virtual children with a pair of view tree appearing and view tree appeared events.
     */
    @ChangeId
    @EnabledSince(targetSdkVersion = UPSIDE_DOWN_CAKE)
    static final long NOTIFY_NODES_DISAPPEAR_NOW_SENDS_TREE_EVENTS = 258825825L;

    @Hide
    @IntDef(
            prefix = {"FLUSH_REASON_"},
            value = {
                FLUSH_REASON_FULL,
                FLUSH_REASON_VIEW_ROOT_ENTERED,
                FLUSH_REASON_SESSION_STARTED,
                FLUSH_REASON_SESSION_FINISHED,
                FLUSH_REASON_IDLE_TIMEOUT,
                FLUSH_REASON_TEXT_CHANGE_TIMEOUT,
                FLUSH_REASON_SESSION_CONNECTED,
                FLUSH_REASON_FORCE_FLUSH,
                FLUSH_REASON_VIEW_TREE_APPEARING,
                FLUSH_REASON_VIEW_TREE_APPEARED
            })
    @Retention(RetentionPolicy.SOURCE)
    public @interface FlushReason {}

    private final Object mLock = new Object();

    /**
     * Guard use to ignore events after it's destroyed.
     */
    private volatile boolean mDestroyed;

    @Hide
    @Nullable
    protected final int mId;

    private int mState = UNKNOWN_STATE;

    // Lazily created on demand.
    private ContentCaptureSessionId mContentCaptureSessionId;

    /**
     * {@link ContentCaptureContext} set by client, or {@code null} when it's the
     * {@link ContentCaptureManager#getMainContentCaptureSession() default session} for the
     * context.
     */
    @Nullable
    private ContentCaptureContext mClientContext;

    /**
     * List of children session.
     */
    @Nullable
    @GuardedBy("mLock")
    private ArrayList<ContentCaptureSession> mChildren;

    @Hide
    protected ContentCaptureSession() {
        this(getRandomSessionId());
    }

    @Hide
    @VisibleForTesting
    public ContentCaptureSession(int id) {
        Preconditions.checkArgument(id != NO_SESSION_ID);
        mId = id;
    }

    // Used by ChildContentCaptureSession
    ContentCaptureSession(@NonNull ContentCaptureContext initialContext) {
        this();
        mClientContext = Objects.requireNonNull(initialContext);
    }

    @Hide
    @NonNull
    protected abstract ContentCaptureSession getMainCaptureSession();

    abstract void start(@NonNull IBinder token, @NonNull IBinder shareableActivityToken,
            @NonNull ComponentName component, int flags);

    abstract boolean isDisabled();

    /**
     * Sets the disabled state of content capture.
     *
     * @return whether disabled state was changed.
     */
    abstract boolean setDisabled(boolean disabled);

    /**
     * Gets the id used to identify this session.
     */
    @NonNull
    public final ContentCaptureSessionId getContentCaptureSessionId() {
        if (mContentCaptureSessionId == null) {
            mContentCaptureSessionId = new ContentCaptureSessionId(mId);
        }
        return mContentCaptureSessionId;
    }

    @Hide
    @NonNull
    public int getId() {
        return mId;
    }

    /**
     * Creates a new {@link ContentCaptureSession}.
     *
     * <p>See {@link View#setContentCaptureSession(ContentCaptureSession)} for more info.
     */
    @NonNull
    public final ContentCaptureSession createContentCaptureSession(
            @NonNull ContentCaptureContext context) {
        final ContentCaptureSession child = newChild(context);
        if (sDebug) {
            Log.d(TAG, "createContentCaptureSession(" + context + ": parent=" + mId + ", child="
                    + child.mId);
        }
        synchronized (mLock) {
            if (mChildren == null) {
                mChildren = new ArrayList<>(INITIAL_CHILDREN_CAPACITY);
            }
            mChildren.add(child);
        }
        return child;
    }

    @Hide
    protected abstract ContentCaptureSession newChild(@NonNull ContentCaptureContext context);

    /**
     * Flushes the buffered events to the service.
     */
    abstract void flush(@FlushReason int reason);

    /**
     * Sets the {@link ContentCaptureContext} associated with the session.
     *
     * <p>Typically used to change the context associated with the default session from an activity.
     */
    public final void setContentCaptureContext(@Nullable ContentCaptureContext context) {
        if (!isContentCaptureEnabled()) return;

        mClientContext = context;
        updateContentCaptureContext(context);
    }

    abstract void updateContentCaptureContext(@Nullable ContentCaptureContext context);

    /**
     * Gets the {@link ContentCaptureContext} associated with the session.
     *
     * @return context set on constructor or by
     *         {@link #setContentCaptureContext(ContentCaptureContext)}, or {@code null} if never
     *         explicitly set.
     */
    @Nullable
    public final ContentCaptureContext getContentCaptureContext() {
        return mClientContext;
    }

    /**
     * Destroys this session, flushing out all pending notifications to the service.
     *
     * <p>Once destroyed, any new notification will be dropped.
     */
    public final void destroy() {
        synchronized (mLock) {
            if (mDestroyed) {
                if (sDebug) Log.d(TAG, "destroy(" + mId + "): already destroyed");
                return;
            }
            mDestroyed = true;

            // TODO(b/111276913): check state (for example, how to handle if it's waiting for remote
            // id) and send it to the cache of batched commands
            if (sVerbose) {
                Log.v(TAG, "destroy(): state=" + getStateAsString(mState) + ", mId=" + mId);
            }
            // Finish children first
            if (mChildren != null) {
                final int numberChildren = mChildren.size();
                if (sVerbose) Log.v(TAG, "Destroying " + numberChildren + " children first");
                for (int i = 0; i < numberChildren; i++) {
                    final ContentCaptureSession child = mChildren.get(i);
                    try {
                        child.destroy();
                    } catch (Exception e) {
                        Log.w(TAG, "exception destroying child session #" + i + ": " + e);
                    }
                }
            }
        }

        onDestroy();
    }

    abstract void onDestroy();

    @Hide
    @Override
    public void close() {
        destroy();
    }

    /**
     * Notifies the Content Capture Service that a node has been added to the view structure.
     *
     * <p>Typically called "manually" by views that handle their own virtual view hierarchy, or
     * automatically by the Android System for views that return {@code true} on
     * {@link View#onProvideContentCaptureStructure(ViewStructure, int)}.
     *
     * <p>Consider use {@link #notifyViewsAppeared} which has a better performance when notifying
     * a list of nodes has appeared.
     *
     * <p>Note; This function belongs to Content Capture Version 1.</p>
     *
     * @param node node that has been added.
     */
    public final void notifyViewAppeared(@NonNull ViewStructure node) {
        Objects.requireNonNull(node);
        if (!isContentCaptureEnabled() || !isContentCaptureVersion1()) return;

        if (!(node instanceof ViewNode.ViewStructureImpl)) {
            throw new IllegalArgumentException("Invalid node class: " + node.getClass());
        }

        internalNotifyViewAppeared(mId, (ViewStructureImpl) node);
    }

    @Hide
    protected abstract void internalNotifyViewAppeared(
            int sessionId, @NonNull ViewNode.ViewStructureImpl node);

    /**
     * Notifies the Content Capture Service that a node has been removed from the view structure.
     *
     * <p>Typically called "manually" by views that handle their own virtual view hierarchy, or
     * automatically by the Android System for standard views.
     *
     * <p>Consider use {@link #notifyViewsDisappeared} which has a better performance when notifying
     * a list of nodes has disappeared.
     *
     * <p>Note; This function belongs to Content Capture Version 1.</p>
     *
     * @see #getContentCaptureVersion()
     *
     * @param id id of the node that has been removed.
     */
    public final void notifyViewDisappeared(@NonNull AutofillId id) {
        Objects.requireNonNull(id);
        if (!isContentCaptureEnabled() || !isContentCaptureVersion1()) return;

        internalNotifyViewDisappeared(mId, id);
    }

    @Hide
    protected abstract void internalNotifyViewDisappeared(int sessionId, @NonNull AutofillId id);

    /**
     * Notifies the Content Capture Service that a list of nodes has appeared in the view structure.
     *
     * <p>Typically called manually by views that handle their own virtual view hierarchy.
     *
     * <p>Note; This function belongs to Content Capture Version 1.</p>
     *
     * @see #getContentCaptureVersion()
     *
     * @param appearedNodes nodes that have appeared. Each element represents a view node that has
     * been added to the view structure. The order of the elements is important, which should be
     * preserved as the attached order of when the node is attached to the virtual view hierarchy.
     */
    public final void notifyViewsAppeared(@NonNull List<ViewStructure> appearedNodes) {
        Preconditions.checkCollectionElementsNotNull(appearedNodes, "appearedNodes");
        if (!isContentCaptureEnabled() || !isContentCaptureVersion1()) return;

        for (int i = 0; i < appearedNodes.size(); i++) {
            ViewStructure v = appearedNodes.get(i);
            if (!(v instanceof ViewNode.ViewStructureImpl)) {
                throw new IllegalArgumentException("Invalid class: " + v.getClass());
            }
        }

        internalNotifyViewTreeEvent(mId, /* started= */ true);
        for (int i = 0; i < appearedNodes.size(); i++) {
            ViewStructure v = appearedNodes.get(i);
            internalNotifyViewAppeared(mId, (ViewStructureImpl) v);
        }
        internalNotifyViewTreeEvent(mId, /* started= */ false);
    }

    /**
     * Notifies the Content Capture Service that many nodes has been removed from a virtual view
     * structure.
     *
     * <p>Should only be called by views that handle their own virtual view hierarchy.
     *
     * <p>After UPSIDE_DOWN_CAKE, this method wraps the virtual children with a pair of view tree
     * appearing and view tree appeared events.
     *
     * <p>Note; This function belongs to Content Capture Version 1.</p>
     *
     * @see #getContentCaptureVersion()
     *
     * @param hostId id of the non-virtual view hosting the virtual view hierarchy (it can be
     * obtained by calling {@link ViewStructure#getAutofillId()}).
     * @param virtualIds ids of the virtual children.
     *
     * @throws IllegalArgumentException if the {@code hostId} is an autofill id for a virtual view.
     * @throws IllegalArgumentException if {@code virtualIds} is empty
     */
    public final void notifyViewsDisappeared(@NonNull AutofillId hostId,
            @NonNull long[] virtualIds) {
        Preconditions.checkArgument(hostId.isNonVirtual(), "hostId cannot be virtual: %s", hostId);
        Preconditions.checkArgument(!ArrayUtils.isEmpty(virtualIds), "virtual ids cannot be empty");
        if (!isContentCaptureEnabled() || !isContentCaptureVersion1()) return;

        if (CompatChanges.isChangeEnabled(NOTIFY_NODES_DISAPPEAR_NOW_SENDS_TREE_EVENTS)) {
            internalNotifyViewTreeEvent(mId, /* started= */ true);
        }
        for (long id : virtualIds) {
            internalNotifyViewDisappeared(mId, new AutofillId(hostId, id, mId));
        }
        if (CompatChanges.isChangeEnabled(NOTIFY_NODES_DISAPPEAR_NOW_SENDS_TREE_EVENTS)) {
            internalNotifyViewTreeEvent(mId, /* started= */ false);
        }
    }

    /**
     * Notifies the Intelligence Service that the value of a text node has been changed.
     *
     * <p>Note; This function belongs to Content Capture Version 1.</p>
     *
     * @see #getContentCaptureVersion()
     *
     * @param id of the node.
     * @param text new text.
     */
    public final void notifyViewTextChanged(@NonNull AutofillId id, @Nullable CharSequence text) {
        Objects.requireNonNull(id);

        if (!isContentCaptureEnabled() || !isContentCaptureVersion1()) return;

        internalNotifyViewTextChanged(mId, id, text);
    }

    @Hide
    protected abstract void internalNotifyViewTextChanged(int sessionId, @NonNull AutofillId id,
            @Nullable CharSequence text);

    /**
     * Notifies the Intelligence Service that the insets of a view have changed.
     *
     * <p>Note; This function belongs to Content Capture Version 1.</p>
     *
     * @see #getContentCaptureVersion()
     */
    public final void notifyViewInsetsChanged(@NonNull Insets viewInsets) {
        Objects.requireNonNull(viewInsets);

        if (!isContentCaptureEnabled() || !isContentCaptureVersion1()) return;

        internalNotifyViewInsetsChanged(mId, viewInsets);
    }

    @Hide
    protected abstract void internalNotifyViewInsetsChanged(int sessionId,
            @NonNull Insets viewInsets);

    /**
     * Flushes an internal buffer of UI events and signals System Intelligence (SI) that a
     * semantically meaningful state has been reached. SI uses this signal to potentially
     * rebuild the view hierarchy and understand the current state of the UI.
     *
     * <p>UI events are often batched together for performance reasons. A semantic batch
     * represents a series of events that, when applied sequentially, result in a
     * meaningful and complete UI state.
     *
     * <p>It is crucial to call {@code flush()} after completing a semantic batch to ensure
     * SI can accurately reconstruct the view hierarchy.
     *
     * <p><b>Premature Flushing:</b> Calling {@code flush()} within a semantic batch may
     * lead to SI failing to rebuild the view hierarchy correctly. This could manifest as
     * incorrect ordering of sibling nodes.
     *
     * <p><b>Delayed Flushing:</b> While not immediately flushing after a semantic batch is
     * generally safe, it's recommended to do so as soon as possible. In the worst-case
     * scenario where a {@code flush()} is never called, SI will attempt to process the
     * events after a short delay based on view appearance and disappearance events.
     *
     * <p>Note; This function belongs to Content Capture Version 1.</p>
     *
     * @see #getContentCaptureVersion()
     */
    @FlaggedApi(FLAG_CCAPI_BAKLAVA_ENABLED)
    public void flush() {
        if (!isContentCaptureVersion1()) return;
        internalNotifySessionFlushEvent(mId);
    }

    @Hide
    protected abstract void internalNotifySessionFlushEvent(int sessionId);

    /**
     * Notifies the Intelligence Service that a view has been interacted.
     *
     * <p>The view must have appeared before sending the interaction event.
     *
     * @param autofillId id of the node.
     */
    @FlaggedApi(FLAG_CONTENT_INTERACTION_API_ENABLED)
    public void notifyContentInteractionEvent(@NonNull AutofillId autofillId) {
        if (!isContentCaptureEnabled() || !isContentCaptureVersion1()) return;

        internalNotifyContentInteractionEvent(mId, autofillId);
    }

    @Hide
    protected abstract void internalNotifyContentInteractionEvent(int sessionId,
            AutofillId autofillId);


    @Hide
    public void notifyViewTreeEvent(boolean started) {
        internalNotifyViewTreeEvent(mId, started);
    }

    @Hide
    protected abstract void internalNotifyViewTreeEvent(int sessionId, boolean started);

    /**
     * Notifies the Content Capture Service that a session has resumed.
     */
    public final void notifySessionResumed() {
        if (!isContentCaptureEnabled() || !isContentCaptureVersion1()) return;

        internalNotifySessionResumed();
    }

    abstract void internalNotifySessionResumed();

    /**
     * Notifies the Content Capture Service that a session has paused.
     */
    public final void notifySessionPaused() {
        if (!isContentCaptureEnabled() || !isContentCaptureVersion1()) return;

        internalNotifySessionPaused();
    }

    abstract void internalNotifySessionPaused();

    abstract void internalNotifyChildSessionStarted(int parentSessionId, int childSessionId,
            @NonNull ContentCaptureContext clientContext);

    abstract void internalNotifyChildSessionFinished(int parentSessionId, int childSessionId);

    abstract void internalNotifyContextUpdated(
            int sessionId, @Nullable ContentCaptureContext context);

    /**
     * Gets the Content Capture version enabled for this session.
     *
     * <p> ContentCaptureSession offers two versions: </p>
     *
     * <ul>
     *     <li> The first, legacy version that uses {@code ViewStructure}. It is an information
     *          rich, event based system that attempts to transfer the current view hierarchy from
     *          the application process through periodic event dumps, e.g. {@link #flush()}.
     *          However, this system can also be resource heavy due to large number of events it
     *          generates. </li>
     *     <li> The second, focused and leaner approach to passing the View hierarchy. The required
     *          information is mostly limited to text on screen and bounding boxes. This version is
     *          significantly more efficient at resource usage and recommended to be the default
     *          one. </li>
     * </ul>
     *
     * <p> A list of functions that belong explicitly to Content Capture Version 1: </p>
     *
     * <ul>
     *     <li>{@link #notifyViewAppeared(ViewStructure)}</li>
     *     <li>{@link #notifyViewDisappeared(AutofillId)}</li>
     *     <li>{@link #notifyViewsAppeared(List)}</li>
     *     <li>{@link #notifyViewsDisappeared(AutofillId, long[])}</li>
     *     <li>{@link #notifyViewInsetsChanged(Insets)}</li>
     *     <li>{@link #notifyViewTextChanged(AutofillId, CharSequence)}</li>
     *     <li>{@link #flush()}</li>
     *     <li>{@link #newViewStructure(View)}</li>
     *     <li>{@link #newVirtualViewStructure(AutofillId, long)}</li>
     * </ul>
     *
     * <p> The returned version is decided by a System Intelligence Service that coordinates this
     * ContentCaptureSession. </p>
     *
     * @return the enabled version, either {@link #VERSION_1} or {@link #VERSION_2}. Calls to
     *         Version 1 methods become no-ops if the returned version is not {@link #VERSION_1} and
     *         vice versa.
     */
    @FlaggedApi(FLAG_CCAPI_NEXT_ENABLED)
    @ContentCaptureVersion
    public abstract int getContentCaptureVersion();

    /**
     * A handy internal version checker. If we ever add a new version, make sure to check the
     * callers of this function that they are not using this information to mean "not version 2".
     */
    @Hide
    public boolean isContentCaptureVersion1() {
        return getContentCaptureVersion() == VERSION_1;
    }

    @Hide
    public abstract void notifyWindowBoundsChanged(int sessionId, @NonNull Rect bounds);

    @Hide
    public abstract void notifyContentCaptureEvents(
            @NonNull SparseArray<ArrayList<Object>> contentCaptureEvents);

    @Hide
    public abstract void notifyContentCaptureInteractionEvents(
            @NonNull SparseArray<ArrayList<Object>> contentCaptureInteractionEvents);

    /**
     * Creates a {@link ViewStructure} for a "standard" view.
     *
     * <p>This method should be called after a visible view is laid out; the view then must populate
     * the structure and pass it to {@link #notifyViewAppeared(ViewStructure)}.
     *
     * <b>Note: </b>views that manage a virtual structure under this view must populate just the
     * node representing this view and return right away, then asynchronously report (not
     * necessarily in the UI thread) when the children nodes appear, disappear or have their text
     * changed by calling {@link ContentCaptureSession#notifyViewAppeared(ViewStructure)},
     * {@link ContentCaptureSession#notifyViewDisappeared(AutofillId)}, and
     * {@link ContentCaptureSession#notifyViewTextChanged(AutofillId, CharSequence)} respectively.
     * The structure for the a child must be created using
     * {@link ContentCaptureSession#newVirtualViewStructure(AutofillId, long)}, and the
     * {@code autofillId} for a child can be obtained either through
     * {@code childStructure.getAutofillId()} or
     * {@link ContentCaptureSession#newAutofillId(AutofillId, long)}.
     *
     * <p>When the virtual view hierarchy represents a web page, you should also:
     *
     * <ul>
     * <li>Call {@link ContentCaptureManager#getContentCaptureConditions()} to infer content capture
     * events should be generate for that URL.
     * <li>Create a new {@link ContentCaptureSession} child for every HTML element that renders a
     * new URL (like an {@code IFRAME}) and use that session to notify events from that subtree.
     * </ul>
     *
     * <p><b>Note: </b>the following methods of the {@code structure} will be ignored:
     * <ul>
     * <li>{@link ViewStructure#setChildCount(int)}
     * <li>{@link ViewStructure#addChildCount(int)}
     * <li>{@link ViewStructure#getChildCount()}
     * <li>{@link ViewStructure#newChild(int)}
     * <li>{@link ViewStructure#asyncNewChild(int)}
     * <li>{@link ViewStructure#asyncCommit()}
     * <li>{@link ViewStructure#setWebDomain(String)}
     * <li>{@link ViewStructure#newHtmlInfoBuilder(String)}
     * <li>{@link ViewStructure#setHtmlInfo(android.view.ViewStructure.HtmlInfo)}
     * <li>{@link ViewStructure#setDataIsSensitive(boolean)}
     * <li>{@link ViewStructure#setAlpha(float)}
     * <li>{@link ViewStructure#setElevation(float)}
     * <li>{@link ViewStructure#setTransformation(android.graphics.Matrix)}
     * </ul>
     *
     * <p>Note: This function belongs to Content Capture Version 1.</p>
     *
     * @see #getContentCaptureVersion()
     */
    @NonNull
    public final ViewStructure newViewStructure(@NonNull View view) {
        return new ViewNode.ViewStructureImpl(view);
    }

    /**
     * Creates a new {@link AutofillId} for a virtual child, so it can be used to uniquely identify
     * the children in the session.
     *
     * @param hostId id of the non-virtual view hosting the virtual view hierarchy (it can be
     * obtained by calling {@link ViewStructure#getAutofillId()}).
     * @param virtualChildId id of the virtual child, relative to the parent.
     *
     * @return if for the virtual child
     *
     * @throws IllegalArgumentException if the {@code parentId} is a virtual child id.
     */
    public @NonNull AutofillId newAutofillId(@NonNull AutofillId hostId, long virtualChildId) {
        Objects.requireNonNull(hostId);
        Preconditions.checkArgument(hostId.isNonVirtual(), "hostId cannot be virtual: %s", hostId);
        return new AutofillId(hostId, virtualChildId, mId);
    }

    /**
     * Creates a {@link ViewStructure} for a "virtual" view, so it can be passed to
     * {@link #notifyViewAppeared(ViewStructure)} by the view managing the virtual view hierarchy.
     *
     * <p>Note: This function belongs to Content Capture Version 1.</p>
     *
     * @see #getContentCaptureVersion()
     *
     * @param parentId id of the virtual view parent (it can be obtained by calling
     * {@link ViewStructure#getAutofillId()} on the parent).
     * @param virtualId id of the virtual child, relative to the parent.
     *
     * @return a new {@link ViewStructure} that can be used for Content Capture purposes.
     */
    @NonNull
    public final ViewStructure newVirtualViewStructure(@NonNull AutofillId parentId,
            long virtualId) {
        return new ViewNode.ViewStructureImpl(parentId, virtualId, mId);
    }

    boolean isContentCaptureEnabled() {
        return !mDestroyed;
    }

    @CallSuper
    void dump(@NonNull String prefix, @NonNull PrintWriter pw) {
        pw.print(prefix); pw.print("id: "); pw.println(mId);
        if (mClientContext != null) {
            pw.print(prefix); mClientContext.dump(pw); pw.println();
        }
        synchronized (mLock) {
            pw.print(prefix); pw.print("destroyed: "); pw.println(mDestroyed);
            if (mChildren != null && !mChildren.isEmpty()) {
                final String prefix2 = prefix + "  ";
                final int numberChildren = mChildren.size();
                pw.print(prefix); pw.print("number children: "); pw.println(numberChildren);
                for (int i = 0; i < numberChildren; i++) {
                    final ContentCaptureSession child = mChildren.get(i);
                    pw.print(prefix); pw.print(i); pw.println(": "); child.dump(prefix2, pw);
                }
            }
        }
    }

    @Override
    public String toString() {
        return Integer.toString(mId);
    }

    @Hide
    @NonNull
    protected static String getStateAsString(int state) {
        return state + " (" + (state == UNKNOWN_STATE ? "UNKNOWN"
                : DebugUtils.flagsToString(ContentCaptureSession.class, "STATE_", state)) + ")";
    }

    @Hide
    @NonNull
    public static String getFlushReasonAsString(@FlushReason int reason) {
        switch (reason) {
            case FLUSH_REASON_FULL:
                return "FULL";
            case FLUSH_REASON_VIEW_ROOT_ENTERED:
                return "VIEW_ROOT";
            case FLUSH_REASON_SESSION_STARTED:
                return "STARTED";
            case FLUSH_REASON_SESSION_FINISHED:
                return "FINISHED";
            case FLUSH_REASON_IDLE_TIMEOUT:
                return "IDLE";
            case FLUSH_REASON_TEXT_CHANGE_TIMEOUT:
                return "TEXT_CHANGE";
            case FLUSH_REASON_SESSION_CONNECTED:
                return "CONNECTED";
            case FLUSH_REASON_FORCE_FLUSH:
                return "FORCE_FLUSH";
            case FLUSH_REASON_VIEW_TREE_APPEARING:
                return "VIEW_TREE_APPEARING";
            case FLUSH_REASON_VIEW_TREE_APPEARED:
                return "VIEW_TREE_APPEARED";
            default:
                return "UNKNOWN-" + reason;
        }
    }

    private static int getRandomSessionId() {
        int id;
        do {
            id = ID_GENERATOR.nextInt();
        } while (id == NO_SESSION_ID);
        return id;
    }
}
