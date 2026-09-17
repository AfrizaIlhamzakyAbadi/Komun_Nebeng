/**
 * Copyright (C) 2026 The Android Open Source Project
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.app.usage;

import android.annotation.CurrentTimeMillisLong;
import android.annotation.DurationMillisLong;
import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

/**
 * Contains usage statistics for a single app package.
 *
 * <p>This class encapsulates the total duration a specific application was used within a specified
 * time range.
 */
@FlaggedApi(android.app.supervision.flags.Flags.FLAG_ENABLE_SUPERVISION_PACKAGE_USAGE_APIS)
public final class AppUsageDuration implements Parcelable {
    private final String mPackageName;
    private final @DurationMillisLong long mTotalUsageDurationMillis;
    private final @CurrentTimeMillisLong long mStartTimeMillis;
    private final @CurrentTimeMillisLong long mEndTimeMillis;

    /**
     * Constructor for {@link AppUsageDuration}.
     *
     * @param packageName package name of the app
     * @param totalUsageDurationMillis total duration the app was used, in milliseconds
     * @param startTimeMillis start of the time range for which the usage duration is valid, in
     *     milliseconds since epoch
     * @param endTimeMillis end of the time range for which the usage duration is valid, in
     *     milliseconds since epoch
     */
    public AppUsageDuration(
            @NonNull String packageName,
            @DurationMillisLong long totalUsageDurationMillis,
            @CurrentTimeMillisLong long startTimeMillis,
            @CurrentTimeMillisLong long endTimeMillis) {
        validate(packageName, totalUsageDurationMillis, startTimeMillis, endTimeMillis);
        mPackageName = packageName;
        mTotalUsageDurationMillis = totalUsageDurationMillis;
        mStartTimeMillis = startTimeMillis;
        mEndTimeMillis = endTimeMillis;
    }

    /** @hide */
    private AppUsageDuration(Parcel in) {
        final String packageName = in.readString8();
        final long totalUsageDurationMillis = in.readLong();
        final long startTimeMillis = in.readLong();
        final long endTimeMillis = in.readLong();
        validate(packageName, totalUsageDurationMillis, startTimeMillis, endTimeMillis);

        mPackageName = packageName;
        mTotalUsageDurationMillis = totalUsageDurationMillis;
        mStartTimeMillis = startTimeMillis;
        mEndTimeMillis = endTimeMillis;
    }

    /**
     * Returns the package name of the app.
     *
     * @return the package name of the app
     */
    @NonNull
    public String getPackageName() {
        return mPackageName;
    }

    /**
     * Returns the total duration the app was used during the range, measured in milliseconds.
     *
     * @return the total duration the app was used, in milliseconds
     */
    @DurationMillisLong
    public long getTotalUsageDurationMillis() {
        return mTotalUsageDurationMillis;
    }

    /**
     * Returns the start of the time range for which the usage duration is valid, in milliseconds
     * since epoch.
     *
     * @return the start of the time range for which the usage duration is valid, in milliseconds
     *     since epoch
     */
    @CurrentTimeMillisLong
    public long getStartTimeMillis() {
        return mStartTimeMillis;
    }

    /**
     * Returns the end of the time range for which the usage duration is valid, in milliseconds
     * since epoch.
     *
     * @return the end of the time range for which the usage duration is valid, in milliseconds
     *     since epoch
     */
    @CurrentTimeMillisLong
    public long getEndTimeMillis() {
        return mEndTimeMillis;
    }

    public static final @NonNull Creator<AppUsageDuration> CREATOR =
            new Creator<AppUsageDuration>() {
                @Override
                public AppUsageDuration createFromParcel(@NonNull Parcel in) {
                    return new AppUsageDuration(in);
                }

                @Override
                public AppUsageDuration[] newArray(int size) {
                    return new AppUsageDuration[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString8(mPackageName);
        dest.writeLong(mTotalUsageDurationMillis);
        dest.writeLong(mStartTimeMillis);
        dest.writeLong(mEndTimeMillis);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AppUsageDuration)) {
            return false;
        }
        AppUsageDuration that = (AppUsageDuration) o;
        return mPackageName.equals(that.mPackageName)
                && mTotalUsageDurationMillis == that.mTotalUsageDurationMillis
                && mStartTimeMillis == that.mStartTimeMillis
                && mEndTimeMillis == that.mEndTimeMillis;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                mPackageName, mTotalUsageDurationMillis, mStartTimeMillis, mEndTimeMillis);
    }

    @Override
    public String toString() {
        return "AppUsageDuration{"
                + "packageName="
                + mPackageName
                + ", totalUsageDurationMillis="
                + mTotalUsageDurationMillis
                + ", startTimeMillis="
                + mStartTimeMillis
                + ", endTimeMillis="
                + mEndTimeMillis
                + '}';
    }

    private void validate(
            @NonNull String packageName,
            @DurationMillisLong long totalUsageDurationMillis,
            @CurrentTimeMillisLong long startTimeMillis,
            @CurrentTimeMillisLong long endTimeMillis) {
        Objects.requireNonNull(packageName, "packageName cannot be null");
        if (totalUsageDurationMillis < 0) {
            throw new IllegalArgumentException("totalUsageDurationMillis cannot be negative");
        }
        if (startTimeMillis > endTimeMillis || startTimeMillis < 0 || endTimeMillis < 0) {
            throw new IllegalArgumentException("invalid time range");
        }
    }
}
