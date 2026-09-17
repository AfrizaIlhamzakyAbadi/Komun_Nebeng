/*
 * Copyright 2026 The Android Open Source Project
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

package android.service.personalcontext;

import android.annotation.Hide;
import android.annotation.UserIdInt;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

/**
 * Metadata included with all calls to PersonalContextManager.
 */
@Hide
public final class PersonalContextManagerMetadata implements Parcelable {
    @UserIdInt
    private final int mUserId;

    @NonNull
    private final String mPackageName;

    /** Creates a new instance with data filled in from {@link Context}. */
    public PersonalContextManagerMetadata(@NonNull Context context) {
        this(context.getUserId(), context.getPackageName());
    }

    /** For testing purposes only. */
    public PersonalContextManagerMetadata(@UserIdInt int userId, @NonNull String packageName) {
        mUserId = userId;
        mPackageName = packageName;
    }

    private PersonalContextManagerMetadata(@NonNull Parcel src) {
        mUserId = src.readInt();
        mPackageName = src.readString8();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mUserId);
        dest.writeString8(mPackageName);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @UserIdInt
    public int getUserId() {
        return mUserId;
    }

    @NonNull
    public String getPackageName() {
        return mPackageName;
    }

    public static final @NonNull Creator<PersonalContextManagerMetadata> CREATOR = new Creator<>() {
        @Override
        public PersonalContextManagerMetadata createFromParcel(Parcel in) {
            return new PersonalContextManagerMetadata(in);
        }

        @Override
        public PersonalContextManagerMetadata[] newArray(int size) {
            return new PersonalContextManagerMetadata[size];
        }
    };
}
