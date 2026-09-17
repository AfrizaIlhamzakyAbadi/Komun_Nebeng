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
 * Destination spec for the catch-all renderer for legacy understanders.
 *
 * This destination spec is intentionally hidden and will not be exposed. It is only meant to be
 * used when destination-aware understanders have strongly typed destinations and the legacy
 * understanders did not have any RenderTokens (indicating the secondary renderers).
 */
@Hide
public final class LegacyCatchAllDestinationSpec extends DestinationSpec {
    public static final LegacyCatchAllDestinationSpec INSTANCE =
            new LegacyCatchAllDestinationSpec();

    private LegacyCatchAllDestinationSpec() {
        super(DESTINATION_SPEC_TYPE_LEGACY_CATCH_ALL);
    }

    /**
     * Validates a {@link ContextInsight}.
     * @return {@code true} if the insight is valid for this kind of destination.
     */
    @Override
    public boolean validate(@NonNull ContextInsight insight) {
        return true;
    }
}
