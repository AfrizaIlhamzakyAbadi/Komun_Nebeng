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
import android.annotation.TestApi;
import android.app.people.flags.Flags;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Response of the list-top-people method with the list of {@link Entity}s ranked by relevance. See
 * the javadoc on {@link ListTopPeopleRequest} for more details.
 */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
public final class ListTopPeopleResponse implements Parcelable {
    private final long mRequestTimeMillis;
    @NonNull private final List<Entity> mResults;

    private ListTopPeopleResponse(@NonNull Builder builder) {
        mRequestTimeMillis = builder.mRequestTimeMillis;
        mResults = Collections.unmodifiableList(builder.mResults);
    }

    private ListTopPeopleResponse(@NonNull Parcel in) {
        mRequestTimeMillis = in.readLong();
        List<Entity> results = new ArrayList<>();
        in.readTypedList(results, Entity.CREATOR);
        mResults = Collections.unmodifiableList(results);
    }

    /** Returns the timestamp in milliseconds when the request is issued. */
    public long getRequestTimeMillis() {
        return mRequestTimeMillis;
    }

    /** Returns the list of {@link Entity}s ranked by relevance. */
    @NonNull
    public List<Entity> getResults() {
        return mResults;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof ListTopPeopleResponse other) {
            return mRequestTimeMillis == other.mRequestTimeMillis
                    && Objects.equals(mResults, other.mResults);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mRequestTimeMillis, mResults);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(mRequestTimeMillis);
        dest.writeTypedList(mResults);
    }

    @NonNull
    public static final Parcelable.Creator<ListTopPeopleResponse> CREATOR =
            new Parcelable.Creator<ListTopPeopleResponse>() {
                @Override
                public ListTopPeopleResponse createFromParcel(@NonNull Parcel in) {
                    return new ListTopPeopleResponse(in);
                }

                @Override
                public ListTopPeopleResponse[] newArray(int size) {
                    return new ListTopPeopleResponse[size];
                }
            };

    /** Builder for {@link ListTopPeopleResponse}. */
    @TestApi
    public static final class Builder {
        private long mRequestTimeMillis = 0;
        @NonNull private List<Entity> mResults = Collections.emptyList();

        /**
         * Sets the request timestamp in milliseconds.
         *
         * @param requestTimeMillis The request timestamp in milliseconds.
         * @return The {@link Builder} for chaining.
         */
        @NonNull
        public Builder setRequestTimeMillis(long requestTimeMillis) {
            mRequestTimeMillis = requestTimeMillis;
            return this;
        }

        /**
         * Sets the ranked list of {@link Entity}s.
         *
         * @param results The ranked list of {@link Entity}s.
         * @return The {@link Builder} for chaining.
         */
        @NonNull
        public Builder setResults(@NonNull List<Entity> results) {
            mResults = checkNotNull(results, "Cannot set null results");
            return this;
        }

        /** Builds the {@link ListTopPeopleResponse}. */
        @NonNull
        public ListTopPeopleResponse build() {
            return new ListTopPeopleResponse(this);
        }
    }
}
