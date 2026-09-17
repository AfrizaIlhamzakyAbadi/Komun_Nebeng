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
import java.util.Arrays;
import java.util.Objects;

/**
 * Defines destination criteria for DLP.
 */
@Hide
public final class DestinationMatcher implements Parcelable {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
        TRUSTED_MANAGED_LEVEL_SELF,
        TRUSTED_MANAGED_LEVEL_UNTRUSTED
    })
    public @interface DestinationTrustedManagedLevel {}

    // Destination user is the same as the managed source user.
    public static final int TRUSTED_MANAGED_LEVEL_SELF = 1;
    // Destination user is different from the managed source user.
    public static final int TRUSTED_MANAGED_LEVEL_UNTRUSTED = 2;

    // The trust relationship with the source, default to match ANY if empty.
    // trustedLevels would consider match if any value of the list matches.
    private final @NonNull int[] mTrustedLevels;
    private final AppMatcher mAppMatcher;

    public DestinationMatcher(
            @NonNull @DestinationTrustedManagedLevel int[] trustedLevels,
            @NonNull AppMatcher appMatcher) {
        mTrustedLevels = Objects.requireNonNull(trustedLevels).clone();
        for (int level : mTrustedLevels) {
            checkTrustedLevel(level);
        }
        mAppMatcher = Objects.requireNonNull(appMatcher);
    }

    private DestinationMatcher(Parcel in) {
        mTrustedLevels = Objects.requireNonNull(in.createIntArray());
        for (int level : mTrustedLevels) {
            checkTrustedLevel(level);
        }
        mAppMatcher = in.readTypedObject(AppMatcher.CREATOR);
        Objects.requireNonNull(mAppMatcher);
    }

    private void checkTrustedLevel(@DestinationTrustedManagedLevel int level) {
        if (level != TRUSTED_MANAGED_LEVEL_SELF
                && level != TRUSTED_MANAGED_LEVEL_UNTRUSTED) {
            throw new IllegalArgumentException("Invalid trusted level: " + level);
        }
    }

    @NonNull
    public @DestinationTrustedManagedLevel int[] getDestinationTrustedManagedLevels() {
        return mTrustedLevels.clone();
    }

    @NonNull
    public AppMatcher getAppMatcher() {
        return mAppMatcher;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DestinationMatcher that = (DestinationMatcher) o;
        return Arrays.equals(mTrustedLevels, that.mTrustedLevels)
                && Objects.equals(mAppMatcher, that.mAppMatcher);
    }

    @Override
    public int hashCode() {
        return Objects.hash(Arrays.hashCode(mTrustedLevels), mAppMatcher);
    }

    @Override
    public String toString() {
        return "DestinationMatcher {mTrustedLevels="
                + Arrays.toString(mTrustedLevels)
                + ", mAppMatcher="
                + mAppMatcher
                + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeIntArray(mTrustedLevels);
        dest.writeTypedObject(mAppMatcher, flags);
    }

    @NonNull
    public static final Parcelable.Creator<DestinationMatcher> CREATOR =
            new Parcelable.Creator<DestinationMatcher>() {
                @Override
                public DestinationMatcher createFromParcel(Parcel in) {
                    return new DestinationMatcher(in);
                }

                @Override
                public DestinationMatcher[] newArray(int size) {
                    return new DestinationMatcher[size];
                }
            };
}