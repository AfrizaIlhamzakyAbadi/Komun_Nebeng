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

import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SystemApi;
import android.app.people.flags.Flags;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * Request to the list-top-people method for retrieving a list of {@link Entity}s ranked by
 * relevance. The relevance is measured by the {@link RankingSpec} specified in the {@link
 * PeopleInferenceConfig} when creating this session.
 *
 * <p>Note that the term "people" here is confined by the ID space. For example, if only {@link
 * EntityId#TYPE_CONTACT_ID} is requested, only the top contacts will be returned. If you need top
 * people across ID spaces (e.g. top people among contacts and shortcuts), include all relevant ID
 * types in {@link RequestParams#getTargetIdTypes}.
 */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
public final class ListTopPeopleRequest implements Parcelable {
    @NonNull private final RequestParams mParams;
    private final int mLimit;

    private ListTopPeopleRequest(@NonNull Builder builder) {
        mParams = builder.mParams;
        mLimit = builder.mLimit;
    }

    private ListTopPeopleRequest(@NonNull Parcel in) {
        mParams = checkNotNull(in.readTypedObject(RequestParams.CREATOR));
        int limit = in.readInt();
        checkArgument(limit > 0, "limit must be positive");
        mLimit = limit;
    }

    /** Returns the common {@link RequestParams} of the request. */
    @NonNull
    public RequestParams getParams() {
        return mParams;
    }

    /** Returns the maximum number of result {@link Entity}s to return. */
    public int getLimit() {
        return mLimit;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof ListTopPeopleRequest other) {
            return Objects.equals(mParams, other.mParams) && mLimit == other.mLimit;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mParams, mLimit);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedObject(mParams, flags);
        dest.writeInt(mLimit);
    }

    @NonNull
    public static final Parcelable.Creator<ListTopPeopleRequest> CREATOR =
            new Parcelable.Creator<ListTopPeopleRequest>() {
                @Override
                public ListTopPeopleRequest createFromParcel(@NonNull Parcel in) {
                    return new ListTopPeopleRequest(in);
                }

                @Override
                public ListTopPeopleRequest[] newArray(int size) {
                    return new ListTopPeopleRequest[size];
                }
            };

    /** Builder for {@link ListTopPeopleRequest}. */
    @SystemApi
    @FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
    public static final class Builder {
        @NonNull private final RequestParams mParams;
        private int mLimit = 0;

        /**
         * Creates a new {@link Builder} for constructing a {@link ListTopPeopleRequest}.
         *
         * @param params The common {@link RequestParams} of the request.
         */
        public Builder(@NonNull RequestParams params) {
            mParams = checkNotNull(params, "Cannot set null params");
        }

        /**
         * Sets the maximum number of result {@link Entity}s to return.
         *
         * @param limit The maximum number of result {@link Entity}s to return.
         * @return The {@link Builder} for chaining.
         */
        @NonNull
        public Builder setLimit(int limit) {
            checkArgument(limit > 0, "limit must be positive");
            mLimit = limit;
            return this;
        }

        /** Builds the {@link ListTopPeopleRequest}. */
        @NonNull
        public ListTopPeopleRequest build() {
            return new ListTopPeopleRequest(this);
        }
    }
}
