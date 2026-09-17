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
package com.android.internal.telephony.satellite;

import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.hardware.radio.network.NetworkInfo;
import android.hardware.radio.network.PrioritizedNetworkScanRequest;
import android.hardware.radio.network.SatelliteNetworkInfo;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.SubscriptionManager;
import android.telephony.satellite.SatelliteManager;
import android.util.Log;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.flags.Flags;
import com.android.internal.telephony.uicc.IccRecords;
import com.android.internal.telephony.uicc.SIMRecords;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * AutoModeConnectionController orchestrates the configuration and enablement of satellite
 * connections.
 *
 * <p>This controller manages the multistep process of configuring the satellite modem
 * with Public Land Mobile Network (PLMN) lists and network info, and then transitioning
 * the modem's satellite attach state (enabled or disabled) based on carrier requirements.
 *
 * <p>The controller handles asynchronous responses from the modem and ensures that all
 * necessary configurations are successfully applied before the final enablement command is sent.
 * It also maintains a local cache of the satellite enablement status per subscription.
 */
@FlaggedApi(Flags.FLAG_SATELLITE_UPSELL_26Q4)
public class AutoModeConnectionController extends Handler {

    private static final String TAG = "AMConnectionController";
    private static final int CMD_GET_SATELLITE_ENABLED_FOR_CARRIER = 0;
    private static final int EVENT_GET_SATELLITE_ENABLED_FOR_CARRIER_DONE = 1;
    private static final int EVENT_SET_SATELLITE_PLMN_DONE = 2;
    private static final int EVENT_SET_SATELLITE_NETWORK_INFO_DONE = 3;
    private static final int EVENT_SATELLITE_ENABLEMENT_DONE = 4;
    private static final int EVENT_SET_PRIORITIZED_NETWORK_SCAN_DONE = 5;
    private static final int EVENT_GET_FORBIDDEN_PLMNS_DONE = 6;
    private static final int EVENT_SET_FORBIDDEN_PLMNS_DONE = 7;

    private static AutoModeConnectionController sInstance;

    /**
     * Key: Subscription ID, value: the actual satellite enabled state in the modem -
     * {@code true} for enabled and {@code false} for disabled.
     */
    @NonNull
    private final ConcurrentHashMap<Integer, Boolean> mSatelliteAttachEnabledStatusPerSub =
            new ConcurrentHashMap<>();

    /**
     * Private constructor to enforce singleton usage.
     * Initializes the controller and requests the initial satellite enablement status
     * from the modem for all available phones.
     *
     * @param looper The looper for the handler.
     */
    private AutoModeConnectionController(@NonNull Looper looper) {
        super(looper);
        Log.d(TAG,
                "Initializing AutoModeConnectionController and pre-populating enablement cache.");
        for (Phone phone : PhoneFactory.getPhones()) {
            obtainMessage(CMD_GET_SATELLITE_ENABLED_FOR_CARRIER, phone).sendToTarget();
        }
    }

    /**
     * Create the AutoModeConnectionController singleton instance with a specific Looper.
     *
     * @param looper The looper to use for the controller.
     */
    public static void make(@NonNull Looper looper) {
        if (sInstance == null) {
            Log.d(TAG, "make: initializing AutoModeConnectionController with looper");
            sInstance = new AutoModeConnectionController(looper);
        }
    }

    /**
     * Retrieves the singleton instance of AutoModeConnectionController.
     *
     * @return The singleton instance of AutoModeConnectionController, or null if not initialized.
     */
    public static AutoModeConnectionController getInstance() {
        if (sInstance == null) {
            Log.e(TAG, "getInstance: AutoModeConnectionController was not yet initialized.");
        }
        return sInstance;
    }

    /**
     * Set the singleton instance for testing.
     * @param instance The instance to set.
     */
    @VisibleForTesting
    public static void setInstance(@Nullable AutoModeConnectionController instance) {
        sInstance = instance;
    }

