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
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Exception thrown when an egress request fails inside the PCC sandbox. Contains the {@link
 * StatusCode} detailing the failure.
 */
@FlaggedApi(android.app.privatecompute.flags.Flags.FLAG_ENABLE_PCC_ASSISTANT_EGRESS)
public final class PccEgressException extends Exception implements Parcelable {

    @Hide
    @IntDef(
            prefix = {"STATUS_"},
            value = {
                STATUS_FAILURE_MISSING_CONSENT,
                STATUS_FAILURE_CALLER_NOT_PCC,
                STATUS_FAILURE_REQUEST_TOO_LARGE,
                STATUS_FAILURE_UNABLE_TO_DELIVER_REQUEST,
                STATUS_FAILURE_INDICATOR_UNAVAILABLE,
                STATUS_FAILURE_NO_RESPONSE_FROM_PROXY,
                STATUS_FAILURE_INVALID_REQUEST,
                STATUS_FAILURE_UNKNOWN
            })
    @Retention(RetentionPolicy.SOURCE)
    public @interface StatusCode {}

    /** Indicates that the request failed because user consent is missing. */
    public static final int STATUS_FAILURE_MISSING_CONSENT = 1;

    /**
     * Indicates that the request failed because the caller is not a valid Private Compute Core
     * (PCC) process.
     */
    public static final int STATUS_FAILURE_CALLER_NOT_PCC = 2;

    /** Indicates that the request size exceeds the maximum allowed limit. */
    public static final int STATUS_FAILURE_REQUEST_TOO_LARGE = 3;

    /** Indicates that the request failed because the system was unable to deliver the egress. */
    public static final int STATUS_FAILURE_UNABLE_TO_DELIVER_REQUEST = 4;

    /** Indicates that the request failed because system indicator is unavailable. */
    public static final int STATUS_FAILURE_INDICATOR_UNAVAILABLE = 5;

    /** Indicates that the request failed because the proxy service did not respond or timed out. */
    public static final int STATUS_FAILURE_NO_RESPONSE_FROM_PROXY = 6;

    /** Indicates that the request failed because the request arguments were invalid. */
    public static final int STATUS_FAILURE_INVALID_REQUEST = 7;

    /** Indicates an unspecified or unexpected failure. */
    public static final int STATUS_FAILURE_UNKNOWN = -1;

    private final @StatusCode int mStatusCode;

    /**
     * Creates a new PccEgressException with a given {@link StatusCode}.
     *
     * @param statusCode The {@link StatusCode} of the egress failure.
     */
    public PccEgressException(@StatusCode int statusCode) {
        super("Egress failed with status: " + statusCode);
        mStatusCode = statusCode;
    }

    /**
     * Creates a new PccEgressException with a given {@link StatusCode} and detailed error message.
     *
     * @param statusCode The {@link StatusCode} of the egress failure.
     * @param message The detailed error message description.
     */
    public PccEgressException(@StatusCode int statusCode, @Nullable String message) {
        super(message);
        mStatusCode = statusCode;
    }

    private PccEgressException(Parcel in) {
        super(in.readString());
        mStatusCode = in.readInt();
    }

    /**
     * Returns the status code detailing the egress failure.
     *
     * @return The {@link StatusCode} of the egress failure.
     */
    @StatusCode
    public int getStatusCode() {
        return mStatusCode;
    }

    /** {@inheritDoc} */
    @Override
    public int describeContents() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(getMessage());
        dest.writeInt(mStatusCode);
    }

    /** Standard parcelable creator for {@link PccEgressException}. */
    @NonNull
    public static final Parcelable.Creator<PccEgressException> CREATOR =
            new Parcelable.Creator<PccEgressException>() {
                @Override
                public PccEgressException createFromParcel(Parcel in) {
                    return new PccEgressException(in);
                }

                @Override
                public PccEgressException[] newArray(int size) {
                    return new PccEgressException[size];
                }
            };
}
