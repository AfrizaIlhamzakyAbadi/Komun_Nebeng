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

package android.app.admin.metadata;

import static android.app.admin.PolicyIdentifier.BLUETOOTH_SHARING;
import static android.app.admin.PolicyIdentifier.EASTER_EGGS;
import static android.app.admin.PolicyIdentifier.FACTORY_RESET;
import static android.app.admin.PolicyIdentifier.LOCKSCREEN_MESSAGE;
import static android.app.admin.PolicyIdentifier.MANAGED_ESIM_OUTGOING_TRANSFER;
import static android.app.admin.PolicyIdentifier.SAFE_BOOT;
import static android.app.admin.PolicyIdentifier.USER_CONFIGURED_VPN;
import static android.app.admin.PolicyIdentifier.WIFI_NETWORK_SELECTION;
import static android.app.admin.policy.AdvancedSecurityOverrides.UNTRUSTED_APPS;
import static android.app.admin.policy.AdvancedSecurityOverrides.VERIFY_APPS;
import static android.app.admin.policy.ApplicationManagement.APP_INSTALL;
import static android.app.admin.policy.ApplicationManagement.APP_UNINSTALL;
import static android.app.admin.policy.ApplicationManagement.CONTENT_SAFETY_APPS;
import static android.app.admin.policy.DataLeakPrevention.ASSIST_CONTENT;
import static android.app.admin.policy.DataLeakPrevention.SCREEN_CAPTURE;
import static android.app.admin.policy.DataLeakPrevention.UNIVERSAL_CLIPBOARD;
import static android.app.admin.policy.DateTime.AUTO_TIME;
import static android.app.admin.policy.DateTime.AUTO_TIME_ZONE;
import static android.app.admin.policy.DeviceConnectivityManagement.NETWORK_RESET;
import static android.app.admin.policy.DeviceConnectivityManagement.PHYSICAL_SIM;
import static android.app.admin.policy.DeviceConnectivityManagement.USB_DATA_SIGNALING;
import static android.app.admin.policy.DeviceConnectivityManagement.USB_FILE_TRANSFER;
import static android.app.admin.policy.Reporting.NETWORK_LOGGING;

import android.annotation.Hide;
import android.app.admin.PackageIdentifier;
import android.app.admin.PolicyIdentifier;
import java.lang.Integer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Generated class that contains metadata on all known policies.
 */
