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

package com.android.internal.telephony.satellite;

import static android.telephony.CarrierConfigManager.KEY_CARRIER_ROAMING_SATELLITE_UPSELL_NOTIFICATION_HYSTERESIS_SEC_INT;
import static android.telephony.CarrierConfigManager.KEY_CARRIER_ROAMING_SATELLITE_UPSELL_NOTIFICATION_MAXIMUM_DAILY_COUNT_INT;
import static android.telephony.CarrierConfigManager.KEY_CARRIER_ROAMING_SATELLITE_UPSELL_NOTIFICATION_MAXIMUM_MONTHLY_COUNT_INT;
import static android.telephony.CarrierConfigManager.KEY_CARRIER_ROAMING_SATELLITE_UPSELL_NOTIFICATION_THROTTLE_HOURS_INT;
import static android.telephony.CarrierConfigManager.KEY_CARRIER_ROAMING_SATELLITE_UPSELL_SUPPORTED_BOOL;
import static android.telephony.CarrierConfigManager.KEY_SATELLITE_CONFIGS_PER_PLMN_BUNDLE;
import static android.telephony.SubscriptionManager.INVALID_SUBSCRIPTION_ID;
import static android.telephony.TelephonyManager.EXTRA_SUBSCRIPTION_ID;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.UserHandle;
import android.telephony.CarrierConfigManager;
import android.telephony.PersistentLogger;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.SomeArgs;
import com.android.internal.telephony.Phone;
import com.android.internal.telephony.PhoneFactory;
import com.android.internal.telephony.flags.FeatureFlags;
import com.android.internal.telephony.util.WorkerThread;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class UpsellNotificationController extends Handler {
    private static final String TAG = "UpsellNotificationController";
    private static UpsellNotificationController sInstance;

    private final Context mContext;
    private final Executor mExecutor;
    private final TelephonyManager mTelephonyManager;
    private final CarrierConfigManager mCarrierConfigManager;
    private final SubscriptionManager mSubscriptionManager;
    private final NotificationManager mNotificationManager;
    private final FeatureFlags mFeatureFlags;
    private final AtomicInteger mActiveDataSubId = new AtomicInteger(INVALID_SUBSCRIPTION_ID);
    private final AtomicBoolean mIsNotificationShowing = new AtomicBoolean(false);

    /** Event definitions */
    private static final int EVENT_SERVICE_STATE_CHANGED = 1;
    private static final int EVENT_ACTIVE_DATA_SUB_CHANGED = 2;
    private static final int EVENT_CARRIER_CONFIG_CHANGED = 3;
    private static final int EVENT_ACTION_UPSELL_CLICKED = 4;
    private static final int EVENT_ACTION_UPSELL_DISMISSED = 5;
    private static final int EVENT_ACTION_UPSELL_SUPPRESSED = 6;
    private static final int EVENT_HYSTERESIS_TIMER_EXPIRED = 7;
    private static final int EVENT_SCAN_RETRY_TIMER_EXPIRED = 8;
    private static final int EVENT_TN_NETWORK_RESTORED = 9;
    private static final int EVENT_UPSELL_NOTI_INIT = 10;

    /** Notification constants */
    private static final int NOTIFICATION_ID = SatelliteController.NOTIFICATION_ID;
    private static final String NOTIFICATION_TAG = SatelliteController.NOTIFICATION_TAG;
    private static final String NOTIFICATION_CHANNEL = SatelliteController.NOTIFICATION_CHANNEL;
    private static final String NOTIFICATION_CHANNEL_ID =
            SatelliteController.NOTIFICATION_CHANNEL_ID;

    @Nullable
    private PersistentLogger mPersistentLogger;

    /** Intent Actions */
    private static final String ACTION_UPSELL_CLICKED =
            "com.android.internal.telephony.satellite.ACTION_UPSELL_CLICKED";
    private static final String ACTION_UPSELL_DISMISSED =
            "com.android.internal.telephony.satellite.ACTION_UPSELL_DISMISSED";
    private static final String ACTION_UPSELL_SUPPRESSED =
            "com.android.internal.telephony.satellite.ACTION_UPSELL_SUPPRESSED";

    /** Map for holding carrier configs per subscription, Key: subId, Value: PersistableBundle. */
    private final Map<Integer, PersistableBundle> mCarrierConfigMap = new ConcurrentHashMap<>();


    @VisibleForTesting
    public UpsellNotificationController(Context context, Looper looper, FeatureFlags featureFlags) {
        super(looper);
        mContext = context;
        mExecutor = new HandlerExecutor(this);
        mTelephonyManager = context.getSystemService(TelephonyManager.class);
        mCarrierConfigManager = context.getSystemService(CarrierConfigManager.class);
        mSubscriptionManager = context.getSystemService(SubscriptionManager.class);
        mNotificationManager = context.getSystemService(NotificationManager.class);
        mFeatureFlags = featureFlags;
        mPersistentLogger = SatelliteServiceUtils.getPersistentLogger(context);
    }

    /** Create and returns the singleton instance for UpsellNotificationController. */
    public static UpsellNotificationController make(Context context, FeatureFlags featureFlags) {
        if (sInstance == null) {
            sInstance = new UpsellNotificationController(
                    context,
                    WorkerThread.get().getLooper(),
                    featureFlags);
            sInstance.sendMessage(sInstance.obtainMessage(EVENT_UPSELL_NOTI_INIT));
        }
        return sInstance;
    }

    /** Returns existing instance for UpsellNotificationController. */
    public static UpsellNotificationController getInstance() {
        return sInstance;
    }

    /**
     * Sends a message with an object.
     *
     * @param what Message code.
     * @param obj  Object to send.
     */
    private void sendMessageAsync(int what, Object obj) {
        sendMessage(obtainMessage(what, obj));
    }

    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_UPSELL_NOTI_INIT -> {
                logd("EVENT_UPSELL_NOTI_INIT");
                init();
            }
            case EVENT_SERVICE_STATE_CHANGED -> logd("EVENT_SERVICE_STATE_CHANGED");
            case EVENT_ACTIVE_DATA_SUB_CHANGED -> logd("EVENT_ACTIVE_DATA_SUB_CHANGED");
            case EVENT_CARRIER_CONFIG_CHANGED -> {
                logd("EVENT_CARRIER_CONFIG_CHANGED");
                SomeArgs args = (SomeArgs) msg.obj;
                int slotIndex = (int) args.arg1;
                int subId = (int) args.arg2;
                int carrierId = (int) args.arg3;
                int specificCarrierId = (int) args.arg4;
                try {
                    handleCarrierConfigChanged(slotIndex, subId, carrierId, specificCarrierId);
                } finally {
                    args.recycle();
                }
            }
            case EVENT_ACTION_UPSELL_CLICKED -> logd("EVENT_ACTION_UPSELL_CLICKED");
            case EVENT_ACTION_UPSELL_DISMISSED -> logd("EVENT_ACTION_UPSELL_DISMISSED");
            case EVENT_ACTION_UPSELL_SUPPRESSED -> logd("EVENT_ACTION_UPSELL_SUPPRESSED");
            case EVENT_HYSTERESIS_TIMER_EXPIRED -> logd("EVENT_HYSTERESIS_TIMER_EXPIRED");
            case EVENT_TN_NETWORK_RESTORED -> {
                logd("EVENT_TN_NETWORK_RESTORED");
                if (msg.arg1 == mActiveDataSubId.get()) {
                    updateNotificationVisibility(msg.arg1, false);
                }
            }
            case EVENT_SCAN_RETRY_TIMER_EXPIRED -> logd("EVENT_SCAN_RETRY_TIMER_EXPIRED");
            default -> loge("Unexpected message: " + msg.what);
        }
    }

    private void init() {
        logd("init()");
        registerForServiceStateChanged();
        registerActiveDataSubIdListener();
        registerCarrierConfigChangeListener();
        registerNotificationInteractionReceiver();
    }

    /** Registers for service state change monitoring across all Phone objects (slots). */
    private void registerForServiceStateChanged() {
        for (Phone phone : PhoneFactory.getPhones()) {
            phone.registerForServiceStateChanged(this, EVENT_SERVICE_STATE_CHANGED, null);
        }
    }

    /** Monitors changes to the active data subscription. */
    private void registerActiveDataSubIdListener() {
        mSubscriptionManager.addOnSubscriptionsChangedListener(mExecutor,
                new SubscriptionManager.OnSubscriptionsChangedListener() {
                    @Override
                    public void onSubscriptionsChanged() {
                        int subId = SubscriptionManager.getActiveDataSubscriptionId();
                        sendMessage(obtainMessage(EVENT_ACTIVE_DATA_SUB_CHANGED, subId, 0));
                    }
                });
    }

    /* Registers a listener to detect and handle changes in carrier configuration for the subId */
    private void registerCarrierConfigChangeListener() {
        if (mCarrierConfigManager != null) {
            mCarrierConfigManager.registerCarrierConfigChangeListener(mExecutor,
                    (slotIndex, subId, carrierId, specificCarrierId) -> {
                        SomeArgs args = SomeArgs.obtain();
                        args.arg1 = slotIndex;
                        args.arg2 = subId;
                        args.arg3 = carrierId;
                        args.arg4 = specificCarrierId;
                        sendMessageAsync(EVENT_CARRIER_CONFIG_CHANGED, args);
                    }
            );
            logd("CarrierConfigChangeListener registered.");
        }
    }

    /*  A broadcast receiver that handles user interactions with the upsell notification */
    private final BroadcastReceiver mNotificationInteractionBroadcastReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent receivedIntent) {
                    String intentAction = receivedIntent.getAction();
                    if (TextUtils.isEmpty(intentAction)) {
                        loge("Received empty action from the notification");
                        return;
                    }

                    logd("Notification Broadcast received action = " + receivedIntent.getAction());

                    int subId = receivedIntent.getIntExtra(EXTRA_SUBSCRIPTION_ID,
                            INVALID_SUBSCRIPTION_ID);
                    if (subId == INVALID_SUBSCRIPTION_ID) {
                        loge("Received intent with INVALID_SUBSCRIPTION_ID for action: "
                                + intentAction);
                        return;
                    }
                    switch (intentAction) {
                        case ACTION_UPSELL_CLICKED ->
                                sendMessageAsync(EVENT_ACTION_UPSELL_CLICKED, subId);
                        case ACTION_UPSELL_DISMISSED ->
                            sendMessageAsync(EVENT_ACTION_UPSELL_DISMISSED, subId);
                        case ACTION_UPSELL_SUPPRESSED ->
                            sendMessageAsync(EVENT_ACTION_UPSELL_SUPPRESSED, subId);
                        default ->
                            plogd("Unknown notification action: " + intentAction);
                    }
                }
            };

    /** Registers the broadcast receiver to listen for notification-related intent actions */
    private void registerNotificationInteractionReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_UPSELL_CLICKED);
        filter.addAction(ACTION_UPSELL_DISMISSED);
        filter.addAction(ACTION_UPSELL_SUPPRESSED);
        mContext.registerReceiver(mNotificationInteractionBroadcastReceiver, filter,
                Context.RECEIVER_EXPORTED);
        logd("registerNotificationInteractionReceiver registered.");
    }

    /* Loads and caches the upsell-related carrier configuration values for a specific sub id */
    private void loadCarrierConfigsForSubId(int subId) {
        if (mCarrierConfigManager == null) {
            loge("loadCarrierConfigsForSubId: mCarrierConfigManager is null return");
            return;
        }
        try {
            PersistableBundle config = mCarrierConfigManager.getConfigForSubId(subId,
                    KEY_CARRIER_ROAMING_SATELLITE_UPSELL_SUPPORTED_BOOL,
                    KEY_CARRIER_ROAMING_SATELLITE_UPSELL_NOTIFICATION_HYSTERESIS_SEC_INT,
                    KEY_CARRIER_ROAMING_SATELLITE_UPSELL_NOTIFICATION_THROTTLE_HOURS_INT,
                    KEY_CARRIER_ROAMING_SATELLITE_UPSELL_NOTIFICATION_MAXIMUM_DAILY_COUNT_INT,
                    KEY_CARRIER_ROAMING_SATELLITE_UPSELL_NOTIFICATION_MAXIMUM_MONTHLY_COUNT_INT,
                    KEY_SATELLITE_CONFIGS_PER_PLMN_BUNDLE);

            if (config == null || config.equals(PersistableBundle.EMPTY)) {
                config = CarrierConfigManager.getDefaultConfig();
            }
            mCarrierConfigMap.put(subId, config);
            logd("loadCarrierConfigsForSubId: Loaded Upsell CarrierConfigs for subId " + subId);
        } catch (Exception ex) {
            plogd("loadCarrierConfigsForSubId: " + ex);
        }
    }

    /** Processes carrier configuration updates for a given subscription */
    private void handleCarrierConfigChanged(int slotIndex, int subId, int carrierId,
            int specificCarrierId) {
        logd("handleCarrierConfigChanged(): slotIndex(" + slotIndex + "), subId("
                + subId + "), carrierId(" + carrierId + "), specificCarrierId("
                + specificCarrierId + ")");
        if (subId == INVALID_SUBSCRIPTION_ID) {
            return;
        }

        loadCarrierConfigsForSubId(subId);
    }

    private boolean isUpsellEligible(int subId) {
        logd("isUpsellEligible: subId=" + subId);
        // TODO: Implement CarrierConfig and Eligibility checks in subsequent CLs.
        return false;
    }

    /**
     * Updates the visibility of the upsell notification for a specific subscription ID.
     *
     * @param subId   The subscription ID for which to update notification visibility.
     * @param visible {@code true} to display the notification, {@code false} to cancel it.
     */
    public void updateNotificationVisibility(int subId, boolean visible) {
        logd("updateNotificationVisibility: " + visible);
        if (visible) {
            if (!isUpsellEligible(subId)) {
                logd("Not allowed for subId: " + subId);
                return;
            }
            displayUpsellNotification(subId);
        } else {
            cancelUpsellNotification(subId);
        }
    }

    private void createNotificationChannel() {
        logd("createNotificationChannel");

        if (mNotificationManager == null) {
            logd("mNotificationManager is null");
            return;
        }
        final NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT);
        mNotificationManager.createNotificationChannel(channel);
        channel.setSound(null, null);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        mNotificationManager.createNotificationChannel(channel);
        logd("createNotificationChannel: success");
    }

    private void displayUpsellNotification(int subId) {
        logd("displayUpsellNotification: " + subId);
        createNotificationChannel();
        Notification.Builder builder = new Notification.Builder(mContext, NOTIFICATION_CHANNEL_ID)
                // TODO (b/473657577): Update with final strings once available.
                .setContentTitle(mContext.getString(R.string.satellite_upsell_notification_title))
                // TODO (b/473657577): Update with final strings once available.
                .setContentText(mContext.getString(R.string.satellite_upsell_notification_summary))
                .setSmallIcon(R.drawable.ic_android_satellite_24px)
                .setColor(mContext.getColor(
                        com.android.internal.R.color.system_notification_accent_color))
                .setVisibility(Notification.VISIBILITY_PUBLIC);

        // TODO : Action 1: 'Learn More'
        // TODO : Action 2: 'Don't show again'
        // TODO : Action 3: Dismissing/swiping out

        mNotificationManager.notifyAsUser(
                NOTIFICATION_TAG, NOTIFICATION_ID, builder.build(), UserHandle.ALL);
        mIsNotificationShowing.set(true);
    }

    private void cancelUpsellNotification(int subId) {
        logd("cancelUpsellNotification: " + subId);
        if (mNotificationManager != null) {
            mNotificationManager.cancelAsUser(NOTIFICATION_TAG, NOTIFICATION_ID, UserHandle.ALL);
        }
        mIsNotificationShowing.set(false);
    }

    private static void logd(@NonNull String log) {
        Log.d(TAG, log);
    }

    private static void loge(@NonNull String log) {
        Log.e(TAG, log);
    }

    private void plogd(@NonNull String log) {
        Log.d(TAG, log);
        if (mPersistentLogger != null) {
            mPersistentLogger.debug(TAG, log);
        }
    }
}
