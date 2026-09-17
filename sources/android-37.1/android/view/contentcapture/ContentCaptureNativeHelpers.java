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

import dalvik.annotation.optimization.CriticalNative;
import dalvik.annotation.optimization.FastNative;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Provides JNI bindings for a futex-based inter-process lock on a shared ByteBuffer.
 * It also includes utility methods for direct memory operations on ByteBuffers via JNI.
 */
@Hide
public class ContentCaptureNativeHelpers {
    private static final String TAG = "ContentCaptureNative";

    // --- Native Lock Methods ---
    private static native void nativeLock(long address, int offset);

    @CriticalNative
    private static native void nativeUnlock(long address, int offset);

    @CriticalNative
    private static native boolean nativeTryLock(long address, int offset);

    // --- Native Memory Utility Methods ---
    /** Gets the direct address of a ByteBuffer. */
    @FastNative
    public static native long nativeGetDirectBufferAddress(ByteBuffer byteBuffer);

    /**
     * Writes the UTF-16 characters of a Java String directly into a ByteBuffer at a given offset.
     * @param address The destination direct ByteBuffer address.
     * @param offset The starting offset in the ByteBuffer (in bytes).
     * @param text The String to write.
     * @return The number of bytes written, or -1 on error.
     */
    @FastNative
    public static native int nativeWriteCharsToBuffer(long address, int offset,
            int capacity, String text, int textLength);

    @CriticalNative
    private static native void nativeMemcpy(long destAddress, long srcAddress, int length);
    // --- End Native Methods ---

    /**
     * Initializes the shared buffer by setting the futex and state to default values.
     * @param byteBuffer The direct ByteBuffer backed by shared memory.
     * @param offset The offset of the futex lock in the buffer.
     */
    public static void initialize(ByteBuffer byteBuffer, int offset) {
        if (byteBuffer == null || !byteBuffer.isDirect()) {
            throw new IllegalArgumentException("ByteBuffer must be direct and non-null");
        }
        if (offset < 0 || offset % 4 != 0) {
            throw new IllegalArgumentException("offset must be non-negative and 4-byte aligned");
        }
        byteBuffer.order(ByteOrder.nativeOrder());
        byteBuffer.putInt(offset, 0);
    }

    // --- Lock Methods ---

    /**
     * Acquires the inter-process lock.
     * <p>
     * This method will block until the lock is acquired. It uses a futex to efficiently
     * wait without busy-spinning. The lock is non-reentrant.
     *
     * @param address The direct memory address of the buffer.
     * @param offset The offset of the futex lock in the buffer.
     */
    public static void lock(long address, int offset) {
        validateLockArgs(address, offset);
        if (nativeTryLock(address, offset)) {
            return; // Fast path: Uncontended lock, skips normal JNI thread-transitions
        }
        nativeLock(address, offset); // Slow path: Blocks safely via standard JNI
    }

    /**
     * Releases the inter-process lock.
     *
     * @param address The direct memory address of the buffer.
     * @param offset The offset of the futex lock in the buffer.
     */
    public static void unlock(long address, int offset) {
        validateLockArgs(address, offset);
        nativeUnlock(address, offset);
    }

    /**
     * Attempts to acquire the inter-process lock without blocking.
     *
     * @param address The direct memory address of the buffer.
     * @param offset The offset of the futex lock in the buffer.
     * @return {@code true} if the lock was acquired, {@code false} otherwise.
     */
    public static boolean tryLock(long address, int offset) {
        validateLockArgs(address, offset);
        return nativeTryLock(address, offset);
    }

    private static void validateLockArgs(long address, int offset) {
        if (address == 0) {
            throw new IllegalArgumentException("address must not be zero");
        }
        if (offset < 0) {
            throw new IllegalArgumentException("offset must not be negative");
        }
        if ((address + offset) % 4 != 0) {
            throw new IllegalArgumentException("address + offset must be 4-byte aligned");
        }
    }

    /**
     * Copies a block of memory from a source direct address to a destination direct address.
     * This is a direct memory copy, similar to C's memcpy.
     *
     * @param destAddress The destination direct ByteBuffer address.
     * @param destCapacity The total capacity of the destination buffer.
     * @param srcAddress The source direct ByteBuffer address.
     * @param srcCapacity The total capacity of the source buffer.
     * @param length The number of bytes to copy.
     */
    public static void memcpy(long destAddress, int destCapacity, long srcAddress, int srcCapacity,
            int length) {
        if (destAddress == 0 || srcAddress == 0) {
            throw new IllegalArgumentException("Addresses must not be zero");
        }
        if (length < 0 || destCapacity < 0 || srcCapacity < 0) {
            throw new IllegalArgumentException("Capacities and length must be non-negative");
        }
        if (length > destCapacity || length > srcCapacity) {
            throw new IndexOutOfBoundsException("Length exceeds buffer capacity");
        }
        nativeMemcpy(destAddress, srcAddress, length);
    }
}
