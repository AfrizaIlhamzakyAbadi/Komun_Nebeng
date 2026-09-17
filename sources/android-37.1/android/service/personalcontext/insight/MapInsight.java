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

package android.service.personalcontext.insight;

import static java.util.Objects.requireNonNull;

import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.Bundle;
import android.service.personalcontext.insight.interaction.AttributionDetails;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * An insight that stores a key-value mapping of other insights.
 *
 * This should only be used for assigning semantic meaning to other insights. Ordering of insights
 * should be done with {@link ListInsight}.
 */
@Hide
public final class MapInsight extends ContextInsight {
    // TODO(b/516534291): Make this public.

    private final Map<String, ContextInsight> mInsights;

    /**
     * Internal constructor only for use by {@link ContextInsight#createInsightFromBundle(Bundle)}
     * and {@link Builder}.
     */
    MapInsight(
            @NonNull ConstructorParams baseParams,
            @NonNull Bundle bundle) {
        this(baseParams, extractInsightMapFromBundle(bundle));
    }

    private MapInsight(
            @NonNull ConstructorParams baseParams,
            @NonNull Map<String, ContextInsight> insights) {
        super(baseParams);
        mInsights = Collections.unmodifiableMap(insights);
    }

    private static Map<String, ContextInsight> extractInsightMapFromBundle(Bundle bundle) {
        final Map<String, ContextInsight> insights = new HashMap<>();
        for (String key : bundle.keySet()) {
            insights.put(key, ContextInsight.createInsightFromBundle(bundle.getBundle(key)));
        }
        return insights;
    }

    @Override
    @InsightType
    int getInsightType() {
        return INSIGHT_TYPE_MAP;
    }

    @Hide
    @Override
    public void accept(@NonNull InsightVisitor visitor, int index) {
        visitor.visit(this, index);
    }

    /** Returns the insights inside the list. */
    @NonNull
    public Map<String, ContextInsight> getInsights() {
        return mInsights;
    }

    @Override
    @NonNull
    Bundle toBundleImpl(boolean includeHints) {
        final Bundle result = new Bundle();
        for (Map.Entry<String, ContextInsight> entry : mInsights.entrySet()) {
            result.putBundle(entry.getKey(), entry.getValue().toBundle(includeHints));
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MapInsight)) return false;
        if (!super.equals(o)) return false;

        MapInsight that = (MapInsight) o;
        return mInsights.equals(that.mInsights);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mInsights);
    }

    /** Builder for {@link MapInsight}. */
    public static final class Builder {
        private final ConstructorParams.Builder mBaseBuilder = new ConstructorParams.Builder();
        private final Map<String, ContextInsight> mInsights = new HashMap<>();

        /** Adds an insight to the builder's list of insights. */
        @NonNull
        public Builder addInsight(@NonNull String key, @NonNull ContextInsight insight) {
            mInsights.put(key, requireNonNull(insight));
            return this;
        }

        /**
         * Sets the attribution details that can be shown to the user.
         *
         * @param attributionDetails Details to show user when they ask for how this insight was
         *                           generated.
         */
        @NonNull
        Builder setAttributionDetails(@Nullable AttributionDetails attributionDetails) {
            mBaseBuilder.setAttributionDetails(attributionDetails);
            return this;
        }

        /** Create and return a new {@link MapInsight}. */
        @NonNull
        public MapInsight build() {
            return new MapInsight(mBaseBuilder.build(), mInsights);
        }
    }
}
