/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: aidl --lang=java -Weverything -Wno-missing-permission-annotation --structured --version 5 --hash notfrozen --stability vintf --min_sdk_version current -pout/soong/.intermediates/hardware/interfaces/common/aidl/android.hardware.common_interface/2/preprocessed.aidl -pout/soong/.intermediates/hardware/interfaces/common/fmq/aidl/android.hardware.common.fmq_interface/1/preprocessed.aidl -pout/soong/.intermediates/system/hardware/interfaces/media/android.media.audio.common.types_interface/5/preprocessed.aidl --previous_api_dir=hardware/interfaces/vibrator/aidl/aidl_api/android.hardware.vibrator/4 --previous_hash dec155403ea3aa5395b0226de399873712b16082 --ninja -d out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen/android/hardware/vibrator/ActivePwle.java.d -o out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen -Iframeworks/native/aidl/binder -Nhardware/interfaces/vibrator/aidl hardware/interfaces/vibrator/aidl/android/hardware/vibrator/ActivePwle.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.hardware.vibrator;
public class ActivePwle implements android.os.Parcelable
{
  /**
   * Amplitude ranging from 0.0 (inclusive) to 1.0 (inclusive)
   * in units of output acceleration amplitude, not voltage amplitude.
   * 
   * Values should fall within the range of 0.0 (inclusive) to the maximum defined
   * by the corresponding entries in IVibrator#getBandwidthAmplitudeMap (inclusive).
   * If startFrequency falls between two entries, the value will not exceed the
   * largest amplitude of the two bounding frequencies.
   * 
   * 0.0 represents no output acceleration amplitude
   * 1.0 represents maximum output acceleration amplitude
   *     across all supported frequencies
   */
  public float startAmplitude = 0.000000f;
  /**
   * Absolute frequency point in the units of hertz
   * 
   * Values are within the continuous inclusive frequency range defined by
   * IVibrator#getBandwidthAmplitudeMap, and not limited by the
   * IVibrator#getFrequencyResolution.
   */
  public float startFrequency = 0.000000f;
  /**
   * Amplitude ranging from 0.0 (inclusive) to 1.0 (inclusive)
   * in units of output acceleration amplitude, not voltage amplitude.
   * 
   * Values should fall within the range of 0.0 (inclusive) to the maximum defined
   * by the corresponding entries in IVibrator#getBandwidthAmplitudeMap (inclusive).
   * If endFrequency falls between two entries, the value will not exceed the
   * largest amplitude of the two bounding frequencies.
   * 
   * 0.0 represents no output acceleration amplitude
   * 1.0 represents maximum output acceleration amplitude
   *     across all supported frequencies
   */
  public float endAmplitude = 0.000000f;
  /**
   * Absolute frequency point in the units of hertz
   * 
   * Values are within the continuous inclusive frequency range defined by
   * IVibrator#getBandwidthAmplitudeMap, and not limited by the
   * IVibrator#getFrequencyResolution.
   */
  public float endFrequency = 0.000000f;
  /** Total duration from start point to end point in the units of milliseconds */
  public int duration = 0;
  @Override
   public final int getStability() { return android.os.Parcelable.PARCELABLE_STABILITY_VINTF; }
  public static final android.os.Parcelable.Creator<ActivePwle> CREATOR = new android.os.Parcelable.Creator<ActivePwle>() {
    @Override
    public ActivePwle createFromParcel(android.os.Parcel _aidl_source) {
      ActivePwle _aidl_out = new ActivePwle();
      _aidl_out.readFromParcel(_aidl_source);
      return _aidl_out;
    }
    @Override
    public ActivePwle[] newArray(int _aidl_size) {
      return new ActivePwle[_aidl_size];
    }
  };
  @Override public final void writeToParcel(android.os.Parcel _aidl_parcel, int _aidl_flag)
  {
    int _aidl_start_pos = _aidl_parcel.dataPosition();
    _aidl_parcel.writeInt(0);
    _aidl_parcel.writeFloat(startAmplitude);
    _aidl_parcel.writeFloat(startFrequency);
    _aidl_parcel.writeFloat(endAmplitude);
    _aidl_parcel.writeFloat(endFrequency);
    _aidl_parcel.writeInt(duration);
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
      startAmplitude = _aidl_parcel.readFloat();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      startFrequency = _aidl_parcel.readFloat();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      endAmplitude = _aidl_parcel.readFloat();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      endFrequency = _aidl_parcel.readFloat();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      duration = _aidl_parcel.readInt();
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
    return _mask;
  }
}
