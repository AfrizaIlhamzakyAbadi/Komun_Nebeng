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
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS_FULL;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_APPS_CONTROL;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_CONTENT_SAFETY_APPS;
import static android.app.admin.DevicePolicyManager.POLICY_SCOPE_USER;
import static android.app.admin.DevicePolicyManager.RESOURCE_PER_USER;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_APP_INSTALL;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_APP_UNINSTALL;
import static android.app.contentsafety.flags.Flags.FLAG_CONTENT_RESTRICTION_API;
import static android.processor.devicepolicy.AllowedDpcTypes.ALLOWED;
import static android.processor.devicepolicy.AllowedDpcTypes.DISALLOWED;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.SystemApi;
import android.app.admin.PackageIdentifier;
import android.app.admin.PolicyIdentifier;
import android.processor.devicepolicy.AllowedDpcTypes;
import android.processor.devicepolicy.AllowedRoles;
import android.processor.devicepolicy.EnumPolicyDefinition;
import android.processor.devicepolicy.EnumResolutionMechanism;
import android.processor.devicepolicy.ListOfPackagePolicyDefinition;
import android.processor.devicepolicy.ListResolutionMechanism;
import android.processor.devicepolicy.PolicyDefinition;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/** Application related policies that affect all applications on the device. */
@FlaggedApi(FLAG_POLICY_STREAMLINING_APP_INSTALL)
public class ApplicationManagement {

    private ApplicationManagement() {}

    /** Installing Apps is allowed. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_APP_INSTALL)
    public static final int APP_INSTALL_ALLOWED = 1;

    /** Installing Apps is disallowed. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_APP_INSTALL)
    public static final int APP_INSTALL_DISALLOWED = 2;

    /** Possible values {@link #APP_INSTALL} */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"APP_INSTALL_"},
            value = {
                APP_INSTALL_ALLOWED,
                APP_INSTALL_DISALLOWED,
            })
    public @interface AppInstallValue {}

    /** Policy that controls whether app installation is allowed or disallowed. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_APP_INSTALL)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_USER},
                            affectedResource = RESOURCE_PER_USER,
                            requiredPermission = MANAGE_DEVICE_POLICY_APPS_CONTROL,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = ALLOWED,
                                            fullUserProfileOwner = ALLOWED,
                                            financedDeviceOwner = ALLOWED,
                                            profileOwnerOnUser0 = ALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = DISALLOWED)),
            intDef = AppInstallValue.class,
            defaultValue = APP_INSTALL_ALLOWED,
            resolutionMechanism = @EnumResolutionMechanism(custom = true))
    public static final PolicyIdentifier<Integer> APP_INSTALL =
            new PolicyIdentifier<>("APP_INSTALL");

    /** Uninstalling Apps is allowed. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_APP_UNINSTALL)
    public static final int APP_UNINSTALL_ALLOWED = 1;

    /** Uninstalling Apps is disallowed. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_APP_UNINSTALL)
    public static final int APP_UNINSTALL_DISALLOWED = 2;

    /** Possible values {@link #APP_UNINSTALL} */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"APP_UNINSTALL_"},
            value = {
                APP_UNINSTALL_ALLOWED,
                APP_UNINSTALL_DISALLOWED,
            })
    public @interface AppUninstallValue {}

    /** Policy that controls whether app uninstallation is allowed or disallowed. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_APP_UNINSTALL)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_USER},
                            affectedResource = RESOURCE_PER_USER,
                            requiredPermission = MANAGE_DEVICE_POLICY_APPS_CONTROL,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = ALLOWED,
                                            fullUserProfileOwner = ALLOWED,
                                            financedDeviceOwner = ALLOWED,
                                            profileOwnerOnUser0 = ALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = DISALLOWED)),
            intDef = AppUninstallValue.class,
            defaultValue = APP_UNINSTALL_ALLOWED,
            resolutionMechanism = @EnumResolutionMechanism(custom = true))
    public static final PolicyIdentifier<Integer> APP_UNINSTALL =
            new PolicyIdentifier<>("APP_UNINSTALL");

    /**
     * Policy that sets the list of packages as the holders of the {@link
     * android.app.role.RoleManager#ROLE_CONTENT_SAFETY} role.
     *
     * <p>When a package is removed from this list, the role will be removed from the package as
     * well.
     */
    @SystemApi
    @FlaggedApi(FLAG_CONTENT_RESTRICTION_API)
    @NonNull
    @ListOfPackagePolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_USER},
                            affectedResource = RESOURCE_PER_USER,
                            requiredPermission = MANAGE_DEVICE_POLICY_CONTENT_SAFETY_APPS,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS_FULL,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = DISALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice =
                                                    DISALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = DISALLOWED,
                                            fullUserProfileOwner = DISALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = DISALLOWED)),
            resolutionMechanism = @ListResolutionMechanism(union = true))
    public static final PolicyIdentifier<List<PackageIdentifier>> CONTENT_SAFETY_APPS =
            new PolicyIdentifier<>("CONTENT_SAFETY_APPS");
}
