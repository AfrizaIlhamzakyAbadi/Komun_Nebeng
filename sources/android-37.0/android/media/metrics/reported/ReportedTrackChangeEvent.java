/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: out/host/linux-x86/bin/aidl --lang=java -Weverything -Wno-missing-permission-annotation --structured --version 1 --hash notfrozen --min_sdk_version current --ninja -d out/soong/.intermediates/packages/modules/Media/mediametrics/media_metrics/media_metrics_aidl_interface-V1-java-source/gen/android/media/metrics/reported/ReportedTrackChangeEvent.java.d -o out/soong/.intermediates/packages/modules/Media/mediametrics/media_metrics/media_metrics_aidl_interface-V1-java-source/gen -Iframeworks/base/core/java -Iframeworks/native/aidl/binder -Npackages/modules/Media/mediametrics/media_metrics/aidl packages/modules/Media/mediametrics/media_metrics/aidl/android/media/metrics/reported/ReportedTrackChangeEvent.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.media.metrics.reported;
public class ReportedTrackChangeEvent implements android.os.Parcelable
{
  public int trackState = 0;
  public int trackChangeReason = 0;
  public java.lang.String containerMimeType;
  public java.lang.String sampleMimeType;
  public java.lang.String codecName;
  public int bitrate = 0;
  public long timeSinceCreatedMillis = 0L;
  public int trackType = 0;
  public java.lang.String language;
  public java.lang.String languageRegion;
  public int channelCount = 0;
  public int audioSampleRate = 0;
  public int width = 0;
  public int height = 0;
  public float videoFrameRate = 0.000000f;
  public static final android.os.Parcelable.Creator<ReportedTrackChangeEvent> CREATOR = new android.os.Parcelable.Creator<ReportedTrackChangeEvent>() {
    @Override
    public ReportedTrackChangeEvent createFromParcel(android.os.Parcel _aidl_source) {
      ReportedTrackChangeEvent _aidl_out = new ReportedTrackChangeEvent();
      _aidl_out.readFromParcel(_aidl_source);
      return _aidl_out;
    }
    @Override
    public ReportedTrackChangeEvent[] newArray(int _aidl_size) {
      return new ReportedTrackChangeEvent[_aidl_size];
    }
  };
  @Override public final void writeToParcel(android.os.Parcel _aidl_parcel, int _aidl_flag)
  {
    int _aidl_start_pos = _aidl_parcel.dataPosition();
    _aidl_parcel.writeInt(0);
    _aidl_parcel.writeInt(trackState);
    _aidl_parcel.writeInt(trackChangeReason);
    _aidl_parcel.writeString(containerMimeType);
    _aidl_parcel.writeString(sampleMimeType);
    _aidl_parcel.writeString(codecName);
    _aidl_parcel.writeInt(bitrate);
    _aidl_parcel.writeLong(timeSinceCreatedMillis);
    _aidl_parcel.writeInt(trackType);
    _aidl_parcel.writeString(language);
    _aidl_parcel.writeString(languageRegion);
    _aidl_parcel.writeInt(channelCount);
    _aidl_parcel.writeInt(audioSampleRate);
    _aidl_parcel.writeInt(width);
    _aidl_parcel.writeInt(height);
    _aidl_parcel.writeFloat(videoFrameRate);
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
      trackState = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      trackChangeReason = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      containerMimeType = _aidl_parcel.readString();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      sampleMimeType = _aidl_parcel.readString();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      codecName = _aidl_parcel.readString();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      bitrate = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      timeSinceCreatedMillis = _aidl_parcel.readLong();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      trackType = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      language = _aidl_parcel.readString();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      languageRegion = _aidl_parcel.readString();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      channelCount = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      audioSampleRate = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      width = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      height = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      videoFrameRate = _aidl_parcel.readFloat();
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
