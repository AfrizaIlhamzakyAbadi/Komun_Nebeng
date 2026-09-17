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
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.graphics.Path;
import android.os.BadParcelableException;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.PathParser;
import android.util.SparseArray;


import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Allows a SharedAnimationProvider to declare an animation style for either geometric and/or
 * crossfade effects.
 */
@Hide
@FlaggedApi(com.android.window.flags.Flags.FLAG_SHARED_SURFACE_TRANSITION_ANIMATION)
public final class SharedAnimationParams implements Parcelable {

    /**
     * The maximum duration of the animation in milliseconds.
     */
    @Hide
    public static final long MAXIMUM_ANIMATION_DURATION_MS = 1000;

    private final SparseArray<AnimationDescription> mAnimationSparseArray;

    public static final int ANIMATION_UNDEFINED = 0;
    public static final int ANIMATION_X_TRANSLATION = 1;
    public static final int ANIMATION_Y_TRANSLATION = 1 << 1;
    public static final int ANIMATION_SCALE = 1 << 2;
    public static final int ANIMATION_CROSS_FADE = 1 << 3;
    public static final int ANIMATION_CORNER_RADIUS = 1 << 4;
    public static final int ANIMATION_TRANSLATION = ANIMATION_X_TRANSLATION
            | ANIMATION_Y_TRANSLATION;
    public static final int ANIMATION_GEOMETRIC = ANIMATION_TRANSLATION | ANIMATION_SCALE;
    private static final int[] ANIMATIONS_FLAGS = {ANIMATION_X_TRANSLATION,
            ANIMATION_Y_TRANSLATION, ANIMATION_SCALE, ANIMATION_CROSS_FADE,
            ANIMATION_CORNER_RADIUS};
    private static final int ANIMATION_EVERYTHING = ANIMATION_GEOMETRIC | ANIMATION_CROSS_FADE
            | ANIMATION_CORNER_RADIUS;

    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(prefix = {"ANIMATION_"}, flag = true, value = {
            ANIMATION_UNDEFINED,
            ANIMATION_X_TRANSLATION,
            ANIMATION_Y_TRANSLATION,
            ANIMATION_SCALE,
            ANIMATION_CROSS_FADE,
            ANIMATION_CORNER_RADIUS
    })
    public @interface AnimationFlags {}

    // No change in speed.
    public static final int INTERPOLATOR_LINEAR = 0;

    // Starts slow, ends fast.
    public static final int INTERPOLATOR_ACCELERATE = 1;

    // Starts fast, ends slow.
    public static final int INTERPOLATOR_DECELERATE = 2;

    // Slow start and end, fast middle.
    public static final int INTERPOLATOR_ACCELERATE_DECELERATE = 3;

    // Pulls back slightly before moving forward.
    public static final int INTERPOLATOR_ANTICIPATE = 4;

    // Flings forward past the destination, then returns.
    public static final int INTERPOLATOR_OVERSHOOT = 5;

    // A combination of anticipate and overshoot.
    public static final int INTERPOLATOR_ANTICIPATE_OVERSHOOT = 6;
    // Bounces at the end state.
    public static final int INTERPOLATOR_BOUNCE = 7;

    // Path / Cubic-Bezier; This allows the Shell to recreate any smooth easing curve used by modern
    // design systems.
    public static final int INTERPOLATOR_PATH = 8;

