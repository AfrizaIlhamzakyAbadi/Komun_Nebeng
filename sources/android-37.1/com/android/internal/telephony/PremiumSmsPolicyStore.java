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

package com.android.internal.telephony;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.UserHandle;
import android.util.AtomicFile;
import android.util.Xml;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.os.SomeArgs;
import com.android.internal.telephony.util.XmlUtils;
import com.android.internal.util.FastXmlSerializer;
import com.android.telephony.Rlog;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Centralized store for premium SMS package permissions.
 * This class handles the persistence of premium SMS policies and listens for package removals
 * to clean up obsolete entries.
 *
 * All operations are handled on a dedicated background thread to ensure serialization and to
 * avoid blocking the main thread or telephony process.
 *
 * @hide
 */
public class PremiumSmsPolicyStore extends Handler {
    private static final String TAG = "PremiumSmsPolicyStore";

    /** Message code: Initialize the store. */
    private static final int EVENT_INITIALIZE = 0;

    /** Message code: Set a package's premium SMS permission. */
    private static final int EVENT_SET_PREMIUM_SMS_PERMISSION = 1;

    /** Message code: Immediately remove a package from the memory cache. */
    private static final int EVENT_REMOVE_PREMIUM_SMS_PERMISSION = 2;

    /** Message code: Process pending disk write for package removals. */
    private static final int EVENT_WRITE_POLICY_DB = 3;

    /** Delay for batching package removal disk writes (Debouncing). */
    private static final int REMOVAL_DELAY_MS = 5000;

    /** Directory for per-app SMS permission XML file. */
    private static final String SMS_POLICY_FILE_DIRECTORY = "/data/misc/sms";

    /** Per-app SMS permission XML filename. */
    private static final String SMS_POLICY_FILE_NAME = "premium_sms_policy.xml";

    /** XML tag for root element of premium SMS permissions. */
    private static final String TAG_SMS_POLICY_BODY = "premium-sms-policy";

    /** XML tag for a package. */
    private static final String TAG_PACKAGE = "package";

    /** XML attribute for the package name. */
    private static final String ATTR_PACKAGE_NAME = "name";

    /** XML attribute for the package's premium SMS permission (integer type). */
    private static final String ATTR_PACKAGE_SMS_POLICY = "sms-policy";

    private static PremiumSmsPolicyStore sInstance;
    private final Context mContext;
    private final AtomicFile mPolicyFile;

    /**
     * Loaded copy of premium SMS package permissions.
     * Use a synchronized block when accessing this map to protect against concurrent reads
     * from Binder threads while the Handler thread is performing updates.
     */
    private final HashMap<String, Integer> mPremiumSmsPolicy = new HashMap<>();

    private final PackageIntentReceiver mPackageIntentReceiver;

    /**
     * Returns the singleton instance of PremiumSmsPolicyStore.
     *
     * @param context The context used for initialization if needed.
     * @return The singleton instance.
     * @hide
     */
    public static synchronized PremiumSmsPolicyStore getInstance(Context context) {
        if (sInstance == null) {
            Rlog.d(TAG, "getInstance: creating PremiumSmsPolicyStore instance");
            HandlerThread handlerThread = new HandlerThread(TAG);
            handlerThread.start();
            sInstance = new PremiumSmsPolicyStore(context.getApplicationContext(),
                    handlerThread.getLooper());
        }
        return sInstance;
    }

    /**
     * Constructor using the default policy file.
     *
     * @param context The context for registering receivers.
     * @param looper  The looper to run background tasks on.
     * @hide
     */
    @VisibleForTesting
    public PremiumSmsPolicyStore(Context context, Looper looper) {
        this(context, looper, new File(new File(SMS_POLICY_FILE_DIRECTORY), SMS_POLICY_FILE_NAME));
    }

    /**
     * Constructor allowing a custom file for testing.
     *
     * @param context The context for registering receivers.
     * @param looper  The looper to run background tasks on.
     * @param file    The file to use for the policy database.
     * @hide
     */
    @VisibleForTesting
    public PremiumSmsPolicyStore(Context context, Looper looper, File file) {
        super(looper);
        mContext = context;
        mPolicyFile = new AtomicFile(file);
        mPackageIntentReceiver = new PackageIntentReceiver();
        Rlog.d(TAG, "PremiumSmsPolicyStore: created. Scheduling initialization.");

        // Initialize the store asynchronously to avoid blocking the main thread
        // during phone process startup.
        sendMessage(obtainMessage(EVENT_INITIALIZE));
    }

    /**
     * Consolidates initialization tasks (receiver registration and DB loading)
     * to be performed on the background HandlerThread.
     */
    private void initialize() {
        Rlog.d(TAG, "initialize: starting background initialization");
        mPackageIntentReceiver.registerReceiver();
        loadPremiumSmsPolicyDb();
        Rlog.d(TAG, "initialize: background initialization complete");
    }

