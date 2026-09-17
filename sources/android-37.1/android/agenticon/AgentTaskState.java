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
import android.app.PendingIntent;
import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.Objects;

/**
 * Information about the agent's current state. This information is shown as an icon in the status
 * bar.
 *
 * <p>This icon can be clicked to open the agent application.
 */
@Hide
@FlaggedApi(Flags.FLAG_STATUS_BAR_AGENT_ICON)
public final class AgentTaskState implements Parcelable {
    @NonNull
    private final Icon mIcon;
    @NonNull
    private final CharSequence mContentDescription;
    private final boolean mShouldInterruptPreviousAnimation;
    @NonNull
    private final PendingIntent mClickAction;

    private AgentTaskState(Icon icon, CharSequence contentDescription,
            boolean shouldInterruptPreviousAnimation, PendingIntent clickAction) {
        mIcon = icon;
        mContentDescription = contentDescription;
        mShouldInterruptPreviousAnimation = shouldInterruptPreviousAnimation;
        mClickAction = clickAction;
    }

    private AgentTaskState(Parcel in) {
        mIcon = in.readTypedObject(Icon.CREATOR);
        mContentDescription = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        mShouldInterruptPreviousAnimation = in.readBoolean();
        mClickAction = in.readTypedObject(PendingIntent.CREATOR);
    }

    /**
     * Icon to display in the status bar.
     *
     * <p>This icon will be used until the next {@link AgentTaskState} is sent.
     *
     * <p>The icon should be a monochrome icon that will be tinted for contrast purposes.
     *
     * <p>The icon can be an animation. In that case, the animation must be finite in duration. This
     * animation will be played to completion before switching to the next {@link AgentTaskState}.
     * This behavior can be interrupted by {@link #shouldInterruptPreviousAnimation()} being
     * {@code true}.
     * <p>
     * Infinite animation behavior is unspecified.
     *
     * @return the icon representing the current state of the agent.
     */
    @NonNull
    public Icon getIcon() {
        return mIcon;
    }

    /**
     * Content description associated with {@link #getIcon}.
     *
     * <p>This content description will be concatenated with the application name, and may be hidden
     * in sensitive contexts.
     *
     * @return a description of the icon.
     */
    @NonNull
    public CharSequence getContentDescription() {
        return mContentDescription;
    }

    /**
     * Indicates that this new state's icon should interrupt the animation of the previous state's
     * icon.
     *
     * <p>If the previous state's icon is an animation, when this is {@code true}, this new
     * {@link AgentTaskState} will interrupt that animation, immediately moving to the new state
     * (and icon), without waiting for the previous animation to complete.
     *
     * <p>If the previous icon is not animated, this value is ignored.
     *
     * @return {@code true} if this state's icon should interrupt the previous state's animation,
     *         instead of playing it until completion.
     */
    public boolean shouldInterruptPreviousAnimation() {
        return mShouldInterruptPreviousAnimation;
    }

    /**
     * {@link PendingIntent} to send when the event text in the status bar is clicked.
     *
     * <p>The {@link PendingIntent} should target an {@link android.app.Activity}.
     * <p>If the {@link PendingIntent} was created with {@link PendingIntent#FLAG_MUTABLE}, as well
     * as {@link android.content.Intent#FILL_IN_SOURCE_BOUNDS}, then the source bounds will be
     * filled in with the coordinates on screen of the UI element that triggered this intent
     *
     * @return the action to execute when the element is clicked.
     * @see android.content.Intent#getSourceBounds
     */
    @NonNull
    public PendingIntent getClickAction() {
        return mClickAction;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedObject(mIcon, flags);
        TextUtils.writeToParcel(mContentDescription, dest, flags);
        dest.writeBoolean(mShouldInterruptPreviousAnimation);
        dest.writeTypedObject(mClickAction, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public static final Parcelable.Creator<AgentTaskState> CREATOR =
            new Parcelable.Creator<AgentTaskState>() {
                @Override
                public AgentTaskState createFromParcel(Parcel in) {
                    return new AgentTaskState(in);
                }

                @Override
                public AgentTaskState[] newArray(int size) {
                    return new AgentTaskState[size];
                }
            };

    /**
     * Builder for {@link AgentTaskState}.
     */
    @FlaggedApi(Flags.FLAG_STATUS_BAR_AGENT_ICON)
    public static final class Builder {
        private Icon mIcon;
        private CharSequence mContentDescription;
        private boolean mShouldInterruptPreviousAnimation;
        private PendingIntent mClickAction;

        /**
         * Creates a new Builder.
         *
         * @param icon               the icon to display.
         * @param contentDescription the content description for accessibility.
         * @param action             the action to perform on click.
         * @throws IllegalArgumentException if {@code action} does not target an
         * {@link android.app.Activity}.
         */
        public Builder(
                @NonNull Icon icon,
                @NonNull CharSequence contentDescription,
                @NonNull PendingIntent action
        ) {
            mIcon = Objects.requireNonNull(icon);
            mContentDescription = Objects.requireNonNull(contentDescription);
            mClickAction = Objects.requireNonNull(action);
            if (!mClickAction.isActivity()) {
                throw new IllegalArgumentException("PendingIntent must target an Activity");
            }
        }

        /**
         * Creates a new Builder from another Builder.
         *
         * @param other a {@link Builder} to copy
         */
        public Builder(@NonNull Builder other) {
            mIcon = other.mIcon;
            mContentDescription = other.mContentDescription;
            mShouldInterruptPreviousAnimation = other.mShouldInterruptPreviousAnimation;
            mClickAction = other.mClickAction;
        }

        /**
         * Sets the icon and content description.
         *
         * @param icon representing the next state to show.
         * @param contentDescription associated with the icon
         * @return this {@link Builder}
         */
        @NonNull
        public Builder setIconAndContentDescription(
                @NonNull Icon icon,
                @NonNull CharSequence contentDescription
        ) {
            mIcon = Objects.requireNonNull(icon);
            mContentDescription = Objects.requireNonNull(contentDescription);
            return this;
        }

        /**
         * Sets whether this new state should interrupt the animation from the previous state.
         *
         * @param shouldInterruptPreviousAnimation {@code true} if this new state's icon should
         *                                         interrupt the animation from the previous state's
         *                                         icon
         * @return this {@link Builder}
         */
        @NonNull
        public Builder setShouldInterruptPreviousAnimation(
                boolean shouldInterruptPreviousAnimation
        ) {
            mShouldInterruptPreviousAnimation = shouldInterruptPreviousAnimation;
            return this;
        }

        /**
         * Sets the click action.
         *
         * <p>If the {@link PendingIntent} was created with {@link PendingIntent#FLAG_MUTABLE}, as
         * well as {@link android.content.Intent#FILL_IN_SOURCE_BOUNDS}, then the source bounds will
         * be filled in with the coordinates on screen of the UI element that triggered this intent.
         *
         * @param action a {@link PendingIntent} targeting an {@link android.app.Activity}
         * @return this {@link Builder}
         * @throws IllegalArgumentException if {@code action} does not target an
         * {@link android.app.Activity}.
         */
        @NonNull
        public Builder setClickAction(@NonNull PendingIntent action) {
            mClickAction = Objects.requireNonNull(action);
            if (!mClickAction.isActivity()) {
                throw new IllegalArgumentException("PendingIntent must target an Activity");
            }
            return this;
        }

        /**
         * Creates the {@link AgentTaskState} instance.
         */
        @NonNull
        public AgentTaskState build() {
            return new AgentTaskState(mIcon, mContentDescription, mShouldInterruptPreviousAnimation,
                    mClickAction);
        }
    }
}
