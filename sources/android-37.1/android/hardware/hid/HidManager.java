/*
 * Copyright (C) 2026 The Android Open Source Project
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

package android.hardware.hid;

import static com.android.hardware.input.Flags.FLAG_HID_API;

import android.Manifest;
import android.annotation.CallbackExecutor;
import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.RequiresPermission;
import android.annotation.SystemService;
import android.annotation.WorkerThread;
import android.content.Context;
import android.content.PermissionChecker;
import android.os.Binder;
import android.os.Looper;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.ArrayMap;
import android.util.Log;

import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Provides access to Human Interface Device (HID) nodes.
 *
 * <p>This manager allows applications to enumerate connected HID devices and query their
 * information (such as vendor/product IDs and report descriptors).
 *
 * <p>HID devices include peripherals such as keyboards, mice, game controllers, and other
 * interactive devices that follow the HID specification.
 *
 * <p>Access to HID devices requires the {@link android.Manifest.permission#ACCESS_HID} permission.
 * Apps are typically granted access to specific devices via a system-provided chooser or
 * permission dialog.
 *
 * <p>Device access may be restricted when the application is in the background or in a cached
 * state to ensure system security and resource efficiency.
 */
@FlaggedApi(FLAG_HID_API)
@SystemService(Context.HID_SERVICE)
public final class HidManager {
    private static final String TAG = "HidManager";

    private final Object mLock = new Object();

    /** Maps app-level listeners to their Binder delegates for unregistration. */
    @GuardedBy("mLock")
    private final ArrayMap<HidDeviceListener, ListenerDelegate> mListeners = new ArrayMap<>();

    /** Caches HidDevice instances to ensure object identity across the API. */
    @GuardedBy("mLock")
    private final ArrayMap<HidDeviceInfo, HidDevice> mDeviceCache = new ArrayMap<>();
    private final Context mContext;
    private final Object mServiceLock = new Object();
    private volatile IHidManager mService;

    @Hide
    public HidManager(Context context) {
        mContext = context;
    }

    @Hide
    @VisibleForTesting
    public HidManager(Context context, IHidManager service) {
        mContext = context;
        mService = service;
    }

    private IHidManager getService() {
        IHidManager service = mService;
        if (service != null) {
            return service;
        }
        synchronized (mServiceLock) {
            service = mService;
            if (service == null) {
                // Since the service is lazy, we must wait for it.
                service =
                        IHidManager.Stub.asInterface(
                                ServiceManager.waitForService(Context.HID_SERVICE));
                mService = service;
            }
            return service;
        }
    }