    /**
     * Handles messages dispatched to this handler, processing asynchronous responses
     * from modem operations.
     *
     * @param msg The message to handle.
     */
    @Override
    public void handleMessage(Message msg) {
        AsyncResult ar;
        int subId;
        int error;

        switch (msg.what) {
            case CMD_GET_SATELLITE_ENABLED_FOR_CARRIER: {
                Phone phone = (Phone) msg.obj;
                subId = phone.getSubId();
                int simSlot = SubscriptionManager.getSlotIndex(subId);
                Log.d(TAG, "handleMessage:"
                        + " event=CMD_GET_SATELLITE_ENABLED_FOR_CARRIER"
                        + ", subId=" + subId
                        + ", simSlot=" + simSlot);
                Message onCompleted = obtainMessage(EVENT_GET_SATELLITE_ENABLED_FOR_CARRIER_DONE,
                        subId);
                phone.isSatelliteEnabledForCarrier(simSlot, onCompleted);
                break;
            }

            case EVENT_GET_SATELLITE_ENABLED_FOR_CARRIER_DONE: {
                ar = (AsyncResult) msg.obj;

                if (ar.result == null) {
                    Log.e(TAG, "handleMessage:"
                            + " event=EVENT_GET_SATELLITE_ENABLED_FOR_CARRIER_DONE"
                            + ", result=" + null);
                    return;
                }
                subId = (int) ar.userObj;
                error = SatelliteServiceUtils.getSatelliteError(ar, "isSatelliteEnabledForCarrier");
                boolean isEnabled = (Boolean) ar.result;
                if (error == SatelliteManager.SATELLITE_RESULT_SUCCESS) {
                    Log.d(TAG, "handleMessage:"
                            + " event=EVENT_GET_SATELLITE_ENABLED_FOR_CARRIER_DONE"
                            + ", subId=" + subId
                            + ", isEnabled=" + isEnabled);
                    updateSatelliteAttachEnabledStatusForSubId(subId, isEnabled);
                } else {
                    Log.e(TAG, "handleMessage:"
                            + " event=EVENT_GET_SATELLITE_ENABLED_FOR_CARRIER_DONE"
                            + ", subId=" + subId
                            + ", error=" + error);
                }
                break;
            }

            case EVENT_SET_SATELLITE_PLMN_DONE: {
                ar = (AsyncResult) msg.obj;
                AutoModeConnectionControllerRequest request =
                        (AutoModeConnectionControllerRequest) ar.userObj;
                if (request == null) {
                    Log.e(TAG, "handleMessage:"
                            + " event=EVENT_SET_SATELLITE_PLMN_DONE"
                            + ", request=null");
                    return;
                }
                subId = request.mSubId;
                error = SatelliteServiceUtils.getSatelliteError(ar, "setSatellitePlmnInfo");
                if (error == SatelliteManager.SATELLITE_RESULT_SUCCESS) {
                    Log.d(TAG, "handleMessage:"
                            + " event=EVENT_SET_SATELLITE_PLMN_DONE"
                            + ", subId=" + subId
                            + ", result=Success");
                    request.onPlmnConfigurationDone(true);
                } else {
                    Log.e(TAG, "handleMessage:"
                            + " event=EVENT_SET_SATELLITE_PLMN_DONE"
                            + ", subId=" + subId
                            + ", error=" + error);
                    request.onPlmnConfigurationDone(false);
                }
                evaluateIfPLMNConfigurationComplete(request);
                break;
            }

            case EVENT_SET_SATELLITE_NETWORK_INFO_DONE: {
                ar = (AsyncResult) msg.obj;
                AutoModeConnectionControllerRequest request =
                        (AutoModeConnectionControllerRequest) ar.userObj;
                if (request == null) {
                    Log.e(TAG, "handleMessage:"
                            + " event=EVENT_SET_SATELLITE_NETWORK_INFO_DONE"
                            + ", request=null");
                    return;
                }
                subId = request.mSubId;
                error = SatelliteServiceUtils.getSatelliteError(ar, "setSatelliteNetworkInfo");
                if (error == SatelliteManager.SATELLITE_RESULT_SUCCESS) {
                    Log.d(TAG, "handleMessage:"
                            + " event=EVENT_SET_SATELLITE_NETWORK_INFO_DONE"
                            + ", subId=" + subId
                            + ", result=Success");
                    request.onNetworkInfoConfigurationDone(true);
                } else {
                    Log.e(TAG, "handleMessage:"
                            + " event=EVENT_SET_SATELLITE_NETWORK_INFO_DONE"
                            + ", subId=" + subId
                            + ", error=" + error);
                    request.onNetworkInfoConfigurationDone(false);
                }
                evaluateIfPLMNConfigurationComplete(request);
                break;
            }

            case EVENT_SATELLITE_ENABLEMENT_DONE: {
                ar = (AsyncResult) msg.obj;
                AutoModeConnectionControllerRequest request =
                        (AutoModeConnectionControllerRequest) ar.userObj;
                if (request == null) {
                    Log.e(TAG, "handleMessage:"
                            + " event=EVENT_SATELLITE_ENABLEMENT_DONE"
                            + ", request=null");
                    return;
                }
                subId = request.mSubId;
                Phone phone = request.mPhone;
                // Determine the satellite error code from the final enablement step
                error = SatelliteServiceUtils.getSatelliteError(ar,
                        "setSatelliteEnabledForCarrier");
                if (error == SatelliteManager.SATELLITE_RESULT_SUCCESS) {
                    Log.d(TAG, "handleMessage:"
                            + " event=EVENT_SATELLITE_ENABLEMENT_DONE"
                            + ", subId=" + subId
                            + ", result=success");
                    updateSatelliteAttachEnabledStatusForSubId(subId,
                            request.mIsSatelliteExpectedToBeEnabled);
                    obtainMessage(CMD_GET_SATELLITE_ENABLED_FOR_CARRIER, phone)
                            .sendToTarget();
                    if (request.mIsPrioritized) {
                        setPrioritizedNetworkScan(request);
                        return;
                    }
                    request.mCallback.accept(error);
                } else if (error == SatelliteManager.SATELLITE_RESULT_REQUEST_NOT_SUPPORTED) {
                    Log.e(TAG, "handleMessage:"
                            + " event=EVENT_SATELLITE_ENABLEMENT_DONE"
                            + ", subId=" + subId
                            + ", error=" + error);
                    if (SatelliteController.getInstance()
                            .isSatelliteNetworkAttachControlledWithFplmn(subId)) {
                        addOrRemoveSatellitePlmnFromSimFplmn(request);
                        return;
                    }
                    finishSetupAndSyncModemStatus(request, error);
                } else {
                    Log.e(TAG, "handleMessage:"
                            + " event=EVENT_SATELLITE_ENABLEMENT_DONE"
                            + ", subId=" + subId
                            + ", error=" + error);
                    finishSetupAndSyncModemStatus(request, error);
                }
                break;
            }

            case EVENT_SET_PRIORITIZED_NETWORK_SCAN_DONE: {
                ar = (AsyncResult) msg.obj;
                AutoModeConnectionControllerRequest request =
                        (AutoModeConnectionControllerRequest) ar.userObj;
                if (request == null) {
                    Log.e(TAG, "handleMessage:"
                            + " event=EVENT_SET_PRIORITIZED_NETWORK_SCAN_DONE"
                            + ", request=null");
                    return;
                }
                subId = request.mSubId;
                error = SatelliteServiceUtils.getSatelliteError(ar,
                        "setPrioritizedNetworkScan");
                if (error == SatelliteManager.SATELLITE_RESULT_SUCCESS) {
                    Log.d(TAG, "handleMessage:"
                            + " event=EVENT_SET_PRIORITIZED_NETWORK_SCAN_DONE"
                            + ", subId=" + subId
                            + ", result=success");
                    request.mCallback.accept(SatelliteManager.SATELLITE_RESULT_SUCCESS);
                } else {
                    Log.d(TAG, "handleMessage:"
                            + " event=EVENT_SET_PRIORITIZED_NETWORK_SCAN_DONE"
                            + ", subId=" + subId
                            + ", error=" + error);
                    // TODO: Add support for internally triggered prioritized scans once use
                    //  cases are defined in the requirements documentation.
                    request.mCallback.accept(
                            SatelliteManager.SATELLITE_RESULT_PRIORITIZED_SCAN_FAILED);
                }
                break;
            }

            case EVENT_GET_FORBIDDEN_PLMNS_DONE: {
                ar = (AsyncResult) msg.obj;
                AutoModeConnectionControllerRequest request =
                        (AutoModeConnectionControllerRequest) ar.userObj;
                if (request == null) {
                    Log.e(TAG, "handleMessage:"
                            + " event=EVENT_GET_FORBIDDEN_PLMNS_DONE"
                            + ", request=null");
                    return;
                }
                subId = request.mSubId;
                Phone phone = request.mPhone;
                boolean isEnabled = request.mIsSatelliteExpectedToBeEnabled;
                if (ar.exception != null) {
                    Log.e(TAG, "handleMessage:"
                            + " event=EVENT_GET_FORBIDDEN_PLMNS_DONE"
                            + ", subId=" + subId
                            + ", isEnabled=" + isEnabled
                            + ", error=failed to get FPLMNs with exception.", ar.exception);
                    finishSetupAndSyncModemStatus(request,
                            SatelliteManager.SATELLITE_RESULT_REQUEST_NOT_SUPPORTED);
                    return;
                }
                if (ar.result == null) {
                    Log.e(TAG, "handleMessage:"
                            + " event=EVENT_GET_FORBIDDEN_PLMNS_DONE"
                            + ", subId=" + subId
                            + ", isEnabled=" + isEnabled
                            + ", error=failed to get FPLMNs.");
                    finishSetupAndSyncModemStatus(request,
                            SatelliteManager.SATELLITE_RESULT_REQUEST_NOT_SUPPORTED);
                    return;
                }
                String[] fplmns = (String[]) ar.result;
                Set<String> currentFplmnSet = new HashSet<>(Arrays.asList(fplmns));
                String satellitePlmnForSimFplmnManagement =
                        request.mSatellitePlmnForSimFplmnManagement;
                Log.d(TAG, "handleMessage:"
                        + " event=EVENT_GET_FORBIDDEN_PLMNS_DONE"
                        + ", subId=" + subId
                        + ", isEnabled=" + isEnabled
                        + ", satellitePlmnForSimFplmnManagement="
                        + satellitePlmnForSimFplmnManagement
                        + ", currentFplmnSet=" + currentFplmnSet);

                if (isEnabled) {
                    // During enablement, remove the managed PLMN from the forbidden list
                    if (!currentFplmnSet.remove(satellitePlmnForSimFplmnManagement)) {
                        Log.d(TAG, "handleMessage:"
                                + " event=EVENT_GET_FORBIDDEN_PLMNS_DONE"
                                + ", subId=" + subId
                                + ", isEnabled=true"
                                + ", removedList=[]"
                                + " Skipping SIM update.");
                        finishSetupAndSyncModemStatus(request,
                                SatelliteManager.SATELLITE_RESULT_SUCCESS);
                        return;
                    }
                } else {
                    // During disablement, add the managed PLMN to the forbidden list
                    currentFplmnSet.add(satellitePlmnForSimFplmnManagement);
                }

                List<String> updatedFplmnList = new ArrayList<>(currentFplmnSet);
                Log.d(TAG, "handleMessage:"
                        + " event=EVENT_GET_FORBIDDEN_PLMNS_DONE"
                        + ", subId=" + subId
                        + ", isEnabled=" + isEnabled
                        + ", updatedFplmnList=" + updatedFplmnList);
                IccRecords iccRecords = phone.getIccRecords();
                if (iccRecords instanceof SIMRecords) {
                    ((SIMRecords) iccRecords)
                            .setForbiddenPlmns(
                                    obtainMessage(EVENT_SET_FORBIDDEN_PLMNS_DONE, request),
                                    updatedFplmnList);
                    return;
                }
                Log.e(TAG, "handleMessage:"
                        + " event=EVENT_GET_FORBIDDEN_PLMNS_DONE"
                        + ", subId=" + subId
                        + ", isEnabled=" + isEnabled
                        + ", error=No SIMRecords available");
                finishSetupAndSyncModemStatus(request,
                        SatelliteManager.SATELLITE_RESULT_REQUEST_NOT_SUPPORTED);
                break;
            }

            case EVENT_SET_FORBIDDEN_PLMNS_DONE: {
                ar = (AsyncResult) msg.obj;
                AutoModeConnectionControllerRequest request =
                        (AutoModeConnectionControllerRequest) ar.userObj;
                if (request == null) {
                    Log.e(TAG, "handleMessage:"
                            + " event=EVENT_SET_FORBIDDEN_PLMNS_DONE"
                            + ", request=null");
                    return;
                }
                subId = request.mSubId;
                boolean isEnabled = request.mIsSatelliteExpectedToBeEnabled;
                if (ar.exception != null) {
                    Log.e(TAG, "handleMessage:"
                            + " event=EVENT_SET_FORBIDDEN_PLMNS_DONE"
                            + ", subId=" + subId
                            + ", isEnabled=" + isEnabled
                            + ", error=failed to set FPLMNs with exception.", ar.exception);
                    finishSetupAndSyncModemStatus(request,
                            SatelliteManager.SATELLITE_RESULT_REQUEST_NOT_SUPPORTED);
                    return;
                }
                Log.d(TAG, "handleMessage:"
                        + " event=EVENT_SET_FORBIDDEN_PLMNS_DONE"
                        + ", subId=" + subId
                        + ", isEnabled=" + isEnabled
                        + ", result=success");
                // TODO: Trigger an APM toggle or Network Preference toggle to ensure the modem
                //  re-reads the updated FPLMN list from the SIM.
                finishSetupAndSyncModemStatus(request, SatelliteManager.SATELLITE_RESULT_SUCCESS);
                break;
            }

            default:
                Log.w(TAG, "handleMessage: Unknown event=" + msg.what);
                break;
        }
    }

