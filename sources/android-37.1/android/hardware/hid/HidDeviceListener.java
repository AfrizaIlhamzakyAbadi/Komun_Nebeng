/*
 * Copyright (C) 2026 The Android Open Source Project
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

package android.hardware.hid;

import static com.android.hardware.input.Flags.FLAG_HID_API;

import android.annotation.FlaggedApi;
import android.annotation.NonNull;

/**
 * Interface for receiving HID device connectivity updates.
 */
@FlaggedApi(FLAG_HID_API)
public interface HidDeviceListener {
    /**
     * Called when a HID device is added.
     *
     * @param device The HID device that was added.
     */
    void onHidDeviceAdded(@NonNull HidDevice device);

    /**
     * Called when a HID device is removed.
     *
     * @param device The HID device that was removed.
     */
    void onHidDeviceRemoved(@NonNull HidDevice device);
}
