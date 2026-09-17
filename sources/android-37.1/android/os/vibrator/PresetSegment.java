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

package android.os.vibrator;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.TestApi;
import android.os.Parcel;
import android.os.VibrationEffect;
import android.os.VibratorInfo;
import android.util.MathUtils;

import java.util.Locale;
import java.util.Objects;

/**
 * Represents a predefined haptic effect (a preset) as a segment in a VibrationEffect. This segment
 * has an absolute start time, unlike the relative delays in legacy PrimitiveSegment.
 *
 * <p>When adding a new preset to {@link VibrationEffect.Preset}, remember to update
 * {@link #getDuration(VibratorInfo)} and {@link #areVibrationFeaturesSupported(VibratorInfo)}
 * in this class, as well as {@link VibrationEffect.Preset#isPrimitive(int)}.
 */
@TestApi
@FlaggedApi(Flags.FLAG_COMPOSITION_API)
@android.ravenwood.annotation.RavenwoodKeepWholeClass
public final class PresetSegment extends VibrationEffectSegment {
    private final int mPresetId;
    private final float mScale;

    /**
     * Create a preset segment.
     *
     * @param presetId The ID of the preset to play.
     * @param scale The scale to apply to the intensity of the preset.
     * @param startTimeMillis The time in milliseconds at which this segment should start within the
     *     overall {@link VibrationEffect}. The default value is -1. When the value is negative, it
     *     means the segment is not the first segment of an atomic event, it is an intermediate
     *     segment.
     */
    @Hide
    public PresetSegment(@VibrationEffect.Preset.PresetType int presetId, float scale,
            long startTimeMillis) {
        super(startTimeMillis);
        mPresetId = presetId;
        mScale = scale;
    }

    PresetSegment(@android.annotation.NonNull Parcel in) {
        this(in.readInt(), in.readFloat(), in.readLong());
    }

    @VibrationEffect.Preset.PresetType
    public int getPresetId() {
        return mPresetId;
    }

    public float getScale() {
        return mScale;
    }

    @Override
    public long getDuration() {
        return -1; // Duration of a preset is unknown / device-dependent
    }

    // LINT.IfChange

    @Hide
    @Override
    public long getDuration(@Nullable VibratorInfo vibratorInfo) {
        if (vibratorInfo == null) {
            return getDuration();
        }
        if (VibrationEffect.Preset.isPrimitive(mPresetId)) {
            int duration = vibratorInfo.getPrimitiveDuration(mPresetId);
            return duration > 0 ? duration : getDuration();
        }
        return getDuration();
    }

    @Hide
    @Override
    public boolean areVibrationFeaturesSupported(@NonNull VibratorInfo vibratorInfo) {
        if (VibrationEffect.Preset.isPrimitive(mPresetId)) {
            return vibratorInfo.isPrimitiveSupported(mPresetId);
        }
        return false;
    }
    // LINT.ThenChange(../VibrationEffect.java)

    @Hide
    @Override
    public boolean isHapticFeedbackCandidate() {
        return true;
    }

    @Hide
    @NonNull
    @Override
    public PresetSegment resolve(int defaultAmplitude) {
        return this; // Presets are not amplitude-based in this way
    }

    @Hide
    @NonNull
    @Override
    public PresetSegment scale(float scaleFactor) {
        float newScale = VibrationEffect.scale(mScale, scaleFactor);
        if (Float.compare(mScale, newScale) == 0) {
            return this;
        }
        return new PresetSegment(mPresetId, newScale, getStartTimeMillis());
    }

    @Hide
    @NonNull
    @Override
    public PresetSegment applyAdaptiveScale(float scaleFactor) {
        float newScale = MathUtils.constrain(mScale * scaleFactor, 0f, 1f);
        if (Float.compare(mScale, newScale) == 0) {
            return this;
        }
        return new PresetSegment(mPresetId, newScale, getStartTimeMillis());
    }

    @Hide
    @NonNull
    @Override
    public PresetSegment applyEffectStrength(int effectStrength) {
        return this;
    }

    @Hide
    @NonNull
    @Override
    public PresetSegment applyStartTime(long startTimeMillis) {
        if (this.getStartTimeMillis() == startTimeMillis) {
            return this;
        }
        return new PresetSegment(mPresetId, mScale, startTimeMillis);
    }

    @Hide
    @Override
    public void validate() {
        // Basic validation for ID and scale
        if (mPresetId < 0) { // Assuming preset IDs are non-negative
            throw new IllegalArgumentException("Preset ID must be non-negative");
        }
        if (mScale < 0f || mScale > 1f) {
            throw new IllegalArgumentException("Scale must be between 0 and 1");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PresetSegment)) return false;
        PresetSegment that = (PresetSegment) o;
        return mPresetId == that.mPresetId
                && Float.compare(that.mScale, mScale) == 0
                && getStartTimeMillis() == that.getStartTimeMillis();
    }

    @Override
    public int hashCode() {
        return Objects.hash(mPresetId, mScale, getStartTimeMillis());
    }

    @Override
    public String toString() {
        return "PresetSegment{"
                + "presetId="
                + VibrationEffect.Preset.presetToString(mPresetId)
                + ", scale="
                + mScale
                + (getStartTimeMillis() > 0 ? ", startTimeMillis=" + getStartTimeMillis() : "")
                + '}';
    }

    @Hide
    @NonNull
    @Override
    public String toDebugString() {
        return String.format(
                Locale.ROOT,
                "PresetSegment{id=%s, scale=%.2f, startTimeMillis=%d}",
                VibrationEffect.Preset.presetToString(mPresetId),
                mScale,
                getStartTimeMillis());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
        out.writeInt(PARCEL_TOKEN_PRESET);
        out.writeInt(mPresetId);
        out.writeFloat(mScale);
        out.writeLong(getStartTimeMillis());
    }

    public static final @NonNull Creator<PresetSegment> CREATOR =
            new Creator<PresetSegment>() {
                @Override
                public PresetSegment createFromParcel(Parcel in) {
                    // Skip the type token
                    in.readInt();
                    return new PresetSegment(in);
                }

                @Override
                public PresetSegment[] newArray(int size) {
                    return new PresetSegment[size];
                }
            };
}
