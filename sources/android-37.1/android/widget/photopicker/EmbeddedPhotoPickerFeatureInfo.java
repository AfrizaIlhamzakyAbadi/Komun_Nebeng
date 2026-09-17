/*
 * Copyright (C) 2024 The Android Open Source Project
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

import static android.provider.MediaStore.PICK_IMAGES_HIGHLIGHT_TYPE_COLLAPSED;
import static android.provider.MediaStore.PICK_IMAGES_HIGHLIGHT_TYPE_EXPANDED;

import static java.util.Objects.requireNonNull;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresApi;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.MediaStore.PickImagesHighlightAlbum;
import android.provider.MediaStore.PickImagesHighlightType;

import androidx.annotation.ColorLong;
import androidx.annotation.IntRange;

import com.android.providers.media.flags.Flags;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * An immutable parcel to carry information regarding desired features of caller for
 * a given session.
 *
 * <p> Below features are currently supported in embedded photopicker.
 *
 * <ul>
 * <li> Mime type to filter media
 * <li> Accent color to change color of primary picker element
 * <li> Ordered selection of media items
 * <li> Max selection media count restriction
 * <li> Pre-selected uris
 * <li> Theme night mode
 * <li> Highlighting media results based on a given input query including highlighting media
 *      results from certain albums
 * <li> Location metadata request
 * <li> Selection options for media items
 * <li> UI customization options for the embedded photo picker
 * </ul>
 *
 * <p> Callers should use {@link Builder} to set the desired features.
 *
 */
@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@FlaggedApi(Flags.FLAG_ENABLE_EMBEDDED_PHOTOPICKER)
public final class EmbeddedPhotoPickerFeatureInfo implements Parcelable {

    /**
     * Constant to indicate default tab when no tab has been set as the launch tab.
     */
    @FlaggedApi(Flags.FLAG_ENABLE_EMBEDDED_PICKER_V2_APIS)
    public static final int TAB_UNSET = -1;

    /**
     * Constant to indicate the Albums tab as the launch tab.
     */
    @FlaggedApi(Flags.FLAG_ENABLE_EMBEDDED_PICKER_V2_APIS)
    public static final int TAB_ALBUMS = 0;

    /**
     * Constant to indicate the Photos tab as the launch tab.
     */
    @FlaggedApi(Flags.FLAG_ENABLE_EMBEDDED_PICKER_V2_APIS)
    public static final int TAB_IMAGES = 1;

    @Hide
    @IntDef({TAB_UNSET, TAB_ALBUMS, TAB_IMAGES})
    @Retention(RetentionPolicy.SOURCE)
    public @interface LaunchTab {}

    private final List<String> mMimeTypes;
    private final long mAccentColor;
    private final boolean mOrderedSelection;
    private final int mMaxSelectionLimit;
    private final List<Uri> mPreSelectedUris;
    private final int mThemeNightMode;
    @NonNull private final String mHighlightSearchMediaTextQuery;
    @NonNull private final String mHighlightAlbumId;
    private final int mHighlightType;
    private final boolean mLaunchedPickerInExpandedState;
    private final boolean mLocationMetadataRequested;
    private final boolean mCollapsedModeScrollingEnabled;
    @Nullable private final PhotoPickerSelectionParams mSelectionParams;
    @Nullable private final PhotoPickerUiCustomizationParams mUiCustomizationParams;
    @Nullable private final EmbeddedPhotoPickerUiCustomizationParams mEmbeddedUiCustomizationParams;
    private final int mLaunchTab;

    private EmbeddedPhotoPickerFeatureInfo(
            List<String> mimeTypes,
            long accentColor,
            boolean orderedSelection,
            int maxSelectionLimit,
            List<Uri> preSelectedUris,
            int themeNightMode,
            String highlightSearchMediaQuery,
            String highlightAlbumId,
            int highlightType,
            boolean launchedPickerInExpandedState,
            boolean locationMetadataRequested,
            @Nullable PhotoPickerSelectionParams selectionParams,
            @Nullable PhotoPickerUiCustomizationParams uiCustomizationParams,
            boolean collapsedModeScrollingEnabled,
            @Nullable EmbeddedPhotoPickerUiCustomizationParams embeddedUiCustomizationParams,
            int launchTab
    ) {
        this.mMimeTypes = mimeTypes;
        this.mAccentColor = accentColor;
        this.mOrderedSelection = orderedSelection;
        this.mMaxSelectionLimit = maxSelectionLimit;
        this.mPreSelectedUris = preSelectedUris;
        this.mThemeNightMode = themeNightMode;
        this.mHighlightSearchMediaTextQuery = highlightSearchMediaQuery;
        this.mHighlightAlbumId = highlightAlbumId;
        this.mHighlightType = highlightType;
        this.mLaunchedPickerInExpandedState = launchedPickerInExpandedState;
        this.mLocationMetadataRequested = locationMetadataRequested;
        this.mSelectionParams = selectionParams;
        this.mUiCustomizationParams = uiCustomizationParams;
        this.mCollapsedModeScrollingEnabled = collapsedModeScrollingEnabled;
        this.mEmbeddedUiCustomizationParams = embeddedUiCustomizationParams;
        this.mLaunchTab = launchTab;
    }
    @NonNull
    public List<Uri> getPreSelectedUris() {
        return this.mPreSelectedUris;
    }
    public int getMaxSelectionLimit() {
        return this.mMaxSelectionLimit;
    }
    public boolean isOrderedSelection() {
        return this.mOrderedSelection;
    }
    @ColorLong
    public long getAccentColor() {
        return this.mAccentColor;
    }
    @NonNull
    public List<String> getMimeTypes() {
        return this.mMimeTypes;
    }
    public int getThemeNightMode() {
        return this.mThemeNightMode;
    }

