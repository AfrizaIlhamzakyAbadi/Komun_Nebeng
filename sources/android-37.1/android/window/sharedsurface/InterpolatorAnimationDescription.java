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

import static android.window.sharedsurface.SharedAnimationParams.INTERPOLATOR_ACCELERATE;
import static android.window.sharedsurface.SharedAnimationParams.INTERPOLATOR_ACCELERATE_DECELERATE;
import static android.window.sharedsurface.SharedAnimationParams.INTERPOLATOR_ANTICIPATE;
import static android.window.sharedsurface.SharedAnimationParams.INTERPOLATOR_ANTICIPATE_OVERSHOOT;
import static android.window.sharedsurface.SharedAnimationParams.INTERPOLATOR_BOUNCE;
import static android.window.sharedsurface.SharedAnimationParams.INTERPOLATOR_DECELERATE;
import static android.window.sharedsurface.SharedAnimationParams.INTERPOLATOR_LINEAR;
import static android.window.sharedsurface.SharedAnimationParams.INTERPOLATOR_OVERSHOOT;
import static android.window.sharedsurface.SharedAnimationParams.INTERPOLATOR_PATH;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.Parcel;
import android.util.Log;
import android.util.PathParser;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.PathInterpolator;

import java.util.Objects;

/**
 * Description of a one-time time based animation.
 */
final class InterpolatorAnimationDescription extends AnimationDescription {
    private static final String TAG = SharedAnimationController.TAG;

    /**
     * The total duration of the animation in milliseconds.
     */
    private final long mDurationMillis;

    /**
     * Defines the time-curve (e.g., INTERPOLATOR_ACCELERATE_DECELERATE).
     */
    private final @SharedAnimationParams.InterpolatorType int mInterpolatorType;

    private String mPathData;

    @Override
    @NonNull
    public CommonAnimator createAnimator() {
        final ValueAnimator va = ValueAnimator.ofFloat(0, 1);
        va.setDuration(mDurationMillis);
        final Interpolator interpolator = createInterpolator(this);
        va.setInterpolator(interpolator);
        return new CommonAnimator() {

            private volatile float mCurrentVelocity = 0;
            @Override
            public void start() {
                va.start();
            }

            @Override
            public void cancel() {
                va.cancel();
            }

            @Override
            public void addUpdateListener(OnUpdateListener listener) {
                va.addUpdateListener(anim -> {
                    final float fraction = anim.getAnimatedFraction();
                    final float currentValue = (float) anim.getAnimatedValue();

                    // Determine sampling direction to stay within [0, 1]
                    float dt = 0.001f;
                    float f1, f2;
                    float actualDt;

                    if (fraction + dt <= 1.0f) {
                        f1 = interpolator.getInterpolation(fraction);
                        f2 = interpolator.getInterpolation(fraction + dt);
                        actualDt = dt;
                    } else {
                        f1 = interpolator.getInterpolation(fraction - dt);
                        f2 = interpolator.getInterpolation(fraction);
                        actualDt = dt;
                    }

                    // Slope = change in interpolation / change in fraction
                    float slope = (f2 - f1) / actualDt;
                    if (mDurationMillis > 0) {
                        mCurrentVelocity = slope * (1000f / mDurationMillis);
                    } else {
                        mCurrentVelocity = 0;
                    }
                    listener.onUpdate(currentValue, mCurrentVelocity);
                });
            }

            @Override
            public void addEndListener(Runnable onEnd) {
                va.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator a) {
                        onEnd.run();
                    }
                });
            }

            @Override
            public float getCurrentValue() {
                return (float) va.getAnimatedValue();
            }

            @Override
            public float getCurrentVelocity() {
                return mCurrentVelocity;
            }
        };
    }

    InterpolatorAnimationDescription(long durationMillis,
            @SharedAnimationParams.InterpolatorType int interpolatorType) {
        super(DESCRIPTION_TIME);
        mDurationMillis = durationMillis;
        if (interpolatorType == INTERPOLATOR_PATH) {
            Log.e(TAG, "Cannot create path interpolator without control pointers,"
                    + " return with default interpolator");
            mInterpolatorType = INTERPOLATOR_LINEAR;
        } else if (interpolatorType < INTERPOLATOR_LINEAR
                || interpolatorType > INTERPOLATOR_PATH) {
            Log.e(TAG, "Invalid interpolator type=" + interpolatorType);
            mInterpolatorType = INTERPOLATOR_LINEAR;
        } else {
            mInterpolatorType = interpolatorType;
        }
    }

    /**
     * Create a time based description with an SVG-style path data for path interpolator.
     */
    InterpolatorAnimationDescription(long durationMillis, String pathData) {
        super(DESCRIPTION_TIME);
        mDurationMillis = durationMillis;
        mInterpolatorType = INTERPOLATOR_PATH;
        mPathData = pathData;
    }

    InterpolatorAnimationDescription(InterpolatorAnimationDescription source) {
        super(DESCRIPTION_TIME);
        mDurationMillis = source.mDurationMillis;
        mInterpolatorType = source.mInterpolatorType;
        mPathData = source.mPathData;
    }

    InterpolatorAnimationDescription(Parcel in, int descriptionType) {
        super(descriptionType);
        mDurationMillis = in.readLong();
        mInterpolatorType = in.readInt();
        mPathData = in.readString8();
        if (mInterpolatorType == INTERPOLATOR_PATH) {
            if (mPathData == null || mPathData.isEmpty()) {
                throw new IllegalArgumentException("Path data cannot be empty");
            }
        }
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(mDurationMillis);
        dest.writeInt(mInterpolatorType);
        dest.writeString8(mPathData);
    }

    @Override
    public String toString() {
        return "InterpolatorAnimationDescription{"
                + " mDurationMillis=" + mDurationMillis
                + " mInterpolatorType=" + mInterpolatorType
                + "}";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof InterpolatorAnimationDescription other)) return false;
        return mDurationMillis == other.mDurationMillis
                && mInterpolatorType == other.mInterpolatorType
                && Objects.equals(mPathData, other.mPathData);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mDurationMillis, mInterpolatorType, mPathData);
    }

    static @NonNull Interpolator createInterpolator(
            @NonNull InterpolatorAnimationDescription params) {
        return switch (params.mInterpolatorType) {
            case INTERPOLATOR_ACCELERATE -> new AccelerateInterpolator();
            case INTERPOLATOR_DECELERATE -> new DecelerateInterpolator();
            case INTERPOLATOR_ACCELERATE_DECELERATE -> new AccelerateDecelerateInterpolator();
            case INTERPOLATOR_ANTICIPATE -> new AnticipateInterpolator();
            case INTERPOLATOR_OVERSHOOT -> new OvershootInterpolator();
            case INTERPOLATOR_ANTICIPATE_OVERSHOOT ->
                    new AnticipateOvershootInterpolator();
            case INTERPOLATOR_BOUNCE -> new BounceInterpolator();
            case INTERPOLATOR_PATH ->
                // parse the SVG string
                    new PathInterpolator(PathParser.createPathFromPathData(params.mPathData));
            default -> new LinearInterpolator();
        };
    }
}
