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

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.ravenwood.annotation.RavenwoodKeepWholeClass;
import android.view.KeyEvent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * Represents a trigger based on a physical key press on a peripheral.
 */
@FlaggedApi(FLAG_ENABLE_POEM_INPUT_CUSTOMIZATION)
@RavenwoodKeepWholeClass
public final class KeyCustomizationTrigger extends CustomizationTrigger {

    /** Customizable Key code: F1 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F1 = KeyEvent.KEYCODE_F1;
    /** Customizable Key code: F2 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F2 = KeyEvent.KEYCODE_F2;
    /** Customizable Key code: F3 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F3 = KeyEvent.KEYCODE_F3;
    /** Customizable Key code: F4 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F4 = KeyEvent.KEYCODE_F4;
    /** Customizable Key code: F5 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F5 = KeyEvent.KEYCODE_F5;
    /** Customizable Key code: F6 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F6 = KeyEvent.KEYCODE_F6;
    /** Customizable Key code: F7 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F7 = KeyEvent.KEYCODE_F7;
    /** Customizable Key code: F8 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F8 = KeyEvent.KEYCODE_F8;
    /** Customizable Key code: F9 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F9 = KeyEvent.KEYCODE_F9;
    /** Customizable Key code: F10 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F10 = KeyEvent.KEYCODE_F10;
    /** Customizable Key code: F11 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F11 = KeyEvent.KEYCODE_F11;
    /** Customizable Key code: F12 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F12 = KeyEvent.KEYCODE_F12;
    /** Customizable Key code: F13 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F13 = KeyEvent.KEYCODE_F13;
    /** Customizable Key code: F14 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F14 = KeyEvent.KEYCODE_F14;
    /** Customizable Key code: F15 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F15 = KeyEvent.KEYCODE_F15;
    /** Customizable Key code: F16 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F16 = KeyEvent.KEYCODE_F16;
    /** Customizable Key code: F17 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F17 = KeyEvent.KEYCODE_F17;
    /** Customizable Key code: F18 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F18 = KeyEvent.KEYCODE_F18;
    /** Customizable Key code: F19 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F19 = KeyEvent.KEYCODE_F19;
    /** Customizable Key code: F20 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F20 = KeyEvent.KEYCODE_F20;
    /** Customizable Key code: F21 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F21 = KeyEvent.KEYCODE_F21;
    /** Customizable Key code: F22 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F22 = KeyEvent.KEYCODE_F22;
    /** Customizable Key code: F23 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F23 = KeyEvent.KEYCODE_F23;
    /** Customizable Key code: F24 key. */
    public static final int CUSTOMIZABLE_KEYCODE_F24 = KeyEvent.KEYCODE_F24;
    /** Customizable Key code: Assist key. */
    public static final int CUSTOMIZABLE_KEYCODE_ASSIST = KeyEvent.KEYCODE_ASSIST;
    /** Customizable Key code: Back key. */
    public static final int CUSTOMIZABLE_KEYCODE_BACK = KeyEvent.KEYCODE_BACK;
    /** Customizable Key code: Bookmark key. */
    public static final int CUSTOMIZABLE_KEYCODE_BOOKMARK = KeyEvent.KEYCODE_BOOKMARK;
    /** Customizable Key code: Brightness Down key. */
    public static final int CUSTOMIZABLE_KEYCODE_BRIGHTNESS_DOWN = KeyEvent.KEYCODE_BRIGHTNESS_DOWN;
    /** Customizable Key code: Brightness Up key. */
    public static final int CUSTOMIZABLE_KEYCODE_BRIGHTNESS_UP = KeyEvent.KEYCODE_BRIGHTNESS_UP;
    /** Customizable Key code: Calculator key. */
    public static final int CUSTOMIZABLE_KEYCODE_CALCULATOR = KeyEvent.KEYCODE_CALCULATOR;
    /** Customizable Key code: Calendar key. */
    public static final int CUSTOMIZABLE_KEYCODE_CALENDAR = KeyEvent.KEYCODE_CALENDAR;
    /** Customizable Key code: Camera key. */
    public static final int CUSTOMIZABLE_KEYCODE_CAMERA = KeyEvent.KEYCODE_CAMERA;
    /** Customizable Key code: Contacts key. */
    public static final int CUSTOMIZABLE_KEYCODE_CONTACTS = KeyEvent.KEYCODE_CONTACTS;
    /** Customizable Key code: Dictate key. */
    public static final int CUSTOMIZABLE_KEYCODE_DICTATE = KeyEvent.KEYCODE_DICTATE;
    /** Customizable Key code: Envelope key. */
    public static final int CUSTOMIZABLE_KEYCODE_ENVELOPE = KeyEvent.KEYCODE_ENVELOPE;
    /** Customizable Key code: Explorer key. */
    public static final int CUSTOMIZABLE_KEYCODE_EXPLORER = KeyEvent.KEYCODE_EXPLORER;
    /** Customizable Key code: Forward key. */
    public static final int CUSTOMIZABLE_KEYCODE_FORWARD = KeyEvent.KEYCODE_FORWARD;
    /** Customizable Key code: Full Screen key. */
    public static final int CUSTOMIZABLE_KEYCODE_FULLSCREEN = KeyEvent.KEYCODE_FULLSCREEN;
    /** Customizable Key code: Eject key. */
    public static final int CUSTOMIZABLE_KEYCODE_MEDIA_EJECT = KeyEvent.KEYCODE_MEDIA_EJECT;
    /** Customizable Key code: Next Media key. */
    public static final int CUSTOMIZABLE_KEYCODE_MEDIA_NEXT = KeyEvent.KEYCODE_MEDIA_NEXT;
    /** Customizable Key code: Play/Pause Media key. */
    public static final int CUSTOMIZABLE_KEYCODE_MEDIA_PLAY_PAUSE =
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE;
    /** Customizable Key code: Previous Media key. */
    public static final int CUSTOMIZABLE_KEYCODE_MEDIA_PREVIOUS = KeyEvent.KEYCODE_MEDIA_PREVIOUS;
    /** Customizable Key code: Stop Media key. */
    public static final int CUSTOMIZABLE_KEYCODE_MEDIA_STOP = KeyEvent.KEYCODE_MEDIA_STOP;
    /** Customizable Key code: Menu key. */
    public static final int CUSTOMIZABLE_KEYCODE_MENU = KeyEvent.KEYCODE_MENU;
    /** Customizable Key code: Home key. */
    public static final int CUSTOMIZABLE_KEYCODE_MOVE_HOME = KeyEvent.KEYCODE_MOVE_HOME;
    /** Customizable Key code: Music key. */
    public static final int CUSTOMIZABLE_KEYCODE_MUSIC = KeyEvent.KEYCODE_MUSIC;
    /** Customizable Key code: New key. */
    public static final int CUSTOMIZABLE_KEYCODE_NEW = KeyEvent.KEYCODE_NEW;
    /** Customizable Key code: Screenshot key. */
    public static final int CUSTOMIZABLE_KEYCODE_SCREENSHOT = KeyEvent.KEYCODE_SCREENSHOT;
    /** Customizable Key code: Search key. */
    public static final int CUSTOMIZABLE_KEYCODE_SEARCH = KeyEvent.KEYCODE_SEARCH;
    /** Customizable Key code: System Request / Print Screen key. */
    public static final int CUSTOMIZABLE_KEYCODE_SYSRQ = KeyEvent.KEYCODE_SYSRQ;
    /** Customizable Key code: Volume Down key. */
    public static final int CUSTOMIZABLE_KEYCODE_VOLUME_DOWN = KeyEvent.KEYCODE_VOLUME_DOWN;
    /** Customizable Key code: Volume Mute key. */
    public static final int CUSTOMIZABLE_KEYCODE_VOLUME_MUTE = KeyEvent.KEYCODE_VOLUME_MUTE;
    /** Customizable Key code: Volume Up key. */
    public static final int CUSTOMIZABLE_KEYCODE_VOLUME_UP = KeyEvent.KEYCODE_VOLUME_UP;

    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(prefix = {"CUSTOMIZABLE_KEYCODE_"}, value = {
            CUSTOMIZABLE_KEYCODE_F1,
            CUSTOMIZABLE_KEYCODE_F2,
            CUSTOMIZABLE_KEYCODE_F3,
            CUSTOMIZABLE_KEYCODE_F4,
            CUSTOMIZABLE_KEYCODE_F5,
            CUSTOMIZABLE_KEYCODE_F6,
            CUSTOMIZABLE_KEYCODE_F7,
            CUSTOMIZABLE_KEYCODE_F8,
            CUSTOMIZABLE_KEYCODE_F9,
            CUSTOMIZABLE_KEYCODE_F10,
            CUSTOMIZABLE_KEYCODE_F11,
            CUSTOMIZABLE_KEYCODE_F12,
            CUSTOMIZABLE_KEYCODE_F13,
            CUSTOMIZABLE_KEYCODE_F14,
            CUSTOMIZABLE_KEYCODE_F15,
            CUSTOMIZABLE_KEYCODE_F16,
            CUSTOMIZABLE_KEYCODE_F17,
            CUSTOMIZABLE_KEYCODE_F18,
            CUSTOMIZABLE_KEYCODE_F19,
            CUSTOMIZABLE_KEYCODE_F20,
            CUSTOMIZABLE_KEYCODE_F21,
            CUSTOMIZABLE_KEYCODE_F22,
            CUSTOMIZABLE_KEYCODE_F23,
            CUSTOMIZABLE_KEYCODE_F24,
            CUSTOMIZABLE_KEYCODE_ASSIST,
            CUSTOMIZABLE_KEYCODE_BACK,
            CUSTOMIZABLE_KEYCODE_BOOKMARK,
            CUSTOMIZABLE_KEYCODE_BRIGHTNESS_DOWN,
            CUSTOMIZABLE_KEYCODE_BRIGHTNESS_UP,
            CUSTOMIZABLE_KEYCODE_CALCULATOR,
            CUSTOMIZABLE_KEYCODE_CALENDAR,
            CUSTOMIZABLE_KEYCODE_CAMERA,
            CUSTOMIZABLE_KEYCODE_CONTACTS,
            CUSTOMIZABLE_KEYCODE_DICTATE,
            CUSTOMIZABLE_KEYCODE_ENVELOPE,
            CUSTOMIZABLE_KEYCODE_EXPLORER,
            CUSTOMIZABLE_KEYCODE_FORWARD,
            CUSTOMIZABLE_KEYCODE_FULLSCREEN,
            CUSTOMIZABLE_KEYCODE_MEDIA_EJECT,
            CUSTOMIZABLE_KEYCODE_MEDIA_NEXT,
            CUSTOMIZABLE_KEYCODE_MEDIA_PLAY_PAUSE,
            CUSTOMIZABLE_KEYCODE_MEDIA_PREVIOUS,
            CUSTOMIZABLE_KEYCODE_MEDIA_STOP,
            CUSTOMIZABLE_KEYCODE_MENU,
            CUSTOMIZABLE_KEYCODE_MOVE_HOME,
            CUSTOMIZABLE_KEYCODE_MUSIC,
            CUSTOMIZABLE_KEYCODE_NEW,
            CUSTOMIZABLE_KEYCODE_SCREENSHOT,
            CUSTOMIZABLE_KEYCODE_SEARCH,
            CUSTOMIZABLE_KEYCODE_SYSRQ,
            CUSTOMIZABLE_KEYCODE_VOLUME_DOWN,
            CUSTOMIZABLE_KEYCODE_VOLUME_MUTE,
            CUSTOMIZABLE_KEYCODE_VOLUME_UP
    })
    public @interface CustomizableKeyCode {
    }