    /**
     * Returns a list of all currently connected HID devices.
     *
     * <p>The returned list contains {@link HidDevice} objects representing each connected HID node
     * that the system has detected and made available.
     *
     * <p>It is recommended to call {@link #canEnumerateDevices()} before calling this method.
     *
     * @return a list of connected HID devices, or an empty list if no devices are connected.
     * @throws IllegalStateException if called on the main thread.
     * @throws SecurityException if the caller does not have the
     *         {@link android.Manifest.permission#ACCESS_HID} permission or if
     *         {@link #canEnumerateDevices()} returns {@code false}.
     */
    @NonNull
    @WorkerThread
    @RequiresPermission(Manifest.permission.ACCESS_HID)
    public List<HidDevice> getDevices() {
        if (Looper.getMainLooper().isCurrentThread()) {
            throw new IllegalStateException("getDevices() must not be called on the main thread.");
        }

        try {
            IHidManager service = getService();
            if (service == null) {
                Log.w(TAG, "Service not found");
                return Collections.emptyList();
            }
            List<HidDeviceInfo> infos = service.getDevices();
            if (infos == null) {
                Log.w(TAG, "service.getDevices() returned null");
                synchronized (mLock) {
                    mDeviceCache.clear();
                }
                return Collections.emptyList();
            }

            List<HidDevice> devices = new ArrayList<>(infos.size());
            synchronized (mLock) {
                for (HidDeviceInfo info : infos) {
                    HidDevice device = mDeviceCache.get(info);
                    if (device == null) {
                        device = new HidDevice(mContext, info, getService());
                    }
                    devices.add(device);
                }
            }
            return devices;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Registers a listener to receive notifications about HID device lifecycle events.
     *
     * <p>When a HID device is physically connected to or disconnected from the system, the
     * provided {@link HidDeviceListener} will be notified. If the listener is already
     * registered, this call does nothing.
     *
     * <p>All callback methods on the listener will be invoked using the provided {@link Executor}.
     *
     * <p>Upon registration, {@link HidDeviceListener#onHidDeviceAdded(HidDevice)} will be
     * immediately invoked for all HID devices currently connected to the system.
     *
     * @param listener The listener to receive device addition and removal events. Must not be {@code null}.
     * @param executor The executor on which the listener methods will be invoked. Must not be {@code null}.
     *
     * @see #unregisterListener(HidDeviceListener)
     */
    @RequiresPermission(Manifest.permission.ACCESS_HID)
    public void registerListener(@NonNull HidDeviceListener listener,
            @NonNull @CallbackExecutor Executor executor) {
        Objects.requireNonNull(listener, "listener cannot be null");
        Objects.requireNonNull(executor, "executor cannot be null");

        final IHidManager service = getService();
        if (service == null) {
            throw new IllegalStateException(
                    "Failed to register listener: HidManager service not found");
        }

        ListenerDelegate delegate = new ListenerDelegate(listener, executor, service);
        synchronized (mLock) {
            if (mListeners.containsKey(listener)) {
                return;
            } else {
                mListeners.put(listener, delegate);
            }
        }

        List<HidDeviceInfo> infos;
        boolean success = false;
        try {
            infos = service.registerListener(delegate);
            success = true;
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } finally {
            if (!success) {
                synchronized (mLock) {
                    mListeners.remove(listener);
                }
            }
        }

        List<HidDevice> initialDevices = new ArrayList<>();
        boolean stillRegistered;
        synchronized (mLock) {
            stillRegistered = mListeners.containsKey(listener);
            if (stillRegistered && !infos.isEmpty()) {
                for (HidDeviceInfo info : infos) {
                    initialDevices.add(getOrCreateDeviceLocked(info));
                }
            }
        }

        if (!stillRegistered) {
            Log.i(TAG, "Listener unregistered during/after service call, cleaning up");
            try {
                service.unregisterListener(delegate);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to unregister listener after concurrent removal", e);
            }
            return;
        }

        for (HidDevice device : initialDevices) {
            delegate.dispatchCallback(device, HidDeviceListener::onHidDeviceAdded);
        }
    }

    /**
     * Unregisters a previously registered listener for HID device lifecycle events.
     *
     * <p>Once unregistered, the listener will no longer receive notifications when HID
     * devices are added to or removed from the system. If the provided listener was
     * not previously registered via {@link #registerListener}, this call does nothing.
     *
     * <p>After this method returns, no further callbacks will be dispatched to the
     * executor associated with this listener.
     *
     * @param listener The listener to unregister. Must not be {@code null}.
     *
     * @see #registerListener(HidDeviceListener, Executor)
     */
    @RequiresPermission(Manifest.permission.ACCESS_HID)
    public void unregisterListener(@NonNull HidDeviceListener listener) {
        Objects.requireNonNull(listener, "listener cannot be null");

        ListenerDelegate delegate;
        synchronized (mLock) {
            delegate = mListeners.remove(listener);
            if (delegate == null) {
                return;
            }
            // Immediately disable the delegate to stop pending callbacks.
            delegate.nullify();
        }

        try {
            IHidManager service = getService();
            service.unregisterListener(delegate);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Checks if the calling app has the necessary permissions to enumerate connected HID devices.
     *
     * @return {@code true} if the calling app can enumerate HIDs, {@code false} otherwise.
     */
    public boolean canEnumerateDevices() {
        return PermissionChecker.checkPermissionForPreflight(
                        mContext,
                        Manifest.permission.ACCESS_HID,
                        mContext.getAttributionSource())
                == PermissionChecker.PERMISSION_GRANTED;
    }

    /**
     * A delegate that wraps a {@link HidDeviceListener} and ensures that no callbacks
     * are executed after the listener has been unregistered.
     */
    private final class ListenerDelegate extends IHidDeviceListener.Stub {
        private final Object mDelegateLock = new Object();
        @GuardedBy("mDelegateLock")
        private @NonNull HidDeviceListener mListener;
        private @NonNull final Executor mExecutor;
        private @NonNull final IHidManager mService;

        ListenerDelegate(@NonNull HidDeviceListener listener,
                @NonNull Executor executor, @NonNull IHidManager service) {
            mListener = listener;
            mExecutor = executor;
            mService = service;
        }

        /**
         * Disables the delegate so that no further callbacks are dispatched or executed.
         */
        void nullify() {
            synchronized (mDelegateLock) {
                mListener = null;
            }
        }

        @Override
        public void onHidDeviceAdded(@NonNull HidDeviceInfo device) {
            HidDevice hidDevice;
            synchronized (mLock) {
                hidDevice = getOrCreateDeviceLocked(device);
            }
            dispatchCallback(hidDevice, HidDeviceListener::onHidDeviceAdded);
        }

        @Override
        public void onHidDeviceRemoved(@NonNull HidDeviceInfo device) {
            HidDevice hidDevice;
            synchronized (mLock) {
                hidDevice = mDeviceCache.remove(device);
            }
            if (hidDevice == null) {
                // TODO(b/513171067): Removing from cache here causes issues.
                // Refactor to a single service listener.
                hidDevice = new HidDevice(mContext, device, mService);
                Log.w(TAG, "onHidDeviceRemoved: Device not found in cache: " + device.hidrawPath);
            }
            dispatchCallback(hidDevice, HidDeviceListener::onHidDeviceRemoved);
        }

        /**
         * Dispatches a HID device lifecycle event to the registered listener.
         *
         * <p>This method centralizes the complex logic required to safely notify application
         * listeners from a system callback.
         *
         * @param hidDevice The {@link HidDevice} instance.
         * @param action A functional interface representing the specific listener
         *               method to invoke (e.g., onHidDeviceAdded).
         */
        private void dispatchCallback(HidDevice hidDevice,
                java.util.function.BiConsumer<HidDeviceListener, HidDevice> action) {
            mExecutor.execute(() -> {
                final HidDeviceListener localListener;
                synchronized (mDelegateLock) {
                    localListener = mListener;
                }
                if (localListener == null) {
                    return;
                }

                final long identity = Binder.clearCallingIdentity();
                try {
                    action.accept(localListener, hidDevice);
                } catch (RuntimeException e) {
                    Log.w(TAG, "Failed to execute HID lifecycle callback", e);
                } finally {
                    Binder.restoreCallingIdentity(identity);
                }
            });
        }
    }

    /**
     * Gets an existing {@link HidDevice} from the cache or creates a new one if it does not
     * already exist.
     *
     * <p>This helper method ensures that the manager consistently returns the same
     * {@link HidDevice} instance for a given {@link HidDeviceInfo}. This maintains object
     * identity for clients, allowing them to compare device objects across different
     * API calls (e.g., comparing a device from {@link #getDevices()} with one received
     * via a {@link HidDeviceListener}).
     *
     * @param info The device information used as the cache key and for device initialization.
     * @return A {@link HidDevice} instance associated with the provided info.
     */
    @GuardedBy("mLock")
    private HidDevice getOrCreateDeviceLocked(@NonNull HidDeviceInfo info) {
        HidDevice device = mDeviceCache.get(info);
        if (device == null) {
            device = new HidDevice(mContext, info, getService());
            mDeviceCache.put(info, device);
        }
        return device;
    }

    /**
     * Returns a list of all composite permission keys granted to the given packageName.
     *
     * @param packageName The package name to query permissions for.
     * @return A list of composite permission keys granted to the package.
     */
    @Hide
    public @NonNull List<HidDevicePermission> getGrantedPermissions(@NonNull String packageName) {
        Objects.requireNonNull(packageName, "packageName must not be null");
        try {
            IHidManager service = getService();
            if (service == null) {
                Log.w(TAG, "Service not found");
                return Collections.emptyList();
            }
            return service.getGrantedPermissions(packageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Updates the permission state for the given device permission profile.
     *
     * @param permission The device permission profile to update.
     * @param granted    True to grant the permission, false to revoke it.
     */
    @Hide
    public void updatePermission(@NonNull HidDevicePermission permission, boolean granted) {
        Objects.requireNonNull(permission, "permission must not be null");
        try {
            IHidManager service = getService();
            if (service == null) {
                Log.w(TAG, "Service not found");
                return;
            }
            service.updatePermission(permission, granted);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
