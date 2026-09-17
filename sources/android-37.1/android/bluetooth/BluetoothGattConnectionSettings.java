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

package android.bluetooth;

import static android.bluetooth.BluetoothDevice.Transport;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntRange;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresNoPermission;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.bluetooth.flags.Flags;

/**
 * Defines parameters for creating BluetoothGatt connection.
 *
 * <p>Used with {@link BluetoothDevice#connectGatt} to create a Gatt client connection.
 *
 * <p>{@link BluetoothDevice#connectGatt} ensures It applies the Gatt settings passed as part of
 * {@link BluetoothGattConnectionSettings}
 *
 * @see BluetoothDevice#connectGatt
 */
public final class BluetoothGattConnectionSettings {
    /** Value for not using RSSI threshold for triggering the connection. */
    @FlaggedApi(Flags.FLAG_RSSI_THRESHOLD_FOR_LE_CONN)
    public static final int RSSI_THRESHOLD_NOT_SET = -128;

    /** Value for not using pathloss threshold for triggering the connection. */
    @FlaggedApi(Flags.FLAG_RSSI_THRESHOLD_FOR_LE_CONN)
    public static final int PATHLOSS_THRESHOLD_NOT_SET = -1;

    /** Internal representation of the BluetoothGattConnectionSettings parameters. */
    private final InnerParcel mParcel;

    /** Returns true if auto connection enabled or false otherwise. */
    @RequiresNoPermission
    public boolean isAutoConnectEnabled() {
        return !mParcel.isDirectConnection();
    }

    /** Returns if the GATT connection is opportunistic or not. */
    @RequiresNoPermission
    public boolean isOpportunisticEnabled() {
        return mParcel.isOpportunistic();
    }

    /** Returns the transport to be used for GATT connection. */
    @RequiresNoPermission
    public @Transport int getTransport() {
        return mParcel.getTransport();
    }

    /**
     * Returns true if the automatic MTU exchange is enabled for this connection or false otherwise.
     */
    @RequiresNoPermission
    public boolean isAutomaticMtuEnabled() {
        return mParcel.isAutoMtuEnabled();
    }

    /**
     * Returns the RSSI threshold (lower bound) in dBm for initiating connection.
     *
     * @see Builder#setRssiThresholdDbm(int)
     *
     * <p>Return {@link #RSSI_THRESHOLD_NOT_SET} if rssi threshold is not set or a value in range
     * -127 to +20.
     */
    @RequiresNoPermission
    @FlaggedApi(Flags.FLAG_RSSI_THRESHOLD_FOR_LE_CONN)
    public @IntRange(from = -128, to = 20) int getRssiThresholdDbm() {
        return mParcel.getRssiThresholdDbm();
    }

    /**
     * Returns the Pathloss threshold (upper bound) in dB for initiating connection.
     *
     * @see Builder#setPathlossThresholdDb(int)
     *
     * <p>Return {@link #PATHLOSS_THRESHOLD_NOT_SET} if pathloss threshold is not set or a value in
     * range 0 to +100.
     */
    @RequiresNoPermission
    @FlaggedApi(Flags.FLAG_RSSI_THRESHOLD_FOR_LE_CONN)
    public @IntRange(from = -1, to = 100) int getPathlossThresholdDb() {
        return mParcel.getPathlossThresholdDb();
    }

    /**
     * Returns a {@link String} that describes each BluetoothGattConnectionSettings parameter
     * current value.
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("BluetoothGattConnectionSettings{");
        builder.append(", IsAutoConnectEnabled=")
                .append(isAutoConnectEnabled())
                .append(", IsOpportunisticEnabled=")
                .append(isOpportunisticEnabled())
                .append(", Transport=")
                .append(getTransport())
                .append(", AutomaticMtuEnabled=")
                .append(isAutomaticMtuEnabled())
                .append(", RssiThresholdDbm=")
                .append(getRssiThresholdDbm())
                .append(", PathlossThresholdDb=")
                .append(getPathlossThresholdDb())
                .append("}");
        return builder.toString();
    }

    private BluetoothGattConnectionSettings(InnerParcel parcel) {
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
     * @return the {@link BluetoothGattConnectionSettings} associated with this parcel
     */
    @Hide
    @RequiresNoPermission
    public static @Nullable BluetoothGattConnectionSettings fromParcel(InnerParcel parcel) {
        if (parcel == null) {
            return null;
        }
        return new BluetoothGattConnectionSettings(parcel);
    }

    /** Builder for {@link BluetoothGattConnectionSettings}. */
    public static final class Builder {
        private final InnerParcel mParcel = new InnerParcel();

        /** Creates a new Builder for {@link BluetoothGattConnectionSettings}. */
        public Builder() {}

