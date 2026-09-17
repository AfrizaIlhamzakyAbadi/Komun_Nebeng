/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.hardware.hdmi;

import android.annotation.BinderThread;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.TestApi;

import java.util.ArrayList;
import java.util.List;

/**
 * A wrapper of the Binder interface that clients running in the application process
 * will use to perform HDMI-CEC features by communicating with other devices
 * on the bus.
 */
@TestApi
public final class HdmiControlServiceWrapper {

    /** Pure CEC switch device type. */
    public static final int DEVICE_PURE_CEC_SWITCH = HdmiDeviceInfo.DEVICE_PURE_CEC_SWITCH;

    private List<HdmiPortInfo> mInfoList = null;
    private int[] mTypes = null;

    /**
     * Create a new HdmiControlManager with the current HdmiControlService wrapper
     *
     * @return the created HdmiControlManager
     */
    @NonNull
    public HdmiControlManager createHdmiControlManager() {
        return new HdmiControlManager(mInterface);
    }

    private final IHdmiControlService mInterface = new IHdmiControlService.Stub() {

        @Override
        public int[] getSupportedTypes() {
            return HdmiControlServiceWrapper.this.getSupportedTypes();
        }

        @Override
        public HdmiDeviceInfo getActiveSource() {
            return HdmiControlServiceWrapper.this.getActiveSource();
        }

        @Override
        public void oneTouchPlay(IHdmiControlCallback callback) {
            HdmiControlServiceWrapper.this.oneTouchPlay(callback);
        }

        @Override
        public void toggleAndFollowTvPower() {
            HdmiControlServiceWrapper.this.toggleAndFollowTvPower();
        }

        @Override
        public boolean shouldHandleTvPowerKey() {
            return HdmiControlServiceWrapper.this.shouldHandleTvPowerKey();
        }

        @Override
        public void queryDisplayStatus(IHdmiControlCallback callback) {
            HdmiControlServiceWrapper.this.queryDisplayStatus(callback);
        }

        @Override
        public void addHdmiControlStatusChangeListener(IHdmiControlStatusChangeListener listener) {
            HdmiControlServiceWrapper.this.addHdmiControlStatusChangeListener(listener);
        }

        @Override
        public void removeHdmiControlStatusChangeListener(
                IHdmiControlStatusChangeListener listener) {
            HdmiControlServiceWrapper.this.removeHdmiControlStatusChangeListener(listener);
        }

        @Override
        public void addHotplugEventListener(IHdmiHotplugEventListener listener) {
            HdmiControlServiceWrapper.this.addHotplugEventListener(listener);
        }

        @Override
        public void removeHotplugEventListener(IHdmiHotplugEventListener listener) {
            HdmiControlServiceWrapper.this.removeHotplugEventListener(listener);
        }

        @Override
        public void addDeviceEventListener(IHdmiDeviceEventListener listener) {
            HdmiControlServiceWrapper.this.addDeviceEventListener(listener);
        }

        @Override
        public void deviceSelect(int deviceId, IHdmiControlCallback callback) {
            HdmiControlServiceWrapper.this.deviceSelect(deviceId, callback);
        }

        @Override
        public void portSelect(int portId, IHdmiControlCallback callback) {
            HdmiControlServiceWrapper.this.portSelect(portId, callback);
        }

        @Override
        public void sendKeyEvent(int deviceType, int keyCode, boolean isPressed) {
            HdmiControlServiceWrapper.this.sendKeyEvent(deviceType, keyCode, isPressed);
        }

        @Override
        public void sendVolumeKeyEvent(int deviceType, int keyCode, boolean isPressed) {
            HdmiControlServiceWrapper.this.sendVolumeKeyEvent(deviceType, keyCode, isPressed);
        }

        @Override
        public List<HdmiPortInfo> getPortInfo() {
            return HdmiControlServiceWrapper.this.getPortInfo();
        }

        @Override
        public boolean canChangeSystemAudioMode() {
            return HdmiControlServiceWrapper.this.canChangeSystemAudioMode();
        }

        @Override
        public boolean getSystemAudioMode() {
            return HdmiControlServiceWrapper.this.getSystemAudioMode();
        }

        @Override
        public int getPhysicalAddress() {
            return HdmiControlServiceWrapper.this.getPhysicalAddress();
        }

        @Override
        public void setSystemAudioMode(boolean enabled, IHdmiControlCallback callback) {
            HdmiControlServiceWrapper.this.setSystemAudioMode(enabled, callback);
        }

        @Override
        public void addSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener listener) {
            HdmiControlServiceWrapper.this.addSystemAudioModeChangeListener(listener);
        }

