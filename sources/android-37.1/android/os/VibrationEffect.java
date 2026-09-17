/*
 * Copyright (C) 2017 The Android Open Source Project
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

package android.os;

import android.annotation.DurationMillisLong;
import android.annotation.FlaggedApi;
import android.annotation.FloatRange;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.IntRange;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresPermission;
import android.annotation.SystemApi;
import android.annotation.TestApi;
import android.compat.annotation.UnsupportedAppUsage;
import android.content.ContentResolver;
import android.content.Context;
import android.hardware.vibrator.IVibrator;
import android.net.Uri;
import android.text.TextUtils;
import android.os.vibrator.BasicPwleSegment;
import android.os.vibrator.BeatingSegment;
import android.os.vibrator.Flags;
import android.os.vibrator.PrebakedSegment;
import android.os.vibrator.PresetSegment;
import android.os.vibrator.PrimitiveSegment;
import android.os.vibrator.PwleSegment;
import android.os.vibrator.StepSegment;
import android.os.vibrator.VibrationEffectSegment;
import android.os.vibrator.VibratorEnvelopeEffectInfo;
import android.util.Log;
import android.util.MathUtils;

import com.android.internal.util.Preconditions;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.IntStream;

/**
 * A VibrationEffect describes a haptic effect to be performed by a {@link Vibrator}.
 *
 * <p>These effects may be any number of things, from single shot vibrations to complex waveforms.
 */
@android.ravenwood.annotation.RavenwoodKeepWholeClass
public abstract class VibrationEffect implements Parcelable {
    private static final int PARCEL_TOKEN_COMPOSED = 1;
    private static final int PARCEL_TOKEN_VENDOR_EFFECT = 2;

    // If a vibration is playing for longer than 1s, it's probably not haptic feedback
    private static final long MAX_HAPTIC_FEEDBACK_DURATION = 1000;
    // If a vibration is playing more than 3 constants, it's probably not haptic feedback
    private static final long MAX_HAPTIC_FEEDBACK_COMPOSITION_SIZE = 3;

    /**
     * The default vibration strength of the device.
     */
    public static final int DEFAULT_AMPLITUDE = -1;

    /**
     * The maximum amplitude value
     */
    @Hide
    public static final int MAX_AMPLITUDE = 255;

    /**
     * A click effect. Use this effect as a baseline, as it's the most common type of click effect.
     */
    // Internally this maps to the HAL constant Effect::CLICK
    public static final int EFFECT_CLICK = 0;

    /**
     * A double click effect.
     */
    // Internally this maps to the HAL constant Effect::DOUBLE_CLICK
    public static final int EFFECT_DOUBLE_CLICK = 1;

    /**
     * A tick effect. This effect is less strong compared to {@link #EFFECT_CLICK}.
     */
    // Internally this maps to the HAL constant Effect::TICK
    public static final int EFFECT_TICK = 2;

    /**
     * A thud effect.
     * @see #get(int)
     */
    // Internally this maps to the HAL constant Effect::THUD
    @UnsupportedAppUsage(maxTargetSdk = Build.VERSION_CODES.R, trackingBug = 170729553)
    @TestApi
    public static final int EFFECT_THUD = 3;

    /**
     * A pop effect.
     * @see #get(int)
     */
    // Internally this maps to the HAL constant Effect::POP
    @UnsupportedAppUsage(maxTargetSdk = Build.VERSION_CODES.R, trackingBug = 170729553)
    @TestApi
    public static final int EFFECT_POP = 4;

    /**
     * A heavy click effect. This effect is stronger than {@link #EFFECT_CLICK}.
     */
    // Internally this maps to the HAL constant Effect::HEAVY_CLICK
    public static final int EFFECT_HEAVY_CLICK = 5;

    /**
     * A texture effect meant to replicate soft ticks.
     *
     * <p>Unlike normal effects, texture effects are meant to be called repeatedly, generally in
     * response to some motion, in order to replicate the feeling of some texture underneath the
     * user's fingers.
     *
     * @see #get(int)
     */
    // Internally this maps to the HAL constant Effect::TEXTURE_TICK
    @TestApi
    public static final int EFFECT_TEXTURE_TICK = 21;

    // Internally this maps to the HAL constant EffectStrength::LIGHT
    @TestApi
    public static final int EFFECT_STRENGTH_LIGHT = 0;

    // Internally this maps to the HAL constant EffectStrength::MEDIUM
    @TestApi
    public static final int EFFECT_STRENGTH_MEDIUM = 1;

    // Internally this maps to the HAL constant EffectStrength::STRONG
    @TestApi
    public static final int EFFECT_STRENGTH_STRONG = 2;

    /**
     * Ringtone patterns. They may correspond with the device's ringtone audio, or may just be a
     * pattern that can be played as a ringtone with any audio, depending on the device.
     *
     * @see #get(Uri, Context)
     */
    // Internally this maps to the HAL constant Effect::RINGTONE_*
    @UnsupportedAppUsage(maxTargetSdk = Build.VERSION_CODES.R, trackingBug = 170729553)
    @TestApi
    public static final int[] RINGTONES = IntStream.rangeClosed(6, 20).toArray();

    /**
     * The sharpness value that is mapped to the resonant frequency of the device.
     */
    @FlaggedApi(Flags.FLAG_BEATING_EFFECT_API)
    public static final float RESONANT_FREQUENCY_SHARPNESS = 0.7f;

