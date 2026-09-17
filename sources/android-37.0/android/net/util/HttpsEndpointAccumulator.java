/*
 * Copyright (C) 2025 The Android Open Source Project
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

import static android.net.DnsResolver.ERROR_PARSE;
import static android.net.DnsResolver.ERROR_SYSTEM;
import static android.net.DnsResolver.TYPE_A;
import static android.net.DnsResolver.TYPE_AAAA;
import static android.net.DnsResolver.TYPE_HTTPS;
import static android.system.OsConstants.ETIMEDOUT;

import static com.android.net.module.util.DnsPacket.ANSECTION;
import static com.android.net.module.util.DnsPacket.QDSECTION;
import static com.android.net.module.util.DnsPacket.TYPE_CNAME;
import static com.android.net.module.util.DnsPacket.TYPE_SOA;

import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.net.DnsResolver;
import android.net.DnsResolver.DnsException;
import android.net.LinkProperties;
import android.net.Network;
import android.net.ParseException;
import android.net.dns.HttpsEndpoint;
import android.net.dns.HttpsRecord;
import android.net.util.DnsHttpsEndpointEventMetrics.FailureReason;
import android.net.util.DnsHttpsEndpointEventMetrics.ResponseFlag;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Process;
import android.os.SystemClock;
import android.system.ErrnoException;
import android.util.Log;

import com.android.internal.annotations.GuardedBy;
import com.android.net.module.util.DnsHttpsRecord;
import com.android.net.module.util.DnsPacket;
import com.android.net.module.util.DnsPacket.DnsRecord;

import java.lang.ClassCastException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Accumulates the results of concurrent A/AAAA/HTTPS DNS queries.
 *
 * @hide
 */
public class HttpsEndpointAccumulator implements DnsResolver.Callback<byte[]> {

    // As specified in RFC 9460 section 5.1, clients should wait 50 ms before starting optimistic
    // pre-connection. Depending on metric results, this value may be adjusted in the future.
    private static final int DEFAULT_HTTPS_TIMEOUT_MILLIS = 50;
    private static final String UNEXPECTED_ANSWER_TYPE_ERROR_STR = "Unexpected answer type: ";

    private final Network mNetwork;
    private final LinkProperties mLinkProperties;
    private final DnsResolver.Callback<HttpsEndpoint> mUserCallback;
    private final int mTargetQueryCount;
    private final boolean mHasIpv4;
    private final boolean mHasIpv6;

    private final int mHttpsTimeoutMillis;
    private final Handler mHttpsTimeoutHandler;

    @GuardedBy("mResult")
    private final HttpsEndpointAccumulatorResult mResult;
    private final AtomicBoolean mCallbackInvoked = new AtomicBoolean(false);
    private final Object mTimeoutCancellationToken = new Object();
    private final CancellationSignal mUserCancellationSignal;
    private final CancellationSignal mPendingQueryCancellationSignal;

    private final DnsHttpsEndpointEventMetrics.Builder mMetricsBuilder;

    /**
     * Nested class to group the results of the DNS queries that must be kept in sync.
     */
    private static class HttpsEndpointAccumulatorResult {
        final Set<Integer> mReceivedAnswersToQueryTypes;
        final Set<InetAddress> mAddresses;
        final ArrayList<HttpsRecord> mHttpsRecords;
        DnsException mDnsException;

        private HttpsEndpointAccumulatorResult() {
            mReceivedAnswersToQueryTypes = new HashSet<>();
            mAddresses = new LinkedHashSet<>();
            mHttpsRecords = new ArrayList<>();
        }
    }

    public HttpsEndpointAccumulator(
            @NonNull Network network,
            @Nullable LinkProperties linkProperties,
            @NonNull DnsResolver.Callback<HttpsEndpoint> callback, int queryCount,
            int timeoutMillis, boolean hasIpv4, boolean hasIpv6, @NonNull Handler handler,
            @Nullable CancellationSignal userCancellationSignal,
            @NonNull CancellationSignal pendingQueryCancellationSignal) {
        mNetwork = network;
        mLinkProperties = linkProperties;
        mUserCallback = callback;
        mTargetQueryCount = queryCount;
        mHasIpv4 = hasIpv4;
        mHasIpv6 = hasIpv6;
        mResult = new HttpsEndpointAccumulatorResult();

        mHttpsTimeoutMillis = switch(timeoutMillis) {
            case DnsResolver.HTTPS_QUERY_WAIT_NONE -> 0;
            case DnsResolver.HTTPS_QUERY_WAIT_AUTO -> DEFAULT_HTTPS_TIMEOUT_MILLIS;
            // Indicate that we should wait indefinitely for the HTTPS record.
            case DnsResolver.HTTPS_QUERY_WAIT_UNTIL_TIMEOUT -> -1;
            default -> timeoutMillis;
        };
        mHttpsTimeoutHandler = handler;
        mUserCancellationSignal = userCancellationSignal;
        mPendingQueryCancellationSignal = pendingQueryCancellationSignal;

        mMetricsBuilder =
            new DnsHttpsEndpointEventMetrics.Builder()
                .setUserHttpsTimeoutDurationMillis(mHttpsTimeoutMillis)
                .setAccumulatorInitializationNanos(SystemClock.elapsedRealtimeNanos())
                .setUid(Process.myUid());
    }

