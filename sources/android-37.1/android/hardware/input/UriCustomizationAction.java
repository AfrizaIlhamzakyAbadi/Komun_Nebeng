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

package android.hardware.input;

import static com.android.hardware.input.Flags.FLAG_ENABLE_POEM_INPUT_CUSTOMIZATION;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.net.Uri;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * Represents an action that requires an accompanying URI to execute.
 * <p>
 * Examples include opening a specific file, navigating to a URL, or
 * opening a directory in a file browser.
 */
@FlaggedApi(FLAG_ENABLE_POEM_INPUT_CUSTOMIZATION)
public final class UriCustomizationAction extends CustomizationAction {

    /**
     * Action type: Open File.
     * <p>
     * Specifies the action to open a file when the peripheral input is triggered.
     */
    public static final int TYPE_OPEN_FILE = 1;

    /**
     * Action type: Open Folder.
     * <p>
     * Specifies the action to launch the system file browser for a specific directory
     * when the peripheral input is triggered.
     */
    public static final int TYPE_OPEN_FOLDER = 2;

    /**
     * Action type: Open URL.
     * <p>
     * Specifies the action to open a web URL when the peripheral input is triggered.
     */
    public static final int TYPE_OPEN_URL = 3;

    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(prefix = { "TYPE_" }, value = {
            TYPE_OPEN_FILE,
            TYPE_OPEN_FOLDER,
            TYPE_OPEN_URL
    })
    public @interface Type {}

    @NonNull private final AidlUriAction mAidlUriAction;

    /**
     * Creates a new {@link UriCustomizationAction}.
     *
     * @param actionType The type of action to create.
     * @param uri The URI to be opened.
     * @throws IllegalArgumentException if the action type is invalid.
     * @throws NullPointerException if the {@code uri} is null.
     */
    public UriCustomizationAction(@Type int actionType, @NonNull Uri uri) {
        validateActionType(actionType);

        this.mAidlUriAction = new AidlUriAction();
        this.mAidlUriAction.actionType = actionType;
        this.mAidlUriAction.uri = Objects.requireNonNull(uri, "uri must not be null");
    }

    /**
     * Creates a new {@link UriCustomizationAction} wrapping the specified AIDL object.
     */
    @Hide
    public UriCustomizationAction(@NonNull AidlUriAction aidlAction) {
        Objects.requireNonNull(aidlAction, "aidlAction must not be null");
        validateActionType(aidlAction.actionType);

        this.mAidlUriAction = new AidlUriAction();
        this.mAidlUriAction.actionType = aidlAction.actionType;
        this.mAidlUriAction.uri = Objects.requireNonNull(aidlAction.uri, "uri must not be null");
        this.mAidlUriAction.oemPackageName = aidlAction.oemPackageName;
        this.mAidlUriAction.oemUid = aidlAction.oemUid;

        if (this.mAidlUriAction.actionType < TYPE_OPEN_FILE
                || this.mAidlUriAction.actionType > TYPE_OPEN_URL) {
            throw new IllegalArgumentException("Unsupported UriCustomizationAction action type: "
                    + this.mAidlUriAction.actionType);
        }
    }

    /**
     * Gets the action type associated with this customization.
     */
    @Type
    public int getType() {
        return mAidlUriAction.actionType;
    }

    /**
     * Gets the URI for this action.
     *
     * @return The target {@link Uri}.
     */
    @NonNull
    public Uri getUri() {
        return mAidlUriAction.uri;
    }

    /**
     * Gets a defensive copy of the underlying AIDL parcelable data object.
     */
    @Hide
    @NonNull
    public AidlUriAction getAidl() {
        AidlUriAction copy = new AidlUriAction();
        copy.actionType = mAidlUriAction.actionType;
        copy.uri = mAidlUriAction.uri;
        copy.oemPackageName = mAidlUriAction.oemPackageName;
        copy.oemUid = mAidlUriAction.oemUid;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UriCustomizationAction that = (UriCustomizationAction) o;
        // AidlUriAction contains internal system server tracking metadata (oemPackageName
        // and oemUid). Since these fields are not part of the public API and track internal state
        // including them in equals will violate value object equality semantics for the public Api.
        return mAidlUriAction.actionType == that.mAidlUriAction.actionType
                && Objects.equals(mAidlUriAction.uri, that.mAidlUriAction.uri);
    }

    @Override
    public int hashCode() {
        // AidlUriAction contains internal system server tracking metadata (oemPackageName
        // and oemUid). Since these fields are not part of the public API and track internal state
        // including them in hash will violate value object equality semantics for the public Api.
        return Objects.hash(mAidlUriAction.actionType, mAidlUriAction.uri);
    }

    private void validateActionType(@Type int actionType) {
        switch (actionType) {
            case TYPE_OPEN_FILE,
                 TYPE_OPEN_FOLDER,
                 TYPE_OPEN_URL -> { }
            default ->
                    throw new IllegalArgumentException("Invalid UriCustomizationAction type: "
                            + actionType);
        }
    }
}