    @Hide
    @IntDef(prefix = { "EFFECT_" }, value = {
            EFFECT_TICK,
            EFFECT_CLICK,
            EFFECT_HEAVY_CLICK,
            EFFECT_DOUBLE_CLICK,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface EffectType {}

    /**
     * to prevent subclassing from outside of the framework
     */
    @Hide
    public VibrationEffect() { }

    /**
     * Create a one shot vibration.
     *
     * <p>One shot vibrations will vibrate constantly for the specified period of time at the
     * specified amplitude, and then stop.
     *
     * @param milliseconds The number of milliseconds to vibrate. This must be a positive number.
     * @param amplitude The strength of the vibration. This must be a value between 1 and 255, or
     * {@link #DEFAULT_AMPLITUDE}.
     *
     * @return The desired effect.
     */
    public static VibrationEffect createOneShot(long milliseconds, int amplitude) {
        if (amplitude == 0) {
            throw new IllegalArgumentException(
                    "amplitude must either be DEFAULT_AMPLITUDE, "
                            + "or between 1 and 255 inclusive (amplitude=" + amplitude + ")");
        }
        return createWaveform(new long[]{milliseconds}, new int[]{amplitude}, -1 /* repeat */);
    }

    /**
     * Create a waveform vibration, using only off/on transitions at the provided time intervals,
     * and potentially repeating.
     *
     * <p>In effect, the timings array represents the number of milliseconds <em>before</em> turning
     * the vibrator on, followed by the number of milliseconds to keep the vibrator on, then
     * the number of milliseconds turned off, and so on. Consequently, the first timing value will
     * often be 0, so that the effect will start vibrating immediately.
     *
     * <p>This method is equivalent to calling {@link #createWaveform(long[], int[], int)} with
     * corresponding amplitude values alternating between 0 and {@link #DEFAULT_AMPLITUDE},
     * beginning with 0.
     *
     * <p>To cause the pattern to repeat, pass the index into the timings array at which to start
     * the repetition, or -1 to disable repeating. Repeating effects will be played indefinitely
     * and should be cancelled via {@link Vibrator#cancel()}.
     *
     * @param timings The pattern of alternating on-off timings, starting with an 'off' timing, and
     *               representing the length of time to sustain the individual item (not
     *               cumulative).
     * @param repeat The index into the timings array at which to repeat, or -1 if you don't
     *               want to repeat indefinitely.
     *
     * @return The desired effect.
     */
    public static VibrationEffect createWaveform(long[] timings, int repeat) {
        int[] amplitudes = new int[timings.length];
        for (int i = 0; i < (timings.length / 2); i++) {
            amplitudes[i * 2 + 1] = VibrationEffect.DEFAULT_AMPLITUDE;
        }
        return createWaveform(timings, amplitudes, repeat);
    }

    /**
     * Computes a legacy vibration pattern (i.e. a pattern with duration values for "off/on"
     * vibration components) that is equivalent to this VibrationEffect.
     *
     * <p>All non-repeating effects created with {@link #createWaveform(long[], int)} are
     * convertible into an equivalent vibration pattern with this method. It is not guaranteed that
     * an effect created with other means becomes converted into an equivalent legacy vibration
     * pattern, even if it has an equivalent vibration pattern. If this method is unable to create
     * an equivalent vibration pattern for such effects, it will return {@code null}.
     *
     * <p>Note that a valid equivalent long[] pattern cannot be created for an effect that has any
     * form of repeating behavior, regardless of how the effect was created. For repeating effects,
     * the method will always return {@code null}.
     *
     * @return a long array representing a vibration pattern equivalent to the VibrationEffect, if
     *               the method successfully derived a vibration pattern equivalent to the effect
     *               (this will always be the case if the effect was created via
     *               {@link #createWaveform(long[], int)} and is non-repeating). Otherwise, returns
     *               {@code null}.
     */
    @TestApi
    @Nullable
    public abstract long[] computeCreateWaveformOffOnTimingsOrNull();

    /**
     * Create a waveform vibration.
     *
     * <p>Waveform vibrations are a potentially repeating series of timing and amplitude pairs,
     * provided in separate arrays. For each pair, the value in the amplitude array determines
     * the strength of the vibration and the value in the timing array determines how long it
     * vibrates for, in milliseconds.
     *
     * <p>To cause the pattern to repeat, pass the index into the timings array at which to start
     * the repetition, or -1 to disable repeating. Repeating effects will be played indefinitely
     * and should be cancelled via {@link Vibrator#cancel()}.
     *
     * @param timings The timing values, in milliseconds, of the timing / amplitude pairs. Timing
     *                values of 0 will cause the pair to be ignored.
     * @param amplitudes The amplitude values of the timing / amplitude pairs. Amplitude values
     *                   must be between 0 and 255, or equal to {@link #DEFAULT_AMPLITUDE}. An
     *                   amplitude value of 0 implies the motor is off.
     * @param repeat The index into the timings array at which to repeat, or -1 if you don't
     *               want to repeat indefinitely.
     *
     * @return The desired effect.
     */
    public static VibrationEffect createWaveform(long[] timings, int[] amplitudes, int repeat) {
        if (timings.length != amplitudes.length) {
            throw new IllegalArgumentException(
                    "timing and amplitude arrays must be of equal length"
                            + " (timings.length=" + timings.length
                            + ", amplitudes.length=" + amplitudes.length + ")");
        }
        List<StepSegment> segments = new ArrayList<>();
        for (int i = 0; i < timings.length; i++) {
            float parsedAmplitude = amplitudes[i] == DEFAULT_AMPLITUDE
                    ? DEFAULT_AMPLITUDE : (float) amplitudes[i] / MAX_AMPLITUDE;
            segments.add(
                    new StepSegment(
                            parsedAmplitude,
                            (int) timings[i]));
        }
        VibrationEffect effect = new Composed(segments, repeat);
        effect.validate();
        return effect;
    }

    /**
     * Create a predefined vibration effect.
     *
     * <p>Predefined effects are a set of common vibration effects that should be identical,
     * regardless of the app they come from, in order to provide a cohesive experience for users
     * across the entire device. They also may be custom tailored to the device hardware in order to
     * provide a better experience than you could otherwise build using the generic building
     * blocks.
     *
     * <p>This will fallback to a generic pattern if one exists and there does not exist a
     * hardware-specific implementation of the effect.
     *
     * @param effectId The ID of the effect to perform:
     *                 {@link #EFFECT_CLICK}, {@link #EFFECT_DOUBLE_CLICK}, {@link #EFFECT_TICK}
     *
     * @return The desired effect.
     */
    @NonNull
    public static VibrationEffect createPredefined(@EffectType int effectId) {
        return get(effectId, true);
    }

    /**
     * Create a vendor-defined vibration effect.
     *
     * <p>Vendor effects offer more flexibility for accessing vendor-specific vibrator capabilities,
     * enabling control over any vibration parameter and more generic vibration waveforms for apps
     * provided by the device vendor.
     *
     * <p>This requires hardware-specific implementation of the effect and will not have any
     * platform fallback support.
     *
     * @param effect An opaque representation of the vibration effect which can also be serialized.
     * @return The desired effect.
     */
    @NonNull
    @SystemApi
    @RequiresPermission(android.Manifest.permission.VIBRATE_VENDOR_EFFECTS)
    public static VibrationEffect createVendorEffect(@NonNull PersistableBundle effect) {
        VibrationEffect vendorEffect = new VendorEffect(effect, VendorEffect.DEFAULT_STRENGTH,
                VendorEffect.DEFAULT_SCALE, VendorEffect.DEFAULT_SCALE);
        vendorEffect.validate();
        return vendorEffect;
    }

    /**
     * Get a predefined vibration effect.
     *
     * <p>Predefined effects are a set of common vibration effects that should be identical,
     * regardless of the app they come from, in order to provide a cohesive experience for users
     * across the entire device. They also may be custom tailored to the device hardware in order to
     * provide a better experience than you could otherwise build using the generic building
     * blocks.
     *
     * <p>This will fallback to a generic pattern if one exists and there does not exist a
     * hardware-specific implementation of the effect.
     *
     * @param effectId The ID of the effect to perform:
     *                 {@link #EFFECT_CLICK}, {@link #EFFECT_DOUBLE_CLICK}, {@link #EFFECT_TICK}
     *
     * @return The desired effect.
     */
    @TestApi
    public static VibrationEffect get(int effectId) {
        return get(effectId, PrebakedSegment.DEFAULT_SHOULD_FALLBACK);
    }

    /**
     * Get a predefined vibration effect.
     *
     * <p>Predefined effects are a set of common vibration effects that should be identical,
     * regardless of the app they come from, in order to provide a cohesive experience for users
     * across the entire device. They also may be custom tailored to the device hardware in order to
     * provide a better experience than you could otherwise build using the generic building
     * blocks.
     *
     * <p>Some effects you may only want to play if there's a hardware specific implementation
     * because they may, for example, be too disruptive to the user without tuning. The
     * {@code fallback} parameter allows you to decide whether you want to fallback to the generic
     * implementation or only play if there's a tuned, hardware specific one available.
     *
     * @param effectId The ID of the effect to perform:
     *                 {@link #EFFECT_CLICK}, {@link #EFFECT_DOUBLE_CLICK}, {@link #EFFECT_TICK}
     * @param fallback Whether to fall back to a generic pattern if a hardware specific
     *                 implementation doesn't exist.
     *
     * @return The desired effect.
     */
    @TestApi
    public static VibrationEffect get(int effectId, boolean fallback) {
        VibrationEffect effect = new Composed(
                new PrebakedSegment(effectId, fallback, PrebakedSegment.DEFAULT_STRENGTH));
        effect.validate();
        return effect;
    }

    /**
     * Get a predefined vibration effect associated with a given URI.
     *
     * <p>Predefined effects are a set of common vibration effects that should be identical,
     * regardless of the app they come from, in order to provide a cohesive experience for users
     * across the entire device. They also may be custom tailored to the device hardware in order to
     * provide a better experience than you could otherwise build using the generic building
     * blocks.
     *
     * @param uri The URI associated with the haptic effect.
     * @param context The context used to get the URI to haptic effect association.
     *
     * @return The desired effect, or {@code null} if there's no associated effect.
     */
    @TestApi
    @Nullable
    public static VibrationEffect get(Uri uri, Context context) {
        String[] uris = context.getResources().getStringArray(
                com.android.internal.R.array.config_ringtoneEffectUris);

        // Skip doing any IPC if we don't have any effects configured.
        if (uris.length == 0) {
            return null;
        }

        final ContentResolver cr = context.getContentResolver();
        Uri uncanonicalUri = cr.uncanonicalize(uri);
        if (uncanonicalUri == null) {
            // If we already had an uncanonical URI, it's possible we'll get null back here. In
            // this case, just use the URI as passed in since it wasn't canonicalized in the first
            // place.
            uncanonicalUri = uri;
        }

        for (int i = 0; i < uris.length && i < RINGTONES.length; i++) {
            if (uris[i] == null) {
                continue;
            }
            Uri mappedUri = cr.uncanonicalize(Uri.parse(uris[i]));
            if (mappedUri == null) {
                continue;
            }
            if (mappedUri.equals(uncanonicalUri)) {
                return get(RINGTONES[i]);
            }
        }
        return null;
    }

    /**
     * Start composing a haptic effect.
     *
     * @see VibrationEffect.Composition
     */
    @NonNull
    public static Composition startComposition() {
        return new Composition();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Hide
    public abstract void validate();


    /**
     * If supported, truncate the length of this vibration effect to the provided length and return
     * the result. Will always return null for repeating effects.
     *
     * @return The desired effect, or {@code null} if truncation is not applicable.
     */
    @Hide
    @Nullable
    public abstract VibrationEffect cropToLengthOrNull(int length);

    /**
     * Gets the estimated duration of the vibration in milliseconds.
     *
     * <p>For effects without a defined end (e.g. a Waveform with a non-negative repeat index), this
     * returns Long.MAX_VALUE. For effects with an unknown duration (e.g. predefined effects where
     * the length is device and potentially run-time dependent), this returns -1.
     */
    @TestApi
    public abstract long getDuration();

    /**
     * Gets the estimated duration of the segment for given vibrator, in milliseconds.
     *
     * <p>For effects with hardware-dependent constants (e.g. primitive compositions), this returns
     * the estimated duration based on the given {@link VibratorInfo}. For all other effects this
     * will return the same as {@link #getDuration()}.
     */
    @Hide
    public long getDuration(@Nullable VibratorInfo vibratorInfo) {
        return getDuration();
    }

    /**
     * Gets the duration of the repeating part of the effect, in milliseconds.
     *
     * <p>Only valid when the effect is repeating.
     *
     * @return The duration of the repeating part, in milliseconds, or 0 if it is not repeating.
     */
    @FlaggedApi(Flags.FLAG_COMPOSITION_API)
    @TestApi
    public long getRepeatingDurationMillis() {
        return 0;
    }

    /**
     * Checks if a vibrator with a given {@link VibratorInfo} can play this effect as intended.
     *
     * <p>See {@link VibratorInfo#areVibrationFeaturesSupported(VibrationEffect)} for more
     * information about what counts as supported by a vibrator, and what counts as not.
     */
    @Hide
    public abstract boolean areVibrationFeaturesSupported(@NonNull VibratorInfo vibratorInfo);

    /**
     * Returns a list of {@link VibrationEffect.Event}s that make up this effect. This effect must
     * NOT be a repeating effect.
     *
     * <p>This method is used to retrieve the constituent events from a {@link VibrationEffect},
     * primarily for use with {@link VibrationEffect.Builder}.
     *
     * <p>If this effect was created via the {@link Composition} API, then each primitive will be
     * converted into an event. The start time for each event is calculated using the primitive
     * delay and an estimated duration for all preceding primitives. These estimates are based on
     * the recommended primitive duration targets from <a href=
     * "https://source.android.com/docs/core/interaction/haptics/haptics-constants-primitives"
     * >Android Haptics Constants</a>. The estimates used are:
     *  - 12ms for {@link Composition#PRIMITIVE_CLICK},
     *  - 5ms for {@link Composition#PRIMITIVE_TICK},
     *  - 12ms for {@link Composition#PRIMITIVE_LOW_TICK},
     *  - 500ms for {@link Composition#PRIMITIVE_SLOW_RISE},
     *  - 150ms for {@link Composition#PRIMITIVE_QUICK_RISE},
     *  - 100ms for {@link Composition#PRIMITIVE_QUICK_FALL},
     *  - 300ms for {@link Composition#PRIMITIVE_THUD},
     *  - 150ms for {@link Composition#PRIMITIVE_SPIN}.
     *
     * <p>Actual primitive durations may differ between devices.
     *
     * @return A list of events.
     * @throws UnsupportedOperationException if this effect type cannot be represented as a list of
     *     events.
     * @throws IllegalStateException if the effect is a repeating effect.
     */
    @FlaggedApi(Flags.FLAG_COMPOSITION_API)
    @NonNull
    public abstract List<Event> getEvents();

    /**
     * Returns true if the effect is timeline-anchored.
     *
     * <p>Timeline-anchored effects are those where at least the first segment has an absolute
     * start time defined.
     */
    @Hide
    public boolean isTimelineAnchored() {
        return false;
    }

    /**
     * Returns true if this effect could represent a touch haptic feedback.
     *
     * <p>It is strongly recommended that an instance of {@link VibrationAttributes} is specified
     * for each vibration, with the correct usage. When a vibration is played with usage UNKNOWN,
     * then this method will be used to classify the most common use case and make sure they are
     * covered by the user settings for "Touch feedback".
     */
    @Hide
    public boolean isHapticFeedbackCandidate() {
        return false;
    }

    /**
     * Resolve default values into integer amplitude numbers.
     *
     * @param defaultAmplitude the default amplitude to apply, must be between 0 and
     *                         MAX_AMPLITUDE
     * @return this if amplitude value is already set, or a copy of this effect with given default
     *         amplitude otherwise
     */
    @Hide
    @NonNull
    public abstract VibrationEffect resolve(int defaultAmplitude);

    /**
     * Applies given effect strength to predefined and vendor-specific effects.
     *
     * @param effectStrength new effect strength to be applied, one of
     *                       VibrationEffect.EFFECT_STRENGTH_*.
     * @return this if there is no change, or a copy of this effect with new strength otherwise
     */
    @Hide
    @NonNull
    public abstract VibrationEffect applyEffectStrength(int effectStrength);

    /**
     * Scale the vibration effect intensity with the given constraints.
     *
     * @param scaleFactor scale factor to be applied to the intensity. Values within [0,1) will
     *                    scale down the intensity, values larger than 1 will scale up
     * @return this if there is no scaling to be done, or a copy of this effect with scaled
     *         vibration intensity otherwise
     */
    @Hide
    @NonNull
    public abstract VibrationEffect scale(float scaleFactor);

    /**
     * Applies given scale factor as adaptive scale.
     *
     * @param scaleFactor scale factor to be applied to the intensity. Values within [0,1) will
     *                    scale down the intensity, values larger than 1 will scale up
     * @return this if there is no scaling to be done, or a copy of this effect with scaled
     *         vibration intensity otherwise
     */
    @Hide
    @NonNull
    public abstract VibrationEffect applyAdaptiveScale(float scaleFactor);

    /**
     * Ensures that the effect is repeating indefinitely or not. This is a lossy operation and
     * should only be applied once to an original effect - it shouldn't be applied to the
     * result of this method.
     *
     * <p>Non-repeating effects will be made repeating by looping the entire effect with the
     * specified delay between each loop. The delay is added irrespective of whether the effect
     * already has a delay at the beginning or end.
     *
     * <p>Repeating effects will be left with their native repeating portion if it should be
     * repeating, and otherwise the loop index is removed, so that the entire effect plays once.
     *
     * @param wantRepeating Whether the effect is required to be repeating or not.
     * @param loopDelayMs The milliseconds to pause between loops, if repeating is to be added to
     *                    the effect. Ignored if {@code repeating==false} or the effect is already
     *                    repeating itself. No delay is added if <= 0.
     * @return this if the effect already satisfies the repeating requirement, or a copy of this
     *         adjusted to repeat or not repeat as appropriate.
     */
    @Hide
    @NonNull
    public abstract VibrationEffect applyRepeatingIndefinitely(
            boolean wantRepeating, int loopDelayMs);

    /**
     * Scale given vibration intensity by the given factor.
     *
     * <p> This scale is not necessarily linear and may apply a gamma correction to the scale
     * factor before using it.
     *
     * @param intensity   relative intensity of the effect, must be between 0 and 1
     * @param scaleFactor scale factor to be applied to the intensity. Values within [0,1) will
     *                    scale down the intensity, values larger than 1 will scale up
     * @return the scaled intensity which will be values within [0, 1].
     */
    @Hide
    public static float scale(float intensity, float scaleFactor) {
        if (scaleFactor < 0 || Float.compare(scaleFactor, 1f) == 0) {
            return intensity;
        }
        // Using S * x / (1 + (S - 1) * x^2) as the scale up function to converge to 1.0.
        float scaledIntensity = (scaleFactor <= 1 || Float.compare(intensity, 0f) == 0)
                ? scaleFactor * intensity
                : (scaleFactor * intensity) / (1 + (scaleFactor - 1) * intensity * intensity);
        return MathUtils.constrain(scaledIntensity, 0f, 1f);
    }

    /**
     * Returns a compact version of the {@link #toString()} result for debugging purposes.
     */
    @Hide
    public abstract String toDebugString();

    @Hide
    public static String effectIdToString(int effectId) {
        return switch (effectId) {
            case EFFECT_CLICK -> "CLICK";
            case EFFECT_TICK -> "TICK";
            case EFFECT_HEAVY_CLICK -> "HEAVY_CLICK";
            case EFFECT_DOUBLE_CLICK -> "DOUBLE_CLICK";
            case EFFECT_POP -> "POP";
            case EFFECT_THUD -> "THUD";
            case EFFECT_TEXTURE_TICK -> "TEXTURE_TICK";
            default -> Integer.toString(effectId);
        };
    }

    @Hide
    public static String effectStrengthToString(int effectStrength) {
        return switch (effectStrength) {
            case EFFECT_STRENGTH_LIGHT -> "LIGHT";
            case EFFECT_STRENGTH_MEDIUM -> "MEDIUM";
            case EFFECT_STRENGTH_STRONG -> "STRONG";
            default -> Integer.toString(effectStrength);
        };
    }

    /**
     * Returns an estimated duration for a given primitive.
     *
     * <p>These estimates are based on the recommended primitive duration targets from <a
     * href="https://source.android.com/docs/core/interaction/haptics/haptics-constants-primitives">
     * Android Haptics Constants</a>.
     */
    @Hide
    public static long getEstimatedPrimitiveDuration(int primitiveId) {
        return switch (primitiveId) {
            case Composition.PRIMITIVE_NOOP -> 0;
            case Composition.PRIMITIVE_CLICK -> 12;
            case Composition.PRIMITIVE_TICK -> 5;
            case Composition.PRIMITIVE_LOW_TICK -> 12;
            case Composition.PRIMITIVE_SLOW_RISE -> 500;
            case Composition.PRIMITIVE_QUICK_RISE -> 150;
            case Composition.PRIMITIVE_QUICK_FALL -> 100;
            case Composition.PRIMITIVE_THUD -> 300;
            case Composition.PRIMITIVE_SPIN -> 150;
            default -> throw new IllegalArgumentException("Unknown primitive ID: " + primitiveId);
        };
    }

    /**
     * Returns an estimated duration for a given predefined effect.
     */
    @Hide
    public static long getEstimatedPredefinedDuration(int effectId) {
        // TODO: Revisit the duration estimation for predefined effects here.
        return switch (effectId) {
            case EFFECT_TICK, EFFECT_CLICK, EFFECT_HEAVY_CLICK ->
                    getEstimatedPrimitiveDuration(Composition.PRIMITIVE_CLICK);
            case EFFECT_DOUBLE_CLICK ->
                    getEstimatedPrimitiveDuration(Composition.PRIMITIVE_CLICK) * 2;
            case EFFECT_TEXTURE_TICK -> getEstimatedPrimitiveDuration(Composition.PRIMITIVE_TICK);
            case EFFECT_THUD -> getEstimatedPrimitiveDuration(Composition.PRIMITIVE_THUD);
            default -> {
                // Use 1ms as a placeholder for unknown durations to satisfy the strictly
                // increasing timeline requirement. Developers should use setRepeatingEffect
                // for more precise control over the timing of repeating parts.
                yield 1;
            }
        };
    }

    /**
     * Estimates the duration of a single segment.
     *
     * <p>If the segment has a fixed duration, it returns that duration. Otherwise, it uses
     * hardware-dependent estimates for primitives, presets, and prebaked effects.
     */
    private static long getEstimatedDurationForSegment(VibrationEffectSegment segment) {
        long duration = segment.getDuration();
        if (duration >= 0) {
            return duration;
        }
        if (segment instanceof PresetSegment preset) {
            return getEstimatedPrimitiveDuration(preset.getPresetId());
        }
        if (segment instanceof PrebakedSegment prebaked) {
            return getEstimatedPredefinedDuration(prebaked.getEffectId());
        }
        if (segment instanceof PrimitiveSegment primitive) {
            return getEstimatedPrimitiveDuration(primitive.getPrimitiveId());
        }
        return 1;
    }

    /**
     * Transforms a {@link VibrationEffect} using a generic parameter.
     *
     * <p>This can be used for scaling effects based on user settings or adapting them to the
     * capabilities of a specific device vibrator.
     *
     * @param <ParamT> The type of parameter to be used on the effect by this transformation
     */
    @Hide
    public interface Transformation<ParamT> {

        /** Transforms given effect by applying the given parameter. */
        @NonNull
        VibrationEffect transform(@NonNull VibrationEffect effect, @NonNull ParamT param);
    }

    /**
     * Implementation of {@link VibrationEffect} described by a composition of one or more
     * {@link VibrationEffectSegment}, with an optional index to represent repeating effects.
     *
     * <p>Instances of this class are immutable.
     */
    @TestApi
    public static final class Composed extends VibrationEffect {
        private final ArrayList<VibrationEffectSegment> mSegments;
        private final int mRepeatIndex;

        /**
         * The duration of the repeating part of the effect, in milliseconds.
         *
         * <p>This value is used for scheduling and looping, particularly in {@link
         * #createRepeatingEffect}. This is a precise value if explicitly provided by the developer
         * (e.g. via {@link Builder#setRepeatingEffect}). Otherwise, it is a best-effort estimation,
         * especially when legacy segments (which might have hardware-dependent durations) are
         * involved.
         *
         * <p>This is the total cycle time for each repeat. If it's longer than the natural duration
         * (the time from the start of the first segment to the end of the last segment in the
         * repeating part), then an implicit silence (pause) is added at the end of each cycle to
         * fill the remaining time. If it's shorter, then late events within the repeating effect
         * are skipped.
         */
        private final long mRepeatingDurationMillis;

        private static final Function<VibrationEffectSegment, Long> DEFAULT_DURATION_FN =
                VibrationEffectSegment::getDuration;
        // Cached duration for the default duration function to avoid recalculation.
        // This is safe because Composed effects are immutable, and the default
        // duration function does not depend on external state like VibratorInfo.
        // 0 indicates uninitialized, as valid durations are positive or -1.
        private volatile long mCachedDuration = 0;

        @Hide
        Composed(@NonNull Parcel in) {
            this(Objects.requireNonNull(in.readArrayList(
                            VibrationEffectSegment.class.getClassLoader(),
                            VibrationEffectSegment.class)),
                    in.readInt(),
                    in.readLong());
        }

        @Hide
        Composed(@NonNull VibrationEffectSegment segment) {
            // A composed effect initialized with a single segment is not repeating.
            // When an effect is not repeating (repeatIndex = -1), the repeating duration is
            // ignored. Therefore, a value of 0 is safe here.
            this(Arrays.asList(segment), /* repeatIndex= */ -1, /* repeatingDurationMillis= */ 0);
        }

        @Hide
        public Composed(@NonNull List<? extends VibrationEffectSegment> segments, int repeatIndex) {
            // When repeating duration is not provided, the effect was created using the legacy API
            // and the repeating duration is unknown/ignored. Therefore, a value of 0 is safe here.
            this(segments, repeatIndex, /* repeatingDurationMillis= */ 0);
        }

        @Hide
        public Composed(@NonNull List<? extends VibrationEffectSegment> segments, int repeatIndex,
                long repeatingDurationMillis) {
            super();
            mSegments = new ArrayList<>(segments);
            mRepeatIndex = repeatIndex;
            mRepeatingDurationMillis = repeatingDurationMillis;
        }

        @NonNull
        public List<VibrationEffectSegment> getSegments() {
            return mSegments;
        }

        public int getRepeatIndex() {
            return mRepeatIndex;
        }

        /**
         * Returns the duration of the repeating portion of the effect, in milliseconds.
         *
         * <p>This is the total cycle time for each repeat. If it's longer than the natural duration
         * (the time from the start of the first segment to the end of the last segment in the
         * repeating part), then an implicit silence (pause) is added at the end of each cycle to
         * fill the remaining time. If it's shorter, then late events within the repeating effect
         * are skipped.
         *
         * <p>This value is used for scheduling and looping, particularly in
         * {@link #createRepeatingEffect}. This is a precise value if explicitly provided by the
         * developer (e.g. via {@link Builder#setRepeatingEffect}). Otherwise, it is a best-effort
         * estimation, especially when legacy segments (which might have hardware-dependent
         * durations) are involved.
         *
         * <p>Only valid when the effect is repeating (i.e. {@link #getRepeatIndex()} is
         * non-negative).
         */
        @Override
        @TestApi
        @FlaggedApi(Flags.FLAG_COMPOSITION_API)
        public long getRepeatingDurationMillis() {
            return mRepeatingDurationMillis;
        }

        @Hide
        @Override
        public boolean isTimelineAnchored() {
            return !mSegments.isEmpty() && mSegments.get(0).getStartTimeMillis() >= 0;
        }

        private boolean isLegacyPrimitiveComposition() {
            for (int i = 0; i < mSegments.size(); i++) {
                if (!(mSegments.get(i) instanceof PrimitiveSegment)) {
                    return false;
                }
            }
            return !mSegments.isEmpty();
        }

        /**
         * {@inheritDoc}
         *
         * @throws IllegalStateException if the effect is repeating.
         */
        @NonNull
        @Override
        @FlaggedApi(Flags.FLAG_COMPOSITION_API)
        public List<Event> getEvents() {
            if (mRepeatIndex >= 0) {
                throw new IllegalStateException(
                        "Repeating effects do not support being represented as a sequence of"
                            + " events.");
            }

            List<VibrationEffectSegment> workingSegments = mSegments;
            if (isLegacyPrimitiveComposition()) {
                workingSegments = convertToPresetSegments(mSegments, 0);
            }

            ArrayList<Event> events = new ArrayList<>();
            if (workingSegments.isEmpty()) {
                return events;
            }

            int eventStartIdx = 0;
            long eventStartTime = workingSegments.get(0).getStartTimeMillis();
            if (eventStartTime < 0) {
                // Fallback for legacy effects that don't have explicit start times defined
                // (e.g. effects unparceled from older clients).
                eventStartTime = 0;
            }

            for (int i = 1; i < workingSegments.size(); i++) {
                long currentStartTime = workingSegments.get(i).getStartTimeMillis();
                if (currentStartTime >= 0) {
                    // Group the accumulated segments into a single Event.
                    List<VibrationEffectSegment> eventSegments =
                            workingSegments.subList(eventStartIdx, i);
                    events.add(createEventFromSegments(eventSegments, eventStartTime));

                    eventStartIdx = i;
                    eventStartTime = currentStartTime;
                }
            }

            // Add the final event
            List<VibrationEffectSegment> eventSegments =
                    workingSegments.subList(eventStartIdx, workingSegments.size());
            events.add(createEventFromSegments(eventSegments, eventStartTime));
            return Collections.unmodifiableList(events);
        }

        private List<VibrationEffectSegment> convertToPresetSegments(
                List<VibrationEffectSegment> segments, long baseStartTime) {
            // TODO(b/469962388): handle the legacy composition composed using hidden API
            // addEffect() here, which might contain any kind of segments, not just
            // PrimitiveSegment.
            ArrayList<VibrationEffectSegment> presetSegments = new ArrayList<>();
            long currentPrimAbsStartTime = baseStartTime;
            long lastPrimAbsEndTime = baseStartTime;
            long lastPrimAbsStartTime = baseStartTime;

            for (int i = 0; i < segments.size(); i++) {
                PrimitiveSegment prim = (PrimitiveSegment) segments.get(i);

                if (i == 0) {
                    currentPrimAbsStartTime = baseStartTime;
                } else {
                    if (prim.getDelayType() == VibrationEffect.Composition.DELAY_TYPE_PAUSE) {
                        currentPrimAbsStartTime = lastPrimAbsEndTime + prim.getDelay();
                    } else if (prim.getDelayType()
                            == VibrationEffect.Composition.DELAY_TYPE_RELATIVE_START_OFFSET) {
                        currentPrimAbsStartTime = lastPrimAbsStartTime + prim.getDelay();
                    } else {
                        throw new IllegalStateException(
                                "Unknown delay type: " + prim.getDelayType());
                    }
                }

                int primitiveId = prim.getPrimitiveId();
                presetSegments.add(new PresetSegment(
                        primitiveId,
                        prim.getScale(),
                        currentPrimAbsStartTime
                ));

                lastPrimAbsStartTime = currentPrimAbsStartTime;
                lastPrimAbsEndTime =
                        currentPrimAbsStartTime + getEstimatedPrimitiveDuration(primitiveId);
            }
            return presetSegments;
        }



        private Event createEventFromSegments(List<VibrationEffectSegment> segments,
                long eventStartTime) {
            List<VibrationEffectSegment> segmentsForEvent = new ArrayList<>();
            for (int i = 0; i < segments.size(); i++) {
                VibrationEffectSegment currentSeg = segments.get(i);
                // The first segment's start time is now represented by the Event's start time.
                // We set the segment's internal start time to 0 so that it starts immediately
                // when the new Composed effect is played independently.
                segmentsForEvent.add(i == 0 ? currentSeg.applyStartTime(0) : currentSeg);
            }
            return new Event(new Composed(segmentsForEvent, -1), eventStartTime);
        }

        @Hide
        @Override
        @Nullable
        public long[] computeCreateWaveformOffOnTimingsOrNull() {
            if (getRepeatIndex() >= 0) {
                // Repeating effects cannot be fully represented as a long[] legacy pattern.
                return null;
            }

            List<VibrationEffectSegment> segments = getSegments();

            // The maximum possible size of the final pattern is 1 plus the number of segments in
            // the original effect. This is because we will add an empty "off" segment at the
            // start of the pattern if the first segment of the original effect is an "on" segment.
            // (because the legacy patterns start with an "off" pattern). Other than this one case,
            // we will add the durations of back-to-back segments of similar amplitudes (amplitudes
            // that are all "on" or "off") and create a pattern entry for the total duration, which
            // will not take more number pattern entries than the number of segments processed.
            long[] patternBuffer = new long[segments.size() + 1];
            int patternIndex = 0;

            for (int i = 0; i < segments.size(); i++) {
                StepSegment stepSegment =
                        castToValidStepSegmentForOffOnTimingsOrNull(segments.get(i));
                if (stepSegment == null) {
                    // This means that there is 1 or more segments of this effect that is/are not a
                    // possible component of a legacy vibration pattern. Thus, the VibrationEffect
                    // does not have any equivalent legacy vibration pattern.
                    return null;
                }

                boolean isSegmentOff = stepSegment.getAmplitude() == 0;
                // Even pattern indices are "off", and odd pattern indices are "on"
                boolean isCurrentPatternIndexOff = (patternIndex % 2) == 0;
                if (isSegmentOff != isCurrentPatternIndexOff) {
                    // Move the pattern index one step ahead, so that the current segment's
                    // "off"/"on" property matches that of the index's
                    ++patternIndex;
                }
                patternBuffer[patternIndex] += stepSegment.getDuration();
            }

            return Arrays.copyOf(patternBuffer, patternIndex + 1);
        }

        @Hide
        @Override
        public void validate() {
            int segmentCount = mSegments.size();
            boolean hasNonZeroDuration = false;
            for (int i = 0; i < segmentCount; i++) {
                VibrationEffectSegment segment = mSegments.get(i);
                segment.validate();
                // A segment with unknown duration = -1 still counts as a non-zero duration.
                hasNonZeroDuration |= segment.getDuration() != 0;
            }
            if (!hasNonZeroDuration) {
                throw new IllegalArgumentException("at least one timing must be non-zero"
                        + " (segments=" + mSegments + ")");
            }
            if (mRepeatIndex != -1) {
                Preconditions.checkArgumentInRange(mRepeatIndex, 0, segmentCount - 1,
                        "repeat index must be within the bounds of the segments (segments.length="
                                + segmentCount + ", index=" + mRepeatIndex + ")");
            }
        }

        @Hide
        @Override
        @Nullable
        public VibrationEffect cropToLengthOrNull(int length) {
            // drop repeating effects
            if (mRepeatIndex >= 0) {
                return null;
            }

            int segmentCount = mSegments.size();
            if (segmentCount <= length) {
                return this;
            }

            ArrayList truncated = new ArrayList(mSegments.subList(0, length));
            Composed updated = new Composed(truncated, mRepeatIndex);
            try {
                updated.validate();
            } catch (IllegalArgumentException e) {
                return null;
            }
            return updated;
        }

        @Override
        public long getDuration() {
            return getDuration(DEFAULT_DURATION_FN);
        }

        @Hide
        @Override
        public long getDuration(@Nullable VibratorInfo vibratorInfo) {
            return getDuration(segment -> segment.getDuration(vibratorInfo));
        }

        /**
         * Returns an estimate of the duration of the effect in milliseconds.
         *
         * <p>This estimate is based on typical hardware durations for predefined and primitive
         * effects.
         *
         * @return The estimated duration in milliseconds.
         */
        @Hide
        public long getEstimatedDuration() {
            if (isTimelineAnchored() && Flags.compositionApi()) {
                return getTimelineAnchoredDuration(
                        mSegments, VibrationEffect::getEstimatedDurationForSegment);
            }
            if (isLegacyPrimitiveComposition()) {
                return getLegacyPrimitiveCompositionDuration(
                        VibrationEffect::getEstimatedDurationForSegment);
            }
            return calculateTotalDuration(mSegments,
                    VibrationEffect::getEstimatedDurationForSegment);
        }

        /**
         * Calculates the total duration of a list of segments.
         *
         * <p>Note that this method does not account for the start time of the segments. It assumes
         * they are played sequentially and simply aggregates their durations, with special handling
         * for primitive delays.
         */
        private static long calculateTotalDuration(
                List<VibrationEffectSegment> segments,
                Function<VibrationEffectSegment, Long> durationFn) {
            long totalDuration = 0;
            long lastSegmentDuration = 0;
            for (int i = 0; i < segments.size(); i++) {
                VibrationEffectSegment segment = segments.get(i);
                long segmentDuration = durationFn.apply(segment);
                if (segmentDuration < 0) {
                    return segmentDuration;
                }
                totalDuration += segmentDuration;
                if (segment instanceof PrimitiveSegment primitiveSegment) {
                    if (primitiveSegment.getDelayType()
                            == Composition.DELAY_TYPE_RELATIVE_START_OFFSET) {
                        totalDuration += primitiveSegment.getDelay() - lastSegmentDuration;
                    } else if (primitiveSegment.getDelayType()
                            == Composition.DELAY_TYPE_PAUSE) {
                        totalDuration += primitiveSegment.getDelay();
                    }
                }
                lastSegmentDuration = segmentDuration;
            }
            return totalDuration;
        }

        private long getDuration(Function<VibrationEffectSegment, Long> durationFn) {
            if (mRepeatIndex >= 0) {
                return Long.MAX_VALUE;
            }
            long cachedDuration = mCachedDuration;
            // Cache is only valid for the default duration function as it is stateless
            // and Composed effects are immutable.
            if (durationFn == DEFAULT_DURATION_FN && cachedDuration != 0) {
                return cachedDuration;
            }
            long duration;
            if (isTimelineAnchored() && Flags.compositionApi()) {
                duration = getTimelineAnchoredDuration(mSegments, durationFn);
            } else if (isLegacyPrimitiveComposition()) {
                duration = getLegacyPrimitiveCompositionDuration(durationFn);
            } else {
                duration = calculateTotalDuration(mSegments, durationFn);
            }
            if (durationFn == DEFAULT_DURATION_FN) {
                mCachedDuration = duration;
            }
            return duration;
        }

        private static long getTimelineAnchoredDuration(
                List<VibrationEffectSegment> segments,
                Function<VibrationEffectSegment, Long> durationFn) {
            if (segments.isEmpty()) {
                return 0;
            }
            int lastStartIdx = 0;
            long lastEventStartTime = 0;
            for (int i = segments.size() - 1; i >= 0; i--) {
                long currentStartTime = segments.get(i).getStartTimeMillis();
                if (currentStartTime >= 0) {
                    lastStartIdx = i;
                    lastEventStartTime = currentStartTime;
                    break;
                }
            }
            long lastEventDuration =
                    calculateTotalDuration(
                            segments.subList(lastStartIdx, segments.size()),
                            durationFn);
            if (lastEventDuration < 0) {
                return -1;
            }
            return lastEventStartTime + lastEventDuration;
        }

        // Only used for legacy primitive compositions, i.e. when isLegacyPrimitiveComposition() is
        // true.
        private long getLegacyPrimitiveCompositionDuration(
                Function<VibrationEffectSegment, Long> durationFn) {
            long previousSegmentStartTime = 0;
            long previousSegmentEndTime = 0;
            for (VibrationEffectSegment segment : mSegments) {
                if (segment instanceof PrimitiveSegment primitiveSegment) {
                    long currentSegmentStartTime, currentSegmentEndTime;
                    long primitiveDuration = durationFn.apply(segment);
                    if (primitiveDuration < 0) {
                        return -1;
                    }
                    if (primitiveSegment.getDelayType()
                            == VibrationEffect.Composition.DELAY_TYPE_RELATIVE_START_OFFSET) {
                        currentSegmentStartTime =
                                previousSegmentStartTime + primitiveSegment.getDelay();
                    } else { // DELAY_TYPE_PAUSE
                        currentSegmentStartTime =
                                previousSegmentEndTime + primitiveSegment.getDelay();
                    }
                    currentSegmentEndTime = currentSegmentStartTime + primitiveDuration;
                    previousSegmentStartTime = currentSegmentStartTime;
                    previousSegmentEndTime = currentSegmentEndTime;
                }
            }
            return previousSegmentEndTime;
        }

        @Hide
        @Override
        public boolean areVibrationFeaturesSupported(@NonNull VibratorInfo vibratorInfo) {
            for (VibrationEffectSegment segment : mSegments) {
                if (!segment.areVibrationFeaturesSupported(vibratorInfo)) {
                    return false;
                }
            }
            return true;
        }

        @Hide
        @Override
        public boolean isHapticFeedbackCandidate() {
            long totalDuration = getDuration();
            if (totalDuration > MAX_HAPTIC_FEEDBACK_DURATION) {
                // Vibration duration is known and is longer than the max duration used to classify
                // haptic feedbacks (or repeating indefinitely with duration == Long.MAX_VALUE).
                return false;
            }
            int segmentCount = mSegments.size();
            if (segmentCount > MAX_HAPTIC_FEEDBACK_COMPOSITION_SIZE) {
                // Vibration has some predefined or primitive constants, it should be limited to the
                // max composition size used to classify haptic feedbacks.
                return false;
            }
            totalDuration = 0;
            for (int i = 0; i < segmentCount; i++) {
                if (!mSegments.get(i).isHapticFeedbackCandidate()) {
                    // There is at least one segment that is not a candidate for a haptic feedback.
                    return false;
                }
                long segmentDuration = mSegments.get(i).getDuration();
                if (segmentDuration > 0) {
                    totalDuration += segmentDuration;
                }
            }
            // Vibration might still have some ramp or step segments, check the known duration.
            return totalDuration <= MAX_HAPTIC_FEEDBACK_DURATION;
        }

        @Hide
        @NonNull
        @Override
        public Composed resolve(int defaultAmplitude) {
            return applyToSegments(VibrationEffectSegment::resolve, defaultAmplitude);
        }

        @Hide
        @NonNull
        @Override
        public VibrationEffect applyEffectStrength(int effectStrength) {
            return applyToSegments(VibrationEffectSegment::applyEffectStrength, effectStrength);
        }

        @Hide
        @NonNull
        @Override
        public Composed scale(float scaleFactor) {
            return applyToSegments(VibrationEffectSegment::scale, scaleFactor);
        }

        @Hide
        @NonNull
        @Override
        public Composed applyAdaptiveScale(float scaleFactor) {
            return applyToSegments(VibrationEffectSegment::applyAdaptiveScale, scaleFactor);
        }

        @Hide
        @NonNull
        @Override
        public Composed applyRepeatingIndefinitely(boolean wantRepeating, int loopDelayMs) {
            boolean isRepeating = mRepeatIndex >= 0;
            if (isRepeating == wantRepeating) {
                return this;
            } else if (!wantRepeating) {
                return new Composed(mSegments, -1);
            } else if (loopDelayMs <= 0) {
                // Loop with no delay: repeat at index zero.
                long repeatingDurationMillis = (Flags.compositionApi() && isTimelineAnchored())
                        ? getEstimatedDuration() : 0;
                return new Composed(mSegments, 0, repeatingDurationMillis);
            } else {
                // Append a delay and loop. It doesn't matter that there's a delay on the
                // end because the looping is always indefinite until cancelled.
                ArrayList<VibrationEffectSegment> loopingSegments =
                        new ArrayList<>(mSegments.size() + 1);
                loopingSegments.addAll(mSegments);
                loopingSegments.add(new StepSegment(/* amplitude= */ 0, loopDelayMs));
                // The repeating part is the original effect plus the delay.
                // We can estimate the duration by adding the delay to the original duration.
                long repeatingDurationMillis = 0;
                if (Flags.compositionApi() && isTimelineAnchored()) {
                    long originalDuration = getEstimatedDuration();
                    repeatingDurationMillis = originalDuration > 0
                            ? originalDuration + loopDelayMs : 0;
                }
                return new Composed(loopingSegments, 0, repeatingDurationMillis);
            }
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Composed other)) {
                return false;
            }
            return mSegments.equals(other.mSegments) && mRepeatIndex == other.mRepeatIndex
                    && mRepeatingDurationMillis == other.mRepeatingDurationMillis;
        }

        @Override
        public int hashCode() {
            return Objects.hash(mSegments, mRepeatIndex, mRepeatingDurationMillis);
        }

        @Override
        public String toString() {
            return "Composed{segments=" + mSegments
                    + ", repeat=" + mRepeatIndex
                    + ((mRepeatIndex < 0 || mRepeatingDurationMillis <= 0)
                            ? ""
                            : ", repeatingDuration=" + mRepeatingDurationMillis)
                    + "}";
        }

        @Hide
        @Override
        public String toDebugString() {
            if (mSegments.size() == 1 && mRepeatIndex < 0) {
                // Simplify effect string, use the single segment to represent it.
                return mSegments.get(0).toDebugString();
            }
            StringJoiner sj = new StringJoiner(",", "[", "]");
            for (int i = 0; i < mSegments.size(); i++) {
                sj.add(mSegments.get(i).toDebugString());
            }
            if (mRepeatIndex >= 0) {
                if (mRepeatingDurationMillis > 0) {
                    return TextUtils.formatSimple("%s, repeat=%d, repeatingDuration=%d",
                            sj.toString(), mRepeatIndex, mRepeatingDurationMillis);
                }
                return TextUtils.formatSimple("%s, repeat=%d", sj.toString(), mRepeatIndex);
            }
            return sj.toString();
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            out.writeInt(PARCEL_TOKEN_COMPOSED);
            out.writeList(mSegments);
            out.writeInt(mRepeatIndex);
            out.writeLong(mRepeatingDurationMillis);
        }

        @NonNull
        public static final Creator<Composed> CREATOR =
                new Creator<Composed>() {
                    @Override
                    public Composed createFromParcel(Parcel in) {
                        in.readInt(); // Skip the parcel type token
                        return new Composed(in);
                    }

                    @Override
                    public Composed[] newArray(int size) {
                        return new Composed[size];
                    }
                };

        /**
         * Casts a provided {@link VibrationEffectSegment} to a {@link StepSegment} and returns it,
         * only if it can possibly be a segment for an effect created via
         * {@link #createWaveform(long[], int)}. Otherwise, returns {@code null}.
         */
        @Nullable
        private static StepSegment castToValidStepSegmentForOffOnTimingsOrNull(
                VibrationEffectSegment segment) {
            if (!(segment instanceof StepSegment)) {
                return null;
            }

            StepSegment stepSegment = (StepSegment) segment;

            float amplitude = stepSegment.getAmplitude();
            if (amplitude != 0 && amplitude != DEFAULT_AMPLITUDE) {
                return null;
            }

            return stepSegment;
        }

        private <T> Composed applyToSegments(
                BiFunction<VibrationEffectSegment, T, VibrationEffectSegment> function, T param) {
            int segmentCount = mSegments.size();
            ArrayList<VibrationEffectSegment> updatedSegments = new ArrayList<>(segmentCount);
            for (int i = 0; i < segmentCount; i++) {
                updatedSegments.add(function.apply(mSegments.get(i), param));
            }
            if (mSegments.equals(updatedSegments)) {
                return this;
            }
            Composed updated =
                    new Composed(updatedSegments, mRepeatIndex, mRepeatingDurationMillis);
            updated.validate();
            return updated;
        }
    }

    /**
     * A builder for composing haptic effects by sequencing various haptic elements.
     *
     * <p>This API is the preferred alternative to {@link android.os.VibrationEffect.Composition}
     * for creating complex vibration effects. It offers more flexibility for creating haptic
     * patterns by allowing multiple elements to be scheduled at specific points in a timeline.
     *
     * <p>This builder allows you to add {@link Envelope}s, {@link Preset}s, and existing {@link
     * VibrationEffect}s (as {@link Event}s) to create a complex waveform. Each element is added
     * with a specified {@code startTimeMillis}. Note that the use of legacy {@link
     * VibrationEffect}s is not recommended here.
     *
     * <p>Additionally, a repeating part can be set using {@link #setRepeatingEffect(long,
     * VibrationEffect, long)}. Once a repeating effect is set, no more haptic elements can be added
     * to the composition.
     *
     * <p><b>Fallback Support:</b> Vibrations created by this builder will provide fallback support
     * by default if the device does not support the requested haptic elements. If the {@link
     * VibrationAttributes#FLAG_NO_FALLBACK_FOR_TIMELINED_COMPOSITION} flag is set in the attributes
     * when playing the vibration, no fallback will be provided.
     *
     * <p>For more details about the background of haptics implementation and how OEMs support
     * these effects, see the <a href="https://source.android.com/docs/core/interaction/haptics/haptics-implement">
     * Android Haptics Implementation Guidelines</a>.
     *
     * <p><b>Timing and Overlap:</b> The {@code startTimeMillis} is the time in milliseconds from
     * the start of the composition being built. Elements must be added in increasing order of their
     * start times.
     *
     * <p>The builder performs <b>best-effort validation</b> at build time to prevent elements from
     * overlapping. This validation uses the known durations of segments (like those in an {@link
     * Envelope}) or a minimum non-zero duration for elements with device-dependent durations, such
     * as {@link Preset}s. If an overlap is detected based on these durations, an {@link
     * IllegalArgumentException} will be thrown.
     *
     * <p>Because the actual duration of some elements (especially {@link Preset}s) is only fully
     * known at runtime on a specific device, the build-time check cannot guarantee the absence of
     * overlaps in all cases. Developers should be mindful of this when sequencing elements with
     * variable durations close together.
     *
     * <p>If an overlap occurs at playback time, despite the build-time checks, the framework
     * resolves the conflict using the following rules:
     *
     * <ol>
     *   <li>Priority is given to original, non-fallback effects over those that use a fallback.
     *   <li>When overlapping effects have the same priority level, the effect that started earlier
     *       takes precedence. The conflicting portion of the later effect will be truncated or
     *       dropped to prevent the overlap.
     * </ol>
     */
    // TODO(b/469962388): Refine the definitions of fallbacks and priority levels when support for
    // fallbacks is added.
    @FlaggedApi(Flags.FLAG_COMPOSITION_API)
    public static final class Builder {
        private final ArrayList<VibrationEffectSegment> mSegments = new ArrayList<>();
        private long mLastEndTime = 0;
        private int mRepeatIndex = -1;
        private long mRepeatingDurationMillis = 0;

        /**
         * Creates an empty {@link Builder} to compose a new {@link VibrationEffect}.
         */
        public Builder() {}

        /**
         * Creates a {@link Builder} initialized with a copy of an existing {@link VibrationEffect}.
         *
         * <p>Initializes the builder by copying the sequence of events from the provided
         * {@code effect}.
         *
         * @param effect The {@link VibrationEffect} to initialize this builder with.
         * @throws NullPointerException if the provided {@code effect} is null.
         * @throws IllegalStateException if the {@code effect} is repeating.
         */
        public Builder(@NonNull VibrationEffect effect) {
            Objects.requireNonNull(effect);
            addEvents(0, effect.getEvents());
        }

        /**
         * Adds a list of existing {@link Event}s to the composition.
         *
         * <p>Each event in the list has a start time relative to the start of the composition it
         * was retrieved from. The {@code startTimeShiftMillis} parameter
         * is added to the start time of each event in the list, effectively
         * shifting the whole block of events forward or backward in the timeline of
         * this builder.
         *
         * <p>For example, if an event within the list has a start time of 50ms, and
         * {@code startTimeShiftMillis} is 100ms, the event will be shifted to start
         * at 150ms within this builder's timeline.
         *
         * <p>All events will be added and validated according to the builder's timing rules,
         * with best-effort checks to prevent overlaps being introduced by the shift.
         *
         * <p>You can get events by calling {@link VibrationEffect#getEvents()} on non-repeating
         * effects.
         *
         * @param startTimeShiftMillis The non-negative time in milliseconds to add to the start
         *                             time of each event in the provided list.
         * @param events The list of events to add.
         * @return This {@link Builder} object.
         * @throws NullPointerException if the provided {@code events} list is null.
         * @throws IllegalArgumentException if {@code startTimeShiftMillis} is negative or if
         *                                  adding any of the events would result in overlapping
         *                                  haptic elements.
         */
        @NonNull
        public Builder addEvents(
                @DurationMillisLong long startTimeShiftMillis,
                @NonNull List<Event> events) {
            Objects.requireNonNull(events);
            Preconditions.checkArgument(startTimeShiftMillis >= 0,
                    "startTimeShiftMillis must be non-negative");
            checkNotRepeating();
            for (int i = 0; i < events.size(); i++) {
                Event event = events.get(i);
                VibrationEffect effect = event.getEffect();
                long eventStartTime = startTimeShiftMillis + event.getStartTimeMillis();
                if (effect instanceof Composed composed) {
                    List<VibrationEffectSegment> segments = composed.getSegments();
                    for (int j = 0; j < segments.size(); j++) {
                        VibrationEffectSegment segment = segments.get(j);
                        // Apply start time to the first segment of the event
                        if (j == 0) {
                            segment = segment.applyStartTime(eventStartTime);
                        } else {
                            // Subsequent segments follow immediately after the previous one
                            segment = segment.applyStartTime(-1);
                        }
                        addSegment(segment);
                    }
                }
            }
            return this;
        }

        /**
         * Adds a predefined haptic {@link Preset} to the composition.
         *
         * <p>The {@code startTimeMillis} is the time in milliseconds from the
         * start of the composition being built. The preset will be scheduled to start
         * at this time.
         *
         * <p>The builder performs best-effort validation to prevent overlaps with
         * previously added elements. However, the exact duration of a {@link Preset}
         * can be device-dependent. It is the caller's responsibility to choose an
         * appropriate {@code startTimeMillis} to avoid unintended overlaps.
         *
         * @param startTimeMillis The time in milliseconds from the start of the composition
         *                        to start the preset.
         * @param preset The {@link Preset} to add.
         * @return This {@link Builder} object.
         * @throws NullPointerException if the provided {@code preset} is null.
         * @throws IllegalArgumentException if adding the preset would result in overlapping haptic
         *                                  elements.
         */
        @NonNull
        @SuppressWarnings("MissingGetterMatchingBuilder")
        public Builder addPreset(@DurationMillisLong long startTimeMillis,
                @NonNull Preset preset) {
            Objects.requireNonNull(preset);
            checkNotRepeating();
            PresetSegment segment = new PresetSegment(preset.getId(), preset.getScale(),
                    startTimeMillis);
            segment.validate();
            addSegment(segment);
            return this;
        }

        /**
         * Adds a haptic {@link Envelope} to the composition.
         *
         * <p>The {@code startTimeMillis} is the time in milliseconds from the
         * start of the composition being built. The envelope will be scheduled to start
         * at this time.
         *
         * <p>The builder performs best-effort validation to prevent overlaps with
         * previously added elements. The duration of an {@link Envelope} is generally
         * well-defined, but care should still be taken with the {@code startTimeMillis}
         * to ensure correct sequencing with other elements.
         *
         * @param startTimeMillis The time in milliseconds from the start of the composition
         *                        to start the envelope.
         * @param envelope The {@link Envelope} to add.
         * @return This {@link Builder} object.
         * @throws NullPointerException if the provided {@code envelope} is null.
         * @throws IllegalArgumentException if adding the envelope would result in overlapping
         *                                  haptic elements.
         */
        @NonNull
        @SuppressWarnings("MissingGetterMatchingBuilder")
        public Builder addEnvelope(@DurationMillisLong long startTimeMillis,
                @NonNull Envelope envelope) {
            Objects.requireNonNull(envelope);
            checkNotRepeating();
            VibrationEffect effect = envelope.mEffect;
            if (effect instanceof Composed composed) {
                List<VibrationEffectSegment> segments = composed.getSegments();
                for (int i = 0; i < segments.size(); i++) {
                    VibrationEffectSegment segment = segments.get(i);
                    if (i == 0) {
                        segment = segment.applyStartTime(startTimeMillis);
                    } else {
                        segment = segment.applyStartTime(-1);
                    }
                    addSegment(segment);
                }
            }
            return this;
        }

        /**
         * Adds a repeating haptic effect to the composition.
         *
         * <p>The {@code startTimeMillis} is the time in milliseconds from the start of the
         * composition being built. The repeating effect will be scheduled to start at this time.
         *
         * <p>The {@code durationMillis} specifies the total length of one repeat cycle. If the
         * provided {@code effect} has a shorter natural duration (the time from the start of the
         * first segment to the end of the last segment), a pause (silence) will be added at the end
         * of each cycle to fill the remaining time. For example, if the {@code effect} lasts 150ms
         * and
         * {@code durationMillis} is 1000ms, then the effect will play once and be followed by an
         * 850ms pause before repeating again.
         *
         * <p>After this method is called, no more haptic elements can be added to the composition.
         *
         * @param startTimeMillis The non-negative time in milliseconds from the start of the
         *     composition to start the repeating effect.
         * @param effect The non-repeating {@link VibrationEffect} to be repeated.
         * @param durationMillis The positive total duration of each repeat cycle, including any
         *     trailing pause. Must be greater than or equal to the duration of the
         *     effect if the duration is known; otherwise, a best-effort check based on
         *     the estimated duration is performed to prevent impossible values.
         * @return This {@link Builder} object.
         * @throws NullPointerException if the provided {@code effect} is null.
         * @throws IllegalStateException if a repeating effect has already been set, or if other
         *     elements are added after this call.
         * @throws IllegalArgumentException if the provided effect is already repeating, if {@code
         *     startTimeMillis} is negative, or if {@code durationMillis} is not positive.
         */
        @NonNull
        @FlaggedApi(Flags.FLAG_COMPOSITION_API)
        @SuppressWarnings("MissingGetterMatchingBuilder")
        public Builder setRepeatingEffect(
                @DurationMillisLong long startTimeMillis,
                @NonNull VibrationEffect effect,
                @IntRange(from = 1) @DurationMillisLong long durationMillis) {
            Objects.requireNonNull(effect);
            Preconditions.checkArgument(startTimeMillis >= 0,
                    "startTimeMillis must be non-negative");
            Preconditions.checkArgument(durationMillis > 0,
                    "durationMillis must be positive");
            checkNotRepeating();
            long estimatedDurationMillis =
                effect instanceof Composed composed
                    ? composed.getEstimatedDuration()
                    : effect.getDuration();
            Preconditions.checkArgument(durationMillis >= estimatedDurationMillis,
                    "durationMillis (%d) must be greater than or equal to the effect duration (%d)",
                    durationMillis, estimatedDurationMillis);
            int repeatIndex = mSegments.size();
            // Event must be non-repeating, otherwise getEvents() will throw an
            // IllegalStateException.
            List<Event> events = effect.getEvents();
            if (events.isEmpty()) {
                throw new IllegalArgumentException(
                        "Repeating effect must have at least one event.");
            }
            // Add a zero-amplitude waveform event at the beginning if the first event does not
            // start at zero to ensure that the repeating effect always starts with an event and the
            // repeatIndex's segment is always the start of the repeating effect.
            long firstEventStartTime = events.get(0).getStartTimeMillis();
            if (firstEventStartTime > 0) {
                VibrationEffect newRepeatingStart = VibrationEffect.createWaveform(
                        new long[] {firstEventStartTime}, new int[] {0}, -1);
                // This padding segment must also be shifted by startTimeMillis to align with the
                // rest of the repeating effect and avoid overlaps with any preamble already in the
                // builder. It won't overlap with the rest of the repeating effect because the first
                // event of the repeating effect's start time is as long as the padding duration.
                addEvents(startTimeMillis, newRepeatingStart.getEvents());
            }
            addEvents(startTimeMillis, events);
            mRepeatIndex = repeatIndex;
            mRepeatingDurationMillis = durationMillis;
            return this;
        }

        private void checkNotRepeating() {
            if (mRepeatIndex >= 0) {
                throw new IllegalStateException(
                        "No more haptic elements can be added after setRepeatingEffect.");
            }
        }

        private void addSegment(VibrationEffectSegment segment) {
            validateAndRecordSegmentTimeline(segment);
            mSegments.add(segment);
        }

        private void validateAndRecordSegmentTimeline(VibrationEffectSegment segment) {
            long startTime = segment.getStartTimeMillis();
            long duration = segment.getDuration();
            long effectiveDuration = duration >= 0 ? duration : 1; // Use 1ms if duration is unknown

            if (startTime >= 0) {
                if (startTime < mLastEndTime) {
                    throw new IllegalArgumentException(
                            "Vibration events must not overlap and be strictly increasing in time: "
                                    + startTime + " < " + mLastEndTime);
                }
                mLastEndTime = startTime + effectiveDuration;
            } else {
                // Relative timing
                mLastEndTime += effectiveDuration;
            }
        }

        /**
         * Builds the composed {@link VibrationEffect}.
         *
         * <p>This method finalizes the composition of all added elements (Envelopes, Presets,
         * Events) and returns a single {@link VibrationEffect} that can be played by the vibrator.
         *
         * <p>The {@link Builder} object is still valid after this call, so you can
         * continue adding more elements to it and generate more {@link VibrationEffect}s by
         * calling this method again.
         *
         * @return The resulting {@link VibrationEffect}.
         * @throws IllegalStateException if the composition is empty.
         * @throws IllegalArgumentException if the validation of the composed effect fails
         *                                  (e.g., due to timing issues, invalid haptic element
         *                                  parameters).
         */
        @NonNull
        public VibrationEffect build() {
            if (mSegments.isEmpty()) {
                throw new IllegalStateException("Composition must have at least one element.");
            }
            VibrationEffect effect =
                    new Composed(mSegments, mRepeatIndex, mRepeatingDurationMillis);
            effect.validate();
            return effect;
        }
    }

    /**
     * Implementation of {@link VibrationEffect} described by a generic {@link PersistableBundle}
     * defined by vendors.
     */
    @TestApi
    public static final class VendorEffect extends VibrationEffect {

        @Hide
        public static final int DEFAULT_STRENGTH = VibrationEffect.EFFECT_STRENGTH_MEDIUM;

        @Hide
        public static final float DEFAULT_SCALE = 1.0f;

        private final PersistableBundle mVendorData;
        private final int mEffectStrength;
        private final float mScale;
        private final float mAdaptiveScale;

        @Hide
        VendorEffect(@NonNull Parcel in) {
            this(Objects.requireNonNull(
                    in.readPersistableBundle(VibrationEffect.class.getClassLoader())),
                    in.readInt(), in.readFloat(), in.readFloat());
        }

        @Hide
        public VendorEffect(@NonNull PersistableBundle vendorData, int effectStrength,
                float scale, float adaptiveScale) {
            mVendorData = vendorData;
            mEffectStrength = effectStrength;
            mScale = scale;
            mAdaptiveScale = adaptiveScale;
        }

        @NonNull
        public PersistableBundle getVendorData() {
            return mVendorData;
        }

        public int getEffectStrength() {
            return mEffectStrength;
        }

        public float getScale() {
            return mScale;
        }

        public float getAdaptiveScale() {
            return mAdaptiveScale;
        }

        @NonNull
        @Override
        @FlaggedApi(Flags.FLAG_COMPOSITION_API)
        public List<Event> getEvents() {
            throw new UnsupportedOperationException(
                    "VendorEffect do not support being represented as a sequence of events.");
        }

        @Hide
        @Override
        @Nullable
        public long[] computeCreateWaveformOffOnTimingsOrNull() {
            return null;
        }

        @Hide
        @Override
        public void validate() {
            Preconditions.checkArgument(!mVendorData.isEmpty(),
                    "Vendor effect bundle must be non-empty");
        }

        @Hide
        @Override
        @Nullable
        public VibrationEffect cropToLengthOrNull(int length) {
            return null;
        }

        @Override
        public long getDuration() {
            return -1; // UNKNOWN
        }

        @Hide
        @Override
        public boolean areVibrationFeaturesSupported(@NonNull VibratorInfo vibratorInfo) {
            return vibratorInfo.hasCapability(IVibrator.CAP_PERFORM_VENDOR_EFFECTS);
        }

        @Hide
        @Override
        public boolean isHapticFeedbackCandidate() {
            return false;
        }

        @Hide
        @NonNull
        @Override
        public VendorEffect resolve(int defaultAmplitude) {
            return this;
        }

        @Hide
        @NonNull
        @Override
        public VibrationEffect applyEffectStrength(int effectStrength) {
            if (mEffectStrength == effectStrength) {
                return this;
            }
            VendorEffect updated = new VendorEffect(mVendorData, effectStrength, mScale,
                    mAdaptiveScale);
            updated.validate();
            return updated;
        }

        @Hide
        @NonNull
        @Override
        public VendorEffect scale(float scaleFactor) {
            if (Float.compare(mScale, scaleFactor) == 0) {
                return this;
            }
            VendorEffect updated = new VendorEffect(mVendorData, mEffectStrength, scaleFactor,
                    mAdaptiveScale);
            updated.validate();
            return updated;
        }

        @Hide
        @NonNull
        @Override
        public VibrationEffect applyAdaptiveScale(float scaleFactor) {
            if (Float.compare(mAdaptiveScale, scaleFactor) == 0) {
                return this;
            }
            VendorEffect updated = new VendorEffect(mVendorData, mEffectStrength, mScale,
                    scaleFactor);
            updated.validate();
            return updated;
        }

        @Hide
        @NonNull
        @Override
        public VendorEffect applyRepeatingIndefinitely(boolean wantRepeating, int loopDelayMs) {
            return this;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof VendorEffect other)) {
                return false;
            }
            return mEffectStrength == other.mEffectStrength
                    && (Float.compare(mScale, other.mScale) == 0)
                    && (Float.compare(mAdaptiveScale, other.mAdaptiveScale) == 0)
                    && isPersistableBundleEquals(mVendorData, other.mVendorData);
        }

