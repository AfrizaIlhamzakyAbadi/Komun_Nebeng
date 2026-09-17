/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: aidl --lang=java -Weverything -Wno-missing-permission-annotation --min_sdk_version current --ninja -d out/soong/.intermediates/packages/modules/Virtualization/android/virtualizationservice/aidl/android.system.virtualizationcommon-java-source/gen/android/system/virtualizationcommon/IGuestAgent.java.d -o out/soong/.intermediates/packages/modules/Virtualization/android/virtualizationservice/aidl/android.system.virtualizationcommon-java-source/gen -Npackages/modules/Virtualization/android/virtualizationservice/aidl packages/modules/Virtualization/android/virtualizationservice/aidl/android/system/virtualizationcommon/IGuestAgent.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.system.virtualizationcommon;
public interface IGuestAgent extends android.os.IInterface
{
  /** Default implementation for IGuestAgent. */
  public static class Default implements android.system.virtualizationcommon.IGuestAgent
  {
    /** Shuts the VM down gracefully. */
    @Override public void shutdownAsync() throws android.os.RemoteException
    {
    }
    /** Retrieves an extension interface provided by the guest agent. */
    @Override public android.os.IBinder getAgentExtension(java.lang.String name) throws android.os.RemoteException
    {
      return null;
    }
    @Override
    public android.os.IBinder asBinder() {
      return null;
    }
  }
  /** Local-side IPC implementation stub class. */
  public static abstract class Stub extends android.os.Binder implements android.system.virtualizationcommon.IGuestAgent
  {
    /** Construct the stub and attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.system.virtualizationcommon.IGuestAgent interface,
     * generating a proxy if needed.
     */
    public static android.system.virtualizationcommon.IGuestAgent asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.system.virtualizationcommon.IGuestAgent))) {
        return ((android.system.virtualizationcommon.IGuestAgent)iin);
      }
      return new android.system.virtualizationcommon.IGuestAgent.Stub.Proxy(obj);
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
        case TRANSACTION_shutdownAsync:
        {
          this.shutdownAsync();
          break;
        }
        case TRANSACTION_getAgentExtension:
        {
          java.lang.String _arg0;
          _arg0 = data.readString();
          data.enforceNoDataAvail();
          android.os.IBinder _result = this.getAgentExtension(_arg0);
          reply.writeNoException();
          reply.writeStrongBinder(_result);
          break;
        }
        default:
        {
          return super.onTransact(code, data, reply, flags);
        }
      }
      return true;
    }
    private static final class Proxy implements android.system.virtualizationcommon.IGuestAgent
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
      /** Shuts the VM down gracefully. */
      @Override public void shutdownAsync() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_shutdownAsync, _data, null, android.os.IBinder.FLAG_ONEWAY);
        }
        finally {
          _data.recycle();
        }
      }
      /** Retrieves an extension interface provided by the guest agent. */
      @Override public android.os.IBinder getAgentExtension(java.lang.String name) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.os.IBinder _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeString(name);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getAgentExtension, _data, _reply, 0);
          _reply.readException();
          _result = _reply.readStrongBinder();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
    }
    static final int TRANSACTION_shutdownAsync = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_getAgentExtension = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
  }
  @android.annotation.Hide
  public static final java.lang.String DESCRIPTOR = "android.system.virtualizationcommon.IGuestAgent";
  /** Shuts the VM down gracefully. */
  public void shutdownAsync() throws android.os.RemoteException;
  /** Retrieves an extension interface provided by the guest agent. */
  public android.os.IBinder getAgentExtension(java.lang.String name) throws android.os.RemoteException;
}
