/*
 * Copyright (C) 2025 The Android Open Source Project
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

package android.view.selectiontoolbar;

import android.annotation.Dimension;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.window.InputTransferToken;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Hide
public class SelectionToolbarRequest implements Parcelable {
    private boolean mLayoutRequired;
    @NonNull
    private List<ToolbarMenuItem> mMenuItems;
    @NonNull
    private Rect mContentRect;
    @Dimension
    private int mSuggestedWidth;
    @NonNull
    private Rect mViewPortOnScreen;
    @NonNull
    private InputTransferToken mHostInputToken;
    private boolean mIsLightTheme;
    @NonNull
    private Configuration mConfiguration;

    public SelectionToolbarRequest(boolean layoutRequired,
            @NonNull List<ToolbarMenuItem> menuItems, @NonNull Rect contentRect,
            @Dimension int suggestedWidth, @NonNull Rect viewPortOnScreen,
            @NonNull InputTransferToken hostInputToken, boolean isLightTheme,
            @NonNull Configuration configuration) {
        Objects.requireNonNull(menuItems, "menuItems must not be null.");
        for (int i = 0; i < menuItems.size(); i++) {
            ToolbarMenuItem menuItem = menuItems.get(i);
            Objects.requireNonNull(menuItem, "Each ToolbarMenuItem must not be null.");
            Objects.requireNonNull(menuItem.title, "ToolbarMenuItem titles must not be null.");
        }
        Objects.requireNonNull(contentRect, "contentRect must not be null.");
        Objects.requireNonNull(contentRect, "viewPortOnScreen must not be null.");
        Objects.requireNonNull(hostInputToken, "hostInputToken must not be null.");
        Objects.requireNonNull(configuration, "configuration must not be null.");

        mLayoutRequired = layoutRequired;
        mMenuItems = menuItems;
        mContentRect = contentRect;
        mSuggestedWidth = suggestedWidth;
        mViewPortOnScreen = viewPortOnScreen;
        mHostInputToken = hostInputToken;
        mIsLightTheme = isLightTheme;
        mConfiguration = configuration;
    }

    /**
     * @return if the toolbar menu items need to be re-layout.
     */
    public boolean isLayoutRequired() {
        return mLayoutRequired;
    }

    /**
     * @return the menu items to be rendered in the selection toolbar.
     */
    @NonNull
    public List<ToolbarMenuItem> getMenuItems() {
        return mMenuItems;
    }

    /**
     * @return a rect specifying where the selection toolbar on the screen.
     */
    @NonNull
    public Rect getContentRect() {
        return mContentRect;
    }

    /**
     * @return a recommended maximum suggested width of the selection toolbar.
     */
    @Dimension
    public int getSuggestedWidth() {
        return mSuggestedWidth;
    }

    /**
     * @return the portion of the screen that is available to the selection toolbar.
     */
    @NonNull
    public Rect getViewPortOnScreen() {
        return mViewPortOnScreen;
    }

    /**
     * @return the host application's input token, this allows the remote render service to transfer
     * the touch focus to the host application.
     */
    @NonNull
    public InputTransferToken getHostInputToken() {
        return mHostInputToken;
    }

    /**
     * @return if the host application uses light theme.
     */
    public boolean isLightTheme() {
        return mIsLightTheme;
    }

    /**
     * @return configuration used by the context that created the toolbar.
     */
    @NonNull
    public Configuration getConfiguration() {
        return mConfiguration;
    }

    @Override
    public String toString() {
        return "SelectionToolbarRequest{"
                + "layoutRequired=" + mLayoutRequired
                + "menuItems=" + mMenuItems
                + "contentRect=" + mContentRect
                + "suggestedWidth=" + mSuggestedWidth
                + "viewPortOnScreen=" + mViewPortOnScreen
                + "hostInputToken=" + mHostInputToken
                + "isLightTheme=" + mIsLightTheme
                + "configuration=" + mConfiguration
                + "}";
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(
                Arrays.asList(mLayoutRequired, mMenuItems, mContentRect,
                        mSuggestedWidth, mViewPortOnScreen, mHostInputToken, mIsLightTheme,
                        mConfiguration).toArray());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public final void writeToParcel(Parcel dest, int flags) {
        dest.writeBoolean(mLayoutRequired);
        dest.writeTypedList(mMenuItems, flags);
        dest.writeTypedObject(mContentRect, flags);
        dest.writeInt(mSuggestedWidth);
        dest.writeTypedObject(mViewPortOnScreen, flags);
        dest.writeTypedObject(mHostInputToken, flags);
        dest.writeBoolean(mIsLightTheme);
        dest.writeTypedObject(mConfiguration, flags);
    }

    public static final Parcelable.Creator<SelectionToolbarRequest> CREATOR =
            new Parcelable.Creator<>() {
                @Override
                public SelectionToolbarRequest createFromParcel(Parcel in) {
                    return new SelectionToolbarRequest(
                            in.readBoolean(), /* layoutRequired */
                            Objects.requireNonNull(in.createTypedArrayList(
                                    ToolbarMenuItem.CREATOR)), /* menuItems */
                            Objects.requireNonNull(
                                    in.readTypedObject(Rect.CREATOR)), /* contentRect */
                            in.readInt(), /* suggestedWidth */
                            Objects.requireNonNull(
                                    in.readTypedObject(Rect.CREATOR)), /* viewPortOnScreen */
                            Objects.requireNonNull(in.readTypedObject(InputTransferToken.CREATOR)),
                                    /* hostInputToken */
                            in.readBoolean(), /* isLightTheme */
                            Objects.requireNonNull(
                                    in.readTypedObject(Configuration.CREATOR)) /* configuration */
                    );
                }

                @Override
                public SelectionToolbarRequest[] newArray(int size) {
                    return new SelectionToolbarRequest[size];
                }
            };
}
