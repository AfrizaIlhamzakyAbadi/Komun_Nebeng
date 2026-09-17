/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package android.bluetooth.le;

import static android.Manifest.permission.BLUETOOTH_PRIVILEGED;
import static android.Manifest.permission.BLUETOOTH_SCAN;
import static android.Manifest.permission.UPDATE_DEVICE_STATS;
import static android.bluetooth.BluetoothUtils.executeFromBinder;
import static android.bluetooth.BluetoothUtils.logRemoteException;
import static android.bluetooth.le.ScanCallback.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED;
import static android.bluetooth.le.ScanCallback.SCAN_FAILED_SCANNING_TOO_FREQUENTLY;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.Objects.requireNonNullElseGet;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresNoPermission;
import android.annotation.RequiresPermission;
import android.annotation.SystemApi;
import android.app.PendingIntent;
import android.bluetooth.Attributable;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.IBluetoothScan;
import android.bluetooth.annotations.RequiresBluetoothLocationPermission;
import android.bluetooth.annotations.RequiresBluetoothScanPermission;
import android.bluetooth.annotations.RequiresLegacyBluetoothAdminPermission;
import android.content.AttributionSource;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.os.WorkSource;
import android.util.Log;

import com.android.bluetooth.flags.Flags;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class provides methods to perform scan related operations for Bluetooth LE devices. An
 * application can scan for a particular type of Bluetooth LE devices using {@link ScanFilter}. It
 * can also request different types of callbacks for delivering the result.
 *
 * <p>Use {@link BluetoothAdapter#getBluetoothLeScanner()} to get an instance of {@link
 * BluetoothLeScanner}.
 *
 * @see ScanFilter
 */
public final class BluetoothLeScanner {
    private static final String TAG = BluetoothLeScanner.class.getSimpleName();

    private static final boolean VDBG = Log.isLoggable("bluetooth", Log.VERBOSE);

    /**
     * Extra containing a list of ScanResults. It can have one or more results if there was no
     * error. In case of error, {@link #EXTRA_ERROR_CODE} will contain the error code and this extra
     * will not be available.
     */
    public static final String EXTRA_LIST_SCAN_RESULT =
            "android.bluetooth.le.extra.LIST_SCAN_RESULT";

    /**
     * Optional extra indicating the error code, if any. The error code will be one of the
     * SCAN_FAILED_* codes in {@link ScanCallback}.
     */
    public static final String EXTRA_ERROR_CODE = "android.bluetooth.le.extra.ERROR_CODE";

    /**
     * Optional extra indicating the callback type, which will be one of CALLBACK_TYPE_* constants
     * in {@link ScanSettings}.
     *
     * @see ScanCallback#onScanResult(int, ScanResult)
     */
    public static final String EXTRA_CALLBACK_TYPE = "android.bluetooth.le.extra.CALLBACK_TYPE";

    private final Map<ScanCallback, CallbackWrapper> mScanClients = new ConcurrentHashMap<>();

    private final BluetoothAdapter mAdapter;
    private final AttributionSource mSource;
    private final Handler mHandler;

    /** Use {@link BluetoothAdapter#getBluetoothLeScanner()} instead. */
    @Hide
    public BluetoothLeScanner(BluetoothAdapter bluetoothAdapter) {
        mAdapter = requireNonNull(bluetoothAdapter);
        mSource = mAdapter.getAttributionSource();
        mHandler = new Handler(Looper.getMainLooper());
    }

    /** Cleans up scan clients. Should be called when bluetooth is down. */
    @Hide
    @RequiresNoPermission
    public void cleanup() {
        mScanClients.clear();
    }

