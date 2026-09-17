/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: out/host/linux-x86/bin/aidl --lang=java -Weverything -Wno-missing-permission-annotation --structured --version 1 --hash notfrozen --min_sdk_version current --ninja -d out/soong/.intermediates/packages/modules/Media/mediametrics/media_metrics/media_metrics_aidl_interface-V1-java-source/gen/android/media/metrics/reported/ReportedMediaItemInfo.java.d -o out/soong/.intermediates/packages/modules/Media/mediametrics/media_metrics/media_metrics_aidl_interface-V1-java-source/gen -Iframeworks/base/core/java -Iframeworks/native/aidl/binder -Npackages/modules/Media/mediametrics/media_metrics/aidl packages/modules/Media/mediametrics/media_metrics/aidl/android/media/metrics/reported/ReportedMediaItemInfo.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.media.metrics.reported;
public class ReportedMediaItemInfo implements android.os.Parcelable
{
  public int sourceType = 0;
  public long dataTypes = 0L;
  public long durationMillis = 0L;
  public long clipDurationMillis = 0L;
  public java.lang.String containerMimeType;
  public java.lang.String[] sampleMimeTypes;
  public java.lang.String[] codecNames;
  public int audioSampleRateHz = 0;
  public int audioChannelCount = 0;
  public long audioSampleCount = 0L;
  public android.media.metrics.reported.ReportedMediaItemInfo.Size videoSize;
  public int videoDataSpace = 0;
  public float videoFrameRate = 0.000000f;
  public long videoSampleCount = 0L;
  public static final android.os.Parcelable.Creator<ReportedMediaItemInfo> CREATOR = new android.os.Parcelable.Creator<ReportedMediaItemInfo>() {
    @Override
    public ReportedMediaItemInfo createFromParcel(android.os.Parcel _aidl_source) {
      ReportedMediaItemInfo _aidl_out = new ReportedMediaItemInfo();
      _aidl_out.readFromParcel(_aidl_source);
      return _aidl_out;
    }
    @Override
    public ReportedMediaItemInfo[] newArray(int _aidl_size) {
      return new ReportedMediaItemInfo[_aidl_size];
    }
  };
  @Override public final void writeToParcel(android.os.Parcel _aidl_parcel, int _aidl_flag)
  {
    int _aidl_start_pos = _aidl_parcel.dataPosition();
    _aidl_parcel.writeInt(0);
    _aidl_parcel.writeInt(sourceType);
    _aidl_parcel.writeLong(dataTypes);
    _aidl_parcel.writeLong(durationMillis);
    _aidl_parcel.writeLong(clipDurationMillis);
    _aidl_parcel.writeString(containerMimeType);
    _aidl_parcel.writeStringArray(sampleMimeTypes);
    _aidl_parcel.writeStringArray(codecNames);
    _aidl_parcel.writeInt(audioSampleRateHz);
    _aidl_parcel.writeInt(audioChannelCount);
    _aidl_parcel.writeLong(audioSampleCount);
    _aidl_parcel.writeTypedObject(videoSize, _aidl_flag);
    _aidl_parcel.writeInt(videoDataSpace);
    _aidl_parcel.writeFloat(videoFrameRate);
    _aidl_parcel.writeLong(videoSampleCount);
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
      sourceType = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      dataTypes = _aidl_parcel.readLong();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      durationMillis = _aidl_parcel.readLong();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      clipDurationMillis = _aidl_parcel.readLong();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      containerMimeType = _aidl_parcel.readString();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      sampleMimeTypes = _aidl_parcel.createStringArray();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      codecNames = _aidl_parcel.createStringArray();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      audioSampleRateHz = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      audioChannelCount = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      audioSampleCount = _aidl_parcel.readLong();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      videoSize = _aidl_parcel.readTypedObject(android.media.metrics.reported.ReportedMediaItemInfo.Size.CREATOR);
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      videoDataSpace = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      videoFrameRate = _aidl_parcel.readFloat();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      videoSampleCount = _aidl_parcel.readLong();
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
    _mask |= describeContents(videoSize);
    return _mask;
  }
  private int describeContents(Object _v) {
    if (_v == null) return 0;
    if (_v instanceof android.os.Parcelable) {
      return ((android.os.Parcelable) _v).describeContents();
    }
    return 0;
  }
  public static class Size implements android.os.Parcelable
  {
    public int height = 0;
    public int width = 0;
    public static final android.os.Parcelable.Creator<Size> CREATOR = new android.os.Parcelable.Creator<Size>() {
      @Override
      public Size createFromParcel(android.os.Parcel _aidl_source) {
        Size _aidl_out = new Size();
        _aidl_out.readFromParcel(_aidl_source);
        return _aidl_out;
      }
      @Override
      public Size[] newArray(int _aidl_size) {
        return new Size[_aidl_size];
      }
    };
    @Override public final void writeToParcel(android.os.Parcel _aidl_parcel, int _aidl_flag)
    {
      int _aidl_start_pos = _aidl_parcel.dataPosition();
      _aidl_parcel.writeInt(0);
      _aidl_parcel.writeInt(height);
      _aidl_parcel.writeInt(width);
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
        height = _aidl_parcel.readInt();
        if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
        width = _aidl_parcel.readInt();
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
}
