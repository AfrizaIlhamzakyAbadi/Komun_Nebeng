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

package android.view.contentcapture;

import android.annotation.Hide;
import android.annotation.NonNull;
import android.view.autofill.AutofillId;

import com.android.internal.annotations.VisibleForTesting;

import java.util.Arrays;

/**
 * A zero-allocation map from {@link AutofillId} to primitive {@code int} values.
 *
 * <p>This map uses parallel arrays and open addressing with linear probing.
 * It uses shift-deletion to guarantee zero allocations during high-churn
 * insert/remove cycles.
 *
 * <p><b>Thread Safety:</b> This class is not thread-safe. If accessed by multiple threads
 * concurrently, it must be synchronized externally.
 */
@Hide
@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public final class AutofillIdOffsetMap {
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public AutofillId[] mKeys;
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public int[] mValues;
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public int[] mHashes;
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public int mCapacity;
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public int mOccupied; // Precisely tracks active elements

    /**
     * @param expectedCapacity The expected number of items. The map will allocate
     *                         enough capacity to maintain a 50% load factor.
     * @throws IllegalArgumentException if expectedCapacity is negative or too large.
     */
    public AutofillIdOffsetMap(int expectedCapacity) {
        if (expectedCapacity < 0) {
            throw new IllegalArgumentException("Invalid capacity: " + expectedCapacity);
        }
        if (expectedCapacity > (1 << 29)) {
            throw new IllegalArgumentException("Capacity too large: " + expectedCapacity);
        }
        // Force a 50% load factor by doubling the expected capacity,
        // then finding the next power of 2.
        int targetCapacity = Math.max(2, expectedCapacity * 2);
        mCapacity = Integer.highestOneBit(targetCapacity - 1) << 1;

        mKeys = new AutofillId[mCapacity];
        mValues = new int[mCapacity];
        mHashes = new int[mCapacity];
        mOccupied = 0;
    }

    /**
     * Associates the specified primitive value with the specified key in this map.
     *
     * @param key   The {@link AutofillId} key.
     * @param value The primitive offset value.
     * @throws IllegalArgumentException if value is negative.
     */
    public void put(@NonNull AutofillId key, int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Value must be non-negative: " + value);
        }
        int hash = mixHash(key);
        int index = hash & (mCapacity - 1);
        final AutofillId[] keys = mKeys;
        final int[] values = mValues;
        final int[] hashes = mHashes;
        final int mask = mCapacity - 1;

        while (keys[index] != null) {
            if (hashes[index] == hash && (keys[index] == key || keys[index].equals(key))) {
                values[index] = value; // Update existing
                return;
            }
            index = (index + 1) & mask;
        }

        // Insert new
        keys[index] = key;
        values[index] = value;
        hashes[index] = hash;
        mOccupied++;

        // 50% load factor threshold
        if (mOccupied > (mCapacity >> 1)) {
            resize();
        }
    }

    /**
     * Returns the primitive value associated with the specified key, or -1 if not found.
     *
     * @param key The {@link AutofillId} key to lookup.
     * @return The associated offset, or -1 if the key is not in the map.
     */
    public int get(@NonNull AutofillId key) {
        int hash = mixHash(key);
        int index = hash & (mCapacity - 1);
        final AutofillId[] keys = mKeys;
        final int[] hashes = mHashes;
        final int mask = mCapacity - 1;

        AutofillId k;
        while ((k = keys[index]) != null) {
            if (hashes[index] == hash && (k == key || k.equals(key))) {
                return mValues[index];
            }
            index = (index + 1) & mask;
        }
        return -1; // Not found
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     *
     * @param key The {@link AutofillId} key to remove.
     */
    public void remove(@NonNull AutofillId key) {
        int hash = mixHash(key);
        int index = hash & (mCapacity - 1);
        final AutofillId[] keys = mKeys;
        final int[] values = mValues;
        final int[] hashes = mHashes;
        final int mask = mCapacity - 1;

        while (keys[index] != null) {
            if (hashes[index] == hash && (keys[index] == key || keys[index].equals(key))) {
                int hole = index;
                int current = (hole + 1) & mask;

                // Shift elements backward to fill the hole without breaking collision chains
                while (keys[current] != null) {
                    int idealSlot = hashes[current] & mask;

                    // Circular distance check: is hole between idealSlot and current?
                    if (((hole - idealSlot) & mask) < ((current - idealSlot) & mask)) {
                        keys[hole] = keys[current];
                        values[hole] = values[current];
                        hashes[hole] = hashes[current];
                        hole = current;
                    }
                    current = (current + 1) & mask;
                }

                keys[hole] = null;
                values[hole] = 0;
                hashes[hole] = 0;
                mOccupied--;
                return;
            }
            index = (index + 1) & mask;
        }
    }

    private void resize() {
        if (mCapacity >= (1 << 30)) {
            throw new IllegalStateException(
                    "Map has reached maximum capacity and cannot be resized");
        }
        int newCapacity = mCapacity * 2;
        AutofillId[] newKeys = new AutofillId[newCapacity];
        int[] newValues = new int[newCapacity];
        int[] newHashes = new int[newCapacity];

        final int mask = newCapacity - 1;
        for (int i = 0; i < mCapacity; i++) {
            AutofillId key = mKeys[i];
            if (key != null) {
                int hash = mHashes[i];
                int index = hash & mask;
                while (newKeys[index] != null) {
                    index = (index + 1) & mask;
                }
                newKeys[index] = key;
                newValues[index] = mValues[i];
                newHashes[index] = hash;
            }
        }

        mKeys = newKeys;
        mValues = newValues;
        mHashes = newHashes;
        mCapacity = newCapacity;
    }

    /** Cheap JDK-style hash spreader. */
    private int mixHash(AutofillId key) {
        int h = key.hashCode();
        return h ^ (h >>> 16);
    }

    /**
     * Clears all mappings from this map.
     */
    public void clear() {
        Arrays.fill(mKeys, null);
        mOccupied = 0;
    }
}