        @Override
        public int hashCode() {
            // PersistableBundle does not implement hashCode, so use its size as a shortcut.
            return Objects.hash(mVendorData.size(), mEffectStrength, mScale, mAdaptiveScale);
        }

        @Override
        public String toString() {
            return String.format(Locale.ROOT,
                    "VendorEffect{vendorData=%s, strength=%s, scale=%.2f, adaptiveScale=%.2f}",
                    mVendorData, effectStrengthToString(mEffectStrength), mScale, mAdaptiveScale);
        }

        @Hide
        @Override
        public String toDebugString() {
            return String.format(Locale.ROOT,
                    "vendorEffect=%s, strength=%s, scale=%.2f, adaptiveScale=%.2f",
                    mVendorData.toShortString(), effectStrengthToString(mEffectStrength),
                    mScale, mAdaptiveScale);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(@NonNull Parcel out, int flags) {
            out.writeInt(PARCEL_TOKEN_VENDOR_EFFECT);
            out.writePersistableBundle(mVendorData);
            out.writeInt(mEffectStrength);
            out.writeFloat(mScale);
            out.writeFloat(mAdaptiveScale);
        }

        /**
         * Compares two {@link PersistableBundle} objects are equals.
         */
        private static boolean isPersistableBundleEquals(
                PersistableBundle first, PersistableBundle second) {
            if (first == second) {
                return true;
            }
            if (first == null || second == null || first.size() != second.size()) {
                return false;
            }
            for (String key : first.keySet()) {
                if (!isPersistableBundleSupportedValueEquals(first.get(key), second.get(key))) {
                    return false;
                }
            }
            return true;
        }

        /**
         * Compares two values which type is supported by {@link PersistableBundle}.
         *
         * <p>If the type isn't supported. The equality is done by {@link Object#equals(Object)}.
         */
        private static boolean isPersistableBundleSupportedValueEquals(
                Object first, Object second) {
            if (first == second) {
                return true;
            } else if (first == null || second == null
                    || !first.getClass().equals(second.getClass())) {
                return false;
            } else if (first instanceof PersistableBundle) {
                return isPersistableBundleEquals(
                        (PersistableBundle) first, (PersistableBundle) second);
            } else if (first instanceof int[]) {
                return Arrays.equals((int[]) first, (int[]) second);
            } else if (first instanceof long[]) {
                return Arrays.equals((long[]) first, (long[]) second);
            } else if (first instanceof double[]) {
                return Arrays.equals((double[]) first, (double[]) second);
            } else if (first instanceof boolean[]) {
                return Arrays.equals((boolean[]) first, (boolean[]) second);
            } else if (first instanceof String[]) {
                return Arrays.equals((String[]) first, (String[]) second);
            } else {
                return Objects.equals(first, second);
            }
        }

        @NonNull
        public static final Creator<VendorEffect> CREATOR =
                new Creator<VendorEffect>() {
                    @Override
                    public VendorEffect createFromParcel(Parcel in) {
                        in.readInt(); // Skip the parcel type token
                        return new VendorEffect(in);
                    }

                    @Override
                    public VendorEffect[] newArray(int size) {
                        return new VendorEffect[size];
                    }
                };
    }

