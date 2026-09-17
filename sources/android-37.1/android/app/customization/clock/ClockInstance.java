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

package android.app.customization.clock;

import static android.app.Flags.FLAG_CLOCK_MANAGER_SERVICE;

import android.annotation.Hide;
import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.app.customization.clock.ClockDescription;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A Clock Instance describes a currently selected clock, including associated metadata and assets.
 *
 * <p>This class is used to communicate among a clock rendering service, a clock chooser UI, and
 * {@link android.app.ClockManager}. This class describes a specific instance of a clock, which is
 * defined by its {@link ClockDescription} and the associated assets (files) that are needed to
 * render the clock.
 */
@Hide
@FlaggedApi(FLAG_CLOCK_MANAGER_SERVICE)
public final class ClockInstance implements Parcelable {
    @NonNull private final ClockDescription mClockDescription;
    @Nullable private final Map<String, ParcelFileDescriptor> mAssets;

    public ClockInstance(
            @NonNull ClockDescription clockDescription,
            @Nullable Map<String, ParcelFileDescriptor> assets) {
        mClockDescription =
                Objects.requireNonNull(clockDescription, "ClockDescription cannot be null");
        mAssets = assets;
    }

    ////// Parcelable implementation
    ClockInstance(@NonNull Parcel in) {
        // Enforce NonNull contract during unparceling
        mClockDescription =
                Objects.requireNonNull(
                        in.readTypedObject(ClockDescription.CREATOR),
                        "ClockDescription cannot be null");

        // Read the map size
        int mapSize = in.readInt();
        if (mapSize < 0) {
            mAssets = null;
        } else {
            mAssets = new HashMap<>();
            for (int i = 0; i < mapSize; i++) {
                String key = in.readString8();
                ParcelFileDescriptor value = in.readTypedObject(ParcelFileDescriptor.CREATOR);
                mAssets.put(key, value);
            }
        }
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedObject(mClockDescription, flags);

        // Write the map size and entries
        if (mAssets == null) {
            dest.writeInt(-1);
        } else {
            dest.writeInt(mAssets.size());
            for (Map.Entry<String, ParcelFileDescriptor> entry : mAssets.entrySet()) {
                dest.writeString8(entry.getKey());
                dest.writeTypedObject(entry.getValue(), flags);
            }
        }
    }

    public ClockDescription getClockDescription() {
        return mClockDescription;
    }

    @Nullable
    public Map<String, ParcelFileDescriptor> getAssets() {
        return mAssets;
    }

    ////// Comparison overrides
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClockInstance that)) {
            return false;
        }
        return Objects.equals(mClockDescription, that.mClockDescription)
                && Objects.equals(mAssets, that.mAssets);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mClockDescription, mAssets);
    }

    @Override
    public int describeContents() {
        return (mAssets != null && !mAssets.isEmpty()) ? Parcelable.CONTENTS_FILE_DESCRIPTOR : 0;
    }

    @NonNull
    public static final Parcelable.Creator<ClockInstance> CREATOR =
            new Parcelable.Creator<ClockInstance>() {
                @Override
                public ClockInstance createFromParcel(Parcel source) {
                    return new ClockInstance(source);
                }

                @Override
                public ClockInstance[] newArray(int size) {
                    return new ClockInstance[size];
                }
            };
}