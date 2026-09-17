/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: aidl --lang=java -Weverything -Wno-missing-permission-annotation --structured --version 5 --hash notfrozen --stability vintf --min_sdk_version current -pout/soong/.intermediates/hardware/interfaces/common/aidl/android.hardware.common_interface/2/preprocessed.aidl -pout/soong/.intermediates/hardware/interfaces/common/fmq/aidl/android.hardware.common.fmq_interface/1/preprocessed.aidl -pout/soong/.intermediates/system/hardware/interfaces/media/android.media.audio.common.types_interface/5/preprocessed.aidl --previous_api_dir=hardware/interfaces/vibrator/aidl/aidl_api/android.hardware.vibrator/4 --previous_hash dec155403ea3aa5395b0226de399873712b16082 --ninja -d out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen/android/hardware/vibrator/VendorEffect.java.d -o out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen -Iframeworks/native/aidl/binder -Nhardware/interfaces/vibrator/aidl hardware/interfaces/vibrator/aidl/android/hardware/vibrator/VendorEffect.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.hardware.vibrator;
public class VendorEffect implements android.os.Parcelable
{
  /**
   * Vendor data describing the haptic effect. Expected fields should be defined by the vendor.
   * 
   * Vendors can use this as a platform extension point for experimental hardware capabilities,
   * but they are strongly discouraged from using it as an alternative to the AOSP support for
   * stable vibrator APIs. Implemenitng vendor-specific custom effects outside the platform APIs
   * will hinder portability for the code and overall user experience.
   * 
   * Vendors are encouraged to upstream new capabilities to the IVibrator surface once it has
   * matured into a stable interface.
   */
  public android.os.PersistableBundle vendorData;
  /**
   * The intensity of the haptic effect.
   * 
   * This value is defined by discrete scale levels that represents the intensity of this haptic
   * effect. This is a discrete representation of the scale parameter below.
   */
  public byte strength = android.hardware.vibrator.EffectStrength.MEDIUM;
  /**
   * The intensity of the haptic effect.
   * 
   * This value is defined by continuous scale that represents the intensity of this haptic
   * effect. The vendor implementation can follow the platform scaling function or customize the
   * implementation to their needs. This is a continuous representation of the strength parameter
   * above.
   * 
   * Values in [0,1) should scale down. Values > 1 should scale up within hardware bounds.
   */
  public float scale = 0.000000f;
  /**
   * The dynamic scale parameter provided by the vendor vibrator controller.
   * 
   * This value is the same provided by the vendor to the platform IVibratorControlService and
   * should be applied on top of the effect intensity provided by the strength/scale fields.
   * The vendor can use this to dynamically adapt the haptic effect intensity to the device state.
   * 
   * See frameworks/hardware/interfaces/vibrator for more documentation on vendor vibrator
   * controller, and ScaleParam for more about this scale parameter.
   * 
   * Values in [0,1) should scale down. Values > 1 should scale up within hardware bounds.
   */
  public float vendorScale = 0.000000f;
  @Override
   public final int getStability() { return android.os.Parcelable.PARCELABLE_STABILITY_VINTF; }
  public static final android.os.Parcelable.Creator<VendorEffect> CREATOR = new android.os.Parcelable.Creator<VendorEffect>() {
    @Override
    public VendorEffect createFromParcel(android.os.Parcel _aidl_source) {
      VendorEffect _aidl_out = new VendorEffect();
      _aidl_out.readFromParcel(_aidl_source);
      return _aidl_out;
    }
    @Override
    public VendorEffect[] newArray(int _aidl_size) {
      return new VendorEffect[_aidl_size];
    }
  };
  @Override public final void writeToParcel(android.os.Parcel _aidl_parcel, int _aidl_flag)
  {
    int _aidl_start_pos = _aidl_parcel.dataPosition();
    _aidl_parcel.writeInt(0);
    _aidl_parcel.writeTypedObject(vendorData, _aidl_flag);
    _aidl_parcel.writeByte(strength);
    _aidl_parcel.writeFloat(scale);
    _aidl_parcel.writeFloat(vendorScale);
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
      vendorData = _aidl_parcel.readTypedObject(android.os.PersistableBundle.CREATOR);
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      strength = _aidl_parcel.readByte();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      scale = _aidl_parcel.readFloat();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      vendorScale = _aidl_parcel.readFloat();
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
    _mask |= describeContents(vendorData);
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
