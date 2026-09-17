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
import static android.Manifest.permission.SET_TIME;
import static android.Manifest.permission.SET_TIME_ZONE;
import static android.app.admin.DevicePolicyManager.POLICY_SCOPE_DEVICE;
import static android.app.admin.DevicePolicyManager.RESOURCE_DEVICE_WIDE;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_AUTO_TIME;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_AUTO_TIME_ZONE;
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

/** Policies related to date and time. */
@FlaggedApi(FLAG_POLICY_STREAMLINING_AUTO_TIME)
public class DateTime {

    private DateTime() {}

    /** The user can choose whether the time is automatically obtained from the network or not. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_AUTO_TIME)
    public static final int AUTO_TIME_USER_CHOICE =
            DevicePolicyManager.AUTO_TIME_NOT_CONTROLLED_BY_POLICY;

    /**
     * The admin has disabled the time to be automatically obtained from the network. This is not
     * enforced and the user can still enable it.
     *
     * <p><b>Note:</b> Using this value is highly discouraged. Prefer using the {@link
     * #AUTO_TIME_DISABLED} instead.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_AUTO_TIME)
    public static final int AUTO_TIME_DISABLED_UNENFORCED = DevicePolicyManager.AUTO_TIME_DISABLED;

    /**
     * The admin has enabled the time to be automatically obtained from the network. This is not
     * enforced and the user can still disable it.
     *
     * <p><b>Note:</b> Using this value is highly discouraged. Prefer using the {@link
     * #AUTO_TIME_ENABLED} instead.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_AUTO_TIME)
    public static final int AUTO_TIME_ENABLED_UNENFORCED = DevicePolicyManager.AUTO_TIME_ENABLED;

    /**
     * The admin has disabled the time to be automatically obtained from the network. This is
     * enforced and the user cannot enable it.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_AUTO_TIME)
    public static final int AUTO_TIME_DISABLED = 3;

    /**
     * The admin has enabled the time to be automatically obtained from the network. This is
     * enforced and the user cannot disable it.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_AUTO_TIME)
    public static final int AUTO_TIME_ENABLED = 4;

    /**
     * Possible values {@link AUTO_TIME}
     */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"AUTO_TIME_"},
            value = {
                AUTO_TIME_USER_CHOICE,
                AUTO_TIME_DISABLED_UNENFORCED,
                AUTO_TIME_ENABLED_UNENFORCED,
                AUTO_TIME_DISABLED,
                AUTO_TIME_ENABLED,
            })
    public @interface AutoTimeValue {}

    /** Policy that controls whether the time is automatically obtained from the network or not. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_AUTO_TIME)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_DEVICE},
                            affectedResource = RESOURCE_DEVICE_WIDE,
                            requiredPermission = SET_TIME,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = DISALLOWED,
                                            fullUserProfileOwner = DISALLOWED,
                                            profileOwnerOnUser0 = ALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = ALLOWED)),
            intDef = AutoTimeValue.class,
            defaultValue = AUTO_TIME_USER_CHOICE,
            resolutionMechanism = @EnumResolutionMechanism(custom = true))
    public static final PolicyIdentifier<Integer> AUTO_TIME = new PolicyIdentifier<>("AUTO_TIME");

    /** The user can choose whether the device's time zone is set automatically or not. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_AUTO_TIME_ZONE)
    public static final int AUTO_TIME_ZONE_USER_CHOICE =
            DevicePolicyManager.AUTO_TIME_ZONE_NOT_CONTROLLED_BY_POLICY;

    /**
     * The admin has disabled automatic time zone detection. This is not enforced and the user can
     * still enable it.
     *
     * <p><b>Note:</b> Using this value is highly discouraged. Prefer using the {@link
     * #AUTO_TIME_ZONE_DISABLED} instead.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_AUTO_TIME_ZONE)
    public static final int AUTO_TIME_ZONE_DISABLED_UNENFORCED =
            DevicePolicyManager.AUTO_TIME_ZONE_DISABLED;

    /**
     * The admin has enabled the time zone to be automatically obtained from the network. This is
     * not enforced and the user can still disable it.
     *
     * <p><b>Note:</b> Using this value is highly discouraged. Prefer using the {@link
     * #AUTO_TIME_ZONE_ENABLED} instead.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_AUTO_TIME_ZONE)
    public static final int AUTO_TIME_ZONE_ENABLED_UNENFORCED =
            DevicePolicyManager.AUTO_TIME_ZONE_ENABLED;

    /**
     * The admin has disabled automatic time zone detection. This is enforced and the user cannot
     * enable it.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_AUTO_TIME_ZONE)
    public static final int AUTO_TIME_ZONE_DISABLED = 3;

    /**
     * The admin has enabled the time zone to be automatically obtained from the network. This is
     * enforced and the user cannot disable it.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_AUTO_TIME_ZONE)
    public static final int AUTO_TIME_ZONE_ENABLED = 4;

    /**
     * Possible values {@link #AUTO_TIME_ZONE}
     */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"AUTO_TIME_ZONE_"},
            value = {
                AUTO_TIME_ZONE_USER_CHOICE,
                AUTO_TIME_ZONE_DISABLED_UNENFORCED,
                AUTO_TIME_ZONE_ENABLED_UNENFORCED,
                AUTO_TIME_ZONE_DISABLED,
                AUTO_TIME_ZONE_ENABLED,
            })
    public @interface AutoTimeZoneValue {}

    /**
     * Policy that controls whether the device's time zone is set automatically, e.g. obtained from
     * network or location.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_AUTO_TIME_ZONE)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_DEVICE},
                            affectedResource = RESOURCE_DEVICE_WIDE,
                            requiredPermission = SET_TIME_ZONE,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = DISALLOWED,
                                            fullUserProfileOwner = DISALLOWED,
                                            profileOwnerOnUser0 = ALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = ALLOWED)),
            intDef = AutoTimeZoneValue.class,
            defaultValue = AUTO_TIME_ZONE_USER_CHOICE,
            resolutionMechanism = @EnumResolutionMechanism(custom = true))
    public static final PolicyIdentifier<Integer> AUTO_TIME_ZONE =
            new PolicyIdentifier<>("AUTO_TIME_ZONE");
}
