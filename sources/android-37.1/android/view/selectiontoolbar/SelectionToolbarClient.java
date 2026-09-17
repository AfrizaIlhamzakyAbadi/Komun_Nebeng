/*
 * Copyright (C) 2025 The Android Open Source Project
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

package android.view.selectiontoolbar;

import android.annotation.Hide;
import android.annotation.NonNull;
import android.graphics.Rect;
import android.graphics.Region;
import android.view.SurfaceControlViewHost;

/**
 * A callback interface for receiving events about the selection toolbar.
 */
@Hide
public interface SelectionToolbarClient {

    /**
     * Called when the selection toolbar session is successfully created.
     */
    void onSessionOpened(@NonNull SelectionToolbarSession session);

    /**
     * Called when an error occurs either during the creation of the session or after the session
     * is created.
     */
    void onSessionError(@NonNull Throwable cause);

    /**
     * Called when the remote selection toolbar is invisible.
     */
    void onVisibilityChanged(boolean visible);

    /**
     * Called when the remote selection toolbar widget is updated.
     */
    void onUpdated(@NonNull SurfaceControlViewHost.SurfacePackage surfacePackage,
            @NonNull Rect contentRect, @NonNull Region touchableRegion);

    /**
     * Called when a menu item in the remote selection toolbar is clicked.
     */
    void onMenuItemClicked(int itemIndex);
}