    private void reportMetrics(long callbackNanos) {
        synchronized (mResult) {
            mMetricsBuilder.setCallbackNanos(callbackNanos)
                    .setHttpsRecords(mResult.mHttpsRecords)
                    .build()
                    .reportMetrics();
        }
    }

    /**
     * Called when the user cancels the query.
     */
    public void onUserCancellation() {
        if (!mCallbackInvoked.compareAndSet(false, true)) {
            return;
        }

        synchronized (mResult) {
            mMetricsBuilder.setFailureReason(FailureReason.USER_CANCELLATION);
        }
        reportMetrics(SystemClock.elapsedRealtimeNanos());
    }

    @Override
    public void onAnswer(@NonNull byte[] answer, int rcode) {
        long packetReceivedNanos = SystemClock.elapsedRealtimeNanos();
        Objects.requireNonNull(answer, "Raw byte response for query cannot be null");

        // Callback has already been invoked, skip parsing this response.
        if (mCallbackInvoked.get()) return;
        if (mUserCancellationSignal != null && mUserCancellationSignal.isCanceled()) {
            onUserCancellation();
            return;
        }

        DnsPacket dnsPacket = new DnsPacket(answer);
        final List<DnsRecord> questionRecords = dnsPacket.getRecords(QDSECTION);
        final int questionCount = questionRecords.size();
        if (questionCount != 1) {
            synchronized (mResult) {
                mResult.mDnsException = new DnsException(ERROR_PARSE,
                        new ParseException("Unexpected question count: " + questionCount));
            }
            return;
        }

        final int queryType = questionRecords.get(0).nsType;

        if (queryType != TYPE_A && queryType != TYPE_AAAA && queryType != TYPE_HTTPS) {
            return;
        }

        final List<DnsRecord> answerRecords = dnsPacket.getRecords(ANSECTION);
        Set<InetAddress> addresses = new LinkedHashSet<>();
        ArrayList<HttpsRecord> httpsRecords = new ArrayList<>();
        DnsException exception = null;

        boolean addressRecordReceived = false;
        boolean httpsRecordReceived = false;
        boolean hasMandatory = false;

        // In the NODATA scenario, answerRecords will be empty and this whole loop will be skipped.
        for (DnsRecord record : answerRecords) {
            int answerType = record.nsType;

            try {
                switch (answerType) {
                    case TYPE_A, TYPE_AAAA -> {
                        addresses.add(InetAddress.getByAddress(record.getRR()));
                        addressRecordReceived = true;
                    }
                    case TYPE_HTTPS -> {
                        httpsRecordReceived = true;
                        DnsHttpsRecord dnsHttpsRecord = (DnsHttpsRecord) record;
                        try {
                            if (dnsHttpsRecord.getMandatory().length > 0) {
                                hasMandatory = true;
                                dnsHttpsRecord.verifyMandatoryKeys();
                            }
                        } catch (DnsPacket.ParseException e) {
                            // Ignore the HTTPS record as it is missing mandatory keys.
                            mMetricsBuilder.addResponseFlag(ResponseFlag.MISSING_MANDATORY_KEYS);
                            continue;
                        }

                        HttpsRecord httpsRecord =
                                new HttpsRecord(mNetwork, mLinkProperties, dnsHttpsRecord);
                        httpsRecords.add(httpsRecord);

                        // Add only the relevant IP hints to the list of IP addresses.
                        if (mHasIpv4) addresses.addAll(httpsRecord.getIpv4Hints());
                        if (mHasIpv6) addresses.addAll(httpsRecord.getIpv6Hints());
                    }
                    case TYPE_CNAME, TYPE_SOA -> {
                        // Skip CNAME and SOA records, but don't mark this as an error.
                        // Most servers will return the full CNAME chain in the answer to the final
                        // A/AAAA record. If not, we'll assume NODATA for this queryType.
                        continue;
                    }
                    default -> {
                        exception = new DnsException(ERROR_PARSE,
                                new ParseException(UNEXPECTED_ANSWER_TYPE_ERROR_STR + answerType));
                    }
                }
            } catch (DnsPacket.ParseException e) {
                exception = new DnsException(ERROR_PARSE, new ParseException(e.reason, e));
            } catch (ClassCastException e) {
                exception = new DnsException(ERROR_PARSE, e);
            } catch (UnknownHostException e) {
                exception = new DnsException(ERROR_SYSTEM, e);
            }
        }

        HttpsEndpoint endpoint = null;
        DnsException exceptionToReport = null;
        boolean shouldStartHttpsTimeout = false;

        synchronized (mResult) {
            if (mResult.mReceivedAnswersToQueryTypes.isEmpty()) {
                mMetricsBuilder.setFirstRecordReceivedNanos(packetReceivedNanos);
            }
            if (addressRecordReceived) {
                // Deliberately overwrite the address record received nanos to the latest one.
                mMetricsBuilder.setAddressRecordReceivedNanos(packetReceivedNanos);
            }
            if (httpsRecordReceived) {
                mMetricsBuilder.setHttpsRecordReceivedNanos(packetReceivedNanos);
            }
            if (hasMandatory) {
                mMetricsBuilder.setHasMandatory(true);
            }

            mResult.mAddresses.addAll(addresses);
            mResult.mHttpsRecords.addAll(httpsRecords);
            // Add the query type even in the case of NODATA, mismatched query/response type (even
            // in the case of CNAME responses with no actual address records), since this indicates
            // we received an answer for the type of record we were querying.
            mResult.mReceivedAnswersToQueryTypes.add(queryType);
            if (exception != null) {
                mResult.mDnsException = exception;
            }

            if (mCallbackInvoked.get()) return; // Skip if the callback has already been invoked.

            exceptionToReport = mResult.mDnsException;
            if (hasEnoughAddressInfo()) {
                if (mResult.mHttpsRecords.isEmpty()) {
                    // If we have received all but the HTTPS records, decide whether to return
                    // immediately or wait the given amount of time before returning incomplete
                    // results.
                    if (mHttpsTimeoutMillis == DnsResolver.HTTPS_QUERY_WAIT_NONE
                            || mResult.mReceivedAnswersToQueryTypes.contains(TYPE_HTTPS)) {
                        endpoint = createHttpsEndpoint(rcode);
                    } else if (mHttpsTimeoutMillis > 0) {
                        shouldStartHttpsTimeout = true;
                    }
                } else {
                    // Disregard any exceptions and return if we got valid HTTPS and IP data.
                    exceptionToReport = null;
                    endpoint = createHttpsEndpoint(rcode);
                }
            } else if (mTargetQueryCount == mResult.mReceivedAnswersToQueryTypes.size()) {
                mMetricsBuilder.addResponseFlag(ResponseFlag.NOT_ENOUGH_ADDRESS_INFO);
                // Return if we have gotten all the records as we expected.
                endpoint = createHttpsEndpoint(rcode);
            }
        }

        if (endpoint != null || exceptionToReport != null) {
            reportResult(endpoint, exceptionToReport, rcode);
        } else if (shouldStartHttpsTimeout) {
            mHttpsTimeoutHandler.postDelayed(() -> {
                HttpsEndpoint timeoutEndpoint = null;
                DnsException timeoutException = null;

                synchronized (mResult) {
                    timeoutEndpoint = createHttpsEndpoint(rcode);
                    timeoutException = mResult.mDnsException;
                }
                reportResult(timeoutEndpoint, timeoutException, rcode);
            }, mTimeoutCancellationToken, mHttpsTimeoutMillis);
        }
        // TODO(b/494647387): handle if the HTTPS record name does not match the A/AAAA ones
    }

