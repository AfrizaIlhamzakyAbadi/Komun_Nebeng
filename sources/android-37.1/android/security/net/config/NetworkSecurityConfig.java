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

package android.security.net.config;

import static android.security.NetworkSecurityPolicy.DOMAIN_ENCRYPTION_MODE_DISABLED;
import static android.security.NetworkSecurityPolicy.DOMAIN_ENCRYPTION_MODE_ENABLED;
import static android.security.NetworkSecurityPolicy.DOMAIN_ENCRYPTION_MODE_OPPORTUNISTIC;
import static android.security.NetworkSecurityPolicy.DOMAIN_ENCRYPTION_MODE_REQUIRED;

import static com.android.org.conscrypt.net.flags.Flags.encryptedClientHelloPlatform;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.app.compat.CompatChanges;
import android.compat.annotation.ChangeId;
import android.compat.annotation.EnabledAfter;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.security.NetworkSecurityPolicy;
import android.util.ArrayMap;
import android.util.ArraySet;

import com.android.org.conscrypt.ConscryptNetworkSecurityPolicy;

import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * The security configuration to use when performing network communications.
 *
 * <p>A {@link NetworkSecurityConfig} can specify security requirements for all network
 * communications, or for communications with a specific domain or localhost connections.
 *
 * @see <a
 *     href="https://developer.android.com/privacy-and-security/security-config#base-config">base-config</a>
 * @see <a
 *     href="https://developer.android.com/privacy-and-security/security-config#Localhost">localhost</a>
 * @see <a
 *     href="https://developer.android.com/privacy-and-security/security-config#domain-config">domain-config</a>
 */
@FlaggedApi(com.android.org.conscrypt.net.flags.Flags.FLAG_DYNAMIC_NETWORK_SECURITY_POLICY)
public final class NetworkSecurityConfig {
    @Hide
    public static final boolean DEFAULT_CLEARTEXT_TRAFFIC_PERMITTED = true;

    /**
     * Enable Certificate Transparency verification checks by default on all TLS connections. Apps
     * can still opt-out via their Network Security Config.
     */
    @ChangeId
    @EnabledAfter(targetSdkVersion = Build.VERSION_CODES.BAKLAVA)
    static final long DEFAULT_ENABLE_CERTIFICATE_TRANSPARENCY = 407952621L;

    /**
     * Enable Encrypted Client Hello by default on all TLS connections in Network Security Config.
     * Apps can still opt-out via their Network Security Config.
     */
    @ChangeId
    @EnabledAfter(targetSdkVersion = Build.VERSION_CODES.BAKLAVA)
    static final long ENABLE_DEFAULT_ENCRYPTED_CLIENT_HELLO = 419020719L;

    private static final AtomicReference<Boolean>
            sCertificateTransparencyVerificationRequiredDefault = new AtomicReference<>();

    private final boolean mCleartextTrafficPermitted;
    private final boolean mCertificateTransparencyVerificationRequired;
    private final int mDomainEncryptionMode;
    private final PinSet mPins;
    private final List<CertificatesEntryRef> mCertificatesEntryRefs;
    private Set<TrustAnchor> mAnchors;
    private final Object mAnchorsLock = new Object();
    private NetworkSecurityTrustManager mTrustManager;
    private final Object mTrustManagerLock = new Object();

    private NetworkSecurityConfig(boolean cleartextTrafficPermitted,
                                  boolean certificateTransparencyVerificationRequired,
                                  int domainEncryptionMode, PinSet pins,
                                  List<CertificatesEntryRef> certificatesEntryRefs) {
        mCleartextTrafficPermitted = cleartextTrafficPermitted;
        mCertificateTransparencyVerificationRequired = certificateTransparencyVerificationRequired;
        mDomainEncryptionMode = domainEncryptionMode;
        mPins = pins;
        mCertificatesEntryRefs = certificatesEntryRefs;
        // Sort the certificates entry refs so that all entries that override pins come before
        // non-override pin entries. This allows us to handle the case where a certificate is in
        // multiple entry refs by returning the certificate from the first entry ref.
        Collections.sort(mCertificatesEntryRefs, new Comparator<CertificatesEntryRef>() {
            @Override
            public int compare(CertificatesEntryRef lhs, CertificatesEntryRef rhs) {
                if (lhs.overridesPins()) {
                    return rhs.overridesPins() ? 0 : -1;
                } else {
                    return rhs.overridesPins() ? 1 : 0;
                }
            }
        });
    }

