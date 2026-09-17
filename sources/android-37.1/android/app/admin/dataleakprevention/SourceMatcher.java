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

import java.util.Objects;

/**
 * Defines source criteria for DLP.
 */
@Hide
public final class SourceMatcher implements Parcelable {
    private final AppMatcher mAppMatcher;

    public SourceMatcher(@NonNull AppMatcher appMatcher) {
        mAppMatcher = Objects.requireNonNull(appMatcher);
    }

    private SourceMatcher(Parcel in) {
        mAppMatcher = in.readTypedObject(AppMatcher.CREATOR);
        Objects.requireNonNull(mAppMatcher);
    }

    @NonNull
    public AppMatcher getAppMatcher() {
        return mAppMatcher;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SourceMatcher that = (SourceMatcher) o;
        return Objects.equals(mAppMatcher, that.mAppMatcher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mAppMatcher);
    }

    @Override
    public String toString() {
        return "SourceMatcher {mAppMatcher=" + mAppMatcher + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedObject(mAppMatcher, flags);
    }

    @NonNull
    public static final Parcelable.Creator<SourceMatcher> CREATOR =
            new Parcelable.Creator<SourceMatcher>() {
                @Override
                public SourceMatcher createFromParcel(Parcel in) {
                    return new SourceMatcher(in);
                }

                @Override
                public SourceMatcher[] newArray(int size) {
                    return new SourceMatcher[size];
                }
            };
}