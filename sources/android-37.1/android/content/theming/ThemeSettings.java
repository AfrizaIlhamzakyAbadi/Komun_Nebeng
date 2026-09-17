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

package android.content.theming;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents the theme settings for the system.
 * This class holds the core properties that define a user's chosen theme, such as the color
 * source, theme style, and an optional preset color. It is designed to be immutable.
 */
@Hide
@FlaggedApi(android.server.Flags.FLAG_ENABLE_THEME_SERVICE)
public final class ThemeSettings implements Parcelable {
    private final Instant mAppliedTimestamp;
    @ThemeStyle.Type
    private final int mThemeStyle;
    private final boolean mIsWallpaperSeed;
    @NonNull
    private final List<Color> mSeedColors;

    ThemeSettings(Instant appliedTimestamp, @ThemeStyle.Type int themeStyle,
            boolean isWallpaperSeed, @NonNull List<Color> seedColors) {
        this.mAppliedTimestamp = appliedTimestamp;
        this.mThemeStyle = themeStyle;
        this.mIsWallpaperSeed = isWallpaperSeed;
        this.mSeedColors = Collections.unmodifiableList(new ArrayList<>(seedColors));
    }

    private ThemeSettings(Parcel in) {
        mAppliedTimestamp = Instant.ofEpochMilli(in.readLong());
        mThemeStyle = in.readInt();
        mIsWallpaperSeed = in.readBoolean();
        int size = in.readInt();
        List<Color> colors = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            colors.add(Color.valueOf(in.readInt()));
        }
        mSeedColors = Collections.unmodifiableList(colors);
    }

    /**
     * Returns the timestamp indicating when these theme settings were applied or generated.
     */
    public Instant timeStamp() {
        return mAppliedTimestamp;
    }

    /**
     * Returns whether the theme is sourced from the wallpaper.
     *
     * <p>When {@code true}, the theme is dynamically updated by the system whenever the
     * home wallpaper colors change.
     *
     * @return {@code true} if the theme follows the home wallpaper, {@code false} if it uses
     *         fixed preset colors.
     */
    public boolean isWallpaperSeed() {
        return mIsWallpaperSeed;
    }

    /**
     * Returns the style of the theme (e.g., TONAL_SPOT, VIBRANT).
     *
     * @return The seed {@link ThemeStyle.Type}.
     */
    @ThemeStyle.Type
    public int themeStyle() {
        return mThemeStyle;
    }

    /**
     * Alias for {@link #seedColors()}.
     */
    @NonNull
    public List<Color> seedColors() {
        return mSeedColors;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ThemeSettings that = (ThemeSettings) o;
        return mThemeStyle == that.mThemeStyle && mIsWallpaperSeed == that.mIsWallpaperSeed
                && mSeedColors.equals(that.mSeedColors);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mThemeStyle, mIsWallpaperSeed, mSeedColors);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(mAppliedTimestamp.toEpochMilli());
        dest.writeInt(mThemeStyle);
        dest.writeBoolean(mIsWallpaperSeed);
        dest.writeInt(mSeedColors.size());
        for (Color color : mSeedColors) {
            dest.writeInt(color.toArgb());
        }
    }

    public static final Creator<ThemeSettings> CREATOR = new Creator<ThemeSettings>() {
        @Override
        public ThemeSettings createFromParcel(Parcel in) {
            return new ThemeSettings(in);
        }

        @Override
        public ThemeSettings[] newArray(int size) {
            return new ThemeSettings[size];
        }
    };

    @NonNull
    @Override
    public String toString() {
        return "ThemeSettings{" + "mAppliedTimestamp=" + mAppliedTimestamp + ", mThemeStyle="
                + mThemeStyle + ", mIsWallpaperSeed=" + mIsWallpaperSeed + ", mSeedColors="
                + mSeedColors + '}';
    }

    /**
     * Entry point for the {@link ThemeSettings} step-builder.
     *
     * @return the first step in the builder chain.
     */
    public static ThemeSettingsBuilder.StyleSelector builder() {
        return new ThemeSettingsBuilder.BuilderImpl();
    }

    /**
     * Internal factory for reconstructing ThemeSettings from storage or when caching
     * derived colors.
     */
    @Hide
    @NonNull
    public static ThemeSettings createSettingsForService(@NonNull Instant timestamp,
            @ThemeStyle.Type int themeStyle, boolean isWallpaperSeed,
            @NonNull List<Color> seedColors) {
        return ThemeSettingsBuilder.createSettingsForService(timestamp, themeStyle, isWallpaperSeed,
                seedColors);
    }
}
