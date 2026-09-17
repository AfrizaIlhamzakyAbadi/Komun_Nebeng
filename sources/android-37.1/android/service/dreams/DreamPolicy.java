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

package android.service.dreams;

import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Dream manager policy.
 *
 * <p>This can be used to determine when dreams can be started or if they can continue. To access
 * the current system policy use {@link DreamManagerInternal.DreamManagerPolicyListener}.
 *
 * @param shouldRequestOnScreenTimeout True if the dream should be requested on screen timeout.
 * @param shouldKeepDreamingWhenUnplugged True if the dream should continue when the device is
 *      unplugged.
 * @param isDreamEnabled Whether dreams are enabled based on user settings and device state, e.g.
 *     device is charging, battery level is sufficient, etc.
 * @param dreamRestrictions The restrictions in place for dreams being allowed to start or continue.
 *
 * Only for use within the system server.
 */
@Hide
public record DreamPolicy(
    boolean shouldRequestOnScreenTimeout,
    boolean shouldKeepDreamingWhenUnplugged,
    boolean isDreamEnabled,
    @Restriction int dreamRestrictions
) {
    /**
     * Dreams only allowed if ambient display is not being suppressed.
     */
    public static final int RESTRICTION_AMBIENT_DISPLAY_NOT_SUPPRESSED = 1 << 0;

    /**
     * Dreams only allowed if the device is being kept awake.
     *
     * <p>This restriction is applied when:
     * <ul>
     *   <li>The device is charging but still below the min battery level for dreaming.
     *   <li>The device is dreaming but the battery drain is over the threshold.
     * </ul>
     */
    public static final int RESTRICTION_DEVICE_KEPT_AWAKE = 1 << 1;

    @IntDef(prefix = "RESTRICTION_", flag = true, value = {
            RESTRICTION_AMBIENT_DISPLAY_NOT_SUPPRESSED,
            RESTRICTION_DEVICE_KEPT_AWAKE,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Restriction {}

    /** Returns a string representation of given dream restrictions. */
    @NonNull
    public static String restrictionToString(@Restriction int restrictions) {
        return Arrays.toString(restrictionToStringArray(restrictions));
    }

    /** Returns a string array representing given dream restrictions. */
    @NonNull
    public static String[] restrictionToStringArray(@Restriction int restrictions) {
        if (restrictions == 0) {
            return new String[0];
        }
        ArrayList<String> restrictionList = new ArrayList<>();
        if ((restrictions & RESTRICTION_AMBIENT_DISPLAY_NOT_SUPPRESSED) != 0) {
            restrictionList.add("ambient_display_not_suppressed");
        }
        if ((restrictions & RESTRICTION_DEVICE_KEPT_AWAKE) != 0) {
            restrictionList.add("device_kept_awake");
        }
        return restrictionList.toArray(new String[0]);
    }

    public DreamPolicy() {
        this(false, false, false, 0);
    }

    /**
     * Returns whether a dream is allowed to start or continue given the satisfied restrictions.
     *
     * <p>This applies both {@link #isDreamEnabled()} and {@link #dreamRestrictions()} to determine
     * if a dream can happen given the current state of the device.
     */
    public boolean isDreamAllowed(@Restriction int satisfiedRestrictions) {
        // All restrictions must be satisfied for dreams to be allowed.
        return isDreamEnabled && ((dreamRestrictions & satisfiedRestrictions) == dreamRestrictions);
    }

    @Override
    public String toString() {
        return "DreamPolicy{"
            + "shouldRequestOnScreenTimeout=" + shouldRequestOnScreenTimeout
            + ", shouldKeepDreamingWhenUnplugged=" + shouldKeepDreamingWhenUnplugged
            + ", isDreamEnabled=" + isDreamEnabled
            + ", dreamRestrictions=" + restrictionToString(dreamRestrictions)
            + '}';
    }

    /** Returns a short single-line string with stable formatting for debugging. */
    public String toShortString() {
        return TextUtils.formatSimple("dreamWhenScreenTimeout=%5s | keepDreamingWhenUnplugged=%5s"
                        + " | enabled=%5s | restrictions=%s",
                shouldRequestOnScreenTimeout, shouldKeepDreamingWhenUnplugged, isDreamEnabled,
                restrictionToString(dreamRestrictions));
    }

    /**
     * Returns whether the dream policy is the same as the given parameters.
     *
     * <p>This can be used to determine if the dream policy has changed without instantiating a new
     * DreamPolicy object for comparison.
     */
    public boolean isSamePolicy(
            boolean shouldRequestOnScreenTimeout,
            boolean shouldKeepDreamingWhenUnplugged,
            boolean isDreamEnabled,
            @Restriction int dreamRestrictions) {
        return this.shouldRequestOnScreenTimeout == shouldRequestOnScreenTimeout
                && this.shouldKeepDreamingWhenUnplugged == shouldKeepDreamingWhenUnplugged
                && this.isDreamEnabled == isDreamEnabled
                && this.dreamRestrictions == dreamRestrictions;
    }
}
