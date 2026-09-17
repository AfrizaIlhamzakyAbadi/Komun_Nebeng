/*
 * Copyright (C) 2014 The Android Open Source Project
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

package android.service.dreams;

import android.annotation.Hide;
import android.annotation.IntDef;

import android.annotation.NonNull;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Dream manager local system service interface.
 */
@Hide
public abstract class DreamManagerInternal {

    public static final int DREAM_START_REASON_UNKNOWN = 0;
    public static final int DREAM_START_REASON_POWER_MANAGER_REQUESTED = 1;
    public static final int DREAM_START_REASON_PLAYLIST_CHANGED = 2;
    public static final int DREAM_START_REASON_SYSTEM_COMPONENT_SET = 3;
    public static final int DREAM_START_REASON_SYSTEM_COMPONENT_CLEARED = 4;
    public static final int DREAM_START_REASON_TEST_REQUESTED = 5;

    @Hide
    @IntDef(prefix = "DREAM_START_REASON_", value = {
            DREAM_START_REASON_UNKNOWN,
            DREAM_START_REASON_POWER_MANAGER_REQUESTED,
            DREAM_START_REASON_PLAYLIST_CHANGED,
            DREAM_START_REASON_SYSTEM_COMPONENT_SET,
            DREAM_START_REASON_SYSTEM_COMPONENT_CLEARED,
            DREAM_START_REASON_TEST_REQUESTED,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface DreamStartReason {}

    public static final int DREAM_STOP_REASON_UNKNOWN = 0;
    public static final int DREAM_STOP_REASON_DREAM_FINISHED = 1;
    public static final int DREAM_STOP_REASON_DREAM_RESTARTED = 2;
    public static final int DREAM_STOP_REASON_WAKE_UP_REQUESTED = 3;
    public static final int DREAM_STOP_REASON_POWER_MANAGER_REQUESTED = 4;
    public static final int DREAM_STOP_REASON_POWER_MANAGER_CLEANED_UP = 5;
    public static final int DREAM_STOP_REASON_POWER_BUTTON_SHORT_PRESS = 6;
    public static final int DREAM_STOP_REASON_HOME_BUTTON_SHORT_PRESS = 7;
    public static final int DREAM_STOP_REASON_SLOW_TO_CONNECT = 8;
    public static final int DREAM_STOP_REASON_SLOW_TO_FINISH = 9;
    public static final int DREAM_STOP_REASON_ATTACH_TO_SERVICE_FAILED = 10;
    public static final int DREAM_STOP_REASON_BIND_TO_SERVICE_FAILED = 11;
    public static final int DREAM_STOP_REASON_BIND_TO_SERVICE_FORBIDDEN = 12;
    public static final int DREAM_STOP_REASON_START_ACTIVITY_FAILED = 13;
    public static final int DREAM_STOP_REASON_BINDER_DIED = 14;
    public static final int DREAM_STOP_REASON_SERVICE_DISCONNECTED = 15;
    public static final int DREAM_STOP_REASON_SHELL_REQUESTED = 16;
    public static final int DREAM_STOP_REASON_USER_SWITCHED = 17;
    public static final int DREAM_STOP_REASON_PREVIOUS_DREAM_CLEANED_UP = 18;
    public static final int DREAM_STOP_REASON_ACTIVITY_STARTED = 19;

    @Hide
    @IntDef(prefix = "DREAM_STOP_REASON_", value = {
            DREAM_STOP_REASON_UNKNOWN,
            DREAM_STOP_REASON_DREAM_FINISHED,
            DREAM_STOP_REASON_DREAM_RESTARTED,
            DREAM_STOP_REASON_WAKE_UP_REQUESTED,
            DREAM_STOP_REASON_POWER_MANAGER_REQUESTED,
            DREAM_STOP_REASON_POWER_MANAGER_CLEANED_UP,
            DREAM_STOP_REASON_POWER_BUTTON_SHORT_PRESS,
            DREAM_STOP_REASON_HOME_BUTTON_SHORT_PRESS,
            DREAM_STOP_REASON_SLOW_TO_CONNECT,
            DREAM_STOP_REASON_SLOW_TO_FINISH,
            DREAM_STOP_REASON_ATTACH_TO_SERVICE_FAILED,
            DREAM_STOP_REASON_BIND_TO_SERVICE_FAILED,
            DREAM_STOP_REASON_BIND_TO_SERVICE_FORBIDDEN,
            DREAM_STOP_REASON_START_ACTIVITY_FAILED,
            DREAM_STOP_REASON_BINDER_DIED,
            DREAM_STOP_REASON_SERVICE_DISCONNECTED,
            DREAM_STOP_REASON_SHELL_REQUESTED,
            DREAM_STOP_REASON_USER_SWITCHED,
            DREAM_STOP_REASON_PREVIOUS_DREAM_CLEANED_UP,
            DREAM_STOP_REASON_ACTIVITY_STARTED,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface DreamStopReason {}

    public static String dreamStartReasonToString(@DreamStartReason int reason) {
        return switch (reason) {
            case DREAM_START_REASON_UNKNOWN -> "unknown";
            case DREAM_START_REASON_POWER_MANAGER_REQUESTED -> "power_manager_requested";
            case DREAM_START_REASON_PLAYLIST_CHANGED -> "playlist_changed";
            case DREAM_START_REASON_SYSTEM_COMPONENT_SET -> "system_component_set";
            case DREAM_START_REASON_SYSTEM_COMPONENT_CLEARED -> "system_component_cleared";
            case DREAM_START_REASON_TEST_REQUESTED -> "test_requested";
            default -> Integer.toString(reason);
        };
    }

    public static String dreamStopReasonToString(@DreamStopReason int reason) {
        return switch (reason) {
            case DREAM_STOP_REASON_UNKNOWN -> "unknown";
            case DREAM_STOP_REASON_DREAM_FINISHED -> "dream_finished";
            case DREAM_STOP_REASON_DREAM_RESTARTED -> "dream_restarted";
            case DREAM_STOP_REASON_WAKE_UP_REQUESTED -> "wake_up_requested";
            case DREAM_STOP_REASON_POWER_MANAGER_REQUESTED -> "power_manager_requested";
            case DREAM_STOP_REASON_POWER_MANAGER_CLEANED_UP -> "power_manager_cleaned_up";
            case DREAM_STOP_REASON_POWER_BUTTON_SHORT_PRESS -> "power_button_short_press";
            case DREAM_STOP_REASON_HOME_BUTTON_SHORT_PRESS -> "home_button_short_press";
            case DREAM_STOP_REASON_SLOW_TO_CONNECT -> "slow_to_connect";
            case DREAM_STOP_REASON_SLOW_TO_FINISH -> "slow_to_finish";
            case DREAM_STOP_REASON_ATTACH_TO_SERVICE_FAILED -> "attach_to_service_failed";
            case DREAM_STOP_REASON_BIND_TO_SERVICE_FAILED -> "bind_to_service_failed";
            case DREAM_STOP_REASON_BIND_TO_SERVICE_FORBIDDEN -> "bind_to_service_forbidden";
            case DREAM_STOP_REASON_START_ACTIVITY_FAILED -> "start_activity_failed";
            case DREAM_STOP_REASON_BINDER_DIED -> "binder_died";
            case DREAM_STOP_REASON_SERVICE_DISCONNECTED -> "service_disconnected";
            case DREAM_STOP_REASON_SHELL_REQUESTED -> "shell_requested";
            case DREAM_STOP_REASON_USER_SWITCHED -> "user_switched";
            case DREAM_STOP_REASON_PREVIOUS_DREAM_CLEANED_UP -> "previous_dream_cleaned_up";
            case DREAM_STOP_REASON_ACTIVITY_STARTED -> "activity_started";
            default -> Integer.toString(reason);
        };
    }

    /** Returns true if the given dream stop reason indicates a failure to start a dream. */
    public static boolean isDreamStartFailure(@DreamStopReason int reason) {
        return switch (reason) {
            case DREAM_STOP_REASON_SLOW_TO_CONNECT,
                 DREAM_STOP_REASON_ATTACH_TO_SERVICE_FAILED,
                 DREAM_STOP_REASON_BIND_TO_SERVICE_FAILED,
                 DREAM_STOP_REASON_BIND_TO_SERVICE_FORBIDDEN,
                 DREAM_STOP_REASON_START_ACTIVITY_FAILED -> true;
            default -> false;
        };
    }

    /**
     * Called by the power manager to start a dream.
     *
     * @param doze If true, starts the doze dream component if one has been configured,
     * otherwise starts the user-specified dream.
     * @param reason The reason to start dreaming, which is logged to help debugging.
     */
    public abstract void startDream(boolean doze, @DreamStartReason int reason);

    /**
     * Called by the power manager to stop a dream.
     *
     * @param immediate If true, ends the dream summarily, otherwise gives it some time
     * to perform a proper exit transition.
     * @param reason The reason to stop dreaming, which is logged to help debugging.
     */
    public abstract void stopDream(boolean immediate, @DreamStopReason int reason);

    /**
     * Called by the power manager to determine whether a dream is running.
     */
    public abstract boolean isDreaming();

    /**
     * Ask the power manager to nap.  It will eventually call back into startDream() if/when it is
     * appropriate to start dreaming.
     */
    public abstract void requestDream();

    /**
     * Whether dreaming can start given user settings and the current dock/charge state.
     *
     * @param isScreenOn True if the screen is currently on.
     */
    public abstract boolean canStartDreaming(boolean isScreenOn);

    /**
     * Whether or not the device is currently in the user's "when to dream" state, ex.
     * docked & charging.
     */
    public abstract boolean dreamConditionActive();

    /**
     * Register a {@link DreamManagerStateListener}, which will be called when there are changes to
     * dream state.
     *
     * @param listener The listener to register.
     */
    public abstract void registerDreamManagerStateListener(DreamManagerStateListener listener);

    /**
     * Unregister a {@link DreamManagerStateListener}, which will be called when there are changes
     * to dream state.
     *
     * @param listener The listener to unregister.
     */
    public abstract void unregisterDreamManagerStateListener(DreamManagerStateListener listener);

    /**
     * Register a {@link DreamManagerPolicyListener}, which will be called when there are changes to
     * dream policy.
     *
     * @param listener The listener to register.
     */
    public abstract void registerDreamManagerPolicyListener(DreamManagerPolicyListener listener);

    /**
     * Unregister a {@link DreamManagerPolicyListener}, which will be called when there are changes
     * to dream policy.
     *
     * @param listener The listener to unregister.
     */
    public abstract void unregisterDreamManagerPolicyListener(DreamManagerPolicyListener listener);

    /** Called when there are changes to dream state. */
    public interface DreamManagerStateListener {

        /** Called when dreaming has started. */
        default void onDreamingStarted() {
        }

        /** Called when dreaming has stopped. */
        default void onDreamingStopped(@DreamStopReason int reason) {
        }
    }

    /** Called when there are changes to dream policy. */
    public interface DreamManagerPolicyListener {
        /**
         * Called when keep dreaming when plug has changed.
         *
         * @param keepDreaming True if the current dream should continue when undocking.
         */
        // TODO(b/458631623): Remove this once flag_dream_policy_enabled is removed.
        default void onKeepDreamingWhenUnpluggingChanged(boolean keepDreaming) {
        }

        /**
         * Called when the dream policy has changed by user settings or device state.
         *
         * @param newPolicy The new {@link DreamPolicy} to be applied to all dream requests.
         */
        default void onDreamPolicyChanged(@NonNull DreamPolicy newPolicy) {
        }
    }
}