    /**
     * Creates a new {@link VibrationEffect} that repeats the given effect indefinitely.
     *
     * <p>The input vibration must not be a repeating vibration. If it is, an
     * {@link IllegalArgumentException} will be thrown.
     *
     * @param effect The {@link VibrationEffect} that will be repeated.
     * @return A {@link VibrationEffect} that repeats the effect indefinitely.
     * @throws IllegalArgumentException if the effect is already a repeating vibration.
     */
    @FlaggedApi(Flags.FLAG_NORMALIZED_PWLE_EFFECTS)
    @NonNull
    public static VibrationEffect createRepeatingEffect(@NonNull VibrationEffect effect) {
        Preconditions.checkArgument(effect instanceof Composed, "Can't repeat a vendor effect.");
        Preconditions.checkArgument(effect.getDuration() < Long.MAX_VALUE,
                "Can't repeat an indefinitely repeating effect.");
        VibrationEffect repeating = new Composed(((Composed) effect).getSegments(), 0);
        repeating.validate();
        return repeating;
    }

    /**
     * Creates a new {@link VibrationEffect} by merging the preamble and repeating vibration effect.
     *
     * <p>Neither input vibration may already be repeating. An {@link IllegalArgumentException} will
     * be thrown if either input vibration is set to repeat indefinitely.
     *
     * <p>If this method is invoked with mixed effects (one timelined and one legacy), the legacy
     * effect will be converted into a timelined effect. When converting, the effect's duration is
     * estimated, but this estimation is best-effort and may lead to hardware-dependent timing
     * variations. In such cases, the {@link Builder#setRepeatingEffect(long, VibrationEffect,
     * long)} method is preferred as it allows for explicit control over the timing of the repeating
     * part.
     *
     * @param preamble The starting vibration effect, which must be finite.
     * @param repeatingEffect The vibration effect to be repeated indefinitely after the preamble.
     * @return A {@link VibrationEffect} that plays the preamble once followed by the
     *     `repeatingEffect` indefinitely.
     * @throws IllegalArgumentException if either preamble or repeatingEffect is already a repeating
     *     vibration.
     */
    @FlaggedApi(Flags.FLAG_NORMALIZED_PWLE_EFFECTS)
    @NonNull
    public static VibrationEffect createRepeatingEffect(
            @NonNull VibrationEffect preamble, @NonNull VibrationEffect repeatingEffect) {
        Preconditions.checkArgument(preamble instanceof Composed, "Can't repeat a vendor effect.");
        Preconditions.checkArgument(preamble.getDuration() < Long.MAX_VALUE,
                "Can't repeat an indefinitely repeating effect.");
        Preconditions.checkArgument(repeatingEffect instanceof Composed,
                "Can't repeat a vendor effect.");
        Preconditions.checkArgument(repeatingEffect.getDuration() < Long.MAX_VALUE,
                "Can't repeat an indefinitely repeating effect.");

        Composed p = (Composed) preamble;
        Composed r = (Composed) repeatingEffect;

        if (Flags.compositionApi() && (p.isTimelineAnchored() || r.isTimelineAnchored())) {
            // Convert legacy effects to timelined effects if needed.
            if (!p.isTimelineAnchored()) {
                p = (Composed) new Builder(p).build();
            }
            if (!r.isTimelineAnchored()) {
                r = (Composed) new Builder(r).build();
            }
            return new Builder(p)
                    .setRepeatingEffect(p.getEstimatedDuration(), r, r.getEstimatedDuration())
                    .build();
        }

        List<VibrationEffectSegment> segments =
                new ArrayList<>(((Composed) preamble).getSegments());
        int repeatIndex = segments.size();
        segments.addAll(((Composed) repeatingEffect).getSegments());
        VibrationEffect repeating = new Composed(segments, repeatIndex);
        repeating.validate();
        return repeating;
    }


