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
package android.companion;

import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * Represents a request for out-of-band pairing. Out-of-band pairing refers to using an alternative
 * channel or method to exchange pairing information or facilitate the secure establishment of a
 * connection, rather than relying solely on the primary communication channel. This class currently
 * supports requesting out-of-band pairing via Bluetooth.
 */
@FlaggedApi(Flags.FLAG_TRUST_PAIRING)
public final class OutOfBandPairingRequest implements Parcelable {

    private final boolean mBluetooth;

    private OutOfBandPairingRequest(Builder builder) {
        mBluetooth = builder.mBluetooth;
    }

    private OutOfBandPairingRequest(Parcel in) {
        mBluetooth = in.readBoolean();
    }

    /**
     * @return true if Bluetooth pairing is requested.
     */
    public boolean isBluetoothPairingRequested() {
        return mBluetooth;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeBoolean(mBluetooth);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OutOfBandPairingRequest that = (OutOfBandPairingRequest) o;
        return mBluetooth == that.mBluetooth;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mBluetooth);
    }

    @Override
    public String toString() {
        return "OutOfBandPairingRequest{"
                + "mBluetooth=" + mBluetooth
                + '}';
    }

    public static final @NonNull Creator<OutOfBandPairingRequest> CREATOR = new Creator<>() {
        @Override
        public OutOfBandPairingRequest createFromParcel(Parcel in) {
            return new OutOfBandPairingRequest(in);
        }

        @Override
        public OutOfBandPairingRequest[] newArray(int size) {
            return new OutOfBandPairingRequest[size];
        }
    };

    /**
     * A builder for creating {@link OutOfBandPairingRequest}.
     */
    public static final class Builder {
        private boolean mBluetooth = false;

        public Builder() {}

        /**
         * Sets whether Bluetooth out-of-band pairing is requested.
         */
        @NonNull
        @SuppressLint("MissingGetterMatchingBuilder")
        public Builder setBluetooth(boolean bluetooth) {
            mBluetooth = bluetooth;
            return this;
        }

        /**
         * Builds the {@link OutOfBandPairingRequest} object.
         */
        @NonNull
        public OutOfBandPairingRequest build() {
            return new OutOfBandPairingRequest(this);
        }
    }
}
