/*
 * Copyright (C) 2020 The Android Open Source Project
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

package android.hardware.fingerprint;

import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.hardware.biometrics.ComponentInfoInternal;
import android.hardware.biometrics.SensorProperties;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Container for fingerprint sensor properties.
 */
@Hide
public class FingerprintSensorProperties extends SensorProperties {

    @Hide
    public static final int TYPE_UNKNOWN = 0;

    @Hide
    public static final int TYPE_REAR = 1;

    @Hide
    public static final int TYPE_UDFPS_ULTRASONIC = 2;

    @Hide
    public static final int TYPE_UDFPS_OPTICAL = 3;

    @Hide
    public static final int TYPE_POWER_BUTTON = 4;

    @Hide
    public static final int TYPE_HOME_BUTTON = 5;

    @Hide
    public static final int TYPE_STANDALONE = 6;

    @Hide
    @IntDef({TYPE_UNKNOWN,
            TYPE_REAR,
            TYPE_UDFPS_ULTRASONIC,
            TYPE_UDFPS_OPTICAL,
            TYPE_POWER_BUTTON,
            TYPE_HOME_BUTTON,
            TYPE_STANDALONE})
    @Retention(RetentionPolicy.SOURCE)
    public @interface SensorType {}

    @SensorType final int mSensorType;

    /**
     * Constructs a {@link FingerprintSensorProperties} from the internal parcelable representation.
     */
    @Hide
    public static FingerprintSensorProperties from(
            FingerprintSensorPropertiesInternal internalProp) {
        final List<ComponentInfo> componentInfo = new ArrayList<>();
        for (ComponentInfoInternal internalComp : internalProp.componentInfo) {
            componentInfo.add(ComponentInfo.from(internalComp));
        }
        return new FingerprintSensorProperties(internalProp.sensorId,
                internalProp.sensorStrength,
                componentInfo,
                internalProp.sensorType);
    }

    @Hide
    public FingerprintSensorProperties(int sensorId, int sensorStrength,
            @NonNull List<ComponentInfo> componentInfo, @SensorType int sensorType) {
        super(sensorId, sensorStrength, componentInfo);
        mSensorType = sensorType;
    }

    /**
     * @return The sensor's type.
     */
    @Hide
    @SensorType
    public int getSensorType() {
        return mSensorType;
    }
}
