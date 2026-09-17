/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: aidl --lang=java -Weverything -Wno-missing-permission-annotation --structured --version 5 --hash notfrozen --stability vintf --min_sdk_version current -pout/soong/.intermediates/hardware/interfaces/common/aidl/android.hardware.common_interface/2/preprocessed.aidl -pout/soong/.intermediates/hardware/interfaces/common/fmq/aidl/android.hardware.common.fmq_interface/1/preprocessed.aidl -pout/soong/.intermediates/system/hardware/interfaces/media/android.media.audio.common.types_interface/5/preprocessed.aidl --previous_api_dir=hardware/interfaces/vibrator/aidl/aidl_api/android.hardware.vibrator/4 --previous_hash dec155403ea3aa5395b0226de399873712b16082 --ninja -d out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen/android/hardware/vibrator/IVibratorManager.java.d -o out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen -Iframeworks/native/aidl/binder -Nhardware/interfaces/vibrator/aidl hardware/interfaces/vibrator/aidl/android/hardware/vibrator/IVibratorManager.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.hardware.vibrator;
public interface IVibratorManager extends android.os.IInterface
{
  /**
   * The version of this interface that the caller is built against.
   * This might be different from what {@link #getInterfaceVersion()
   * getInterfaceVersion} returns as that is the version of the interface
   * that the remote object is implementing.
   */
  public static final int VERSION = true ? 4 : 5;
  // Interface is being downgraded to the last frozen version due to
  // RELEASE_AIDL_USE_UNFROZEN. See
  // https://source.android.com/docs/core/architecture/aidl/stable-aidl#flag-based-development
  public static final String HASH = "dec155403ea3aa5395b0226de399873712b16082";
  /** Default implementation for IVibratorManager. */
  public static class Default implements android.hardware.vibrator.IVibratorManager
  {
    /** Determine capabilities of the vibrator manager HAL (CAP_* mask) */
    @Override public int getCapabilities() throws android.os.RemoteException
    {
      return 0;
    }
    /** List the id of available vibrators. This result should be static and not change. */
    @Override public int[] getVibratorIds() throws android.os.RemoteException
    {
      return null;
    }
    /** Return an available vibrator identified with given id. */
    @Override public android.hardware.vibrator.IVibrator getVibrator(int vibratorId) throws android.os.RemoteException
    {
      return null;
    }
    /**
     * Start preparation for a synced vibration
     * 
     * This function must only be called after the previous synced vibration was triggered or
     * canceled (through cancelSynced()).
     * 
     * Doing this operation while any of the specified vibrators is already on is undefined
     * behavior. Clients should explicitly call off in each vibrator.
     * 
     * @param vibratorIds ids of the vibrators to play vibrations in sync.
     */
    @Override public void prepareSynced(int[] vibratorIds) throws android.os.RemoteException
    {
    }
    /**
     * Trigger a prepared synced vibration
     * 
     * Trigger a previously-started preparation for synced vibration, if any.
     * A callback is only expected to be supported when getCapabilities CAP_TRIGGER_CALLBACK
     * is specified.
     * 
     * @param callback A callback used to inform Frameworks of state change, if supported.
     */
    @Override public void triggerSynced(android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException
    {
    }
    /**
     * Cancel preparation of synced vibration
     * 
     * Cancel a previously-started preparation for synced vibration, if any.
     */
    @Override public void cancelSynced() throws android.os.RemoteException
    {
    }
    /**
     * Start a vibration session.
     * 
     * A vibration session can be used to send commands without resetting the vibrator state. Once a
     * session starts, the individual vibrators can receive one or more commands like on(),
     * performEffect(), setAmplitude(), etc. The vibrations performed in a session must have the
     * same behavior they have outside them. Multiple commands can be synced in a session via
     * prepareSynced as usual.
     * 
     * Starting a session on a vibrator already in another session or in a prepareSynced state is
     * not allowed and should throw illegal state. The end of a session should always notify the
     * callback provided, even if it ends prematurely due to an error.
     * 
     * This may not be supported and this support is reflected in
     * getCapabilities (CAP_START_SESSIONS). IVibratorCallback.onComplete() support is required for
     * this API.
     * 
     * @param vibratorIds ids of the vibrators in the session.
     * @param config The parameters for starting a vibration session.
     * @param callback A callback used to inform Frameworks of state change.
     * @throws :
     *         - EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
     *         - EX_ILLEGAL_ARGUMENT for invalid vibrator IDs.
     *         - EX_ILLEGAL_STATE for vibrator IDs already in a session or in a prepareSynced state.
     *         - EX_SERVICE_SPECIFIC for bad vendor data.
     */
    @Override public android.hardware.vibrator.IVibrationSession startSession(int[] vibratorIds, android.hardware.vibrator.VibrationSessionConfig config, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException
    {
      return null;
    }
    /**
     * Aborts and clears all ongoing vibration and haptic generator sessions.
     * 
     * <p>This can be used to reset the vibrator manager and its vibrators to an
     * idle state. Any active {@link IVibrationSession} or {@link HapticGeneratorSession} will
     * be terminated, and their respective {@link IVibratorCallback#onComplete} callbacks
     * will be triggered.
     */
    @Override public void clearSessions() throws android.os.RemoteException
    {
    }
    /**
     * Starts a new haptic generator session that converts effects to haptic PCM data.
     * 
     * <p>A haptic generator session can be used to convert a stream of
     * {@link VibrationEffectContent} into a haptic PCM data stream.
     * 
     * <p>The session operates independently and can run concurrently with
     * vibrations being played via {@link IVibrator}, ensuring that PCM generation does not
     * block other haptic functionality. The same vibrator can have multiple generator sessions
     * running in parallel.
     * 
     * <p>Communication is managed through a set of Fast Message Queues (FMQs) which are returned in
     * the {@link HapticGeneratorSession} object. The framework uses these queues to:
     * <ol>
     * <li> Send commands (e.g., burst command, close session). </li>
     * <li> Stream the {@link VibrationEffectContent} to the HAL. </li>
     * <li> Receive status replies from the HAL.</li>
     * <li> Read the generated haptic PCM data from the HAL.</li>
     * </ol>
     * 
     * <p>The provided callback will be triggered when the session ends for any reason, such as
     * being terminated by a call to `IVibratorManager.clearSessions()`, a `close` command, or an
     * error within the HAL.
     * 
     * <p>This may not be supported, which is reflected in getCapabilities() (CAP_HAPTIC_GENERATOR).
     * 
     * @param vibratorIds ids of the vibrators in the session.
     * @param config Configuration parameters for the PCM generation.
     * @param callback A callback used to inform Frameworks of state changes.
     * @return A {@link HapticGeneratorSession} containing the communication queues.
     * @throws :
     *         - EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
     *         - EX_ILLEGAL_ARGUMENT for invalid vibrator IDs or invalid config data.
     */
    @Override public android.hardware.vibrator.HapticGeneratorSession startHapticGeneratorSession(int[] vibratorIds, android.hardware.vibrator.HapticGeneratorConfig config, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException
    {
      return null;
    }
    /**
     * Sets the vibration metadata for the vibrator manager synchronously.
     * 
     * <p>This function is used to set the vibration metadata for the vibrator manager.
     * <p>This may not be supported, which is reflected in getCapabilities() (CAP_CONTEXT_AWARE_VIBRATION).
     * 
     * @param metadata The vibration metadata to set.
     * @throws :
     *         - EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
     *         - EX_ILLEGAL_ARGUMENT if:
     *           - The metadata array is empty.
     *           - The vibratorMask in any entry specifies a bit corresponding to an invalid or non-existent vibrator ID.
     *           - Any parameter in the metadata (e.g. usage) contains an invalid or undefined value.
     */
    @Override public void setVibrationMetadata(android.hardware.vibrator.VibrationMetadata[] metadata) throws android.os.RemoteException
    {
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
  public static abstract class Stub extends android.os.Binder implements android.hardware.vibrator.IVibratorManager
  {
    /** Construct the stub and attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.markVintfStability();
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.hardware.vibrator.IVibratorManager interface,
     * generating a proxy if needed.
     */
    public static android.hardware.vibrator.IVibratorManager asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.hardware.vibrator.IVibratorManager))) {
        return ((android.hardware.vibrator.IVibratorManager)iin);
      }
      return new android.hardware.vibrator.IVibratorManager.Stub.Proxy(obj);
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
        case TRANSACTION_getCapabilities:
        {
          int _result = this.getCapabilities();
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        case TRANSACTION_getVibratorIds:
        {
          int[] _result = this.getVibratorIds();
          reply.writeNoException();
          reply.writeIntArray(_result);
          break;
        }
        case TRANSACTION_getVibrator:
        {
          int _arg0;
          _arg0 = data.readInt();
          data.enforceNoDataAvail();
          android.hardware.vibrator.IVibrator _result = this.getVibrator(_arg0);
          reply.writeNoException();
          reply.writeStrongInterface(_result);
          break;
        }
        case TRANSACTION_prepareSynced:
        {
          int[] _arg0;
          _arg0 = data.createIntArray();
          data.enforceNoDataAvail();
          this.prepareSynced(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_triggerSynced:
        {
          android.hardware.vibrator.IVibratorCallback _arg0;
          _arg0 = android.hardware.vibrator.IVibratorCallback.Stub.asInterface(data.readStrongBinder());
          data.enforceNoDataAvail();
          this.triggerSynced(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_cancelSynced:
        {
          this.cancelSynced();
          reply.writeNoException();
          break;
        }
        case TRANSACTION_startSession:
        {
          int[] _arg0;
          _arg0 = data.createIntArray();
          android.hardware.vibrator.VibrationSessionConfig _arg1;
          _arg1 = data.readTypedObject(android.hardware.vibrator.VibrationSessionConfig.CREATOR);
          android.hardware.vibrator.IVibratorCallback _arg2;
          _arg2 = android.hardware.vibrator.IVibratorCallback.Stub.asInterface(data.readStrongBinder());
          data.enforceNoDataAvail();
          android.hardware.vibrator.IVibrationSession _result = this.startSession(_arg0, _arg1, _arg2);
          reply.writeNoException();
          reply.writeStrongInterface(_result);
          break;
        }
        case TRANSACTION_clearSessions:
        {
          this.clearSessions();
          reply.writeNoException();
          break;
        }
        case TRANSACTION_startHapticGeneratorSession:
        {
          int[] _arg0;
          _arg0 = data.createIntArray();
          android.hardware.vibrator.HapticGeneratorConfig _arg1;
          _arg1 = data.readTypedObject(android.hardware.vibrator.HapticGeneratorConfig.CREATOR);
          android.hardware.vibrator.IVibratorCallback _arg2;
          _arg2 = android.hardware.vibrator.IVibratorCallback.Stub.asInterface(data.readStrongBinder());
          data.enforceNoDataAvail();
          android.hardware.vibrator.HapticGeneratorSession _result = this.startHapticGeneratorSession(_arg0, _arg1, _arg2);
          reply.writeNoException();
          reply.writeTypedObject(_result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_setVibrationMetadata:
        {
          if (true) {
            throw new android.os.RemoteException("Unimplemented");
          }
          android.hardware.vibrator.VibrationMetadata[] _arg0;
          _arg0 = data.createTypedArray(android.hardware.vibrator.VibrationMetadata.CREATOR);
          data.enforceNoDataAvail();
          this.setVibrationMetadata(_arg0);
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
    private static final class Proxy implements android.hardware.vibrator.IVibratorManager
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
      /** Determine capabilities of the vibrator manager HAL (CAP_* mask) */
      @Override public int getCapabilities() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getCapabilities, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
          _result = _reply.readInt();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /** List the id of available vibrators. This result should be static and not change. */
      @Override public int[] getVibratorIds() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int[] _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getVibratorIds, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
          _result = _reply.createIntArray();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /** Return an available vibrator identified with given id. */
      @Override public android.hardware.vibrator.IVibrator getVibrator(int vibratorId) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.hardware.vibrator.IVibrator _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(vibratorId);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getVibrator, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
          _result = android.hardware.vibrator.IVibrator.Stub.asInterface(_reply.readStrongBinder());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * Start preparation for a synced vibration
       * 
       * This function must only be called after the previous synced vibration was triggered or
       * canceled (through cancelSynced()).
       * 
       * Doing this operation while any of the specified vibrators is already on is undefined
       * behavior. Clients should explicitly call off in each vibrator.
       * 
       * @param vibratorIds ids of the vibrators to play vibrations in sync.
       */
      @Override public void prepareSynced(int[] vibratorIds) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeIntArray(vibratorIds);
          boolean _status = mRemote.transact(Stub.TRANSACTION_prepareSynced, _data, _reply, 0);
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
       * Trigger a prepared synced vibration
       * 
       * Trigger a previously-started preparation for synced vibration, if any.
       * A callback is only expected to be supported when getCapabilities CAP_TRIGGER_CALLBACK
       * is specified.
       * 
       * @param callback A callback used to inform Frameworks of state change, if supported.
       */
      @Override public void triggerSynced(android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeStrongInterface(callback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_triggerSynced, _data, _reply, 0);
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
       * Cancel preparation of synced vibration
       * 
       * Cancel a previously-started preparation for synced vibration, if any.
       */
      @Override public void cancelSynced() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_cancelSynced, _data, _reply, 0);
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
       * Start a vibration session.
       * 
       * A vibration session can be used to send commands without resetting the vibrator state. Once a
       * session starts, the individual vibrators can receive one or more commands like on(),
       * performEffect(), setAmplitude(), etc. The vibrations performed in a session must have the
       * same behavior they have outside them. Multiple commands can be synced in a session via
       * prepareSynced as usual.
       * 
       * Starting a session on a vibrator already in another session or in a prepareSynced state is
       * not allowed and should throw illegal state. The end of a session should always notify the
       * callback provided, even if it ends prematurely due to an error.
       * 
       * This may not be supported and this support is reflected in
       * getCapabilities (CAP_START_SESSIONS). IVibratorCallback.onComplete() support is required for
       * this API.
       * 
       * @param vibratorIds ids of the vibrators in the session.
       * @param config The parameters for starting a vibration session.
       * @param callback A callback used to inform Frameworks of state change.
       * @throws :
       *         - EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
       *         - EX_ILLEGAL_ARGUMENT for invalid vibrator IDs.
       *         - EX_ILLEGAL_STATE for vibrator IDs already in a session or in a prepareSynced state.
       *         - EX_SERVICE_SPECIFIC for bad vendor data.
       */
      @Override public android.hardware.vibrator.IVibrationSession startSession(int[] vibratorIds, android.hardware.vibrator.VibrationSessionConfig config, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.hardware.vibrator.IVibrationSession _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeIntArray(vibratorIds);
          _data.writeTypedObject(config, 0);
          _data.writeStrongInterface(callback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startSession, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
          _result = android.hardware.vibrator.IVibrationSession.Stub.asInterface(_reply.readStrongBinder());
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * Aborts and clears all ongoing vibration and haptic generator sessions.
       * 
       * <p>This can be used to reset the vibrator manager and its vibrators to an
       * idle state. Any active {@link IVibrationSession} or {@link HapticGeneratorSession} will
       * be terminated, and their respective {@link IVibratorCallback#onComplete} callbacks
       * will be triggered.
       */
      @Override public void clearSessions() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_clearSessions, _data, _reply, 0);
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
       * Starts a new haptic generator session that converts effects to haptic PCM data.
       * 
       * <p>A haptic generator session can be used to convert a stream of
       * {@link VibrationEffectContent} into a haptic PCM data stream.
       * 
       * <p>The session operates independently and can run concurrently with
       * vibrations being played via {@link IVibrator}, ensuring that PCM generation does not
       * block other haptic functionality. The same vibrator can have multiple generator sessions
       * running in parallel.
       * 
       * <p>Communication is managed through a set of Fast Message Queues (FMQs) which are returned in
       * the {@link HapticGeneratorSession} object. The framework uses these queues to:
       * <ol>
       * <li> Send commands (e.g., burst command, close session). </li>
       * <li> Stream the {@link VibrationEffectContent} to the HAL. </li>
       * <li> Receive status replies from the HAL.</li>
       * <li> Read the generated haptic PCM data from the HAL.</li>
       * </ol>
       * 
       * <p>The provided callback will be triggered when the session ends for any reason, such as
       * being terminated by a call to `IVibratorManager.clearSessions()`, a `close` command, or an
       * error within the HAL.
       * 
       * <p>This may not be supported, which is reflected in getCapabilities() (CAP_HAPTIC_GENERATOR).
       * 
       * @param vibratorIds ids of the vibrators in the session.
       * @param config Configuration parameters for the PCM generation.
       * @param callback A callback used to inform Frameworks of state changes.
       * @return A {@link HapticGeneratorSession} containing the communication queues.
       * @throws :
       *         - EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
       *         - EX_ILLEGAL_ARGUMENT for invalid vibrator IDs or invalid config data.
       */
      @Override public android.hardware.vibrator.HapticGeneratorSession startHapticGeneratorSession(int[] vibratorIds, android.hardware.vibrator.HapticGeneratorConfig config, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        android.hardware.vibrator.HapticGeneratorSession _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeIntArray(vibratorIds);
          _data.writeTypedObject(config, 0);
          _data.writeStrongInterface(callback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_startHapticGeneratorSession, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
          _result = _reply.readTypedObject(android.hardware.vibrator.HapticGeneratorSession.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * Sets the vibration metadata for the vibrator manager synchronously.
       * 
       * <p>This function is used to set the vibration metadata for the vibrator manager.
       * <p>This may not be supported, which is reflected in getCapabilities() (CAP_CONTEXT_AWARE_VIBRATION).
       * 
       * @param metadata The vibration metadata to set.
       * @throws :
       *         - EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
       *         - EX_ILLEGAL_ARGUMENT if:
       *           - The metadata array is empty.
       *           - The vibratorMask in any entry specifies a bit corresponding to an invalid or non-existent vibrator ID.
       *           - Any parameter in the metadata (e.g. usage) contains an invalid or undefined value.
       */
      @Override public void setVibrationMetadata(android.hardware.vibrator.VibrationMetadata[] metadata) throws android.os.RemoteException
      {
        if (true) {
          throw new android.os.RemoteException("Unimplemented");
        }
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeTypedArray(metadata, 0);
          boolean _status = mRemote.transact(Stub.TRANSACTION_setVibrationMetadata, _data, _reply, 0);
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
    static final int TRANSACTION_getCapabilities = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
    static final int TRANSACTION_getVibratorIds = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_getVibrator = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_prepareSynced = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_triggerSynced = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_cancelSynced = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    static final int TRANSACTION_startSession = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
    static final int TRANSACTION_clearSessions = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
    static final int TRANSACTION_startHapticGeneratorSession = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
    static final int TRANSACTION_setVibrationMetadata = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
    static final int TRANSACTION_getInterfaceVersion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16777214);
    static final int TRANSACTION_getInterfaceHash = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16777213);
  }
  @android.annotation.Hide
  public static final java.lang.String DESCRIPTOR = "android$hardware$vibrator$IVibratorManager".replace('$', '.');
  /** Whether prepare/trigger synced are supported. */
  public static final int CAP_SYNC = 1;
  /** Whether IVibrator 'on' can be used with 'prepareSynced' function. */
  public static final int CAP_PREPARE_ON = 2;
  /** Whether IVibrator 'perform' can be used with 'prepareSynced' function. */
  public static final int CAP_PREPARE_PERFORM = 4;
  /** Whether IVibrator 'compose' can be used with 'prepareSynced' function. */
  public static final int CAP_PREPARE_COMPOSE = 8;
  /** Whether IVibrator 'on' can be triggered with other functions in sync with 'triggerSynced'. */
  public static final int CAP_MIXED_TRIGGER_ON = 16;
  /**
   * Whether IVibrator 'perform' can be triggered with other functions in sync with
   * 'triggerSynced'.
   */
  public static final int CAP_MIXED_TRIGGER_PERFORM = 32;
  /**
   * Whether IVibrator 'compose' can be triggered with other functions in sync with
   * 'triggerSynced'.
   */
  public static final int CAP_MIXED_TRIGGER_COMPOSE = 64;
  /** Whether on w/ IVibratorCallback can be used w/ 'trigerSynced' function. */
  public static final int CAP_TRIGGER_CALLBACK = 128;
  /** Whether vibration sessions are supported. */
  public static final int CAP_START_SESSIONS = 256;
  /** Whether haptic generator is supported. */
  public static final int CAP_HAPTIC_GENERATOR = 512;
  /** Whether 'setVibrationMetadata' can be triggered before any other playback functions. */
  public static final int CAP_CONTEXT_AWARE_VIBRATION = 1024;
  /** Determine capabilities of the vibrator manager HAL (CAP_* mask) */
  public int getCapabilities() throws android.os.RemoteException;
  /** List the id of available vibrators. This result should be static and not change. */
  public int[] getVibratorIds() throws android.os.RemoteException;
  /** Return an available vibrator identified with given id. */
  public android.hardware.vibrator.IVibrator getVibrator(int vibratorId) throws android.os.RemoteException;
  /**
   * Start preparation for a synced vibration
   * 
   * This function must only be called after the previous synced vibration was triggered or
   * canceled (through cancelSynced()).
   * 
   * Doing this operation while any of the specified vibrators is already on is undefined
   * behavior. Clients should explicitly call off in each vibrator.
   * 
   * @param vibratorIds ids of the vibrators to play vibrations in sync.
   */
  public void prepareSynced(int[] vibratorIds) throws android.os.RemoteException;
  /**
   * Trigger a prepared synced vibration
   * 
   * Trigger a previously-started preparation for synced vibration, if any.
   * A callback is only expected to be supported when getCapabilities CAP_TRIGGER_CALLBACK
   * is specified.
   * 
   * @param callback A callback used to inform Frameworks of state change, if supported.
   */
  public void triggerSynced(android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException;
  /**
   * Cancel preparation of synced vibration
   * 
   * Cancel a previously-started preparation for synced vibration, if any.
   */
  public void cancelSynced() throws android.os.RemoteException;
  /**
   * Start a vibration session.
   * 
   * A vibration session can be used to send commands without resetting the vibrator state. Once a
   * session starts, the individual vibrators can receive one or more commands like on(),
   * performEffect(), setAmplitude(), etc. The vibrations performed in a session must have the
   * same behavior they have outside them. Multiple commands can be synced in a session via
   * prepareSynced as usual.
   * 
   * Starting a session on a vibrator already in another session or in a prepareSynced state is
   * not allowed and should throw illegal state. The end of a session should always notify the
   * callback provided, even if it ends prematurely due to an error.
   * 
   * This may not be supported and this support is reflected in
   * getCapabilities (CAP_START_SESSIONS). IVibratorCallback.onComplete() support is required for
   * this API.
   * 
   * @param vibratorIds ids of the vibrators in the session.
   * @param config The parameters for starting a vibration session.
   * @param callback A callback used to inform Frameworks of state change.
   * @throws :
   *         - EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
   *         - EX_ILLEGAL_ARGUMENT for invalid vibrator IDs.
   *         - EX_ILLEGAL_STATE for vibrator IDs already in a session or in a prepareSynced state.
   *         - EX_SERVICE_SPECIFIC for bad vendor data.
   */
  public android.hardware.vibrator.IVibrationSession startSession(int[] vibratorIds, android.hardware.vibrator.VibrationSessionConfig config, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException;
  /**
   * Aborts and clears all ongoing vibration and haptic generator sessions.
   * 
   * <p>This can be used to reset the vibrator manager and its vibrators to an
   * idle state. Any active {@link IVibrationSession} or {@link HapticGeneratorSession} will
   * be terminated, and their respective {@link IVibratorCallback#onComplete} callbacks
   * will be triggered.
   */
  public void clearSessions() throws android.os.RemoteException;
  /**
   * Starts a new haptic generator session that converts effects to haptic PCM data.
   * 
   * <p>A haptic generator session can be used to convert a stream of
   * {@link VibrationEffectContent} into a haptic PCM data stream.
   * 
   * <p>The session operates independently and can run concurrently with
   * vibrations being played via {@link IVibrator}, ensuring that PCM generation does not
   * block other haptic functionality. The same vibrator can have multiple generator sessions
   * running in parallel.
   * 
   * <p>Communication is managed through a set of Fast Message Queues (FMQs) which are returned in
   * the {@link HapticGeneratorSession} object. The framework uses these queues to:
   * <ol>
   * <li> Send commands (e.g., burst command, close session). </li>
   * <li> Stream the {@link VibrationEffectContent} to the HAL. </li>
   * <li> Receive status replies from the HAL.</li>
   * <li> Read the generated haptic PCM data from the HAL.</li>
   * </ol>
   * 
   * <p>The provided callback will be triggered when the session ends for any reason, such as
   * being terminated by a call to `IVibratorManager.clearSessions()`, a `close` command, or an
   * error within the HAL.
   * 
   * <p>This may not be supported, which is reflected in getCapabilities() (CAP_HAPTIC_GENERATOR).
   * 
   * @param vibratorIds ids of the vibrators in the session.
   * @param config Configuration parameters for the PCM generation.
   * @param callback A callback used to inform Frameworks of state changes.
   * @return A {@link HapticGeneratorSession} containing the communication queues.
   * @throws :
   *         - EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
   *         - EX_ILLEGAL_ARGUMENT for invalid vibrator IDs or invalid config data.
   */
  public android.hardware.vibrator.HapticGeneratorSession startHapticGeneratorSession(int[] vibratorIds, android.hardware.vibrator.HapticGeneratorConfig config, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException;
  /**
   * Sets the vibration metadata for the vibrator manager synchronously.
   * 
   * <p>This function is used to set the vibration metadata for the vibrator manager.
   * <p>This may not be supported, which is reflected in getCapabilities() (CAP_CONTEXT_AWARE_VIBRATION).
   * 
   * @param metadata The vibration metadata to set.
   * @throws :
   *         - EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
   *         - EX_ILLEGAL_ARGUMENT if:
   *           - The metadata array is empty.
   *           - The vibratorMask in any entry specifies a bit corresponding to an invalid or non-existent vibrator ID.
   *           - Any parameter in the metadata (e.g. usage) contains an invalid or undefined value.
   */
  public void setVibrationMetadata(android.hardware.vibrator.VibrationMetadata[] metadata) throws android.os.RemoteException;
  public int getInterfaceVersion() throws android.os.RemoteException;
  public String getInterfaceHash() throws android.os.RemoteException;
}
