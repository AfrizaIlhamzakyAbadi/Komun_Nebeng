/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: out/host/linux-x86/bin/aidl --lang=java -Weverything -Wno-missing-permission-annotation --structured --version 1 --hash notfrozen --min_sdk_version current --ninja -d out/soong/.intermediates/packages/modules/Media/mediametrics/media_metrics/media_metrics_aidl_interface-V1-java-source/gen/android/media/metrics/reported/ReportedEditingEndedEvent.java.d -o out/soong/.intermediates/packages/modules/Media/mediametrics/media_metrics/media_metrics_aidl_interface-V1-java-source/gen -Iframeworks/base/core/java -Iframeworks/native/aidl/binder -Npackages/modules/Media/mediametrics/media_metrics/aidl packages/modules/Media/mediametrics/media_metrics/aidl/android/media/metrics/reported/ReportedEditingEndedEvent.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.media.metrics.reported;
public class ReportedEditingEndedEvent implements android.os.Parcelable
{
  public int finalState = 0;
  public float finalProgressPercent = 0.000000f;
  public int errorCode = 0;
  public long timeSinceCreatedMillis = 0L;
  public java.lang.String exporterName;
  public java.lang.String muxerName;
  public android.media.metrics.reported.ReportedMediaItemInfo[] inputMediaItemInfos;
  public android.media.metrics.reported.ReportedMediaItemInfo outputMediaItemInfo;
  public long operationTypes = 0L;
  public static final android.os.Parcelable.Creator<ReportedEditingEndedEvent> CREATOR = new android.os.Parcelable.Creator<ReportedEditingEndedEvent>() {
    @Override
    public ReportedEditingEndedEvent createFromParcel(android.os.Parcel _aidl_source) {
      ReportedEditingEndedEvent _aidl_out = new ReportedEditingEndedEvent();
      _aidl_out.readFromParcel(_aidl_source);
      return _aidl_out;
    }
    @Override
    public ReportedEditingEndedEvent[] newArray(int _aidl_size) {
      return new ReportedEditingEndedEvent[_aidl_size];
    }
  };
  @Override public final void writeToParcel(android.os.Parcel _aidl_parcel, int _aidl_flag)
  {
    int _aidl_start_pos = _aidl_parcel.dataPosition();
    _aidl_parcel.writeInt(0);
    _aidl_parcel.writeInt(finalState);
    _aidl_parcel.writeFloat(finalProgressPercent);
    _aidl_parcel.writeInt(errorCode);
    _aidl_parcel.writeLong(timeSinceCreatedMillis);
    _aidl_parcel.writeString(exporterName);
    _aidl_parcel.writeString(muxerName);
    _aidl_parcel.writeTypedArray(inputMediaItemInfos, _aidl_flag);
    _aidl_parcel.writeTypedObject(outputMediaItemInfo, _aidl_flag);
    _aidl_parcel.writeLong(operationTypes);
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
      finalState = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      finalProgressPercent = _aidl_parcel.readFloat();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      errorCode = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      timeSinceCreatedMillis = _aidl_parcel.readLong();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      exporterName = _aidl_parcel.readString();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      muxerName = _aidl_parcel.readString();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      inputMediaItemInfos = _aidl_parcel.createTypedArray(android.media.metrics.reported.ReportedMediaItemInfo.CREATOR);
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      outputMediaItemInfo = _aidl_parcel.readTypedObject(android.media.metrics.reported.ReportedMediaItemInfo.CREATOR);
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      operationTypes = _aidl_parcel.readLong();
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
    _mask |= describeContents(inputMediaItemInfos);
    _mask |= describeContents(outputMediaItemInfo);
    return _mask;
  }
  private int describeContents(Object _v) {
    if (_v == null) return 0;
    if (_v instanceof Object[]) {
      int _mask = 0;
      for (Object o : (Object[]) _v) {
        _mask |= describeContents(o);
      }
      return _mask;
    }
    if (_v instanceof android.os.Parcelable) {
      return ((android.os.Parcelable) _v).describeContents();
    }
    return 0;
  }
}
