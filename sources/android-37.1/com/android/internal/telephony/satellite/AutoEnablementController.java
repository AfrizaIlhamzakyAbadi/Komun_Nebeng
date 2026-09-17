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
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.telephony.CarrierConfigManager;
import android.telephony.satellite.EnableRequestAttributes;
import android.telephony.satellite.SatelliteManager;
import android.util.Log;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.flags.FeatureFlags;
import com.android.internal.telephony.flags.Flags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Manages satellite enablement for automatic connections.
 */
@FlaggedApi(Flags.FLAG_SATELLITE_UPSELL_26Q4)
public class AutoEnablementController extends Handler implements SatelliteEnablementStrategy {

    private static final String TAG = "AutoEnableController";
    @NonNull private final FeatureFlags mFeatureFlags;
    @NonNull private final Context mContext;

    private static AutoEnablementController sInstance;

    /**
     * @return The singleton instance of AutoEnablementController.
     */
    public static AutoEnablementController getInstance() {
        if (sInstance == null) {
            loge("AutoEnableController was not yet initialized.");
        }
        return sInstance;
    }

    /**
     * Set the singleton instance for testing.
     * @param instance The instance to set.
     */
    @VisibleForTesting
    public static void setInstance(@NonNull AutoEnablementController instance) {
        sInstance = instance;
    }

    /**
     * Create the AutoEnablementController singleton instance.
     * @param context The Context to use.
     * @param looper The looper for the handler.
     * @param featureFlags The feature flag.
     */
    public static void make(@NonNull Context context, @NonNull Looper looper,
            @NonNull FeatureFlags featureFlags) {
        if (sInstance == null) {
            sInstance = new AutoEnablementController(context, looper, featureFlags);
        }
    }

    /**
     * Create a AutoEnablementController to handle all events
     * relating to Auto Satellites.
     *
     * @param context The Context to use
     * @param looper The looper for the handler
     * @param featureFlags The feature flag
     */
    @VisibleForTesting
    public AutoEnablementController(@NonNull Context context, @NonNull Looper looper,
            @NonNull FeatureFlags featureFlags) {
        super(looper);
        mContext = context;
        mFeatureFlags = featureFlags;
    }



    @Override
    public void enableSatellite(int subId, @NonNull EnableRequestAttributes attributes,
            @NonNull Executor executor, @NonNull Consumer<Integer> resultListener) {
        Log.d(TAG, "enableSatellite: subId=" + subId + ", attributes=" + attributes);
        // TODO(b/519490748): Add support for enabling satellite for specific reason once
        //  use cases are defined.
        AutoModeConnectionController connectionController =
                AutoModeConnectionController.getInstance();
        if (connectionController == null) {
            loge("enableSatellite: connectionController is null");
            executor.execute(() -> resultListener.accept(
                    SatelliteManager.SATELLITE_RESULT_INVALID_TELEPHONY_STATE));
            return;
        }

        Consumer<Integer> wrappedListener = result -> {
            Log.d(TAG, "enableSatellite: result=" + result + " for subId=" + subId);
            executor.execute(() -> resultListener.accept(result));
        };
        SatelliteController satelliteController = SatelliteController.getInstance();
        // TODO(b/519489989): Add PLMN calculations, based on satellite
        //  enablement/disablement use-cases
        List<String> allPlmnList = new ArrayList<>(satelliteController.getAllPlmnSet());
        List<String> allowedPlmnList =
                satelliteController.getAllowedCarrierPlmnListForModem(subId);
        Map<String, List<Integer>> supportedTechMap =
                satelliteController.getPlmnSatelliteTechForCarrier(subId);
        PersistableBundle config = getCarrierConfig(subId);
        int reason = attributes.getSatelliteEnablementRequestReason();
        int searchIntervalMs = getSearchIntervalMillis(config, reason);
        int validDurationSec = getValidDurationSec(config, reason);

        Log.d(TAG, "enableSatellite: subId=" + subId
                + ", allPlmnList=" + allPlmnList
                + ", allowedPlmnList=" + allowedPlmnList
                + ", searchIntervalMs=" + searchIntervalMs
                + ", validDurationSec=" + validDurationSec
                + ", supportedTechMap=" + supportedTechMap);

        connectionController.setupSatelliteConnection(
                subId,
                true, /* isSatelliteExpectedToBeEnabled */
                allPlmnList,
                allowedPlmnList,
                attributes.isPrioritizedScanningRequired(),
                searchIntervalMs,
                validDurationSec,
                supportedTechMap,
                "", /* satellitePlmnForSimFplmnManagement (empty for now) */
                wrappedListener
        );
    }

