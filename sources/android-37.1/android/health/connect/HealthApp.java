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

package android.health.connect;

import android.annotation.Hide;
import android.annotation.NonNull;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;

/**
 * A data class summarizing metadata for an app that should be managed by Health Connect.
 *
 * @param packageName the package name of the app
 * @param isSystem whether the app is a system app
 * @param isHealthDataContributor whether the app is contributing health data
 * @param healthPermissionStatus the health permission status of the app
 */
@Hide
public record HealthApp(
        String packageName,
        boolean isSystem,
        boolean isHealthDataContributor,
        HealthPermissionStatus healthPermissionStatus)
        implements Parcelable {

    public HealthApp {
        Objects.requireNonNull(packageName);
        Objects.requireNonNull(healthPermissionStatus);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(packageName);
        dest.writeBoolean(isSystem);
        dest.writeBoolean(isHealthDataContributor);
        dest.writeTypedObject(healthPermissionStatus, flags);
    }

    public static final @NonNull Creator<HealthApp> CREATOR =
            new Creator<>() {
                @Override
                public HealthApp createFromParcel(Parcel in) {
                    return new HealthApp(
                            Objects.requireNonNull(in.readString()),
                            in.readBoolean(),
                            in.readBoolean(),
                            Objects.requireNonNull(
                                    in.readTypedObject(HealthPermissionStatus.CREATOR)));
                }

                @Override
                public HealthApp[] newArray(int size) {
                    return new HealthApp[size];
                }
            };

    /**
     * A data class summarizing health permission statuses for an app.
     */
    @Hide
    public record HealthPermissionStatus(
            @HealthPermissions.PermissionStatus int fitnessStatus,
            @HealthPermissions.PermissionStatus int medicalStatus,
            @HealthPermissions.PermissionStatus int additionalStatus,
            @HealthPermissions.PermissionStatus int legacyStatus)
            implements Parcelable {

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel dest, int flags) {
            dest.writeInt(fitnessStatus);
            dest.writeInt(medicalStatus);
            dest.writeInt(additionalStatus);
            dest.writeInt(legacyStatus);
        }

        public static final @NonNull Creator<HealthPermissionStatus> CREATOR =
                new Creator<>() {
                    @Override
                    public HealthPermissionStatus createFromParcel(Parcel in) {
                        return new HealthPermissionStatus(
                                in.readInt(), in.readInt(), in.readInt(), in.readInt());
                    }

                    @Override
                    public HealthPermissionStatus[] newArray(int size) {
                        return new HealthPermissionStatus[size];
                    }
                };
    }
}