    /**
     * Initiates the orchestration process to update the satellite connection state.
     *
     * <p>This is the entry point for both enabling and disabling the satellite connection.
     * The orchestration involves:
     * 1. Retrieving the {@link Phone} instance for the subId.
     * 2. Configuring PLMN lists via {@code configureSatellitePlmnForCarrier}.
     * 3. Waiting for asynchronous configuration results.
     * 4. Triggering the final modem enablement command.
     *
     * @param subId                              The subscription ID for which to set up the
     *                                           connection.
     * @param isSatelliteExpectedToBeEnabled     {@code true} to enable satellite attach,
     *                                           {@code false} to disable.
     * @param allPlmnList                        A complete list of known satellite PLMNs.
     * @param allowedPlmnList                    The list of satellite PLMNs the modem is allowed to
     *                                           use.
     * @param isPrioritized                      {@code true} if a prioritized scan should be
     *                                           triggered after enablement.
     * @param searchIntervalMs                   The search interval in milliseconds for prioritized
     *                                           network scan. Unit: milliseconds. -1 for default
     *                                           interval. Callers are recommended to retrieve this
     *                                           value from
     *                                           {@link android.telephony.CarrierConfigManager}
     *                                           using use-case specific keys (e.g. PURCHASE).
     * @param validDurationSec                   The valid duration in seconds for prioritized
     *                                           network scan. Unit: seconds. 0 for one-shot
     *                                           duration. Callers are recommended to retrieve this
     *                                           value from
     *                                           {@link android.telephony.CarrierConfigManager}
     *                                           using use-case specific keys (e.g. PURCHASE).
     * @param supportedTechMap                   Map of PLMN to its supported technologies.
     * @param satellitePlmnForSimFplmnManagement The satellite PLMN to be managed on the SIM's
     *                                           forbidden list.
     * @param callback                           Callback to receive the final result code.
     */
    public void setupSatelliteConnection(
            int subId,
            boolean isSatelliteExpectedToBeEnabled,
            @NonNull List<String> allPlmnList,
            @NonNull List<String> allowedPlmnList,
            boolean isPrioritized,
            int searchIntervalMs,
            int validDurationSec,
            @NonNull Map<String, List<Integer>> supportedTechMap,
            @NonNull String satellitePlmnForSimFplmnManagement,
            @NonNull Consumer<Integer> callback
    ) {
        // TODO(b/519488613): Move isSatelliteExpectedToBeEnabled as the first argument.
        if (subId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            Log.d(TAG, "setupSatelliteConnection: Invalid subId");
            callback.accept(SatelliteManager.SATELLITE_RESULT_NO_VALID_SATELLITE_SUBSCRIPTION);
            return;
        }

        Log.d(TAG, "setupSatelliteConnection: Entering with subId=" + subId
                + ", isSatelliteExpectedToBeEnabled=" + isSatelliteExpectedToBeEnabled
                + ", isPrioritized=" + isPrioritized);

        // Retrieve the Phone instance associated with the given subscription ID.
        // This relies on SatelliteServiceUtils which in turn uses PhoneFactory.
        // If the telephony stack is not yet fully initialized or in an invalid state,
        // this may return null.
        Phone phone = SatelliteServiceUtils.getPhone(subId);
        if (phone == null) {
            Log.w(TAG, "setupSatelliteConnection: subId=" + subId
                    + ", Phone instance is null. Aborting.");
            callback.accept(SatelliteManager.SATELLITE_RESULT_INVALID_TELEPHONY_STATE);
            return;
        }

        AutoModeConnectionControllerRequest request = new AutoModeConnectionControllerRequest(
                subId, allPlmnList, allowedPlmnList, isSatelliteExpectedToBeEnabled,
                isPrioritized, searchIntervalMs, validDurationSec,
                supportedTechMap, satellitePlmnForSimFplmnManagement, phone, callback);

        // Configure the satellite PLMNs and Network Info for the carrier.
        // Both commands are dispatched asynchronously.
        Log.d(TAG, "setupSatelliteConnection: subId=" + subId
                + ", Initiating PLMN configuration for carrier.");
        configureSatellitePlmnForCarrier(request);
    }