    /**
     * A composition of haptic elements that are combined to be playable as a single
     * {@link VibrationEffect}.
     *
     * <p>The haptic primitives are available as {@code Composition.PRIMITIVE_*} constants and
     * can be added to a composition to create a custom vibration effect. Here is an example of an
     * effect that grows in intensity and then dies off, with a longer rising portion for emphasis
     * and an extra tick 100ms after:
     *
     * <pre>
     * {@code VibrationEffect effect = VibrationEffect.startComposition()
     *     .addPrimitive(VibrationEffect.Composition.PRIMITIVE_SLOW_RISE, 0.5f)
     *     .addPrimitive(VibrationEffect.Composition.PRIMITIVE_QUICK_FALL, 0.5f)
     *     .addPrimitive(VibrationEffect.Composition.PRIMITIVE_TICK, 1.0f, 100)
     *     .compose();}</pre>
     *
     * <p>When choosing to play a composed effect, you should check that individual components are
     * supported by the device by using {@link Vibrator#arePrimitivesSupported}.
     *
     * @see VibrationEffect#startComposition()
     */
    public static final class Composition {

        @Hide
        @IntDef(prefix = { "PRIMITIVE_" }, value = {
                PRIMITIVE_CLICK,
                PRIMITIVE_THUD,
                PRIMITIVE_SPIN,
                PRIMITIVE_QUICK_RISE,
                PRIMITIVE_SLOW_RISE,
                PRIMITIVE_QUICK_FALL,
                PRIMITIVE_TICK,
                PRIMITIVE_LOW_TICK,
        })
        @Retention(RetentionPolicy.SOURCE)
        public @interface PrimitiveType {
        }

