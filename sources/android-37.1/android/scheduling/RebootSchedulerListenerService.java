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
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresNoPermission;
import android.annotation.SystemApi;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.OutcomeReceiver;

import com.android.scheduling.flags.Flags;

/**
 * Base class for a service that receives notifications about reboot events from the
 * RebootScheduler.
 *
 * <p>Clients should extend this class and override the methods to provide the desired behavior.
 *
 * <p>Services extending this class must declare an intent filter for the action {@code
 * android.scheduling.action.BIND_REBOOT_SCHEDULER_LISTENER} in their {@code AndroidManifest.xml}
 * and must be exported. To ensure that only the system can bind to the service, the service must be
 * protected by the {@code android.permission.BIND_RESUME_ON_REBOOT_SERVICE} permission.
 *
 * <p>Example manifest declaration:
 *
 * <pre>{@code
 * <service android:name=".MyRebootListenerService"
 *          android:exported="true"
 *          android:permission="android.permission.BIND_RESUME_ON_REBOOT_SERVICE">
 *     <intent-filter>
 *         <action android:name="android.scheduling.action.BIND_REBOOT_SCHEDULER_LISTENER" />
 *     </intent-filter>
 * </service>
 * }</pre>
 */
@FlaggedApi(Flags.FLAG_ENABLE_REBOOT_SCHEDULER_IMPLEMENTATION)
@SystemApi
public abstract class RebootSchedulerListenerService extends Service {

    /** Exception used to indicate that the service failed to prepare for a scheduled reboot. */
    @SystemApi
    public static class PrepareRebootException extends RuntimeException {
        /**
         * Creates a new {@link PrepareRebootException} with the specified reason.
         *
         * @param reason the reason for the failure.
         */
        @SystemApi
        public PrepareRebootException(@NonNull String reason) {
            super(reason);
        }
    }

    private final IRebootSchedulerListener.Stub mStub =
            new IRebootSchedulerListener.Stub() {
                @Override
                @RequiresNoPermission
                public void onRebootPrepared(String tag, IPreparedRebootCallback binderCallback) {
                    // TODO: Add implementation
                }

                @Override
                @RequiresNoPermission
                public void onRebootCancelled(String tag) {
                    // TODO: Add implementation
                }
            };

    /**
     * @return an {@link IBinder} that clients can use to communicate with the service.
     */
    @NonNull
    @Override
    public final IBinder onBind(@Nullable Intent intent) {
        return mStub.asBinder();
    }

    /**
     * Called when the system is prepared to reboot.
     *
     * <p>The default implementation invokes {@link OutcomeReceiver#onResult} immediately,
     * indicating that the service is ready for the reboot. Overriding implementations can perform
     * necessary cleanup or state saving and call {@link OutcomeReceiver#onResult} when finished, or
     * call {@link OutcomeReceiver#onError} with a {@link PrepareRebootException} if they fail to
     * prepare.
     *
     * @param tag a tag identifying the source of the reboot request.
     * @param outcome an {@link OutcomeReceiver} to notify the system of the result.
     */
    public void onRebootPrepared(
            @NonNull String tag, @NonNull OutcomeReceiver<Void, PrepareRebootException> outcome) {
        outcome.onResult(null);
    }

    /**
     * Called when the prepared reboot has been cancelled.
     *
     * @param tag an optional tag identifying the source of the reboot request
     */
    public void onRebootCancelled(@NonNull String tag) {}
}
