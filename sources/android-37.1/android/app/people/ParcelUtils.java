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
import static com.android.internal.util.Preconditions.checkState;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SuppressLint;
import android.app.people.flags.Flags;
import android.os.Parcel;
import android.os.Parcelable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

/**
 * Utility methods for parceling objects.
 */
@Hide
@FlaggedApi(Flags.FLAG_ENABLE_PWM_SERVICE)
public final class ParcelUtils {
    @SuppressLint("MinMaxConstant")
    private static final int MAX_COLLECTION_SIZE = 2048;

    @SuppressLint("MinMaxConstant")
    private static final int MAX_RECURSION_DEPTH = 256;

    /**
     * Checks that the object recursion depth is within the limit.
     *
     * @param depth The current recursion depth.
     * @return The incremented recursion depth for the next nested object.
     * @throws IllegalStateException if the recursion depth exceeds the limit.
     */
    @Hide
    public static int checkRecursionDepth(int depth) {
        checkState(depth <= MAX_RECURSION_DEPTH, "Maximum recursion depth exceeded");
        return depth + 1;
    }

    /**
     * Checks that the size of a collection is within the limit.
     *
     * @param size The size of the collection.
     * @return The size of the collection if it is within the limit.
     * @throws IllegalStateException if the size of the collection exceeds the limit.
     */
    @Hide
    public static int checkCollectionSize(int size) {
        checkState(size <= MAX_COLLECTION_SIZE, "Maximum collection size exceeded");
        return size;
    }

    /**
     * Same as {@link Parcel#readTypedObject} but with recursion depth check.
     *
     * @param in The parcel to read from.
     * @param depth The current recursion depth.
     * @param parceler The function for reading a typed object from the parcel.
     * @return The typed object read from the parcel.
     */
    @Hide
    @Nullable
    public static <T> T readTypedObject(
            @NonNull Parcel in, int depth, @NonNull BiFunction<Parcel, Integer, T> parceler) {
        depth = checkRecursionDepth(depth);
        if (in.readInt() == 0) {
            return null;
        }
        return parceler.apply(in, depth);
    }

    /**
     * Writes a list of integers to the parcel.
     *
     * @param list The list to write.
     * @param out The parcel to write to.
     * @param flags The parcel flags.
     */
    @Hide
    public static void writeIntList(@NonNull List<Integer> list, @NonNull Parcel out, int flags) {
        out.writeInt(list.size());
        for (int element : list) {
            out.writeInt(element);
        }
    }

    /**
     * Reads a list of integers from the parcel.
     *
     * @param in The parcel to read from.
     * @return An immutable list of integers read from the parcel.
     */
    @Hide
    @NonNull
    public static List<Integer> readIntList(@NonNull Parcel in) {
        int size = checkCollectionSize(in.readInt());
        if (size <= 0) {
            return Collections.emptyList();
        }
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            list.add(in.readInt());
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Same as {@link Parcel#readTypedList} but with recursion depth check and accepts a parceler
     * function to allow additional checks in object parceling.
     *
     * @param in The parcel to read from.
     * @param depth The current recursion depth.
     * @param parceler The function for reading a typed list element from the parcel.
     * @return An immutable list of typed objects read from the parcel.
     */
    @Hide
    @NonNull
    public static <T> List<T> readTypedList(
            @NonNull Parcel in, int depth, @NonNull BiFunction<Parcel, Integer, T> parceler) {
        depth = checkRecursionDepth(depth);
        int size = checkCollectionSize(in.readInt());
        if (size <= 0) {
            return Collections.emptyList();
        }
        List<T> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            T element = readTypedObject(in, depth, parceler);
            list.add(checkNotNull(element, "list element cannot be null"));
        }
        return Collections.unmodifiableList(list);
    }

    /**
     * Writes a map of integers to typed objects to the parcel.
     *
     * @param map The map to write.
     * @param out The parcel to write to.
     * @param flags The parcel flags.
     */
    @Hide
    public static <V extends Parcelable> void writeIntMap(
            @NonNull Map<Integer, V> map, @NonNull Parcel out, int flags) {
        out.writeInt(map.size());
        for (Map.Entry<Integer, V> entry : map.entrySet()) {
            out.writeInt(entry.getKey());
            out.writeTypedObject(entry.getValue(), flags);
        }
    }

    /**
     * Reads a map of integers to typed objects from the parcel by the given parceler function with
     * recursion depth check.
     *
     * @param in The parcel to read from.
     * @param depth The current recursion depth.
     * @param parceler The function for reading a typed map value from the parcel.
     * @return An immutable map of integers to typed objects read from the parcel.
     */
    @Hide
    @NonNull
    public static <V> Map<Integer, V> readIntMap(
            @NonNull Parcel in, int depth, @NonNull BiFunction<Parcel, Integer, V> parceler) {
        depth = checkRecursionDepth(depth);
        int size = checkCollectionSize(in.readInt());
        if (size <= 0) {
            return Collections.emptyMap();
        }
        Map<Integer, V> map = new HashMap<>(size);
        for (int i = 0; i < size; i++) {
            int key = in.readInt();
            V val = checkNotNull(readTypedObject(in, depth, parceler), "map value cannot be null");
            map.put(key, val);
        }
        return Collections.unmodifiableMap(map);
    }

    private ParcelUtils() {}
}
