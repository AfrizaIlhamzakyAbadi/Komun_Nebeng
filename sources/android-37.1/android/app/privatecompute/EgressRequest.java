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
 * Abstract base request class for PccSandboxManager.egressData operations.
 *
 * <p>Since the interfaces do not extend Parcelable, they cannot be passed directly as AIDL
 * parameters. The AIDL compiler generates code that tries to call
 * Parcel.readTypedObject(EgressResponse.CREATOR), which triggers compilation errors (cannot find
 * symbol: variable CREATOR and incompatible type bounds). Therefore, this base class implements
 * Parcelable and provides the base unmarshalling stubs.
 *
 * @param <R> The type of EgressResponse expected for this request.
 */
@FlaggedApi(android.app.privatecompute.flags.Flags.FLAG_ENABLE_PCC_ASSISTANT_EGRESS)
@SuppressLint("ParcelNotFinal")
public abstract class EgressRequest<R extends EgressResponse> implements Parcelable {

    /**
     * Indicates the request is for the Query Assistant use-case. */
    /* package */ static final int USE_CASE_QUERY_ASSISTANT = 1;

    private final int mUseCase;

    /**
     * Creates a new EgressRequest with the given use case.
     *
     * @param useCase The use case identifier.
     */
    EgressRequest(int useCase) {
        this.mUseCase = useCase;
    }

    /**
     * Returns the use case of this egress request.
     *
     * @return The use case integer identifier.
     */
    /* package */ int getUseCase() {
        return mUseCase;
    }

    /** {@inheritDoc} */
    @Override
    public int describeContents() {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mUseCase);
    }

    /** Creator for {@link EgressRequest} instance from a {@link Parcel}. */
    @NonNull
    public static final Parcelable.Creator<EgressRequest<?>> CREATOR =
            new Parcelable.Creator<EgressRequest<?>>() {
                @Override
                public EgressRequest<?> createFromParcel(Parcel in) {
                    final int useCase = in.readInt();
                    if (useCase == USE_CASE_QUERY_ASSISTANT) {
                        return new AssistantQueryRequest(in);
                    }
                    throw new IllegalArgumentException("Unknown EgressRequest useCase: " + useCase);
                }

                @Override
                public EgressRequest<?>[] newArray(int size) {
                    return new EgressRequest<?>[size];
                }
            };
}
