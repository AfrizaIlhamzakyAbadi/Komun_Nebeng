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

package android.view.selectiontoolbar;

import android.annotation.Hide;
import android.annotation.NonNull;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;

import com.android.internal.R;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * A factory for creating {@link SelectionToolbarProvider} instances.
 */
@Hide
public class SelectionToolbarProviderFactory {
    private static final String LOG_TAG = "SelectionToolbar";

    private SelectionToolbarProviderFactory() {
    }

    /**
     * Creates a new instance of the {@link SelectionToolbarProvider}.
     *
     * <p>This method should be called to obtain a provider object, which can then be used to
     * create and manage Selection Toolbar sessions.
     *
     * @param context The {@link Context} of the calling application. This is used to access
     *                system services and resources required by the Selection Toolbar.
     * @return A new {@link SelectionToolbarProvider} instance that can be used to open selection
     * toolbar sessions.
     */
    @NonNull
    public static SelectionToolbarProvider create(@NonNull Context context) {
        return new SelectionToolbarProviderImpl(context);
    }

    static final class SelectionToolbarProviderImpl extends SelectionToolbarProviderFactory
            implements SelectionToolbarProvider {
        private static final int OPEN_SESSION_ATTEMPTS_LIMIT = 5;
        private static final boolean DEBUG = Log.isLoggable(LOG_TAG, Log.DEBUG);
        private static final long UNBIND_SERVICE_DELAY_MILLIS = 5000;

        private final Context mContext;
        private ServiceConnectionHandler mConnection;
        private ISelectionToolbarService mService;
        private final Handler mHandler = new Handler(Objects.requireNonNull(Looper.myLooper()));

        private final SerialExecutor mSerialExecutor = new SerialExecutor(Runnable::run);

        private final Runnable mUnbindRunnable = () -> mSerialExecutor.execute(
                this::unbindServiceInternalSerialized);

        private final ArrayDeque<OpenSessionRequest> mPendingOpenSessionRequests =
                new ArrayDeque<>();
        private final ArrayMap<SelectionToolbarClient, SelectionToolbarSessionRecord>
                mOpenSessions = new ArrayMap<>();

        SelectionToolbarProviderImpl(@NonNull Context context) {
            mContext = context;
            mConnection = new ServiceConnectionHandler(mContext, this);
        }

        @Override
        public void openSession(@NonNull SelectionToolbarRequest request,
                @NonNull Executor clientExecutor, @NonNull SelectionToolbarClient client) {
            if (DEBUG) {
                Log.d(LOG_TAG, "openSession received for client " + client);
            }
            Objects.requireNonNull(request, "request must not be null");
            Objects.requireNonNull(clientExecutor, "clientExecutor must not be null");
            Objects.requireNonNull(client, "client must not be null");

            mSerialExecutor.execute(
                    () -> openSessionSerialized(request, clientExecutor, client));
        }

        void updateSession(@NonNull SelectionToolbarClientWrapper clientWrapper,
                boolean isLayoutRequired, List<ToolbarMenuItem> menuItems,
                Rect contentRect, int suggestedWidth) {
            mSerialExecutor.execute(() -> {
                if (mService != null) {
                    try {
                        mService.updateSession(clientWrapper, isLayoutRequired, menuItems,
                                contentRect, suggestedWidth);
                    } catch (RemoteException e) {
                        Log.e(LOG_TAG, "Failed to call updateSession", e);
                    }
                } else {
                    Log.w(LOG_TAG, "updateSession: service is null");
                }
            });
        }

        void hideSession(@NonNull SelectionToolbarClientWrapper clientWrapper) {
            mSerialExecutor.execute(() -> {
                if (mService != null) {
                    try {
                        mService.hideSession(clientWrapper);
                    } catch (RemoteException e) {
                        Log.e(LOG_TAG, "Failed to call hideSession", e);
                    }
                } else {
                    Log.w(LOG_TAG, "hideSession: service is null");
                }
            });
        }

        void closeSession(@NonNull SelectionToolbarClientWrapper clientWrapper) {
            mSerialExecutor.execute(() -> {
                if (mService != null) {
                    try {
                        mService.closeSession(clientWrapper);
                    } catch (RemoteException e) {
                        Log.e(LOG_TAG, "Failed to call closeSession", e);
                    }
                } else {
                    Log.w(LOG_TAG, "closeSession: service is null");
                }
            });
        }

        private void openSessionSerialized(@NonNull SelectionToolbarRequest request,
                @NonNull Executor clientExecutor, @NonNull SelectionToolbarClient client) {
            mHandler.removeCallbacks(mUnbindRunnable);
            SelectionToolbarClientWrapper clientWrapper = new SelectionToolbarClientWrapper(this,
                    client, new SerialExecutor(clientExecutor));
            OpenSessionRequest sessionRequest = new OpenSessionRequest(request, client,
                    clientWrapper, clientExecutor);
            if (DEBUG) {
                Log.d(LOG_TAG, "openSession request received with params = " + sessionRequest);
            }
            ensureValidServiceConnectionExistsSerialized();
            if (mConnection.isConnected()) {
                openSessionInternalSerialized(sessionRequest);
            } else {
                bindServiceAndSaveRequestSerialized(sessionRequest);
            }
        }

        private void ensureValidServiceConnectionExistsSerialized() {
            if (!mConnection.isConnectionValid()) {
                mConnection = new ServiceConnectionHandler(mContext, this);
            }
        }

        private void bindServiceAndSaveRequestSerialized(
                @NonNull OpenSessionRequest sessionRequest) {
            ensureValidServiceConnectionExistsSerialized();
            mConnection.bindServiceSerialized();
            if (mConnection.isBindRequested()) {
                sessionRequest.mTotalOpenSessionAttempts++;
                mPendingOpenSessionRequests.add(sessionRequest);
            } else {
                reportSessionErrorSerialized(sessionRequest,
                        new RuntimeException("Unable to bind selection toolbar service."));
            }
        }

        private void openSessionInternalSerialized(@NonNull OpenSessionRequest sessionRequest) {
            try {
                mService.openSession(sessionRequest.mRequest,
                        sessionRequest.mClientCallbackWrapper);
            } catch (DeadObjectException e) {
                Log.e(LOG_TAG, "Couldn't make call to remote delegate. Retrying.", e);

                mConnection.disposeSerialized();
                mService = null;
                if (sessionRequest.mTotalOpenSessionAttempts < OPEN_SESSION_ATTEMPTS_LIMIT) {
                    bindServiceAndSaveRequestSerialized(sessionRequest);
                } else {
                    reportSessionErrorSerialized(sessionRequest,
                            new RuntimeException("Unable to get valid remote delegate."));
                }
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "Remote delegate is Invalid! Failed to open session.", e);
                reportSessionErrorSerialized(sessionRequest, new RemoteException(
                        "Remote delegate is Invalid! Failed to open session"));
            }
        }