        @Override
        public void removeSystemAudioModeChangeListener(
                IHdmiSystemAudioModeChangeListener listener) {
            HdmiControlServiceWrapper.this.removeSystemAudioModeChangeListener(listener);
        }

        @Override
        public void setArcMode(boolean enabled) {
            HdmiControlServiceWrapper.this.setArcMode(enabled);
        }

        @Override
        public void setProhibitMode(boolean enabled) {
            HdmiControlServiceWrapper.this.setProhibitMode(enabled);
        }

        @Override
        public void setSystemAudioVolume(int oldIndex, int newIndex, int maxIndex) {
            HdmiControlServiceWrapper.this.setSystemAudioVolume(oldIndex, newIndex, maxIndex);
        }

        @Override
        public void setSystemAudioMute(boolean mute) {
            HdmiControlServiceWrapper.this.setSystemAudioMute(mute);
        }

        @Override
        public void setInputChangeListener(IHdmiInputChangeListener listener) {
            HdmiControlServiceWrapper.this.setInputChangeListener(listener);
        }

        @Override
        public List<HdmiDeviceInfo> getInputDevices() {
            return HdmiControlServiceWrapper.this.getInputDevices();
        }

        @Override
        public List<HdmiDeviceInfo> getDeviceList() {
            return HdmiControlServiceWrapper.this.getDeviceList();
        }

        @Override
        public void powerOffRemoteDevice(int logicalAddress, int powerStatus) {
            HdmiControlServiceWrapper.this.powerOffRemoteDevice(logicalAddress, powerStatus);
        }

        @Override
        public void powerOnRemoteDevice(int logicalAddress, int powerStatus) {
            HdmiControlServiceWrapper.this.powerOnRemoteDevice(logicalAddress, powerStatus);
        }

        @Override
        public void askRemoteDeviceToBecomeActiveSource(int physicalAddress) {
            HdmiControlServiceWrapper.this.askRemoteDeviceToBecomeActiveSource(physicalAddress);
        }

        @Override
        public void sendVendorCommand(int deviceType, int targetAddress, byte[] params,
                boolean hasVendorId) {
            HdmiControlServiceWrapper.this.sendVendorCommand(
                    deviceType, targetAddress, params, hasVendorId);
        }

        @Override
        public void addVendorCommandListener(IHdmiVendorCommandListener listener, int vendorId) {
            HdmiControlServiceWrapper.this.addVendorCommandListener(listener, vendorId);
        }

        @Override
        public void sendStandby(int deviceType, int deviceId) {
            HdmiControlServiceWrapper.this.sendStandby(deviceType, deviceId);
        }

        @Override
        public void setHdmiRecordListener(IHdmiRecordListener callback) {
            HdmiControlServiceWrapper.this.setHdmiRecordListener(callback);
        }

        @Override
        public void startOneTouchRecord(int recorderAddress, byte[] recordSource) {
            HdmiControlServiceWrapper.this.startOneTouchRecord(recorderAddress, recordSource);
        }

        @Override
        public void stopOneTouchRecord(int recorderAddress) {
            HdmiControlServiceWrapper.this.stopOneTouchRecord(recorderAddress);
        }

        @Override
        public void startTimerRecording(int recorderAddress, int sourceType, byte[] recordSource) {
            HdmiControlServiceWrapper.this.startTimerRecording(
                    recorderAddress, sourceType, recordSource);
        }

        @Override
        public void clearTimerRecording(int recorderAddress, int sourceType, byte[] recordSource) {
            HdmiControlServiceWrapper.this.clearTimerRecording(
                    recorderAddress, sourceType, recordSource);
        }

        @Override
        public void sendMhlVendorCommand(int portId, int offset, int length, byte[] data) {
            HdmiControlServiceWrapper.this.sendMhlVendorCommand(portId, offset, length, data);
        }

        @Override
        public void addHdmiMhlVendorCommandListener(IHdmiMhlVendorCommandListener listener) {
            HdmiControlServiceWrapper.this.addHdmiMhlVendorCommandListener(listener);
        }

        @Override
        public void setStandbyMode(boolean isStandbyModeOn) {
            HdmiControlServiceWrapper.this.setStandbyMode(isStandbyModeOn);
        }

