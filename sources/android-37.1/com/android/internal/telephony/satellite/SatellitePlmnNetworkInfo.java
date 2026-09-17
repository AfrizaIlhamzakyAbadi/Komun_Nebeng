/*
 * Copyright (C) 2025 The Android Open Source Project
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

import static android.telephony.satellite.SatelliteManager.NT_RADIO_TECHNOLOGY_LTE_DTC;
import static android.telephony.satellite.SatelliteManager.NT_RADIO_TECHNOLOGY_NB_IOT_NTN;
import static android.telephony.satellite.SatelliteManager.NT_RADIO_TECHNOLOGY_NR_DTC;
import static android.telephony.satellite.SatelliteManager.NT_RADIO_TECHNOLOGY_NR_NTN;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.hardware.radio.network.SatelliteNetworkInfo;
import android.telephony.AccessNetworkConstants.AccessNetworkType;
import android.telephony.SubscriptionManager;
import android.telephony.satellite.SatelliteManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents the complete satellite PLMN network information, including lists of
 * allowed and disallowed networks.
 *
 * <p>This class serves as the primary data structure for populating the
 * {@link android.hardware.radio.network.SatelliteNetworkInfo} structure in the HAL API.
 * It translates high-level framework configurations into the specific technical details
 * required by the modem, such as Radio Access Technology (RAT) and priority.
 *
 * <p>Each network is encapsulated in the nested {@link NetworkInfo} class,
 * which can be constructed using a {@link NetworkInfo.Builder}.
 */
public final class SatellitePlmnNetworkInfo {
    private static final String TAG = "SatellitePlmnNetworkInfo";
    /** List of networks that the modem is permitted to attempt connection to. */
    @NonNull
    private List<NetworkInfo> mAllowedPlmns = new ArrayList<>();
    /** List of networks that the modem must explicitly ignore or bar. */
    @NonNull
    private List<NetworkInfo> mDisallowedPlmns = new ArrayList<>();

    /**
     * Constructs a new SatellitePlmnNetworkInfo.
     *
     * @param allowedPlmns    A list of networks the user is allowed to connect to.
     * @param disallowedPlmns A list of networks the user is not allowed to connect to.
     */
    public SatellitePlmnNetworkInfo(@NonNull List<NetworkInfo> allowedPlmns,
            @NonNull List<NetworkInfo> disallowedPlmns) {
        mAllowedPlmns = Collections.unmodifiableList(Objects.requireNonNull(allowedPlmns));
        mDisallowedPlmns = Collections.unmodifiableList(Objects.requireNonNull(disallowedPlmns));
    }

    /**
     * @return The unmodifiable list of allowed satellite networks.
     */
    @NonNull
    public List<NetworkInfo> getAllowedPlmns() {
        return mAllowedPlmns;
    }

    /**
     * @return The unmodifiable list of disallowed satellite networks.
     */
    @NonNull
    public List<NetworkInfo> getDisallowedPlmns() {
        return mDisallowedPlmns;
    }

    /**
     * @return A new array containing the allowed satellite networks.
     */
    @NonNull
    public NetworkInfo[] getAllowedPlmnsAsArray() {
        return mAllowedPlmns.toArray(new NetworkInfo[0]);
    }

    /**
     * @return A new array containing the disallowed satellite networks.
     */
    @NonNull
    public NetworkInfo[] getDisallowedPlmnsAsArray() {
        return mDisallowedPlmns.toArray(new NetworkInfo[0]);
    }

    /**
     * Configuration for a specific cellular or satellite network.
     * An instance of this class can be created using {@link Builder}.
     */
    public static final class NetworkInfo {

        /**
         * Public Land Mobile Network ID (MCC + MNC) as a 5 or 6 digit string.
         * This field is mandatory.
         */
        @NonNull
        private final String mPlmn;

        /**
         * List of Absolute Radio Frequency Channel Numbers. This list is optional
         * and thus might not be filled by framework.
         *
         * For GSM: List of ARFCN values.
         * For LTE: List of EARFCN values.
         * For NR: List of NRARFCN values.
         *
         * The default value is an empty array.
         */
        @NonNull
        private final int[] mArfcns;

