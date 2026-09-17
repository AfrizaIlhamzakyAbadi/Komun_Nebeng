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

package android.view;

import android.annotation.NonNull;

/**
 * The default implementation of {@link KinematicScrollerSystem} in the Android platform.
 *
 * <p>TODO(b/497943375): Once ready, the implementation computations should use the NDK APIs
 */
final class KinematicScrollerSystemImpl implements KinematicScrollerSystem {

    private static final float ACCUMULATION_VELOCITY_STABILITY_DELTA = 10f;

    // Deceleration parameters
    private final float mLaminarDecelerationFactor;
    private final float mFinalApproachDeceleration;
    private final float mVelocityDecelerationThreshold;
    private final float mFinalApproachFriction;

    // Velocity accumulation parameters in pixels
    private final float mVelocityAccumulationThreshold;
    private final float mMaxAccumulatedVelocity;
    private final float mAccumulationTimeConstant;

    // Kinematic state variables
    private float mCurrentPosition = 0;
    private float mCurrentVelocity = 0;
    private long mCurrentTimeMillis = 0;

    private float mCurrentMaxAccumulatedVelocity = 0f;
    private float mTargetAccumulationVelocity = 0f;

    KinematicScrollerSystemImpl(
            float laminarDecelerationFactor,
            float finalApproachDeceleration,
            float velocityDecelerationThreshold,
            float velocityAccumulationThreshold,
            float maximumAccumulatedVelocity,
            long velocityAccumulationTimeMillis) {

        mLaminarDecelerationFactor = laminarDecelerationFactor;
        mFinalApproachDeceleration = finalApproachDeceleration;
        mVelocityDecelerationThreshold = velocityDecelerationThreshold;
        mFinalApproachFriction =
                mFinalApproachDeceleration / mVelocityDecelerationThreshold
                        - laminarDecelerationFactor;
        mVelocityAccumulationThreshold = velocityAccumulationThreshold;
        mMaxAccumulatedVelocity = maximumAccumulatedVelocity;
        mAccumulationTimeConstant = velocityAccumulationTimeMillis / 1000f;
    }

    public float getCurrentMaxAccumulatedVelocity() {
        return mCurrentMaxAccumulatedVelocity;
    }

    public float getTargetAccumulationVelocity() {
        return mTargetAccumulationVelocity;
    }

    @Override
    public void advanceTo(long timeMillis) {
        if (isAccumulatingVelocity()) {
            long stableTimeMillis =
                    getStableAccumulationTimeMillis(mCurrentTimeMillis, mCurrentVelocity);
            if (timeMillis >= stableTimeMillis) {
                // The transition from accumulation to deceleration will happen in this frame.
                // We need to split the frame so that we perform the necessary accumulation until
                // the continuous point when the stable accumulated velocity is reached
                float timeDeltaAccumSec = (stableTimeMillis - mCurrentTimeMillis) / 1000f;
                if (timeDeltaAccumSec > 0) {
                  // Last mid-frame accumulation
                    float nextVelocityAbs = getNextAccumulatedVelocityAbs(timeDeltaAccumSec);
                    mCurrentPosition =
                            getNextAccumulatedPosition(timeDeltaAccumSec, nextVelocityAbs);
                    mCurrentVelocity = Math.signum(mCurrentVelocity) * nextVelocityAbs;
                    mCurrentTimeMillis = stableTimeMillis;
                }

                // Transition to deceleration for the rest of the frame
                mTargetAccumulationVelocity = 0f;
                mCurrentMaxAccumulatedVelocity = 0f;

                advanceWhileDecelerating(timeMillis);
            } else {
                advanceWhileAccumulating(timeMillis);
            }
        } else {
            advanceWhileDecelerating(timeMillis);
        }
    }

    private boolean advanceWhileAccumulating(long timeMillis) {
        float timeDeltaSec = (timeMillis - mCurrentTimeMillis) / 1000f;
        float nextVelocityAbs = getNextAccumulatedVelocityAbs(timeDeltaSec);
        mCurrentPosition = getNextAccumulatedPosition(timeDeltaSec, nextVelocityAbs);
        mCurrentVelocity = Math.signum(mCurrentVelocity) * nextVelocityAbs;
        mCurrentTimeMillis = timeMillis;
        return true;
    }

    private float getNextAccumulatedVelocityAbs(float timeDeltaSec) {
        double expTerm = Math.exp(-timeDeltaSec / mAccumulationTimeConstant);
        double expDecay = mTargetAccumulationVelocity * (1f - expTerm);
        return (float) (Math.abs(mCurrentVelocity) * expTerm + expDecay);
    }

    private float getNextAccumulatedPosition(float timeDeltaSec, float nextAccumulatedVelocityAbs) {
        float currentVelocityAbs = Math.abs(mCurrentVelocity);
        float direction = Math.signum(mCurrentVelocity);
        float step =
                mTargetAccumulationVelocity * timeDeltaSec
                        - mAccumulationTimeConstant
                                * (nextAccumulatedVelocityAbs - currentVelocityAbs);
        return mCurrentPosition + direction * step;
    }

