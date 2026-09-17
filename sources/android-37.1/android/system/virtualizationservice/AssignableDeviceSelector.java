/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Using: aidl --lang=java -Weverything -Wno-missing-permission-annotation --min_sdk_version current -pout/soong/.intermediates/packages/modules/Virtualization/android/virtualizationservice/aidl/android.system.virtualizationcommon_interface/preprocessed.aidl --ninja -d out/soong/.intermediates/packages/modules/Virtualization/android/virtualizationservice/aidl/android.system.virtualizationservice-java-source/gen/android/system/virtualizationservice/AssignableDeviceSelector.java.d -o out/soong/.intermediates/packages/modules/Virtualization/android/virtualizationservice/aidl/android.system.virtualizationservice-java-source/gen -Npackages/modules/Virtualization/android/virtualizationservice/aidl packages/modules/Virtualization/android/virtualizationservice/aidl/android/system/virtualizationservice/AssignableDeviceSelector.aidl
 *
 * DO NOT CHECK THIS FILE INTO A CODE TREE (e.g. git, etc..).
 * ALWAYS GENERATE THIS FILE FROM UPDATED AIDL COMPILER
 * AS A BUILD INTERMEDIATE ONLY. THIS IS NOT SOURCE CODE.
 */
package android.system.virtualizationservice;
/** Defines the criteria used to identify an AssignableDevice for assignment to a virtual machine. */
public final class AssignableDeviceSelector implements android.os.Parcelable {
  // tags for union fields
  public final static int byLabel = 0;  // String byLabel;
  public final static int byPath = 1;  // String byPath;

  private int _tag;
  private Object _value;

  public AssignableDeviceSelector() {
    java.lang.String _value = null;
    this._tag = byLabel;
    this._value = _value;
  }

  private AssignableDeviceSelector(android.os.Parcel _aidl_parcel) {
    readFromParcel(_aidl_parcel);
  }

  private AssignableDeviceSelector(int _tag, Object _value) {
    this._tag = _tag;
    this._value = _value;
  }

  public int getTag() {
    return _tag;
  }

  // String byLabel;

  /** DTBO label of the device, e.g. gpu */
  public static AssignableDeviceSelector byLabel(java.lang.String _value) {
    return new AssignableDeviceSelector(byLabel, _value);
  }

  public java.lang.String getByLabel() {
    _assertTag(byLabel);
    return (java.lang.String) _value;
  }

  public void setByLabel(java.lang.String _value) {
    _set(byLabel, _value);
  }

  // String byPath;

  /** Absolute sysfs path to the device, e.g. /sys/bus/platform/devices/1c00000.pinctrl */
  public static AssignableDeviceSelector byPath(java.lang.String _value) {
    return new AssignableDeviceSelector(byPath, _value);
  }

  public java.lang.String getByPath() {
    _assertTag(byPath);
    return (java.lang.String) _value;
  }

  public void setByPath(java.lang.String _value) {
    _set(byPath, _value);
  }

  public static final android.os.Parcelable.Creator<AssignableDeviceSelector> CREATOR = new android.os.Parcelable.Creator<AssignableDeviceSelector>() {
    @Override
    public AssignableDeviceSelector createFromParcel(android.os.Parcel _aidl_source) {
      return new AssignableDeviceSelector(_aidl_source);
    }
    @Override
    public AssignableDeviceSelector[] newArray(int _aidl_size) {
      return new AssignableDeviceSelector[_aidl_size];
    }
  };

  @Override
  public final void writeToParcel(android.os.Parcel _aidl_parcel, int _aidl_flag) {
    _aidl_parcel.writeInt(_tag);
    switch (_tag) {
    case byLabel:
      _aidl_parcel.writeString(getByLabel());
      break;
    case byPath:
      _aidl_parcel.writeString(getByPath());
      break;
    }
  }

  public void readFromParcel(android.os.Parcel _aidl_parcel) {
    int _aidl_tag;
    _aidl_tag = _aidl_parcel.readInt();
    switch (_aidl_tag) {
    case byLabel: {
      java.lang.String _aidl_value;
      _aidl_value = _aidl_parcel.readString();
      _set(_aidl_tag, _aidl_value);
      return; }
    case byPath: {
      java.lang.String _aidl_value;
      _aidl_value = _aidl_parcel.readString();
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
    case byLabel: return "byLabel";
    case byPath: return "byPath";
    }
    throw new IllegalStateException("unknown field: " + _tag);
  }

  private void _set(int _tag, Object _value) {
    this._tag = _tag;
    this._value = _value;
  }
  public static @interface Tag {
    /** DTBO label of the device, e.g. gpu */
    public static final int byLabel = 0;
    /** Absolute sysfs path to the device, e.g. /sys/bus/platform/devices/1c00000.pinctrl */
    public static final int byPath = 1;
  }
}
