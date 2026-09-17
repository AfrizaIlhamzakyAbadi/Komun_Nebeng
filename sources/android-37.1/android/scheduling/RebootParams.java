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
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SystemApi;

import com.android.scheduling.flags.Flags;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.time.Duration;
import java.util.Objects;

/**
 * Represents the parameters reboot options that will be used. This class is immutable and should be
 * instantiated using the {@link Builder}.
 */
@FlaggedApi(Flags.FLAG_ENABLE_REBOOT_SCHEDULER_IMPLEMENTATION)
@SystemApi
public final class RebootParams {

    /** The device will reboot as soon as all required conditions are met. */
    @SystemApi public static final int REBOOT_MODE_IMMEDIATE_WHEN_READY = 0;

    /** The device will reboot within a specified time window once conditions are met. */
    @SystemApi public static final int REBOOT_MODE_WITHIN_WINDOW = 1;

    /** The strategy that will be used for handling the reboot request. */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({REBOOT_MODE_IMMEDIATE_WHEN_READY, REBOOT_MODE_WITHIN_WINDOW})
    public @interface RebootMode {}

    /** The Resume-on-Reboot will be used to reboot and decrypt the CE storage. */
    @SystemApi public static final int REBOOT_MECHANISM_RESUME_ON_REBOOT = 0;

    /**
     * The normal reboot will be performed, leaving the device CE storage encrypted after the device
     * boots.
     *
     * <p><strong>Warning:</strong> Be aware that this will create big disruption to the user. Use
     * it only in cases where you want the CE storage to stay encrypted.
     */
    @SystemApi public static final int REBOOT_MECHANISM_REGULAR_REBOOT = 1;

    /** Defines the mechanisms available for rebooting the system. */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({REBOOT_MECHANISM_RESUME_ON_REBOOT, REBOOT_MECHANISM_REGULAR_REBOOT})
    public @interface RebootMechanism {}

    private final @RebootMode int mMode;
    private final @RebootMechanism int mMechanism;
    private final String mReason;
    private final @Nullable Duration mDelay;
    private final RebootConditions mConditions;
    private final boolean mRequiresPreRebootNotification;

    @Hide
    private RebootParams(Builder builder) {
        this.mMode = builder.mMode;
        this.mMechanism = builder.mMechanism;
        this.mReason = builder.mReason;
        this.mDelay = builder.mDelay;
        this.mConditions = builder.mConditions;
        this.mRequiresPreRebootNotification = builder.mRequiresPreRebootNotification;
    }

    /**
     * Gets the configured reboot mode.
     *
     * @return The reboot mode, as defined in {@link RebootMode}.
     */
    @SystemApi
    public @RebootMode int getMode() {
        return mMode;
    }

    /**
     * Gets the configured reboot mechanism.
     *
     * @return The reboot mechanism, as defined in {@link RebootMechanism}.
     */
    @SystemApi
    public @RebootMechanism int getMechanism() {
        return mMechanism;
    }

    /**
     * Gets the reason for the scheduled reboot.
     *
     * @return The reason for reboot.
     */
    @NonNull
    @SystemApi
    public String getReason() {
        return mReason;
    }

    /**
     * Gets the delay before the reboot will be attempted. This delay will be used only {@link
     * REBOOT_MODE_WITHIN_WINDOW} mode.
     *
     * @return A {@link Duration} representing the delay, or {@code null} if no delay is set.
     */
    @Nullable
    @SystemApi
    public Duration getDelay() {
        return mDelay;
    }

    /**
     * Gets whether the client wants to be notified before a reboot. By default this value will be
     * {@code false}.
     *
     * @return {@code true} if the client want to be notified, {@code false} otherwise.
     */
    @SystemApi
    public boolean isPreRebootNotificationRequired() {
        return mRequiresPreRebootNotification;
    }

