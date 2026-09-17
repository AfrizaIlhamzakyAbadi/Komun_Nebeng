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

package android.hardware.input;

import static com.android.hardware.input.Flags.FLAG_ENABLE_POEM_INPUT_CUSTOMIZATION;

import android.annotation.FlaggedApi;
import android.ravenwood.annotation.RavenwoodKeepWholeClass;

/**
 * Represents an action that can be associated with a peripheral customization trigger.
 * <p>
 * This is an abstract class that serves as the base for all customization actions,
 * such as {@link SimpleCustomizationAction}, {@link AppCustomizationAction} and
 * {@link UriCustomizationAction}.
 */
@FlaggedApi(FLAG_ENABLE_POEM_INPUT_CUSTOMIZATION)
@RavenwoodKeepWholeClass
public abstract class CustomizationAction {
    /* package */ CustomizationAction() {}
}