    /**
     * Checks if satellite attach is currently enabled at the modem for a given subscription.
     * Returns false if the status is not cached.
     *
     * @param subId The subscription ID.
     * @return {@code true} if enabled, {@code false} otherwise.
     */
    public boolean isSatelliteEnabledForCarrierAtModem(int subId) {
        return mSatelliteAttachEnabledStatusPerSub.getOrDefault(subId, false);
    }

    /**
     * Updates the local cached status of satellite attach enablement for a given subscription.
     *
     * @param subId     The subscription ID.
     * @param isEnabled The enablement status to cache.
     */
    private void updateSatelliteAttachEnabledStatusForSubId(int subId, boolean isEnabled) {
        mSatelliteAttachEnabledStatusPerSub.put(subId, isEnabled);
    }

    /**
     * Configures the satellite PLMN and network info for the given carrier.
     * Both setSatellitePlmn and setSatelliteNetworkInfo commands are dispatched to the modem.
     *
     * @param request The AutoModeConnectionControllerRequest holding the state of the operation.
     */
    private void configureSatellitePlmnForCarrier(AutoModeConnectionControllerRequest request) {
        Phone phone = request.mPhone;
        int subId = request.mSubId;
        try {
            Log.d(TAG, "configureSatellitePlmnForCarrier: subId=" + subId
                    + ", Setting " + request.mAllPlmnList.size() + " total PLMNs.");

            // Set PLMNs to the modem
            phone.setSatellitePlmn(phone.getPhoneId(), request.mAllowedPlmnList,
                    request.mAllPlmnList,
                    obtainMessage(EVENT_SET_SATELLITE_PLMN_DONE, request));

            Log.d(TAG, "configureSatellitePlmnForCarrier: subId=" + subId
                    + ", Configuring SatelliteNetworkInfo.");

            // If satellite is being disabled, we clear the allowed PLMN list in the NetworkInfo
            List<String> actualAllowedPlmnList =
                    request.mIsSatelliteExpectedToBeEnabled ? request.mAllowedPlmnList
                            : new ArrayList<>();

            Set<String> allowedPlmnsSet = new HashSet<>(request.mAllowedPlmnList);
            List<String> disallowedPlmnList = request.mAllPlmnList.stream()
                    .filter(plmn -> !allowedPlmnsSet.contains(plmn))
                    .collect(Collectors.toList());

            Log.d(TAG, "configureSatellitePlmnForCarrier: subId=" + subId
                    + ", NetworkInfo Allowed PLMN count=" + actualAllowedPlmnList.size()
                    + ", Disallowed PLMN count=" + disallowedPlmnList.size());

            SatelliteNetworkInfo satelliteNetworkInfo =
                    SatellitePlmnNetworkInfo.fromPlmn(subId, actualAllowedPlmnList,
                                    disallowedPlmnList, request.mSupportedTechMap)
                            .toHalSatelliteNetworkInfo();

            // Cache the detailed network info list from the HAL structure to be used
            // later in the enablePrioritizedNetworkScan request.
            request.setAllowedNetworkInfoList(satelliteNetworkInfo.allowedPlmns);

            phone.setSatelliteNetworkInfo(phone.getPhoneId(),
                    satelliteNetworkInfo,
                    obtainMessage(EVENT_SET_SATELLITE_NETWORK_INFO_DONE, request));
        } catch (Exception e) {
            Log.e(TAG, "configureSatellitePlmnForCarrier: subId=" + subId
                    + ", Failed to configure satellite PLMN: " + e.getMessage(), e);
            request.mCallback.accept(SatelliteManager.SATELLITE_RESULT_MODEM_ERROR);
        }
    }

