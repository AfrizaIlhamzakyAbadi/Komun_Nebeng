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

package android.health.connect.common;

import static android.health.connect.HealthPermissions.READ_HEALTH_DATA_HISTORY;
import static android.health.connect.HealthPermissions.READ_HEALTH_DATA_IN_BACKGROUND;
import static android.health.connect.HealthPermissions.WRITE_MEDICAL_DATA;

import android.annotation.Hide;
import android.annotation.Nullable;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.health.connect.HealthConnectManager;
import android.health.connect.HealthPermissions;
import android.health.connect.internal.datatypes.utils.HealthConnectMappings;
import android.os.Build;
import android.util.ArraySet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Utility class for health permissions. */
@Hide
public final class PermissionUtils {
    private PermissionUtils() {}

    /**
     * Returns a set of health permissions requested by the given package.
     *
     * @param context The context to use for retrieving health permissions.
     * @param packageInfo The PackageInfo of the package.
     * @param includeImplicit Whether to include implicit permissions.
     */
    public static Set<String> getRequestedHealthPermissions(
            Context context, PackageInfo packageInfo, boolean includeImplicit) {
        if (packageInfo.requestedPermissions == null
                || (!includeImplicit && packageInfo.requestedPermissionsFlags == null)) {
            return new ArraySet<>();
        }

        Set<String> healthPermissions = HealthConnectManager.getHealthPermissions(context);
        Set<String> declaredPermissions = new ArraySet<>();

        for (int i = 0; i < packageInfo.requestedPermissions.length; i++) {
            if (healthPermissions.contains(packageInfo.requestedPermissions[i])
                    && (includeImplicit
                            || isExplicitlyRequested(packageInfo.requestedPermissionsFlags[i]))) {
                declaredPermissions.add(packageInfo.requestedPermissions[i]);
            }
        }

        return declaredPermissions;
    }

    /**
     * Similar to {@link #getValidRequestedHealthPermissions(Context, PackageInfo, boolean,
     * boolean)} with {@code filterSystemPermissions} set to {@code false}.
     */
    public static Set<String> getValidRequestedHealthPermissions(
            Context context, PackageInfo packageInfo, boolean includeImplicit) {
        return getValidRequestedHealthPermissions(
                context, packageInfo, includeImplicit, /* filterSystemPermissions= */ false);
    }

    /**
     * Returns the set of valid requested health permissions for the package, optionally filtering
     * out permissions that are not allowed system permissions.
     *
     * <p>Filters out invalid additional and normal permissions (i.e. those that would not make
     * sense to display in the frontend). See {@link #filterValidRequestedHealthPermissions} for
     * details.
     *
     * @param context The context to use.
     * @param packageInfo The PackageInfo of the package.
     * @param includeImplicit Whether to include implicit permissions.
     * @param filterSystemPermissions Whether to only retain allowed system permissions.
     */
    public static Set<String> getValidRequestedHealthPermissions(
            Context context,
            PackageInfo packageInfo,
            boolean includeImplicit,
            boolean filterSystemPermissions) {
        Set<String> requestedHealthPermissions =
                getRequestedHealthPermissions(context, packageInfo, includeImplicit);

        if (filterSystemPermissions) {
            Set<String> allowedPermissions =
                    new ArraySet<>(HealthConnectManager.getSystemHealthPermissions(context));
            allowedPermissions.add(READ_HEALTH_DATA_IN_BACKGROUND);
            requestedHealthPermissions.retainAll(allowedPermissions);
        }

        HealthConnectMappings mappings = HealthConnectMappings.getInstance();
        Set<String> medicalPermissions = HealthPermissions.getAllMedicalPermissions();
        Set<String> normalPermissions = HealthPermissions.getNormalHealthPermissions();

        return filterValidRequestedHealthPermissions(
                mappings, requestedHealthPermissions, normalPermissions, medicalPermissions);
    }

    /**
     * Similar to {@link #getValidRequestedHealthPermissionsForUid(PackageManager, String, Context,
     * boolean)} with {@code filterSystemPermissions} set to {@code false}.
     */
    public static Set<String> getValidRequestedHealthPermissionsForUid(
            PackageManager packageManager, String packageName, Context context) {
        return getValidRequestedHealthPermissionsForUid(
                packageManager, packageName, context, /* filterSystemPermissions= */ false);
    }

