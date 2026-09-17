/*
 * Copyright (C) 2020 The Android Open Source Project
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


package android.companion;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.MainThread;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresPermission;
import android.annotation.TestApi;
import android.app.Service;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * A service that receives calls from the system with device events.
 *
 * <p>
 * Companion applications must create a service that {@code extends}
 * {@link CompanionDeviceService}, and declare it in their AndroidManifest.xml with the
 * "android.permission.BIND_COMPANION_DEVICE_SERVICE" permission
 * (see {@link android.Manifest.permission#BIND_COMPANION_DEVICE_SERVICE}),
 * as well as add an intent filter for the "android.companion.CompanionDeviceService" action
 * (see {@link #SERVICE_INTERFACE}).
 *
 * <p>
 * Following is an example of such declaration:
 * <pre>
 * &lt;service
 *        android:name=".CompanionService"
 *        android:label="@string/service_name"
 *        android:exported="true"
 *        android:permission="android.permission.BIND_COMPANION_DEVICE_SERVICE">
 *    &lt;intent-filter>
 *        &lt;action android:name="android.companion.CompanionDeviceService" />
 *    &lt;/intent-filter>
 * &lt;/service>
 * </pre>
 *
 * <p>
 * If the companion application has requested observing device presence (see
 * {@link CompanionDeviceManager#stopObservingDevicePresence(ObservingDevicePresenceRequest)})
 * the system will <a href="https://developer.android.com/guide/components/bound-services">
 * bind the service</a> when one of the {@link DevicePresenceEvent#EVENT_BLE_APPEARED},
 * {@link DevicePresenceEvent#EVENT_BT_CONNECTED},
 * {@link DevicePresenceEvent#EVENT_SELF_MANAGED_APPEARED} event is notified.
 *
 * <p>
 * The system binding {@link CompanionDeviceService} elevates the priority of the process that
 * the service is running in, and thus may prevent
 * <a href="https://developer.android.com/topic/performance/memory-management#low-memory_killer">
 * the Low-memory killer</a> from killing the process at expense of other processes with lower
 * priority.
 *
 * <p>
 * It is possible for an application to declare multiple {@link CompanionDeviceService}-s.
 * In such case, the system will bind all declared services, but will deliver
 * {@link #onDeviceAppeared(AssociationInfo)} and {@link #onDeviceDisappeared(AssociationInfo)}
 * only to one "primary" services.
 * Applications that declare multiple {@link CompanionDeviceService}-s should indicate the "primary"
 * service using "android.companion.PROPERTY_PRIMARY_COMPANION_DEVICE_SERVICE" service level
 * property.
 * <pre>
 * &lt;property
 *       android:name="android.companion.PROPERTY_PRIMARY_COMPANION_DEVICE_SERVICE"
 *       android:value="true" />
 * </pre>
 *
 * <p>
 * If the application declares multiple {@link CompanionDeviceService}-s, but does not indicate
 * the "primary" one, the system will pick one of the declared services to use as "primary".
 *
 * <p>
 * If the application declares multiple "primary" {@link CompanionDeviceService}-s, the system
 * will pick single one of them to use as "primary".
 */
public abstract class CompanionDeviceService extends Service {

    private static final String LOG_TAG = "CDM_CompanionDeviceService";

    /**
     * An intent action for a service to be bound whenever this app's companion device(s)
     * are nearby or self-managed device(s) report app appeared.
     *
     * <p>The app will be kept bound by the system when one of the
     * {@link DevicePresenceEvent#EVENT_BLE_APPEARED},
     * {@link DevicePresenceEvent#EVENT_BT_CONNECTED},
     * {@link DevicePresenceEvent#EVENT_SELF_MANAGED_APPEARED} event is notified.
     *
     * If the app is not running when one of the
     * {@link DevicePresenceEvent#EVENT_BLE_APPEARED},
     * {@link DevicePresenceEvent#EVENT_BT_CONNECTED},
     * {@link DevicePresenceEvent#EVENT_SELF_MANAGED_APPEARED} event is notified, the app will be
     * kept bound by the system.</p>
     *
     * <p>Shortly, the service will be unbound if both
     * {@link DevicePresenceEvent#EVENT_BLE_DISAPPEARED} and
     * {@link DevicePresenceEvent#EVENT_BT_DISCONNECTED} are notified, or
     * {@link DevicePresenceEvent#EVENT_SELF_MANAGED_DISAPPEARED} event is notified.
     * The app will be eligible for cleanup, unless any other user-visible components are
     * running.</p>
     *
     * If running in background is not essential for the devices that this app can manage,
     * app should avoid declaring this service.</p>
     *
     * <p>The service must also require permission
     * {@link android.Manifest.permission#BIND_COMPANION_DEVICE_SERVICE}</p>
     */
    public static final String SERVICE_INTERFACE = "android.companion.CompanionDeviceService";


