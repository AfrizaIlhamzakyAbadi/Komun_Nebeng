/*
 * Copyright (C) 2021 The Android Open Source Project
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

package android.service.selectiontoolbar;

import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.app.Service;
import android.content.ClipboardManager;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.ArrayMap;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.selectiontoolbar.ISelectionToolbarClient;
import android.view.selectiontoolbar.ISelectionToolbarService;
import android.view.selectiontoolbar.SelectionToolbarRequest;
import android.view.selectiontoolbar.ShowInfo;
import android.view.selectiontoolbar.ToolbarMenuItem;
import android.view.selectiontoolbar.WidgetInfo;
import android.window.InputTransferToken;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;

/**
 * Service for rendering selection toolbar.
 */
@Hide
public abstract class SelectionToolbarRenderService2 extends Service {

    private static final String TAG = "SelectionToolbarRenderService2";

    /**
     * The {@link Intent} that must be declared as handled by the service.
     *
     * <p>To be supported, the service must also require the
     * {@link android.Manifest.permission#BIND_SELECTION_TOOLBAR_RENDER_SERVICE} permission so
     * that other applications can not abuse it.
     */
    public static final String SERVICE_INTERFACE =
            "android.service.selectiontoolbar.SelectionToolbarRenderService2";

    private final HandlerThread mThread =
            new HandlerThread(SelectionToolbarRenderService2.class.getSimpleName());
    private final Executor mThreadExecutor;

    private final ArrayMap<IBinder, SessionRecord> mSessions = new ArrayMap<>();
    /**
     * Whether the uid has a session.
     */
    private final SparseBooleanArray mHasSession = new SparseBooleanArray();

    private record SessionRecord(int mUid, SelectionToolbarRequest mRequest,
            ISelectionToolbarClient mClient, IBinder.DeathRecipient mDeathRecipient) {
    }

    public SelectionToolbarRenderService2() {
        mThread.start();
        mThreadExecutor = mThread.getThreadExecutor();
    }

    protected Handler getThreadHandler() {
        return mThread.getThreadHandler();
    }

    private void handleClientDied(int uid, @NonNull IBinder client) {
        SessionRecord record = mSessions.remove(client);
        if (record != null) {
            mHasSession.delete(uid);
            onClose(client);
        }
    }

    /**
     * Binder to receive calls from host.
     */
    private final ISelectionToolbarService mInterface =
            new ISelectionToolbarService.Stub() {

                @Override
                public void openSession(@NonNull SelectionToolbarRequest request,
                        @NonNull ISelectionToolbarClient selectionToolbarClient) {
                    Objects.requireNonNull(request, "request must not be null.");
                    Objects.requireNonNull(selectionToolbarClient, "client must not be null.");
                    int uid = Binder.getCallingUid();
                    IBinder client = selectionToolbarClient.asBinder();
                    mThreadExecutor.execute(() -> {
                        if (mHasSession.get(uid)) {
                            boolean isSameClient = mSessions.containsKey(client);
                            Log.w(TAG, "Session already exists for uid: " + uid
                                    + (isSameClient ? " using client: " + client : ""));
                            return;
                        }
                        DeathRecipient deathRecipient = () -> mThreadExecutor.execute(
                                () -> handleClientDied(uid, client));
                        try {
                            client.linkToDeath(deathRecipient, 0);
                        } catch (RemoteException e) {
                            Log.e(TAG, "ISelectionToolbarClient has already died");
                            return;
                        }

                        SessionRecord record = new SessionRecord(uid, request,
                                selectionToolbarClient, deathRecipient);
                        RemoteCallbackWrapper remoteCallbackWrapper = new RemoteCallbackWrapper(
                                selectionToolbarClient, deathRecipient);

                        mSessions.put(client, record);
                        mHasSession.put(uid, true);

                        ShowInfo showInfo = createShowInfo(request.isLayoutRequired(),
                                request.getMenuItems(), request.getContentRect(),
                                request.getSuggestedWidth(), request.getViewPortOnScreen(),
                                request.getHostInputToken(), request.isLightTheme(),
                                request.getConfiguration());
                        onShow(client, uid, showInfo, remoteCallbackWrapper);
                    });
                }

                @Override
                public void updateSession(@NonNull ISelectionToolbarClient selectionToolbarClient,
                        boolean layoutRequired, @NonNull List<ToolbarMenuItem> menuItems,
                        @NonNull Rect contentRect, int suggestedWidth) {
                    Objects.requireNonNull(selectionToolbarClient, "client must not be null.");
                    IBinder client = selectionToolbarClient.asBinder();
                    mThreadExecutor.execute(() -> {
                        SessionRecord record = mSessions.get(client);
                        if (record == null) {
                            Log.w(TAG, "updateSession: Session not found for client: "
                                    + client);
                            return;
                        }
                        ShowInfo showInfo = createShowInfo(layoutRequired, menuItems, contentRect,
                                suggestedWidth, record.mRequest.getViewPortOnScreen(),
                                record.mRequest.getHostInputToken(), record.mRequest.isLightTheme(),
                                record.mRequest.getConfiguration());
                        onUpdate(client, showInfo);
                    });
                }

                @Override
                public void hideSession(@NonNull ISelectionToolbarClient selectionToolbarClient) {
                    Objects.requireNonNull(selectionToolbarClient, "client must not be null.");
                    IBinder client = selectionToolbarClient.asBinder();
                    mThreadExecutor.execute(() -> {
                        SessionRecord record = mSessions.get(client);
                        if (record == null) {
                            Log.w(TAG, "hideSession: Session not found for client: " + client);
                            return;
                        }
                        onHide(client);
                    });
                }

                @Override
                public void closeSession(@NonNull ISelectionToolbarClient selectionToolbarClient) {
                    Objects.requireNonNull(selectionToolbarClient, "client must not be null.");
                    int uid = Binder.getCallingUid();
                    IBinder client = selectionToolbarClient.asBinder();
                    mThreadExecutor.execute(() -> {
                        SessionRecord record = mSessions.remove(client);
                        if (record == null) {
                            Log.w(TAG, "closeSession: Session not found for client: "
                                    + client);
                            return;
                        }
                        mHasSession.delete(uid);
                        client.unlinkToDeath(record.mDeathRecipient, 0);
                        onClose(client);
                    });
                }
            };

