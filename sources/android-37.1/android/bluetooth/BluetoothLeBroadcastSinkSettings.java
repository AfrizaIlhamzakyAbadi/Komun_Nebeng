/*
 * Copyright (C) 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.bluetooth;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresNoPermission;
import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.bluetooth.flags.Flags;

import java.util.Arrays;
import java.util.Objects;

/**
 * This class contains the broadcast group settings information for this Broadcast Group.
 *
 * <p>This class is used to configure the Broadcast Sink when it is first added.
 */
@SystemApi
@FlaggedApi(Flags.FLAG_LEAUDIO_BROADCAST_SINK_FEATURE)
public final class BluetoothLeBroadcastSinkSettings {

    private final InnerParcel mParcel;

    /**
     * Constructor for {@link BluetoothLeBroadcastSinkSettings}.
     *
     * @param broadcastId The Broadcast ID of the source.
     * @param broadcastCode The Broadcast Code to be used for decrypting the stream.
     * @param selectedChannels The bitfield representing the BISes wanted to be synchronized.
     */
    @Hide
    public BluetoothLeBroadcastSinkSettings(
            int broadcastId, byte[] broadcastCode, long selectedChannels) {
        mParcel = new InnerParcel(broadcastId, broadcastCode, selectedChannels);
    }

    /**
     * Private constructor to create {@link BluetoothLeBroadcastSinkSettings} from an InnerParcel.
     *
     * @param parcel The parcel to create the settings from.
     */
    private BluetoothLeBroadcastSinkSettings(InnerParcel parcel) {
        mParcel = parcel;
    }

    /**
     * Get the underlying parcel for this settings.
     *
     * @return The parcel for this settings.
     */
    @Hide
    @RequiresNoPermission
    public InnerParcel getParcel() {
        return mParcel;
    }

    /**
     * @return the {@link BluetoothLeBroadcastSinkSettings} associated with this parcel
     */
    @Hide
    @RequiresNoPermission
    public static @Nullable BluetoothLeBroadcastSinkSettings fromParcel(InnerParcel parcel) {
        if (parcel == null) {
            return null;
        }
        return new BluetoothLeBroadcastSinkSettings(parcel);
    }

    /**
     * Get the Broadcast ID of the source.
     *
     * @return The 3-byte long Broadcast_ID of the Broadcast Source.
     */
    @RequiresNoPermission
    public int getBroadcastId() {
        return mParcel.mBroadcastId;
    }

    /**
     * Get the Broadcast Code to be used for decrypting the stream.
     *
     * @return The Broadcast Code, or null if not set.
     */
    @RequiresNoPermission
    public @Nullable byte[] getBroadcastCode() {
        if (mParcel.mBroadcastCode == null) {
            return null;
        }
        return mParcel.mBroadcastCode.clone();
    }

