/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: out/host/linux-x86/bin/aidl --lang=java -Weverything -Wno-missing-permission-annotation --structured --version 1 --hash notfrozen --min_sdk_version current --ninja -d out/soong/.intermediates/packages/modules/Media/mediametrics/media_metrics/media_metrics_aidl_interface-V1-java-source/gen/android/media/metrics/IMediaMetricsManager.java.d -o out/soong/.intermediates/packages/modules/Media/mediametrics/media_metrics/media_metrics_aidl_interface-V1-java-source/gen -Iframeworks/base/core/java -Iframeworks/native/aidl/binder -Npackages/modules/Media/mediametrics/media_metrics/aidl packages/modules/Media/mediametrics/media_metrics/aidl/android/media/metrics/IMediaMetricsManager.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.media.metrics;
/**
 * Interface to the media metrics manager service.
 * @hide
 */
public interface IMediaMetricsManager extends android.os.IInterface
{
  /**
   * The version of this interface that the caller is built against.
   * This might be different from what {@link #getInterfaceVersion()
   * getInterfaceVersion} returns as that is the version of the interface
   * that the remote object is implementing.
   */
  public static final int VERSION = 1;
  public static final String HASH = "notfrozen";
  /** Default implementation for IMediaMetricsManager. */
  public static class Default implements android.media.metrics.IMediaMetricsManager
  {
    /** Report Playback data to the mediametrics subsystem */
    @Override public void reportPlaybackMetrics(java.lang.String sessionId, android.media.metrics.reported.ReportedPlaybackMetrics metrics, int userId) throws android.os.RemoteException
    {
    }
    /** Creates a playback session, returning a session id. */
    @Override public java.lang.String getPlaybackSessionId(int userId) throws android.os.RemoteException
    {
      return null;
    }
    /** Creates a recording session, returning a session id. */
    @Override public java.lang.String getRecordingSessionId(int userId) throws android.os.RemoteException
    {
      return null;
    }
    /** Report a Network event to the mediametrics subsystem */
    @Override public void reportNetworkEvent(java.lang.String sessionId, android.media.metrics.reported.ReportedNetworkEvent event, int userId) throws android.os.RemoteException
    {
    }
    /** Report a Playback error to the mediametrics subsystem */
    @Override public void reportPlaybackErrorEvent(java.lang.String sessionId, android.media.metrics.reported.ReportedPlaybackErrorEvent event, int userId) throws android.os.RemoteException
    {
    }
    /** Report a Playback state change to the mediametrics subsystem */
    @Override public void reportPlaybackStateEvent(java.lang.String sessionId, android.media.metrics.reported.ReportedPlaybackStateEvent event, int userId) throws android.os.RemoteException
    {
    }
    /** Report a Track Change to the mediametrics subsystem */
    @Override public void reportTrackChangeEvent(java.lang.String sessionId, android.media.metrics.reported.ReportedTrackChangeEvent event, int userId) throws android.os.RemoteException
    {
    }
    /** Reports the end of an editing session to the mediametrics service */
    @Override public void reportEditingEndedEvent(java.lang.String sessionId, android.media.metrics.reported.ReportedEditingEndedEvent event, int userId) throws android.os.RemoteException
    {
    }
    /** Creates a transcoding session, returning a session id. */
    @Override public java.lang.String getTranscodingSessionId(int userId) throws android.os.RemoteException
    {
      return null;
    }
    /** Creates an editing session, returning a session id. */
    @Override public java.lang.String getEditingSessionId(int userId) throws android.os.RemoteException
    {
      return null;
    }
    /** Creates a bundle session, returning a session id. */
    @Override public java.lang.String getBundleSessionId(int userId) throws android.os.RemoteException
    {
      return null;
    }
    /** Reports bundle metrics to the mediametrics service */
    @Override public void reportBundleMetrics(java.lang.String sessionId, android.os.PersistableBundle metrics, int userId) throws android.os.RemoteException
    {
    }
    /** Marks the indicates sessionId as complete and no longer active */
    @Override public void releaseSessionId(java.lang.String sessionId, int userId) throws android.os.RemoteException
    {
    }
    /**
     * Translate userId to a package name.
     * 
     * If no translation is possible, returns an empty string.
     * If multiple packages share the userId, it returns the first package found.
     */
    @Override public java.lang.String getFirstPackageName(int userId) throws android.os.RemoteException
    {
      return null;
    }
    /**
     * Determine whether the given uid holds the indicated permission.
     * 
     * Currently ignores the supplied pid.
     */
    @Override public boolean checkPermission(java.lang.String permission, int pid, int uid) throws android.os.RemoteException
    {
      return false;
    }
    @Override
    public int getInterfaceVersion() {
      return 0;
    }
    @Override
    public String getInterfaceHash() {
      return "";
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.media.metrics.IMediaMetricsManager
  {
    /** Construct the stub and attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.media.metrics.IMediaMetricsManager interface,
     * generating a proxy if needed.
     */
    public static android.media.metrics.IMediaMetricsManager asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.media.metrics.IMediaMetricsManager))) {
        return ((android.media.metrics.IMediaMetricsManager)iin);
      }
      return new android.media.metrics.IMediaMetricsManager.Stub.Proxy(obj);
    }
    @Override public android.os.IBinder asBinder()
    {
      return this;
    }
    @Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
    {
      if (code >= android.os.IBinder.FIRST_CALL_TRANSACTION && code <= android.os.IBinder.LAST_CALL_TRANSACTION) {
        data.enforceInterface(DESCRIPTOR);
      }
      if (code == TRANSACTION_getInterfaceVersion) {
        reply.writeNoException();
        reply.writeInt(getInterfaceVersion());
        return true;
      }
      else if (code == TRANSACTION_getInterfaceHash) {
        reply.writeNoException();
        reply.writeString(getInterfaceHash());
        return true;
      }
      switch (code)
      {
        case TRANSACTION_reportPlaybackMetrics:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          android.media.metrics.reported.ReportedPlaybackMetrics _arg1;
          _arg1 = data.readTypedObject(android.media.metrics.reported.ReportedPlaybackMetrics.CREATOR);
          int _arg2;
          _arg2 = data.readInt();
          data.enforceNoDataAvail();
          this.reportPlaybackMetrics(_arg0, _arg1, _arg2);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_getPlaybackSessionId:
        {
          int _arg0;
          _arg0 = data.readInt();
          data.enforceNoDataAvail();
          java.lang.String _result = this.getPlaybackSessionId(_arg0);
          reply.writeNoException();
          reply.writeString(_result);
          break;
        }
        case TRANSACTION_getRecordingSessionId:
        {
          int _arg0;
          _arg0 = data.readInt();
          data.enforceNoDataAvail();
          java.lang.String _result = this.getRecordingSessionId(_arg0);
          reply.writeNoException();
          reply.writeString(_result);
          break;
        }
        case TRANSACTION_reportNetworkEvent:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          android.media.metrics.reported.ReportedNetworkEvent _arg1;
          _arg1 = data.readTypedObject(android.media.metrics.reported.ReportedNetworkEvent.CREATOR);
          int _arg2;
          _arg2 = data.readInt();
          data.enforceNoDataAvail();
          this.reportNetworkEvent(_arg0, _arg1, _arg2);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_reportPlaybackErrorEvent:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          android.media.metrics.reported.ReportedPlaybackErrorEvent _arg1;
          _arg1 = data.readTypedObject(android.media.metrics.reported.ReportedPlaybackErrorEvent.CREATOR);
          int _arg2;
          _arg2 = data.readInt();
          data.enforceNoDataAvail();
          this.reportPlaybackErrorEvent(_arg0, _arg1, _arg2);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_reportPlaybackStateEvent:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          android.media.metrics.reported.ReportedPlaybackStateEvent _arg1;
          _arg1 = data.readTypedObject(android.media.metrics.reported.ReportedPlaybackStateEvent.CREATOR);
          int _arg2;
          _arg2 = data.readInt();
          data.enforceNoDataAvail();
          this.reportPlaybackStateEvent(_arg0, _arg1, _arg2);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_reportTrackChangeEvent:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          android.media.metrics.reported.ReportedTrackChangeEvent _arg1;
          _arg1 = data.readTypedObject(android.media.metrics.reported.ReportedTrackChangeEvent.CREATOR);
          int _arg2;
          _arg2 = data.readInt();
          data.enforceNoDataAvail();
          this.reportTrackChangeEvent(_arg0, _arg1, _arg2);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_reportEditingEndedEvent:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          android.media.metrics.reported.ReportedEditingEndedEvent _arg1;
          _arg1 = data.readTypedObject(android.media.metrics.reported.ReportedEditingEndedEvent.CREATOR);
          int _arg2;
          _arg2 = data.readInt();
          data.enforceNoDataAvail();
          this.reportEditingEndedEvent(_arg0, _arg1, _arg2);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_getTranscodingSessionId:
        {
          int _arg0;
          _arg0 = data.readInt();
          data.enforceNoDataAvail();
          java.lang.String _result = this.getTranscodingSessionId(_arg0);
          reply.writeNoException();
          reply.writeString(_result);
          break;
        }
        case TRANSACTION_getEditingSessionId:
        {
          int _arg0;
          _arg0 = data.readInt();
          data.enforceNoDataAvail();
          java.lang.String _result = this.getEditingSessionId(_arg0);
          reply.writeNoException();
          reply.writeString(_result);
          break;
        }
        case TRANSACTION_getBundleSessionId:
        {
          int _arg0;
          _arg0 = data.readInt();
          data.enforceNoDataAvail();
          java.lang.String _result = this.getBundleSessionId(_arg0);
          reply.writeNoException();
          reply.writeString(_result);
          break;
        }
        case TRANSACTION_reportBundleMetrics:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          android.os.PersistableBundle _arg1;
          _arg1 = data.readTypedObject(android.os.PersistableBundle.CREATOR);
          int _arg2;
          _arg2 = data.readInt();
          data.enforceNoDataAvail();
          this.reportBundleMetrics(_arg0, _arg1, _arg2);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_releaseSessionId:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          int _arg1;
          _arg1 = data.readInt();
          data.enforceNoDataAvail();
          this.releaseSessionId(_arg0, _arg1);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_getFirstPackageName:
        {
          int _arg0;
          _arg0 = data.readInt();
          data.enforceNoDataAvail();
          java.lang.String _result = this.getFirstPackageName(_arg0);
          reply.writeNoException();
          reply.writeString(_result);
          break;
        }
        case TRANSACTION_checkPermission:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          int _arg1;
          _arg1 = data.readInt();
          int _arg2;
          _arg2 = data.readInt();
          data.enforceNoDataAvail();
          boolean _result = this.checkPermission(_arg0, _arg1, _arg2);
          reply.writeNoException();
          reply.writeBoolean(_result);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static final class Proxy implements android.media.metrics.IMediaMetricsManager
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      private int mCachedVersion = -1;
      private String mCachedHash = "-1";
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public final java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      /** Report Playback data to the mediametrics subsystem */
      @Override public void reportPlaybackMetrics(java.lang.String sessionId, android.media.metrics.reported.ReportedPlaybackMetrics metrics, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(sessionId);
          _data.writeTypedObject(metrics, 0);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_reportPlaybackMetrics, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Creates a playback session, returning a session id. */
      @Override public java.lang.String getPlaybackSessionId(int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getPlaybackSessionId, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /** Creates a recording session, returning a session id. */
      @Override public java.lang.String getRecordingSessionId(int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getRecordingSessionId, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /** Report a Network event to the mediametrics subsystem */
      @Override public void reportNetworkEvent(java.lang.String sessionId, android.media.metrics.reported.ReportedNetworkEvent event, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(sessionId);
          _data.writeTypedObject(event, 0);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_reportNetworkEvent, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Report a Playback error to the mediametrics subsystem */
      @Override public void reportPlaybackErrorEvent(java.lang.String sessionId, android.media.metrics.reported.ReportedPlaybackErrorEvent event, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(sessionId);
          _data.writeTypedObject(event, 0);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_reportPlaybackErrorEvent, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Report a Playback state change to the mediametrics subsystem */
      @Override public void reportPlaybackStateEvent(java.lang.String sessionId, android.media.metrics.reported.ReportedPlaybackStateEvent event, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(sessionId);
          _data.writeTypedObject(event, 0);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_reportPlaybackStateEvent, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Report a Track Change to the mediametrics subsystem */
      @Override public void reportTrackChangeEvent(java.lang.String sessionId, android.media.metrics.reported.ReportedTrackChangeEvent event, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(sessionId);
          _data.writeTypedObject(event, 0);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_reportTrackChangeEvent, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Reports the end of an editing session to the mediametrics service */
      @Override public void reportEditingEndedEvent(java.lang.String sessionId, android.media.metrics.reported.ReportedEditingEndedEvent event, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(sessionId);
          _data.writeTypedObject(event, 0);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_reportEditingEndedEvent, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Creates a transcoding session, returning a session id. */
      @Override public java.lang.String getTranscodingSessionId(int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getTranscodingSessionId, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /** Creates an editing session, returning a session id. */
      @Override public java.lang.String getEditingSessionId(int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getEditingSessionId, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /** Creates a bundle session, returning a session id. */
      @Override public java.lang.String getBundleSessionId(int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getBundleSessionId, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /** Reports bundle metrics to the mediametrics service */
      @Override public void reportBundleMetrics(java.lang.String sessionId, android.os.PersistableBundle metrics, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(sessionId);
          _data.writeTypedObject(metrics, 0);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_reportBundleMetrics, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Marks the indicates sessionId as complete and no longer active */
      @Override public void releaseSessionId(java.lang.String sessionId, int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(sessionId);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_releaseSessionId, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /**
       * Translate userId to a package name.
       * 
       * If no translation is possible, returns an empty string.
       * If multiple packages share the userId, it returns the first package found.
       */
      @Override public java.lang.String getFirstPackageName(int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.lang.String _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getFirstPackageName, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
          _result = _reply.readString();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * Determine whether the given uid holds the indicated permission.
       * 
       * Currently ignores the supplied pid.
       */
      @Override public boolean checkPermission(java.lang.String permission, int pid, int uid) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        boolean _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(permission);
          _data.writeInt(pid);
          _data.writeInt(uid);
          boolean _status = mRemote.transact(Stub.TRANSACTION_checkPermission, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
          _result = _reply.readBoolean();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      @Override
      public int getInterfaceVersion() throws android.os.RemoteException {
        if (mCachedVersion == -1) {
          android.os.Parcel data = android.os.Parcel.obtain(asBinder());
          android.os.Parcel reply = android.os.Parcel.obtain();
          try {
            data.writeInterfaceToken(DESCRIPTOR);
            boolean _status = mRemote.transact(Stub.TRANSACTION_getInterfaceVersion, data, reply, 0);
            reply.readException();
            mCachedVersion = reply.readInt();
          } finally {
            reply.recycle();
            data.recycle();
          }
        }
        return mCachedVersion;
      }
      @Override
      public synchronized String getInterfaceHash() throws android.os.RemoteException {
        if ("-1".equals(mCachedHash)) {
          android.os.Parcel data = android.os.Parcel.obtain(asBinder());
          android.os.Parcel reply = android.os.Parcel.obtain();
          try {
            data.writeInterfaceToken(DESCRIPTOR);
            boolean _status = mRemote.transact(Stub.TRANSACTION_getInterfaceHash, data, reply, 0);
            reply.readException();
            mCachedHash = reply.readString();
          } finally {
            reply.recycle();
            data.recycle();
          }
        }
        return mCachedHash;
      }
    }
    static final int TRANSACTION_reportPlaybackMetrics = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_getPlaybackSessionId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_getRecordingSessionId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_reportNetworkEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_reportPlaybackErrorEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_reportPlaybackStateEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    static final int TRANSACTION_reportTrackChangeEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
    static final int TRANSACTION_reportEditingEndedEvent = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
    static final int TRANSACTION_getTranscodingSessionId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
    static final int TRANSACTION_getEditingSessionId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
    static final int TRANSACTION_getBundleSessionId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
    static final int TRANSACTION_reportBundleMetrics = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
    static final int TRANSACTION_releaseSessionId = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
    static final int TRANSACTION_getFirstPackageName = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
    static final int TRANSACTION_checkPermission = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
    static final int TRANSACTION_getInterfaceVersion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16777214);
    static final int TRANSACTION_getInterfaceHash = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16777213);
  }
  /** @hide */
  public static final java.lang.String DESCRIPTOR = "android$media$metrics$IMediaMetricsManager".replace('$', '.');
  /** Report Playback data to the mediametrics subsystem */
  public void reportPlaybackMetrics(java.lang.String sessionId, android.media.metrics.reported.ReportedPlaybackMetrics metrics, int userId) throws android.os.RemoteException;
  /** Creates a playback session, returning a session id. */
  public java.lang.String getPlaybackSessionId(int userId) throws android.os.RemoteException;
  /** Creates a recording session, returning a session id. */
  public java.lang.String getRecordingSessionId(int userId) throws android.os.RemoteException;
  /** Report a Network event to the mediametrics subsystem */
  public void reportNetworkEvent(java.lang.String sessionId, android.media.metrics.reported.ReportedNetworkEvent event, int userId) throws android.os.RemoteException;
  /** Report a Playback error to the mediametrics subsystem */
  public void reportPlaybackErrorEvent(java.lang.String sessionId, android.media.metrics.reported.ReportedPlaybackErrorEvent event, int userId) throws android.os.RemoteException;
  /** Report a Playback state change to the mediametrics subsystem */
  public void reportPlaybackStateEvent(java.lang.String sessionId, android.media.metrics.reported.ReportedPlaybackStateEvent event, int userId) throws android.os.RemoteException;
  /** Report a Track Change to the mediametrics subsystem */
  public void reportTrackChangeEvent(java.lang.String sessionId, android.media.metrics.reported.ReportedTrackChangeEvent event, int userId) throws android.os.RemoteException;
  /** Reports the end of an editing session to the mediametrics service */
  public void reportEditingEndedEvent(java.lang.String sessionId, android.media.metrics.reported.ReportedEditingEndedEvent event, int userId) throws android.os.RemoteException;
  /** Creates a transcoding session, returning a session id. */
  public java.lang.String getTranscodingSessionId(int userId) throws android.os.RemoteException;
  /** Creates an editing session, returning a session id. */
  public java.lang.String getEditingSessionId(int userId) throws android.os.RemoteException;
  /** Creates a bundle session, returning a session id. */
  public java.lang.String getBundleSessionId(int userId) throws android.os.RemoteException;
  /** Reports bundle metrics to the mediametrics service */
  public void reportBundleMetrics(java.lang.String sessionId, android.os.PersistableBundle metrics, int userId) throws android.os.RemoteException;
  /** Marks the indicates sessionId as complete and no longer active */
  public void releaseSessionId(java.lang.String sessionId, int userId) throws android.os.RemoteException;
  /**
   * Translate userId to a package name.
   * 
   * If no translation is possible, returns an empty string.
   * If multiple packages share the userId, it returns the first package found.
   */
  public java.lang.String getFirstPackageName(int userId) throws android.os.RemoteException;
  /**
   * Determine whether the given uid holds the indicated permission.
   * 
   * Currently ignores the supplied pid.
   */
  public boolean checkPermission(java.lang.String permission, int pid, int uid) throws android.os.RemoteException;
  public int getInterfaceVersion() throws android.os.RemoteException;
  public String getInterfaceHash() throws android.os.RemoteException;
}