    @Override
    public void disableSatellite(int subId, @NonNull EnableRequestAttributes attributes,
            @NonNull Executor executor, @NonNull Consumer<Integer> resultListener) {
        Log.d(TAG, "disableSatellite: subId=" + subId + ", attributes=" + attributes);
        // TODO(b/519490748): Add support for disabling satellite for specific reason once
        //  use cases are defined.
        AutoModeConnectionController connectionController =
                AutoModeConnectionController.getInstance();
        if (connectionController == null) {
            loge("disableSatellite: connectionController is null");
            executor.execute(() -> resultListener.accept(
                    SatelliteManager.SATELLITE_RESULT_INVALID_TELEPHONY_STATE));
            return;
        }

        Consumer<Integer> wrappedListener = result -> {
            Log.d(TAG, "disableSatellite: result=" + result + " for subId=" + subId);
            executor.execute(() -> resultListener.accept(result));
        };
        SatelliteController satelliteController = SatelliteController.getInstance();
        List<String> allPlmnList = new ArrayList<>(satelliteController.getAllPlmnSet());
        List<String> allowedPlmnList =
                satelliteController.getAllowedCarrierPlmnListForModem(subId);
        Map<String, List<Integer>> supportedTechMap =
                satelliteController.getPlmnSatelliteTechForCarrier(subId);
        PersistableBundle config = getCarrierConfig(subId);
        int reason = attributes.getSatelliteEnablementRequestReason();
        int searchIntervalMs = getSearchIntervalMillis(config, reason);
        int validDurationSec = getValidDurationSec(config, reason);

        Log.d(TAG, "disableSatellite: subId=" + subId
                + ", allPlmnList=" + allPlmnList
                + ", allowedPlmnList=" + allowedPlmnList
                + ", searchIntervalMs=" + searchIntervalMs
                + ", validDurationSec=" + validDurationSec
                + ", supportedTechMap=" + supportedTechMap);

        connectionController.setupSatelliteConnection(
                subId,
                false, /* isSatelliteExpectedToBeEnabled */
                allPlmnList,
                allowedPlmnList,
                attributes.isPrioritizedScanningRequired(),
                searchIntervalMs,
                validDurationSec,
                supportedTechMap,
                "", /* satellitePlmnForSimFplmnManagement (empty for now) */
                wrappedListener
        );
    }

    private PersistableBundle getCarrierConfig(int subId) {
        CarrierConfigManager carrierConfigManager =
                mContext.getSystemService(CarrierConfigManager.class);
        if (carrierConfigManager != null) {
            return carrierConfigManager.getConfigForSubId(subId);
        }
        return null;
    }

    // TODO(b/519490063): Add necessary SatelliteConfig reader methods that will look up
    // both config updater and carrier config with right precedence and return the right values.
    private int getSearchIntervalMillis(PersistableBundle config, int reason) {
        if (config == null) {
            return -1;
        }
        PersistableBundle bundle = config.getPersistableBundle(
                CarrierConfigManager.KEY_SATELLITE_PRIORITIZED_SCAN_SEARCH_INTERVAL_MS_BUNDLE);
        if (bundle == null) {
            return -1;
        }
        return bundle.getInt(String.valueOf(reason), -1);
    }

    private int getValidDurationSec(PersistableBundle config, int reason) {
        if (config == null) {
            return 0;
        }
        PersistableBundle bundle = config.getPersistableBundle(
                CarrierConfigManager.KEY_SATELLITE_PRIORITIZED_SCAN_VALID_DURATION_SEC_BUNDLE);
        if (bundle == null) {
            return 0;
        }
        return bundle.getInt(String.valueOf(reason), 0);
    }

    private static void loge(@NonNull String log) {
        Log.e(TAG, log);
    }

}
