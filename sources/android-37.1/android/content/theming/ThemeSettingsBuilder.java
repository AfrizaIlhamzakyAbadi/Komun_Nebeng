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

import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * Builder for creating {@link ThemeSettings} instances using a fluent configuration API.
 */
@Hide
@FlaggedApi(android.server.Flags.FLAG_ENABLE_THEME_SERVICE)
public final class ThemeSettingsBuilder {

    private ThemeSettingsBuilder() {
    }

    @Hide
    @NonNull
    static ThemeSettings createSettingsForService(@NonNull Instant timestamp,
            @ThemeStyle.Type int themeStyle, boolean isWallpaperSeed,
            @NonNull List<Color> seedColors) {
        BuilderImpl builder = new BuilderImpl();
        builder.setAppliedTimestamp(timestamp);
        builder.setStyle(themeStyle);
        return builder.build(isWallpaperSeed, seedColors);
    }

    /** Interface to select the theme style. */
    public interface StyleSelector {
        /** Selects the SPRITZ theme style. */
        SeedConfiguration spritz();

        /** Selects the TONAL_SPOT theme style. */
        SeedConfiguration tonalSpot();

        /** Selects the VIBRANT theme style. */
        SeedConfiguration vibrant();

        /** Selects the EXPRESSIVE theme style. */
        SeedConfiguration expressive();

        /** Selects the RAINBOW theme style. */
        SeedConfiguration rainbow();

        /** Selects the FRUIT_SALAD theme style. */
        SeedConfiguration fruitSalad();

        /** Selects the MONOCHROMATIC theme style. */
        SeedConfiguration monochromatic();

        /** Selects the CMF theme style. */
        DualSeedConfiguration cmf();
    }

    /** Configuration for single-seed theme styles. */
    public interface SeedConfiguration {
        /** Builds theme settings using a preset seed color. */
        ThemeSettings buildPreset(@NonNull Color seed);

        /**
         * Builds theme settings that follow the home wallpaper.
         *
         * <p>Colors will be automatically extracted from the home wallpaper by the system.
         */
        ThemeSettings buildFromWallpaper();

        /**
         * Builds theme settings that follow the home wallpaper, using a specific seed as a hint.
         *
         * <p>The system will use the provided seed color for the current wallpaper, but will
         * automatically extract new colors if the wallpaper changes.
         */
        ThemeSettings buildFromWallpaper(@NonNull Color seed);
    }

    /** Configuration for double-seed theme styles like CMF. */
    public interface DualSeedConfiguration {
        /** Builds theme settings using preset primary and secondary seed colors. */
        ThemeSettings buildPreset(@NonNull Color primarySeed, @NonNull Color secondarySeed);

        /**
         * Builds theme settings that follow the home wallpaper.
         *
         * <p>Colors will be automatically extracted from the home wallpaper by the system.
         */
        ThemeSettings buildFromWallpaper();

        /**
         * Builds theme settings that follow the home wallpaper, using specific seeds as a hint.
         *
         * <p>The system will use the provided seed colors for the current wallpaper, but will
         * automatically extract new colors if the wallpaper changes.
         */
        ThemeSettings buildFromWallpaper(@NonNull Color primarySeed, @NonNull Color secondarySeed);
    }

    static final class BuilderImpl implements StyleSelector, SeedConfiguration,
            DualSeedConfiguration {
        private Instant mAppliedTimestamp;
        private Integer mThemeStyle;

        BuilderImpl setAppliedTimestamp(@NonNull Instant timestamp) {
            mAppliedTimestamp = timestamp;
            return this;
        }

        SeedConfiguration setStyle(@ThemeStyle.Type int style) {
            mThemeStyle = style;
            return this;
        }

        @Override
        public SeedConfiguration spritz() {
            return setStyle(ThemeStyle.SPRITZ);
        }

        @Override
        public SeedConfiguration tonalSpot() {
            return setStyle(ThemeStyle.TONAL_SPOT);
        }

        @Override
        public SeedConfiguration vibrant() {
            return setStyle(ThemeStyle.VIBRANT);
        }

        @Override
        public SeedConfiguration expressive() {
            return setStyle(ThemeStyle.EXPRESSIVE);
        }

        @Override
        public SeedConfiguration rainbow() {
            return setStyle(ThemeStyle.RAINBOW);
        }

        @Override
        public SeedConfiguration fruitSalad() {
            return setStyle(ThemeStyle.FRUIT_SALAD);
        }

        @Override
        public SeedConfiguration monochromatic() {
            return setStyle(ThemeStyle.MONOCHROMATIC);
        }

        @Override
        public DualSeedConfiguration cmf() {
            mThemeStyle = ThemeStyle.CMF;
            return this;
        }

        @Override
        public ThemeSettings buildPreset(@NonNull Color seed) {
            return build(false, List.of(seed));
        }

        @Override
        public ThemeSettings buildPreset(@NonNull Color primarySeed, @NonNull Color secondarySeed) {
            return build(false, List.of(primarySeed, secondarySeed));
        }

        @Override
        public ThemeSettings buildFromWallpaper() {
            return build(true, Collections.emptyList());
        }

        @Override
        public ThemeSettings buildFromWallpaper(@NonNull Color seed) {
            return build(true, List.of(seed));
        }

        @Override
        public ThemeSettings buildFromWallpaper(@NonNull Color primarySeed,
                @NonNull Color secondarySeed) {
            return build(true, List.of(primarySeed, secondarySeed));
        }

        @NonNull
        private ThemeSettings build(boolean isWallpaperSeed, List<Color> seedColors) {
            if (mThemeStyle == null) {
                throw new IllegalStateException("ThemeStyle must be set.");
            }
            if (!isWallpaperSeed && seedColors.isEmpty()) {
                throw new IllegalStateException("At least one seed color must be set for presets.");
            }

            if (!new FieldThemeStyle().validate(mThemeStyle)) {
                throw new IllegalArgumentException("Invalid themeStyle: " + mThemeStyle);
            }

            int requiredSeeds = ThemeStyle.getRequiredSeedCount(mThemeStyle);

            // If seeds are provided (either preset, or service reconstructing auto seeds),
            // strictly enforce that the payload contains enough seeds for the style.
            if (!seedColors.isEmpty() && seedColors.size() < requiredSeeds) {
                throw new IllegalStateException(
                        "Not enough seed colors provided. Style " + mThemeStyle + " requires "
                                + requiredSeeds);
            }

            List<Color> finalSeeds = new java.util.ArrayList<>();
            for (int i = 0; i < requiredSeeds; i++) {
                if (i < seedColors.size()) {
                    finalSeeds.add(seedColors.get(i));
                }
            }

            FieldColor colorHandler = new FieldColor();
            for (Color color : finalSeeds) {
                if (!colorHandler.validate(color)) {
                    throw new IllegalArgumentException("Invalid seed color: " + color);
                }
            }

            Instant timestamp = (mAppliedTimestamp != null) ? mAppliedTimestamp : Instant.now();
            return new ThemeSettings(timestamp, mThemeStyle, isWallpaperSeed, finalSeeds);
        }
    }
}
