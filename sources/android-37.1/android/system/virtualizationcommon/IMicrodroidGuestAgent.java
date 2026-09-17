/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: aidl --lang=java -Weverything -Wno-missing-permission-annotation --min_sdk_version current --ninja -d out/soong/.intermediates/packages/modules/Virtualization/android/virtualizationservice/aidl/android.system.virtualizationcommon-java-source/gen/android/system/virtualizationcommon/IMicrodroidGuestAgent.java.d -o out/soong/.intermediates/packages/modules/Virtualization/android/virtualizationservice/aidl/android.system.virtualizationcommon-java-source/gen -Npackages/modules/Virtualization/android/virtualizationservice/aidl packages/modules/Virtualization/android/virtualizationservice/aidl/android/system/virtualizationcommon/IMicrodroidGuestAgent.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.system.virtualizationcommon;
public interface IMicrodroidGuestAgent extends android.os.IInterface
{
  /** Default implementation for IMicrodroidGuestAgent. */
  public static class Default implements android.system.virtualizationcommon.IMicrodroidGuestAgent
  {
    /**
     * Starts a vsock server to dump the VM's state, and return a port number for the listening
     * vsock. The guest agent must open a vsock server which accepts one client, and then sends VM's
     * dump to the client. Writing to the client vsock must be done within 5 seconds. Otherwise, the
     * requester may regard it as a timeout.
     * 
     * TODO(b/395205629): Use IBinder::Interface::dump().
     */
    @Override public int startDumpVsockServer(java.lang.String[] args) throws android.os.RemoteException
    {
      return 0;
    }
    /** Requests the VM to trim its memory usage. */
    @Override public void trimAsync() throws android.os.RemoteException
    {
    }
    /** Called when a user is unlocked. */
    @Override public void userUnlocked(int user_id, android.system.virtualizationcommon.ICEStoreKEK per_user_kek) throws android.os.RemoteException
    {
    }
    /**
     * Whether to start or stop adbd service in Microdroid guest.
     * 
     * This function is only supported for debuggable Microdroid guests.
     */
    @Override public void startOrStopAdbd(boolean start) throws android.os.RemoteException
    {
    }
    /** Called when given {@code userId} is removed */
    @Override public void userRemoved(int userId) throws android.os.RemoteException
    {
    }
    /** Called when given {@code userId} is locked */
    @Override public void userLocked(int userId) throws android.os.RemoteException
    {
    }
    /** Called with list of active users to tell guest to prune any other users */
    @Override public void pruneUsers(int[] users) throws android.os.RemoteException
    {
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.system.virtualizationcommon.IMicrodroidGuestAgent
  {
    /** Construct the stub and attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.system.virtualizationcommon.IMicrodroidGuestAgent interface,
     * generating a proxy if needed.
     */
    public static android.system.virtualizationcommon.IMicrodroidGuestAgent asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.system.virtualizationcommon.IMicrodroidGuestAgent))) {
        return ((android.system.virtualizationcommon.IMicrodroidGuestAgent)iin);
      }
      return new android.system.virtualizationcommon.IMicrodroidGuestAgent.Stub.Proxy(obj);
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
      switch (code)
      {
        case TRANSACTION_startDumpVsockServer:
        {
          java.lang.String[] _arg0;
          _arg0 = data.createStringArray();
          data.enforceNoDataAvail();
          int _result = this.startDumpVsockServer(_arg0);
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        case TRANSACTION_trimAsync:
        {
          this.trimAsync();
          break;
        }
        case TRANSACTION_userUnlocked:
        {
          int _arg0;
          _arg0 = data.readInt();
          android.system.virtualizationcommon.ICEStoreKEK _arg1;
          _arg1 = android.system.virtualizationcommon.ICEStoreKEK.Stub.asInterface(data.readStrongBinder());
          data.enforceNoDataAvail();
          this.userUnlocked(_arg0, _arg1);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_startOrStopAdbd:
        {
          boolean _arg0;
          _arg0 = data.readBoolean();
          data.enforceNoDataAvail();
          this.startOrStopAdbd(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_userRemoved:
        {
          int _arg0;
          _arg0 = data.readInt();
          data.enforceNoDataAvail();
          this.userRemoved(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_userLocked:
        {
          int _arg0;
          _arg0 = data.readInt();
          data.enforceNoDataAvail();
          this.userLocked(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_pruneUsers:
        {
          int[] _arg0;
          _arg0 = data.createIntArray();
          data.enforceNoDataAvail();
          this.pruneUsers(_arg0);
          reply.writeNoException();
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static final class Proxy implements android.system.virtualizationcommon.IMicrodroidGuestAgent
    {
      private android.os.IBinder mRemote;
      Proxy(android.os.IBinder remote)
      {
        mRemote = remote;
      }
      @Override public android.os.IBinder asBinder()
      {
        return mRemote;
      }
      public final java.lang.String getInterfaceDescriptor()
      {
        return DESCRIPTOR;
      }
      /**
       * Starts a vsock server to dump the VM's state, and return a port number for the listening
       * vsock. The guest agent must open a vsock server which accepts one client, and then sends VM's
       * dump to the client. Writing to the client vsock must be done within 5 seconds. Otherwise, the
       * requester may regard it as a timeout.
       * 
       * TODO(b/395205629): Use IBinder::Interface::dump().
       */
      @Override public int startDumpVsockServer(java.lang.String[] args) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStringArray(args);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startDumpVsockServer, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /** Requests the VM to trim its memory usage. */
      @Override public void trimAsync() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_trimAsync, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      /** Called when a user is unlocked. */
      @Override public void userUnlocked(int user_id, android.system.virtualizationcommon.ICEStoreKEK per_user_kek) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(user_id);
          _data.writeStrongInterface(per_user_kek);
          boolean _status = mRemote.transact(Stub.TRANSACTION_userUnlocked, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /**
       * Whether to start or stop adbd service in Microdroid guest.
       * 
       * This function is only supported for debuggable Microdroid guests.
       */
      @Override public void startOrStopAdbd(boolean start) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeBoolean(start);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startOrStopAdbd, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Called when given {@code userId} is removed */
      @Override public void userRemoved(int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_userRemoved, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Called when given {@code userId} is locked */
      @Override public void userLocked(int userId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(userId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_userLocked, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
      /** Called with list of active users to tell guest to prune any other users */
      @Override public void pruneUsers(int[] users) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeIntArray(users);
          boolean _status = mRemote.transact(Stub.TRANSACTION_pruneUsers, _data, _reply, 0);
          _reply.readException();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
      }
    }
    static final int TRANSACTION_startDumpVsockServer = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_trimAsync = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_userUnlocked = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_startOrStopAdbd = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_userRemoved = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    static final int TRANSACTION_userLocked = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
    static final int TRANSACTION_pruneUsers = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
  }
  @android.annotation.Hide
  public static final java.lang.String DESCRIPTOR = "android.system.virtualizationcommon.IMicrodroidGuestAgent";
  /**
   * Starts a vsock server to dump the VM's state, and return a port number for the listening
   * vsock. The guest agent must open a vsock server which accepts one client, and then sends VM's
   * dump to the client. Writing to the client vsock must be done within 5 seconds. Otherwise, the
   * requester may regard it as a timeout.
   * 
   * TODO(b/395205629): Use IBinder::Interface::dump().
   */
  public int startDumpVsockServer(java.lang.String[] args) throws android.os.RemoteException;
  /** Requests the VM to trim its memory usage. */
  public void trimAsync() throws android.os.RemoteException;
  /** Called when a user is unlocked. */
  public void userUnlocked(int user_id, android.system.virtualizationcommon.ICEStoreKEK per_user_kek) throws android.os.RemoteException;
  /**
   * Whether to start or stop adbd service in Microdroid guest.
   * 
   * This function is only supported for debuggable Microdroid guests.
   */
  public void startOrStopAdbd(boolean start) throws android.os.RemoteException;
  /** Called when given {@code userId} is removed */
  public void userRemoved(int userId) throws android.os.RemoteException;
  /** Called when given {@code userId} is locked */
  public void userLocked(int userId) throws android.os.RemoteException;
  /** Called with list of active users to tell guest to prune any other users */
  public void pruneUsers(int[] users) throws android.os.RemoteException;
}
