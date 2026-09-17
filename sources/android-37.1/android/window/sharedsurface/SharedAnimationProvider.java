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
import android.annotation.Nullable;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceControl;

import com.android.internal.annotations.GuardedBy;
import com.android.internal.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * A client can create a SharedAnimationProvider and then register it to the system server, then by
 * register SharedAnimationFilter, system will check whether the provider can potentially provide
 * the shared surface when a transition occur, and play the transition animation by pre-defined
 * SharedAnimationParams.
 */
@Hide
@FlaggedApi(com.android.window.flags.Flags.FLAG_SHARED_SURFACE_TRANSITION_ANIMATION)
public abstract class SharedAnimationProvider {
    private static final String TAG = SharedAnimationController.TAG;
    final Object mLock = new Object();
    private final Executor mExecutor;
    @GuardedBy("mLock")
    private final List<SharedAnimationFilter> mSharedAnimationFilters = new ArrayList<>();
    @GuardedBy("mLock")
    private SharedAnimationParams mSharedAnimationParams;
    SharedAnimationController mSharedAnimationController;

    public SharedAnimationProvider(@NonNull Executor executor) {
        Objects.requireNonNull(executor);
        mExecutor = executor;
        mSharedAnimationParams = new SharedAnimationParams();
    }
    /**
     * Constructor for the caller to set default {@link SharedAnimationParams}.
     * @param defaultParams The default parameters to be added.
     */
    public SharedAnimationProvider(@NonNull Executor executor,
            @NonNull SharedAnimationParams defaultParams) {
        this(executor);
        mSharedAnimationParams = defaultParams;
    }

    /**
     * Returns the set of animation patterns this provider can handle.
     */
    @VisibleForTesting
    @GuardedBy("mLock")
    @NonNull
    public final List<SharedAnimationFilter> getSharedAnimationFilters() {
        return mSharedAnimationFilters;
    }

    /**
     * Add a shared surface animation filter for this provider.
     */
    public final void addSharedAnimationFilter(@NonNull SharedAnimationFilter filter) {
        synchronized (mLock) {
            if (mSharedAnimationFilters.contains(filter)) {
                return;
            }
            mSharedAnimationFilters.add(filter);
            if (mSharedAnimationController != null) {
                mSharedAnimationController.updateSharedAnimationFilter(this);
            }
        }
    }

    /**
     * Remove a shared surface animation filter for this provider.
     */
    public final void removeSharedAnimationFilter(@NonNull SharedAnimationFilter filter) {
        synchronized (mLock) {
            mSharedAnimationFilters.remove(filter);
            if (mSharedAnimationController != null) {
                mSharedAnimationController.updateSharedAnimationFilter(this);
            }
        }
    }

    /**
     * Update a shared surface animation parameters for this provider.
     * This will replace previous registered property if any.
     */
    public final void updateSharedAnimationParams(@NonNull SharedAnimationParams parameters) {
        synchronized (mLock) {
            if (mSharedAnimationParams != null && mSharedAnimationParams.equals(parameters)) {
                return;
            }
            mSharedAnimationParams = parameters;
            if (mSharedAnimationController != null) {
                mSharedAnimationController.updateSharedAnimationParams(this);
            }
        }
    }

    /**
     * Remove a shared surface animation parameters for this provider.
     * The removed property will be replaced by default behavior.
     */
    public final void removeSharedAnimationParams() {
        synchronized (mLock) {
            mSharedAnimationParams = null;
            if (mSharedAnimationController != null) {
                mSharedAnimationController.updateSharedAnimationParams(this);
            }
        }
    }

    @VisibleForTesting
    @GuardedBy("mLock")
    public void setSharedAnimationController(SharedAnimationController controller) {
        mSharedAnimationController = controller;
    }

    /**
     * Get the blueprint (duration, interpolator, properties) for the animation.
     */
    @VisibleForTesting
    @GuardedBy("mLock")
    public final @Nullable SharedAnimationParams getAnimationParameters() {
        return mSharedAnimationParams;
    }

    /**
     * To create the matched surface handler for drawing the sharing object.
     * The Shell provides a SurfaceControl as a canvas. The client must draw its shared context
     * (e.g., icon, widget surface) onto this canvas.
     *
     * @param canvas The canvas for client to attach or draw the shared element on it.
     * @param transaction Use the transaction to reparent the SurfaceControlViewHost to the surface.
     *
     * @return null if cannot recognize the matched filter.
     * Sample:
     * <pre>{@code
     * final SurfaceControlViewHost viewHost = new SurfaceControlViewHost(...);
     * viewHost.setView(container, width, height);
     * transaction.reparent(viewHost.getSurfacePackage().getSurfaceControl(), canvas);
     * transaction.setVisibility(viewHost.getSurfacePackage().getSurfaceControl(), true);
     * }</pre>
     */
    public abstract @Nullable SharedSurfaceHandlerInfo onSharingSurface(
            @NonNull SharedAnimationFilter matchFilter, SurfaceControl canvas,
            int width, int height, SurfaceControl.Transaction transaction);


    @VisibleForTesting
    public static class SharedAnimationProviderWrapper extends ISharedAnimationProvider.Stub {
        private final SharedAnimationProvider mSharedAnimationProvider;

        @VisibleForTesting
        public SharedAnimationProviderWrapper(SharedAnimationProvider provider) {
            mSharedAnimationProvider = provider;
        }

        @Override
        public void createSurfaceHandler(@NonNull SharedAnimationFilter matched,
                @NonNull SurfaceControl surface, int width, int height,
                @NonNull ISharedAnimationReady ready) {
            mSharedAnimationProvider.mExecutor.execute(() -> {
                final SurfaceControl.Transaction t = new SurfaceControl.Transaction();
                final SharedSurfaceHandlerInfo info = mSharedAnimationProvider.onSharingSurface(
                        matched, surface, width, height, t);
                if (info == null) {
                    notifyHandlerReady(ready, null);
                    return;
                }
                t.addTransactionCompletedListener(Runnable::run, stats ->
                        notifyHandlerReady(ready, info));
                t.apply();
            });
        }

        private static void notifyHandlerReady(ISharedAnimationReady ready,
                SharedSurfaceHandlerInfo info) {
            try {
                ready.onHandlerReady(info);
            } catch (RemoteException e) {
                Log.e(TAG, "Failed to notify system that surface is ready", e);
            }
        }
    }
}
