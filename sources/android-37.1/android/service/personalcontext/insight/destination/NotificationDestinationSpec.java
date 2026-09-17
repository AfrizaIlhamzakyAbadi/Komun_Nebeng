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
import android.service.personalcontext.insight.ActionableInsight;
import android.service.personalcontext.insight.ContextInsight;
import android.service.personalcontext.insight.DisplayInsight;
import android.service.personalcontext.insight.ListInsight;

/**
 * Destination spec for insights that will be rendererd as an adjustment to an existing
 * notification.
 *
 * Notification renderers expect any of
 * <ul>
 *     <li>{@link DisplayInsight}
 *     <li>{@link ActionableInsight}
 *     <li>{@link ListInsight} which contains a list of {@link DisplayInsight}s and/or
 *         {@link ActionableInsight}s
 * </ul>
 */
@Hide
public final class NotificationDestinationSpec extends DestinationSpec {
    // TODO(b/516534291): Make this public.

    public static final NotificationDestinationSpec INSTANCE = new NotificationDestinationSpec();

    private NotificationDestinationSpec() {
        super(DESTINATION_SPEC_TYPE_NOTIFICATION);
    }

    private static final InsightRule VALIDATION_RULE = isAnyOf(
            isClass(DisplayInsight.class),
            isClass(ActionableInsight.class),
            isListOfAnyOf(
                    isClass(DisplayInsight.class),
                    isClass(ActionableInsight.class)));

    /**
     * Validates a {@link ContextInsight}.
     * @return {@code true} if the insight is valid for this kind of destination.
     */
    @Override
    public boolean validate(@NonNull ContextInsight insight) {
        return VALIDATION_RULE.validate(insight);
    }
}
