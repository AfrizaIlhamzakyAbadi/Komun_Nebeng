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

import android.annotation.Nullable;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.hardware.biometrics.BiometricEnrollmentStatus;
import android.hardware.biometrics.BiometricManager;
import android.os.Build;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Slog;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.internal.R;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.widget.LockPatternUtils;
import com.android.internal.widget.ResolverDrawerLayout;

import java.util.Map;

/**
 * An activity that educates the user about the App Lock feature and prompts for its enablement.
 *
 * <p>This activity is launched by {@link AppLockActivity} using
 * {@link android.app.Activity#startActivityForResult(android.content.Intent, int)} with the
 * request code {@code REQUEST_CODE_USER_EDUCATION_DIALOG}. It should only be started after
 * {@link AppLockActivity} has confirmed that a screen lock is set on the device.
 *
 * <p>Upon completion, this activity returns {@link android.app.Activity#RESULT_OK} if the user
 * chooses to lock the app. This result is then handled by
 * {@link AppLockActivity#onActivityResult(int, int, Intent)}, which proceeds to show the biometric
 * prompt to finalize the setup.
 */
public class AppLockUserEducationActivity extends Activity {
    private static final String TAG = "AppLockUserEducation";
    private static final boolean DEBUG = Build.IS_DEBUGGABLE && Log.isLoggable(TAG, Log.DEBUG);

    private final int mUserId = UserHandle.myUserId();

    @Nullable
    private static Injector sInjector;

    private final Injector mInjector = sInjector == null ? new Injector() : sInjector;

    private View mAppLockLayout;
    private ViewTreeObserver.OnGlobalLayoutListener mGlobalLayoutListener;
    private ScrollView mScrollView;

    /**
     * Creates an {@link Intent} to launch {@link AppLockUserEducationActivity} to show the user
     * education flow before enabling App Lock.
     */
    static Intent createIntent(Context context, String packageName, CharSequence packageLabel) {
        final Intent userEducationIntent = new Intent(context, AppLockUserEducationActivity.class);
        userEducationIntent.putExtra(Intent.EXTRA_PACKAGE_NAME, packageName);
        userEducationIntent.putExtra(Intent.EXTRA_TITLE, packageLabel);
        return userEducationIntent;
    }

    /**
     * Sets the {@link Injector} for testing purposes.
     *
     * <p>This method allows replacing the default injector with a mock implementation to facilitate
     * testing of this activity. This should only be used in debugg
     */
    @VisibleForTesting
    public static void setInjectorForTesting(@Nullable Injector injector) {
        if (!Build.IS_DEBUGGABLE) {
            throw new SecurityException("Injector should only be set in debuggable builds.");
        }
        sInjector = injector;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addSystemFlags(SYSTEM_FLAG_HIDE_NON_SYSTEM_OVERLAY_WINDOWS);

        final Intent intent = getIntent();
        final String packageName = intent.getStringExtra(Intent.EXTRA_PACKAGE_NAME);
        final CharSequence packageLabel = intent.getCharSequenceExtra(Intent.EXTRA_TITLE);

        // Required package data must be present to show the dialog.
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(packageLabel)) {
            Slog.w(TAG, "Missing package name or label, finishing.");
            finish();
            return;
        }
        setupUiAndShowDialog(packageLabel);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        final CharSequence packageLabel = getIntent().getCharSequenceExtra(Intent.EXTRA_TITLE);
        setupUiAndShowDialog(packageLabel);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Finish the activity if it is being stopped for reasons other than a config change (e.g.,
        // Home button).
        if (!isChangingConfigurations()) {
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        setResult(Activity.RESULT_CANCELED);

        // Use finishAffinity() to dismiss the entire AppLockActivity stack as a single unit.
        // This ensures a clean task-level exit transition and prevents the parent
        // activity from momentarily appearing (flickering) during dismissal.
        finishAffinity();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGlobalLayoutListener != null && mScrollView != null) {
            final ViewTreeObserver observer = mScrollView.getViewTreeObserver();
            if (observer.isAlive()) {
                observer.removeOnGlobalLayoutListener(mGlobalLayoutListener);
            }
            mGlobalLayoutListener = null;
        }
    }