    /**
     * Start Bluetooth LE scan with default parameters and no filters. The scan results will be
     * delivered through {@code callback}. For unfiltered scans, scanning is stopped on screen off
     * to save power. Scanning is resumed when screen is turned on again. To avoid this, use {@link
     * #startScan(List, ScanSettings, ScanCallback)} with desired {@link ScanFilter}.
     *
     * <p>An app must have {@link android.Manifest.permission#ACCESS_COARSE_LOCATION
     * ACCESS_COARSE_LOCATION} permission in order to get results. An App targeting Android Q or
     * later must have {@link android.Manifest.permission#ACCESS_FINE_LOCATION ACCESS_FINE_LOCATION}
     * permission in order to get results.
     *
     * <p>This method requires the calling app to have the {@link
     * android.Manifest.permission#BLUETOOTH_SCAN} permission. Additionally, an app must have the
     * {@link android.Manifest.permission#BLUETOOTH_PRIVILEGED} if it is used for BLE scan only mode
     * (when the adapter state is not {@link BluetoothAdapter#STATE_ON}).
     *
     * @param callback Callback used to deliver scan results.
     * @throws IllegalArgumentException If {@code callback} is null.
     */
    @FlaggedApi(Flags.FLAG_ENFORCE_NON_NULL_WORKSOURCE)
    @RequiresLegacyBluetoothAdminPermission
    @RequiresBluetoothScanPermission
    @RequiresBluetoothLocationPermission
    @RequiresPermission(
            allOf = {BLUETOOTH_PRIVILEGED, BLUETOOTH_SCAN},
            conditional = true)
    public void startScan(@NonNull final ScanCallback callback) {
        startScan(null, new ScanSettings.Builder().build(), callback);
    }

    /**
     * Start Bluetooth LE scan. The scan results will be delivered through {@code callback}. For
     * unfiltered scans, scanning is stopped on screen off to save power. Scanning is resumed when
     * screen is turned on again. To avoid this, do filtered scanning by using proper {@link
     * ScanFilter}.
     *
     * <p>An app must have {@link android.Manifest.permission#ACCESS_COARSE_LOCATION
     * ACCESS_COARSE_LOCATION} permission in order to get results. An App targeting Android Q or
     * later must have {@link android.Manifest.permission#ACCESS_FINE_LOCATION ACCESS_FINE_LOCATION}
     * permission in order to get results.
     *
     * <p>This method requires the calling app to have the {@link
     * android.Manifest.permission#BLUETOOTH_SCAN} permission. Additionally, an app must have the
     * {@link android.Manifest.permission#BLUETOOTH_PRIVILEGED} if any of the following is true:
     *
     * <ul>
     *   <li>it is used for BLE scan only mode (when the adapter state is not {@link
     *       BluetoothAdapter#STATE_ON}).
     *   <li>the {@link ScanSettings} uses {@link ScanSettings#SCAN_MODE_AMBIENT_DISCOVERY}.
     *   <li>the {@link ScanSettings} uses batched scanning ({@link
     *       ScanSettings#getReportDelayMillis()} > 0) with {@link
     *       ScanSettings#SCAN_RESULT_TYPE_ABBREVIATED}.
     *   <li>a {@link ScanFilter} has a device address set, and either the address type is not
     *       {@link BluetoothDevice#ADDRESS_TYPE_PUBLIC} or the IRK is not null.
     * </ul>
     *
     * @param filters {@link ScanFilter}s for finding exact BLE devices.
     * @param settings Settings for the scan.
     * @param callback Callback used to deliver scan results.
     * @throws IllegalArgumentException If {@code settings} or {@code callback} is null.
     */
    @FlaggedApi(Flags.FLAG_ENFORCE_NON_NULL_WORKSOURCE)
    @RequiresLegacyBluetoothAdminPermission
    @RequiresBluetoothScanPermission
    @RequiresBluetoothLocationPermission
    @RequiresPermission(
            allOf = {BLUETOOTH_PRIVILEGED, BLUETOOTH_SCAN},
            conditional = true)
    public void startScan(
            @Nullable final List<ScanFilter> filters,
            @NonNull final ScanSettings settings,
            @NonNull final ScanCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback is null");
        }
        if (settings == null) {
            throw new IllegalArgumentException("Settings is null");
        }
        IBluetoothScan scan = mAdapter.getBluetoothScan();
        if (scan == null) {
            Log.w(TAG, "startScan(): Scanner is not available");
            postCallbackError(callback, ScanCallback.SCAN_FAILED_INTERNAL_ERROR);
            return;
        }
        if (!Flags.checkScanHardwareResourcesAvailabilityInBinder()
                && !isHardwareResourcesAvailableForScan(settings)) {
            postCallbackError(callback, ScanCallback.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES);
            return;
        }

