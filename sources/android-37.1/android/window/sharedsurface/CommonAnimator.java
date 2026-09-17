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


/**
 * The common interface to control the animator created from description from WmShell.
 */
@Hide
public abstract class CommonAnimator {
    /**
     * Start the animator.
     */
    public abstract void start();

    /**
     * Cancel the animator.
     */
    public abstract void cancel();

    /**
     * Register an animation update listener from this animator.
     */
    public abstract void addUpdateListener(OnUpdateListener listener);

    /**
     * Register an animation end listener from this animator.
     */
    public abstract void addEndListener(Runnable onEnd);

    // The "Interception" API, if another animation want start from current animator, it can
    // query the current animation status as a reference for it's beginning status.
    /**
     * Returns the current animated value (e.g., current scale or translation)
     */
    public abstract float getCurrentValue();

    /**
     * Returns the current velocity in pixels per second.
     */
    public abstract float getCurrentVelocity();

    /**
     * To listen from animation update from the SharedAnimator.
     */
    public interface OnUpdateListener {
        /**
         * Called when animation update.
         * @param value The animation progress, the value always between 0 ~ 1 which represent
         *              start and finish.
         * @param velocity The latest velocity.
         */
        void onUpdate(float value, float velocity);
    }
}
