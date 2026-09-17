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

package android.app.contentsafety;

import static android.app.contentsafety.flags.Flags.FLAG_CONTENT_SAFETY_API_V2;

import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.annotation.SystemApi;

import java.util.List;

/**
 * A callback for streaming classification results.
 *
 */
@SystemApi
@FlaggedApi(FLAG_CONTENT_SAFETY_API_V2)
public interface CheckContentCallback {

    /**
     * Called when classification results are ready.
     *
     * @param result is a list of the {@link ContentClassificationResult} from classifying the
     *               provided input list or a subset.
     */
    void onClassification(@NonNull List<ContentClassificationResult> result);

    /**
     * Called when classification is completed for all items in the provided input list.
     */
    void onClassificationComplete();
}
