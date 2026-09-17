/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: out/host/linux-x86/bin/aidl --lang=java -Weverything -Wno-missing-permission-annotation -t --min_sdk_version platform_apis -pout/soong/.intermediates/system/hardware/interfaces/keystore2/aidl/android.system.keystore2_interface/6/preprocessed.aidl --ninja -d out/soong/.intermediates/system/security/keystore2/aidl/android.security.metrics-java-source/gen/android/security/metrics/KeyOperationStreamingStats.java.d -o out/soong/.intermediates/system/security/keystore2/aidl/android.security.metrics-java-source/gen -Nsystem/security/keystore2/aidl system/security/keystore2/aidl/android/security/metrics/KeyOperationStreamingStats.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.security.metrics;
/**
 * KeyOperationStreamingStats atom as defined in Keystore2KeyOperationStreamingStats of
 * frameworks/proto_logging/stats/atoms/keystore/keystore_extension_atoms.proto.
 * @hide
 */
public class KeyOperationStreamingStats implements android.os.Parcelable
{
  public int algorithm;
  public boolean is_success = false;
  public int call_count = 0;
  public long total_input_bytes = 0L;
  public static final android.os.Parcelable.Creator<KeyOperationStreamingStats> CREATOR = new android.os.Parcelable.Creator<KeyOperationStreamingStats>() {
    @Override
    public KeyOperationStreamingStats createFromParcel(android.os.Parcel _aidl_source) {
      KeyOperationStreamingStats _aidl_out = new KeyOperationStreamingStats();
      _aidl_out.readFromParcel(_aidl_source);
      return _aidl_out;
    }
    @Override
    public KeyOperationStreamingStats[] newArray(int _aidl_size) {
      return new KeyOperationStreamingStats[_aidl_size];
    }
  };
  @Override public final void writeToParcel(android.os.Parcel _aidl_parcel, int _aidl_flag)
  {
    int _aidl_start_pos = _aidl_parcel.dataPosition();
    _aidl_parcel.writeInt(0);
    _aidl_parcel.writeInt(algorithm);
    _aidl_parcel.writeBoolean(is_success);
    _aidl_parcel.writeInt(call_count);
    _aidl_parcel.writeLong(total_input_bytes);
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
      algorithm = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      is_success = _aidl_parcel.readBoolean();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      call_count = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      total_input_bytes = _aidl_parcel.readLong();
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
