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

package com.android.internal.policy;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.app.TaskInfo;
import android.app.WindowConfiguration;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;

import java.util.Objects;

/**
 * Exposes windowing mode exemptions info for desktop mode compatibility policies.
 */
public class WindowingExemptionsInfo {
    @Nullable
    private final ComponentName mBaseActivity;
    private final boolean mIsTopActivityNoDisplay;
    private final boolean mIsActivityStackTransparent;
    private final int mNumActivities;
    private final int mUserId;
    @Nullable
    private final ActivityInfo mTopActivityInfo;
    @WindowConfiguration.ActivityType
    private final int mTopActivityType;

    private WindowingExemptionsInfo(Builder builder) {
        this.mBaseActivity = builder.mBaseActivity;
        this.mIsTopActivityNoDisplay = builder.mIsTopActivityNoDisplay;
        this.mIsActivityStackTransparent = builder.mIsActivityStackTransparent;
        this.mNumActivities = builder.mNumActivities;
        this.mUserId = builder.mUserId;
        this.mTopActivityInfo = builder.mTopActivityInfo;
        this.mTopActivityType = builder.mTopActivityType;
    }

    /**
     * Creates a WindowingExemptionsInfo from a TaskInfo object.
     */
    @NonNull
    public static WindowingExemptionsInfo fromTaskInfo(@NonNull TaskInfo taskInfo) {
        return new WindowingExemptionsInfo.Builder()
                .setBaseActivity(taskInfo.baseActivity)
                .setTopActivityNoDisplay(taskInfo.isTopActivityNoDisplay)
                .setActivityStackTransparent(taskInfo.isActivityStackTransparent)
                .setNumActivities(taskInfo.numActivities)
                .setUserId(taskInfo.userId)
                .setTopActivityInfo(taskInfo.topActivityInfo)
                .setTopActivityType(taskInfo.topActivityType)
                .build();
    }

    /**
     * Returns the base activity component name.
     */
    @Nullable
    public ComponentName getBaseActivity() {
        return mBaseActivity;
    }

    /**
     * Returns whether the top activity is no display.
     */
    public boolean isTopActivityNoDisplay() {
        return mIsTopActivityNoDisplay;
    }

    /**
     * Returns whether the activity stack is transparent.
     */
    public boolean isActivityStackTransparent() {
        return mIsActivityStackTransparent;
    }

    /**
     * Returns the number of activities.
     */
    public int getNumActivities() {
        return mNumActivities;
    }

    /**
     * Returns the user ID.
     */
    public int getUserId() {
        return mUserId;
    }

    /**
     * Returns the top activity info.
     */
    @Nullable
    public ActivityInfo getTopActivityInfo() {
        return mTopActivityInfo;
    }

    /**
     * Returns the top activity type.
     */
    @WindowConfiguration.ActivityType
    public int getTopActivityType() {
        return mTopActivityType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WindowingExemptionsInfo)) return false;
        WindowingExemptionsInfo that = (WindowingExemptionsInfo) o;
        return mIsTopActivityNoDisplay == that.mIsTopActivityNoDisplay
                && mIsActivityStackTransparent == that.mIsActivityStackTransparent
                && mNumActivities == that.mNumActivities
                && mUserId == that.mUserId
                && mTopActivityType == that.mTopActivityType
                && Objects.equals(mBaseActivity, that.mBaseActivity)
                && Objects.equals(mTopActivityInfo, that.mTopActivityInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mBaseActivity, mIsTopActivityNoDisplay, mIsActivityStackTransparent,
                mNumActivities, mUserId, mTopActivityInfo, mTopActivityType);
    }

    @Override
    public String toString() {
        return "WindowingExemptionsInfo{"
                + "baseActivity=" + mBaseActivity
                + ", isTopActivityNoDisplay=" + mIsTopActivityNoDisplay
                + ", isActivityStackTransparent=" + mIsActivityStackTransparent
                + ", numActivities=" + mNumActivities
                + ", userId=" + mUserId
                + ", topActivityInfo=" + mTopActivityInfo
                + ", topActivityType=" + mTopActivityType
                + '}';
    }

    /**
     * Builder for WindowingExemptionsInfo.
     */
    public static class Builder {
        private ComponentName mBaseActivity;
        private boolean mIsTopActivityNoDisplay;
        private boolean mIsActivityStackTransparent;
        private int mNumActivities;
        private int mUserId;
        private ActivityInfo mTopActivityInfo;
        private int mTopActivityType;

        /**
         * Sets the base activity component name.
         */
        public Builder setBaseActivity(@Nullable ComponentName baseActivity) {
            this.mBaseActivity = baseActivity;
            return this;
        }

        /**
         * Sets whether the top activity has no display.
         */
        public Builder setTopActivityNoDisplay(boolean topActivityNoDisplay) {
            this.mIsTopActivityNoDisplay = topActivityNoDisplay;
            return this;
        }

        /**
         * Sets whether the top activity has no display.
         */
        public Builder setIsTopActivityNoDisplay(boolean topActivityNoDisplay) {
            this.mIsTopActivityNoDisplay = topActivityNoDisplay;
            return this;
        }

        /**
         * Sets whether the activity stack is transparent.
         */
        public Builder setActivityStackTransparent(boolean activityStackTransparent) {
            this.mIsActivityStackTransparent = activityStackTransparent;
            return this;
        }

        /**
         * Sets whether the activity stack is transparent.
         */
        public Builder setIsActivityStackTransparent(boolean activityStackTransparent) {
            this.mIsActivityStackTransparent = activityStackTransparent;
            return this;
        }

        /**
         * Sets the number of activities.
         */
        public Builder setNumActivities(int numActivities) {
            this.mNumActivities = numActivities;
            return this;
        }

        /**
         * Sets the user ID.
         */
        public Builder setUserId(int userId) {
            this.mUserId = userId;
            return this;
        }

        /**
         * Sets the top activity info.
         */
        public Builder setTopActivityInfo(@Nullable ActivityInfo topActivityInfo) {
            this.mTopActivityInfo = topActivityInfo;
            return this;
        }

        /**
         * Sets the top activity type.
         */
        public Builder setTopActivityType(@WindowConfiguration.ActivityType int topActivityType) {
            this.mTopActivityType = topActivityType;
            return this;
        }

        /**
         * Builds the WindowingExemptionsInfo instance.
         */
        public WindowingExemptionsInfo build() {
            return new WindowingExemptionsInfo(this);
        }
    }
}