    /**
     * Returns the union of all valid requested health permissions for the packages in the same UID
     * group, optionally filtering out permissions that are not allowed system permissions.
     *
     * <p>Filters out invalid additional and normal permissions (i.e. those that would not make
     * sense to display in the frontend). See {@link #filterValidRequestedHealthPermissions} for
     * details.
     *
     * @param packageManager The PackageManager instance to use.
     * @param packageName The name of the package.
     * @param context The context to use.
     * @param filterSystemPermissions Whether to only retain allowed system permissions.
     */
    public static Set<String> getValidRequestedHealthPermissionsForUid(
            PackageManager packageManager,
            String packageName,
            Context context,
            boolean filterSystemPermissions) {
        List<String> rawPermissions =
                getDeclaredHealthPermissionsForUid(packageManager, packageName, context);

        Set<String> requestedHealthPermissions = new ArraySet<>(rawPermissions);
        if (filterSystemPermissions) {
            Set<String> allowedPermissions =
                    new ArraySet<>(HealthConnectManager.getSystemHealthPermissions(context));
            allowedPermissions.add(READ_HEALTH_DATA_IN_BACKGROUND);
            requestedHealthPermissions.retainAll(allowedPermissions);
        }

        HealthConnectMappings mappings = HealthConnectMappings.getInstance();
        Set<String> medicalPermissions = HealthPermissions.getAllMedicalPermissions();
        Set<String> normalPermissions = HealthPermissions.getNormalHealthPermissions();

        return filterValidRequestedHealthPermissions(
                mappings, requestedHealthPermissions, normalPermissions, medicalPermissions);
    }

    /** Returns true if the given package explicitly requests health permissions in its manifest. */
    public static boolean isPackageRequestingHealthPermissions(
            Context context, PackageInfo info, boolean includeImplicit) {
        Set<String> healthPermissions = HealthConnectManager.getHealthPermissions(context);
        return hasRequestedFromPermissions(info, healthPermissions, includeImplicit);
    }

    /** Returns true if the given package explicitly requests health permissions in its manifest. */
    public static boolean isPackageRequestingHealthPermissions(
            PackageManager packageManager, String packageName, Context context) {
        try {
            PackageInfo info =
                    packageManager.getPackageInfo(
                            packageName,
                            PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS));
            return isPackageRequestingHealthPermissions(
                    context, info, /* includeImplicit= */ false);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /** Returns true if the given package info requests any of the provided permissions. */
    @Hide
    public static boolean hasRequestedFromPermissions(
            PackageInfo info, Set<String> permissions, boolean includeImplicit) {
        if (info.requestedPermissions == null || info.requestedPermissionsFlags == null) {
            return false;
        }
        for (int i = 0; i < info.requestedPermissions.length; i++) {
            if (permissions.contains(info.requestedPermissions[i])
                    && (includeImplicit
                            || isExplicitlyRequested(info.requestedPermissionsFlags[i]))) {
                return true;
            }
        }
        return false;
    }

    private static boolean isExplicitlyRequested(int flags) {
        return (flags & PackageInfo.REQUESTED_PERMISSION_IMPLICIT) == 0;
    }

    /**
     * Returns an Intent with action Intent.ACTION_VIEW_PERMISSION_USAGE and category
     * HealthConnectManager.CATEGORY_HEALTH_PERMISSIONS.
     *
     * @param packageName Optional package name to set on the intent.
     */
    public static Intent getHealthPermissionsUsageIntent(@Nullable String packageName) {
        Intent intent = new Intent(Intent.ACTION_VIEW_PERMISSION_USAGE);
        intent.addCategory(HealthConnectManager.CATEGORY_HEALTH_PERMISSIONS);
        if (packageName != null) {
            intent.setPackage(packageName);
        }
        return intent;
    }

    /**
     * Returns true if the given package supports the health permissions usage intent.
     *
     * @param packageManager The PackageManager to use for querying.
     * @param packageName The package name to check.
     * @param flags The flags to use for querying.
     */
    public static boolean hasHealthPermissionsUsageIntent(
            PackageManager packageManager, String packageName, long flags) {
        Intent intent = getHealthPermissionsUsageIntent(packageName);
        return !packageManager
                .queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(flags))
                .isEmpty();
    }

