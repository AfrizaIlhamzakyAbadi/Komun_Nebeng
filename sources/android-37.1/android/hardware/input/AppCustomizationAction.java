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

package android.hardware.input;

import static com.android.hardware.input.Flags.FLAG_ENABLE_POEM_INPUT_CUSTOMIZATION;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.ComponentName;
import android.net.Uri;

import java.util.Objects;

/**
 * Represents an action that launches or sends an intent to a specific application component in
 * response to a peripheral input customization trigger.
 *
 * This class holds the target component (required), action (required), and data (optional)
 * required to create the intent for execution.
 */
@FlaggedApi(FLAG_ENABLE_POEM_INPUT_CUSTOMIZATION)
@android.ravenwood.annotation.RavenwoodKeepWholeClass
public final class AppCustomizationAction extends CustomizationAction {
    @NonNull private final AidlAppAction mAidlAppAction;

    private AppCustomizationAction(Builder builder) {
        this.mAidlAppAction = new AidlAppAction();
        this.mAidlAppAction.component = Objects.requireNonNull(builder.mComponent,
                "component must not be null");
        this.mAidlAppAction.action = Objects.requireNonNull(builder.mComponentAction,
                "action must not be null");
        this.mAidlAppAction.data = builder.mData;
    }

    /**
     * Creates a new {@link AppCustomizationAction} wrapping the specified AIDL object.
     */
    @Hide
    public AppCustomizationAction(@NonNull AidlAppAction aidlAppAction) {
        Objects.requireNonNull(aidlAppAction, "aidlAppAction must not be null");
        this.mAidlAppAction = new AidlAppAction();
        this.mAidlAppAction.component = Objects.requireNonNull(aidlAppAction.component,
                "component must not be null");
        this.mAidlAppAction.action = Objects.requireNonNull(aidlAppAction.action,
                "action must not be null");
        this.mAidlAppAction.data = aidlAppAction.data;
        this.mAidlAppAction.oemPackageName = aidlAppAction.oemPackageName;
        this.mAidlAppAction.oemUid = aidlAppAction.oemUid;

        if (!android.content.Intent.ACTION_MAIN.equals(this.mAidlAppAction.action)
                && !android.content.Intent.ACTION_VIEW.equals(this.mAidlAppAction.action)) {
            throw new IllegalArgumentException("Only ACTION_MAIN or ACTION_VIEW are supported");
        }
    }

    /**
     * Gets the target application component to be launched.
     *
     * @return The target {@link ComponentName}. Never {@code null}.
     */
    @NonNull
    public ComponentName getComponent() {
        return mAidlAppAction.component;
    }

    /**
     * Gets the explicit intent action to be used during launch.
     *
     * @return The intent action string. Never {@code null}.
     */
    @NonNull
    public String getIntentAction() {
        return mAidlAppAction.action;
    }

    /**
     * Gets the explicit data URI to be supplied to the target intent.
     *
     * @return The target {@link Uri}, or {@code null} if no data was specified.
     */
    @Nullable
    public Uri getData() {
        return mAidlAppAction.data;
    }

    /**
     * Gets the underlying AIDL parcelable object.
     */
    @Hide
    @NonNull
    public AidlAppAction getAidl() {
        AidlAppAction copy = new AidlAppAction();
        copy.component = mAidlAppAction.component;
        copy.action = mAidlAppAction.action;
        copy.data = mAidlAppAction.data;
        copy.oemPackageName = mAidlAppAction.oemPackageName;
        copy.oemUid = mAidlAppAction.oemUid;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppCustomizationAction that = (AppCustomizationAction) o;
        // AidlAppAction contains internal system server tracking metadata (oemPackageName and
        // oemUid). Since these fields are not part of the public API and track internal state
        // including them in equals will violate value object equality semantics for the public
        // Api.
        return Objects.equals(mAidlAppAction.component, that.mAidlAppAction.component)
                && Objects.equals(mAidlAppAction.action, that.mAidlAppAction.action)
                && Objects.equals(mAidlAppAction.data, that.mAidlAppAction.data);
    }

    @Override
    public int hashCode() {
        // AidlAppAction contains internal system server tracking metadata (oemPackageName and
        // oemUid). Since these fields are not part of the public API and track internal state
        // including them in hash will violate value object equality semantics for the public
        // Api.
        return Objects.hash(mAidlAppAction.component, mAidlAppAction.action, mAidlAppAction.data);
    }

    /** Builder for {@link AppCustomizationAction}. */
    public static final class Builder {
        private final ComponentName mComponent;
        private String mComponentAction;
        private Uri mData;

        /**
         * Creates a new Builder for an {@link AppCustomizationAction} targeting the
         * specified component and action.
         *
         * @param component The target {@link ComponentName} to be launched. Must not be
         * {@code null}.
         * @param action The intent action to set. Only ACTION_MAIN and ACTION_VIEW are supported.
         * Must not be {@code null}.
         * @throws NullPointerException if component or action is {@code null}.
         * @throws IllegalArgumentException if action is not ACTION_MAIN or ACTION_VIEW.
         */
        public Builder(@NonNull ComponentName component, @NonNull String action) {
            mComponent = Objects.requireNonNull(component, "component must not be null");
            mComponentAction = Objects.requireNonNull(action, "action must not be null");
            if (!android.content.Intent.ACTION_MAIN.equals(action)
                    && !android.content.Intent.ACTION_VIEW.equals(action)) {
                throw new IllegalArgumentException("Only ACTION_MAIN or ACTION_VIEW are supported");
            }
        }

        /**
         * Sets the intent data URI. This field is optional.
         *
         * @param data The data URI to set.
         * @return This builder.
         */
        @NonNull
        public Builder setData(@Nullable Uri data) {
            mData = data;
            return this;
        }

        /**
         * Builds the {@link AppCustomizationAction}.
         *
         * @return The built AppCustomizationAction.
         */
        @NonNull
        public AppCustomizationAction build() {
            return new AppCustomizationAction(this);
        }
    }
}
