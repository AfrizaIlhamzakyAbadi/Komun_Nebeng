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

import static android.Manifest.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS_SECURITY_CRITICAL;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_INSTALL_UNKNOWN_SOURCES;
import static android.app.admin.DevicePolicyManager.POLICY_SCOPE_DEVICE;
import static android.app.admin.DevicePolicyManager.POLICY_SCOPE_USER;
import static android.app.admin.DevicePolicyManager.RESOURCE_DEVICE_WIDE;
import static android.app.admin.DevicePolicyManager.RESOURCE_PER_USER;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_UNTRUSTED_APPS;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_VERIFY_APPS;
import static android.processor.devicepolicy.AllowedDpcTypes.ALLOWED;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.app.admin.PolicyIdentifier;
import android.processor.devicepolicy.AllowedDpcTypes;
import android.processor.devicepolicy.AllowedRoles;
import android.processor.devicepolicy.EnumPolicyDefinition;
import android.processor.devicepolicy.EnumResolutionMechanism;
import android.processor.devicepolicy.PolicyDefinition;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Advanced security settings. These policies could make a managed device less secure, so DPCs might
 * want to consider always setting these policies.
 */
@FlaggedApi(FLAG_POLICY_STREAMLINING_UNTRUSTED_APPS)
public class AdvancedSecurityOverrides {

    private AdvancedSecurityOverrides() {}

    /**
     * Enum value for {@link #UNTRUSTED_APPS} indicating that installing apps from untrusted sources
     * is allowed.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_UNTRUSTED_APPS)
    public static final int UNTRUSTED_APPS_ALLOWED = 1;

    /**
     * Enum value for {@link #UNTRUSTED_APPS} indicating that installing apps from untrusted sources
     * is disallowed.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_UNTRUSTED_APPS)
    public static final int UNTRUSTED_APPS_DISALLOWED = 2;

    /** Possible values for {@link #UNTRUSTED_APPS}. */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"UNTRUSTED_APPS_"},
            value = {UNTRUSTED_APPS_ALLOWED, UNTRUSTED_APPS_DISALLOWED})
    public @interface UntrustedAppsValue {}

    /**
     * Policy to control whether the user is allowed to enable the "Unknown Sources" setting, that
     * allows installation of apps from unknown sources. Unknown sources exclude ADB and special
     * apps such as trusted app stores.
     *
     * <p>Note: This policy shares internal state with {@link
     * android.os.UserManager#DISALLOW_INSTALL_UNKNOWN_SOURCES} and {@link
     * android.os.UserManager#DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY}. Using both simultaneously
     * will cause undefined behavior.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_UNTRUSTED_APPS)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_USER, POLICY_SCOPE_DEVICE},
                            affectedResource = RESOURCE_PER_USER,
                            requiredPermission = MANAGE_DEVICE_POLICY_INSTALL_UNKNOWN_SOURCES,
                            requiredCrossUserPermission =
                                    MANAGE_DEVICE_POLICY_ACROSS_USERS_SECURITY_CRITICAL,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = ALLOWED,
                                            fullUserProfileOwner = ALLOWED,
                                            financedDeviceOwner = ALLOWED,
                                            profileOwnerOnUser0 = ALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = ALLOWED)),
            intDef = UntrustedAppsValue.class,
            defaultValue = UNTRUSTED_APPS_ALLOWED,
            resolutionMechanism = @EnumResolutionMechanism(custom = true))
    public static final PolicyIdentifier<Integer> UNTRUSTED_APPS =
            new PolicyIdentifier<>("UNTRUSTED_APPS");

    /** Application verification is enforced and the user can not turn it off. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_VERIFY_APPS)
    public static final int VERIFY_APPS_ENFORCED = 1;

    /** Application verification is not enforced and the user can choose whether to enable it. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_VERIFY_APPS)
    public static final int VERIFY_APPS_USER_CHOICE = 2;

    /** Possible values {@link #VERIFY_APPS} */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"VERIFY_APPS_"},
            value = {
                VERIFY_APPS_ENFORCED,
                VERIFY_APPS_USER_CHOICE,
            })
    public @interface VerifyAppsValue {}

    /**
     * Policy that controls whether application verification is enforced. When enforced, the user
     * can not turn off application verification.
     *
     * <p>Note: This policy shares internal state with {@link
     * android.os.UserManager#ENSURE_VERIFY_APPS}. Using both simultaneously will cause undefined
     * behavior.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_VERIFY_APPS)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_DEVICE},
                            affectedResource = RESOURCE_DEVICE_WIDE,
                            requiredPermission = MANAGE_DEVICE_POLICY_INSTALL_UNKNOWN_SOURCES,
                            requiredCrossUserPermission =
                                    MANAGE_DEVICE_POLICY_ACROSS_USERS_SECURITY_CRITICAL,
                            allowedRoles = @AllowedRoles(deviceController = ALLOWED),
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            financedDeviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = ALLOWED,
                                            fullUserProfileOwner = ALLOWED,
                                            profileOwnerOnUser0 = ALLOWED)),
            intDef = VerifyAppsValue.class,
            defaultValue = VERIFY_APPS_USER_CHOICE,
            resolutionMechanism = @EnumResolutionMechanism(custom = true))
    public static final PolicyIdentifier<Integer> VERIFY_APPS =
            new PolicyIdentifier<>("VERIFY_APPS");
}