    @Hide
    public Set<TrustAnchor> getTrustAnchors() {
        synchronized (mAnchorsLock) {
            if (mAnchors != null) {
                return mAnchors;
            }
            // Merge trust anchors based on the X509Certificate.
            // If we see the same certificate in two TrustAnchors, one with overridesPins and one
            // without, the one with overridesPins wins.
            // Because mCertificatesEntryRefs is sorted with all overridesPins anchors coming first
            // this can be simplified to just using the first occurrence of a certificate.
            Map<X509Certificate, TrustAnchor> anchorMap = new ArrayMap<>();
            for (CertificatesEntryRef ref : mCertificatesEntryRefs) {
                Set<TrustAnchor> anchors = ref.getTrustAnchors();
                for (TrustAnchor anchor : anchors) {
                    X509Certificate cert = anchor.certificate;
                    if (!anchorMap.containsKey(cert)) {
                        anchorMap.put(cert, anchor);
                    }
                }
            }
            ArraySet<TrustAnchor> anchors = new ArraySet<TrustAnchor>(anchorMap.size());
            anchors.addAll(anchorMap.values());
            mAnchors = anchors;
            return mAnchors;
        }
    }

    /**
     * @return true if cleartext traffic is permitted for this {@link NetworkSecurityConfig}.
     */
    public boolean isCleartextTrafficPermitted() {
        return mCleartextTrafficPermitted;
    }

    /**
     * @return true if Certificate Transparency is required for this {@link NetworkSecurityConfig}.
     */
    public boolean isCertificateTransparencyVerificationRequired() {
        return mCertificateTransparencyVerificationRequired;
    }

    /**
     * @return the domain encryption mode. For possible values see {@link
     *     NetworkSecurityPolicy#getDomainEncryptionMode(String)}.
     */
    @NetworkSecurityPolicy.DomainEncryptionMode
    public int getDomainEncryptionMode() {
        return mDomainEncryptionMode;
    }

    @Hide
    public PinSet getPins() {
        return mPins;
    }

    @Hide
    public NetworkSecurityTrustManager getTrustManager() {
        synchronized (mTrustManagerLock) {
            if (mTrustManager == null) {
                mTrustManager = new NetworkSecurityTrustManager(this);
            }
            return mTrustManager;
        }
    }

    /**
     * Sets the NetworkSecurityPolicy for the associated TrustManager.
     */
    @Hide
    void setNetworkSecurityPolicy(libcore.net.NetworkSecurityPolicy policy) {
        getTrustManager().setNetworkSecurityPolicy(new ConscryptNetworkSecurityPolicy(policy));
    }

    @Hide
    public TrustAnchor findTrustAnchorBySubjectAndPublicKey(X509Certificate cert) {
        for (CertificatesEntryRef ref : mCertificatesEntryRefs) {
            TrustAnchor anchor = ref.findBySubjectAndPublicKey(cert);
            if (anchor != null) {
                return anchor;
            }
        }
        return null;
    }

    @Hide
    public TrustAnchor findTrustAnchorByIssuerAndSignature(X509Certificate cert) {
        for (CertificatesEntryRef ref : mCertificatesEntryRefs) {
            TrustAnchor anchor = ref.findByIssuerAndSignature(cert);
            if (anchor != null) {
                return anchor;
            }
        }
        return null;
    }

    @Hide
    public Set<X509Certificate> findAllCertificatesByIssuerAndSignature(X509Certificate cert) {
        Set<X509Certificate> certs = new ArraySet<X509Certificate>();
        for (CertificatesEntryRef ref : mCertificatesEntryRefs) {
            certs.addAll(ref.findAllCertificatesByIssuerAndSignature(cert));
        }
        return certs;
    }

