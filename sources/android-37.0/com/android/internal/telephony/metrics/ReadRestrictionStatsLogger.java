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

package com.android.internal.telephony.metrics;

import android.content.Context;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.nano.PersistAtomsProto.MessagingReadRestrictionEvent;
import com.android.telephony.Rlog;
import java.util.function.Supplier;

import static com.android.internal.telephony.TelephonyStatsLog.MESSAGING_READ_RESTRICTION_REPORTED__READ_RESTRICTED_MESSAGES_APP_OP_MODE__APP_OP_MODE_ALLOWED;
import static com.android.internal.telephony.TelephonyStatsLog.MESSAGING_READ_RESTRICTION_REPORTED__READ_RESTRICTED_MESSAGES_APP_OP_MODE__APP_OP_MODE_IGNORED;
import static com.android.internal.telephony.TelephonyStatsLog.MESSAGING_READ_RESTRICTION_REPORTED__CONTENT_PROVIDER__CONTENT_PROVIDER_SMS_PROVIDER;
import static com.android.internal.telephony.TelephonyStatsLog.MESSAGING_READ_RESTRICTION_REPORTED__CONTENT_PROVIDER__CONTENT_PROVIDER_MMS_PROVIDER;
import static com.android.internal.telephony.TelephonyStatsLog.MESSAGING_READ_RESTRICTION_REPORTED__EVENT_TYPE__RESTRICTED_MESSAGE_INSERTED;
import static com.android.internal.telephony.TelephonyStatsLog.MESSAGING_READ_RESTRICTION_REPORTED__EVENT_TYPE__UNRESTRICTED_MESSAGE_INSERTED;
import static com.android.internal.telephony.TelephonyStatsLog.MESSAGING_READ_RESTRICTION_REPORTED__EVENT_TYPE__UPDATED_MESSAGE_TO_UNRESTRICTED;
import static com.android.internal.telephony.TelephonyStatsLog.MESSAGING_READ_RESTRICTION_REPORTED__EVENT_TYPE__QUERY_RESTRICTED_MESSAGES;

/** Utility class for logging messaging read restriction events to statsd. */
public class ReadRestrictionStatsLogger {
    private static final String TAG = "ReadRestrictionStatsLogger";

    /**
     * Fetches the atoms storage from the PhoneFactory lazily.
     *
     * @return {@code null} if the metrics collector is not available, otherwise the atoms storage
     * instance.
     */
    private Supplier<PersistAtomsStorage> mAtomsStorage = new Supplier<PersistAtomsStorage>() {
        @Override
        public PersistAtomsStorage get() {
            MetricsCollector metricsCollector = PhoneFactory.getMetricsCollector();
            if (metricsCollector == null) {
                Rlog.w(TAG, "MetricsCollector is not available. Skipping logging.");
                return null;
            }
            return metricsCollector.getAtomsStorage();
        }
    };

    private static ReadRestrictionStatsLogger sInstance = null;

    /** Gets the instance of ReadRestrictionStatsLogger */
    public static ReadRestrictionStatsLogger getInstance() {
        if (sInstance == null) {
            synchronized (ReadRestrictionStatsLogger.class) {
                if (sInstance == null) {
                    sInstance = new ReadRestrictionStatsLogger();
                }
            }
        }
        return sInstance;
    }

    private ReadRestrictionStatsLogger() {}

    public enum ContentProvider {
        SMS,
        MMS
    }

    /** Logs a messaging read restriction event to statsd. */
    public void onMessageInserted(ContentProvider contentProvider, int callerUid,
        boolean isMessageReadRestricted) {
        PersistAtomsStorage atomsStorage = mAtomsStorage.get();
        if (atomsStorage == null) {
            return;
        }
        MessagingReadRestrictionEvent event = new MessagingReadRestrictionEvent();
        event.callerUid = callerUid;
        event.contentProvider = getContentProviderId(contentProvider);
        if (isMessageReadRestricted) {
            event.eventType =
                    MESSAGING_READ_RESTRICTION_REPORTED__EVENT_TYPE__RESTRICTED_MESSAGE_INSERTED;
        } else {
            event.eventType =
                    MESSAGING_READ_RESTRICTION_REPORTED__EVENT_TYPE__UNRESTRICTED_MESSAGE_INSERTED;
        }
        atomsStorage.addMessagingReadRestrictionEvent(event);
    }

    /** Logs a messaging read restriction event to statsd. */
    public void onMessageUnrestricted(ContentProvider contentProvider, int callerUid) {
        PersistAtomsStorage atomsStorage = mAtomsStorage.get();
        if (atomsStorage == null) {
            return;
        }
        MessagingReadRestrictionEvent event = new MessagingReadRestrictionEvent();
        event.callerUid = callerUid;
        event.contentProvider = getContentProviderId(contentProvider);
        event.eventType =
                MESSAGING_READ_RESTRICTION_REPORTED__EVENT_TYPE__UPDATED_MESSAGE_TO_UNRESTRICTED;
        atomsStorage.addMessagingReadRestrictionEvent(event);
    }

    /** Logs a messaging read restriction event to statsd. */
    public void onRestrictedMessagesQueried(ContentProvider contentProvider,
        int callerUid, boolean canReadRestrictedMessages) {
        PersistAtomsStorage atomsStorage = mAtomsStorage.get();
        if (atomsStorage == null) {
            return;
        }
        MessagingReadRestrictionEvent event = new MessagingReadRestrictionEvent();
        event.callerUid = callerUid;
        event.contentProvider = getContentProviderId(contentProvider);
        event.eventType
            = MESSAGING_READ_RESTRICTION_REPORTED__EVENT_TYPE__QUERY_RESTRICTED_MESSAGES;
        event.readRestrictedMessagesAppOpMode = canReadRestrictedMessages
                ? MESSAGING_READ_RESTRICTION_REPORTED__READ_RESTRICTED_MESSAGES_APP_OP_MODE__APP_OP_MODE_ALLOWED
                : MESSAGING_READ_RESTRICTION_REPORTED__READ_RESTRICTED_MESSAGES_APP_OP_MODE__APP_OP_MODE_IGNORED;
        atomsStorage.addMessagingReadRestrictionEvent(event);
    }

    private int getContentProviderId(ContentProvider contentProvider) {
        return switch (contentProvider) {
            case SMS ->
               MESSAGING_READ_RESTRICTION_REPORTED__CONTENT_PROVIDER__CONTENT_PROVIDER_SMS_PROVIDER;
            case MMS ->
               MESSAGING_READ_RESTRICTION_REPORTED__CONTENT_PROVIDER__CONTENT_PROVIDER_MMS_PROVIDER;
        };
    }

    @VisibleForTesting
    public static void setInstance(ReadRestrictionStatsLogger instance) {
        sInstance = instance;
    }
}
