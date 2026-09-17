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

import android.annotation.Hide;
import android.annotation.NonNull;
import android.app.ActivityOptions;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;
import android.view.IWindow;
import android.view.IWindowSession;

import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Forward animation filters and animation parameters to system server.
 */
@Hide
@VisibleForTesting
public class SharedAnimationController implements SharedSurfaceDispatcher {
    static final String TAG = SharedAnimationController.class.getSimpleName();
    static final boolean DEBUG = false;

    // The handler to keep mProviderMap synced.
    @NonNull
    private final Handler mHandler;
    private final Object mLock = new Object();
    @GuardedBy("mLock")
    private IWindowSession mWindowSession;
    // The reference location of the animation; either be the location for open transition, or
    // the back-to location for a close transition.
    @GuardedBy("mLock")
    private IWindow mWindow;

    // Queue for providers added before the window is attached
    @GuardedBy("mLock")
    private final List<SharedAnimationProvider> mPendingProviders = new ArrayList<>();
    private final ArrayMap<SharedAnimationProvider, WeakReference<ISharedAnimationProvider>>
            mProviderMap = new ArrayMap<>();

    public SharedAnimationController(Looper looper) {
        mHandler = new Handler(looper);
    }

    @VisibleForTesting
    @Override
    public void addSharedAnimationProvider(@NonNull SharedAnimationProvider provider) {
        synchronized (mLock) {
            if (!isWindowReadyLocked()) {
                // Queue it up if the window isn't attached yet
                if (!mPendingProviders.contains(provider)) {
                    mPendingProviders.add(provider);
                }
                return;
            }
        }
        mHandler.post(() -> {
            if (!validateFilters(provider)) {
                return;
            }
            if (mProviderMap.containsKey(provider)) {
                return;
            }
            postAddSharedAnimationProvider(provider);
        });
    }

    private void postAddSharedAnimationProvider(@NonNull SharedAnimationProvider provider) {
        final IWindowSession session;
        final IWindow window;
        synchronized (mLock) {
            if (!isWindowReadyLocked()) {
                return;
            }
            session = mWindowSession;
            window = mWindow;
        }
        final ISharedAnimationProvider wrapper;
        try {
            synchronized (provider.mLock) {
                final SharedAnimationParams animationParams = provider.getAnimationParameters();
                final List<SharedAnimationFilter> filters = provider.getSharedAnimationFilters();
                wrapper = new SharedAnimationProvider.SharedAnimationProviderWrapper(provider);
                provider.setSharedAnimationController(this);
                session.addSharedAnimationProvider(
                        window, wrapper, animationParams, filters);
            }
        } catch (RemoteException e) {
            mProviderMap.remove(provider);
            synchronized (provider.mLock) {
                provider.setSharedAnimationController(null);
            }
            Log.e(TAG, "addSharedAnimationProvider failed: ", e);
            return;
        }
        mProviderMap.put(provider, new WeakReference<>(wrapper));
    }

    @Override
    public void removeSharedAnimationProvider(@NonNull SharedAnimationProvider provider) {
        final IWindowSession session;
        synchronized (mLock) {
            if (!isWindowReadyLocked()) {
                return;
            }
            session = mWindowSession;
        }
        mHandler.post(() -> postRemoveSharedAnimationProvider(session, provider));
    }

    private void postRemoveSharedAnimationProvider(final IWindowSession session,
            @NonNull SharedAnimationProvider provider) {
        final WeakReference<ISharedAnimationProvider> wrapper = mProviderMap.remove(provider);
        if (wrapper == null) {
            return;
        }
        final ISharedAnimationProvider iProvider = wrapper.get();
        if (iProvider == null) {
            return;
        }
        synchronized (provider.mLock) {
            provider.setSharedAnimationController(null);
        }
        try {
            session.removeSharedAnimationProvider(iProvider);
        } catch (RemoteException e) {
            Log.e(TAG, "removeSharedAnimationProvider failed: ", e);
        }
    }

    @Override
    public ActivityOptions.SharedSurfaceAnimationInfo createSharedSurfaceAnimationInfo(
            @NonNull SharedAnimationProvider provider) {
        final IWindow window;
        synchronized (mLock) {
            if (!isWindowReadyLocked()) {
                return null;
            }
            window = mWindow;
        }
        synchronized (provider.mLock) {
            final ISharedAnimationProvider iProvider =
                    new SharedAnimationProvider.SharedAnimationProviderWrapper(provider);
            return new ActivityOptions.SharedSurfaceAnimationInfo(
                    window, iProvider, provider.getAnimationParameters());
        }
    }

