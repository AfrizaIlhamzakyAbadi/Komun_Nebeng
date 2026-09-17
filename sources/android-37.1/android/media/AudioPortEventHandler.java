/*
 * Copyright (C) 2014 The Android Open Source Project
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

package android.media;

import android.annotation.Hide;
import android.compat.annotation.UnsupportedAppUsage;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.android.internal.annotations.GuardedBy;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * The AudioPortEventHandler handles AudioManager.OnAudioPortUpdateListener callbacks
 * posted from JNI
 */
@Hide

class AudioPortEventHandler {
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private final Object mLock = new Object();

    @GuardedBy("mLock")
    private final ArrayList<AudioManager.OnAudioPortUpdateListener> mListeners =
            new ArrayList<AudioManager.OnAudioPortUpdateListener>();

    private static final String TAG = "AudioPortEventHandler";

    private static final int AUDIOPORT_EVENT_PORT_LIST_UPDATED = 1;
    private static final int AUDIOPORT_EVENT_PATCH_LIST_UPDATED = 2;
    private static final int AUDIOPORT_EVENT_SERVICE_DIED = 3;
    private static final int AUDIOPORT_EVENT_NEW_LISTENER = 4;

    private static final long RESCHEDULE_MESSAGE_DELAY_MS = 100;

    /**
     * Accessed by native methods: JNI Callback context.
     */
    @SuppressWarnings("unused")
    @UnsupportedAppUsage(maxTargetSdk = Build.VERSION_CODES.R, trackingBug = 170729553)
    private long mJniCallback;

    void init() {
        synchronized (mLock) {
            if (mHandler != null) {
                return;
            }
            // create a new thread for our new event handler
            mHandlerThread = new HandlerThread(TAG);
            mHandlerThread.start();

            if (mHandlerThread.getLooper() != null) {
                mHandler = new Handler(mHandlerThread.getLooper()) {
                    @Override
                    public void handleMessage(Message msg) {
                        ArrayList<AudioManager.OnAudioPortUpdateListener> listeners;
                        synchronized (mLock) {
                            if (msg.what == AUDIOPORT_EVENT_NEW_LISTENER) {
                                listeners = new ArrayList<AudioManager.OnAudioPortUpdateListener>();
                                if (mListeners.contains(msg.obj)) {
                                    listeners.add((AudioManager.OnAudioPortUpdateListener)msg.obj);
                                }
                            } else {
                                listeners = (ArrayList<AudioManager.OnAudioPortUpdateListener>)
                                        mListeners.clone();
                            }
                        }
                        // reset audio port cache if the event corresponds to a change coming
                        // from audio policy service or if mediaserver process died.
                        if (msg.what == AUDIOPORT_EVENT_PORT_LIST_UPDATED) {
                            if (msg.arg1 == AudioPort.TYPE_DEVICE) {
                                AudioManager.resetAudioDevicePortGeneration();
                            } else {
                                AudioManager.resetAudioMixPortGeneration();
                            }
                        }
                        if (msg.what == AUDIOPORT_EVENT_SERVICE_DIED) {
                            AudioManager.resetAudioPortGeneration();
                        }
                        if (msg.what == AUDIOPORT_EVENT_PATCH_LIST_UPDATED) {
                            AudioManager.resetAudioPatchPortGeneration();
                        }

                        if (listeners.isEmpty()) {
                            return;
                        }


                        switch (msg.what) {
                            case AUDIOPORT_EVENT_NEW_LISTENER:
                            case AUDIOPORT_EVENT_PORT_LIST_UPDATED: {
                                ArrayList<AudioPort> ports = new ArrayList<AudioPort>();
                                int status = AudioManager.updateAudioPortCache(ports, null,
                                        msg.what == AUDIOPORT_EVENT_PORT_LIST_UPDATED
                                                ? msg.arg1 : AudioPort.TYPE_NONE);
                                if (status != AudioManager.SUCCESS) {
                                    return;
                                }
                                AudioPort[] portList = ports.toArray(new AudioPort[0]);
                                for (int i = 0; i < listeners.size(); i++) {
                                    listeners.get(i).onAudioPortListUpdate(portList);
                                }
                                if (msg.what == AUDIOPORT_EVENT_PORT_LIST_UPDATED) {
                                    break;
                                }
                            } // FALL THROUGH

                            case AUDIOPORT_EVENT_PATCH_LIST_UPDATED: {
                                ArrayList<AudioPatch> patches = new ArrayList<AudioPatch>();
                                int status = AudioManager.updateAudioPatchCache(patches);
                                if (status != AudioManager.SUCCESS) {
                                    return;
                                }
                                AudioPatch[] patchList = patches.toArray(new AudioPatch[0]);
                                for (int i = 0; i < listeners.size(); i++) {
                                    listeners.get(i).onAudioPatchListUpdate(patchList);
                                }
                            } break;

                            case AUDIOPORT_EVENT_SERVICE_DIED:
                                for (int i = 0; i < listeners.size(); i++) {
                                    listeners.get(i).onServiceDied();
                                }
                                break;

                            default:
                                break;
                        }
                    }
                };
                native_setup(new WeakReference<AudioPortEventHandler>(this));
            } else {
                mHandler = null;
            }
        }
    }

    private native void native_setup(Object module_this);

    @Override
    protected void finalize() {
        native_finalize();
        if (mHandlerThread.isAlive()) {
            mHandlerThread.quit();
        }
    }
    private native void native_finalize();

    void registerListener(AudioManager.OnAudioPortUpdateListener l) {
        synchronized (mLock) {
            mListeners.add(l);
        }
        if (mHandler != null) {
            Message m = mHandler.obtainMessage(AUDIOPORT_EVENT_NEW_LISTENER, 0, 0, l);
            mHandler.sendMessage(m);
        }
    }

    void unregisterListener(AudioManager.OnAudioPortUpdateListener l) {
        synchronized (mLock) {
            mListeners.remove(l);
        }
    }

    Handler handler() {
        return mHandler;
    }

    @SuppressWarnings("unused")
    @UnsupportedAppUsage(maxTargetSdk = Build.VERSION_CODES.R, trackingBug = 170729553)
    private static void postEventFromNative(Object module_ref,
                                            int what, int arg1, int arg2, Object obj) {
        AudioPortEventHandler eventHandler =
                (AudioPortEventHandler)((WeakReference)module_ref).get();
        if (eventHandler == null) {
            return;
        }

        if (eventHandler != null) {
            Handler handler = eventHandler.handler();
            if (handler != null) {
                Message m = handler.obtainMessage(what, arg1, arg2, obj);
                if (what != AUDIOPORT_EVENT_NEW_LISTENER) {
                    // Except AUDIOPORT_EVENT_NEW_LISTENER, we can only respect the last message.
                    handler.removeMessages(what);
                }
                handler.sendMessage(m);
            }
        }
    }

}
