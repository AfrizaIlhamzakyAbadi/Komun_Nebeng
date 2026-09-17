/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: aidl --lang=java -Weverything -Wno-missing-permission-annotation --min_sdk_version current --ninja -d out/soong/.intermediates/frameworks/base/media/java/android/media/quality/media_quality_aidl_interface-java-source/gen/android/media/quality/DtsVirtualXSettings.java.d -o out/soong/.intermediates/frameworks/base/media/java/android/media/quality/media_quality_aidl_interface-java-source/gen -Nframeworks/base/media/java/android/media/quality/aidl frameworks/base/media/java/android/media/quality/aidl/android/media/quality/DtsVirtualXSettings.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.media.quality;
/**
 * Settings for DTS Virtual:X.
 * 
 */
@android.annotation.Hide
public class DtsVirtualXSettings implements android.os.Parcelable
{
  public boolean tbHdx = false;
  public boolean limiter = false;
  public boolean truSurroundX = false;
  public boolean truVolumeHd = false;
  public boolean dialogClarity = false;
  public boolean definition = false;
  public boolean height = false;
  public static final android.os.Parcelable.Creator<DtsVirtualXSettings> CREATOR = new android.os.Parcelable.Creator<DtsVirtualXSettings>() {
    @Override
    public DtsVirtualXSettings createFromParcel(android.os.Parcel _aidl_source) {
      DtsVirtualXSettings _aidl_out = new DtsVirtualXSettings();
      _aidl_out.readFromParcel(_aidl_source);
      return _aidl_out;
    }
    @Override
    public DtsVirtualXSettings[] newArray(int _aidl_size) {
      return new DtsVirtualXSettings[_aidl_size];
    }
  };
  @Override public final void writeToParcel(android.os.Parcel _aidl_parcel, int _aidl_flag)
  {
    int _aidl_start_pos = _aidl_parcel.dataPosition();
    _aidl_parcel.writeInt(0);
    _aidl_parcel.writeBoolean(tbHdx);
    _aidl_parcel.writeBoolean(limiter);
    _aidl_parcel.writeBoolean(truSurroundX);
    _aidl_parcel.writeBoolean(truVolumeHd);
    _aidl_parcel.writeBoolean(dialogClarity);
    _aidl_parcel.writeBoolean(definition);
    _aidl_parcel.writeBoolean(height);
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
      tbHdx = _aidl_parcel.readBoolean();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      limiter = _aidl_parcel.readBoolean();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      truSurroundX = _aidl_parcel.readBoolean();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      truVolumeHd = _aidl_parcel.readBoolean();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      dialogClarity = _aidl_parcel.readBoolean();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      definition = _aidl_parcel.readBoolean();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      height = _aidl_parcel.readBoolean();
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
    _aidl_sj.add("tbHdx: " + (tbHdx));
    _aidl_sj.add("limiter: " + (limiter));
    _aidl_sj.add("truSurroundX: " + (truSurroundX));
    _aidl_sj.add("truVolumeHd: " + (truVolumeHd));
    _aidl_sj.add("dialogClarity: " + (dialogClarity));
    _aidl_sj.add("definition: " + (definition));
    _aidl_sj.add("height: " + (height));
    return "DtsVirtualXSettings" + _aidl_sj.toString()  ;
  }
  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null) return false;
    if (!(other instanceof DtsVirtualXSettings)) return false;
    DtsVirtualXSettings that = (DtsVirtualXSettings)other;
    if (!java.util.Objects.deepEquals(tbHdx, that.tbHdx)) return false;
    if (!java.util.Objects.deepEquals(limiter, that.limiter)) return false;
    if (!java.util.Objects.deepEquals(truSurroundX, that.truSurroundX)) return false;
    if (!java.util.Objects.deepEquals(truVolumeHd, that.truVolumeHd)) return false;
    if (!java.util.Objects.deepEquals(dialogClarity, that.dialogClarity)) return false;
    if (!java.util.Objects.deepEquals(definition, that.definition)) return false;
    if (!java.util.Objects.deepEquals(height, that.height)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    return java.util.Arrays.deepHashCode(java.util.Arrays.asList(tbHdx, limiter, truSurroundX, truVolumeHd, dialogClarity, definition, height).toArray());
  }
  @Override
  public int describeContents() {
    int _mask = 0;
    return _mask;
  }
}
