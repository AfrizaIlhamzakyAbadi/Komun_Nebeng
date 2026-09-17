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

package android.agenticon;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.app.Flags;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Outcome of a call to {@link android.app.StatusBarManager#setAgentTask}.
 */
@Hide
@FlaggedApi(Flags.FLAG_STATUS_BAR_AGENT_ICON)
public final class AgentTaskOutcome implements Parcelable {
    private static final int FLAG_AGENT_TASK_RESULT_STATE_CHANGED = 1 << 0;
    private static final int FLAG_AGENT_TASK_RESULT_EVENT_SHOWN = 1 << 1;

    private final int mResult;

    private AgentTaskOutcome(int result) {
        mResult = result;
    }

    private AgentTaskOutcome(Parcel in) {
        mResult = in.readInt();
    }

    /**
     * Returns {@code true} if the state has been successfully changed to the requested state.
     *
     * <p>If the corresponding {@link AgentTaskUpdate} request did not contain an
     * {@link AgentTaskState} (i.e. {@link AgentTaskUpdate#getAgentTaskState()} was {@code null}),
     * this value should be ignored.
     */
    public boolean isStateChanged() {
        return (mResult & FLAG_AGENT_TASK_RESULT_STATE_CHANGED) != 0;
    }

    /**
     * Returns {@code true} if the event has been displayed to the user in the status bar.
     *
     * <p>If the corresponding {@link AgentTaskUpdate} request did not contain an
     * {@link AgentTaskEvent} (i.e. {@link AgentTaskUpdate#getAgentTaskEvent()} was {@code null}),
     * this value should be ignored.
     */
    public boolean isEventShown() {
        return (mResult & FLAG_AGENT_TASK_RESULT_EVENT_SHOWN) != 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mResult);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public static final Parcelable.Creator<AgentTaskOutcome> CREATOR =
            new Parcelable.Creator<AgentTaskOutcome>() {
                @Override
                public AgentTaskOutcome createFromParcel(Parcel in) {
                    return new AgentTaskOutcome(in);
                }

                @Override
                public AgentTaskOutcome[] newArray(int size) {
                    return new AgentTaskOutcome[size];
                }
            };

    /**
     * Builder class for {@link AgentTaskOutcome}.
     */
    @FlaggedApi(Flags.FLAG_STATUS_BAR_AGENT_ICON)
    public static final class Builder {
        private int mResult = 0;

        /**
         * Creates a new {@link Builder}.
         */
        public Builder() {}

        /**
         * Sets whether the state has been successfully changed to the requested state.
         */
        @NonNull
        public Builder setStateChanged(boolean stateChanged) {
            if (stateChanged) {
                mResult |= FLAG_AGENT_TASK_RESULT_STATE_CHANGED;
            } else {
                mResult &= ~FLAG_AGENT_TASK_RESULT_STATE_CHANGED;
            }
            return this;
        }

        /**
         * Sets whether the event has been displayed to the user in the status bar.
         */
        @NonNull
        public Builder setEventShown(boolean eventShown) {
            if (eventShown) {
                mResult |= FLAG_AGENT_TASK_RESULT_EVENT_SHOWN;
            } else {
                mResult &= ~FLAG_AGENT_TASK_RESULT_EVENT_SHOWN;
            }
            return this;
        }

        /**
         * Builds and returns a new {@link AgentTaskOutcome} instance.
         */
        @NonNull
        public AgentTaskOutcome build() {
            return new AgentTaskOutcome(mResult);
        }
    }
}