    @Override
    public void onError(@NonNull DnsException error) {
        // Callback has already been invoked, skip.
        if (mCallbackInvoked.get()) return;
        if (mUserCancellationSignal != null && mUserCancellationSignal.isCanceled()) {
            onUserCancellation();
            return;
        }

        DnsException exception = null;
        synchronized (mResult) {
            mResult.mDnsException = error;
            exception = mResult.mDnsException;
        }

        reportResult(/* endpoint= */ null, exception, /* rcode= */ 0);
    }

    /**
     * Constructs the data to be returned via the user callback, whether success or failure.
     */
    @GuardedBy("mResult")
    @NonNull
    private HttpsEndpoint createHttpsEndpoint(int rcode) {
        Collections.sort(mResult.mHttpsRecords, Comparator.comparing(HttpsRecord::getPriority));
        return new HttpsEndpoint(
                mNetwork,
                mResult.mHttpsRecords,
                new ArrayList<>(mResult.mAddresses));
    }

    private FailureReason getFailureReason(@Nullable DnsException exception) {
        if (exception == null) {
            synchronized (mResult) {
                // If the host is not recognized, empty records may be returned instead of an
                // UnknownHostException being thrown. Note that the onAnswer user callback will
                // still be invoked, but with an empty endpoint.
                if (mResult.mHttpsRecords.isEmpty() && mResult.mAddresses.isEmpty()) {
                    return FailureReason.UNKNOWN_HOST;
                }
            }

            return FailureReason.UNSPECIFIED;
        }

        switch(exception.code) {
            case ERROR_PARSE -> {
                if (exception.getMessage() != null &&
                        exception.getMessage().startsWith(UNEXPECTED_ANSWER_TYPE_ERROR_STR)) {
                    return FailureReason.UNEXPECTED_ANSWER_TYPE;
                } else {
                    return FailureReason.PARSE_EXCEPTION;
                }
            }
            case ERROR_SYSTEM -> {
                if (exception.getCause() instanceof UnknownHostException) {
                    return FailureReason.UNKNOWN_HOST;
                } else if (exception.getCause() instanceof ErrnoException
                    && ((ErrnoException) exception.getCause()).errno == ETIMEDOUT) {
                    return FailureReason.RECORD_TIMEOUT;
                } else {
                    return FailureReason.FATAL_ERROR;
                }
            }
            default -> { return FailureReason.UNSPECIFIED; }
        }
    }

