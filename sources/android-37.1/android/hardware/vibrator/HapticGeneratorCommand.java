/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: aidl --lang=java -Weverything -Wno-missing-permission-annotation --structured --version 5 --hash notfrozen --stability vintf --min_sdk_version current -pout/soong/.intermediates/hardware/interfaces/common/aidl/android.hardware.common_interface/2/preprocessed.aidl -pout/soong/.intermediates/hardware/interfaces/common/fmq/aidl/android.hardware.common.fmq_interface/1/preprocessed.aidl -pout/soong/.intermediates/system/hardware/interfaces/media/android.media.audio.common.types_interface/5/preprocessed.aidl --previous_api_dir=hardware/interfaces/vibrator/aidl/aidl_api/android.hardware.vibrator/4 --previous_hash dec155403ea3aa5395b0226de399873712b16082 --ninja -d out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen/android/hardware/vibrator/HapticGeneratorCommand.java.d -o out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen -Iframeworks/native/aidl/binder -Nhardware/interfaces/vibrator/aidl hardware/interfaces/vibrator/aidl/android/hardware/vibrator/HapticGeneratorCommand.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.hardware.vibrator;
public final class HapticGeneratorCommand implements android.os.Parcelable {
  // tags for union fields
  public final static int reserved = 0;  // byte[32] reserved;
  public final static int effect = 1;  // android.hardware.vibrator.HapticGeneratorCommand.Effect effect;
  public final static int session = 2;  // android.hardware.vibrator.HapticGeneratorCommand.Session session;
  public final static int burstBytes = 3;  // int burstBytes;

  private int _tag;
  private Object _value;

  public HapticGeneratorCommand() {
    byte[] _value = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    this._tag = reserved;
    this._value = _value;
  }

  private HapticGeneratorCommand(android.os.Parcel _aidl_parcel) {
    readFromParcel(_aidl_parcel);
  }

  private HapticGeneratorCommand(int _tag, Object _value) {
    this._tag = _tag;
    this._value = _value;
  }

  public int getTag() {
    return _tag;
  }

  // byte[32] reserved;

  /**
   * Reserved space for future additions to this union. This ensures
   * backward compatibility.
   */
  public static HapticGeneratorCommand reserved(byte[] _value) {
    return new HapticGeneratorCommand(reserved, _value);
  }

  public byte[] getReserved() {
    _assertTag(reserved);
    return (byte[]) _value;
  }

  public void setReserved(byte[] _value) {
    _set(reserved, _value);
  }

  // android.hardware.vibrator.HapticGeneratorCommand.Effect effect;

  public static HapticGeneratorCommand effect(byte _value) {
    return new HapticGeneratorCommand(effect, _value);
  }

  public byte getEffect() {
    _assertTag(effect);
    return (byte) _value;
  }

  public void setEffect(byte _value) {
    _set(effect, _value);
  }

  // android.hardware.vibrator.HapticGeneratorCommand.Session session;

  public static HapticGeneratorCommand session(byte _value) {
    return new HapticGeneratorCommand(session, _value);
  }

  public byte getSession() {
    _assertTag(session);
    return (byte) _value;
  }

  public void setSession(byte _value) {
    _set(session, _value);
  }

  // int burstBytes;

  /**
   * Informs the HAL the number of bytes of PCM data that it should generate
   * and write into the `HapticGeneratorQueues.pcm`.
   * 
   * <p>The HAL should attempt to generate this many bytes, but is allowed
   * to generate fewer bytes if the vibration effect completes partway through
   * the request. The HAL must report the actual number of bytes written in the
   * 'HapticGeneratorReply.burstBytesReady' field.
   * 
   * <p>If the HAL cannot generate any PCM data because it is waiting for more
   * effect input data from the framework, it MUST reply with a
   * `STATUS_NOT_ENOUGH_DATA` status.
   * 
   * <p>This command must be preceded by a `START` command. If this
   * command is received when no effect is active, the HAL MUST return a
   * `STATUS_INVALID_OPERATION` status in the reply.
   */
  public static HapticGeneratorCommand burstBytes(int _value) {
    return new HapticGeneratorCommand(burstBytes, _value);
  }

  public int getBurstBytes() {
    _assertTag(burstBytes);
    return (int) _value;
  }

  public void setBurstBytes(int _value) {
    _set(burstBytes, _value);
  }

  @Override
  public final int getStability() {
    return android.os.Parcelable.PARCELABLE_STABILITY_VINTF;
  }

  public static final android.os.Parcelable.Creator<HapticGeneratorCommand> CREATOR = new android.os.Parcelable.Creator<HapticGeneratorCommand>() {
    @Override
    public HapticGeneratorCommand createFromParcel(android.os.Parcel _aidl_source) {
      return new HapticGeneratorCommand(_aidl_source);
    }
    @Override
    public HapticGeneratorCommand[] newArray(int _aidl_size) {
      return new HapticGeneratorCommand[_aidl_size];
    }
  };

