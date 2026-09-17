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

package android.security.authenticationpolicy;

import static android.hardware.biometrics.Flags.FLAG_AGENT_AUTH_XDEVICE_TOKEN;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.SystemApi;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Exception thrown when cross-device authentication fails. */
@SystemApi
@FlaggedApi(FLAG_AGENT_AUTH_XDEVICE_TOKEN)
public final class CrossDeviceAuthenticationException extends Exception {

    @Hide
    @IntDef(prefix = {"ERROR_"}, value = {
            ERROR_UNKNOWN,
            ERROR_UNAUTHORIZED,
            ERROR_DECRYPTION_FAILED,
            ERROR_INVALID_PACKAGE,
            ERROR_INVALID_TIMESTAMP,
            ERROR_SERIALIZATION_FAILED,
            ERROR_NOT_IN_FOREGROUND,

    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ErrorCode {
    }

    /** Unknown error occurred. */
    public static final int ERROR_UNKNOWN = 0;

    /**
     * The request is not authorized.
     *
     * This error indicates that the user must take some action on the source device before
     * the request can be fulfilled. The required conditions vary by form factor, but typically
     * require that the has either unlocked the source device directly or has an an active
     * connection to another trusted device. See the associated message for more details.
     *
     * A more specific error code from this class will be returned instead this error for
     * common error conditions that do not vary.
     */
    public static final int ERROR_UNAUTHORIZED = 1;

    /** Decryption of the payload failed. */
    public static final int ERROR_DECRYPTION_FAILED = 2;

    /** The calling package is invalid or not allowlisted. */
    public static final int ERROR_INVALID_PACKAGE = 3;

    /** The request timestamp is invalid. */
    public static final int ERROR_INVALID_TIMESTAMP = 4;

    /** Serialization or deserialization of the request failed. */
    public static final int ERROR_SERIALIZATION_FAILED = 5;

    /**
     * The calling package was not visible in the foreground.
     *
     * Some form of foreground visibility is always required, but the exact definition of
     * varies based on the device's form factor.
     */
    public static final int ERROR_NOT_IN_FOREGROUND = 6;

    @ErrorCode
    private final int mErrorCode;

    public CrossDeviceAuthenticationException(@ErrorCode int errorCode) {
        super("Authentication failed with error code: " + errorCode);
        mErrorCode = errorCode;
    }

    public CrossDeviceAuthenticationException(@ErrorCode int errorCode, @NonNull String message) {
        super(message);
        mErrorCode = errorCode;
    }

    public CrossDeviceAuthenticationException(@ErrorCode int errorCode, @NonNull String message,
            @NonNull Throwable cause) {
        super(message, cause);
        mErrorCode = errorCode;
    }

    /** Returns the error code associated with this exception. */
    @ErrorCode
    public int getErrorCode() {
        return mErrorCode;
    }
}
