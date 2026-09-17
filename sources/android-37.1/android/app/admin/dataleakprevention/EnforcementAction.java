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

package android.app.admin.dataleakprevention;

import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * Represents the outcome of a rule match.
 */
@Hide
public final class EnforcementAction implements Parcelable {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
        ACCESS_CONTROL_ALLOWED,
        ACCESS_CONTROL_BLOCKED,
        ACCESS_CONTROL_REDACTED
    })
    public @interface AccessControl {}

    public static final int ACCESS_CONTROL_ALLOWED = 1;
    public static final int ACCESS_CONTROL_BLOCKED = 2;
    public static final int ACCESS_CONTROL_REDACTED = 3;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({REPORTING_DISABLED, REPORTING_ENABLED})
    public @interface Reporting {}

    public static final int REPORTING_DISABLED = 1;
    public static final int REPORTING_ENABLED = 2;

    /**
     * The default enforcement action. Permits the data leak event and does not report it.
     */
    public static final EnforcementAction DEFAULT_ALLOW =
            new EnforcementAction(ACCESS_CONTROL_ALLOWED, REPORTING_DISABLED);

    private final @AccessControl int mAccessControl;
    private final @Reporting int mReporting;

    public EnforcementAction(@AccessControl int accessControl, @Reporting int reporting) {
        if (accessControl != ACCESS_CONTROL_ALLOWED
                && accessControl != ACCESS_CONTROL_BLOCKED
                && accessControl != ACCESS_CONTROL_REDACTED) {
            throw new IllegalArgumentException("Invalid access control: " + accessControl);
        }
        if (reporting != REPORTING_DISABLED
                && reporting != REPORTING_ENABLED) {
            throw new IllegalArgumentException("Invalid reporting: " + reporting);
        }
        mAccessControl = accessControl;
        mReporting = reporting;
    }

    private EnforcementAction(Parcel in) {
        mAccessControl = in.readInt();
        mReporting = in.readInt();
    }

    public @AccessControl int getAccessControl() {
        return mAccessControl;
    }

    public @Reporting int getReporting() {
        return mReporting;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EnforcementAction that = (EnforcementAction) o;
        return mAccessControl == that.mAccessControl && mReporting == that.mReporting;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mAccessControl, mReporting);
    }

    @Override
    public String toString() {
        return "EnforcementAction {mAccessControl="
                + mAccessControl
                + ", mReporting="
                + mReporting
                + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mAccessControl);
        dest.writeInt(mReporting);
    }

    @NonNull
    public static final Parcelable.Creator<EnforcementAction> CREATOR =
            new Parcelable.Creator<EnforcementAction>() {
                @Override
                public EnforcementAction createFromParcel(Parcel in) {
                    return new EnforcementAction(in);
                }

                @Override
                public EnforcementAction[] newArray(int size) {
                    return new EnforcementAction[size];
                }
            };
}
