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

package android.content.pm;

import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.WindowMetrics;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.Objects;

/**
 * Memory budget information for an application.
 */
@Hide
public final class MemoryBudget implements Parcelable {
    public static final int STATE_FOREGROUND = 1;
    public static final int STATE_PERCEPTIBLE = 2;
    public static final int STATE_BACKGROUND = 3;

    @IntDef(prefix = { "STATE_" }, value = {
            STATE_FOREGROUND,
            STATE_PERCEPTIBLE,
            STATE_BACKGROUND
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface State {}

    // Upper limits on the values.
    public static final long MAX_MAXMB = 1024 * 1024L;
    public static final long MAX_ADDITIONAL_BYTES_PER_DISPLAY_PIXEL = 4096L;
    public static final long MAX_ADDITIONAL_MB_PER_DENSITY = 4096L;
    // Permit at most 100 budgets in a manifest, to protect against DoS attacks.
    public static final int MAX_BUDGETS = 100;
    // Permit at most 256 characters in a feature string, to protect against DoS attacks.
    public static final int MAX_FEATURE_LENGTH = 256;

    private static final long BYTES_PER_MB = 1024 * 1024L;

    private final long mMaxMb;
    private final @State int mState;
    // Additional memory needed for each display pixel.  This allows the budget to scale with
    // display size.
    private final long mAdditionalBytesPerDisplayPixel;
    // Additional memory needed per density level,
    // e.g. (N * { ldpi=0.75, mdpi=1.0, hdpi=1.5, xhdpi=2... })
    private final long mAdditionalMbPerDensity;
    // The feature to which the budget applies.  The feature may be an empty string, in which case
    // the object matches any feature, if the no more specific budget is in the manifest.
    private final @NonNull String mFeature;

    /**
     * Calculate the memory budget represented by this MemoryBudget in bytes. The caller is
     * responsible for providing a WindowMetrics that is representative of the app's expected UI
     * size (e.g. WindowManager.getCurrentWindowMetrics(), WindowManager.getMaximumWindowMetrics()).
     */
    public long calculateBudgetInBytes(@NonNull WindowMetrics wm) {
        long area = (long) wm.getBounds().width() * wm.getBounds().height();
        return mMaxMb * BYTES_PER_MB
                + mAdditionalBytesPerDisplayPixel * area
                + (long) ((double) mAdditionalMbPerDensity * wm.getDensity() * BYTES_PER_MB);
    }

    public long getMaxMb() {
        return mMaxMb;
    }

    public @State int getState() {
        return mState;
    }

    public long getAdditionalBytesPerDisplayPixel() {
        return mAdditionalBytesPerDisplayPixel;
    }

    public long getAdditionalMbPerDensity() {
        return mAdditionalMbPerDensity;
    }

    public @NonNull String getFeature() {
        return mFeature;
    }

    /**
     * Return the name of the state, for debug messages.
     */
    public static String stateString(int state) {
        return switch (state) {
            case STATE_FOREGROUND -> "foreground";
            case STATE_PERCEPTIBLE -> "perceptible";
            case STATE_BACKGROUND -> "background";
            default -> "unknown";
        };
    }

    private static boolean validState(int state) {
        return switch (state) {
            case STATE_FOREGROUND -> true;
            case STATE_PERCEPTIBLE -> true;
            case STATE_BACKGROUND -> true;
            default -> false;
        };
    }

    public MemoryBudget(long maxMb, @State int state) {
        this(maxMb, state, 0, 0, "");
    }

    public MemoryBudget(long maxMb, @State int state, long additionalBytesPerDisplayPixel,
            long additionalMbPerDensity) {
        this(maxMb, state, additionalBytesPerDisplayPixel, additionalMbPerDensity, "");
    }

    public MemoryBudget(long maxMb, @State int state, long additionalBytesPerDisplayPixel,
            long additionalMbPerDensity, @NonNull String feature) {
        this.mMaxMb = maxMb;
        this.mState = state;
        this.mAdditionalBytesPerDisplayPixel = additionalBytesPerDisplayPixel;
        this.mAdditionalMbPerDensity = additionalMbPerDensity;
        this.mFeature = Objects.requireNonNull(feature);
        validate();
    }

    public MemoryBudget(@NonNull MemoryBudget orig) {
        this.mMaxMb = orig.mMaxMb;
        this.mState = orig.mState;
        this.mAdditionalBytesPerDisplayPixel = orig.mAdditionalBytesPerDisplayPixel;
        this.mAdditionalMbPerDensity = orig.mAdditionalMbPerDensity;
        this.mFeature = orig.mFeature;
        validate();
    }

    private MemoryBudget(Parcel source) {
        mMaxMb = source.readLong();
        mState = source.readInt();
        mAdditionalBytesPerDisplayPixel = source.readLong();
        mAdditionalMbPerDensity = source.readLong();
        mFeature = Objects.requireNonNull(source.readString8());
        validate();
    }

    // Throw an exception if the constructed object is invalid.
    private void validate() {
        if (mMaxMb > MAX_MAXMB || mMaxMb <= 0) {
            throw new IllegalArgumentException("Budget maxMb invalid: " + mMaxMb);
        }
        if (!validState(mState)) {
            throw new IllegalArgumentException("Budget state is invalid: " + mState);
        }
        if (mAdditionalBytesPerDisplayPixel > MAX_ADDITIONAL_BYTES_PER_DISPLAY_PIXEL
                || mAdditionalBytesPerDisplayPixel < 0) {
            throw new IllegalArgumentException("Budget mAdditionalBytesPerDisplayPixel invalid: "
                    + mAdditionalBytesPerDisplayPixel);
        }
        if (mAdditionalMbPerDensity > MAX_ADDITIONAL_MB_PER_DENSITY
                || mAdditionalMbPerDensity < 0) {
            throw new IllegalArgumentException("Budget mAdditionalMbPerDensity invalid: "
                    + mAdditionalMbPerDensity);
        }
        if (mFeature == null) {
            throw new IllegalArgumentException("Budget feature must not be null");
        }
        if (mFeature.length() > MAX_FEATURE_LENGTH) {
            throw new IllegalArgumentException("Budget feature is too long");
        }
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mMaxMb);
        dest.writeInt(mState);
        dest.writeLong(mAdditionalBytesPerDisplayPixel);
        dest.writeLong(mAdditionalMbPerDensity);
        dest.writeString8(mFeature);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final @NonNull Parcelable.Creator<MemoryBudget> CREATOR =
            new Parcelable.Creator<MemoryBudget>() {
                @Override
                public MemoryBudget createFromParcel(Parcel source) {
                    return new MemoryBudget(source);
                }

                @Override
                public MemoryBudget[] newArray(int size) {
                    return new MemoryBudget[size];
                }
            };

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MemoryBudget that = (MemoryBudget) o;
        return mMaxMb == that.mMaxMb && mState == that.mState
                && mAdditionalBytesPerDisplayPixel == that.mAdditionalBytesPerDisplayPixel
                && mAdditionalMbPerDensity == that.mAdditionalMbPerDensity
                && Objects.equals(mFeature, that.mFeature);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(mMaxMb, mState, mAdditionalBytesPerDisplayPixel,
                mAdditionalMbPerDensity, mFeature);
    }

    @Override
    public String toString() {
        return "MemoryBudget{maxMb=" + mMaxMb + ", state=" + stateString(mState)
                + ", additionalBytesPerDisplayPixel=" + mAdditionalBytesPerDisplayPixel
                + ", additionalMbPerDensity=" + mAdditionalMbPerDensity
                + ", feature=\"" + mFeature + "\"}";
    }

    /**
     * This method adds a budget to a list of budgets.  The incoming budget replaces any existing
     * budgets with the same state and feature. Thus, if there are duplicate budgets in the
     * manifest, the last budget is the one that is saved.
     *
     * The method throws an exception if the list size is at or above MAX_BUDGETS.
     */
    public static void addBudget(List<MemoryBudget> budgetList, MemoryBudget budget) {
        budgetList.removeIf(b -> b.mState == budget.mState
                && java.util.Objects.equals(b.mFeature, budget.mFeature));
        if (budgetList.size() >= MAX_BUDGETS) {
            throw new IllegalArgumentException("Too many memory budgets, limit is " + MAX_BUDGETS);
        }
        budgetList.add(budget);
    }
}
