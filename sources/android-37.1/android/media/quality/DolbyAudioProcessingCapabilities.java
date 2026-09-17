/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: aidl --lang=java -Weverything -Wno-missing-permission-annotation --min_sdk_version current --ninja -d out/soong/.intermediates/frameworks/base/media/java/android/media/quality/media_quality_aidl_interface-java-source/gen/android/media/quality/DolbyAudioProcessingCapabilities.java.d -o out/soong/.intermediates/frameworks/base/media/java/android/media/quality/media_quality_aidl_interface-java-source/gen -Nframeworks/base/media/java/android/media/quality/aidl frameworks/base/media/java/android/media/quality/aidl/android/media/quality/DolbyAudioProcessingCapabilities.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.media.quality;
/**
 * A class to represent the static capabilities of Dolby Audio Processing.
 * 
 */
@android.annotation.Hide
public class DolbyAudioProcessingCapabilities implements android.os.Parcelable
{
  public int[] supportedSoundModes;
  public boolean isVolumeLevelerSupported = false;
  public boolean isSurroundVirtualizerSupported = false;
  public boolean isAtmosSupported = false;
  public static final android.os.Parcelable.Creator<DolbyAudioProcessingCapabilities> CREATOR = new android.os.Parcelable.Creator<DolbyAudioProcessingCapabilities>() {
    @Override
    public DolbyAudioProcessingCapabilities createFromParcel(android.os.Parcel _aidl_source) {
      DolbyAudioProcessingCapabilities _aidl_out = new DolbyAudioProcessingCapabilities();
      _aidl_out.readFromParcel(_aidl_source);
      return _aidl_out;
    }
    @Override
    public DolbyAudioProcessingCapabilities[] newArray(int _aidl_size) {
      return new DolbyAudioProcessingCapabilities[_aidl_size];
    }
  };
  @Override public final void writeToParcel(android.os.Parcel _aidl_parcel, int _aidl_flag)
  {
    int _aidl_start_pos = _aidl_parcel.dataPosition();
    _aidl_parcel.writeInt(0);
    _aidl_parcel.writeIntArray(supportedSoundModes);
    _aidl_parcel.writeBoolean(isVolumeLevelerSupported);
    _aidl_parcel.writeBoolean(isSurroundVirtualizerSupported);
    _aidl_parcel.writeBoolean(isAtmosSupported);
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
      supportedSoundModes = _aidl_parcel.createIntArray();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      isVolumeLevelerSupported = _aidl_parcel.readBoolean();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      isSurroundVirtualizerSupported = _aidl_parcel.readBoolean();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      isAtmosSupported = _aidl_parcel.readBoolean();
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
    _aidl_sj.add("supportedSoundModes: " + (java.util.Arrays.toString(supportedSoundModes)));
    _aidl_sj.add("isVolumeLevelerSupported: " + (isVolumeLevelerSupported));
    _aidl_sj.add("isSurroundVirtualizerSupported: " + (isSurroundVirtualizerSupported));
    _aidl_sj.add("isAtmosSupported: " + (isAtmosSupported));
    return "DolbyAudioProcessingCapabilities" + _aidl_sj.toString()  ;
  }
  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null) return false;
    if (!(other instanceof DolbyAudioProcessingCapabilities)) return false;
    DolbyAudioProcessingCapabilities that = (DolbyAudioProcessingCapabilities)other;
    if (!java.util.Objects.deepEquals(supportedSoundModes, that.supportedSoundModes)) return false;
    if (!java.util.Objects.deepEquals(isVolumeLevelerSupported, that.isVolumeLevelerSupported)) return false;
    if (!java.util.Objects.deepEquals(isSurroundVirtualizerSupported, that.isSurroundVirtualizerSupported)) return false;
    if (!java.util.Objects.deepEquals(isAtmosSupported, that.isAtmosSupported)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    return java.util.Arrays.deepHashCode(java.util.Arrays.asList(supportedSoundModes, isVolumeLevelerSupported, isSurroundVirtualizerSupported, isAtmosSupported).toArray());
  }
  @Override
  public int describeContents() {
    int _mask = 0;
    return _mask;
  }
}