    /**
     * Synchronizes the asynchronous configuration results and triggers enablement if appropriate.
     *
     * <p>This method is called after each configuration step (PLMN or NetworkInfo) completes.
     * It ensures that we wait for BOTH async operations to finish before evaluating the next step.
     *
     * <p>TODO: Evaluate if the orchestration logic can be simplified in the future, particularly
     * for modems that support both the old (setSatellitePlmn) and new (setSatelliteNetworkInfo)
     * APIs concurrently.
     *
     * @param request The current orchestration request state.
     */
    private void evaluateIfPLMNConfigurationComplete(AutoModeConnectionControllerRequest request) {
        int subId = request.mSubId;

        // Wait for both async operations to report back (success or failure).
        if (!request.isSatellitePlmnConfigurationComplete()) {
            Log.d(TAG, "evaluateIfPLMNConfigurationComplete: subId=" + subId
                    + ", Configurations pending.");
            return;
        }

        // Both configuration steps are done. If either succeeded, we can proceed
        // to actually enabling/disabling the satellite attach state in the modem.
        if (request.isSatellitePlmnConfigurationSuccessful()) {
            Log.d(TAG, "evaluateIfPLMNConfigurationComplete: subId=" + subId
                    + ", Required configurations set successfully."
                    + " Proceeding to modem enablement.");
            setSatelliteEnabledForCarrier(request);
        } else {
            // Both completed, but NEITHER succeeded (or failed prematurely).
            // This is a fatal modem error for the request.
            Log.e(TAG, "evaluateIfPLMNConfigurationComplete: subId=" + subId
                    + ", All configurations failed. Aborting orchestration.");
            request.mCallback.accept(SatelliteManager.SATELLITE_RESULT_MODEM_ERROR);
        }
    }

