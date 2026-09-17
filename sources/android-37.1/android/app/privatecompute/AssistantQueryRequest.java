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

import android.service.personalcontext.hint.ContextHint;

import java.util.Objects;

/**
 * Request object specifically for Assistant Query Egress operations. Binds this request to the
 * {@link AssistantQueryResponse} type.
 */
@FlaggedApi(android.app.privatecompute.flags.Flags.FLAG_ENABLE_PCC_ASSISTANT_EGRESS)
public final class AssistantQueryRequest extends EgressRequest<AssistantQueryResponse>
        implements Parcelable {

    /** Payload containing the raw query details to be routed to the non-PCC assistant process. */
    public static final class Query implements Parcelable {
        /** The identifier for the query. */
        @Nullable
        private final String mQueryId;

        /** The raw query string. */
        private final String mQueryString;

        /** The session identifier. */
        private final String mSessionId;

        private Query(@NonNull Builder builder) {
            this.mQueryId = builder.mQueryId;
            this.mQueryString = builder.mQueryString;
            this.mSessionId = builder.mSessionId;
        }

        private Query(@NonNull Parcel in) {
            this.mQueryId = in.readString8();
            this.mQueryString = Objects.requireNonNull(in.readString8());
            this.mSessionId = Objects.requireNonNull(in.readString8());
        }

        /** Returns the identifier for the query. */
        @Nullable
        public String getQueryId() {
            return mQueryId;
        }

        /** Returns the raw query string. */
        @NonNull
        public String getQueryString() {
            return mQueryString;
        }

        // TODO(b/523297495): Update documentation
        /**
         * Returns the session identifier of the hint.
         *
         * <p>Session Id is provided by the {@link ContextHint} and represents a unique identifier
         * for the {@link ContextHint}.
         */
        @NonNull
        public String getSessionId() {
            return mSessionId;
        }

        /** {@inheritDoc} */
        @Override
        public int describeContents() {
            return 0;
        }

        /** {@inheritDoc} */
        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeString8(mQueryId);
            dest.writeString8(mQueryString);
            dest.writeString8(mSessionId);
        }

        /** Creator for {@link Query} instance from a {@link Parcel}. */
        @NonNull
        public static final Parcelable.Creator<Query> CREATOR =
                new Parcelable.Creator<Query>() {
                    @Override
                    public Query createFromParcel(Parcel in) {
                        return new Query(in);
                    }

                    @Override
                    public Query[] newArray(int size) {
                        return new Query[size];
                    }
                };

        /** Builder for {@link Query}. */
        public static final class Builder {
            @Nullable private String mQueryId = null;
            private final String mQueryString;
            private final String mSessionId;

            // TODO(b/523297495): Update documentation
            /**
             * Creates a new Builder for a Query.
             *
             * @param queryString The raw string of the query.
             * @param sessionId The session identifier of the hint.
             */
            public Builder(
                    @NonNull String queryString,
                    @NonNull String sessionId) {
                this.mQueryString = Objects.requireNonNull(queryString);
                this.mSessionId = Objects.requireNonNull(sessionId);
            }

            /**
             * Sets the identifier for the query. This is meant for developer use to link
             * the request to the response.
             *
             * @param queryId The identifier for the query.
             */
            @NonNull
            public Builder setQueryId(@Nullable String queryId) {
                this.mQueryId = queryId;
                return this;
            }

            /** Builds the {@link Query} instance. */
            @NonNull
            public Query build() {
                return new Query(this);
            }
        }
    }

    /** The raw assistant query payload. */
    private final Query mQuery;

    private AssistantQueryRequest(@NonNull Builder builder) {
        super(EgressRequest.USE_CASE_QUERY_ASSISTANT);
        this.mQuery = builder.mQuery;
    }

    AssistantQueryRequest(@NonNull Parcel in) {
        super(EgressRequest.USE_CASE_QUERY_ASSISTANT);
        this.mQuery = Objects.requireNonNull(in.readTypedObject(Query.CREATOR));
    }

    /**
     * Returns the query payload associated with this request.
     *
     * @return The {@link Query} payload.
     */
    @NonNull
    public Query getQuery() {
        return mQuery;
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
        dest.writeTypedObject(mQuery, flags);
    }

    /** Creator for {@link AssistantQueryRequest} instance. */
    @NonNull
    public static final Parcelable.Creator<AssistantQueryRequest> CREATOR =
            new Parcelable.Creator<AssistantQueryRequest>() {
                @Override
                public AssistantQueryRequest createFromParcel(Parcel in) {
                    in.readInt(); // skip useCase token
                    return new AssistantQueryRequest(in);
                }

                @Override
                public AssistantQueryRequest[] newArray(int size) {
                    return new AssistantQueryRequest[size];
                }
            };

    /** Builder for {@link AssistantQueryRequest}. */
    public static final class Builder {
        private final Query mQuery;

        /**
         * Creates a new Builder for AssistantQueryRequest.
         *
         * @param query The {@link Query} payload to be included.
         */
        public Builder(@NonNull Query query) {
            this.mQuery = Objects.requireNonNull(query);
        }

        /** Builds the {@link AssistantQueryRequest} instance. */
        @NonNull
        public AssistantQueryRequest build() {
            return new AssistantQueryRequest(this);
        }
    }
}