        private boolean performBindServiceSerialized(@NonNull Intent intent) {
            boolean bindRequested = true;
            try {
                bindRequested = mContext.bindService(intent, Context.BIND_AUTO_CREATE,
                        mSerialExecutor, mConnection);

                if (!bindRequested) {
                    removePendingRequestsAndNotifyClients(
                            new RuntimeException("Unable to bind selection toolbar service."));
                }
            } catch (SecurityException e) {
                bindRequested = false;
                removePendingRequestsAndNotifyClients(
                        new SecurityException("Unable to bind selection toolbar service."));
            } finally {
                if (!bindRequested) {
                    mConnection.unbindAndDisposeConnectionSerialized();
                }
            }
            return bindRequested;
        }

        private void onServiceConnectedSerialized(@NonNull IBinder service) {
            mService = ISelectionToolbarService.Stub.asInterface(service);

            while (!mPendingOpenSessionRequests.isEmpty()) {
                openSessionInternalSerialized(mPendingOpenSessionRequests.remove());
            }
        }

        private void onServiceDisconnectedSerialized() {
            mService = null;
            closeActiveSessionsAndNotifyClients(
                    new RuntimeException("Service disconnected, Session closed."));
            mConnection.bindServiceSerialized();
        }

        private void onBindingDiedSerialized() {
            closeActiveSessionsAndNotifyClients(
                    new RuntimeException("Service died, Session closed."));
            mConnection.unbindAndDisposeConnectionSerialized();
            ensureValidServiceConnectionExistsSerialized();
            mConnection.bindServiceSerialized();
        }

