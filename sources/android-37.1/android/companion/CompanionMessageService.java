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

package android.companion;

import android.annotation.FlaggedApi;
import android.annotation.SystemApi;
import android.annotation.MainThread;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import java.util.Objects;

/**
 * A service that receives messages dispatched from associated companion devices reactively.
 *
 * <p>
 * Companion applications must create a service that {@code extends}
 * {@link CompanionMessageService}, and declare it in their {@code AndroidManifest.xml} with the
 * {@link android.Manifest.permission#BIND_COMPANION_MESSAGE_SERVICE} permission,
 * as well as add an intent filter for the {@link #SERVICE_INTERFACE} action,
 * and declare the message type it wants to receive in a {@code meta-data} tag.
 * </p>
 *
 * <p>
 * Following is an example of such declaration in the manifest:
 * <pre>{@code
 * <service
 *        android:name=".MyCompanionMessageService"
 *        android:exported="true"
 *        android:permission="android.permission.BIND_COMPANION_MESSAGE_SERVICE">
 *    <intent-filter>
 *        <action android:name="android.companion.CompanionMessageService" />
 *    </intent-filter>
 *    <meta-data
 *        android:name="receiving_message_type"
 *        android:value="MESSAGE_ONEWAY_PCC" />
 * </service>
 * }</pre>
 * </p>
 *
 * <p>
 * The {@code receiving_message_type} {@code meta-data} value must be one of the
 * strings defined in {@link CompanionDeviceManager.MessageType}.
 * </p>
 */
@SystemApi
@FlaggedApi(android.companion.Flags.FLAG_MESSAGE_RECEIVER)
public abstract class CompanionMessageService extends Service {

    private static final String LOG_TAG = "CDM_CompanionMessageService";

    /**
     * The Intent action that must be declared by the service in its manifest
     * to receive messages from companion devices.
     */
    public static final String SERVICE_INTERFACE = "android.companion.CompanionMessageService";

    private final CompanionMessageServiceProxy mRemote = new CompanionMessageServiceProxy();

    /**
     * Called when a message is received from an associated companion device.
     *
     * <p>Note: This callback is executed on the main thread. Post complex or long-running work
     * to a background thread to prevent blocking the UI.
     *
     * @param associationId The ID of the association linking this companion device.
     * @param messageType The type of message.
     * @param message The raw byte payload dispatched from the companion device.
     */
    @MainThread
    public abstract void onMessageReceived(
            int associationId,
            @CompanionDeviceManager.MessageType int messageType,
            @NonNull byte[] message);

    @Nullable
    @Override
    public final IBinder onBind(@Nullable Intent intent) {
        if (intent != null && Objects.equals(intent.getAction(), SERVICE_INTERFACE)) {
            return mRemote;
        }
        Log.w(LOG_TAG, "Tried to bind to wrong action (should be " + SERVICE_INTERFACE
                + "): " + intent);
        return null;
    }

    private class CompanionMessageServiceProxy extends ICompanionMessageService.Stub {
        final Handler mMainHandler = Handler.getMain();

        @Override
        public void onMessageReceived(int associationId, int messageType, byte[] message) {
            mMainHandler.post(() ->
                CompanionMessageService.this.onMessageReceived(associationId, messageType, message)
            );
        }
    }
}