  @Override
  public final void writeToParcel(android.os.Parcel _aidl_parcel, int _aidl_flag) {
    _aidl_parcel.writeInt(_tag);
    switch (_tag) {
    case reserved:
      _aidl_parcel.writeFixedArray(getReserved(), _aidl_flag, 32);
      break;
    case effect:
      _aidl_parcel.writeByte(getEffect());
      break;
    case session:
      _aidl_parcel.writeByte(getSession());
      break;
    case burstBytes:
      _aidl_parcel.writeInt(getBurstBytes());
      break;
    }
  }

  public void readFromParcel(android.os.Parcel _aidl_parcel) {
    int _aidl_tag;
    _aidl_tag = _aidl_parcel.readInt();
    switch (_aidl_tag) {
    case reserved: {
      byte[] _aidl_value;
      _aidl_value = _aidl_parcel.createFixedArray(byte[].class, 32);
      _set(_aidl_tag, _aidl_value);
      return; }
    case effect: {
      byte _aidl_value;
      _aidl_value = _aidl_parcel.readByte();
      _set(_aidl_tag, _aidl_value);
      return; }
    case session: {
      byte _aidl_value;
      _aidl_value = _aidl_parcel.readByte();
      _set(_aidl_tag, _aidl_value);
      return; }
    case burstBytes: {
      int _aidl_value;
      _aidl_value = _aidl_parcel.readInt();
      _set(_aidl_tag, _aidl_value);
      return; }
    }
    throw new IllegalArgumentException("union: unknown tag: " + _aidl_tag);
  }

  @Override
  public int describeContents() {
    int _mask = 0;
    switch (getTag()) {
    }
    return _mask;
  }

  private void _assertTag(int tag) {
    if (getTag() != tag) {
      throw new IllegalStateException("bad access: " + _tagString(tag) + ", " + _tagString(getTag()) + " is available.");
    }
  }

  private String _tagString(int _tag) {
    switch (_tag) {
    case reserved: return "reserved";
    case effect: return "effect";
    case session: return "session";
    case burstBytes: return "burstBytes";
    }
    throw new IllegalStateException("unknown field: " + _tag);
  }

  private void _set(int _tag, Object _value) {
    this._tag = _tag;
    this._value = _value;
  }
  /** A set of commands that manage the state of a single vibration effect conversion. */
  public static @interface Effect {
    /**
     * Informs the HAL to prepare for a new vibration effect.
     * 
     * <p>If this command is received while another effect is being processed,
     * it acts as an implicit cancellation of the previous effect.
     * 
     * <p>Upon receiving this command, the HAL MUST clear the
     * HapticGeneratorQueues.effect queue and prepare to receive new data. Any
     * subsequent `burstBytes` command must only use the new effect data. The
     * client is responsible for clearing the HapticGeneratorQueues.pcm queue to
     * discard any buffered PCM data from the previous effect.
     * 
     * <p>The framework will not write to the 'effect' queue until a success
     * reply is received for this command.
     */
    public static final byte START = 0;
    /**
     * Informs the HAL that the framework has sent all data for the currently
     * active effect.
     * 
     * <p>The HAL must continue to service `burstBytes` commands until all
     * generated data has been consumed.
     * 
     * <p>This command must be preceded by a `START` command. If this
     * command is received when no effect is active, the HAL MUST return a
     * `STATUS_INVALID_OPERATION` status in the reply.
     */
    public static final byte COMPLETE = 1;
    /**
     * Informs the HAL to immediately stop processing the currently active effect.
     * 
     * <p>Upon receiving this command, the HAL MUST clear the
     * HapticGeneratorQueues.effect queue. The client is responsible for clearing
     * the HapticGeneratorQueues.pcm queue to discard any buffered PCM data from
     * the cancelled effect.
     * 
     * <p>Any `burstBytes` command received before a new `START` command
     * should be rejected. If no effect is currently active, the HAL MUST reply
     * with STATUS_INVALID_OPERATION.
     */
    public static final byte CANCEL = 2;
  }
  /** A set of commands that manage the state of the entire haptic generator session. */
  public static @interface Session {
    /**
     * Informs the HAL that the session is terminated and it should release all
     * server-side resources.
     * 
     * <p>All commands after this should be rejected.
     */
    public static final byte CLOSE = 0;
  }
  public static @interface Tag {
    /**
     * Reserved space for future additions to this union. This ensures
     * backward compatibility.
     */
    public static final byte reserved = 0;
    public static final byte effect = 1;
    public static final byte session = 2;
    /**
     * Informs the HAL the number of bytes of PCM data that it should generate
     * and write into the `HapticGeneratorQueues.pcm`.
     * 
     * <p>The HAL should attempt to generate this many bytes, but is allowed
     * to generate fewer bytes if the vibration effect completes partway through
     * the request. The HAL must report the actual number of bytes written in the
     * 'HapticGeneratorReply.burstBytesReady' field.
     * 
     * <p>If the HAL cannot generate any PCM data because it is waiting for more
     * effect input data from the framework, it MUST reply with a
     * `STATUS_NOT_ENOUGH_DATA` status.
     * 
     * <p>This command must be preceded by a `START` command. If this
     * command is received when no effect is active, the HAL MUST return a
     * `STATUS_INVALID_OPERATION` status in the reply.
     */
    public static final byte burstBytes = 3;
  }
}