        final List<ScanFilter> finalFilters = requireNonNullElse(filters, Collections.emptyList());

        var wrapper = new CallbackWrapper(callback);
        var existingWrapper = mScanClients.putIfAbsent(callback, wrapper);
        if (existingWrapper != null) {
            postCallbackError(callback, ScanCallback.SCAN_FAILED_ALREADY_STARTED);
            return;
        }
        int registrationError;
        try {
            registrationError = scan.startScan(mSource, wrapper, finalFilters, settings);
        } catch (RemoteException e) {
            logRemoteException(TAG, e);
            registrationError = ScanCallback.SCAN_FAILED_INTERNAL_ERROR;
        }
        if (registrationError != ScanCallback.NO_ERROR) {
            mScanClients.remove(callback);
            postCallbackError(callback, registrationError);
        }
    }

    /**
     * Start Bluetooth LE scan using a {@link PendingIntent}. The scan results will be delivered via
     * the PendingIntent. Use this method of scanning if your process is not always running and it
     * should be started when scan results are available.
     *
     * <p>An app must have {@link android.Manifest.permission#ACCESS_COARSE_LOCATION
     * ACCESS_COARSE_LOCATION} permission in order to get results. An App targeting Android Q or
     * later must have {@link android.Manifest.permission#ACCESS_FINE_LOCATION ACCESS_FINE_LOCATION}
     * permission in order to get results.
     *
     * <p>When the PendingIntent is delivered, the Intent passed to the receiver or activity will
     * contain one or more of the extras {@link #EXTRA_CALLBACK_TYPE}, {@link #EXTRA_ERROR_CODE} and
     * {@link #EXTRA_LIST_SCAN_RESULT} to indicate the result of the scan.
     *
     * <p>This method requires the calling app to have the {@link
     * android.Manifest.permission#BLUETOOTH_SCAN} permission. Additionally, an app must have the
     * {@link android.Manifest.permission#BLUETOOTH_PRIVILEGED} if any of the following is true:
     *
     * <ul>
     *   <li>it is used for BLE scan only mode (when the adapter state is not {@link
     *       BluetoothAdapter#STATE_ON}).
     *   <li>the {@link ScanSettings} uses {@link ScanSettings#SCAN_MODE_AMBIENT_DISCOVERY}.
     *   <li>the {@link ScanSettings} uses batched scanning ({@link
     *       ScanSettings#getReportDelayMillis()} > 0) with {@link
     *       ScanSettings#SCAN_RESULT_TYPE_ABBREVIATED}.
     *   <li>a {@link ScanFilter} has a device address set, and either the address type is not
     *       {@link BluetoothDevice#ADDRESS_TYPE_PUBLIC} or the IRK is not null.
     * </ul>
     *
     * @param filters Optional list of ScanFilters for finding exact BLE devices.
     * @param settings Optional settings for the scan.
     * @param callbackIntent The PendingIntent to deliver the result to.
     * @return Returns 0 for success or an error code from {@link ScanCallback} if the scan request
     *     could not be sent.
     * @see #stopScan(PendingIntent)
     */
    @RequiresLegacyBluetoothAdminPermission
    @RequiresBluetoothScanPermission
    @RequiresBluetoothLocationPermission
    @RequiresPermission(
            allOf = {BLUETOOTH_PRIVILEGED, BLUETOOTH_SCAN},
            conditional = true)
    public int startScan(
            @Nullable final List<ScanFilter> filters,
            @Nullable final ScanSettings settings,
            @NonNull final PendingIntent callbackIntent) {
        if (callbackIntent == null) {
            throw new IllegalArgumentException("Callback is null");
        }
        IBluetoothScan scan = mAdapter.getBluetoothScan();
        if (scan == null) {
            Log.w(TAG, "startScan(): Scanner is not available");
            return ScanCallback.SCAN_FAILED_INTERNAL_ERROR;
        }

        final ScanSettings finalSettings =
                requireNonNullElseGet(settings, () -> new ScanSettings.Builder().build());
        final List<ScanFilter> finalFilters = requireNonNullElse(filters, Collections.emptyList());

        if (!Flags.checkScanHardwareResourcesAvailabilityInBinder()
                && !isHardwareResourcesAvailableForScan(finalSettings)) {
            return ScanCallback.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES;
        }

        try {
            return scan.startScanPendingIntent(
                    mSource, callbackIntent, finalFilters, finalSettings);
        } catch (RemoteException e) {
            logRemoteException(TAG, e);
            return ScanCallback.SCAN_FAILED_INTERNAL_ERROR;
        }
    }

    /**
     * Start Bluetooth LE scan. Same as {@link #startScan(ScanCallback)} but allows the caller to
     * specify on behalf of which application(s) the work is being done.
     *
     * <p>This method requires the calling app to have the {@link
     * android.Manifest.permission#BLUETOOTH_SCAN} permission. Additionally, an app must have the
     * {@link android.Manifest.permission#BLUETOOTH_PRIVILEGED} if it is used for BLE scan only mode
     * (when the adapter state is not {@link BluetoothAdapter#STATE_ON}).
     *
     * <p>This method also requires the {@link android.Manifest.permission#UPDATE_DEVICE_STATS}
     * permission if the {@code workSource} is not null.
     *
     * @param workSource {@link WorkSource} identifying the application(s) for which to blame for
     *     the scan.
     * @param callback Callback used to deliver scan results.
     */
    @FlaggedApi(Flags.FLAG_ENFORCE_NON_NULL_WORKSOURCE)
    @Hide
    @SystemApi
    @RequiresLegacyBluetoothAdminPermission
    @RequiresBluetoothScanPermission
    @RequiresBluetoothLocationPermission
    @RequiresPermission(
            allOf = {BLUETOOTH_PRIVILEGED, BLUETOOTH_SCAN, UPDATE_DEVICE_STATS},
            conditional = true)
    public void startScanFromSource(
            @NonNull final WorkSource workSource, @NonNull final ScanCallback callback) {
        startScanFromSource(null, new ScanSettings.Builder().build(), workSource, callback);
    }

    /**
     * Start Bluetooth LE scan. Same as {@link #startScan(List, ScanSettings, ScanCallback)} but
     * allows the caller to specify on behalf of which application(s) the work is being done.
     *
     * <p>This method requires the calling app to have the {@link
     * android.Manifest.permission#BLUETOOTH_SCAN} permission. Additionally, an app must have the
     * {@link android.Manifest.permission#BLUETOOTH_PRIVILEGED} if any of the following is true:
     *
     * <ul>
     *   <li>it is used for BLE scan only mode (when the adapter state is not {@link
     *       BluetoothAdapter#STATE_ON}).
     *   <li>the {@link ScanSettings} uses {@link ScanSettings#SCAN_MODE_AMBIENT_DISCOVERY}.
     *   <li>the {@link ScanSettings} uses batched scanning ({@link
     *       ScanSettings#getReportDelayMillis()} > 0) with {@link
     *       ScanSettings#SCAN_RESULT_TYPE_ABBREVIATED}.
     *   <li>a {@link ScanFilter} has a device address set, and either the address type is not
     *       {@link BluetoothDevice#ADDRESS_TYPE_PUBLIC} or the IRK is not null.
     * </ul>
     *
     * <p>This method also requires the {@link android.Manifest.permission#UPDATE_DEVICE_STATS}
     * permission if the {@code workSource} is not null.
     *
     * @param filters {@link ScanFilter}s for finding exact BLE devices.
     * @param settings Settings for the scan.
     * @param workSource {@link WorkSource} identifying the application(s) for which to blame for
     *     the scan.
     * @param callback Callback used to deliver scan results.
     */
    @FlaggedApi(Flags.FLAG_ENFORCE_NON_NULL_WORKSOURCE)
    @Hide
    @SystemApi
    @RequiresLegacyBluetoothAdminPermission
    @RequiresBluetoothScanPermission
    @RequiresBluetoothLocationPermission
    @RequiresPermission(
            allOf = {BLUETOOTH_PRIVILEGED, BLUETOOTH_SCAN, UPDATE_DEVICE_STATS},
            conditional = true)
    public void startScanFromSource(
            @Nullable final List<ScanFilter> filters,
            @NonNull final ScanSettings settings,
            @NonNull final WorkSource workSource,
            @NonNull final ScanCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback is null");
        }
        if (settings == null) {
            throw new IllegalArgumentException("Settings is null");
        }
        if (Flags.enforceNonNullWorksource() && workSource == null) {
            throw new IllegalArgumentException("WorkSource is null");
        }
        IBluetoothScan scan = mAdapter.getBluetoothScan();
        if (scan == null) {
            Log.w(TAG, "startScanFromSource(): Scanner is not available");
            postCallbackError(callback, ScanCallback.SCAN_FAILED_INTERNAL_ERROR);
            return;
        }
        if (!Flags.checkScanHardwareResourcesAvailabilityInBinder()
                && !isHardwareResourcesAvailableForScan(settings)) {
            postCallbackError(callback, ScanCallback.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES);
            return;
        }

        final List<ScanFilter> finalFilters = requireNonNullElse(filters, Collections.emptyList());

        var wrapper = new CallbackWrapper(callback);
        var existingWrapper = mScanClients.putIfAbsent(callback, wrapper);
        if (existingWrapper != null) {
            postCallbackError(callback, ScanCallback.SCAN_FAILED_ALREADY_STARTED);
            return;
        }
        int registrationError;
        try {
            registrationError =
                    scan.startScanFromWorkSource(
                            mSource, wrapper, finalFilters, settings, workSource);
        } catch (RemoteException e) {
            logRemoteException(TAG, e);
            registrationError = ScanCallback.SCAN_FAILED_INTERNAL_ERROR;
        }
        if (registrationError != ScanCallback.NO_ERROR) {
            mScanClients.remove(callback);
            postCallbackError(callback, registrationError);
        }
    }

    /** Stops an ongoing Bluetooth LE scan. */
    @RequiresLegacyBluetoothAdminPermission
    @RequiresBluetoothScanPermission
    @RequiresPermission(BLUETOOTH_SCAN)
    public void stopScan(ScanCallback callback) {
        CallbackWrapper wrapper = mScanClients.remove(callback);
        if (wrapper == null) {
            Log.d(TAG, "could not find callback wrapper");
            return;
        }
        IBluetoothScan scan = mAdapter.getBluetoothScan();
        if (scan == null) {
            Log.w(TAG, "stopScan(callback): BLE is not available");
            return;
        }
        try {
            scan.stopScan(wrapper, mSource);
        } catch (RemoteException e) {
            logRemoteException(TAG, e);
        }
    }

    /**
     * Stops an ongoing Bluetooth LE scan started using a PendingIntent. When creating the
     * PendingIntent parameter, please do not use the FLAG_CANCEL_CURRENT flag. Otherwise, the stop
     * scan may have no effect.
     *
     * @param callbackIntent The PendingIntent that was used to start the scan.
     * @see #startScan(List, ScanSettings, PendingIntent)
     */
    @RequiresLegacyBluetoothAdminPermission
    @RequiresBluetoothScanPermission
    @RequiresPermission(BLUETOOTH_SCAN)
    public void stopScan(PendingIntent callbackIntent) {
        IBluetoothScan scan = mAdapter.getBluetoothScan();
        if (scan == null) {
            Log.w(TAG, "stopScan(callbackIntent): BLE is not available");
            return;
        }
        try {
            scan.stopScanForIntent(callbackIntent, mSource);
        } catch (RemoteException e) {
            logRemoteException(TAG, e);
        }
    }

    /**
     * Flush pending batch scan results stored in Bluetooth controller. This will return Bluetooth
     * LE scan results batched on bluetooth controller. Returns immediately, batch scan results data
     * will be delivered through the {@code callback}.
     *
     * @param callback Callback of the Bluetooth LE Scan, it has to be the same instance as the one
     *     used to start scan.
     */
    @RequiresLegacyBluetoothAdminPermission
    @RequiresBluetoothScanPermission
    @RequiresPermission(BLUETOOTH_SCAN)
    public void flushPendingScanResults(ScanCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null!");
        }
        CallbackWrapper wrapper = mScanClients.get(callback);
        if (wrapper == null) {
            return;
        }
        IBluetoothScan scan = mAdapter.getBluetoothScan();
        if (scan == null) {
            Log.w(TAG, "flushPendingScanResults(): BLE is not available");
            return;
        }
        try {
            scan.flushPendingBatchResults(wrapper, mSource);
        } catch (RemoteException e) {
            logRemoteException(TAG, e);
        }
    }

    /** Scan interface callbacks */
    private final class CallbackWrapper extends IScannerCallback.Stub {
        @NonNull private final ScanCallback mCallback;

        CallbackWrapper(@NonNull ScanCallback scanCallback) {
            mCallback = scanCallback;
        }

        private void executeIfRegistered(Runnable runnable) {
            executeFromBinder(
                    mHandler::post,
                    () -> {
                        if (mScanClients.containsKey(mCallback)) {
                            runnable.run();
                        }
                    });
        }

        @Override
        public void onScanResult(final ScanResult scanResult) {
            Attributable.setAttributionSource(scanResult, mSource);
            if (VDBG) {
                Log.d(TAG, "onScanResult(): " + scanResult.toString());
            } else if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "onScanResult()");
            }
            executeIfRegistered(
                    () -> {
                        if (Log.isLoggable(TAG, Log.VERBOSE)) {
                            Log.v(TAG, "onScanResult(): Handler run");
                        }
                        mCallback.onScanResult(ScanSettings.CALLBACK_TYPE_ALL_MATCHES, scanResult);
                    });
        }

        @Override
        public void onBatchScanResults(final List<ScanResult> results) {
            Attributable.setAttributionSource(results, mSource);
            executeIfRegistered(() -> mCallback.onBatchScanResults(results));
        }

        @Override
        public void onFoundOrLost(final boolean onFound, final ScanResult scanResult) {
            Attributable.setAttributionSource(scanResult, mSource);
            if (VDBG) {
                Log.d(TAG, "onFoundOrLost(): onFound=" + onFound + " " + scanResult.toString());
            }

            int callbackType =
                    onFound
                            ? ScanSettings.CALLBACK_TYPE_FIRST_MATCH
                            : ScanSettings.CALLBACK_TYPE_MATCH_LOST;
            executeIfRegistered(() -> mCallback.onScanResult(callbackType, scanResult));
        }

        @Override
        public void onError(@ScanCallback.ScanFailed int error) {
            executeIfRegistered(
                    () -> {
                        if (error == SCAN_FAILED_SCANNING_TOO_FREQUENTLY
                                || error == SCAN_FAILED_APPLICATION_REGISTRATION_FAILED) {
                            Log.e(TAG, "onError(): Unregistering the app");
                            mScanClients.remove(mCallback);
                        } else if (VDBG) {
                            Log.d(TAG, "onError(" + error + ")");
                        }

                        // If scanning too frequently, don't report anything to the app.
                        if (error != SCAN_FAILED_SCANNING_TOO_FREQUENTLY) {
                            mCallback.onScanFailed(error);
                        }
                    });
        }
    }

    private void postCallbackError(final ScanCallback callback, final int errorCode) {
        executeFromBinder(mHandler::post, () -> callback.onScanFailed(errorCode));
    }

    // TODO(b/497584056): Delete on check_scan_hardware_resources_availability_in_binder cleanup
    @RequiresPermission(BLUETOOTH_SCAN)
    private boolean isHardwareResourcesAvailableForScan(ScanSettings settings) {
        final int callbackType = settings.getCallbackType();
        if ((callbackType & ScanSettings.CALLBACK_TYPE_FIRST_MATCH) != 0
                || (callbackType & ScanSettings.CALLBACK_TYPE_MATCH_LOST) != 0) {
            // For onlost/onfound, we required hw support be available
            return (mAdapter.isOffloadedFilteringSupported()
                    && mAdapter.isHardwareTrackingFiltersAvailable());
        }
        return true;
    }

    /**
     * Start truncated scan.
     *
     * @removed this is not used anywhere
     */
    @Hide
    @Deprecated
    @SystemApi
    @RequiresBluetoothScanPermission
    @RequiresPermission(BLUETOOTH_SCAN)
    public void startTruncatedScan(List<TruncatedFilter> a, ScanSettings b, final ScanCallback c) {
        Log.wtf(TAG, "startTruncatedScan is deprecated and not supported; Will be removed soon");
    }
}
