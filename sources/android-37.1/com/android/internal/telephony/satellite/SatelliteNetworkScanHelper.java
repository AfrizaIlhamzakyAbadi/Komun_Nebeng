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

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.Context;
import android.os.PersistableBundle;
import android.telephony.AccessNetworkConstants;
import android.telephony.AccessNetworkUtils;
import android.telephony.CarrierConfigManager;
import android.telephony.NetworkScanRequest;
import android.telephony.RadioAccessSpecifier;
import android.telephony.satellite.SatelliteManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Helper class to build NetworkScanRequest for satellite network scan.
 */
public class SatelliteNetworkScanHelper {
    private static final String TAG = "SatelliteNetworkScanHelper";

    private static final int DEFAULT_MAX_SEARCH_TIME_SEC = 60;

    /**
     * Builds a NetworkScanRequest for active satellite upsell search.
     *
     * @param context Context.
     * @param satelliteController SatelliteController instance.
     * @param subId Subscription ID.
     * @return A built NetworkScanRequest wrapped in Optional, or Optional.empty() if none built.
     */
    public static Optional<NetworkScanRequest> buildNetworkRequestForUpsellScan(
            Context context, SatelliteController satelliteController, int subId) {
        List<String> purchasablePlmnList = satelliteController.getPurchaseCapablePlmns(subId);
        if (purchasablePlmnList == null || purchasablePlmnList.isEmpty()) {
            Log.d(TAG, "buildNetworkRequestForUpsellScan: purchasablePlmnList is null or empty");
            return Optional.empty();
        }

        PersistableBundle satellitePlmnBundle = getSatellitePlmnBundle(context, subId);
        Set<RadioAccessSpecifier> specs = new LinkedHashSet<>();
        Set<String> plmnsToScan = new LinkedHashSet<>();

        Set<RadioAccessSpecifier> defaultSpecs = getDefaultSpecifiers(satelliteController);

        for (String plmn : purchasablePlmnList) {
            PersistableBundle plmnConfig = (satellitePlmnBundle != null)
                    ? satellitePlmnBundle.getPersistableBundle(plmn)
                    : null;

            if (plmnConfig == null) {
                Log.d(TAG, "buildNetworkRequestForUpsellScan: plmnConfig is null for " + plmn);
                plmnsToScan.add(plmn);
                specs.addAll(defaultSpecs);
                continue;
            }
            Set<RadioAccessSpecifier> specsForPlmn =
                    getSpecifiersForPlmnScan(plmn, plmnConfig, defaultSpecs);

            if (!specsForPlmn.isEmpty()) {
                specs.addAll(specsForPlmn);
                plmnsToScan.add(plmn);
            }
        }

        if (specs.isEmpty()) {
            Log.d(TAG, "buildNetworkRequestForUpsellScan: No radio access specifiers built");
            return Optional.empty();
        }

        return Optional.of(new NetworkScanRequest(
                NetworkScanRequest.SCAN_TYPE_ONE_SHOT,
                specs.toArray(new RadioAccessSpecifier[0]),
                0, // searchPeriodicity
                getMaxSearchTimeSec(context),
                false, // incrementalResults
                0, // incrementalResultsPeriodicity
                new ArrayList<>(plmnsToScan)
        ));
    }

    /** Extracts the satellite configuration bundle from CarrierConfig. */
    @Nullable
    private static PersistableBundle getSatellitePlmnBundle(Context context, int subId) {
        CarrierConfigManager configManager = context.getSystemService(CarrierConfigManager.class);
        if (configManager == null) {
            return null;
        }

        PersistableBundle carrierConfigs = configManager.getConfigForSubId(subId);
        return (carrierConfigs != null)
                ? carrierConfigs.getPersistableBundle(
                        CarrierConfigManager.KEY_SATELLITE_CONFIGS_PER_PLMN_BUNDLE)
                : null;
    }

    /** Resolves the access network types (RANs) to scan for a PLMN. */
    @NonNull
    private static int[] getAccessNetworkTypesForPlmn(
            @NonNull PersistableBundle plmnConfig,
            @NonNull Set<RadioAccessSpecifier> defaultSpecs) {
        int[] techs = plmnConfig.getIntArray(
                CarrierConfigManager.KEY_SATELLITE_TECHNOLOGY_INT_ARRAY);
        if (techs == null) {
            return defaultSpecs.stream()
                    .map(RadioAccessSpecifier::getRadioAccessNetwork)
                    .mapToInt(Integer::intValue)
                    .toArray();
        }
        return Arrays.stream(techs)
                .map(SatelliteNetworkScanHelper::mapSatelliteTechToAccessNetworkType)
                .filter(ran -> ran != AccessNetworkConstants.AccessNetworkType.UNKNOWN)
                .toArray();
    }