    /**
     * Get the bitfield of whether a Broadcast Isochronous Stream (BIS) is selected to be
     * synchronized.
     *
     * <p>This is a bitfield where Bit 0-30 represents the selected state of BIS_index[1-31] in the
     * broadcast. Channel index stands for the BIS index in the Bluetooth Core Specification.
     *
     * <p>For example, if the long value is 0b111, then BIS_index 1, 2, and 3 are selected for
     * synchronization.
     *
     * @return The bitfield of whether a BIS is selected to be synchronized.
     */
    @RequiresNoPermission
    public long getSelectedChannels() {
        return mParcel.mSelectedChannels;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof BluetoothLeBroadcastSinkSettings)) return false;
        BluetoothLeBroadcastSinkSettings that = (BluetoothLeBroadcastSinkSettings) o;
        return mParcel.equals(that.mParcel);
    }

    @Override
    public int hashCode() {
        return mParcel.hashCode();
    }

    @Override
    public String toString() {
        return "BluetoothLeBroadcastSinkSettings{"
                + "broadcastId="
                + getBroadcastId()
                + ", broadcastCode="
                + Arrays.toString(getBroadcastCode())
                + ", selectedChannels="
                + getSelectedChannels()
                + '}';
    }

    @Hide
    public static final class InnerParcel implements Parcelable {
        private final int mBroadcastId;
        private final byte[] mBroadcastCode;
        private final long mSelectedChannels;

        InnerParcel(int broadcastId, byte[] broadcastCode, long selectedChannels) {
            checkBroadcastId(broadcastId);
            checkBroadcastCode(broadcastCode);
            checkSelectedChannels(selectedChannels);

            mBroadcastId = broadcastId;
            mBroadcastCode = broadcastCode;
            mSelectedChannels = selectedChannels;
        }

        private InnerParcel(Parcel in) {
            mBroadcastId = in.readInt();
            mBroadcastCode = in.createByteArray();
            mSelectedChannels = in.readLong();

            checkBroadcastId(mBroadcastId);
            checkBroadcastCode(mBroadcastCode);
            checkSelectedChannels(mSelectedChannels);
        }

        /**
         * Check if the broadcast ID is valid.
         *
         * @param broadcastId The broadcast ID to check.
         * @throws IllegalArgumentException if the broadcast ID is not a 3-byte value.
         */
        static void checkBroadcastId(int broadcastId) {
            if (broadcastId < 0 || broadcastId > 0xFFFFFF) {
                throw new IllegalArgumentException("broadcastId is not a 3-byte value");
            }
        }

        /**
         * Check if the broadcast code is valid.
         *
         * @param broadcastCode The broadcast code to check.
         * @throws IllegalArgumentException if the broadcast code is not at least 4 octets and
         *     should not exceed 16 octets.
         */
        static void checkBroadcastCode(byte[] broadcastCode) {
            if (broadcastCode != null
                    && ((broadcastCode.length > 16) || (broadcastCode.length < 4))) {
                throw new IllegalArgumentException("Invalid broadcast code length");
            }
        }

        /**
         * Check if the BIS selection states are valid.
         *
         * @param selectedChannels The bitfield representing the BISes wanted to be synchronized.
         * @throws IllegalArgumentException if the bitfield is 0, or if it contains invalid bits.
         *     Callers should select at least one BIS to synchronize.
         */
        static void checkSelectedChannels(long selectedChannels) {
            // The caller should select at least one BIS to synchronize.
            if (selectedChannels == 0) {
                throw new IllegalArgumentException("selectedChannels must not be 0");
            }

            // The caller should not select any BISes beyond the 31st BIS, i.e., the 32nd bit should
            // not be set.
            if ((selectedChannels & ~0x7FFFFFFFL) != 0) {
                throw new IllegalArgumentException("selectedChannels contains invalid bits");
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InnerParcel)) return false;
            InnerParcel that = (InnerParcel) o;
            return mBroadcastId == that.mBroadcastId
                    && Arrays.equals(mBroadcastCode, that.mBroadcastCode)
                    && mSelectedChannels == that.mSelectedChannels;
        }

        @Override
        public int hashCode() {
            return Objects.hash(mBroadcastId, Arrays.hashCode(mBroadcastCode), mSelectedChannels);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeInt(mBroadcastId);
            dest.writeByteArray(mBroadcastCode);
            dest.writeLong(mSelectedChannels);
        }

        public static final @NonNull Creator<InnerParcel> CREATOR =
                new Creator<>() {
                    @Override
                    public InnerParcel createFromParcel(Parcel in) {
                        return new InnerParcel(in);
                    }

                    @Override
                    public InnerParcel[] newArray(int size) {
                        return new InnerParcel[size];
                    }
                };
    }

    private static final int INVALID_BROADCAST_ID = -1;
    private static final long INVALID_SELECTED_CHANNELS = -1L;

    /** Builder for {@link BluetoothLeBroadcastSinkSettings}. */
    public static final class Builder {
        private int mBroadcastId = INVALID_BROADCAST_ID;
        private byte[] mBroadcastCode = null;
        private long mSelectedChannels = INVALID_SELECTED_CHANNELS;

        /** Create an empty builder. */
        @RequiresNoPermission
        public Builder() {}

        /**
         * Create a builder with copies of information from original object.
         *
         * @param original original object
         */
        @RequiresNoPermission
        public Builder(@NonNull BluetoothLeBroadcastSinkSettings original) {
            mBroadcastId = original.getBroadcastId();
            mBroadcastCode = original.getBroadcastCode();
            mSelectedChannels = original.getSelectedChannels();
        }

        /**
         * Set the Broadcast ID. This must be a 3-byte value ranging from 0x000000 to 0xFFFFFF.
         *
         * @param broadcastId The Broadcast ID.
         * @return this builder
         * @throws IllegalArgumentException if broadcastId is not a 3-byte value
         */
        @RequiresNoPermission
        public @NonNull Builder setBroadcastId(int broadcastId) {
            InnerParcel.checkBroadcastId(broadcastId);
            mBroadcastId = broadcastId;
            return this;
        }

        /**
         * Set the Broadcast Code currently set for this broadcast group.
         *
         * <p>Only needed when encryption is enabled As defined in Volume 3, Part C, Section 3.2.6
         * of Bluetooth Core Specification, Version 5.3, Broadcast Code is used to encrypt a
         * broadcast audio stream. It must be a UTF-8 string that has at least 4 octets and should
         * not exceed 16 octets.
         *
         * @param broadcastCode Broadcast Code for this broadcast group, null if code is not
         *     required for non-encrypted broadcast
         * @throws IllegalArgumentException if broadcastCode is non-null and its length is less than
         *     4 characters or greater than 16 characters
         * @return this builder
         */
        @RequiresNoPermission
        public @NonNull Builder setBroadcastCode(@Nullable byte[] broadcastCode) {
            InnerParcel.checkBroadcastCode(broadcastCode);
            mBroadcastCode =
                    (broadcastCode == null)
                            ? null
                            : Arrays.copyOf(broadcastCode, broadcastCode.length);
            return this;
        }

        /**
         * Set the bitfield of whether a Broadcast Isochronous Stream (BIS) is selected to be
         * synchronized.
         *
         * <p>This is a bitfield where Bit 0-30 represents the selected state of BIS_index[1-31] in
         * the broadcast. Channel index stands for the BIS index in the Bluetooth Core
         * Specification.
         *
         * <p>For example, if the long value is 0b111, then BIS_index 1, 2, and 3 are selected for
         * synchronization.
         *
         * <p>The caller should select at least one BIS to synchronize. 0xFFFFFFFFL, which means no
         * preference in Broadcast Audio Scan Service (BASS), is not a valid value for broadcast
         * sink here.
         *
         * @param selectedChannels The bitfield representing the BISes wanted to be synchronized.
         * @throws IllegalArgumentException if selectedChannels is 0, or if it contains invalid bits
         * @return this builder
         */
        @RequiresNoPermission
        public @NonNull Builder setSelectedChannels(long selectedChannels) {
            InnerParcel.checkSelectedChannels(selectedChannels);
            mSelectedChannels = selectedChannels;
            return this;
        }

        /**
         * Build {@link BluetoothLeBroadcastSinkSettings}.
         *
         * @return {@link BluetoothLeBroadcastSinkSettings}
         * @throws IllegalStateException if any mandatory field is not set
         * @throws IllegalArgumentException if any field is invalid
         */
        @RequiresNoPermission
        public @NonNull BluetoothLeBroadcastSinkSettings build() {
            if (mBroadcastId == INVALID_BROADCAST_ID) {
                throw new IllegalStateException("broadcastId is not set");
            }
            if (mSelectedChannels == INVALID_SELECTED_CHANNELS) {
                throw new IllegalStateException("selectedChannels is not set");
            }
            return new BluetoothLeBroadcastSinkSettings(
                    mBroadcastId, mBroadcastCode, mSelectedChannels);
        }
    }
}
