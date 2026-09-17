/*
 * Copyright (C) 2024 The Android Open Source Project
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

package android.content.pm;

import static android.permission.flags.Flags.FLAG_ALLOWLIST_V2_CHANGES_ENABLED;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.app.appfunctions.flags.Flags;
import android.os.BadParcelableException;
import android.os.Parcel;
import android.os.Parcelable;

import com.android.internal.annotations.VisibleForTesting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;

/**
 * A data class representing a package and an optional SHA-256 hash of its signing certificate
 * (history).
 */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_APP_FUNCTION_PERMISSION_V2)
public final class SignedPackage implements Parcelable {

    private static final String WILDCARD_PACKAGE_NAME = "*";
    @VisibleForTesting
    static final int MAX_CERTIFICATE_HISTORY_SIZE = 128;

    private final boolean mWildcardPackageName;

    @Nullable
    private final String mPackageName;
    @Nullable
    private final List<byte[]> mCertificateDigestHistory;

    /**
     * Create a new instance of SignedPackage.
     *
     * @param packageName The name of the package. Starting in {@link
     *                    android.os.Build.VERSION_CODES_FULL#CINNAMON_BUN}, this constructor
     *                    can take a {@code *} value which indicates this instance represents any
     *                    package. This will mainly be used with filtering logic and represents that
     *                    all packages will match a certain filter criteria.
     * @param certificateDigest The sha-256 hash of the package's signing certificate, or null if
     *                          none
     */
    public SignedPackage(@NonNull String packageName, @Nullable byte[] certificateDigest) {
        this(packageName, certificateDigest != null ? List.of(certificateDigest) : null);
    }

    /**
     * Create a new instance of {@link SignedPackage} with signing certificate history.
     *
     * @param packageName The name of the package. Starting in {@link
     *                    android.os.Build.VERSION_CODES_FULL#CINNAMON_BUN}, this method
     *                    can take a {@code *} value which indicates this instance represents any
     *                    package.
     * @param certificateDigestHistory The certificate history. Can be null to indicate
     *                                 that the package should be matched by name only,
     *                                 without requiring a specific signing certificate.
     *                                 The history should be ordered from oldest to newest.
     * @return a new {@link SignedPackage} instance.
     */
    @NonNull
    @FlaggedApi(FLAG_ALLOWLIST_V2_CHANGES_ENABLED)
    public static SignedPackage createWithCertificateDigestHistory(
            @NonNull String packageName,
            @Nullable List<byte[]> certificateDigestHistory) {
        if (certificateDigestHistory != null) {
            if (certificateDigestHistory.isEmpty()) {
                throw new IllegalArgumentException(
                        "certificateDigestHistory cannot be empty");
            }
            if (certificateDigestHistory.size() > MAX_CERTIFICATE_HISTORY_SIZE) {
                throw new IllegalArgumentException(
                        "certificateDigestHistory size exceeds limit: "
                                + certificateDigestHistory.size());
            }
            for (byte[] digest : certificateDigestHistory) {
                if (digest == null) {
                    throw new IllegalArgumentException(
                        "certificateDigestHistory cannot contain null digests");
                }
            }
        }
        return new SignedPackage(packageName, certificateDigestHistory);
    }

    private SignedPackage(@NonNull String packageName,
            @Nullable List<byte[]> certificateDigestHistory) {
        mWildcardPackageName = WILDCARD_PACKAGE_NAME.equals(packageName);
        // TODO(b/516888899): validate package name
        mPackageName = mWildcardPackageName ? null : packageName;
        mCertificateDigestHistory = certificateDigestHistory;
    }

    @Hide
    public SignedPackage(@NonNull Parcel data) {
        String packageName = Objects.requireNonNull(data.readString8());
        mWildcardPackageName = WILDCARD_PACKAGE_NAME.equals(packageName);
        mPackageName = mWildcardPackageName ? null : packageName;
        mCertificateDigestHistory = readCertificateHistory(data);
    }