    /** Builds and returns the Set of RadioAccessSpecifiers for a specific PLMN. */
    @NonNull
    private static Set<RadioAccessSpecifier> getSpecifiersForPlmnScan(
            String plmn,
            @NonNull PersistableBundle plmnConfig,
            @NonNull Set<RadioAccessSpecifier> defaultSpecs) {
        int[] accessNetworkTypes = getAccessNetworkTypesForPlmn(plmnConfig, defaultSpecs);
        int[] freqs = plmnConfig.getIntArray(
                CarrierConfigManager.KEY_SATELLITE_SUPPORTED_FREQUENCIES_INT_ARRAY);

        Log.d(TAG, "getSpecifiersForPlmnScan: accessNetworkTypes="
                + Arrays.toString(accessNetworkTypes) + ", freqs=" + Arrays.toString(freqs));

        if (shouldFallbackToDefaultSpecs(accessNetworkTypes, freqs)) {
            Log.w(TAG, "Fallback to default for PLMN " + plmn);
            return defaultSpecs;
        }

        Set<RadioAccessSpecifier> specsForPlmn = new LinkedHashSet<>();
        for (int accessNetworkType : accessNetworkTypes) {
            RadioAccessSpecifier spec = buildRadioAccessSpecifier(accessNetworkType, freqs);
            if (spec != null) {
                specsForPlmn.add(spec);
            }
        }
        return specsForPlmn;
    }

    @Nullable
    private static RadioAccessSpecifier buildRadioAccessSpecifier(
            int accessNetworkType, int[] freqs) {
        if (freqs == null) {
            return new RadioAccessSpecifier(accessNetworkType, null, null);
        }
        int[] validFreqs = Arrays.stream(freqs)
                .filter(f -> AccessNetworkUtils.getOperatingBand(accessNetworkType, f)
                        != AccessNetworkUtils.INVALID_BAND)
                .toArray();
        if (validFreqs.length == 0) {
            Log.d(TAG, "No valid frequencies found in " + Arrays.toString(freqs));
            return null;
        }
        int[] bands = Arrays.stream(validFreqs)
                .map(f -> AccessNetworkUtils.getOperatingBand(accessNetworkType, f))
                .distinct()
                .toArray();

        return new RadioAccessSpecifier(accessNetworkType, bands, validFreqs);
    }

    /** Determines if the configuration requires a fallback to default specifiers. */
    private static boolean shouldFallbackToDefaultSpecs(
            @NonNull int[] accessNetworkTypes, @Nullable int[] freqs) {
        return accessNetworkTypes.length == 0
                || (freqs != null && freqs.length > 0
                        && !hasAnyValidBand(accessNetworkTypes, freqs));
    }

    /** Checks if at least one valid operating band exists for frequencies and RANs. */
    private static boolean hasAnyValidBand(int[] accessNetworkTypes, int[] freqs) {
        return Arrays.stream(accessNetworkTypes).anyMatch(ran ->
                Arrays.stream(freqs).anyMatch(freq ->
                        AccessNetworkUtils.getOperatingBand(ran, freq)
                                != AccessNetworkUtils.INVALID_BAND));
    }

    /** Retrieves maximum search time for upsell network scan from resource config. */
    private static int getMaxSearchTimeSec(@NonNull Context context) {
        try {
            return context.getResources().getInteger(
                    com.android.internal.R.integer
                            .config_upsell_network_scan_max_search_time_sec);
        } catch (android.content.res.Resources.NotFoundException e) {
            Log.d(TAG, "getMaxSearchTimeSec: Config not found, using default "
                    + DEFAULT_MAX_SEARCH_TIME_SEC + "s");
            return DEFAULT_MAX_SEARCH_TIME_SEC;
        }
    }

    /** Builds default fallback wildcard specifiers from supported satellite techs. */
    private static Set<RadioAccessSpecifier> getDefaultSpecifiers(
            SatelliteController satelliteController) {
        Set<RadioAccessSpecifier> specs = new LinkedHashSet<>();
        Set<Integer> supportedRadioTechs = satelliteController.getSupportedRadioTechnologies();
        if (supportedRadioTechs != null && !supportedRadioTechs.isEmpty()) {
            for (int tech : supportedRadioTechs) {
                int accessNetworkType = mapSatelliteTechToAccessNetworkType(tech);
                if (accessNetworkType == AccessNetworkConstants.AccessNetworkType.UNKNOWN) {
                    continue;
                }
                RadioAccessSpecifier spec = buildRadioAccessSpecifier(accessNetworkType, null);
                if (spec != null) {
                    specs.add(spec);
                }
            }
        }
        Log.d(TAG, "getDefaultSpecifiers: " + specs);
        return specs;
    }

    private static int mapSatelliteTechToAccessNetworkType(int tech) {
        return switch (tech) {
            case SatelliteManager.NT_RADIO_TECHNOLOGY_NB_IOT_NTN,
                 SatelliteManager.NT_RADIO_TECHNOLOGY_LTE_DTC ->
                    AccessNetworkConstants.AccessNetworkType.EUTRAN;
            case SatelliteManager.NT_RADIO_TECHNOLOGY_NR_NTN,
                 SatelliteManager.NT_RADIO_TECHNOLOGY_NR_DTC ->
                    AccessNetworkConstants.AccessNetworkType.NGRAN;
            default -> AccessNetworkConstants.AccessNetworkType.UNKNOWN;
        };
    }
}