    /**
     * Evaluates and updates the actual enablement state of the satellite connection
     * at the modem level. This should only be called once all necessary configurations
     * (PLMNs, Network Info) have been successfully pushed to the modem.
     *
     * @param request The ConnectionControllerRequest holding the state of the operation.
     */
    private void setSatelliteEnabledForCarrier(AutoModeConnectionControllerRequest request) {
        int subId = request.mSubId;
        Log.d(TAG, "setSatelliteEnabledForCarrier: subId=" + subId
                + ", expectedEnabled=" + request.mIsSatelliteExpectedToBeEnabled);

        int simSlot = SubscriptionManager.getSlotIndex(request.mSubId);
        Phone phone = request.mPhone;
        try {
            Message onCompleted = obtainMessage(EVENT_SATELLITE_ENABLEMENT_DONE, request);

            Log.d(TAG, "setSatelliteEnabledForCarrier: subId=" + subId
                    + ", Sending setSatelliteEnabledForCarrier request for simSlot=" + simSlot);

            phone.setSatelliteEnabledForCarrier(simSlot,
                    request.mIsSatelliteExpectedToBeEnabled, onCompleted);
        } catch (Exception e) {
            Log.e(TAG, "setSatelliteEnabledForCarrier: subId=" + subId
                    + ", Failed to dispatch satellite enablement request: " + e.getMessage(), e);
            request.mCallback.accept(SatelliteManager.SATELLITE_RESULT_MODEM_ERROR);
        }
    }

