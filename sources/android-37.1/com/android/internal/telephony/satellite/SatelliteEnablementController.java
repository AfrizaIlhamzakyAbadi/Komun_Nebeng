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

import static android.telephony.CarrierConfigManager.CARRIER_ROAMING_NTN_CONNECT_AUTOMATIC;
import static android.telephony.CarrierConfigManager.CARRIER_ROAMING_NTN_CONNECT_MANUAL;

import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.telephony.CarrierConfigManager;
import android.telephony.satellite.EnableRequestAttributes;
import android.telephony.satellite.SatelliteManager;
import android.util.Log;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.flags.FeatureFlags;
import com.android.internal.telephony.flags.Flags;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Manages satellite enablement logic and resolves conflicts between different enablement requests.
 * This class acts as a central orchestrator, delegating to the appropriate strategy
 * (e.g., Manual or Automatic) based on the request.
 */
@FlaggedApi(Flags.FLAG_SATELLITE_UPSELL_26Q4)
public class SatelliteEnablementController extends Handler {

    private static final String TAG = "SatEnablementController";
    @Nullable private static SatelliteEnablementController sInstance;
    private static final int SATELLITE_ENABLED_FOR_UNKNOWN =
            1 << SatelliteManager.SATELLITE_ENABLEMENT_REQUEST_REASON_UNKNOWN;
    private static final int SATELLITE_ENABLED_FOR_PURCHASE =
            1 << SatelliteManager.SATELLITE_ENABLEMENT_REQUEST_REASON_PURCHASE;
    private static final int SATELLITE_ENABLED_FOR_USER =
            1 << SatelliteManager.SATELLITE_ENABLEMENT_REQUEST_REASON_USER;
    private static final int SATELLITE_ENABLED_FOR_POWER =
            1 << SatelliteManager.SATELLITE_ENABLEMENT_REQUEST_REASON_POWER;
    private static final int SATELLITE_ENABLED_FOR_CARRIER_CONFIG_UPDATE =
            1 << SatelliteManager.SATELLITE_ENABLEMENT_REQUEST_REASON_CARRIER_CONFIG_UPDATE;
    private static final int SATELLITE_ENABLED_FOR_ENTITLEMENT =
            1 << SatelliteManager.SATELLITE_ENABLEMENT_REQUEST_REASON_ENTITLEMENT;

    // Map<SubId, Map<ConnectType, DisabledMask>>
    private final Map<Integer, Map<Integer, Integer>> mSatelliteDisabledMaskMap =
            new ConcurrentHashMap<>();
    private final ManualEnablementController mManualSatelliteController;
    private final AutoEnablementController mAutoSatelliteController;

    @NonNull private final FeatureFlags mFeatureFlags;
    @NonNull private final Context mContext;

    /**
     * Create the SatelliteEnablementController singleton instance.
     * @param context The Context to use to create the SatelliteEnablementController.
     * @param looper The looper for the handler.
     * @param featureFlags The feature flag.
     */
    public static void make(@NonNull Context context, @NonNull Looper looper,
            @NonNull FeatureFlags featureFlags) {
        if (sInstance == null) {
            ManualEnablementController.make(looper, featureFlags);
            AutoEnablementController.make(context, looper, featureFlags);
            sInstance = new SatelliteEnablementController(context, looper, featureFlags,
                    ManualEnablementController.getInstance(),
                    AutoEnablementController.getInstance());
        }
    }

    /**
     * Get the SatelliteEnablementController singleton instance.
     */
    @Nullable
    public static SatelliteEnablementController getInstance() {
        return sInstance;
    }

    /**
     * Set the SatelliteEnablementController singleton instance for testing.
     */
    @VisibleForTesting
    public static void setInstance(@Nullable SatelliteEnablementController instance) {
        sInstance = instance;
    }

    @VisibleForTesting
    public SatelliteEnablementController(
            @NonNull Context context, @NonNull Looper looper, @NonNull FeatureFlags featureFlags,
            @NonNull ManualEnablementController manualEnablementController,
            @NonNull AutoEnablementController autoEnablementController) {
        super(looper);
        mContext = context;
        mFeatureFlags = featureFlags;
        mAutoSatelliteController = autoEnablementController;
        mManualSatelliteController = manualEnablementController;
    }

