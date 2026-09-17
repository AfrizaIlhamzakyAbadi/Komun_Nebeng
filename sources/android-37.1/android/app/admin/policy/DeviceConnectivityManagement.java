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
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_MOBILE_NETWORK;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_PHYSICAL_SIM;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_USB_DATA_SIGNALLING;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_USB_FILE_TRANSFER;
import static android.app.admin.DevicePolicyManager.POLICY_SCOPE_DEVICE;
import static android.app.admin.DevicePolicyManager.POLICY_SCOPE_USER;
import static android.app.admin.DevicePolicyManager.RESOURCE_DEVICE_WIDE;
import static android.app.admin.DevicePolicyManager.RESOURCE_PER_USER;
import static android.app.admin.flags.Flags.FLAG_PHYSICAL_SIM_POLICY;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_DISALLOW_USB_FILE_TRANSFER;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_USB_DATA_SIGNALING;
import static android.processor.devicepolicy.AllowedDpcTypes.ALLOWED;
import static android.processor.devicepolicy.AllowedDpcTypes.DISALLOWED;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.app.admin.PolicyIdentifier;
import android.app.admin.flags.Flags;
import android.processor.devicepolicy.AllowedDpcTypes;
import android.processor.devicepolicy.AllowedRoles;
import android.processor.devicepolicy.EnumPolicyDefinition;
import android.processor.devicepolicy.EnumResolutionMechanism;
import android.processor.devicepolicy.PolicyDefinition;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Covers controls for device connectivity such as Wi-Fi, USB data access, keyboard/mouse
 * connections, and more.
 */
@FlaggedApi(FLAG_PHYSICAL_SIM_POLICY)
public class DeviceConnectivityManagement {

    private DeviceConnectivityManagement() {}

    /** Specifies that the user is allowed to use physical SIMs on the device. */
    @FlaggedApi(FLAG_PHYSICAL_SIM_POLICY)
    public static final int PHYSICAL_SIM_ALLOWED = 1;

    /** Specifies that the user is NOT allowed to use physical SIMs on the device. */
    @FlaggedApi(FLAG_PHYSICAL_SIM_POLICY)
    public static final int PHYSICAL_SIM_DISALLOWED = 2;

    /** TODO: Document this properly. */
    @Hide
    @IntDef(
            prefix = {"PHYSICAL_SIM_"},
            value = {PHYSICAL_SIM_ALLOWED, PHYSICAL_SIM_DISALLOWED})
    @Retention(RetentionPolicy.SOURCE)
    public @interface PhysicalSimValue {}