        /**
         * Access network type. This field is optional and thus might not be filled by framework.
         */
        private final int mAccessNetwork;

        /**
         * Type of satellite technology if the target network is a satellite network.
         * This field is optional and thus might not be filled by framework.
         */
        private final int mSatelliteTechnology;

        /**
         * When satelliteTechnology is different from SatelliteTechnology#NONE,
         * the expected behavior is as follows:
         * {@code true} Modem shall treat the satellite network the same priority as other
         * terrestrial networks in cell reselection.
         * {@code false} Modem shall treat the satellite network with less priority than terrestrial
         * networks in cell reselection.
         */
        private final boolean mHasSamePriorityAsTn;

        private NetworkInfo(@NonNull Builder builder) {
            this.mPlmn = builder.mPlmn;
            this.mArfcns = builder.mArfcns;
            this.mAccessNetwork = builder.mAccessNetwork;
            this.mSatelliteTechnology = builder.mSatelliteTechnology;
            this.mHasSamePriorityAsTn = builder.mHasSamePriorityAsTn;
        }

        @NonNull
        public String getPlmn() {
            return mPlmn;
        }

        @NonNull
        public int[] getArfcns() {
            return mArfcns;
        }

        public int getAccessNetwork() {
            return mAccessNetwork;
        }

        public int getSatelliteTechnology() {
            return mSatelliteTechnology;
        }

        /**
         * @return mHasSamePriorityAsTn
         */
        public boolean hasSamePriorityAsTn() {
            return mHasSamePriorityAsTn;
        }

        /**
         * Static factory method to create a NetworkInfo with default values for a given PLMN.
         * The default values are:
         * - arfcns: empty array
         * - accessNetwork: AccessNetworkType.UNKNOWN
         * - satelliteTechnology: SatelliteManager.NT_RADIO_TECHNOLOGY_UNKNOWN
         * - hasSamePriorityAsTn: false
         *
         * @param plmn The PLMN for the network.
         * @return A new NetworkInfo object with default settings.
         */
        @NonNull
        public static NetworkInfo createWithDefaults(@NonNull String plmn) {
            // Builder's fields are already set to the defaults.
            return new Builder(plmn).build();
        }

        /**
         * Builder for creating {@link NetworkInfo} instances.
         */
        public static final class Builder {
            // Mandatory parameter
            @NonNull
            private final String mPlmn;

            // Optional parameters with default values
            @NonNull
            private int[] mArfcns = new int[0];
            private int mAccessNetwork = AccessNetworkType.UNKNOWN;
            private int mSatelliteTechnology = SatelliteManager.NT_RADIO_TECHNOLOGY_UNKNOWN;
            private boolean mHasSamePriorityAsTn = false;

            /**
             * Creates a builder for NetworkInfo.
             *
             * @param plmn The Public Land Mobile Network ID (MCC + MNC). This field is mandatory.
             */
            public Builder(@NonNull String plmn) {
                this.mPlmn = Objects.requireNonNull(plmn);
            }

            /** Sets the list of Absolute Radio Frequency Channel Numbers. */
            public Builder setArfcns(@NonNull int[] arfcns) {
                this.mArfcns = Objects.requireNonNull(arfcns);
                return this;
            }

            /** Sets the access network type. */
            public Builder setAccessNetwork(int accessNetwork) {
                this.mAccessNetwork = accessNetwork;
                return this;
            }

            /** Sets the satellite technology type. */
            public Builder setSatelliteTechnology(int satelliteTechnology) {
                this.mSatelliteTechnology = satelliteTechnology;
                return this;
            }

            /** Sets if the satellite network has the same priority as a terrestrial network. */
            public Builder setHasSamePriorityAsTn(boolean hasSamePriorityAsTn) {
                this.mHasSamePriorityAsTn = hasSamePriorityAsTn;
                return this;
            }

            /**
             * Builds the {@link NetworkInfo} object.
             *
             * @return The constructed NetworkInfo object.
             */
            @NonNull
            public NetworkInfo build() {
                return new NetworkInfo(this);
            }

