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

import static android.Manifest.permission.MANAGE_DEVICE_POLICY_ACROSS_USERS_FULL;
import static android.Manifest.permission.MANAGE_DEVICE_POLICY_NETWORK_LOGGING;
import static android.app.admin.DevicePolicyManager.POLICY_SCOPE_USER;
import static android.app.admin.DevicePolicyManager.RESOURCE_PER_USER;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING;
import static android.app.admin.flags.Flags.FLAG_POLICY_STREAMLINING_NETWORK_LOGGING_ENABLED;
import static android.processor.devicepolicy.AllowedDpcTypes.ALLOWED;

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

/**
 * Class containing policy definitions for Reporting.
 */
@FlaggedApi(FLAG_POLICY_STREAMLINING)
public final class Reporting {
    private Reporting() {}

    // LINT.IfChange

    /** Network logging is enabled. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_NETWORK_LOGGING_ENABLED)
    public static final int NETWORK_LOGGING_ENABLED = 1;

    /** Network logging is disabled. */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_NETWORK_LOGGING_ENABLED)
    public static final int NETWORK_LOGGING_DISABLED = 2;

    /**
     * Possible values for {@link #NETWORK_LOGGING}.
     */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            prefix = {"NETWORK_LOGGING_"},
            value = {
                NETWORK_LOGGING_ENABLED,
                NETWORK_LOGGING_DISABLED,
            })
    public @interface NetworkLoggingValue {}

    /**
     * Policy that controls whether network logging is enabled or disabled.
     *
     * <p> Network logs contain DNS lookup and connect() library call events. The following library
     *     functions are recorded while network logging is active:
     *     <ul>
     *       <li>{@code getaddrinfo()}</li>
     *       <li>{@code gethostbyname()}</li>
     *       <li>{@code connect()}</li>
     *     </ul>
     *
     * <p> Network logging is a low-overhead tool for forensics but it is not guaranteed to use
     *     full system call logging; event reporting is enabled by default for all processes but not
     *     strongly enforced.
     *     Events from applications using alternative implementations of libc, making direct kernel
     *     calls, or deliberately obfuscating traffic may not be recorded.
     *
     * <p> Some common network events may not be reported. For example:
     *     <ul>
     *       <li>Applications may hardcode IP addresses to reduce the number of DNS lookups, or use
     *           an alternative system for name resolution, and so avoid calling
     *           {@code getaddrinfo()} or {@code gethostbyname}.</li>
     *       <li>Applications may use datagram sockets for performance reasons, for example
     *           for a game client. Calling {@code connect()} is unnecessary for this kind of
     *           socket, so it will not trigger a network event.</li>
     *     </ul>
     *
     * <p> It is possible to directly intercept layer 3 traffic leaving the device using an
     *     always-on VPN service.
     *     See {@link DevicePolicyManager#setAlwaysOnVpnPackage}
     *     and {@link android.net.VpnService} for details.
     *
     * <p><strong>Note:</strong> The device owner won't be able to retrieve network logs if there
     * are unaffiliated secondary users or profiles on the device, regardless of whether the
     * feature is enabled. Logs will be discarded if the internal buffer fills up while waiting for
     * all users to become affiliated. Therefore it's recommended that affiliation ids are set for
     * new users as soon as possible after provisioning via
     * {@link DevicePolicyManager#setAffiliationIds}.
     *
     * @see DevicePolicyManager#setAffiliationIds
     * @see DevicePolicyManager#retrieveNetworkLogs
     */
    @FlaggedApi(FLAG_POLICY_STREAMLINING_NETWORK_LOGGING_ENABLED)
    @NonNull
    @EnumPolicyDefinition(
            base =
                    @PolicyDefinition(
                            allowedScopes = {POLICY_SCOPE_USER},
                            affectedResource = RESOURCE_PER_USER,
                            requiredPermission = MANAGE_DEVICE_POLICY_NETWORK_LOGGING,
                            requiredCrossUserPermission = MANAGE_DEVICE_POLICY_ACROSS_USERS_FULL,
                            allowedDpcTypes =
                                    @AllowedDpcTypes(
                                            deviceOwner = ALLOWED,
                                            managedProfileOwnerOfOrganizationOwnedDevice = ALLOWED,
                                            managedProfileOwnerOfPersonalOwnedDevice = ALLOWED,
                                            fullUserProfileOwner = ALLOWED,
                                            profileOwnerOnUser0 = ALLOWED),
                            allowedRoles = @AllowedRoles(deviceController = ALLOWED)),
            intDef = NetworkLoggingValue.class,
            defaultValue = NETWORK_LOGGING_DISABLED,
            resolutionMechanism =
                    @EnumResolutionMechanism(
                            mostRestrictive = {NETWORK_LOGGING_ENABLED, NETWORK_LOGGING_DISABLED}))
    public static final PolicyIdentifier<Integer> NETWORK_LOGGING =
            new PolicyIdentifier<>("NETWORK_LOGGING");

    // Make sure to update the policy metadata file when updating the definitions above by running
    // the following command:
    // m export_policies_textproto dist && \
    // cp out/dist/policies.textproto frameworks/base/tools/policymetadata/policies.textproto

    // LINT.ThenChange(/tools/policymetadata/policies.textproto)
}
