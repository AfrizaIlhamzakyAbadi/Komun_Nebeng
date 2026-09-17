/*
 * Copyright (C) 2011 The Android Open Source Project
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

package android.nfc;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.app.Activity;
import android.app.Application;
import android.compat.annotation.UnsupportedAppUsage;
import android.nfc.NfcAdapter.ReaderCallback;
import android.os.Binder;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Manages NFC API's that are coupled to the life-cycle of an Activity.
 *
 * <p>Uses {@link Application#registerActivityLifecycleCallbacks} to hook
 * into activity life-cycle events such as onPause() and onResume().
 */
@Hide
public final class NfcActivityManager extends IAppCallback.Stub
        implements Application.ActivityLifecycleCallbacks {
    static final String TAG = NfcAdapter.TAG;
    static final Boolean DBG = false;

    @UnsupportedAppUsage
    final NfcAdapter mAdapter;

    private static final ExecutorService sExecutor = Executors.newSingleThreadExecutor();

    // All objects in the lists are protected by this
    final List<NfcApplicationState> mApps;  // Application(s) that have NFC state. Usually one
    final List<NfcActivityState> mActivities;  // Activities that have NFC state

    @Hide
    public NfcApplicationState findAppState(Application app) {
        for (NfcApplicationState appState : mApps) {
            if (appState.app == app) {
                return appState;
            }
        }
        return null;
    }

    @Hide
    public void registerApplication(Application app) {
        NfcApplicationState appState = findAppState(app);
        if (appState == null) {
            appState = new NfcApplicationState(app, this);
            mApps.add(appState);
        }
        appState.register();
    }

    @Hide
    public void unregisterApplication(Application app) {
        NfcApplicationState appState = findAppState(app);
        if (appState == null) {
            Log.e(TAG, "unregisterApplication: app was not registered " + app);
            return;
        }
        appState.unregister();
    }

    /**
     * find activity state from mActivities
     */
    @Hide
    public synchronized NfcActivityState findActivityState(Activity activity) {
        for (NfcActivityState state : mActivities) {
            if (state.activity == activity) {
                return state;
            }
        }
        return null;
    }

    /**
     * find or create activity state from mActivities
     */
    @Hide
    public synchronized NfcActivityState getActivityState(Activity activity) {
        NfcActivityState state = findActivityState(activity);
        if (state == null) {
            state = new NfcActivityState(activity, this);
            mActivities.add(state);
        }
        return state;
    }

    @Hide
    public synchronized NfcActivityState findResumedActivityState() {
        for (NfcActivityState state : mActivities) {
            if (state.resumed) {
                return state;
            }
        }
        return null;
    }

    @Hide
    public synchronized void destroyActivityState(Activity activity) {
        NfcActivityState activityState = findActivityState(activity);
        if (activityState != null) {
            activityState.destroy();
            mActivities.remove(activityState);
        }
    }

    public NfcActivityManager(NfcAdapter adapter) {
        mAdapter = adapter;
        mActivities = new LinkedList<NfcActivityState>();
        mApps = new ArrayList<NfcApplicationState>(1);  // Android VM usually has 1 app
    }

    public void enableReaderMode(Activity activity, ReaderCallback callback, int flags,
            Bundle extras) {
        boolean isResumed;
        Binder token;
        int pollTech, listenTech;
        synchronized (NfcActivityManager.this) {
            NfcActivityState state = getActivityState(activity);
            state.readerCallback = callback;
            state.readerModeFlags = flags;
            state.readerModeExtras = extras;
            pollTech = state.mPollTech;
            listenTech = state.mListenTech;
            token = state.token;
            isResumed = state.resumed;
        }
        if (isResumed) {
            if (listenTech != NfcAdapter.FLAG_USE_ALL_TECH
                    || pollTech != NfcAdapter.FLAG_USE_ALL_TECH) {
                throw new IllegalStateException(
                    "Cannot be used when alternative DiscoveryTechnology is set");
            } else {
                setReaderMode(token, flags, extras);
            }
        }
    }

    public void disableReaderMode(Activity activity) {
        boolean isResumed;
        Binder token;
        synchronized (NfcActivityManager.this) {
            NfcActivityState state;
            try {
                state = getActivityState(activity);
            } catch (IllegalStateException e) {
                Log.e(TAG, "Exception in disableReaderMode: " + e);
                return;
            }
            state.readerCallback = null;
            state.readerModeFlags = 0;
            state.readerModeExtras = null;
            token = state.token;
            isResumed = state.resumed;
        }
        if (isResumed) {
            setReaderMode(token, 0, null);
        }

    }

    public void setReaderMode(Binder token, int flags, Bundle extras) {
        if (DBG) Log.d(TAG, "setReaderMode");
        NfcAdapter.callService(() -> NfcAdapter.sService.setReaderMode(
                token, this, flags, extras, mAdapter.getContext().getPackageName()));
    }

    /**
     * Request or unrequest NFC service callbacks.
     * Makes IPC call - do not hold lock.
     */
    void requestNfcServiceCallback() {
        NfcAdapter.callService(
                () -> NfcAdapter.sService.setAppCallback(this));
    }

    void verifyNfcPermission() {
        NfcAdapter.callService(() -> NfcAdapter.sService.verifyNfcPermission());
    }

    @Override
    public void onTagDiscovered(Tag tag) throws RemoteException {
        NfcAdapter.ReaderCallback callback;
        synchronized (NfcActivityManager.this) {
            NfcActivityState state = findResumedActivityState();
            if (state == null) return;

            callback = state.readerCallback;
        }

        // Make callback without lock
        if (callback != null) {
            callback.onTagDiscovered(tag);
        }

    }

    @FlaggedApi(com.android.nfc.module.flags.Flags.FLAG_TAP_TO_X)
    @Override
    public void onTagLost(Tag tag) throws RemoteException {
        NfcAdapter.ReaderCallback callback;
        synchronized (NfcActivityManager.this) {
            NfcActivityState state = findResumedActivityState();
            if (state == null) return;

            callback = state.readerCallback;
        }

        // Make callback without lock
        if (callback != null) {
            callback.onTagLost(tag);
        }

    }

    /** Callback from Activity life-cycle, on main thread */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) { /* NO-OP */ }

    /** Callback from Activity life-cycle, on main thread */
    @Override
    public void onActivityStarted(Activity activity) { /* NO-OP */ }

    /** Callback from Activity life-cycle, on main thread */
    @Override
    public void onActivityResumed(Activity activity) {
        final int fReaderModeFlags;
        final Bundle fReaderModeExtras;
        final Binder fToken;
        final int fPollTech;
        final int fListenTech;

        synchronized (NfcActivityManager.this) {
            NfcActivityState state = findActivityState(activity);
            if (DBG) Log.d(TAG, "onActivityResumed: " + activity + " " + state);
            if (state == null) return;
            state.resumed = true;
            fToken = state.token;
            fReaderModeFlags = state.readerModeFlags;
            fReaderModeExtras = state.readerModeExtras;

            fPollTech = state.mPollTech;
            fListenTech = state.mListenTech;
        }

        sExecutor.execute(() -> {
            if (fReaderModeFlags != 0) {
                setReaderMode(fToken, fReaderModeFlags, fReaderModeExtras);
            } else if (fListenTech != NfcAdapter.FLAG_USE_ALL_TECH
                    || fPollTech != NfcAdapter.FLAG_USE_ALL_TECH) {
                changeDiscoveryTech(fToken, fPollTech, fListenTech);
            }
            requestNfcServiceCallback();
        });
    }

    /** Callback from Activity life-cycle, on main thread */
    @Override
    public void onActivityPaused(Activity activity) {
        final boolean fReaderModeFlagsSet;
        final Binder fToken;
        final int fPollTech;
        final int fListenTech;

        synchronized (NfcActivityManager.this) {
            NfcActivityState state = findActivityState(activity);
            if (DBG) Log.d(TAG, "onActivityPaused: " + activity + " " + state);
            if (state == null) return;
            state.resumed = false;
            fToken = state.token;
            fReaderModeFlagsSet = state.readerModeFlags != 0;

            fPollTech = state.mPollTech;
            fListenTech = state.mListenTech;
        }
        sExecutor.execute(() -> {
            if (fReaderModeFlagsSet) {
                // Restore default p2p modes
                setReaderMode(fToken, 0, null);
            } else if (fListenTech != NfcAdapter.FLAG_USE_ALL_TECH
                    || fPollTech != NfcAdapter.FLAG_USE_ALL_TECH) {
                changeDiscoveryTech(fToken,
                        NfcAdapter.FLAG_USE_ALL_TECH, NfcAdapter.FLAG_USE_ALL_TECH);
            }
        });
    }

    /** Callback from Activity life-cycle, on main thread */
    @Override
    public void onActivityStopped(Activity activity) { /* NO-OP */ }

    /** Callback from Activity life-cycle, on main thread */
    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) { /* NO-OP */ }

    /** Callback from Activity life-cycle, on main thread */
    @Override
    public void onActivityDestroyed(Activity activity) {
        synchronized (NfcActivityManager.this) {
            NfcActivityState state = findActivityState(activity);
            if (DBG) Log.d(TAG, "onActivityDestroyed: " + activity + " " + state);
            if (state != null) {
                // release all associated references
                destroyActivityState(activity);
            }
        }
    }

    /** setDiscoveryTechnology() implementation */
    public void setDiscoveryTech(Activity activity, int pollTech, int listenTech) {
        boolean isResumed;
        Binder token;
        boolean readerModeFlagsSet;
        synchronized (NfcActivityManager.this) {
            NfcActivityState state = getActivityState(activity);
            readerModeFlagsSet = state.readerModeFlags != 0;
            state.mListenTech = listenTech;
            state.mPollTech = pollTech;
            token = state.token;
            isResumed = state.resumed;
        }
        if (!readerModeFlagsSet && isResumed) {
            changeDiscoveryTech(token, pollTech, listenTech);
        } else if (readerModeFlagsSet) {
            throw new IllegalStateException("Cannot be used when the Reader Mode is enabled");
        }
    }

    /** resetDiscoveryTechnology() implementation */
    public void resetDiscoveryTech(Activity activity) {
        boolean isResumed;
        Binder token;
        boolean readerModeFlagsSet;
        synchronized (NfcActivityManager.this) {
            NfcActivityState state = getActivityState(activity);
            state.mListenTech = NfcAdapter.FLAG_USE_ALL_TECH;
            state.mPollTech = NfcAdapter.FLAG_USE_ALL_TECH;
            token = state.token;
            isResumed = state.resumed;
        }
        if (isResumed) {
            changeDiscoveryTech(token, NfcAdapter.FLAG_USE_ALL_TECH, NfcAdapter.FLAG_USE_ALL_TECH);
        }

    }

    private void changeDiscoveryTech(Binder token, int pollTech, int listenTech) {
        NfcAdapter.callService(
                () -> NfcAdapter.sService.updateDiscoveryTechnology(
                        token, pollTech, listenTech, mAdapter.getContext().getPackageName()));
    }

}
