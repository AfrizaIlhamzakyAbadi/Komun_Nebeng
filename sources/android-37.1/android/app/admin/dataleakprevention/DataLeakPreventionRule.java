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

package android.app.admin.dataleakprevention;

import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The condition and action logic unit for DLP.
 */
@Hide
public final class DataLeakPreventionRule implements Parcelable {
    private final String mRuleId;
    private final @Nullable SourceMatcher mSourceMatcher;
    private final @Nullable DestinationMatcher mDestinationMatcher;
    private final @Nullable TransferActionMatcher mTransferActionMatcher;
    private final EnforcementAction mEnforcement;

    // SourceMatcher and DestinationMatcher are nullable to allow for rules that apply to all
    // sources or all destinations.
    public DataLeakPreventionRule(
            @NonNull String ruleId,
            @Nullable SourceMatcher sourceMatcher,
            @Nullable DestinationMatcher destinationMatcher,
            @Nullable TransferActionMatcher transferActionMatcher,
            @NonNull EnforcementAction enforcement) {
        if (ruleId == null || ruleId.isEmpty()) {
            throw new IllegalArgumentException("ruleId cannot be null or empty");
        }
        mRuleId = ruleId;
        mSourceMatcher = sourceMatcher;
        mDestinationMatcher = destinationMatcher;
        mTransferActionMatcher = transferActionMatcher;
        mEnforcement = Objects.requireNonNull(enforcement);
    }

    private DataLeakPreventionRule(Parcel in) {
        mRuleId = in.readString8();
        if (mRuleId == null || mRuleId.isEmpty()) {
            throw new IllegalArgumentException("ruleId cannot be null or empty");
        }
        mSourceMatcher = in.readTypedObject(SourceMatcher.CREATOR);
        mDestinationMatcher = in.readTypedObject(DestinationMatcher.CREATOR);
        mTransferActionMatcher = in.readTypedObject(TransferActionMatcher.CREATOR);
        mEnforcement = in.readTypedObject(EnforcementAction.CREATOR);
        Objects.requireNonNull(mEnforcement);
    }

    @NonNull
    public String getRuleId() {
        return mRuleId;
    }

    @Nullable
    public SourceMatcher getSourceMatcher() {
        return mSourceMatcher;
    }

    @Nullable
    public DestinationMatcher getDestinationMatcher() {
        return mDestinationMatcher;
    }

    @Nullable
    public TransferActionMatcher getTransferActionMatcher() {
        return mTransferActionMatcher;
    }

    @NonNull
    public EnforcementAction getEnforcementAction() {
        return mEnforcement;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataLeakPreventionRule that = (DataLeakPreventionRule) o;
        return Objects.equals(mRuleId, that.mRuleId)
                && Objects.equals(mSourceMatcher, that.mSourceMatcher)
                && Objects.equals(mDestinationMatcher, that.mDestinationMatcher)
                && Objects.equals(mTransferActionMatcher, that.mTransferActionMatcher)
                && Objects.equals(mEnforcement, that.mEnforcement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                mRuleId, mSourceMatcher, mDestinationMatcher, mTransferActionMatcher, mEnforcement);
    }

    @Override
    public String toString() {
        return "DataLeakPreventionRule {mRuleId="
                + mRuleId
                + ", mSourceMatcher="
                + mSourceMatcher
                + ", mDestinationMatcher="
                + mDestinationMatcher
                + ", mTransferActionMatcher="
                + mTransferActionMatcher
                + ", mEnforcement="
                + mEnforcement
                + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString8(mRuleId);
        dest.writeTypedObject(mSourceMatcher, flags);
        dest.writeTypedObject(mDestinationMatcher, flags);
        dest.writeTypedObject(mTransferActionMatcher, flags);
        dest.writeTypedObject(mEnforcement, flags);
    }

    @NonNull
    public static final Parcelable.Creator<DataLeakPreventionRule> CREATOR =
            new Parcelable.Creator<DataLeakPreventionRule>() {
                @Override
                public DataLeakPreventionRule createFromParcel(Parcel in) {
                    return new DataLeakPreventionRule(in);
                }

                @Override
                public DataLeakPreventionRule[] newArray(int size) {
                    return new DataLeakPreventionRule[size];
                }
            };
}