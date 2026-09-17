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

package android.view;

import android.annotation.Hide;
import android.os.PerfettoCategories;
import android.util.LongArray;

import com.android.internal.dev.perfetto.sdk.PerfettoTrace;
import com.android.internal.dev.perfetto.sdk.PerfettoTrackEventBuilder;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Helper class to trace invalidations that cause window redraws.
 * Guarded by Perfetto category "view_invalidation".
 */
@Hide
public final class ViewInvalidationTracer {

    private static final AtomicLong sFlowIdCounter = new AtomicLong(1);
    private final LongArray mCurrentFlowIds = new LongArray();

    public ViewInvalidationTracer() {
    }

    /**
     * Records an invalidation triggered by a descendant view.
     *
     * @param descendant The view that was invalidated.}
     */
    public void recordInvalidation(View descendant) {
        if (PerfettoCategories.VIEW_INVALIDATION_CATEGORY.isEnabled()) {
            final long flowId = sFlowIdCounter.getAndIncrement();
            mCurrentFlowIds.add(flowId);

            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            final String reason = descendant != null
                    ? stack[4] + ": descendant=" + descendant.getClass().getName()
                    : stack[4].toString();

            PerfettoTrace.expensiveDebugCallStack(
                            PerfettoCategories.VIEW_INVALIDATION_CATEGORY, reason,
                            stack)
                    .addFlow(flowId)
                    .emit();

            PerfettoTrace.instant(PerfettoCategories.VIEW_INVALIDATION_CATEGORY, reason)
                    .emit();
        }
    }

    /**
     * Traces the traversal with all accumulated invalidation flows.
     *
     * @param title The title of the window or section being logged.
     */
    public void traceTraversals(CharSequence title) {
        if (PerfettoCategories.VIEW_INVALIDATION_CATEGORY.isEnabled()) {
            if (mCurrentFlowIds.size() != 0) {
                final PerfettoTrackEventBuilder builder =
                        PerfettoTrace.instant(PerfettoCategories.VIEW_INVALIDATION_CATEGORY,
                                title + ": performTraversals");
                for (int idx = 0; idx < mCurrentFlowIds.size(); idx++) {
                    builder.addTerminatingFlow(mCurrentFlowIds.get(idx));
                }
                builder.emit();
                mCurrentFlowIds.clear();
            }
        }
    }
}

