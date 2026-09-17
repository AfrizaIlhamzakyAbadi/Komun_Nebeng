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
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Data Leak Prevention (DLP) configuration.
 */
@Hide
public final class DataLeakPrevention implements Parcelable {

    private final List<DataLeakPreventionRule> mRules;

    public DataLeakPrevention(@NonNull List<DataLeakPreventionRule> rules) {
        mRules = new ArrayList<>(Objects.requireNonNull(rules));
    }

    private DataLeakPrevention(Parcel in) {
        mRules = new ArrayList<>();
        byte[] bytes = in.readBlob();
        if (bytes != null) {
            Parcel data = Parcel.obtain();
            try {
                data.unmarshall(bytes, 0, bytes.length);
                data.setDataPosition(0);
                data.readTypedList(mRules, DataLeakPreventionRule.CREATOR);
            } finally {
                data.recycle();
            }
        }
    }

    @NonNull
    public List<DataLeakPreventionRule> getRules() {
        return Collections.unmodifiableList(mRules);
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataLeakPrevention other = (DataLeakPrevention) o;
        return Objects.equals(mRules, other.mRules);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mRules);
    }

    @Override
    public String toString() {
        return "DataLeakPrevention {mRules=" + mRules + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        Parcel data = Parcel.obtain();
        try {
            data.writeTypedList(mRules);
            dest.writeBlob(data.marshall());
        } finally {
            data.recycle();
        }
    }

    @NonNull
    public static final Parcelable.Creator<DataLeakPrevention> CREATOR =
            new Parcelable.Creator<DataLeakPrevention>() {
                @Override
                public DataLeakPrevention createFromParcel(Parcel in) {
                    return new DataLeakPrevention(in);
                }

                @Override
                public DataLeakPrevention[] newArray(int size) {
                    return new DataLeakPrevention[size];
                }
            };
}