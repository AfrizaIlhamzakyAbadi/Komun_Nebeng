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

package android.net.util;

import static com.android.net.module.util.FrameworkConnectivityStatsLog.DNS_HTTPS_ENDPOINT_EVENT_REPORTED;
import static com.android.net.module.util.FrameworkConnectivityStatsLog.DNS_HTTPS_ENDPOINT_EVENT_REPORTED__FAILURE_REASON__HTTPS_ENDPOINT_FAILURE_REASON_FATAL_ERROR;
import static com.android.net.module.util.FrameworkConnectivityStatsLog.DNS_HTTPS_ENDPOINT_EVENT_REPORTED__FAILURE_REASON__HTTPS_ENDPOINT_FAILURE_REASON_MISMATCHED_DOMAIN;
import static com.android.net.module.util.FrameworkConnectivityStatsLog.DNS_HTTPS_ENDPOINT_EVENT_REPORTED__FAILURE_REASON__HTTPS_ENDPOINT_FAILURE_REASON_PARSE_EXCEPTION;
import static com.android.net.module.util.FrameworkConnectivityStatsLog.DNS_HTTPS_ENDPOINT_EVENT_REPORTED__FAILURE_REASON__HTTPS_ENDPOINT_FAILURE_REASON_RECORD_TIMEOUT;
import static com.android.net.module.util.FrameworkConnectivityStatsLog.DNS_HTTPS_ENDPOINT_EVENT_REPORTED__FAILURE_REASON__HTTPS_ENDPOINT_FAILURE_REASON_UNSPECIFIED;
import static com.android.net.module.util.FrameworkConnectivityStatsLog.DNS_HTTPS_ENDPOINT_EVENT_REPORTED__FAILURE_REASON__HTTPS_ENDPOINT_FAILURE_REASON_UNKNOWN_HOST;
import static com.android.net.module.util.FrameworkConnectivityStatsLog.DNS_HTTPS_ENDPOINT_EVENT_REPORTED__FAILURE_REASON__HTTPS_ENDPOINT_FAILURE_REASON_UNEXPECTED_ANSWER_TYPE;
import static com.android.net.module.util.FrameworkConnectivityStatsLog.DNS_HTTPS_ENDPOINT_EVENT_REPORTED__FAILURE_REASON__HTTPS_ENDPOINT_FAILURE_REASON_USER_CANCELLATION;
import static com.android.net.module.util.FrameworkConnectivityStatsLog.DNS_HTTPS_ENDPOINT_EVENT_REPORTED__RESULT__HTTPS_ENDPOINT_RESULT_FAILURE;
import static com.android.net.module.util.FrameworkConnectivityStatsLog.DNS_HTTPS_ENDPOINT_EVENT_REPORTED__RESULT__HTTPS_ENDPOINT_RESULT_ONLY_ADDRESS_RECORDS;
import static com.android.net.module.util.FrameworkConnectivityStatsLog.DNS_HTTPS_ENDPOINT_EVENT_REPORTED__RESULT__HTTPS_ENDPOINT_RESULT_SUCCESS;
import static com.android.net.module.util.FrameworkConnectivityStatsLog.DNS_HTTPS_ENDPOINT_EVENT_REPORTED__RESULT__HTTPS_ENDPOINT_RESULT_UNKNOWN;

import android.net.dns.HttpsRecord;
import android.net.ssl.InvalidEchDataException;

import com.android.net.module.util.FrameworkConnectivityStatsLog;

import java.util.concurrent.TimeUnit;
import java.util.ArrayList;

/**
 * Collects and reports metrics regarding DNS HTTPS endpoint events.
 *
 * @hide
 */
public class DnsHttpsEndpointEventMetrics {

    public enum FailureReason {
        UNSPECIFIED(
            DNS_HTTPS_ENDPOINT_EVENT_REPORTED__FAILURE_REASON__HTTPS_ENDPOINT_FAILURE_REASON_UNSPECIFIED),
        PARSE_EXCEPTION(
            DNS_HTTPS_ENDPOINT_EVENT_REPORTED__FAILURE_REASON__HTTPS_ENDPOINT_FAILURE_REASON_PARSE_EXCEPTION),
        UNKNOWN_HOST(
            DNS_HTTPS_ENDPOINT_EVENT_REPORTED__FAILURE_REASON__HTTPS_ENDPOINT_FAILURE_REASON_UNKNOWN_HOST),
        UNEXPECTED_ANSWER_TYPE(
            DNS_HTTPS_ENDPOINT_EVENT_REPORTED__FAILURE_REASON__HTTPS_ENDPOINT_FAILURE_REASON_UNEXPECTED_ANSWER_TYPE),
        USER_CANCELLATION(
            DNS_HTTPS_ENDPOINT_EVENT_REPORTED__FAILURE_REASON__HTTPS_ENDPOINT_FAILURE_REASON_USER_CANCELLATION),
        RECORD_TIMEOUT(
            DNS_HTTPS_ENDPOINT_EVENT_REPORTED__FAILURE_REASON__HTTPS_ENDPOINT_FAILURE_REASON_RECORD_TIMEOUT),
        MISMATCHED_DOMAIN(
            DNS_HTTPS_ENDPOINT_EVENT_REPORTED__FAILURE_REASON__HTTPS_ENDPOINT_FAILURE_REASON_MISMATCHED_DOMAIN),
        FATAL_ERROR(
            DNS_HTTPS_ENDPOINT_EVENT_REPORTED__FAILURE_REASON__HTTPS_ENDPOINT_FAILURE_REASON_FATAL_ERROR);

