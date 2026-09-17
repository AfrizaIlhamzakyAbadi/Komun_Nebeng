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

package android.content;

import android.annotation.Hide;
import android.annotation.NonNull;
import android.app.ActivityManager;
import android.app.ActivityThread;
import android.app.IActivityManager;
import android.app.QueuedWork;
import android.util.Slog;

/**
 * Implementation of {@link ReceiverFinishController} that reports
 * completion back to the system server.
 */
@Hide
public class RemoteReceiverFinishController implements ReceiverFinishController {
    private static final RemoteReceiverFinishController sInstance =
            new RemoteReceiverFinishController();

    /**
     * Get the instance of {@link RemoteReceiverFinishController}.
     */
    public static @NonNull RemoteReceiverFinishController getInstance() {
        return sInstance;
    }

    private RemoteReceiverFinishController() {}

    @Override
    public void finishReceiver(@NonNull BroadcastReceiver.PendingResult res) {
        if (res.mType == BroadcastReceiver.PendingResult.TYPE_COMPONENT) {
            final IActivityManager mgr = ActivityManager.getService();
            if (QueuedWork.hasPendingWork()) {
                // If this is a broadcast component, we need to make sure any
                // queued work is complete before telling AM we are done, so
                // we don't have our process killed before that.  We now know
                // there is pending work; put another piece of work at the end
                // of the list to finish the broadcast, so we don't block this
                // thread (which may be the main thread) to have it finished.
                //
                // Note that we don't need to use QueuedWork.addFinisher() with the
                // runnable, since we know the AM is waiting for us until the
                // executor gets to it.
                QueuedWork.queue(new Runnable() {
                    @Override
                    public void run() {
                        if (ActivityThread.DEBUG_BROADCAST) {
                            Slog.i(ActivityThread.TAG,
                                    "Finishing broadcast after work to component "
                                            + res.mToken);
                        }
                        res.sendFinished(mgr);
                    }
                }, false);
            } else {
                if (ActivityThread.DEBUG_BROADCAST) {
                    Slog.i(ActivityThread.TAG,
                            "Finishing broadcast to component " + res.mToken);
                }
                res.sendFinished(mgr);
            }
        } else {
            if (ActivityThread.DEBUG_BROADCAST) {
                Slog.i(ActivityThread.TAG,
                        "Finishing broadcast to " + res.mToken);
            }
            final IActivityManager mgr = ActivityManager.getService();
            res.sendFinished(mgr);
        }
    }
}