            /**
             * Creates a builder initialized with the values from an existing NetworkInfo object.
             */
            public Builder(@NonNull NetworkInfo info) {
                this.mPlmn = info.mPlmn;
                this.mArfcns = info.mArfcns;
                this.mAccessNetwork = info.mAccessNetwork;
                this.mSatelliteTechnology = info.mSatelliteTechnology;
                this.mHasSamePriorityAsTn = info.mHasSamePriorityAsTn;
            }
        }

        /**
         * Converts this object to its equivalent HAL parcelable representation.
         *
         * @return A new HAL NetworkInfo object.
         */
        @NonNull
        public android.hardware.radio.network.NetworkInfo toHalNetworkInfo() {
            android.hardware.radio.network.NetworkInfo halInfo =
                    new android.hardware.radio.network.NetworkInfo();
            halInfo.plmn = this.mPlmn;
            halInfo.arfcns = this.mArfcns;
            halInfo.accessNetwork = this.mAccessNetwork;
            halInfo.satelliteTechnology = toHalSatelliteTechnology(this.mSatelliteTechnology);
            halInfo.hasSamePriorityAsTn = this.mHasSamePriorityAsTn;
            return halInfo;
        }

        /**
         * Match SatelliteManager constants to HAL SatelliteTechnology constants.
         */
        private static int toHalSatelliteTechnology(int frameworkTech) {
            switch (frameworkTech) {
                case SatelliteManager.NT_RADIO_TECHNOLOGY_NB_IOT_NTN:
                    return android.hardware.radio.network.SatelliteTechnology.SAT_TECH_NB_IOT_NTN;

                case SatelliteManager.NT_RADIO_TECHNOLOGY_LTE_DTC:
                case SatelliteManager.NT_RADIO_TECHNOLOGY_NR_DTC:
                    return android.hardware.radio.network.SatelliteTechnology.SAT_TECH_DTC;

                case SatelliteManager.NT_RADIO_TECHNOLOGY_NR_NTN:
                    return android.hardware.radio.network.SatelliteTechnology.SAT_TECH_3GPP_NTN;

                default:
                    logd("Handle unsupported or unknown technologies as SAT_TECH_NONE or default");
                    return android.hardware.radio.network.SatelliteTechnology.SAT_TECH_NONE;
            }
        }

        @Override
        public String toString() {
            return "NetworkInfo{"
                    + "mPlmn='" + mPlmn + '\''
                    + ", mArfcns=" + Arrays.toString(mArfcns)
                    + ", mAccessNetwork=" + mAccessNetwork
                    + ", mSatelliteTechnology=" + mSatelliteTechnology
                    + ", mHasSamePriorityAsTn=" + mHasSamePriorityAsTn
                    + '}';
        }
    }

