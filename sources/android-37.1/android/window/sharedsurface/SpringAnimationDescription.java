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

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.Parcel;
import android.util.Log;

import com.android.internal.dynamicanimation.animation.FloatValueHolder;
import com.android.internal.dynamicanimation.animation.SpringAnimation;
import com.android.internal.dynamicanimation.animation.SpringForce;

import java.util.Objects;

/**
 * Description of a one-time physics-based animation.
 */
final class SpringAnimationDescription extends AnimationDescription {
    private static final String TAG = SharedAnimationController.TAG;

    /**
     * The maximum damping ratio of this description to avoid infinite animation.
     */
    public static final float MAXIMUM_DAMPING_RATIO = 10f;

    /**
     * Defines the spring stiffness.
     */
    private final float mStiffness;

    /**
     * Defines the spring damping (e.g., 0.8).
     */
    private final float mDampingRatio;

    private static final float FINAL_POSITION = 100f;


    public float getStiffness() {
        return mStiffness;
    }

    public float getDampingRatio() {
        return mDampingRatio;
    }

    @Override
    @NonNull
    public CommonAnimator createAnimator() {
        final FloatValueHolder valueHolder = new FloatValueHolder(0);
        final SpringAnimation sa = new SpringAnimation(valueHolder);
        final SpringForce force = new SpringForce(FINAL_POSITION)
                .setStiffness(mStiffness)
                .setDampingRatio(mDampingRatio);

        sa.setSpring(force);
        sa.setStartVelocity(0);

        return new CommonAnimator() {
            private volatile float mLastVelocity = 0;

            @Override
            public void start() {
                sa.start();
            }

            @Override
            public void cancel() {
                sa.cancel();
            }

            @Override
            public void addUpdateListener(OnUpdateListener listener) {
                sa.addUpdateListener((animation, value, velocity) -> {
                    mLastVelocity = velocity;
                    float fraction = value / FINAL_POSITION;
                    listener.onUpdate(fraction, velocity);
                });
            }

            @Override
            public void addEndListener(Runnable onEnd) {
                sa.addEndListener((anim, canceled, val, vel) -> onEnd.run());
            }

            @Override
            public float getCurrentValue() {
                return valueHolder.getValue();
            }

            @Override
            public float getCurrentVelocity() {
                return mLastVelocity;
            }
        };
    }

    SpringAnimationDescription(float stiffness, float dampingRatio) {
        super(DESCRIPTION_SPRING);
        mStiffness = stiffness;
        if (dampingRatio > MAXIMUM_DAMPING_RATIO) {
            Log.e(TAG, "Damping ratio too big, consider less than " + MAXIMUM_DAMPING_RATIO);
            mDampingRatio = MAXIMUM_DAMPING_RATIO;
        } else {
            mDampingRatio = dampingRatio;
        }
    }

    SpringAnimationDescription(SpringAnimationDescription source) {
        super(DESCRIPTION_SPRING);
        mStiffness = source.mStiffness;
        mDampingRatio = source.mDampingRatio;
    }

    SpringAnimationDescription(Parcel in, int descriptionType) {
        super(descriptionType);
        mStiffness = in.readFloat();
        mDampingRatio = in.readFloat();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeFloat(mStiffness);
        dest.writeFloat(mDampingRatio);
    }

    @Override
    public String toString() {
        return "SpringAnimationDescription{"
                + " mStiffness=" + mStiffness
                + " mDampingRatio=" + mDampingRatio
                + "}";
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SpringAnimationDescription other)) return false;
        return mStiffness == other.mStiffness && mDampingRatio == other.mDampingRatio;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mStiffness, mDampingRatio);
    }
}
