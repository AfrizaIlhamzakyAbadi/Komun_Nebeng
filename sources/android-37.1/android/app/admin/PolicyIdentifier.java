/*
 * Copyright (C) 2025 The Android Open Source Project
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

package android.app.admin;

import static android.Manifest.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_BLUETOOTH;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_FACTORY_RESET;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_FUN;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_LOCKSCREEN_MESSAGE;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_MANAGED_SUBSCRIPTIONS;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_SAFE_BOOT;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_VPN;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_WIFI;
import static android.app.admin.DevicePolicyManager.POLICY_SCOPE_DEVICE;
import static android.app.admin.DevicePolicyManager.POLICY_SCOPE_USER;
import static android.app.admin.DevicePolicyManager.RESOURCE_DEVICE_WIDE;
import static android.app.admin.DevicePolicyManager.RESOURCE_PER_USER;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_APP_INSTALL;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_APP_UNINSTALL;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_ASSIST_CONTENT;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_DISALLOW_FACTORY_RESET;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_EASTER_EGGS;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_LOCKSCREEN_MESSAGE;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_SAFE_BOOT;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_SCREEN_CAPTURE_API;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_USER_CONFIGURED_VPN;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_VERIFY_APPS;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_WIFI_NETWORK_SELECTION;
import static android.processor.devicepolicy.AllowedDpcTypes.ALLOWED;
import static android.processor.devicepolicy.AllowedDpcTypes.DISALLOWED;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.SystemApi;
import android.app.admin.flags.Flags;
import android.app.admin.policy.AdvancedSecurityOverrides;
import android.app.admin.policy.ApplicationManagement;
import android.app.admin.policy.DataLeakPrevention;
import android.processor.devicepolicy.AllowedDpcTypes;
import android.processor.devicepolicy.AllowedRoles;
import android.processor.devicepolicy.EnumPolicyDefinition;
import android.processor.devicepolicy.EnumResolutionMechanism;
import android.processor.devicepolicy.PolicyDefinition;
import android.processor.devicepolicy.StringPolicyDefinition;
import android.processor.devicepolicy.StringResolutionMechanism;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

/**
 * Represents a type safe identifier for a policy. Use it as a key for {@link
 * DevicePolicyManager#setPolicy setPolicy} and related APIs.
 *
 * <p>Policies should be structured as:
 *
 * <pre>{@code
 * {@literal @}TypePolicyDefinition
 * private static final PolicyIdentifier<Type> POLICY_NAME =
 *     new PolicyIdentifier<>("POLICY_NAME");
 * }</pre>
 *
 * <p>Currently policy definitions are restricted to fields of {@link PolicyIdentifier}. This
 * restriction might be lifted in the future.
 *
 * @param <T> Represents the type of the value that is associated with this identifier.
 */
@FlaggedApi(FLAG_POLICY_STREAMLINING)
public final class PolicyIdentifier<T> {
    private final String mId;

    /**
     * Create an instance of PolicyIdentifier. Should only be used to create the static definitions
     * below.
     *
     * <p><b>This API is only public for testing purposes. Real applications should only use the
     * static instances defined below.</b>
     */
    public PolicyIdentifier(@NonNull String id) {
        this.mId = id;
    }