        private void onServiceNullBindingSerialized() {
            mService = null;
            removePendingRequestsAndNotifyClients(
                    new RuntimeException("Unable to bind selection toolbar service (null)."));
        }

        private void removePendingRequestsAndNotifyClients(@NonNull Throwable cause) {
            while (!mPendingOpenSessionRequests.isEmpty()) {
                reportSessionErrorSerialized(mPendingOpenSessionRequests.remove(), cause);
            }
        }

        private void reportSessionErrorSerialized(@NonNull OpenSessionRequest request,
                @NonNull Throwable cause) {
            request.mClientExecutor.execute(() -> request.mClientCallback.onSessionError(cause));
        }

        void onSessionClosed(@NonNull SelectionToolbarClient client) {
            if (DEBUG) {
                Log.d(LOG_TAG, "onSessionClosed received for client " + client);
            }
            mSerialExecutor.execute(() -> onSessionClosedSerialized(client));
        }

        void addActiveSessionRecord(@NonNull SelectionToolbarClient client,
                @NonNull SelectionToolbarSessionRecord session) {
            if (DEBUG) {
                Log.d(LOG_TAG, "addActiveSessionRecord received for client " + client);
            }
            mSerialExecutor.execute(() -> mOpenSessions.put(client, session));
        }

        private void onSessionClosedSerialized(@NonNull SelectionToolbarClient client) {
            if (DEBUG) {
                Log.d(LOG_TAG, "onSessionClosedSerialized received for client " + client);
            }
            if (mOpenSessions.remove(client) == null) {
                return;
            }

            if (mOpenSessions.isEmpty() && mPendingOpenSessionRequests.isEmpty()) {
                mHandler.removeCallbacks(mUnbindRunnable);
                mHandler.postDelayed(mUnbindRunnable, UNBIND_SERVICE_DELAY_MILLIS);
            }
        }

        private void unbindServiceInternalSerialized() {
            if (mOpenSessions.isEmpty() && mPendingOpenSessionRequests.isEmpty()) {
                if (DEBUG) {
                    Log.d(LOG_TAG, "Unbinding service as no active session open & pending request.");
                }
                mService = null;
                mConnection.unbindAndDisposeConnectionSerialized();
            }
        }

        private void closeActiveSessionsAndNotifyClients(Throwable cause) {
            if (DEBUG) {
                Log.d(LOG_TAG, "Closing all active sessions with cause", cause);
            }
            for (int i = 0; i < mOpenSessions.size(); i++) {
                SelectionToolbarClient client = mOpenSessions.keyAt(i);
                SelectionToolbarSessionRecord sessionWrapper = mOpenSessions.valueAt(i);
                sessionWrapper.clientExecutor.execute(() -> client.onSessionError(cause));
            }
            mOpenSessions.clear();
        }

        private static class ServiceConnectionHandler implements ServiceConnection {
            private boolean mIsConnected;
            private boolean mIsBindRequested;
            private boolean mIsConnectionValid = true;
            private Context mContext;
            private SelectionToolbarProviderImpl mProvider;
            private final Intent mIntent;

            ServiceConnectionHandler(@NonNull Context context,
                    @NonNull SelectionToolbarProviderImpl provider) {
                mContext = context;
                mProvider = provider;
                mIntent = new Intent();
                mIntent.setComponent(ComponentName.unflattenFromString(
                        context.getResources().getString(
                                R.string.config_systemUiSelectionToolbarRenderService2)));
            }