        @Hide
        @IntDef(prefix = { "DELAY_TYPE_" }, value = {
                DELAY_TYPE_PAUSE,
                DELAY_TYPE_RELATIVE_START_OFFSET,
        })
        @Retention(RetentionPolicy.SOURCE)
        public @interface DelayType {
        }

        /**
         * No haptic effect. Used to generate extended delays between primitives.
         */
        @Hide
        // Internally this maps to the HAL constant CompositePrimitive::NOOP
        public static final int PRIMITIVE_NOOP = 0;
        /**
         * This effect should produce a sharp, crisp click sensation.
         */
        // Internally this maps to the HAL constant CompositePrimitive::CLICK
        public static final int PRIMITIVE_CLICK = 1;
        /**
         * A haptic effect that simulates downwards movement with gravity. Often
         * followed by extra energy of hitting and reverberation to augment
         * physicality.
         */
        // Internally this maps to the HAL constant CompositePrimitive::THUD
        public static final int PRIMITIVE_THUD = 2;
        /**
         * A haptic effect that simulates spinning momentum.
         */
        // Internally this maps to the HAL constant CompositePrimitive::SPIN
        public static final int PRIMITIVE_SPIN = 3;
        /**
         * A haptic effect that simulates quick upward movement against gravity.
         */
        // Internally this maps to the HAL constant CompositePrimitive::QUICK_RISE
        public static final int PRIMITIVE_QUICK_RISE = 4;
        /**
         * A haptic effect that simulates slow upward movement against gravity.
         */
        // Internally this maps to the HAL constant CompositePrimitive::SLOW_RISE
        public static final int PRIMITIVE_SLOW_RISE = 5;
        /**
         * A haptic effect that simulates quick downwards movement with gravity.
         */
        // Internally this maps to the HAL constant CompositePrimitive::QUICK_FALL
        public static final int PRIMITIVE_QUICK_FALL = 6;
        /**
         * This very short effect should produce a light crisp sensation intended
         * to be used repetitively for dynamic feedback.
         */
        // Internally this maps to the HAL constant CompositePrimitive::LIGHT_TICK
        public static final int PRIMITIVE_TICK = 7;
        /**
         * This very short low frequency effect should produce a light crisp sensation
         * intended to be used repetitively for dynamic feedback.
         */
        // Internally this maps to the HAL constant CompositePrimitive::LOW_TICK
        public static final int PRIMITIVE_LOW_TICK = 8;

        /**
         * The delay represents a pause in the composition between the end of the previous primitive
         * and the beginning of the next one.
         *
         * <p>The primitive will start after the requested pause after the last primitive ended.
         * The actual time the primitive will be played depends on the previous primitive's actual
         * duration on the device hardware. This enables the combination of primitives to create
         * more complex effects based on how close to each other they'll play. Here is an example:
         *
         * <pre>
         *     VibrationEffect popEffect = VibrationEffect.startComposition()
         *         .addPrimitive(PRIMITIVE_QUICK_RISE)
         *         .addPrimitive(PRIMITIVE_CLICK, 0.7, 50, DELAY_TYPE_PAUSE)
         *         .compose()
         * </pre>
         */
        public static final int DELAY_TYPE_PAUSE = 0;

        /**
         * The delay represents an offset before starting this primitive, relative to the start
         * time of the previous primitive in the composition.
         *
         * <p>The primitive will start at the requested fixed time after the last primitive started,
         * independently of that primitive's actual duration on the device hardware. This enables
         * precise timings of primitives within a composition, ensuring they'll be played at the
         * desired intervals. Here is an example:
         *
         * <pre>
         *     VibrationEffect.startComposition()
         *         .addPrimitive(PRIMITIVE_CLICK, 1.0)
         *         .addPrimitive(PRIMITIVE_TICK, 1.0, 20, DELAY_TYPE_RELATIVE_START_OFFSET)
         *         .addPrimitive(PRIMITIVE_THUD, 1.0, 80, DELAY_TYPE_RELATIVE_START_OFFSET)
         *         .compose()
         * </pre>
         *
         * Will be performed on the device as follows:
         *
         * <pre>
         *  0ms               20ms                     100ms
         *  PRIMITIVE_CLICK---PRIMITIVE_TICK-----------PRIMITIVE_THUD
         * </pre>
         *
         * <p>A primitive will be dropped from the composition if it overlaps with previous ones.
         */
        public static final int DELAY_TYPE_RELATIVE_START_OFFSET = 1;

        private final ArrayList<VibrationEffectSegment> mSegments = new ArrayList<>();
        private int mRepeatIndex = -1;

        Composition() {}

        /**
         * Add a haptic effect to the end of the current composition.
         *
         * <p>If this effect is repeating (e.g. created by {@link VibrationEffect#createWaveform}
         * with a non-negative repeat index, or created by another composition that has effects
         * repeating indefinitely), then no more effects or primitives will be accepted by this
         * composition after this method. Such effects should be cancelled via
         * {@link Vibrator#cancel()}.
         *
         * @param effect The effect to add to the end of this composition.
         * @return This {@link Composition} object to enable adding multiple elements in one chain.
         */
        @Hide
        @NonNull
        public Composition addEffect(@NonNull VibrationEffect effect) {
            return addSegments(effect);
        }

        /**
         * Add a haptic primitive to the end of the current composition.
         *
         * <p>Similar to {@link #addPrimitive(int, float, int)}, but with no delay and a
         * default scale applied.
         *
         * @param primitiveId The primitive to add
         * @return This {@link Composition} object to enable adding multiple elements in one chain.
         */
        @NonNull
        public Composition addPrimitive(@PrimitiveType int primitiveId) {
            return addPrimitive(primitiveId, PrimitiveSegment.DEFAULT_SCALE);
        }

        /**
         * Add a haptic primitive to the end of the current composition.
         *
         * <p>Similar to {@link #addPrimitive(int, float, int)}, but with no delay.
         *
         * @param primitiveId The primitive to add
         * @param scale The scale to apply to the intensity of the primitive.
         * @return This {@link Composition} object to enable adding multiple elements in one chain.
         */
        @NonNull
        public Composition addPrimitive(@PrimitiveType int primitiveId,
                @FloatRange(from = 0f, to = 1f) float scale) {
            return addPrimitive(primitiveId, scale, PrimitiveSegment.DEFAULT_DELAY_MILLIS);
        }

        /**
         * Add a haptic primitive to the end of the current composition.
         *
         * <p>Similar to {@link #addPrimitive(int, float, int, int)}, but default
         * delay type applied is {@link #DELAY_TYPE_PAUSE}.
         *
         * @param primitiveId The primitive to add
         * @param scale The scale to apply to the intensity of the primitive.
         * @param delay The amount of time in milliseconds to wait between the end of the last
         *              primitive and the beginning of this one (i.e. a pause in the composition).
         * @return This {@link Composition} object to enable adding multiple elements in one chain.
         */
        @NonNull
        public Composition addPrimitive(@PrimitiveType int primitiveId,
                @FloatRange(from = 0f, to = 1f) float scale, @IntRange(from = 0) int delay) {
            return addPrimitive(primitiveId, scale, delay, PrimitiveSegment.DEFAULT_DELAY_TYPE);
        }

        /**
         * Add a haptic primitive to the end of the current composition.
         *
         * @param primitiveId The primitive to add
         * @param scale The scale to apply to the intensity of the primitive.
         * @param delay The amount of time in milliseconds to wait before playing this primitive,
         *              as defined by the given {@code delayType}.
         * @param delayType The type of delay to be applied, e.g. a pause between last primitive and
         *                  this one or a start offset.
         * @return This {@link Composition} object to enable adding multiple elements in one chain.
         */
        @NonNull
        public Composition addPrimitive(@PrimitiveType int primitiveId,
                @FloatRange(from = 0f, to = 1f) float scale, @IntRange(from = 0) int delay,
                @DelayType int delayType) {
            PrimitiveSegment primitive = new PrimitiveSegment(primitiveId, scale, delay, delayType);
            primitive.validate();
            return addSegment(primitive);
        }

        private Composition addSegment(VibrationEffectSegment segment) {
            if (mRepeatIndex >= 0) {
                throw new IllegalStateException("Can't add effects after a repeating effect.");
            }
            mSegments.add(segment);
            return this;
        }

        private Composition addSegments(VibrationEffect effect) {
            if (mRepeatIndex >= 0) {
                throw new IllegalStateException("Can't add effects after a repeating effect.");
            }
            if (!(effect instanceof Composed composed)) {
                throw new IllegalArgumentException("Can't add vendor effects to composition.");
            }
            if (composed.getRepeatIndex() >= 0) {
                // Start repeating from the index relative to the composed waveform.
                mRepeatIndex = mSegments.size() + composed.getRepeatIndex();
            }
            mSegments.addAll(composed.getSegments());
            return this;
        }

        /**
         * Compose all of the added primitives together into a single {@link VibrationEffect}.
         *
         * <p>The {@link Composition} object is still valid after this call, so you can continue
         * adding more primitives to it and generating more {@link VibrationEffect}s by calling this
         * method again.
         *
         * @return The {@link VibrationEffect} resulting from the composition of the primitives.
         */
        @NonNull
        public VibrationEffect compose() {
            if (mSegments.isEmpty()) {
                throw new IllegalStateException(
                        "Composition must have at least one element to compose.");
            }
            VibrationEffect effect = new Composed(mSegments, mRepeatIndex);
            effect.validate();
            return effect;
        }

        /**
         * Convert the primitive ID to a human readable string for debugging.
         * @param id The ID to convert
         * @return The ID in a human readable format.
         */
        @Hide
        public static String primitiveToString(@PrimitiveType int id) {
            return switch (id) {
                case PRIMITIVE_NOOP -> "NOOP";
                case PRIMITIVE_CLICK -> "CLICK";
                case PRIMITIVE_THUD -> "THUD";
                case PRIMITIVE_SPIN -> "SPIN";
                case PRIMITIVE_QUICK_RISE -> "QUICK_RISE";
                case PRIMITIVE_SLOW_RISE -> "SLOW_RISE";
                case PRIMITIVE_QUICK_FALL -> "QUICK_FALL";
                case PRIMITIVE_TICK -> "TICK";
                case PRIMITIVE_LOW_TICK -> "LOW_TICK";
                default -> Integer.toString(id);
            };
        }

        /**
         * Convert the delay type to a human readable string for debugging.
         * @param type The delay type to convert
         * @return The delay type in a human readable format.
         */
        @Hide
        public static String delayTypeToString(@DelayType int type) {
            return switch (type) {
                case DELAY_TYPE_PAUSE -> "PAUSE";
                case DELAY_TYPE_RELATIVE_START_OFFSET -> "START_OFFSET";
                default -> Integer.toString(type);
            };
        }
    }

