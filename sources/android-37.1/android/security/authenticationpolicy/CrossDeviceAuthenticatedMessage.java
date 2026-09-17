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
import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.app.KeyguardManager;
import android.content.pm.Signature;
import android.os.Parcel;
import android.os.Parcelable;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Represents the state of the source device at the moment a cross-device request was initiated.
 */
@SystemApi
@FlaggedApi(FLAG_AGENT_AUTH_XDEVICE_TOKEN)
public final class CrossDeviceAuthenticatedMessage implements Parcelable {

    private final boolean mIsDeviceSecure;
    private final boolean mIsDeviceLocked;
    private final Instant mTimestamp;
    private final CallingPackage mCallingPackage;
    private final boolean mCallingPackageWasInForeground;

    /** See {@link CrossDeviceAuthenticatedMessage.Builder}. */
    private CrossDeviceAuthenticatedMessage(boolean isDeviceSecure,
            boolean isDeviceLocked, @NonNull Instant timestamp,
            @NonNull CallingPackage callingPackage, boolean callingPackageWasInForeground) {
        mIsDeviceSecure = isDeviceSecure;
        mIsDeviceLocked = isDeviceLocked;
        mTimestamp = Objects.requireNonNull(timestamp);
        mCallingPackage = Objects.requireNonNull(callingPackage);
        mCallingPackageWasInForeground = callingPackageWasInForeground;
    }

    private CrossDeviceAuthenticatedMessage(@NonNull Parcel in) {
        mIsDeviceSecure = in.readBoolean();
        mIsDeviceLocked = in.readBoolean();
        mTimestamp = Instant.ofEpochMilli(in.readLong());
        mCallingPackage = in.readTypedObject(CallingPackage.CREATOR);
        mCallingPackageWasInForeground = in.readBoolean();
    }

    /**
     * Returns if the source device has a secure lockscreen as defined by
     * {@link KeyguardManager#isDeviceSecure()} for the user associated with this request.
     */
    public boolean isDeviceSecure() {
        return mIsDeviceSecure;
    }

    /**
     * Returns if the source device is currently locked as defined by
     * {@link KeyguardManager#isDeviceLocked()} for the user associated with this request.
     */
    public boolean isDeviceLocked() {
        return mIsDeviceLocked;
    }

    /**
     * Returns the timestamp that this request was created at according to the source device's
     * {@link System#currentTimeMillis()} clock.
     */
    @NonNull
    public Instant getTimestamp() {
        return mTimestamp;
    }

    /**
     * Returns information about the app on the source device that is requesting authentication
     * for some action on a remote device.
     */
    @NonNull
    public CallingPackage getCallingPackage() {
        return mCallingPackage;
    }

    /**
     * Returns if the app on the source device had some visible UI in the foreground at the time
     * of the request.
     */
    public boolean getWasCallingPackageInForeground() {
        return mCallingPackageWasInForeground;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeBoolean(mIsDeviceSecure);
        dest.writeBoolean(mIsDeviceLocked);
        dest.writeLong(mTimestamp.toEpochMilli());
        dest.writeTypedObject(mCallingPackage, flags);
        dest.writeBoolean(mCallingPackageWasInForeground);
    }

    @NonNull
    public static final Creator<CrossDeviceAuthenticatedMessage> CREATOR =
            new Creator<CrossDeviceAuthenticatedMessage>() {
                @Override
                public CrossDeviceAuthenticatedMessage createFromParcel(Parcel in) {
                    return new CrossDeviceAuthenticatedMessage(in);
                }

                @Override
                public CrossDeviceAuthenticatedMessage[] newArray(int size) {
                    return new CrossDeviceAuthenticatedMessage[size];
                }
            };

    /**
     * Builder for {@link CrossDeviceAuthenticatedMessage}.
     */
    @FlaggedApi(FLAG_AGENT_AUTH_XDEVICE_TOKEN)
    public static final class Builder {
        private boolean mIsDeviceSecure;
        private boolean mIsDeviceLocked;
        private Instant mTimestamp = Instant.now();
        private CallingPackage mCallingPackage;
        private boolean mCallingPackageWasInForeground;

        public Builder() {
        }


        /**
         * Sets whether the source device has a secure lockscreen.
         */
        @NonNull
        public Builder setDeviceSecure(boolean isDeviceSecure) {
            mIsDeviceSecure = isDeviceSecure;
            return this;
        }

        /**
         * Sets whether the source device is currently locked.
         */
        @NonNull
        public Builder setDeviceLocked(boolean isDeviceLocked) {
            mIsDeviceLocked = isDeviceLocked;
            return this;
        }

        /**
         * Sets the timestamp this request was created.
         */
        @NonNull
        public Builder setTimestamp(@NonNull Instant timestamp) {
            mTimestamp = Objects.requireNonNull(timestamp);
            return this;
        }

        /**
         * Sets information about the app on the source device that is requesting authentication.
         */
        @NonNull
        public Builder setCallingPackage(@NonNull CallingPackage callingPackage) {
            mCallingPackage = Objects.requireNonNull(callingPackage);
            return this;
        }

        /**
         * Sets whether the app on the source device had some visible UI in the foreground.
         */
        @NonNull
        @SuppressLint("MissingGetterMatchingBuilder")
        public Builder setWasCallingPackageInForeground(boolean wasCallingPackageInForeground) {
            mCallingPackageWasInForeground = wasCallingPackageInForeground;
            return this;
        }

        /**
         * Builds the {@link CrossDeviceAuthenticatedMessage}.
         */
        @NonNull
        public CrossDeviceAuthenticatedMessage build() {
            return new CrossDeviceAuthenticatedMessage(mIsDeviceSecure, mIsDeviceLocked,
                    mTimestamp, mCallingPackage, mCallingPackageWasInForeground);
        }
    }

    /**
     * Represents the calling application for a {@link CrossDeviceAuthenticatedMessage}.
     */
    @SystemApi
    @FlaggedApi(FLAG_AGENT_AUTH_XDEVICE_TOKEN)
    public static final class CallingPackage implements Parcelable {
        private final String mPackageName;
        private final List<Signature> mSignatures;

        public CallingPackage(@NonNull String packageName,
                @NonNull List<Signature> signatures) {
            mPackageName = Objects.requireNonNull(packageName);
            mSignatures = List.copyOf(signatures);
        }

        private CallingPackage(Parcel in) {
            mPackageName = in.readString8();
            mSignatures = in.createTypedArrayList(Signature.CREATOR);
        }

        /**
         * Returns the package name of the app.
         */
        @NonNull
        public String getPackageName() {
            return mPackageName;
        }

        /**
         * Returns the signatures of the app.
         */
        @NonNull
        public List<Signature> getSignatures() {
            return mSignatures;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeString8(mPackageName);
            dest.writeTypedList(mSignatures);
        }

        @NonNull
        public static final Creator<CallingPackage> CREATOR = new Creator<CallingPackage>() {
            @Override
            public CallingPackage createFromParcel(Parcel in) {
                return new CallingPackage(in);
            }

            @Override
            public CallingPackage[] newArray(int size) {
                return new CallingPackage[size];
            }
        };
    }
}