            private void disposeSerialized() {
                if (DEBUG) {
                    Log.d(LOG_TAG, "ServiceConnection disposing previous states");
                }
                mProvider = null;
                mIsConnected = false;
                mIsBindRequested = false;
                mIsConnectionValid = false;
                mContext = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                if (DEBUG) {
                    Log.d(LOG_TAG, "onServiceConnected: " + name);
                }
                if (mProvider != null) {
                    mIsConnected = true;
                    mIsBindRequested = false;
                    mProvider.onServiceConnectedSerialized(service);
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                if (DEBUG) {
                    Log.d(LOG_TAG, "onServiceDisconnected: " + name);
                }
                if (mProvider != null) {
                    mIsConnected = false;
                    mIsBindRequested = false;
                    mProvider.onServiceDisconnectedSerialized();
                }
            }

            @Override
            public void onNullBinding(ComponentName name) {
                if (DEBUG) {
                    Log.d(LOG_TAG, "onNullBinding: " + name);
                }
                if (mProvider != null) {
                    mIsConnected = false;
                    mIsBindRequested = false;
                    unbindAndDisposeConnectionSerialized();
                    mProvider.onServiceNullBindingSerialized();
                }
            }

            @Override
            public void onBindingDied(ComponentName name) {
                if (DEBUG) {
                    Log.d(LOG_TAG, "onBindingDied: " + name);
                }
                if (mProvider != null) {
                    mIsConnected = false;
                    mIsBindRequested = false;
                    mProvider.onBindingDiedSerialized();
                }
            }

            private void bindServiceSerialized() {
                if (DEBUG) {
                    Log.d(LOG_TAG, "bindServiceSerialized");
                }
                if (mProvider != null && !mIsBindRequested) {
                    mIsBindRequested = mProvider.performBindServiceSerialized(mIntent);
                }
            }

            private void unbindAndDisposeConnectionSerialized() {
                if (DEBUG) {
                    Log.d(LOG_TAG, "unbindAndDisposeConnectionSerialized");
                }
                try {
                    mContext.unbindService(this);
                } catch (IllegalArgumentException ex) {
                    // Ignore if the service was already unbound.
                }
                disposeSerialized();
            }

            private boolean isConnected() {
                return mIsConnected;
            }

            private boolean isBindRequested() {
                return mIsBindRequested;
            }

            private boolean isConnectionValid() {
                return mIsConnectionValid;
            }
        }

        private static class OpenSessionRequest {
            private final SelectionToolbarRequest mRequest;
            private final Executor mClientExecutor;
            private final SelectionToolbarClient mClientCallback;
            private final SelectionToolbarClientWrapper mClientCallbackWrapper;
            private int mTotalOpenSessionAttempts = 0;

            private OpenSessionRequest(@NonNull SelectionToolbarRequest request,
                    @NonNull SelectionToolbarClient clientCallback,
                    @NonNull SelectionToolbarClientWrapper clientCallbackWrapper,
                    @NonNull Executor clientExecutor) {
                mRequest = request;
                mClientCallback = clientCallback;
                mClientCallbackWrapper = clientCallbackWrapper;
                mClientExecutor = clientExecutor;
            }

            @Override
            public String toString() {
                return "OpenSessionRequest{" + "mRequest=" + mRequest
                        + ", mClientCallbackWrapper=" + mClientCallbackWrapper
                        + ", mClientCallback=" + mClientCallback
                        + ", mClientExecutor=" + mClientExecutor + '}';
            }
        }
    }

    private static class SerialExecutor implements Executor {
        final ArrayDeque<Runnable> mTasks = new ArrayDeque<>();
        final Executor mExecutor;
        Runnable mActive;

        SerialExecutor(Executor executor) {
            this.mExecutor = executor;
        }

        @Override
        public synchronized void execute(@NonNull Runnable r) {
            mTasks.add(() -> {
                try {
                    r.run();
                } finally {
                    scheduleNext();
                }
            });
            if (mActive == null) {
                scheduleNext();
            }
        }

        private synchronized void scheduleNext() {
            if ((mActive = mTasks.poll()) != null) {
                mExecutor.execute(mActive);
            }
        }
    }

    record SelectionToolbarSessionRecord(
            @NonNull Executor clientExecutor) {
    }
}
