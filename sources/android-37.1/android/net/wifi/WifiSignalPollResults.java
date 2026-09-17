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

package android.net.wifi;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.util.SparseArray;

import com.android.wifi.flags.Flags;

import java.util.Objects;

/**
 * A class representing signal poll results collected over multiple links.
 */
@SystemApi
@FlaggedApi(Flags.FLAG_WIFICOND_REPLACEMENT_APIS)
public final class WifiSignalPollResults implements Parcelable {
    private static final String TAG = "WifiSignalPollResults";

    private static final int MIN_RSSI = -127;
    private static final int MAX_ENTRIES = 16;

    @Hide
    public WifiSignalPollResults() {
        mEntries = new SparseArray<>(MAX_ENTRIES);
        mBestLinkId = 0;
        mDefault = new SignalPollResult(0, MIN_RSSI, 0, 0, 0);
    }

    private static class SignalPollResult {
        SignalPollResult(int linkId, int currentRssiDbm, int txBitrateMbps,
                int rxBitrateMbps, int frequencyMHz) {
            this.linkId = linkId;
            this.currentRssiDbm = currentRssiDbm;
            this.txBitrateMbps = txBitrateMbps;
            this.rxBitrateMbps = rxBitrateMbps;
            this.frequencyMHz = frequencyMHz;
        }

        /**
         * Link identifier.
         */
        public final int linkId;

        /**
         * RSSI value in dBM.
         */
        public final int currentRssiDbm;

        /**
         * Last transmitted bit rate in Mbps.
         */
        public final int txBitrateMbps;

        /**
         * Last received packet bit rate in Mbps.
         */
        public final int rxBitrateMbps;