    /**
     * Returns the highlight media text query set by the app
     */
    @NonNull
    @FlaggedApi(Flags.FLAG_ENABLE_PICKER_HIGHLIGHT_SEARCH_RESULTS_APIS)
    public String getHighlightSearchMediaTextQuery() {
        return this.mHighlightSearchMediaTextQuery;
    }

    /**
     * Returns the highlight album set by the app
     */
    @NonNull
    @FlaggedApi(Flags.FLAG_ENABLE_PICKER_HIGHLIGHT_SEARCH_RESULTS_APIS)
    public String getHighlightAlbumId() {
        return this.mHighlightAlbumId;
    }

    /**
     * Returns the highlight type set by the app
     */
    @FlaggedApi(Flags.FLAG_ENABLE_EMBEDDED_PICKER_EXPANDED_HIGHLIGHT_TYPE_API)
    @PickImagesHighlightType
    public int getHighlightType() {
        return mHighlightType;
    }

    /**
     * Returns whether or not the picker was launched in expanded state
     */
    @FlaggedApi(Flags.FLAG_ENABLE_EMBEDDED_PICKER_EXPANDED_HIGHLIGHT_TYPE_API)
    public boolean isPickerLaunchedInExpandedState() {
        return mLaunchedPickerInExpandedState;
    }

    /**
     * Returns whether the app is requesting location metadata for selected media items.
     */
    @FlaggedApi(Flags.FLAG_ENABLE_PICKER_LOCATION_METADATA_API)
    public boolean isLocationMetadataRequested() {
        return mLocationMetadataRequested;
    }

    /**
     * Returns the selection options, which specify filters for media item properties.
     *
     * @return The {@link PhotoPickerSelectionParams} object containing the selection filters,
     * or {@code null} if no custom selection options are set.
     */
    @FlaggedApi(Flags.FLAG_ENABLE_PHOTOPICKER_SELECTION_PARAMS_API)
    @Nullable
    public PhotoPickerSelectionParams getSelectionParams() {
        return mSelectionParams;
    }
    /**
     * Returns the ui customization options set by the app.
     * @deprecated Use {@link #getEmbeddedUiCustomizationParams()} instead.
     * @return The {@link PhotoPickerUiCustomizationParams} object containing the ui
     *         customization options, or {@code null} if no custom options are set.
     */
    @Nullable
    @FlaggedApi(Flags.FLAG_ENABLE_EMBEDDED_PICKER_V2_APIS)
    @Deprecated
    public PhotoPickerUiCustomizationParams getUiCustomizationParams() {
        return mUiCustomizationParams;
    }

    /**
     * Returns whether the app enabled scrolling in collapsed mode.
     *
     *  @return {@code true} if the app enabled scrolling in collapsed mode,
     *          {@code false} otherwise.
     */
    @FlaggedApi(Flags.FLAG_ENABLE_EMBEDDED_PICKER_V2_APIS)
    public boolean isCollapsedModeScrollingEnabled() {
        return mCollapsedModeScrollingEnabled;
    }

    /**
     * Returns the embedded-specific UI customization options set by the app.
     *
     * @return The {@link EmbeddedPhotoPickerUiCustomizationParams} object,
     * or {@code null} if no custom embedded options are set.
     */
    @FlaggedApi(Flags.FLAG_ENABLE_EMBEDDED_PICKER_V2_APIS)
    @Nullable
    public EmbeddedPhotoPickerUiCustomizationParams getEmbeddedUiCustomizationParams() {
        return mEmbeddedUiCustomizationParams;
    }