    @Hide
    public void handleTrustStorageUpdate() {
        synchronized (mAnchorsLock) {
            mAnchors = null;
            for (CertificatesEntryRef ref : mCertificatesEntryRefs) {
                ref.handleTrustStorageUpdate();
            }
        }
        getTrustManager().handleTrustStorageUpdate();
    }

    /**
     * Returns the default value for SCT verification. The value depends on the platform version and
     * on the app target sdk level.
     */
    @Hide
    public static boolean certificateTransparencyVerificationRequiredDefault() {
        return sCertificateTransparencyVerificationRequiredDefault.updateAndGet(
                defaultEnabled
                -> defaultEnabled != null
                        ? defaultEnabled
                        : CompatChanges.isChangeEnabled(DEFAULT_ENABLE_CERTIFICATE_TRANSPARENCY));
    }

    /**
     * Returns the default domain encryption mode. The value depends on the platform version and on
     * the app target sdk level.
     */
    @Hide
    static int defaultDomainEncryptionMode() {
        return (CompatChanges.isChangeEnabled(ENABLE_DEFAULT_ENCRYPTED_CLIENT_HELLO)
                && encryptedClientHelloPlatform())
                ? DOMAIN_ENCRYPTION_MODE_OPPORTUNISTIC
                : DOMAIN_ENCRYPTION_MODE_DISABLED;
    }

    @Hide
    public static NetworkSecurityConfig.Builder createBaseConfigBuilder(ApplicationInfo info) {
        // System certificate store, does not bypass static pins, does not disable CT.
        CertificatesEntryRef systemRef =
                new CertificatesEntryRef(SystemCertificateSource.getInstance(), false, false);
        Builder builder = new Builder().addCertificatesEntryRef(systemRef);
        final boolean cleartextTrafficPermitted =
                info.targetSdkVersion < Build.VERSION_CODES.P && !info.isInstantApp();
        builder.setCleartextTrafficPermitted(cleartextTrafficPermitted);
        // Applications targeting N and above must opt in into trusting the user added
        // certificate store.
        if (info.targetSdkVersion <= Build.VERSION_CODES.M && !info.isPrivilegedApp()) {
            // User certificate store, does not bypass static pins. CT is disabled.
            builder.addCertificatesEntryRef(
                    new CertificatesEntryRef(UserCertificateSource.getInstance(), false, true));
        }
        return builder;
    }

    /**
     * Returns a base {@link NetworkSecurityConfig} using the provided app's defaults.
     *
     * <p>An app default configuration has the following properties:
     *
     * <ol>
     *   <li>If the application targets API level 27 (Android O MR1) or lower, cleartext traffic
     * is allowed by default. <li>Cleartext traffic is not permitted for ephemeral apps. <li>No
     * certificate pinning is used. <li>The system certificate store is trusted for connections.
     *   <li>If the application targets API level 23 (Android M) or lower than the user
     * certificate store is trusted by default as well for non-privileged applications.
     *   <li>Privileged applications do not trust the user certificate store on Android P and
     *       higher.
     * </ol>
     *
     * @param context of the application. Must not be {@code null}.
     * @return the application {@link NetworkSecurityConfig}, or {@code null} if the config cannot
     *         be found.
     */
    @Nullable
    public static NetworkSecurityConfig createBaseConfig(@NonNull Context context) {
        Objects.requireNonNull(context);
        ApplicationInfo info = context.getApplicationInfo();
        return info != null ? createBaseConfigBuilder(info).build() : null;
    }

    /**
     * Return a {@link Builder} for localhost.
     */
    @Hide
    public static Builder createLocalhostConfigBuilder() {
        return new Builder()
                .setCleartextTrafficPermitted(true)
                .setCertificateTransparencyVerificationRequired(false);
    }

    /**
     * Returns a {@link NetworkSecurityConfig} for localhost.
     *
     * <p>The localhost configuration has the following properties:
     *
     * <ol>
     *   <li>Cleartext traffic is permitted.
     *   <li>Certificate Transparency is disabled.
     * </ol>
     *
     * @return the {@link NetworkSecurityConfig}.
     */
    @NonNull
    public static NetworkSecurityConfig createLocalhostConfig() {
        return createLocalhostConfigBuilder().build();
    }

