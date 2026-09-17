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

package android.service.personalcontext.understander;

import android.annotation.Hide;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.personalcontext.insight.ContextInsight;
import android.service.personalcontext.insight.ContextInsightWrapper;
import android.service.personalcontext.insight.destination.ContextDestination;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Response data type for IUnderstander#understand.
 */
@Hide
public final class UnderstandCallbackResponse implements Parcelable {
    final Map<ContextDestination, ContextInsight> mInsights;

    public UnderstandCallbackResponse(Map<ContextDestination, ContextInsight> insights) {
        mInsights = insights != null ? insights : Collections.emptyMap();
    }

    private UnderstandCallbackResponse(Parcel in) {
        mInsights = new HashMap<>();
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            mInsights.put(
                    in.readParcelable(null, ContextDestination.class),
                    in.readParcelable(null, ContextInsightWrapper.class).getContextInsight());
        }
    }

    public static final Creator<UnderstandCallbackResponse> CREATOR =
            new Creator<>() {
                @Override
                public UnderstandCallbackResponse createFromParcel(Parcel in) {
                    return new UnderstandCallbackResponse(in);
                }

                @Override
                public UnderstandCallbackResponse[] newArray(int size) {
                    return new UnderstandCallbackResponse[size];
                }
            };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mInsights.size());
        for (Map.Entry<ContextDestination, ContextInsight> entry : mInsights.entrySet()) {
            dest.writeParcelable(entry.getKey(), flags);
            dest.writeParcelable(new ContextInsightWrapper(entry.getValue()), flags);
        }
    }

    public Map<ContextDestination, ContextInsight> getInsights() {
        return mInsights;
    }
}