    /**
     * Handles requests to enable or disable satellite connectivity.
     *
     * @param attributes The attributes of the enable request.
     * @param executor The executor on which the callback will be called.
     * @param resultListener Listener for the result of the operation.
     */
    public void requestSatelliteEnabled(int subId, @NonNull EnableRequestAttributes attributes,
            @NonNull Executor executor, @NonNull Consumer<Integer> resultListener) {
        setSatelliteEnabledForReason(subId, attributes.getConnectType(),
                attributes.getSatelliteEnablementRequestReason(), attributes.isEnabled());
        boolean isEnabled = isSatelliteEnabled(subId, attributes.getConnectType());

        SatelliteEnablementStrategy enablementController =
                getEnablementController(attributes.getConnectType());
        logd("requestSatelliteEnabled: subId = " + subId
                + " connectType = " + attributes.getConnectType()
                + " enabled = " + attributes.isEnabled()
                + " isEnabled = " + isEnabled);

        // TODO(b/509718939): Handle enablement controller selection for Hybrid connections
        if (enablementController != null) {
            if (isEnabled) {
                enablementController.enableSatellite(subId, attributes, executor, resultListener);
            } else {
                enablementController.disableSatellite(subId, attributes, executor, resultListener);
            }
        } else {
            Log.w(TAG, "requestSatelliteEnabled: No enablement controller found for connectType="
                    + SatelliteServiceUtils.carrierRoamingNtnConnectTypeToString(
                            attributes.getConnectType())
                    + ", subId=" + subId);
            executor.execute(() -> resultListener.accept(
                    SatelliteManager.SATELLITE_RESULT_INVALID_ARGUMENTS));
        }
    }

    /**
     * @return {@code true} if satellite is enabled for the given subId and connectType,
     * {@code false} otherwise.
     */
    public boolean isSatelliteEnabled(int subId, int connectType) {
        Map<Integer, Integer> subMap = mSatelliteDisabledMaskMap.get(subId);
        if (subMap == null) {
            return true; // No masks set, so enabled by default
        }
        return subMap.getOrDefault(connectType, 0) == 0;
    }

    /**
     * Updates the satellite enablement state for a specific subId, connectType, and reason.
     */
    private void setSatelliteEnabledForReason(int subId, int connectType, int reason,
            boolean enabled) {
        logd("setSatelliteEnabledForReason: subId = " + subId
                + " connectType = " + connectType
                + " reason = " + reason
                + " enabled = " + enabled);
        int flag = 1 << reason;

        // Ensure subId map exists, then update the mask for the specific connectType
        mSatelliteDisabledMaskMap.computeIfAbsent(subId, k -> new ConcurrentHashMap<>())
                .compute(connectType, (k, currentMask) -> {
                    int oldMask = (currentMask == null) ? 0 : currentMask;
                    int newMask;
                    if (enabled) {
                        newMask = oldMask & ~flag; // Remove the disablement reason
                    } else {
                        newMask = oldMask | flag;  // Add the disablement reason
                    }
                    logd("setSatelliteEnabledForReason: subId=" + subId
                            + ", connectType=" + SatelliteServiceUtils
                                    .carrierRoamingNtnConnectTypeToString(connectType)
                            + ", oldMask=" + oldMask
                            + ", newMask=" + newMask);
                    return newMask;
                });
        // TODO: persist satellite enablement state on SubscriptionManager DB
        // persistSatelliteDisabledFlags(subId);
    }

    private SatelliteEnablementStrategy getEnablementController(
            @CarrierConfigManager.CARRIER_ROAMING_NTN_CONNECT_TYPE int connectType) {
        switch (connectType) {
            case CARRIER_ROAMING_NTN_CONNECT_MANUAL:
                return mManualSatelliteController;
            case CARRIER_ROAMING_NTN_CONNECT_AUTOMATIC:
                return mAutoSatelliteController;
            default:
                return null;
        }
    }

    private static void logd(@NonNull String log) {
        Log.d(TAG, log);
    }
}
