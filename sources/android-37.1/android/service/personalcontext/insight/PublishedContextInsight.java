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
import android.annotation.SystemApi;
import android.annotation.TestApi;
import android.os.Bundle;
import android.os.Parcel;
import android.service.personalcontext.Flags;
import android.service.personalcontext.PersonalContextManager;
import android.service.personalcontext.RenderToken;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.UUID;

/**
 * This class captures the details around a published {@link ContextInsight}, including the insight
 * itself and the component that published it to the
 * {@link com.android.server.personalcontext.PersonalContextManagerService}.
 */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PERSONAL_CONTEXT_SERVICE)
public final class PublishedContextInsight {
    private static final String KEY_INSIGHT = "key_insight";

    private final ContextInsight mInsight;

    /**
     * Default constructor for {@link PublishedContextInsight}
     * @param insight the {@link ContextInsight} being published
     * @param publisherComponentId the component id of the publisher
     */
    @Hide
    public PublishedContextInsight(@NonNull ContextInsight insight,
            @NonNull UUID publisherComponentId) {
        if (insight.getPublisherComponentId() != null) {
            mInsight = insight;
        } else {
            mInsight = insight.attachPublisherInfo(publisherComponentId);
        }
    }

    /**
     * Default constructor for {@link PublishedContextInsight}
     * @param insight the {@link ContextInsight} being published
     */
    @Hide
    public PublishedContextInsight(@NonNull ContextInsight insight) {
        assert insight.getPublisherComponentId() != null;
        mInsight = insight;
    }

    /**
     * Returns the published {@link ContextInsight}. This is the content originally sent by the
     * publisher to {@link PersonalContextManager}.
     *
     * @return the originally published {@link ContextInsight}
     */
    public @NonNull ContextInsight getInsight() {
        return mInsight;
    }

    /**
     * Returns the {@link UUID} representing the unique id of the component that published the
     * contained {@link ContextInsight} to the {@link PersonalContextManager}. This is used for
     * cases where the published {@link ContextInsight} is sent back to the
     * {@link PersonalContextManager}, such as event reporting. In these cases, the id helps route
     * back the {@link ContextInsight} to the original publisher.
     *
     * @see PersonalContextManager#reportInsightEvent(PublishedContextInsight, int, RenderToken)
     * @return the component id of the original publisher
     */
    public @NonNull UUID getPublisherComponentId() {
        return mInsight.getPublisherComponentId();
    }

    /**
     * Unbundles a published insight. This is for internal use only in order to assist moving
     * the {@link PublishedContextInsight} inside {@link Parcel}s.
     *
     * @param bundle the {@link Bundle} where the {@link PublishedContextInsight} is written to.
     * @return the retrieved {@link PublishedContextInsight}.
     */
    @TestApi
    @NonNull
    public static PublishedContextInsight createPublishedInsightFromBundle(
            @Nullable Bundle bundle) {
        if (bundle == null) {
            return new PublishedContextInsight(ContextInsight.ERROR_INSIGHT, UUID.randomUUID());
        }

        final ContextInsight insight = ContextInsight.createInsightFromBundle(
                bundle.getBundle(KEY_INSIGHT));
        return new PublishedContextInsight(insight);
    }

    /**
     * Return the {@link Bundle} representation of the {@link PublishedContextInsight}'s data.
     */
    @TestApi
    @NonNull
    public Bundle toBundle() {
        final Bundle b = new Bundle();
        b.putBundle(KEY_INSIGHT, mInsight.toBundle());

        return b;
    }
}
