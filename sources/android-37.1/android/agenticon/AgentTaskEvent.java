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
import android.app.PendingIntent;
import android.graphics.drawable.Icon;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.Objects;

/**
 * Information about a one-time agent-related event to communicate to the user.
 *
 * <p>These will be shown to the user in the status bar for a short period of time, possibly hiding
 * the current {@link AgentTaskState}.
 *
 * <p>These events may not be shown in some cases, for example when many are sent in a short period
 * of time, or if the user's status bar is hidden, or if other critical information must be
 * displayed in the status bar instead.
 */
@Hide
@FlaggedApi(Flags.FLAG_STATUS_BAR_AGENT_ICON)
public final class AgentTaskEvent implements Parcelable {

    @NonNull
    private final CharSequence mText;
    @Nullable
    private final Icon mTextBackground;
    @Nullable
    private final Icon mIcon;
    @NonNull
    private final PendingIntent mClickAction;

    private AgentTaskEvent(
            CharSequence text,
            Icon textBackground,
            Icon icon,
            PendingIntent clickAction
    ) {
        mText = text;
        mTextBackground = textBackground;
        mIcon = icon;
        mClickAction = clickAction;
    }

    private AgentTaskEvent(Parcel in) {
        mText = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        mTextBackground = in.readTypedObject(Icon.CREATOR);
        mIcon = in.readTypedObject(Icon.CREATOR);
        mClickAction = in.readTypedObject(PendingIntent.CREATOR);
    }

    /**
     * Text to show to the user.
     * <p>
     * The text may be truncated if there's not enough space to show it.
     *
     * @return the text to show as part of the event.
     */
    @NonNull
    public CharSequence getText() {
        return mText;
    }

    /**
     * Background drawable to use behind the text.
     *
     * <p>This drawable will be masked to an appropriate shape and size, without changing the
     * underlying proportions of the drawable. The mask will be centered on the scaled drawable.
     * It is recommended for the drawable to be "wide", to ensure that top and bottom are not
     * cropped.
     *
     * <p>If {@code null}, the text background will be chosen by the implementation.
     *
     * @return the background to use for the text. If {@code null}, the implementation will decide.
     */
    @Nullable
    public Icon getTextBackground() {
        return mTextBackground;
    }

    /**
     * Optional icon to show alongside the text.
     *
     * <p>This icon will be shown over the {@link #getTextBackground()}.
     *
     * @return the icon to show alongside the text. If {@code null}, no icon will be shown.
     */
    @Nullable
    public Icon getIcon() {
        return mIcon;
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
        TextUtils.writeToParcel(mText, dest, flags);
        dest.writeTypedObject(mTextBackground, flags);
        dest.writeTypedObject(mIcon, flags);
        dest.writeTypedObject(mClickAction, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public static final Parcelable.Creator<AgentTaskEvent> CREATOR =
            new Parcelable.Creator<AgentTaskEvent>() {
                @Override
                public AgentTaskEvent createFromParcel(Parcel in) {
                    return new AgentTaskEvent(in);
                }

                @Override
                public AgentTaskEvent[] newArray(int size) {
                    return new AgentTaskEvent[size];
                }
            };

    /**
     * Builder for {@link AgentTaskEvent}.
     */
    @FlaggedApi(Flags.FLAG_STATUS_BAR_AGENT_ICON)
    public static final class Builder {
        private CharSequence mText;
        private Icon mTextBackground;
        private Icon mIcon;
        private PendingIntent mClickAction;

        /**
         * Creates a new Builder.
         *
         * @param text   the text to show.
         * @param action the action to perform on click.
         * @throws IllegalArgumentException if {@code action} does not target an
         * {@link android.app.Activity}.
         */
        public Builder(@NonNull CharSequence text, @NonNull PendingIntent action) {
            mText = Objects.requireNonNull(text);
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
            mText = other.mText;
            mTextBackground = other.mTextBackground;
            mIcon = other.mIcon;
            mClickAction = other.mClickAction;
        }

        /**
         * Sets the text to show.
         *
         * @param text the text to show as part of this event. It's recommended that it be short to
         *             fit in a reduced space.
         * @return this {@link Builder}
         */
        @NonNull
        public Builder setText(@NonNull CharSequence text) {
            mText = Objects.requireNonNull(text);
            return this;
        }

        /**
         * Sets the text background.
         *
         * <p>This drawable will be masked to an appropriate shape and size, without changing the
         * underlying proportions of the drawable. The mask will be centered on the scaled drawable.
         * It is recommended for the drawable to be "wide", to ensure that top and bottom are not
         * cropped.
         *
         * <p>If {@code null}, the text background will be chosen by the implementation.
         * @param background a drawable to use for the background of the text.
         * @return this {@link Builder}
         */
        @NonNull
        public Builder setTextBackground(@Nullable Icon background) {
            mTextBackground = background;
            return this;
        }

        /**
         * Sets the optional icon to show alongside the text.
         *
         * <p>The icon will be shown alongside the text, over the {@link #getTextBackground()}.
         *
         * @param icon the icon to show alongside the text. If {@code null}, no icon will be shown.
         * @return this {@link Builder}
         */
        @NonNull
        public Builder setIcon(@Nullable Icon icon) {
            mIcon = icon;
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
         * Creates the {@link AgentTaskEvent} instance.
         */
        @NonNull
        public AgentTaskEvent build() {
            return new AgentTaskEvent(mText, mTextBackground, mIcon, mClickAction);
        }
    }
}