    /**
     * Creates a Trigger for a specific key code.
     *
     * @param keyCode The key code for the trigger. Must be a {@link CustomizableKeyCode}.
     */
    public KeyCustomizationTrigger(@CustomizableKeyCode int keyCode) {
        super(new AidlCustomizationTrigger());

        if (!isValidKeyCode(keyCode)) {
            throw new IllegalArgumentException("Invalid customizable key code: "
                    + KeyEvent.keyCodeToString(keyCode));
        }
        mAidlTrigger.mType = TRIGGER_TYPE_KEY;
        mAidlTrigger.mCode = keyCode;
    }

    @Hide
    public KeyCustomizationTrigger(@NonNull AidlCustomizationTrigger aidlTrigger) {
        super(Objects.requireNonNull(aidlTrigger));

        if (aidlTrigger.mType != TRIGGER_TYPE_KEY) {
            throw new IllegalArgumentException("Expected TRIGGER_TYPE_KEY, but got "
                    + typeToString(aidlTrigger.mType));
        }
        if (!isValidKeyCode(aidlTrigger.mCode)) {
            throw new IllegalArgumentException("Invalid customizable key code: "
                    + KeyEvent.keyCodeToString(aidlTrigger.mCode));
        }
    }

    /**
     * Returns the key code associated with this trigger.
     */
    @CustomizableKeyCode
    public int getKeyCode() {
        return mAidlTrigger.mCode;
    }

