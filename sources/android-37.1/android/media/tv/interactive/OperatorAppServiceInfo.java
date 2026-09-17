/*
 * Copyright 2026 The Android Open Source Project
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

package android.media.tv.interactive;

import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.ComponentName;
import android.content.pm.ResolveInfo;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * TV interactive app service information for Operator Applications.
 */
@Hide
public final class OperatorAppServiceInfo implements Parcelable {

    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(prefix = "OPERATOR_APP_STATE_", value = {
            OPERATOR_APP_STATE_BACKGROUND,
            OPERATOR_APP_STATE_FOREGROUND,
            OPERATOR_APP_STATE_TRANSIENT,
            OPERATOR_APP_STATE_OVERLAID_FOREGROUND,
            OPERATOR_APP_STATE_OVERLAID_TRANSIENT
    })
    public @interface OperatorAppState {}

    public static final int OPERATOR_APP_STATE_BACKGROUND = 1;
    public static final int OPERATOR_APP_STATE_FOREGROUND = 2;
    public static final int OPERATOR_APP_STATE_TRANSIENT = 3;
    public static final int OPERATOR_APP_STATE_OVERLAID_FOREGROUND = 4;
    public static final int OPERATOR_APP_STATE_OVERLAID_TRANSIENT = 5;

    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(prefix = "OPERATOR_APP_PACKAGE_STATUS_", value = {
            OPERATOR_APP_PACKAGE_STATUS_DISCOVERY_SUCCESS,
            OPERATOR_APP_PACKAGE_STATUS_DISCOVERY_FAIL,
            OPERATOR_APP_PACKAGE_STATUS_DOWNLOAD_SUCCESS,
            OPERATOR_APP_PACKAGE_STATUS_DOWNLOAD_FAIL,
            OPERATOR_APP_PACKAGE_STATUS_INSTALL_SUCCESS,
            OPERATOR_APP_PACKAGE_STATUS_INSTALL_FAIL,
            OPERATOR_APP_PACKAGE_STATUS_UNINSTALL_SUCCESS,
            OPERATOR_APP_PACKAGE_STATUS_UNINSTALL_FAIL,
            OPERATOR_APP_PACKAGE_STATUS_CANCEL_SUCCESS,
            OPERATOR_APP_PACKAGE_STATUS_CANCEL_FAIL,
            OPERATOR_APP_PACKAGE_STATUS_UPDATE_SUCCESS,
            OPERATOR_APP_PACKAGE_STATUS_UPDATE_FAIL
    })
    public @interface OperatorAppPackageStatus {}

    public static final int OPERATOR_APP_PACKAGE_STATUS_DISCOVERY_SUCCESS = 1;
    public static final int OPERATOR_APP_PACKAGE_STATUS_DISCOVERY_FAIL = 2;
    public static final int OPERATOR_APP_PACKAGE_STATUS_DOWNLOAD_SUCCESS = 3;
    public static final int OPERATOR_APP_PACKAGE_STATUS_DOWNLOAD_FAIL = 4;
    public static final int OPERATOR_APP_PACKAGE_STATUS_INSTALL_SUCCESS = 5;
    public static final int OPERATOR_APP_PACKAGE_STATUS_INSTALL_FAIL = 6;
    public static final int OPERATOR_APP_PACKAGE_STATUS_UNINSTALL_SUCCESS = 7;
    public static final int OPERATOR_APP_PACKAGE_STATUS_UNINSTALL_FAIL = 8;
    public static final int OPERATOR_APP_PACKAGE_STATUS_CANCEL_SUCCESS = 9;
    public static final int OPERATOR_APP_PACKAGE_STATUS_CANCEL_FAIL = 10;
    public static final int OPERATOR_APP_PACKAGE_STATUS_UPDATE_SUCCESS = 11;
    public static final int OPERATOR_APP_PACKAGE_STATUS_UPDATE_FAIL = 12;

    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(prefix = "OPERATOR_APP_STATUS_", value = {
            OPERATOR_APP_STATUS_RUNNING,
            OPERATOR_APP_STATUS_TERMINATED,
            OPERATOR_APP_STATUS_ABNORMAL_EXIT,
            OPERATOR_APP_STATUS_NOT_STARTED
    })
    public @interface OperatorAppStatus {}

    public static final int OPERATOR_APP_STATUS_RUNNING = 1;
    public static final int OPERATOR_APP_STATUS_TERMINATED = 2;
    public static final int OPERATOR_APP_STATUS_ABNORMAL_EXIT = 3;
    public static final int OPERATOR_APP_STATUS_NOT_STARTED = 4;

    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(prefix = "OPERATOR_APP_SEARCH_METHOD_", value = {
            OPERATOR_APP_SEARCH_METHOD_FQDN_BROADCAST,
            OPERATOR_APP_SEARCH_METHOD_AIT_BROADCAST,
            OPERATOR_APP_SEARCH_METHOD_AIT_CICAM,
            OPERATOR_APP_SEARCH_METHOD_FQDN_HARDWIRED,
            OPERATOR_APP_SEARCH_METHOD_AIT_HARDWIRED,
            OPERATOR_APP_SEARCH_METHOD_DNS_SRV_LOOKUP
    })
    public @interface OperatorAppSearchMethod {}