    /**
     * Reports the DNS query results to the user callback, both success and failure.
     *
     * <p>If the user callback has already been invoked (regardless of success or failure), this
     * method does nothing.
     */
    private void reportResult(
            @Nullable HttpsEndpoint endpoint, @Nullable DnsException exception, int rcode) {
        if (!mCallbackInvoked.compareAndSet(false, true)) {
            // Callback has already been invoked, do nothing.
            return;
        }

        if (endpoint == null && exception == null) {
            // No results (success or failure) to report. This should never happen.
            return;
        }

        // Get the latency measurement just before user callbacks are triggered.
        long callbackNanos = SystemClock.elapsedRealtimeNanos();
        mMetricsBuilder.setFailureReason(getFailureReason(exception));

        if (exception != null) {
            mUserCallback.onError(exception);
        } else {
            mUserCallback.onAnswer(endpoint, rcode);
        }

        // Reporting the metrics might be latency-sensitive, so call it after the user callbacks.
        reportMetrics(callbackNanos);
        // Cancel any pending queries or timeout callbacks.
        mPendingQueryCancellationSignal.cancel();
        mHttpsTimeoutHandler.removeCallbacksAndMessages(mTimeoutCancellationToken);
    }

    /**
     * Returns true if we've received enough address information to return the results we have.
     *
     * <p>This is the case if we have received at least one HTTPS record with the IP hints relevant
     * to network, or if we have received the A or AAAA records relevant to the network.
     */
    @GuardedBy("mResult")
    private boolean hasEnoughAddressInfo() {
        for (HttpsRecord record: mResult.mHttpsRecords) {
            boolean missingIpv4Hint = mHasIpv4 && record.getIpv4Hints().isEmpty();
            boolean missingIpv6Hint = mHasIpv6 && record.getIpv6Hints().isEmpty();
            if (!missingIpv4Hint && !missingIpv6Hint) {
                // If we have at least one HTTPS record with the relevant IP hints, don't bother
                // checking the other HTTPS records.
                return true;
            }
        }

        boolean hasIpv6IfNeeded = !mHasIpv6 ||
                mResult.mReceivedAnswersToQueryTypes.contains(TYPE_AAAA);
        boolean hasIpv4IfNeeded = !mHasIpv4 ||
                mResult.mReceivedAnswersToQueryTypes.contains(TYPE_A);

        return hasIpv6IfNeeded && hasIpv4IfNeeded;
    }
}
