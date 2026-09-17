/*
 * Copyright (C) 2017 The Android Open Source Project
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

package android.os;

import android.annotation.Hide;
import android.annotation.TestApi;

/**
 * Java API for ::android::vintf::RuntimeInfo. Methods return null / 0 on any error.
 */
@TestApi
public class VintfRuntimeInfo {

    private VintfRuntimeInfo() {}

    static {
        System.loadLibrary("vintf_jni");
    }

    /**
     * @return /sys/fs/selinux/policyvers, via security_policyvers() native call
     */
    @Hide
    public static native long getKernelSepolicyVersion();

    /**
     * @return content of /proc/cpuinfo
     */
    @TestApi
    public static native String getCpuInfo();

    /**
     * @return os name extracted from uname() native call
     */
    @TestApi
    public static native String getOsName();

    /**
     * @return node name extracted from uname() native call
     */
    @TestApi
    public static native String getNodeName();

    /**
     * @return os release extracted from uname() native call
     */
    @TestApi
    public static native String getOsRelease();

    /**
     * @return os version extracted from uname() native call
     */
    @TestApi
    public static native String getOsVersion();

    /**
     * @return hardware id extracted from uname() native call
     */
    @TestApi
    public static native String getHardwareId();

    /**
     * @return kernel version extracted from uname() native call. Format is
     * {@code x.y.z}.
     */
    @TestApi
    public static native String getKernelVersion();

    /**
     * @return libavb version in OS. Format is {@code x.y}.
     */
    @Hide
    public static native String getBootAvbVersion();

    /**
     * @return libavb version in bootloader. Format is {@code x.y}.
     */
    @Hide
    public static native String getBootVbmetaAvbVersion();
}
