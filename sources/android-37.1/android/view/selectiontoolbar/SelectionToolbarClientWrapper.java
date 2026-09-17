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
import android.os.ParcelableException;
import android.view.SurfaceControlViewHost;

import java.util.concurrent.Executor;

/**
 * An internal binder adapter that bridges communication from the remote selection toolbar service
 * back to the application's {@link SelectionToolbarClient}.
 */
@Hide
public class SelectionToolbarClientWrapper extends ISelectionToolbarClient.Stub {
    private final SelectionToolbarProviderFactory.SelectionToolbarProviderImpl mProvider;
    private final SelectionToolbarClient mClient;
    private final Executor mClientExecutor;

    SelectionToolbarClientWrapper(
            @NonNull SelectionToolbarProviderFactory.SelectionToolbarProviderImpl provider,
            @NonNull SelectionToolbarClient client,
            @NonNull Executor clientExecutor) {
        mProvider = provider;
        mClient = client;
        mClientExecutor = clientExecutor;
    }

    @Override
    public void onSessionOpened() {
        mClientExecutor.execute(() -> mClient.onSessionOpened(
                new SelectionToolbarSessionWrapper(mProvider, mClient, this)));
        mProvider.addActiveSessionRecord(mClient,
                new SelectionToolbarProviderFactory.SelectionToolbarSessionRecord(mClientExecutor));
    }

    @Override
    public void onSessionError(@NonNull ParcelableException e) {
        mProvider.onSessionClosed(mClient);
        mClientExecutor.execute(() -> mClient.onSessionError(e.getCause()));
    }

    @Override
    public void onUpdated(@NonNull SurfaceControlViewHost.SurfacePackage surfacePackage,
            @NonNull Rect contentRect, @NonNull Region touchableRegion) {
        mClientExecutor.execute(() -> mClient.onUpdated(surfacePackage, contentRect,
                touchableRegion));
    }

    @Override
    public void onVisibilityChanged(boolean visible) {
        mClientExecutor.execute(() -> mClient.onVisibilityChanged(visible));
    }

    @Override
    public void onMenuItemClicked(int itemIndex) {
        mClientExecutor.execute(() -> mClient.onMenuItemClicked(itemIndex));
    }
}
