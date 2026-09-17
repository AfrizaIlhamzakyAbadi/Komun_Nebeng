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

package android.processor.devicepolicy;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Describes the roles that are allowed to set this policy. */
@Retention(RetentionPolicy.SOURCE)
public @interface AllowedRoles {
    /** Value used to indicate that the given role is allowed to set the policy. */
    public static final int ALLOWED = 1;

    /** Value used to indicate that the given role not allowed to set the policy. */
    public static final int DISALLOWED = 2;

    public int deviceController() default DISALLOWED;
}
