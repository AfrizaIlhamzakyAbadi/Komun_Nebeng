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
import android.view.MotionEvent;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * Represents a trigger based on a physical button press on a peripheral (e.g., a mouse).
 */
@FlaggedApi(FLAG_ENABLE_POEM_INPUT_CUSTOMIZATION)
public final class ButtonCustomizationTrigger extends CustomizationTrigger {

    /** Customizable Button code: Tertiary/Middle button. */
    public static final int CUSTOMIZABLE_BUTTON_TERTIARY = MotionEvent.BUTTON_TERTIARY;
    /** Customizable Button code: Back button. */
    public static final int CUSTOMIZABLE_BUTTON_BACK = MotionEvent.BUTTON_BACK;
    /** Customizable Button code: Forward button. */
    public static final int CUSTOMIZABLE_BUTTON_FORWARD = MotionEvent.BUTTON_FORWARD;
    /** Customizable Button code: Extra button. */
    public static final int CUSTOMIZABLE_BUTTON_EXTRA = MotionEvent.BUTTON_EXTRA;
    /** Customizable Button code: Side button. */
    public static final int CUSTOMIZABLE_BUTTON_SIDE = MotionEvent.BUTTON_SIDE;
    /** Customizable Button code: Task button. */
    public static final int CUSTOMIZABLE_BUTTON_TASK = MotionEvent.BUTTON_TASK;

    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(prefix = {"CUSTOMIZABLE_BUTTON_"}, value = {
            CUSTOMIZABLE_BUTTON_TERTIARY,
            CUSTOMIZABLE_BUTTON_BACK,
            CUSTOMIZABLE_BUTTON_FORWARD,
            CUSTOMIZABLE_BUTTON_EXTRA,
            CUSTOMIZABLE_BUTTON_SIDE,
            CUSTOMIZABLE_BUTTON_TASK
    })
    public @interface CustomizableButton {
    }

    /**
     * Creates a Trigger for a specific button code.
     *
     * @param buttonCode The button code for the trigger.
     */
    public ButtonCustomizationTrigger(@CustomizableButton int buttonCode) {
        super(new AidlCustomizationTrigger());
        checkArgument(isValidButtonCode(buttonCode),
                "Invalid customizable button code: %s",
                MotionEvent.buttonStateToString(buttonCode));
        mAidlTrigger.mType = TRIGGER_TYPE_BUTTON;
        mAidlTrigger.mCode = buttonCode;
    }

    @Hide
    public ButtonCustomizationTrigger(@NonNull AidlCustomizationTrigger aidlTrigger) {
        super(Objects.requireNonNull(aidlTrigger));
        checkArgument(aidlTrigger.mType == TRIGGER_TYPE_BUTTON,
                "Expected TRIGGER_TYPE_BUTTON, but got %s", typeToString(aidlTrigger.mType));
        checkArgument(isValidButtonCode(aidlTrigger.mCode),
                "Invalid customizable button code: %s",
                MotionEvent.buttonStateToString(aidlTrigger.mCode));
    }

    /**
     * Returns the button code associated with this trigger.
     */
    @CustomizableButton
    public int getButtonCode() {
        return mAidlTrigger.mCode;
    }

    @Override
    public String toString() {
        return "ButtonCustomizationTrigger { "
                + "type=" + typeToString(getTriggerType())
                + ", buttonCode=" + MotionEvent.buttonStateToString(mAidlTrigger.mCode)
                + " }";
    }

    /**
     * Checks if the provided button code is supported for customization.
     *
     * @param buttonCode The button code to check.
     * @return {@code true} if the button code can be customized, {@code false} otherwise.
     */
    @Hide
    public static boolean isValidButtonCode(@CustomizableButton int buttonCode) {
        return switch (buttonCode) {
            case CUSTOMIZABLE_BUTTON_TERTIARY,
                 CUSTOMIZABLE_BUTTON_BACK,
                 CUSTOMIZABLE_BUTTON_FORWARD,
                 CUSTOMIZABLE_BUTTON_EXTRA,
                 CUSTOMIZABLE_BUTTON_SIDE,
                 CUSTOMIZABLE_BUTTON_TASK -> true;
            default -> false;
        };
    }
}
