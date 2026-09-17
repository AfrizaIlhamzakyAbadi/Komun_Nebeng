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
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SystemApi;
import android.app.people.flags.Flags;
import android.os.Parcel;
import android.os.Parcelable;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Generic representation of a feature to support different data types. Only exactly one type can be
 * set. This class is used for a) specifying constants in the People Inference config and b) holding
 * feature values during inference.
 */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
public final class Feature implements Parcelable {

    /**
     * Supported feature data types.
     */
    @Hide
    @IntDef({
        KIND_BOOLEAN,
        KIND_LONG,
        KIND_DOUBLE,
        KIND_STRING,
        KIND_BYTES,
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface Kind {}

    public static final int KIND_BOOLEAN = 1;
    public static final int KIND_LONG = 2;
    public static final int KIND_DOUBLE = 3;
    public static final int KIND_STRING = 4;
    public static final int KIND_BYTES = 5;

    @Kind private final int mKind;
    @NonNull private final Object mValues;

    private Feature(@Kind int kind, @NonNull Object values) {
        mKind = kind;
        mValues = checkNotNull(values, "values cannot be null");
    }

    /**
     * Creates a new {@link Feature} of type {@link #KIND_BOOLEAN}.
     *
     * @param values The boolean values of the feature.
     * @return A new {@link Feature} with the given values.
     */
    @NonNull
    public static Feature booleanFeature(@NonNull List<Boolean> values) {
        return new Feature(KIND_BOOLEAN, Collections.unmodifiableList(values));
    }

    /**
     * Same as above but accepts vararg.
     *
     * @param values The boolean values of the feature.
     * @return A new {@link Feature} with the given values.
     */
    @NonNull
    public static Feature booleanFeature(@NonNull boolean... values) {
        List<Boolean> allValues = new ArrayList<>(values.length);
        for (boolean v : values) {
            allValues.add(v);
        }
        return Feature.booleanFeature(allValues);
    }

    /**
     * Creates a new {@link Feature} of type {@link #KIND_LONG}.
     *
     * @param values The long values of the feature.
     * @return A new {@link Feature} with the given values.
     */
    @NonNull
    public static Feature longFeature(@NonNull List<Long> values) {
        return new Feature(KIND_LONG, Collections.unmodifiableList(values));
    }

    /**
     * Same as above but accepts vararg.
     *
     * @param values The long values of the feature.
     * @return A new {@link Feature} with the given values.
     */
    @NonNull
    public static Feature longFeature(@NonNull long... values) {
        return Feature.longFeature(Arrays.stream(values).boxed().collect(Collectors.toList()));
    }

    /**
     * Creates a new {@link Feature} of type {@link #KIND_DOUBLE}.
     *
     * @param values The double values of the feature.
     * @return A new {@link Feature} with the given values.
     */
    @NonNull
    public static Feature doubleFeature(@NonNull List<Double> values) {
        return new Feature(KIND_DOUBLE, Collections.unmodifiableList(values));
    }

    /**
     * Same as above but accepts vararg.
     *
     * @param values The double values of the feature.
     * @return A new {@link Feature} with the given values.
     */
    @NonNull
    public static Feature doubleFeature(@NonNull double... values) {
        return Feature.doubleFeature(Arrays.stream(values).boxed().collect(Collectors.toList()));
    }

    /**
     * Creates a new {@link Feature} of type {@link #KIND_STRING}.
     *
     * @param values The string values of the feature.
     * @return A new {@link Feature} with the given values.
     */
    @NonNull
    public static Feature stringFeature(@NonNull List<String> values) {
        return new Feature(KIND_STRING, Collections.unmodifiableList(values));
    }

    /**
     * Same as above but accepts vararg.
     *
     * @param values The string values of the feature.
     * @return A new {@link Feature} with the given values.
     */
    @NonNull
    public static Feature stringFeature(@NonNull String... values) {
        return Feature.stringFeature(Arrays.asList(values));
    }

    /**
     * Creates a new {@link Feature} of type {@link #KIND_BYTES}.
     *
     * @param values The bytes values of the feature.
     * @return A new {@link Feature} with the given values.
     */
    @NonNull
    public static Feature bytesFeature(@NonNull List<byte[]> values) {
        return new Feature(KIND_BYTES, Collections.unmodifiableList(values));
    }

    /**
     * Same as above but accepts vararg.
     *
     * @param values The bytes values of the feature.
     * @return A new {@link Feature} with the given values.
     */
    @NonNull
    public static Feature bytesFeature(@NonNull byte[]... values) {
        return Feature.bytesFeature(Arrays.asList(values));
    }

    /** Returns the data type of this {@link Feature}. */
    @Kind
    public int getKind() {
        return mKind;
    }

    /** Returns the boolean values if the type is {@link #KIND_BOOLEAN}, otherwise an empty list. */
    @NonNull
    public List<Boolean> getBooleanValues() {
        return mKind == KIND_BOOLEAN ? (List<Boolean>) mValues : Collections.emptyList();
    }

    /** Returns the long values if the type is {@link #KIND_LONG}, otherwise an empty list. */
    @NonNull
    public List<Long> getLongValues() {
        return mKind == KIND_LONG ? (List<Long>) mValues : Collections.emptyList();
    }

    /** Returns the double values if the type is {@link #KIND_DOUBLE}, otherwise an empty list. */
    @NonNull
    public List<Double> getDoubleValues() {
        return mKind == KIND_DOUBLE ? (List<Double>) mValues : Collections.emptyList();
    }

    /** Returns the string values if the type is {@link #KIND_STRING}, otherwise an empty list. */
    @NonNull
    public List<String> getStringValues() {
        return mKind == KIND_STRING ? (List<String>) mValues : Collections.emptyList();
    }

    /** Returns the bytes values if the type is {@link #KIND_BYTES}, otherwise an empty list. */
    @NonNull
    public List<byte[]> getBytesValues() {
        return mKind == KIND_BYTES ? (List<byte[]>) mValues : Collections.emptyList();
    }

    @Override
    public boolean equals(@Nullable Object o) {
        if (this == o) {
            return true;
        }
        if (o != null && o instanceof Feature other) {
            return mKind == other.mKind && Objects.equals(mValues, other.mValues);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mKind, mValues);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeInt(mKind);
        switch (mKind) {
            case KIND_BOOLEAN -> dest.writeList((List<Boolean>) mValues);
            case KIND_LONG -> dest.writeList((List<Long>) mValues);
            case KIND_DOUBLE -> dest.writeList((List<Double>) mValues);
            case KIND_STRING -> dest.writeStringList((List<String>) mValues);
            case KIND_BYTES -> dest.writeList((List<byte[]>) mValues);
        }
    }

    @NonNull
    public static final Parcelable.Creator<Feature> CREATOR =
            new Parcelable.Creator<Feature>() {
                @Override
                public Feature createFromParcel(@NonNull Parcel in) {
                    int kind = in.readInt();
                    return switch (kind) {
                        case KIND_BOOLEAN ->
                                Feature.booleanFeature(readListFromParcel(in, Boolean.class));
                        case KIND_LONG -> Feature.longFeature(readListFromParcel(in, Long.class));
                        case KIND_DOUBLE ->
                                Feature.doubleFeature(readListFromParcel(in, Double.class));
                        case KIND_STRING -> {
                            List<String> list = new ArrayList<>();
                            in.readStringList(list);
                            yield Feature.stringFeature(list);
                        }
                        case KIND_BYTES ->
                                Feature.bytesFeature(readListFromParcel(in, byte[].class));
                        default ->
                                throw new IllegalArgumentException("Unknown feature kind: " + kind);
                    };
                }

                @Override
                public Feature[] newArray(int size) {
                    return new Feature[size];
                }
            };

    @NonNull
    private static <T> List<T> readListFromParcel(@NonNull Parcel in, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        in.readList(list, clazz.getClassLoader(), clazz);
        return list;
    }
}