    /** Sets up the UI and displays the App Lock education dialog. */
    @SuppressLint("AndroidFrameworkRequiresPermission")
    private void setupUiAndShowDialog(CharSequence packageLabel) {
        if (DEBUG) {
            Slog.d(TAG, "setupUiAndShowDialog called with label: " + packageLabel);
        }
        setContentView(R.layout.app_lock_edu_activity);
        mAppLockLayout = findViewById(R.id.app_lock_edu_dialog);
        final TextView titleView = mAppLockLayout.findViewById(R.id.app_lock_edu_dialog_title);
        titleView.setText(getString(R.string.app_lock_edu_dialog_enable_app_lock_title,
                packageLabel));

        // Show the divider only when content is scrollable.
        mScrollView = mAppLockLayout.findViewById(R.id.app_lock_edu_scroll_view);
        if (mScrollView != null) {
            mGlobalLayoutListener = () -> {
                final boolean canScroll = mScrollView.canScrollVertically(/* direction= */ 1);
                mAppLockLayout.findViewById(R.id.app_lock_edu_bottom_sheet_divider)
                        .setVisibility(canScroll ? View.VISIBLE : View.GONE);
            };
            mScrollView.getViewTreeObserver().addOnGlobalLayoutListener(mGlobalLayoutListener);
        } else {
            Slog.w(TAG, "mScrollView is null, cannot toggle divider visibility.");
        }

        // Customize description based on available authentication methods.
        final TextView descriptionView = mAppLockLayout.findViewById(R.id.app_lock_edu_dialog_desc);
        final int credentialType = mInjector.getLockPatternUtils(this).getCredentialTypeForUser(
                mUserId);
        if (credentialType == LockPatternUtils.CREDENTIAL_TYPE_NONE) {
            Slog.e(TAG, "Credential type is none, finishing.");
            finish();
            return;
        }

        // Query user's fingerprint and face enrollment status using BiometricManager.
        boolean hasFingerprint = false;
        boolean hasFace = false;
        try {
            final BiometricManager biometricManager = mInjector.getBiometricManager(this);
            if (biometricManager != null) {
                final Map<Integer, BiometricEnrollmentStatus> enrollmentStatus =
                        biometricManager.getEnrollmentStatus();

                final BiometricEnrollmentStatus fingerprintStatus = enrollmentStatus.get(
                        BiometricManager.TYPE_FINGERPRINT);
                hasFingerprint = fingerprintStatus != null
                        && fingerprintStatus.getStrength() == BiometricManager.Authenticators
                                .BIOMETRIC_STRONG
                        && fingerprintStatus.getEnrollmentCount() > 0;

                final BiometricEnrollmentStatus faceStatus = enrollmentStatus.get(
                        BiometricManager.TYPE_FACE);
                hasFace = faceStatus != null
                        && faceStatus.getStrength() == BiometricManager.Authenticators
                                .BIOMETRIC_STRONG
                        && faceStatus.getEnrollmentCount() > 0;
            }
        } catch (SecurityException e) {
            Slog.e(TAG, "Exception while checking enrollment status", e);
        }

        final int descriptionResId = getDescriptionResId(credentialType, hasFingerprint, hasFace);
        if (descriptionResId == Resources.ID_NULL) {
            finish();
            return;
        }
        descriptionView.setText(getString(descriptionResId));

        final TextView aiDisclaimerView = mAppLockLayout.findViewById(
                R.id.app_lock_edu_dialog_info_ai_text);
        if (aiDisclaimerView != null) {
            aiDisclaimerView.setText(getString(R.string.app_lock_edu_dialog_info_ai_disclaimer,
                    packageLabel));
        }

        final Button lockButton = mAppLockLayout.findViewById(
                R.id.app_lock_edu_dialog_btn_lock_app);
        lockButton.setOnClickListener(v -> {
            setResult(Activity.RESULT_OK);
            finish();
        });

        final Button cancelButton = mAppLockLayout.findViewById(
                R.id.app_lock_edu_dialog_btn_cancel);
        cancelButton.setOnClickListener(v -> {
            onBackPressed();
        });

        // Dismiss App Lock flow when the transparent background is tapped.
        final View rootLayout = findViewById(R.id.app_lock_edu_layout);
        if (rootLayout instanceof ResolverDrawerLayout) {
            ((ResolverDrawerLayout) rootLayout).setOnDismissListener(this::onBackPressed);
        } else {
            rootLayout.setOnClickListener(v -> onBackPressed());
        }
    }

    /** Returns the description resource ID based on the credential type and biometrics. */
    private int getDescriptionResId(int credentialType, boolean hasFingerprint, boolean hasFace) {
        switch (credentialType) {
            case LockPatternUtils.CREDENTIAL_TYPE_PATTERN -> {
                if (hasFingerprint && hasFace) {
                    return R.string.app_lock_edu_dialog_description_fp_face_pattern;
                } else if (hasFingerprint) {
                    return R.string.app_lock_edu_dialog_description_fp_pattern;
                } else if (hasFace) {
                    return R.string.app_lock_edu_dialog_description_face_pattern;
                } else {
                    return R.string.app_lock_edu_dialog_description_pattern;
                }
            }
            case LockPatternUtils.CREDENTIAL_TYPE_PIN -> {
                if (hasFingerprint && hasFace) {
                    return R.string.app_lock_edu_dialog_description_fp_face_pin;
                } else if (hasFingerprint) {
                    return R.string.app_lock_edu_dialog_description_fp_pin;
                } else if (hasFace) {
                    return R.string.app_lock_edu_dialog_description_face_pin;
                } else {
                    return R.string.app_lock_edu_dialog_description_pin;
                }
            }
            case LockPatternUtils.CREDENTIAL_TYPE_PASSWORD -> {
                if (hasFingerprint && hasFace) {
                    return R.string.app_lock_edu_dialog_description_fp_face_password;
                } else if (hasFingerprint) {
                    return R.string.app_lock_edu_dialog_description_fp_password;
                } else if (hasFace) {
                    return R.string.app_lock_edu_dialog_description_face_password;
                } else {
                    return R.string.app_lock_edu_dialog_description_password;
                }
            }
            default -> {
                Slog.w(TAG, "Unexpected credential type: " + credentialType);
                return Resources.ID_NULL;
            }
        }
    }

    /**
     * An injector class for dependency injection, allowing for easier testing.
     */
    public static class Injector {
        /** Returns a new {@link LockPatternUtils} for the given context. */
        public LockPatternUtils getLockPatternUtils(Context context) {
            return new LockPatternUtils(context);
        }

        /** Returns the {@link BiometricManager} for the given context. */
        public BiometricManager getBiometricManager(Context context) {
            return context.getSystemService(BiometricManager.class);
        }
    }
}
