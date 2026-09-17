/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: out/host/linux-x86/bin/aidl --lang=java -Weverything -Wno-missing-permission-annotation -t --min_sdk_version platform_apis -pout/soong/.intermediates/system/hardware/interfaces/keystore2/aidl/android.system.keystore2_interface/6/preprocessed.aidl --ninja -d out/soong/.intermediates/system/security/keystore2/aidl/android.security.metrics-java-source/gen/android/security/metrics/OperationLatency.java.d -o out/soong/.intermediates/system/security/keystore2/aidl/android.security.metrics-java-source/gen -Nsystem/security/keystore2/aidl system/security/keystore2/aidl/android/security/metrics/OperationLatency.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.security.metrics;
/**
 * Keystore2OperationLatency atom as defined in
 * frameworks/proto_logging/stats/atoms/keystore/keystore_extension_atoms.proto.
 * @hide
 */
public class OperationLatency implements android.os.Parcelable
{
  /** The type of operation being performed. */
  public int operation_type;
  /** The algorithm used in the operation. */
  public int algorithm;
  /** The size of the key in bits. Use -1 if not applicable. */
  public int key_size = 0;
  /** The elliptic curve used, if the algorithm is EC. Use UNSPECIFIED if not applicable. */
  public int ec_curve;
  /** The security level (e.g. TEE, StrongBox) where the operation took place. */
  public int security_level;
  /** Whether the operation was successful. */
  public boolean is_success = false;
  /**
   * The rounded latency of the operation in milliseconds.
   * Latency is rounded to preserve useful precision while strictly limiting cardinality
   * to manage the system-wide metrics cache size limit.
   * 
   * Rounding Logic:
   * - Values <= 10ms: rounded to nearest 5ms.
   * - Values 11ms - 100ms: rounded to nearest 10ms.
   * - Values > 100ms: rounded to approximately 5% of the current order of magnitude
   *   (targeting ~18 buckets per order of magnitude).
   */
  public int latency_ms = 0;
  public static final android.os.Parcelable.Creator<OperationLatency> CREATOR = new android.os.Parcelable.Creator<OperationLatency>() {
    @Override
    public OperationLatency createFromParcel(android.os.Parcel _aidl_source) {
      OperationLatency _aidl_out = new OperationLatency();
      _aidl_out.readFromParcel(_aidl_source);
      return _aidl_out;
    }
    @Override
    public OperationLatency[] newArray(int _aidl_size) {
      return new OperationLatency[_aidl_size];
    }
  };
  @Override public final void writeToParcel(android.os.Parcel _aidl_parcel, int _aidl_flag)
  {
    int _aidl_start_pos = _aidl_parcel.dataPosition();
    _aidl_parcel.writeInt(0);
    _aidl_parcel.writeInt(operation_type);
    _aidl_parcel.writeInt(algorithm);
    _aidl_parcel.writeInt(key_size);
    _aidl_parcel.writeInt(ec_curve);
    _aidl_parcel.writeInt(security_level);
    _aidl_parcel.writeBoolean(is_success);
    _aidl_parcel.writeInt(latency_ms);
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
      operation_type = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      algorithm = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      key_size = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      ec_curve = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      security_level = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      is_success = _aidl_parcel.readBoolean();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      latency_ms = _aidl_parcel.readInt();
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