    /**
     * Triggers the prioritized network scan HAL API.
     *
     * @param request The current orchestration request state.
     */
    private void setPrioritizedNetworkScan(AutoModeConnectionControllerRequest request) {
        int subId = request.mSubId;
        Phone phone = request.mPhone;
        int simSlot = SubscriptionManager.getSlotIndex(subId);
        Message onCompleted = obtainMessage(EVENT_SET_PRIORITIZED_NETWORK_SCAN_DONE, request);

        try {
            if (request.mIsSatelliteExpectedToBeEnabled) {
                NetworkInfo[] allowedNetworkInfoList = request.mAllowedNetworkInfoList;
                if (allowedNetworkInfoList == null || allowedNetworkInfoList.length == 0) {
                    Log.e(TAG, "setPrioritizedNetworkScan: No allowed network infos available."
                            + " Skipping scan.");
                    AsyncResult.forMessage(onCompleted, null, null);
                    onCompleted.sendToTarget();
                    return;
                }

                Log.d(TAG, "setPrioritizedNetworkScan: Enabling prioritized scan for"
                        + " subId=" + subId
                        + ", networkInfos=" + Arrays.toString(request.mAllowedNetworkInfoList)
                        + ", searchIntervalMs=" + request.mSearchIntervalMs
                        + ", validDurationSec=" + request.mValidDurationSec);
                PrioritizedNetworkScanRequest scanRequest = new PrioritizedNetworkScanRequest();
                scanRequest.networkInfos = request.mAllowedNetworkInfoList;
                scanRequest.searchIntervalMs = request.mSearchIntervalMs;
                scanRequest.validDurationSec = request.mValidDurationSec;
                phone.enablePrioritizedNetworkScan(simSlot, scanRequest, onCompleted);
            } else {
                Log.d(TAG, "setPrioritizedNetworkScan: Disabling prioritized scan for subId="
                        + subId);
                phone.disablePrioritizedNetworkScan(simSlot, onCompleted);
            }
        } catch (Exception e) {
            Log.e(TAG, "setPrioritizedNetworkScan: Failed to dispatch prioritized scan request: "
                    + e.getMessage(), e);
            // Finish the orchestration by manually sending the completion message.
            AsyncResult.forMessage(onCompleted, null, e);
            onCompleted.sendToTarget();
        }
    }

    /**
     * Updates the forbidden PLMN list on the SIM.
     *
     * @param request The current orchestration request state.
     */
    private void addOrRemoveSatellitePlmnFromSimFplmn(AutoModeConnectionControllerRequest request) {
        Log.d(TAG, "addOrRemoveSatellitePlmnFromSimFplmn: Initiating FPLMN update for"
                + " subId=" + request.mSubId);
        Phone phone = request.mPhone;
        IccRecords iccRecords = phone.getIccRecords();
        if (iccRecords instanceof SIMRecords) {
            ((SIMRecords) iccRecords)
                    .getForbiddenPlmns(obtainMessage(EVENT_GET_FORBIDDEN_PLMNS_DONE, request));
        } else {
            Log.e(TAG, "addOrRemoveSatellitePlmnFromSimFplmn: No SIMRecords available");
            request.mCallback.accept(SatelliteManager.SATELLITE_RESULT_REQUEST_NOT_SUPPORTED);
        }
    }

