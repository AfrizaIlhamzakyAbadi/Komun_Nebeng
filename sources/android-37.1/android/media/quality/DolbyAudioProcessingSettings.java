/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: aidl --lang=java -Weverything -Wno-missing-permission-annotation --min_sdk_version current --ninja -d out/soong/.intermediates/frameworks/base/media/java/android/media/quality/media_quality_aidl_interface-java-source/gen/android/media/quality/DolbyAudioProcessingSettings.java.d -o out/soong/.intermediates/frameworks/base/media/java/android/media/quality/media_quality_aidl_interface-java-source/gen -Nframeworks/base/media/java/android/media/quality/aidl frameworks/base/media/java/android/media/quality/aidl/android/media/quality/DolbyAudioProcessingSettings.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.media.quality;
/**
 * A class to represent the current settings of Dolby Audio Processing.
 * 
 */
@android.annotation.Hide
public class DolbyAudioProcessingSettings implements android.os.Parcelable
{
  public int soundMode = 5;
  public boolean isVolumeLevelerEnabled = false;
  public boolean isSurroundVirtualizerEnabled = false;
  public boolean isAtmosEnabled = false;
  public static final android.os.Parcelable.Creator<DolbyAudioProcessingSettings> CREATOR = new android.os.Parcelable.Creator<DolbyAudioProcessingSettings>() {
    @Override
    public DolbyAudioProcessingSettings createFromParcel(android.os.Parcel _aidl_source) {
      DolbyAudioProcessingSettings _aidl_out = new DolbyAudioProcessingSettings();
      _aidl_out.readFromParcel(_aidl_source);
      return _aidl_out;
    }
    @Override
    public DolbyAudioProcessingSettings[] newArray(int _aidl_size) {
      return new DolbyAudioProcessingSettings[_aidl_size];
    }
  };
  @Override public final void writeToParcel(android.os.Parcel _aidl_parcel, int _aidl_flag)
  {
    int _aidl_start_pos = _aidl_parcel.dataPosition();
    _aidl_parcel.writeInt(0);
    _aidl_parcel.writeInt(soundMode);
    _aidl_parcel.writeBoolean(isVolumeLevelerEnabled);
    _aidl_parcel.writeBoolean(isSurroundVirtualizerEnabled);
    _aidl_parcel.writeBoolean(isAtmosEnabled);
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
      soundMode = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      isVolumeLevelerEnabled = _aidl_parcel.readBoolean();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      isSurroundVirtualizerEnabled = _aidl_parcel.readBoolean();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      isAtmosEnabled = _aidl_parcel.readBoolean();
    } finally {
      if (_aidl_start_pos > (Integer.MAX_VALUE - _aidl_parcelable_size)) {
        throw new android.os.BadParcelableException("Overflow in the size of parcelable");
      }
      _aidl_parcel.setDataPosition(_aidl_start_pos + _aidl_parcelable_size);
    }
  }
  public static final int DOLBY_SOUND_MODE_STANDARD = 5;
  @Override
  public String toString() {
    java.util.StringJoiner _aidl_sj = new java.util.StringJoiner(", ", "{", "}");
    _aidl_sj.add("soundMode: " + (soundMode));
    _aidl_sj.add("isVolumeLevelerEnabled: " + (isVolumeLevelerEnabled));
    _aidl_sj.add("isSurroundVirtualizerEnabled: " + (isSurroundVirtualizerEnabled));
    _aidl_sj.add("isAtmosEnabled: " + (isAtmosEnabled));
    return "DolbyAudioProcessingSettings" + _aidl_sj.toString()  ;
  }
  @Override
  public boolean equals(Object other) {
    if (this == other) return true;
    if (other == null) return false;
    if (!(other instanceof DolbyAudioProcessingSettings)) return false;
    DolbyAudioProcessingSettings that = (DolbyAudioProcessingSettings)other;
    if (!java.util.Objects.deepEquals(soundMode, that.soundMode)) return false;
    if (!java.util.Objects.deepEquals(isVolumeLevelerEnabled, that.isVolumeLevelerEnabled)) return false;
    if (!java.util.Objects.deepEquals(isSurroundVirtualizerEnabled, that.isSurroundVirtualizerEnabled)) return false;
    if (!java.util.Objects.deepEquals(isAtmosEnabled, that.isAtmosEnabled)) return false;
    return true;
  }

  @Override
  public int hashCode() {
    return java.util.Arrays.deepHashCode(java.util.Arrays.asList(soundMode, isVolumeLevelerEnabled, isSurroundVirtualizerEnabled, isAtmosEnabled).toArray());
  }
  @Override
  public int describeContents() {
    int _mask = 0;
    return _mask;
  }
}
