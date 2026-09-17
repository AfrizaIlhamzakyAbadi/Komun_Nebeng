/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: aidl --lang=java -Weverything -Wno-missing-permission-annotation --structured --version 1 --hash notfrozen --min_sdk_version current --ninja -d out/soong/.intermediates/packages/modules/Media/mediametrics/media_metrics/media_metrics_aidl_interface-V1-java-source/gen/android/media/metrics/reported/ReportedPlaybackMetrics.java.d -o out/soong/.intermediates/packages/modules/Media/mediametrics/media_metrics/media_metrics_aidl_interface-V1-java-source/gen -Iframeworks/base/core/java -Iframeworks/native/aidl/binder -Npackages/modules/Media/mediametrics/media_metrics/aidl packages/modules/Media/mediametrics/media_metrics/aidl/android/media/metrics/reported/ReportedPlaybackMetrics.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.media.metrics.reported;
public class ReportedPlaybackMetrics implements android.os.Parcelable
{
  public long mediaDurationMillis = 0L;
  public int streamSource = 0;
  public int streamType = 0;
  public int playbackType = 0;
  public int drmType = 0;
  public int contentType = 0;
  public java.lang.String playerName;
  public java.lang.String playerVersion;
  public long[] experimentIds;
  public int videoFramesPlayed = 0;
  public int videoFramesDropped = 0;
  public int audioUnderrunCount = 0;
  public long networkBytesRead = 0L;
  public long localBytesRead = 0L;
  public long networkTransferDurationMillis = 0L;
  public byte[] drmSessionId;
  public static final android.os.Parcelable.Creator<ReportedPlaybackMetrics> CREATOR = new android.os.Parcelable.Creator<ReportedPlaybackMetrics>() {
    @Override
    public ReportedPlaybackMetrics createFromParcel(android.os.Parcel _aidl_source) {
      ReportedPlaybackMetrics _aidl_out = new ReportedPlaybackMetrics();
      _aidl_out.readFromParcel(_aidl_source);
      return _aidl_out;
    }
    @Override
    public ReportedPlaybackMetrics[] newArray(int _aidl_size) {
      return new ReportedPlaybackMetrics[_aidl_size];
    }
  };
  @Override public final void writeToParcel(android.os.Parcel _aidl_parcel, int _aidl_flag)
  {
    int _aidl_start_pos = _aidl_parcel.dataPosition();
    _aidl_parcel.writeInt(0);
    _aidl_parcel.writeLong(mediaDurationMillis);
    _aidl_parcel.writeInt(streamSource);
    _aidl_parcel.writeInt(streamType);
    _aidl_parcel.writeInt(playbackType);
    _aidl_parcel.writeInt(drmType);
    _aidl_parcel.writeInt(contentType);
    _aidl_parcel.writeString(playerName);
    _aidl_parcel.writeString(playerVersion);
    _aidl_parcel.writeLongArray(experimentIds);
    _aidl_parcel.writeInt(videoFramesPlayed);
    _aidl_parcel.writeInt(videoFramesDropped);
    _aidl_parcel.writeInt(audioUnderrunCount);
    _aidl_parcel.writeLong(networkBytesRead);
    _aidl_parcel.writeLong(localBytesRead);
    _aidl_parcel.writeLong(networkTransferDurationMillis);
    _aidl_parcel.writeByteArray(drmSessionId);
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
      mediaDurationMillis = _aidl_parcel.readLong();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      streamSource = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      streamType = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      playbackType = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      drmType = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      contentType = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      playerName = _aidl_parcel.readString();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      playerVersion = _aidl_parcel.readString();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      experimentIds = _aidl_parcel.createLongArray();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      videoFramesPlayed = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      videoFramesDropped = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      audioUnderrunCount = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      networkBytesRead = _aidl_parcel.readLong();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      localBytesRead = _aidl_parcel.readLong();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      networkTransferDurationMillis = _aidl_parcel.readLong();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      drmSessionId = _aidl_parcel.createByteArray();
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
