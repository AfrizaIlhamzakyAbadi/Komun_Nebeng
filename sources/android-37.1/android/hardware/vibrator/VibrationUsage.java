/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: aidl --lang=java -Weverything -Wno-missing-permission-annotation --structured --version 5 --hash notfrozen --stability vintf --min_sdk_version current -pout/soong/.intermediates/hardware/interfaces/common/aidl/android.hardware.common_interface/2/preprocessed.aidl -pout/soong/.intermediates/hardware/interfaces/common/fmq/aidl/android.hardware.common.fmq_interface/1/preprocessed.aidl -pout/soong/.intermediates/system/hardware/interfaces/media/android.media.audio.common.types_interface/5/preprocessed.aidl --previous_api_dir=hardware/interfaces/vibrator/aidl/aidl_api/android.hardware.vibrator/4 --previous_hash dec155403ea3aa5395b0226de399873712b16082 --ninja -d out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen/android/hardware/vibrator/VibrationUsage.java.d -o out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen -Iframeworks/native/aidl/binder -Nhardware/interfaces/vibrator/aidl hardware/interfaces/vibrator/aidl/android/hardware/vibrator/VibrationUsage.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.hardware.vibrator;
/**
 * Represents the intent or context of a vibration playback.
 * 
 * Mapped directly from standard android.os.VibrationAttributes. Usage constants defined in:
 * frameworks/base/core/java/android/os/VibrationAttributes.java
 * 
 * The enum values are derived via bitwise OR mapping of class and usage offsets:
 *   Usage = (Category Offset) | (Usage Class)
 * Refer to VibrationAttributes.java for constant values (e.g., USAGE_RINGTONE = 0x21).
 * 
 * Note on Design: The HAL API defines specific leaf usages (e.g., RINGTONE, TOUCH) rather than
 * broad classes (e.g., ALARM, FEEDBACK). The system framework uses the class to enforce global
 * policies (such as Do Not Disturb bypass), whereas the HAL driver only requires the specific
 * intent (Usage). If class-level categories are needed by the hardware/driver, they can be derived
 * implicitly in C++ by masking the usage value with 0x0F (e.g., static_cast<int>(usage) & 0x0F).
 */
public @interface VibrationUsage {
  public static final byte UNKNOWN = 0;
  public static final byte ALARM = 17;
  public static final byte RINGTONE = 33;
  public static final byte NOTIFICATION = 49;
  public static final byte COMMUNICATION_REQUEST = 65;
  public static final byte TOUCH = 18;
  public static final byte PHYSICAL_EMULATION = 34;
  public static final byte HARDWARE_FEEDBACK = 50;
  public static final byte ACCESSIBILITY = 66;
  public static final byte IME_FEEDBACK = 82;
  public static final byte GESTURE_INPUT = 98;
  public static final byte MEDIA = 19;
}