    /**
     * Builder for creating {@code NetworkSecurityConfig} objects.
     */
    @FlaggedApi(com.android.org.conscrypt.net.flags.Flags.FLAG_DYNAMIC_NETWORK_SECURITY_POLICY)
    public static final class Builder {
        private List<CertificatesEntryRef> mCertificatesEntryRefs;
        private PinSet mPinSet;
        private boolean mCleartextTrafficPermitted = DEFAULT_CLEARTEXT_TRAFFIC_PERMITTED;
        private boolean mCleartextTrafficPermittedSet = false;
        private boolean mCertificateTransparencyVerificationRequired =
                certificateTransparencyVerificationRequiredDefault();
        private boolean mCertificateTransparencyVerificationRequiredSet = false;
        private int mDomainEncryptionMode = defaultDomainEncryptionMode();
        private boolean mDomainEncryptionModeSet = false;
        private Builder mParentBuilder;

        /**
         * Creates a new, empty Builder.
         */
        public Builder() {}

        /**
         * Creates a new Builder and initializes its values according to the provided {@link
         * NetworkSecurityConfig}.
         *
         * @param config the {@link NetworkSecurityConfig} to use when initializing this builder.
         */
        public Builder(@NonNull NetworkSecurityConfig config) {
            addCertificatesEntryRefs(config.mCertificatesEntryRefs);
            setPinSet(config.mPins);
            setCleartextTrafficPermitted(config.mCleartextTrafficPermitted);
            setCertificateTransparencyVerificationRequired(
                    config.mCertificateTransparencyVerificationRequired);
            setDomainEncryptionMode(config.mDomainEncryptionMode);
        }

        /**
         * Sets the parent {@code Builder} for this {@code Builder}.
         * The parent will be used to determine values not configured in this {@code Builder}
         * in {@link Builder#build()}, recursively if needed.
         */
        @Hide
        public Builder setParent(Builder parent) {
            // Quick check to avoid adding loops.
            Builder current = parent;
            while (current != null) {
                if (current == this) {
                    throw new IllegalArgumentException("Loops are not allowed in Builder parents");
                }
                current = current.getParent();
            }
            mParentBuilder = parent;
            return this;
        }

        @Hide
        public Builder getParent() {
            return mParentBuilder;
        }

        @Hide
        public Builder setPinSet(PinSet pinSet) {
            mPinSet = pinSet;
            return this;
        }

        private PinSet getEffectivePinSet() {
            if (mPinSet != null) {
                return mPinSet;
            }
            if (mParentBuilder != null) {
                return mParentBuilder.getEffectivePinSet();
            }
            return PinSet.EMPTY_PINSET;
        }

        /**
         * Specifies whether cleartext traffic is permitted.
         *
         * @param cleartextTrafficPermitted whether cleartext traffic is permitted.
         * @return the builder.
         */
        @NonNull
        public Builder setCleartextTrafficPermitted(boolean cleartextTrafficPermitted) {
            mCleartextTrafficPermitted = cleartextTrafficPermitted;
            mCleartextTrafficPermittedSet = true;
            return this;
        }

        private boolean getEffectiveCleartextTrafficPermitted() {
            if (mCleartextTrafficPermittedSet) {
                return mCleartextTrafficPermitted;
            }
            if (mParentBuilder != null) {
                return mParentBuilder.getEffectiveCleartextTrafficPermitted();
            }
            return DEFAULT_CLEARTEXT_TRAFFIC_PERMITTED;
        }

        @Hide
        public Builder addCertificatesEntryRef(CertificatesEntryRef ref) {
            if (mCertificatesEntryRefs == null) {
                mCertificatesEntryRefs = new ArrayList<CertificatesEntryRef>();
            }
            mCertificatesEntryRefs.add(ref);
            return this;
        }

        @Hide
        public Builder addCertificatesEntryRefs(Collection<? extends CertificatesEntryRef> refs) {
            if (mCertificatesEntryRefs == null) {
                mCertificatesEntryRefs = new ArrayList<CertificatesEntryRef>();
            }
            mCertificatesEntryRefs.addAll(refs);
            return this;
        }

