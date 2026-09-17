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
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.util.Log;

import java.util.Objects;
import java.util.concurrent.Executor;

@Hide
public final class SharedSurfaceHandlerInfo implements Parcelable {
    private static final String TAG = SharedAnimationController.TAG;
    private final ISharedSurfaceHandler mSharedSurfaceHandler;
    private final Rect mSharedSurfaceBounds;
    private float mInitialAlpha;
    private float mFinalAlpha;
    private float mInitialCornerRadius;
    private float mFinalCornerRadius;

    /**
     * Build the SharedSurfaceHandlerInfo to pass relative information to system.
     *
     * @param surfaceBounds The location of the shared surface on the pivot window.
     * @param onRemoveCallback The remove callback to clear up created resources.
     */
    public SharedSurfaceHandlerInfo(@NonNull Rect surfaceBounds,
            @NonNull Runnable onRemoveCallback,
            @NonNull Executor executor) {
        Objects.requireNonNull(surfaceBounds);
        Objects.requireNonNull(onRemoveCallback);
        Objects.requireNonNull(executor);
        if (surfaceBounds.isEmpty() || !surfaceBounds.isValid()) {
            throw new IllegalArgumentException("Invalid surface bounds!");
        }
        mSharedSurfaceBounds = new Rect(surfaceBounds);
        mSharedSurfaceHandler = new SharedSurfaceHandlerWrapper(onRemoveCallback, executor);
    }

    private SharedSurfaceHandlerInfo(Parcel in) {
        mSharedSurfaceHandler = ISharedSurfaceHandler.Stub.asInterface(in.readStrongBinder());
        mSharedSurfaceBounds = in.readTypedObject(Rect.CREATOR);
        mInitialAlpha = in.readFloat();
        mFinalAlpha = in.readFloat();
        mInitialCornerRadius = in.readFloat();
        mFinalCornerRadius = in.readFloat();
    }

    /**
     * Called when the animation is complete, the surface provider can now remove the shared
     * surface.
     */
    @Hide
    public void onRemoveSharingSurface() {
        if (mSharedSurfaceHandler == null) {
            Log.w(TAG, "onRemoveSharingSurface: SharedSurfaceHandler is null");
            return;
        }
        try {
            mSharedSurfaceHandler.onRemoveSharingSurface();
        } catch (RemoteException re) {
            Log.w(TAG, "Unable to remove shared surface, app crashed?");
        }
    }

    /**
     * Set the expected alpha value during animation.
     * @param initialAlpha The initial alpha applied on the Shared Surface
     * @param finalAlpha The final alpha applied on the Shared Surface
     */
    public void setAlpha(float initialAlpha, float finalAlpha) {
        mInitialAlpha = initialAlpha;
        mFinalAlpha = finalAlpha;
    }

    /**
     * Set the expected corner radius value during animation.
     *
     * @param initialCornerRadius The initial radius applied on the Shared Surface
     * @param finalCornerRadius The final radius applied on the Shared Surface
     */
    public void setCornerRadius(float initialCornerRadius, float finalCornerRadius) {
        mInitialCornerRadius = initialCornerRadius;
        mFinalCornerRadius = finalCornerRadius;
    }

    /**
     * Get the bounds of the Shared Surface on the pivot window.
     */
    @Hide
    public @NonNull Rect getSharedSurfaceBounds() {
        return mSharedSurfaceBounds;
    }

    /**
     * Get the initial alpha of the Shared Surface requested to.
     */
    @Hide
    public float getInitialAlpha() {
        return mInitialAlpha;
    }


    /**
     * Get the final alpha of the Shared Surface requested to.
     */
    @Hide
    public float getFinalAlpha() {
        return mFinalAlpha;
    }

    /**
     * Get the initial corner radius of the Shared Surface requested to.
     */
    @Hide
    public float getInitialCornerRadius() {
        return mInitialCornerRadius;
    }


    /**
     * Get the final corner radius of the Shared Surface requested to.
     */
    @Hide
    public float getFinalCornerRadius() {
        return mFinalCornerRadius;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, @WriteFlags int flags) {
        dest.writeStrongInterface(mSharedSurfaceHandler);
        dest.writeTypedObject(mSharedSurfaceBounds, flags);
        dest.writeFloat(mInitialAlpha);
        dest.writeFloat(mFinalAlpha);
        dest.writeFloat(mInitialCornerRadius);
        dest.writeFloat(mFinalCornerRadius);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SharedSurfaceHandlerInfo> CREATOR = new Creator<>() {
        @Override
        public SharedSurfaceHandlerInfo createFromParcel(Parcel in) {
            return new SharedSurfaceHandlerInfo(in);
        }

        @Override
        public SharedSurfaceHandlerInfo[] newArray(int size) {
            return new SharedSurfaceHandlerInfo[size];
        }
    };

    private static class SharedSurfaceHandlerWrapper extends ISharedSurfaceHandler.Stub {
        private final Runnable mOnRemoveCallback;
        private final Executor mExecutor;

        SharedSurfaceHandlerWrapper(Runnable onRemoveCallback, Executor executor) {
            mOnRemoveCallback = onRemoveCallback;
            mExecutor = executor;
        }

        @Override
        public void onRemoveSharingSurface() {
            if (SharedAnimationController.DEBUG) {
                Log.d(TAG, "SharedSurfaceHandlerWrapper#onRemoveSharingSurface");
            }
            mExecutor.execute(mOnRemoveCallback);
        }
    }
}
