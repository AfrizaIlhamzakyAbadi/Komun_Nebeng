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

package android.ranging.uwb;

import static android.ranging.uwb.UwbConstants.DEFAULT_DLTDOA_CHANNEL_9;
import static android.ranging.uwb.UwbConstants.DEFAULT_DLTDOA_RANGING_INTERVAL_250_MS;
import static android.ranging.uwb.UwbConstants.DLTDOA_CHANNEL_5;
import static android.ranging.uwb.UwbConstants.DLTDOA_RANGING_INTERVAL_100_MS;
import static android.ranging.uwb.UwbConstants.DLTDOA_RANGING_INTERVAL_200_MS;
import static android.ranging.uwb.UwbConstants.SLOT_DURATION_1MS_RSTU;
import static android.ranging.uwb.UwbConstants.SLOT_DURATION_2MS_RSTU;

import android.annotation.Hide;
import android.annotation.NonNull;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class to parse FiRa compliant OOB packets for DL-TDoA.
 */
@Hide
public final class DlTdoaPacketParser {

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

    public static class FiraConfigData {
        public Integer configId;
        public Integer sessionId;
        public Short channelNumber;
        public byte[] deviceMacAddress;
        public Integer slotDuration;
        public Long rangingDuration;
        public Short preambleCodeIndex;
        public Short slotsPerRangingRound;
        public byte[] vendorId;
        public byte[] staticStsIv;
        public Set<Byte> rangingRoundIndexes = new LinkedHashSet<>();
        public Integer measurementVersion;
    }

    /**
     * Extracts payload from a single packet (BLE or WiFi).
     */
    @NonNull
    public static byte[] extractPayload(@NonNull byte[] config) {
        Objects.requireNonNull(config);
        ByteArrayOutputStream payloadStream = new ByteArrayOutputStream();
        int adOffset = 0;
        boolean isBle = false;
        boolean isWifi = false;

        if (config.length >= FIRA_OOB_BLE_VSE_MINIMUM_TOTAL_LENGTH
                && (config[1] & 0xFF) == FIRA_OOB_BLE_DATA_TYPE_UUID_16BITS) {
            int u0 = config[2] & 0xFF;
            int u1 = config[3] & 0xFF;
            if ((u0 == FIRA_OOB_BLE_CP_UUID_0 && u1 == FIRA_OOB_BLE_CP_UUID_1)
                    || (u0 == FIRA_OOB_BLE_CS_UUID_0 && u1 == FIRA_OOB_BLE_CS_UUID_1)) {
                isBle = true;
            }
        }

        if (!isBle && config.length >= FIRA_OOB_WIFI_VSE_MINIMUM_TOTAL_LENGTH
                && (config[0] & 0xFF) == FIRA_OOB_WIFI_VSE_ID
                && (config[2] & 0xFF) == FIRA_OOB_WIFI_OUI_0
                && (config[3] & 0xFF) == FIRA_OOB_WIFI_OUI_1
                && (config[4] & 0xFF) == FIRA_OOB_WIFI_OUI_2) {
            isWifi = true;
        }

        if (isBle) {
            while (adOffset + 3 < config.length) {
                int adLength = config[adOffset] & 0xFF;
                if (adLength < 3 || adOffset + 1 + adLength > config.length) break;
                int adType = config[adOffset + 1] & 0xFF;
                if (adType == FIRA_OOB_BLE_DATA_TYPE_UUID_16BITS) {
                    int u0 = config[adOffset + 2] & 0xFF;
                    int u1 = config[adOffset + 3] & 0xFF;
                    if ((u0 == FIRA_OOB_BLE_CP_UUID_0 && u1 == FIRA_OOB_BLE_CP_UUID_1)
                            || (u0 == FIRA_OOB_BLE_CS_UUID_0 && u1 == FIRA_OOB_BLE_CS_UUID_1)) {
                        int payloadStart = adOffset + 4;
                        int payloadLen = adLength - 3;
                        if (payloadLen > 0 && (config[payloadStart] & 0xF0) == 0xF0) {
                            // Skip fragmentation indication
                            payloadStart++;
                            payloadLen--;
                        }
                        payloadStream.write(config, payloadStart, payloadLen);
                    }
                }
                adOffset += 1 + adLength;
            }
        } else if (isWifi) {
            while (adOffset + 4 < config.length) {
                if ((config[adOffset] & 0xFF) != FIRA_OOB_WIFI_VSE_ID) break;
                int adLength = config[adOffset + 1] & 0xFF;
                if (adLength < 3 || adOffset + 2 + adLength > config.length) break;
                int o0 = config[adOffset + 2] & 0xFF;
                int o1 = config[adOffset + 3] & 0xFF;
                int o2 = config[adOffset + 4] & 0xFF;
                if (o0 == FIRA_OOB_WIFI_OUI_0 && o1 == FIRA_OOB_WIFI_OUI_1
                        && o2 == FIRA_OOB_WIFI_OUI_2) {
                    payloadStream.write(config, adOffset + 5, adLength - 3);
                }
                adOffset += 2 + adLength;
            }
        }
        return payloadStream.toByteArray();
    }

