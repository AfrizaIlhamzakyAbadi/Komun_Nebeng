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
import android.os.Handler;
import android.os.Looper;
import android.telephony.satellite.EnableRequestAttributes;
import android.telephony.satellite.SatelliteManager;
import android.util.Log;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.flags.FeatureFlags;
import com.android.internal.telephony.flags.Flags;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * Manages satellite enablement for manual connections.
 */
@FlaggedApi(Flags.FLAG_SATELLITE_UPSELL_26Q4)
public class ManualEnablementController extends Handler implements SatelliteEnablementStrategy {

    private static final String TAG = "ManualEnableController";

    @NonNull private final FeatureFlags mFeatureFlags;

    private static ManualEnablementController sInstance;

    /**
     * @return The singleton instance of ManualEnablementController.
     */
    public static ManualEnablementController getInstance() {
        if (sInstance == null) {
            loge("ManualEnablementController was not yet initialized.");
        }
        return sInstance;
    }

    /**
     * Set the singleton instance for testing.
     * @param instance The instance to set.
     */
    @VisibleForTesting
    public static void setInstance(@NonNull ManualEnablementController instance) {
        sInstance = instance;
    }

    /**
     * Create the ManualEnablementController singleton instance.
     * @param looper The looper for the handler.
     * @param featureFlags The feature flag.
     */
    public static void make(@NonNull Looper looper, @NonNull FeatureFlags featureFlags) {
        if (sInstance == null) {
            sInstance = new ManualEnablementController(looper, featureFlags);
        }
    }

    /**
     * Create a ManualEnablementController to handle all events
     * relating to Manual Satellites.
     *
     * @param looper The looper for the handler
     * @param featureFlags The feature flag
     */
    @VisibleForTesting
    public ManualEnablementController(@NonNull Looper looper, @NonNull FeatureFlags featureFlags) {
        super(looper);
        mFeatureFlags = featureFlags;
    }

    @Override
    public void enableSatellite(int subId, @NonNull EnableRequestAttributes attributes,
            @NonNull Executor executor, @NonNull Consumer<Integer> resultListener) {
        // Implementation for manual satellite enablement will go here.
        resultListener.accept(SatelliteManager.SATELLITE_RESULT_SUCCESS);
    }

    @Override
    public void disableSatellite(int subId, @NonNull EnableRequestAttributes attributes,
            @NonNull Executor executor, @NonNull Consumer<Integer> resultListener) {
        // Implementation for manual satellite disablement will go here.
        resultListener.accept(SatelliteManager.SATELLITE_RESULT_SUCCESS);
    }

    private static void loge(@NonNull String log) {
        Log.e(TAG, log);
    }
}
