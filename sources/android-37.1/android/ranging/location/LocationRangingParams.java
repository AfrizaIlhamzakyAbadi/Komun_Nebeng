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

package android.ranging.location;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.os.Parcel;
import android.os.Parcelable;
import android.ranging.oob.TransportHandle;

import com.android.ranging.flags.Flags;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Duration;
import java.util.Objects;

/**
 * Ranging parameters for the Location technology used in Generic Ranging API (GRAPI) RAW mode.
 *
 * <p>This class allows setting {@link android.ranging.RangingTechnology#LOCATION} as the
 * ranging technology in GRAPI RAW mode by providing a {@link TransportHandle}.
 *
 * <p>Unlike standard OOB ranging sessions, when using the Location technology in RAW mode:
 * <ul>
 *   <li>The provided {@code TransportHandle} is used <strong>only</strong> to exchange the
 *       "Location Update" control messages containing the devices' coordinates.</li>
 *   <li>The regular OOB setup flows (such as capability exchange, configuration negotiation,
 *       and ranging start/stop control messages) are entirely bypassed.</li>
 *   <li>The session is active as long as the raw ranging session is started, and it will
 *       periodically send and receive location coordinates over the transport.</li>
 * </ul>
 *
 * <p>Location precision requirements are configured using the {@link LocationQuality} constants:
 * <ul>
 *   <li>{@link #LOCATION_QUALITY_HIGH_ACCURACY}: Requests fine accuracy location (e.g., GPS/GNSS).
 *   </li>
 *   <li>{@link #LOCATION_QUALITY_BALANCED}: Requests block-level accuracy location (e.g.,
 *       Wi-Fi/Cellular).</li>
 *   <li>{@link #LOCATION_QUALITY_LOW_POWER}: Requests city-level accuracy with minimal power
 *       consumption.</li>
 * </ul>
 */
@FlaggedApi(Flags.FLAG_RANGING_LOCATION_ENABLED)
public final class LocationRangingParams implements Parcelable {

    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
            LOCATION_QUALITY_HIGH_ACCURACY,
            LOCATION_QUALITY_BALANCED,
            LOCATION_QUALITY_LOW_POWER,
    })
    public @interface LocationQuality {}

    /** High accuracy GNSS location quality. */
    public static final int LOCATION_QUALITY_HIGH_ACCURACY = 1;
    /** Balanced network location quality. */
    public static final int LOCATION_QUALITY_BALANCED = 2;
    /** Low power location quality. */
    public static final int LOCATION_QUALITY_LOW_POWER = 3;

    private final Duration mUpdateInterval;
    private final @LocationQuality int mLocationQuality;
    private final TransportHandle mTransportHandle;

    private LocationRangingParams(Builder builder) {
        mUpdateInterval = builder.mUpdateInterval;
        mLocationQuality = builder.mLocationQuality;
        mTransportHandle = builder.mTransportHandle;
    }

    private LocationRangingParams(Parcel in) {
        mUpdateInterval = Duration.ofMillis(in.readLong());
        mLocationQuality = in.readInt();
        mTransportHandle = null;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(mUpdateInterval.toMillis());
        dest.writeInt(mLocationQuality);
    }

    @NonNull
    public static final Creator<LocationRangingParams> CREATOR =
            new Creator<LocationRangingParams>() {
        @Override
        public LocationRangingParams createFromParcel(Parcel in) {
            return new LocationRangingParams(in);
        }

        @Override
        public LocationRangingParams[] newArray(int size) {
            return new LocationRangingParams[size];
        }
    };

    /**
     * Returns the location update interval.
     *
     * @return the location update interval.
     */
    @NonNull
    public Duration getUpdateInterval() {
        return mUpdateInterval;
    }

    /**
     * Returns the requested location quality.
     *
     * @return the location quality, one of {@link LocationQuality}.
     */
    public @LocationQuality int getLocationQuality() {
        return mLocationQuality;
    }

    /**
     * Returns the OOB transport handle used to exchange coordinates.
     *
     * @return the OOB transport handle.
     */
    @NonNull
    public TransportHandle getTransportHandle() {
        return mTransportHandle;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationRangingParams that = (LocationRangingParams) o;
        return mLocationQuality == that.mLocationQuality
                && Objects.equals(mUpdateInterval, that.mUpdateInterval)
                && Objects.equals(mTransportHandle, that.mTransportHandle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mUpdateInterval, mLocationQuality, mTransportHandle);
    }

    @Override
    public String toString() {
        return "LocationRangingParams{"
                + "mUpdateInterval=" + mUpdateInterval
                + ", mLocationQuality=" + mLocationQuality
                + ", mTransportHandle=" + mTransportHandle
                + '}';
    }

    /**
     * Builder class for {@link LocationRangingParams}.
     */
    public static final class Builder {
        private final TransportHandle mTransportHandle;
        private Duration mUpdateInterval = Duration.ofSeconds(1);
        private @LocationQuality int mLocationQuality = LOCATION_QUALITY_BALANCED;

        /**
         * Creates a new Builder instance with mandatory transport handle.
         *
         * @param transportHandle the OOB transport handle used to exchange coordinates.
         */
        public Builder(@NonNull TransportHandle transportHandle) {
            mTransportHandle = Objects.requireNonNull(transportHandle);
        }

        /**
         * Sets the location update interval.
         *
         * <p>Defaults to 1 second if not explicitly set.
         *
         * @param updateInterval the update interval.
         * @return this {@link Builder} instance for chaining.
         */
        @NonNull
        public Builder setUpdateInterval(@NonNull Duration updateInterval) {
            mUpdateInterval = Objects.requireNonNull(updateInterval);
            return this;
        }

        /**
         * Sets the requested location quality.
         *
         * <p>Defaults to {@link #LOCATION_QUALITY_BALANCED} if not explicitly set.
         *
         * @param locationQuality the location quality, must be one of the
         *                        {@link LocationQuality} constants.
         * @return this {@link Builder} instance for chaining.
         * @throws IllegalArgumentException if {@code locationQuality} is not a valid quality
         *                                  value.
         */
        @NonNull
        public Builder setLocationQuality(@LocationQuality int locationQuality) {
            if (locationQuality != LOCATION_QUALITY_HIGH_ACCURACY
                    && locationQuality != LOCATION_QUALITY_BALANCED
                    && locationQuality != LOCATION_QUALITY_LOW_POWER) {
                throw new IllegalArgumentException("Invalid locationQuality: " + locationQuality);
            }
            mLocationQuality = locationQuality;
            return this;
        }

        /**
         * Builds and returns a new {@link LocationRangingParams} instance.
         *
         * @return the newly constructed {@link LocationRangingParams}.
         */
        @NonNull
        public LocationRangingParams build() {
            return new LocationRangingParams(this);
        }
    }
}