        /**
         * Frequency in MHz.
         */
        public final int frequencyMHz;

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof SignalPollResult)) return false;
            SignalPollResult other = (SignalPollResult) obj;
            return linkId == other.linkId
                    && currentRssiDbm == other.currentRssiDbm
                    && txBitrateMbps == other.txBitrateMbps
                    && rxBitrateMbps == other.rxBitrateMbps
                    && frequencyMHz == other.frequencyMHz;
        }

        @Override
        public int hashCode() {
            return Objects.hash(linkId, currentRssiDbm, txBitrateMbps, rxBitrateMbps, frequencyMHz);
        }
    }

    /* Signal poll result entries. Maps linkId to SignalPollResult. */
    private SparseArray<SignalPollResult> mEntries;
    /* Link id of the best link. */
    private int mBestLinkId;
    /* Default entry with default values. */
    private SignalPollResult mDefault;

    private WifiSignalPollResults(@NonNull Parcel in) {
        mEntries = new SparseArray<>(MAX_ENTRIES);
        mDefault = new SignalPollResult(0, MIN_RSSI, 0, 0, 0);
        mBestLinkId = in.readInt();

        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            int linkId = in.readInt();
            int currentRssiDbm = in.readInt();
            int txBitrateMbps = in.readInt();
            int rxBitrateMbps = in.readInt();
            int frequencyMHz = in.readInt();
            mEntries.put(linkId, new SignalPollResult(linkId, currentRssiDbm,
                    txBitrateMbps, rxBitrateMbps, frequencyMHz));
        }
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mBestLinkId);
        dest.writeInt(mEntries.size());
        for (int i = 0; i < mEntries.size(); i++) {
            SignalPollResult result = mEntries.valueAt(i);
            dest.writeInt(result.linkId);
            dest.writeInt(result.currentRssiDbm);
            dest.writeInt(result.txBitrateMbps);
            dest.writeInt(result.rxBitrateMbps);
            dest.writeInt(result.frequencyMHz);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public static final Creator<WifiSignalPollResults> CREATOR =
            new Creator<WifiSignalPollResults>() {
                @Override
                public WifiSignalPollResults createFromParcel(@NonNull Parcel in) {
                    return new WifiSignalPollResults(in);
                }

                @Override
                public WifiSignalPollResults[] newArray(int size) {
                    return new WifiSignalPollResults[size];
                }
            };

    /**
     * Add a new entry of signal poll result.
     *
     * @param linkId         Identifier of the link (0 - 14).
     * @param currentRssiDbm Current RSSI in dBm.
     * @param txBitRateMbps  Transmit bit rate of the link.
     * @param rxBitRateMbps  Receive bit rate of the link.
     * @param frequencyMHz   Frequency of the link.
     */
    @Hide
    public void addEntry(int linkId, int currentRssiDbm, int txBitRateMbps, int rxBitRateMbps,
            int frequencyMHz) {
        // Prevent exceeding MAX_ENTRIES, but allow updates to existing linkIds
        if (mEntries.size() >= MAX_ENTRIES && !mEntries.contains(linkId)) {
            Log.e(TAG, "addEntry: failed, reached maximum entries " + MAX_ENTRIES);
            return;
        }
        // Update the best link id.
        if (mEntries.size() == 0 || currentRssiDbm > mEntries.get(
                mBestLinkId).currentRssiDbm) {
            mBestLinkId = linkId;
        }
        // Add a new Entry.
        mEntries.put(linkId,
                new SignalPollResult(linkId, currentRssiDbm, txBitRateMbps, rxBitRateMbps,
                        frequencyMHz));
    }

    /**
     * Get current RSSI. In case of multi links, return the maximum RSSI (best link).
     *
     * @return rssi in dBm or {@link WifiSignalPollResults#MIN_RSSI} if no poll results.
     */
    @SystemApi
    public int getRssiDbm() {
        return mEntries.get(mBestLinkId, mDefault).currentRssiDbm;
    }

    /**
     * Get current RSSI of the link.
     *
     * @param linkId Identifier of the link.
     * @return rssi in dBm or {@link WifiSignalPollResults#MIN_RSSI} if link is not present.
     */
    @SystemApi
    public int getRssiDbm(int linkId) {
        return mEntries.get(linkId, mDefault).currentRssiDbm;
    }

    /**
     * Get transmit link speed in Mbps. In case of multi links, return the rate of the best link.
     *
     * @return tx link speed in Mbps or 0 if no poll results.
     */
    @SystemApi
    public int getTxLinkSpeedMbps() {
        return mEntries.get(mBestLinkId, mDefault).txBitrateMbps;
    }

    /**
     * Get transmit link speed in Mbps of the link.
     *
     * @param linkId Identifier of the link.
     * @return tx link speed in Mbps or 0 if link not present.
     */
    @SystemApi
    public int getTxLinkSpeedMbps(int linkId) {
        return mEntries.get(linkId, mDefault).txBitrateMbps;
    }

    /**
     * Get receive link speed in Mbps. In case of multi links, return the rate of the best link.
     *
     * @return rx link speed in Mbps or 0 if no poll results.
     */
    @SystemApi
    public int getRxLinkSpeedMbps() {
        return mEntries.get(mBestLinkId, mDefault).rxBitrateMbps;
    }

    /**
     * Get receive link speed in Mbps of the link.
     *
     * @param linkId Identifier of the link.
     * @return rx link speed in Mbps or 0 if link not present.
     */
    @SystemApi
    public int getRxLinkSpeedMbps(int linkId) {
        return mEntries.get(linkId, mDefault).rxBitrateMbps;
    }

    /**
     * Get frequency. In case of multi links, return frequency of the best link.
     *
     * @return frequency in MHz or 0 if no poll results.
     */
    @SystemApi
    public int getFrequencyMhz() {
        return mEntries.get(mBestLinkId, mDefault).frequencyMHz;
    }

    /**
     * Get frequency of the link.
     *
     * @param linkId Identifier of the link.
     * @return frequency in MHz or 0 if link is not present.
     */
    @SystemApi
    public int getFrequencyMhz(int linkId) {
        return mEntries.get(linkId, mDefault).frequencyMHz;
    }

    /**
     * Return whether the poll results available for the specific link.
     *
     * @param linkId Identifier of the link.
     * @return true if available, otherwise false
     */
    @SystemApi
    public boolean isAvailable(int linkId) {
        return mEntries.contains(linkId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof WifiSignalPollResults)) return false;
        WifiSignalPollResults other = (WifiSignalPollResults) obj;
        if (mBestLinkId != other.mBestLinkId) return false;
        if (mEntries.size() != other.mEntries.size()) return false;

        for (int i = 0; i < mEntries.size(); i++) {
            if (mEntries.keyAt(i) != other.mEntries.keyAt(i)) return false;
            if (!Objects.equals(mEntries.valueAt(i), other.mEntries.valueAt(i))) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = Objects.hash(mBestLinkId);
        // SparseArray does not override hashCode, so we calculate it from contents
        for (int i = 0; i < mEntries.size(); i++) {
            hash = 31 * hash + mEntries.keyAt(i);
            hash = 31 * hash + Objects.hashCode(mEntries.valueAt(i));
        }
        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("WifiSignalPollResults { ");
        sb.append("bestLinkId=").append(mBestLinkId).append(", ");
        sb.append("entries=[ ");
        for (int i = 0; i < mEntries.size(); i++) {
            SignalPollResult result = mEntries.valueAt(i);
            sb.append("linkId=").append(result.linkId).append(", ")
                    .append("rssi=").append(result.currentRssiDbm).append(", ")
                    .append("txBitrate=").append(result.txBitrateMbps).append(", ")
                    .append("rxBitrate=").append(result.rxBitrateMbps).append(", ")
                    .append("freq=").append(result.frequencyMHz).append(" | ");
        }
        sb.append("] }");
        return sb.toString();
    }
}