    /**
     * Returns the package name of the first package in the UID group that supports the {@link
     * Intent#ACTION_VIEW_PERMISSION_USAGE} intent with category {@link
     * HealthConnectManager#CATEGORY_HEALTH_PERMISSIONS}, or null if none do.
     */
    @Nullable
    public static String getPackageDeclaringUsageIntentInUid(
            PackageManager packageManager, String packageName, long flags) {
        String[] packagesInUid = getPackagesWithSharedUid(packageManager, packageName);
        for (String pkg : packagesInUid) {
            if (hasHealthPermissionsUsageIntent(packageManager, pkg, flags)) {
                return pkg;
            }
        }
        return null;
    }

    /**
     * Returns true if all packages in the UID group support the health permissions usage intent.
     */
    public static boolean uidHasHealthPermissionsUsageIntent(
            PackageManager packageManager, String packageName, Context context, long flags) {
        String[] packagesInUid = getPackagesWithSharedUid(packageManager, packageName);
        if (packagesInUid.length == 0) {
            return false;
        }
        for (String pkg : packagesInUid) {
            if (!hasHealthPermissionsUsageIntent(packageManager, pkg, flags)) {
                return false;
            }
        }
        return true;
    }

    /** Returns all packages sharing the same UID as the provided package. */
    public static String[] getPackagesWithSharedUid(
            PackageManager packageManager, String packageName) {
        try {
            int uid =
                    packageManager.getPackageUid(
                            packageName, PackageManager.PackageInfoFlags.of(0));
            String[] packages = packageManager.getPackagesForUid(uid);
            return (packages != null) ? packages : new String[] {packageName};
        } catch (PackageManager.NameNotFoundException e) {
            return new String[0];
        }
    }

