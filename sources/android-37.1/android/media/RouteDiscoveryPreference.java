/*
 * Copyright 2020 The Android Open Source Project
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

package android.media;

import static android.media.MediaRoute2Info.ROUTING_TYPE_REMOTE;

import static com.android.media.flags.Flags.FLAG_ENABLE_MIRRORING_IN_MEDIA_ROUTER_2;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.SystemApi;
import android.content.Context;
import android.media.MediaRoute2Info.RoutingType;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * A media route discovery preference describing the features of routes that media router
 * would like to discover and whether to perform active scanning.
 * <p>
 * When {@link MediaRouter2} instances set discovery preferences by calling
 * {@link MediaRouter2#registerRouteCallback}, they are merged into a single discovery preference
 * and it is delivered to call {@link MediaRoute2ProviderService#onDiscoveryPreferenceChanged}.
 * </p><p>
 * According to the given discovery preference, {@link MediaRoute2ProviderService} discovers
 * routes and publishes them.
 * </p>
 *
 * @see MediaRouter2#registerRouteCallback
 */
public final class RouteDiscoveryPreference implements Parcelable {
    @NonNull
    public static final Creator<RouteDiscoveryPreference> CREATOR =
            new Creator<RouteDiscoveryPreference>() {
                @Override
                public RouteDiscoveryPreference createFromParcel(Parcel in) {
                    return new RouteDiscoveryPreference(in);
                }

                @Override
                public RouteDiscoveryPreference[] newArray(int size) {
                    return new RouteDiscoveryPreference[size];
                }
            };

    @NonNull
    private final List<String> mPreferredFeatures;
    @NonNull private final Set<Integer> mRoutingTypes;
    @NonNull private final List<String> mPackageOrder;

    private final boolean mShouldPerformActiveScan;

    /**
     * An empty discovery preference.
     */
    @SystemApi
    public static final RouteDiscoveryPreference EMPTY =
            new Builder(Collections.emptyList(), false).build();

    RouteDiscoveryPreference(@NonNull Builder builder) {
        mPreferredFeatures = builder.mPreferredFeatures;
        mRoutingTypes = builder.mRoutingTypes;
        mPackageOrder = builder.mPackageOrder;
        mShouldPerformActiveScan = builder.mActiveScan;
    }

    RouteDiscoveryPreference(@NonNull Parcel in) {
        mPreferredFeatures = in.createStringArrayList();
        int[] routingTypesArray = in.createIntArray();
        var routingTypesSet = Arrays.stream(routingTypesArray).boxed().collect(Collectors.toSet());
        mRoutingTypes =
                MediaRoute2Info.validateRoutingTypes(routingTypesSet, /* allowEmpty= */ true);
        mPackageOrder = in.createStringArrayList();
        mShouldPerformActiveScan = in.readBoolean();
    }

    /**
     * Gets the features of routes that media router would like to discover.
     * <p>
     * Routes that have at least one of the features will be discovered.
     * They may include predefined features such as
     * {@link MediaRoute2Info#FEATURE_LIVE_AUDIO}, {@link MediaRoute2Info#FEATURE_LIVE_VIDEO},
     * or {@link MediaRoute2Info#FEATURE_REMOTE_PLAYBACK} or custom features defined by a provider.
     * </p>
     */
    @NonNull
    public List<String> getPreferredFeatures() {
        return mPreferredFeatures;
    }

    /**
     * Gets the {@link MediaRoute2Info#getSupportedRoutingTypes() routing types} for which there's a
     * scan request.
     *
     * <p>It is ok to {@link MediaRoute2ProviderService#notifyRoutes publish routes} with routing
     * types that are not in this set.
     *
     * <p>A discovered route must support at least one of the routing types in this set in order to
     * satisfy this discovery preference. It does not need to support all routing types.
     */
    @NonNull
    @FlaggedApi(FLAG_ENABLE_MIRRORING_IN_MEDIA_ROUTER_2)
    public Set<Integer> getRoutingTypes() {
        return mRoutingTypes;
    }