    /**
     * Finishes the satellite orchestration request by updating the local enablement cache from the
     * modem and notifying the caller with the final result.
     *
     * @param request    The orchestration request state.
     * @param resultCode The result code to return to the caller.
     */
    private void finishSetupAndSyncModemStatus(
            @NonNull AutoModeConnectionControllerRequest request,
            int resultCode) {
        Log.d(TAG, "finishSetupAndSyncModemStatus: subId=" + request.mSubId
                + ", resultCode=" + resultCode);
        obtainMessage(CMD_GET_SATELLITE_ENABLED_FOR_CARRIER, request.mPhone).sendToTarget();
        request.mCallback.accept(resultCode);
    }

    /**
     * A state holder object that tracks the progress of a single satellite orchestration request
     * across multiple asynchronous modem calls.
     */
    private static class AutoModeConnectionControllerRequest {
        private final int mSubId;
        @NonNull
        private final List<String> mAllPlmnList;
        @NonNull
        private final List<String> mAllowedPlmnList;
        private final boolean mIsSatelliteExpectedToBeEnabled;
        private final boolean mIsPrioritized;
        @Nullable
        private NetworkInfo[] mAllowedNetworkInfoList;
        private final int mSearchIntervalMs;
        private final int mValidDurationSec;
        @NonNull
        private final Map<String, List<Integer>> mSupportedTechMap;
        @NonNull
        private final String mSatellitePlmnForSimFplmnManagement;
        @NonNull
        private final Phone mPhone;
        @NonNull
        private final Consumer<Integer> mCallback;

        // State tracking variables for asynchronous responses
        private boolean mPlmnDone = false;
        private boolean mNetworkInfoDone = false;
        private boolean mPlmnSuccess = false;
        private boolean mNetworkInfoSuccess = false;

        AutoModeConnectionControllerRequest(
                int subId,
                @NonNull List<String> allPlmnList,
                @NonNull List<String> allowedPlmnList,
                boolean isSatelliteExpectedToBeEnabled,
                boolean isPrioritized,
                int searchIntervalMs,
                int validDurationSec,
                @NonNull Map<String, List<Integer>> supportedTechMap,
                @NonNull String satellitePlmnForSimFplmnManagement,
                @NonNull Phone phone,
                @NonNull Consumer<Integer> callback
        ) {
            this.mSubId = subId;
            this.mAllPlmnList = allPlmnList;
            this.mAllowedPlmnList = allowedPlmnList;
            this.mIsSatelliteExpectedToBeEnabled = isSatelliteExpectedToBeEnabled;
            this.mIsPrioritized = isPrioritized;
            this.mAllowedNetworkInfoList = null;
            this.mSearchIntervalMs = searchIntervalMs;
            this.mValidDurationSec = validDurationSec;
            this.mSupportedTechMap = supportedTechMap;
            this.mSatellitePlmnForSimFplmnManagement = satellitePlmnForSimFplmnManagement;
            this.mPhone = phone;
            this.mCallback = callback;
        }

        public void setAllowedNetworkInfoList(@NonNull NetworkInfo[] allowedNetworkInfoList) {
            mAllowedNetworkInfoList = allowedNetworkInfoList;
        }

        public void onPlmnConfigurationDone(boolean success) {
            mPlmnDone = true;
            mPlmnSuccess = success;
        }

        public void onNetworkInfoConfigurationDone(boolean success) {
            mNetworkInfoDone = true;
            mNetworkInfoSuccess = success;
        }

        public boolean isSatellitePlmnConfigurationComplete() {
            return mPlmnDone && mNetworkInfoDone;
        }

        public boolean isSatellitePlmnConfigurationSuccessful() {
            return mPlmnSuccess || mNetworkInfoSuccess;
        }

        @Override
        public String toString() {
            return "AutoModeConnectionControllerRequest{"
                    + "mSubId=" + mSubId
                    + ", mAllPlmnList=" + mAllPlmnList
                    + ", mAllowedPlmnList=" + mAllowedPlmnList
                    + ", mIsSatelliteExpectedToBeEnabled=" + mIsSatelliteExpectedToBeEnabled
                    + ", mIsPrioritized=" + mIsPrioritized
                    + ", mAllowedNetworkInfoList=" + Arrays.toString(mAllowedNetworkInfoList)
                    + ", mSearchIntervalMs=" + mSearchIntervalMs
                    + ", mValidDurationSec=" + mValidDurationSec
                    + ", mSupportedTechMap=" + mSupportedTechMap
                    + ", mSatellitePlmnForSimFplmnManagement=" + mSatellitePlmnForSimFplmnManagement
                    + ", mPhone=" + mPhone.getPhoneId()
                    + ", mPlmnDone=" + mPlmnDone
                    + ", mNetworkInfoDone=" + mNetworkInfoDone
                    + ", mPlmnSuccess=" + mPlmnSuccess
                    + ", mNetworkInfoSuccess=" + mNetworkInfoSuccess
                    + '}';
        }
    }
}