    private final Stub mRemote = new Stub();

    /**
     * Called by system whenever a device associated with this app is available.
     *
     * @param address the MAC address of the device
     * @deprecated please override {@link #onDeviceAppeared(AssociationInfo)} instead.
     */
    @Deprecated
    @MainThread
    public void onDeviceAppeared(@NonNull String address) {
        // Do nothing. Companion apps can override this function.
    }

    /**
     * Called by system whenever a device associated with this app stops being available.
     *
     * Usually this means the device goes out of range or is turned off.
     *
     * @param address the MAC address of the device
     * @deprecated please override {@link #onDeviceDisappeared(AssociationInfo)} instead.
     */
    @Deprecated
    @MainThread
    public void onDeviceDisappeared(@NonNull String address) {
        // Do nothing. Companion apps can override this function.
    }

    /**
     * Called by system whenever the system dispatches a message to the app to send it to
     * an associated device.
     *
     * @param messageId system assigned id of the message to be sent
     * @param associationId association id of the associated device
     * @param message message to be sent
     */
    @Hide
    @Deprecated
    public void onMessageDispatchedFromSystem(int messageId, int associationId,
            @NonNull byte[] message) {
        Log.w(LOG_TAG, "Replaced by attachSystemDataTransport");
        // do nothing. Companion apps can override this function for system to send messages.
    }

    /**
     * App calls this method when there's a message received from an associated device,
     * which needs to be dispatched to system for processing.
     *
     * <p>Calling app must declare uses-permission
     * {@link android.Manifest.permission#DELIVER_COMPANION_MESSAGES}</p>
     *
     * <p>You need to start the service before calling this method, otherwise the system can't
     * get the context and the dispatch would fail.</p>
     *
     * <p>Note 1: messageId was assigned by the system, and sender should send the messageId along
     * with the message to the receiver. messageId will later be used for verification purpose.
     * Misusing the messageId will result in no action.</p>
     *
     * <p>Note 2: associationId should be local to your device which is calling this API. It's not
     * the associationId on your remote device. If you don't have one, you can call
     * {@link CompanionDeviceManager#associate(AssociationRequest, Executor,
     * CompanionDeviceManager.Callback)} to create one. Misusing the associationId will result in
     * {@link DeviceNotAssociatedException}.</p>
     *
     * @param messageId id of the message
     * @param associationId id of the associated device
     * @param message message received from the associated device
     */
    @Hide
    @Deprecated
    @RequiresPermission(android.Manifest.permission.DELIVER_COMPANION_MESSAGES)
    public final void dispatchMessageToSystem(int messageId, int associationId,
            @NonNull byte[] message)
            throws DeviceNotAssociatedException {
        Log.w(LOG_TAG, "Replaced by attachSystemDataTransport");
    }