    /**
     * Gets the ordered list of package names used to remove duplicate routes.
     * <p>
     * Duplicate route removal is enabled if the returned list is non-empty. Routes are deduplicated
     * based on their {@link MediaRoute2Info#getDeduplicationIds() deduplication IDs}. If two routes
     * have a deduplication ID in common, only the route from the provider whose package name is
     * first in the provided list will remain.
     *
     * @see #shouldRemoveDuplicates()
     */
    @Hide
    @NonNull
    public List<String> getDeduplicationPackageOrder() {
        return mPackageOrder;
    }

    /**
     * Gets whether active scanning should be performed.
     * <p>
     * If any of discovery preferences sets this as {@code true}, active scanning will
     * be performed regardless of other discovery preferences.
     * </p>
     */
    public boolean shouldPerformActiveScan() {
        return mShouldPerformActiveScan;
    }

    /**
     * Gets whether duplicate routes removal is enabled.
     *
     * @see #getDeduplicationPackageOrder()
     */
    @Hide
    public boolean shouldRemoveDuplicates() {
        return !mPackageOrder.isEmpty();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeStringList(mPreferredFeatures);
        dest.writeIntArray(mRoutingTypes.stream().mapToInt(Integer::intValue).toArray());
        dest.writeStringList(mPackageOrder);
        dest.writeBoolean(mShouldPerformActiveScan);
    }

    /**
     * Dumps current state of the instance. Use with {@code dumpsys}.
     *
     * See {@link android.os.Binder#dump(FileDescriptor, PrintWriter, String[])}.
     */
    @Hide
    public void dump(@NonNull PrintWriter pw, @NonNull String prefix) {
        pw.println(prefix + "RouteDiscoveryPreference");

        String indent = prefix + "  ";

        pw.println(indent + "mShouldPerformActiveScan=" + mShouldPerformActiveScan);
        pw.println(indent + "mPreferredFeatures=" + mPreferredFeatures);
        pw.println(
                indent
                        + "mRoutingTypes="
                        + MediaRoute2Info.getSupportedRoutingTypesString(mRoutingTypes));
        pw.println(indent + "mPackageOrder=" + mPackageOrder);
    }

