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
import android.annotation.Nullable;
import android.app.Flags;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Data to request a change in the UI for Agent Task space in the status bar.
 *
 * <ul>
 *   <li>If {@link #getAgentTaskState()} is non {@code null}, this will become the current state and
 *       its icon will be shown (when there are no events to show) until the next request for state
 *       is placed. If this is {@code null}, the last sent state will be maintained.
 *   <li>If {@link #getAgentTaskEvent()} is non {@code null}, the text will be shown for a short
 *       period of time. It is possible that in some circumstances, the text cannot be shown.
 *   <li>If both elements are {@code null}, no changes will be applied.
 * </ul>
 */
@Hide
@FlaggedApi(Flags.FLAG_STATUS_BAR_AGENT_ICON)
public final class AgentTaskUpdate implements Parcelable {

    private final AgentTaskState mAgentTaskState;
    private final AgentTaskEvent mAgentTaskEvent;

    private AgentTaskUpdate(AgentTaskState agentTaskState, AgentTaskEvent agentTaskEvent) {
        mAgentTaskState = agentTaskState;
        mAgentTaskEvent = agentTaskEvent;
    }

    private AgentTaskUpdate(Parcel in) {
        mAgentTaskState = in.readTypedObject(AgentTaskState.CREATOR);
        mAgentTaskEvent = in.readTypedObject(AgentTaskEvent.CREATOR);
    }

    /**
     * The next state to show in the status bar space. If {@code null}, the last state will be
     * maintained.
     */
    @Nullable
    public AgentTaskState getAgentTaskState() {
        return mAgentTaskState;
    }

    /**
     * An event to show in the status bar for a short period of time. If {@code null}, no event will
     * be shown.
     */
    @Nullable
    public AgentTaskEvent getAgentTaskEvent() {
        return mAgentTaskEvent;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedObject(mAgentTaskState, flags);
        dest.writeTypedObject(mAgentTaskEvent, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public static final Parcelable.Creator<AgentTaskUpdate> CREATOR =
            new Parcelable.Creator<AgentTaskUpdate>() {
                @Override
                public AgentTaskUpdate createFromParcel(Parcel in) {
                    return new AgentTaskUpdate(in);
                }

                @Override
                public AgentTaskUpdate[] newArray(int size) {
                    return new AgentTaskUpdate[size];
                }
            };

    /**
     * Builder for {@link AgentTaskUpdate}.
     */
    @FlaggedApi(Flags.FLAG_STATUS_BAR_AGENT_ICON)
    public static final class Builder {
        private AgentTaskState mAgentTaskState;
        private AgentTaskEvent mAgentTaskEvent;

        /**
         * Creates a new Builder.
         */
        public Builder() {}

        /**
         * Creates a new Builder from another Builder.
         */
        public Builder(@NonNull Builder other) {
            mAgentTaskState = other.mAgentTaskState;
            mAgentTaskEvent = other.mAgentTaskEvent;
        }

        /**
         * Sets the agent task state.
         */
        @NonNull
        public Builder setAgentTaskState(@Nullable AgentTaskState agentTaskState) {
            mAgentTaskState = agentTaskState;
            return this;
        }

        /**
         * Sets the agent task event.
         */
        @NonNull
        public Builder setAgentTaskEvent(@Nullable AgentTaskEvent agentTaskEvent) {
            mAgentTaskEvent = agentTaskEvent;
            return this;
        }

        /**
         * Creates the {@link AgentTaskUpdate} instance.
         */
        @NonNull
        public AgentTaskUpdate build() {
            return new AgentTaskUpdate(mAgentTaskState, mAgentTaskEvent);
        }
    }
}