    /**
     * Handles background tasks such as loading and writing the policy database.
     *
     * @param msg The message containing the task to perform.
     */
    @Override
    public void handleMessage(Message msg) {
        switch (msg.what) {
            case EVENT_INITIALIZE:
                initialize();
                break;

            case EVENT_SET_PREMIUM_SMS_PERMISSION: {
                SomeArgs args = (SomeArgs) msg.obj;
                try {
                    String packageName = (String) args.arg1;
                    int permission = args.argi1;
                    Rlog.d(TAG,
                            "handleMessage:"
                                    + " event=EVENT_SET_PREMIUM_SMS_PERMISSION"
                                    + ", packageName=" + packageName
                                    + ", permission=" + permission);
                    handleSetPremiumSmsPermission(packageName, permission);
                } finally {
                    args.recycle();
                }
                break;
            }

            case EVENT_REMOVE_PREMIUM_SMS_PERMISSION: {
                String packageName = (String) msg.obj;
                Rlog.d(TAG,
                        "handleMessage:"
                                + " event=EVENT_REMOVE_PREMIUM_SMS_PERMISSION"
                                + ", packageName=" + packageName);
                handleRemoveFromMemory(packageName);
                break;
            }

            case EVENT_WRITE_POLICY_DB:
                Rlog.d(TAG, "handleMessage: event=EVENT_WRITE_POLICY_DB");
                writePremiumSmsPolicyDb();
                break;

            default:
                Rlog.w(TAG, "handleMessage: unknown event: event=" + msg.what);
        }
    }

    /**
     * Returns the premium SMS permission for the specified package.
     * Note: This returns the current cached value in memory.
     *
     * @param packageName The package name to query.
     * @return The current premium SMS policy (ALLOW/DENY/ASK/UNKNOWN).
     * @hide
     */
    public int getPremiumSmsPermission(String packageName) {
        synchronized (mPremiumSmsPolicy) {
            Integer policy = mPremiumSmsPolicy.get(packageName);
            if (policy == null) {
                Rlog.e(TAG,
                        "getPremiumSmsPermission: policy is null. returning permission=0"
                                + " for packageName=" + packageName);
                return SmsUsageMonitor.PREMIUM_SMS_PERMISSION_UNKNOWN;
            }
            Rlog.d(TAG,
                    "getPremiumSmsPermission: returning permission=" + policy
                            + " for packageName=" + packageName);
            return policy;
        }
    }

    /**
     * Asynchronously sets the premium SMS permission for the specified package.
     *
     * @param packageName The package name.
     * @param permission  The premium SMS policy to set.
     * @hide
     */
    public void setPremiumSmsPermission(String packageName, int permission) {
        Rlog.d(TAG, "setPremiumSmsPermission: packageName=" + packageName
                + ", permission=" + permission);
        SomeArgs args = SomeArgs.obtain();
        args.arg1 = packageName;
        args.argi1 = permission;
        sendMessage(obtainMessage(EVENT_SET_PREMIUM_SMS_PERMISSION, args));
    }

    /**
     * Asynchronously removes the premium SMS permission for the specified package.
     * Memory is updated immediately to prevent permission persistence, while disk
     * writes are debounced and processed with a delay for efficiency.
     *
     * @param packageName The package name to remove.
     * @hide
     */
    public void removePremiumSmsPermission(String packageName) {
        Rlog.d(TAG, "removePremiumSmsPermission: packageName=" + packageName);
        sendMessage(obtainMessage(EVENT_REMOVE_PREMIUM_SMS_PERMISSION, packageName));
    }

    /**
     * Load the premium SMS policy from an XML file into the memory cache.
     * This is performed on the background HandlerThread.
     */
    private void loadPremiumSmsPolicyDb() {
        long startTime = SystemClock.elapsedRealtime();
        synchronized (mPremiumSmsPolicy) {
            mPremiumSmsPolicy.clear();

            FileInputStream infile = null;
            try {
                infile = mPolicyFile.openRead();
                final XmlPullParser parser = Xml.newPullParser();
                parser.setInput(infile, StandardCharsets.UTF_8.name());

                XmlUtils.beginDocument(parser, TAG_SMS_POLICY_BODY);

                while (true) {
                    XmlUtils.nextElement(parser);

                    String element = parser.getName();
                    if (element == null) break;

                    if (element.equals(TAG_PACKAGE)) {
                        String packageName = parser.getAttributeValue(null, ATTR_PACKAGE_NAME);
                        String policy = parser.getAttributeValue(null, ATTR_PACKAGE_SMS_POLICY);
                        if (packageName == null) {
                            Rlog.e(TAG, "loadPremiumSmsPolicyDb: missing package name attribute");
                        } else if (policy == null) {
                            Rlog.e(TAG, "loadPremiumSmsPolicyDb: missing package policy attribute");
                        } else {
                            try {
                                mPremiumSmsPolicy.put(packageName, Integer.parseInt(policy));
                            } catch (NumberFormatException e) {
                                Rlog.e(TAG,
                                        "loadPremiumSmsPolicyDb: non-numeric policy type " + policy,
                                        e);
                            }
                        }
                    } else {
                        Rlog.e(TAG, "loadPremiumSmsPolicyDb: skipping unknown XML tag " + element);
                    }
                }
                Rlog.d(TAG, "loadPremiumSmsPolicyDb: Loaded " + mPremiumSmsPolicy.size()
                        + " entries in " + (SystemClock.elapsedRealtime() - startTime) + "ms.");
            } catch (FileNotFoundException e) {
                Rlog.d(TAG, "loadPremiumSmsPolicyDb: No premium SMS policy file found");
            } catch (IOException | XmlPullParserException | NumberFormatException e) {
                Rlog.e(TAG, "loadPremiumSmsPolicyDb: Unable to read premium SMS policy database",
                        e);
            } finally {
                if (infile != null) {
                    try {
                        infile.close();
                    } catch (IOException e) {
                        Rlog.e(TAG, "loadPremiumSmsPolicyDb: Error closing premium SMS policy file",
                                e);
                    }
                }
            }
        }
    }