    private void advanceWhileDecelerating(long timeMillis) {
        // If stable, the system stays still
        if (isStable()) {
            mCurrentTimeMillis = timeMillis;
            return;
        }

        float deltaTimeSec = (timeMillis - mCurrentTimeMillis) / 1000.0f;
        float direction = Math.signum(mCurrentVelocity);
        float currentVelocityAbs = Math.abs(mCurrentVelocity);

        float nextVelocityAbs;
        float positionStep;
        if (currentVelocityAbs <= mVelocityDecelerationThreshold) {
            float expTerm = (float) Math.exp(mFinalApproachFriction * deltaTimeSec);
            float expDecayVelocity =
                    expTerm
                            * (currentVelocityAbs
                                    - mFinalApproachDeceleration / mFinalApproachFriction);

            nextVelocityAbs =
                    Math.max(
                            0f,
                            expDecayVelocity + mFinalApproachDeceleration / mFinalApproachFriction);
            positionStep =
                    (nextVelocityAbs
                                    - currentVelocityAbs
                                    + mFinalApproachDeceleration * deltaTimeSec)
                            / mFinalApproachFriction;

        } else {
            nextVelocityAbs =
                    (float)
                            (currentVelocityAbs
                                    * Math.exp(-mLaminarDecelerationFactor * deltaTimeSec));
            positionStep = (currentVelocityAbs - nextVelocityAbs) / mLaminarDecelerationFactor;
        }
        mCurrentPosition += direction * positionStep;
        mCurrentVelocity = direction * nextVelocityAbs;
        mCurrentTimeMillis = timeMillis;
    }

    @Override
    public void setInitialState(
            float initialPosition, float initialVelocity, long initialTimeMillis) {
        mCurrentVelocity = initialVelocity;
        mCurrentPosition = initialPosition;
        mCurrentTimeMillis = initialTimeMillis;
    }

    @Override
    public void accumulateVelocityOrRestart(
            float extraVelocity, float startPosition, long startTimeMillis) {
        if (Math.signum(extraVelocity) == Math.signum(mCurrentVelocity)) {
            float extraVelocityAbs = Math.abs(extraVelocity);
            boolean canAccumulate =
                    mCurrentMaxAccumulatedVelocity != 0f
                            || extraVelocityAbs >= mVelocityAccumulationThreshold;

            if (canAccumulate) {
                if (mCurrentMaxAccumulatedVelocity == 0f) {
                    // Velocity accumulation will start for the first time, so we set the maximum
                    // velocity that could be reached.
                    mCurrentMaxAccumulatedVelocity =
                            Math.abs(mCurrentVelocity) + mMaxAccumulatedVelocity;
                }

                // Update the current target velocity to reach
                mTargetAccumulationVelocity =
                        Math.min(
                                Math.abs(mCurrentVelocity) + extraVelocityAbs,
                                mCurrentMaxAccumulatedVelocity);
                mCurrentPosition = startPosition;
                mCurrentTimeMillis = startTimeMillis;
                return;
            }
        }

        // Fallback to starting a new session (resetting accumulation)
        reset();
        setInitialState(startPosition, extraVelocity, startTimeMillis);
    }

    @Override
    public boolean isAccumulatingVelocity() {
        return mMaxAccumulatedVelocity != 0f && mTargetAccumulationVelocity != 0f;
    }

    @Override
    public void reset() {
        mCurrentPosition = 0f;
        mCurrentVelocity = 0f;
        mCurrentTimeMillis = 0;

        mCurrentMaxAccumulatedVelocity = 0f;
        mTargetAccumulationVelocity = 0f;
    }

    @Override
    public boolean isStable() {
        return mCurrentVelocity == 0f;
    }

    @Override
    public float getCurrentPosition() {
        return mCurrentPosition;
    }

    @Override
    public float getCurrentVelocity() {
        return mCurrentVelocity;
    }

    @Override
    public long getCurrentTimeMillis() {
        return mCurrentTimeMillis;
    }

    @Override
    public float estimateStablePosition() {
        float startingPosition;
        float startingVelocity;
        if (isAccumulatingVelocity()) {
            long stableTimeMillis =
                    getStableAccumulationTimeMillis(mCurrentTimeMillis, mCurrentVelocity);
            startingPosition =
                    getStableAccumulationPosition(
                            stableTimeMillis, mCurrentPosition, mCurrentVelocity);
            float direction = Math.signum(mCurrentVelocity);
            startingVelocity =
                    direction
                            * (mTargetAccumulationVelocity - ACCUMULATION_VELOCITY_STABILITY_DELTA);
        } else {
            startingPosition = mCurrentPosition;
            startingVelocity = mCurrentVelocity;
        }

        if (Math.abs(startingVelocity) <= mVelocityDecelerationThreshold) {
            return getStablePositionBelowThreshold(startingPosition, startingVelocity);
        } else {
            return getStablePositionAboveThreshold(startingPosition, startingVelocity);
        }
    }

