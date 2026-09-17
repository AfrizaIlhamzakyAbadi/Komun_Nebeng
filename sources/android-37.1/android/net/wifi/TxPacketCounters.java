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

import static com.android.wifi.flags.Flags.FLAG_WIFICOND_REPLACEMENT_APIS;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * Class representing packet transmission counters.
 */
@SystemApi
@FlaggedApi(FLAG_WIFICOND_REPLACEMENT_APIS)
public final class TxPacketCounters implements Parcelable {
    private final int mTxPacketSucceeded;
    private final int mTxPacketFailed;

    @Hide
    public TxPacketCounters(int txPacketSucceeded, int txPacketFailed) {
        mTxPacketSucceeded = txPacketSucceeded;
        mTxPacketFailed = txPacketFailed;
    }

    /**
     * Get the number of successfully transmitted packets.
     */
    @SystemApi
    @FlaggedApi(FLAG_WIFICOND_REPLACEMENT_APIS)
    public int getTxPacketSucceeded() {
        return mTxPacketSucceeded;
    }

    /**
     * Get the number of failed packet transmissions.
     */
    @SystemApi
    @FlaggedApi(FLAG_WIFICOND_REPLACEMENT_APIS)
    public int getTxPacketFailed() {
        return mTxPacketFailed;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mTxPacketSucceeded);
        dest.writeInt(mTxPacketFailed);
    }

    @NonNull
    public static final Creator<TxPacketCounters> CREATOR = new Creator<>() {
        @Override
        public TxPacketCounters createFromParcel(Parcel in) {
            return new TxPacketCounters(in.readInt(), in.readInt());
        }

        @Override
        public TxPacketCounters[] newArray(int size) {
            return new TxPacketCounters[size];
        }
    };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TxPacketCounters)) return false;
        TxPacketCounters that = (TxPacketCounters) o;
        return mTxPacketSucceeded == that.mTxPacketSucceeded
                && mTxPacketFailed == that.mTxPacketFailed;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mTxPacketSucceeded, mTxPacketFailed);
    }

    @Override
    public String toString() {
        return "TxPacketCounters{"
                + "txPacketSucceeded=" + mTxPacketSucceeded
                + ", txPacketFailed=" + mTxPacketFailed
                + '}';
    }
}
