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

package android.app.privatecompute;

import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * Specialized response for Assistant Query use-cases. Includes the specific query ID and the
 * generated response payload.
 */
@FlaggedApi(android.app.privatecompute.flags.Flags.FLAG_ENABLE_PCC_ASSISTANT_EGRESS)
public final class AssistantQueryResponse extends EgressResponse implements Parcelable {

    /** The unique identifier for the query. */
    private final String mQueryId;

    /** The raw query response string. */
    private final String mQueryResponse;

    private AssistantQueryResponse(@NonNull Builder builder) {
        super(EgressResponse.TYPE_ASSISTANT_QUERY);
        this.mQueryId = builder.mQueryId;
        this.mQueryResponse = builder.mQueryResponse;
    }

    AssistantQueryResponse(@NonNull Parcel in) {
        super(EgressResponse.TYPE_ASSISTANT_QUERY);
        this.mQueryId = in.readString8();
        this.mQueryResponse = in.readString8();
    }

    /**
     * Returns the unique identifier of the query associated with this egress response.
     *
     * @return A string representing the unique query ID.
     */
    @NonNull
    public String getQueryId() {
        return mQueryId;
    }

    /**
     * Returns the raw query response payload generated for the query.
     *
     * <p>May be {@code null} if the response is empty.
     *
     * @return The raw query response string, or {@code null} if unavailable.
     */
    @Nullable
    public String getQueryResponse() {
        return mQueryResponse;
    }

    /** {@inheritDoc} */
    @Override
    public int describeContents() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString8(mQueryId);
        dest.writeString8(mQueryResponse);
    }

    /** Creator for {@link AssistantQueryResponse} instance. */
    @NonNull
    public static final Parcelable.Creator<AssistantQueryResponse> CREATOR =
            new Parcelable.Creator<AssistantQueryResponse>() {
                @Override
                public AssistantQueryResponse createFromParcel(Parcel in) {
                    in.readInt(); // skip type token
                    return new AssistantQueryResponse(in);
                }

                @Override
                public AssistantQueryResponse[] newArray(int size) {
                    return new AssistantQueryResponse[size];
                }
            };

    /** Builder for {@link AssistantQueryResponse}. */
    public static final class Builder {
        /** The unique identifier for the query. */
        private final String mQueryId;

        /** The raw query response payload. */
        private String mQueryResponse;

        /**
         * Creates a new Builder for an AssistantQueryResponse.
         *
         * @param queryId The ID of the query this response corresponds to.
         */
        public Builder(@NonNull String queryId) {
            this.mQueryId = Objects.requireNonNull(queryId);
        }

        /**
         * Sets the optional string response generated for the query.
         *
         * @param queryResponse The response string generated.
         * @return This Builder instance.
         */
        @NonNull
        public Builder setQueryResponse(@Nullable String queryResponse) {
            this.mQueryResponse = queryResponse;
            return this;
        }

        /**
         * Builds the {@link AssistantQueryResponse} instance.
         *
         * @return The newly built AssistantQueryResponse.
         */
        @NonNull
        public AssistantQueryResponse build() {
            return new AssistantQueryResponse(this);
        }
    }
}
