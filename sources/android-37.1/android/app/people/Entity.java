/**
 * Copyright (C) 2026 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.app.people;

import static com.android.internal.util.Preconditions.checkNotNull;

import android.annotation.FlaggedApi;
import android.annotation.FloatRange;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.annotation.TestApi;
import android.app.people.flags.Flags;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Representation of a single People Inference result item, e.g. a ranked contact, shortcut, phone
 * number, etc.
 */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
public final class Entity implements Parcelable {
    @NonNull private final EntityId mId;
    private final double mScore;
    @NonNull private final List<String> mLabels;

    private Entity(@NonNull Builder builder) {
        mId = builder.mId;
        mScore = builder.mScore;
        mLabels = Collections.unmodifiableList(builder.mLabels);
    }

    private Entity(@NonNull Parcel in) {
        mId = checkNotNull(in.readTypedObject(EntityId.CREATOR), "id cannot be null");
        mScore = in.readDouble();
        List<String> labels = new ArrayList<>();
        in.readStringList(labels);
        mLabels = Collections.unmodifiableList(labels);
    }

    /** Returns the {@link EntityId} of this entity. */
    @NonNull
    public EntityId getId() {
        return mId;
    }

    /**
     * Returns the score of this entity. There's no particular restriction on the score range, it's
     * determined by the inference config supplied.
     */
    @FloatRange(from = Double.MIN_VALUE, to = Double.MAX_VALUE)
    public double getScore() {
        return mScore;
    }

    /**
     * Returns the labels on this entity. A label is an annotation on the entity as requested by the
     * client. For example, it can be "CONFIDENCE_HIGH", "PRIMARY", etc.
     */
    @NonNull
    public List<String> getLabels() {
        return mLabels;
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof Entity other) {
            return Objects.equals(mId, other.mId);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mId);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedObject(mId, flags);
        dest.writeDouble(mScore);
        dest.writeStringList(mLabels);
    }

    @NonNull
    public static final Parcelable.Creator<Entity> CREATOR =
            new Parcelable.Creator<Entity>() {
                @Override
                public Entity createFromParcel(@NonNull Parcel in) {
                    return new Entity(in);
                }

                @Override
                public Entity[] newArray(int size) {
                    return new Entity[size];
                }
            };

    /**
     * Builder for {@link Entity}.
     */
    @SystemApi
    @FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
    public static final class Builder {
        @NonNull private final EntityId mId;
        private double mScore = Double.MIN_VALUE;
        @NonNull private List<String> mLabels = Collections.emptyList();

        /**
         * Creates a new {@link Builder} for constructing an {@link Entity}.
         *
         * @param id The {@link EntityId} of this entity.
         */
        public Builder(@NonNull EntityId id) {
            mId = checkNotNull(id, "id cannot be null");
        }

        /** Returns the {@link EntityId} of this entity. */
        @SuppressLint("GetterOnBuilder")
        @NonNull
        public EntityId getId() {
            return mId;
        }

        /**
         * Sets the score on this entity. This method should be only for internal use.
         *
         * @param score The score of this entity.
         * @return The {@link Builder} for chaining.
         */
        @TestApi
        @NonNull
        public Builder setScore(
                @FloatRange(from = Double.MIN_VALUE, to = Double.MAX_VALUE) double score) {
            mScore = score;
            return this;
        }

        /**
         * Sets the labels on this entity. This method should be only for internal use.
         *
         * @param labels The labels on this entity.
         * @return The {@link Builder} for chaining.
         */
        @TestApi
        @NonNull
        public Builder setLabels(@NonNull List<String> labels) {
            mLabels = checkNotNull(labels, "labels cannot be null");
            return this;
        }

        /** Builds the {@link Entity}. */
        @NonNull
        public Entity build() {
            return new Entity(this);
        }
    }
}