    void updateSharedAnimationFilter(@NonNull SharedAnimationProvider provider) {
        final IWindowSession session;
        synchronized (mLock) {
            if (!isWindowReadyLocked()) {
                return;
            }
            session = mWindowSession;
        }
        mHandler.post(() -> {
            if (!mProviderMap.containsKey(provider)) {
                return;
            }
            if (!validateFilters(provider)) {
                return;
            }
            final WeakReference<ISharedAnimationProvider> wrapper = mProviderMap.get(provider);
            if (wrapper == null) {
                return;
            }
            final ISharedAnimationProvider iProvider = wrapper.get();
            if (iProvider == null) {
                return;
            }
            try {
                synchronized (provider.mLock) {
                    final List<SharedAnimationFilter> filters =
                            provider.getSharedAnimationFilters();
                    session.updateSharedAnimationFilters(iProvider, filters);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "updateSharedAnimationFilter failed: ", e);
            }
        });
    }

    @Hide
    @VisibleForTesting
    public void updateSharedAnimationParams(@NonNull SharedAnimationProvider provider) {
        final IWindowSession session;
        synchronized (mLock) {
            if (!isWindowReadyLocked()) {
                return;
            }
            session = mWindowSession;
        }
        mHandler.post(() -> {
            if (!mProviderMap.containsKey(provider)) {
                return;
            }
            if (!validateFilters(provider)) {
                return;
            }
            final WeakReference<ISharedAnimationProvider> wrapper = mProviderMap.get(provider);
            if (wrapper == null) {
                return;
            }
            final ISharedAnimationProvider iProvider = wrapper.get();
            if (iProvider == null) {
                return;
            }
            try {
                synchronized (provider.mLock) {
                    final SharedAnimationParams  params = provider.getAnimationParameters();
                    session.updateSharedAnimationParams(iProvider, params);
                }
            } catch (RemoteException e) {
                Log.e(TAG, "updateSharedAnimationParams failed: ", e);
            }
        });
    }

    /** Attaches the controller instance on its window. */
    public void attachOnWindow(@NonNull IWindowSession windowSession, @NonNull IWindow window) {
        synchronized (mLock) {
            if (isWindowReadyLocked()) {
                Log.e(TAG, "Re-attach window while already exist.");
                return;
            }
            mWindowSession = windowSession;
            mWindow = window;

            // Process any providers that were queued before attachment
            if (!mPendingProviders.isEmpty()) {
                for (SharedAnimationProvider provider : mPendingProviders) {
                    mHandler.post(() -> postAddSharedAnimationProvider(provider));
                }
                mPendingProviders.clear();
            }
        }
    }

    /** Detaches the controller instance from its window. */
    public void detachFromWindow() {
        final IWindowSession windowSession;
        synchronized (mLock) {
            if (!isWindowReadyLocked()) {
                return;
            }
            windowSession = mWindowSession;
            mWindow = null;
            mWindowSession = null;
        }
        mHandler.post(() -> {
            for (int i = mProviderMap.size() - 1; i >= 0; --i) {
                postRemoveSharedAnimationProvider(windowSession, mProviderMap.keyAt(i));
            }
        });
    }

    @GuardedBy("mLock")
    private boolean isWindowReadyLocked() {
        return mWindowSession != null && mWindow != null;
    }

    private boolean validateFilters(SharedAnimationProvider provider) {
        final List<SharedAnimationFilter> filters;
        synchronized (provider.mLock) {
            filters = List.copyOf(provider.getSharedAnimationFilters());
        }
        for (int i = filters.size() - 1; i >= 0; --i) {
            if (!filters.get(i).isValid()) {
                Log.e(TAG, "SharedAnimationProvider contains invalid filter: "
                        + filters.get(i));
                return false;
            }
        }

        final List<SharedAnimationFilter> allOtherFilters = new ArrayList<>();
        for (int i = mProviderMap.size() - 1; i >= 0; --i) {
            final SharedAnimationProvider other = mProviderMap.keyAt(i);
            if (other == provider) {
                continue;
            }
            synchronized (other.mLock) {
                allOtherFilters.addAll(other.getSharedAnimationFilters());
            }
        }

        for (int i = filters.size() - 1; i >= 0; --i) {
            final SharedAnimationFilter filter = filters.get(i);
            if (allOtherFilters.contains(filter)) {
                Log.e(TAG, "Pre-registered SharedAnimationProvider contains same filter: "
                        + filter);
                return false;
            }
        }
        return true;
    }
}
