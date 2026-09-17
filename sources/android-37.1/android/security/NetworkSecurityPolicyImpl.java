/**
 * Copyright (c) 2026 The Android Open Source Project
 *
 * <p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.security;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.security.net.config.ApplicationConfig;
import android.security.net.config.Domain;
import android.security.net.config.NetworkSecurityConfig;
import android.util.Pair;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * An implementation of the Network security policy to allow apps to dynamically set network
 * security requirements.
 */
final class NetworkSecurityPolicyImpl extends NetworkSecurityPolicy {
    private final NetworkSecurityConfig mBaseConfig;
    private final NetworkSecurityConfig mLocalhostConfig;
    private final Set<Pair<Domain, NetworkSecurityConfig>> mConfigs = new HashSet<>();

    NetworkSecurityPolicyImpl(NetworkSecurityConfig baseConfig,
                              NetworkSecurityConfig localhostConfig,
                              Map<Domain, NetworkSecurityConfig> configs) {
        mBaseConfig = baseConfig;
        mLocalhostConfig = localhostConfig;
        for (Entry<Domain, NetworkSecurityConfig> config : configs.entrySet()) {
            mConfigs.add(new Pair<>(config.getKey(), config.getValue()));
        }
    }

    private NetworkSecurityConfig getConfigForHostname(String hostname) {
        return ApplicationConfig.getConfigForHostname(mBaseConfig, mLocalhostConfig, mConfigs,
                                                      hostname);
    }

    @Override
    public boolean isCleartextTrafficPermitted() {
        return getConfigForHostname(null).isCleartextTrafficPermitted();
    }

    @Override
    public void setCleartextTrafficPermitted(boolean permitted) {
        throw new UnsupportedOperationException(
                "Cannot set cleartext traffic permitted on dynamic policy.");
    }

    @Override
    public boolean isCleartextTrafficPermitted(@Nullable String hostname) {
        return getConfigForHostname(hostname).isCleartextTrafficPermitted();
    }

    @Override
    public boolean isCertificateTransparencyVerificationRequired(@NonNull String hostname) {
        return getConfigForHostname(hostname).isCertificateTransparencyVerificationRequired();
    }

    @Override
    public int getDomainEncryptionMode(@NonNull String hostname) {
        return getConfigForHostname(hostname).getDomainEncryptionMode();
    }

    @Override
    public void handleTrustStorageUpdate() {
        throw new UnsupportedOperationException(
                "Cannot handle trust storage update on dynamic policy.");
    }
}
