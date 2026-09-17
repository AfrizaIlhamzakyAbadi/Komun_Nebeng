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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Record a requested animation property from the client.
 */
abstract class AnimationDescription implements Parcelable {
    static final int DESCRIPTION_SPRING = 0;
    static final int DESCRIPTION_TIME = 1;

    protected final int mDescriptionType;

    AnimationDescription(int descriptionType) {
        mDescriptionType = descriptionType;
    }

    /**
     * Create the real animator from request, only be used in client.
     */
    abstract CommonAnimator createAnimator();

    public static final Creator<AnimationDescription> CREATOR = new Creator<>() {
        @Override
        public AnimationDescription createFromParcel(Parcel in) {
            // Read the type first. Note: The write order must put mDescriptionType first!
            int descriptionType = in.readInt();
            return switch (descriptionType) {
                case DESCRIPTION_SPRING ->
                        new SpringAnimationDescription(in, descriptionType);
                case DESCRIPTION_TIME ->
                        new InterpolatorAnimationDescription(in, descriptionType);
                default -> throw new IllegalArgumentException("Unknown description type: "
                        + descriptionType);
            };
        }

        @Override
        public AnimationDescription[] newArray(int size) {
            return new AnimationDescription[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(mDescriptionType);
    }
}
