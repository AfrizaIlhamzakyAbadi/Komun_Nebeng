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

package com.android.internal.inputmethod;

import android.Manifest;
import android.annotation.AnyThread;
import android.annotation.CurrentTimeMillisLong;
import android.annotation.ElapsedRealtimeLong;
import android.annotation.NonNull;
import android.annotation.RequiresPermission;
import android.annotation.UserIdInt;
import android.os.RemoteException;
import android.util.ExceptionUtils;
import android.view.inputmethod.ImeTracker;

import com.android.internal.infra.AndroidFuture;

import java.util.concurrent.TimeUnit;

public class ImeTrackerServiceWrapper {
    /** The threshold in milliseconds for an {@link AndroidFuture} completion signal. */
    private static final long TIMEOUT_MS = 10_000;

    @NonNull private IImeTracker mService;

    public ImeTrackerServiceWrapper(@NonNull IImeTracker service) {
        mService = service;
    }

    /**
     * @see com.android.server.inputmethod.ImeTrackerService#onStart
     */
    @AnyThread
    public void onStart(
            @NonNull ImeTracker.Token statsToken,
            int uid,
            @ImeTracker.Type int type,
            @ImeTracker.Origin int origin,
            @SoftInputShowHideReason int reason,
            boolean fromUser,
            @UserIdInt int userId,
            int displayId,
            @CurrentTimeMillisLong long startWallTimeMs,
            @ElapsedRealtimeLong long startTimestampMs) {
        try {
            mService.onStart(
                    statsToken,
                    uid,
                    type,
                    origin,
                    reason,
                    fromUser,
                    userId,
                    displayId,
                    startWallTimeMs,
                    startTimestampMs);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * @see com.android.server.inputmethod.ImeTrackerService#onProgress
     */
    @AnyThread
    public void onProgress(@NonNull ImeTracker.Token statsToken, @ImeTracker.Phase int phase) {
        try {
            mService.onProgress(statsToken, phase);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * @see com.android.server.inputmethod.ImeTrackerService#onFailed
     */
    @AnyThread
    public void onFailed(@NonNull ImeTracker.Token statsToken, @ImeTracker.Phase int phase) {
        try {
            mService.onFailed(statsToken, phase);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * @see com.android.server.inputmethod.ImeTrackerService#onCancelled
     */
    @AnyThread
    public void onCancelled(@NonNull ImeTracker.Token statsToken, @ImeTracker.Phase int phase) {
        try {
            mService.onCancelled(statsToken, phase);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * @see com.android.server.inputmethod.ImeTrackerService#onShown
     */
    @AnyThread
    public void onShown(@NonNull ImeTracker.Token statsToken) {
        try {
            mService.onShown(statsToken);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * @see com.android.server.inputmethod.ImeTrackerService#onHidden
     */
    @AnyThread
    public void onHidden(@NonNull ImeTracker.Token statsToken) {
        try {
            mService.onHidden(statsToken);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * @see com.android.server.inputmethod.ImeTrackerService#onDispatched
     */
    @AnyThread
    public void onDispatched(@NonNull ImeTracker.Token statsToken) {
        try {
            mService.onDispatched(statsToken);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * @see com.android.server.inputmethod.ImeTrackerService#waitUntilNoPendingRequests
     */
    @AnyThread
    @RequiresPermission(Manifest.permission.TEST_INPUT_METHOD)
    public void waitUntilNoPendingRequests(long timeoutMs) {
        try {
            final var future = new AndroidFuture<Void>();
            mService.waitUntilNoPendingRequests(future, timeoutMs);
            future.get(timeoutMs, TimeUnit.MILLISECONDS);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (Exception e) {
            throw ExceptionUtils.propagate(e);
        }
    }

    @AnyThread
    @RequiresPermission(Manifest.permission.TEST_INPUT_METHOD)
    public void finishTrackingPendingRequests() {
        try {
            final var future = new AndroidFuture<Void>();
            mService.finishTrackingPendingRequests(future);
            future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        } catch (Exception e) {
            throw ExceptionUtils.propagate(e);
        }
    }
}
