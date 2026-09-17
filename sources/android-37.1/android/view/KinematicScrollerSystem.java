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

import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.content.Context;
import android.view.flags.Flags;

/**
 * A system that describes scrolling motion following kinematic rules.
 *
 * <p>At the core, the system defines rules that determine how its kinematic state evolves through
 * time. These rules can mimic the real world through classical physics, or can follow customized
 * and fine-tuned motion curves.
 *
 * <p>This system is stateful. It maintains an internal state by managing internal variables that
 * represent position, velocity, and time.
 */
@FlaggedApi(Flags.FLAG_PHYSICAL_SCROLLING)
public interface KinematicScrollerSystem {

    /**
     * Creates a default {@link KinematicScrollerSystem} that follows Stokes' law of drag forces.
     *
     * <p>This is the default scroller used in the Android framework for one-dimensional scrollable
     * surfaces. The system uses a drag force to decelerate motion similar to a laminar flow. Below
     * a threshold, velocities are further reduced by an additional deceleration to smoothly
     * terminate the motion at zero velocity.
     *
     * <p>Spatial parameters that represent position, velocity and acceleration are considered to be
     * given in arbitrary units [U]. Therefore, these quantities are assumed to denote [U],
     * [U/second] and [U/second²], respectively. Users of this API should maintain consistent units
     * and scalings when providing spatial properties to the scroller, and when interpreting the
     * numerical outputs of the scroller.
     *
     * <p>The system also supports a controlled and smooth velocity accumulation behavior. While in
     * motion, the caller can use the {@link #accumulateVelocityOrRestart(float, float, long)}
     * method to add velocity into the system during its deceleration. The system will apply an
     * exponential "charge" behavior that will smoothly accumulate velocity until it reaches the
     * requested target value. If no further accumulations are requested, the system will resume its
     * deceleration behavior starting from the newly accumulated velocity.
     *
     * @param laminarDecelerationFactor Controls the exponential decay of velocity above the
     *     velocity deceleration threshold. This decay reflects laminar flow motion. This parameter
     *     is unitless.
     * @param finalApproachDeceleration Additional deceleration applied when the model is below the
     *     velocity threshold. It is given in units of [U/second²] and must be a positive magnitude.
     * @param velocityDecelerationThreshold Above this threshold, velocity decays as a laminar flow.
     *     Below the threshold, an additional constant deceleration is applied. It is given in units
     *     of [U/second].
     * @param velocityAccumulationThreshold The minimum velocity that this model would accept to be
     *     accumulated when initiating velocity accumulation. It is given in units of [U/second] and
     *     ust be a positive magnitude.
     * @param maximumAccumulatedVelocity The maximum velocity (above the current velocity) of the
     *     system that could be accumulated. It is given in units of [U/second] and must be a
     *     positive magnitude.
     * @param velocityAccumulationTimeMillis The time period over which the system will reach its
     *     velocity accumulation target. Given in milliseconds.
     * @return The default implementation of {@link KinematicScrollerSystem} in Android.
     */
    static @NonNull KinematicScrollerSystem createDefaultScroller(
            float laminarDecelerationFactor,
            float finalApproachDeceleration,
            float velocityDecelerationThreshold,
            float velocityAccumulationThreshold,
            float maximumAccumulatedVelocity,
            long velocityAccumulationTimeMillis) {
        return new KinematicScrollerSystemImpl(
                laminarDecelerationFactor,
                finalApproachDeceleration,
                velocityDecelerationThreshold,
                velocityAccumulationThreshold,
                maximumAccumulatedVelocity,
                velocityAccumulationTimeMillis);
    }

    /**
     * Creates a default {@link KinematicScrollerSystem} in pixel space following Stokes' drag
     * forces.
     *
     * <p>This function uses the default parameters from {@link ViewConfiguration} to create a
     * scroller that operates in spatial units of pixels, pixels/second and pixels/second².
     *
     * @param context The {@link Context} used to access default scaled parameters from {@link
     *     ViewConfiguration}.
     */
    static @NonNull KinematicScrollerSystem createDefaultScroller(@NonNull Context context) {
        final ViewConfiguration vc = ViewConfiguration.get(context);
        return createDefaultScroller(
                vc.getFlingLaminarDeceleration(),
                vc.getScaledFlingFinalApproachDeceleration(),
                vc.getScaledFlingDecelerationThreshold(),
                vc.getScaledFlingAccumulationThreshold(),
                vc.getScaledFlingMaximumAccumulatedVelocity(),
                vc.getFlingAccumulationTimeMillis());
    }

