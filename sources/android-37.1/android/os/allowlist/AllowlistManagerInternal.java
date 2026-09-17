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

package android.os.allowlist;

import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.UserIdInt;

import java.util.Set;

/**
 * Allowlist manager local system service interface.
 * <p>Only for use within the system server.
 */
@Hide
public interface AllowlistManagerInternal {
    /**
     * Get the package name of the allowlist provider.
     */
    @NonNull
    Set<String> getAllowlistProviderPackageNames(@UserIdInt int userId);

    interface TestProviderChangeListener {
        /**
         * Called when the test provider is changed.
         */
        void onTestProviderChanged();
    }

    /**
     * Register a listener for test provider change.
     */
    void registerTestProviderChangeListener(@NonNull TestProviderChangeListener listener);

    /**
     * Unregister a listener for test provider change.
     */
    void unregisterTestProviderChangeListener(@NonNull TestProviderChangeListener listener);
}
