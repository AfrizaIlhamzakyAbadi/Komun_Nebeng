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

package android.service.contentsafety;

import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SdkConstant;
import android.annotation.SdkConstant.SdkConstantType;
import android.annotation.SuppressLint;
import android.annotation.TestApi;
import android.app.Service;
import android.app.contentsafety.ClassifiableContent;
import android.app.contentsafety.ContentClassificationResult;
import android.app.contentsafety.IContentSafetyCallback;
import android.app.contentsafety.flags.Flags;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Slog;

/**
 * Base class for a service that the
 * {@code android.app.role.RoleManager.ROLE_CONTENT_SAFETY} role holder must implement.
 *
 * <p>When the content safety subsystem is enabled or disabled, {@link #onContentSafetyEnabled()}
 * and {@link #onContentSafetyDisabled()} will be called, respectively.
 *
 * <p>When the content safety subsystem is enabled and an app calls
 * {@link ContentSafetyManager#requestContentClassification}, the system service will call
 * {@link #onClassifyContent(ClassifiableContent)}.
 *
 * <pre>
 *     {@code
 *     <service android:name=".SampleContentSafetyAppService"
 *              android:permission="android.permission.BIND_CONTENT_SAFETY_SERVICE"
 *              android:privateComputeCore="true">
 *         <intent-filter>
 *             <action android:name="android.app.action.CONTENT_SAFETY_SERVICE" />
 *         </intent-filter>
 *      </service>}
 */
@FlaggedApi(Flags.FLAG_CONTENT_RESTRICTION_API)
public abstract class ContentSafetyAppService extends Service {
    private static final String TAG = "ContentSafetyAppService";

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    /**
     * Service action: Action for a service that the {@code
     * android.app.role.RoleManager.ROLE_CONTENT_SAFETY} role holder must implement.
     */
    @SuppressLint("ActionValue")
    @SdkConstant(SdkConstantType.SERVICE_ACTION)
    public static final String ACTION_CONTENT_SAFETY_APP_SERVICE =
            "android.app.action.CONTENT_SAFETY_SERVICE";

    private final IContentSafetyAppService.Stub mBinder = new IContentSafetyAppService.Stub() {
        @Override
        public void onContentSafetyEnabled(boolean enabled) {
            mHandler.post(() -> {
                if (enabled) {
                    ContentSafetyAppService.this.onContentSafetyEnabled();
                } else {
                    ContentSafetyAppService.this.onContentSafetyDisabled();
                }
            });
        }

        @Override
        public void onClassifyContent(ClassifiableContent content,
                IContentSafetyCallback callback) {
            mHandler.post(() -> {
                try {
                    ContentClassificationResult result =
                            ContentSafetyAppService.this.onClassifyContent(content);
                    if (result == null) {
                        callback.onResult(new ContentClassificationResult(
                                content.getId(), ContentClassificationResult.TYPE_UNCLASSIFIED));
                    } else {
                        callback.onResult(result);
                    }
                } catch (Exception e) {
                    Slog.e(TAG, "Implementation of onClassifyContent crashed.", e);
                    try {
                        callback.onResult(new ContentClassificationResult(
                                content.getId(), ContentClassificationResult.TYPE_UNCLASSIFIED));
                    } catch (RemoteException re) {
                        Slog.e(TAG, "System service died", re);
                    }
                }
            });
        }
    };

    @Nullable
    @Override
    public final IBinder onBind(@Nullable Intent intent) {
        onServiceBound(intent);
        return mBinder.asBinder();
    }

    /**
     * Called when the service is bound.
     *
     * <p>Used for testing since {@code onBind} is final.
     */
    @TestApi
    public void onServiceBound(@Nullable Intent intent) {}

    /**
     * Called when content safety is enabled.
     *
     * <p>This is called on the main thread.
     */
    @FlaggedApi(Flags.FLAG_CONTENT_RESTRICTION_API)
    public void onContentSafetyEnabled() {}

    /**
     * Called when content safety is disabled.
     *
     * <p>This is called on the main thread.
     */
    @FlaggedApi(Flags.FLAG_CONTENT_RESTRICTION_API)
    public void onContentSafetyDisabled() {}

    /**
     * Called when content needs to be classified.
     *
     * <p>This is called on the main thread.
     *
     * @param content the content to be classified
     * @return the content classification result
     */
    @FlaggedApi(Flags.FLAG_CONTENT_RESTRICTION_API)
    @NonNull
    public abstract ContentClassificationResult onClassifyContent(
            @NonNull ClassifiableContent content);
}
