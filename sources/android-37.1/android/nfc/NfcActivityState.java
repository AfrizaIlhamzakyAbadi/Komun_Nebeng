/*
 * Copyright (C) 2025 The Android Open Source Project
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

import android.annotation.Hide;
import android.app.Activity;
import android.compat.annotation.UnsupportedAppUsage;
import android.os.Binder;
import android.os.Bundle;

/**
 * NFC state associated with an {@link Activity}
 */
@Hide
public class NfcActivityState {

    @Hide
    @UnsupportedAppUsage
    boolean resumed = false;

    @Hide
    @UnsupportedAppUsage
    Activity activity;

    @Hide
    @UnsupportedAppUsage
    NfcAdapter.ReaderCallback readerCallback = null;

    @Hide
    @UnsupportedAppUsage
    int readerModeFlags = 0;

    @Hide
    @UnsupportedAppUsage
    Bundle readerModeExtras = null;

    @Hide
    @UnsupportedAppUsage
    Binder token;

    @Hide
    @UnsupportedAppUsage
    int mPollTech = NfcAdapter.FLAG_USE_ALL_TECH;

    @Hide
    @UnsupportedAppUsage
    int mListenTech = NfcAdapter.FLAG_USE_ALL_TECH;

    @Hide
    @UnsupportedAppUsage
    private final NfcActivityManager mNfcActivityManager;

    @Hide
    public NfcActivityState(Activity activity, NfcActivityManager activityManager) {
        this.mNfcActivityManager = activityManager;
        if (com.android.nfc.module.flags.Flags.tapToX() && activity == null) {
            resumed = true;
            this.token = new Binder();
            return;
        }
        if (activity.isDestroyed()) {
            throw new IllegalStateException("activity is already destroyed");
        }
        // Check if activity is resumed right now, as we will not
        // immediately get a callback for that.
        resumed = activity.isResumed();

        this.activity = activity;
        this.token = new Binder();
        mNfcActivityManager.registerApplication(activity.getApplication());
    }

    @Hide
    public void destroy() {
        if (activity != null) mNfcActivityManager.unregisterApplication(activity.getApplication());
        resumed = false;
        activity = null;
        readerCallback = null;
        readerModeFlags = 0;
        readerModeExtras = null;
        token = null;

        mPollTech = NfcAdapter.FLAG_USE_ALL_TECH;
        mListenTech = NfcAdapter.FLAG_USE_ALL_TECH;
    }

    @Hide
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("[");
        s.append(readerCallback);
        s.append("]");
        return s.toString();
    }
}
