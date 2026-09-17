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

package android.scheduling;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresPermission;
import android.annotation.SystemApi;
import android.annotation.SystemService;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;

import com.android.scheduling.flags.Flags;

import java.time.LocalTime;

/**
 * Manages the scheduling and execution of system reboots to minimize user disruption.
 *
 * <p>The {@code RebootSchedulerManager} provides a centralized system service for scheduling
 * postponed reboots. It ensures that reboots are performed only under optimal conditions (e.g.
 * sufficient battery, device charging, or adequate storage space) as defined by {@link
 * RebootConditions}.
 *
 * <p>By centralizing these requests, the system avoids unorganized and concurrent reboot attempts
 * from multiple privileged applications. Clients can schedule reboots to occur either immediately
 * once conditions are met ({@link RebootParams#REBOOT_MODE_IMMEDIATE_WHEN_READY}) or within a
 * designated time window ({@link RebootParams#REBOOT_MODE_WITHIN_WINDOW}).
 *
 * <p><b>Policy:</b> Each client application is restricted to a single active scheduled reboot
 * request. Subsequent invocations of {@link #scheduleReboot(RebootParams)} by the same client will
 * override any previously scheduled request.
 *
 * <p>Usage typically involves obtaining an instance via {@link
 * android.content.Context#getSystemService(String)}:
 *
 * <pre>{@code
 * RebootSchedulerManager schedulerManager =
 *             context.getSystemService(RebootSchedulerManager.class);
 * }</pre>
 *
 * <p><b>Note:</b> Interacting with this service generally requires the caller to hold the {@link
 * android.Manifest.permission#REBOOT} permission.
 */
@FlaggedApi(Flags.FLAG_ENABLE_REBOOT_SCHEDULER_IMPLEMENTATION)
@SystemApi
@SystemService(RebootSchedulerManager.REBOOT_SCHEDULER_SERVICE)
public final class RebootSchedulerManager {
    private static final String TAG = "RebootSchedulerManager";

    @Hide static final String REBOOT_SCHEDULER_SERVICE = "reboot_scheduler";

    private final Context mContext;
    private final IRebootReadinessManager mService;

    /**
     * Constructor for RebootSchedulerManager.
     *
     * @param context The application context.
     * @param service The backing IRebootReadinessManager instance.
     */
    @Hide
    RebootSchedulerManager(@NonNull Context context, @NonNull IRebootReadinessManager service) {
        mContext = context;
        mService = service;
    }

    /**
     * @return The current unified window that will be used for {@code
     *     RebootParams#REBOOT_MODE_WITHIN_WINDOW} reboots. This reboot window is set globally for
     *     all device users and for all clients.
     */
    @SystemApi
    public @NonNull RebootWindow getUnifiedRebootWindow() {
        Log.w(TAG, "getUnifiedRebootWindow is not implemented");
        return new RebootWindow(LocalTime.of(0, 0), LocalTime.of(0, 0));
    }

    /**
     * Allows the customisation of the unified reboot window.
     *
     * <p>This method must only be called by applications acting on behalf of the user.
     *
     * @param window The new reboot window that will be used.
     */
    @SystemApi(client = SystemApi.Client.MODULE_LIBRARIES)
    @RequiresPermission(android.Manifest.permission.REBOOT)
    public void setUnifiedRebootWindow(@NonNull RebootWindow window) {
        Log.w(TAG, "setUnifiedRebootWindow is not implemented");
    }