    /**
     * Returns the launch tab set by the app.
     *
     * @return The launch tab for the embedded PhotoPicker, which is one of the
     * {@link LaunchTab} values (e.g., {@link EmbeddedPhotoPickerFeatureInfo#TAB_IMAGES}
     * or {@link EmbeddedPhotoPickerFeatureInfo#TAB_ALBUMS}). If no launch tab is set then
     * {@link EmbeddedPhotoPickerFeatureInfo#TAB_UNSET} is the default value.
     */
    @FlaggedApi(Flags.FLAG_ENABLE_EMBEDDED_PICKER_V2_APIS)
    @LaunchTab
    public int getLaunchTab() {
        return mLaunchTab;
    }

    public static final class Builder {
        //All mime-types are returned by default.
        @NonNull private static final List<String> DEFAULT_MIME_TYPES =
                Arrays.asList("image/*", "video/*");
        @ColorLong
        private static final long DEFAULT_ACCENT_COLOR = -1;
        private static final boolean DEFAULT_ORDERED_SELECTION = false;
        /**
         * By-default session will open in multiselect mode and below is the maximum
         * selection limit if user doesn't specify anything.
         */
        private static final int DEFAULT_MAX_SELECTION_LIMIT = 100;
        @NonNull
        private static final List<Uri> DEFAULT_PRE_SELECTED_URIS = Arrays.asList();
        private static final int DEFAULT_NIGHT_MODE = Configuration.UI_MODE_NIGHT_UNDEFINED;
        private static final String DEFAULT_HIGHLIGHT_SEARCH_MEDIA_TEXT_QUERY = "";
        private static final String DEFAULT_HIGHLIGHT_ALBUM_ID = "";
        private static final int DEFAULT_HIGHLIGHT_TYPE = PICK_IMAGES_HIGHLIGHT_TYPE_COLLAPSED;
        private static final boolean DEFAULT_EXPANDED_STATE = false;
        private static final boolean DEFAULT_ACCESS_LOCATION_METADATA = false;
        private static final PhotoPickerSelectionParams DEFAULT_SELECTION_PARAMS = null;
        private static final PhotoPickerUiCustomizationParams DEFAULT_UI_CUSTOMIZATION_PARAMS =
                null;
        private static  final boolean DEFAULT_COLLAPSED_MODE_SCROLLING_ENABLED = false;
        private static final EmbeddedPhotoPickerUiCustomizationParams
                DEFAULT_EMBEDDED_UI_CUSTOMIZATION_PARAMS = null;
        private static final int DEFAULT_LAUNCH_TAB = TAB_UNSET;

        private List<String> mMimeTypes = DEFAULT_MIME_TYPES;
        private long mAccentColor = DEFAULT_ACCENT_COLOR;
        private boolean mOrderedSelection = DEFAULT_ORDERED_SELECTION;
        private int mMaxSelectionLimit = DEFAULT_MAX_SELECTION_LIMIT;
        private List<Uri> mPreSelectedUris = DEFAULT_PRE_SELECTED_URIS;
        private int mThemeNightMode = DEFAULT_NIGHT_MODE;
        private String mHighlightSearchMediaTextQuery = DEFAULT_HIGHLIGHT_SEARCH_MEDIA_TEXT_QUERY;
        private String mHighlightAlbumId = DEFAULT_HIGHLIGHT_ALBUM_ID;
        private int mHighlightType = DEFAULT_HIGHLIGHT_TYPE;
        private boolean mLaunchedPickerInExpandedState = DEFAULT_EXPANDED_STATE;
        private boolean mLocationMetadataRequested = DEFAULT_ACCESS_LOCATION_METADATA;
        private boolean mCollapsedModeScrollingEnabled = DEFAULT_COLLAPSED_MODE_SCROLLING_ENABLED;
        private PhotoPickerSelectionParams mSelectionParams = DEFAULT_SELECTION_PARAMS;
        private PhotoPickerUiCustomizationParams mUiCustomizationParams =
                DEFAULT_UI_CUSTOMIZATION_PARAMS;
        private EmbeddedPhotoPickerUiCustomizationParams mEmbeddedUiCustomizationParams =
                DEFAULT_EMBEDDED_UI_CUSTOMIZATION_PARAMS;
        private int mLaunchTab = DEFAULT_LAUNCH_TAB;

        public Builder() {}

