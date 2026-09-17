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

package android.app.admin.policy;

import static android.Manifest.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_ASSIST_CONTENT;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_SCREEN_CAPTURE;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_UNIVERSAL_CLIPBOARD;
import static android.app.admin.DevicePolicyManager.POLICY_SCOPE_DEVICE;
import static android.app.admin.DevicePolicyManager.POLICY_SCOPE_USER;
import static android.app.admin.DevicePolicyManager.RESOURCE_PER_USER;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_ASSIST_CONTENT;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_SCREEN_CAPTURE_API;
import static android.companion.Flags.FLAG_UNIVERSAL_CLIPBOARD;
import static android.processor.devicepolicy.AllowedDpcTypes.ALLOWED;
import static android.processor.devicepolicy.AllowedDpcTypes.DISALLOWED;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.app.admin.DevicePolicyManager;
import android.app.admin.PolicyIdentifier;
import android.processor.devicepolicy.AllowedDpcTypes;
import android.processor.devicepolicy.AllowedRoles;
import android.processor.devicepolicy.EnumPolicyDefinition;
import android.processor.devicepolicy.EnumResolutionMechanism;
import android.processor.devicepolicy.PolicyDefinition;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/** Policies that allow the admin to prevent data leaks. */
@FlaggedApi(FLAG_POLICY_STREAMLINING_SCREEN_CAPTURE_API)
public class DataLeakPrevention {

    private DataLeakPrevention() {}

    /**
     * Screen capture is disallowed. See {@link android.view.Display#FLAG_SECURE} for more details
     * on how blocking works.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_SCREEN_CAPTURE_API)
    public static final int SCREEN_CAPTURE_DISALLOWED = 1;

    /** Screen capture is allowed. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_SCREEN_CAPTURE_API)
    public static final int SCREEN_CAPTURE_ALLOWED = 2;

    /** Possible values {@link SCREEN_CAPTURE} */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"SCREEN_CAPTURE_"},
            value = {
                SCREEN_CAPTURE_DISALLOWED,
                SCREEN_CAPTURE_ALLOWED,
            })
    public @interface ScreenCaptureValue {}

    /**
     * Policy that controls whether the screen capture is allowed or disallowed. Disallowing screen
     * capture also prevents the content from being shown on display devices that do not have a
     * secure video output. See {@link android.view.Display#FLAG_SECURE} for more details about
     * secure surfaces and secure displays. Throws SecurityException if the caller is not permitted
     * to control screen capture policy. If the scope is set to {@link
     * DevicePolicyManager#POLICY_SCOPE_DEVICE} and the caller is not a profile owner of an
     * organization-owned managed profile or a device owner, a security exception will be thrown.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_SCREEN_CAPTURE_API)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_USER, POLICY_SCOPE_DEVICE},
                            affectedResource = RESOURCE_PER_USER,
                            requiredPermission = MANAGE_DEVICE_POLICY_SCREEN_CAPTURE,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = ALLOWED,
                                            fullUserProfileOwner = ALLOWED,
                                            profileOwnerOnUser0 = ALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = DISALLOWED)),
            intDef = ScreenCaptureValue.class,
            defaultValue = SCREEN_CAPTURE_ALLOWED,
            resolutionMechanism = @EnumResolutionMechanism(custom = true))
    public static final PolicyIdentifier<Integer> SCREEN_CAPTURE =
            new PolicyIdentifier<>("SCREEN_CAPTURE");

    /** Assist content is disallowed. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_ASSIST_CONTENT)
    public static final int ASSIST_CONTENT_DISALLOWED = 1;

    /** Assist content is allowed. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_ASSIST_CONTENT)
    public static final int ASSIST_CONTENT_ALLOWED = 2;

    /** Possible values for {@link #ASSIST_CONTENT}. */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"ASSIST_CONTENT_"},
            value = {
                ASSIST_CONTENT_DISALLOWED,
                ASSIST_CONTENT_ALLOWED,
            })
    public @interface AssistContentValue {}

    /**
     * Policy that controls whether assist content is allowed or disallowed. Disallowing assist
     * content prevents screenshots and information about an app from being sent to a privileged app
     * such as the Assistant app.
     *
     * <p>Note: This policy shares internal state with {@link
     * android.os.UserManager#DISALLOW_ASSIST_CONTENT}. Using both simultaneously will cause
     * undefined behavior.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_ASSIST_CONTENT)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_USER, POLICY_SCOPE_DEVICE},
                            affectedResource = RESOURCE_PER_USER,
                            requiredPermission = MANAGE_DEVICE_POLICY_ASSIST_CONTENT,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = ALLOWED,
                                            fullUserProfileOwner = ALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = DISALLOWED)),
            intDef = AssistContentValue.class,
            defaultValue = ASSIST_CONTENT_ALLOWED,
            resolutionMechanism =
                    @EnumResolutionMechanism(
                            mostRestrictive = {ASSIST_CONTENT_DISALLOWED, ASSIST_CONTENT_ALLOWED}))
    public static final PolicyIdentifier<Integer> ASSIST_CONTENT =
            new PolicyIdentifier<>("ASSIST_CONTENT");

    /** Universal Clipboard is disallowed, preventing clip data being shared user's other devices */
    @FlaggedApi(FLAG_UNIVERSAL_CLIPBOARD)
    public static final int UNIVERSAL_CLIPBOARD_DISALLOWED = 1;

    /**
     * Universal Clipboard is allowed, allowing user to opt in clip data sharing among other user
     * owned devices.
     */
    @FlaggedApi(FLAG_UNIVERSAL_CLIPBOARD)
    public static final int UNIVERSAL_CLIPBOARD_ALLOWED = 2;

    /** Possible values {@link UNIVERSAL_CLIPBOARD} */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"UNIVERSAL_CLIPBOARD_"},
            value = {
                UNIVERSAL_CLIPBOARD_DISALLOWED,
                UNIVERSAL_CLIPBOARD_ALLOWED,
            })
    public @interface UniversalClipboardValue {}

    /**
     * Policy that controls whether the Universal Clipboard feature is allowed or disallowed.
     * Universal Clipboard is the feature that allows users to share clip data among their own
     * devices automatically. When disallowed, users cannot enable the feature. When allowed, users
     * can choose to enabled or disable the feature.
     */
    @FlaggedApi(FLAG_UNIVERSAL_CLIPBOARD)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_USER, POLICY_SCOPE_DEVICE},
                            affectedResource = RESOURCE_PER_USER,
                            requiredPermission = MANAGE_DEVICE_POLICY_UNIVERSAL_CLIPBOARD,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = ALLOWED,
                                            fullUserProfileOwner = ALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = DISALLOWED)),
            intDef = UniversalClipboardValue.class,
            defaultValue = UNIVERSAL_CLIPBOARD_ALLOWED,
            resolutionMechanism =
                    @EnumResolutionMechanism(
                            mostRestrictive = {
                                UNIVERSAL_CLIPBOARD_DISALLOWED,
                                UNIVERSAL_CLIPBOARD_ALLOWED
                            }))
    public static final PolicyIdentifier<Integer> UNIVERSAL_CLIPBOARD =
            new PolicyIdentifier<>("UNIVERSAL_CLIPBOARD");
}