        @Override
        public void reportAudioStatus(int deviceType, int volume, int maxVolume, boolean isMute) {
            HdmiControlServiceWrapper.this.reportAudioStatus(deviceType, volume, maxVolume, isMute);
        }

        @Override
        public void setSystemAudioModeOnForAudioOnlySource() {
            HdmiControlServiceWrapper.this.setSystemAudioModeOnForAudioOnlySource();
        }

        @Override
        public void addHdmiCecVolumeControlFeatureListener(
                IHdmiCecVolumeControlFeatureListener listener) {
            HdmiControlServiceWrapper.this.addHdmiCecVolumeControlFeatureListener(listener);
        }

        @Override
        public void removeHdmiCecVolumeControlFeatureListener(
                IHdmiCecVolumeControlFeatureListener listener) {
            HdmiControlServiceWrapper.this.removeHdmiCecVolumeControlFeatureListener(listener);
        }

        @Override
        public int getMessageHistorySize() {
            return HdmiControlServiceWrapper.this.getMessageHistorySize();
        }

        @Override
        public boolean setMessageHistorySize(int newSize) {
            return HdmiControlServiceWrapper.this.setMessageHistorySize(newSize);
        }

        @Override
        public void addCecSettingChangeListener(String name,
                IHdmiCecSettingChangeListener listener) {
            HdmiControlServiceWrapper.this.addCecSettingChangeListener(name, listener);
        }

        @Override
        public void removeCecSettingChangeListener(String name,
                IHdmiCecSettingChangeListener listener) {
            HdmiControlServiceWrapper.this.removeCecSettingChangeListener(name, listener);
        }

        @Override
        public List<String> getUserCecSettings() {
            return HdmiControlServiceWrapper.this.getUserCecSettings();
        }

        @Override
        public List<String> getAllowedCecSettingStringValues(String name) {
            return HdmiControlServiceWrapper.this.getAllowedCecSettingStringValues(name);
        }

        @Override
        public int[] getAllowedCecSettingIntValues(String name) {
            return HdmiControlServiceWrapper.this.getAllowedCecSettingIntValues(name);
        }

        @Override
        public String getCecSettingStringValue(String name) {
            return HdmiControlServiceWrapper.this.getCecSettingStringValue(name);
        }

        @Override
        public void setCecSettingStringValue(String name, String value) {
            HdmiControlServiceWrapper.this.setCecSettingStringValue(name, value);
        }

        @Override
        public int getCecSettingIntValue(String name) {
            return HdmiControlServiceWrapper.this.getCecSettingIntValue(name);
        }