    public static final int OPERATOR_APP_SEARCH_METHOD_FQDN_BROADCAST = 1;
    public static final int OPERATOR_APP_SEARCH_METHOD_AIT_BROADCAST = 2;
    public static final int OPERATOR_APP_SEARCH_METHOD_AIT_CICAM = 3;
    public static final int OPERATOR_APP_SEARCH_METHOD_FQDN_HARDWIRED = 4;
    public static final int OPERATOR_APP_SEARCH_METHOD_AIT_HARDWIRED = 5;
    public static final int OPERATOR_APP_SEARCH_METHOD_DNS_SRV_LOOKUP = 6;

    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(prefix = "OPERATOR_APP_UNINSTALL_METHOD_", value = {
            OPERATOR_APP_UNINSTALL_METHOD_WITH_USER_INTERACTION,
            OPERATOR_APP_UNINSTALL_METHOD_WITHOUT_USER_INTERACTION
    })
    public @interface OperatorAppUninstallMethod {}

    public static final int OPERATOR_APP_UNINSTALL_METHOD_WITH_USER_INTERACTION = 1;
    public static final int OPERATOR_APP_UNINSTALL_METHOD_WITHOUT_USER_INTERACTION = 2;

    public final @TvInteractiveAppServiceInfo.InteractiveAppSubtype int mSubtype;
    public final String mOperatorName;
    public final String mGroupName;
    @Nullable
    public final Bundle mExtra;

    private final TvInteractiveAppServiceInfo mBaseInfo;

    private OperatorAppServiceInfo(@NonNull Builder builder) {
        mBaseInfo = new TvInteractiveAppServiceInfo(
                builder.mService, builder.mId, builder.mTypes, builder.mExtraTypes);
        this.mSubtype = builder.mSubtype;
        this.mOperatorName = builder.mOperatorName;
        this.mGroupName = builder.mGroupName;
        this.mExtra = builder.mExtra;
    }

    private OperatorAppServiceInfo(Parcel in) {
        mBaseInfo = TvInteractiveAppServiceInfo.CREATOR.createFromParcel(in);
        mSubtype = in.readInt();
        mOperatorName = in.readString();
        mGroupName = in.readString();
        mExtra = in.readBundle(getClass().getClassLoader());
    }

    @NonNull
    public static final Creator<OperatorAppServiceInfo> CREATOR =
            new Creator<OperatorAppServiceInfo>() {
        @Override
        public OperatorAppServiceInfo createFromParcel(Parcel in) {
            return new OperatorAppServiceInfo(in);
        }

        @Override
        public OperatorAppServiceInfo[] newArray(int size) {
            return new OperatorAppServiceInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        mBaseInfo.writeToParcel(dest, flags);
        dest.writeInt(mSubtype);
        dest.writeString(mOperatorName);
        dest.writeString(mGroupName);
        dest.writeBundle(mExtra);
    }

    @Hide
    public TvInteractiveAppServiceInfo getBaseInfo() {
        return mBaseInfo;
    }

    /**
     * A builder class for {@link OperatorAppServiceInfo}.
     */
    public static final class Builder {
        private ResolveInfo mService;
        private String mId;
        private int mTypes;
        private final List<String> mExtraTypes = new ArrayList<>();
        private int mSubtype;
        private String mOperatorName;
        private String mGroupName;
        private Bundle mExtra;

        public Builder() {
        }

        /**
         * Sets the service.
         */
        @NonNull
        public Builder setService(@NonNull ResolveInfo service) {
            if (service == null) {
                throw new IllegalArgumentException("service cannot be null.");
            }
            mService = service;
            return this;
        }

        /**
         * Sets the id.
         */
        @NonNull
        public Builder setId(@NonNull String id) {
            if (id == null) {
                throw new IllegalArgumentException("id cannot be null.");
            }
            mId = id;
            return this;
        }

        /**
         * Sets the types.
         */
        @NonNull
        public Builder setSupportedTypes(int types) {
            mTypes = types;
            return this;
        }

        /**
         * Sets the subtype.
         */
        @NonNull
        public Builder setSubtype(int subtype) {
            mSubtype = subtype;
            return this;
        }

        /**
         * Sets custom extra types.
         */
        @NonNull
        public Builder setCustomSupportedTypes(@Nullable List<String> extraTypes) {
            mExtraTypes.clear();
            if (extraTypes != null) {
                mExtraTypes.addAll(extraTypes);
            }
            return this;
        }

        /**
         * Sets the operator name.
         */
        @NonNull
        public Builder setOperatorName(@Nullable String operatorName) {
            mOperatorName = operatorName;
            return this;
        }

        /**
         * Sets the group name.
         */
        @NonNull
        public Builder setGroupName(@Nullable String groupName) {
            mGroupName = groupName;
            return this;
        }

        /**
         * Sets the extra bundle.
         */
        @NonNull
        public Builder setExtra(@Nullable Bundle extra) {
            mExtra = extra;
            return this;
        }

        /**
         * Builds a {@link OperatorAppServiceInfo} object.
         */
        @NonNull
        public OperatorAppServiceInfo build() {
            return new OperatorAppServiceInfo(this);
        }
    }
}