    @Nullable
    private static List<byte[]> readCertificateHistory(@NonNull Parcel data) {
        int size = data.readInt();
        if (size < 0) {
            return null;
        }
        if (size == 0) {
            throw new BadParcelableException("Certificate history size cannot be 0");
        }
        if (size > MAX_CERTIFICATE_HISTORY_SIZE) {
            throw new BadParcelableException("Certificate history size exceeds limit: " + size);
        }
        List<byte[]> history = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            byte[] digest = data.createByteArray();
            if (digest == null) {
                throw new BadParcelableException("Certificate digest at index " + i + " is null");
            }
            history.add(digest);
        }
        return history;
    }

    @Hide
    public SignedPackage(@NonNull SignedPackageParcel data) {
        mWildcardPackageName = WILDCARD_PACKAGE_NAME.equals(data.packageName);
        mPackageName = mWildcardPackageName ? null : data.packageName;
        mCertificateDigestHistory = data.certificateDigest != null
                ? List.of(data.certificateDigest) : null;
    }

    /**
     * Converts this instance to a legacy {@link SignedPackageParcel}.
     * <p>
     * Note: {@link SignedPackageParcel} only supports a single certificate digest.
     * If this package has a certificate history, only the most recent digest
     * (the last element in the history) will be preserved in the resulting parcel.
     *
     * @return a legacy parcel representation of this signed package.
     */
    @Hide
    public SignedPackageParcel toSignedPackageParcel() {
        SignedPackageParcel parcel = new SignedPackageParcel();
        parcel.packageName = mWildcardPackageName ? WILDCARD_PACKAGE_NAME : mPackageName;
        parcel.certificateDigest = hasCertificateDigest() ? getCertificateDigest() : null;
        return parcel;
    }

    public static final @NonNull Creator<SignedPackage> CREATOR = new Creator<>() {
        @Override
        public SignedPackage createFromParcel(Parcel in) {
            return new SignedPackage(in);
        }

        @Override
        public SignedPackage[] newArray(int size) {
            return new SignedPackage[size];
        }
    };

    /**
     * Get the package name. If this instance was constructed with a wildcard package name {@code
     * *}, the method will throw an {@link IllegalStateException}.
     */
    public @NonNull String getPackageName() {
        if (isWildcardPackageName()) {
            throw new IllegalStateException(
                    "Cannot get package name from a SignedPackage with wildcard package name");
        }
        return mPackageName;
    }

    /**
     * @return the newest certificate digest. If none was provided, the method will throw a {@link
     * NullPointerException}.
     */
    public @NonNull byte[] getCertificateDigest() {
        if (mCertificateDigestHistory == null) {
            throw new NullPointerException("No certificate digest available");
        }
        return mCertificateDigestHistory.get(mCertificateDigestHistory.size() - 1);
    }

    /** @return true if this SignedPackage has a certificate attached, false otherwise */
    public boolean hasCertificateDigest() {
        return mCertificateDigestHistory != null;
    }

    // TODO(b/519283854): Make System API in 26Q4.

    @Hide
    public boolean isWildcardPackageName() {
        return mWildcardPackageName;
    }

    /**
     * Returns the list of certificate digests representing the package's signing certificate
     * history, including the latest certificate digest. The list is ordered from oldest to newest.
     *
     * @return the certificate digest history, or null if the package should be matched by name
     *         only, without requiring a specific signing certificate.
     */
    @Nullable
    @SuppressLint("NullableCollection")
    @FlaggedApi(FLAG_ALLOWLIST_V2_CHANGES_ENABLED)
    public List<byte[]> getCertificateDigestHistory() {
        return mCertificateDigestHistory;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SignedPackage that)) return false;
        return Objects.equals(mPackageName, that.mPackageName)
                && byteArrayListEquals(mCertificateDigestHistory, that.mCertificateDigestHistory);
    }

    private static boolean byteArrayListEquals(
            List<byte[]> list1, List<byte[]> list2) {
        if (list1 == list2) return true;
        if (list1 == null || list2 == null) return false;
        if (list1.size() != list2.size()) return false;
        for (int i = 0; i < list1.size(); i++) {
            if (!Arrays.equals(list1.get(i), list2.get(i))) return false;
        }
        return true;
    }

    private static int byteArrayListHashCode(@Nullable List<byte[]> list) {
        if (list == null) return 0;
        int hash = 0;
        for (byte[] digest : list) {
            hash = 31 * hash + Arrays.hashCode(digest);
        }
        return hash;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mPackageName, byteArrayListHashCode(mCertificateDigestHistory));
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("SignedPackage{packageName=").append(mPackageName)
                .append(", certificateDigestHistory=");
        if (mCertificateDigestHistory == null) {
            sb.append("null");
        } else {
            sb.append("[");
            for (int i = 0; i < mCertificateDigestHistory.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(HexFormat.of().formatHex(mCertificateDigestHistory.get(i)));
            }
            sb.append("]");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString8(mWildcardPackageName ? WILDCARD_PACKAGE_NAME : mPackageName);
        if (mCertificateDigestHistory == null) {
            dest.writeInt(-1);
        } else {
            dest.writeInt(mCertificateDigestHistory.size());
            for (byte[] digest : mCertificateDigestHistory) {
                dest.writeByteArray(digest);
            }
        }
    }
}
