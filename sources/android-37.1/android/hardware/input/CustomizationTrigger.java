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
import android.annotation.Nullable;
import android.ravenwood.annotation.RavenwoodKeepWholeClass;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * Represents a trigger that initiates a peripheral customization action.
 *
 * <p>This is an abstract base class. Use specific subclasses like {@link KeyCustomizationTrigger}
 * to represent different types of input triggers.</p>
 */
@FlaggedApi(FLAG_ENABLE_POEM_INPUT_CUSTOMIZATION)
@RavenwoodKeepWholeClass
public abstract class CustomizationTrigger {
    /**
     * Indicates that the customization trigger is initiated by a physical key press, typically
     * from a keyboard. These triggers correspond to standard  {@link android.view.KeyEvent} codes
     */
    @Hide public static final int TRIGGER_TYPE_KEY = 1;

    /**
     * Indicates that the customization trigger is initiated by a button press on a pointing device,
     * such as a mouse or stylus. These triggers correspond to {@link android.view.MotionEvent}
     * button states.
     */
    @Hide public static final int TRIGGER_TYPE_BUTTON = 2;

    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(prefix = {"TRIGGER_TYPE_"}, value = {
            TRIGGER_TYPE_KEY,
            TRIGGER_TYPE_BUTTON,
    })
    public @interface TriggerType {
    }

    @Hide
    @NonNull
    protected final AidlCustomizationTrigger mAidlTrigger;

    CustomizationTrigger(@NonNull AidlCustomizationTrigger aidlTrigger) {
        this.mAidlTrigger = Objects.requireNonNull(aidlTrigger);
    }

    @Hide
    @NonNull
    public AidlCustomizationTrigger getAidlTrigger() {
        return mAidlTrigger;
    }

    /**
     * Returns the type of the trigger, indicating the customizable code this trigger holds.
     * For example, {@link #TRIGGER_TYPE_KEY} indicates the trigger is a key press.
     */
    @Hide
    public @TriggerType int getTriggerType() {
        return mAidlTrigger.mType;
    }

    static String typeToString(int type) {
        return switch (type) {
            case TRIGGER_TYPE_KEY -> "TRIGGER_TYPE_KEY";
            case TRIGGER_TYPE_BUTTON -> "TRIGGER_TYPE_BUTTON";
            default -> "UNKNOWN_TYPE (" + type + ")";
        };
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomizationTrigger)) return false;
        CustomizationTrigger that = (CustomizationTrigger) o;

        return this.mAidlTrigger.mType == that.mAidlTrigger.mType
                && this.mAidlTrigger.mCode == that.mAidlTrigger.mCode;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mAidlTrigger.mType, mAidlTrigger.mCode);
    }
}