        /**
         *
         * @param featureInfo {@link EmbeddedPhotoPickerFeatureInfo} object whose properties
         *                     need to be copied to create a new object
         */
        @FlaggedApi(Flags.FLAG_ENABLE_EMBEDDED_PICKER_EXPANDED_HIGHLIGHT_TYPE_API)
        public Builder(@NonNull EmbeddedPhotoPickerFeatureInfo featureInfo) {
            requireNonNull(
                    featureInfo,
                    "EmbeddedPhotoPickerFeatureInfo object cannot be null in constructor call"
            );

            // Make a deep copy of all the properties
            this.mMimeTypes = new ArrayList<>(featureInfo.getMimeTypes());
            this.mAccentColor = featureInfo.getAccentColor();
            this.mOrderedSelection = featureInfo.isOrderedSelection();
            this.mMaxSelectionLimit = featureInfo.getMaxSelectionLimit();
            this.mPreSelectedUris = new ArrayList<>(featureInfo.getPreSelectedUris());
            this.mThemeNightMode = featureInfo.getThemeNightMode();
            this.mHighlightSearchMediaTextQuery = featureInfo.getHighlightSearchMediaTextQuery();
            this.mHighlightAlbumId = featureInfo.getHighlightAlbumId();
            this.mHighlightType = featureInfo.getHighlightType();
            this.mLaunchedPickerInExpandedState = featureInfo.isPickerLaunchedInExpandedState();
            this.mLocationMetadataRequested = featureInfo.isLocationMetadataRequested();
            this.mSelectionParams = featureInfo.getSelectionParams();
            this.mUiCustomizationParams = featureInfo.getUiCustomizationParams();
            this.mCollapsedModeScrollingEnabled = featureInfo.isCollapsedModeScrollingEnabled();
            this.mEmbeddedUiCustomizationParams = featureInfo.getEmbeddedUiCustomizationParams();
            this.mLaunchTab = featureInfo.getLaunchTab();
        }

        /**
         * Sets the mime type to filter media items on.
         *
         * <p> Values may be a combination of concrete MIME types (such as "image/png")
         * and/or partial MIME types (such as "image/*").
         *
         * @param mimeTypes List of mime types to filter. By default, all media items
         *                  will be returned
         */
        @NonNull
        public Builder setMimeTypes(@NonNull List<String> mimeTypes) {
            validateMimeType(mimeTypes);
            mMimeTypes = mimeTypes;
            return this;
        }

        private void validateMimeType(List<String> mimeTypes) {
            requireNonNull(mimeTypes, "Mime type list must not be null.");
            for (String mimeType : mimeTypes) {
                requireNonNull(mimeType, "Mime type must not be null.");
                if (!isMimeTypeMedia(mimeType)) {
                    throw new IllegalArgumentException("Invalid mime type found. "
                            + "Only image/video mime types are supported");
                }
            }
        }

        /**
         * Checks if the given string is an image or video mime type
         */
        private static boolean isMimeTypeMedia(@NonNull String mimeType) {
            return mimeType.toLowerCase(Locale.getDefault()).startsWith("image/")
                    || mimeType.toLowerCase(Locale.getDefault()).startsWith("video/");
        }

        /**
         * Sets accent color which will change color of primary picker elements like Done button,
         * selected media icon colors, tab color etc.
         *
         * <p> The value of this intent-extra must be a string specifying the hex code of the
         * accent color that is to be used within the picker.
         *
         * <p> This param is same as {@link MediaStore#EXTRA_PICK_IMAGES_ACCENT_COLOR}. See {@link
         * MediaStore#EXTRA_PICK_IMAGES_ACCENT_COLOR} for more details on accepted colors.
         *
         * @param accentColor Hex code of desired accent color. By default, the color of elements
         * will reflect based on device theme
         */
        @NonNull
        public Builder setAccentColor(@ColorLong long accentColor) {
            mAccentColor = accentColor;
            return this;
        }

        /**
         * The app can choose to highlight media items in the embedded photopicker in its
         * expanded state. The media items in this highlighted section are based on the string
         * input query set in this method. The photopicker will trigger a search based on this input
         * value to show media results in this section.This can be any string literal for which the
         * app wants to highlight media results.
         *
         * <p> The value of this string param must not be empty or null in case the app wants to
         * show a highlighted media section. An empty value will result in simply ignoring
         * the request for a highlighted media section. A null value will result in
         * {@code IllegalArgumentException}
         * The app can also choose to highlight a specific photopicker album using
         * {@link EmbeddedPhotoPickerFeatureInfo#setHighlightAlbumName}. Only one of album
         * highlight or text highlight should be used at any point. Using both will result in
         * {@code IllegalArgumentException} to be thrown.
         *
         * @param highlightSearchMediaTextQuery A String param based on which the highlighted
         *                                      results shown.
         * @throws IllegalArgumentException in case input string query is null
         */
        @NonNull
        @FlaggedApi(Flags.FLAG_ENABLE_PICKER_HIGHLIGHT_SEARCH_RESULTS_APIS)
        public Builder setHighlightSearchMediaTextQuery(
                @NonNull String highlightSearchMediaTextQuery
        ) {
            if (highlightSearchMediaTextQuery == null) {
                throw new IllegalArgumentException(
                        "Input search highlight text query cannot be null"
                );
            }
            mHighlightSearchMediaTextQuery = highlightSearchMediaTextQuery;
            return this;
        }

