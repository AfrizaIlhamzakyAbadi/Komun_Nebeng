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

package android.health.connect.common;

import android.annotation.Hide;
import android.content.Context;

/**
 * Utility methods for device information.
 */
@Hide
public interface DeviceUtils {
    /** Returns true if HealthConnect is fully available (permissions and data APIs) */
    boolean isHealthConnectFullyAvailable(Context context);

    /** Returns true if health connect permissions are available. */
    boolean areHealthPermissionsAvailable(Context context);

    /** Returns true if only health connect permissions are available on watch. */
    boolean isWearPermissionsMode(Context context);

    /** Returns true if the current user is a profile. */
    boolean isProfile(Context context);

    /** Returns true if the current user is the system user. */
    boolean isSystemUser(Context context);
}