    /**
     * Attach the given bidirectional communication streams to be used for
     * transporting system data between associated devices.
     * <p>
     * The companion service providing these streams is responsible for ensuring
     * that all data is transported accurately and in-order between the two
     * devices, including any fragmentation and re-assembly when carried over a
     * size-limited transport.
     * <p>
     * As an example, it's valid to provide streams obtained from a
     * {@link BluetoothSocket} to this method, since {@link BluetoothSocket}
     * meets the API contract described above.
     * <p>
     * This method passes through to
     * {@link CompanionDeviceManager#attachSystemDataTransport(int, InputStream, OutputStream)}
     * for your convenience if you get callbacks in this class.
     *
     * @param associationId id of the associated device
     * @param in already connected stream of data incoming from remote
     *            associated device
     * @param out already connected stream of data outgoing to remote associated
     *            device
     */
    @RequiresPermission(android.Manifest.permission.DELIVER_COMPANION_MESSAGES)
    public final void attachSystemDataTransport(int associationId, @NonNull InputStream in,
            @NonNull OutputStream out) throws DeviceNotAssociatedException {
        getSystemService(CompanionDeviceManager.class)
                .attachSystemDataTransport(associationId,
                        Objects.requireNonNull(in),
                        Objects.requireNonNull(out));
    }

    /**
     * Detach any bidirectional communication streams previously configured
     * through {@link #attachSystemDataTransport}.
     * <p>
     * This method passes through to
     * {@link CompanionDeviceManager#detachSystemDataTransport(int)}
     * for your convenience if you get callbacks in this class.
     *
     * @param associationId id of the associated device
     */
    @RequiresPermission(android.Manifest.permission.DELIVER_COMPANION_MESSAGES)
    public final void detachSystemDataTransport(int associationId)
            throws DeviceNotAssociatedException {
        getSystemService(CompanionDeviceManager.class)
                .detachSystemDataTransport(associationId);
    }

    /**
     * Called by the system when an associated device is nearby or connected.
     *
     * @param associationInfo A record for the companion device.
     * @deprecated use {@link #onDevicePresenceEvent(DevicePresenceEvent)}} instead.
     */
    @Deprecated
    @MainThread
    public void onDeviceAppeared(@NonNull AssociationInfo associationInfo) {
        if (!associationInfo.isSelfManaged()) {
            onDeviceAppeared(associationInfo.getDeviceMacAddressAsString());
        }
    }

    /**
     * Called by the system when an associated device is out of range or disconnected.
     *
     * @param associationInfo A record for the companion device.
     * @deprecated use {@link #onDevicePresenceEvent(DevicePresenceEvent)}} instead.
     */
    @Deprecated
    @MainThread
    public void onDeviceDisappeared(@NonNull AssociationInfo associationInfo) {
        if (!associationInfo.isSelfManaged()) {
            onDeviceDisappeared(associationInfo.getDeviceMacAddressAsString());
        }
    }

    /**
     * Called by the system when an associated device's presence state changes.
     *
     * @see CompanionDeviceManager#startObservingDevicePresence(ObservingDevicePresenceRequest)
     */
    @MainThread
    public void onDevicePresenceEvent(@NonNull DevicePresenceEvent event) {
        // Do nothing. Companion apps can override this function.
    }

    /**
     * Called by the system when it requests an action for an association managed
     * by this app.
     * @param associationInfo The association for which the action is requested.
     * @param request The specific request being requested
     */
    @MainThread
    @FlaggedApi(Flags.FLAG_ENABLE_DATA_SYNC)
    public void onActionRequested(
            @NonNull AssociationInfo associationInfo, @NonNull ActionRequest request) {
        // Do nothing. Companion apps can override this function.
    }

    /**
     * Called by the system when a trust pairing response is pending user confirmation
     * on the responder device. This can happen when the pairing was initiated by the remote
     * device. The application is expected to launch the provided {@code intentSender} to
     * display the user confirmation dialog.
     *
     * @param associationInfo The association for which trust pairing is requested.
     * @param intentSender An {@link IntentSender} which the application should use to launch
     *                     the UI for the user to confirm the trust pairing.
     */
    @MainThread
    @FlaggedApi(Flags.FLAG_TRUST_PAIRING)
    public void onTrustPairingResponsePending(
            @NonNull AssociationInfo associationInfo, @NonNull IntentSender intentSender) {
        // Do nothing. Companion apps can override this function.
    }

