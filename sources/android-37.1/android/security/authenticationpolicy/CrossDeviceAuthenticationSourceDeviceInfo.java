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

package android.security.authenticationpolicy;

import static android.hardware.biometrics.Flags.FLAG_AGENT_AUTH_XDEVICE_TOKEN;

import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SuppressLint;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import android.annotation.TestApi;
import android.content.pm.Signature;
import android.os.Parcel;
import android.os.Parcelable;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents the fully populated cross-device payload, forming a superset
 * of the publicly exposed CrossDeviceAuthenticatedMessage. Used for testing.
 */
@TestApi
@FlaggedApi(FLAG_AGENT_AUTH_XDEVICE_TOKEN)
@SuppressLint("MissingGetterMatchingBuilder")
// LINT.IfChange
public final class CrossDeviceAuthenticationSourceDeviceInfo implements Parcelable {
    private final byte[] mUuidBytes;
    private final String mPackageName;
    private final List<Signature> mSignatures;
    private final boolean mWasCallingPackageInForeground;
    private final Instant mTimestamp;
    private final boolean mIsPc;
    private final boolean mIsWatch;
    private final boolean mIsAutomotive;
    private final boolean mIsXrPeripheral;
    private final boolean mIsDeviceSecure;
    private final boolean mIsDeviceLocked;

    private CrossDeviceAuthenticationSourceDeviceInfo(
            @NonNull byte[] uuid,
            @NonNull String packageName,
            @NonNull List<Signature> signatures,
            boolean wasCallingPackageInForeground,
            @NonNull Instant timestamp,
            boolean isPc,
            boolean isWatch,
            boolean isAutomotive,
            boolean isXrPeripheral,
            boolean isDeviceSecure,
            boolean isDeviceLocked) {
        mUuidBytes = Objects.requireNonNull(uuid).clone();
        mPackageName = Objects.requireNonNull(packageName);
        mSignatures = List.copyOf(signatures);
        mWasCallingPackageInForeground = wasCallingPackageInForeground;
        mTimestamp = Objects.requireNonNull(timestamp);
        mIsPc = isPc;
        mIsWatch = isWatch;
        mIsAutomotive = isAutomotive;
        mIsXrPeripheral = isXrPeripheral;
        mIsDeviceSecure = isDeviceSecure;
        mIsDeviceLocked = isDeviceLocked;
    }

    private CrossDeviceAuthenticationSourceDeviceInfo(Parcel in) {
        mUuidBytes = in.createByteArray();
        mPackageName = in.readString();
        mSignatures = in.createTypedArrayList(Signature.CREATOR);
        mWasCallingPackageInForeground = in.readBoolean();
        mTimestamp = Instant.ofEpochMilli(in.readLong());
        mIsPc = in.readBoolean();
        mIsWatch = in.readBoolean();
        mIsAutomotive = in.readBoolean();
        mIsXrPeripheral = in.readBoolean();
        mIsDeviceSecure = in.readBoolean();
        mIsDeviceLocked = in.readBoolean();
    }

    @NonNull
    public byte[] getUuidBytes() {
        return mUuidBytes;
    }

    @NonNull
    public UUID getUuid() {
        return getUuidFromBytes(mUuidBytes);
    }

    @NonNull
    public String getPackageName() {
        return mPackageName;
    }

    @NonNull
    public List<Signature> getSignatures() {
        return mSignatures;
    }

    public boolean wasCallingPackageInForeground() {
        return mWasCallingPackageInForeground;
    }

    @NonNull
    public Instant getTimestamp() {
        return mTimestamp;
    }

    public boolean isDevicePc() {
        return mIsPc;
    }

    public boolean isDeviceWatch() {
        return mIsWatch;
    }

    public boolean isDeviceAutomotive() {
        return mIsAutomotive;
    }

    public boolean isDeviceXrPeripheral() {
        return mIsXrPeripheral;
    }

    public boolean isDeviceSecure() {
        return mIsDeviceSecure;
    }

    public boolean isDeviceLocked() {
        return mIsDeviceLocked;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeByteArray(mUuidBytes);
        dest.writeString(mPackageName);
        dest.writeTypedList(mSignatures);
        dest.writeBoolean(mWasCallingPackageInForeground);
        dest.writeLong(mTimestamp.toEpochMilli());
        dest.writeBoolean(mIsPc);
        dest.writeBoolean(mIsWatch);
        dest.writeBoolean(mIsAutomotive);
        dest.writeBoolean(mIsXrPeripheral);
        dest.writeBoolean(mIsDeviceSecure);
        dest.writeBoolean(mIsDeviceLocked);
    }

