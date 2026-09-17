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

package android.os;

import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.SuppressLint;
import android.ravenwood.annotation.RavenwoodKeepWholeClass;

import com.android.internal.dev.perfetto.sdk.PerfettoTrace;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Contains the list of all Perfetto categories used by the Android framework when
 * the Perfetto SDK tracing API is enabled.
 *
 * These categories are used to group trace events and allow users to enable or disable
 * specific groups of events when collecting a trace.
 */
@Hide
@RavenwoodKeepWholeClass
public final class PerfettoCategories {
    private PerfettoCategories() {}

    @NonNull
    public static final PerfettoTrace.Category MQ_CATEGORY = new PerfettoTrace.Category("mq");

    @NonNull
    public static final PerfettoTrace.Category JOB_SCHEDULER_CATEGORY =
            new PerfettoTrace.Category("jobscheduler");

    @NonNull
    public static final PerfettoTrace.Category CC_CATEGORY = new PerfettoTrace.Category("cc");

    @NonNull
    public static final PerfettoTrace.Category BIG_LOCKS_CATEGORY =
            new PerfettoTrace.Category("big_locks");

    @NonNull
    public static final PerfettoTrace.Category PROC_LIFECYCLE_CATEGORY =
            new PerfettoTrace.Category("proc_lifecycle");

    @NonNull
    public static final PerfettoTrace.Category PROC_STATE_CATEGORY =
            new PerfettoTrace.Category("proc_state");

    @NonNull
    public static final PerfettoTrace.Category PROC_STATE_COUNTER_CATEGORY =
            new PerfettoTrace.Category("proc_state_counter");

    @NonNull
    public static final PerfettoTrace.Category BROADCAST_CATEGORY =
            new PerfettoTrace.Category("broadcast");

    @NonNull
    public static final PerfettoTrace.Category FREEZER_CATEGORY =
            new PerfettoTrace.Category("freezer");

    @NonNull public static final String PROC_LIFECYCLE_TRACK_NAME = "proc_lifecycle";

    @NonNull public static final String PROC_STATE_TRACK_NAME = "proc_state";

    @NonNull public static final String BROADCAST_TRACK_NAME = "broadcast";

    @NonNull public static final String FREEZER_TRACK_NAME = "freezer";

    @NonNull
    public static final PerfettoTrace.Category ANR_CATEGORY =
            new PerfettoTrace.Category("debug.anr");

    @NonNull
    public static final PerfettoTrace.Category BITMAP_CATEGORY =
            new PerfettoTrace.Category("bitmap");

    @NonNull
    public static final PerfettoTrace.Category SERVICE_STATE_CATEGORY =
            new PerfettoTrace.Category("service_state");

    @NonNull
    public static final PerfettoTrace.Category PROVIDER_STATE_CATEGORY =
            new PerfettoTrace.Category("provider_state");

    /**
     * List of all Perfetto categories used by the Android framework.
     *
     * <p>This list is used to register all framework categories with the Perfetto backend.
     * NOTE: Do not add new categories which are dupes of atrace categories.
     */
    @NonNull
    public static final PerfettoTrace.Category DEBUG_CAPABILITY_CATEGORY =
            new PerfettoTrace.Category("debug.capability");

    @NonNull
    public static final PerfettoTrace.Category DEBUG_RELAYOUT =
            new PerfettoTrace.Category("debug.relayout");

    @NonNull
    public static final PerfettoTrace.Category VIEW_INVALIDATION_CATEGORY =
            new PerfettoTrace.Category("debug.view_invalidation");

    @NonNull
    public static final List<PerfettoTrace.Category> ALL_CATEGORIES =
            Collections.unmodifiableList(Arrays.asList(
                    // go/keep-sorted start
                    ANR_CATEGORY,
                    BIG_LOCKS_CATEGORY,
                    BITMAP_CATEGORY,
                    BROADCAST_CATEGORY,
                    CC_CATEGORY,
                    DEBUG_CAPABILITY_CATEGORY,
                    DEBUG_RELAYOUT,
                    FREEZER_CATEGORY,
                    JOB_SCHEDULER_CATEGORY,
                    MQ_CATEGORY,
                    PROC_LIFECYCLE_CATEGORY,
                    PROC_STATE_CATEGORY,
                    PROC_STATE_COUNTER_CATEGORY,
                    PROVIDER_STATE_CATEGORY,
                    SERVICE_STATE_CATEGORY,
                    VIEW_INVALIDATION_CATEGORY
                    // go/keep-sorted end
    ));

    /**
     * Registers all framework Perfetto categories.
     *
     * <p>This should be called during process initialization to ensure that all categories
     * are known to the Perfetto backend.
     */
    @SuppressLint("UnflaggedApi")
    public static void registerCategories() {
        Trace.nativeRegisterWithPerfetto();
        // TODO(b/498251413): Move this code elsewhere.
        for (PerfettoTrace.Category category : ALL_CATEGORIES) {
            category.register();
        }
    }
}
