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

package android.net.http;

import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.net.http.flags.Flags;

import androidx.annotation.IntRange;
import androidx.annotation.Nullable;

import org.chromium.net.RequestFinishedInfo;
import org.chromium.net.impl.CronetMetrics;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Metrics collected for a single request. Most of these metrics are timestamps for events during
 * the lifetime of the {@link BidirectionalStream} or {@link UrlRequest}, which can be used to build
 * a detailed timeline for investigating performance.
 *
 * <p>Timestamps are derived from a monotonic clock.
 *
 * <p>Start times are reported as the time when a request started waiting for an event, not when the
 * event actually started. If a metric is not available, including cases when a request finished
 * before reaching that stage, start and end times will be {@code null}. If no time was spent
 * blocking on an event, start and end will be the same time.
 *
 * <p>A metric can be null if it's unavailable (e.g. SSL metrics are null if the host name was not
 * resolved).
 */
@FlaggedApi(Flags.FLAG_REQUEST_FINISHED_TIMINGS)
public final class FinishedRequestTimings {
    @NonNull private final CronetMetrics mBackend;

    FinishedRequestTimings(@NonNull RequestFinishedInfo.Metrics backend) {
        mBackend = (CronetMetrics) backend;
    }

    private Instant getTimestampAsInstant(long timestamp) {
        if (timestamp == -1) return null;
        return Instant.EPOCH.plus(timestamp, ChronoUnit.MICROS);
    }

    /**
     * Returns the timestamp when the request started.
     *
     * <p>This value is {@code null} if the request fails initial validation checks during {@link
     * UrlRequest#start()} or {@link org.chromium.net.BidirectionalStream#start()} (for example, if
     * an invalid header is provided).
     *
     * <p>See {@link RequestFinishedTimings} for more information.
     *
     * @return the {@link java.time.Instant} when the request began, or {@code null} if the request
     *     failed before starting.
     */
    @Nullable
    public Instant getRequestStart() {
        return getTimestampAsInstant(mBackend.getRequestStartMicroseconds());
    }

    /**
     * Returns the timestamp when DNS lookup started. If a connection is already active, this
     * returns null.
     *
     * <p>See the documentation on {@link RequestFinishedTimings} for more information.
     *
     * @return {@link java.time.Instant} representing when the DNS lookup started, or null.
     */
    @Nullable
    public Instant getDnsStart() {
        return getTimestampAsInstant(mBackend.getDnsStartMicroseconds());
    }

    /**
     * Returns the timestamp when the DNS lookup finished.
     *
     * <p>This value is {@code null} if no DNS lookup was required, such as when the request reuses
     * an existing connection. If a lookup was attempted, this value will be non-null, regardless of
     * whether the lookup succeeded (via network or cache) or failed.
     *
     * <p>See {@link RequestFinishedTimings} for more detailed timing information.
     *
     * @return the {@link java.time.Instant} when the DNS lookup completed, or {@code null} if no
     *     lookup was performed.
     */
    @Nullable
    public Instant getDnsEnd() {
        return getTimestampAsInstant(mBackend.getDnsEndMicroseconds());
    }

    /**
     * Returns the timestamp for when the connection attempt began. If a connection is already
     * active, this returns null.
     *
     * <p>See the documentation on {@link RequestFinishedTimings} for more information.
     *
     * @return {@link java.time.Instant} representing when the connection establishment started, or
     *     null.
     */
    @Nullable
    public Instant getConnectingStart() {
        return getTimestampAsInstant(mBackend.getConnectStartMicroseconds());
    }

    /**
     * Returns the timestamp for when the connection establishment ended. If the connection used by
     * the request is already active, this returns null.
     *
     * <p>See the documentation on {@link RequestFinishedTimings} for more information.
     *
     * @return {@link java.time.Instant} representing when the connection establishment finished. If
     *     using HTTPS, TLS handshake is completed. In some cases, such as 0-RTT, the stack may
     *     start sending data before connection establishment is considered complete; in such cases,
     *     this timestamp may occur after {@link #getSendingStart()}.
     */
    @Nullable
    public Instant getConnectingEnd() {
        return getTimestampAsInstant(mBackend.getConnectEndMicroseconds());
    }

    /**
     * Returns the timestamp when TLS handshake started.
     *
     * <p>See the documentation on {@link RequestFinishedTimings} for more information.
     *
     * @return {@link java.time.Instant} representing when the TLS handshake has started. This will
     *     be later than getConnectStart. In cases where the TLS handshake is started immediately
     *     (e.g. QUIC), this may be equal to getConnectStart.
     */
    @Nullable
    public Instant getTlsHandshakeStart() {
        return getTimestampAsInstant(mBackend.getSslStartMicroseconds());
    }

    /**
     * Returns the timestamp when TLS handshake finished. This will always be equal to {@link
     * #getConnectEnd}.
     *
     * <p>See the documentation on {@link RequestFinishedTimings} for more information.
     *
     * @return {@link java.time.Instant} representing when the TLS establishment finished.
     */
    @Nullable
    public Instant getTlsHandshakeEnd() {
        return getTimestampAsInstant(mBackend.getSslEndMicroseconds());
    }

    /**
     * Returns the timestamp when sending the request started.
     *
     * <p>See the documentation on {@link RequestFinishedTimings} for more information.
     *
     * @return {@link java.time.Instant} representing the beginning of sending HTTP request headers.
     */
    @Nullable
    public Instant getSendingStart() {
        return getTimestampAsInstant(mBackend.getSendingStartMicroseconds());
    }

    /**
     * Returns the timestamp when sending the request finished.
     *
     * <p>See the documentation on {@link RequestFinishedTimings} for more information.
     *
     * @return {@link java.time.Instant} representing the end of sending HTTP request (including the
     *     body).
     */
    @Nullable
    public Instant getSendingEnd() {
        return getTimestampAsInstant(mBackend.getSendingEndMicroseconds());
    }

    /**
     * Returns the timestamp when the request finished.
     *
     * <p>This value is {@code null} if the request failed to start, as documented in {@link
     * #getRequestStart()}. If the request started successfully, this reflects the time it reached a
     * terminal state, including success, failure, or cancellation.
     *
     * <p>See {@link RequestFinishedTimings} for more information.
     *
     * @return the {@link java.time.Instant} when the request finished, or {@code null} if the
     *     request never started.
     */
    @Nullable
    public Instant getRequestEnd() {
        return getTimestampAsInstant(mBackend.getRequestFinishedMicroseconds());
    }

    /**
     * Returns whether the socket was reused from a previous request. Cases where this can happen
     * include: reusing an idle socket that was used to complete a previous HTTP/1 request;
     * multiplexing streams on a single HTTP/2 or QUIC connection.
     *
     * <p>Note that when a socket is reused, then DNS, connect and SSL timings are meaningless and
     * undefined, as the request did not go through any of these stages.
     *
     * @return true if a socket has been reused.
     */
    public boolean wasSocketReused() {
        return mBackend.getSocketReused();
    }

    /** Returns total bytes sent. */
    @IntRange(from = 0)
    public long getSentByteCount() {
        return Objects.requireNonNullElse(mBackend.getSentByteCount(), 0L);
    }

    /** Returns total bytes received. */
    @IntRange(from = 0)
    public long getReceivedByteCount() {
        return Objects.requireNonNullElse(mBackend.getReceivedByteCount(), 0L);
    }
}
