/*
 * Copyright (C) 2023 The Android Open Source Project
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

package android.companion.virtual.camera;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.annotation.TestApi;
import android.app.AppGlobals;
import android.companion.virtual.IVirtualDevice;
import android.companion.virtual.VirtualDeviceManager;
import android.companion.virtual.VirtualDeviceParams;
import android.companion.virtualdevice.flags.Flags;
import android.content.pm.PackageManager;
import android.hardware.HardwareBuffer;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.os.RemoteException;

import androidx.annotation.NonNull;

import java.io.Closeable;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * A VirtualCamera is the representation of a remote or computer generated camera that will be
 * exposed to applications using the Android Camera APIs.
 *
 * <p>A VirtualCamera is created using {@link
 * VirtualDeviceManager.VirtualDevice#createVirtualCamera(VirtualCameraConfig)}.
 *
 * <p>Once a virtual camera is created, it will receive callbacks from the system when an
 * application attempts to use it via the {@link VirtualCameraCallback} class set using {@link
 * VirtualCameraConfig.Builder#setVirtualCameraCallback(Executor, VirtualCameraCallback)}
 *
 * @see VirtualDeviceManager.VirtualDevice#createVirtualDevice(int, VirtualDeviceParams)
 * @see VirtualCameraConfig.Builder#setVirtualCameraCallback(Executor, VirtualCameraCallback)
 * @see android.hardware.camera2.CameraManager#openCamera(String, CameraDevice.StateCallback,
 *     android.os.Handler)
 */
@SystemApi
public final class VirtualCamera implements Closeable {

    private final IVirtualDevice mVirtualDevice;

    private final String mCameraId;
    private final VirtualCameraConfig mConfig;
    private static Boolean sVirtualCameraSupported = null;

    /**
     * VirtualCamera device constructor.
     *
     * @param virtualDevice The Binder object representing this camera in the server.
     * @param config Configuration for the new virtual camera
     */
    @Hide
    public VirtualCamera(
            @NonNull IVirtualDevice virtualDevice,
            @NonNull String cameraId,
            @NonNull VirtualCameraConfig config) {
        mVirtualDevice = Objects.requireNonNull(virtualDevice);
        mCameraId = Objects.requireNonNull(cameraId);
        mConfig = Objects.requireNonNull(config);
    }

    @Hide
    public static boolean isSupported() {
        if (!Flags.virtualCameraSupportApi()) {
            throw new UnsupportedOperationException(
                    "Flag " + Flags.FLAG_VIRTUAL_CAMERA_SUPPORT_API + " is not enabled");
        }

        if (sVirtualCameraSupported != null) {
            return sVirtualCameraSupported;
        }

        try {
            if (!AppGlobals.getPackageManager()
                    .hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY, 0)) {
                sVirtualCameraSupported = false;
                return false;
            }
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }

        final int checkWidth = 640;
        final int checkHeight = 480;

        // Fast Path Check
        final long yuvUsage =
                HardwareBuffer.USAGE_GPU_SAMPLED_IMAGE
                        | HardwareBuffer.USAGE_GPU_COLOR_OUTPUT
                        | HardwareBuffer.USAGE_CPU_WRITE_OFTEN
                        | HardwareBuffer.USAGE_CPU_READ_OFTEN;
        boolean fastPathSupported =
                HardwareBuffer.isSupported(
                        checkWidth,
                        checkHeight,
                        HardwareBuffer.YCBCR_420_888,
                        1 /* layers */,
                        yuvUsage);

        // Fallback Path Check
        final long rgbaUsage =
                HardwareBuffer.USAGE_GPU_COLOR_OUTPUT
                        | HardwareBuffer.USAGE_CPU_READ_OFTEN
                        | HardwareBuffer.USAGE_CPU_WRITE_OFTEN;
        boolean fallbackPathSupported =
                Flags.virtualCameraYuvTargetExtensionFallback()
                        && HardwareBuffer.isSupported(
                                checkWidth,
                                checkHeight,
                                HardwareBuffer.RGBA_8888,
                                1 /* layers */,
                                rgbaUsage);

        sVirtualCameraSupported = fastPathSupported || fallbackPathSupported;
        return sVirtualCameraSupported;
    }

    /** Returns the configuration of this virtual camera instance. */
    @NonNull
    public VirtualCameraConfig getConfig() {
        return mConfig;
    }

    /**
     * Returns the id of this virtual camera instance.
     */
    @SuppressLint("UnflaggedApi") // @TestApi without associated feature.
    @TestApi
    @NonNull
    public String getId() {
        return mCameraId;
    }

    /**
     * Closes the current session for the virtual camera instance.
     * The client will be notified in {@link CameraDevice.StateCallback#onError(CameraDevice, int)}
     * with {@link CameraDevice.StateCallback#ERROR_CAMERA_DEVICE}.
     * The VirtualCamera is still registered and available, though the camera client needs to open
     * a new camera session to use it.
     */
    @FlaggedApi(Flags.FLAG_VIRTUAL_CAMERA_CLOSE_SESSION)
    public void closeSessionOnError() {
        try {
            mVirtualDevice.closeVirtualCameraSession(mConfig);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    /**
     * Notifies the virtual camera of a request error for the given frame.
     * The app using the virtual camera will receive
     * {@link CameraCaptureSession.CaptureCallback#onCaptureFailed} for the specified frame.
     *
     * @param frameId The frame number where the error occurred.
     */
    @FlaggedApi(Flags.FLAG_VIRTUAL_CAMERA_NOTIFY_ERROR)
    public void notifyRequestError(long frameId) {
        try {
            mVirtualDevice.notifyVirtualCameraRequestError(mConfig, frameId);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }

    @Override
    public void close() {
        try {
            mVirtualDevice.unregisterVirtualCamera(mConfig);
        } catch (RemoteException e) {
            e.rethrowFromSystemServer();
        }
    }
}
