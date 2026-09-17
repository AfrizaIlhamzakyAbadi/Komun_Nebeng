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

package com.android.internal.telephony.emergency;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.Phone;
import com.android.telephony.Rlog;

/**
 * This helper class implements special behavior related to emergency calls and includes logic for
 * enabling 2G on the phone before a call is initiated.
 */
public class Enable2gHelper implements Enable2gStateListener.Callback {

    private static final String TAG = "Enable2gHelper";
    private Enable2gStateListener.Callback mCallback;
    private Enable2gStateListener mListener;

    private void setupListeners() {
        mListener = new Enable2gStateListener();
    }

    /**
     * Starts the "enable 2g" for all the active SIM's. This is the (single) external API of the
     * Enable2gHelper class.
     * <p>
     * This method kicks off the following sequence:
     * - enable 2G for each active SIM
     * - Listen for events telling us the 2g has enabled.
     * - Finally, clean up any leftover state.
     * <p>
     * This method is safe to call from any thread, since it simply posts a message to the
     * Enable2gHelper's handler (thus ensuring that the rest of the sequence is entirely
     * serialized, and runs on the main looper.)
     */
    public void triggerEnable2gAndListen(Phone phone, Enable2gStateListener.Callback callback,
                                         int modemReconfigDelayMillis) {
        Rlog.d(TAG, "triggerEnable2gAndListen");
        setupListeners();
        mCallback = callback;
        mListener.waitForEnable2g(phone, this, modemReconfigDelayMillis);
    }

    @Override
    public void onComplete(boolean is2gEnabled) {
        mListener = null;
        if (mCallback != null) {
            mCallback.onComplete(is2gEnabled);
        }
    }

    @VisibleForTesting
    public Enable2gStateListener getListener() {
        return mListener;
    }
}
