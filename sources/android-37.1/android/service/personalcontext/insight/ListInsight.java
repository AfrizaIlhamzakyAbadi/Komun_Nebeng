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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An insight that stores an ordered list of other insights.
 *
 * This should only be used for an ordered list of insights. Semantic meaning should not be assigned
 * to specific positions in the list, and insights in the list should be independent of each other.
 */
@Hide
public final class ListInsight extends ContextInsight {
    // TODO(b/516534291): Make this public.

    private static final String KEY_INSIGHTS = "insights";

    private final List<ContextInsight> mInsights;

    /**
     * Internal constructor only for use by {@link ContextInsight#createInsightFromBundle(Bundle)}
     * and {@link Builder}.
     */
    ListInsight(
            @NonNull ContextInsight.ConstructorParams baseParams,
            @NonNull Bundle bundle) {
        this(
                baseParams,
                ContextInsightWrapper.unwrapList(List.of(
                        bundle.getParcelableArray(KEY_INSIGHTS, ContextInsightWrapper.class))));
    }

    private ListInsight(
            @NonNull ContextInsight.ConstructorParams baseParams,
            @NonNull List<ContextInsight> insights) {
        super(baseParams);
        mInsights = Collections.unmodifiableList(insights);
    }

    @Override
    @InsightType
    int getInsightType() {
        return INSIGHT_TYPE_LIST;
    }

    @Hide
    @Override
    public void accept(@NonNull InsightVisitor visitor, int index) {
        visitor.visit(this, index);
    }

    /** Returns the insights inside the list. */
    @NonNull
    public List<ContextInsight> getInsights() {
        return mInsights;
    }

    @Override
    @NonNull
    Bundle toBundleImpl(boolean includeHints) {
        final Bundle result = new Bundle();
        result.putParcelableArray(KEY_INSIGHTS,
                ContextInsightWrapper.wrapList(mInsights).toArray(new ContextInsightWrapper[0]));
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ListInsight)) return false;
        if (!super.equals(o)) return false;
        ListInsight that = (ListInsight) o;
        return mInsights.equals(that.mInsights);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mInsights);
    }

    /** Builder for {@link ListInsight}. */
    public static final class Builder {
        private final ConstructorParams.Builder mBaseBuilder = new ConstructorParams.Builder();
        private final List<ContextInsight> mInsights = new ArrayList<>();

        /** Adds an insight to the builder's list of insights. */
        @NonNull
        public Builder addInsight(@NonNull ContextInsight insight) {
            mInsights.add(requireNonNull(insight));
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

        /** Create and return a new {@link ListInsight}. */
        @NonNull
        public ListInsight build() {
            return new ListInsight(mBaseBuilder.build(), mInsights);
        }
    }
}
