/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: aidl --lang=java -Weverything -Wno-missing-permission-annotation --structured --version 5 --hash notfrozen --stability vintf --min_sdk_version current -pout/soong/.intermediates/hardware/interfaces/common/aidl/android.hardware.common_interface/2/preprocessed.aidl -pout/soong/.intermediates/hardware/interfaces/common/fmq/aidl/android.hardware.common.fmq_interface/1/preprocessed.aidl -pout/soong/.intermediates/system/hardware/interfaces/media/android.media.audio.common.types_interface/5/preprocessed.aidl --previous_api_dir=hardware/interfaces/vibrator/aidl/aidl_api/android.hardware.vibrator/4 --previous_hash dec155403ea3aa5395b0226de399873712b16082 --ninja -d out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen/android/hardware/vibrator/HapticGeneratorQueues.java.d -o out/soong/.intermediates/hardware/interfaces/vibrator/aidl/android.hardware.vibrator-V5-java-source/gen -Iframeworks/native/aidl/binder -Nhardware/interfaces/vibrator/aidl hardware/interfaces/vibrator/aidl/android/hardware/vibrator/HapticGeneratorQueues.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.hardware.vibrator;
/**
 * A collection of message queues for haptic PCM data generation.
 * 
 * The following sequence of operations is used for generating haptic PCM data:
 *  1. The framework writes a 'startEffect' command to the 'command' queue and waits for the reply.
 *     If needed, the framework will also clear the 'pcm' queue.
 *  2. The HAL clears the 'effect' queue, if needed, and prepares to generate PCM
 *     data for a new effect.
 *  3. The HAL writes a reply into the 'reply' queue informing it's ready to
 *     start generation.
 *  4. If there is more VibrationEffectContent to be sent to the HAL,
 *     the framework writes VibrationEffectContent into the 'effect' queue.
 *  5. [Optional] If the entire effect was written into the 'effect' queue in
 *     the previous step:
 *     5.1 The framework sends a 'completeEffect' command to the 'command'
 *         queue and waits for reply.
 *     5.2 The HAL writes a reply into the 'reply' queue acknowledging.
 *  6. The framework writes a 'burstBytes' command into the 'command' queue and
 *     waits for the reply.
 *  7. The HAL reads any available VibrationEffectContent from the 'effect' queue.
 *  8. The HAL generates the next bytes of haptic PCM data for the current
 *     effect and writes it into the 'pcm' queue. The generated data is not
 *     necessarily based on the most recent data read from the 'effect' queue.
 *  9. The HAL writes a reply into the 'reply' queue informing how many bytes
 *     were written in the previous step.
 * 10. The framework reads all PCM bytes from the 'pcm' queue.
 * 11. The framework determines if the effect is complete. The effect is
 *     considered complete if the 'completeEffect' command has been acknowledged
 *     by the HAL and the HAL has replied to a 'burstBytes' command with 0
 *     bytes ready.
 * 12. Go back to step 4 until generation is complete.
 */
public class HapticGeneratorQueues implements android.os.Parcelable
{
  /** Id of the vibrator associated with these haptic generator queues. */
  public int vibratorId = 0;
  /** For commands from the framework to the HAL (e.g., burst, close). */
  public android.hardware.common.fmq.MQDescriptor<android.hardware.vibrator.HapticGeneratorCommand,Byte> command;
  /**
   * For vibration data from the framework to the HAL.
   * This queue acts as a buffer for the vibration effect data that the HAL
   * will process to generate haptic PCM.
   */
  public android.hardware.common.fmq.MQDescriptor<android.hardware.vibrator.VibrationEffectContent,Byte> effect;
  /** For replies from the HAL back to the framework. */
  public android.hardware.common.fmq.MQDescriptor<android.hardware.vibrator.HapticGeneratorReply,Byte> reply;
  /**
   * For the generated haptic PCM data from the HAL to the framework.
   * This queue provides the raw haptic waveform, which can be played through
   * the audio pipeline to potentially achieve precise audio-haptic synchronization.
   */
  public android.hardware.common.fmq.MQDescriptor<Byte,Byte> pcm;
  @Override
   public final int getStability() { return android.os.Parcelable.PARCELABLE_STABILITY_VINTF; }
  public static final android.os.Parcelable.Creator<HapticGeneratorQueues> CREATOR = new android.os.Parcelable.Creator<HapticGeneratorQueues>() {
    @Override
    public HapticGeneratorQueues createFromParcel(android.os.Parcel _aidl_source) {
      HapticGeneratorQueues _aidl_out = new HapticGeneratorQueues();
      _aidl_out.readFromParcel(_aidl_source);
      return _aidl_out;
    }
    @Override
    public HapticGeneratorQueues[] newArray(int _aidl_size) {
      return new HapticGeneratorQueues[_aidl_size];
    }
  };
  @Override public final void writeToParcel(android.os.Parcel _aidl_parcel, int _aidl_flag)
  {
    int _aidl_start_pos = _aidl_parcel.dataPosition();
    _aidl_parcel.writeInt(0);
    _aidl_parcel.writeInt(vibratorId);
    _aidl_parcel.writeTypedObject(command, _aidl_flag);
    _aidl_parcel.writeTypedObject(effect, _aidl_flag);
    _aidl_parcel.writeTypedObject(reply, _aidl_flag);
    _aidl_parcel.writeTypedObject(pcm, _aidl_flag);
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
      vibratorId = _aidl_parcel.readInt();
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      command = _aidl_parcel.readTypedObject(android.hardware.common.fmq.MQDescriptor.CREATOR);
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      effect = _aidl_parcel.readTypedObject(android.hardware.common.fmq.MQDescriptor.CREATOR);
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      reply = _aidl_parcel.readTypedObject(android.hardware.common.fmq.MQDescriptor.CREATOR);
      if (_aidl_parcel.dataPosition() - _aidl_start_pos >= _aidl_parcelable_size) return;
      pcm = _aidl_parcel.readTypedObject(android.hardware.common.fmq.MQDescriptor.CREATOR);
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
    _mask |= describeContents(command);
    _mask |= describeContents(effect);
    _mask |= describeContents(reply);
    _mask |= describeContents(pcm);
    return _mask;
  }
  private int describeContents(Object _v) {
    if (_v == null) return 0;
    if (_v instanceof android.os.Parcelable) {
      return ((android.os.Parcelable) _v).describeContents();
    }
    return 0;
  }
}
