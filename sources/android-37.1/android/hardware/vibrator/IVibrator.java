/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: aidl --lang=java -Weverything -Wno-missing-permission-annotation --structured --version 5 --hash notfrozen --stability vintf --min_sdk_version current -pout/soong/.intermediates/hardware/interfaces/common/aidl/android.hardware.common_interface/2/preprocessed.aidl -pout/soong/.intermediates/hardware/interfaces/common/fmq/aidl/android.hardware.common.fmq_interface/1/preprocessed.aidl -pout/soong/.intermediates/system/hardware/interfaces/media/android.media.audio.common.types_interface/5/preprocessed.aidl --previous_api_dir=hardware/interfaces/vibrator/aidl/aidl_api/android.hardware.vibrator/4 --previous_hash dec155403ea3aa5395b0226de399873712b16082 --ninja -d out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen/android/hardware/vibrator/IVibrator.java.d -o out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen -Iframeworks/native/aidl/binder -Nhardware/interfaces/vibrator/aidl hardware/interfaces/vibrator/aidl/android/hardware/vibrator/IVibrator.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.hardware.vibrator;
public interface IVibrator extends android.os.IInterface
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
  /** Default implementation for IVibrator. */
  public static class Default implements android.hardware.vibrator.IVibrator
  {
    /** Determine capabilities of the vibrator HAL (CAP_* mask) */
    @Override public int getCapabilities() throws android.os.RemoteException
    {
      return 0;
    }
    /**
     * Turn off vibrator
     * 
     * Cancel a previously-started vibration, if any. If a previously-started vibration is
     * associated with a callback, then onComplete should still be called on that callback.
     * This also clears any vibration amplitude set by `setAmplitude` and disables external
     * control, if enabled.
     */
    @Override public void off() throws android.os.RemoteException
    {
    }
    /**
     * Turn on vibrator
     * 
     * This function must only be called after the previous timeout has expired or
     * was canceled (through off()). A callback is only expected to be supported when
     * getCapabilities CAP_ON_CALLBACK is specified.
     * 
     * Doing this operation while the vibrator is already on is undefined behavior. Clients should
     * explicitly call off.
     * 
     * @param timeoutMs number of milliseconds to vibrate.
     * @param callback A callback used to inform Frameworks of state change, if supported.
     */
    @Override public void on(int timeoutMs, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException
    {
    }
    /**
     * Fire off a predefined haptic event.
     * 
     * A callback is only expected to be supported when getCapabilities CAP_PERFORM_CALLBACK
     * is specified.
     * 
     * Doing this operation while the vibrator is already on is undefined behavior. Clients should
     * explicitly call off.
     * 
     * @param effect The type of haptic event to trigger.
     * @param strength The intensity of haptic event to trigger.
     * @param callback A callback used to inform Frameworks of state change, if supported.
     * @return The length of time the event is expected to take in
     *     milliseconds. This doesn't need to be perfectly accurate, but should be a reasonable
     *     approximation.
     */
    @Override public int perform(int effect, byte strength, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException
    {
      return 0;
    }
    /**
     * List supported effects.
     * 
     * Return the effects which are supported (an effect is expected to be supported at every
     * strength level.
     */
    @Override public int[] getSupportedEffects() throws android.os.RemoteException
    {
      return null;
    }
    /**
     * Sets the motor's vibrational amplitude.
     * 
     * Changes the force being produced by the underlying motor. This amplitude applies to
     * ongoing and future vibrations triggered by the `on()` function and does not affect
     * other vibration types. This may not be supported and this support is reflected in
     * getCapabilities (CAP_AMPLITUDE_CONTROL). When this device is under external
     * control (via setExternalControl), amplitude control may not be supported even
     * though it is supported normally. This can be checked with
     * CAP_EXTERNAL_AMPLITUDE_CONTROL.
     * 
     * @param amplitude The unitless force setting. Note that this number must
     *                  be between 0.0 (exclusive) and 1.0 (inclusive). It must
     *                  do it's best to map it onto the number of steps it does have.
     */
    @Override public void setAmplitude(float amplitude) throws android.os.RemoteException
    {
    }
    /**
     * Enables/disables control override of vibrator to audio.
     * 
     * Support is reflected in getCapabilities (CAP_EXTERNAL_CONTROL).
     * 
     * When this API is set, the vibrator control should be ceded to audio system
     * for haptic audio. While this is enabled, issuing of other commands to control
     * the vibrator is unsupported and the resulting behavior is undefined. Amplitude
     * control may or may not be supported and is reflected in the return value of
     * getCapabilities (CAP_EXTERNAL_AMPLITUDE_CONTROL) while this is enabled. When this is
     * disabled, the vibrator should resume to an off state.
     * 
     * @param enabled Whether external control should be enabled or disabled.
     */
    @Override public void setExternalControl(boolean enabled) throws android.os.RemoteException
    {
    }
    /**
     * Retrieve composition delay limit.
     * 
     * Support is reflected in getCapabilities (CAP_COMPOSE_EFFECTS).
     * 
     * @return Maximum delay for a single CompositeEffect[] entry.
     */
    @Override public int getCompositionDelayMax() throws android.os.RemoteException
    {
      return 0;
    }
    /**
     * Retrieve composition size limit.
     * 
     * Support is reflected in getCapabilities (CAP_COMPOSE_EFFECTS).
     * 
     * @return Maximum number of entries in CompositeEffect[].
     * @param maxDelayMs Maximum delay for a single CompositeEffect[] entry.
     */
    @Override public int getCompositionSizeMax() throws android.os.RemoteException
    {
      return 0;
    }
    /**
     * List of supported effect primitive.
     * 
     * Return the effect primitives which are supported by the compose API.
     * Implementations are expected to support all required primitives of the
     * interface version that they implement (see primitive definitions).
     */
    @Override public int[] getSupportedPrimitives() throws android.os.RemoteException
    {
      return null;
    }
    /**
     * Retrieve effect primitive's duration in milliseconds.
     * 
     * Support is reflected in getCapabilities (CAP_COMPOSE_EFFECTS).
     * 
     * @return Best effort estimation of effect primitive's duration.
     * @param primitive Effect primitive being queried.
     */
    @Override public int getPrimitiveDuration(int primitive) throws android.os.RemoteException
    {
      return 0;
    }
    /**
     * Fire off a string of effect primitives, combined to perform richer effects.
     * 
     * Support is reflected in getCapabilities (CAP_COMPOSE_EFFECTS).
     * 
     * Doing this operation while the vibrator is already on is undefined behavior. Clients should
     * explicitly call off. IVibratorCallback.onComplete() support is required for this API.
     * 
     * @param composite Array of composition parameters.
     */
    @Override public void compose(android.hardware.vibrator.CompositeEffect[] composite, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException
    {
    }
    /**
     * List of supported always-on effects.
     * 
     * Return the effects which are supported by the alwaysOnEnable (an effect
     * is expected to be supported at every strength level.
     */
    @Override public int[] getSupportedAlwaysOnEffects() throws android.os.RemoteException
    {
      return null;
    }
    /**
     * Enable an always-on haptic source, assigning a specific effect. An
     * always-on haptic source is a source that can be triggered externally
     * once enabled and assigned an effect to play. This may not be supported
     * and this support is reflected in getCapabilities (CAP_ALWAYS_ON_CONTROL).
     * 
     * The always-on source ID is conveyed directly to clients through
     * device/board configuration files ensuring that no ID is assigned to
     * multiple clients. No client should use this API unless explicitly
     * assigned an always-on source ID. Clients must develop their own way to
     * get IDs from vendor in a stable way. For instance, a client may expose
     * a stable API (via HAL, sysprops, or xml overlays) to allow vendor to
     * associate a hardware ID with a specific usecase. When that usecase is
     * triggered, a client would use that hardware ID here.
     * 
     * @param id The device-specific always-on source ID to enable.
     * @param effect The type of haptic event to trigger.
     * @param strength The intensity of haptic event to trigger.
     */
    @Override public void alwaysOnEnable(int id, int effect, byte strength) throws android.os.RemoteException
    {
    }
    /**
     * Disable an always-on haptic source. This may not be supported and this
     * support is reflected in getCapabilities (CAP_ALWAYS_ON_CONTROL).
     * 
     * The always-on source ID is conveyed directly to clients through
     * device/board configuration files ensuring that no ID is assigned to
     * multiple clients. No client should use this API unless explicitly
     * assigned an always-on source ID. Clients must develop their own way to
     * get IDs from vendor in a stable way.
     * 
     * @param id The device-specific always-on source ID to disable.
     */
    @Override public void alwaysOnDisable(int id) throws android.os.RemoteException
    {
    }
    /**
     * Retrieve the measured resonant frequency of the actuator.
     * 
     * This may not be supported and this support is reflected in
     * getCapabilities (CAP_GET_RESONANT_FREQUENCY)
     * 
     * @return Measured resonant frequency in Hz. Non-zero value if supported,
     *         or value should be ignored if not supported.
     */
    @Override public float getResonantFrequency() throws android.os.RemoteException
    {
      return 0.0f;
    }
    /**
     * Retrieve the measured Q factor.
     * 
     * This may not be supported and this support is reflected in
     * getCapabilities (CAP_GET_Q_FACTOR)
     * 
     * @return Measured Q factor. Non-zero value if supported, or value should be
     *         ignored if not supported.
     */
    @Override public float getQFactor() throws android.os.RemoteException
    {
      return 0.0f;
    }
    /**
     * Retrieve the frequency resolution used in getBandwidthAmplitudeMap() in units of hertz
     * 
     * This may not be supported and this support is reflected in
     * getCapabilities (CAP_FREQUENCY_CONTROL).
     * 
     * @return The frequency resolution of the bandwidth amplitude map.
     *         Non-zero value if supported, or value should be ignored if not supported.
     * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
     * implemented even if CAP_FREQUENCY_CONTROL capability is reported.
     */
    @Deprecated
    @Override public float getFrequencyResolution() throws android.os.RemoteException
    {
      return 0.0f;
    }
    /**
     * Retrieve the minimum allowed frequency in units of hertz
     * 
     * This may not be supported and this support is reflected in
     * getCapabilities (CAP_FREQUENCY_CONTROL).
     * 
     * @return The minimum frequency allowed. Non-zero value if supported,
     *         or value should be ignored if not supported.
     * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
     * implemented even if CAP_FREQUENCY_CONTROL capability is reported.
     */
    @Deprecated
    @Override public float getFrequencyMinimum() throws android.os.RemoteException
    {
      return 0.0f;
    }
    /**
     * Retrieve the output acceleration amplitude values per frequency supported
     * 
     * This may not be supported and this support is reflected in
     * getCapabilities (CAP_FREQUENCY_CONTROL).
     * 
     * The mapping is represented as a list of amplitude values in the inclusive range [0.0, 1.0].
     * The first value represents the amplitude at the frequency returned by getFrequencyMinimum().
     * Each subsequent element is the amplitude at the next supported frequency, in increments
     * of getFrequencyResolution(). The value returned by getResonantFrequency() must be
     * represented in the returned list.
     * 
     * The amplitude values represent the maximum output acceleration amplitude supported for each
     * given frequency. Equal amplitude values for different frequencies represent equal output
     * accelerations.
     * 
     * @return The maximum output acceleration amplitude for each supported frequency,
     *         starting at getMinimumFrequency()
     * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
     * implemented even if CAP_FREQUENCY_CONTROL capability is reported.
     */
    @Deprecated
    @Override public float[] getBandwidthAmplitudeMap() throws android.os.RemoteException
    {
      return null;
    }
    /**
     * Retrieve the maximum duration allowed for any primitive PWLE in units of milliseconds.
     * 
     * This may not be supported and this support is reflected in
     * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS).
     * 
     * @return The maximum duration allowed for a single PrimitivePwle.
     *         Non-zero value if supported, or value should be ignored if not supported.
     * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
     * implemented. Use `IVibrator.getPwleV2PrimitiveDurationMaxMillis` instead.
     */
    @Deprecated
    @Override public int getPwlePrimitiveDurationMax() throws android.os.RemoteException
    {
      return 0;
    }
    /**
     * Retrieve the maximum count for allowed PWLEs in one composition.
     * 
     * This may not be supported and this support is reflected in
     * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS).
     * 
     * @return The maximum count allowed. Non-zero value if supported,
     *         or value should be ignored if not supported.
     * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
     * implemented. Use `IVibrator.getPwleV2CompositionSizeMax` instead.
     */
    @Deprecated
    @Override public int getPwleCompositionSizeMax() throws android.os.RemoteException
    {
      return 0;
    }
    /**
     * List of supported braking mechanism.
     * 
     * This may not be supported and this support is reflected in
     * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS).
     * Implementations are optional but encouraged if available.
     * 
     * @return The braking mechanisms which are supported by the composePwle API.
     * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
     * implemented.
     */
    @Deprecated
    @Override public int[] getSupportedBraking() throws android.os.RemoteException
    {
      return null;
    }
    /**
     * Fire off a string of PWLEs.
     * 
     * This may not be supported and this support is reflected in
     * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS).
     * 
     * Doing this operation while the vibrator is already on is undefined behavior. Clients should
     * explicitly call off. IVibratorCallback.onComplete() support is required for this API.
     * 
     * @param composite Array of PWLEs.
     * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
     * implemented. Use `IVibrator.composePwleV2` instead.
     */
    @Deprecated
    @Override public void composePwle(android.hardware.vibrator.PrimitivePwle[] composite, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException
    {
    }
    /**
     * Fire off a vendor-defined haptic event.
     * 
     * This may not be supported and this support is reflected in
     * getCapabilities (CAP_PERFORM_VENDOR_EFFECTS).
     * 
     * The duration of the effect is unknown and can be undefined for looping effects.
     * IVibratorCallback.onComplete() support is required for this API.
     * 
     * Doing this operation while the vibrator is already on is undefined behavior. Clients should
     * explicitly call off.
     * 
     * @param effect The vendor data representing the effect to be performed.
     * @param callback A callback used to inform Frameworks of state change.
     * @throws :
     *         - EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
     *         - EX_ILLEGAL_ARGUMENT for bad framework parameters, e.g. scale or effect strength.
     *         - EX_SERVICE_SPECIFIC for bad vendor data, vibration is not triggered.
     */
    @Override public void performVendorEffect(android.hardware.vibrator.VendorEffect vendorEffect, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException
    {
    }
    /**
     * Retrieves a mapping of vibration frequency (Hz) to the maximum achievable output
     * acceleration (Gs) the device can reach at that frequency.
     * 
     * The map, represented as a list of `FrequencyAccelerationMapEntry` (frequency, output
     * acceleration) pairs, defines the device's frequency response. The platform uses the minimum
     * and maximum frequency values to determine the supported input range for
     * `IVibrator.composePwleV2`. Output acceleration values are used to identify a frequency range
     * suitable to safely play perceivable vibrations with a simple API. The map is also exposed for
     * developers using an advanced API.
     * 
     * The platform does not impose specific requirements on map resolution which can vary
     * depending on the shape of device output curve. The values will be linearly interpolated
     * during lookups. The platform will provide a simple API, defined by a frequency range from
     * where the output acceleration first exceeds a minimum threshold of 10 db SL, to where the
     * output acceleration last exceeds that threshold.
     * 
     * This may not be supported and this support is reflected in getCapabilities
     * (CAP_FREQUENCY_CONTROL). If this is supported, it's expected to be non-empty and
     * describe a valid non-empty frequency range where the simple API can be defined.
     * 
     * For devices that also support CAP_GET_RESONANT_FREQUENCY, the resonant frequency must be
     * within the frequency range defined by the simple API.
     * 
     * @return A list of map entries representing the frequency to max acceleration
     *         mapping.
     * @throws EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
     */
    @Override public java.util.List<android.hardware.vibrator.FrequencyAccelerationMapEntry> getFrequencyToOutputAccelerationMap() throws android.os.RemoteException
    {
      return null;
    }
    /**
     * Retrieve the maximum duration allowed for any primitive PWLE in units of
     * milliseconds.
     * 
     * This may not be supported and this support is reflected in
     * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS_V2).
     * 
     * @return The maximum duration allowed for a single PrimitivePwle. Non-zero value if supported.
     * @throws EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
     */
    @Override public int getPwleV2PrimitiveDurationMaxMillis() throws android.os.RemoteException
    {
      return 0;
    }
    /**
     * Retrieve the maximum number of PWLE primitives input supported by IVibrator.composePwleV2.
     * 
     * This may not be supported and this support is reflected in
     * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS_V2). Devices supporting
     * PWLE effects must support effects with at least 16 PwleV2Primitive.
     * 
     * @return The maximum count allowed. Non-zero value if supported.
     * @throws EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
     */
    @Override public int getPwleV2CompositionSizeMax() throws android.os.RemoteException
    {
      return 0;
    }
    /**
     * Retrieves the minimum duration (in milliseconds) of any segment within a
     * PWLE effect. Devices supporting PWLE effects must support a minimum ramp
     * time of 20 milliseconds.
     * 
     * This may not be supported and this support is reflected in
     * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS_V2).
     * 
     * @return The minimum duration allowed for a single PrimitivePwle. Non-zero value if supported.
     * @throws EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
     */
    @Override public int getPwleV2PrimitiveDurationMinMillis() throws android.os.RemoteException
    {
      return 0;
    }
    /**
     * Play composed sequence of PWLEs with optional callback upon completion.
     * 
     * A PWLE (Piecewise-Linear Envelope) effect defines a vibration waveform using amplitude and
     * frequency points. The envelope linearly interpolates both amplitude and frequency between
     * consecutive points, creating smooth transitions in the vibration pattern.
     * 
     * This may not be supported and this support is reflected in
     * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS_V2).
     * 
     * Note: Devices reporting CAP_COMPOSE_PWLE_EFFECTS_V2 support must also have the
     * CAP_FREQUENCY_CONTROL and CAP_GET_RESONANT_FREQUENCY capabilities, and provide a valid
     * frequency to output acceleration map.
     * 
     * Doing this operation while the vibrator is already on is undefined behavior. Clients should
     * explicitly call off. IVibratorCallback.onComplete() support is required for this API.
     * 
     * @param composite A CompositePwleV2 representing a composite vibration effect, composed of an
     *                  array of primitives that define the PWLE (Piecewise-Linear Envelope).
     */
    @Override public void composePwleV2(android.hardware.vibrator.CompositePwleV2 composite, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException
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
  public static abstract class Stub extends android.os.Binder implements android.hardware.vibrator.IVibrator
  {
    /** Construct the stub and attach it to the interface. */
    @SuppressWarnings("this-escape")
    public Stub()
    {
      this.markVintfStability();
      this.attachInterface(this, DESCRIPTOR);
    }
    /**
     * Cast an IBinder object into an android.hardware.vibrator.IVibrator interface,
     * generating a proxy if needed.
     */
    public static android.hardware.vibrator.IVibrator asInterface(android.os.IBinder obj)
    {
      if ((obj==null)) {
        return null;
      }
      android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
      if (((iin!=null)&&(iin instanceof android.hardware.vibrator.IVibrator))) {
        return ((android.hardware.vibrator.IVibrator)iin);
      }
      return new android.hardware.vibrator.IVibrator.Stub.Proxy(obj);
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
        case TRANSACTION_off:
        {
          this.off();
          reply.writeNoException();
          break;
        }
        case TRANSACTION_on:
        {
          int _arg0;
          _arg0 = data.readInt();
          android.hardware.vibrator.IVibratorCallback _arg1;
          _arg1 = android.hardware.vibrator.IVibratorCallback.Stub.asInterface(data.readStrongBinder());
          data.enforceNoDataAvail();
          this.on(_arg0, _arg1);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_perform:
        {
          int _arg0;
          _arg0 = data.readInt();
          byte _arg1;
          _arg1 = data.readByte();
          android.hardware.vibrator.IVibratorCallback _arg2;
          _arg2 = android.hardware.vibrator.IVibratorCallback.Stub.asInterface(data.readStrongBinder());
          data.enforceNoDataAvail();
          int _result = this.perform(_arg0, _arg1, _arg2);
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        case TRANSACTION_getSupportedEffects:
        {
          int[] _result = this.getSupportedEffects();
          reply.writeNoException();
          reply.writeIntArray(_result);
          break;
        }
        case TRANSACTION_setAmplitude:
        {
          float _arg0;
          _arg0 = data.readFloat();
          data.enforceNoDataAvail();
          this.setAmplitude(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_setExternalControl:
        {
          boolean _arg0;
          _arg0 = data.readBoolean();
          data.enforceNoDataAvail();
          this.setExternalControl(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_getCompositionDelayMax:
        {
          int _result = this.getCompositionDelayMax();
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        case TRANSACTION_getCompositionSizeMax:
        {
          int _result = this.getCompositionSizeMax();
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        case TRANSACTION_getSupportedPrimitives:
        {
          int[] _result = this.getSupportedPrimitives();
          reply.writeNoException();
          reply.writeIntArray(_result);
          break;
        }
        case TRANSACTION_getPrimitiveDuration:
        {
          int _arg0;
          _arg0 = data.readInt();
          data.enforceNoDataAvail();
          int _result = this.getPrimitiveDuration(_arg0);
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        case TRANSACTION_compose:
        {
          android.hardware.vibrator.CompositeEffect[] _arg0;
          _arg0 = data.createTypedArray(android.hardware.vibrator.CompositeEffect.CREATOR);
          android.hardware.vibrator.IVibratorCallback _arg1;
          _arg1 = android.hardware.vibrator.IVibratorCallback.Stub.asInterface(data.readStrongBinder());
          data.enforceNoDataAvail();
          this.compose(_arg0, _arg1);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_getSupportedAlwaysOnEffects:
        {
          int[] _result = this.getSupportedAlwaysOnEffects();
          reply.writeNoException();
          reply.writeIntArray(_result);
          break;
        }
        case TRANSACTION_alwaysOnEnable:
        {
          int _arg0;
          _arg0 = data.readInt();
          int _arg1;
          _arg1 = data.readInt();
          byte _arg2;
          _arg2 = data.readByte();
          data.enforceNoDataAvail();
          this.alwaysOnEnable(_arg0, _arg1, _arg2);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_alwaysOnDisable:
        {
          int _arg0;
          _arg0 = data.readInt();
          data.enforceNoDataAvail();
          this.alwaysOnDisable(_arg0);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_getResonantFrequency:
        {
          float _result = this.getResonantFrequency();
          reply.writeNoException();
          reply.writeFloat(_result);
          break;
        }
        case TRANSACTION_getQFactor:
        {
          float _result = this.getQFactor();
          reply.writeNoException();
          reply.writeFloat(_result);
          break;
        }
        case TRANSACTION_getFrequencyResolution:
        {
          float _result = this.getFrequencyResolution();
          reply.writeNoException();
          reply.writeFloat(_result);
          break;
        }
        case TRANSACTION_getFrequencyMinimum:
        {
          float _result = this.getFrequencyMinimum();
          reply.writeNoException();
          reply.writeFloat(_result);
          break;
        }
        case TRANSACTION_getBandwidthAmplitudeMap:
        {
          float[] _result = this.getBandwidthAmplitudeMap();
          reply.writeNoException();
          reply.writeFloatArray(_result);
          break;
        }
        case TRANSACTION_getPwlePrimitiveDurationMax:
        {
          int _result = this.getPwlePrimitiveDurationMax();
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        case TRANSACTION_getPwleCompositionSizeMax:
        {
          int _result = this.getPwleCompositionSizeMax();
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        case TRANSACTION_getSupportedBraking:
        {
          int[] _result = this.getSupportedBraking();
          reply.writeNoException();
          reply.writeIntArray(_result);
          break;
        }
        case TRANSACTION_composePwle:
        {
          android.hardware.vibrator.PrimitivePwle[] _arg0;
          _arg0 = data.createTypedArray(android.hardware.vibrator.PrimitivePwle.CREATOR);
          android.hardware.vibrator.IVibratorCallback _arg1;
          _arg1 = android.hardware.vibrator.IVibratorCallback.Stub.asInterface(data.readStrongBinder());
          data.enforceNoDataAvail();
          this.composePwle(_arg0, _arg1);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_performVendorEffect:
        {
          android.hardware.vibrator.VendorEffect _arg0;
          _arg0 = data.readTypedObject(android.hardware.vibrator.VendorEffect.CREATOR);
          android.hardware.vibrator.IVibratorCallback _arg1;
          _arg1 = android.hardware.vibrator.IVibratorCallback.Stub.asInterface(data.readStrongBinder());
          data.enforceNoDataAvail();
          this.performVendorEffect(_arg0, _arg1);
          reply.writeNoException();
          break;
        }
        case TRANSACTION_getFrequencyToOutputAccelerationMap:
        {
          java.util.List<android.hardware.vibrator.FrequencyAccelerationMapEntry> _result = this.getFrequencyToOutputAccelerationMap();
          reply.writeNoException();
          reply.writeTypedList(_result, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
          break;
        }
        case TRANSACTION_getPwleV2PrimitiveDurationMaxMillis:
        {
          int _result = this.getPwleV2PrimitiveDurationMaxMillis();
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        case TRANSACTION_getPwleV2CompositionSizeMax:
        {
          int _result = this.getPwleV2CompositionSizeMax();
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        case TRANSACTION_getPwleV2PrimitiveDurationMinMillis:
        {
          int _result = this.getPwleV2PrimitiveDurationMinMillis();
          reply.writeNoException();
          reply.writeInt(_result);
          break;
        }
        case TRANSACTION_composePwleV2:
        {
          android.hardware.vibrator.CompositePwleV2 _arg0;
          _arg0 = data.readTypedObject(android.hardware.vibrator.CompositePwleV2.CREATOR);
          android.hardware.vibrator.IVibratorCallback _arg1;
          _arg1 = android.hardware.vibrator.IVibratorCallback.Stub.asInterface(data.readStrongBinder());
          data.enforceNoDataAvail();
          this.composePwleV2(_arg0, _arg1);
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
    private static final class Proxy implements android.hardware.vibrator.IVibrator
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
      /** Determine capabilities of the vibrator HAL (CAP_* mask) */
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
      /**
       * Turn off vibrator
       * 
       * Cancel a previously-started vibration, if any. If a previously-started vibration is
       * associated with a callback, then onComplete should still be called on that callback.
       * This also clears any vibration amplitude set by `setAmplitude` and disables external
       * control, if enabled.
       */
      @Override public void off() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_off, _data, _reply, 0);
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
       * Turn on vibrator
       * 
       * This function must only be called after the previous timeout has expired or
       * was canceled (through off()). A callback is only expected to be supported when
       * getCapabilities CAP_ON_CALLBACK is specified.
       * 
       * Doing this operation while the vibrator is already on is undefined behavior. Clients should
       * explicitly call off.
       * 
       * @param timeoutMs number of milliseconds to vibrate.
       * @param callback A callback used to inform Frameworks of state change, if supported.
       */
      @Override public void on(int timeoutMs, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(timeoutMs);
          _data.writeStrongInterface(callback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_on, _data, _reply, 0);
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
       * Fire off a predefined haptic event.
       * 
       * A callback is only expected to be supported when getCapabilities CAP_PERFORM_CALLBACK
       * is specified.
       * 
       * Doing this operation while the vibrator is already on is undefined behavior. Clients should
       * explicitly call off.
       * 
       * @param effect The type of haptic event to trigger.
       * @param strength The intensity of haptic event to trigger.
       * @param callback A callback used to inform Frameworks of state change, if supported.
       * @return The length of time the event is expected to take in
       *     milliseconds. This doesn't need to be perfectly accurate, but should be a reasonable
       *     approximation.
       */
      @Override public int perform(int effect, byte strength, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(effect);
          _data.writeByte(strength);
          _data.writeStrongInterface(callback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_perform, _data, _reply, 0);
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
      /**
       * List supported effects.
       * 
       * Return the effects which are supported (an effect is expected to be supported at every
       * strength level.
       */
      @Override public int[] getSupportedEffects() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int[] _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getSupportedEffects, _data, _reply, 0);
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
      /**
       * Sets the motor's vibrational amplitude.
       * 
       * Changes the force being produced by the underlying motor. This amplitude applies to
       * ongoing and future vibrations triggered by the `on()` function and does not affect
       * other vibration types. This may not be supported and this support is reflected in
       * getCapabilities (CAP_AMPLITUDE_CONTROL). When this device is under external
       * control (via setExternalControl), amplitude control may not be supported even
       * though it is supported normally. This can be checked with
       * CAP_EXTERNAL_AMPLITUDE_CONTROL.
       * 
       * @param amplitude The unitless force setting. Note that this number must
       *                  be between 0.0 (exclusive) and 1.0 (inclusive). It must
       *                  do it's best to map it onto the number of steps it does have.
       */
      @Override public void setAmplitude(float amplitude) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeFloat(amplitude);
          boolean _status = mRemote.transact(Stub.TRANSACTION_setAmplitude, _data, _reply, 0);
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
       * Enables/disables control override of vibrator to audio.
       * 
       * Support is reflected in getCapabilities (CAP_EXTERNAL_CONTROL).
       * 
       * When this API is set, the vibrator control should be ceded to audio system
       * for haptic audio. While this is enabled, issuing of other commands to control
       * the vibrator is unsupported and the resulting behavior is undefined. Amplitude
       * control may or may not be supported and is reflected in the return value of
       * getCapabilities (CAP_EXTERNAL_AMPLITUDE_CONTROL) while this is enabled. When this is
       * disabled, the vibrator should resume to an off state.
       * 
       * @param enabled Whether external control should be enabled or disabled.
       */
      @Override public void setExternalControl(boolean enabled) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeBoolean(enabled);
          boolean _status = mRemote.transact(Stub.TRANSACTION_setExternalControl, _data, _reply, 0);
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
       * Retrieve composition delay limit.
       * 
       * Support is reflected in getCapabilities (CAP_COMPOSE_EFFECTS).
       * 
       * @return Maximum delay for a single CompositeEffect[] entry.
       */
      @Override public int getCompositionDelayMax() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getCompositionDelayMax, _data, _reply, 0);
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
      /**
       * Retrieve composition size limit.
       * 
       * Support is reflected in getCapabilities (CAP_COMPOSE_EFFECTS).
       * 
       * @return Maximum number of entries in CompositeEffect[].
       * @param maxDelayMs Maximum delay for a single CompositeEffect[] entry.
       */
      @Override public int getCompositionSizeMax() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getCompositionSizeMax, _data, _reply, 0);
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
      /**
       * List of supported effect primitive.
       * 
       * Return the effect primitives which are supported by the compose API.
       * Implementations are expected to support all required primitives of the
       * interface version that they implement (see primitive definitions).
       */
      @Override public int[] getSupportedPrimitives() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int[] _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getSupportedPrimitives, _data, _reply, 0);
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
      /**
       * Retrieve effect primitive's duration in milliseconds.
       * 
       * Support is reflected in getCapabilities (CAP_COMPOSE_EFFECTS).
       * 
       * @return Best effort estimation of effect primitive's duration.
       * @param primitive Effect primitive being queried.
       */
      @Override public int getPrimitiveDuration(int primitive) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(primitive);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getPrimitiveDuration, _data, _reply, 0);
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
      /**
       * Fire off a string of effect primitives, combined to perform richer effects.
       * 
       * Support is reflected in getCapabilities (CAP_COMPOSE_EFFECTS).
       * 
       * Doing this operation while the vibrator is already on is undefined behavior. Clients should
       * explicitly call off. IVibratorCallback.onComplete() support is required for this API.
       * 
       * @param composite Array of composition parameters.
       */
      @Override public void compose(android.hardware.vibrator.CompositeEffect[] composite, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeTypedArray(composite, 0);
          _data.writeStrongInterface(callback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_compose, _data, _reply, 0);
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
       * List of supported always-on effects.
       * 
       * Return the effects which are supported by the alwaysOnEnable (an effect
       * is expected to be supported at every strength level.
       */
      @Override public int[] getSupportedAlwaysOnEffects() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int[] _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getSupportedAlwaysOnEffects, _data, _reply, 0);
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
      /**
       * Enable an always-on haptic source, assigning a specific effect. An
       * always-on haptic source is a source that can be triggered externally
       * once enabled and assigned an effect to play. This may not be supported
       * and this support is reflected in getCapabilities (CAP_ALWAYS_ON_CONTROL).
       * 
       * The always-on source ID is conveyed directly to clients through
       * device/board configuration files ensuring that no ID is assigned to
       * multiple clients. No client should use this API unless explicitly
       * assigned an always-on source ID. Clients must develop their own way to
       * get IDs from vendor in a stable way. For instance, a client may expose
       * a stable API (via HAL, sysprops, or xml overlays) to allow vendor to
       * associate a hardware ID with a specific usecase. When that usecase is
       * triggered, a client would use that hardware ID here.
       * 
       * @param id The device-specific always-on source ID to enable.
       * @param effect The type of haptic event to trigger.
       * @param strength The intensity of haptic event to trigger.
       */
      @Override public void alwaysOnEnable(int id, int effect, byte strength) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(id);
          _data.writeInt(effect);
          _data.writeByte(strength);
          boolean _status = mRemote.transact(Stub.TRANSACTION_alwaysOnEnable, _data, _reply, 0);
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
       * Disable an always-on haptic source. This may not be supported and this
       * support is reflected in getCapabilities (CAP_ALWAYS_ON_CONTROL).
       * 
       * The always-on source ID is conveyed directly to clients through
       * device/board configuration files ensuring that no ID is assigned to
       * multiple clients. No client should use this API unless explicitly
       * assigned an always-on source ID. Clients must develop their own way to
       * get IDs from vendor in a stable way.
       * 
       * @param id The device-specific always-on source ID to disable.
       */
      @Override public void alwaysOnDisable(int id) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeInt(id);
          boolean _status = mRemote.transact(Stub.TRANSACTION_alwaysOnDisable, _data, _reply, 0);
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
       * Retrieve the measured resonant frequency of the actuator.
       * 
       * This may not be supported and this support is reflected in
       * getCapabilities (CAP_GET_RESONANT_FREQUENCY)
       * 
       * @return Measured resonant frequency in Hz. Non-zero value if supported,
       *         or value should be ignored if not supported.
       */
      @Override public float getResonantFrequency() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        float _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getResonantFrequency, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
          _result = _reply.readFloat();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * Retrieve the measured Q factor.
       * 
       * This may not be supported and this support is reflected in
       * getCapabilities (CAP_GET_Q_FACTOR)
       * 
       * @return Measured Q factor. Non-zero value if supported, or value should be
       *         ignored if not supported.
       */
      @Override public float getQFactor() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        float _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getQFactor, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
          _result = _reply.readFloat();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * Retrieve the frequency resolution used in getBandwidthAmplitudeMap() in units of hertz
       * 
       * This may not be supported and this support is reflected in
       * getCapabilities (CAP_FREQUENCY_CONTROL).
       * 
       * @return The frequency resolution of the bandwidth amplitude map.
       *         Non-zero value if supported, or value should be ignored if not supported.
       * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
       * implemented even if CAP_FREQUENCY_CONTROL capability is reported.
       */
      @Deprecated
      @Override public float getFrequencyResolution() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        float _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getFrequencyResolution, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
          _result = _reply.readFloat();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * Retrieve the minimum allowed frequency in units of hertz
       * 
       * This may not be supported and this support is reflected in
       * getCapabilities (CAP_FREQUENCY_CONTROL).
       * 
       * @return The minimum frequency allowed. Non-zero value if supported,
       *         or value should be ignored if not supported.
       * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
       * implemented even if CAP_FREQUENCY_CONTROL capability is reported.
       */
      @Deprecated
      @Override public float getFrequencyMinimum() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        float _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getFrequencyMinimum, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
          _result = _reply.readFloat();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * Retrieve the output acceleration amplitude values per frequency supported
       * 
       * This may not be supported and this support is reflected in
       * getCapabilities (CAP_FREQUENCY_CONTROL).
       * 
       * The mapping is represented as a list of amplitude values in the inclusive range [0.0, 1.0].
       * The first value represents the amplitude at the frequency returned by getFrequencyMinimum().
       * Each subsequent element is the amplitude at the next supported frequency, in increments
       * of getFrequencyResolution(). The value returned by getResonantFrequency() must be
       * represented in the returned list.
       * 
       * The amplitude values represent the maximum output acceleration amplitude supported for each
       * given frequency. Equal amplitude values for different frequencies represent equal output
       * accelerations.
       * 
       * @return The maximum output acceleration amplitude for each supported frequency,
       *         starting at getMinimumFrequency()
       * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
       * implemented even if CAP_FREQUENCY_CONTROL capability is reported.
       */
      @Deprecated
      @Override public float[] getBandwidthAmplitudeMap() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        float[] _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getBandwidthAmplitudeMap, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
          _result = _reply.createFloatArray();
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * Retrieve the maximum duration allowed for any primitive PWLE in units of milliseconds.
       * 
       * This may not be supported and this support is reflected in
       * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS).
       * 
       * @return The maximum duration allowed for a single PrimitivePwle.
       *         Non-zero value if supported, or value should be ignored if not supported.
       * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
       * implemented. Use `IVibrator.getPwleV2PrimitiveDurationMaxMillis` instead.
       */
      @Deprecated
      @Override public int getPwlePrimitiveDurationMax() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getPwlePrimitiveDurationMax, _data, _reply, 0);
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
      /**
       * Retrieve the maximum count for allowed PWLEs in one composition.
       * 
       * This may not be supported and this support is reflected in
       * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS).
       * 
       * @return The maximum count allowed. Non-zero value if supported,
       *         or value should be ignored if not supported.
       * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
       * implemented. Use `IVibrator.getPwleV2CompositionSizeMax` instead.
       */
      @Deprecated
      @Override public int getPwleCompositionSizeMax() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getPwleCompositionSizeMax, _data, _reply, 0);
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
      /**
       * List of supported braking mechanism.
       * 
       * This may not be supported and this support is reflected in
       * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS).
       * Implementations are optional but encouraged if available.
       * 
       * @return The braking mechanisms which are supported by the composePwle API.
       * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
       * implemented.
       */
      @Deprecated
      @Override public int[] getSupportedBraking() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int[] _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getSupportedBraking, _data, _reply, 0);
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
      /**
       * Fire off a string of PWLEs.
       * 
       * This may not be supported and this support is reflected in
       * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS).
       * 
       * Doing this operation while the vibrator is already on is undefined behavior. Clients should
       * explicitly call off. IVibratorCallback.onComplete() support is required for this API.
       * 
       * @param composite Array of PWLEs.
       * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
       * implemented. Use `IVibrator.composePwleV2` instead.
       */
      @Deprecated
      @Override public void composePwle(android.hardware.vibrator.PrimitivePwle[] composite, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeTypedArray(composite, 0);
          _data.writeStrongInterface(callback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_composePwle, _data, _reply, 0);
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
       * Fire off a vendor-defined haptic event.
       * 
       * This may not be supported and this support is reflected in
       * getCapabilities (CAP_PERFORM_VENDOR_EFFECTS).
       * 
       * The duration of the effect is unknown and can be undefined for looping effects.
       * IVibratorCallback.onComplete() support is required for this API.
       * 
       * Doing this operation while the vibrator is already on is undefined behavior. Clients should
       * explicitly call off.
       * 
       * @param effect The vendor data representing the effect to be performed.
       * @param callback A callback used to inform Frameworks of state change.
       * @throws :
       *         - EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
       *         - EX_ILLEGAL_ARGUMENT for bad framework parameters, e.g. scale or effect strength.
       *         - EX_SERVICE_SPECIFIC for bad vendor data, vibration is not triggered.
       */
      @Override public void performVendorEffect(android.hardware.vibrator.VendorEffect vendorEffect, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeTypedObject(vendorEffect, 0);
          _data.writeStrongInterface(callback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_performVendorEffect, _data, _reply, 0);
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
       * Retrieves a mapping of vibration frequency (Hz) to the maximum achievable output
       * acceleration (Gs) the device can reach at that frequency.
       * 
       * The map, represented as a list of `FrequencyAccelerationMapEntry` (frequency, output
       * acceleration) pairs, defines the device's frequency response. The platform uses the minimum
       * and maximum frequency values to determine the supported input range for
       * `IVibrator.composePwleV2`. Output acceleration values are used to identify a frequency range
       * suitable to safely play perceivable vibrations with a simple API. The map is also exposed for
       * developers using an advanced API.
       * 
       * The platform does not impose specific requirements on map resolution which can vary
       * depending on the shape of device output curve. The values will be linearly interpolated
       * during lookups. The platform will provide a simple API, defined by a frequency range from
       * where the output acceleration first exceeds a minimum threshold of 10 db SL, to where the
       * output acceleration last exceeds that threshold.
       * 
       * This may not be supported and this support is reflected in getCapabilities
       * (CAP_FREQUENCY_CONTROL). If this is supported, it's expected to be non-empty and
       * describe a valid non-empty frequency range where the simple API can be defined.
       * 
       * For devices that also support CAP_GET_RESONANT_FREQUENCY, the resonant frequency must be
       * within the frequency range defined by the simple API.
       * 
       * @return A list of map entries representing the frequency to max acceleration
       *         mapping.
       * @throws EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
       */
      @Override public java.util.List<android.hardware.vibrator.FrequencyAccelerationMapEntry> getFrequencyToOutputAccelerationMap() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        java.util.List<android.hardware.vibrator.FrequencyAccelerationMapEntry> _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getFrequencyToOutputAccelerationMap, _data, _reply, 0);
          if (!_status) {
            throw new android.os.RemoteException("Unimplemented");
          }
          _reply.readException();
          _result = _reply.createTypedArrayList(android.hardware.vibrator.FrequencyAccelerationMapEntry.CREATOR);
        }
        finally {
          _reply.recycle();
          _data.recycle();
        }
        return _result;
      }
      /**
       * Retrieve the maximum duration allowed for any primitive PWLE in units of
       * milliseconds.
       * 
       * This may not be supported and this support is reflected in
       * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS_V2).
       * 
       * @return The maximum duration allowed for a single PrimitivePwle. Non-zero value if supported.
       * @throws EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
       */
      @Override public int getPwleV2PrimitiveDurationMaxMillis() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getPwleV2PrimitiveDurationMaxMillis, _data, _reply, 0);
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
      /**
       * Retrieve the maximum number of PWLE primitives input supported by IVibrator.composePwleV2.
       * 
       * This may not be supported and this support is reflected in
       * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS_V2). Devices supporting
       * PWLE effects must support effects with at least 16 PwleV2Primitive.
       * 
       * @return The maximum count allowed. Non-zero value if supported.
       * @throws EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
       */
      @Override public int getPwleV2CompositionSizeMax() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getPwleV2CompositionSizeMax, _data, _reply, 0);
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
      /**
       * Retrieves the minimum duration (in milliseconds) of any segment within a
       * PWLE effect. Devices supporting PWLE effects must support a minimum ramp
       * time of 20 milliseconds.
       * 
       * This may not be supported and this support is reflected in
       * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS_V2).
       * 
       * @return The minimum duration allowed for a single PrimitivePwle. Non-zero value if supported.
       * @throws EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
       */
      @Override public int getPwleV2PrimitiveDurationMinMillis() throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        int _result;
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          boolean _status = mRemote.transact(Stub.TRANSACTION_getPwleV2PrimitiveDurationMinMillis, _data, _reply, 0);
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
      /**
       * Play composed sequence of PWLEs with optional callback upon completion.
       * 
       * A PWLE (Piecewise-Linear Envelope) effect defines a vibration waveform using amplitude and
       * frequency points. The envelope linearly interpolates both amplitude and frequency between
       * consecutive points, creating smooth transitions in the vibration pattern.
       * 
       * This may not be supported and this support is reflected in
       * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS_V2).
       * 
       * Note: Devices reporting CAP_COMPOSE_PWLE_EFFECTS_V2 support must also have the
       * CAP_FREQUENCY_CONTROL and CAP_GET_RESONANT_FREQUENCY capabilities, and provide a valid
       * frequency to output acceleration map.
       * 
       * Doing this operation while the vibrator is already on is undefined behavior. Clients should
       * explicitly call off. IVibratorCallback.onComplete() support is required for this API.
       * 
       * @param composite A CompositePwleV2 representing a composite vibration effect, composed of an
       *                  array of primitives that define the PWLE (Piecewise-Linear Envelope).
       */
      @Override public void composePwleV2(android.hardware.vibrator.CompositePwleV2 composite, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException
      {
        android.os.Parcel _data = android.os.Parcel.obtain(asBinder());
        android.os.Parcel _reply = android.os.Parcel.obtain();
        try {
          _data.writeInterfaceToken(DESCRIPTOR);
          _data.writeTypedObject(composite, 0);
          _data.writeStrongInterface(callback);
          boolean _status = mRemote.transact(Stub.TRANSACTION_composePwleV2, _data, _reply, 0);
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
    static final int TRANSACTION_off = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
    static final int TRANSACTION_on = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
    static final int TRANSACTION_perform = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
    static final int TRANSACTION_getSupportedEffects = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
    static final int TRANSACTION_setAmplitude = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
    static final int TRANSACTION_setExternalControl = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
    static final int TRANSACTION_getCompositionDelayMax = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
    static final int TRANSACTION_getCompositionSizeMax = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
    static final int TRANSACTION_getSupportedPrimitives = (android.os.IBinder.FIRST_CALL_TRANSACTION + 9);
    static final int TRANSACTION_getPrimitiveDuration = (android.os.IBinder.FIRST_CALL_TRANSACTION + 10);
    static final int TRANSACTION_compose = (android.os.IBinder.FIRST_CALL_TRANSACTION + 11);
    static final int TRANSACTION_getSupportedAlwaysOnEffects = (android.os.IBinder.FIRST_CALL_TRANSACTION + 12);
    static final int TRANSACTION_alwaysOnEnable = (android.os.IBinder.FIRST_CALL_TRANSACTION + 13);
    static final int TRANSACTION_alwaysOnDisable = (android.os.IBinder.FIRST_CALL_TRANSACTION + 14);
    static final int TRANSACTION_getResonantFrequency = (android.os.IBinder.FIRST_CALL_TRANSACTION + 15);
    static final int TRANSACTION_getQFactor = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16);
    static final int TRANSACTION_getFrequencyResolution = (android.os.IBinder.FIRST_CALL_TRANSACTION + 17);
    static final int TRANSACTION_getFrequencyMinimum = (android.os.IBinder.FIRST_CALL_TRANSACTION + 18);
    static final int TRANSACTION_getBandwidthAmplitudeMap = (android.os.IBinder.FIRST_CALL_TRANSACTION + 19);
    static final int TRANSACTION_getPwlePrimitiveDurationMax = (android.os.IBinder.FIRST_CALL_TRANSACTION + 20);
    static final int TRANSACTION_getPwleCompositionSizeMax = (android.os.IBinder.FIRST_CALL_TRANSACTION + 21);
    static final int TRANSACTION_getSupportedBraking = (android.os.IBinder.FIRST_CALL_TRANSACTION + 22);
    static final int TRANSACTION_composePwle = (android.os.IBinder.FIRST_CALL_TRANSACTION + 23);
    static final int TRANSACTION_performVendorEffect = (android.os.IBinder.FIRST_CALL_TRANSACTION + 24);
    static final int TRANSACTION_getFrequencyToOutputAccelerationMap = (android.os.IBinder.FIRST_CALL_TRANSACTION + 25);
    static final int TRANSACTION_getPwleV2PrimitiveDurationMaxMillis = (android.os.IBinder.FIRST_CALL_TRANSACTION + 26);
    static final int TRANSACTION_getPwleV2CompositionSizeMax = (android.os.IBinder.FIRST_CALL_TRANSACTION + 27);
    static final int TRANSACTION_getPwleV2PrimitiveDurationMinMillis = (android.os.IBinder.FIRST_CALL_TRANSACTION + 28);
    static final int TRANSACTION_composePwleV2 = (android.os.IBinder.FIRST_CALL_TRANSACTION + 29);
    static final int TRANSACTION_getInterfaceVersion = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16777214);
    static final int TRANSACTION_getInterfaceHash = (android.os.IBinder.FIRST_CALL_TRANSACTION + 16777213);
  }
  @android.annotation.Hide
  public static final java.lang.String DESCRIPTOR = "android$hardware$vibrator$IVibrator".replace('$', '.');
  /** Whether on w/ IVibratorCallback can be used w/ 'on' function */
  public static final int CAP_ON_CALLBACK = 1;
  /** Whether on w/ IVibratorCallback can be used w/ 'perform' function */
  public static final int CAP_PERFORM_CALLBACK = 2;
  /** Whether setAmplitude is supported (when external control is disabled) */
  public static final int CAP_AMPLITUDE_CONTROL = 4;
  /** Whether setExternalControl is supported. */
  public static final int CAP_EXTERNAL_CONTROL = 8;
  /** Whether setAmplitude is supported (when external control is enabled) */
  public static final int CAP_EXTERNAL_AMPLITUDE_CONTROL = 16;
  /** Whether compose is supported. */
  public static final int CAP_COMPOSE_EFFECTS = 32;
  /** Whether alwaysOnEnable/alwaysOnDisable is supported. */
  public static final int CAP_ALWAYS_ON_CONTROL = 64;
  /** Whether getResonantFrequency is supported. */
  public static final int CAP_GET_RESONANT_FREQUENCY = 128;
  /** Whether getQFactor is supported. */
  public static final int CAP_GET_Q_FACTOR = 256;
  /** Whether frequency control is supported. */
  public static final int CAP_FREQUENCY_CONTROL = 512;
  /** Whether composePwle is supported. */
  public static final int CAP_COMPOSE_PWLE_EFFECTS = 1024;
  /** Whether perform w/ vendor effect is supported. */
  public static final int CAP_PERFORM_VENDOR_EFFECTS = 2048;
  /** Whether composePwleV2 for PwlePrimitives is supported. */
  public static final int CAP_COMPOSE_PWLE_EFFECTS_V2 = 4096;
  /** Determine capabilities of the vibrator HAL (CAP_* mask) */
  public int getCapabilities() throws android.os.RemoteException;
  /**
   * Turn off vibrator
   * 
   * Cancel a previously-started vibration, if any. If a previously-started vibration is
   * associated with a callback, then onComplete should still be called on that callback.
   * This also clears any vibration amplitude set by `setAmplitude` and disables external
   * control, if enabled.
   */
  public void off() throws android.os.RemoteException;
  /**
   * Turn on vibrator
   * 
   * This function must only be called after the previous timeout has expired or
   * was canceled (through off()). A callback is only expected to be supported when
   * getCapabilities CAP_ON_CALLBACK is specified.
   * 
   * Doing this operation while the vibrator is already on is undefined behavior. Clients should
   * explicitly call off.
   * 
   * @param timeoutMs number of milliseconds to vibrate.
   * @param callback A callback used to inform Frameworks of state change, if supported.
   */
  public void on(int timeoutMs, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException;
  /**
   * Fire off a predefined haptic event.
   * 
   * A callback is only expected to be supported when getCapabilities CAP_PERFORM_CALLBACK
   * is specified.
   * 
   * Doing this operation while the vibrator is already on is undefined behavior. Clients should
   * explicitly call off.
   * 
   * @param effect The type of haptic event to trigger.
   * @param strength The intensity of haptic event to trigger.
   * @param callback A callback used to inform Frameworks of state change, if supported.
   * @return The length of time the event is expected to take in
   *     milliseconds. This doesn't need to be perfectly accurate, but should be a reasonable
   *     approximation.
   */
  public int perform(int effect, byte strength, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException;
  /**
   * List supported effects.
   * 
   * Return the effects which are supported (an effect is expected to be supported at every
   * strength level.
   */
  public int[] getSupportedEffects() throws android.os.RemoteException;
  /**
   * Sets the motor's vibrational amplitude.
   * 
   * Changes the force being produced by the underlying motor. This amplitude applies to
   * ongoing and future vibrations triggered by the `on()` function and does not affect
   * other vibration types. This may not be supported and this support is reflected in
   * getCapabilities (CAP_AMPLITUDE_CONTROL). When this device is under external
   * control (via setExternalControl), amplitude control may not be supported even
   * though it is supported normally. This can be checked with
   * CAP_EXTERNAL_AMPLITUDE_CONTROL.
   * 
   * @param amplitude The unitless force setting. Note that this number must
   *                  be between 0.0 (exclusive) and 1.0 (inclusive). It must
   *                  do it's best to map it onto the number of steps it does have.
   */
  public void setAmplitude(float amplitude) throws android.os.RemoteException;
  /**
   * Enables/disables control override of vibrator to audio.
   * 
   * Support is reflected in getCapabilities (CAP_EXTERNAL_CONTROL).
   * 
   * When this API is set, the vibrator control should be ceded to audio system
   * for haptic audio. While this is enabled, issuing of other commands to control
   * the vibrator is unsupported and the resulting behavior is undefined. Amplitude
   * control may or may not be supported and is reflected in the return value of
   * getCapabilities (CAP_EXTERNAL_AMPLITUDE_CONTROL) while this is enabled. When this is
   * disabled, the vibrator should resume to an off state.
   * 
   * @param enabled Whether external control should be enabled or disabled.
   */
  public void setExternalControl(boolean enabled) throws android.os.RemoteException;
  /**
   * Retrieve composition delay limit.
   * 
   * Support is reflected in getCapabilities (CAP_COMPOSE_EFFECTS).
   * 
   * @return Maximum delay for a single CompositeEffect[] entry.
   */
  public int getCompositionDelayMax() throws android.os.RemoteException;
  /**
   * Retrieve composition size limit.
   * 
   * Support is reflected in getCapabilities (CAP_COMPOSE_EFFECTS).
   * 
   * @return Maximum number of entries in CompositeEffect[].
   * @param maxDelayMs Maximum delay for a single CompositeEffect[] entry.
   */
  public int getCompositionSizeMax() throws android.os.RemoteException;
  /**
   * List of supported effect primitive.
   * 
   * Return the effect primitives which are supported by the compose API.
   * Implementations are expected to support all required primitives of the
   * interface version that they implement (see primitive definitions).
   */
  public int[] getSupportedPrimitives() throws android.os.RemoteException;
  /**
   * Retrieve effect primitive's duration in milliseconds.
   * 
   * Support is reflected in getCapabilities (CAP_COMPOSE_EFFECTS).
   * 
   * @return Best effort estimation of effect primitive's duration.
   * @param primitive Effect primitive being queried.
   */
  public int getPrimitiveDuration(int primitive) throws android.os.RemoteException;
  /**
   * Fire off a string of effect primitives, combined to perform richer effects.
   * 
   * Support is reflected in getCapabilities (CAP_COMPOSE_EFFECTS).
   * 
   * Doing this operation while the vibrator is already on is undefined behavior. Clients should
   * explicitly call off. IVibratorCallback.onComplete() support is required for this API.
   * 
   * @param composite Array of composition parameters.
   */
  public void compose(android.hardware.vibrator.CompositeEffect[] composite, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException;
  /**
   * List of supported always-on effects.
   * 
   * Return the effects which are supported by the alwaysOnEnable (an effect
   * is expected to be supported at every strength level.
   */
  public int[] getSupportedAlwaysOnEffects() throws android.os.RemoteException;
  /**
   * Enable an always-on haptic source, assigning a specific effect. An
   * always-on haptic source is a source that can be triggered externally
   * once enabled and assigned an effect to play. This may not be supported
   * and this support is reflected in getCapabilities (CAP_ALWAYS_ON_CONTROL).
   * 
   * The always-on source ID is conveyed directly to clients through
   * device/board configuration files ensuring that no ID is assigned to
   * multiple clients. No client should use this API unless explicitly
   * assigned an always-on source ID. Clients must develop their own way to
   * get IDs from vendor in a stable way. For instance, a client may expose
   * a stable API (via HAL, sysprops, or xml overlays) to allow vendor to
   * associate a hardware ID with a specific usecase. When that usecase is
   * triggered, a client would use that hardware ID here.
   * 
   * @param id The device-specific always-on source ID to enable.
   * @param effect The type of haptic event to trigger.
   * @param strength The intensity of haptic event to trigger.
   */
  public void alwaysOnEnable(int id, int effect, byte strength) throws android.os.RemoteException;
  /**
   * Disable an always-on haptic source. This may not be supported and this
   * support is reflected in getCapabilities (CAP_ALWAYS_ON_CONTROL).
   * 
   * The always-on source ID is conveyed directly to clients through
   * device/board configuration files ensuring that no ID is assigned to
   * multiple clients. No client should use this API unless explicitly
   * assigned an always-on source ID. Clients must develop their own way to
   * get IDs from vendor in a stable way.
   * 
   * @param id The device-specific always-on source ID to disable.
   */
  public void alwaysOnDisable(int id) throws android.os.RemoteException;
  /**
   * Retrieve the measured resonant frequency of the actuator.
   * 
   * This may not be supported and this support is reflected in
   * getCapabilities (CAP_GET_RESONANT_FREQUENCY)
   * 
   * @return Measured resonant frequency in Hz. Non-zero value if supported,
   *         or value should be ignored if not supported.
   */
  public float getResonantFrequency() throws android.os.RemoteException;
  /**
   * Retrieve the measured Q factor.
   * 
   * This may not be supported and this support is reflected in
   * getCapabilities (CAP_GET_Q_FACTOR)
   * 
   * @return Measured Q factor. Non-zero value if supported, or value should be
   *         ignored if not supported.
   */
  public float getQFactor() throws android.os.RemoteException;
  /**
   * Retrieve the frequency resolution used in getBandwidthAmplitudeMap() in units of hertz
   * 
   * This may not be supported and this support is reflected in
   * getCapabilities (CAP_FREQUENCY_CONTROL).
   * 
   * @return The frequency resolution of the bandwidth amplitude map.
   *         Non-zero value if supported, or value should be ignored if not supported.
   * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
   * implemented even if CAP_FREQUENCY_CONTROL capability is reported.
   */
  @Deprecated
  public float getFrequencyResolution() throws android.os.RemoteException;
  /**
   * Retrieve the minimum allowed frequency in units of hertz
   * 
   * This may not be supported and this support is reflected in
   * getCapabilities (CAP_FREQUENCY_CONTROL).
   * 
   * @return The minimum frequency allowed. Non-zero value if supported,
   *         or value should be ignored if not supported.
   * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
   * implemented even if CAP_FREQUENCY_CONTROL capability is reported.
   */
  @Deprecated
  public float getFrequencyMinimum() throws android.os.RemoteException;
  /**
   * Retrieve the output acceleration amplitude values per frequency supported
   * 
   * This may not be supported and this support is reflected in
   * getCapabilities (CAP_FREQUENCY_CONTROL).
   * 
   * The mapping is represented as a list of amplitude values in the inclusive range [0.0, 1.0].
   * The first value represents the amplitude at the frequency returned by getFrequencyMinimum().
   * Each subsequent element is the amplitude at the next supported frequency, in increments
   * of getFrequencyResolution(). The value returned by getResonantFrequency() must be
   * represented in the returned list.
   * 
   * The amplitude values represent the maximum output acceleration amplitude supported for each
   * given frequency. Equal amplitude values for different frequencies represent equal output
   * accelerations.
   * 
   * @return The maximum output acceleration amplitude for each supported frequency,
   *         starting at getMinimumFrequency()
   * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
   * implemented even if CAP_FREQUENCY_CONTROL capability is reported.
   */
  @Deprecated
  public float[] getBandwidthAmplitudeMap() throws android.os.RemoteException;
  /**
   * Retrieve the maximum duration allowed for any primitive PWLE in units of milliseconds.
   * 
   * This may not be supported and this support is reflected in
   * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS).
   * 
   * @return The maximum duration allowed for a single PrimitivePwle.
   *         Non-zero value if supported, or value should be ignored if not supported.
   * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
   * implemented. Use `IVibrator.getPwleV2PrimitiveDurationMaxMillis` instead.
   */
  @Deprecated
  public int getPwlePrimitiveDurationMax() throws android.os.RemoteException;
  /**
   * Retrieve the maximum count for allowed PWLEs in one composition.
   * 
   * This may not be supported and this support is reflected in
   * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS).
   * 
   * @return The maximum count allowed. Non-zero value if supported,
   *         or value should be ignored if not supported.
   * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
   * implemented. Use `IVibrator.getPwleV2CompositionSizeMax` instead.
   */
  @Deprecated
  public int getPwleCompositionSizeMax() throws android.os.RemoteException;
  /**
   * List of supported braking mechanism.
   * 
   * This may not be supported and this support is reflected in
   * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS).
   * Implementations are optional but encouraged if available.
   * 
   * @return The braking mechanisms which are supported by the composePwle API.
   * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
   * implemented.
   */
  @Deprecated
  public int[] getSupportedBraking() throws android.os.RemoteException;
  /**
   * Fire off a string of PWLEs.
   * 
   * This may not be supported and this support is reflected in
   * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS).
   * 
   * Doing this operation while the vibrator is already on is undefined behavior. Clients should
   * explicitly call off. IVibratorCallback.onComplete() support is required for this API.
   * 
   * @param composite Array of PWLEs.
   * @deprecated This method is deprecated from AIDL v3 and is no longer required to be
   * implemented. Use `IVibrator.composePwleV2` instead.
   */
  @Deprecated
  public void composePwle(android.hardware.vibrator.PrimitivePwle[] composite, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException;
  /**
   * Fire off a vendor-defined haptic event.
   * 
   * This may not be supported and this support is reflected in
   * getCapabilities (CAP_PERFORM_VENDOR_EFFECTS).
   * 
   * The duration of the effect is unknown and can be undefined for looping effects.
   * IVibratorCallback.onComplete() support is required for this API.
   * 
   * Doing this operation while the vibrator is already on is undefined behavior. Clients should
   * explicitly call off.
   * 
   * @param effect The vendor data representing the effect to be performed.
   * @param callback A callback used to inform Frameworks of state change.
   * @throws :
   *         - EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
   *         - EX_ILLEGAL_ARGUMENT for bad framework parameters, e.g. scale or effect strength.
   *         - EX_SERVICE_SPECIFIC for bad vendor data, vibration is not triggered.
   */
  public void performVendorEffect(android.hardware.vibrator.VendorEffect vendorEffect, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException;
  /**
   * Retrieves a mapping of vibration frequency (Hz) to the maximum achievable output
   * acceleration (Gs) the device can reach at that frequency.
   * 
   * The map, represented as a list of `FrequencyAccelerationMapEntry` (frequency, output
   * acceleration) pairs, defines the device's frequency response. The platform uses the minimum
   * and maximum frequency values to determine the supported input range for
   * `IVibrator.composePwleV2`. Output acceleration values are used to identify a frequency range
   * suitable to safely play perceivable vibrations with a simple API. The map is also exposed for
   * developers using an advanced API.
   * 
   * The platform does not impose specific requirements on map resolution which can vary
   * depending on the shape of device output curve. The values will be linearly interpolated
   * during lookups. The platform will provide a simple API, defined by a frequency range from
   * where the output acceleration first exceeds a minimum threshold of 10 db SL, to where the
   * output acceleration last exceeds that threshold.
   * 
   * This may not be supported and this support is reflected in getCapabilities
   * (CAP_FREQUENCY_CONTROL). If this is supported, it's expected to be non-empty and
   * describe a valid non-empty frequency range where the simple API can be defined.
   * 
   * For devices that also support CAP_GET_RESONANT_FREQUENCY, the resonant frequency must be
   * within the frequency range defined by the simple API.
   * 
   * @return A list of map entries representing the frequency to max acceleration
   *         mapping.
   * @throws EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
   */
  public java.util.List<android.hardware.vibrator.FrequencyAccelerationMapEntry> getFrequencyToOutputAccelerationMap() throws android.os.RemoteException;
  /**
   * Retrieve the maximum duration allowed for any primitive PWLE in units of
   * milliseconds.
   * 
   * This may not be supported and this support is reflected in
   * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS_V2).
   * 
   * @return The maximum duration allowed for a single PrimitivePwle. Non-zero value if supported.
   * @throws EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
   */
  public int getPwleV2PrimitiveDurationMaxMillis() throws android.os.RemoteException;
  /**
   * Retrieve the maximum number of PWLE primitives input supported by IVibrator.composePwleV2.
   * 
   * This may not be supported and this support is reflected in
   * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS_V2). Devices supporting
   * PWLE effects must support effects with at least 16 PwleV2Primitive.
   * 
   * @return The maximum count allowed. Non-zero value if supported.
   * @throws EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
   */
  public int getPwleV2CompositionSizeMax() throws android.os.RemoteException;
  /**
   * Retrieves the minimum duration (in milliseconds) of any segment within a
   * PWLE effect. Devices supporting PWLE effects must support a minimum ramp
   * time of 20 milliseconds.
   * 
   * This may not be supported and this support is reflected in
   * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS_V2).
   * 
   * @return The minimum duration allowed for a single PrimitivePwle. Non-zero value if supported.
   * @throws EX_UNSUPPORTED_OPERATION if unsupported, as reflected by getCapabilities.
   */
  public int getPwleV2PrimitiveDurationMinMillis() throws android.os.RemoteException;
  /**
   * Play composed sequence of PWLEs with optional callback upon completion.
   * 
   * A PWLE (Piecewise-Linear Envelope) effect defines a vibration waveform using amplitude and
   * frequency points. The envelope linearly interpolates both amplitude and frequency between
   * consecutive points, creating smooth transitions in the vibration pattern.
   * 
   * This may not be supported and this support is reflected in
   * getCapabilities (CAP_COMPOSE_PWLE_EFFECTS_V2).
   * 
   * Note: Devices reporting CAP_COMPOSE_PWLE_EFFECTS_V2 support must also have the
   * CAP_FREQUENCY_CONTROL and CAP_GET_RESONANT_FREQUENCY capabilities, and provide a valid
   * frequency to output acceleration map.
   * 
   * Doing this operation while the vibrator is already on is undefined behavior. Clients should
   * explicitly call off. IVibratorCallback.onComplete() support is required for this API.
   * 
   * @param composite A CompositePwleV2 representing a composite vibration effect, composed of an
   *                  array of primitives that define the PWLE (Piecewise-Linear Envelope).
   */
  public void composePwleV2(android.hardware.vibrator.CompositePwleV2 composite, android.hardware.vibrator.IVibratorCallback callback) throws android.os.RemoteException;
  public int getInterfaceVersion() throws android.os.RemoteException;
  public String getInterfaceHash() throws android.os.RemoteException;
}
