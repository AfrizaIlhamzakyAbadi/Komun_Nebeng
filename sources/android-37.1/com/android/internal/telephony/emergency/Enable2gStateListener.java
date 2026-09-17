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

import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.SomeArgs;
import com.android.internal.telephony.Phone;
import com.android.telephony.Rlog;

import java.util.Locale;

/**
 * Helper class that listens to a Phone's 2g state and sends an onComplete callback.
 */
public class Enable2gStateListener {

    public interface Callback {
        /**
         * Receives the result of the Enable2gStateListener's attempt to enable 2g network.
         */
        void onComplete(boolean is2gEnabled);
    }

    private static final String TAG = "Enable2gStateListener";

    // Handler message codes; see handleMessage()
    private static final int MSG_START_SEQUENCE = 1;
    @VisibleForTesting
    public static final int MSG_ENABLED_2G = 2;
    private static final int MSG_MODEM_RECONFIGURED = 3;

    private final Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_SEQUENCE -> {
                    SomeArgs args = (SomeArgs) msg.obj;
                    try {
                        Phone phone = (Phone) args.arg1;
                        Enable2gStateListener.Callback callback =
                                (Enable2gStateListener.Callback) args.arg2;
                        int modemReconfigDelayMillis = args.argi1;
                        startSequenceInternal(phone, callback, modemReconfigDelayMillis);
                    } finally {
                        args.recycle();
                    }
                }
                case MSG_MODEM_RECONFIGURED -> onTimeoutCallbackTimeout();
                case MSG_ENABLED_2G -> {
                    AsyncResult ar = (AsyncResult) msg.obj;
                    if (ar.exception == null) {
                        startDelayForModemReconfig();
                    } else {
                        onComplete(false);
                        Rlog.e(TAG, "Phone id:" + mPhone.getPhoneId()
                                    + " Failed to execute setAllowedNetworkTypesForReason:"
                                    + ar.exception);
                    }
                }
                default -> Rlog.w(TAG, String.format(Locale.getDefault(),
                        "handleMessage: unexpected message: %d.", msg.what));
            }
        }
    };

    private Callback mCallback; // The callback to notify upon completion.
    private Phone mPhone; // The phone that will attempt to place the call.
    private int mModemReconfigDelayMillis; // The delay time for modem to reconfigure the changes

    /**
     * Starts the "wait for enableing 2G". This is the (single) external API of the
     * Enable2gStateListener class.
     *
     * This method kicks off the following sequence:
     * - enable 2G for each active SIM
     * - Listen for events telling us the 2g has enabled.
     * - Finally, clean up any leftover state.
     *
     * This method is safe to call from any thread, since it simply posts a message to the
     * Enable2gStateListener's handler (thus ensuring that the rest of the sequence is entirely
     * serialized, and runs only on the handler thread.)
     */
    public void waitForEnable2g(Phone phone, Callback callback, int modemReconfigDelayMillis) {
        Rlog.d(TAG, "waitForEnable2g: Phone " + phone.getPhoneId());
        if (mPhone != null) {
            // If there already is an ongoing request, ignore the new one!
            return;
        }
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = phone;
        args.arg2 = callback;
        args.argi1 = modemReconfigDelayMillis;
        mHandler.obtainMessage(MSG_START_SEQUENCE, args).sendToTarget();
    }

    /**
     * Actual implementation of waitForEnable2g(), guaranteed to run on the handler thread.
     *
     * @see #waitForEnable2g
     */
    private void startSequenceInternal(Phone phone, Callback callback,
            int modemReconfigDelayMillis) {
        Rlog.d(TAG, "startSequenceInternal: Phone " + phone.getPhoneId());

        // First of all, clean up any state left over from a prior enabling 2g network.
        cleanup();

        mPhone = phone;
        mCallback = callback;
        mModemReconfigDelayMillis = modemReconfigDelayMillis;

        long currentlyAllowedNetworkTypes = phone.getAllowedNetworkTypes(
                TelephonyManager.ALLOWED_NETWORK_TYPES_REASON_ENABLE_2G);
        if ((currentlyAllowedNetworkTypes & TelephonyManager.NETWORK_CLASS_BITMASK_2G) == 0) {
            phone.setAllowedEmergencyNetworkTypes(
                    TelephonyManager.ALLOWED_NETWORK_TYPES_REASON_ENABLE_2G,
                    currentlyAllowedNetworkTypes | TelephonyManager.NETWORK_CLASS_BITMASK_2G,
                    mHandler.obtainMessage(MSG_ENABLED_2G));
        } else {
            // If 2G is already enabled, then sending completed.
            onComplete(true);
        }
    }

    private void onTimeoutCallbackTimeout() {
        if (mPhone == null) {
            return;
        }
        Rlog.d(TAG, "onTimeout");
        onComplete(true);
        cleanup();
    }

    /**
     * Clean up when done with the whole sequence: either after successfully enabling 2g, or after
     * getting failures.
     *
     * The exact cleanup steps are:
     * - Notify callback if we still hadn't sent it a response.
     * - Clean up
     *
     * Basically this method guarantees that there will be no more activity from the
     * Enable2gStateListener until someone kicks off the whole sequence again with another call to
     * {@link #waitForEnable2g}
     */
    private void cleanup() {
        Rlog.d(TAG, "cleanup()");
        // This will send a failure call back if callback has yet to be invoked. If the callback was
        // already invoked, it's a no-op.
        onComplete(false);
        mPhone = null;
        mModemReconfigDelayMillis = 0;
    }

    private void startDelayForModemReconfig() {
        Rlog.d(TAG, "startDelayForModemReconfig: mModemReconfigDelayMillis="
                + mModemReconfigDelayMillis);
        mHandler.removeMessages(MSG_MODEM_RECONFIGURED);
        if (mModemReconfigDelayMillis > 0) {
            mHandler.sendEmptyMessageDelayed(MSG_MODEM_RECONFIGURED,
                    mModemReconfigDelayMillis);
        } else {
            onComplete(true);
        }
    }

    private void onComplete(boolean is2gEnabled) {
        mHandler.removeMessages(MSG_MODEM_RECONFIGURED);
        if (mCallback != null) {
            Callback tempCallback = mCallback;
            mCallback = null;
            tempCallback.onComplete(is2gEnabled);
        }
    }

    @VisibleForTesting
    public Handler getHandler() {
        return mHandler;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Enable2gStateListener)) {
            return false;
        }
        Enable2gStateListener that = (Enable2gStateListener) o;
        if (mCallback != null ? !mCallback.equals(that.mCallback) : that.mCallback != null) {
            return false;
        }
        return mPhone != null ? mPhone.equals(that.mPhone) : that.mPhone == null;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (mCallback == null ? 0 : mCallback.hashCode());
        hash = 31 * hash + (mPhone == null ? 0 : mPhone.hashCode());
        return hash;
    }
}
