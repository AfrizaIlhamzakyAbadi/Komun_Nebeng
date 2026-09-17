/*
 * Copyright 2026 The Android Open Source Project
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

package android.service.personalcontext.insight.destination;

import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.os.Parcel;
import android.service.personalcontext.insight.BundleInsight;
import android.service.personalcontext.insight.ContextInsight;
import android.service.personalcontext.insight.ListInsight;
import android.service.personalcontext.insight.MapInsight;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.function.Function;

/**
 * Sentinel interface for destination specifications.
 *
 * Subclasses for this interface describe how to build a conforming {@link ContextInsight} for a
 * given {@link ContextDestination}.
 *
 * @see android.service.personalcontext.understander.ContextUnderstanderService#onUnderstand
 */
@Hide
public abstract class DestinationSpec {
    // TODO(b/516534291): Make this public.

    /** Enumeration of destination spec types. */
    @Hide
    @IntDef(
            prefix = {"DESTINATION_SPEC_TYPE_"},
            value = {
                    DESTINATION_SPEC_TYPE_UNKNOWN,
                    DESTINATION_SPEC_TYPE_LEGACY_RENDER_TOKEN,
                    DESTINATION_SPEC_TYPE_LEGACY_CATCH_ALL,
                    DESTINATION_SPEC_TYPE_CATCH_ALL,
                    DESTINATION_SPEC_TYPE_NOTIFICATION,
            })
    @Retention(RetentionPolicy.SOURCE)
    public @interface DestinationSpecType {}

    static final int DESTINATION_SPEC_TYPE_UNKNOWN = -1;
    static final int DESTINATION_SPEC_TYPE_LEGACY_RENDER_TOKEN = 1;
    static final int DESTINATION_SPEC_TYPE_LEGACY_CATCH_ALL = 2;
    static final int DESTINATION_SPEC_TYPE_CATCH_ALL = 3;
    static final int DESTINATION_SPEC_TYPE_NOTIFICATION = 4;

    private final int mType;

    /** Constructor for use by subclasses. */
    DestinationSpec(@DestinationSpecType int type) {
        mType = type;
    }

    /**
     * Validates a {@link ContextInsight} against this {@link DestinationSpec}.
     *
     * <p>Understanders can call this method before returning an insight to help make sure that the
     * insight is well-formed and has all required fields for this destination.
     *
     * <p>Implementations of this method are subject to change between versions of Android.
     *
     * @return {@code true} if the insight is valid for this kind of destination.
     */
    public abstract boolean validate(@NonNull ContextInsight insight);

    /** Writes data to a parcel to be able to reconstruct a {@link DestinationSpec}. */
    void writeToParcel(@NonNull Parcel dest) {
        dest.writeInt(mType);
    }

    /** Reads data from a parcel and reconstructs a {@link DestinationSpec}. */
    @NonNull
    static DestinationSpec readFromParcel(Parcel src) {
        return switch (src.readInt()) {
            case DESTINATION_SPEC_TYPE_LEGACY_RENDER_TOKEN ->
                    LegacyRenderTokenDestinationSpec.INSTANCE;
            case DESTINATION_SPEC_TYPE_LEGACY_CATCH_ALL -> LegacyCatchAllDestinationSpec.INSTANCE;
            case DESTINATION_SPEC_TYPE_CATCH_ALL -> CatchAllDestinationSpec.INSTANCE;
            case DESTINATION_SPEC_TYPE_NOTIFICATION -> NotificationDestinationSpec.INSTANCE;
            default -> UnknownDestinationSpec.INSTANCE;
        };
    }

    /** Composable insight rule. */
    interface InsightRule {
        boolean validate(@NonNull ContextInsight insight);
    }

    /** Checks to see if an insight is an exact class. */
    static InsightRule isClass(Class<? extends ContextInsight> checkClass) {
        return insight -> checkClass.equals(insight.getClass());
    }

    /** Checks multiple rules, passes if any of them pass. */
    static InsightRule isAnyOf(InsightRule... rules) {
        return insight -> {
            for (InsightRule rule : rules) {
                if (rule.validate(insight)) return true;
            }
            return false;
        };
    }

    /** Checks multiple rules, only passes if all of them pass. */
    static InsightRule isAllOf(InsightRule... rules) {
        return insight -> {
            for (InsightRule rule : rules) {
                if (!rule.validate(insight)) return false;
            }
            return true;
        };
    }

    /** Checks that an insight is non-empty ListInsight, all children match a child rule. */
    static InsightRule isListOf(InsightRule childRule) {
        return insight -> {
            if (insight instanceof ListInsight listInsight) {
                if (listInsight.getInsights().isEmpty()) return false;
                for (ContextInsight child : listInsight.getInsights()) {
                    if (!childRule.validate(child)) return false;
                }
                return true;
            }
            return false;
        };
    }

    /** Checks that an insight is non-empty ListInsight, each child must match any child rule. */
    static InsightRule isListOfAnyOf(InsightRule... childRules) {
        return isListOf(isAnyOf(childRules));
    }

    /** Checks that an insight is non-empty ListInsight, each child must match all child rule. */
    static InsightRule isListOfAllOf(InsightRule... childRules) {
        return isListOf(isAllOf(childRules));
    }

    /** Checks that an insight is a MapInsight, with an optional key, where child matches rule. */
    static InsightRule isMapWithOptional(String key, InsightRule childRule) {
        return isMapWith(key, true, childRule);
    }

    /** Checks that an insight is a MapInsight, with a required key, where child matches rule. */
    static InsightRule isMapWithRequired(String key, InsightRule childRule) {
        return isMapWith(key, false, childRule);
    }

    /** Checks that an insight is a MapInsight, where child matches rule. */
    static InsightRule isMapWith(String key, boolean optional, InsightRule childRule) {
        return insight -> {
            if (insight instanceof MapInsight mapInsight) {
                final ContextInsight child = mapInsight.getInsights().get(key);
                if (child == null) {
                    return optional;
                } else {
                    return childRule.validate(child);
                }
            }
            return false;
        };
    }

    /** Checks that an insight is a BundleInsight, with an optional key, value matches type. */
    static InsightRule isBundleWithOptional(String key, Class<?> type) {
        return isBundleWith(key, true, value -> type.equals(value.getClass()));
    }

    /** Checks that an insight is a BundleInsight, with an optional key, value passes test. */
    static <T> InsightRule isBundleWithOptional(String key, Function<T, Boolean> valueCheck) {
        return isBundleWith(key, true, valueCheck);
    }

    /** Checks that an insight is a BundleInsight, with a required key, value matches type. */
    static InsightRule isBundleWithRequired(String key, Class<?> type) {
        return isBundleWith(key, false, value -> type.equals(value.getClass()));
    }

    /** Checks that an insight is a BundleInsight, with a required key, value passes test. */
    static <T> InsightRule isBundleWithRequired(String key, Function<T, Boolean> valueCheck) {
        return isBundleWith(key, false, valueCheck);
    }

    /** Checks that an insight is a BundleInsight, value passes test. */
    static <T> InsightRule isBundleWith(
            String key, boolean optional, Function<T, Boolean> valueCheck) {
        return insight -> {
            if (insight instanceof BundleInsight bundleInsight) {
                final T value = (T) bundleInsight.getDataBundle().get(key);
                if (value == null) {
                    return optional;
                } else {
                    return valueCheck.apply(value);
                }
            }
            return false;
        };
    }
}