        private final int metricsValue;

        public int getMetricsValue() {
            return metricsValue;
        }

        private FailureReason(int metricsValue) {
            this.metricsValue = metricsValue;
        }
    }

    // Corresponds to HttpsEndpointResponseFlag in connectivity/enums.proto
    public enum ResponseFlag {
        // This doesn't use an enum constant as the HttpsEndpointResponseFlag enum gets stripped
        // from the auto-generation of FrameworkConnectivityStatsLog, since we cannot directly
        // reference it from the atom definition for the bitmask field
        UNSPECIFIED(0),
        NOT_ENOUGH_ADDRESS_INFO(1 << 0),
        MISSING_MANDATORY_KEYS(1 << 1);

        private final int metricsValue;

        public int getMetricsValue() {
            return metricsValue;
        }

        private ResponseFlag(int metricsValue) {
            this.metricsValue = metricsValue;
        }
    }

    private final int result;
    private final FailureReason failureReason;

    private final boolean hasMandatory;
    private final boolean hasAlpn;
    private final boolean hasIpHints;
    private final boolean hasEch;

    private final boolean additionalLookupNeeded;
    private final int userHttpsTimeoutDurationMillis;
    private final int addressRecordLatencyMicros;
    private final int httpsRecordLatencyMicros;
    private final int firstRecordUntilCallbackMicros;
    private final int uid;
    private final int responseFlags;

    private DnsHttpsEndpointEventMetrics(int result, FailureReason failureReason,
            boolean hasMandatory, boolean hasAlpn, boolean hasIpHints, boolean hasEch,
            boolean additionalLookupNeeded, int userHttpsTimeoutDurationMillis,
            int addressRecordLatencyMicros, int httpsRecordLatencyMicros,
            int firstRecordUntilCallbackMicros, int uid, int responseFlags) {
        this.result = result;
        this.failureReason = failureReason;
        this.hasMandatory = hasMandatory;
        this.hasAlpn = hasAlpn;
        this.hasIpHints = hasIpHints;
        this.hasEch = hasEch;
        this.additionalLookupNeeded = additionalLookupNeeded;
        this.userHttpsTimeoutDurationMillis = userHttpsTimeoutDurationMillis;
        this.addressRecordLatencyMicros = addressRecordLatencyMicros;
        this.httpsRecordLatencyMicros = httpsRecordLatencyMicros;
        this.firstRecordUntilCallbackMicros = firstRecordUntilCallbackMicros;
        this.uid = uid;
        this.responseFlags = responseFlags;
    }

    public static class Builder {
        private ArrayList<HttpsRecord> httpsRecords = new ArrayList<>();
        private FailureReason failureReason = FailureReason.UNSPECIFIED;
        private int responseFlags = 0;
        private boolean hasMandatory = false;

        private boolean additionalLookupNeeded = false;
        private int userHttpsTimeoutDurationMillis;
        private long accumulatorInitializationNanos;
        private long addressRecordReceivedNanos;
        private long httpsRecordReceivedNanos;
        private long firstRecordReceivedNanos;
        private long callbackNanos;
        private int uid = 0;

        public Builder setFailureReason(FailureReason failureReason) {
            this.failureReason = failureReason;
            return this;
        }

        public Builder addResponseFlag(ResponseFlag responseFlag) {
            this.responseFlags |= responseFlag.getMetricsValue();
            return this;
        }

        public Builder setHttpsRecords(ArrayList<HttpsRecord> httpsRecords) {
            // Defensive copy, since the HttpsRecords could be modified elsewhere
            this.httpsRecords = new ArrayList<>(httpsRecords);
            return this;
        }

        public Builder setHasMandatory(boolean hasMandatory) {
            this.hasMandatory = hasMandatory;
            return this;
        }

