/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: aidl --lang=java -Weverything -Wno-missing-permission-annotation --structured --version 5 --hash notfrozen --stability vintf --min_sdk_version current -pout/soong/.intermediates/hardware/interfaces/common/aidl/android.hardware.common_interface/2/preprocessed.aidl -pout/soong/.intermediates/hardware/interfaces/common/fmq/aidl/android.hardware.common.fmq_interface/1/preprocessed.aidl -pout/soong/.intermediates/system/hardware/interfaces/media/android.media.audio.common.types_interface/5/preprocessed.aidl --previous_api_dir=hardware/interfaces/vibrator/aidl/aidl_api/android.hardware.vibrator/4 --previous_hash dec155403ea3aa5395b0226de399873712b16082 --ninja -d out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen/android/hardware/vibrator/CompositePrimitive.java.d -o out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen -Iframeworks/native/aidl/binder -Nhardware/interfaces/vibrator/aidl hardware/interfaces/vibrator/aidl/android/hardware/vibrator/CompositePrimitive.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.hardware.vibrator;
public @interface CompositePrimitive {
  /**
   * No haptic effect. Used to generate extended delays between primitives.
   * 
   * Support is required.
   */
  public static final int NOOP = 0;
  /**
   * This effect should produce a sharp, crisp click sensation.
   * 
   * Support is required.
   */
  public static final int CLICK = 1;
  /**
   * A haptic effect that simulates downwards movement with gravity. Often
   * followed by extra energy of hitting and reverberation to augment
   * physicality.
   * 
   * Support is optional.
   */
  public static final int THUD = 2;
  /**
   * A haptic effect that simulates spinning momentum.
   * 
   * Support is optional.
   */
  public static final int SPIN = 3;
  /**
   * A haptic effect that simulates quick upward movement against gravity.
   * 
   * Support is required.
   */
  public static final int QUICK_RISE = 4;
  /**
   * A haptic effect that simulates slow upward movement against gravity.
   * 
   * Support is required.
   */
  public static final int SLOW_RISE = 5;
  /**
   * A haptic effect that simulates quick downwards movement with gravity.
   * 
   * Support is required.
   */
  public static final int QUICK_FALL = 6;
  /**
   * This very short effect should produce a light crisp sensation intended
   * to be used repetitively for dynamic feedback.
   * 
   * Support is required.
   */
  public static final int LIGHT_TICK = 7;
  /**
   * This very short low frequency effect should produce a light crisp sensation intended
   * to be used repetitively for dynamic feedback.
   * 
   * Support is required.
   */
  public static final int LOW_TICK = 8;
}