        /**
         * The app can choose to highlight media items of a photopicker album in the embedded
         * photopicker in its expanded state. These can be one of Favorites, Camera, Screenshots,
         * Videos or Downloads. In order to do so, the input value should be one of the album
         * values:
         * {@link MediaStore#PICK_IMAGES_HIGHLIGHT_ALBUM_FAVORITES} for the Favorites album,
         * {@link MediaStore#PICK_IMAGES_HIGHLIGHT_ALBUM_CAMERA} for the Camera album,
         * {@link MediaStore#PICK_IMAGES_HIGHLIGHT_ALBUM_SCREENSHOTS} for the Screenshots album,
         * {@link MediaStore#PICK_IMAGES_HIGHLIGHT_ALBUM_VIDEOS} for the Videos album and
         * {@link MediaStore#PICK_IMAGES_HIGHLIGHT_ALBUM_DOWNLOADS} for the Downloads album.
         *
         * <p> The value of this string param must not be empty or null in case the app wants to
         * show a highlighted album media section. An empty value will result in simply ignoring
         * the request for a highlighted media section. A null value will result in
         * {@code IllegalArgumentException} being thrown. Any other value except the ones
         * specified will also result in {@code IllegalArgumentException} to be thrown.
         * The app can also choose to highlight media items based on a text query using
         * {@link EmbeddedPhotoPickerFeatureInfo#setHighlightMediaTextQuery}. Only one of album
         * highlight or text highlight should be used at any point. Using both will result in
         * {@code IllegalArgumentException} to be thrown.
         *
         * @param highlightAlbumId One of the above mentioned string params specifying the
         *                           album name.
         * @throws IllegalArgumentException in case input album is null
         */
        @NonNull
        @FlaggedApi(Flags.FLAG_ENABLE_PICKER_HIGHLIGHT_SEARCH_RESULTS_APIS)
        public Builder setHighlightAlbumId(
                @NonNull @PickImagesHighlightAlbum String highlightAlbumId
        ) {
            if (highlightAlbumId == null) {
                throw new IllegalArgumentException("Input highlight album cannot be null");
            }
            mHighlightAlbumId = highlightAlbumId;
            return this;
        }

        /**
         * The app can choose to specify the highlight type i.e. the way in which the highlighted
         * media results will be shown in the photopicker. The highlight type can be set for both
         * album and search highlights.
         *
         * <p> The value can be one of:
         * <ul>
         * <li> {@link MediaStore#PICK_IMAGES_HIGHLIGHT_TYPE_COLLAPSED} to show a highlighted media
         * section in the photopicker or
         * <li> {@link MediaStore#PICK_IMAGES_HIGHLIGHT_TYPE_EXPANDED}
         * to show a highlighted media results grid. If this is the preferred highlight type,
         * the embedded picker must be launched in the expanded state iniially by the app itself and
         * {@link setLaunchedPickerInExpandedState} must be set to true to indicate the same.
         * If the embedded picker's initial expanded state is found to be false, then the
         * request for {@link MediaStore#PICK_IMAGES_HIGHLIGHT_TYPE_EXPANDED} is ignored.
         * </ul>
         * The default highlight type value will be
         * {@link MediaStore#PICK_IMAGES_HIGHLIGHT_TYPE_COLLAPSED}.
         * Any other input highlight value will result in {@code IllegalArgumentException} to be
         * thrown.
         * @param highlightType One of the above mentioned int params specifying the highlight
         *                      type.
         * @throws IllegalArgumentException if the input highlight type is invalid.
         */
        @NonNull
        @FlaggedApi(Flags.FLAG_ENABLE_EMBEDDED_PICKER_EXPANDED_HIGHLIGHT_TYPE_API)
        public Builder setHighlightType(@PickImagesHighlightType int highlightType) {
            if (highlightType != PICK_IMAGES_HIGHLIGHT_TYPE_COLLAPSED
                    && highlightType != PICK_IMAGES_HIGHLIGHT_TYPE_EXPANDED) {
                throw new IllegalArgumentException("Invalid value for input highlight type");
            }
            mHighlightType = highlightType;
            return this;
        }

        /**
         * Embedded photopicker can be launched in the expanded state by the app. If the app opts
         * to do so, this field must be set to true indicating the app chose to initially launch
         * the embedded picker in the expanded state.
         * @param launchedPickerInExpandedState Indicates that the app chose to
         *                                      launch the picker in expanded state.
         */
        @NonNull
        @FlaggedApi(Flags.FLAG_ENABLE_EMBEDDED_PICKER_EXPANDED_HIGHLIGHT_TYPE_API)
        public Builder setPickerLaunchedInExpandedState(boolean launchedPickerInExpandedState) {
            mLaunchedPickerInExpandedState = launchedPickerInExpandedState;
            return this;
        }