    /**
     * Static factory method to create a {@link SatellitePlmnNetworkInfo} from simple PLMN lists.
     *
     * <p>This method performs the heavy lifting of resolving high-level PLMN strings into
     * detailed {@link NetworkInfo} objects.
     *
     * <p>For each PLMN in the allowed list:
     * 1. It looks up all supported satellite technologies via the provided
     * {@code supportedTechMap}.
     * 2. It creates a distinct {@link NetworkInfo} entry for each supported technology.
     * 3. It automatically maps the technology to the correct 3GPP Access Network Type
     * (e.g., NB-IOT/LTE-DTC maps to EUTRAN, while NR-NTN/NR-DTC maps to NGRAN).
     *
     * <p>For each PLMN in the disallowed list:
     * 1. It creates a {@link NetworkInfo} object with default technical settings (e.g., unknown
     * RAT),
     * as the specific technology details are generally not required for barring.
     *
     * @param allowedPlmns     A list of PLMN strings (MCC+MNC) to be allowed.
     * @param disallowedPlmns  A list of PLMN strings (MCC+MNC) to be disallowed.
     * @param supportedTechMap A mapping of PLMN to its supported
     *                         {@link SatelliteManager.NTRadioTechnology}.
     * @return A new {@link SatellitePlmnNetworkInfo} object containing the fully resolved
     * configuration.
     */
    @NonNull
    public static SatellitePlmnNetworkInfo fromPlmn(
            int subId,
            @Nullable List<String> allowedPlmns,
            @Nullable List<String> disallowedPlmns,
            @NonNull Map<String, List<Integer>> supportedTechMap) {
        if (subId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            logd("fromPlmn: Invalid subId");
            return new SatellitePlmnNetworkInfo(new ArrayList<>(), new ArrayList<>());
        }

        logd("fromPlmn: allowedPlmns=" + allowedPlmns + ", disallowedPlmns="
                + disallowedPlmns);

        List<NetworkInfo> allowedList = new ArrayList<>();
        if (allowedPlmns != null) {
            for (String plmn : allowedPlmns) {
                if (!TextUtils.isEmpty(plmn)) {
                    // Fetch the supported satellite technologies for this specific PLMN.
                    List<Integer> supportedSatelliteTechList =
                            supportedTechMap.getOrDefault(plmn, new ArrayList<>());

                    // We create a separate NetworkInfo for each supported technology of the PLMN
                    // to allow the modem to prioritize or handle them specifically.
                    for (int satelliteTech : supportedSatelliteTechList) {
                        NetworkInfo.Builder networkInfobuilder = new NetworkInfo.Builder(plmn);
                        networkInfobuilder.setSatelliteTechnology(satelliteTech);

                        // Map the satellite technology to the appropriate 3GPP access network type
                        // required by the HAL layer.
                        switch (satelliteTech) {
                            case NT_RADIO_TECHNOLOGY_NB_IOT_NTN:
                            case NT_RADIO_TECHNOLOGY_LTE_DTC:
                                networkInfobuilder.setAccessNetwork(AccessNetworkType.EUTRAN);
                                break;
                            case NT_RADIO_TECHNOLOGY_NR_NTN:
                            case NT_RADIO_TECHNOLOGY_NR_DTC:
                                networkInfobuilder.setAccessNetwork(AccessNetworkType.NGRAN);
                                break;
                            default:
                                // Fallback for unknown or proprietary technologies.
                                networkInfobuilder.setAccessNetwork(AccessNetworkType.UNKNOWN);
                                break;
                        }
                        allowedList.add(networkInfobuilder.build());
                    }
                }
            }
        }

        List<NetworkInfo> disallowedList = new ArrayList<>();
        if (disallowedPlmns != null) {
            for (String plmn : disallowedPlmns) {
                if (!TextUtils.isEmpty(plmn)) {
                    // Disallowed PLMNs are created with default network settings.
                    disallowedList.add(NetworkInfo.createWithDefaults(plmn));
                }
            }
        }

        return new SatellitePlmnNetworkInfo(allowedList, disallowedList);
    }

    /**
     * Converts this Java object to its equivalent HAL parcelable representation,
     * which can be sent to the modem.
     *
     * @return A new HAL SatelliteNetworkInfo object.
     */
    @NonNull
    public android.hardware.radio.network.SatelliteNetworkInfo toHalSatelliteNetworkInfo() {
        android.hardware.radio.network.SatelliteNetworkInfo satelliteNetworkInfoForHal =
                new SatelliteNetworkInfo();

        satelliteNetworkInfoForHal.allowedPlmns = mAllowedPlmns.stream()
                .map(NetworkInfo::toHalNetworkInfo)
                .toArray(android.hardware.radio.network.NetworkInfo[]::new);

        satelliteNetworkInfoForHal.disallowedPlmns = mDisallowedPlmns.stream()
                .map(NetworkInfo::toHalNetworkInfo)
                .toArray(android.hardware.radio.network.NetworkInfo[]::new);

        return satelliteNetworkInfoForHal;
    }

    @Override
    public String toString() {
        return "SatellitePlmnNetworkInfo{"
                + "mAllowedPlmns=" + mAllowedPlmns
                + ", mDisallowedPlmns=" + mDisallowedPlmns
                + '}';
    }

    private static void logd(@NonNull String log) {
        Log.d(TAG, log);
    }
}
