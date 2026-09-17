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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Request to the get-people-by-id method for looking up {@link Entity}s by {@link EntityId}s, but
 * returned with the ID types as specified in the request. For example, when the caller is looking
 * up for phone numbers of contact A, returns a phone {@link Entity} per phone number associated
 * with the contact.
 *
 * <p>Note that a single request only allows up to 2048 keys. If more keys are needed, consider
 * splitting to multiple requests or using {@link ListTopPeopleRequest} instead.
 */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
public final class GetPeopleByIdRequest implements Parcelable {
    @NonNull private final RequestParams mParams;
    @NonNull private final List<EntityId> mKeys;

    private GetPeopleByIdRequest(@NonNull Builder builder) {
        mParams = builder.mParams;
        mKeys = builder.mKeys;
    }

    private GetPeopleByIdRequest(@NonNull Parcel in) {
        mParams = checkNotNull(in.readTypedObject(RequestParams.CREATOR));
        mKeys =
                ParcelUtils.readTypedList(
                        in, /* depth= */ 0, (p, d) -> EntityId.CREATOR.createFromParcel(p));
    }

    /** Returns the common {@link RequestParams} of the request. */
    @NonNull
    public RequestParams getParams() {
        return mParams;
    }

    /** Returns the lookup keys represented as {@link EntityId}s. */
    @NonNull
    public List<EntityId> getKeys() {
        return mKeys;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof GetPeopleByIdRequest other) {
            return Objects.equals(mParams, other.mParams) && Objects.equals(mKeys, other.mKeys);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mParams, mKeys);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedObject(mParams, flags);
        dest.writeTypedList(mKeys);
    }

    @NonNull
    public static final Parcelable.Creator<GetPeopleByIdRequest> CREATOR =
            new Parcelable.Creator<GetPeopleByIdRequest>() {
                @Override
                public GetPeopleByIdRequest createFromParcel(@NonNull Parcel in) {
                    return new GetPeopleByIdRequest(in);
                }

                @Override
                public GetPeopleByIdRequest[] newArray(int size) {
                    return new GetPeopleByIdRequest[size];
                }
            };

    /** Builder for {@link GetPeopleByIdRequest}. */
    @SystemApi
    @FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
    public static final class Builder {
        @NonNull private final RequestParams mParams;
        @NonNull private List<EntityId> mKeys = Collections.emptyList();

        /**
         * Creates a new {@link Builder} for constructing a {@link GetPeopleByIdRequest}.
         *
         * @param params The common {@link RequestParams} of the request.
         */
        public Builder(@NonNull RequestParams params) {
            mParams = checkNotNull(params, "Cannot set null params");
        }

        /**
         * Sets the keys to look up.
         *
         * @param keys The lookup keys represented as {@link EntityId}s.
         * @return The {@link Builder} for chaining.
         */
        @NonNull
        public Builder setKeys(@NonNull List<EntityId> keys) {
            mKeys = checkNotNull(keys, "Cannot set null keys");
            return this;
        }

        /** Builds the {@link GetPeopleByIdRequest}. */
        @NonNull
        public GetPeopleByIdRequest build() {
            return new GetPeopleByIdRequest(this);
        }
    }
}
