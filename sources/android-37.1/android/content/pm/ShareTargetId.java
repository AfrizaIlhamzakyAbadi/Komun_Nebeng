/**
 * Copyright (C) 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.content.pm;

import static com.android.internal.util.Preconditions.checkNotNull;

import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SystemApi;
import android.app.people.flags.Flags;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.Objects;

/**
 * Device-wide unique identifier for a {@link ShortcutManager.ShareShortcutInfo}.
 */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
public final class ShareTargetId implements Parcelable {
    @NonNull private final UserPackage mUserPackage;
    @NonNull private final String mClassName;
    @NonNull private final String mId;

    /**
     * Creates a new {@link ShareTargetId} with the given {@link UserPackage}, class name and ID.
     */
    public ShareTargetId(
            @NonNull UserPackage userPackage, @NonNull String className, @NonNull String id) {
        mUserPackage = checkNotNull(userPackage, "userPackage cannot be null");
        mClassName = checkNotNull(className, "className cannot be null");
        mId = checkNotNull(id, "id cannot be null");
    }

    /** Returns the {@link UserPackage} of this {@link ShortcutManager.ShareShortcutInfo}. */
    @NonNull
    public UserPackage getUserPackage() {
        return mUserPackage;
    }

    /** Returns the class name of this {@link ShortcutManager.ShareShortcutInfo}. */
    @NonNull
    public String getClassName() {
        return mClassName;
    }

    /** Returns the ID of this {@link ShortcutManager.ShareShortcutInfo}. */
    @NonNull
    public String getId() {
        return mId;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof ShareTargetId other) {
            return Objects.equals(mUserPackage, other.mUserPackage)
                    && Objects.equals(mClassName, other.mClassName)
                    && Objects.equals(mId, other.mId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mUserPackage, mClassName, mId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedObject(mUserPackage, flags);
        dest.writeString8(mClassName);
        dest.writeString8(mId);
    }

    @NonNull
    public static final Parcelable.Creator<ShareTargetId> CREATOR =
            new Parcelable.Creator<ShareTargetId>() {
                @Override
                public ShareTargetId createFromParcel(@NonNull Parcel in) {
                    UserPackage userPackage = in.readTypedObject(UserPackage.CREATOR);
                    String className = in.readString8();
                    String id = in.readString8();
                    return new ShareTargetId(userPackage, className, id);
                }

                @Override
                public ShareTargetId[] newArray(int size) {
                    return new ShareTargetId[size];
                }
            };
}