        /**
         * Setting this to true will enable the automatic connection to remote device when It is
         * available. Setting it to False would trigger direct connect to remote device
         *
         * @param autoConnectEnabled true if auto connection is enabled, false otherwise.
         * @return This builder.
         */
        @NonNull
        @RequiresNoPermission
        public Builder setAutoConnectEnabled(boolean autoConnectEnabled) {
            mParcel.mIsDirectConnection = !autoConnectEnabled;
            return this;
        }

        /**
         * Sets whether this GATT client is opportunistic. An opportunistic GATT client does not
         * hold a GATT connection. It automatically disconnects when no other GATT connections are
         * active for the remote device
         *
         * @param opportunisticEnabled true if this connection is opportunistic, false otherwise.
         * @return This builder.
         */
        @NonNull
        @RequiresNoPermission
        public Builder setOpportunisticEnabled(boolean opportunisticEnabled) {
            mParcel.mOpportunistic = opportunisticEnabled;
            return this;
        }

        /**
         * Sets the transport for this Gatt settings. preferred transport for GATT connections to
         * remote dual-mode devices.
         *
         * @return This builder.
         */
        @NonNull
        @RequiresNoPermission
        public Builder setTransport(@Transport int transport) {
            mParcel.mTransport = transport;
            return this;
        }

        /**
         * Sets if the MTU (Maximum Transmission Unit) needs to be negotiated for given connection
         * or not. This is set to true by default so that MTU exchange happens after the connection.
         * Applications have to set it to false to disable the automatic negotiation. Setting this
         * to false does not prevent MTU negotiation if a client explicitly requests it using {@link
         * BluetoothGatt#requestMtu} or if it's triggered internally by other profiles.
         *
         * @param automaticMtuEnabled true if Default MTU setting needs to be applied on this
         *     connection, false otherwise.
         * @return This builder.
         */
        @NonNull
        @RequiresNoPermission
        public Builder setAutomaticMtuEnabled(boolean automaticMtuEnabled) {
            mParcel.mAutoMtuEnabled = automaticMtuEnabled;
            return this;
        }

        /**
         * Sets the RSSI threshold for initiating a connection. If the device is already connected,
         * then the GATT connection will succeed regardless of the RSSI.
         *
         * <p>The RSSI threshold is used to initiate an LE connection to a remote device when
         * autoConnect is enabled. When autoConnect is enabled, the Bluetooth controller will add
         * the remote device to the accept list and initiate the connection when the device is in
         * range and has a strong signal. The Bluetooth controller will initiate the connection if
         * the RSSI is greater than the given RSSI threshold value.
         *
         * <p>This is just a preference for the Bluetooth controller; the controller may or may not
         * use this threshold to initiate the connection.
         *
         * <p>When multiple applications request connections with different RSSI threshold values,
         * the Bluetooth system will use the lowest (least stringent) RSSI threshold among all
         * active requests when initiating the next LE connection based on signal strength, allowing
         * connections to be initiated at weaker signal levels if any app requests it.
         *
         * <p>Default RSSI threshold will be set to {@link #RSSI_THRESHOLD_NOT_SET} which indicates
         * that RSSI based LE connection trigger is not enabled.
         *
         * <p>Pathloss threshold set using {@link #setPathlossThresholdDb} will be prioritized over
         * RSSI threshold for triggering LE connection. RSSI threshold will be used for triggering
         * LE connection only if pathloss threshold is not set.
         *
         * @param rssiThresholdDbm the RSSI threshold in dBm for initiating connection.
         * @return This builder.
         */
        @NonNull
        @RequiresNoPermission
        @FlaggedApi(Flags.FLAG_RSSI_THRESHOLD_FOR_LE_CONN)
        public Builder setRssiThresholdDbm(@IntRange(from = -128, to = 20) int rssiThresholdDbm) {
            if (rssiThresholdDbm < -128 || rssiThresholdDbm > 20) {
                throw new IllegalArgumentException(
                        "Invalid rssiThresholdDbm: "
                                + rssiThresholdDbm
                                + ". Must be between -128 and 20");
            }
            mParcel.mRssiThresholdDbm = rssiThresholdDbm;
            return this;
        }