    /**
     * A builder for waveform effects described by its envelope.
     *
     * <p>Waveform effect envelopes are defined by one or more control points describing a target
     * vibration amplitude and frequency, and a duration to reach those targets. The vibrator
     * will perform smooth transitions between control points.
     *
     * <p>For example, the following code ramps a vibrator from off to full amplitude at 120Hz over
     * 100ms, holds that state for 200ms, and then ramps back down over 100ms:
     *
     * <pre>{@code
     * VibrationEffect effect = new VibrationEffect.WaveformEnvelopeBuilder()
     *     .addControlPoint(1.0f, 120f, 100)
     *     .addControlPoint(1.0f, 120f, 200)
     *     .addControlPoint(0.0f, 120f, 100)
     *     .build();
     * }</pre>
     *
     * <p>The builder automatically starts all effects at 0 amplitude.
     *
     * <p>It is crucial to ensure that the frequency range used in your effect is compatible with
     * the device's capabilities. The framework will not play effects containing frequencies that
     * fall outside the device's supported range. It will also not attempt to correct or modify
     * these frequencies.
     *
     * <p>Therefore, it is strongly recommended that you design your haptic effects with the
     * device's frequency profile in mind. You can obtain the supported frequency range and other
     * relevant frequency-related information by getting the
     * {@link android.os.vibrator.VibratorFrequencyProfile} using the
     * {@link Vibrator#getFrequencyProfile()} method.
     *
     * <p>In addition to these limitations, when designing vibration patterns, it is important to
     * consider the physical limitations of the vibration actuator. These limitations include
     * factors such as the maximum number of control points allowed in an envelope effect, the
     * minimum and maximum durations permitted for each control point, and the maximum overall
     * duration of the effect. If a pattern exceeds the maximum number of allowed control points,
     * the framework will automatically break down the effect to ensure it plays correctly.
     *
     * <p>You can use the following APIs to obtain these limits:
     * <ul>
     * <li>Maximum envelope control points: {@link VibratorEnvelopeEffectInfo#getMaxSize()}
     * <li>Minimum control point duration:
     * {@link VibratorEnvelopeEffectInfo#getMinControlPointDurationMillis()}
     * <li>Maximum control point duration:
     * {@link VibratorEnvelopeEffectInfo#getMaxControlPointDurationMillis()}
     * <li>Maximum total effect duration: {@link VibratorEnvelopeEffectInfo#getMaxDurationMillis()}
     * </ul>
     */
    @FlaggedApi(Flags.FLAG_NORMALIZED_PWLE_EFFECTS)
    public static final class WaveformEnvelopeBuilder {

        private ArrayList<PwleSegment> mSegments = new ArrayList<>();
        private float mLastAmplitude = 0f;
        private float mLastFrequencyHz = Float.NaN;

        public WaveformEnvelopeBuilder() {}

        /**
         * Sets the initial frequency for the waveform in Hertz.
         *
         * <p>The effect will start vibrating at this frequency when it transitions to the
         * amplitude and frequency defined by the first control point.
         *
         * <p>The frequency must be greater than zero and within the supported range. To determine
         * the supported range, use {@link Vibrator#getFrequencyProfile()}. Creating
         * effects using frequencies outside this range will result in the vibration not playing.
         *
         * @param initialFrequencyHz The starting frequency of the vibration, in Hz. Must be
         *                           greater than zero.
         */
        @FlaggedApi(Flags.FLAG_NORMALIZED_PWLE_EFFECTS)
        @SuppressWarnings("MissingGetterMatchingBuilder")// No getter to initial frequency once set.
        @NonNull
        public WaveformEnvelopeBuilder setInitialFrequencyHz(
                @FloatRange(from = 0) float initialFrequencyHz) {

            if (mSegments.isEmpty()) {
                mLastFrequencyHz = initialFrequencyHz;
            } else {
                PwleSegment firstSegment = mSegments.getFirst();
                mSegments.set(0, new PwleSegment(
                        firstSegment.getStartAmplitude(),
                        firstSegment.getEndAmplitude(),
                        initialFrequencyHz, // Update start frequency
                        firstSegment.getEndFrequencyHz(),
                        firstSegment.getDuration(),
                        firstSegment.getStartTimeMillis()));
            }

            return this;
        }

        /**
         * Adds a new control point to the end of this waveform envelope.
         *
         * <p>Amplitude defines the vibrator's strength at this frequency, ranging from 0 (off) to 1
         * (maximum achievable strength). This value scales linearly with output strength, not
         * perceived intensity. It's determined by the actuator response curve.
         *
         * <p>Frequency must be greater than zero and within the supported range. To determine
         * the supported range, use {@link Vibrator#getFrequencyProfile()}. Creating
         * effects using frequencies outside this range will result in the vibration not playing.
         *
         * <p>Time specifies the duration (in milliseconds) for the vibrator to smoothly transition
         * from the previous control point to this new one. It must be greater than zero. To
         * transition as quickly as possible, use
         * {@link VibratorEnvelopeEffectInfo#getMinControlPointDurationMillis()}.
         *
         * @param amplitude      The amplitude value between 0 and 1, inclusive. 0 represents the
         *                       vibrator being off, and 1 represents the maximum achievable
         *                       amplitude
         *                       at this frequency.
         * @param frequencyHz    The frequency in Hz, must be greater than zero.
         * @param durationMillis The transition time in milliseconds.
         */
        @FlaggedApi(Flags.FLAG_NORMALIZED_PWLE_EFFECTS)
        @SuppressWarnings("MissingGetterMatchingBuilder") // No getters to segments once created.
        @NonNull
        public WaveformEnvelopeBuilder addControlPoint(
                @FloatRange(from = 0, to = 1) float amplitude,
                @FloatRange(from = 0) float frequencyHz, @DurationMillisLong long durationMillis) {

            if (Float.isNaN(mLastFrequencyHz)) {
                mLastFrequencyHz = frequencyHz;
            }


            mSegments.add(
                    new PwleSegment(
                            mLastAmplitude,
                            amplitude,
                            mLastFrequencyHz,
                            frequencyHz,
                            durationMillis));

            mLastAmplitude = amplitude;
            mLastFrequencyHz = frequencyHz;

            return this;
        }

        /**
         * Build the waveform as a single {@link VibrationEffect}.
         *
         * <p>The {@link WaveformEnvelopeBuilder} object is still valid after this call, so you can
         * continue adding more primitives to it and generating more {@link VibrationEffect}s by
         * calling this method again.
         *
         * @return The {@link VibrationEffect} resulting from the list of control points.
         * @throws IllegalStateException if no control points were added to the builder.
         */
        @FlaggedApi(Flags.FLAG_NORMALIZED_PWLE_EFFECTS)
        @NonNull
        public VibrationEffect build() {
            if (mSegments.isEmpty()) {
                throw new IllegalStateException(
                        "WaveformEnvelopeBuilder must have at least one control point to build.");
            }
            VibrationEffect effect = new Composed(mSegments, /* repeatIndex= */ -1);
            effect.validate();
            return effect;
        }
    }

    /**
     * A builder for waveform effects defined by their envelope, designed to provide a consistent
     * haptic perception across devices with varying capabilities.
     *
     * <p>This builder simplifies the creation of waveform effects by automatically adapting them
     * to different devices based on their capabilities. Effects are defined by control points
     * specifying target vibration intensity and sharpness, along with durations to reach those
     * targets. The vibrator will smoothly transition between these control points.
     *
     * <p><b>Intensity:</b> Defines the overall strength of the vibration, ranging from
     * 0 (off) to 1 (maximum achievable strength). Higher values result in stronger
     * vibrations. Supported intensity values guarantee sensitivity levels (SL) above
     * 10 dB SL to ensure human perception.
     *
     * <p><b>Sharpness:</b> Defines the crispness of the vibration, ranging from 0 to 1.
     * Lower values produce smoother vibrations, while higher values create a sharper,
     * more snappy sensation. Sharpness is mapped to its equivalent frequency within
     * the device's supported frequency range.{@if (flag(Flags.FLAG_BEATING_EFFECT_API)) {
     * A value of {@link #RESONANT_FREQUENCY_SHARPNESS} is mapped to the device's resonant
     * frequency.}}
     *
     * <p>While this builder handles most of the adaptation logic, it does come with some
     * limitations:
     * <ul>
     *     <li>It may not use the full range of frequencies</li>
     *     <li>It's restricted to a frequency range that can generate output of at least 10 db
     *     SL</li>
     *     <li>Effects must end with a zero intensity control point. Failure to end at a zero
     *     intensity control point will result in an {@link IllegalStateException}.</li>
     * </ul>
     *
     * <p>The builder automatically starts all effects at 0 intensity.
     *
     * <p>To avoid these limitations and to have more control over the effects output, use
     * {@link WaveformEnvelopeBuilder}, where direct amplitude and frequency values can be used.
     *
     * <p>For optimal cross-device consistency, it's recommended to limit the number of control
     * points to a maximum of 16. However this is not mandatory, and if a pattern exceeds the
     * maximum number of allowed control points, the framework will automatically break down the
     * effect to ensure it plays correctly.
     *
     * <p>For example, the following code creates a vibration effect that ramps up the intensity
     * from a low-pitched to a high-pitched strong vibration over 500ms and then ramps it down to
     * 0 (off) over 100ms:
     *
     * <pre>{@code
     * VibrationEffect effect = new VibrationEffect.BasicEnvelopeBuilder()
     *     .setInitialSharpness(0.0f)
     *     .addControlPoint(1.0f, 1.0f, 500)
     *     .addControlPoint(0.0f, 1.0f, 100)
     *     .build();
     * }</pre>
     */
    @FlaggedApi(Flags.FLAG_NORMALIZED_PWLE_EFFECTS)
    public static final class BasicEnvelopeBuilder {

        private ArrayList<BasicPwleSegment> mSegments = new ArrayList<>();
        private float mLastIntensity = 0f;
        private float mLastSharpness = Float.NaN;

        public BasicEnvelopeBuilder() {}

        /**
         * Sets the initial sharpness for the basic envelope effect.
         *
         * <p>The effect will start vibrating at this sharpness when it transitions to the
         * intensity and sharpness defined by the first control point.
         *
         * <p> The sharpness defines the crispness of the vibration, ranging from 0 to 1. Lower
         * values translate to smoother vibrations, while higher values create a sharper more snappy
         * sensation. This value is mapped to the supported frequency range of the device.
         *
         * @param initialSharpness The starting sharpness of the vibration in the range of [0, 1].
         */
        @FlaggedApi(Flags.FLAG_NORMALIZED_PWLE_EFFECTS)
        @SuppressWarnings("MissingGetterMatchingBuilder")// No getter to initial sharpness once set.
        @NonNull
        public BasicEnvelopeBuilder setInitialSharpness(
                @FloatRange(from = 0, to = 1) float initialSharpness) {

            if (mSegments.isEmpty()) {
                mLastSharpness = initialSharpness;
            } else {
                BasicPwleSegment firstSegment = mSegments.getFirst();
                mSegments.set(0, new BasicPwleSegment(
                        firstSegment.getStartIntensity(),
                        firstSegment.getEndIntensity(),
                        initialSharpness, // Update start sharpness
                        firstSegment.getEndSharpness(),
                        firstSegment.getDuration(),
                        firstSegment.getStartTimeMillis()));
            }

            return this;
        }

        /**
         * Adds a new control point to the end of this waveform envelope.
         *
         * <p>Intensity defines the overall strength of the vibration, ranging from 0 (off) to 1
         * (maximum achievable strength). Higher values translate to stronger vibrations.
         *
         * <p>Sharpness defines the crispness of the vibration, ranging from 0 to 1. Lower
         * values translate to smoother vibrations, while higher values create a sharper more snappy
         * sensation. This value is mapped to the supported frequency range of the device.
         *
         * <p>Time specifies the duration (in milliseconds) for the vibrator to smoothly transition
         * from the previous control point to this new one. It must be greater than zero. To
         * transition as quickly as possible, use
         * {@link VibratorEnvelopeEffectInfo#getMinControlPointDurationMillis()}.
         *
         * @param intensity      The target vibration intensity, ranging from 0 (off) to 1 (maximum
         *                       strength).
         * @param sharpness      The target sharpness, ranging from 0 (smoothest) to 1 (sharpest).
         * @param durationMillis The transition time in milliseconds.
         */
        @FlaggedApi(Flags.FLAG_NORMALIZED_PWLE_EFFECTS)
        @SuppressWarnings("MissingGetterMatchingBuilder") // No getters to segments once created.
        @NonNull
        public BasicEnvelopeBuilder addControlPoint(
                @FloatRange(from = 0, to = 1) float intensity,
                @FloatRange(from = 0, to = 1) float sharpness,
                @DurationMillisLong long durationMillis) {

            if (Float.isNaN(mLastSharpness)) {
                mLastSharpness = sharpness;
            }

            mSegments.add(
                    new BasicPwleSegment(
                            mLastIntensity,
                            intensity,
                            mLastSharpness,
                            sharpness,
                            durationMillis));

            mLastIntensity = intensity;
            mLastSharpness = sharpness;

            return this;
        }

        /**
         * Build the waveform as a single {@link VibrationEffect}.
         *
         * <p>The {@link BasicEnvelopeBuilder} object is still valid after this call, so you can
         * continue adding more primitives to it and generating more {@link VibrationEffect}s by
         * calling this method again.
         *
         * @return The {@link VibrationEffect} resulting from the list of control points.
         * @throws IllegalStateException if the last control point does not end at zero intensity.
         */
        @FlaggedApi(Flags.FLAG_NORMALIZED_PWLE_EFFECTS)
        @NonNull
        public VibrationEffect build() {
            if (mSegments.isEmpty()) {
                throw new IllegalStateException(
                        "BasicEnvelopeBuilder must have at least one control point to build.");
            }
            if (mSegments.getLast().getEndIntensity() != 0) {
                throw new IllegalStateException(
                        "Basic envelope effects must end at a zero intensity control point.");
            }
            VibrationEffect effect = new Composed(mSegments, /* repeatIndex= */ -1);
            effect.validate();
            return effect;
        }

    }

    /**
     * Represents a single event in a vibration composition with a start time relative to the
     * start of the composition.
     *
     * <p>An event is a self-contained vibration component with a specific starting point.
     * Common examples include:
     * <ul>
     *     <li>A single {@link Preset} with a specific start time.</li>
     *     <li>A single {@link Envelope} with a specific start time.</li>
     *     <li>A legacy effect created via static {@link VibrationEffect} methods like
     *     {@link #createOneShot(long, int)}, {@link #createWaveform(long[], int[], int)},
     *     {@link #createPredefined(int)}, etc., with a specific start time.</li>
     * </ul>
     */
    @FlaggedApi(Flags.FLAG_COMPOSITION_API)
    public static final class Event {
        private final VibrationEffect mEffect;
        private final long mStartTimeMillis;

        Event(@NonNull VibrationEffect effect, long startTimeMillis) {
            mEffect = effect;
            mStartTimeMillis = startTimeMillis;
        }

