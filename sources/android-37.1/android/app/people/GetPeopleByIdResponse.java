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
import android.util.ArrayMap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Response of the get-people-by-id method with the lookup results. */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
public final class GetPeopleByIdResponse implements Parcelable {
    private final long mRequestTimeMillis;
    @NonNull private final Map<EntityId, List<Entity>> mResults;

    private GetPeopleByIdResponse(@NonNull Builder builder) {
        mRequestTimeMillis = builder.mRequestTimeMillis;
        mResults = Collections.unmodifiableMap(builder.mResults);
    }

    private GetPeopleByIdResponse(@NonNull Parcel in) {
        mRequestTimeMillis = in.readLong();
        int size = in.readInt();
        if (size <= 0) {
            mResults = Collections.emptyMap();
            return;
        }
        Map<EntityId, List<Entity>> results = new ArrayMap<>(size);
        for (int i = 0; i < size; i++) {
            EntityId key = in.readTypedObject(EntityId.CREATOR);
            List<Entity> values = new ArrayList<>();
            in.readTypedList(values, Entity.CREATOR);
            results.put(key, Collections.unmodifiableList(values));
        }
        mResults = Collections.unmodifiableMap(results);
    }

    /** Returns the timestamp in milliseconds when the request was issued. */
    public long getRequestTimeMillis() {
        return mRequestTimeMillis;
    }

    /**
     * Returns the map of a lookup key {@link EntityId} to a list of result {@link Entity}s. If no
     * result for a lookup key is found, the key won't be included in the map.
     */
    @NonNull
    public Map<EntityId, List<Entity>> getResults() {
        return mResults;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof GetPeopleByIdResponse other) {
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
        dest.writeInt(mResults.size());
        for (Map.Entry<EntityId, List<Entity>> entry : mResults.entrySet()) {
            dest.writeTypedObject(entry.getKey(), flags);
            dest.writeTypedList(entry.getValue());
        }
    }

    @NonNull
    public static final Parcelable.Creator<GetPeopleByIdResponse> CREATOR =
            new Parcelable.Creator<GetPeopleByIdResponse>() {
                @Override
                public GetPeopleByIdResponse createFromParcel(@NonNull Parcel in) {
                    return new GetPeopleByIdResponse(in);
                }

                @Override
                public GetPeopleByIdResponse[] newArray(int size) {
                    return new GetPeopleByIdResponse[size];
                }
            };

    /** Builder for {@link GetPeopleByIdResponse}. */
    @TestApi
    public static final class Builder {
        private long mRequestTimeMillis = 0;
        @NonNull private Map<EntityId, List<Entity>> mResults = Collections.emptyMap();

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
         * Sets the lookup results.
         *
         * @param results The map of a lookup key {@link EntityId} to a list of result {@link
         *     Entity}s.
         * @return The {@link Builder} for chaining.
         */
        @NonNull
        public Builder setResults(@NonNull Map<EntityId, List<Entity>> results) {
            mResults = checkNotNull(results, "Cannot set null results");
            return this;
        }

        /** Builds the {@link GetPeopleByIdResponse}. */
        @NonNull
        public GetPeopleByIdResponse build() {
            return new GetPeopleByIdResponse(this);
        }
    }
}
