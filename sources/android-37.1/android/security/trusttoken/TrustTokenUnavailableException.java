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

package android.security.trusttoken;

import android.annotation.Hide;
import android.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * The exception is thrown when the TrustToken service is temporarily unavailable.
 */
@Hide
public class TrustTokenUnavailableException extends IllegalStateException {

    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = "ERROR_",
            value = {
                ERROR_NO_VALID_ANCHOR,
                ERROR_TOKEN_EXHAUSTED,
                ERROR_INVALID_TOKENS,
                ERROR_ATTESTATION,
                ERROR_MASTER_KEY,
                ERROR_NO_PROVIDER,
                ERROR_BOOT_NOT_COMPLETE
            })
    public @interface ErrorCode {}

    public static final int ERROR_NO_VALID_ANCHOR = 1;
    public static final int ERROR_TOKEN_EXHAUSTED = 2;
    public static final int ERROR_INVALID_TOKENS = 3;
    public static final int ERROR_ATTESTATION = 4;
    public static final int ERROR_MASTER_KEY = 5;
    public static final int ERROR_NO_PROVIDER = 6;
    public static final int ERROR_BOOT_NOT_COMPLETE = 7;

    private final @ErrorCode int mErrorCode;

    public TrustTokenUnavailableException(@ErrorCode int errorCode) {
        super();
        mErrorCode = errorCode;
    }

    public TrustTokenUnavailableException(@ErrorCode int errorCode, String message) {
        super(message);
        mErrorCode = errorCode;
    }

    public TrustTokenUnavailableException(
            @ErrorCode int errorCode, String message, Throwable cause) {
        super(message, cause);
        mErrorCode = errorCode;
    }

    public @ErrorCode int getErrorCode() {
        return mErrorCode;
    }
}