    /**
     * Get the string representation of this identifier.
     *
     * @return The string representation of this identifier
     */
    @Hide
    @NonNull
    public String getId() {
        return mId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PolicyIdentifier)) return false;
        PolicyIdentifier<?> that = (PolicyIdentifier<?>) o;
        return mId.equals(that.mId);
    }

    @Override
    public int hashCode() {
        return mId.hashCode();
    }

    @Override
    @NonNull
    public String toString() {
        return mId;
    }

    // LINT.IfChange

    /** Specifies that the user is allowed to transfer managed eSIMs from the device. */
    @FlaggedApi(Flags.FLAG_MANAGED_ESIM_OUTGOING_TRANSFER_POLICY)
    public static final int MANAGED_ESIM_OUTGOING_TRANSFER_ALLOWED = 1;

    /** Specifies that the user is not allowed to transfer managed eSIMs from the device. */
    @FlaggedApi(Flags.FLAG_MANAGED_ESIM_OUTGOING_TRANSFER_POLICY)
    public static final int MANAGED_ESIM_OUTGOING_TRANSFER_DISALLOWED = 2;

    /** TODO: Document this properly. */
    @Hide
    @IntDef(
            prefix = {"MANAGED_ESIM_OUTGOING_TRANSFER_"},
            value = {
                MANAGED_ESIM_OUTGOING_TRANSFER_ALLOWED,
                MANAGED_ESIM_OUTGOING_TRANSFER_DISALLOWED
            })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ManagedEsimOutgoingTransfer {}

    /**
     * Policy that controls whether outgoing transfer is allowed for managed embedded subscriptions.
     */
    @FlaggedApi(Flags.FLAG_MANAGED_ESIM_OUTGOING_TRANSFER_POLICY)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_DEVICE},
                            affectedResource = RESOURCE_DEVICE_WIDE,
                            requiredPermission = MANAGE_DEVICE_POLICY_MANAGED_SUBSCRIPTIONS,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = ALLOWED,
                                            fullUserProfileOwner = ALLOWED,
                                            profileOwnerOnUser0 = ALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = DISALLOWED)),
            intDef = ManagedEsimOutgoingTransfer.class,
            defaultValue = MANAGED_ESIM_OUTGOING_TRANSFER_ALLOWED,
            resolutionMechanism =
                    @EnumResolutionMechanism(
                            mostRestrictive = {
                                MANAGED_ESIM_OUTGOING_TRANSFER_DISALLOWED,
                                MANAGED_ESIM_OUTGOING_TRANSFER_ALLOWED
                            }))
    public static final PolicyIdentifier<Integer> MANAGED_ESIM_OUTGOING_TRANSFER =
            new PolicyIdentifier<>("MANAGED_ESIM_OUTGOING_TRANSFER");

    /**
     * Policy that sets a custom message to be shown on the lock screen. This message is displayed
     * on the device screen when locked, and is useful for a lost or stolen device.
     *
     * <p>The message set using this method overrides any owner information manually set by the user
     * and prevents the user from further changing it.
     *
     * <p>If the message is {@code null} then the device owner info is cleared and the user owner
     * info is shown on the lock screen if it is set.
     *
     * <p>If the message contains only whitespaces then the message on the lock screen will be blank
     * and the user will not be allowed to change it.
     *
     * <p>If the message needs to be localized, it is the responsibility of the {@link
     * DeviceAdminReceiver} to listen to the {@link Intent#ACTION_LOCALE_CHANGED} broadcast and set
     * a new version of this string accordingly.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_LOCKSCREEN_MESSAGE)
    @NonNull
    @StringPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_DEVICE},
                            affectedResource = RESOURCE_DEVICE_WIDE,
                            requiredPermission = MANAGE_DEVICE_POLICY_LOCKSCREEN_MESSAGE,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = DISALLOWED,
                                            fullUserProfileOwner = DISALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = ALLOWED)),
            resolutionMechanism = @StringResolutionMechanism(custom = true),
            emptyStringAllowed = false)
    public static final PolicyIdentifier<String> LOCKSCREEN_MESSAGE =
            new PolicyIdentifier<>("LOCKSCREEN_MESSAGE");

    /** The device can connect to any Wi-Fi network. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_WIFI_NETWORK_SELECTION)
    public static final int WIFI_NETWORK_SELECTION_NO_RESTRICTION = 1;

    /**
     * The device can only connect to managed Wi-Fi networks if any are available. If no managed
     * Wi-Fi networks are available, the device can connect to any network.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_WIFI_NETWORK_SELECTION)
    public static final int WIFI_NETWORK_SELECTION_MANAGED_WHEN_AVAILABLE = 2;

    /** The device can connect to managed Wi-Fi networks only. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_WIFI_NETWORK_SELECTION)
    public static final int WIFI_NETWORK_SELECTION_MANAGED_ONLY = 3;

    /** Possible values for {@link #WIFI_NETWORK_SELECTION} */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"WIFI_NETWORK_SELECTION_"},
            value = {
                WIFI_NETWORK_SELECTION_NO_RESTRICTION,
                WIFI_NETWORK_SELECTION_MANAGED_WHEN_AVAILABLE,
                WIFI_NETWORK_SELECTION_MANAGED_ONLY,
            })
    public @interface WifiNetworkSelectionValue {}

    /**
     * Policy that controls whether the device's Wi-Fi selection is restricted to managed networks.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_WIFI_NETWORK_SELECTION)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_DEVICE},
                            affectedResource = RESOURCE_DEVICE_WIDE,
                            requiredPermission = MANAGE_DEVICE_POLICY_WIFI,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = DISALLOWED,
                                            fullUserProfileOwner = DISALLOWED,
                                            profileOwnerOnUser0 = DISALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = ALLOWED)),
            intDef = WifiNetworkSelectionValue.class,
            defaultValue = WIFI_NETWORK_SELECTION_NO_RESTRICTION,
            resolutionMechanism =
                    @EnumResolutionMechanism(
                            mostRestrictive = {
                                WIFI_NETWORK_SELECTION_MANAGED_ONLY,
                                WIFI_NETWORK_SELECTION_MANAGED_WHEN_AVAILABLE,
                                WIFI_NETWORK_SELECTION_NO_RESTRICTION
                            }))
    public static final PolicyIdentifier<Integer> WIFI_NETWORK_SELECTION =
            new PolicyIdentifier<>("WIFI_NETWORK_SELECTION");

    /** Easter eggs are disallowed. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_EASTER_EGGS)
    public static final int EASTER_EGGS_DISALLOWED = 1;

    /** Easter eggs are allowed. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_EASTER_EGGS)
    public static final int EASTER_EGGS_ALLOWED = 2;

    /** Possible values {@link EASTER_EGGS} */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"EASTER_EGGS_"},
            value = {
                EASTER_EGGS_DISALLOWED,
                EASTER_EGGS_ALLOWED,
            })
    public @interface EasterEggsValue {}

    /**
     * Policy that controls whether the user is allowed to access various Easter egg games across
     * the system (for instance, in settings).
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_EASTER_EGGS)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_USER, POLICY_SCOPE_DEVICE},
                            affectedResource = RESOURCE_PER_USER,
                            requiredPermission = MANAGE_DEVICE_POLICY_FUN,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = DISALLOWED,
                                            profileOwnerOnUser0 = ALLOWED,
                                            fullUserProfileOwner = ALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = DISALLOWED)),
            intDef = EasterEggsValue.class,
            defaultValue = EASTER_EGGS_ALLOWED,
            resolutionMechanism = @EnumResolutionMechanism(custom = true))
    public static final PolicyIdentifier<Integer> EASTER_EGGS =
            new PolicyIdentifier<>("EASTER_EGGS");

    /** Possible values {@link FACTORY_RESET} */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"FACTORY_RESET_"},
            value = {
                FACTORY_RESET_DISALLOWED,
                FACTORY_RESET_ALLOWED,
            })
    public @interface FactoryResetValue {}

    /** The settings menu of the user has factory reset disabled. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_DISALLOW_FACTORY_RESET)
    public static final int FACTORY_RESET_DISALLOWED = 1;

    /** The settings menu of the user has the factory reset option. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_DISALLOW_FACTORY_RESET)
    public static final int FACTORY_RESET_ALLOWED = 2;

    /**
     * Policy that controls if the factory reset option is available in the settings menu. Even if
     * it is disabled factory reset might still be possible through other means.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_DISALLOW_FACTORY_RESET)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_DEVICE, POLICY_SCOPE_USER},
                            affectedResource = RESOURCE_PER_USER,
                            requiredPermission = MANAGE_DEVICE_POLICY_FACTORY_RESET,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            profileOwnerOnUser0 = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            financedDeviceOwner = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = ALLOWED,
                                            fullUserProfileOwner = DISALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = DISALLOWED)),
            intDef = FactoryResetValue.class,
            defaultValue = FACTORY_RESET_ALLOWED,
            resolutionMechanism = @EnumResolutionMechanism(custom = true))
    public static final PolicyIdentifier<Integer> FACTORY_RESET =
            new PolicyIdentifier<>("FACTORY_RESET");

    /** Bluetooth sharing is disallowed. */
    @FlaggedApi(Flags.FLAG_POLICY_STREAMLINING_BLUETOOTH_SHARING)
    public static final int BLUETOOTH_SHARING_DISALLOWED = 1;

    /** Bluetooth sharing is allowed. */
    @FlaggedApi(Flags.FLAG_POLICY_STREAMLINING_BLUETOOTH_SHARING)
    public static final int BLUETOOTH_SHARING_ALLOWED = 2;

    /** Possible values {@link BLUETOOTH_SHARING} */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"BLUETOOTH_SHARING_"},
            value = {
                BLUETOOTH_SHARING_DISALLOWED,
                BLUETOOTH_SHARING_ALLOWED,
            })
    public @interface BluetoothSharingValue {}

    /** Policy that controls whether Bluetooth sharing is allowed or disallowed. */
    @FlaggedApi(Flags.FLAG_POLICY_STREAMLINING_BLUETOOTH_SHARING)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_USER, POLICY_SCOPE_DEVICE},
                            affectedResource = RESOURCE_PER_USER,
                            requiredPermission = MANAGE_DEVICE_POLICY_BLUETOOTH,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = ALLOWED,
                                            fullUserProfileOwner = ALLOWED,
                                            profileOwnerOnUser0 = ALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = DISALLOWED)),
            intDef = BluetoothSharingValue.class,
            defaultValue = BLUETOOTH_SHARING_ALLOWED,
            resolutionMechanism = @EnumResolutionMechanism(custom = true))
    public static final PolicyIdentifier<Integer> BLUETOOTH_SHARING =
            new PolicyIdentifier<>("BLUETOOTH_SHARING");

    /** Safe boot is disallowed. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_SAFE_BOOT)
    public static final int SAFE_BOOT_DISALLOWED = 1;

    /** Safe boot is allowed. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_SAFE_BOOT)
    public static final int SAFE_BOOT_ALLOWED = 2;

    /** Possible values {@link SAFE_BOOT} */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"SAFE_BOOT_"},
            value = {
                SAFE_BOOT_DISALLOWED,
                SAFE_BOOT_ALLOWED,
            })
    public @interface SafeBootValue {}

    /**
     * Policy that controls whether the user is allowed to reboot the device into safe boot mode.
     *
     * <p>Note: This policy shares internal state with {@link
     * android.os.UserManager#DISALLOW_SAFE_BOOT}. Using both simultaneously will cause undefined
     * behavior.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_SAFE_BOOT)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_DEVICE},
                            affectedResource = RESOURCE_DEVICE_WIDE,
                            requiredPermission = MANAGE_DEVICE_POLICY_SAFE_BOOT,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = DISALLOWED,
                                            fullUserProfileOwner = DISALLOWED,
                                            financedDeviceOwner = ALLOWED,
                                            profileOwnerOnUser0 = ALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = ALLOWED)),
            intDef = SafeBootValue.class,
            defaultValue = SAFE_BOOT_ALLOWED,
            resolutionMechanism = @EnumResolutionMechanism(custom = true))
    public static final PolicyIdentifier<Integer> SAFE_BOOT = new PolicyIdentifier<>("SAFE_BOOT");

    /** Configuring VPN is disallowed. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_USER_CONFIGURED_VPN)
    public static final int USER_CONFIGURED_VPN_DISALLOWED = 1;

    /** Configuring VPN is allowed. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_USER_CONFIGURED_VPN)
    public static final int USER_CONFIGURED_VPN_ALLOWED = 2;

    /** Possible values {@link #USER_CONFIGURED_VPN} */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"USER_CONFIGURED_VPN_"},
            value = {
                USER_CONFIGURED_VPN_DISALLOWED,
                USER_CONFIGURED_VPN_ALLOWED,
            })
    public @interface UserConfiguredVpnValue {}

    /**
     * Policy that controls whether the user is allowed to configure VPN. Disallowing VPN
     * configuration prevents the user from adding, removing, or modifying VPN configurations, and
     * also removes the VPNs that were previously configured by the user.
     *
     * <p>Note: This policy shares internal state with {@link
     * android.os.UserManager#DISALLOW_CONFIG_VPN}. Using both simultaneously will cause undefined
     * behavior.
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_USER_CONFIGURED_VPN)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_USER, POLICY_SCOPE_DEVICE},
                            affectedResource = RESOURCE_PER_USER,
                            requiredPermission = MANAGE_DEVICE_POLICY_VPN,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = ALLOWED,
                                            profileOwnerOnUser0 = ALLOWED,
                                            fullUserProfileOwner = ALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = ALLOWED)),
            intDef = UserConfiguredVpnValue.class,
            defaultValue = USER_CONFIGURED_VPN_ALLOWED,
            resolutionMechanism =
                    @EnumResolutionMechanism(
                            mostRestrictive = {
                                USER_CONFIGURED_VPN_DISALLOWED,
                                USER_CONFIGURED_VPN_ALLOWED
                            }))
    public static final PolicyIdentifier<Integer> USER_CONFIGURED_VPN =
            new PolicyIdentifier<>("USER_CONFIGURED_VPN");

    // Make sure to update the policy metadata file when updating the definitions
    // above by running the following command:
    //
    // m export_policies_textproto dist &&
    // cp out/dist/policies.textproto \
    // frameworks/base/tools/policymetadata/policies.textproto
    //
    // LINT.ThenChange(/tools/policymetadata/policies.textproto)

    /** Will be removed when callers are updated to use the new location. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_SCREEN_CAPTURE_API)
    public static final int SCREEN_CAPTURE_DISALLOWED = 1;

    /** Will be removed when callers are updated to use the new location. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_SCREEN_CAPTURE_API)
    public static final int SCREEN_CAPTURE_ALLOWED = 2;

    /** Will be removed when callers are updated to use the new location. */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"SCREEN_CAPTURE_"},
            value = {
                SCREEN_CAPTURE_DISALLOWED,
                SCREEN_CAPTURE_ALLOWED,
            })
    public @interface ScreenCaptureValue {}

    /** Will be removed when callers are updated to use the new location. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_SCREEN_CAPTURE_API)
    @NonNull
    public static final PolicyIdentifier<Integer> SCREEN_CAPTURE =
            DataLeakPrevention.SCREEN_CAPTURE;

    @FlaggedApi(FLAG_POLICY_STREAMLINING_VERIFY_APPS)
    public static final int VERIFY_APPS_ENFORCED = 1;

    /** Will be removed when callers are updated to use the new location. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_VERIFY_APPS)
    public static final int VERIFY_APPS_USER_CHOICE = 2;

    /** Will be removed when callers are updated to use the new location. */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"VERIFY_APPS_"},
            value = {
                VERIFY_APPS_ENFORCED,
                VERIFY_APPS_USER_CHOICE,
            })
    public @interface VerifyAppsValue {}

    /** Will be removed when callers are updated to use the new location. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_VERIFY_APPS)
    @NonNull
    public static final PolicyIdentifier<Integer> VERIFY_APPS =
            AdvancedSecurityOverrides.VERIFY_APPS;

    /** Will be removed when callers are updated to use the new location. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_APP_INSTALL)
    public static final int APP_INSTALL_ALLOWED = 1;

    /** Will be removed when callers are updated to use the new location. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_APP_INSTALL)
    public static final int APP_INSTALL_DISALLOWED = 2;

    /** Will be removed when callers are updated to use the new location. */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"APP_INSTALL_"},
            value = {
                APP_INSTALL_ALLOWED,
                APP_INSTALL_DISALLOWED,
            })
    public @interface AppInstallValue {}

    /** Will be removed when callers are updated to use the new location. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_APP_INSTALL)
    @NonNull
    public static final PolicyIdentifier<Integer> APP_INSTALL = ApplicationManagement.APP_INSTALL;

    /** Will be removed when callers are updated to use the new location. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_APP_UNINSTALL)
    public static final int APP_UNINSTALL_ALLOWED = 1;

    /** Will be removed when callers are updated to use the new location. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_APP_UNINSTALL)
    public static final int APP_UNINSTALL_DISALLOWED = 2;

    /** Will be removed when callers are updated to use the new location. */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"APP_UNINSTALL_"},
            value = {
                APP_UNINSTALL_ALLOWED,
                APP_UNINSTALL_DISALLOWED,
            })
    public @interface AppUninstallValue {}

    /** Will be removed when callers are updated to use the new location. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_APP_UNINSTALL)
    @NonNull
    public static final PolicyIdentifier<Integer> APP_UNINSTALL =
            ApplicationManagement.APP_UNINSTALL;

    /** Will be removed when callers are updated to use the new location. */
    @SystemApi
    @FlaggedApi(android.app.contentsafety.flags.Flags.FLAG_CONTENT_RESTRICTION_API)
    @NonNull
    public static final PolicyIdentifier<List<PackageIdentifier>> CONTENT_SAFETY_APPS =
            ApplicationManagement.CONTENT_SAFETY_APPS;

    /** Will be removed when callers are updated to use the new location. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_ASSIST_CONTENT)
    public static final int ASSIST_CONTENT_DISALLOWED = 1;

    /** Will be removed when callers are updated to use the new location. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_ASSIST_CONTENT)
    public static final int ASSIST_CONTENT_ALLOWED = 2;

    /** Will be removed when callers are updated to use the new location. */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"ASSIST_CONTENT_"},
            value = {
                ASSIST_CONTENT_DISALLOWED,
                ASSIST_CONTENT_ALLOWED,
            })
    public @interface AssistContentValue {}

    /** Will be removed when callers are updated to use the new location. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_ASSIST_CONTENT)
    @NonNull
    public static final PolicyIdentifier<Integer> ASSIST_CONTENT =
            DataLeakPrevention.ASSIST_CONTENT;

    /** Will be removed when callers are updated to use the new location. */
    @FlaggedApi(android.companion.Flags.FLAG_UNIVERSAL_CLIPBOARD)
    public static final int UNIVERSAL_CLIPBOARD_DISALLOWED = 1;

    /** Will be removed when callers are updated to use the new location. */
    @FlaggedApi(android.companion.Flags.FLAG_UNIVERSAL_CLIPBOARD)
    public static final int UNIVERSAL_CLIPBOARD_ALLOWED = 2;

    /** Will be removed when callers are updated to use the new location. */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"UNIVERSAL_CLIPBOARD_"},
            value = {
                UNIVERSAL_CLIPBOARD_DISALLOWED,
                UNIVERSAL_CLIPBOARD_ALLOWED,
            })
    public @interface UniversalClipboardValue {}

    /** Will be removed when callers are updated to use the new location. */
    @FlaggedApi(android.companion.Flags.FLAG_UNIVERSAL_CLIPBOARD)
    @NonNull
    public static final PolicyIdentifier<Integer> UNIVERSAL_CLIPBOARD =
            DataLeakPrevention.UNIVERSAL_CLIPBOARD;
}
