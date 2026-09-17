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
import android.content.pm.PackageManager;
import android.os.UserManager;

import com.android.modules.utils.build.SdkLevel;

/**
 * Implementation of {@link DeviceUtils}.
 */
@Hide
public class DeviceUtilsImpl implements DeviceUtils {

    @Override
    public boolean isHealthConnectFullyAvailable(Context context) {
        return isHardwareSupported(context) && !isWatch(context) && !isProfile(context);
    }

    @Override
    public boolean areHealthPermissionsAvailable(Context context) {
        return isHealthConnectFullyAvailable(context)
                || (isHardwareSupported(context) && SdkLevel.isAtLeastB());
    }

    @Override
    public boolean isWearPermissionsMode(Context context) {
        return isWatch(context) && SdkLevel.isAtLeastB();
    }

    @Override
    public boolean isProfile(Context context) {
        UserManager userManager = context.getSystemService(UserManager.class);
        return userManager.isProfile();
    }

    @Override
    public boolean isSystemUser(Context context) {
        UserManager userManager = context.getSystemService(UserManager.class);
        return userManager.isSystemUser();
    }

    private boolean isHardwareSupported(Context context) {
        PackageManager pm = context.getPackageManager();
        return !pm.hasSystemFeature(PackageManager.FEATURE_EMBEDDED)
                && !pm.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
                && !pm.hasSystemFeature(PackageManager.FEATURE_AUTOMOTIVE)
                && !pm.hasSystemFeature(PackageManager.FEATURE_PC);
    }

    private boolean isWatch(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WATCH);
    }
}
