/**
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

package android.app.people;

import static com.android.internal.util.Preconditions.checkArgument;
import static com.android.internal.util.Preconditions.checkNotNull;
import static com.android.internal.util.Preconditions.checkStringNotEmpty;

import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SystemApi;
import android.app.people.flags.Flags;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Configures how a ranked list of {@link Entity} should be computed, including filters for
 * determining the eligible {@link Entity}s and scorers for calculating their scores, etc.
 *
 * <p>Note that only up to 2048 {@link FeatureSpec}s are supported per {@link RankingSpec}.
 */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
public final class RankingSpec implements Parcelable {
    @Nullable private final String mPredefinedSpecName;
    @NonNull private final Map<Integer, FeatureSpec> mFeatures;
    @NonNull private final List<HeuristicInferrer> mHeuristicInferrers;

    /**
     * Constructs a {@link RankingSpec} that points to a predefined spec by the given name. If a
     * custom spec is needed, use the {@link Builder} instead.
     *
     * @param predefinedSpecName The name of the predefined {@link RankingSpec}.
     */
    public RankingSpec(@NonNull String predefinedSpecName) {
        mPredefinedSpecName =
                checkStringNotEmpty(
                        predefinedSpecName, "predefinedSpecName cannot be null or empty");
        mFeatures = Collections.emptyMap();
        mHeuristicInferrers = Collections.emptyList();
    }

    private RankingSpec(@NonNull Builder builder) {
        mPredefinedSpecName = null;
        mFeatures =
                Collections.unmodifiableMap(
                        checkNotNull(builder.mFeatures, "features cannot be null"));
        mHeuristicInferrers =
                Collections.unmodifiableList(
                        checkNotNull(
                                builder.mHeuristicInferrers, "heuristicInferrers cannot be null"));
    }

    private RankingSpec(@NonNull Parcel in) {
        mPredefinedSpecName = in.readString8();
        mFeatures = ParcelUtils.readIntMap(in, /* depth= */ 0, FeatureSpec::new);
        mHeuristicInferrers = ParcelUtils.readTypedList(in, /* depth= */ 0, HeuristicInferrer::new);
    }

    /** Returns the name of the predefined {@link RankingSpec}, or null if not specified. */
    @Nullable
    public String getPredefinedSpecName() {
        return mPredefinedSpecName;
    }

    /** Returns the specifications of the features to be used in the {@link HeuristicInferrer}s. */
    @NonNull
    public Map<Integer, FeatureSpec> getFeatures() {
        return mFeatures;
    }

    /**
     * Returns the {@link HeuristicInferrer}s to be applied at inference time. The {@link
     * HeuristicInferrer}s are applied in the order as they appear in the list.
     */
    @NonNull
    public List<HeuristicInferrer> getHeuristicInferrers() {
        return mHeuristicInferrers;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof RankingSpec other) {
            return Objects.equals(mPredefinedSpecName, other.mPredefinedSpecName)
                    && Objects.equals(mFeatures, other.mFeatures)
                    && Objects.equals(mHeuristicInferrers, other.mHeuristicInferrers);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mPredefinedSpecName, mFeatures, mHeuristicInferrers);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString8(mPredefinedSpecName);
        ParcelUtils.writeIntMap(mFeatures, dest, flags);
        dest.writeTypedList(mHeuristicInferrers, flags);
    }

    @NonNull
    public static final Parcelable.Creator<RankingSpec> CREATOR =
            new Parcelable.Creator<RankingSpec>() {
                @Override
                public RankingSpec createFromParcel(@NonNull Parcel in) {
                    return new RankingSpec(in);
                }

                @Override
                public RankingSpec[] newArray(int size) {
                    return new RankingSpec[size];
                }
            };

    /** Builder for {@link RankingSpec}. */
    @SystemApi
    @FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
    public static final class Builder {
        @NonNull private Map<Integer, FeatureSpec> mFeatures = Collections.emptyMap();
        @NonNull private final ArrayList<HeuristicInferrer> mHeuristicInferrers = new ArrayList<>();

        /**
         * Sets the specifications of the features to be used in the {@link HeuristicInferrer}s.
         *
         * @param features The specifications of the features to be used in the {@link
         *     HeuristicInferrer}s.
         * @return The {@link Builder} for chaining.
         */
        @NonNull
        public Builder setFeatures(@NonNull Map<Integer, FeatureSpec> features) {
            mFeatures = features;
            return this;
        }

        /**
         * Adds a {@link HeuristicInferrer} to be applied at inference time.
         *
         * @param heuristicInferrers The {@link HeuristicInferrer}s to add.
         * @return The {@link Builder} for chaining.
         */
        @NonNull
        public Builder addHeuristicInferrers(@NonNull HeuristicInferrer... heuristicInferrers) {
            for (HeuristicInferrer inferrer : heuristicInferrers) {
                mHeuristicInferrers.add(checkNotNull(inferrer, "Cannot add null inferrer"));
            }
            return this;
        }

        /** Builds a {@link RankingSpec}. */
        @NonNull
        public RankingSpec build() {
            return new RankingSpec(this);
        }
    }
}