        /**
         * The app can request access to the location metadata of the media items selected by
         * the user.
         * <p>
         * This is indicated by a boolean value which when set to {@code true} informs the
         * picker that the app is requesting location information for the media items selected by
         * the user.
         * The default value for this option will always be {@code false} i.e. not sharing the
         * location metadata of the selected media items with the calling app.
         * <p>
         * Setting this option to true does not guarantee that the calling app will get the location
         * information. The media items selected by the user may not have any location metadata
         * associated with them at all. The picker also reserves the right to inform the user of
         * this request and the user's choice to share the location information will be final.
         * The calling app will not be able to get the requested data in both these cases.
         *
         * @param accessLocationMetadata boolean value indicating location access request
         */
        @NonNull
        @FlaggedApi(Flags.FLAG_ENABLE_PICKER_LOCATION_METADATA_API)
        public Builder setRequestLocationMetadata(boolean accessLocationMetadata) {
            mLocationMetadataRequested = accessLocationMetadata;
            return this;
        }

        /**
         * Sets the ui customization params to apply to the Photo Picker.
         * @deprecated Use {@link #setEmbeddedUiCustomizationParams
         * (EmbeddedPhotoPickerUiCustomizationParams)}
         * to set all UI parameters, including embedded-specific ones.
         *
         * @param uiCustomizationParams The {@link PhotoPickerUiCustomizationParams} object
         *                               containing the ui customization params, or {@code null} to
         *                               clear any custom options.
         * @return This Builder object to allow for chaining of calls.
         */
        @FlaggedApi(Flags.FLAG_ENABLE_EMBEDDED_PICKER_V2_APIS)
        @Deprecated
        @NonNull
        public Builder setUiCustomizationParams(
                @Nullable PhotoPickerUiCustomizationParams uiCustomizationParams
        ) {
            mUiCustomizationParams = uiCustomizationParams;
            return this;
        }

        /**
         * Sets the embedded-specific UI customization params for the Photo Picker.
         * This includes options like controlling the selection bar visibility in expanded mode,
         * and any base UI customizations.
         *
         * If both {@link #setUiCustomizationParams(PhotoPickerUiCustomizationParams)} and
         * this method are called, the parameters set in
         * {@link EmbeddedPhotoPickerUiCustomizationParams} will take precedence and the parameters
         * set via {@link #setUiCustomizationParams(PhotoPickerUiCustomizationParams)} will be
         * ignored.
         *
         * @param embeddedUiCustomizationParams The {@link EmbeddedPhotoPickerUiCustomizationParams}
         * object containing the ui customization params, or {@code null} to clear any custom
         * options.
         * @return This Builder object to allow for chaining of calls.
         */
        @FlaggedApi(Flags.FLAG_ENABLE_EMBEDDED_PICKER_V2_APIS)
        @NonNull
        public Builder setEmbeddedUiCustomizationParams(
                @Nullable EmbeddedPhotoPickerUiCustomizationParams embeddedUiCustomizationParams) {
            mEmbeddedUiCustomizationParams = embeddedUiCustomizationParams;
            return this;
        }

        /**
         * Specifies the default tab to display when the embedded PhotoPicker is first launched.
         *
         * The app can choose to launch to a specific tab in the embedded photopicker
         * This can be the Photos tab (which will be the default tab if not specified by the app) or
         * the Collections tab. In order to do so, the input value should be one of the
         * {@link LaunchTab} values:
         * {@link EmbeddedPhotoPickerFeatureInfo#TAB_IMAGES} for the Photos tab,
         * {@link EmbeddedPhotoPickerFeatureInfo#TAB_ALBUMS} for the Collections tab.
         * {@link EmbeddedPhotoPickerFeatureInfo#TAB_UNSET} for the default tab.
         *
         * <p> The value of this int param must be set in case the app wants to launch to a
         * particular start destination.
         *
         * @param launchTab One of the above int params specifying the
         *                           start destination for launching picker.
         * @throws IllegalArgumentException if the provided launchTab is not a valid
         *                                  {@link LaunchTab} value.
         */
        @NonNull
        @FlaggedApi(Flags.FLAG_ENABLE_EMBEDDED_PICKER_V2_APIS)
        public Builder setLaunchTab(@LaunchTab int launchTab) {
            if (launchTab != TAB_IMAGES
                    && launchTab != TAB_ALBUMS
                    && launchTab != TAB_UNSET) {
                throw new IllegalArgumentException("Invalid launch tab. Must be one of "
                        + "EmbeddedPhotoPickerFeatureInfo.TAB_IMAGES or "
                        + "EmbeddedPhotoPickerFeatureInfo.TAB_ALBUMS or "
                + "EmbeddedPhotoPickerFeatureInfo.TAB_UNSET");
            }
            mLaunchTab = launchTab;
            return this;
        }

        /**
         * Sets ordered selection of media items i.e. this allows user to view/receive items in
         * their selected order
         *
         * @param orderedSelection Pass true to set ordered selection. Default is false
         */
        @NonNull
        public Builder setOrderedSelection(boolean orderedSelection) {
            mOrderedSelection = orderedSelection;
            return this;
        }

