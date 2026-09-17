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

package android.hardware.input;

import static com.android.hardware.input.Flags.FLAG_ENABLE_POEM_INPUT_CUSTOMIZATION;
import static com.android.internal.util.Preconditions.checkArgument;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.ravenwood.annotation.RavenwoodKeepWholeClass;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * Represents predefined simple actions, such as Volume up/down, Screenshot, etc.
 */
@FlaggedApi(FLAG_ENABLE_POEM_INPUT_CUSTOMIZATION)
@RavenwoodKeepWholeClass
public final class SimpleCustomizationAction extends CustomizationAction {

    /**
     * Action type: Back.
     * <p>
     * Specifies the action to go back when the peripheral input is triggered.
     */
    public static final int TYPE_BACK = SimpleActionType.BACK;

    /**
     * Action type: Brightness down.
     * <p>
     * Specifies the action to decrease screen brightness when the peripheral input is triggered.
     */
    public static final int TYPE_BRIGHTNESS_DOWN = SimpleActionType.BRIGHTNESS_DOWN;

    /**
     * Action type: Brightness up.
     * <p>
     * Specifies the action to increase screen brightness when the peripheral input is triggered.
     */
    public static final int TYPE_BRIGHTNESS_UP = SimpleActionType.BRIGHTNESS_UP;

    /**
     * Action type: Calculator.
     * <p>
     * Specifies the action to launch calculator app when the peripheral input is triggered.
     */
    public static final int TYPE_CALCULATOR = SimpleActionType.CALCULATOR;

    /**
     * Action type: Dictation.
     * <p>
     * Specifies the action to start dictation when the peripheral input is triggered.
     */
    public static final int TYPE_DICTATION = SimpleActionType.DICTATION;

    /**
     * Action type: Emoji menu.
     * <p>
     * Specifies the action to show emoji menu when the peripheral input is triggered.
     */
    public static final int TYPE_EMOJI_MENU = SimpleActionType.EMOJI_MENU;

    /**
     * Action type: Forward.
     * <p>
     * Specifies the action to go forward when the peripheral input is triggered.
     */
    public static final int TYPE_FORWARD = SimpleActionType.FORWARD;

    /**
     * Action type: Lock screen.
     * <p>
     * Specifies the action to lock screen when the peripheral input is triggered.
     */
    public static final int TYPE_LOCK = SimpleActionType.LOCK;

    /**
     * Action type: Media next.
     * <p>
     * Specifies the action to skip to the next media track when the peripheral input is triggered.
     */
    public static final int TYPE_MEDIA_NEXT = SimpleActionType.MEDIA_NEXT;

    /**
     * Action type: Media play/pause.
     * <p>
     * Specifies the action to toggle media play/pause when the peripheral input is triggered.
     */
    public static final int TYPE_MEDIA_PLAY_PAUSE = SimpleActionType.MEDIA_PLAY_PAUSE;

    /**
     * Action type: Media previous.
     * <p>
     * Specifies the action to skip to the previous media track when the peripheral input is
     * triggered.
     */
    public static final int TYPE_MEDIA_PREVIOUS = SimpleActionType.MEDIA_PREVIOUS;

    /**
     * Action type: Print screen.
     * <p>
     * Specifies the action to take a snapshot that is first sent to the foreground
     * app for handling. If the app does not consume it, the framework captures the
     * screen, mimicking {@link android.view.KeyEvent#KEYCODE_SYSRQ}.
     */
    public static final int TYPE_PRINT_SCREEN = SimpleActionType.PRINT_SCREEN;

    /**
     * Action type: Screen capture.
     * <p>
     * Specifies the action to trigger a global framework-level screenshot. Unlike
     * {@link #TYPE_PRINT_SCREEN}, this action bypasses the
     * foreground app and typically displays system UI tools for editing or sharing.
     */
    public static final int TYPE_SCREEN_CAPTURE = SimpleActionType.SCREEN_CAPTURE;

    /**
     * Action type: Show desktop.
     * <p>
     * Specifies the action to show desktop when the peripheral input is triggered.
     */
    public static final int TYPE_SHOW_DESKTOP = SimpleActionType.SHOW_DESKTOP;

    /**
     * Action type: Volume down.
     * <p>
     * Specifies the action to decrease the current system volume level when the peripheral input
     * is triggered.
     */
    public static final int TYPE_VOLUME_DOWN = SimpleActionType.VOLUME_DOWN;

    /**
     * Action type: Volume mute.
     * <p>
     * Specifies the action to toggle the system mute state or mute the current volume when the
     * peripheral input is triggered.
     */
    public static final int TYPE_VOLUME_MUTE = SimpleActionType.VOLUME_MUTE;

    /**
     * Action type: Volume up.
     * <p>
     * Specifies the action to increase the current system volume level when the peripheral input
     * is triggered.
     */
    public static final int TYPE_VOLUME_UP = SimpleActionType.VOLUME_UP;