    /** Returns the union of all health permissions declared by any package in the same UID. */
    public static List<String> getDeclaredHealthPermissionsForUid(
            PackageManager packageManager, String packageName, Context context) {
        String[] packagesInUid = getPackagesWithSharedUid(packageManager, packageName);
        Set<String> healthPermissions = HealthConnectManager.getHealthPermissions(context);
        Set<String> allDeclared = new ArraySet<>();

        for (String pkg : packagesInUid) {
            try {
                PackageInfo info =
                        packageManager.getPackageInfo(
                                pkg,
                                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS));
                if (info != null && info.requestedPermissions != null) {
                    for (String perm : info.requestedPermissions) {
                        if (healthPermissions.contains(perm)) {
                            allDeclared.add(perm);
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                // Skip this package
            }
        }
        return new ArrayList<>(allDeclared);
    }

    /** Returns the union of all granted health permissions for any package in the same UID. */
    public static List<String> getGrantedHealthPermissionsForUid(
            PackageManager packageManager, String packageName, Context context) {
        String[] packagesInUid = getPackagesWithSharedUid(packageManager, packageName);
        Set<String> healthPermissions = HealthConnectManager.getHealthPermissions(context);
        Set<String> allGranted = new ArraySet<>();

        for (String pkg : packagesInUid) {
            try {
                PackageInfo info =
                        packageManager.getPackageInfo(
                                pkg,
                                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS));
                if (info != null
                        && info.requestedPermissions != null
                        && info.requestedPermissionsFlags != null) {
                    for (int i = 0; i < info.requestedPermissions.length; i++) {
                        String perm = info.requestedPermissions[i];
                        if (healthPermissions.contains(perm)
                                && (info.requestedPermissionsFlags[i]
                                                & PackageInfo.REQUESTED_PERMISSION_GRANTED)
                                        != 0) {
                            allGranted.add(perm);
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                // Skip this package
            }
        }
        return new ArrayList<>(allGranted);
    }

    /**
     * Returns a mapping from UID to the union of health permissions declared by all packages
     * sharing that UID.
     *
     * @param packages The list of PackageInfo objects to process.
     * @param context The context to use for retrieving health permissions.
     * @return A map where keys are UIDs and values are sets of health permissions.
     */
    public static Map<Integer, Set<String>> getUidToPermissionsMap(
            List<PackageInfo> packages, Context context) {
        Set<String> healthPermissions = HealthConnectManager.getHealthPermissions(context);
        Map<Integer, Set<String>> uidToPermissions = new HashMap<>();
        for (PackageInfo info : packages) {
            if (info.applicationInfo == null) {
                // Cannot aggregate by UID if ApplicationInfo is missing.
                continue;
            }
            int uid = info.applicationInfo.uid;
            Set<String> declaredForUid =
                    uidToPermissions.computeIfAbsent(uid, k -> new ArraySet<>());
            if (info.requestedPermissions != null) {
                for (String perm : info.requestedPermissions) {
                    if (healthPermissions.contains(perm)) {
                        declaredForUid.add(perm);
                    }
                }
            }
        }
        return uidToPermissions;
    }

    /**
     * Finds a package within the same UID as the provided package that explicitly declares the
     * given permission in its manifest.
     *
     * <p>The provided {@code packageName} is checked first to maintain preference for the original
     * caller if it is already a valid declarer.
     *
     * @return The name of the declaring package, or the original packageName if no other declarer
     *     is found or an error occurs.
     */
    public static String getPackageDeclaringPermission(
            PackageManager packageManager, String packageName, String permission) {
        String[] packagesInUid = getPackagesWithSharedUid(packageManager, packageName);

        // Prefer the input package if it already declares the permission
        try {
            PackageInfo info =
                    packageManager.getPackageInfo(
                            packageName,
                            PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS));
            if (info.requestedPermissions != null) {
                for (String requestedPermission : info.requestedPermissions) {
                    if (requestedPermission.equals(permission)) {
                        return packageName;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            // Continue to search other packages in UID
        }

        for (String pkg : packagesInUid) {
            if (pkg.equals(packageName)) {
                continue;
            }
            try {
                PackageInfo info =
                        packageManager.getPackageInfo(
                                pkg,
                                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS));
                if (info.requestedPermissions != null) {
                    for (String requestedPermission : info.requestedPermissions) {
                        if (requestedPermission.equals(permission)) {
                            return pkg;
                        }
                    }
                }
            } catch (PackageManager.NameNotFoundException e) {
                // Continue to next package
            }
        }
        return packageName;
    }

    /**
     * Returns true if the given permission flag indicates that the permission is being requested by
     * an app targeting a version of Android that requires split permissions.
     */
    @Hide
    public static boolean isFromSplitPermission(int flags, int targetSdkVersion) {
        return (targetSdkVersion >= Build.VERSION_CODES.M)
                ? (flags & PackageManager.FLAG_PERMISSION_REVOKE_WHEN_REQUESTED) != 0
                : (flags & PackageManager.FLAG_PERMISSION_REVIEW_REQUIRED) != 0;
    }

    /**
     * Filter requested permissions of {@code info} to only contain health permissions, valid
     * additional permissions, and non-user-hidden permissions.
     *
     * <p>READ_HEALTH_DATA_HISTORY is valid if at least one FITNESS READ permission is declared.
     * READ_HEALTH_DATA_IN_BACKGROUND is valid if at least one HEALTH READ permission is declared.
     */
    public static Set<String> filterValidRequestedHealthPermissions(
            HealthConnectMappings healthConnectMappings,
            Collection<String> requestedHealthPermissions,
            Set<String> normalPermissions,
            Set<String> medicalPermissions) {
        ArraySet<String> validPermissions = new ArraySet<>();

        boolean atLeastOneFitnessReadDeclared = false;
        boolean atLeastOneMedicalReadDeclared = false;

        for (String requestedPermission : requestedHealthPermissions) {
            if (healthConnectMappings.isReadPermission(requestedPermission)) {
                atLeastOneFitnessReadDeclared = true;
            } else if (!Objects.equals(requestedPermission, WRITE_MEDICAL_DATA)
                    && medicalPermissions.contains(requestedPermission)) {
                atLeastOneMedicalReadDeclared = true;
            }

            if (atLeastOneFitnessReadDeclared && atLeastOneMedicalReadDeclared) {
                break;
            }
        }

        boolean atLeastOneHealthReadDeclared =
                atLeastOneFitnessReadDeclared || atLeastOneMedicalReadDeclared;

        for (String requestedPermission : requestedHealthPermissions) {
            if (requestedPermission.equals(READ_HEALTH_DATA_HISTORY)
                    && !atLeastOneFitnessReadDeclared) continue;
            if (requestedPermission.equals(READ_HEALTH_DATA_IN_BACKGROUND)
                    && !atLeastOneHealthReadDeclared) continue;
            if (normalPermissions.contains(requestedPermission)) continue;

            validPermissions.add(requestedPermission);
        }

        return validPermissions;
    }
}
