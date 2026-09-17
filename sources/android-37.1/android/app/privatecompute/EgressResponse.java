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

package android.app.privatecompute;

import android.annotation.FlaggedApi;
import android.annotation.NonNull;
import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Abstract base response class for PccSandboxManager.egressData operations.
 *
 * <p>Since the interfaces do not extend Parcelable, they cannot be passed directly as AIDL
 * parameters. The AIDL compiler generates code that tries to call
 * Parcel.readTypedObject(EgressResponse.CREATOR), which triggers compilation errors (cannot find
 * symbol: variable CREATOR and incompatible type bounds). Therefore, this base class implements
 * Parcelable and provides the base unmarshalling stubs.
 */
@FlaggedApi(android.app.privatecompute.flags.Flags.FLAG_ENABLE_PCC_ASSISTANT_EGRESS)
@SuppressLint("ParcelNotFinal")
public abstract class EgressResponse implements Parcelable {

    /* package */ static final int TYPE_ASSISTANT_QUERY = 1;

    private final int mType;

    /**
     * Creates a new EgressResponse with a given type.
     *
     * @param type The subclass type of the egress response.
     */
    EgressResponse(int type) {
        if (type != TYPE_ASSISTANT_QUERY) {
            throw new IllegalArgumentException("Invalid EgressResponse type: " + type);
        }
        this.mType = type;
    }

    /**
     * Returns the subclass type of this response.
     *
     * @return The subclass type integer.
     */
    /* package */ int getType() {
        return mType;
    }

    /** {@inheritDoc} */
    @Override
    public int describeContents() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mType);
    }

    /** Creator for {@link EgressResponse} instance from a {@link Parcel}. */
    @NonNull
    public static final Parcelable.Creator<EgressResponse> CREATOR =
            new Parcelable.Creator<EgressResponse>() {
                @Override
                public EgressResponse createFromParcel(Parcel in) {
                    final int type = in.readInt();
                    if (type == TYPE_ASSISTANT_QUERY) {
                        return new AssistantQueryResponse(in);
                    }
                    throw new IllegalArgumentException(
                            "Unknown EgressResponse subclass type: " + type);
                }

                @Override
                public EgressResponse[] newArray(int size) {
                    return new EgressResponse[size];
                }
            };
}