    @Override
    @Nullable
    public final IBinder onBind(@NonNull Intent intent) {
        if (android.permission.flags.Flags.systemSelectionToolbarSessionEnabled()) {
            return mInterface.asBinder();
        }
        return null;
    }

    protected void onPasteAction(int uid) {
        getSystemService(ClipboardManager.class).notifyUserAuthorizedClipAccess(uid);
    }

    /**
     * Called when showing the selection toolbar.
     */
    public abstract void onShow(@NonNull IBinder client, int uid, @NonNull ShowInfo showInfo,
            @NonNull SelectionToolbarRenderCallback callback);

    /**
     * Called when updating the selection toolbar.
     */
    public abstract void onUpdate(@NonNull IBinder client, @NonNull ShowInfo showInfo);

    /**
     * Called when hiding the selection toolbar.
     */
    public abstract void onHide(@NonNull IBinder client);

    /**
     * Called when dismissing the selection toolbar.
     */
    public abstract void onClose(@NonNull IBinder client);

    /**
     * Callback to notify the client toolbar events.
     */
    public class RemoteCallbackWrapper implements SelectionToolbarRenderCallback {
        // This subclasses SelectionToolbarRenderService.RemoteCallbackWrapper to avoid redundant
        // code in service implementation

        private final ISelectionToolbarClient mClient;
        private final IBinder.DeathRecipient mDeathRecipient;

        private RemoteCallbackWrapper(@NonNull ISelectionToolbarClient mSelectionToolbarClient,
                @NonNull IBinder.DeathRecipient deathRecipient) {
            mClient = mSelectionToolbarClient;
            mDeathRecipient = deathRecipient;
        }

        @Override
        public void onShown(WidgetInfo widgetInfo) {
            mThreadExecutor.execute(() -> {
                try {
                    mClient.onSessionOpened();

                    // Adapter logic to update
                    mClient.onUpdated(widgetInfo.surfacePackage, widgetInfo.contentRect,
                            widgetInfo.touchableRegion);
                } catch (RemoteException e) {
                    e.rethrowFromSystemServer();
                }
            });
        }

        @Override
        public void onInvisible() {
            try {
                mClient.onVisibilityChanged(false);
            } catch (RemoteException e) {
                // no-op
            }
        }

        @Override
        public void onUpdated(WidgetInfo widgetInfo) {
            try {
                mClient.onUpdated(widgetInfo.surfacePackage, widgetInfo.contentRect,
                        widgetInfo.touchableRegion);
            } catch (RemoteException e) {
                // no-op
            }
        }

        @Override
        public void onMenuItemClicked(int itemIndex) {
            try {
                mClient.onMenuItemClicked(itemIndex);
            } catch (RemoteException e) {
                // no-op
            }
        }
    }

    @NonNull
    private static ShowInfo createShowInfo(boolean layoutRequired,
            @NonNull List<ToolbarMenuItem> menuItems, @NonNull Rect contentRect, int suggestedWidth,
            @NonNull Rect viewPortOnScreen, @NonNull InputTransferToken hostInputToken,
            boolean isLightTheme, @NonNull Configuration configuration) {
        ShowInfo showInfo = new ShowInfo();
        showInfo.layoutRequired = layoutRequired;
        showInfo.menuItems = menuItems;
        showInfo.contentRect = contentRect;
        showInfo.suggestedWidth = suggestedWidth;
        showInfo.viewPortOnScreen = viewPortOnScreen;
        showInfo.hostInputToken = hostInputToken.getToken();
        showInfo.isLightTheme = isLightTheme;
        showInfo.configuration = configuration;
        return showInfo;
    }
}
