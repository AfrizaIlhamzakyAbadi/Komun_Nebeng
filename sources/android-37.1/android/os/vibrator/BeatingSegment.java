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
import android.annotation.TestApi;
import android.os.Parcel;
import android.os.VibrationEffect;
import android.os.VibratorInfo;
import android.util.MathUtils;

import com.android.internal.util.Preconditions;

import java.util.Locale;
import java.util.Objects;

/**
 * A {@link VibrationEffectSegment} that represents a beating effect segment.
 *
 * <p>A beating effect is created by mixing two sinusoidal signals with similar frequencies,
 * producing a vibration that pulses at the difference of those frequencies (the beats per second).
 * The overall amplitude envelope transitions from a starting intensity to an ending intensity
 * over the specified duration. The start intensity is always 0 for the whole envelope. The carrier
 * frequency (expressed as sharpness) can also be controlled.
 *
 * <p>The intensities and sharpness are expressed by float values in the range [0, 1].
 * The beats per second is expressed by a positive finite float value.
 */
@TestApi
@FlaggedApi(Flags.FLAG_BEATING_EFFECT_API)
@android.ravenwood.annotation.RavenwoodKeepWholeClass
public final class BeatingSegment extends VibrationEffectSegment {
    private final float mStartIntensity;
    private final float mEndIntensity;
    private final long mDuration;
    private final float mBeatsPerSecond;
    private final float mSharpness;

    BeatingSegment(@NonNull Parcel in) {
        this(in.readFloat(), in.readFloat(), in.readLong(), in.readFloat(), in.readFloat(),
                in.readLong());
    }

    @Hide
    public BeatingSegment(float startIntensity, float endIntensity, long duration,
            float beatsPerSecond, float sharpness) {
        this(startIntensity, endIntensity, duration, beatsPerSecond, sharpness, -1);
    }

    /**
     * @param startIntensity  The starting intensity of the segment, in the range [0, 1].
     * @param endIntensity    The ending intensity of the segment, in the range [0, 1].
     * @param duration        The duration of the segment in milliseconds. The value must be
     *                        greater than 0.
     * @param beatsPerSecond  The number of beats per second. The value must be greater than 0.
     * @param sharpness       The sharpness of the vibration, which maps to the carrier frequency,
     *                        in the range [0, 1].
     * @param startTimeMillis The time in milliseconds at which this segment should start within
     *                        the overall {@link VibrationEffect}. The default value is -1. When
     *                        the value is negative, it means the segment is not the first segment
     *                        of an atomic event, it is an intermediate segment.
     */
    @Hide
    public BeatingSegment(float startIntensity, float endIntensity, long duration,
            float beatsPerSecond, float sharpness, long startTimeMillis) {
        super(startTimeMillis);
        mStartIntensity = startIntensity;
        mEndIntensity = endIntensity;
        mDuration = duration;
        mBeatsPerSecond = beatsPerSecond;
        mSharpness = sharpness;
    }

    public float getStartIntensity() {
        return mStartIntensity;
    }

    public float getEndIntensity() {
        return mEndIntensity;
    }

    @Override
    public long getDuration() {
        return mDuration;
    }

    public float getBeatsPerSecond() {
        return mBeatsPerSecond;
    }

    public float getSharpness() {
        return mSharpness;
    }

    @Hide
    @Override
    public boolean areVibrationFeaturesSupported(@NonNull VibratorInfo vibratorInfo) {
        // Same as BasicPwleSegment.
        return vibratorInfo.areEnvelopeEffectsSupported();
    }

    @Hide
    @Override
    public boolean isHapticFeedbackCandidate() {
        return true;
    }

    @Hide
    @Override
    public void validate() {
        Preconditions.checkArgumentPositive((float)mDuration,
                "Duration must be greater than zero.");
        Preconditions.checkArgumentInRange(mStartIntensity, 0f, 1f, "startIntensity");
        Preconditions.checkArgumentInRange(mEndIntensity, 0f, 1f, "endIntensity");
        Preconditions.checkArgumentInRange(mSharpness, 0f, 1f, "sharpness");
        Preconditions.checkArgumentPositive(mBeatsPerSecond,
                "Beats per second must be greater than zero.");
    }

