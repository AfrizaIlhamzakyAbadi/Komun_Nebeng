/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: aidl --lang=java -Weverything -Wno-missing-permission-annotation --structured --version 5 --hash notfrozen --stability vintf --min_sdk_version current -pout/soong/.intermediates/hardware/interfaces/common/aidl/android.hardware.common_interface/2/preprocessed.aidl -pout/soong/.intermediates/hardware/interfaces/common/fmq/aidl/android.hardware.common.fmq_interface/1/preprocessed.aidl -pout/soong/.intermediates/system/hardware/interfaces/media/android.media.audio.common.types_interface/5/preprocessed.aidl --previous_api_dir=hardware/interfaces/vibrator/aidl/aidl_api/android.hardware.vibrator/4 --previous_hash dec155403ea3aa5395b0226de399873712b16082 --ninja -d out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen/android/hardware/vibrator/Effect.java.d -o out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen -Iframeworks/native/aidl/binder -Nhardware/interfaces/vibrator/aidl hardware/interfaces/vibrator/aidl/android/hardware/vibrator/Effect.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.hardware.vibrator;
public @interface Effect {
  /**
   * A single click effect.
   * 
   * This effect should produce a sharp, crisp click sensation.
   */
  public static final int CLICK = 0;
  /**
   * A double click effect.
   * 
   * This effect should produce two sequential sharp, crisp click sensations with a minimal
   * amount of time between them.
   */
  public static final int DOUBLE_CLICK = 1;
  /**
   * A tick effect.
   * 
   * This effect should produce a soft, short sensation, like the tick of a clock.
   */
  public static final int TICK = 2;
  /**
   * A thud effect.
   * 
   * This effect should solid feeling bump, like the depression of a heavy mechanical button.
   */
  public static final int THUD = 3;
  /**
   * A pop effect.
   * 
   * A short, quick burst effect.
   */
  public static final int POP = 4;
  /**
   * A heavy click effect.
   * 
   * This should produce a sharp striking sensation, like a click but stronger.
   */
  public static final int HEAVY_CLICK = 5;
  /**
   * Ringtone patterns. They may correspond with the device's ringtone audio, or may just be a
   * pattern that can be played as a ringtone with any audio, depending on the device.
   */
  public static final int RINGTONE_1 = 6;
  public static final int RINGTONE_2 = 7;
  public static final int RINGTONE_3 = 8;
  public static final int RINGTONE_4 = 9;
  public static final int RINGTONE_5 = 10;
  public static final int RINGTONE_6 = 11;
  public static final int RINGTONE_7 = 12;
  public static final int RINGTONE_8 = 13;
  public static final int RINGTONE_9 = 14;
  public static final int RINGTONE_10 = 15;
  public static final int RINGTONE_11 = 16;
  public static final int RINGTONE_12 = 17;
  public static final int RINGTONE_13 = 18;
  public static final int RINGTONE_14 = 19;
  public static final int RINGTONE_15 = 20;
  /**
   * A soft tick effect meant to be played as a texture.
   * 
   * A soft, short sensation like the tick of a clock. Unlike regular effects, texture effects
   * are expected to be played multiple times in quick succession, replicating a specific
   * texture to the user as a form of haptic feedback.
   */
  public static final int TEXTURE_TICK = 21;
}