        /**
         * Sets the Pathloss threshold for initiating a connection. If the device is already
         * connected, then the GATT connection will succeed regardless of the Pathloss Threshold.
         *
         * <p>The Pathloss threshold is used to initiate an LE connection to a remote device when
         * autoConnect is enabled. When autoConnect is enabled, the Bluetooth controller will add
         * the remote device to the accept list and initiate the connection when the device is in
         * range and has minimal pathloss. The Bluetooth controller will initiate the connection if
         * the Pathloss is lower than the given Pathloss threshold value.
         *
         * <p>This is just a preference for the Bluetooth controller; the controller may or may not
         * use this pathloss threshold value to initiate the connection.
         *
         * <p>When multiple applications request connections with different pathloss threshold
         * values, the Bluetooth system will use the maximum of pathloss threshold among all active
         * requests when initiating the next LE connection based on pathloss threshold.
         *
         * <p>Default pathloss threshold will be set to {@link #PATHLOSS_THRESHOLD_NOT_SET} which
         * indicates that pathloss based LE connection trigger is not enabled.
         *
         * <p>When the Pathloss is not available, or this value is {@link
         * #PATHLOSS_THRESHOLD_NOT_SET}, then system will fall back to the RSSI threshold based
         * connection trigger if RSSI threshold is set using {@link #setRssiThresholdDbm}
         *
         * @param pathlossThresholdDb the pathloss threshold in dB for initiating connection.
         * @return This builder.
         */
        @NonNull
        @RequiresNoPermission
        @FlaggedApi(Flags.FLAG_RSSI_THRESHOLD_FOR_LE_CONN)
        public Builder setPathlossThresholdDb(
                @IntRange(from = -1, to = 100) int pathlossThresholdDb) {
            if (pathlossThresholdDb < -1 || pathlossThresholdDb > 100) {
                throw new IllegalArgumentException(
                        "Invalid pathlossThresholdDb: "
                                + pathlossThresholdDb
                                + ". Must be between -1 and 100");
            }
            mParcel.mPathlossThresholdDb = pathlossThresholdDb;
            return this;
        }

        /**
         * Builds a {@link BluetoothGattConnectionSettings} object.
         *
         * @return A new {@link BluetoothGattConnectionSettings} object with the configured
         *     parameters.
         * @throws IllegalArgumentException on invalid parameters
         */
        @NonNull
        @RequiresNoPermission
        public BluetoothGattConnectionSettings build() {
            return new BluetoothGattConnectionSettings(mParcel);
        }
    }

    @Hide
    public static final class InnerParcel implements Parcelable {
        boolean mIsDirectConnection = true;
        int mTransport = BluetoothDevice.TRANSPORT_LE;
        boolean mOpportunistic = false;
        boolean mAutoMtuEnabled = true;
        int mRssiThresholdDbm = RSSI_THRESHOLD_NOT_SET;
        int mPathlossThresholdDb = PATHLOSS_THRESHOLD_NOT_SET;

        InnerParcel() {}

        public InnerParcel(@NonNull Parcel in) {
            this(
                    in.readBoolean(),
                    in.readInt(),
                    in.readBoolean(),
                    in.readBoolean(),
                    in.readInt(),
                    in.readInt());
        }

        public InnerParcel(
                boolean isDirectConnection,
                int transport,
                boolean opportunistic,
                boolean autoMtuEnabled) {
            this(
                    isDirectConnection,
                    transport,
                    opportunistic,
                    autoMtuEnabled,
                    RSSI_THRESHOLD_NOT_SET,
                    PATHLOSS_THRESHOLD_NOT_SET);
        }

        public InnerParcel(
                boolean isDirectConnection,
                int transport,
                boolean opportunistic,
                boolean autoMtuEnabled,
                int rssiThresholdDbm,
                int pathlossThresholdDb) {
            mIsDirectConnection = isDirectConnection;
            mTransport = transport;
            mOpportunistic = opportunistic;
            mAutoMtuEnabled = autoMtuEnabled;
            mRssiThresholdDbm = rssiThresholdDbm;
            mPathlossThresholdDb = pathlossThresholdDb;
        }

        @RequiresNoPermission
        public boolean isDirectConnection() {
            return mIsDirectConnection;
        }

        @RequiresNoPermission
        public int getTransport() {
            return mTransport;
        }

        @RequiresNoPermission
        public boolean isOpportunistic() {
            return mOpportunistic;
        }

        @RequiresNoPermission
        public boolean isAutoMtuEnabled() {
            return mAutoMtuEnabled;
        }

        @RequiresNoPermission
        public int getRssiThresholdDbm() {
            return mRssiThresholdDbm;
        }

        @RequiresNoPermission
        public int getPathlossThresholdDb() {
            return mPathlossThresholdDb;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            out.writeBoolean(mIsDirectConnection);
            out.writeInt(mTransport);
            out.writeBoolean(mOpportunistic);
            out.writeBoolean(mAutoMtuEnabled);
            out.writeInt(mRssiThresholdDbm);
            out.writeInt(mPathlossThresholdDb);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            InnerParcel that = (InnerParcel) o;
            return mIsDirectConnection == that.mIsDirectConnection
                    && mTransport == that.mTransport
                    && mOpportunistic == that.mOpportunistic
                    && mAutoMtuEnabled == that.mAutoMtuEnabled
                    && mRssiThresholdDbm == that.mRssiThresholdDbm
                    && mPathlossThresholdDb == that.mPathlossThresholdDb;
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(
                    mIsDirectConnection,
                    mTransport,
                    mOpportunistic,
                    mAutoMtuEnabled,
                    mRssiThresholdDbm,
                    mPathlossThresholdDb);
        }

        public static final @NonNull Parcelable.Creator<InnerParcel> CREATOR =
                new Creator<InnerParcel>() {
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
}