    /**
     * Called by the system when the trust pairing is canceled by the remote device. The
     * application is expected to launch the provided {@code cancelIntentSender} to cancel the
     * trust pairing.
     *
     * @param associationInfo The association for which trust pairing is canceled.
     * @param cancelIntentSender An {@link IntentSender} which the application should use to
     *                           launch in order to cancel the trust pairing dialog.
     */
    @MainThread
    @FlaggedApi(Flags.FLAG_TRUST_PAIRING)
    public void onTrustPairingCanceledFromRemote(
            @NonNull AssociationInfo associationInfo, @NonNull IntentSender cancelIntentSender) {
        // Do nothing. Companion apps can override this function.
    }

    /**
     * Called by the system when the device becomes trusted following a successful trust pairing.
     *
     * @param associationInfo The association that is now trusted.
     */
    @MainThread
    @FlaggedApi(Flags.FLAG_TRUST_PAIRING)
    public void onDeviceTrusted(@NonNull AssociationInfo associationInfo) {
        // Do nothing. Companion apps can override this function.
    }

    /**
     * Called by the system when trust pairing fails.
     *
     * @param associationInfo The association for which trust pairing failed.
     * @param errorCode The error code indicating the reason for failure.
     */
    @MainThread
    @FlaggedApi(Flags.FLAG_TRUST_PAIRING)
    public void onTrustPairingFailure(@NonNull AssociationInfo associationInfo,
            @CompanionDeviceManager.TrustPairingErrorCode int errorCode) {
        // Do nothing. Companion apps can override this function.
    }

    @Nullable
    @Override
    public final IBinder onBind(@NonNull Intent intent) {
        if (Objects.equals(intent.getAction(), SERVICE_INTERFACE)) {
            onBindCompanionDeviceService(intent);
            return mRemote;
        }
        Log.w(LOG_TAG,
                "Tried to bind to wrong intent (should be " + SERVICE_INTERFACE + "): " + intent);
        return null;
    }

    /**
     * Used to track the state of Binder connection in CTS tests.
     */
    @TestApi
    public void onBindCompanionDeviceService(@NonNull Intent intent) {
    }

    private class Stub extends ICompanionDeviceService.Stub {
        final Handler mMainHandler = Handler.getMain();
        final CompanionDeviceService mService = CompanionDeviceService.this;

        @Override
        public void onDeviceAppeared(AssociationInfo associationInfo) {
            mMainHandler.postAtFrontOfQueue(() -> mService.onDeviceAppeared(associationInfo));
        }

        @Override
        public void onDeviceDisappeared(AssociationInfo associationInfo) {
            mMainHandler.postAtFrontOfQueue(() -> mService.onDeviceDisappeared(associationInfo));
        }

        @Override
        public void onDevicePresenceEvent(DevicePresenceEvent event) {
            if (Flags.devicePresence()) {
                mMainHandler.postAtFrontOfQueue(() -> mService.onDevicePresenceEvent(event));
            }
        }

        @Override
        public void onActionRequested(@NonNull AssociationInfo association,
                 ActionRequest request) {
            if (Flags.enableDataSync()) {
                mMainHandler.postAtFrontOfQueue(
                        () -> mService.onActionRequested(association, request));
            }
        }

        @Override
        public void onTrustPairingResponsePending(@NonNull AssociationInfo association,
                 @NonNull IntentSender intentSender) {
            mMainHandler.postAtFrontOfQueue(
                    () -> mService.onTrustPairingResponsePending(association, intentSender));
        }

        @Override
        public void onTrustPairingCanceledFromRemote(@NonNull AssociationInfo association,
                 @NonNull IntentSender cancelIntentSender) {
            mMainHandler.postAtFrontOfQueue(
                    () -> mService.onTrustPairingCanceledFromRemote(association,
                            cancelIntentSender));
        }

        @Override
        public void onDeviceTrusted(@NonNull AssociationInfo association) {
            mMainHandler.postAtFrontOfQueue(
                    () -> mService.onDeviceTrusted(association));
        }

        @Override
        public void onTrustPairingFailure(@NonNull AssociationInfo association,
                 int errorCode) {
            mMainHandler.postAtFrontOfQueue(
                    () -> mService.onTrustPairingFailure(association, errorCode));
        }
    }
}
