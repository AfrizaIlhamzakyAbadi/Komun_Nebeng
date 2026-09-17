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

package android.uilatencystats;

import android.annotation.Hide;
import android.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Represents a UI latency event type and event-specific data associated with {@link
 * android.uilatencystats.Event}. {@link android.uilatencystats.UiLatencyEventListener} will receive
 * events of these types and convert them to statsd metrics.
 */
@Hide
public sealed interface EventType {
    int EVENT_USER_SWITCH = 0;
    int EVENT_LAUNCHER_SHOWN = 1;
    int EVENT_LOCK_SCREEN_UNLOCK_START = 2;
    int EVENT_BOOT_COMPLETED = 3;

    /**
     * Integer IDs for event types.
     */
    @Hide
    @IntDef(
            prefix = {"EVENT_"},
            value = {
                EVENT_USER_SWITCH,
                EVENT_LAUNCHER_SHOWN,
                EVENT_LOCK_SCREEN_UNLOCK_START,
                EVENT_BOOT_COMPLETED,
            })
    @Retention(RetentionPolicy.SOURCE)
    @interface Id {}

    /**
     * Returns the ID of the event.
     */
    @Hide
    @Id
    int getId();

    /**
     * Returns the name of the event.
     */
    @Hide
    String getName();

    /**
     * The launcher is rendered to the user for the first time in the app lifecycle. Launchers still
     * will send this multiple times as they restart.
     */
    @Hide
    record LauncherShown() implements EventType {

        @Hide
        @Override
        public @Id int getId() {
            return EVENT_LAUNCHER_SHOWN;
        }

        @Hide
        @Override
        public String getName() {
            return "LauncherShown";
        }
    }

    /**
     * The lockscreen unlocking starts.
     */
    @Hide
    record LockScreenUnlockStart() implements EventType {

        @Hide
        @Override
        public @Id int getId() {
            return EVENT_LOCK_SCREEN_UNLOCK_START;
        }

        @Hide
        @Override
        public String getName() {
            return "LockScreenUnlockStart";
        }
    }

    /**
     * A user switch has occurred.
     */
    @Hide
    record UserSwitch(int toUserId) implements EventType {

        @Hide
        @Override
        public @Id int getId() {
            return EVENT_USER_SWITCH;
        }

        @Hide
        @Override
        public String getName() {
            return "UserSwitch";
        }
    }

    /**
     * Boot has completed (SystemService.PHASE_BOOT_COMPLETED).
     */
    @Hide
    record BootCompleted() implements EventType {

        @Hide
        @Override
        public @Id int getId() {
            return EVENT_BOOT_COMPLETED;
        }

        @Hide
        @Override
        public String getName() {
            return "BootCompleted";
        }
    }
}
