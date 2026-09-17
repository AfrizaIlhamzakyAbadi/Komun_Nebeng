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

import java.util.List;

/**
 * This interface defines selection toolbar session.
 */
@Hide
public interface SelectionToolbarSession extends AutoCloseable {

    /**
     * Updates the selection toolbar with new menu items and layout information.
     *
     * @param isLayoutRequired True if a layout pass is required for the toolbar.
     * @param menuItems        The list of {@link ToolbarMenuItem} to display in the toolbar.
     * @param contentRect      The {@link Rect} that defines the size and positioning of the remote
     *                         view.
     * @param suggestedWidth     The suggested width for the toolbar.
     */
    void update(boolean isLayoutRequired, @NonNull
            List<ToolbarMenuItem> menuItems, @NonNull Rect contentRect, int suggestedWidth);

    /**
     * Request to hide selection toolbar for the current UID.
     */
    void hide();

    /**
     * Closes the session and releases all associated resources.
     */
    @Override
    void close();
}
