/*
 * Copyright (C) 2019 The Android Open Source Project
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
package android.net.ipsec.ike;

import android.annotation.Hide;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.net.ipsec.ike.utils.IkeMetrics;
import com.android.internal.net.utils.Log;

/**
 * This class provides global logging methods.
 */
@Hide
public final class IkeManager {
    private static final String IKE_TAG = "IKE";
    private static final boolean LOG_SENSITIVE = false;

    private static Log sIkeLog = new Log(IKE_TAG, LOG_SENSITIVE);
    private static IkeMetrics sIkeMetrics = new IkeMetrics();

    /**
     * Returns IKE logger.
     */
    @Hide
    public static Log getIkeLog() {
        return sIkeLog;
    }

    /**
     * Injects IKE logger for testing.
     */
    @Hide
    @VisibleForTesting
    public static void setIkeLog(Log log) {
        sIkeLog = log;
    }

    /**
     * Resets IKE logger.
     */
    @Hide
    @VisibleForTesting
    public static void resetIkeLog() {
        sIkeLog = new Log(IKE_TAG, LOG_SENSITIVE);
    }

    /**
     * Returns IKE metrics tool.
     */
    @Hide
    public static IkeMetrics getIkeMetrics() {
        return sIkeMetrics;
    }

    /**
     * Injects IKE metrics tool for testing.
     */
    @Hide
    @VisibleForTesting
    public static void setIkeMetrics(IkeMetrics ikeMetrics) {
        sIkeMetrics = ikeMetrics;
    }
}
