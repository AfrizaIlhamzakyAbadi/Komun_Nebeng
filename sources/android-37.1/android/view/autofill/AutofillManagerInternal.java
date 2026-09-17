/*
 * Copyright (C) 2017 The Android Open Source Project
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
package android.view.autofill;

import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.UserIdInt;
import android.content.AutofillOptions;

/**
 * Autofill Manager local system service interface.
 */
@Hide
public abstract class AutofillManagerInternal {

    /**
     * Notifies the manager that the back key was pressed.
     *
     * @param userId The user id of the key event.
     */
    public abstract void onBackKeyPressed(@UserIdInt int userId);

    /**
     * Gets autofill options for a package.
     *
     * <p><b>NOTE: </b>this method is called by the {@code ActivityManager} service and hence cannot
     * hold the main service lock.
     *
     * @param packageName The package for which to query.
     * @param versionCode The package version code.
     * @param userId The user id for which to query.
     */
    @Nullable
    public abstract AutofillOptions getAutofillOptions(@NonNull String packageName,
            long versionCode, @UserIdInt int userId);

    /**
     * Checks whether the given {@code uid} owns the
     * {@link android.service.autofill.augmented.AugmentedAutofillService} implementation associated
     * with the given {@code userId}.
     */
    public abstract boolean isAugmentedAutofillServiceForUser(@NonNull int callingUid,
            @UserIdInt int userId);

    /**
     * Notifies the autofill service that the restriction state of a specific display has changed.
     *
     * <p> Restrictions are typically toggled when an automation agent (e.g., Computer Control)
     * takes over a virtual display. When a display becomes restricted (e.g., during active
     * computer control or automation) any existing autofill sessions on that display are
     * immediately terminated and new sessions should be blocked to prevent unauthorized access
     * or UI leakage.
     *
     * <p>When restrictions are lifted ({@code isRestricted} is false), the display ID is
     * removed from the restricted list.
     *
     * @param displayId The ID of the display whose restriction state changed
     * @param isRestricted {@code true} if autofill should be blocked on this display;
     *                     {@code false} if restrictions should be lifted.
     */

    public abstract void onDisplayRestrictionChanged(int displayId, boolean isRestricted);
}