@Hide
public class Policies {
    /**
     * Generated method that returns a list of all policy metadata
     */
    public static List<PolicyMetadata<?>> loadPolicyMetadata() {
        List<PolicyMetadata<?>> policies = new ArrayList<PolicyMetadata<?>>();
        policies.add(new EnumPolicyMetadata(
            /* id= */ APP_INSTALL,
            /* allowedScopes= */ Set.of(
                1
            ),
            /* affectedResource= */ 2,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_APPS_CONTROL",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                2, // FINANCED_DEVICE_OWNER
                3, // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
                4, // PROFILE_OWNER_ON_USER0
                5, // MANAGED_PROFILE_OWNER_OF_PERSONAL_OWNED_DEVICE
                6, // UNAFFILIATED_FULL_USER_PROFILE_OWNER
                7  // AFFILIATED_FULL_USER_PROFILE_OWNER
            ),
            /* featureFlag= */ "android.app.admin.flags.policy_streamlining_app_install",
            /* resolutionMechanism= */ null,
            /* allowedValues= */ Set.of(
                1,
                2
            )
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ APP_UNINSTALL,
            /* allowedScopes= */ Set.of(
                1
            ),
            /* affectedResource= */ 2,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_APPS_CONTROL",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                2, // FINANCED_DEVICE_OWNER
                3, // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
                4, // PROFILE_OWNER_ON_USER0
                5, // MANAGED_PROFILE_OWNER_OF_PERSONAL_OWNED_DEVICE
                6, // UNAFFILIATED_FULL_USER_PROFILE_OWNER
                7  // AFFILIATED_FULL_USER_PROFILE_OWNER
            ),
            /* featureFlag= */ "android.app.admin.flags.policy_streamlining_app_uninstall",
            /* resolutionMechanism= */ null,
            /* allowedValues= */ Set.of(
                1,
                2
            )
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ ASSIST_CONTENT,
            /* allowedScopes= */ Set.of(
                1,
                2
            ),
            /* affectedResource= */ 2,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ASSIST_CONTENT",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                3, // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
                5, // MANAGED_PROFILE_OWNER_OF_PERSONAL_OWNED_DEVICE
                6, // UNAFFILIATED_FULL_USER_PROFILE_OWNER
                7  // AFFILIATED_FULL_USER_PROFILE_OWNER
            ),
            /* featureFlag= */ "android.app.admin.flags.policy_streamlining_assist_content",
            /* resolutionMechanism= */ new ResolutionMechanismMetadata.MostRestrictive<Integer>(
                List.of(
                    new Integer(1),
                    new Integer(2)
                )
            ),
            /* allowedValues= */ Set.of(
                1,
                2
            )
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ AUTO_TIME,
            /* allowedScopes= */ Set.of(
                2
            ),
            /* affectedResource= */ 1,
            /* requiredPermission= */ "android.permission.SET_TIME",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                3, // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
                4  // PROFILE_OWNER_ON_USER0
            ),
            /* featureFlag= */ "android.app.admin.flags.policy_streamlining_auto_time",
            /* resolutionMechanism= */ null,
            /* allowedValues= */ Set.of(
                0,
                1,
                2,
                3,
                4
            )
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ AUTO_TIME_ZONE,
            /* allowedScopes= */ Set.of(
                2
            ),
            /* affectedResource= */ 1,
            /* requiredPermission= */ "android.permission.SET_TIME_ZONE",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                3, // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
                4  // PROFILE_OWNER_ON_USER0
            ),
            /* featureFlag= */ "android.app.admin.flags.policy_streamlining_auto_time_zone",
            /* resolutionMechanism= */ null,
            /* allowedValues= */ Set.of(
                0,
                1,
                2,
                3,
                4
            )
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ BLUETOOTH_SHARING,
            /* allowedScopes= */ Set.of(
                1,
                2
            ),
            /* affectedResource= */ 2,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_BLUETOOTH",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                3, // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
                4, // PROFILE_OWNER_ON_USER0
                5, // MANAGED_PROFILE_OWNER_OF_PERSONAL_OWNED_DEVICE
                6, // UNAFFILIATED_FULL_USER_PROFILE_OWNER
                7  // AFFILIATED_FULL_USER_PROFILE_OWNER
            ),
            /* featureFlag= */ "android.app.admin.flags.policy_streamlining_bluetooth_sharing",
            /* resolutionMechanism= */ null,
            /* allowedValues= */ Set.of(
                1,
                2
            )
        ));
        policies.add(new ListPolicyMetadata<PackageIdentifier>(
            /* id= */ CONTENT_SAFETY_APPS,
            /* elementMetadata= */ new PackagePolicyMetadata(
                /* id= */ new PolicyIdentifier<PackageIdentifier>(CONTENT_SAFETY_APPS.getId() + "#elements"),
                /* allowedScopes= */ Set.of(
                    1
                ),
                /* affectedResource= */ 2,
                /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_CONTENT_SAFETY_APPS",
                /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS_FULL",
                /* allowedDpcTypes= */ Set.of(),
                /* featureFlag= */ "android.app.contentsafety.flags.content_restriction_api",
                /* resolutionMechanism= */ null
            ),
            /* resolutionMechanism= */ new ResolutionMechanismMetadata.ListUnion<List<PackageIdentifier>>(),
            /* emptyListAllowed= */ false,
            /* maxListLength= */ 10000,
            /* featureFlag= */ "android.app.contentsafety.flags.content_restriction_api"
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ EASTER_EGGS,
            /* allowedScopes= */ Set.of(
                1,
                2
            ),
            /* affectedResource= */ 2,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_FUN",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                3, // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
                4, // PROFILE_OWNER_ON_USER0
                6, // UNAFFILIATED_FULL_USER_PROFILE_OWNER
                7  // AFFILIATED_FULL_USER_PROFILE_OWNER
            ),
            /* featureFlag= */ "android.app.admin.flags.policy_streamlining_easter_eggs",
            /* resolutionMechanism= */ null,
            /* allowedValues= */ Set.of(
                1,
                2
            )
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ FACTORY_RESET,
            /* allowedScopes= */ Set.of(
                2,
                1
            ),
            /* affectedResource= */ 2,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_FACTORY_RESET",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                2, // FINANCED_DEVICE_OWNER
                3, // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
                4, // PROFILE_OWNER_ON_USER0
                5  // MANAGED_PROFILE_OWNER_OF_PERSONAL_OWNED_DEVICE
            ),
            /* featureFlag= */ "android.app.admin.flags.policy_streamlining_disallow_factory_reset",
            /* resolutionMechanism= */ null,
            /* allowedValues= */ Set.of(
                1,
                2
            )
        ));
        policies.add(new StringPolicyMetadata(
            /* id= */ LOCKSCREEN_MESSAGE,
            /* allowedScopes= */ Set.of(
                2
            ),
            /* affectedResource= */ 1,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_LOCKSCREEN_MESSAGE",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                3  // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
            ),
            /* featureFlag= */ "android.app.admin.flags.policy_streamlining_lockscreen_message",
            /* resolutionMechanism= */ null,
            /* emptyStringAllowed= */ false,
            /* unprintableCharactersAllowed= */ false,
            /* pureWhitespaceAllowed= */ false,
            /* unstrippedStringAllowed= */ false,
            /* maxLength= */ Integer.MAX_VALUE
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ MANAGED_ESIM_OUTGOING_TRANSFER,
            /* allowedScopes= */ Set.of(
                2
            ),
            /* affectedResource= */ 1,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_MANAGED_SUBSCRIPTIONS",
            /* requiredCrossUserPermission= */ null,
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                3, // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
                4, // PROFILE_OWNER_ON_USER0
                5, // MANAGED_PROFILE_OWNER_OF_PERSONAL_OWNED_DEVICE
                6, // UNAFFILIATED_FULL_USER_PROFILE_OWNER
                7  // AFFILIATED_FULL_USER_PROFILE_OWNER
            ),
            /* featureFlag= */ "android.app.admin.flags.managed_esim_outgoing_transfer_policy",
            /* resolutionMechanism= */ new ResolutionMechanismMetadata.MostRestrictive<Integer>(
                List.of(
                    new Integer(2),
                    new Integer(1)
                )
            ),
            /* allowedValues= */ Set.of(
                1,
                2
            )
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ NETWORK_LOGGING,
            /* allowedScopes= */ Set.of(
                1
            ),
            /* affectedResource= */ 2,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_NETWORK_LOGGING",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS_FULL",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                3, // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
                4, // PROFILE_OWNER_ON_USER0
                5, // MANAGED_PROFILE_OWNER_OF_PERSONAL_OWNED_DEVICE
                6, // UNAFFILIATED_FULL_USER_PROFILE_OWNER
                7  // AFFILIATED_FULL_USER_PROFILE_OWNER
            ),
            /* featureFlag= */ "android.app.admin.flags.policy_streamlining_network_logging_enabled",
            /* resolutionMechanism= */ new ResolutionMechanismMetadata.MostRestrictive<Integer>(
                List.of(
                    new Integer(1),
                    new Integer(2)
                )
            ),
            /* allowedValues= */ Set.of(
                1,
                2
            )
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ PHYSICAL_SIM,
            /* allowedScopes= */ Set.of(
                2
            ),
            /* affectedResource= */ 1,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_PHYSICAL_SIM",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                3  // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
            ),
            /* featureFlag= */ "android.app.admin.flags.physical_sim_policy",
            /* resolutionMechanism= */ new ResolutionMechanismMetadata.MostRestrictive<Integer>(
                List.of(
                    new Integer(2),
                    new Integer(1)
                )
            ),
            /* allowedValues= */ Set.of(
                1,
                2
            )
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ SAFE_BOOT,
            /* allowedScopes= */ Set.of(
                2
            ),
            /* affectedResource= */ 1,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_SAFE_BOOT",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                2, // FINANCED_DEVICE_OWNER
                3, // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
                4  // PROFILE_OWNER_ON_USER0
            ),
            /* featureFlag= */ "android.app.admin.flags.policy_streamlining_safe_boot",
            /* resolutionMechanism= */ null,
            /* allowedValues= */ Set.of(
                1,
                2
            )
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ SCREEN_CAPTURE,
            /* allowedScopes= */ Set.of(
                1,
                2
            ),
            /* affectedResource= */ 2,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_SCREEN_CAPTURE",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                3, // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
                4, // PROFILE_OWNER_ON_USER0
                5, // MANAGED_PROFILE_OWNER_OF_PERSONAL_OWNED_DEVICE
                6, // UNAFFILIATED_FULL_USER_PROFILE_OWNER
                7  // AFFILIATED_FULL_USER_PROFILE_OWNER
            ),
            /* featureFlag= */ "android.app.admin.flags.policy_streamlining_screen_capture_api",
            /* resolutionMechanism= */ null,
            /* allowedValues= */ Set.of(
                1,
                2
            )
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ UNIVERSAL_CLIPBOARD,
            /* allowedScopes= */ Set.of(
                1,
                2
            ),
            /* affectedResource= */ 2,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_UNIVERSAL_CLIPBOARD",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                3, // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
                5, // MANAGED_PROFILE_OWNER_OF_PERSONAL_OWNED_DEVICE
                6, // UNAFFILIATED_FULL_USER_PROFILE_OWNER
                7  // AFFILIATED_FULL_USER_PROFILE_OWNER
            ),
            /* featureFlag= */ "android.companion.universal_clipboard",
            /* resolutionMechanism= */ new ResolutionMechanismMetadata.MostRestrictive<Integer>(
                List.of(
                    new Integer(1),
                    new Integer(2)
                )
            ),
            /* allowedValues= */ Set.of(
                1,
                2
            )
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ UNTRUSTED_APPS,
            /* allowedScopes= */ Set.of(
                1,
                2
            ),
            /* affectedResource= */ 2,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_INSTALL_UNKNOWN_SOURCES",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS_SECURITY_CRITICAL",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                2, // FINANCED_DEVICE_OWNER
                3, // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
                4, // PROFILE_OWNER_ON_USER0
                5, // MANAGED_PROFILE_OWNER_OF_PERSONAL_OWNED_DEVICE
                6, // UNAFFILIATED_FULL_USER_PROFILE_OWNER
                7  // AFFILIATED_FULL_USER_PROFILE_OWNER
            ),
            /* featureFlag= */ "android.app.admin.flags.policy_streamlining_untrusted_apps",
            /* resolutionMechanism= */ null,
            /* allowedValues= */ Set.of(
                1,
                2
            )
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ USB_DATA_SIGNALING,
            /* allowedScopes= */ Set.of(
                1,
                2
            ),
            /* affectedResource= */ 1,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_USB_DATA_SIGNALLING",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                3, // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
                4, // PROFILE_OWNER_ON_USER0
                6, // UNAFFILIATED_FULL_USER_PROFILE_OWNER
                7  // AFFILIATED_FULL_USER_PROFILE_OWNER
            ),
            /* featureFlag= */ "android.app.admin.flags.policy_streamlining_usb_data_signaling",
            /* resolutionMechanism= */ new ResolutionMechanismMetadata.MostRestrictive<Integer>(
                List.of(
                    new Integer(2),
                    new Integer(1)
                )
            ),
            /* allowedValues= */ Set.of(
                1,
                2
            )
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ USB_FILE_TRANSFER,
            /* allowedScopes= */ Set.of(
                1,
                2
            ),
            /* affectedResource= */ 2,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_USB_FILE_TRANSFER",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                3, // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
                4, // PROFILE_OWNER_ON_USER0
                6, // UNAFFILIATED_FULL_USER_PROFILE_OWNER
                7  // AFFILIATED_FULL_USER_PROFILE_OWNER
            ),
            /* featureFlag= */ "android.app.admin.flags.policy_streamlining_disallow_usb_file_transfer",
            /* resolutionMechanism= */ new ResolutionMechanismMetadata.MostRestrictive<Integer>(
                List.of(
                    new Integer(2),
                    new Integer(1)
                )
            ),
            /* allowedValues= */ Set.of(
                1,
                2
            )
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ USER_CONFIGURED_VPN,
            /* allowedScopes= */ Set.of(
                1,
                2
            ),
            /* affectedResource= */ 2,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_VPN",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                3, // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
                4, // PROFILE_OWNER_ON_USER0
                5, // MANAGED_PROFILE_OWNER_OF_PERSONAL_OWNED_DEVICE
                6, // UNAFFILIATED_FULL_USER_PROFILE_OWNER
                7  // AFFILIATED_FULL_USER_PROFILE_OWNER
            ),
            /* featureFlag= */ "android.app.admin.flags.policy_streamlining_user_configured_vpn",
            /* resolutionMechanism= */ new ResolutionMechanismMetadata.MostRestrictive<Integer>(
                List.of(
                    new Integer(1),
                    new Integer(2)
                )
            ),
            /* allowedValues= */ Set.of(
                1,
                2
            )
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ VERIFY_APPS,
            /* allowedScopes= */ Set.of(
                2
            ),
            /* affectedResource= */ 1,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_INSTALL_UNKNOWN_SOURCES",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS_SECURITY_CRITICAL",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                2, // FINANCED_DEVICE_OWNER
                3, // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
                4, // PROFILE_OWNER_ON_USER0
                5, // MANAGED_PROFILE_OWNER_OF_PERSONAL_OWNED_DEVICE
                6, // UNAFFILIATED_FULL_USER_PROFILE_OWNER
                7  // AFFILIATED_FULL_USER_PROFILE_OWNER
            ),
            /* featureFlag= */ "android.app.admin.flags.policy_streamlining_verify_apps",
            /* resolutionMechanism= */ null,
            /* allowedValues= */ Set.of(
                1,
                2
            )
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ WIFI_NETWORK_SELECTION,
            /* allowedScopes= */ Set.of(
                2
            ),
            /* affectedResource= */ 1,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_WIFI",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                3  // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
            ),
            /* featureFlag= */ "android.app.admin.flags.policy_streamlining_wifi_network_selection",
            /* resolutionMechanism= */ new ResolutionMechanismMetadata.MostRestrictive<Integer>(
                List.of(
                    new Integer(3),
                    new Integer(2),
                    new Integer(1)
                )
            ),
            /* allowedValues= */ Set.of(
                1,
                2,
                3
            )
        ));
        policies.add(new EnumPolicyMetadata(
            /* id= */ NETWORK_RESET,
            /* allowedScopes= */ Set.of(
                2
            ),
            /* affectedResource= */ 1,
            /* requiredPermission= */ "android.permission.MANAGE_DEVICE_POLICY_MOBILE_NETWORK",
            /* requiredCrossUserPermission= */ "android.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS",
            /* allowedDpcTypes= */ Set.of(
                1, // DEVICE_OWNER
                3, // MANAGED_PROFILE_OWNER_OF_ORGANIZATION_OWNED_DEVICE
                4  // PROFILE_OWNER_ON_USER0
            ),
            /* featureFlag= */ "android.app.admin.flags.policy_streamlining_network_reset",
            /* resolutionMechanism= */ new ResolutionMechanismMetadata.MostRestrictive<Integer>(
                List.of(
                    new Integer(2),
                    new Integer(1)
                )
            ),
            /* allowedValues= */ Set.of(
                1,
                2
            )
        ));
        return policies;
    }
}
