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

package com.android.internal.app;

import static android.view.WindowManager.LayoutParams.SYSTEM_FLAG_HIDE_NON_SYSTEM_OVERLAY_WINDOWS;

import android.annotation.SuppressLint;
import android.app.contentsafety.ContentSafetyManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.LocusId;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.util.Slog;
import android.view.Window;
import android.view.WindowManager;

import com.android.internal.R;

/**
 * A dialog shown to the user when content safety apps block a request. Allows opening specific
 * block details provided by content safety apps.
 */
public class ContentSafetyDialogActivity extends AlertActivity
        implements DialogInterface.OnClickListener {

    private static final String TAG = "ContentSafetyDialogActivity";

    private String mBlockingPackage;
    private LocusId mLocusId;
    private int mUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent intent = getIntent();

        mUserId = getUserId();
        mLocusId =
                intent.getParcelableExtra(
                        ContentSafetyManager.EXTRA_CONTENT_LOCUS_ID, LocusId.class);
        mBlockingPackage = resolveSecureBlockingPackage(intent);

        mAlertParams.mIcon = getDrawable(R.drawable.ic_lock);
        mAlertParams.mPositiveButtonText = getString(R.string.close_button_text);
        mAlertParams.mPositiveButtonListener = this;

        if (TextUtils.isEmpty(mBlockingPackage)) {
            mAlertParams.mMessage = getString(R.string.content_safety_dialog_message_generic);
        } else {
            final CharSequence appLabel = getAppLabel(mUserId, mBlockingPackage);
            mAlertParams.mMessage = getString(R.string.content_safety_dialog_message, appLabel);
            mAlertParams.mNeutralButtonText = getString(R.string.app_suspended_more_details);
            mAlertParams.mNeutralButtonListener = this;
        }

        setupAlert();
    }

    @Override
    protected void onStart() {
        super.onStart();
        getWindow().addSystemFlags(SYSTEM_FLAG_HIDE_NON_SYSTEM_OVERLAY_WINDOWS);
    }

    @Override
    protected void onStop() {
        super.onStop();
        final Window window = getWindow();
        final WindowManager.LayoutParams attrs = window.getAttributes();
        attrs.privateFlags &= ~SYSTEM_FLAG_HIDE_NON_SYSTEM_OVERLAY_WINDOWS;
        window.setAttributes(attrs);
    }

    private String resolveSecureBlockingPackage(Intent intent) {
        final String callerPackage = intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME);
        if (TextUtils.isEmpty(callerPackage)) {
            return null;
        }

        final int launchedFromUid = getLaunchedFromUid();
        if (launchedFromUid != android.os.Process.SYSTEM_UID) {
            final String[] packagesForUid = getPackageManager().getPackagesForUid(launchedFromUid);
            boolean matchesCaller = false;
            if (packagesForUid != null) {
                for (String pkg : packagesForUid) {
                    if (TextUtils.equals(callerPackage, pkg)) {
                        matchesCaller = true;
                        break;
                    }
                }
            }
            if (!matchesCaller) {
                Slog.w(
                        TAG,
                        "Security violation: Launcher UID "
                                + launchedFromUid
                                + " does not own package "
                                + callerPackage);
                return null;
            }
        }

        final ContentSafetyManager csm = getSystemService(ContentSafetyManager.class);
        if (csm == null) {
            Slog.w(TAG, "ContentSafetyManager unavailable");
            return null;
        }
        return csm.getClassifyingPackage(mUserId, callerPackage, mLocusId);
    }

    private CharSequence getAppLabel(int userId, String packageName) {
        final PackageManager pm = getPackageManager();
        try {
            final ApplicationInfo aInfo =
                    pm.getApplicationInfoAsUser(
                            packageName, PackageManager.ApplicationInfoFlags.of(0), userId);
            return aInfo.loadLabel(pm);
        } catch (PackageManager.NameNotFoundException ne) {
            Slog.e(TAG, "Package " + packageName + " not found for label resolution", ne);
        }
        return packageName;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_NEUTRAL) {
            openCustomDetailsPage();
        }
        finish();
    }

    @SuppressLint("AndroidFrameworkRequiresPermission")
    private void openCustomDetailsPage() {
        final Intent detailsIntent =
                new Intent(ContentSafetyManager.ACTION_SHOW_RESTRICTED_CONTENT_DETAILS)
                        .setPackage(mBlockingPackage)
                        .putExtra(ContentSafetyManager.EXTRA_CONTENT_LOCUS_ID, mLocusId)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        final PackageManager pm = getPackageManager();
        final ResolveInfo ri =
                pm.resolveActivityAsUser(
                        detailsIntent, PackageManager.ResolveInfoFlags.of(0), mUserId);
        if (ri == null || ri.activityInfo == null || !ri.activityInfo.exported) {
            Slog.w(
                    TAG,
                    "Target custom details page activity is not exported or unavailable."
                            + " Abortion of launch.");
            return;
        }
        try {
            startActivityAsUser(detailsIntent, UserHandle.of(mUserId));
        } catch (android.content.ActivityNotFoundException e) {
            Slog.e(TAG, "Failed to launch content safety app custom block details page", e);
        }
    }

    /**
     * Creates an {@link Intent} to launch {@link ContentSafetyDialogActivity}.
     *
     * @hide
     */
    public static Intent createIntent(String callerPackage, LocusId locusId) {
        return new Intent()
                .setClassName("android", ContentSafetyDialogActivity.class.getName())
                .putExtra(ContentSafetyManager.EXTRA_CONTENT_LOCUS_ID, locusId)
                .putExtra(Intent.EXTRA_PACKAGE_NAME, callerPackage);
    }
}
