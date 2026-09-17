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
package android.scheduling;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntRange;
import android.annotation.NonNull;
import android.annotation.SystemApi;

import com.android.scheduling.flags.Flags;

/**
 * Represents the conditions that must be met before a device can reboot. This class is immutable
 * and should be instantiated using the {@link Builder}.
 */
@FlaggedApi(Flags.FLAG_ENABLE_REBOOT_SCHEDULER_IMPLEMENTATION)
@SystemApi
public final class RebootConditions {
    private final boolean mChargingRequired;
    private final long mMinimumStorageBytes;
    private final int mMinimumBatteryPercentage;

    /**
     * Constructs a new RebootConditions instance from the Builder.
     *
     * @param builder The builder instance to source the conditions from.
     */
    @Hide
    private RebootConditions(Builder builder) {
        this.mChargingRequired = builder.mChargingRequired;
        this.mMinimumStorageBytes = builder.mMinimumStorageBytes;
        this.mMinimumBatteryPercentage = builder.mMinimumBatteryPercentage;
    }

    /**
     * Gets whether the device requires charging to reboot.
     *
     * @return {@code true} if the device must be charging, {@code false} otherwise.
     */
    @SystemApi
    public boolean isChargingRequired() {
        return mChargingRequired;
    }

    /**
     * Gets the minimum available storage space in bytes required for the device to reboot.
     *
     * @return The minimum space in bytes.
     */
    @SystemApi
    @IntRange(from = 0)
    public long getMinimumStorageBytes() {
        return mMinimumStorageBytes;
    }

    /**
     * Gets the minimum battery percentage required for the device to reboot.
     *
     * @return The minimum battery percentage (0-100).
     */
    @SystemApi
    @IntRange(from = 0, to = 100)
    public int getMinimumBatteryPercentage() {
        return mMinimumBatteryPercentage;
    }

    @Override
    public String toString() {
        return "RebootConditions{"
                + "mChargingRequired="
                + mChargingRequired
                + ", mMinimumStorageBytes="
                + mMinimumStorageBytes
                + ", mMinimumBatteryPercentage="
                + mMinimumBatteryPercentage
                + '}';
    }

    /** Builder for {@link RebootConditions}. */
    @SystemApi
    public static final class Builder {
        private boolean mChargingRequired = false;
        private long mMinimumStorageBytes = 0;
        private int mMinimumBatteryPercentage = 0;

        @SystemApi
        public Builder() {}

        /**
         * Sets whether the device requires charging to reboot.
         *
         * @param chargingRequired {@code true} if charging is required or {@code false} otherwise.
         * @return this {@link Builder} instance.
         */
        @NonNull
        @SystemApi
        public Builder setChargingRequired(boolean chargingRequired) {
            mChargingRequired = chargingRequired;
            return this;
        }

        /**
         * Sets the minimum space in bytes required to reboot.
         *
         * @param minimumStorageBytes the minimum space in bytes.
         * @return this {@link Builder} instance.
         */
        @NonNull
        @SystemApi
        public Builder setMinimumStorageBytes(@IntRange(from = 0) long minimumStorageBytes) {
            if (minimumStorageBytes < 0) {
                throw new IllegalArgumentException("Minimum storage left must be non-negative.");
            }
            mMinimumStorageBytes = minimumStorageBytes;
            return this;
        }

        /**
         * Sets the minimum battery percentage required to reboot.
         *
         * @param minimumBatteryPercentage the minimum battery percentage.
         * @return this {@link Builder} instance.
         * @throws IllegalArgumentException if the value is not between 0 and 100
         */
        @NonNull
        @SystemApi
        public Builder setMinimumBatteryPercentage(
                @IntRange(from = 0, to = 100) int minimumBatteryPercentage) {
            if (minimumBatteryPercentage < 0 || minimumBatteryPercentage > 100) {
                throw new IllegalArgumentException(
                        "Minimum battery percent must be in [0, 100] range.");
            }
            mMinimumBatteryPercentage = minimumBatteryPercentage;
            return this;
        }

        /**
         * Builds a new {@link RebootConditions}.
         *
         * @return a new {@link RebootConditions}.
         */
        @NonNull
        @SystemApi
        public RebootConditions build() {
            return new RebootConditions(this);
        }
    }
}