        @TestApi
        public long getStartTimeMillis() {
            return mStartTimeMillis;
        }

        @TestApi
        @NonNull
        public VibrationEffect getEffect() {
            return mEffect;
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof Event other)) {
                return false;
            }
            return mStartTimeMillis == other.mStartTimeMillis
                    && Objects.equals(mEffect, other.mEffect);
        }

        @Override
        public int hashCode() {
            return Objects.hash(mEffect, mStartTimeMillis);
        }
    }

    /**
     * Represents a predefined haptics pattern, corresponding to
     * {@link Composition#PrimitiveType}.
     *
     * <p>These presets are building blocks that can be added to a {@link VibrationEffect.Builder}
     * to create more complex haptic compositions. They represent common, short haptic sensations.
     */
    @FlaggedApi(Flags.FLAG_COMPOSITION_API)
    public static final class Preset {
        // LINT.IfChange
        // Internally mapped to Composition.PRIMITIVE_*

        @Hide
        @IntDef(prefix = { "PRESET_" }, value = {
                PRESET_NOOP,
                PRESET_CLICK,
                PRESET_THUD,
                PRESET_SPIN,
                PRESET_QUICK_RISE,
                PRESET_SLOW_RISE,
                PRESET_QUICK_FALL,
                PRESET_TICK,
                PRESET_LOW_TICK,
        })
        @Retention(RetentionPolicy.SOURCE)
        public @interface PresetType {}
        // LINT.ThenChange(vibrator/PresetSegment.java)

        /**
         * A sharp, short pulse sensation. This is the unified identifier for a click effect.
         */
        // Internally this maps to the HAL constant CompositePrimitive::CLICK
        public static final int PRESET_CLICK = Composition.PRIMITIVE_CLICK;

        /**
         * A very short pulse sensation. This is the unified identifier for a tick effect.
         */
        // Internally this maps to the HAL constant CompositePrimitive::LIGHT_TICK
        public static final int PRESET_TICK = Composition.PRIMITIVE_TICK;

        /**
         * A very short, low-frequency pulse sensation. This is the unified identifier for a low
         * tick effect.
         */
        // Internally this maps to the HAL constant CompositePrimitive::LOW_TICK
        public static final int PRESET_LOW_TICK = Composition.PRIMITIVE_LOW_TICK;

        /**
         * No-op effect, mapped to {@link Composition#PRIMITIVE_NOOP} for unified use.
         */
        @Hide
        // Internally this maps to the HAL constant CompositePrimitive::NOOP
        public static final int PRESET_NOOP = Composition.PRIMITIVE_NOOP;

        /**
         * A thud sensation, mapped to {@link Composition#PRIMITIVE_THUD} for unified use.
         */
        @Hide
        // Internally this maps to the HAL constant CompositePrimitive::THUD
        public static final int PRESET_THUD = Composition.PRIMITIVE_THUD;

        /**
         * A spin sensation, mapped to {@link Composition#PRIMITIVE_SPIN} for unified use.
         */
        @Hide
        // Internally this maps to the HAL constant CompositePrimitive::SPIN
        public static final int PRESET_SPIN = Composition.PRIMITIVE_SPIN;

        /**
         * A quick rise sensation, mapped to {@link Composition#PRIMITIVE_QUICK_RISE} for unified
         * use.
         */
        @Hide
        // Internally this maps to the HAL constant CompositePrimitive::QUICK_RISE
        public static final int PRESET_QUICK_RISE = Composition.PRIMITIVE_QUICK_RISE;

        /**
         * A slow rise sensation, mapped to {@link Composition#PRIMITIVE_SLOW_RISE} for unified use.
         */
        @Hide
        // Internally this maps to the HAL constant CompositePrimitive::SLOW_RISE
        public static final int PRESET_SLOW_RISE = Composition.PRIMITIVE_SLOW_RISE;

        /**
         * A quick fall sensation, mapped to {@link Composition#PRIMITIVE_QUICK_FALL} for unified
         * use.
         */
        @Hide
        // Internally this maps to the HAL constant CompositePrimitive::QUICK_FALL
        public static final int PRESET_QUICK_FALL = Composition.PRIMITIVE_QUICK_FALL;

        private final int mId;
        private final float mScale;

        Preset(int id, float scale) {
            mId = id;
            mScale = scale;
        }

        /**
         * Creates a new haptic {@link Preset} with the specified ID.
         *
         * @param presetId The ID of the preset to create.
         * @return The created {@link Preset}.
         */
        @NonNull
        public static Preset create(@PresetType int presetId) {
            return create(presetId, 1.0f);
        }

        /**
         * Creates a new haptic {@link Preset} with the specified ID and scale.
         *
         * @param presetId The ID of the preset to create.
         * @param scale The scale to apply to the preset's intensity, ranging from 0 to 1.
         * @return The created {@link Preset}.
         * @throws IllegalArgumentException if the {@code scale} is not between 0 and 1.
         */
        @NonNull
        public static Preset create(@PresetType int presetId,
                @FloatRange(from = 0f, to = 1f) float scale) {
            Preconditions.checkArgument(scale >= 0f && scale <= 1f,
                    "scale must be between 0 and 1");
            return new Preset(presetId, scale);
        }

        @TestApi
        public int getId() {
            return mId;
        }

        @TestApi
        public float getScale() {
            return mScale;
        }

        /**
         * Returns true if the preset ID is a primitive that is supported by the vibrator.
         */
        @Hide
        public static boolean isPrimitive(@PresetType int id) {
            return switch (id) {
                case PRESET_NOOP,
                        PRESET_CLICK,
                        PRESET_THUD,
                        PRESET_SPIN,
                        PRESET_QUICK_RISE,
                        PRESET_SLOW_RISE,
                        PRESET_QUICK_FALL,
                        PRESET_TICK,
                        PRESET_LOW_TICK -> true;
                default -> false;
            };
        }

        /**
         * Convert the preset ID to a human readable string for debugging.
         * @param id The ID to convert
         * @return The ID in a human readable format.
         */
        @Hide
        public static String presetToString(@PresetType int id) {
            return switch (id) {
                case PRESET_NOOP -> "NOOP";
                case PRESET_CLICK -> "CLICK";
                case PRESET_THUD -> "THUD";
                case PRESET_SPIN -> "SPIN";
                case PRESET_QUICK_RISE -> "QUICK_RISE";
                case PRESET_SLOW_RISE -> "SLOW_RISE";
                case PRESET_QUICK_FALL -> "QUICK_FALL";
                case PRESET_TICK -> "TICK";
                case PRESET_LOW_TICK -> "LOW_TICK";
                default -> Integer.toString(id);
            };
        }
    }

    /**
     * Represents a haptic effect defined by an envelope, to be used within a
     * {@link VibrationEffect.Builder}.
     *
     * <p>This class encapsulates Piecewise Linear Envelope (PWLE) effects, which can be created
     * using either the {@link WaveformEnvelopeBuilder} for direct amplitude and frequency control,
     * or the {@link BasicEnvelopeBuilder} for a more perception-based approach using intensity
     * and sharpness.
     *
     * <p>Envelopes are fundamental building blocks in the composition API, allowing for expressive
     * and complex haptic effects by sequencing them with other elements like {@link Preset}s.
     */
    @FlaggedApi(Flags.FLAG_COMPOSITION_API)
    public static final class Envelope {
        private final VibrationEffect mEffect;

        Envelope(@NonNull VibrationEffect effect) {
            mEffect = effect;
        }

        /**
         * Creates a new haptic {@link Envelope} from the provided {@link BasicEnvelopeBuilder}.
         *
         * @param builder The builder used to define the envelope's basic parameters.
         * @return The created {@link Envelope}.
         */
        @NonNull
        public static Envelope create(@NonNull BasicEnvelopeBuilder builder) {
            return new Envelope(builder.build());
        }

        /**
         * Creates a new haptic {@link Envelope} from the provided {@link WaveformEnvelopeBuilder}.
         *
         * @param builder The builder used to define the envelope's waveform.
         * @return The created {@link Envelope}.
         */
        @NonNull
        public static Envelope create(@NonNull WaveformEnvelopeBuilder builder) {
            return new Envelope(builder.build());
        }

        /**
         * A builder to help users create rich and expressive beating vibrations, taking beats per
         * second, vibration sharpness, and an intensity envelope as input. Beats per second is the
         * frequency of the beats, i.e., the number of beats per second. The intensity envelope
         * defines the overall strength of the beats, which transitions from a starting intensity to
         * an ending intensity over the specified duration. The sharpness defines the crispness of
         * the vibration.
         *
         * <p><b>Intensity:</b> Defines the overall strength of the vibration, ranging from 0 (off)
         * to 1 (maximum achievable strength). The envelope always starts at 0 intensity, and must
         * end with 0 intensity.
         *
         * <p><b>DurationMillis:</b> Defines the transition time (in milliseconds) from the previous
         * control point to this new one. It must be strictly positive (greater than zero).
         *
         * <p><b>Sharpness:</b> Defines the crispness of the vibration, maps to the carrier
         * frequency of the vibration, ranging from 0 (smoothest) to 1 (sharpest), inclusive.
         * A value of {@link #RESONANT_FREQUENCY_SHARPNESS} is mapped to the device's resonant
         * frequency.
         *
         * <p><b>Beats Per Second:</b> The beating frequency in Hz, i.e., how many beats per second.
         * It must be greater than zero. It is recommended to keep this value within the range of
         * (0, 25] Hz. This ensures that the beating effect will have a more aligned and consistent
         * behavior across different Android devices with basic envelope support.
         *
         * <p>For example, the following code creates a beating vibration effect with a snappy
         * sharpness and 8 beats per second. The intensity ramps up to 0.8 over 500ms, stays at 0.8
         * for 500ms, and then ramps down to 0 (off) over 500ms:
         *
         * <pre>{@code
         * VibrationEffect.Envelope envelope =
         *     new VibrationEffect.Envelope.BeatingEnvelopeBuilder(/* sharpness= *\/ 0.8f,
         *             /* beatsPerSecond= *\/ 8.0f)
         *         .addControlPoint(0.8f, 500)
         *         .addControlPoint(0.8f, 500)
         *         .addControlPoint(0.0f, 500)
         *         .build();
         * VibrationEffect effect = new VibrationEffect.Builder()
         *     .addEnvelope(/* startTimeMillis= *\/ 0, envelope)
         *     .build();
         * }</pre>
         */
        @FlaggedApi(Flags.FLAG_BEATING_EFFECT_API)
        public static final class BeatingEnvelopeBuilder {

            public static final class ControlPoint {
                private final float mIntensity;
                private final long mDurationMillis;

                @Hide
                ControlPoint(float intensity, long durationMillis) {
                    mIntensity = intensity;
                    mDurationMillis = durationMillis;
                }

                @Hide
                public float getIntensity() {
                    return mIntensity;
                }

                @Hide
                public long getDurationMillis() {
                    return mDurationMillis;
                }
            }

            private final ArrayList<ControlPoint> mControlPoints = new ArrayList<>();
            private float mSharpness = Float.NaN;
            private float mBeatsPerSecond = Float.NaN;

            /**
             * Creates a new {@link BeatingEnvelopeBuilder} with the specified sharpness and beats
             * per second.
             *
             * @param sharpness The sharpness of the vibration, ranging from 0 (smoothest) to 1
             *     (sharpest).
             * @param beatsPerSecond The beating frequency in Hz. Must be strictly positive.
             */
            public BeatingEnvelopeBuilder(@FloatRange(from = 0f, to = 1f) float sharpness,
                    @FloatRange(from = 0f, fromInclusive = false) float beatsPerSecond) {
                Preconditions.checkArgumentInRange(sharpness, 0f, 1f,
                    "sharpness must be between 0 and 1 (inclusive)");
                Preconditions.checkArgumentPositive(beatsPerSecond, "beatsPerSecond must be > 0");
                mSharpness = sharpness;
                mBeatsPerSecond = beatsPerSecond;
            }

            /**
             * Adds a new control point to the end of this beating envelope.
             *
             * <p>Intensity defines the overall strength of the vibration, ranging from 0 (off) to 1
             * (maximum achievable strength).
             *
             * <p>Time specifies the duration (in milliseconds) for the vibrator to smoothly
             * transition from the previous control point to this new one. It must be strictly
             * positive (greater than zero).
             *
             * @param intensity The target vibration intensity, ranging from 0 (off) to 1 (maximum
             *     strength), inclusive.
             * @param durationMillis The transition time in milliseconds. Must be strictly positive.
             * @return This {@link BeatingEnvelopeBuilder} object.
             */
            @SuppressWarnings(
                    "MissingGetterMatchingBuilder") // No getters to segments once created.
            @NonNull
            public BeatingEnvelopeBuilder addControlPoint(
                    @FloatRange(from = 0f, to = 1f) float intensity,
                    @IntRange(from = 1) @DurationMillisLong long durationMillis) {
                Preconditions.checkArgumentInRange(intensity, 0f, 1f,
                    "intensity must be between 0 and 1 (inclusive)");
                Preconditions.checkArgumentPositive(durationMillis,
                    "durationMillis must be > 0");
                mControlPoints.add(new ControlPoint(intensity, durationMillis));
                return this;
            }

            /**
             * Build the beating envelope as a single {@link Envelope}.
             *
             * @return The {@link Envelope} resulting from the list of control points.
             * @throws IllegalStateException if no control points were added to the builder, or if
             *     the last control point does not end at zero intensity.
             */
            @NonNull
            public Envelope build() {
                if (mControlPoints.isEmpty()) {
                    throw new IllegalStateException(
                            "BeatingEnvelopeBuilder must have at least one control point "
                                    + "to build.");
                }
                if (mControlPoints.get(mControlPoints.size() - 1).getIntensity() != 0) {
                    throw new IllegalStateException(
                            "Beating envelope effects must end at a zero intensity control point.");
                }

                ArrayList<BeatingSegment> finalSegments = new ArrayList<>(mControlPoints.size());
                float startIntensity = 0f;
                for (int i = 0; i < mControlPoints.size(); i++) {
                    ControlPoint cp = mControlPoints.get(i);
                    finalSegments.add(
                            new BeatingSegment(
                                    startIntensity,
                                    cp.getIntensity(),
                                    cp.getDurationMillis(),
                                    mBeatsPerSecond,
                                    mSharpness));
                    startIntensity = cp.getIntensity();
                }

                VibrationEffect effect = new Composed(finalSegments, /* repeatIndex= */ -1);
                effect.validate();
                return new Envelope(effect);
            }
        }
    }

    @NonNull
    public static final Parcelable.Creator<VibrationEffect> CREATOR =
            new Parcelable.Creator<VibrationEffect>() {
                @Override
                public VibrationEffect createFromParcel(Parcel in) {
                    switch (in.readInt()) {
                        case PARCEL_TOKEN_COMPOSED:
                            return new Composed(in);
                        case PARCEL_TOKEN_VENDOR_EFFECT:
                            return new VendorEffect(in);
                        default:
                            throw new IllegalStateException(
                                    "Unexpected vibration effect type token in parcel.");
                    }
                }

                @Override
                public VibrationEffect[] newArray(int size) {
                    return new VibrationEffect[size];
                }
            };
}
