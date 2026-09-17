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
import android.annotation.NonNull;
import android.service.personalcontext.insight.ContextInsight;

/**
 * Destination spec for a renderer that uses RenderTokens. There is no structure documentation, and
 * this is an internal {@link DestinationSpec}, to support legacy flows that use
 * {@link android.service.personalcontext.RenderToken}. This class is not visible to third parties
 * because it is only used to support legacy, pre-public release OEM implementations that still use
 * {@link android.service.personalcontext.RenderToken} instead of {@link ContextDestination}.
 *
 */
@Hide
public final class LegacyRenderTokenDestinationSpec extends DestinationSpec {
    public static final LegacyRenderTokenDestinationSpec INSTANCE =
            new LegacyRenderTokenDestinationSpec();

    private LegacyRenderTokenDestinationSpec() {
        super(DESTINATION_SPEC_TYPE_LEGACY_RENDER_TOKEN);
    }

    @Override
    public boolean validate(@NonNull ContextInsight insight) {
        // Allow any insight in any structure.
        return insight != null;
    }
}
