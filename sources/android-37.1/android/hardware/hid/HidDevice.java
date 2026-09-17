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
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.RequiresPermission;
import android.content.Context;
import android.os.Binder;
import android.os.DeadObjectException;
import android.os.IBinder;
import android.os.OutcomeReceiver;
import android.os.RemoteException;
import android.os.ServiceSpecificException;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.BackgroundThread;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Represents a Human Interface Device (HID) connected to the system.
 *
 * <p>HIDs are devices like keyboards, mice, game controllers, and other peripherals that follow
 * the HID specification for communication over various transports (USB, Bluetooth, etc.).
 */
@FlaggedApi(FLAG_HID_API)
public final class HidDevice implements AutoCloseable {
    private static final String TAG = "HidDevice";

    /** Transport type is unknown. */
    public static final int TRANSPORT_UNKNOWN = HidTransport.UNKNOWN;
    /** Transport type is USB. */
    public static final int TRANSPORT_USB = HidTransport.USB;
    /** Transport type is Bluetooth. */
    public static final int TRANSPORT_BLUETOOTH = HidTransport.BLUETOOTH;
    /** Transport type is SPI. */
    public static final int TRANSPORT_SPI = HidTransport.SPI;
    /** Transport type is I2C. */
    public static final int TRANSPORT_I2C = HidTransport.I2C;
    /** Transport type is Virtual. */
    public static final int TRANSPORT_VIRTUAL = HidTransport.VIRTUAL;