        /**
         * Sets maximum number of items that can be selected by the user
         *
         * <p> The value of this intent-extra should be a positive integer greater than
         * or equal to 1 and less than or equal to {@link MediaStore#getPickImagesMaxLimit}
         *
         * @param maxSelectionLimit Max selection count restriction. Pass limit as 1 to open
         * PhotoPicker in single-select mode. Default is multi select mode with limit as
         * {@link MediaStore#getPickImagesMaxLimit()}
         */
        @NonNull
        public Builder setMaxSelectionLimit(@IntRange(from = 1) int maxSelectionLimit) {
            if (maxSelectionLimit > DEFAULT_MAX_SELECTION_LIMIT) {
                throw new IllegalArgumentException("Max selection limit should be less than "
                        + DEFAULT_MAX_SELECTION_LIMIT);
            }
            mMaxSelectionLimit = maxSelectionLimit;
            return this;
        }

        /**
         * Sets list of uris to be pre-selected when embedded picker is opened.
         *
         * <p> This is same as {@link MediaStore#EXTRA_PICKER_PRE_SELECTION_URIS}.
         * See {@link MediaStore#EXTRA_PICKER_PRE_SELECTION_URIS} for more details
         * on restrictions and filter criteria.
         *
         * @param preSelectedUris list of uris to be pre-selected
         */
        @NonNull
        public Builder setPreSelectedUris(@NonNull List<Uri> preSelectedUris) {
            requireNonNull(preSelectedUris, "Preselected uri list can not be null.");
            mPreSelectedUris = preSelectedUris;
            return this;
        }

        /**
         * Sets the embedded photo picker theme to light or dark irrespective of the device theme.
         *
         * @param themeNightMode hex code of the desired {@link Configuration#UI_MODE_NIGHT_MASK}
         *                       value.
         *
         * <p> The default value is {@link Configuration#UI_MODE_NIGHT_UNDEFINED} to apply the
         * system (device) theme.
         *
         * <p> Supported values are -</p>
         * <li> {@link Configuration#UI_MODE_NIGHT_UNDEFINED} -> system theme
         * <li> {@link Configuration#UI_MODE_NIGHT_YES} -> dark theme
         * <li> {@link Configuration#UI_MODE_NIGHT_NO} -> light theme
         */
        @NonNull
        public Builder setThemeNightMode(int themeNightMode) {
            if (!isSupportedNightModeConstant(themeNightMode)) {
                throw new IllegalArgumentException("Unsupported themeNightMode: " + themeNightMode);
            }
            mThemeNightMode = themeNightMode;
            return this;
        }

        /**
         * Sets the selection options, which specify filters for media item properties.
         *
         * @param selectionParams The {@link PhotoPickerSelectionParams} object containing the
         *                         selection filters, or {@code null} to clear any custom selection
         *                         options.
         * @return This Builder object to allow for chaining of calls.
         */
        @FlaggedApi(Flags.FLAG_ENABLE_PHOTOPICKER_SELECTION_PARAMS_API)
        @NonNull
        public Builder setSelectionParams(@Nullable PhotoPickerSelectionParams selectionParams) {
            mSelectionParams = selectionParams;
            return this;
        }

        /**
         * Sets whether the app chooses to allow the collapsed mode to be scrollable.
         *
         * <p>By default, the photopicker is non-scrollable in the collapsed state.
         * Setting this to {@code false} or {@code true} does not affect the expanded mode, which
         * remains scrollable. This is a setup-time option and cannot be
         * changed mid-session.
         *
         * @param collapsedModeScrollingEnabled {@code true} if the app chooses to allow
         *                              the collapsed mode to be scrollable,
         *                              {@code false} otherwise.
         * @return This Builder object to allow for chaining of calls.
         */
        @NonNull
        @FlaggedApi(Flags.FLAG_ENABLE_EMBEDDED_PICKER_V2_APIS)
        public Builder setCollapsedModeScrollingEnabled(boolean collapsedModeScrollingEnabled) {
            mCollapsedModeScrollingEnabled = collapsedModeScrollingEnabled;
            return this;
        }

        private static boolean isSupportedNightModeConstant(int value) {
            return value == Configuration.UI_MODE_NIGHT_UNDEFINED
                    || value == Configuration.UI_MODE_NIGHT_NO
                    || value == Configuration.UI_MODE_NIGHT_YES;
        }