    /**
     * Policy that controls whether the usage of physical SIMs is allowed.
     *
     * <p>When enforced, this policy blocks all functionality of physical SIMs, effectively
     * preventing them from being activated or used on the device. This policy does not affect any
     * embedded SIMs (eSIMs).
     *
     * <p><b>Warning:</b> This policy applies immediately upon enablement. Callers should proceed
     * with caution to avoid unintended user disruption.
     */
    @FlaggedApi(FLAG_PHYSICAL_SIM_POLICY)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_DEVICE},
                            affectedResource = RESOURCE_DEVICE_WIDE,
                            requiredPermission = MANAGE_DEVICE_POLICY_PHYSICAL_SIM,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = DISALLOWED,
                                            fullUserProfileOwner = DISALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = ALLOWED)),
            intDef = PhysicalSimValue.class,
            defaultValue = PHYSICAL_SIM_ALLOWED,
            resolutionMechanism =
                    @EnumResolutionMechanism(
                            mostRestrictive = {PHYSICAL_SIM_DISALLOWED, PHYSICAL_SIM_ALLOWED}))
    public static final PolicyIdentifier<Integer> PHYSICAL_SIM =
            new PolicyIdentifier<>("PHYSICAL_SIM");

    /** USB file transfer is allowed. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_DISALLOW_USB_FILE_TRANSFER)
    public static final int USB_FILE_TRANSFER_ALLOWED = 1;

    /** USB file transfer is disallowed. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_DISALLOW_USB_FILE_TRANSFER)
    public static final int USB_FILE_TRANSFER_DISALLOWED = 2;

    /** Possible values of {@link USB_FILE_TRANSFER} */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"USB_FILE_TRANSFER_"},
            value = {
                USB_FILE_TRANSFER_ALLOWED,
                USB_FILE_TRANSFER_DISALLOWED,
            })
    public @interface UsbFileTransferValue {}

    /**
     * Policy that controls whether USB file transfer is allowed or disallowed.
     *
     * <p>When disallowed ({@link #USB_FILE_TRANSFER_DISALLOWED}), file transfer over USB is
     * blocked.
     *
     * <p>Note: This restriction shares internal state with the policy {@link
     * android.os.UserManager#DISALLOW_USB_FILE_TRANSFER}. Using both simultaneously will cause
     * undefined behavior.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_DISALLOW_USB_FILE_TRANSFER)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_USER, POLICY_SCOPE_DEVICE},
                            affectedResource = RESOURCE_PER_USER,
                            requiredPermission = MANAGE_DEVICE_POLICY_USB_FILE_TRANSFER,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = DISALLOWED,
                                            fullUserProfileOwner = ALLOWED,
                                            profileOwnerOnUser0 = ALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = ALLOWED)),
            intDef = UsbFileTransferValue.class,
            defaultValue = USB_FILE_TRANSFER_ALLOWED,
            resolutionMechanism =
                    @EnumResolutionMechanism(
                            mostRestrictive = {
                                USB_FILE_TRANSFER_DISALLOWED,
                                USB_FILE_TRANSFER_ALLOWED
                            }))
    public static final PolicyIdentifier<Integer> USB_FILE_TRANSFER =
            new PolicyIdentifier<>("USB_FILE_TRANSFER");

    /** USB data signaling is allowed. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_USB_DATA_SIGNALING)
    public static final int USB_DATA_SIGNALING_ALLOWED = 1;

    /** USB data signaling is disallowed. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_USB_DATA_SIGNALING)
    public static final int USB_DATA_SIGNALING_DISALLOWED = 2;

    /** Possible values of {@link USB_DATA_SIGNALING} */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"USB_DATA_SIGNALING_"},
            value = {
                USB_DATA_SIGNALING_ALLOWED,
                USB_DATA_SIGNALING_DISALLOWED,
            })
    public @interface UsbDataSignalingValue {}

    /**
     * Policy that controls whether USB data signaling is allowed or disallowed.
     *
     * <p>When disallowed ({@link #USB_DATA_SIGNALING_DISALLOWED}), data signaling over USB is
     * disabled. This typically disables data transfer but may still allow charging.
     *
     * <p>Note: Disabling USB data signaling will effectively block file transfer over USB as well,
     * regardless of the state of the {@link #USB_FILE_TRANSFER} policy.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_USB_DATA_SIGNALING)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_USER, POLICY_SCOPE_DEVICE},
                            affectedResource = RESOURCE_DEVICE_WIDE,
                            requiredPermission = MANAGE_DEVICE_POLICY_USB_DATA_SIGNALLING,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = DISALLOWED,
                                            fullUserProfileOwner = ALLOWED,
                                            profileOwnerOnUser0 = ALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = ALLOWED)),
            intDef = UsbDataSignalingValue.class,
            defaultValue = USB_DATA_SIGNALING_ALLOWED,
            resolutionMechanism =
                    @EnumResolutionMechanism(
                            mostRestrictive = {
                                USB_DATA_SIGNALING_DISALLOWED,
                                USB_DATA_SIGNALING_ALLOWED
                            }))
    public static final PolicyIdentifier<Integer> USB_DATA_SIGNALING =
            new PolicyIdentifier<>("USB_DATA_SIGNALING");

    /** Network reset is allowed. */
    @FlaggedApi(Flags.FLAG_POLICY_STREAMLINING_NETWORK_RESET)
    public static final int NETWORK_RESET_ALLOWED = 1;

    /** Network reset is disallowed. */
    @FlaggedApi(Flags.FLAG_POLICY_STREAMLINING_NETWORK_RESET)
    public static final int NETWORK_RESET_DISALLOWED = 2;

    /** Possible values {@link NETWORK_RESET} */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"NETWORK_RESET_"},
            value = {
                NETWORK_RESET_ALLOWED,
                NETWORK_RESET_DISALLOWED,
            })
    public @interface NetworkResetValue {}

    /**
     * Policy that controls whether the user is permitted to reset network settings from Settings.
     *
     * <p>Note: This policy shares internal state with {@link
     * android.os.UserManager#DISALLOW_NETWORK_RESET}. Using both simultaneously will cause
     * undefined behavior.
     */
    @FlaggedApi(Flags.FLAG_POLICY_STREAMLINING_NETWORK_RESET)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_DEVICE},
                            affectedResource = RESOURCE_DEVICE_WIDE,
                            requiredPermission = MANAGE_DEVICE_POLICY_MOBILE_NETWORK,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            profileOwnerOnUser0 = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = DISALLOWED,
                                            fullUserProfileOwner = DISALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = ALLOWED)),
            intDef = NetworkResetValue.class,
            defaultValue = NETWORK_RESET_ALLOWED,
            resolutionMechanism =
                    @EnumResolutionMechanism(
                            mostRestrictive = {
                                NETWORK_RESET_DISALLOWED,
                                NETWORK_RESET_ALLOWED,
                            }))
    public static final PolicyIdentifier<Integer> NETWORK_RESET =
            new PolicyIdentifier<>("NETWORK_RESET");
}
