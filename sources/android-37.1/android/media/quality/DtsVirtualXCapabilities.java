/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: aidl --lang=java -Weverything -Wno-missing-permission-annotation --min_sdk_version current --ninja -d out/soong/.intermediates/frameworks/base/media/java/android/media/quality/media_quality_aidl_interface-java-source/gen/android/media/quality/DtsVirtualXCapabilities.java.d -o out/soong/.intermediates/frameworks/base/media/java/android/media/quality/media_quality_aidl_interface-java-source/gen -Nframeworks/base/media/java/android/media/quality/aidl frameworks/base/media/java/android/media/quality/aidl/android/media/quality/DtsVirtualXCapabilities.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.media.quality;
/**
 * Capabilities for DTS Virtual:X.
 * 
 */
@android.annotation.Hide
public class DtsVirtualXCapabilities implements android.os.Parcelable
{
  public boolean isTbHdxSupported = false;
  public boolean isLimiterSupported = false;
  public boolean isTruSurroundXSupported = false;
  public boolean isTruVolumeHdSupported = false;
  public boolean isDialogClaritySupported = false;
  public boolean isDefinitionSupported = false;
  public boolean isHeightSupported = false;
  public static final android.os.Parcelable.Creator<DtsVirtualXCapabilities> CREATOR = new android.os.Parcelable.Creator<DtsVirtualXCapabilities>() {
    @Override
    public DtsVirtualXCapabilities createFromParcel(android.os.Parcel _aidl_source) {
      DtsVirtualXCapabilities _aidl_out = new DtsVirtualXCapabilities();
      _aidl_out.readFromParcel(_aidl_source);
      return _aidl_out;
    }
    @Override
    public DtsVirtualXCapabilities[] newArray(int _aidl_size) {
      return new DtsVirtualXCapabilities[_aidl_size];
    }
  };
  @Override public final void writeToParcel(android.os.Parcel _aidl_parcel, int _aidl_flag)
  {
    int _aidl_start_pos = _aidl_parcel.dataPosition();
    _aidl_parcel.writeInt(0);
    _aidl_parcel.writeBoolean(isTbHdxSupported);
    _aidl_parcel.writeBoolean(isLimiterSupported);
    _aidl_parcel.writeBoolean(isTruSurroundXSupported);
    _aidl_parcel.writeBoolean(isTruVolumeHdSupported);
    _aidl_parcel.writeBoolean(isDialogClaritySupported);
    _aidl_parcel.writeBoolean(isDefinitionSupported);
    _aidl_parcel.writeBoolean(isHeightSupported);
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
      isTbHdxSupported = _aidl_parcel.readBoolean();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      isLimiterSupported = _aidl_parcel.readBoolean();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      isTruSurroundXSupported = _aidl_parcel.readBoolean();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      isTruVolumeHdSupported = _aidl_parcel.readBoolean();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      isDialogClaritySupported = _aidl_parcel.readBoolean();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      isDefinitionSupported = _aidl_parcel.readBoolean();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      isHeightSupported = _aidl_parcel.readBoolean();
    } finally {
      if (_aidl_start_pos > (Integer.MAX_VALUE - _aidl_parcelable_size)) {
        throw new android.os.BadParcelableException("Overflow in the size of parcelable");
      }
      _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
    }
  }
  @Override
  public String toString() {
    java.util.StringJoiner _aidl_sj = new java.util.StringJoiner(", ", "{", "}");
    _aidl_sj.add("isTbHdxSupported: " + (isTbHdxSupported));
    _aidl_sj.add("isLimiterSupported: " + (isLimiterSupported));
    _aidl_sj.add("isTruSurroundXSupported: " + (isTruSurroundXSupported));
    _aidl_sj.add("isTruVolumeHdSupported: " + (isTruVolumeHdSupported));
    _aidl_sj.add("isDialogClaritySupported: " + (isDialogClaritySupported));
    _aidl_sj.add("isDefinitionSupported: " + (isDefinitionSupported));
    _aidl_sj.add("isHeightSupported: " + (isHeightSupported));
    return "DtsVirtualXCapabilities" + _aidl_sj.toString()  ;
  }
  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null) return false;
    if (!(other instanceof DtsVirtualXCapabilities)) return false;
    DtsVirtualXCapabilities that = (DtsVirtualXCapabilities)other;
    if (!java.util.Objects.deepEquals(isTbHdxSupported, that.isTbHdxSupported)) return false;
    if (!java.util.Objects.deepEquals(isLimiterSupported, that.isLimiterSupported)) return false;
    if (!java.util.Objects.deepEquals(isTruSurroundXSupported, that.isTruSurroundXSupported)) return false;
    if (!java.util.Objects.deepEquals(isTruVolumeHdSupported, that.isTruVolumeHdSupported)) return false;
    if (!java.util.Objects.deepEquals(isDialogClaritySupported, that.isDialogClaritySupported)) return false;
    if (!java.util.Objects.deepEquals(isDefinitionSupported, that.isDefinitionSupported)) return false;
    if (!java.util.Objects.deepEquals(isHeightSupported, that.isHeightSupported)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    return java.util.Arrays.deepHashCode(java.util.Arrays.asList(isTbHdxSupported, isLimiterSupported, isTruSurroundXSupported, isTruVolumeHdSupported, isDialogClaritySupported, isDefinitionSupported, isHeightSupported).toArray());
  }
  @Override
  public int describeContents() {
    int _mask = 0;
    return _mask;
  }
}
