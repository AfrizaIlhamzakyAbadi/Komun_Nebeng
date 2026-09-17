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

package com.android.internal.telephony.cat;

import android.content.Context;
import android.os.AsyncResult;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.android.internal.telephony.CallManager;
import com.android.internal.telephony.MmiCode;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneInternalInterface.DialArgs;

/**
 * Class for handling SEND USSD proactive UICC commands.
 */
public class SendUssdCommandHandler extends Handler {
    private static final int MSG_ID_MMI_COMPLETE = 1;

    private final Handler mCaller;
    private final Context mContext;
    private final int mSubId;
    private final int mSlotId;
    private CommandDetails mCmdDet;
    private byte mCodingScheme;
    private String mRequest;

    public SendUssdCommandHandler(
            Looper looper, Handler caller, Context context, int subId, int slotId) {
        super(looper);
        mCaller = caller;
        mContext = context;
        mSubId = subId;
        mSlotId = slotId;
    }

    /**
     * Starts to send a USSD request based on the given command details.
     *
     * @param cmdDet Command details.
     * @param request USSD request string.
     * @param codingScheme Coding scheme.
     */
    public void start(CommandDetails cmdDet, String request, byte codingScheme) {
        mCmdDet = cmdDet;
        mCodingScheme = codingScheme;
        mRequest = request;
        CallManager callManager = CallManager.getInstance(mContext);
        callManager.registerForMmiComplete(this, MSG_ID_MMI_COMPLETE, null);

        try {
            Phone phone = callManager.getPhone(mSubId);
            if (phone != null) {
                callManager.dial(phone, request, new DialArgs.Builder<>().build());
            } else {
                CatLog.e(this, "Phone object is null");
                finishAndNotifyResult(ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, null);
            }
        } catch (Exception e) {
            CatLog.e(this, "Failed to dial USSD: " + e.getMessage());
            finishAndNotifyResult(ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, null);
        }
    }

    @Override
    public void handleMessage(Message msg) {
        if (msg.what != MSG_ID_MMI_COMPLETE) {
            return;
        }
        CatLog.d(this, "MSG_ID_MMI_COMPLETE received");

        AsyncResult ar = (AsyncResult) msg.obj;
        if (ar == null || !(ar.result instanceof MmiCode)) {
            CatLog.e(this, "Can't get MmiCode");
            finishAndNotifyResult(ResultCode.TERMINAL_CRNTLY_UNABLE_TO_PROCESS, null);
            return;
        }

        MmiCode mmi = (MmiCode) ar.result;
        if (mmi.getPhone().getPhoneId() != mSlotId || mmi.isPinPukCommand()) {
            CatLog.d(this, "Ignore unrelated MMI event");
            return;
        }

        if (mRequest == null || !mRequest.equals(mmi.getDialString())) {
            CatLog.d(this, "Ignore unrelated USSD event");
            return;
        }

        MmiCode.State state = mmi.getState();
        CatLog.d(this, "MMI state: " + state);
        if (state == MmiCode.State.COMPLETE) {
            ResponseData resp = new SendUssdResponseData(
                    mmi.getMessage() == null ? "" : mmi.getMessage().toString(),
                    mCodingScheme);
            finishAndNotifyResult(ResultCode.OK, resp);
        } else if (state == MmiCode.State.FAILED) {
            finishAndNotifyResult(ResultCode.USSD_RETURN_ERROR, null);
        } else if (state == MmiCode.State.CANCELLED) {
            finishAndNotifyResult(ResultCode.UICC_SESSION_TERM_BY_USER, null);
        }
    }

    private void finishAndNotifyResult(ResultCode resultCode, ResponseData resp) {
        CatLog.d(this, "Finishing with result: " + resultCode);
        CallManager.getInstance(mContext).unregisterForMmiComplete(this);
        mCaller.obtainMessage(CatService.MSG_ID_NOTIFY_SEND_USSD_RESULT,
                new UssdResult(mCmdDet, resultCode, resp)).sendToTarget();
    }

    /**
     * Container class for USSD result to be sent back to CatService.
     */
    public static class UssdResult {
        public final CommandDetails cmdDet;
        public final ResultCode resultCode;
        public final ResponseData resp;

        public UssdResult(CommandDetails cmdDet, ResultCode resultCode, ResponseData resp) {
            this.cmdDet = cmdDet;
            this.resultCode = resultCode;
            this.resp = resp;
        }
    }
}