        private List<CertificatesEntryRef> getEffectiveCertificatesEntryRefs() {
            if (mCertificatesEntryRefs != null) {
                return mCertificatesEntryRefs;
            }
            if (mParentBuilder != null) {
                return mParentBuilder.getEffectiveCertificatesEntryRefs();
            }
            return Collections.<CertificatesEntryRef>emptyList();
        }

        @Hide
        public boolean hasCertificatesEntryRefs() {
            return mCertificatesEntryRefs != null;
        }

        List<CertificatesEntryRef> getCertificatesEntryRefs() {
            return mCertificatesEntryRefs;
        }

        /**
         * Specifies whether certificate transparency verification is required.
         *
         * @param required whether certificate transparency verification is required
         * @return the builder.
         */
        @NonNull
        public Builder setCertificateTransparencyVerificationRequired(boolean required) {
            mCertificateTransparencyVerificationRequired = required;
            mCertificateTransparencyVerificationRequiredSet = true;
            return this;
        }

        private boolean getCertificateTransparencyVerificationRequired() {
            if (mCertificateTransparencyVerificationRequiredSet) {
                return mCertificateTransparencyVerificationRequired;
            }
            // CT verification has not been set explicitly. Before deferring to
            // the parent, check if any of the CertificatesEntryRef requires it
            // to be disabled (i.e., user store or inline certificate).
            if (hasCertificatesEntryRefs()) {
                for (CertificatesEntryRef ref : getCertificatesEntryRefs()) {
                    if (ref.disableCT()) {
                        return false;
                    }
                }
            }
            if (mParentBuilder != null) {
                return mParentBuilder.getCertificateTransparencyVerificationRequired();
            }
            return certificateTransparencyVerificationRequiredDefault();
        }

        /**
         * Specifies the domain encryption mode.
         *
         * @param mode the domain encryption mode. For possible values see {@link
         *     NetworkSecurityPolicy#getDomainEncryptionMode(String)}.
         * @return the builder.
         */
        @NonNull
        public Builder setDomainEncryptionMode(@NetworkSecurityPolicy.DomainEncryptionMode
                                               int mode) {
            mDomainEncryptionMode = mode;
            mDomainEncryptionModeSet = true;
            return this;
        }

        Builder setDomainEncryptionMode(String domainEncryptionValue) {
            return setDomainEncryptionMode(switch (domainEncryptionValue) {
                case "disabled" -> DOMAIN_ENCRYPTION_MODE_DISABLED;
                case "required" -> DOMAIN_ENCRYPTION_MODE_REQUIRED;
                case "enabled" -> DOMAIN_ENCRYPTION_MODE_ENABLED;
                case "opportunistic" -> DOMAIN_ENCRYPTION_MODE_OPPORTUNISTIC;
                default -> defaultDomainEncryptionMode();
            });
        }

        /**
         * Corresponds to the IntDef defined in
         * {@link NetworkSecurityPolicy.DomainEncryptionMode}.
         */
        private int getDomainEncryptionMode() {
            if (mDomainEncryptionModeSet) {
                return mDomainEncryptionMode;
            }

            if (mParentBuilder != null) {
                return mParentBuilder.getDomainEncryptionMode();
            }

            return defaultDomainEncryptionMode();
        }

        /**
         * Creates a {@link NetworkSecurityConfig} with the arguments provided to this builder.
         *
         * @return a {@link NetworkSecurityConfig}
         */
        @NonNull
        public NetworkSecurityConfig build() {
            boolean cleartextPermitted = getEffectiveCleartextTrafficPermitted();
            boolean certificateTransparencyVerificationRequired =
                    getCertificateTransparencyVerificationRequired();
            int domainEncryptionMode = getDomainEncryptionMode();
            PinSet pinSet = getEffectivePinSet();
            List<CertificatesEntryRef> entryRefs = getEffectiveCertificatesEntryRefs();
            return new NetworkSecurityConfig(cleartextPermitted,
                                             certificateTransparencyVerificationRequired,
                                             domainEncryptionMode, pinSet, entryRefs);
        }
    }
}
