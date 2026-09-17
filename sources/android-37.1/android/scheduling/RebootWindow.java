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
package android.scheduling;

import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.annotation.SystemApi;

import com.android.scheduling.flags.Flags;

import java.time.LocalTime;

/** Represents a daily time interval during which a reboot can be performed. */
@FlaggedApi(Flags.FLAG_ENABLE_REBOOT_SCHEDULER_IMPLEMENTATION)
@SystemApi
public final class RebootWindow {
    private LocalTime mStartTime;
    private LocalTime mEndTime;

    @SystemApi
    public RebootWindow(@NonNull LocalTime startTime, @NonNull LocalTime endTime) {
        mStartTime = startTime;
        mEndTime = endTime;
    }

    /**
     * @return the start time of the reboot window.
     */
    @NonNull
    @SystemApi
    public LocalTime getStartTime() {
        return mStartTime;
    }

    /**
     * @return the end time of the reboot window.
     */
    @NonNull
    @SystemApi
    public LocalTime getEndTime() {
        return mEndTime;
    }

    @Override
    public String toString() {
        return "RebootWindow{" + "mStartTime=" + mStartTime + ", mEndTime=" + mEndTime + '}';
    }
}