    @IntDef(prefix = "INTERPOLATOR_", value = {
            INTERPOLATOR_LINEAR,
            INTERPOLATOR_ACCELERATE,
            INTERPOLATOR_DECELERATE,
            INTERPOLATOR_ACCELERATE_DECELERATE,
            INTERPOLATOR_ANTICIPATE,
            INTERPOLATOR_OVERSHOOT,
            INTERPOLATOR_ANTICIPATE_OVERSHOOT,
            INTERPOLATOR_BOUNCE,
            INTERPOLATOR_PATH,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface InterpolatorType {}

    public SharedAnimationParams() {
        mAnimationSparseArray = new SparseArray<>();
    }

    public SharedAnimationParams(SharedAnimationParams params) {
        mAnimationSparseArray = new SparseArray<>(params.mAnimationSparseArray.size());
        for (int i = params.mAnimationSparseArray.size() - 1; i >= 0; --i) {
            final int key = params.mAnimationSparseArray.keyAt(i);
            final AnimationDescription sourceDescription = params.mAnimationSparseArray.valueAt(i);
            final AnimationDescription description =
                    sourceDescription.mDescriptionType == AnimationDescription.DESCRIPTION_TIME
                            ? new InterpolatorAnimationDescription(
                                    (InterpolatorAnimationDescription) sourceDescription)
                            : new SpringAnimationDescription(
                                    (SpringAnimationDescription) sourceDescription);
            mAnimationSparseArray.put(key, description);
        }
    }

    private SharedAnimationParams(Parcel in) {
        mAnimationSparseArray = new SparseArray<>();

        final List<AnimationDescription> uniqueDescriptions = new ArrayList<>();
        in.readTypedList(uniqueDescriptions, AnimationDescription.CREATOR);
        if (uniqueDescriptions.size() > ANIMATIONS_FLAGS.length) {
            throw new BadParcelableException("Too many unique descriptions: "
                    + uniqueDescriptions.size());
        }

        final int size = in.readInt();
        if (size < 0 || size > ANIMATIONS_FLAGS.length) {
            throw new BadParcelableException("Invalid animation sparse array size: " + size);
        }
        for (int i = 0; i < size; i++) {
            final int flag = in.readInt();
            final int index = in.readInt();
            if (index < 0 || index >= uniqueDescriptions.size()) {
                throw new BadParcelableException("Invalid index for uniqueDescriptions: " + index);
            }
            mAnimationSparseArray.put(flag, uniqueDescriptions.get(index));
        }
    }

    /**
     * Create animation parameter with Spring based description.
     */
    public void addAnimationSpring(@AnimationFlags int flag, float stiffness,
            float dampingRatio) {
        validateFlags(flag);
        validateSpringParam(stiffness, dampingRatio);
        final AnimationDescription description = new SpringAnimationDescription(
                stiffness, dampingRatio);
        distributeFlagToDescription(flag, description);
    }

    /**
     * Create animation parameter with time based description.
     */
    public void addAnimationInterpolated(@AnimationFlags int flag, long durationMillis,
            @InterpolatorType int type) {
        validateFlags(flag);
        validateDuration(durationMillis);
        validateInterpolatorType(type);
        final AnimationDescription description = new InterpolatorAnimationDescription(
                durationMillis, type);
        distributeFlagToDescription(flag, description);
    }

    /**
     * Create animation parameter with a complex SVG-style path description.
     */
    public void addAnimationPath(@AnimationFlags int flag, long durationMillis,
            @NonNull String pathData) {
        validateFlags(flag);
        validateDuration(durationMillis);
        Objects.requireNonNull(pathData);
        validatePathData(pathData);

        final AnimationDescription description = new InterpolatorAnimationDescription(
                durationMillis, pathData);
        distributeFlagToDescription(flag, description);
    }

    private void distributeFlagToDescription(@AnimationFlags int flag,
            AnimationDescription description) {
        for (int p : ANIMATIONS_FLAGS) {
            // Check if this specific bit is set in the flag
            if ((flag & p) != 0) {
                // Internally store them as individual indexes
                mAnimationSparseArray.put(p, description);
            }
        }
    }

    private static void validateSpringParam(float stiffness, float dampingRatio) {
        if (stiffness <= 0) {
            throw new IllegalArgumentException("Stiffness must be positive!");
        }
        if (dampingRatio < 0) {
            throw new IllegalArgumentException("Damping ratio must be non-negative!");
        }
    }

    private static void validateDuration(long durationMillis) {
        if (durationMillis < 0) {
            throw new IllegalArgumentException("Duration must be non-negative");
        }
        if (durationMillis > MAXIMUM_ANIMATION_DURATION_MS) {
            throw new IllegalArgumentException("Duration too large, consider less than "
                    + MAXIMUM_ANIMATION_DURATION_MS + " ms.");
        }
    }

    private static void validateInterpolatorType(int interpolatorType) {
        if (interpolatorType < INTERPOLATOR_LINEAR
                || interpolatorType > INTERPOLATOR_PATH) {
            throw new IllegalArgumentException("Invalid interpolator type= " + interpolatorType);
        }
    }

    private static void validateFlags(@AnimationFlags int flag) {
        if (flag <= ANIMATION_UNDEFINED || flag > ANIMATION_EVERYTHING) {
            throw new IllegalArgumentException("Invalid flag");
        }
    }

    private static void validatePathData(@NonNull String pathData) {
        try {
            final Path path = PathParser.createPathFromPathData(pathData);
            if (path.isEmpty()) {
                throw new IllegalArgumentException("Path data cannot be empty");
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid SVG path syntax", e);
        }
    }


    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        final List<AnimationDescription> uniqueDescriptions = new ArrayList<>();
        for (int i = 0; i < mAnimationSparseArray.size(); i++) {
            final AnimationDescription desc = mAnimationSparseArray.valueAt(i);
            if (!uniqueDescriptions.contains(desc)) {
                uniqueDescriptions.add(desc);
            }
        }
        dest.writeTypedList(uniqueDescriptions);
        final int size = mAnimationSparseArray.size();
        dest.writeInt(size);
        for (int i = 0; i < size; i++) {
            dest.writeInt(mAnimationSparseArray.keyAt(i));
            dest.writeInt(uniqueDescriptions.indexOf(mAnimationSparseArray.valueAt(i)));
        }
    }

    @NonNull
    public static final Creator<SharedAnimationParams> CREATOR = new Creator<>() {
        @Override
        public SharedAnimationParams createFromParcel(Parcel in) {
            return new SharedAnimationParams(in);
        }

        @Override
        public SharedAnimationParams[] newArray(int size) {
            return new SharedAnimationParams[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String toString() {
        return "SharedAnimationParams, Animations= " + mAnimationSparseArray.toString();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SharedAnimationParams other)) return false;

        if (mAnimationSparseArray.size() != other.mAnimationSparseArray.size()) {
            return false;
        }

        for (int i = 0; i < mAnimationSparseArray.size(); i++) {
            int key = mAnimationSparseArray.keyAt(i);
            if (!Objects.equals(mAnimationSparseArray.valueAt(i),
                    other.mAnimationSparseArray.get(key))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = 1;
        for (int i = 0; i < mAnimationSparseArray.size(); i++) {
            result = 31 * result + Integer.hashCode(mAnimationSparseArray.keyAt(i));
            result = 31 * result + Objects.hashCode(mAnimationSparseArray.valueAt(i));
        }
        return result;
    }

    /**
     * Create all animators requested from client.
     */
    @Hide
    public SparseArray<CommonAnimator> createAnimators() {
        final SparseArray<CommonAnimator> animators = new SparseArray<>();
        // Cache animators by value equality to ensure identical descriptions get shared animators
        final Map<AnimationDescription, CommonAnimator> sharedAnimators = new HashMap<>();
        for (int i = mAnimationSparseArray.size() - 1; i >= 0; --i) {
            final AnimationDescription description = mAnimationSparseArray.valueAt(i);
            final int flag = mAnimationSparseArray.keyAt(i);

            CommonAnimator animator = sharedAnimators.get(description);
            if (animator == null) {
                animator = description.createAnimator();
                sharedAnimators.put(description, animator);
            }
            animators.put(flag, animator);
        }
        return animators;
    }
}