    @Hide
    @IntDef(prefix = "TRANSPORT_", value = {
        TRANSPORT_UNKNOWN,
        TRANSPORT_USB,
        TRANSPORT_BLUETOOTH,
        TRANSPORT_SPI,
        TRANSPORT_I2C,
        TRANSPORT_VIRTUAL
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Transport {}

   /**
     * Internal state of the HID device connection.
     *
     * <p>This enum is used to manage the lifecycle of the device and ensure thread-safe
     * transitions between connection states, specifically to prevent race conditions
     * during asynchronous {@link #open} operations.
     */
    private enum State {
        /** The device is not connected or has been explicitly closed. */
        CLOSED,
        /** An asynchronous open operation is currently in progress. */
        OPENING,
        /** The device is successfully connected and ready for communication. */
        OPENED,
    }

    private final @NonNull Context mContext;
    private final @NonNull HidDeviceInfo mInfo;
    private final Object mDeviceLock = new Object();
    private final @NonNull IBinder mToken = new Binder();
    @GuardedBy("mDeviceLock")
    private final List<Pair<Executor,
            OutcomeReceiver<HidDevice, Exception>>> mPendingOpenCallbacks = new ArrayList<>();

    private volatile State mState = State.CLOSED;
    /**
     * Tracks the current lifecycle session of the device. Background tasks capture
     * this value at the start of an operation and verify it before committing state
     * changes to ensure they haven't been invalidated by a concurrent close or a
     * newer open request.
     */
    @GuardedBy("mDeviceLock")
    private int mOpenGeneration = 0;
    private @NonNull IHidManager mService;

    @Hide
    public HidDevice(@NonNull Context context, HidDeviceInfo info, @NonNull IHidManager manager) {
        mContext = Objects.requireNonNull(context);
        mInfo = Objects.requireNonNull(info);
        mService = Objects.requireNonNull(manager);

        // Validate transport.
        int transport = mInfo.transport;
        if (!(transport == TRANSPORT_UNKNOWN)
                && !(transport == TRANSPORT_USB)
                && !(transport == TRANSPORT_BLUETOOTH)
                && !(transport == TRANSPORT_SPI)
                && !(transport == TRANSPORT_I2C)
                && !(transport == TRANSPORT_VIRTUAL)) {
            throw new java.lang.IllegalArgumentException(
                    "transport was " + transport + " but must be one of: "
                            + "TRANSPORT_UNKNOWN(" + TRANSPORT_UNKNOWN + "), "
                            + "TRANSPORT_USB(" + TRANSPORT_USB + "), "
                            + "TRANSPORT_BLUETOOTH(" + TRANSPORT_BLUETOOTH + "), "
                            + "TRANSPORT_SPI(" + TRANSPORT_SPI + "), "
                            + "TRANSPORT_I2C(" + TRANSPORT_I2C + "), "
                            + "TRANSPORT_VIRTUAL(" + TRANSPORT_VIRTUAL + ")");
        }
    }

    @Hide
    public static String transportToString(@Transport int value) {
        return switch (value) {
            case TRANSPORT_UNKNOWN -> "TRANSPORT_UNKNOWN";
            case TRANSPORT_USB -> "TRANSPORT_USB";
            case TRANSPORT_BLUETOOTH -> "TRANSPORT_BLUETOOTH";
            case TRANSPORT_SPI -> "TRANSPORT_SPI";
            case TRANSPORT_I2C -> "TRANSPORT_I2C";
            case TRANSPORT_VIRTUAL -> "TRANSPORT_VIRTUAL";
            default -> Integer.toHexString(value);
        };
    }

    /**
     * The vendor ID for this device.
     *
     * <p>A vendor ID uniquely identifies the company who manufactured the device. A value of 0 will
     * be assigned where a vendor ID is not available.
     */
    public int getVendorId() {
        return mInfo.vendorId;
    }

    /**
     * The product ID for this device.
     *
     * <p>A product ID uniquely identifies which product within the address space of a given
     * vendor, identified by the device's vendor ID. A value of 0 will be assigned where a product
     * ID is not available.
     */
    public int getProductId() {
        return mInfo.productId;
    }

    /**
     * The transport type used by this device.
     */
    public @Transport int getTransport() {
        return mInfo.transport;
    }

    /**
     * The physical address of the device, representing its connection path through the system's
     * device tree (e.g., the USB hub and port path).
     *
     * <p>This value is unique to the device's physical connection point on the current system.
     */
    public @NonNull String getPhysicalAddress() {
        return mInfo.physicalAddress;
    }

    /**
     * A unique identifier for this device instance.
     *
     * <p>This value is typically the device's serial number if available, or a system-generated
     * unique ID. This ID is unique among all currently connected HID devices.
     */
    public @NonNull String getUniqueId() {
        return mInfo.uniqueId;
    }

    /**
     * The raw HID report descriptor for the device.
     *
     * <p>This descriptor defines the format and meaning of the reports sent and received by the
     * device, following the HID specification.
     */
    public @NonNull byte[] getReportDescriptor() {
        return mInfo.reportDescriptor;
    }

    /**
     * The human-readable name of the HID device.
     */
    public @NonNull String getName() {
        return mInfo.name;
    }

    @Override
    public boolean equals(@android.annotation.Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        @SuppressWarnings("unchecked")
        HidDevice that = (HidDevice) o;
        return Objects.equals(mInfo, that.mInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mInfo);
    }

    @Override
    public String toString() {
        return "HidDevice { "
                + "vendorId = " + mInfo.vendorId
                + ", productId = " + mInfo.productId
                + ", transport = " + transportToString(mInfo.transport)
                + ", physicalAddress = " + mInfo.physicalAddress
                + ", uniqueId = " + mInfo.uniqueId
                + ", reportDescriptor = " + java.util.Arrays.toString(mInfo.reportDescriptor)
                + ", name = " + mInfo.name
                + " }";
    }

    /**
     * Opens the HID device so that it can be used to send and receive HID reports.
     *
     * <p>This is an asynchronous operation. If an open operation is already in progress, the
     * provided callback is queued and will be notified once the underlying operation completes.
     * The state transitions are as follows:
     * <ul>
     *     <li>If the device is already <b>opened</b>, {@code callback.onResult} is
     *         invoked immediately.</li>
     *     <li>If the device is currently <b>opening</b>, the request is queued and
     *         {@code callback} will be notified when the existing operation finishes.</li>
     *     <li>If the device is <b>closed</b>, it transitions to an opening state and
     *         attempts to establish a connection via the system service.</li>
     * </ul>
     *
     * <p>On success, {@code callback.onResult} is called with this {@link HidDevice} instance.
     * On failure, {@code callback.onError} is called with one of the following:
     * <ul>
     *     <li>{@link IOException}: If the underlying hardware device (e.g., hidraw node)
     *         could not be accessed or the system service returned a failure.</li>
     *     <li>{@link SecurityException}: If the caller lacks the required permission, or the
     *         user has rejected access to the device.</li>
     *     <li>{@link RemoteException}: If the HID system service is unreachable.</li>
     * </ul>
     *
     * @param executor The executor on which the callback will be invoked.
     * @param callback The object to be notified of success or failure.
     */
    @RequiresPermission(Manifest.permission.ACCESS_HID)
    public void open(
            @NonNull @CallbackExecutor Executor executor,
            @NonNull OutcomeReceiver<HidDevice, Exception> callback) {
        Objects.requireNonNull(executor, "executor cannot be null");
        Objects.requireNonNull(callback, "callback cannot be null");

        final int generation;
        synchronized (mDeviceLock) {
            if (mState == State.OPENED) {
                executor.execute(() -> callback.onResult(this));
                return;
            }

            mPendingOpenCallbacks.add(Pair.create(executor, callback));

            if (mState == State.OPENING) {
                // An open operation is already in progress. The callback is now queued
                // and will be notified when the current operation finishes.
                return;
            }

            mState = State.OPENING;
            mOpenGeneration++;
            generation = mOpenGeneration;
        }

        try {
            BackgroundThread.getExecutor().execute(() -> {
                Exception error = null;
                boolean superseded = false;
                try {
                    // TODO(b/508113295): Update the service to include IBinder to detect the crash
                    // and cleanly close.
                    String callingPackage = mContext.getOpPackageName();
                    mService.open(mInfo, TextUtils.emptyIfNull(callingPackage), mToken);

                    synchronized (mDeviceLock) {
                        if (generation != mOpenGeneration) {
                            superseded = true;
                        }
                    }
                } catch (ServiceSpecificException e) {
                    error = toMappedException(e, "Failed to open HID device");
                } catch (Exception e) {
                    error = e;
                }

                if (superseded) {
                    try {
                        // Synchronous Binder IPC is performed outside the lock to prevent
                        // deadlocks and thread starvation if the system service is slow
                        // or attempts to call back into this process while the lock is held.
                        mService.close(mInfo);
                    } catch (RemoteException | SecurityException e) {
                        Log.e(TAG, "Error closing superseded device " + getDeviceLogId(), e);
                    }
                    return;
                }

                List<Pair<Executor, OutcomeReceiver<HidDevice, Exception>>> callbacksToNotify =
                        null;
                synchronized (mDeviceLock) {
                    if (generation == mOpenGeneration) {
                        if (error == null) {
                            mState = State.OPENED;
                        } else {
                            Log.e(TAG, "Error opening device " + getDeviceLogId(), error);
                            mState = State.CLOSED;
                        }
                        callbacksToNotify = new ArrayList<>(mPendingOpenCallbacks);
                        mPendingOpenCallbacks.clear();
                    }
                }

                if (callbacksToNotify != null) {
                    if (error == null) {
                        notifyCallbacksOfResult(callbacksToNotify, this);
                    } else {
                        notifyCallbacksOfError(callbacksToNotify, error);
                    }
                }
            });
        } catch (RuntimeException e) {
            List<Pair<Executor, OutcomeReceiver<HidDevice, Exception>>> callbacksToNotify = null;
            synchronized (mDeviceLock) {
                if (generation == mOpenGeneration) {
                    mState = State.CLOSED;
                    callbacksToNotify = new ArrayList<>(mPendingOpenCallbacks);
                    mPendingOpenCallbacks.clear();
                }
            }
            if (callbacksToNotify != null) {
                notifyCallbacksOfError(callbacksToNotify,
                        new IOException("Failed to schedule open operation", e));
            }
        }
    }

    /**
     * Returns whether the HID device is currently open and ready for communication.
     *
     * <p>Note: This method returns {@code false} while the device is in the process
     * of opening. It only returns {@code true} after the {@link #open} operation
     * has successfully completed.
     *
     * @return {@code true} if the device is fully opened; {@code false} otherwise.
     */
    public boolean isOpen() {
        // Volatile read is thread-safe without a lock for a simple equality check.
        return mState == State.OPENED;
    }

    /**
     * Get a feature report from the HID device.
     *
     * <p>The device must be opened first before calling this method.
     *
     * <p>On success, {@code callback.onResult} is called with the retrieved {@link Report}.
     * On failure, {@code callback.onError} is called with one of the following:
     * <ul>
     *     <li>{@link IOException}: If the underlying hardware device could not be accessed
     *         or the system service returned a failure.</li>
     *     <li>{@link SecurityException}: If the caller lacks the required permission.</li>
     * </ul>
     *
     * @param reportId the feature report to get.
     * @param executor the executor on which the callback will be invoked.
     * @param callback the callback object to be used to notify of the report or an error.
     * @throws IllegalStateException if the device is not open.
     */
    @RequiresPermission(Manifest.permission.ACCESS_HID)
    public void getFeatureReport(int reportId,
            @NonNull @CallbackExecutor Executor executor,
            @NonNull OutcomeReceiver<Report, Exception> callback) {
        Objects.requireNonNull(executor, "executor cannot be null");
        Objects.requireNonNull(callback, "callback cannot be null");

        if (!isOpen()) {
            throw new IllegalStateException("Device is not open");
        }
        BackgroundThread.getExecutor().execute(() -> {
            try {
                // TODO(b/502947755): Parse the report descriptor to retrieve report sizes and
                // ensure that feature, output, and input reports are within the valid range,
                // rather than using a hardcoded static limit.
                // TODO(b/512971550): Redesign the Binder interface to use the session
                // token or a dedicated session object for all report operations to
                // ensure proper authorization.
                AidlReport report = mService.getFeatureReport(mInfo, reportId);
                notifyCallbackOfResult(executor, callback,
                        new Report(report.reportId, java.nio.ByteBuffer.wrap(report.data)));
            } catch (ServiceSpecificException e) {
                notifyCallbackOfError(executor, callback,
                        toMappedException(e, "Failed to get feature report"));
            } catch (SecurityException e) {
                synchronized (mDeviceLock) {
                    mState = State.CLOSED;
                }
                notifyCallbackOfError(executor, callback, e);
            } catch (Exception e) {
                notifyCallbackOfError(executor, callback, e);
            }
        });
    }

    /**
     * Send a feature report to the HID device.
     *
     * <p>The device must be opened first before calling this method.
     *
     * <p>On success, {@code callback.onResult} is called with {@code null}.
     * On failure, {@code callback.onError} is called with one of the following:
     * <ul>
     *     <li>{@link IOException}: If the underlying hardware device could not be accessed
     *         or the system service returned a failure.</li>
     *     <li>{@link SecurityException}: If the caller lacks the required permission.</li>
     * </ul>
     *
     * @param report the feature report to send.
     * @param executor the executor on which the callback will be invoked.
     * @param callback the callback object to be used to notify of an error.
     * @throws IllegalStateException if the device is not open.
     */
    @RequiresPermission(Manifest.permission.ACCESS_HID)
    public void sendFeatureReport(@NonNull Report report,
            @NonNull @CallbackExecutor Executor executor,
            @NonNull OutcomeReceiver<Void, Exception> callback) {
        // TODO(b/512971550): Redesign the Binder interface to use the session token or a
        // dedicated session object for all report operations to ensure proper authorization.
        sendReportInternal(report, executor, callback, "feature",
                (aidlReport) -> mService.sendFeatureReport(mInfo, aidlReport));
    }

    /**
     * Send an output report to the HID device.
     *
     * <p>The device must be opened first before calling this method.
     *
     * <p>On success, {@code callback.onResult} is called with {@code null}.
     * On failure, {@code callback.onError} is called with one of the following:
     * <ul>
     *     <li>{@link IOException}: If the underlying hardware device could not be accessed
     *         or the system service returned a failure.</li>
     *     <li>{@link SecurityException}: If the caller lacks the required permission.</li>
     * </ul>
     *
     * @param report the output report to send.
     * @param executor the executor on which the callback will be invoked.
     * @param callback the callback object to be used to notify of an error.
     * @throws IllegalStateException if the device is not open.
     */
    @RequiresPermission(Manifest.permission.ACCESS_HID)
    public void sendOutputReport(@NonNull Report report,
            @NonNull @CallbackExecutor Executor executor,
            @NonNull OutcomeReceiver<Void, Exception> callback) {
        // TODO(b/512971550): Redesign the Binder interface to use the session token or a
        // dedicated session object for all report operations to ensure proper authorization.
        sendReportInternal(report, executor, callback, "output",
                (aidlReport) -> mService.sendOutputReport(mInfo, aidlReport));
    }

    @FunctionalInterface
    private interface BinderCall {
        void run(AidlReport report) throws RemoteException;
    }

    private void sendReportInternal(
            @NonNull Report report,
            @NonNull Executor executor,
            @NonNull OutcomeReceiver<Void, Exception> callback,
            @NonNull String reportType,
            @NonNull BinderCall binderCall) {
        Objects.requireNonNull(report, "report cannot be null");
        Objects.requireNonNull(executor, "executor cannot be null");
        Objects.requireNonNull(callback, "callback cannot be null");
        Objects.requireNonNull(reportType, "reportType cannot be null");
        Objects.requireNonNull(binderCall, "binderCall cannot be null");

        if (!isOpen()) {
            throw new IllegalStateException("Device is not open");
        }
        // TODO(b/502947755): Parse the report descriptor to retrieve report sizes and ensure that
        // feature, output, and input reports are within the valid range, rather than using a
        // hardcoded static limit.
        final AidlReport aidlReport = report.toAidlReport();
        BackgroundThread.getExecutor().execute(() -> {
            try {
                binderCall.run(aidlReport);
                notifyCallbackOfResult(executor, callback, null);
            } catch (ServiceSpecificException e) {
                notifyCallbackOfError(executor, callback,
                        toMappedException(e, "Failed to send " + reportType + " report"));
            } catch (SecurityException e) {
                synchronized (mDeviceLock) {
                    mState = State.CLOSED;
                }
                notifyCallbackOfError(executor, callback, e);
            } catch (Exception e) {
                notifyCallbackOfError(executor, callback, e);
            }
        });
    }

    /**
     * Releases all system resources and closes the connection to the HID device.
     *
     * <p>This method is synchronous and idempotent; calling it on an already closed
     * device has no effect.
     */
    @Override
    @RequiresPermission(Manifest.permission.ACCESS_HID)
    public void close() {
        List<Pair<Executor, OutcomeReceiver<HidDevice, Exception>>> callbacksToNotify = null;
        boolean shouldCloseService = false;

        synchronized (mDeviceLock) {
            if (mState == State.CLOSED) return;

            if (mState == State.OPENING) {
                mState = State.CLOSED;
            }
            mOpenGeneration++;

            if (!mPendingOpenCallbacks.isEmpty()) {
                callbacksToNotify = new ArrayList<>(mPendingOpenCallbacks);
                mPendingOpenCallbacks.clear();
            }
            shouldCloseService = true;
        }

        if (callbacksToNotify != null) {
            notifyCallbacksOfError(callbacksToNotify,
                    new IOException("Device closed while open was pending"));
        }

        if (shouldCloseService) {
            try {
                // TODO(b/508113295): Update the service to include IBinder to detect the crash
                // and cleanly close.
                mService.close(mInfo);
                synchronized (mDeviceLock) {
                    mState = State.CLOSED;
                }
            } catch (DeadObjectException e) {
                synchronized (mDeviceLock) {
                    // If service process has crashed, the connection is effectively dead.
                    // We transition to CLOSED to allow the local handle to be cleaned up.
                    mState = State.CLOSED;
                }
                throw e.rethrowAsRuntimeException();
            } catch (RemoteException e) {
                // Maintains OPENED state if test relies on exceptions preventing closure.
                throw e.rethrowAsRuntimeException();
            } catch (SecurityException e) {
                synchronized (mDeviceLock) {
                    mState = State.CLOSED;
                }
                throw e;
            }
        }
    }

    /**
     * Maps service-specific error codes from the HID system service to standard Java exceptions.
     *
     * <p>This helper method translates the integer error codes defined in {@code IHidManager}
     * into standard exceptions to provide callers with specific and actionable error information.
     *
     * @param e The service-specific exception thrown by the HID system service.
     * @param defaultMessage A descriptive message providing context for the operation that failed.
     * @return A standard Java exception representing the specific service error.
     */
    private static Exception toMappedException(ServiceSpecificException e, String defaultMessage) {
        return switch (e.errorCode) {
            case IHidManager.ERROR_DEVICE_NOT_FOUND -> {
                FileNotFoundException fne =
                        new FileNotFoundException("Device not found: " + e.getMessage());
                fne.initCause(e);
                yield fne;
            }
            case IHidManager.ERROR_DEVICE_MISMATCH ->
                    new IllegalArgumentException("Device mismatch: " + e.getMessage(), e);
            case IHidManager.ERROR_INVALID_ARGUMENT ->
                    new IllegalArgumentException(e.getMessage(), e);
            case IHidManager.ERROR_INVALID_STATE ->
                    new IllegalStateException(e.getMessage(), e);
            case IHidManager.ERROR_IO_ERROR ->
                    new IOException(e.getMessage(), e);
            case IHidManager.ERROR_INTERNAL_ERROR ->
                    new IOException(defaultMessage + ": Internal error: " + e.getMessage(), e);
            default ->
                    new IOException(defaultMessage + ": " + e.getMessage(), e);
        };
    }

    /**
     * Dispatches the successful open result to a single callback on its executor.
     */
    private <T> void notifyCallbackOfResult(
            @NonNull Executor executor,
            @NonNull OutcomeReceiver<T, Exception> callback,
            @NonNull T result) {
        try {
            executor.execute(() -> callback.onResult(result));
        } catch (RuntimeException e) {
            Log.e(TAG, "User-provided executor failed for " + getDeviceLogId(), e);
        }
    }

    /**
     * Dispatches an error to a single callback on its executor.
     */
    private <T> void notifyCallbackOfError(
            @NonNull Executor executor,
            @NonNull OutcomeReceiver<T, Exception> callback,
            @NonNull Exception error) {
        try {
            executor.execute(() -> callback.onError(error));
        } catch (RuntimeException e) {
            Log.e(TAG, "User-provided executor failed for " + getDeviceLogId(), e);
        }
    }

    /**
     * Dispatches the successful open result to the provided list of callbacks.
     *
     * <p>Each callback is invoked on its associated {@link Executor}. If an executor
     * throws a {@link RuntimeException} (e.g., if the executor is saturated and rejects
     * the task), the error is logged and the remaining callbacks in the list continue
     * to be processed.
     *
     * @param callbacks The list of executor-callback pairs to be notified.
     * @param result The successfully opened {@link HidDevice} instance.
     */
    private <T> void notifyCallbacksOfResult(
            @NonNull List<Pair<Executor, OutcomeReceiver<T, Exception>>> callbacks,
            @NonNull T result) {
        for (Pair<Executor, OutcomeReceiver<T, Exception>> request : callbacks) {
            notifyCallbackOfResult(request.first, request.second, result);
        }
    }

    /**
     * Dispatches an error to the provided list of callbacks.
     *
     * <p>Each callback is invoked on its associated {@link Executor}. If an executor
     * throws a {@link RuntimeException}, the error is logged and the remaining
     * callbacks in the list continue to be processed.
     *
     * @param callbacks The list of executor-callback pairs to be notified.
     * @param error The exception representing the failure to be delivered.
     */
    private <T> void notifyCallbacksOfError(
            @NonNull List<Pair<Executor, OutcomeReceiver<T, Exception>>> callbacks,
            @NonNull Exception error) {
        for (Pair<Executor, OutcomeReceiver<T, Exception>> request : callbacks) {
            notifyCallbackOfError(request.first, request.second, error);
        }
    }

    /**
     * Returns a string identifier for this device suitable for logging.
     *
     * <p>The identifier includes the Vendor ID and Product ID formatted as a
     * hexadecimal pair, followed by the device's unique ID.
     * Example format: "1234:5678 (UniqueID: serial_number)".
     *
     * @return A formatted string identifying the device.
     */
    private String getDeviceLogId() {
        return TextUtils.formatSimple("%04x:%04x", mInfo.vendorId, mInfo.productId)
                + " (UniqueID: " + mInfo.uniqueId + ")";
    }
}
