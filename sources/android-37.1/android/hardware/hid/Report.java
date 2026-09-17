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

package android.hardware.hid;

import static com.android.hardware.input.Flags.FLAG_HID_API;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Represents a Human Interface Device (HID) report exchanged between a host and a HID device.
 *
 * <p>A HID report contains structured data defined by the device's report descriptor.
 * It includes an optional report ID that distinguishes between multiple report formats
 * supported by the same device, along with a read-only binary data payload.
 *
 * @see HidDevice#getFeatureReport
 */
@FlaggedApi(FLAG_HID_API)
public final class Report {
    private final AidlReport mReport;

    /**
     * Creates a new {@link Report} instance with a specified report ID and binary data payload.
     *
     * <p>To prevent external modification, the provided {@link ByteBuffer} is copied
     * into an internal payload buffer.
     *
     * @param reportId the ID for this report format, or zero if report IDs are not used.
     * @param data the non-null buffer containing the raw binary report payload.
     */
    public Report(int reportId, @NonNull ByteBuffer data) {
        Objects.requireNonNull(data, "data cannot be null");
        mReport = new AidlReport();
        mReport.reportId = reportId;
        byte[] bytes = new byte[data.remaining()];
        data.duplicate().get(bytes);
        mReport.data = bytes;
    }

    /**
     * Constructor for a report that does not use report ID.
     *
     * @param data the report data.
     */
    public Report(@NonNull ByteBuffer data) {
        this(0, data);
    }

    /**
     * Accessor for the report ID.
     *
     * @return the report ID or zero if the device does not use report IDs.
     */
    public int getReportId() {
        return mReport.reportId;
    }

    /**
     * Returns the data provided by the device for the report ID.
     *
     * <p>The data format is specified in the device descriptor, provided by
     * {@link HidDevice#getReportDescriptor}.
     *
     * @return the {@link ByteBuffer} of the raw report data.
     */
    @NonNull
    public ByteBuffer getData() {
        return ByteBuffer.wrap(mReport.data).asReadOnlyBuffer();
    }

    @Hide
    public AidlReport toAidlReport() {
        return mReport;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Report)) {
            return false;
        }
        Report other = (Report) obj;
        return mReport.equals(other.mReport);
    }

    @Override
    public int hashCode() {
        return mReport.hashCode();
    }
}