    @Override
    public String toString() {
        return "KeyCustomizationTrigger { "
                + "type=" + typeToString(getTriggerType())
                + ", keyCode=" + KeyEvent.keyCodeToString(mAidlTrigger.mCode)
                + " }";
    }

    /**
     * Checks if the provided key code is supported for customization.
     *
     * @param keyCode The key code to check.
     * @return {@code true} if the key code can be customized, {@code false} otherwise.
     */
    @Hide
    public static boolean isValidKeyCode(int keyCode) {
        return switch (keyCode) {
            case CUSTOMIZABLE_KEYCODE_F1,
                 CUSTOMIZABLE_KEYCODE_F2,
                 CUSTOMIZABLE_KEYCODE_F3,
                 CUSTOMIZABLE_KEYCODE_F4,
                 CUSTOMIZABLE_KEYCODE_F5,
                 CUSTOMIZABLE_KEYCODE_F6,
                 CUSTOMIZABLE_KEYCODE_F7,
                 CUSTOMIZABLE_KEYCODE_F8,
                 CUSTOMIZABLE_KEYCODE_F9,
                 CUSTOMIZABLE_KEYCODE_F10,
                 CUSTOMIZABLE_KEYCODE_F11,
                 CUSTOMIZABLE_KEYCODE_F12,
                 CUSTOMIZABLE_KEYCODE_F13,
                 CUSTOMIZABLE_KEYCODE_F14,
                 CUSTOMIZABLE_KEYCODE_F15,
                 CUSTOMIZABLE_KEYCODE_F16,
                 CUSTOMIZABLE_KEYCODE_F17,
                 CUSTOMIZABLE_KEYCODE_F18,
                 CUSTOMIZABLE_KEYCODE_F19,
                 CUSTOMIZABLE_KEYCODE_F20,
                 CUSTOMIZABLE_KEYCODE_F21,
                 CUSTOMIZABLE_KEYCODE_F22,
                 CUSTOMIZABLE_KEYCODE_F23,
                 CUSTOMIZABLE_KEYCODE_F24,
                 CUSTOMIZABLE_KEYCODE_ASSIST,
                 CUSTOMIZABLE_KEYCODE_BACK,
                 CUSTOMIZABLE_KEYCODE_BOOKMARK,
                 CUSTOMIZABLE_KEYCODE_BRIGHTNESS_DOWN,
                 CUSTOMIZABLE_KEYCODE_BRIGHTNESS_UP,
                 CUSTOMIZABLE_KEYCODE_CALCULATOR,
                 CUSTOMIZABLE_KEYCODE_CALENDAR,
                 CUSTOMIZABLE_KEYCODE_CAMERA,
                 CUSTOMIZABLE_KEYCODE_CONTACTS,
                 CUSTOMIZABLE_KEYCODE_DICTATE,
                 CUSTOMIZABLE_KEYCODE_ENVELOPE,
                 CUSTOMIZABLE_KEYCODE_EXPLORER,
                 CUSTOMIZABLE_KEYCODE_FORWARD,
                 CUSTOMIZABLE_KEYCODE_FULLSCREEN,
                 CUSTOMIZABLE_KEYCODE_MEDIA_EJECT,
                 CUSTOMIZABLE_KEYCODE_MEDIA_NEXT,
                 CUSTOMIZABLE_KEYCODE_MEDIA_PLAY_PAUSE,
                 CUSTOMIZABLE_KEYCODE_MEDIA_PREVIOUS,
                 CUSTOMIZABLE_KEYCODE_MEDIA_STOP,
                 CUSTOMIZABLE_KEYCODE_MENU,
                 CUSTOMIZABLE_KEYCODE_MOVE_HOME,
                 CUSTOMIZABLE_KEYCODE_MUSIC,
                 CUSTOMIZABLE_KEYCODE_NEW,
                 CUSTOMIZABLE_KEYCODE_SCREENSHOT,
                 CUSTOMIZABLE_KEYCODE_SEARCH,
                 CUSTOMIZABLE_KEYCODE_SYSRQ,
                 CUSTOMIZABLE_KEYCODE_VOLUME_DOWN,
                 CUSTOMIZABLE_KEYCODE_VOLUME_MUTE,
                 CUSTOMIZABLE_KEYCODE_VOLUME_UP -> true;
            default -> false;
        };
    }
}
