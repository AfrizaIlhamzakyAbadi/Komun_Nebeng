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
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Objects;

/**
 * Specific data movement types.
 */
@Hide
public final class TransferAction implements Parcelable {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({
        ACTION_TYPE_DATA_TRANSFER,
        ACTION_TYPE_SCREEN_CAPTURE
    })
    public @interface ActionType {}

    public static final int ACTION_TYPE_DATA_TRANSFER = 1;
    public static final int ACTION_TYPE_SCREEN_CAPTURE = 2;

    private final @ActionType int mActionType;

    public TransferAction(@ActionType int actionType) {
        checkActionType(actionType);
        mActionType = actionType;
    }

    private TransferAction(Parcel in) {
        mActionType = in.readInt();
        checkActionType(mActionType);
    }

    private void checkActionType(@ActionType int actionType) {
        if (actionType != ACTION_TYPE_DATA_TRANSFER
                && actionType != ACTION_TYPE_SCREEN_CAPTURE) {
            throw new IllegalArgumentException("Invalid action type: " + actionType);
        }
    }

    public @ActionType int getActionType() {
        return mActionType;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TransferAction that = (TransferAction) o;
        return mActionType == that.mActionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mActionType);
    }

    @Override
    public String toString() {
        return "TransferAction {mActionType=" + mActionType + "}";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mActionType);
    }

    @NonNull
    public static final Parcelable.Creator<TransferAction> CREATOR =
            new Parcelable.Creator<TransferAction>() {
                @Override
                public TransferAction createFromParcel(Parcel in) {
                    return new TransferAction(in);
                }

                @Override
                public TransferAction[] newArray(int size) {
                    return new TransferAction[size];
                }
            };
}