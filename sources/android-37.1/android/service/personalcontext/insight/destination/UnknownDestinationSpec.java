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

/** Destination spec for an unknown renderer. Accepts any kind of {@link ContextInsight}. */
@Hide
public final class UnknownDestinationSpec extends DestinationSpec {
    // TODO(b/516534291): Make this public.

    @Hide
    public static final UnknownDestinationSpec INSTANCE = new UnknownDestinationSpec();

    private UnknownDestinationSpec() {
        super(DESTINATION_SPEC_TYPE_UNKNOWN);
    }

    @Override
    public boolean validate(@NonNull ContextInsight insight) {
        // Allow any insight in any structure.
        return insight != null;
    }
}
