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

package android.service.personalcontext.insight.destination;

import android.annotation.Hide;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.Objects;
import java.util.UUID;

/**
 * Routing and structure information for
 * {@link android.service.personalcontext.insight.ContextInsight}s.
 *
 * When a {@link android.service.personalcontext.understander.ContextUnderstanderService} needs to
 * generate an insight, it is provided with a list of available {@link ContextDestination}s. Each
 * destination provides a {@link DestinationSpec} class. This class includes documentation on the
 * expected type and structure of the generated insight.
 *
 * @see android.service.personalcontext.understander.ContextUnderstanderService#onUnderstand
 */
@Hide
public final class ContextDestination implements Parcelable {
    // TODO(b/516534291): Make this public.

    private static final String TAG = "ContextDestination";

    private final UUID mId;
    private final DestinationSpec mDestinationSpec;

    @Hide
    public ContextDestination(DestinationSpec destinationSpec) {
        mId = UUID.randomUUID();
        mDestinationSpec = destinationSpec;
    }

    private ContextDestination(Parcel in) {
        mId = UUID.fromString(in.readString8());
        mDestinationSpec = DestinationSpec.readFromParcel(in);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString8(mId.toString());
        mDestinationSpec.writeToParcel(dest);
    }

    /**
     * Gets the class that describes the specification for the insights expected by this
     * destination.
     */
    public DestinationSpec getDestinationSpec() {
        return mDestinationSpec;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ContextDestination that)) return false;
        return Objects.equals(mId, that.mId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mId);
    }

    public static final Creator<ContextDestination> CREATOR = new Creator<>() {
        @Override
        public ContextDestination createFromParcel(Parcel in) {
            return new ContextDestination(in);
        }

        @Override
        public ContextDestination[] newArray(int size) {
            return new ContextDestination[size];
        }
    };
}
