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
package android.window.sharedsurface;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.app.ActivityOptions;

/**
 * Dispatch to register SharedSurfaceProvider
 */
@Hide
@FlaggedApi(com.android.window.flags.Flags.FLAG_SHARED_SURFACE_TRANSITION_ANIMATION)
public interface SharedSurfaceDispatcher {
    /**
     * Register a SharedSurface animation provider.
     */
    void addSharedAnimationProvider(@NonNull SharedAnimationProvider provider);

    /**
     * Unregister a SharedSurface animation provider.
     */
    void removeSharedAnimationProvider(@NonNull SharedAnimationProvider provider);

    @Hide
    default ActivityOptions.SharedSurfaceAnimationInfo createSharedSurfaceAnimationInfo(
            @NonNull SharedAnimationProvider provider) {
        return null;
    }
}