        @Override
        public void setCecSettingIntValue(String name, int value) {
            HdmiControlServiceWrapper.this.setCecSettingIntValue(name, value);
        }
    };

    @BinderThread
    public void setPortInfo(@NonNull List<HdmiPortInfo> infoList) {
        mInfoList = infoList;
    }

    @BinderThread
    public void setDeviceTypes(@NonNull int[] types) {
        mTypes = types;
    }

    @Hide
    public List<HdmiPortInfo> getPortInfo() {
        return mInfoList;
    }

    @Hide
    public int[] getSupportedTypes() {
        return mTypes;
    }

    @Hide
    public HdmiDeviceInfo getActiveSource() {
        return null;
    }

    @Hide
    public void oneTouchPlay(IHdmiControlCallback callback) {}

    @Hide
    public void toggleAndFollowTvPower() {}

    @Hide
    public boolean shouldHandleTvPowerKey() {
        return true;
    }

    @Hide
    public void queryDisplayStatus(IHdmiControlCallback callback) {}

    @Hide
    public void addHdmiControlStatusChangeListener(IHdmiControlStatusChangeListener listener) {}

    @Hide
    public void removeHdmiControlStatusChangeListener(IHdmiControlStatusChangeListener listener) {}

    @Hide
    public void addHotplugEventListener(IHdmiHotplugEventListener listener) {}

    @Hide
    public void removeHotplugEventListener(IHdmiHotplugEventListener listener) {}

    @Hide
    public void addDeviceEventListener(IHdmiDeviceEventListener listener) {}

    @Hide
    public void deviceSelect(int deviceId, IHdmiControlCallback callback) {}

    @Hide
    public void portSelect(int portId, IHdmiControlCallback callback) {}

    @Hide
    public void sendKeyEvent(int deviceType, int keyCode, boolean isPressed) {}

    @Hide
    public void sendVolumeKeyEvent(int deviceType, int keyCode, boolean isPressed) {}

    @Hide
    public boolean canChangeSystemAudioMode() {
        return true;
    }

    @Hide
    public boolean getSystemAudioMode() {
        return true;
    }

    @Hide
    public int getPhysicalAddress() {
        return 0xffff;
    }

    @Hide
    public void setSystemAudioMode(boolean enabled, IHdmiControlCallback callback) {}

    @Hide
    public void addSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener listener) {}

    @Hide
    public void removeSystemAudioModeChangeListener(IHdmiSystemAudioModeChangeListener listener) {}

    @Hide
    public void setArcMode(boolean enabled) {}

    @Hide
    public void setProhibitMode(boolean enabled) {}

    @Hide
    public void setSystemAudioVolume(int oldIndex, int newIndex, int maxIndex) {}

    @Hide
    public void setSystemAudioMute(boolean mute) {}

    @Hide
    public void setInputChangeListener(IHdmiInputChangeListener listener) {}

    @Hide
    public List<HdmiDeviceInfo> getInputDevices() {
        return null;
    }

    @Hide
    public List<HdmiDeviceInfo> getDeviceList() {
        return null;
    }

    @Hide
    public void powerOffRemoteDevice(int logicalAddress, int powerStatus) {}

    @Hide
    public void powerOnRemoteDevice(int logicalAddress, int powerStatus) {}

    @Hide
    public void askRemoteDeviceToBecomeActiveSource(int physicalAddress) {}

    @Hide
    public void sendVendorCommand(int deviceType, int targetAddress, byte[] params,
            boolean hasVendorId) {}

    @Hide
    public void addVendorCommandListener(IHdmiVendorCommandListener listener, int vendorId) {}

    @Hide
    public void sendStandby(int deviceType, int deviceId) {}

    @Hide
    public void setHdmiRecordListener(IHdmiRecordListener callback) {}

    @Hide
    public void startOneTouchRecord(int recorderAddress, byte[] recordSource) {}

    @Hide
    public void stopOneTouchRecord(int recorderAddress) {}

    @Hide
    public void startTimerRecording(int recorderAddress, int sourceType, byte[] recordSource) {}

    @Hide
    public void clearTimerRecording(int recorderAddress, int sourceType, byte[] recordSource) {}

    @Hide
    public void sendMhlVendorCommand(int portId, int offset, int length, byte[] data) {}

    @Hide
    public void addHdmiMhlVendorCommandListener(IHdmiMhlVendorCommandListener listener) {}

    @Hide
    public void setStandbyMode(boolean isStandbyModeOn) {}

    @Hide
    public void setHdmiCecVolumeControlEnabled(boolean isHdmiCecVolumeControlEnabled) {}

    @Hide
    public boolean isHdmiCecVolumeControlEnabled() {
        return true;
    }

    @Hide
    public void reportAudioStatus(int deviceType, int volume, int maxVolume, boolean isMute) {}

    @Hide
    public void setSystemAudioModeOnForAudioOnlySource() {}

    @Hide
    public void addHdmiCecVolumeControlFeatureListener(
            IHdmiCecVolumeControlFeatureListener listener) {}

    @Hide
    public void removeHdmiCecVolumeControlFeatureListener(
            IHdmiCecVolumeControlFeatureListener listener) {}

    @Hide
    public int getMessageHistorySize() {
        return 0;
    }

    @Hide
    public boolean setMessageHistorySize(int newSize) {
        return true;
    }

    @Hide
    public void addCecSettingChangeListener(String name,
            IHdmiCecSettingChangeListener listener) {}

    @Hide
    public void removeCecSettingChangeListener(String name,
            IHdmiCecSettingChangeListener listener) {}

    @Hide
    public List<String> getUserCecSettings() {
        return new ArrayList<>();
    }

    @Hide
    public List<String> getAllowedCecSettingStringValues(String name) {
        return new ArrayList<>();
    }

    @Hide
    public int[] getAllowedCecSettingIntValues(String name) {
        return new int[0];
    }

    @Hide
    public String getCecSettingStringValue(String name) {
        return "";
    }

    @Hide
    public void setCecSettingStringValue(String name, String value) {
    }

    @Hide
    public int getCecSettingIntValue(String name) {
        return 0;
    }

    @Hide
    public void setCecSettingIntValue(String name, int value) {
    }
}