    @Override
    public String toString() {
        return "RouteDiscoveryRequest{ "
                + "preferredFeatures={"
                + String.join(", ", mPreferredFeatures)
                + "}"
                + ", routingTypes={"
                + MediaRoute2Info.getSupportedRoutingTypesString(mRoutingTypes)
                + "}"
                + ", activeScan="
                + mShouldPerformActiveScan
                + " }";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RouteDiscoveryPreference)) {
            return false;
        }
        RouteDiscoveryPreference other = (RouteDiscoveryPreference) o;
        return Objects.equals(mPreferredFeatures, other.mPreferredFeatures)
                && Objects.equals(mRoutingTypes, other.mRoutingTypes)
                && Objects.equals(mPackageOrder, other.mPackageOrder)
                && mShouldPerformActiveScan == other.mShouldPerformActiveScan;
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                mPreferredFeatures,
                mRoutingTypes,
                mPackageOrder,
                mShouldPerformActiveScan);
    }

    /**
     * Builder for {@link RouteDiscoveryPreference}.
     */
    public static final class Builder {
        List<String> mPreferredFeatures;
        Set<Integer> mRoutingTypes;
        List<String> mPackageOrder;
        boolean mActiveScan;

        public Builder(@NonNull List<String> preferredFeatures, boolean activeScan) {
            Objects.requireNonNull(preferredFeatures, "preferredFeatures must not be null");
            mPreferredFeatures = preferredFeatures.stream().filter(str -> !TextUtils.isEmpty(str))
                    .collect(Collectors.toList());
            mRoutingTypes = Set.of(ROUTING_TYPE_REMOTE);
            mPackageOrder = List.of();
            mActiveScan = activeScan;
        }

        public Builder(@NonNull RouteDiscoveryPreference preference) {
            Objects.requireNonNull(preference, "preference must not be null");

            mPreferredFeatures = preference.getPreferredFeatures();
            mRoutingTypes = preference.getRoutingTypes();
            mPackageOrder = preference.getDeduplicationPackageOrder();
            mActiveScan = preference.shouldPerformActiveScan();
        }

        /**
         * A constructor to combine multiple preferences into a single preference.
         * It ignores extras of preferences.
         */
        @Hide
        public Builder(@NonNull Collection<RouteDiscoveryPreference> preferences) {
            Objects.requireNonNull(preferences, "preferences must not be null");

            Set<String> preferredFeatures = new HashSet<>();
            Set<Integer> preferredRoutingTypes = new HashSet<>();
            mPackageOrder = List.of();
            boolean activeScan = false;
            for (RouteDiscoveryPreference preference : preferences) {
                preferredFeatures.addAll(preference.mPreferredFeatures);
                preferredRoutingTypes.addAll(preference.mRoutingTypes);

                activeScan |= preference.mShouldPerformActiveScan;
                // Choose one of either
                if (mPackageOrder.isEmpty() && !preference.mPackageOrder.isEmpty()) {
                    mPackageOrder = List.copyOf(preference.mPackageOrder);
                }
            }
            mPreferredFeatures = List.copyOf(preferredFeatures);
            mRoutingTypes = Set.copyOf(preferredRoutingTypes);
            mActiveScan = activeScan;
        }

        /**
         * Sets preferred route features to discover.
         * @param preferredFeatures features of routes that media router would like to discover.
         *                          May include predefined features
         *                          such as {@link MediaRoute2Info#FEATURE_LIVE_AUDIO},
         *                          {@link MediaRoute2Info#FEATURE_LIVE_VIDEO},
         *                          or {@link MediaRoute2Info#FEATURE_REMOTE_PLAYBACK}
         *                          or custom features defined by a provider.
         */
        @NonNull
        public Builder setPreferredFeatures(@NonNull List<String> preferredFeatures) {
            Objects.requireNonNull(preferredFeatures, "preferredFeatures must not be null");
            mPreferredFeatures = preferredFeatures.stream().filter(str -> !TextUtils.isEmpty(str))
                    .collect(Collectors.toList());
            return this;
        }

        /**
         * Sets the {@link MediaRoute2Info#getSupportedRoutingTypes() routing types} to discover.
         *
         * <p>The default value is a singleton set with {@link MediaRoute2Info#ROUTING_TYPE_REMOTE}.
         *
         * <p>Routing types other than {@link MediaRoute2Info#ROUTING_TYPE_REMOTE} passed to {@link
         * MediaRouter2#registerRouteCallback(Executor, MediaRouter2.RouteCallback,
         * RouteDiscoveryPreference)} will be ignored. Scanning for other routing types requires
         * using {@link MediaRouter2.ScanRequest#getRoutingTypes() a privileged scan request}, which
         * is reserved for {@link MediaRouter2#getInstance(Context, String, Executor, Runnable)
         * proxy routers}.
         *
         * @see #getRoutingTypes()
         */
        @NonNull
        @FlaggedApi(FLAG_ENABLE_MIRRORING_IN_MEDIA_ROUTER_2)
        public Builder setRoutingTypes(@NonNull Set<@RoutingType Integer> routingTypes) {
            Objects.requireNonNull(routingTypes, "routingTypes must not be null");
            mRoutingTypes =
                    Set.copyOf(
                            MediaRoute2Info.validateRoutingTypes(
                                    routingTypes, /* allowEmpty= */ true));
            return this;
        }

        /**
         * Sets if active scanning should be performed.
         * <p>
         * Since active scanning uses more system resources, set this as {@code true} only
         * when it's necessary.
         * </p>
         */
        @NonNull
        public Builder setShouldPerformActiveScan(boolean activeScan) {
            mActiveScan = activeScan;
            return this;
        }

        /**
         * Sets the order of packages to use when removing duplicate routes.
         * <p>
         * Routes are deduplicated based on their
         * {@link MediaRoute2Info#getDeduplicationIds() deduplication IDs}.
         * If two routes have a deduplication ID in common, only the route from the provider whose
         * package name is first in the provided list will remain.
         *
         * @param packageOrder ordered list of package names used to remove duplicate routes, or an
         *                     empty list if deduplication should not be enabled.
         */
        @Hide
        @NonNull
        public Builder setDeduplicationPackageOrder(@NonNull List<String> packageOrder) {
            Objects.requireNonNull(packageOrder, "packageOrder must not be null");
            mPackageOrder = List.copyOf(packageOrder);
            return this;
        }

        /**
         * Builds the {@link RouteDiscoveryPreference}.
         */
        @NonNull
        public RouteDiscoveryPreference build() {
            return new RouteDiscoveryPreference(this);
        }
    }
}
