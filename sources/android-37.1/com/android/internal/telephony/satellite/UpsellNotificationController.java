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
import static android.telephony.ServiceState.STATE_IN_SERVICE;
import static android.telephony.SubscriptionManager.INVALID_SUBSCRIPTION_ID;
import static android.telephony.TelephonyManager.EXTRA_SUBSCRIPTION_ID;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.Looper;
import android.os.Message;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.telephony.CarrierConfigManager;
import android.telephony.NetworkScan;
import android.telephony.NetworkScanRequest;
import android.telephony.PersistentLogger;
import android.telephony.ServiceState;
import android.telephony.SubscriptionManager;
import android.telephony.SubscriptionPlan;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class UpsellNotificationController extends Handler {
    private static final String TAG = "UpsellNotificationController";
    private static UpsellNotificationController sInstance;

    public static final String KEY_SATELLITE_PURCHASE_TEST_APP_PACKAGE =
            "persist.telephony.satellite.purchasetestapp";
    public static final String EXTRA_SUB_ID = "sub_id";

    private final Context mContext;
    private final Executor mExecutor;
    private final TelephonyManager mTelephonyManager;
    private final CarrierConfigManager mCarrierConfigManager;
    private final SubscriptionManager mSubscriptionManager;
    private final NotificationManager mNotificationManager;
    private final SatelliteController mSatelliteController;
    private final ConnectivityManager mConnectivityManager;
    private final FeatureFlags mFeatureFlags;
    private final AtomicInteger mActiveDataSubId = new AtomicInteger(INVALID_SUBSCRIPTION_ID);
    private final AtomicBoolean mIsSatelliteUpsellNotificationShowing = new AtomicBoolean(false);
    private final AtomicBoolean mIsWifiConnected = new AtomicBoolean(false);
    private ConnectivityManager.NetworkCallback mWifiNetworkCallback;

    /** Event definitions */
    private static final int EVENT_SERVICE_STATE_CHANGED = 1;
    private static final int EVENT_ACTIVE_DATA_SUB_CHANGED = 2;
    private static final int EVENT_CARRIER_CONFIG_CHANGED = 3;
    private static final int EVENT_ACTION_UPSELL_CLICKED = 4;
    private static final int EVENT_ACTION_UPSELL_DISMISSED = 5;
    private static final int EVENT_ACTION_UPSELL_SUPPRESSED = 6;
    private static final int EVENT_SATELLITE_UPSELL_OOS_HYSTERESIS_TIMER_EXPIRED = 7;
    private static final int EVENT_SCAN_RETRY_TIMER_EXPIRED = 8;
    private static final int EVENT_SATELLITE_UPSELL_TN_RESTORED_STABILITY_TIMER_EXPIRED = 9;
    private static final int EVENT_SATELLITE_UPSELL_NOTIFICATION_INIT = 10;
    private static final int EVENT_WIFI_CONNECTIVITY_CHANGED = 11;

    /** Notification constants */
    private static final int NOTIFICATION_ID = SatelliteController.NOTIFICATION_ID;
    private static final String NOTIFICATION_TAG = SatelliteController.NOTIFICATION_TAG;
    private static final String NOTIFICATION_CHANNEL = SatelliteController.NOTIFICATION_CHANNEL;
    private static final String NOTIFICATION_CHANNEL_ID =
            SatelliteController.NOTIFICATION_CHANNEL_ID;

    private static final int DEFAULT_UPSELL_NOTIFICATION_THROTTLE_HOURS = 24;
    private static final int DEFAULT_UPSELL_NOTIFICATION_MAX_DAILY_COUNT = 5;
    private static final int DEFAULT_UPSELL_NOTIFICATION_MAX_MONTHLY_COUNT = 20;

    @Nullable
    private PersistentLogger mPersistentLogger;

    private static final String PREF_KEY_SATELLITE_UPSELL_NOTIFICATION_SUPPRESSED_BY_USER_PREFIX =
            "satellite_upsell_notification_suppressed_by_user_";

    private enum SatelliteUpsellNotificationEligibility {
        ELIGIBLE,
        INELIGIBLE_AIRPLANE_MODE,
        INELIGIBLE_CARRIER_NOT_SUPPORTED,
        INELIGIBLE_ALREADY_HAS_PLAN,
        INELIGIBLE_NO_ENROLLABLE_PLANS,
        INELIGIBLE_CELLULAR_IN_SERVICE,
        INELIGIBLE_WIFI_ON,
        INELIGIBLE_USER_SUPPRESSED,
        INELIGIBLE_THROTTLED,
        INELIGIBLE_INVALID_SUB_ID
    }

    private final java.util.concurrent.atomic
            .AtomicReference<SatelliteUpsellNotificationEligibility> mCurrentEligibilityStatus =
            new java.util.concurrent.atomic.AtomicReference<>(
                    SatelliteUpsellNotificationEligibility.INELIGIBLE_INVALID_SUB_ID);

    /** Intent Actions */
    private static final String ACTION_UPSELL_CLICKED =
            "com.android.internal.telephony.satellite.ACTION_UPSELL_CLICKED";
    private static final String ACTION_UPSELL_DISMISSED =
            "com.android.internal.telephony.satellite.ACTION_UPSELL_DISMISSED";
    private static final String ACTION_UPSELL_SUPPRESSED =
            "com.android.internal.telephony.satellite.ACTION_UPSELL_SUPPRESSED";

    /** Map for holding carrier configs per subscription, Key: subId, Value: PersistableBundle. */
    private final Map<Integer, PersistableBundle> mCarrierConfigMap = new ConcurrentHashMap<>();

    private final Map<Integer, NotificationHistory> mNotificationHistoryMap =
            new ConcurrentHashMap<>();

    @VisibleForTesting
    public UpsellNotificationController(Context context, Looper looper, FeatureFlags featureFlags) {
        super(looper);
        mContext = context;
        mExecutor = new HandlerExecutor(this);
        mTelephonyManager = context.getSystemService(TelephonyManager.class);
        mCarrierConfigManager = context.getSystemService(CarrierConfigManager.class);
        mSubscriptionManager = context.getSystemService(SubscriptionManager.class);
        mNotificationManager = context.getSystemService(NotificationManager.class);
        mConnectivityManager = context.getSystemService(ConnectivityManager.class);
        mSatelliteController = SatelliteController.getInstance();
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
            sInstance.sendMessage(
                    sInstance.obtainMessage(EVENT_SATELLITE_UPSELL_NOTIFICATION_INIT));
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
            case EVENT_SATELLITE_UPSELL_NOTIFICATION_INIT -> {
                logd("EVENT_SATELLITE_UPSELL_NOTIFICATION_INIT");
                init();
            }

            case EVENT_SERVICE_STATE_CHANGED -> {
                logd("EVENT_SERVICE_STATE_CHANGED");
                evaluateSatelliteUpsellNotification();
            }

            case EVENT_ACTIVE_DATA_SUB_CHANGED -> {
                logd("EVENT_ACTIVE_DATA_SUB_CHANGED");
                int subId = msg.arg1;
                mActiveDataSubId.set(subId);
                evaluateSatelliteUpsellNotification();
            }

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

            case EVENT_ACTION_UPSELL_CLICKED -> {
                int subId = (int) msg.obj;
                logd("EVENT_ACTION_UPSELL_CLICKED: subId=" + subId);
                handleUpsellClicked(subId);
            }
            case EVENT_ACTION_UPSELL_DISMISSED -> {
                int subId = (int) msg.obj;
                logd("EVENT_ACTION_UPSELL_DISMISSED: subId=" + subId);
                handleUpsellDismissed(subId);
            }
            case EVENT_ACTION_UPSELL_SUPPRESSED -> {
                int subId = (int) msg.obj;
                logd("EVENT_ACTION_UPSELL_SUPPRESSED: subId=" + subId);
                handleUpsellSuppressed(subId);
            }

            case EVENT_SATELLITE_UPSELL_OOS_HYSTERESIS_TIMER_EXPIRED -> {
                int subId = msg.arg1;
                logd("EVENT_SATELLITE_UPSELL_OOS_HYSTERESIS_TIMER_EXPIRED: subId=" + subId);
                handleEventSatelliteUpsellOosHysteresisTimerExpired(subId);
            }

            case EVENT_SATELLITE_UPSELL_TN_RESTORED_STABILITY_TIMER_EXPIRED -> {
                int subId = msg.arg1;
                logd("EVENT_SATELLITE_UPSELL_TN_RESTORED_STABILITY_TIMER_EXPIRED: subId=" + subId);
                handleEventSatelliteUpsellTnRestoredStabilityTimerExpired(subId);
            }

            case EVENT_WIFI_CONNECTIVITY_CHANGED -> {
                logd("EVENT_WIFI_CONNECTIVITY_CHANGED: connected=" + mIsWifiConnected.get());
                evaluateSatelliteUpsellNotification();
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
        registerWifiConnectivityCallback();
    }

    /** Registers a network callback to monitor Wi-Fi connectivity changes in real-time */
    private void registerWifiConnectivityCallback() {
        if (mConnectivityManager == null) {
            loge("registerWifiConnectivityCallback: ConnectivityManager is null");
            return;
        }

        android.net.NetworkRequest request = new android.net.NetworkRequest.Builder()
                .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addTransportType(android.net.NetworkCapabilities.TRANSPORT_WIFI)
                .build();

        mWifiNetworkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull android.net.Network network) {
                mIsWifiConnected.set(true);
                sendMessage(obtainMessage(EVENT_WIFI_CONNECTIVITY_CHANGED));
            }

            @Override
            public void onLost(@NonNull android.net.Network network) {
                mIsWifiConnected.set(false);
                sendMessage(obtainMessage(EVENT_WIFI_CONNECTIVITY_CHANGED));
            }
        };

        mConnectivityManager.registerNetworkCallback(request, mWifiNetworkCallback, this);
        logd("registerWifiConnectivityCallback: registered.");
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

    /** Registers a listener to detect and handle changes in carrier configuration for the subId */
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

    /**  A broadcast receiver that handles user interactions with the upsell notification */
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

    /** Loads and caches the upsell-related carrier configuration values for a specific sub id */
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
        sendMessage(obtainMessage(EVENT_SERVICE_STATE_CHANGED));
    }

    /** Handles processing when the satellite upsell notification eligibility is ELIGIBLE. */
    private void startOosHysteresisTimer(int subId) {
        logd("startOosHysteresisTimer: subId=" + subId);
        // Stop the stability timer since the device is OOS or in limited service again.
        removeMessages(EVENT_SATELLITE_UPSELL_TN_RESTORED_STABILITY_TIMER_EXPIRED);

        // Do nothing if the notification is already showing
        if (mIsSatelliteUpsellNotificationShowing.get()) {
            logd("startOosHysteresisTimer: Satellite upsell notification is already showing");
            return;
        }
        //  Do nothing if the hysteresis timer is already running.
        if (hasMessages(EVENT_SATELLITE_UPSELL_OOS_HYSTERESIS_TIMER_EXPIRED)) {
            logd("startOosHysteresisTimer: "
                    + "Satellite upsell notification timer is already triggered");
            return;
        }

        // Start the hysteresis timer before showing the notification.
        long hysteresisMillis = getSatelliteUpsellNotificationHysteresisDurationMillis(subId);
        plogd("startOosHysteresisTimer: : " + hysteresisMillis + "ms for subId=" + subId);
        sendMessageDelayed(
                obtainMessage(EVENT_SATELLITE_UPSELL_OOS_HYSTERESIS_TIMER_EXPIRED, subId, 0),
                hysteresisMillis);
    }

    /** Handles processing when the satellite upsell notification eligibility is INELIGIBLE. */
    private void startInServiceStabilityTimer(int subId) {
        logd("startInServiceStabilityTimer: subId=" + subId);
        // Cancel the OOS hysteresis timer as the terrestrial network is in service
        removeMessages(EVENT_SATELLITE_UPSELL_OOS_HYSTERESIS_TIMER_EXPIRED);

        // No further action is required if the notification is not currently showing.
        if (!mIsSatelliteUpsellNotificationShowing.get()) {
            logd("startInServiceStabilityTimer: Satellite upsell notification is not showing");
            return;
        }

        // Start the TN stability timer to ensure TN connection is stable
        if (hasMessages(EVENT_SATELLITE_UPSELL_TN_RESTORED_STABILITY_TIMER_EXPIRED)) {
            logd("startInServiceStabilityTimer: TN restored stability timer is already running");
            return;
        }
        long tnStabilityMillis = getTnStabilityDurationMillis();
        plogd("startInServiceStabilityTimer: TN restored. Starting stability timer "
                + tnStabilityMillis + "ms for subId=" + subId);
        sendMessageDelayed(
                obtainMessage(EVENT_SATELLITE_UPSELL_TN_RESTORED_STABILITY_TIMER_EXPIRED,
                        subId, 0), tnStabilityMillis);
    }

    /**
     * Returns the duration (in milliseconds) for which the terrestrial network must remain stable
     * before the upsell notification is automatically dismissed.
     * @return The stability duration in milliseconds. Default is 1800 (30 minutes).
     */
    private long getTnStabilityDurationMillis() {
        int tnStabilitySec = 1800; // Default value: 30 minutes
        try {
            tnStabilitySec = mContext.getResources().getInteger(
                    com.android.internal.R.integer
                            .config_upsell_notification_dismiss_delay_after_tn_stable_sec);
        } catch (Exception e) {
            // Maintain default value if resource is not found or an error occurs
            plogd("getTnStabilityDurationMillis: Resource not found or error, default 1800s");
        }
        return tnStabilitySec * 1000L;
    }

    /** Check if the cellular service is available */
    private boolean isCellularInService() {
        logd("isCellularInService");
        for (Phone phone : PhoneFactory.getPhones()) {
            ServiceState ss = phone.getServiceState();
            if (ss != null && (ss.getState() == STATE_IN_SERVICE
                    || ss.getDataRegistrationState() == STATE_IN_SERVICE)) {
                logd("isCellularInService: slot " + phone.getPhoneId() + " is in service");
                return true;
            }
        }
        logd("isCellularInService: return false");
        return false;
    }

    /** cases for the satellite upsell notification should be removed immediately */
    private SatelliteUpsellNotificationEligibility getUpsellEligibilityStatus(int subId) {
        if (subId == INVALID_SUBSCRIPTION_ID) {
            logd("getUpsellEligibilityStatus: INELIGIBLE_INVALID_SUB_ID");
            return SatelliteUpsellNotificationEligibility.INELIGIBLE_INVALID_SUB_ID;
        }
        if (!isSatelliteUpsellSupportedByCarrier(subId)) {
            logd("getUpsellEligibilityStatus: INELIGIBLE_CARRIER_NOT_SUPPORTED");
            return SatelliteUpsellNotificationEligibility.INELIGIBLE_CARRIER_NOT_SUPPORTED;
        }
        if (isAirplaneModeOn()) {
            logd("getUpsellEligibilityStatus: INELIGIBLE_AIRPLANE_MODE");
            return SatelliteUpsellNotificationEligibility.INELIGIBLE_AIRPLANE_MODE;
        }
        if (mIsWifiConnected.get()) {
            logd("getUpsellEligibilityStatus: INELIGIBLE_WIFI_ON");
            return SatelliteUpsellNotificationEligibility.INELIGIBLE_WIFI_ON;
        }
        if (hasActiveSatellitePlan(subId)) {
            logd("getUpsellEligibilityStatus: INELIGIBLE_ALREADY_HAS_PLAN");
            return SatelliteUpsellNotificationEligibility.INELIGIBLE_ALREADY_HAS_PLAN;
        }
        if (!hasEnrollableSatellitePlan(subId)) {
            logd("getUpsellEligibilityStatus: INELIGIBLE_NO_ENROLLABLE_PLANS");
            return SatelliteUpsellNotificationEligibility.INELIGIBLE_NO_ENROLLABLE_PLANS;
        }
        // Checks if the user has permanently suppressed satellite upsell notifications by
        // selecting the 'Don't show again' option at previous satellite upsell notification.
        if (isSatelliteUpsellNotificationSuppressedByUser(subId)) {
            logd("getUpsellEligibilityStatus: INELIGIBLE_USER_SUPPRESSED");
            return SatelliteUpsellNotificationEligibility.INELIGIBLE_USER_SUPPRESSED;
        }
        // Checks if the notification should be throttled based on frequency caps
        // and cooldown periods (hours, daily, monthly) defined in the carrier configuration.
        if (isSatelliteUpsellNotificationThrottled(subId)) {
            logd("getUpsellEligibilityStatus: INELIGIBLE_THROTTLED");
            return SatelliteUpsellNotificationEligibility.INELIGIBLE_THROTTLED;
        }
        // Checks if Wi-Fi is connected.
        if (mIsWifiConnected.get()) {
            logd("getUpsellEligibilityStatus: INELIGIBLE_WIFI_ON.");
            return SatelliteUpsellNotificationEligibility.INELIGIBLE_WIFI_ON;
        }
        // Checks if the device is in cellular service.
        if (isCellularInService()) {
            logd("getUpsellEligibilityStatus: INELIGIBLE_CELLULAR_IN_SERVICE");
            return SatelliteUpsellNotificationEligibility.INELIGIBLE_CELLULAR_IN_SERVICE;
        }
        logd("getUpsellEligibilityStatus: ELIGIBLE");
        return SatelliteUpsellNotificationEligibility.ELIGIBLE;
    }

    /**
     * Determines if the upsell notification should be suppressed due to frequency caps
     * or cooldown periods defined in the carrier configuration.
     *
     * <p>Throttling duration is determined by
     * {@link KEY_CARRIER_ROAMING_SATELLITE_UPSELL_NOTIFICATION_THROTTLE_HOURS_INT},
     * and frequency is limited by
     * {@link KEY_CARRIER_ROAMING_SATELLITE_UPSELL_NOTIFICATION_MAXIMUM_DAILY_COUNT_INT}
     * and {@link KEY_CARRIER_ROAMING_SATELLITE_UPSELL_NOTIFICATION_MAXIMUM_MONTHLY_COUNT_INT}.
     */
    @VisibleForTesting
    protected long getElapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }

    private void recordNotificationSent(int subId) {
        NotificationHistory history = mNotificationHistoryMap.computeIfAbsent(
                subId, k -> new NotificationHistory());
        synchronized (history) {
            history.notificationTimestamps.add(getElapsedRealtime());
        }
    }

    private void recordUserInteraction(int subId) {
        NotificationHistory history = mNotificationHistoryMap.computeIfAbsent(
                subId, k -> new NotificationHistory());
        synchronized (history) {
            history.lastUserInteractionTimeMillis = getElapsedRealtime();
        }
    }

    private int getUpsellNotificationThrottleHours(int subId) {
        PersistableBundle config = mCarrierConfigMap.get(subId);
        if (config == null) {
            return DEFAULT_UPSELL_NOTIFICATION_THROTTLE_HOURS;
        }
        return config.getInt(
                KEY_CARRIER_ROAMING_SATELLITE_UPSELL_NOTIFICATION_THROTTLE_HOURS_INT,
                DEFAULT_UPSELL_NOTIFICATION_THROTTLE_HOURS);
    }

    private int getUpsellNotificationMaxDailyCount(int subId) {
        PersistableBundle config = mCarrierConfigMap.get(subId);
        if (config == null) {
            return DEFAULT_UPSELL_NOTIFICATION_MAX_DAILY_COUNT;
        }
        return config.getInt(
                KEY_CARRIER_ROAMING_SATELLITE_UPSELL_NOTIFICATION_MAXIMUM_DAILY_COUNT_INT,
                DEFAULT_UPSELL_NOTIFICATION_MAX_DAILY_COUNT);
    }

    private int getUpsellNotificationMaxMonthlyCount(int subId) {
        PersistableBundle config = mCarrierConfigMap.get(subId);
        if (config == null) {
            return DEFAULT_UPSELL_NOTIFICATION_MAX_MONTHLY_COUNT;
        }
        return config.getInt(
                KEY_CARRIER_ROAMING_SATELLITE_UPSELL_NOTIFICATION_MAXIMUM_MONTHLY_COUNT_INT,
                DEFAULT_UPSELL_NOTIFICATION_MAX_MONTHLY_COUNT);
    }

    private boolean isSatelliteUpsellNotificationThrottled(int subId) {
        logd("isSatelliteUpsellNotificationThrottled, subId=" + subId);
        NotificationHistory history = mNotificationHistoryMap.get(subId);
        if (history == null) {
            return false;
        }

        long currentTime = getElapsedRealtime();
        long cooldownHours = getUpsellNotificationThrottleHours(subId);
        long cooldownMillis = cooldownHours * 3600 * 1000L;

        long maxDailyCount = getUpsellNotificationMaxDailyCount(subId);
        long maxMonthlyCount = getUpsellNotificationMaxMonthlyCount(subId);

        synchronized (history) {
            // Clean up old timestamps
            long monthlyCutoff = currentTime - TimeUnit.DAYS.toMillis(30);
            history.notificationTimestamps.removeIf(timestamp -> timestamp < monthlyCutoff);

            // Check cooldown
            if ((history.lastUserInteractionTimeMillis > 0)
                    && (currentTime - history.lastUserInteractionTimeMillis < cooldownMillis)) {
                logd("Throttled: Cooldown active. Last interaction: "
                        + history.lastUserInteractionTimeMillis);
                return true;
            }

            // Check daily limit
            long dailyCutoff = currentTime - TimeUnit.DAYS.toMillis(1);
            long dailyCount = history.notificationTimestamps.stream()
                    .filter(timestamp -> timestamp >= dailyCutoff)
                    .count();
            if (dailyCount >= maxDailyCount) {
                logd("Throttled: Daily limit reached. Count: " + dailyCount
                        + " (max: " + maxDailyCount + ")");
                return true;
            }

            // Check monthly limit
            // After cleanup, all are within monthly window
            long monthlyCount = history.notificationTimestamps.size();
            if (monthlyCount >= maxMonthlyCount) {
                logd("Throttled: Monthly limit reached. Count: " + monthlyCount
                        + " (max: " + maxMonthlyCount + ")");
                return true;
            }
        }

        return false;
    }

    /**
     * Persistently saves the user's preference to restrict satellite upsell notifications for
     * a given subscription ID in SharedPreferences.
     */
    private void setSatelliteUpsellNotificationSuppressedByUser(int subId, boolean suppressed) {
        try {
            if (mContext == null) {
                loge("setSatelliteUpsellNotificationSuppressedByUser: Context is null");
                return;
            }
            mContext.getSharedPreferences(SatelliteController.SATELLITE_SHARED_PREF,
                            Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean(PREF_KEY_SATELLITE_UPSELL_NOTIFICATION_SUPPRESSED_BY_USER_PREFIX
                            + subId, suppressed)
                    .apply();
            plogd("setSatelliteUpsellNotificationSuppressedByUser: Successfully saved. subId="
                    + subId + ", suppressed=" + suppressed);
        } catch (Exception e) {
            loge("setSatelliteUpsellNotificationSuppressedByUser: Failed to save for subId="
                    + subId + ". Exception: " + e);
        }
    }

    /**
     * Checks if the user has permanently suppressed satellite upsell notifications
     * by selecting the 'Don't show again' option.
     */
    private boolean isSatelliteUpsellNotificationSuppressedByUser(int subId) {
        return mContext.getSharedPreferences(SatelliteController.SATELLITE_SHARED_PREF,
                        Context.MODE_PRIVATE)
                .getBoolean(PREF_KEY_SATELLITE_UPSELL_NOTIFICATION_SUPPRESSED_BY_USER_PREFIX
                        + subId, false);
    }

    /** Checks if there are any satellite plans available for the user to enroll in or purchase */
    private boolean hasEnrollableSatellitePlan(int subId) {
        List<SubscriptionPlan> plans = mSubscriptionManager.getEnrollableSubscriptionPlans(subId);
        for (SubscriptionPlan plan : plans) {
            if (plan.getTypes().contains(SubscriptionPlan.PLAN_TYPE_SATELLITE)) {
                logd("hasEnrollableSatellitePlan: found enrollable satellite plan");
                return true;
            }
        }

        logd("hasEnrollableSatellitePlan: not found enrollable satellite plan");
        return false;
    }


    /** Checks if the subscription already has a satellite plan that is currently active */
    private boolean hasActiveSatellitePlan(int subId) {
        List<SubscriptionPlan> plans = mSubscriptionManager.getSubscriptionPlans(subId);
        if (plans == null) {
            logd("hasActiveSatellitePlan: plans is null");
            return false;
        }
        for (SubscriptionPlan plan : plans) {
            boolean isSatPlan = plan.getTypes().contains(SubscriptionPlan.PLAN_TYPE_SATELLITE);
            boolean isSatPlanActive =
                    (plan.getSubscriptionStatus() == SubscriptionPlan.SUBSCRIPTION_STATUS_ACTIVE);
            logd("hasActiveSatellitePlan: isSatPlan=" + isSatPlan
                    + ", isSatPlanActive=" + isSatPlanActive);

            if (isSatPlan && isSatPlanActive) {
                logd("hasActiveSatellitePlan: Found an active satellite plan");
                return true;
            }
        }

        logd("hasActiveSatellitePlan: No active satellite plan found");
        return false;
    }

    /** Checks if the carrier supports satellite upsell for the given subscription. */
    private boolean isSatelliteUpsellSupportedByCarrier(int subId) {
        PersistableBundle config = mCarrierConfigMap.get(subId);
        if (config == null) {
            plogd("isSatelliteUpsellSupportedByCarrier: config is null for subId=" + subId);
            return false;
        }
        boolean isSupported = config.getBoolean(
                KEY_CARRIER_ROAMING_SATELLITE_UPSELL_SUPPORTED_BOOL, false);
        plogd("isSatelliteUpsellSupportedByCarrier: subId=" + subId
                + ", isSupported=" + isSupported);
        return isSupported;
    }

    /** Returns the hysteresis delay (default=15min.) for satellite upsell notifications */
    private long getSatelliteUpsellNotificationHysteresisDurationMillis(int subId) {
        int defaultDuration = 900; // 15 min.
        PersistableBundle config = mCarrierConfigMap.get(subId);
        if (config == null) {
            logd("getSatelliteUpsellNotificationHysteresisDurationMillis:"
                    + "config is null return default duration");
            return defaultDuration * 1000L;
        }
        int hysteresisSec = config.getInt(
                KEY_CARRIER_ROAMING_SATELLITE_UPSELL_NOTIFICATION_HYSTERESIS_SEC_INT,
                defaultDuration);
        logd("getSatelliteUpsellNotificationHysteresisDurationMillis: "
                + "hysteresisSec=" + hysteresisSec);
        return hysteresisSec * 1000L;
    }

    /** Checks whether the device is in Airplane Mode by verifying the radio power status. */
    private boolean isAirplaneModeOn() {
        boolean isApmOn = (mTelephonyManager != null && !mTelephonyManager.isRadioOn());
        logd("isAirplaneModeOn: isApmOn=" + isApmOn);
        return isApmOn;
    }

    private void createSatelliteUpsellNotificationChannel() {
        logd("createSatelliteUpsellNotificationChannel");
        if (mNotificationManager == null) {
            logd("mNotificationManager is null");
            return;
        }
        final NotificationChannel channel = new NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL,
                NotificationManager.IMPORTANCE_DEFAULT);
        channel.setSound(null, null);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        mNotificationManager.createNotificationChannel(channel);
        logd("createSatelliteUpsellNotificationChannel: success");
    }

    private void displaySatelliteUpsellNotification(int subId) {
        logd("displaySatelliteUpsellNotification: " + subId);
        createSatelliteUpsellNotificationChannel();

        String satellitePurchaseAppPackage = getSatellitePurchaseAppPackage();
        logd("displaySatelliteUpsellNotification:"
                + " satellitePurchaseAppPackage=" + satellitePurchaseAppPackage);

        Intent intent = null;
        if (!TextUtils.isEmpty(satellitePurchaseAppPackage)) {
            intent = mContext.getPackageManager()
                    .getLaunchIntentForPackage(satellitePurchaseAppPackage);
            if (intent != null) {
                intent.putExtra(EXTRA_SUB_ID, subId);
            } else {
                loge("displaySatelliteUpsellNotification: Failed to resolve launch intent for: "
                        + satellitePurchaseAppPackage);
            }
        }

        Notification.Builder builder = new Notification.Builder(mContext, NOTIFICATION_CHANNEL_ID)
                // TODO (b/473657577): Update with final strings once available.
                .setContentTitle(mContext.getString(R.string.satellite_upsell_notification_title))
                // TODO (b/473657577): Update with final strings once available.
                .setContentText(mContext.getString(R.string.satellite_upsell_notification_summary))
                .setSmallIcon(R.drawable.ic_android_satellite_24px)
                .setColor(mContext.getColor(
                        com.android.internal.R.color.system_notification_accent_color))
                .setVisibility(Notification.VISIBILITY_PUBLIC);
        if (intent != null) {
            PendingIntent contentIntent = getActivityPendingIntent(intent);
            builder.setContentIntent(contentIntent);
        }
        // TODO (b/473922125) :
        //  Action 1: 'Learn More', Action 2: 'Don't show again', Action 3: Dismissing/swiping out
        mNotificationManager.notifyAsUser(
                NOTIFICATION_TAG, NOTIFICATION_ID, builder.build(), UserHandle.ALL);
        mIsSatelliteUpsellNotificationShowing.set(true);
        recordNotificationSent(subId);
    }

    /**
     * Resolves the target application package name to be used for satellite upsell
     * notification click redirection.
     *
     * <p>Redirection package resolution is strictly conditional:
     * <ul>
     *   <li>In debuggable builds, it checks if the system property
     *       {@link #KEY_SATELLITE_PURCHASE_TEST_APP_PACKAGE} is set and non-empty. If so,
     *       it returns the property-defined package name.</li>
     *   <li>In non-debuggable builds or when the system property is unset, it returns {@code null}.
     * </ul>
     *
     * @return The package name to redirect notification clicks to, or {@code null} if fallback
     * default browser behavior should be used.
     */
    protected String getSatellitePurchaseAppPackage() {
        String packageName = getSystemProperty(KEY_SATELLITE_PURCHASE_TEST_APP_PACKAGE);
        if (isDebuggable() && !TextUtils.isEmpty(packageName)) {
            return packageName;
        }
        // TODO(b/473922125): Return correct package in production
        return null;
    }

    /**
     * Returns whether the current build is debuggable.
     */
    @VisibleForTesting
    protected boolean isDebuggable() {
        return Build.isDebuggable();
    }

    /**
     * Returns the value of the specified system property.
     */
    @VisibleForTesting
    protected String getSystemProperty(String key) {
        return SystemProperties.get(key);
    }

    protected PendingIntent getActivityPendingIntent(Intent intent) {
        return PendingIntent.getActivity(
                mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    /** Checks whether the specified subscription is eligible for a satellite upsell notification */
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    boolean isSubscriptionEligibleForSatelliteUpsellNotification(int subId) {
        logd("isSubscriptionEligibleForSatelliteUpsellNotification: subId=" + subId);
        if (subId == INVALID_SUBSCRIPTION_ID) {
            logd("isSubscriptionEligibleForSatelliteUpsellNotification: INVALID_SUBSCRIPTION_ID");
            return false;
        }

        // TODO (b/513401465) : Add check for purchase PLMN availability once support is in place.
        return isSatelliteUpsellSupportedByCarrier(subId)
                && !hasActiveSatellitePlan(subId)
                && hasEnrollableSatellitePlan(subId)
                && !isSatelliteUpsellNotificationSuppressedByUser(subId)
                && !isSatelliteUpsellNotificationThrottled(subId);
    }

    /** Returns the most suitable subscription ID for the satellite upsell notification. */
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    int getSatelliteUpsellNotificationSubId() {
        int defaultDataSubId = mActiveDataSubId.get();
        int candidateSubId = INVALID_SUBSCRIPTION_ID;

        // Check if any other active subscription is eligible.
        for (Phone phone : PhoneFactory.getPhones()) {
            int subId = phone.getSubId();
            if (subId == INVALID_SUBSCRIPTION_ID) {
                logd("getSatelliteUpsellNotificationSubId: continue, subId" + subId);
                continue;
            }
            if (isSubscriptionEligibleForSatelliteUpsellNotification(subId)) {
                if (subId == defaultDataSubId) {
                    logd("getSatelliteUpsellNotificationSubId: return defaultSubId=" + subId);
                    return subId;
                }
                candidateSubId = subId;
            }
        }

        logd("getSatelliteUpsellNotificationSubId: return candidateSubId=" + candidateSubId);
        return candidateSubId;
    }


    /**
     * Evaluates satellite upsell notification eligibility and
     * triggers an event on status transition
     */
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    void evaluateSatelliteUpsellNotification() {
        logd("evaluateSatelliteUpsellNotification");

        // Selects the subscription ID for the satellite upsell notification.
        int satelliteUpsellNotificationSubId = getSatelliteUpsellNotificationSubId();

        SatelliteUpsellNotificationEligibility eligibilityStatus =
                getUpsellEligibilityStatus(satelliteUpsellNotificationSubId);
        plogd("evaluateSatelliteUpsellNotification: eligibilityStatus=" + eligibilityStatus);
        switch(eligibilityStatus) {
            case INELIGIBLE_AIRPLANE_MODE:
                removeMessages(EVENT_SATELLITE_UPSELL_OOS_HYSTERESIS_TIMER_EXPIRED);
                removeMessages(EVENT_SATELLITE_UPSELL_TN_RESTORED_STABILITY_TIMER_EXPIRED);
                break;

            case INELIGIBLE_WIFI_ON:
            case INELIGIBLE_CELLULAR_IN_SERVICE:
                startInServiceStabilityTimer(satelliteUpsellNotificationSubId);
                break;

            case INELIGIBLE_INVALID_SUB_ID:
            case INELIGIBLE_CARRIER_NOT_SUPPORTED:
            case INELIGIBLE_ALREADY_HAS_PLAN:
            case INELIGIBLE_NO_ENROLLABLE_PLANS:
            case INELIGIBLE_USER_SUPPRESSED:
            case INELIGIBLE_THROTTLED:
                cancelSatelliteUpsellNotification(satelliteUpsellNotificationSubId);
                break;

            case ELIGIBLE:
                startOosHysteresisTimer(satelliteUpsellNotificationSubId);
                logd("evaluateSatelliteUpsellNotification: eligible");
                break;

            default :
                logd("evaluateSatelliteUpsellNotification: default do nothing");
                break;
        }
    }

    /**
     * Processes the expiration of the hysteresis timer by validating eligibility and
     * triggering either an immediate notification or a satellite network scan
     */
    private void handleEventSatelliteUpsellOosHysteresisTimerExpired(int subId) {
        SatelliteUpsellNotificationEligibility currentStatus = getUpsellEligibilityStatus(subId);
        logd("Current eligibility status after Oos hysteresis timer: " + currentStatus);
        if (currentStatus == SatelliteUpsellNotificationEligibility.ELIGIBLE) {
            logd("Current eligibility status is eligible");
            if (isNtnLimitedService(subId)) {
                logd("Device is in NTN limited service mode, showing notification immediately");
                displaySatelliteUpsellNotification(subId);
            } else {
                logd("Device is not in NTN limited service mode, triggering network scan");
                startSatelliteNetworkScan(subId);
            }
        }
    }

    private boolean isNtnLimitedService(int subId) {
        Phone phone = getPhoneForSubId(subId);
        ServiceState ss = (phone != null) ? phone.getServiceState() : null;
        if (ss == null) {
            plogd("isNtnLimitedService: ServiceState is null");
            return false;
        }
        if (!ss.isUsingNonTerrestrialNetwork()) {
            plogd("isNtnLimitedService: isUsingNonTerrestrialNetwork() is false");
            return false;
        }
        boolean isEmergency = ss.getState() == ServiceState.STATE_EMERGENCY_ONLY
                || ss.getDataRegistrationState() == ServiceState.STATE_EMERGENCY_ONLY;
        plogd("isNtnLimitedService: isEmergency=" + isEmergency);
        return isEmergency;
    }

    private Phone getPhoneForSubId(int subId) {
        for (Phone phone : PhoneFactory.getPhones()) {
            if (phone.getSubId() == subId) {
                return phone;
            }
        }
        return null;
    }

    @VisibleForTesting
    protected void startSatelliteNetworkScan(int subId) {
        logd("startSatelliteNetworkScan: subId=" + subId + " (stub)");
        logd("startSatelliteNetworkScan: Built scan request: "
                + SatelliteNetworkScanHelper.buildNetworkRequestForUpsellScan(
                        mContext, mSatelliteController, subId));
        // TODO: Request the active network scan using TelephonyManager and
        // handle the callback results in the next CL.
    }

    @VisibleForTesting
    protected void stopSatelliteNetworkScan() {
        logd("stopSatelliteNetworkScan (stub)");
    }

    /**
     * Processes the expiration of the stability timer by validating eligibility and
     * triggering either an immediate cancellation of the satellite upsell notification
     */
    private void handleEventSatelliteUpsellTnRestoredStabilityTimerExpired(int subId) {
        SatelliteUpsellNotificationEligibility currentStatus = getUpsellEligibilityStatus(subId);
        logd("Current eligibility status after TN stability timer: " + currentStatus);
        if ((currentStatus == SatelliteUpsellNotificationEligibility
                .INELIGIBLE_CELLULAR_IN_SERVICE)
                || (currentStatus == SatelliteUpsellNotificationEligibility
                .INELIGIBLE_WIFI_ON)) {
            cancelSatelliteUpsellNotification(subId);
        }
    }

    private void cancelSatelliteUpsellNotification(int subId) {
        logd("cancelSatelliteUpsellNotification: " + subId);
        if (mNotificationManager != null && mIsSatelliteUpsellNotificationShowing.get()) {
            mNotificationManager.cancelAsUser(NOTIFICATION_TAG, NOTIFICATION_ID, UserHandle.ALL);
        } else {
            logd("cancelSatelliteUpsellNotification: mIsSatelliteUpsellNotificationShowing="
                    + mIsSatelliteUpsellNotificationShowing.get());
        }
        mIsSatelliteUpsellNotificationShowing.set(false);
    }

    private void handleUpsellClicked(int subId) {
        recordUserInteraction(subId);
        cancelSatelliteUpsellNotification(subId);
    }

    private void handleUpsellDismissed(int subId) {
        recordUserInteraction(subId);
        mIsSatelliteUpsellNotificationShowing.set(false);
    }

    private void handleUpsellSuppressed(int subId) {
        setSatelliteUpsellNotificationSuppressedByUser(subId, true);
        cancelSatelliteUpsellNotification(subId);
    }

    private static class NotificationHistory {
        long lastUserInteractionTimeMillis = 0;
        final List<Long> notificationTimestamps = new ArrayList<>();
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
