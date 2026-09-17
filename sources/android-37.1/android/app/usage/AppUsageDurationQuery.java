/*
 * Copyright (C) 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.app.usage;

import android.annotation.CurrentTimeMillisLong;
import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * Represents a query for application usage duration statistics. Used by {@link
 * UsageStatsManager#queryAppUsageDuration(AppUsageDurationQuery)} call.
 *
 * <p>This class only supports queries for the past 30 days. Start times older than 30 days will
 * result in an empty list.
 */
@FlaggedApi(android.app.supervision.flags.Flags.FLAG_ENABLE_SUPERVISION_PACKAGE_USAGE_APIS)
public final class AppUsageDurationQuery implements Parcelable {

    @Hide
    @IntDef(
            prefix = {"INTERVAL_"},
            value = {
                UsageStatsManager.INTERVAL_DAILY,
                UsageStatsManager.INTERVAL_WEEKLY,
                UsageStatsManager.INTERVAL_MONTHLY,
                UsageStatsManager.INTERVAL_YEARLY,
            })
    @Retention(RetentionPolicy.SOURCE)
    public @interface IntervalType {}

    private final @CurrentTimeMillisLong long mStartTimeMillis;
    private final @CurrentTimeMillisLong long mEndTimeMillis;
    private final @IntervalType int mIntervalType;

    private AppUsageDurationQuery(@NonNull Builder builder) {
        mStartTimeMillis = builder.mStartTimeMillis;
        mEndTimeMillis = builder.mEndTimeMillis;
        mIntervalType = builder.mIntervalType;
    }

    private AppUsageDurationQuery(Parcel in) {
        mStartTimeMillis = in.readLong();
        mEndTimeMillis = in.readLong();
        mIntervalType = in.readInt();

        validate(mStartTimeMillis, mEndTimeMillis, mIntervalType);
    }

    /**
     * Returns the inclusive timestamp to indicate the start of the range of events. Defined in
     * terms of "Unix time", see {@link java.lang.System#currentTimeMillis()}.
     */
    public @CurrentTimeMillisLong long getStartTimeMillis() {
        return mStartTimeMillis;
    }

    /**
     * Returns the exclusive timestamp to indicate the end of the range of events. Defined in terms
     * of "Unix time", see {@link java.lang.System#currentTimeMillis()}.
     */
    public @CurrentTimeMillisLong long getEndTimeMillis() {
        return mEndTimeMillis;
    }

    /** Returns the time interval by which the stats are aggregated. */
    public @IntervalType int getIntervalType() {
        return mIntervalType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(mStartTimeMillis);
        dest.writeLong(mEndTimeMillis);
        dest.writeInt(mIntervalType);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof AppUsageDurationQuery)) return false;
        AppUsageDurationQuery that = (AppUsageDurationQuery) o;
        return mStartTimeMillis == that.mStartTimeMillis
                && mEndTimeMillis == that.mEndTimeMillis
                && mIntervalType == that.mIntervalType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mStartTimeMillis, mEndTimeMillis, mIntervalType);
    }

    @Override
    public String toString() {
        return "AppUsageDurationQuery{"
                + "mStartTimeMillis=" + mStartTimeMillis
                + ", mEndTimeMillis=" + mEndTimeMillis
                + ", mIntervalType=" + mIntervalType
                + '}';
    }

    @NonNull
    public static final Creator<AppUsageDurationQuery> CREATOR =
            new Creator<AppUsageDurationQuery>() {
                @Override
                public AppUsageDurationQuery createFromParcel(Parcel in) {
                    return new AppUsageDurationQuery(in);
                }

                @Override
                public AppUsageDurationQuery[] newArray(int size) {
                    return new AppUsageDurationQuery[size];
                }
            };

    /** Builder for {@link AppUsageDurationQuery}. */
    public static final class Builder {
        private @CurrentTimeMillisLong long mStartTimeMillis;
        private @CurrentTimeMillisLong long mEndTimeMillis;
        private @IntervalType int mIntervalType;

        /**
         * Constructor that specifies the period for which to return events.
         *
         * @param startTimeMillis inclusive start timestamp, as per {@link
         *     java.lang.System#currentTimeMillis()}
         * @param endTimeMillis exclusive end timestamp, as per {@link
         *     java.lang.System#currentTimeMillis()}
         * @param intervalType time interval by which the stats are aggregated.
         * @throws IllegalArgumentException if {@code endTimeMillis} < {@code startTimeMillis} or if
         *     {@code startTimeMillis} is negative, or if {@code intervalType} is invalid.
         */
        public Builder(
                @CurrentTimeMillisLong long startTimeMillis,
                @CurrentTimeMillisLong long endTimeMillis,
                @IntervalType int intervalType) {
            validate(startTimeMillis, endTimeMillis, intervalType);
            mStartTimeMillis = startTimeMillis;
            mEndTimeMillis = endTimeMillis;
            mIntervalType = intervalType;
        }

        /**
         * Sets the inclusive start timestamp.
         *
         * @param startTimeMillis inclusive start timestamp, as per {@link
         *     java.lang.System#currentTimeMillis()}
         * @return this {@link Builder} object
         */
        public @NonNull Builder setStartTimeMillis(
                @CurrentTimeMillisLong long startTimeMillis) {
            validate(startTimeMillis, mEndTimeMillis, mIntervalType);
            mStartTimeMillis = startTimeMillis;
            return this;
        }

        /**
         * Sets the exclusive end timestamp.
         *
         * @param endTimeMillis exclusive end timestamp, as per {@link
         *     java.lang.System#currentTimeMillis()}
         * @return this {@link Builder} object
         */
        public @NonNull Builder setEndTimeMillis(
                @CurrentTimeMillisLong long endTimeMillis) {
            validate(mStartTimeMillis, endTimeMillis, mIntervalType);
            mEndTimeMillis = endTimeMillis;
            return this;
        }

        /**
         * Sets the time interval by which the stats are aggregated.
         *
         * @param intervalType time interval by which the stats are aggregated
         * @return this {@link Builder} object
         */
        public @NonNull Builder setIntervalType(@IntervalType int intervalType) {
            validate(mStartTimeMillis, mEndTimeMillis, intervalType);
            mIntervalType = intervalType;
            return this;
        }

        /** Builds a read-only {@link AppUsageDurationQuery} object. */
        public @NonNull AppUsageDurationQuery build() {
            return new AppUsageDurationQuery(this);
        }
    }

    private static void validate(
            @CurrentTimeMillisLong long startTimeMillis,
            @CurrentTimeMillisLong long endTimeMillis,
            @IntervalType int intervalType) {
        if (startTimeMillis < 0 || endTimeMillis < startTimeMillis) {
            throw new IllegalArgumentException("Invalid period");
        }
        switch (intervalType) {
            case UsageStatsManager.INTERVAL_DAILY:
            case UsageStatsManager.INTERVAL_WEEKLY:
            case UsageStatsManager.INTERVAL_MONTHLY:
            case UsageStatsManager.INTERVAL_YEARLY:
                // Valid interval types.
                break;
            default:
                throw new IllegalArgumentException("Invalid interval type: " + intervalType);
        }
    }
}
