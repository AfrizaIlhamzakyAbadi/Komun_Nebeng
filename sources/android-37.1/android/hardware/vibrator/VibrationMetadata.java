/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: aidl --lang=java -Weverything -Wno-missing-permission-annotation --structured --version 5 --hash notfrozen --stability vintf --min_sdk_version current -pout/soong/.intermediates/hardware/interfaces/common/aidl/android.hardware.common_interface/2/preprocessed.aidl -pout/soong/.intermediates/hardware/interfaces/common/fmq/aidl/android.hardware.common.fmq_interface/1/preprocessed.aidl -pout/soong/.intermediates/system/hardware/interfaces/media/android.media.audio.common.types_interface/5/preprocessed.aidl --previous_api_dir=hardware/interfaces/vibrator/aidl/aidl_api/android.hardware.vibrator/4 --previous_hash dec155403ea3aa5395b0226de399873712b16082 --ninja -d out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen/android/hardware/vibrator/VibrationMetadata.java.d -o out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen -Iframeworks/native/aidl/binder -Nhardware/interfaces/vibrator/aidl hardware/interfaces/vibrator/aidl/android/hardware/vibrator/VibrationMetadata.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.hardware.vibrator;
public class VibrationMetadata implements android.os.Parcelable
{
  /**
   * Bitmask of target vibrator IDs to which this context applies.
   * 
   * Each bit index corresponds directly to the ID of the physical vibrator (i.e., the
   * values returned by IVibratorManager.getVibratorIds()).
   * 
   * Note: Targeted vibrator IDs must fall within the range [0, 31] due to the 32-bit bitmask
   * representation. A vibrator ID outside this range is not supported by the bitmask context.
   * 
   * Examples:
   * - Set to 0 to apply as a wildcard/global fallback context for ALL active vibrators.
   * - Set to (1 << 0) = 1 to target only the vibrator with ID 0.
   * - Set to (1 << 1) = 2 to target only the vibrator with ID 1.
   * - Set to (1 << 0) | (1 << 1) = 3 to target both vibrators with IDs 0 and 1.
   */
  public int vibratorMask = 0;
  /**
   * The high-level intent of the vibration, representing the application-level haptic context.
   * The integer value maps to standard android.os.VibrationAttributes.Usage constants.
   * 
   * Refer to:
   * - SDK Reference: https://developer.android.com/reference/android/os/VibrationAttributes
   * - AOSP Source: frameworks/base/core/java/android/os/VibrationAttributes.java
   */
  public byte usage;
  /** Extension point for OEM-specific parameters. */
  public final android.os.ParcelableHolder extension = new android.os.ParcelableHolder(android.os.Parcelable.PARCELABLE_STABILITY_VINTF);
  @Override
   public final int getStability() { return android.os.Parcelable.PARCELABLE_STABILITY_VINTF; }
  public static final android.os.Parcelable.Creator<VibrationMetadata> CREATOR = new android.os.Parcelable.Creator<VibrationMetadata>() {
    @Override
    public VibrationMetadata createFromParcel(android.os.Parcel _aidl_source) {
      VibrationMetadata _aidl_out = new VibrationMetadata();
      _aidl_out.readFromParcel(_aidl_source);
      return _aidl_out;
    }
    @Override
    public VibrationMetadata[] newArray(int _aidl_size) {
      return new VibrationMetadata[_aidl_size];
    }
  };
  @Override public final void writeToParcel(android.os.Parcel _aidl_parcel, int _aidl_flag)
  {
    int _aidl_start_pos = _aidl_parcel.dataPosition();
    _aidl_parcel.writeInt(0);
    _aidl_parcel.writeInt(vibratorMask);
    _aidl_parcel.writeByte(usage);
    _aidl_parcel.writeTypedObject(extension, 0);
    int _aidl_end_pos = _aidl_parcel.dataPosition();
    _aidl_parcel.setDataPosition(_aidl_start_pos);
    _aidl_parcel.writeInt(_aidl_end_pos - _aidl_start_pos);
    _aidl_parcel.setDataPosition(_aidl_end_pos);
  }
  public final void readFromParcel(android.os.Parcel _aidl_parcel)
  {
    int _aidl_start_pos = _aidl_parcel.dataPosition();
    int _aidl_parcelable_size = _aidl_parcel.readInt();
    try {
      if (_aidl_parcelable_size < 4) throw new android.os.BadParcelableException("Parcelable too small");;
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      vibratorMask = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      usage = _aidl_parcel.readByte();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      if ((0!=_aidl_parcel.readInt())) {
        extension.readFromParcel(_aidl_parcel);
      }
    } finally {
      if (_aidl_start_pos > (Integer.MAX_VALUE - _aidl_parcelable_size)) {
        throw new android.os.BadParcelableException("Overflow in the size of parcelable");
      }
      _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
    }
  }
  @Override
  public int describeContents() {
    int _mask = 0;
    _mask |= describeContents(extension);
    return _mask;
  }
  private int describeContents(Object _v) {
    if (_v == null) return 0;
    if (_v instanceof android.os.Parcelable) {
      return ((android.os.Parcelable) _v).describeContents();
    }
    return 0;
  }
}