    @Hide
    @NonNull
    @Override
    public BeatingSegment resolve(int defaultAmplitude) {
        return this;
    }

    @Hide
    @NonNull
    @Override
    public BeatingSegment scale(float scaleFactor) {
        float newStartIntensity = VibrationEffect.scale(mStartIntensity, scaleFactor);
        float newEndIntensity = VibrationEffect.scale(mEndIntensity, scaleFactor);
        if (Float.compare(mStartIntensity, newStartIntensity) == 0
                && Float.compare(mEndIntensity, newEndIntensity) == 0) {
            return this;
        }
        return new BeatingSegment(newStartIntensity, newEndIntensity, mDuration, mBeatsPerSecond,
                mSharpness, getStartTimeMillis());
    }

    @Hide
    @NonNull
    @Override
    public BeatingSegment applyAdaptiveScale(float scaleFactor) {
        float newStartIntensity = MathUtils.constrain(mStartIntensity * scaleFactor, 0f, 1f);
        float newEndIntensity = MathUtils.constrain(mEndIntensity * scaleFactor, 0f, 1f);
        if (Float.compare(mStartIntensity, newStartIntensity) == 0
                && Float.compare(mEndIntensity, newEndIntensity) == 0) {
            return this;
        }
        return new BeatingSegment(newStartIntensity, newEndIntensity, mDuration, mBeatsPerSecond,
                mSharpness, getStartTimeMillis());
    }

    @Hide
    @NonNull
    @Override
    public BeatingSegment applyEffectStrength(int effectStrength) {
        return this;
    }

    @Hide
    @NonNull
    @Override
    public BeatingSegment applyStartTime(long startTimeMillis) {
        return new BeatingSegment(mStartIntensity, mEndIntensity, mDuration, mBeatsPerSecond,
                mSharpness, startTimeMillis);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BeatingSegment that = (BeatingSegment) o;
        return Float.compare(that.mStartIntensity, mStartIntensity) == 0
                && Float.compare(that.mEndIntensity, mEndIntensity) == 0
                && mDuration == that.mDuration
                && Float.compare(that.mBeatsPerSecond, mBeatsPerSecond) == 0
                && Float.compare(that.mSharpness, mSharpness) == 0
                && getStartTimeMillis() == that.getStartTimeMillis();
    }

    @Override
    public int hashCode() {
        return Objects.hash(mStartIntensity, mEndIntensity, mDuration, mBeatsPerSecond, mSharpness,
                getStartTimeMillis());
    }

    @Override
    public String toString() {
        return "Beating{"
                + "startIntensity=" + mStartIntensity
                + ", endIntensity=" + mEndIntensity
                + ", duration=" + mDuration
                + ", beatsPerSecond=" + mBeatsPerSecond
                + ", sharpness=" + mSharpness
                + (getStartTimeMillis() > 0 ? ", startTimeMillis=" + getStartTimeMillis() : "")
                + '}';
    }

    @Hide
    @Override
    public String toDebugString() {
        return String.format(Locale.US,
                "Beating=%dms(intensity=%.2f to %.2f, %.2f beats/sec, sharpness=%.2f%s)",
                mDuration, mStartIntensity, mEndIntensity, mBeatsPerSecond, mSharpness,
                ", startTime=" + getStartTimeMillis());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(PARCEL_TOKEN_BEATING);
        dest.writeFloat(mStartIntensity);
        dest.writeFloat(mEndIntensity);
        dest.writeLong(mDuration);
        dest.writeFloat(mBeatsPerSecond);
        dest.writeFloat(mSharpness);
        dest.writeLong(getStartTimeMillis());
    }

    @NonNull
    public static final Creator<BeatingSegment> CREATOR =
            new Creator<BeatingSegment>() {
                @Override
                public BeatingSegment createFromParcel(Parcel in) {
                    // Skip the type token
                    in.readInt();
                    return new BeatingSegment(in);
                }

                @Override
                public BeatingSegment[] newArray(int size) {
                    return new BeatingSegment[size];
                }
            };
}
