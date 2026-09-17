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
package android.window.sharedsurface;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.app.ActivityTaskManager;
import android.content.ComponentName;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.UserHandle;

import java.util.Objects;

/**
 * Allows a SharedAnimationProvider to declare that it can provide a shared surface when a
 * transition occurs with predefined condition.
 */
@Hide
@FlaggedApi(com.android.window.flags.Flags.FLAG_SHARED_SURFACE_TRANSITION_ANIMATION)
public final class SharedAnimationFilter implements Parcelable {

    final ComponentName mComponent;
    final String mPackageName;
    final IBinder mLaunchCookie;
    final int mUserId;
    /**
     * The task ID to match. This is an optional field.
     */
    final int mTaskId;

    private SharedAnimationFilter(Parcel in) {
        mComponent = in.readTypedObject(ComponentName.CREATOR);
        mPackageName = in.readString8();
        mLaunchCookie = in.readStrongBinder();
        mTaskId = in.readInt();
        mUserId = in.readInt();
    }

    private SharedAnimationFilter(@Nullable ComponentName component, @Nullable String packageName,
            @Nullable IBinder launchCookie, int taskId, int userId) {
        mComponent = component;
        mPackageName = packageName;
        mLaunchCookie = launchCookie;
        mTaskId = taskId;
        mUserId = userId;
    }

    public @Nullable ComponentName getComponentName() {
        return mComponent;
    }

    public @Nullable String getPackageName() {
        return mPackageName;
    }

    public @Nullable IBinder getLaunchCookie() {
        return mLaunchCookie;
    }

    public int getTaskId() {
        return mTaskId;
    }

    public int getUserId() {
        return mUserId;
    }

    public static final @NonNull Creator<SharedAnimationFilter> CREATOR =
            new Creator<>() {
                @Override
                public SharedAnimationFilter createFromParcel(Parcel in) {
                    return new SharedAnimationFilter(in);
                }

                @Override
                public SharedAnimationFilter[] newArray(int size) {
                    return new SharedAnimationFilter[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedObject(mComponent, flags);
        dest.writeString8(mPackageName);
        dest.writeStrongBinder(mLaunchCookie);
        dest.writeInt(mTaskId);
        dest.writeInt(mUserId);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj instanceof SharedAnimationFilter other) {
            return Objects.equals(mComponent, other.mComponent)
                    && Objects.equals(mPackageName, other.mPackageName)
                    && Objects.equals(mLaunchCookie, other.mLaunchCookie)
                    && mTaskId == other.mTaskId
                    && mUserId == other.mUserId;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(mComponent, mPackageName, mLaunchCookie, mTaskId, mUserId);
    }

    @Override
    public String toString() {
        return "SharedAnimationFilter{"
                + " userId=" + mUserId
                + " topActivity=" + mComponent
                + " packageName=" + mPackageName
                + " launchCookie=" + mLaunchCookie
                + " taskId=" + mTaskId
                + "}";
    }

    boolean isValid() {
        return mComponent != null || mPackageName != null || mLaunchCookie != null
                || mUserId != UserHandle.USER_NULL;
    }

    /**
     * Builder class for {@link SharedAnimationFilter} class.
     *
     * It is necessary to set at least one property to create a valid filter.
     */
    public static class Builder {
        ComponentName mComponent;
        String mPackageName;
        IBinder mLaunchCookie;
        int mUserId = UserHandle.USER_NULL;
        int mTaskId = ActivityTaskManager.INVALID_TASK_ID;

        /**
         * Sets the {@link ComponentName} of the top activity to match.
         */
        public Builder setComponent(@Nullable ComponentName componentName) {
            if (componentName != null && mPackageName != null
                    && !componentName.getPackageName().equals(mPackageName)) {
                throw new IllegalArgumentException("Component package name "
                        + componentName.getPackageName() + " does not match package "
                        + mPackageName);
            }
            mComponent = componentName;
            return this;
        }

        /**
         * Sets the package name to match.
         */
        public Builder setPackageName(@Nullable String packageName) {
            if (packageName != null && mComponent != null
                    && !mComponent.getPackageName().equals(packageName)) {
                throw new IllegalArgumentException("Package name " + packageName
                        + " does not match component package " + mComponent.getPackageName());
            }
            mPackageName = packageName;
            return this;
        }

        /**
         * Sets the launch cookie to match.
         */
        public Builder setLaunchCookie(@NonNull IBinder launchCookie) {
            mLaunchCookie = launchCookie;
            return this;
        }

        /**
         * Sets the user ID to match.
         */
        public Builder setUserId(int userId) {
            mUserId = userId;
            return this;
        }

        /**
         * Sets the task ID to match.
         */
        public Builder setTaskId(int taskId) {
            mTaskId = taskId;
            return this;
        }

        /**
         * Builds the {@link SharedAnimationFilter}.
         *
         * @return The created filter.
         * @throws IllegalArgumentException if no property was set.
         */
        public @NonNull SharedAnimationFilter build() {
            final SharedAnimationFilter filter = new SharedAnimationFilter(
                    mComponent, mPackageName, mLaunchCookie, mTaskId, mUserId);
            if (!filter.isValid()) {
                throw new IllegalArgumentException("Invalid filter, must fill information to "
                        + "create one");
            }
            return filter;
        }
    }
}
