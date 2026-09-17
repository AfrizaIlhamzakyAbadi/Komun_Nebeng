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

package android.ranging.uwb;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.IntRange;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.Parcel;
import android.os.Parcelable;
import android.ranging.uwb.UwbRangingParams.SlotDuration;
import android.util.Log;

import com.android.ranging.flags.Flags;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Class to represent UWB Downlink TDoA ranging parameters.
 */
@FlaggedApi(Flags.FLAG_RANGING_STACK_UPDATES_26_Q_2)
public final class DlTdoaRangingParams implements Parcelable {
    private static final String TAG = "DlTdoaRangingParams";

    /**
     * Defines supported DL-TDoA Ranging Measurement notification and result versions.
     */
    /** DL-TDoA Ranging Measurement version 1 */
    public static final int MEASUREMENT_VERSION_1 = 1;
    /** DL-TDoA Ranging Measurement version 2 */
    public static final int MEASUREMENT_VERSION_2 = 2;
    /** DL-TDoA Ranging Measurement version unknown */
    public static final int MEASUREMENT_VERSION_UNKNOWN = Integer.MAX_VALUE;

    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            MEASUREMENT_VERSION_1,
            MEASUREMENT_VERSION_2,
            MEASUREMENT_VERSION_UNKNOWN,
    })
    public @interface MeasurementVersion {
    }

    // Vendor Specific Element
    private static final int FIRA_OOB_BLE_VSE_MINIMUM_TOTAL_LENGTH = 5;
    private static final int FIRA_OOB_WIFI_VSE_MINIMUM_TOTAL_LENGTH = 6;

    // BLE Specific Header
    private static final int FIRA_OOB_BLE_DATA_TYPE_UUID_16BITS = 0x16;
    private static final int FIRA_OOB_BLE_CP_UUID_0 = 0xF3;  // Connector Primary
    private static final int FIRA_OOB_BLE_CP_UUID_1 = 0xFF;  // Connector Primary
    private static final int FIRA_OOB_BLE_CS_UUID_0 = 0xF4;  // Connector Secondary
    private static final int FIRA_OOB_BLE_CS_UUID_1 = 0xFF;  // Connector Secondary

    // WiFi Specific Header
    private static final int FIRA_OOB_WIFI_VSE_ID = 0xDD;
    private static final int FIRA_OOB_WIFI_OUI_0 = 0x5A;
    private static final int FIRA_OOB_WIFI_OUI_1 = 0x18;
    private static final int FIRA_OOB_WIFI_OUI_2 = 0xFF;

    // UWB Configuration Sub-Element
    private static final int FIRA_SUB_ELEMENT_TYPE_UWB_CONFIG = 0x05;
    private static final int FIRA_SUB_ELEMENT_TYPE_WIFI_TIME_SYNC = 0x0B;
    private static final int FIRA_OOB_UWB_CONFIGURATION_HEADER_LENGTH = 2;

    // UWB Configuration ID for Untracked Navigation Profile
    private static final int FIRA_UWB_UNTRACKED_NAVIGATION_PROFILE_ID = 0x02;

    // UWB Configuration Parameter Tags
    private static final int TAG_CHANNEL_NUMBER = 0x04;
    private static final int TAG_DEVICE_MAC_ADDRESS = 0x06;
    private static final int TAG_SLOT_DURATION = 0x08;
    private static final int TAG_RANGING_DURATION = 0x09;
    private static final int TAG_PREAMBLE_CODE_INDEX = 0x14;
    private static final int TAG_SLOTS_PER_RR = 0x1B;
    private static final int TAG_VENDOR_ID = 0x27;
    private static final int TAG_STATIC_STS_IV = 0x28;
    private static final int TAG_DL_TDOA_MEASUREMENT_NTF_V2 = 0x4F;
    private static final int TAG_INITIATOR_DT_ANCHOR_ROUND_INDEX = 0x52;
    private static final int TAG_SESSION_ID = 0x9F;

    // As per FiRa/UCI, Slot Duration is typically in RSTU (Ranging Slot Time Units),
    // where 1200 RSTU is approx 1ms. We convert to the nearest supported ms value.
    private static final int RSTU_PER_MS = 1200;

    private final int mSessionId;
    private final UwbAddress mDeviceAddress;
    private final byte[] mSessionKeyInfo;
    private final UwbComplexChannel mComplexChannel;
    private final int mRangingIntervalMs;
    @SlotDuration
    private final int mSlotDuration;
    private final int mSlotsPerRangingRound;
    private final byte[] mRangingRoundIndexes;
    @MeasurementVersion
    private final int mMeasurementVersion;

    private DlTdoaRangingParams(Builder builder) {
        mSessionId = builder.mSessionId;
        mDeviceAddress = builder.mDeviceAddress;
        mSessionKeyInfo = builder.mSessionKeyInfo;
        mComplexChannel = builder.mComplexChannel;
        mRangingIntervalMs = builder.mRangingIntervalMs;
        mSlotDuration = builder.mSlotDuration;
        mSlotsPerRangingRound = builder.mSlotsPerRangingRound;
        mRangingRoundIndexes = builder.mRangingRoundIndexes;
        mMeasurementVersion = builder.mMeasurementVersion;
    }

    private DlTdoaRangingParams(Parcel in) {
        mSessionId = in.readInt();
        mDeviceAddress = Objects.requireNonNull(
                in.readParcelable(UwbAddress.class.getClassLoader(), UwbAddress.class));
        mSessionKeyInfo = in.createByteArray();
        mComplexChannel = in.readParcelable(UwbComplexChannel.class.getClassLoader(),
                UwbComplexChannel.class);
        mRangingIntervalMs = in.readInt();
        mSlotDuration = in.readInt();
        mSlotsPerRangingRound = in.readInt();
        mRangingRoundIndexes = in.createByteArray();
        mMeasurementVersion = in.readInt();
    }

    public static final @NonNull Creator<DlTdoaRangingParams> CREATOR =
            new Creator<DlTdoaRangingParams>() {
                @Override
                public DlTdoaRangingParams createFromParcel(Parcel in) {
                    return new DlTdoaRangingParams(in);
                }

                @Override
                public DlTdoaRangingParams[] newArray(int size) {
                    return new DlTdoaRangingParams[size];
                }
            };

    /**
     * Creates a {@link DlTdoaRangingParams} from a FiRa compliant configuration packet.
     *
     * @param config              The byte array containing the FiRa Specific OOB Vendor Specific
     *                            Element (VSE)
     *                            for BLE or WiFi.
     * @param rangingRoundIndexes The active ranging round indexes. If {@code null}, the indexes
     *                            may be extracted from the WiFi Time Sync sub-element in
     *                            {@code config} if present; otherwise,
     *                            the default value from {@link DlTdoaRangingParams.Builder} is
     *                            used.
     * @return A {@link DlTdoaRangingParams} instance.
     * @throws IllegalArgumentException if the configuration packet is malformed or missing
     *                                  mandatory fields.
     * @see <a href="https://groups.firaconsortium.org/wg/FPSG/document/5944">FiRa Specific OOB
     * Profile Advertisement Message</a> for the configuration packet format.
     */
    @NonNull
    public static DlTdoaRangingParams createFromFiraConfigPacket(
            @NonNull byte[] config, @Nullable byte[] rangingRoundIndexes) {
        Objects.requireNonNull(config);

        byte[] payload = DlTdoaPacketParser.extractPayload(config);
        if (payload.length == 0) {
            throw new IllegalArgumentException("No FiRa payload found.");
        }

        DlTdoaPacketParser.FiraConfigData data = DlTdoaPacketParser.parsePayload(payload);

        if (data.sessionId == null) {
            throw new IllegalArgumentException("Missing SESSION_ID parameter.");
        }

        Builder builder = new Builder(data.sessionId);

        if (data.channelNumber != null || data.preambleCodeIndex != null) {
            int channel = data.channelNumber == null
                    ? UwbConstants.DEFAULT_DLTDOA_CHANNEL_9 : data.channelNumber;
            int preambleIndex = data.preambleCodeIndex == null
                    ? UwbConstants.DEFAULT_DLTDOA_PREAMBLE_INDEX_10 : data.preambleCodeIndex;
            builder.setComplexChannel(new UwbComplexChannel.Builder()
                    .setChannel(channel)
                    .setPreambleIndex(preambleIndex)
                    .build());
        }

        if (data.deviceMacAddress != null) {
            builder.setDeviceAddress(UwbAddress.fromBytes(data.deviceMacAddress));
        }

        if (data.slotDuration != null) {
            builder.setSlotDuration(data.slotDuration / RSTU_PER_MS);
        }

        if (data.rangingDuration != null) {
            builder.setRangingIntervalMillis(data.rangingDuration.intValue());
        }

        if (data.slotsPerRangingRound != null) {
            builder.setSlotsPerRangingRound(data.slotsPerRangingRound);
        }

        if (data.vendorId != null && data.staticStsIv != null) {
            byte[] sessionKeyInfo = new byte[data.vendorId.length + data.staticStsIv.length];
            System.arraycopy(data.vendorId, 0, sessionKeyInfo, 0, data.vendorId.length);
            System.arraycopy(data.staticStsIv, 0, sessionKeyInfo, data.vendorId.length,
                    data.staticStsIv.length);
            builder.setSessionKeyInfo(sessionKeyInfo);
        }

        byte[] finalRangingRoundIndexes = null;
        if (rangingRoundIndexes != null) {
            finalRangingRoundIndexes = rangingRoundIndexes;
        } else if (!data.rangingRoundIndexes.isEmpty()) {
            finalRangingRoundIndexes = new byte[data.rangingRoundIndexes.size()];
            int i = 0;
            for (Byte b : data.rangingRoundIndexes) {
                finalRangingRoundIndexes[i++] = b;
            }
        }

        if (finalRangingRoundIndexes != null) {
            builder.setRangingRoundIndexes(finalRangingRoundIndexes);
        }

        if (data.measurementVersion != null) {
            builder.setMeasurementVersion(data.measurementVersion);
        }

        return builder.build();
    }

    /**
     * Creates a {@link DlTdoaRangingParams} from a list of FiRa compliant configuration packets.
     *
     * @param configPackets The list of byte arrays containing the FiRa Specific OOB Vendor Specific
     *                      Elements. The first packet is used for the main configuration, while
     *                      ranging round indexes are extracted from all packets that match the
     *                      session ID of the first packet.
     * @return A {@link DlTdoaRangingParams} instance.
     * @throws IllegalArgumentException if the configuration packets are empty, or the first packet
     *                                  is malformed.
     */
    @NonNull
    @FlaggedApi(Flags.FLAG_RANGING_STACK_UPDATES_26_Q_4)
    public static DlTdoaRangingParams createFromFiraConfigPackets(
            @NonNull List<byte[]> configPackets) {
        Objects.requireNonNull(configPackets);
        if (configPackets.isEmpty()) {
            throw new IllegalArgumentException("Config packets list cannot be empty.");
        }

        // Parse first packet to get session ID for filtering
        byte[] firstPacket = configPackets.get(0);
        byte[] firstPayload = DlTdoaPacketParser.extractPayload(firstPacket);
        if (firstPayload.length == 0) {
            throw new IllegalArgumentException("No FiRa payload found in first packet.");
        }
        DlTdoaPacketParser.FiraConfigData firstData = DlTdoaPacketParser.parsePayload(firstPayload);
        Integer sessionId = firstData.sessionId;
        if (sessionId == null) {
            throw new IllegalArgumentException("Missing SESSION_ID parameter in first packet.");
        }

        Set<Byte> rangingRoundIndexesSet = extractRangingRoundIndexes(configPackets, sessionId);
        byte[] rangingRoundIndexes = null;
        if (!rangingRoundIndexesSet.isEmpty()) {
            rangingRoundIndexes = new byte[rangingRoundIndexesSet.size()];
            int i = 0;
            for (Byte b : rangingRoundIndexesSet) {
                rangingRoundIndexes[i++] = b;
            }
        }
        return createFromFiraConfigPacket(firstPacket, rangingRoundIndexes);
    }

    /**
     * Gets the session ID.
     *
     * @return The session ID as an integer.
     */
    public int getSessionId() {
        return mSessionId;
    }

    /**
     * Gets the UWB address of the device.
     *
     * @return The {@link UwbAddress} of the device.
     */
    @NonNull
    public UwbAddress getDeviceAddress() {
        return mDeviceAddress;
    }

    /**
     * Gets the session key information.
     *
     * @return A byte array containing session key info, or null if not available.
     */
    @Nullable
    public byte[] getSessionKeyInfo() {
        return mSessionKeyInfo == null ? null : Arrays.copyOf(mSessionKeyInfo,
                mSessionKeyInfo.length);
    }

    /**
     * Gets the complex channel used for the session.
     *
     * @return A {@link UwbComplexChannel} object containing channel and preamble index.
     */
    @NonNull
    public UwbComplexChannel getComplexChannel() {
        return mComplexChannel;
    }

    /**
     * Gets the ranging interval in milliseconds.
     *
     * @return The ranging interval in milliseconds.
     */
    public int getRangingIntervalMillis() {
        return mRangingIntervalMs;
    }

    /**
     * Gets the slot duration.
     *
     * @return The slot duration.
     */
    @SlotDuration
    public int getSlotDuration() {
        return mSlotDuration;
    }

    /**
     * Gets the number of slots per ranging round.
     *
     * @return The number of slots per ranging round.
     */
    public int getSlotsPerRangingRound() {
        return mSlotsPerRangingRound;
    }

    /**
     * Gets the active ranging round indexes.
     */
    @Nullable
    public byte[] getRangingRoundIndexes() {
        return mRangingRoundIndexes == null ? null : Arrays.copyOf(mRangingRoundIndexes,
                mRangingRoundIndexes.length);
    }

    /**
     * Gets the measurement version.
     *
     * @return The measurement version.
     */
    @MeasurementVersion
    public int getMeasurementVersion() {
        return mMeasurementVersion;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mSessionId);
        dest.writeParcelable(mDeviceAddress, flags);
        dest.writeByteArray(mSessionKeyInfo);
        dest.writeParcelable(mComplexChannel, flags);
        dest.writeInt(mRangingIntervalMs);
        dest.writeInt(mSlotDuration);
        dest.writeInt(mSlotsPerRangingRound);
        dest.writeByteArray(mRangingRoundIndexes);
        dest.writeInt(mMeasurementVersion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DlTdoaRangingParams)) return false;
        DlTdoaRangingParams that = (DlTdoaRangingParams) o;
        return mSessionId == that.mSessionId &&
                mRangingIntervalMs == that.mRangingIntervalMs &&
                mSlotDuration == that.mSlotDuration &&
                mSlotsPerRangingRound == that.mSlotsPerRangingRound &&
                Objects.equals(mDeviceAddress, that.mDeviceAddress) &&
                Arrays.equals(mSessionKeyInfo, that.mSessionKeyInfo) &&
                Objects.equals(mComplexChannel, that.mComplexChannel) &&
                Arrays.equals(mRangingRoundIndexes, that.mRangingRoundIndexes) &&
                mMeasurementVersion == that.mMeasurementVersion;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(mSessionId, mDeviceAddress, mComplexChannel, mRangingIntervalMs,
                mSlotDuration, mSlotsPerRangingRound, mMeasurementVersion);
        result = 31 * result + Arrays.hashCode(mSessionKeyInfo);
        result = 31 * result + Arrays.hashCode(mRangingRoundIndexes);
        return result;
    }

    @Override
    public String toString() {
        return "DlTdoaRangingParams{"
                + "mSessionId=" + mSessionId +
                ", mDeviceAddress=" + mDeviceAddress +
                ", mSessionKeyInfo=" + Arrays.toString(mSessionKeyInfo) +
                ", mComplexChannel=" + mComplexChannel +
                ", mRangingIntervalMs=" + mRangingIntervalMs +
                ", mSlotDuration=" + mSlotDuration +
                ", mSlotsPerRangingRound=" + mSlotsPerRangingRound +
                ", mRangingRoundIndexes=" + Arrays.toString(mRangingRoundIndexes) +
                ", mMeasurementVersion=" + mMeasurementVersion +
                '}';
    }

    /**
     * Helper to get the ranging round indexes that were able to be parsed from FiRa OOB packets and
     * match the specified session ID.
     *
     * @param firaOobDtPackets list of FiRa OOB packets.
     * @param sessionId the session ID to filter by.
     * @return byte[] of ranging round indexes.
     *
     * @see <a href="https://groups.firaconsortium.org/wg/FPSG/document/5944">FiRa Specific OOB
     * Profile Advertisement Message</a> for the configuration packet format.
     */
    @NonNull
    @FlaggedApi(Flags.FLAG_RANGING_STACK_UPDATES_26_Q_4)
    public static byte[] getDlTdoaRangingRoundsFiraPacket(
            @NonNull List<byte[]> firaOobDtPackets, int sessionId) {
        Set<Byte> indexes = extractRangingRoundIndexes(firaOobDtPackets, sessionId);
        byte[] result = new byte[indexes.size()];
        int i = 0;
        for (Byte b : indexes) {
            result[i++] = b;
        }
        return result;
    }

    private static Set<Byte> extractRangingRoundIndexes(
            List<byte[]> firaOobDtPackets, int sessionId) {
        Set<Byte> allIndexes = new java.util.LinkedHashSet<>();
        for (byte[] packet : firaOobDtPackets) {
            byte[] payload = DlTdoaPacketParser.extractPayload(packet);
            if (payload.length > 0) {
                try {
                    DlTdoaPacketParser.FiraConfigData data =
                            DlTdoaPacketParser.parsePayload(payload);
                    if (data.sessionId.equals(sessionId)) {
                        allIndexes.addAll(data.rangingRoundIndexes);
                    }
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Failed to parse payload: " + e.getMessage());
                }
            }
        }
        return allIndexes;
    }

    /**
     * Builder for {@link DlTdoaRangingParams}.
     */
    public static final class Builder {
        private final int mSessionId;
        private UwbAddress mDeviceAddress = UwbAddress.createRandomShortAddress();
        private byte[] mSessionKeyInfo = UwbConstants.DEFAULT_DLTDOA_SESSION_KEY_INFO.clone();
        private UwbComplexChannel mComplexChannel =
                new UwbComplexChannel.Builder()
                        .setChannel(UwbConstants.DEFAULT_DLTDOA_CHANNEL_9)
                        .setPreambleIndex(UwbConstants.DEFAULT_DLTDOA_PREAMBLE_INDEX_10)
                        .build();
        private int mRangingIntervalMs = UwbConstants.DEFAULT_DLTDOA_RANGING_INTERVAL_250_MS;
        @SlotDuration
        private int mSlotDuration = UwbConstants.DEFAULT_DLTDOA_SLOT_DURATION_2_MS;
        private int mSlotsPerRangingRound = UwbConstants.DEFAULT_DLTDOA_SLOTS_PER_RANGING_ROUND_10;
        private byte[] mRangingRoundIndexes =
                UwbConstants.DEFAULT_DLTDOA_RANGING_ROUND_INDEXES.clone();
        @MeasurementVersion
        private int mMeasurementVersion = MEASUREMENT_VERSION_1;

        /**
         * Constructor for the Builder.
         *
         * @param sessionId The session ID.
         */
        public Builder(int sessionId) {
            mSessionId = sessionId;
        }

        /**
         * Sets the UWB address of the device.
         *
         * <p>If not set, a random short address is used as default.
         *
         * @param deviceAddress The UWB address of the device.
         * @return this {@link Builder} instance.
         */
        @NonNull
        public Builder setDeviceAddress(@NonNull UwbAddress deviceAddress) {
            Objects.requireNonNull(deviceAddress);
            mDeviceAddress = deviceAddress;
            return this;
        }

        /**
         * Sets the session key information.
         *
         * <p>If not set, {@code {0x00, 0x00, 0xA1, 0xB2, 0xC3, 0xD4, 0xE5, 0xF6}} is used as
         * default.
         *
         * @param sessionKeyInfo The session key information.
         * @return this {@link Builder} instance.
         */
        @NonNull
        public Builder setSessionKeyInfo(@NonNull byte[] sessionKeyInfo) {
            mSessionKeyInfo = Objects.requireNonNull(sessionKeyInfo);
            return this;
        }

        /**
         * Sets the complex channel.
         *
         * <p>If not set, a default channel with channel 9 and preamble index 10 is used.
         *
         * @param complexChannel The complex channel.
         * @return this {@link Builder} instance.
         */
        @NonNull
        public Builder setComplexChannel(@NonNull UwbComplexChannel complexChannel) {
            mComplexChannel = Objects.requireNonNull(complexChannel);
            return this;
        }

        /**
         * Sets the ranging interval in milliseconds.
         *
         * <p>If not set, 250ms is used as default.
         *
         * @param rangingIntervalMs The ranging interval in milliseconds.
         * @return this {@link Builder} instance.
         * @throws IllegalArgumentException if the ranging interval is not positive.
         */
        @NonNull
        public Builder setRangingIntervalMillis(@IntRange(from = 1) int rangingIntervalMs) {
            if (rangingIntervalMs <= 0) {
                throw new IllegalArgumentException("Ranging interval must be positive.");
            }
            mRangingIntervalMs = rangingIntervalMs;
            return this;
        }

        /**
         * Sets the slot duration.
         *
         * <p>If not set, {@link UwbRangingParams#DURATION_2_MS} is used as default.
         *
         * @param slotDuration The slot duration.
         * @return this {@link Builder} instance.
         */
        @NonNull
        public Builder setSlotDuration(@SlotDuration int slotDuration) {
            mSlotDuration = slotDuration;
            return this;
        }

        /**
         * Sets the number of slots per ranging round.
         *
         * <p>If not set, 10 is used as default.
         *
         * @param slotsPerRangingRound The number of slots per ranging round.
         * @return this {@link Builder} instance.
         * @throws IllegalArgumentException if the slots per ranging round is not positive.
         */
        @NonNull
        public Builder setSlotsPerRangingRound(@IntRange(from = 1) int slotsPerRangingRound) {
            if (slotsPerRangingRound <= 0) {
                throw new IllegalArgumentException("Slots per ranging round must be positive.");
            }
            mSlotsPerRangingRound = slotsPerRangingRound;
            return this;
        }

        /**
         * Sets the active ranging round indexes.
         *
         * <p>If not set, {@code {0}} is used as default.
         *
         * @param rangingRoundIndexes The active ranging round indexes.
         * @return this {@link Builder} instance.
         */
        @NonNull
        public Builder setRangingRoundIndexes(@NonNull byte[] rangingRoundIndexes) {
            mRangingRoundIndexes = Objects.requireNonNull(rangingRoundIndexes);
            return this;
        }

        /**
         * Sets the measurement version.
         *
         * @param measurementVersion The measurement version.
         * @return this {@link Builder} instance.
         */
        @NonNull
        public Builder setMeasurementVersion(@MeasurementVersion int measurementVersion) {
            mMeasurementVersion = measurementVersion;
            return this;
        }

        /**
         * Builds the {@link DlTdoaRangingParams} instance.
         *
         * @return The {@link DlTdoaRangingParams} instance.
         */
        @NonNull
        public DlTdoaRangingParams build() {
            return new DlTdoaRangingParams(this);
        }
    }
}