    /**
     * Action type: Do nothing.
     * <p>
     * Specifies that no action should occur when the peripheral input is triggered,
     * effectively swallowing the default hardware behavior.
     */
    public static final int TYPE_DO_NOTHING = SimpleActionType.DO_NOTHING;

    /**
     * Action type: Close window.
     * <p>
     * Specifies the action to close the currently focused window when the
     * peripheral input is triggered.
     */
    public static final int TYPE_CLOSE_WINDOW = SimpleActionType.CLOSE_WINDOW;

    /**
     * Action type: Partial Screenshot.
     * <p>
     * Specifies the action to activate the system region capture utility to take a partial
     * screenshot when the peripheral input is triggered.
     */
    public static final int TYPE_PARTIAL_SCREENSHOT = SimpleActionType.PARTIAL_SCREENSHOT;

    /**
     * Action type: Assist.
     * <p>
     * Specifies the action to launch the default assistant for textual input when the peripheral
     * input is triggered.
     */
    public static final int TYPE_ASSIST = SimpleActionType.ASSIST;

    /**
     * Action type: Contextual Insert.
     * <p>
     * Specifies the action to trigger a contextual panel for inserting content into the focused
     * input field when the peripheral input is triggered.
     */
    public static final int TYPE_CONTEXTUAL_INSERT = SimpleActionType.CONTEXTUAL_INSERT;

    /**
     * Action type: Contextual Search.
     * <p>
     * Specifies the action to launch a search experience that allows the user to
     * search for information about any content displayed on the screen - using gestures like
     * circling, highlighting, or tapping - without switching apps, when the peripheral
     * input is triggered.
     */
    public static final int TYPE_CONTEXTUAL_SEARCH = SimpleActionType.CONTEXTUAL_SEARCH;

    /**
     * Action type: Contextual Query.
     * <p>
     * Specifies the action to launch the contextual cursor. When available, this intelligent
     * pointer analyzes screen elements beneath the cursor (such as images or text paragraphs)
     * and displays contextual suggestions to discover available actions (like copy, translate,
     * or share) when the peripheral input is triggered.
     */
    public static final int TYPE_CONTEXTUAL_QUERY = SimpleActionType.CONTEXTUAL_QUERY;

    /**
     * Action type: Voice Assist.
     * <p>
     * Specifies the action to launch the default assistant for voice input when the peripheral
     * input is triggered.
     */
    public static final int TYPE_VOICE_ASSIST = SimpleActionType.VOICE_ASSIST;

    @Hide
    @IntDef(prefix = "TYPE_", value = {
            TYPE_BACK,
            TYPE_BRIGHTNESS_DOWN,
            TYPE_BRIGHTNESS_UP,
            TYPE_CALCULATOR,
            TYPE_DICTATION,
            TYPE_EMOJI_MENU,
            TYPE_FORWARD,
            TYPE_LOCK,
            TYPE_MEDIA_NEXT,
            TYPE_MEDIA_PLAY_PAUSE,
            TYPE_MEDIA_PREVIOUS,
            TYPE_PRINT_SCREEN,
            TYPE_SCREEN_CAPTURE,
            TYPE_SHOW_DESKTOP,
            TYPE_VOLUME_DOWN,
            TYPE_VOLUME_MUTE,
            TYPE_VOLUME_UP,
            TYPE_DO_NOTHING,
            TYPE_CLOSE_WINDOW,
            TYPE_PARTIAL_SCREENSHOT,
            TYPE_ASSIST,
            TYPE_CONTEXTUAL_INSERT,
            TYPE_CONTEXTUAL_SEARCH,
            TYPE_CONTEXTUAL_QUERY,
            TYPE_VOICE_ASSIST
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Type {
    }

    @NonNull
    private final AidlSimpleAction mAidlSimpleAction;

    /**
     * Creates a new {@link SimpleCustomizationAction} with the specified action type.
     *
     * @param actionType The type of action to be performed, such as
     * {@link #TYPE_SCREEN_CAPTURE}.
     * @throws IllegalArgumentException if the provided action type does not match any
     * defined {@link Type}.
     */
    public SimpleCustomizationAction(@Type int actionType) {
        // Manual validation because @Type is SOURCE-retained.
        checkArgument(actionType >= TYPE_BACK && actionType <= TYPE_VOICE_ASSIST,
                "Invalid Type: %s", actionType);

        mAidlSimpleAction = new AidlSimpleAction();
        mAidlSimpleAction.actionType = actionType;
    }


    /**
     * Returns the specific type of action to be performed.
     * <p>
     * The returned value corresponds to one of the action type constants,
     * such as {@link #TYPE_SCREEN_CAPTURE} which defines the behavior
     * triggered by the peripheral customization.
     *
     * @return the type of action
     */
    @Type
    public int getType() {
        return mAidlSimpleAction.actionType;
    }

    @Hide
    @NonNull
    public AidlSimpleAction getAidl() {
        return mAidlSimpleAction;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SimpleCustomizationAction that = (SimpleCustomizationAction) o;
        return Objects.equals(mAidlSimpleAction, that.mAidlSimpleAction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mAidlSimpleAction);
    }
}
