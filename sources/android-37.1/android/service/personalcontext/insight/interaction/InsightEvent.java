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

package android.service.personalcontext.insight.interaction;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.personalcontext.Flags;
import android.service.personalcontext.RenderToken;
import android.service.personalcontext.insight.ContextInsight;
import android.service.personalcontext.insight.ContextInsightWrapper;
import android.service.personalcontext.insight.PublishedContextInsight;

import androidx.annotation.NonNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.UUID;

/**
 * Provides information about an event that occurred on an insight.
 */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PERSONAL_CONTEXT_SERVICE)
public final class InsightEvent implements Parcelable {
    // TODO(b/516534291): Make this public.

    /**
     * Enumeration of event types.
     */
    @Hide
    @IntDef(
            prefix = {"EVENT_"},
            value = {
                    EVENT_UNKNOWN,
                    EVENT_SHOW,
                    EVENT_HIDE,
                    EVENT_USER_TAP,
                    EVENT_USER_LONG_PRESS,
                    EVENT_USER_DISMISS,
                    EVENT_USER_ATTRIBUTION_REQUESTED,
                    EVENT_USER_FEEDBACK_POSITIVE,
                    EVENT_USER_FEEDBACK_NEGATIVE,
                    EVENT_RETIRED,
            })
    @Retention(RetentionPolicy.SOURCE)
    public @interface EventType {
    }

    /** Event type for unknown events. */
    public static final int EVENT_UNKNOWN = 0;

    /** Event type for when the insight is shown to the user. */
    public static final int EVENT_SHOW = 1;

    /** Event type for when the insight is hidden from the user. */
    public static final int EVENT_HIDE = 2;

    /** Event type for when the user taps on the insight. */
    public static final int EVENT_USER_TAP = 3;

    /** Event type for when the user long-presses on the insight. */
    public static final int EVENT_USER_LONG_PRESS = 4;

    /** Event type for when the user dismisses the insight. */
    public static final int EVENT_USER_DISMISS = 5;

    /** Event type for when the user requests attribution for the insight. */
    public static final int EVENT_USER_ATTRIBUTION_REQUESTED = 6;

    /** Event type for when the user enters positive feedback on the insight. */
    public static final int EVENT_USER_FEEDBACK_POSITIVE = 7;

    /** Event type for when the user enters negative feedback on the insight. */
    public static final int EVENT_USER_FEEDBACK_NEGATIVE = 8;

    /** Event type for when the insight is no longer used by renderers. */
    // TODO(b/516534291): Make this public.
    @Hide
    public static final int EVENT_RETIRED = 9;

    private final @EventType int mEventType;
    private final ContextInsight mContextInsight;
    private final long mTimestamp;

    @Hide
    public InsightEvent(
            @EventType int eventType,
            @NonNull ContextInsight insight,
            long timestamp) {
        mEventType = eventType;
        mContextInsight = insight;
        mTimestamp = timestamp;
    }

    private InsightEvent(Parcel in) {
        mEventType = in.readInt();
        mContextInsight = in.readParcelable(
                /* classLoader= */ null, ContextInsightWrapper.class).getContextInsight();
        mTimestamp = in.readLong();
    }

    /** Gets the type of event that occurred. */
    public int getEventType() {
        return mEventType;
    }

    /** Gets the insight that the event occurred on. */
    @NonNull
    @SystemApi
    public PublishedContextInsight getInsight() {
        // TODO(b/516534291): Make this deprecated.
        return new PublishedContextInsight(mContextInsight);
    }

    /** Gets the insight that the event occurred on. */
    @Hide
    @NonNull
    public ContextInsight getContextInsight() {
        // TODO(b/516534291): Make this public.

        return mContextInsight;
    }

    /** Gets the system timethat the event occurred at. */
    public long getTimestamp() {
        return mTimestamp;
    }

    /** Gets the RenderToken of the Renderer that triggered the event. */
    @NonNull
    public RenderToken getRenderToken() {
        // TODO(b/516534291): Make this deprecated.
        return new RenderToken(UUID.randomUUID(), null);
    }

    @NonNull
    public static final Creator<InsightEvent> CREATOR = new Creator<InsightEvent>() {
        @Override
        public InsightEvent createFromParcel(Parcel in) {
            return new InsightEvent(in);
        }

        @Override
        public InsightEvent[] newArray(int size) {
            return new InsightEvent[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mEventType);
        dest.writeParcelable(new ContextInsightWrapper(mContextInsight), 0);
        dest.writeLong(mTimestamp);
    }
}
