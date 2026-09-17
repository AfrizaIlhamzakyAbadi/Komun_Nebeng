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

import android.accounts.Account;
import android.annotation.FlaggedApi;
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
 * Common request parameters for people inference requests, e.g. {@link ListTopPeopleRequest} and
 * {@link GetPeopleByIdRequest}.
 */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
public final class RequestParams implements Parcelable {
    @EntityId.Type @NonNull private final List<Integer> mTargetIdTypes;
    @NonNull private final List<String> mTargetLabels;
    @NonNull private final List<String> mMimeTypes;
    @Nullable private final Account mAccount;

    private RequestParams(@NonNull Builder builder) {
        checkArgument(!builder.mTargetIdTypes.isEmpty(), "targetIdTypes cannot be empty");
        mTargetIdTypes = Collections.unmodifiableList(builder.mTargetIdTypes);
        mTargetLabels = Collections.unmodifiableList(builder.mTargetLabels);
        mMimeTypes = Collections.unmodifiableList(builder.mMimeTypes);
        mAccount = builder.mAccount;
    }

    private RequestParams(@NonNull Parcel in) {
        mTargetIdTypes = ParcelUtils.readIntList(in);
        List<String> targetLabels = new ArrayList<>();
        in.readStringList(targetLabels);
        ParcelUtils.checkCollectionSize(targetLabels.size());
        mTargetLabels = Collections.unmodifiableList(targetLabels);
        List<String> mimeTypes = new ArrayList<>();
        in.readStringList(mimeTypes);
        ParcelUtils.checkCollectionSize(mimeTypes.size());
        mMimeTypes = Collections.unmodifiableList(mimeTypes);
        mAccount = in.readTypedObject(Account.CREATOR);
    }

    /**
     * Returns the target ID types, i.e. the types of {@link EntityId}s the results should be
     * returned with.
     */
    @EntityId.Type
    @NonNull
    public List<Integer> getTargetIdTypes() {
        return mTargetIdTypes;
    }

    /**
     * Returns the target labels. If not empty, the response will only contain {@link Entity}s
     * having one or more of the target labels.
     */
    @NonNull
    public List<String> getTargetLabels() {
        return mTargetLabels;
    }

    /**
     * Returns the MIME types of the content related to the request. This optional parameter
     * provides additional context for the inference, e.g. if a MIME type is the same as what has
     * been shared to person X often in the past, person X may be ranked higher.
     */
    @NonNull
    public List<String> getMimeTypes() {
        return mMimeTypes;
    }

    /**
     * Returns the {@link Account} from which the request is issued, or null if not set. This is
     * mainly used for filtering the results, e.g. limit results to contacts from the specified
     * account only. If no account is specified, no filtering will be applied.
     */
    @Nullable
    public Account getAccount() {
        return mAccount;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof RequestParams other) {
            return Objects.equals(mTargetIdTypes, other.mTargetIdTypes)
                    && Objects.equals(mTargetLabels, other.mTargetLabels)
                    && Objects.equals(mMimeTypes, other.mMimeTypes)
                    && Objects.equals(mAccount, other.mAccount);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mTargetIdTypes, mTargetLabels, mMimeTypes, mAccount);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        ParcelUtils.writeIntList(mTargetIdTypes, dest, flags);
        dest.writeStringList(mTargetLabels);
        dest.writeStringList(mMimeTypes);
        dest.writeTypedObject(mAccount, flags);
    }

    @NonNull
    public static final Parcelable.Creator<RequestParams> CREATOR =
            new Parcelable.Creator<RequestParams>() {
                @Override
                public RequestParams createFromParcel(@NonNull Parcel in) {
                    return new RequestParams(in);
                }

                @Override
                public RequestParams[] newArray(int size) {
                    return new RequestParams[size];
                }
            };

    /** Builder for {@link RequestParams}. */
    @SystemApi
    @FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
    public static final class Builder {
        @EntityId.Type @NonNull private List<Integer> mTargetIdTypes = Collections.emptyList();
        @NonNull private List<String> mTargetLabels = Collections.emptyList();
        @NonNull private List<String> mMimeTypes = Collections.emptyList();
        @Nullable private Account mAccount = null;

        /**
         * Sets the target ID types.
         *
         * @param targetIdTypes The target types of {@link EntityId}s.
         * @return The {@link Builder} for chaining.
         */
        @NonNull
        public Builder setTargetIdTypes(@EntityId.Type @NonNull List<Integer> targetIdTypes) {
            mTargetIdTypes = checkNotNull(targetIdTypes, "Cannot set null targetIdTypes");
            return this;
        }

        /**
         * Sets the target labels.
         *
         * @param targetLabels The target labels.
         * @return The {@link Builder} for chaining.
         */
        @NonNull
        public Builder setTargetLabels(@NonNull List<String> targetLabels) {
            mTargetLabels = checkNotNull(targetLabels, "Cannot set null targetLabels");
            return this;
        }

        /**
         * Sets the MIME types of the contents related to the request.
         *
         * @param mimeTypes The MIME types of the contents related to the request.
         * @return The {@link Builder} for chaining.
         */
        @NonNull
        public Builder setMimeTypes(@NonNull List<String> mimeTypes) {
            mMimeTypes = checkNotNull(mimeTypes, "Cannot set null mimeTypes");
            return this;
        }

        /**
         * Sets the {@link Account} from which the request is issued.
         *
         * @param account The {@link Account} from which the request is issued.
         * @return The {@link Builder} for chaining.
         */
        @NonNull
        public Builder setAccount(@Nullable Account account) {
            mAccount = account;
            return this;
        }

        /** Builds the {@link RequestParams}. */
        @NonNull
        public RequestParams build() {
            return new RequestParams(this);
        }
    }
}