        public Builder setAdditionalLookupNeeded(boolean additionalLookupNeeded) {
            // TODO(b/494647387): call this method when domain mismatch is detected.
            this.additionalLookupNeeded = additionalLookupNeeded;
            return this;
        }

        public Builder setUserHttpsTimeoutDurationMillis(int userHttpsTimeoutDurationMillis) {
            this.userHttpsTimeoutDurationMillis = userHttpsTimeoutDurationMillis;
            return this;
        }

        public Builder setAccumulatorInitializationNanos(long accumulatorInitializationNanos) {
            this.accumulatorInitializationNanos = accumulatorInitializationNanos;
            return this;
        }

        public Builder setAddressRecordReceivedNanos(long addressRecordReceivedNanos) {
            this.addressRecordReceivedNanos = addressRecordReceivedNanos;
            return this;
        }

        public Builder setHttpsRecordReceivedNanos(long httpsRecordReceivedNanos) {
            this.httpsRecordReceivedNanos = httpsRecordReceivedNanos;
            return this;
        }

        public Builder setFirstRecordReceivedNanos(long firstRecordReceivedNanos) {
            this.firstRecordReceivedNanos = firstRecordReceivedNanos;
            return this;
        }

        public Builder setCallbackNanos(long callbackNanos) {
            this.callbackNanos = callbackNanos;
            return this;
        }

        public Builder setUid(int uid) {
            this.uid = uid;
            return this;
        }

        private int calculateResult() {
            if (failureReason != FailureReason.UNSPECIFIED) {
                return DNS_HTTPS_ENDPOINT_EVENT_REPORTED__RESULT__HTTPS_ENDPOINT_RESULT_FAILURE;
            }

            if (httpsRecords.isEmpty()) {
                return DNS_HTTPS_ENDPOINT_EVENT_REPORTED__RESULT__HTTPS_ENDPOINT_RESULT_ONLY_ADDRESS_RECORDS;
            }

            return DNS_HTTPS_ENDPOINT_EVENT_REPORTED__RESULT__HTTPS_ENDPOINT_RESULT_SUCCESS;
        }

        private static int calculateLatencyMicros(long startTimeNanos, long endTimeNanos) {
            if (startTimeNanos > 0 && endTimeNanos > startTimeNanos) {
                return (int) TimeUnit.NANOSECONDS.toMicros(endTimeNanos - startTimeNanos);
            }
            return 0;
        }

        public DnsHttpsEndpointEventMetrics build() {
            boolean hasEch = false;
            boolean hasAlpn = false;
            boolean hasIpHints = false;

            // Avoid using a stream because it's not memory-efficient
            // Since we're updating 3 boolean fields, do one manual loop as opposed to 3 calls to
            // CollectionUtils.any()
            for (HttpsRecord httpsRecord : httpsRecords) {
                if (!hasAlpn && !httpsRecord.getAlpnIds().isEmpty()) {
                    hasAlpn = true;
                }

                if (!hasIpHints && !httpsRecord.getIpAddressHints().isEmpty()) {
                    hasIpHints = true;
                }

                if (!hasEch) {
                    try {
                        if (httpsRecord.getEchConfigList() != null) {
                            hasEch = true;
                        }
                    } catch (NoClassDefFoundError | InvalidEchDataException e) {
                        // NoClassDefFoundError can be thrown if the Tethering module is installed,
                        // but not Conscrypt where EchConfigList is defined.
                        // No valid ECH config list, so keep hasEch as false.
                    }
                }
            }

            return new DnsHttpsEndpointEventMetrics(
                    calculateResult(),
                    this.failureReason,
                    hasMandatory,
                    hasAlpn,
                    hasIpHints,
                    hasEch,
                    additionalLookupNeeded,
                    userHttpsTimeoutDurationMillis,
                    calculateLatencyMicros(
                          accumulatorInitializationNanos, addressRecordReceivedNanos),
                    calculateLatencyMicros(
                          accumulatorInitializationNanos, httpsRecordReceivedNanos),
                    calculateLatencyMicros(firstRecordReceivedNanos, callbackNanos),
                    uid,
                    responseFlags);
        }
    }

    /**
     * Logs a DNS HTTPS endpoint event to statsd.
     */
    public void reportMetrics() {
        FrameworkConnectivityStatsLog.write(
                DNS_HTTPS_ENDPOINT_EVENT_REPORTED,
                result,
                failureReason.getMetricsValue(),
                hasMandatory,
                hasAlpn,
                hasIpHints,
                hasEch,
                additionalLookupNeeded,
                userHttpsTimeoutDurationMillis,
                addressRecordLatencyMicros,
                httpsRecordLatencyMicros,
                firstRecordUntilCallbackMicros,
                uid,
                responseFlags);
    }
}
