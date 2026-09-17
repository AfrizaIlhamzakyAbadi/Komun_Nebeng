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

package android.net.wifi.nl80211;

import static android.net.wifi.flags.Flags.FLAG_WIFI_SCAN_RESULT_INTERCEPTOR;

import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.annotation.SystemApi;
import android.net.wifi.WifiScanner;
import android.os.Bundle;

import java.util.List;

/**
 * Interceptor for Wi-Fi scan results.
 * <p>
 * This class is not implemented by default. It is intended for partners to implement
 * in order to override the default Wi-Fi scan result processing.
 */
@FlaggedApi(FLAG_WIFI_SCAN_RESULT_INTERCEPTOR)
@SystemApi
public class WifiScanResultInterceptor {
    private WifiScanResultInterceptor() {}

    /**
     * Bundle extra key for PNO settings.
     * The value is a {@link PnoSettings} object.
     */
    public static final String EXTRA_PNO_SETTINGS =
            "android.net.wifi.nl80211.extra.PNO_SETTINGS";

    /**
     * Bundle extra key for enabling 6GHz RNR (Reduced Neighbor Report).
     * The value is a boolean.
     */
    public static final String EXTRA_SCANNING_PARAM_ENABLE_6GHZ_RNR =
            "android.net.wifi.nl80211.extra.SCANNING_PARAM_ENABLE_6GHZ_RNR";

    /**
     * Bundle extra key for vendor IEs (Information Elements).
     * The value is a byte array.
     */
    public static final String EXTRA_SCANNING_PARAM_VENDOR_IES =
            "android.net.wifi.nl80211.extra.SCANNING_PARAM_VENDOR_IES";

    /**
     * Bundle extra key for scanned frequencies in MHz.
     * The value is an {@code List<Integer>}.
     */
    public static final String EXTRA_SCANNING_PARAM_FREQUENCIES =
            "android.net.wifi.nl80211.extra.SCANNING_PARAM_FREQUENCIES";

    /**
     * Bundle extra key for scan type.
     * The value is an integer, one of {@link WifiScanner#SCAN_TYPE_LOW_LATENCY},
     * {@link WifiScanner#SCAN_TYPE_LOW_POWER}, or
     * {@link WifiScanner#SCAN_TYPE_HIGH_ACCURACY}.
     */
    public static final String EXTRA_SCANNING_PARAM_SCAN_TYPE =
            "android.net.wifi.nl80211.extra.SCANNING_PARAM_SCAN_TYPE";

    /**
     * Bundle extra key for hidden SSIDs to scan for.
     * The value is a {@code List<byte[]>}.
     */
    public static final String EXTRA_SCANNING_PARAM_HIDDEN_SSIDS =
            "android.net.wifi.nl80211.extra.SCANNING_PARAM_HIDDEN_SSIDS";

    /**
     * Intercept and override Wi-Fi scan results.
     *
     * @param scanResults List of scan results to be intercepted.
     * @param scanParameters Bundle containing scan parameters.
     * Supported keys:
     * <ul>
     * <li>{@link #EXTRA_SCANNING_PARAM_ENABLE_6GHZ_RNR} (boolean)</li>
     * <li>{@link #EXTRA_SCANNING_PARAM_VENDOR_IES} (byte[])</li>
     * <li>{@link #EXTRA_SCANNING_PARAM_FREQUENCIES} (List of Integer)</li>
     * <li>{@link #EXTRA_SCANNING_PARAM_SCAN_TYPE} (int)</li>
     * <li>{@link #EXTRA_SCANNING_PARAM_HIDDEN_SSIDS} (List of byte[])</li>
     * </ul>
     * @return List of overridden scan results.
     * @throws UnsupportedOperationException if not implemented.
     */
    @NonNull
    public static List<NativeScanResult> interceptScanResults(
            @NonNull List<NativeScanResult> scanResults, @NonNull Bundle scanParameters) {
        throw new UnsupportedOperationException();
    }

    /**
     * Intercept and override PNO Wi-Fi scan results.
     *
     * @param scanResults List of PNO scan results to be intercepted.
     * @param scanParameters Bundle containing PNO scan parameters.
     * Supported keys:
     * <ul>
     * <li>{@link #EXTRA_PNO_SETTINGS} ({@link PnoSettings})</li>
     * </ul>
     * @return List of overridden scan results.
     * @throws UnsupportedOperationException if not implemented.
     */
    @NonNull
    public static List<NativeScanResult> interceptPnoScanResults(
            @NonNull List<NativeScanResult> scanResults, @NonNull Bundle scanParameters) {
        throw new UnsupportedOperationException();
    }
}