    /**
     * Gets the current position.
     *
     * @return The current position of the system.
     */
    float getCurrentPosition();

    /**
     * Gets the current velocity.
     *
     * @return The current velocity of the system.
     */
    float getCurrentVelocity();

    /**
     * Gets the current time.
     *
     * @return The current time in the internal state of the system in units of milliseconds.
     */
    long getCurrentTimeMillis();

    /**
     * Applies kinematic rules defined by this system to make its internal state variables of
     * position and velocity advance to new values at the given time.
     *
     * <p>The input time must be monotonically increasing and greater than or equal to zero.
     *
     * @param timeMillis The new time that the system will advance to (in milliseconds). Must be
     *     monotonically increasing and greater than or equal to zero.
     */
    void advanceTo(long timeMillis);

    /**
     * Sets the initial position, velocity, and time that the system will advance from.
     *
     * <p>These are the initial conditions that the system will use in computation to derive new
     * positions and velocities when called to {@link #advanceTo(long)}. This method does not
     * perform any velocity accumulation or reset any ongoing accumulation. To attempt velocity
     * accumulation, use {@link #accumulateVelocityOrRestart(float, float, long)}.
     *
     * @param initialPosition The initial position.
     * @param initialVelocity The initial velocity.
     * @param initialTimeMillis The initial time in milliseconds.
     */
    void setInitialState(float initialPosition, float initialVelocity, long initialTimeMillis);

    /**
     * Attempts to accumulate the given {@code extraVelocity} onto the current velocity of the
     * system, provided they are in the same direction.
     *
     * <p>If the system is decelerating, the magnitude of the given velocity must be above the
     * accumulation threshold (see {@link ViewConfiguration#getScaledFlingAccumulationThreshold()}).
     * If the system is already accumulating velocity, the new velocity will be accumulated.
     * However, the accumulation will never exceed the maximum accumulated velocity set by the
     * system. If the conditions for velocity accumulation are met, the system will transition to an
     * accelerating state towards the accumulated target velocity, starting from the current
     * velocity and the given {@code startPosition} and {@code startTimeMillis}.
     *
     * <p>If the accumulation checks fail, the system will reset any ongoing accumulation and
     * restart the motion by setting the initial state to the given {@code startPosition},
     * {@code extraVelocity} (as the new initial velocity), and {@code startTimeMillis}.
     *
     * @param extraVelocity The velocity to be accumulated.
     * @param startPosition The position to restart from if accumulation fails, or the starting
     *     position for the accelerated motion if accumulation succeeds.
     * @param startTimeMillis The time in milliseconds to restart from if accumulation fails, or
     *     the starting time for the accelerated motion if accumulation succeeds.
     */
    void accumulateVelocityOrRestart(
            float extraVelocity, float startPosition, long startTimeMillis);

    /**
     * Resets the system to a ground state by resetting the internal position, velocity, and time
     * variables to zero.
     *
     * <p>If an initial state was previously set via {@link #setInitialState(float, float, long)}
     * or {@link #accumulateVelocityOrRestart(float, float, long)}, it will be cleared.
     */
    void reset();

    /**
     * Determines if the system is currently accumulating velocity (accelerating).
     *
     * @return {@code true} if the system is accumulating velocity; {@code false} otherwise.
     */
    boolean isAccumulatingVelocity();

    /**
     * Determines if the system is stable and no longer in motion.
     *
     * @return true if the system has reached a stability condition (e.g., the velocity is virtually
     *     zero).
     */
    boolean isStable();

    /**
     * Estimates the stable position of the system if left to advance undisturbed from an initial
     * state (via {@link #setInitialState(float, float, long)} or
     * {@link #accumulateVelocityOrRestart(float, float, long)}) towards its stable state.
     *
     * @return The estimated stable position of the system.
     */
    float estimateStablePosition();

    /**
     * Estimates the time until the system reaches stability if left to advance undisturbed from an
     * initial state (via {@link #setInitialState(float, float, long)} or
     * {@link #accumulateVelocityOrRestart(float, float, long)}) towards its stable
     * state.
     *
     * @return The estimated time until the system reaches stability.
     */
    long estimateStableTimeMillis();

    /**
     * Makes this system a deep copy of a given {@link KinematicScrollerSystem}.
     *
     * <p>The copy allows the caller to perform operations on the system, without mutating the
     * internal state of the system that it was copied from.
     *
     * @param other The other {@link KinematicScrollerSystem} to copy from.
     * @throws IllegalArgumentException If this and the given system to copy are of different types.
     */
    void copyFrom(@NonNull KinematicScrollerSystem other);
}