    /**
     * Internal handler to update memory cache and initiate a disk write.
     */
    private void handleSetPremiumSmsPermission(String packageName, int permission) {
        synchronized (mPremiumSmsPolicy) {
            mPremiumSmsPolicy.put(packageName, permission);
        }
        Rlog.d(TAG, "handleSetPremiumSmsPermission: Set memory policy for"
                + " packageName=" + packageName
                + ", permission=" + permission);
        writePremiumSmsPolicyDb();
    }

    /**
     * Internal handler to update memory cache and schedule a debounced disk write.
     */
    private void handleRemoveFromMemory(String packageName) {
        synchronized (mPremiumSmsPolicy) {
            if (mPremiumSmsPolicy.remove(packageName) == null) {
                return;
            }
        }
        Rlog.d(TAG, "handleRemoveFromMemory: Removed memory policy for " + packageName
                + ". Scheduling disk write.");

        // Debounce the disk write: Reset the 5s timer for EVERY removal.
        // This ensures that bulk removals only trigger a single disk write.
        removeMessages(EVENT_WRITE_POLICY_DB);
        sendEmptyMessageDelayed(EVENT_WRITE_POLICY_DB, REMOVAL_DELAY_MS);
    }

    /**
     * Persist the current memory copy of policies to disk atomically.
     */
    private void writePremiumSmsPolicyDb() {
        long startTime = SystemClock.elapsedRealtime();
        synchronized (mPremiumSmsPolicy) {
            FileOutputStream outfile = null;
            try {
                outfile = mPolicyFile.startWrite();

                XmlSerializer out = new FastXmlSerializer();
                out.setOutput(outfile, StandardCharsets.UTF_8.name());

                out.startDocument(null, true);

                out.startTag(null, TAG_SMS_POLICY_BODY);

                for (Map.Entry<String, Integer> policy : mPremiumSmsPolicy.entrySet()) {
                    out.startTag(null, TAG_PACKAGE);
                    out.attribute(null, ATTR_PACKAGE_NAME, policy.getKey());
                    out.attribute(null, ATTR_PACKAGE_SMS_POLICY, policy.getValue().toString());
                    out.endTag(null, TAG_PACKAGE);
                }

                out.endTag(null, TAG_SMS_POLICY_BODY);
                out.endDocument();

                mPolicyFile.finishWrite(outfile);
                Rlog.d(TAG, "writePremiumSmsPolicyDb: Write premium SMS policy db success!"
                        + " policyMap=" + mPremiumSmsPolicy
                        + " (Time: " + (SystemClock.elapsedRealtime() - startTime) + "ms)");
            } catch (IOException e) {
                Rlog.e(TAG, "writePremiumSmsPolicyDb: Unable to write premium SMS policy database",
                        e);
                if (outfile != null) {
                    mPolicyFile.failWrite(outfile);
                }
            }
        }
    }

    /**
     * BroadcastReceiver for package uninstallation events.
     */
    private final class PackageIntentReceiver extends BroadcastReceiver {
        /**
         * Registers the receiver for package removal events for all users.
         */
        void registerReceiver() {
            IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_FULLY_REMOVED);
            filter.addDataScheme("package");
            // Register for all users since premium SMS policies are device-wide.
            mContext.registerReceiverAsUser(this, UserHandle.ALL, filter, null, null);
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) {
                Rlog.d(TAG, "onReceive: action is null");
                return;
            }
            Rlog.d(TAG, "onReceive: action=" + action);
            if (Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(action)) {
                Uri data = intent.getData();
                if (data == null) {
                    Rlog.e(TAG, "onReceive: intent data is null");
                    return;
                }
                String packageName = data.getSchemeSpecificPart();
                if (packageName == null) {
                    Rlog.e(TAG, "onReceive: package name is null");
                    return;
                }

                Rlog.d(TAG, "onReceive: Scheduling removal of premium SMS policy for: "
                        + "packageName=" + packageName);
                removePremiumSmsPermission(packageName);
            }
        }
    }
}