    /**
     * Gets the conditions that must be met for the reboot to occur.
     *
     * @return The non-null {@link RebootConditions}.
     */
    @NonNull
    @SystemApi
    public RebootConditions getConditions() {
        return mConditions;
    }

    @Override
    public String toString() {
        return "RebootParams{"
                + "mMode="
                + mMode
                + ", mMechanism="
                + mMechanism
                + ", mReason='"
                + mReason
                + '\''
                + ", mDelay="
                + mDelay
                + ", mConditions="
                + mConditions
                + ", mRequiresPreRebootNotification="
                + mRequiresPreRebootNotification
                + '}';
    }

    /**
     * Builder for {@link RebootParams}.
     *
     * <p>The default parameters are:
     *
     * <ul>
     *   <li>Mode: {@link RebootParams#REBOOT_MODE_WITHIN_WINDOW}
     *   <li>Mechanism: {@link RebootParams#REBOOT_MECHANISM_RESUME_ON_REBOOT}
     *   <li>Pre-reboot notification required: {@code false}
     * </ul>
     */
    @SystemApi
    public static final class Builder {
        private @RebootMode int mMode = REBOOT_MODE_WITHIN_WINDOW;
        private @RebootMechanism int mMechanism = REBOOT_MECHANISM_RESUME_ON_REBOOT;
        private String mReason;
        private @Nullable Duration mDelay;
        private RebootConditions mConditions;
        private boolean mRequiresPreRebootNotification = false;

        /**
         * Creates a new {@link Builder}.
         *
         * @param reason the reason for the reboot.
         * @param conditions the conditions that must be met for the reboot to occur.
         * @throws NullPointerException if {@code reason} or {@code conditions} is null.
         */
        @SystemApi
        public Builder(@NonNull String reason, @NonNull RebootConditions conditions) {
            mReason = Objects.requireNonNull(reason, "reason cannot be null");
            mConditions = Objects.requireNonNull(conditions, "conditions cannot be null");
        }

        /**
         * Sets the reboot mode.
         *
         * <p>If not called, it defaults to {@link RebootParams#REBOOT_MODE_WITHIN_WINDOW}.
         *
         * @param mode the reboot mode.
         * @return this {@link Builder} instance.
         * @see RebootParams#getMode()
         */
        @NonNull
        @SystemApi
        public Builder setMode(@RebootMode int mode) {
            mMode = mode;
            return this;
        }

        /**
         * Sets the reboot mechanism.
         *
         * <p>If not called, it defaults to {@link RebootParams#REBOOT_MECHANISM_RESUME_ON_REBOOT}.
         *
         * @param mechanism the reboot mechanism.
         * @return this {@link Builder} instance.
         * @see RebootParams#getMechanism()
         */
        @NonNull
        @SystemApi
        public Builder setMechanism(@RebootMechanism int mechanism) {
            mMechanism = mechanism;
            return this;
        }

        /**
         * Sets the delay before the reboot.
         *
         * @param delay the delay.
         * @return this {@link Builder} instance.
         * @see RebootParams#getDelay()
         */
        @NonNull
        @SystemApi
        public Builder setDelay(@Nullable Duration delay) {
            mDelay = delay;
            return this;
        }

        /**
         * Sets whether the client wants to be notified before a reboot.
         *
         * <p>If not called, it defaults to {@code false}.
         *
         * @param requiresPreRebootNotification whether the client wants to be notified.
         * @return this {@link Builder} instance.
         * @see RebootParams#requiresPreRebootNotification()
         */
        @NonNull
        @SystemApi
        public Builder setPreRebootNotificationRequired(boolean requiresPreRebootNotification) {
            mRequiresPreRebootNotification = requiresPreRebootNotification;
            return this;
        }

        /**
         * Builds a new {@link RebootParams}.
         *
         * @return a new {@link RebootParams}.
         */
        @NonNull
        @SystemApi
        public RebootParams build() {
            return new RebootParams(this);
        }
    }
}