    /**
     * Stitches fragmented payloads based on fragment indication.
     */
    @NonNull
    public static byte[] stitchFragments(@NonNull Set<byte[]> payloads) {
        Objects.requireNonNull(payloads);
        if (payloads.isEmpty()) {
            return new byte[0];
        }

        List<byte[]> sortedPayloads = new ArrayList<>(payloads);

        // Sort based on assumption that first byte is 0xF0 | seq
        sortedPayloads.sort(Comparator.comparingInt(a -> {
            if (a.length > 0 && (a[0] & 0xF0) == 0xF0) {
                return a[0] & 0x0F;
            }
            return 0;
        }));

        ByteArrayOutputStream stitched = new ByteArrayOutputStream();
        for (byte[] p : sortedPayloads) {
            if (p.length > 0) {
                int start = 0;
                if ((p[0] & 0xF0) == 0xF0) {
                    start = 1; // Skip fragment indication
                }
                stitched.write(p, start, p.length - start);
            }
        }
        return stitched.toByteArray();
    }

    /**
     * Parses the stitched payload.
     */
    @NonNull
    public static FiraConfigData parsePayload(@NonNull byte[] payload) {
        Objects.requireNonNull(payload);
        FiraConfigData data = new FiraConfigData();

        int offset = 0;
        while (offset < payload.length) {
            int header = payload[offset] & 0xFF;
            int type = (header & 0xF0) >> 4;
            int length = header & 0x0F;
            int subElementDataOffset = offset + 1;

            if (length == 0x0F) {
                int lengthExtensionOffset = subElementDataOffset;
                if (lengthExtensionOffset >= payload.length) {
                    throw new IllegalArgumentException("Invalid sub-element length extension.");
                }
                int extendedLength = 0x0F;
                int lengthExtension = payload[lengthExtensionOffset++] & 0xFF;
                while (lengthExtension == 0xFF && lengthExtensionOffset < payload.length) {
                    extendedLength += lengthExtension;
                    lengthExtension = payload[lengthExtensionOffset++] & 0xFF;
                }
                extendedLength += lengthExtension;
                length = extendedLength;
                subElementDataOffset = lengthExtensionOffset;
            }

            if ((subElementDataOffset + length) > payload.length) {
                throw new IllegalArgumentException("Not enough bytes for Sub-Element content.");
            }

            if (type == FIRA_SUB_ELEMENT_TYPE_UWB_CONFIG) {
                if (length < FIRA_OOB_UWB_CONFIGURATION_HEADER_LENGTH) {
                    throw new IllegalArgumentException(
                            "Invalid UWB Configuration Sub-Element length.");
                }

                if ((payload[subElementDataOffset] & 0xFF)
                        != FIRA_UWB_UNTRACKED_NAVIGATION_PROFILE_ID) {
                    throw new IllegalArgumentException(
                            "Invalid UWB Configuration Sub-Element header.");
                }

                int configId = payload[subElementDataOffset + 1] & 0xFF;
                data.configId = configId;
                switch (configId) {
                    case 0 -> {
                        data.channelNumber = (short) DEFAULT_DLTDOA_CHANNEL_9;
                        data.slotDuration = SLOT_DURATION_2MS_RSTU;
                        data.rangingDuration = (long) DEFAULT_DLTDOA_RANGING_INTERVAL_250_MS;
                    }
                    case 1 -> {
                        data.channelNumber = (short) DEFAULT_DLTDOA_CHANNEL_9;
                        data.slotDuration = SLOT_DURATION_1MS_RSTU;
                        data.rangingDuration = (long) DLTDOA_RANGING_INTERVAL_200_MS;
                    }
                    case 2 -> {
                        data.channelNumber = (short) DLTDOA_CHANNEL_5;
                        data.slotDuration = SLOT_DURATION_1MS_RSTU;
                        data.rangingDuration = (long) DLTDOA_RANGING_INTERVAL_200_MS;
                    }
                    case 3, 5, 7 -> {
                        data.channelNumber = (short) DEFAULT_DLTDOA_CHANNEL_9;
                        data.slotDuration = SLOT_DURATION_1MS_RSTU;
                        data.rangingDuration = (long) DLTDOA_RANGING_INTERVAL_100_MS;
                    }
                    case 4, 6, 8 -> {
                        data.channelNumber = (short) DLTDOA_CHANNEL_5;
                        data.slotDuration = SLOT_DURATION_1MS_RSTU;
                        data.rangingDuration = (long) DLTDOA_RANGING_INTERVAL_100_MS;
                    }
                }

                int tagOffset = subElementDataOffset + FIRA_OOB_UWB_CONFIGURATION_HEADER_LENGTH;
                int subElementEnd = subElementDataOffset + length;
                while (tagOffset + 1 < subElementEnd) {
                    int tag = payload[tagOffset++] & 0xFF;
                    int tagLength = payload[tagOffset++] & 0xFF;

                    if (tagOffset + tagLength > subElementEnd) {
                        throw new IllegalArgumentException(
                                "Not enough bytes for UWB Configuration Parameter List "
                                        + "content.");
                    }

                    ByteBuffer buffer = ByteBuffer.wrap(payload, tagOffset, tagLength).order(
                            ByteOrder.LITTLE_ENDIAN);

                    switch (tag) {
                        case TAG_CHANNEL_NUMBER -> {
                            if (tagLength != 1) {
                                throw new IllegalArgumentException(
                                        "Invalid length for CHANNEL_NUMBER.");
                            }
                            data.channelNumber = (short) (buffer.get() & 0xFF);
                        }
                        case TAG_DEVICE_MAC_ADDRESS -> {
                            if (tagLength != 2 && tagLength != 8) {
                                throw new IllegalArgumentException(
                                        "Invalid length for DEVICE_MAC_ADDRESS.");
                            }
                            data.deviceMacAddress = new byte[tagLength];
                            buffer.get(data.deviceMacAddress);
                        }
                        case TAG_SLOT_DURATION -> {
                            if (tagLength != 2) {
                                throw new IllegalArgumentException(
                                        "Invalid length for SLOT_DURATION.");
                            }
                            data.slotDuration = buffer.getShort() & 0xFFFF;
                        }
                        case TAG_RANGING_DURATION -> {
                            if (tagLength != 4) {
                                throw new IllegalArgumentException(
                                        "Invalid length for RANGING_DURATION.");
                            }
                            data.rangingDuration = buffer.getInt() & 0xFFFFFFFFL;
                        }
                        case TAG_PREAMBLE_CODE_INDEX -> {
                            if (tagLength != 1) {
                                throw new IllegalArgumentException(
                                        "Invalid length for PREAMBLE_CODE_INDEX.");
                            }
                            data.preambleCodeIndex = (short) (buffer.get() & 0xFF);
                        }
                        case TAG_SLOTS_PER_RR -> {
                            if (tagLength != 1) {
                                throw new IllegalArgumentException(
                                        "Invalid length for SLOTS_PER_RR.");
                            }
                            data.slotsPerRangingRound = (short) (buffer.get() & 0xFF);
                        }
                        case TAG_VENDOR_ID -> {
                            if (tagLength != 2) {
                                throw new IllegalArgumentException(
                                        "Invalid length for VENDOR_ID.");
                            }
                            data.vendorId = new byte[tagLength];
                            buffer.get(data.vendorId);
                        }
                        case TAG_STATIC_STS_IV -> {
                            if (tagLength != 6) {
                                throw new IllegalArgumentException(
                                        "Invalid length for STATIC_STS_IV.");
                            }
                            data.staticStsIv = new byte[tagLength];
                            buffer.get(data.staticStsIv);
                        }
                        case TAG_DL_TDOA_MEASUREMENT_NTF_V2 -> {
                            if (tagLength != 1) {
                                throw new IllegalArgumentException(
                                        "Invalid length for DL_TDOA_MEASUREMENT_NTF_V2.");
                            }
                            int version = buffer.get() & 0xFF;
                            data.measurementVersion = switch (version) {
                                case 0x00 -> DlTdoaRangingParams.MEASUREMENT_VERSION_1;
                                case 0x01 -> DlTdoaRangingParams.MEASUREMENT_VERSION_2;
                                default -> throw new IllegalArgumentException(
                                        "Invalid measurement version.");
                            };
                        }
                        case TAG_INITIATOR_DT_ANCHOR_ROUND_INDEX -> {
                            for (int i = 0; i < tagLength; i++) {
                                data.rangingRoundIndexes.add(buffer.get());
                            }
                        }
                        case TAG_SESSION_ID -> {
                            if (tagLength != 4) {
                                throw new IllegalArgumentException(
                                        "Invalid length for SESSION_ID.");
                            }
                            data.sessionId = buffer.getInt();
                        }
                        default -> { /* Skip unknown tags */ }
                    }
                    tagOffset += tagLength;
                }
            } else if (type == FIRA_SUB_ELEMENT_TYPE_WIFI_TIME_SYNC) {
                if (length >= 2) {
                    int pos = subElementDataOffset;
                    pos++; // Skip Profile ID
                    int modeAndControl = payload[pos++] & 0xFF;
                    int addressMode = (modeAndControl & 0xF0) >> 4;
                    int infoControl = modeAndControl & 0x0F;

                    int addrLen = (addressMode == 0x0) ? 2 : 8;
                    if (pos + addrLen <= subElementDataOffset + length) {
                        pos += addrLen; // Skip DT-Anchor Address

                        if ((infoControl & 0x01) != 0) {
                            pos += 6; // Skip Time Offset and Uncertainty
                        }

                        if (pos + 1 <= subElementDataOffset + length) {
                            data.rangingRoundIndexes.add(payload[pos++]);
                        }

                        if ((infoControl & 0x02) != 0) {
                            if (pos + 1 <= subElementDataOffset + length) {
                                int count = payload[pos++] & 0xFF;
                                for (int i = 0; i < count; i++) {
                                    if (pos + addrLen + 1 <= subElementDataOffset + length) {
                                        pos += addrLen; // Skip Address
                                        data.rangingRoundIndexes.add(payload[pos++]);
                                    } else {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
            offset = subElementDataOffset + length;
        }
        return data;
    }
}