    private float getStableAccumulationPosition(
            long stableAccumulationTimeMillis, float startPosition, float startVelocity) {
        float timeDeltaSec = (stableAccumulationTimeMillis - mCurrentTimeMillis) / 1000f;
        float currentVelocityAbs = Math.abs(startVelocity);
        float direction = Math.signum(startVelocity);
        float stableVelocityAbs =
                mTargetAccumulationVelocity - ACCUMULATION_VELOCITY_STABILITY_DELTA;
        float step =
                timeDeltaSec * mTargetAccumulationVelocity
                        - mAccumulationTimeConstant * (stableVelocityAbs - currentVelocityAbs);

        return startPosition + direction * step;
    }

    private float getStablePositionBelowThreshold(float startPosition, float startVelocity) {
        float startVelocityAbs = Math.abs(startVelocity);
        float direction = Math.signum(startVelocity);
        double logTerm =
                Math.log(
                        mFinalApproachDeceleration
                                / (mFinalApproachDeceleration
                                        - startVelocityAbs * mFinalApproachFriction));
        double positionDelta =
                ((mFinalApproachDeceleration / mFinalApproachFriction) * logTerm - startVelocityAbs)
                        / mFinalApproachFriction;

        return startPosition + (float) (direction * positionDelta);
    }

    private float getStablePositionAboveThreshold(float startPosition, float startVelocity) {
        float direction = Math.signum(startVelocity);
        float startVelocityAbs = Math.abs(startVelocity);

        float positionAtThreshold =
                startPosition
                        + direction
                                * (startVelocityAbs - mVelocityDecelerationThreshold)
                                / mLaminarDecelerationFactor;
        return getStablePositionBelowThreshold(
                positionAtThreshold, direction * mVelocityDecelerationThreshold);
    }

    @Override
    public long estimateStableTimeMillis() {
        long startingTimeMillis;
        float startingVelocity;
        if (isAccumulatingVelocity()) {
            startingTimeMillis =
                    getStableAccumulationTimeMillis(mCurrentTimeMillis, mCurrentVelocity);
            float direction = Math.signum(mCurrentVelocity);
            startingVelocity =
                    direction
                            * (mTargetAccumulationVelocity - ACCUMULATION_VELOCITY_STABILITY_DELTA);
        } else {
            startingTimeMillis = mCurrentTimeMillis;
            startingVelocity = mCurrentVelocity;
        }

        if (Math.abs(startingVelocity) <= mVelocityDecelerationThreshold) {
            return getStableTimeMillisBelowThreshold(startingTimeMillis, startingVelocity);
        } else {
            return getStableTimeMillisAboveThreshold(startingTimeMillis, startingVelocity);
        }
    }

    public long getStableAccumulationTimeMillis(long startTimeMillis, float startVelocity) {
        float diff = mTargetAccumulationVelocity - Math.abs(startVelocity);
        if (diff <= 0) {
            return startTimeMillis;
        }
        double logArg = diff / ACCUMULATION_VELOCITY_STABILITY_DELTA;
        if (logArg <= 0) {
            return startTimeMillis;
        }
        double expTerm = Math.log(logArg);
        return startTimeMillis + (long) (mAccumulationTimeConstant * expTerm * 1000);
    }

    private long getStableTimeMillisBelowThreshold(long startTimeMillis, float startVelocity) {
        float startVelocityAbs = Math.abs(startVelocity);
        double timeDeltaSec =
                Math.log(
                                mFinalApproachDeceleration
                                        / (mFinalApproachDeceleration
                                                - startVelocityAbs * mFinalApproachFriction))
                        / mFinalApproachFriction;
        return startTimeMillis + (long) (timeDeltaSec * 1000);
    }

    private long getStableTimeMillisAboveThreshold(long startTimeMillis, float startVelocity) {
        float startVelocityAbs = Math.abs(startVelocity);
        double timeDeltaSec =
                Math.log(startVelocityAbs / mVelocityDecelerationThreshold)
                        / mLaminarDecelerationFactor;
        long timeToThreshold = startTimeMillis + (long) (timeDeltaSec * 1000);
        return getStableTimeMillisBelowThreshold(timeToThreshold, mVelocityDecelerationThreshold);
    }

    @Override
    public void copyFrom(@NonNull KinematicScrollerSystem other) throws IllegalArgumentException {
        if (!(other instanceof KinematicScrollerSystemImpl)) {
            throw new IllegalArgumentException(
                    "Failed to copy because the KinematicScrollerSystem is "
                            + "of a different type");
        }
        mTargetAccumulationVelocity =
                ((KinematicScrollerSystemImpl) other).getTargetAccumulationVelocity();
        mCurrentMaxAccumulatedVelocity =
                ((KinematicScrollerSystemImpl) other).getCurrentMaxAccumulatedVelocity();
        setInitialState(
                other.getCurrentPosition(),
                other.getCurrentVelocity(),
                other.getCurrentTimeMillis());
    }
}
