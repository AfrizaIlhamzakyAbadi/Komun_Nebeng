/*
 * Copyright 2026 The Android Open Source Project
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

package android.media.tv.interactive;
import android.annotation.Hide;


/**
 * Constants for DIAL (DIscovery And Launch) remote device requests and responses.
 */
@Hide
public final class TvInteractiveAppDialConstants {
    private TvInteractiveAppDialConstants() {
    }

    /**
     * Broadcast intent action for DIAL remote device request.
     */
    @Hide
    public static final String ACTION_DIAL_REMOTE_DEVICE_REQUEST =
            "android.media.tv.interactive.action.DIAL_REMOTE_DEVICE_REQUEST";

    /**
     * Broadcast intent action for DIAL remote device response.
     */
    @Hide
    public static final String ACTION_DIAL_REMOTE_DEVICE_RESPONSE =
            "android.media.tv.interactive.action.DIAL_REMOTE_DEVICE_RESPONSE";

    /**
     * Intent value for request type: query.
     */
    @Hide
    public static final String INTENT_VALUE_DIAL_REQUEST_TYPE_QUERY = "query";

    /**
     * Intent value for request type: launch.
     */
    @Hide
    public static final String INTENT_VALUE_DIAL_REQUEST_TYPE_LAUNCH = "launch";

    /**
     * Intent value for request type: stop.
     */
    @Hide
    public static final String INTENT_VALUE_DIAL_REQUEST_TYPE_STOP = "stop";

    /**
     * Intent value for request type: hide.
     */
    @Hide
    public static final String INTENT_VALUE_DIAL_REQUEST_TYPE_HIDE = "hide";

    /**
     * Intent value for request type: other.
     */
    @Hide
    public static final String INTENT_VALUE_DIAL_REQUEST_TYPE_OTHER = "other";

    /**
     * Intent key for DIAL request type.
     * <p>Type: String
     */
    @Hide
    public static final String INTENT_KEY_DIAL_REQUEST_TYPE = "dial_request_type";

    /**
     * Intent key for DIAL app name.
     * <p>Type: String
     */
    @Hide
    public static final String INTENT_KEY_DIAL_APP_NAME = "dial_app_name";

    /**
     * Intent key for DIAL app full URL.
     * <p>Type: String
     */
    @Hide
    public static final String INTENT_KEY_DIAL_APP_FULL_URL = "dial_app_full_url";

    /**
     * Intent key for DIAL additional data.
     * <p>Type: String
     */
    @Hide
    public static final String INTENT_KEY_DIAL_ADDITIONAL_DATA = "dial_additional_data";

    /**
     * Intent key for DIAL app DB URL.
     * <p>Type: String
     */
    @Hide
    public static final String INTENT_KEY_DIAL_APP_DB_URL = "dial_app_db_url";

    /**
     * Intent key for DIAL app launch type.
     * <p>Type: String
     */
    @Hide
    public static final String INTENT_KEY_DIAL_APP_LAUNCH_TYPE = "dial_app_launch_type";

    /**
     * Intent key for DIAL app source type.
     * <p>Type: String
     */
    @Hide
    public static final String INTENT_KEY_DIAL_APP_SOURCE_TYPE = "dial_app_source_type";

    /**
     * Intent key for DIAL response code.
     * <p>Type: int
     */
    @Hide
    public static final String INTENT_KEY_DIAL_RESPONSE_CODE = "dial_response_code";

    /**
     * Intent key for DIAL response state.
     * <p>Type: String
     */
    @Hide
    public static final String INTENT_KEY_DIAL_RESPONSE_STATE = "dial_response_state";

    /**
     * Intent value for DIAL app launch type: HbbTV.
     */
    @Hide
    public static final int INTENT_VALUE_DIAL_LAUNCH_TYPE_HBBTV =
            TvInteractiveAppServiceInfo.INTERACTIVE_APP_TYPE_HBBTV;

    /**
     * Intent value for DIAL app source type: HbbTV.
     */
    @Hide
    public static final int INTENT_VALUE_DIAL_SOURCE_TYPE_HBBTV =
            TvInteractiveAppServiceInfo.INTERACTIVE_APP_TYPE_HBBTV;

    /**
     * Intent value for DIAL response code: OK.
     */
    @Hide
    public static final int INTENT_VALUE_DIAL_RESPONSE_CODE_OK = 200;

    /**
     * Intent value for DIAL response code: Created.
     */
    @Hide
    public static final int INTENT_VALUE_DIAL_RESPONSE_CODE_CREATED = 201;

    /**
     * Intent value for DIAL response code: Forbidden (Terminal).
     */
    @Hide
    public static final int INTENT_VALUE_DIAL_RESPONSE_CODE_FORBIDDEN_TERMINAL = 4031;

    /**
     * Intent value for DIAL response code: Forbidden (User).
     */
    @Hide
    public static final int INTENT_VALUE_DIAL_RESPONSE_CODE_FORBIDDEN_USER = 4032;

    /**
     * Intent value for DIAL response code: Not Found.
     */
    @Hide
    public static final int INTENT_VALUE_DIAL_RESPONSE_CODE_NOT_FOUND = 404;

    /**
     * Intent value for DIAL response code: Internal Server Error.
     */
    @Hide
    public static final int INTENT_VALUE_DIAL_RESPONSE_CODE_INTERNAL_SERVER_ERROR = 500;

    /**
     * Intent value for DIAL response code: Service Unavailable.
     */
    @Hide
    public static final int INTENT_VALUE_DIAL_RESPONSE_CODE_SERVICE_UNAVAILABLE = 503;

    /**
     * Intent value for DIAL response state: running.
     */
    @Hide
    public static final String INTENT_VALUE_DIAL_RESPONSE_STATE_RUNNING = "running";

    /**
     * Intent value for DIAL response state: stopped.
     */
    @Hide
    public static final String INTENT_VALUE_DIAL_RESPONSE_STATE_STOPPED = "stopped";

    /**
     * Intent value for DIAL response state: hidden.
     */
    @Hide
    public static final String INTENT_VALUE_DIAL_RESPONSE_STATE_HIDDEN = "hidden";
}
