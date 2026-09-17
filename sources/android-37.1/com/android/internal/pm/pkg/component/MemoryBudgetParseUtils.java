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

package com.android.internal.pm.pkg.component;

import static android.content.pm.PackageManager.FEATURE_AUTOMOTIVE;
import static android.content.pm.PackageManager.FEATURE_LEANBACK;
import static android.content.pm.PackageManager.FEATURE_WATCH;

import android.annotation.NonNull;
import android.content.pm.MemoryBudget;
import android.content.pm.parsing.result.ParseInput;
import android.content.pm.parsing.result.ParseResult;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.content.res.XmlResourceParser;

import com.android.internal.R;
import com.android.internal.pm.pkg.parsing.ParsingPackage;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Set;

/**
 * @hide
 */
public class MemoryBudgetParseUtils {

    private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
    private static final String PRV_ANDROID_NS = "http://schemas.android.com/apk/prv/res/android";
    private static final Set<String> SUPPORTED_HW_FEATURES =
            Set.of(FEATURE_AUTOMOTIVE, FEATURE_LEANBACK, FEATURE_WATCH);

    private static boolean hasUnknownAndroidAttributes(XmlResourceParser parser, int[] styleable) {
        int attributeCount = parser.getAttributeCount();
        for (int i = 0; i < attributeCount; i++) {
            String namespace = parser.getAttributeNamespace(i);

            // Only treat an attribute as an "unknown platform attribute" if it belongs
            // to the standard or private Android namespaces. Attributes from other
            // namespaces (e.g. custom namespaces for tooling or third-party libraries)
            // should be ignored by the parser but should not cause the entire
            // memory-budget element to be discarded.
            if (ANDROID_NS.equals(namespace) || PRV_ANDROID_NS.equals(namespace)) {
                int attrResId = parser.getAttributeNameResource(i);
                if (attrResId == 0 || java.util.Arrays.binarySearch(styleable, attrResId) < 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @NonNull
    public static ParseResult<MemoryBudget> parseMemoryBudget(ParseInput input,
            ParsingPackage pkg, Resources res, XmlResourceParser parser)
            throws IOException, XmlPullParserException {
        // If the element contains unknown attributes from standard or private Android namespaces,
        // it means this budget definition is using features not supported by the current platform.
        // We ignore the entire element to allow fallback to a baseline budget.
        if (hasUnknownAndroidAttributes(parser, R.styleable.AndroidManifestMemoryBudget)) {
            return input.success(null);
        }
        TypedArray sa = res.obtainAttributes(parser, R.styleable.AndroidManifestMemoryBudget);
        try {
            long maxMb = sa.getInt(R.styleable.AndroidManifestMemoryBudget_maxMb, -1);
            if (sa.hasValue(R.styleable.AndroidManifestMemoryBudget_maxMb) && maxMb <= 0) {
                return input.error("maxMb must be a positive integer, got: " + maxMb);
            }

            int state = sa.getInt(R.styleable.AndroidManifestMemoryBudget_state,
                    MemoryBudget.STATE_FOREGROUND);
            if (state != MemoryBudget.STATE_FOREGROUND
                    && state != MemoryBudget.STATE_PERCEPTIBLE
                    && state != MemoryBudget.STATE_BACKGROUND) {
                return input.error("Invalid memory budget state: " + state);
            }

            long additionalBytesPerDisplayPixel = sa.getInt(
                    R.styleable.AndroidManifestMemoryBudget_additionalBytesPerDisplayPixel, 0);
            if (additionalBytesPerDisplayPixel < 0) {
                return input.error("additionalBytesPerDisplayPixel must be a "
                    + "non-negative integer, got: " + additionalBytesPerDisplayPixel);
            }

            long additionalMbPerDensity = sa.getInt(
                    R.styleable.AndroidManifestMemoryBudget_additionalMbPerDensity, 0);
            if (additionalMbPerDensity < 0) {
                return input.error("additionalMbPerDensity must be a non-negative integer, got: "
                        + additionalMbPerDensity);
            }

            String feature = sa.getString(R.styleable.AndroidManifestMemoryBudget_feature);
            if (feature == null) {
                feature = "";
            } else if (!SUPPORTED_HW_FEATURES.contains(feature)) {
                return input.error("feature must be in the set of {"
                        + String.join(",", SUPPORTED_HW_FEATURES) + "}, got: "
                        + feature);
            }

            return input.success(new MemoryBudget(maxMb, state, additionalBytesPerDisplayPixel,
                    additionalMbPerDensity, feature));
        } catch (IllegalArgumentException e) {
            // The parameters to the budget are illegal or the total number of budgets in the list
            // is too great.
            return input.error(e.getMessage());
        } finally {
            sa.recycle();
        }
    }
}

