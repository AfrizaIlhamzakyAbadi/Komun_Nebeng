// This file is auto-generated. DO NOT MODIFY.
// Args: com.android.internal.pm.RoSystemFeatures \
//            --feature=AUTOMOTIVE:UNAVAILABLE \
//            --feature=EMBEDDED:UNAVAILABLE \
//            --feature=LEANBACK:UNAVAILABLE \
//            --feature=PC:UNAVAILABLE \
//            --feature=TELEVISION:UNAVAILABLE \
//            --feature=WATCH:0 \
//            --readonly=true \
//            --output=out/soong/.intermediates/frameworks/base/core/java/systemfeatures-gen-srcs/gen/RoSystemFeatures.java
package com.android.internal.pm;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.util.ArrayMap;

/**
 * @hide
 */
public final class RoSystemFeatures {
    /**
     * @hide
     */
    @Nullable
    public static Boolean maybeHasFeature(String featureName, int version) {
        if (featureName == null) return null;
        switch (featureName) {
            case PackageManager.FEATURE_AUTOMOTIVE: return false;
            case PackageManager.FEATURE_EMBEDDED: return false;
            case PackageManager.FEATURE_LEANBACK: return false;
            case PackageManager.FEATURE_PC: return false;
            case PackageManager.FEATURE_TELEVISION: return false;
            case PackageManager.FEATURE_WATCH: return 0 >= version;
            default: break;
        }
        return null;
    }

    /**
     * Gets features marked as available at compile-time, keyed by name.
     *
     * @hide
     */
    @NonNull
    public static ArrayMap<String, FeatureInfo> getReadOnlySystemEnabledFeatures() {
        ArrayMap<String, FeatureInfo> features = new ArrayMap<>(1);
        FeatureInfo fi = new FeatureInfo();
        fi.name = PackageManager.FEATURE_WATCH;
        fi.version = 0;
        features.put(fi.name, new FeatureInfo(fi));
        return features;
    }
}
