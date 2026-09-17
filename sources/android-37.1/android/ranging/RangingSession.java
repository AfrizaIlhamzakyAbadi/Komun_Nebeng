/*
 * Copyright 2024 The Android Open Source Project
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

package android.ranging;

import android.Manifest;
import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.IntRange;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresPermission;
import android.annotation.TestApi;
import android.content.AttributionSource;
import android.os.CancellationSignal;
import android.os.IBinder;
import android.os.RemoteException;
import android.ranging.oob.DeviceHandle;
import android.ranging.oob.OobHandle;
import android.ranging.oob.OobInitiatorRangingConfig;
import android.ranging.oob.OobResponderRangingConfig;
import android.ranging.oob.TransportHandle;
import android.ranging.raw.RawDtTagRangingConfig;
import android.ranging.raw.RawInitiatorRangingConfig;
import android.ranging.raw.RawRangingDevice;
import android.ranging.raw.RawResponderRangingConfig;
import android.ranging.uwb.DlTdoaRangingParams;
import android.util.Log;

import com.android.ranging.flags.Flags;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Represents a session for performing ranging operations. A {@link RangingSession} manages
 * the lifecycle of a ranging operation, including start, stop, and event callbacks.
 *
 * <p>All methods are asynchronous and rely on the provided {@link Executor} to invoke
 * callbacks on appropriate threads.
 *
 * <p>This class implements {@link AutoCloseable}, ensuring that resources can be
 * automatically released when the session is closed.
 *
 */
@FlaggedApi(Flags.FLAG_RANGING_STACK_ENABLED)
public final class RangingSession implements AutoCloseable {
    private static final String TAG = "RangingSession";
    private final AttributionSource mAttributionSource;
    private final SessionHandle mSessionHandle;
    private final IRangingAdapter mRangingAdapter;
    private final RangingSessionManager mRangingSessionManager;
    private final Callback mCallback;
    private final Executor mExecutor;
    private final Map<RangingDevice, android.ranging.oob.TransportHandle> mTransportHandles =
            new ConcurrentHashMap<>();
    private @Nullable Integer mDlTdoaSessionId = null;
    private boolean mClosePending = false;
    private boolean mIsClosed = false;
    private final IBinder.DeathRecipient mDeathRecipient = new IBinder.DeathRecipient() {
        @Override
        public void binderDied() {
            Log.e(TAG, "Ranging service died");
            mIsClosed = true;
            destroyTransportHandles();
        }
    };

    @Hide
    public RangingSession(RangingSessionManager rangingSessionManager,
            AttributionSource attributionSource,
            SessionHandle sessionHandle, IRangingAdapter rangingAdapter,
            Callback callback, Executor executor) {
        mRangingSessionManager = rangingSessionManager;
        mAttributionSource = attributionSource;
        mSessionHandle = sessionHandle;
        mRangingAdapter = rangingAdapter;
        mCallback = callback;
        mExecutor = executor;
        try {
            mRangingAdapter.asBinder().linkToDeath(mDeathRecipient, 0);
        } catch (RemoteException e) {
            Log.e(TAG, "Failed to link to death recipient", e);
        }
    }

