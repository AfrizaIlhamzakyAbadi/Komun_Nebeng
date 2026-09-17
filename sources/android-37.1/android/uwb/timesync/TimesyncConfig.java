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

package android.uwb.timesync;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.SystemApi;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * UWB Timesync configuration.
 */
@SystemApi
@FlaggedApi(com.android.ranging.flags.Flags.FLAG_RANGING_STACK_UPDATES_26_Q_4)
public final class TimesyncConfig implements Parcelable {
    private final boolean mIsTimesyncSupported;
    private final boolean mIsTimesyncAccuracyVerified;

    @Hide
    public TimesyncConfig(boolean isTimesyncSupported, boolean isTimesyncAccuracyVerified) {
        mIsTimesyncSupported = isTimesyncSupported;
        mIsTimesyncAccuracyVerified = isTimesyncAccuracyVerified;
    }

    /**
     * Returns whether timesync is supported.
     *
     * <p>Under the Aliro and CCC specifications, the user device (in this case, the Android
     * device) must exchange its UWBS timestamp with the peer device—either a vehicle (CCC)
     * or a lock (Aliro)—over Bluetooth before starting a UWB ranging session. This ensures
     * that the peer device can minimize the duration the initial RX window is kept open,
     * which improves overall system power efficiency.</p>
     *
     * <p>
     * Both procedure 0 and 1 shall be supported.
     * </p>
     *
     * @see <a href="https://causeway.carconnectivity.org/wg/Members/document/14598">CCC
     * specification</a>
     *
     * @return {@code true} if the system supports the UWB time synchronization procedures 0 and 1,
     *         {@code false} otherwise.
     *
     * Device specific config of aosp_timesync_supported in UWB overlay.
     */
    public boolean isTimesyncSupported() {
        return mIsTimesyncSupported;
    }

    /**
     * Returns whether timesync accuracy is verified, configured by timesync_accuracy_verified
     * in UWB overlay.
     *
     * In order to ensure UWB ranging performance and best possible user experience for use cases
     * such as Passive Entry as defined in the CCC specification, devices must implement accurate
     * timesync. Based on the UWB_Device_Time and UWB_Device_Time_Uncertainty sent within the latest
     * Time_Sync message to the vehicle, the UWB_Time0 sent with the Ranging Session Setup Response
     * (RSS-RS) or RR-RS or CRR-RS should correspond to the actual moment in time the first UWB
     * pre-poll packet is received by the vehicle ± UWB_Device_Time_Uncertainty everytime.
     * It is highly recommended to keep the uncertainty as low as possible, preferably under 10ms.
     */
    public boolean isTimesyncAccuracyVerified() {
        return mIsTimesyncAccuracyVerified;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeBoolean(mIsTimesyncSupported);
        dest.writeBoolean(mIsTimesyncAccuracyVerified);
    }

    @NonNull
    public static final Creator<TimesyncConfig> CREATOR = new Creator<TimesyncConfig>() {
        @Override
        public TimesyncConfig createFromParcel(Parcel in) {
            return new TimesyncConfig(in.readBoolean(), in.readBoolean());
        }

        @Override
        public TimesyncConfig[] newArray(int size) {
            return new TimesyncConfig[size];
        }
    };

    @Override
    public String toString() {
        return "TimesyncConfig{" +
                "mIsTimesyncSupported=" + mIsTimesyncSupported +
                ", mIsTimesyncAccuracyVerified=" + mIsTimesyncAccuracyVerified +
                '}';
    }
}
