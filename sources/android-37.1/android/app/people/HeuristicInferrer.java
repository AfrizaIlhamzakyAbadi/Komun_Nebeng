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

import static com.android.internal.util.Preconditions.checkNotNull;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SystemApi;
import android.app.people.flags.Flags;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Configures the heuristics for determining the set of desired {@link Entity}s (filtering) and how
 * each {@link Entity} should be scored (scoring).
 */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
public final class HeuristicInferrer implements Parcelable {
    @NonNull private final List<Expression> mFilters;
    @NonNull private final List<Expression> mScorers;

    private HeuristicInferrer(@NonNull Builder builder) {
        mFilters =
                Collections.unmodifiableList(
                        checkNotNull(builder.mFilters, "filters cannot be null"));
        mScorers =
                Collections.unmodifiableList(
                        checkNotNull(builder.mScorers, "scorers cannot be null"));
    }

    /**
     * Creates a {@link HeuristicInferrer} from the given parcel with recursion depth check.
     *
     * @param in The parcel to read from.
     * @param depth The current recursion depth.
     */
    @Hide
    public HeuristicInferrer(@NonNull Parcel in, int depth) {
        depth = ParcelUtils.checkRecursionDepth(depth);
        mFilters = ParcelUtils.readTypedList(in, depth, Expression::new);
        mScorers = ParcelUtils.readTypedList(in, depth, Expression::new);
    }

    /**
     * Returns the filters to be applied at inference time. The filters are applied in the order as
     * they appear in the list. Only the entities that pass all filters will be scored.
     */
    @NonNull
    public List<Expression> getFilters() {
        return mFilters;
    }

    /**
     * Returns the scorers to be applied at inference time. The scorers are applied in the order as
     * they appear in the list.
     */
    @NonNull
    public List<Expression> getScorers() {
        return mScorers;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof HeuristicInferrer other) {
            return Objects.equals(mFilters, other.mFilters)
                    && Objects.equals(mScorers, other.mScorers);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mFilters, mScorers);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedList(mFilters, flags);
        dest.writeTypedList(mScorers, flags);
    }

    @NonNull
    public static final Parcelable.Creator<HeuristicInferrer> CREATOR =
            new Parcelable.Creator<HeuristicInferrer>() {
                @Override
                public HeuristicInferrer createFromParcel(@NonNull Parcel in) {
                    return new HeuristicInferrer(in, /* depth= */ 0);
                }

                @Override
                public HeuristicInferrer[] newArray(int size) {
                    return new HeuristicInferrer[size];
                }
            };

    /** Builder for {@link HeuristicInferrer}. */
    @SystemApi
    @FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
    public static final class Builder {
        @NonNull private final ArrayList<Expression> mFilters = new ArrayList<>();
        @NonNull private final ArrayList<Expression> mScorers = new ArrayList<>();

        /**
         * Adds a filter to be applied at inference time.
         *
         * @param filters The filters to be added.
         * @return The {@link Builder} for chaining.
         */
        @NonNull
        public Builder addFilters(@NonNull Expression... filters) {
            for (Expression filter : filters) {
                mFilters.add(checkNotNull(filter, "Cannot add null filter"));
            }
            return this;
        }

        /**
         * Adds a scorer to be applied at inference time.
         *
         * @param scorers The scorers to be added.
         * @return The {@link Builder} for chaining.
         */
        @NonNull
        public Builder addScorers(@NonNull Expression... scorers) {
            for (Expression scorer : scorers) {
                mScorers.add(checkNotNull(scorer, "Cannot add null scorer"));
            }
            return this;
        }

        /** Builds a {@link HeuristicInferrer}. */
        @NonNull
        public HeuristicInferrer build() {
            return new HeuristicInferrer(this);
        }
    }
}