        /**
         * Build the class for desired feature info arguments
         */
        @NonNull
        public EmbeddedPhotoPickerFeatureInfo build() {
            return new EmbeddedPhotoPickerFeatureInfo(
                    mMimeTypes,
                    mAccentColor,
                    mOrderedSelection,
                    mMaxSelectionLimit,
                    mPreSelectedUris,
                    mThemeNightMode,
                    mHighlightSearchMediaTextQuery,
                    mHighlightAlbumId,
                    mHighlightType,
                    mLaunchedPickerInExpandedState,
                    mLocationMetadataRequested,
                    mSelectionParams,
                    mUiCustomizationParams,
                    mCollapsedModeScrollingEnabled,
                    mEmbeddedUiCustomizationParams,
                    mLaunchTab);
        }
    }
    private EmbeddedPhotoPickerFeatureInfo(Parcel in) {
        List<String> mimeTypes = new java.util.ArrayList<>();
        in.readStringList(mimeTypes);
        this.mMimeTypes = mimeTypes;
        this.mAccentColor = in.readLong();
        this.mOrderedSelection = in.readBoolean();
        this.mMaxSelectionLimit = in.readInt();
        final ArrayList<Uri> preSelectedUris = new ArrayList<>();
        in.readTypedList(preSelectedUris, Uri.CREATOR);
        this.mPreSelectedUris = preSelectedUris;
        this.mThemeNightMode = in.readInt();
        this.mHighlightSearchMediaTextQuery = in.readString();
        this.mHighlightAlbumId = in.readString();
        this.mHighlightType = in.readInt();
        this.mLaunchedPickerInExpandedState = in.readBoolean();
        this.mLocationMetadataRequested = in.readBoolean();
        this.mSelectionParams = in.readParcelable(
                PhotoPickerSelectionParams.class.getClassLoader(),
                PhotoPickerSelectionParams.class);

        this.mUiCustomizationParams = in.readParcelable(
                PhotoPickerUiCustomizationParams.class.getClassLoader(),
                PhotoPickerUiCustomizationParams.class
        );
        this.mCollapsedModeScrollingEnabled = in.readBoolean();
        this.mEmbeddedUiCustomizationParams = in.readParcelable(
                EmbeddedPhotoPickerUiCustomizationParams.class.getClassLoader(),
                EmbeddedPhotoPickerUiCustomizationParams.class
        );
        this.mLaunchTab = in.readInt();
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeStringList(mMimeTypes);
        dest.writeLong(mAccentColor);
        dest.writeBoolean(mOrderedSelection);
        dest.writeInt(mMaxSelectionLimit);
        dest.writeTypedList(mPreSelectedUris, flags);
        dest.writeInt(mThemeNightMode);
        dest.writeString(mHighlightSearchMediaTextQuery);
        dest.writeString(mHighlightAlbumId);
        dest.writeInt(mHighlightType);
        dest.writeBoolean(mLaunchedPickerInExpandedState);
        dest.writeBoolean(mLocationMetadataRequested);
        dest.writeParcelable(mSelectionParams, flags);
        dest.writeParcelable(mUiCustomizationParams, flags);
        dest.writeBoolean(mCollapsedModeScrollingEnabled);
        dest.writeParcelable(mEmbeddedUiCustomizationParams, flags);
        dest.writeInt(mLaunchTab);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @NonNull
    public static final Creator<EmbeddedPhotoPickerFeatureInfo> CREATOR =
            new Creator<EmbeddedPhotoPickerFeatureInfo>() {
                @Override
                public EmbeddedPhotoPickerFeatureInfo createFromParcel(Parcel in) {
                    return new EmbeddedPhotoPickerFeatureInfo(in);
                }

                @Override
                public EmbeddedPhotoPickerFeatureInfo[] newArray(int size) {
                    return new EmbeddedPhotoPickerFeatureInfo[size];
                }
            };

    @Override
    public String toString() {
        return "EmbeddedPhotoPickerFeatureInfo{"
                + "mMimeTypes=" + mMimeTypes
                + ", mAccentColor=" + mAccentColor
                + ", mOrderedSelection=" + mOrderedSelection
                + ", mMaxSelectionLimit=" + mMaxSelectionLimit
                + ", mPreSelectedUris=" + mPreSelectedUris
                + ", mThemeNightMode=" + mThemeNightMode
                + ", mHighlightSearchMediaQuery=" + mHighlightSearchMediaTextQuery
                + ", mHighlightAlbumId=" + mHighlightAlbumId
                + ", mHighlightType=" + mHighlightType
                + ", mLaunchedPickerInExpandedState=" + mLaunchedPickerInExpandedState
                + ", mLocationMetadataRequested=" + mLocationMetadataRequested
                + ", mSelectionParams=" + mSelectionParams
                + ", mUiCustomizationOptions=" + mUiCustomizationParams
                + ", mCollapsedModeScrollingEnabled" + mCollapsedModeScrollingEnabled
                + ", mEmbeddedUiCustomizationParams=" + mEmbeddedUiCustomizationParams
                + ", mStartDestination=" + mLaunchTab
                + '}';
    }
}