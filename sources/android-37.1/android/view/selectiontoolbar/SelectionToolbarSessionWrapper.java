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
 * The client-side implementation of {@link SelectionToolbarSession}.
 */
@Hide
final class SelectionToolbarSessionWrapper implements SelectionToolbarSession {
    private final SelectionToolbarProviderFactory.SelectionToolbarProviderImpl mProvider;
    private final SelectionToolbarClient mClient;
    private final SelectionToolbarClientWrapper mClientWrapper;

    SelectionToolbarSessionWrapper(
            @NonNull SelectionToolbarProviderFactory.SelectionToolbarProviderImpl provider,
            @NonNull SelectionToolbarClient client,
            @NonNull SelectionToolbarClientWrapper clientWrapper) {
        mProvider = provider;
        mClient = client;
        mClientWrapper = clientWrapper;
    }

    @Override
    public void update(boolean isLayoutRequired,
            List<ToolbarMenuItem> menuItems, Rect contentRect, int suggestedWidth) {
        mProvider.updateSession(mClientWrapper, isLayoutRequired, menuItems, contentRect,
                suggestedWidth);
    }

    @Override
    public void hide() {
        mProvider.hideSession(mClientWrapper);
    }

    @Override
    public void close() {
        try {
            mProvider.closeSession(mClientWrapper);
        } finally {
            mProvider.onSessionClosed(mClient);
        }
    }
}

