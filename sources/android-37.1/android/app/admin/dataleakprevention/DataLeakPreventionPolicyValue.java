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
import android.app.admin.PolicyValue;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * Policy value representing a Data Leak Prevention (DLP) configuration.
 */
@Hide
public final class DataLeakPreventionPolicyValue extends PolicyValue<DataLeakPrevention> {

    public DataLeakPreventionPolicyValue(@NonNull DataLeakPrevention value) {
        super(value);
    }

    private DataLeakPreventionPolicyValue(Parcel in) {
        super(in.readTypedObject(DataLeakPrevention.CREATOR));
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataLeakPreventionPolicyValue other = (DataLeakPreventionPolicyValue) o;
        return Objects.equals(getValue(), other.getValue());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getValue());
    }

    @Override
    public String toString() {
        return "DataLeakPreventionPolicyValue {mValue=" + getValue() + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedObject(getValue(), flags);
    }

    @NonNull
    public static final Parcelable.Creator<DataLeakPreventionPolicyValue> CREATOR =
            new Parcelable.Creator<DataLeakPreventionPolicyValue>() {
                @Override
                public DataLeakPreventionPolicyValue createFromParcel(Parcel in) {
                    return new DataLeakPreventionPolicyValue(in);
                }

                @Override
                public DataLeakPreventionPolicyValue[] newArray(int size) {
                    return new DataLeakPreventionPolicyValue[size];
                }
            };
}