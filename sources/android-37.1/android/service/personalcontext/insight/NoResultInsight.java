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

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.Bundle;
import android.service.personalcontext.Flags;
import android.service.personalcontext.insight.interaction.AttributionDetails;

/**
 * An insight that should be returned by an understander when there is no result to be generated.
 * Since there's no synchronous response from sending in a hint to receiving an insight, receiving
 * an insight allows CUJs to stop waiting for a response.
 */
// TODO(b/513263677): make this public
@Hide
@FlaggedApi(Flags.FLAG_PUBLIC_APIS)
public final class NoResultInsight extends ContextInsight {

    /** Internal constructor. */
    @Hide
    public NoResultInsight(@NonNull ConstructorParams params) {
        super(params);
    }

    /** Internal constructor for use by {@link ContextInsight#createInsightFromBundle(Bundle)}. */
    NoResultInsight(@NonNull ConstructorParams baseParams, @NonNull Bundle bundle) {
        super(baseParams);
    }

    @Override
    @InsightType
    int getInsightType() {
        return INSIGHT_TYPE_NO_RESULT;
    }

    @NonNull
    @Override
    Bundle toBundleImpl(boolean includeHints) {
        return new Bundle();
    }

    @Override
    public void accept(@NonNull InsightVisitor visitor, int index) {
        visitor.visit(this, index);
    }

    /** Builder for {@link NoResultInsight}. */
    public static final class Builder {
        private final ConstructorParams.Builder mBaseBuilder = new ConstructorParams.Builder();

        /**
         * Sets the attribution details that can be shown to the user.
         *
         * @param attributionDetails Details to show user when they ask for how this insight was
         *     generated.
         */
        @NonNull
        Builder setAttributionDetails(@Nullable AttributionDetails attributionDetails) {
            mBaseBuilder.setAttributionDetails(attributionDetails);
            return this;
        }

        /** Builds the {@link NoResultInsight}. */
        @NonNull
        public NoResultInsight build() {
            return new NoResultInsight(mBaseBuilder.build());
        }
    }
}
