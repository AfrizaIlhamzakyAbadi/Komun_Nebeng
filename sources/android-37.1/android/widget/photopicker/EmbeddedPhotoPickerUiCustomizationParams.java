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

package android.widget.photopicker;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.android.providers.media.flags.Flags;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Set;

/**
 * Defines UI customization options specific to the embedded Photo Picker interface.
 * <p>
 * This immutable class allows an application to specify visual preferences to optimize
 * the user experience within the picker.
 */
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@FlaggedApi(Flags.FLAG_ENABLE_EMBEDDED_PICKER_V2_APIS)
public final class EmbeddedPhotoPickerUiCustomizationParams implements Parcelable {

    /**
     * Aspect ratio option sent to photo picker when no aspect ratio is set by the calling app.
     */
    public static final int ASPECT_RATIO_UNDEFINED = -1;

    /**
     * Aspect ratio option requesting square 1:1 thumbnail sizing within the media grid.
     */
    public static final int ASPECT_RATIO_SQUARE_1_1 = 0;

    /**
     * Aspect ratio option requesting portrait 9:16 sizing for the thumbnails within the media grid.
     */
    public static final int ASPECT_RATIO_PORTRAIT_9_16 = 1;

    @Hide
    @IntDef({ASPECT_RATIO_UNDEFINED, ASPECT_RATIO_SQUARE_1_1, ASPECT_RATIO_PORTRAIT_9_16})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AspectRatio {
    }

    private final @AspectRatio int mAspectRatio;
    private final boolean mSelectionBarVisibleInExpandedMode;

    private EmbeddedPhotoPickerUiCustomizationParams(
            @AspectRatio int aspectRatio, boolean selectionBarVisibleInExpandedMode) {
        mAspectRatio = aspectRatio;
        mSelectionBarVisibleInExpandedMode = selectionBarVisibleInExpandedMode;
    }

    private EmbeddedPhotoPickerUiCustomizationParams(Parcel in) {
        mAspectRatio = in.readInt();
        mSelectionBarVisibleInExpandedMode = in.readBoolean();
    }

    /**
     * Returns the constant representing the aspect ratio currently configured for thumbnail sizing
     * within the Photo Picker's media grid. (e.g., {@link #ASPECT_RATIO_SQUARE_1_1}).
     *
     * <p>If the aspect ratio is not set by the caller app using
     * {@link Builder#setAspectRatio(int)}, this method returns {@link #ASPECT_RATIO_UNDEFINED},
     * indicating that the photo picker will use its default 1:1 media grid aspect ratio.
     */
    @AspectRatio
    public int getAspectRatio() {
        return mAspectRatio;
    }

    /**
     * Returns whether the Selection Bar should be visible in expanded mode in Embedded picker
     * as set by the calling application.
     *
     * <p>If not set by the caller using
     * {@link Builder#setSelectionBarVisibleInExpandedMode(boolean)}, this method returns
     * {@code true} by default.
     *
     * @return {@code true} if the selection bar should be visible, {@code false} otherwise.
     */
    public boolean isSelectionBarVisibleInExpandedMode() {
        return mSelectionBarVisibleInExpandedMode;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mAspectRatio);
        dest.writeBoolean(mSelectionBarVisibleInExpandedMode);
    }

    @NonNull
    public static final Creator<EmbeddedPhotoPickerUiCustomizationParams> CREATOR = new
            Creator<EmbeddedPhotoPickerUiCustomizationParams>() {
                @Override
                public EmbeddedPhotoPickerUiCustomizationParams createFromParcel(Parcel in) {
                    return new EmbeddedPhotoPickerUiCustomizationParams(in);
                }

                @Override
                public EmbeddedPhotoPickerUiCustomizationParams[] newArray(int size) {
                    return new EmbeddedPhotoPickerUiCustomizationParams[size];
                }
            };

    /**
     * Builder class for {@link EmbeddedPhotoPickerUiCustomizationParams}.
     */
    public static final class Builder {
        // Helper for runtime check, add all supported aspect ratios to this set.
        private static final Set<Integer> VALID_ASPECT_RATIOS = Set.of(
                ASPECT_RATIO_UNDEFINED,
                ASPECT_RATIO_SQUARE_1_1,
                ASPECT_RATIO_PORTRAIT_9_16
        );
        private @AspectRatio int mAspectRatio = ASPECT_RATIO_UNDEFINED;
        private boolean mSelectionBarVisibleInExpandedMode = true;

        public Builder() {}

        /**
         * Sets the desired aspect ratio for the media grid thumbnails within the Photo Picker UI.
         *
         * <p>The value must be one of the following constants:
         * <ul>
         * <li> {@link #ASPECT_RATIO_UNDEFINED}
         * <li> {@link #ASPECT_RATIO_SQUARE_1_1}
         * <li> {@link #ASPECT_RATIO_PORTRAIT_9_16}
         * </ul>
         * Any other value will result in throwing {@code IllegalArgumentException}.
         *
         * <p>If not set, the {@link #ASPECT_RATIO_UNDEFINED} will be used.
         *
         * @param aspectRatio The aspect ratio constant.
         * @throws IllegalArgumentException if the provided {@code aspectRatio} is not one of the
         *                                  supported constants ({@link #VALID_ASPECT_RATIOS}).
         */
        public @NonNull Builder setAspectRatio(@AspectRatio int aspectRatio) {
            if (!VALID_ASPECT_RATIOS.contains(aspectRatio)) {
                throw new IllegalArgumentException(
                        "Unrecognized aspect ratio constant: " + aspectRatio);
            }
            mAspectRatio = aspectRatio;
            return this;
        }

        /**
         * Sets whether to show the Selection Bar in the embedded photopicker when in
         * expanded mode. By default, the Selection Bar is shown.
         *
         * @param visible {@code false} to hide the Selection Bar, {@code true} to show it.
         * @return This Builder object.
         */
        @NonNull
        public Builder setSelectionBarVisibleInExpandedMode(boolean visible) {
            mSelectionBarVisibleInExpandedMode = visible;
            return this;
        }

        /**
         * Builds the {@link EmbeddedPhotoPickerUiCustomizationParams} object.
         * @return A new {@link EmbeddedPhotoPickerUiCustomizationParams} instance with the
         * configured UI properties.
         */
        @NonNull
        public EmbeddedPhotoPickerUiCustomizationParams build() {
            return new EmbeddedPhotoPickerUiCustomizationParams(
                    mAspectRatio,
                    mSelectionBarVisibleInExpandedMode);
        }
    }
}