    @NonNull
    public static final Creator<CrossDeviceAuthenticationSourceDeviceInfo> CREATOR =
            new Creator<CrossDeviceAuthenticationSourceDeviceInfo>() {
                @Override
                public CrossDeviceAuthenticationSourceDeviceInfo createFromParcel(Parcel in) {
                    return new CrossDeviceAuthenticationSourceDeviceInfo(in);
                }

                @Override
                public CrossDeviceAuthenticationSourceDeviceInfo[] newArray(int size) {
                    return new CrossDeviceAuthenticationSourceDeviceInfo[size];
                }
            };

    @TestApi
    @SuppressLint("MissingGetterMatchingBuilder")
    public static final class Builder {
        private final byte[] mUuid;
        private String mPackageName = "";
        private List<Signature> mSignatures = List.of();
        private boolean mWasCallingPackageInForeground = false;
        private Instant mTimestamp = Instant.now();
        private boolean mIsPc = false;
        private boolean mIsWatch = false;
        private boolean mIsAutomotive = false;
        private boolean mIsXrPeripheral = false;
        private boolean mIsDeviceSecure = false;
        private boolean mIsDeviceLocked = false;

        public Builder(@NonNull UUID uuid) {
            mUuid = getBytesFromUuid(Objects.requireNonNull(uuid));
        }

        public Builder(@NonNull byte[] uuidBytes) {
            mUuid = Objects.requireNonNull(uuidBytes).clone();
        }

        @NonNull
        public Builder setPackageName(@NonNull String packageName) {
            mPackageName = packageName;
            return this;
        }

        @NonNull
        public Builder setSignatures(@NonNull List<Signature> signatures) {
            mSignatures = signatures;
            return this;
        }

        @NonNull
        @SuppressLint("MissingGetterMatchingBuilder")
        public Builder setWasCallingPackageInForeground(boolean wasCallingPackageInForeground) {
            mWasCallingPackageInForeground = wasCallingPackageInForeground;
            return this;
        }

        @NonNull
        public Builder setTimestamp(@NonNull Instant timestamp) {
            mTimestamp = timestamp;
            return this;
        }

        @NonNull
        public Builder setDevicePc(boolean isPc) {
            mIsPc = isPc;
            return this;
        }

        @NonNull
        public Builder setDeviceWatch(boolean isWatch) {
            mIsWatch = isWatch;
            return this;
        }

        @NonNull
        public Builder setDeviceAutomotive(boolean isAutomotive) {
            mIsAutomotive = isAutomotive;
            return this;
        }

        @NonNull
        public Builder setDeviceXrPeripheral(boolean isXrPeripheral) {
            mIsXrPeripheral = isXrPeripheral;
            return this;
        }

        @NonNull
        public Builder setDeviceSecure(boolean isDeviceSecure) {
            mIsDeviceSecure = isDeviceSecure;
            return this;
        }

        @NonNull
        public Builder setDeviceLocked(boolean isDeviceLocked) {
            mIsDeviceLocked = isDeviceLocked;
            return this;
        }

        @NonNull
        public CrossDeviceAuthenticationSourceDeviceInfo build() {
            Objects.requireNonNull(mPackageName, "packageName cannot be null");
            Objects.requireNonNull(mTimestamp, "timestamp cannot be null");
            return new CrossDeviceAuthenticationSourceDeviceInfo(
                    mUuid,
                    mPackageName,
                    mSignatures,
                    mWasCallingPackageInForeground,
                    mTimestamp,
                    mIsPc,
                    mIsWatch,
                    mIsAutomotive,
                    mIsXrPeripheral,
                    mIsDeviceSecure,
                    mIsDeviceLocked
            );
        }
    }

    /**
     * Converts a UUID to a byte array.
     *
     * @param uuid the UUID to convert
     * @return the byte array representation of the UUID
     */
    public static @NonNull byte[] getBytesFromUuid(@NonNull UUID uuid) {
        ByteBuffer bytes = ByteBuffer.allocate(16);
        bytes.order(ByteOrder.BIG_ENDIAN);
        bytes.putLong(uuid.getMostSignificantBits());
        bytes.putLong(uuid.getLeastSignificantBits());
        return bytes.array();
    }

    /**
     * Converts a byte array to a UUID.
     *
     * @param bytes the byte array representation of the UUID
     * @return the UUID
     * @throws IllegalArgumentException if the byte array is not 16 bytes
     */
    public static @NonNull UUID getUuidFromBytes(@NonNull byte[] bytes) {
        if (bytes.length != 16) {
            throw new IllegalArgumentException("Must be 16 bytes to be a valid UUID.");
        }

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        buffer.order(ByteOrder.BIG_ENDIAN);
        return new UUID(buffer.getLong(), buffer.getLong());
    }
}
// LINT.ThenChange(frameworks/base/core/proto/android/server/cross_device_authenticated_payload.proto)
