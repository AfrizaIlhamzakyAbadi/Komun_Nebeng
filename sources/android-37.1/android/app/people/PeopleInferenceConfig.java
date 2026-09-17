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
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SystemApi;
import android.app.people.flags.Flags;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * Configures how the inference should be performed. For example, when {@link RankingSpec} is
 * specified, it configures the ranking function for filtering and scoring each candidate (e.g.
 * contact) to produce a ranked list of {@link Entity} (e.g. top 10 most relevant contacts).
 */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
public final class PeopleInferenceConfig implements Parcelable {
    @NonNull private final String mSessionId;
    @Nullable private final RankingSpec mRankingSpec;

    private PeopleInferenceConfig(@NonNull Builder builder) {
        mSessionId = builder.mSessionId;
        mRankingSpec = builder.mRankingSpec;
    }

    private PeopleInferenceConfig(@NonNull Parcel in) {
        mSessionId = checkNotNull(in.readString8(), "sessionId cannot be null");
        mRankingSpec = in.readTypedObject(RankingSpec.CREATOR);
    }

    /** Returns the inference session ID. */
    @NonNull
    public String getSessionId() {
        return mSessionId;
    }

    /** Returns the {@link RankingSpec}. */
    @Nullable
    public RankingSpec getRankingSpec() {
        return mRankingSpec;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof PeopleInferenceConfig other) {
            return Objects.equals(mSessionId, other.mSessionId)
                    && Objects.equals(mRankingSpec, other.mRankingSpec);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mSessionId, mRankingSpec);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString8(mSessionId);
        dest.writeTypedObject(mRankingSpec, flags);
    }

    @NonNull
    public static final Parcelable.Creator<PeopleInferenceConfig> CREATOR =
            new Parcelable.Creator<PeopleInferenceConfig>() {
                @Override
                public PeopleInferenceConfig createFromParcel(@NonNull Parcel in) {
                    return new PeopleInferenceConfig(in);
                }

                @Override
                public PeopleInferenceConfig[] newArray(int size) {
                    return new PeopleInferenceConfig[size];
                }
            };

    /**
     * Builder for {@link PeopleInferenceConfig}.
     */
    @SystemApi
    @FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
    public static final class Builder {
        @NonNull private final String mSessionId;
        @Nullable private RankingSpec mRankingSpec = null;

        /**
         * Constructs a new {@link Builder} with the given session ID.
         *
         * @param sessionId The inference session ID.
         */
        public Builder(@NonNull String sessionId) {
            mSessionId = checkNotNull(sessionId, "sessionId cannot be null");
        }

        /**
         * Sets the {@link RankingSpec}.
         *
         * @param rankingSpec The {@link RankingSpec} to set.
         * @return The {@link Builder} for chaining.
         */
        @NonNull
        public Builder setRankingSpec(@Nullable RankingSpec rankingSpec) {
            mRankingSpec = rankingSpec;
            return this;
        }

        /** Builds a {@link PeopleInferenceConfig}. */
        @NonNull
        public PeopleInferenceConfig build() {
            return new PeopleInferenceConfig(this);
        }
    }
}