    /**
     * Schedules a device reboot with the specified parameters. If the client has previously
     * scheduled another reboot the new request will override the previous one.
     *
     * <p>Compared to the overloaded method that accepts only the {@link RebootParams} parameter, it
     * allows the caller application to set a custom tag pe each request. This is useful for large
     * application such as GMSCore that hosts multiple clients within the same process. By
     * specifying this tag each, clients can differentiate the request between them.
     *
     * @param params Parameters for the reboot.
     * @param tag A unique tag identifying the specific client or context within the calling
     *     process.
     * @throws IllegalArgumentException if {@link tag} is empty
     */
    @SystemApi
    @RequiresPermission(android.Manifest.permission.REBOOT)
    public void scheduleReboot(@NonNull RebootParams params, @NonNull String tag) {
        try {
            mService.scheduleReboot(createRebootRequest(params, tag));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Retrieves the currently scheduled reboot parameters for a specific tag.
     *
     * <p>This version allows large applications (such as GMS Core) that host multiple internal
     * clients to differentiate between their requests by providing the unique {@code tag} used
     * during scheduling.
     *
     * @param tag A unique tag identifying the specific client or context within the calling
     *     process.
     * @throws IllegalArgumentException if {@code tag} is empty.
     */
    @SystemApi
    @RequiresPermission(android.Manifest.permission.REBOOT)
    public void cancelReboot(@NonNull String tag) {
        try {
            mService.cancelReboot(createClientId(tag));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Retrieves the currently scheduled reboot parameters for a specific tag.
     *
     * <p>This version allows large applications (such as GMS Core) that host multiple internal
     * submodules to differentiate between their requests by providing the unique {@code tag} used
     * during scheduling.
     *
     * @param tag A unique tag identifying the specific submodule or context within the calling
     *     process.
     * @return The {@link RebootParams} for the scheduled reboot associated with the provided tag,
     *     or {@code null} if no reboot is scheduled for that tag.
     * @throws IllegalArgumentException if {@code tag} is empty.
     */
    @Nullable
    @SystemApi
    @RequiresPermission(android.Manifest.permission.REBOOT)
    public RebootParams getScheduledReboot(@NonNull String tag) {
        Log.w(TAG, "getScheduledReboot is not implemented");
        return null;
    }

    @NonNull
    private RebootRequest createRebootRequest(@NonNull RebootParams params, @NonNull String tag) {
        RebootRequest request = new RebootRequest();

        /* The request timestamp is set on the system service side */
        request.timestampSeconds = 0;
        request.clientId = createClientId(tag);
        request.reason = params.getReason();
        request.delaySeconds = params.getDelay() != null ? params.getDelay().toSeconds() : 0L;
        request.requiresPreRebootNotification = params.isPreRebootNotificationRequired();

        request.mechanism =
                switch (params.getMechanism()) {
                    case RebootParams.REBOOT_MECHANISM_RESUME_ON_REBOOT ->
                            RebootRequestMechanism.RESUME_ON_REBOOT;
                    case RebootParams.REBOOT_MECHANISM_REGULAR_REBOOT ->
                            RebootRequestMechanism.REGULAR_REBOOT;
                    default -> {
                        throw new IllegalArgumentException(
                                "Invalid reboot request mechanism value");
                    }
                };

        request.mode =
                switch (params.getMode()) {
                    case RebootParams.REBOOT_MODE_IMMEDIATE_WHEN_READY ->
                            RebootRequestMode.IMMEDIATE_WHEN_READY;
                    case RebootParams.REBOOT_MODE_WITHIN_WINDOW -> RebootRequestMode.WITHIN_WINDOW;
                    default -> {
                        throw new IllegalArgumentException("Invalid reboot request mode value");
                    }
                };

        RebootRequestConditions conditions = new RebootRequestConditions();
        conditions.isChargingRequired = params.getConditions().isChargingRequired();
        conditions.minimumSpaceBytes = params.getConditions().getMinimumStorageBytes();
        conditions.minimumBatteryPercentage = params.getConditions().getMinimumBatteryPercentage();
        request.conditions = conditions;

        return request;
    }

    @NonNull
    private RebootClientId createClientId(@NonNull String tag) {
        if (tag.isEmpty()) {
            throw new IllegalArgumentException("tag argument is empty");
        }

        /* The request uid is set on the system service side */
        RebootClientId clientId = new RebootClientId();
        clientId.packageName = mContext.getPackageName();
        clientId.submoduleTag = tag;
        return clientId;
    }
}
