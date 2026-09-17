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

package android.app.admin;

import android.annotation.CallbackExecutor;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.app.admin.metadata.PolicyTransportValueConvertor;
import android.os.RemoteException;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Handler for mapping client policy change callbacks to Binder IPC callbacks.
 */
@Hide
public class PolicyValueCallbackHandler {
    private static final String TAG = "PolicyValueCallbackHandler";

    private final String mPackageName;
    private final IDevicePolicyManager mService;

    public PolicyValueCallbackHandler(String packageName, IDevicePolicyManager service) {
        mPackageName = packageName;
        mService = service;
    }

    private static final class DeviceCallbackKey {
        private final PolicyIdentifier<?> mId;
        private final ResolvedDeviceWidePolicyCallback<?> mCallback;

        DeviceCallbackKey(PolicyIdentifier<?> id, ResolvedDeviceWidePolicyCallback<?> callback) {
            mId = id;
            mCallback = callback;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            DeviceCallbackKey that = (DeviceCallbackKey) o;
            return Objects.equals(mId, that.mId) && Objects.equals(mCallback, that.mCallback);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mId, mCallback);
        }
    }

    @NonNull
    private final Map<DeviceCallbackKey, IDevicePolicyValueCallback> mDeviceCallbacks =
            new HashMap<>();

    @Nullable
    private static <T> T policyValueFromTransport(
            @NonNull PolicyIdentifier<T> id, @Nullable PolicyValueTransport value) {
        if (value == null) {
            return null;
        }

        return PolicyTransportValueConvertor.getInstance(id).fromTransport(value);
    }

    /**
     * Listen for changes to the resolved value of a device-wide policy.
     *
     * @param id The policy identifier to subscribe to.
     * @param executor The executor to run the callback on.
     * @param callback The callback to invoke when the resolved policy value changes.
     * @param <T> The type of the policy.
     */
    public <T> void addResolvedDeviceWidePolicyCallback(
            @NonNull PolicyIdentifier<T> id,
            @NonNull @CallbackExecutor Executor executor,
            @NonNull ResolvedDeviceWidePolicyCallback<T> callback) {

        Objects.requireNonNull(id, "PolicyIdentifier cannot be null");
        Objects.requireNonNull(callback, "ResolvedDeviceWidePolicyCallback cannot be null");
        Objects.requireNonNull(executor, "Executor cannot be null");

        DeviceCallbackKey key = new DeviceCallbackKey(id, callback);
        synchronized (mDeviceCallbacks) {
            if (mDeviceCallbacks.containsKey(key)) {
                Log.e(TAG, "Callback " + key + " is already registered");
                return;
            }
        }

        IDevicePolicyValueCallback ipcCallback =
                new IDevicePolicyValueCallback.Stub() {
                    @Override
                    public void onPolicyValueUpdated(
                            String transportId,
                            PolicyValueTransport newValueTransport,
                            PolicyValueTransport oldValueTransport) {
                        if (!id.getId().equals(transportId)) {
                            throw new IllegalStateException(
                                    "Identifier mismatch, expected '"
                                            + id.getId()
                                            + "' got '"
                                            + transportId
                                            + "'.");
                        }

                        T oldValue = policyValueFromTransport(id, oldValueTransport);
                        T newValue = policyValueFromTransport(id, newValueTransport);

                        synchronized (mDeviceCallbacks) {
                            if (!mDeviceCallbacks.containsKey(key)) {
                                return;
                            }
                        }

                        executor.execute(() -> callback.onValueChanged(id, newValue, oldValue));
                    }
                };

        try {
            mService.addResolvedDeviceWidePolicyCallback(mPackageName, id.getId(), ipcCallback);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }

        synchronized (mDeviceCallbacks) {
            mDeviceCallbacks.put(key, ipcCallback);
        }
    }

    /**
     * Remove a previously added callback.
     *
     * @param id The policy identifier to remove the callback from.
     * @param callback The callback to remove.
     * @param <T> The type of the policy value.
     */
    public <T> void removeResolvedDeviceWidePolicyCallback(
            @NonNull PolicyIdentifier<T> id,
            @NonNull ResolvedDeviceWidePolicyCallback<T> callback) {
        DeviceCallbackKey key = new DeviceCallbackKey(id, callback);
        synchronized (mDeviceCallbacks) {
            var ipcCallback = mDeviceCallbacks.get(key);

            if (ipcCallback == null) {
                Log.w(TAG, "Callback not found for removal: " + id);
                return;
            }

            try {
                mService.removeResolvedDeviceWidePolicyCallback(
                        mPackageName, id.getId(), ipcCallback);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }

            mDeviceCallbacks.remove(key);
        }
    }
}
