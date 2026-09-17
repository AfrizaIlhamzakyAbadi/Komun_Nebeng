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

package android.app.admin;

import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_SUBSCRIPTIONS;

import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.annotation.Nullable;

/**
 * Callback for changes to resolved device-wide policy values.
 *
 * @param <T> The type of the policy value.
 */
@FlaggedApi(FLAG_POLICY_STREAMLINING_SUBSCRIPTIONS)
public interface ResolvedDeviceWidePolicyCallback<T> {
    /**
     * Method invoked when a resolved policy changes.
     *
     * @param id The policy identifier whose resolved value changed.
     * @param newValue The new resolved policy value.
     * @param oldValue The old resolved policy value.
     */
    void onValueChanged(
            @NonNull PolicyIdentifier<T> id, @Nullable T newValue, @Nullable T oldValue);
}