    /**
     * Starts the ranging session with the provided ranging preferences.
     *
     * <p>The {@link Callback#onOpened()} will be called when the session finishes starting.
     *
     * <p>The provided {@link RangingPreference} determines the configuration for the session. A
     * {@link CancellationSignal} is returned to allow the caller to cancel the session if needed.
     * If the session is canceled, the {@link #close()} method will be invoked automatically to
     * release resources.
     *
     * <p>This method conditionally requires one or more permissions depending on the
     * technologies configured in the {@code rangingPreference}:
     * <ul>
     *     <li>{@link Manifest.permission#RANGING} is required for UWB, BLE, or RTT ranging
     *     sessions.</li>
     *     <li>{@link Manifest.permission#ACCESS_FINE_LOCATION} is required for Location-based or
     *     UWB DL-TDoA ranging sessions.</li>
     *     <li>{@link Manifest.permission#ACCESS_BACKGROUND_LOCATION} is additionally required if
     *     Location-based or UWB DL-TDoA ranging is initiated or continues while the application is
     *     in the background.</li>
     * </ul>
     *
     * @param rangingPreference {@link RangingPreference} the preferences for configuring the
     *     ranging session.
     * @return a {@link CancellationSignal} to close the session.
     */
    // Adding an annotation that affects the Javadoc, for example RequiresPermission,
    // RestrictedForEnvironment, etc. is not supported by the flagging infra.
    @SuppressWarnings("UnflaggedApi")
    @RequiresPermission(
            anyOf = {
                Manifest.permission.RANGING,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            },
            conditional = true)
    @NonNull
    public CancellationSignal start(@NonNull RangingPreference rangingPreference) {
        if (rangingPreference.getRangingParams().getRangingSessionType()
                == RangingConfig.RANGING_SESSION_OOB) {
            setupTransportHandles(rangingPreference);
        } else if (rangingPreference.getRangingParams().getRangingSessionType()
                == RangingConfig.RANGING_SESSION_RAW) {
            setupRawTransportHandles(rangingPreference);
        }
        Log.v(TAG, "Start ranging - " + mSessionHandle);
        try {
            mRangingAdapter.startRanging(mAttributionSource, mSessionHandle, rangingPreference,
                    mRangingSessionManager);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
        if (rangingPreference.getDeviceRole() == RangingPreference.DEVICE_ROLE_DT_TAG &&
                rangingPreference.getRangingParams() instanceof RawDtTagRangingConfig config) {
            assert config.getDtTag().getDlTdoaRangingParams() != null;
            mDlTdoaSessionId = config.getDtTag().getDlTdoaRangingParams().getSessionId();
        }
        CancellationSignal cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(this::close);

        return cancellationSignal;
    }

    private void setupTransportHandles(RangingPreference rangingPreference) {
        List<DeviceHandle> deviceHandleList = new ArrayList<>();
        if (rangingPreference.getRangingParams() instanceof OobInitiatorRangingConfig) {
            deviceHandleList.addAll(((OobInitiatorRangingConfig)
                    rangingPreference.getRangingParams()).getDeviceHandles());
        } else if (rangingPreference.getRangingParams() instanceof OobResponderRangingConfig) {
            deviceHandleList.add(((OobResponderRangingConfig)
                    rangingPreference.getRangingParams()).getDeviceHandle());
        }
        for (DeviceHandle deviceHandle : deviceHandleList) {
            TransportHandleReceiveCallback receiveCallback =
                    new TransportHandleReceiveCallback(deviceHandle.getRangingDevice());
            deviceHandle.getTransportHandle().registerReceiveCallback(
                    Executors.newCachedThreadPool(), receiveCallback);
            mTransportHandles.put(deviceHandle.getRangingDevice(),
                    deviceHandle.getTransportHandle());
        }
        mRangingSessionManager.registerOobSendDataListener();
    }

    private void setupRawTransportHandles(RangingPreference preference) {
        List<RawRangingDevice> rawDevices = new ArrayList<>();
        if (preference.getRangingParams() instanceof RawInitiatorRangingConfig config) {
            rawDevices.addAll(config.getRawRangingDevices());
        } else if (preference.getRangingParams() instanceof RawResponderRangingConfig config) {
            rawDevices.add(config.getRawRangingDevice());
        }

        boolean hasLocationRaw = false;
        for (RawRangingDevice rawDevice : rawDevices) {
            if (rawDevice.getLocationRangingParams() != null) {
                hasLocationRaw = true;
                setupLocationTransport(rawDevice);
            }
        }

        if (hasLocationRaw) {
            mRangingSessionManager.registerOobSendDataListener();
        }
    }

    private void setupLocationTransport(RawRangingDevice rawDevice) {
        if (rawDevice.getLocationRangingParams() == null) {
            return;
        }
        TransportHandle transport = rawDevice.getLocationRangingParams().getTransportHandle();
        if (transport != null) {
            TransportHandleReceiveCallback receiveCallback =
                    new TransportHandleReceiveCallback(rawDevice.getRangingDevice());
            transport.registerReceiveCallback(
                    Executors.newCachedThreadPool(), receiveCallback);
            mTransportHandles.put(rawDevice.getRangingDevice(), transport);
        }
    }

    /**
     * Adds a new device to an ongoing ranging session.
     * <p>
     * This method allows for adding a new device to an active ranging session using raw ranging
     * parameters. Only devices represented by {@link RawResponderRangingConfig} is supported.
     * If the provided {@link RangingConfig} does not match one of these types, the addition fails
     * and invokes {@link Callback#onOpenFailed(int)} with a reason of
     * {@link Callback#REASON_UNSUPPORTED}.
     * </p>
     *
     * @param deviceRangingParams the ranging parameters for the device to be added,
     *                            which must be an instance of {@link RawResponderRangingConfig}
     * @apiNote If the underlying ranging technology cannot support this dynamic addition, failure
     * will be indicated via {@code Callback#onStartFailed(REASON_UNSUPPORTED, RangingDevice)}
     *
     */
    @RequiresPermission(Manifest.permission.RANGING)
    public void addDeviceToRangingSession(@NonNull RangingConfig deviceRangingParams) {
        Log.v(TAG, " Add device - " + mSessionHandle);
        try {
            if (deviceRangingParams instanceof RawResponderRangingConfig) {
                RawResponderRangingConfig rawConfig =
                        (RawResponderRangingConfig) deviceRangingParams;
                RawRangingDevice rawDevice = rawConfig.getRawRangingDevice();
                setupLocationTransport(rawDevice);
                mRangingAdapter.addRawDevice(mSessionHandle, rawConfig);
            } else {
                mCallback.onOpenFailed(Callback.REASON_UNSUPPORTED);
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Removes a specific device from an ongoing ranging session.
     * <p>
     * This method removes a specified device from the active ranging session, stopping
     * further ranging operations for that device. The operation is handled by the system
     * server and may throw a {@link RemoteException} in case of server-side communication
     * issues.
     * </p>
     *
     * @param rangingDevice the device to be removed from the session.
     * @apiNote Currently, this API is supported only for UWB multicast session if using
     * {@link RangingConfig#RANGING_SESSION_RAW}.
     *
     */
    @RequiresPermission(Manifest.permission.RANGING)
    public void removeDeviceFromRangingSession(@NonNull RangingDevice rangingDevice) {
        Log.v(TAG, " Remove device - " + mSessionHandle);
        try {
            mRangingAdapter.removeDevice(mSessionHandle, rangingDevice);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Reconfigures the ranging interval for the current session by setting the interval
     * skip count. The {@code intervalSkipCount} defines how many intervals should be skipped
     * between successive ranging rounds. Valid values range from 0 to 255.
     *
     * @param intervalSkipCount the number of intervals to skip, ranging from 0 to 255.
     */
    @RequiresPermission(Manifest.permission.RANGING)
    public void reconfigureRangingInterval(@IntRange(from = 0, to = 255) int intervalSkipCount) {
        Log.v(TAG, " Reconfiguring ranging interval - " + mSessionHandle);
        try {
            mRangingAdapter.reconfigureRangingInterval(mSessionHandle, intervalSkipCount);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Configures the active ranging round indexes of DT-Tag using FiRa Out-of-Band (OOB) packets.
     *
     * <p><b>Preferred Over {@link #updateDlTdoaRangingRounds(byte[])}:</b> This method is the
     * preferred way to update ranging rounds. It accepts the list of all DT-Anchor OOB packets the
     * app sends, automatically parses them to extract the active ranging round indexes, and applies
     * the configuration.
     *
     * <p>Upon completion, the result of the update will be notified via {@link
     * Callback#onDlTdoaRangingRoundIndexesUpdated(byte[])} with the list of successfully activated
     * ranging round indexes.
     *
     * @param firaOobDtPackets The list of raw FiRa OOB packets containing the ranging round
     *     configuration.
     * @apiNote This overrides all the existing active ranging rounds, the app needs to send OOB
     *     packets from all DT-Anchors required for the DL-TDoA measurement.
     * @see #updateDlTdoaRangingRounds(byte[])
     * @see <a href="https://groups.firaconsortium.org/wg/FPSG/document/5944">FiRa Specific OOB
     * Profile Advertisement Message</a> for the configuration packet format.
     */
    @RequiresPermission(Manifest.permission.RANGING)
    @FlaggedApi(Flags.FLAG_RANGING_STACK_UPDATES_26_Q_4)
    public void updateDlTdoaRangingRoundsFiraPacket(@NonNull List<byte[]> firaOobDtPackets) {
        Objects.requireNonNull(firaOobDtPackets);
        if (mDlTdoaSessionId == null) {
            throw new IllegalStateException(TAG + " Dl TDoA sessionId is not set");
        }
        byte[] rangingRoundIndexes = DlTdoaRangingParams.getDlTdoaRangingRoundsFiraPacket(
                        firaOobDtPackets, mDlTdoaSessionId);
        if (rangingRoundIndexes == null || rangingRoundIndexes.length == 0) {
            Log.w(TAG, "No ranging round indexes found in FiRa OOB packets");
            return;
        }
        updateDlTdoaRangingRounds(rangingRoundIndexes);
    }

    /**
     * Configures the active ranging round indexes of DT-Tag.
     *
     * <p>The UWB subsystem (UWBS) of the DT-Tag will turn on its receiver during the configured
     * active ranging rounds to listen for DT-Anchor messages. This allows the DT-Tag to selectively
     * choose which rounds to listen to. The app needs to monitor and update the DT-Tag ranging
     * rounds for multi-cluster setups
     *
     * <p>Upon completion, the result of the update will be notified via {@link
     * Callback#onDlTdoaRangingRoundIndexesUpdated(byte[])} with the list of successfully activated
     * ranging round indexes.
     *
     * @param rangingRoundIndexes The active ranging round indexes to configure.
     * @apiNote This overrides all the existing active ranging rounds, so app needs to set all
     *     ranging round indexes including the ones that were previously enabled.
     * @see #updateDlTdoaRangingRoundsFiraPacket(List)
     */
    @RequiresPermission(Manifest.permission.RANGING)
    @FlaggedApi(Flags.FLAG_RANGING_STACK_UPDATES_26_Q_4)
    public void updateDlTdoaRangingRounds(@NonNull byte[] rangingRoundIndexes) {
        Objects.requireNonNull(rangingRoundIndexes);
        if (rangingRoundIndexes.length == 0) {
            throw new IllegalArgumentException(TAG + " No ranging round indexes found");
        }
        Log.v(TAG, "updateDTRangingRounds - " + mSessionHandle);
        try {
            mRangingAdapter.updateRangingRoundsDtTag(mSessionHandle, rangingRoundIndexes);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Stops the ranging session.
     *
     * <p>This method releases any ongoing ranging operations. If the operation fails,
     * it will propagate a {@link RemoteException} from the system server.
     */
    @RequiresPermission(Manifest.permission.RANGING)
    public void stop() {
        Log.v(TAG, "Stop ranging - " + mSessionHandle);
        try {
            mRangingAdapter.stopRanging(mSessionHandle);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Updates the simulated foreground state of the application for this ranging session.
     *
     * <p>This method allows testing environments to simulate the application transitioning between
     * foreground and background states, enabling verification of the ranging stack's behavior
     * during app lifecycle changes. If the operation fails, it will propagate a
     * {@link RemoteException} from the system server.
     *
     * @param appInForeground {@code true} to simulate the application moving to the foreground,
     *                        {@code false} to simulate it moving to the background.
     */
    @TestApi
    @RequiresPermission(Manifest.permission.RANGING)
    public void updateForegroundStateForTesting(boolean appInForeground) {
        Log.v(TAG, "Update foreground state - " + mSessionHandle + " appInForeground: "
                + appInForeground);
        try {
            mRangingAdapter.updateForegroundStateForTesting(mSessionHandle, appInForeground);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Injects simulated ranging data for testing.
     *
     * <p>Once any data has been injected for a device, the session goes into test mode
     * permanently for that device, and real ranging data will no longer be reported.
     *
     * @param device The device for which to inject data.
     * @param data The list of simulated ranging data.
     */
    @FlaggedApi(Flags.FLAG_RANGING_STACK_ENABLED)
    @TestApi
    @RequiresPermission(Manifest.permission.RANGING)
    public void injectDataForTesting(
            @NonNull RangingDevice device, @NonNull List<RangingData> data) {
        Log.v(TAG, "Inject ranging data - " + mSessionHandle);
        try {
            mRangingAdapter.injectDataForTesting(mSessionHandle, device, data);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    @Hide
    public void onOpened() {
        mExecutor.execute(mCallback::onOpened);
    }

    @Hide
    public void onOpenFailed(@Callback.Reason int reason) {
        mIsClosed = true;
        mExecutor.execute(() -> {
            mCallback.onOpenFailed(reason);
            if (mClosePending) {
                destroyTransportHandles();
            }
        });
    }

    @Hide
    public void onStarted(RangingDevice peer, @RangingManager.RangingTechnology int technology) {
        mExecutor.execute(() -> mCallback.onStarted(peer, technology));
    }

    @Hide
    public void onResults(RangingDevice peer, RangingData data) {
        mExecutor.execute(() -> mCallback.onResults(peer, data));
    }

    @Hide
    public void onDlTdoaResults(RangingDevice peer, DlTdoaMeasurement measurement) {
        mExecutor.execute(() -> mCallback.onDlTdoaResults(peer, measurement));
    }

    @Hide
    public void onMotionReceived(@NonNull RangingDevice peer, @NonNull MotionState motion) {
        mExecutor.execute(() -> mCallback.onMotionReceived(peer, motion));
    }

    @Hide
    public void onDtRangingRoundsUpdated(byte[] rangingRoundIndexes) {
        mExecutor.execute(() -> mCallback.onDlTdoaRangingRoundIndexesUpdated(rangingRoundIndexes));
    }

    @Hide
    public void onStopped(RangingDevice peer, @RangingManager.RangingTechnology int technology) {
        mExecutor.execute(() -> mCallback.onStopped(peer, technology));
    }

    @Hide
    public void onClosed(@Callback.Reason int reason) {
        mIsClosed = true;
        mExecutor.execute(() -> {
            mCallback.onClosed(reason);
            if (mClosePending) {
                destroyTransportHandles();
            }
        });
    }

    @Hide
    void sendOobData(RangingDevice toDevice, byte[] data) {
        android.ranging.oob.TransportHandle handle = mTransportHandles.get(toDevice);
        if (handle != null) {
            handle.sendData(data);
        } else {
            Log.e(TAG, "TransportHandle not found for session: " + mSessionHandle + ", device: "
                    + toDevice);
        }
    }

    @RequiresPermission(Manifest.permission.RANGING)
    @Override
    public void close() {
        stop();
        mClosePending = true;
        if (mIsClosed) {
            destroyTransportHandles();
        }
    }

    private void destroyTransportHandles() {
        try {
            mRangingAdapter.asBinder().unlinkToDeath(mDeathRecipient, 0);
        } catch (Exception e) {
            // ignore
        }
        mTransportHandles.values().forEach(transportHandle -> {
            try {
                transportHandle.close();
            } catch (Exception e) {
                Log.e(TAG, "Failed to close transport handle", e);
            }
        });
        mTransportHandles.clear();
    }

    /**
     * Callback interface for receiving ranging session events.
     */
    public interface Callback {

        @Hide
        @Retention(RetentionPolicy.SOURCE)
        @Target({ElementType.TYPE_USE})
        @IntDef(value = {
                REASON_UNKNOWN,
                REASON_LOCAL_REQUEST,
                REASON_REMOTE_REQUEST,
                REASON_UNSUPPORTED,
                REASON_SYSTEM_POLICY,
                REASON_NO_PEERS_FOUND,
        })
        @interface Reason {
        }

        /**
         * Indicates that the session was closed due to an unknown reason.
         */
        int REASON_UNKNOWN = 0;

        /**
         * Indicates that the session was closed because {@link AutoCloseable#close()} or
         * {@link RangingSession#stop()} was called.
         */
        int REASON_LOCAL_REQUEST = 1;

        /**
         * Indicates that the session was closed at the request of a remote peer.
         */
        int REASON_REMOTE_REQUEST = 2;

        /**
         * Indicates that the session closed because the provided session parameters were not
         * supported.
         */
        int REASON_UNSUPPORTED = 3;

        /**
         * Indicates that the local system policy forced the session to close, such
         * as power management policy, airplane mode etc.
         */
        int REASON_SYSTEM_POLICY = 4;

        /**
         * Indicates that the session was closed because none of the specified peers were found.
         */
        int REASON_NO_PEERS_FOUND = 5;

        /**
         * Called when the ranging session opens successfully.
         */
        void onOpened();

        /**
         * Called when the ranging session failed to open.
         *
         * @param reason the reason for the failure, limited to values defined by
         *               {@link Reason}.
         */
        void onOpenFailed(@Reason int reason);

        /**
         * Called when ranging has started with a particular peer using a particular technology
         * during an ongoing session.
         *
         * @param peer       {@link RangingDevice} the peer with which ranging has started.
         * @param technology {@link android.ranging.RangingManager.RangingTechnology}
         *                   the ranging technology that started.
         */
        void onStarted(
                @NonNull RangingDevice peer, @RangingManager.RangingTechnology int technology);

        /**
         * Called when ranging data has been received from a peer.
         *
         * @param peer {@link RangingDevice} the peer from which ranging data was received.
         * @param data {@link RangingData} the received data.
         */
        void onResults(@NonNull RangingDevice peer, @NonNull RangingData data);

        /**
         * Called when DL-TDOA measurement data has been received from a peer.
         *
         * <p>Only invoked when a DL-TDOA session has been started (i.e., {@link
         * RangingPreference#getDeviceRole()} returns {@link RangingPreference#DEVICE_ROLE_DT_TAG}).
         *
         * @param peer {@link RangingDevice} the peer from which ranging data was received.
         * @param measurement {@link DlTdoaMeasurement} the received measurement.
         */
        @FlaggedApi(Flags.FLAG_RANGING_STACK_UPDATES_26_Q_2)
        default void onDlTdoaResults(
                @NonNull RangingDevice peer, @NonNull DlTdoaMeasurement measurement) {}

        /**
         * Called when the peer's motion state is received when a motion is detected from the
         * previous reported motion change.
         *
         * @param peer The peer device whose movement state has reported.
         * @param motion The new motion state of the peer.
         */
        @FlaggedApi(Flags.FLAG_RANGING_STACK_UPDATES_26_Q_2)
        default void onMotionReceived(@NonNull RangingDevice peer, @NonNull MotionState motion) {}

        /**
         * Called when the ranging round indexes for DT-Tag are updated.
         *
         * @param rangingRoundIndexes the updated ranging round indexes.
         */
        @FlaggedApi(Flags.FLAG_RANGING_STACK_UPDATES_26_Q_4)
        default void onDlTdoaRangingRoundIndexesUpdated(@NonNull byte[] rangingRoundIndexes) {}

        /**
         * Called when ranging has stopped with a particular peer using a particular technology
         * during an ongoing session.
         *
         * @param peer       {@link RangingDevice} the peer with which ranging has stopped.
         * @param technology {@link android.ranging.RangingManager.RangingTechnology}
         *                   the ranging technology that stopped.
         */
        void onStopped(
                @NonNull RangingDevice peer, @RangingManager.RangingTechnology int technology);

        /**
         * Called when the ranging session has closed.
         *
         * @param reason the reason why the session was closed, limited to values
         *               defined by {@link Reason}.
         */
        void onClosed(@Reason int reason);

    }

    class TransportHandleReceiveCallback implements TransportHandle.ReceiveCallback {

        private final android.ranging.oob.OobHandle mOobHandle;

        TransportHandleReceiveCallback(RangingDevice device) {
            mOobHandle = new OobHandle(mSessionHandle, device);
        }

        @Override
        public void onReceiveData(byte[] data) {
            mRangingSessionManager.oobDataReceived(mOobHandle, data);
        }

        @Override
        public void onSendFailed() {
        }

        @Override
        public void onDisconnect() {
            mRangingSessionManager.deviceOobDisconnected(mOobHandle);
        }

        @Override
        public void onReconnect() {
            mRangingSessionManager.deviceOobReconnected(mOobHandle);
        }

        @Override
        public void onClose() {
            mRangingSessionManager.deviceOobClosed(mOobHandle);
        }
    }

    @Override
    public String toString() {
        return "RangingSession{ "
                + "mSessionHandle="
                + mSessionHandle
                + ", mRangingAdapter="
                + mRangingAdapter
                + ", mRangingSessionManager="
                + mRangingSessionManager
                + ", mCallback="
                + mCallback
                + ", mExecutor="
                + mExecutor
                + ", mTransportHandles="
                + mTransportHandles
                + " }";
    }
}
