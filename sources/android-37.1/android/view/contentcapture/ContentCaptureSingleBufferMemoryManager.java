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
import android.util.IntArray;
import android.util.Log;

import com.android.internal.annotations.VisibleForTesting;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * A highly optimized, stateful memory manager for the Content Capture shared memory buffer.
 *
 * <h1>Design & Architecture</h1>
 * <p>This class implements a custom <b>Single Free List First-Fit</b> algorithm combined with an
 * <i>O(1)</i> Node cache, tailored specifically for zero-allocation (on the Java heap),
 * cross-process
 * data synchronization. It bridges a "Producer" process (which writes UI changes) and a "Consumer"
 * process (which reads the UI tree), strictly isolating their access patterns to maximize
 * performance on the UI thread.
 *
 * <h2>1. Producer vs. Consumer Roles</h2>
 * <ul>
 *   <li><b>The Producer (Write-Path):</b> Maintains memory state entirely on the Java heap using
 *   primitive arrays ({@link android.util.IntArray}). By tracking free memory outside the
 *   {@link ByteBuffer}, the producer achieves rapid allocations and frees. It avoids expensive
 *   <i>O(N)</i> linear scans and avoids the JNI boundary-crossing overhead that would be incurred
 *   by delegating allocation state management to native C++ code.</li>
 *   <li><b>The Consumer (Read-Path):</b> Completely stateless. It does not know about free lists.
 *   It relies strictly on the structured block headers embedded directly in the native memory
 *   to linearly scan the buffer in a single pass.</li>
 * </ul>
 *
 * <h2>2. Core Design Decisions</h2>
 * <ul>
 *   <li><b>Contiguous Buffer & Boundary Tags:</b> The allocator is backed by a single contiguous
 *   {@link ByteBuffer}. Every block has a 4-byte header and a 4-byte footer. The footer is an
 *   exact replica of the header, enabling <i>O(1)</i> backward coalescing without scanning the
 *   buffer from the start.</li>
 *   <li><b>O(1) Node Cache (mNodeFreeList):</b> Fixed-size UI Node allocations are served from an
 *   <i>O(1)</i> free list ({@code mNodeFreeList}). A batch of these nodes is pre-allocated at
 *   initialization to handle initial UI element churn rapidly.</li>
 *   <li><b>Single Free List First-Fit (mGeneralFreeList):</b> Variable-sized allocations (strings,
 *   blobs) are served from a general free list ({@code mGeneralFreeList}) using a First-Fit search.
 *   If a found free chunk is larger than requested, it is actively split to minimize internal
 *   fragmentation. If no chunks fit the request, the allocator bumps from the high-water mark
 *   ({@code mArenaTop}).</li>
 * </ul>
 *
 * <h2>3. The Allocation Lifecycle & Memory Reclamation</h2>
 * <p>The behavior of this manager dynamically shifts as the application runs:
 * <ul>
 *   <li><b>Freeing & Aggressive Coalescing:</b> Freeing any block (even a node) triggers aggressive
 *   forward and backward coalescing with adjacent free blocks in the buffer. This is critical to
 *   combat external fragmentation.</li>
 *   <li><b>Node Abandonment:</b> If a freed node coalesces with a neighbor, it abandons the
 *   {@code mNodeFreeList} and merges into a larger chunk within {@code mGeneralFreeList}. This
 *   prevents fragmentation blockades. Conversely, if a freed node does <i>not</i> coalesce, it
 *   perfectly re-enters the {@code mNodeFreeList}.</li>
 *   <li><b>Arena Shrinking:</b> If a newly freed block (or coalesced block) touches the top-most
 *   block in the arena, it safely shrinks {@code mArenaTop}, moving the bump pointer backward.</li>
 * </ul>
 *
 * <h2>4. Out of Memory (OOM) Handling</h2>
 * <p>Because this manager operates within a fixed-size shared memory buffer, it can run out of
 * space. An OOM condition occurs when {@code mArenaTop + requestedSize > buffer.capacity()} (and no
 * suitable blocks are available in the free lists).
 * <p>When this happens, the allocation methods return {@code 0}. It is the responsibility of the
 * caller (the Content Capture framework) to handle this gracefully—typically by flushing the buffer
 * to the consumer, requesting a larger buffer, or dropping the current frame's incremental updates.
 *
 * <h2>5. Memory Layout Configuration</h2>
 *
 * <h3>Global Header (16 bytes)</h3>
 * <table border="1" style="border-collapse: collapse; text-align: left;">
 *   <tr>
 *     <th style="padding: 4px 8px;">Byte Offset</th>
 *     <th style="padding: 4px 8px;">Field</th>
 *     <th style="padding: 4px 8px;">Description</th>
 *   </tr>
 *   <tr>
 *     <td style="padding: 4px 8px;">0 - 3</td>
 *     <td style="padding: 4px 8px;">Arena Top</td>
 *     <td style="padding: 4px 8px;">High-water mark pointer for the Consumer</td>
 *   </tr>
 *   <tr>
 *     <td style="padding: 4px 8px;">4 - 7</td>
 *     <td style="padding: 4px 8px;">Misc Header</td>
 *     <td style="padding: 4px 8px;">Absolute offset to consumer's domain-specific state</td>
 *   </tr>
 *   <tr>
 *     <td style="padding: 4px 8px;">8 - 11</td>
 *     <td style="padding: 4px 8px;">Arena Start</td>
 *     <td style="padding: 4px 8px;">Absolute offset to the very first valid memory block</td>
 *   </tr>
 *   <tr>
 *     <td style="padding: 4px 8px;">12 - 15</td>
 *     <td style="padding: 4px 8px;">Node Size</td>
 *     <td style="padding: 4px 8px;">Static payload size for all UI Node blocks</td>
 *   </tr>
 * </table>
 *
 * <h3>Block Structure</h3>
 * <table border="1" style="border-collapse: collapse; text-align: left;">
 *   <tr>
 *     <th style="padding: 4px 8px;">Byte Offset</th>
 *     <th style="padding: 4px 8px;">Field</th>
 *     <th style="padding: 4px 8px;">Description</th>
 *   </tr>
 *   <tr>
 *     <td style="padding: 4px 8px;">0 - 3</td>
 *     <td style="padding: 4px 8px;">PackedSize</td>
 *     <td style="padding: 4px 8px;"><b>Bit 31:</b> IsFree flag (1=Free, 0=Allocated)<br>
 *     <b>Bits 0-30:</b> Total block size including headers</td>
 *   </tr>
 *   <tr>
 *     <td style="padding: 4px 8px;">4</td>
 *     <td style="padding: 4px 8px;">Type</td>
 *     <td style="padding: 4px 8px;">1 = NODE, 2 = STRING, 3 = BLOB</td>
 *   </tr>
 *   <tr>
 *     <td style="padding: 4px 8px;">5 - 7</td>
 *     <td style="padding: 4px 8px;">Padding</td>
 *     <td style="padding: 4px 8px;">Reserved / Alignment padding</td>
 *   </tr>
 *   <tr>
 *     <td style="padding: 4px 8px;">8...</td>
 *     <td style="padding: 4px 8px;">Payload</td>
 *     <td style="padding: 4px 8px;">The actual domain-specific data</td>
 *   </tr>
 *   <tr>
 *     <td style="padding: 4px 8px;">Size - 4</td>
 *     <td style="padding: 4px 8px;">Footer</td>
 *     <td style="padding: 4px 8px;">
 *       Exact replica of the 0-3 PackedSize for <i>O(1)</i> backward coalescing
 *     </td>
 *   </tr>
 * </table>
 *
 * <p><b>Thread Safety:</b> This class is <b>not</b> thread-safe for the producer. If accessed by
 * multiple threads concurrently (e.g., rendering threads vs. background syncing), it must be
 * externally synchronized.
 */
@Hide
@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public final class ContentCaptureSingleBufferMemoryManager {
    private static final String TAG = "CCSingleBufferMemMgr";

    // Allocated block types
    public static final byte BLOCK_TYPE_NODE = 1;
    public static final byte BLOCK_TYPE_STRING = 2;
    public static final byte BLOCK_TYPE_BLOB = 3;

    // Buffer Header Offsets
    private static final int ALLOCATOR_HEADER_SIZE = 8;
    private static final int HEADER_ARENA_TOP = 0;
    private static final int HEADER_ARENA_START = 4;
    private static final int MISC_HEADER_OFFSET = ALLOCATOR_HEADER_SIZE;

    // Block Metadata Offsets
    public static final int BLOCK_HEADER_SIZE_BYTES = 4;
    private static final int BLOCK_FOOTER_SIZE_BYTES = 4;
    public static final int BLOCK_OVERHEAD_BYTES =
            BLOCK_HEADER_SIZE_BYTES + BLOCK_FOOTER_SIZE_BYTES;

    /**
     * The absolute minimum physical size of a block (16 bytes).
     * An 8-byte block (just header + footer) is useless because 0-byte payloads are forbidden.
     * This constant is used as a threshold during chunk splitting to prevent leaving behind
     * un-allocatable 8-byte "ghost" remainder blocks in the free list.
     */
    private static final int MIN_BLOCK_SIZE = alignBytes(BLOCK_OVERHEAD_BYTES + 1); // 16 bytes
    private static final int PREALLOCATED_NODE_COUNT = 100;

    // Heap State (Producer only)
    private final ByteBuffer mBuffer;
    private final int mArenaStart;
    private final int mNodeSize;
    private int mArenaTop;

    // Free Lists (Heap only)
    private final IntArray mNodeFreeList = new IntArray();
    private final IntArray mGeneralFreeList = new IntArray();

    /**
     * Initializes the memory manager, formatting the buffer and pre-allocating nodes.
     *
     * @param buffer         The shared memory buffer.
     * @param miscHeaderSize Consumer's domain-specific state size (must be a multiple of 8).
     * @param nodeSize       Payload size for UI node blocks (must be a multiple of 8).
     * @throws IllegalArgumentException if nodeSize or miscHeaderSize is not a multiple of 8, or if
     *                                  the buffer is too small.
     */
    public ContentCaptureSingleBufferMemoryManager(@NonNull ByteBuffer buffer, int miscHeaderSize,
            int nodeSize) {
        if ((nodeSize & 7) != 0) {
            throw new IllegalArgumentException("nodeSize must be a multiple of 8: " + nodeSize);
        }
        if ((miscHeaderSize & 7) != 0) {
            throw new IllegalArgumentException(
                    "miscHeaderSize must be a multiple of 8: " + miscHeaderSize);
        }

        mBuffer = buffer;
        mBuffer.order(ByteOrder.nativeOrder());

        // mArenaStart is the start offset of the first dynamic block. Since the block header
        // is 4 bytes (BLOCK_HEADER_SIZE_BYTES), and we want the block's payload to start on
        // a perfect 8-byte boundary, we must offset the block's starting address by 4 bytes.
        // For example, if MISC_HEADER_OFFSET + miscHeaderSize = 16 (multiple of 8), the block
        // starts at 20, and the payload starts at 24 (which is 8-byte aligned).
        mArenaStart = MISC_HEADER_OFFSET + miscHeaderSize + BLOCK_HEADER_SIZE_BYTES;
        mNodeSize = nodeSize;
        mArenaTop = mArenaStart;

        if (mBuffer.capacity() < mArenaStart) {
            throw new IllegalArgumentException("Buffer too small for arena start: " + mArenaStart);
        }

        mBuffer.putInt(HEADER_ARENA_START, mArenaStart);

        preallocateNodes();
        commit();
    }

    private void preallocateNodes() {
        // no need to align to 8 bytes since both node size and block overhead bytes are guaranteed
        // to be multiples of 8.
        int nodeBlockSize = mNodeSize + BLOCK_OVERHEAD_BYTES;
        int requiredSpace = PREALLOCATED_NODE_COUNT * nodeBlockSize;

        if (mBuffer.capacity() < mArenaTop + requiredSpace) {
            Log.w(TAG, "Buffer too small for node pre-allocation");
            return;
        }

        for (int i = 0; i < PREALLOCATED_NODE_COUNT; i++) {
            writeBlockMetadata(mArenaTop, nodeBlockSize, BLOCK_TYPE_NODE, true);
            mNodeFreeList.add(mArenaTop);
            mArenaTop += nodeBlockSize;
        }
    }

    /** Returns the offset of the domain-specific misc header. */
    public int getMiscHeaderOffset() {
        return MISC_HEADER_OFFSET;
    }

    /**
     * Commits the current state of the memory manager by writing the arena top
     * to the global header in the ByteBuffer.
     */
    public void commit() {
        mBuffer.putInt(HEADER_ARENA_TOP, mArenaTop);
    }

    /**
     * Moves the memory manager and all existing data to a new, larger buffer.
     * This is useful when the producer needs to dynamically expand the shared memory space.
     *
     * @param newBuffer The new, larger buffer to migrate into.
     * @return A new MemoryManager instance bound to the new buffer.
     * @throws IllegalArgumentException if the new buffer is not larger than the current buffer.
     */
    public ContentCaptureSingleBufferMemoryManager moveTo(@NonNull ByteBuffer newBuffer) {
        if (newBuffer.capacity() <= mBuffer.capacity()) {
            throw new IllegalArgumentException(
                    "New buffer must be strictly larger than the current buffer.");
        }

        // Create new instance bypassing initialization using the private constructor
        ContentCaptureSingleBufferMemoryManager newManager =
                new ContentCaptureSingleBufferMemoryManager(
                        newBuffer, mNodeSize, mArenaStart, mArenaTop);

        // Copy physical memory exactly up to the current high-water mark
        int oldPosition = mBuffer.position();
        int oldLimit = mBuffer.limit();

        mBuffer.position(0);
        mBuffer.limit(mArenaTop);
        newBuffer.position(0);
        newBuffer.put(mBuffer);

        // Restore original buffer state
        mBuffer.limit(oldLimit);
        mBuffer.position(oldPosition);

        // Copy the heap state explicitly
        newManager.mNodeFreeList.addAll(this.mNodeFreeList);
        newManager.mGeneralFreeList.addAll(this.mGeneralFreeList);

        newManager.commit();
        return newManager;
    }

    // Private constructor for fast cloning during moveTo without re-initializing headers or nodes
    private ContentCaptureSingleBufferMemoryManager(
            @NonNull ByteBuffer buffer, int nodeSize, int arenaStart, int arenaTop) {
        mBuffer = buffer;
        mBuffer.order(ByteOrder.nativeOrder());
        mNodeSize = nodeSize;
        mArenaStart = arenaStart;
        mArenaTop = arenaTop;
    }

    /**
     * Allocates a fixed-size UI Node block from the buffer.
     *
     * <p>This method utilizes a dedicated slab allocator ({@code mNodeFreeList}) to provide
     * strictly <i>O(1)</i> allocation times. Node blocks are served from this cache when available
     * to minimize external fragmentation.
     *
     * <p><b>Important:</b> This method defers writing the arena high-water mark to the shared
     * buffer. You <b>MUST</b> call {@link #commit()} at the end of your write batch before
     * notifying the Consumer process.
     *
     * @return The absolute byte offset to the start of the payload. The caller should write
     *         exactly {@code mNodeSize} bytes starting at this offset. Returns {@code 0} if
     *         the buffer is out of memory.
     */
    public int allocateNode() {
        if (mNodeFreeList.size() > 0) {
            int blockOffset = mNodeFreeList.get(mNodeFreeList.size() - 1);
            mNodeFreeList.remove(mNodeFreeList.size() - 1);

            int packedSize = mBuffer.getInt(blockOffset);
            writeBlockMetadata(
                    blockOffset, unpackSize(packedSize), BLOCK_TYPE_NODE, false);
            return blockOffset + BLOCK_HEADER_SIZE_BYTES;
        }
        return allocateGeneral(mNodeSize, BLOCK_TYPE_NODE);
    }

    /**
     * Allocates a block specifically for a String.
     * The manager automatically reserves and writes a 4-byte length prefix at the start
     * of the payload, allowing the consumer to read it safely despite 8-byte alignment rounding.
     *
     * @param stringByteLength The exact byte length of the encoded string.
     * @return The payload offset. The caller should write the actual string bytes
     *         starting at {@code offset + 4}.
     */
    public int allocateString(int stringByteLength) {
        if (stringByteLength <= 0) return 0;
        // Reserve 4 extra bytes at the front for the length prefix
        int totalPayloadSize = 4 + stringByteLength;

        int payloadOffset = allocateGeneral(totalPayloadSize, BLOCK_TYPE_STRING);

        if (payloadOffset != 0) {
            // The manager writes the domain-specific length prefix for the consumer
            mBuffer.putInt(payloadOffset, stringByteLength);
        }

        return payloadOffset;
    }

    /**
     * Allocates a dynamically sized memory block for a binary blob.
     *
     * <p>Like all allocations, blobs are rounded up to the mandatory 8-byte CPU alignment boundary.
     * Unlike strings, no length prefix is written by the allocator.
     *
     * <p><b>Important:</b> This method defers writing the arena high-water mark to the shared
     * buffer. You <b>MUST</b> call {@link #commit()} at the end of your write batch before
     * notifying the Consumer process.
     *
     * @param payloadSize The exact number of bytes required for the blob payload.
     * @return The absolute byte offset to the start of the payload. Returns {@code 0} if
     *         the buffer is out of memory.
     */
    public int allocateBlob(int payloadSize) {
        if (payloadSize <= 0 || payloadSize > Integer.MAX_VALUE - BLOCK_OVERHEAD_BYTES - 7) {
            return 0;
        }
        return allocateGeneral(payloadSize, BLOCK_TYPE_BLOB);
    }

    private int allocateGeneral(int payloadSize, byte type) {
        if (payloadSize <= 0 || payloadSize > Integer.MAX_VALUE - BLOCK_OVERHEAD_BYTES - 7) {
            return 0;
        }

        int requiredBlockSize = alignBytes(payloadSize + BLOCK_OVERHEAD_BYTES);
        if (requiredBlockSize < MIN_BLOCK_SIZE) {
            requiredBlockSize = MIN_BLOCK_SIZE;
        }

        // 1. Search mGeneralFreeList for First-Fit
        for (int i = 0; i < mGeneralFreeList.size(); i++) {
            int blockOffset = mGeneralFreeList.get(i);
            int packed = mBuffer.getInt(blockOffset);
            int blockSize = unpackSize(packed);

            if (blockSize >= requiredBlockSize) {
                mGeneralFreeList.remove(i);

                if (blockSize >= requiredBlockSize + MIN_BLOCK_SIZE) {
                    // Split
                    int remainderOffset = blockOffset + requiredBlockSize;
                    int remainderSize = blockSize - requiredBlockSize;

                    // Format remainder
                    writeBlockMetadata(remainderOffset, remainderSize, BLOCK_TYPE_BLOB, true);
                    mGeneralFreeList.add(remainderOffset);

                    // Update block size for allocated part
                    blockSize = requiredBlockSize;
                }

                writeBlockMetadata(blockOffset, blockSize, type, false);
                return blockOffset + BLOCK_HEADER_SIZE_BYTES;
            }
        }

        // 2. Bump Allocation Fallback
        if (mArenaTop + requiredBlockSize > mBuffer.capacity()) {
            return 0; // OOM
        }

        int blockOffset = mArenaTop;
        writeBlockMetadata(blockOffset, requiredBlockSize, type, false);
        mArenaTop += requiredBlockSize;

        return blockOffset + BLOCK_HEADER_SIZE_BYTES;
    }

    /**
     * Frees a previously allocated memory block, allowing it to be recycled.
     *
     * <p>This method automatically reads the physical headers and footers of adjacent blocks
     * in the native buffer to perform <i>O(1)</i> forward and backward coalescing. If adjacent
     * blocks are also free, they are merged into a single larger block to prevent memory
     * fragmentation.
     *
     * <p>If a {@link #BLOCK_TYPE_NODE} block coalesces with a neighbor, it abandons the
     * {@code mNodeFreeList} and enters the {@code mGeneralFreeList} as part of the
     * larger coalesced block.
     *
     * <p><b>Important:</b> If this method reclaims memory at the very end of the active arena,
     * it shrinks {@code mArenaTop} but defers writing it to the shared buffer. You <b>MUST</b> call
     * {@link #commit()} at the end of your write batch before notifying the Consumer process.
     *
     * @param payloadOffset The exact payload offset originally returned by an
     *                      {@code allocate} method. Passing {@code 0} is a safe no-op.
     *                      Double-frees are silently ignored.
     */
    public void free(int payloadOffset) {
        if (payloadOffset == 0) return;

        int blockOffset = payloadOffset - BLOCK_HEADER_SIZE_BYTES;
        int packed = mBuffer.getInt(blockOffset);

        if (unpackIsFree(packed)) return;

        byte type = unpackType(packed);
        int blockSize = unpackSize(packed);
        int newBlockOffset = blockOffset;
        boolean coalesced = false;

        // Forward Coalescing
        int nextBlockOffset = blockOffset + blockSize;
        if (nextBlockOffset < mArenaTop) {
            int nextPacked = mBuffer.getInt(nextBlockOffset);
            if (unpackIsFree(nextPacked)) {
                int nextSize = unpackSize(nextPacked);
                blockSize += nextSize;
                coalesced = true;
                removeFromFreeLists(nextBlockOffset);
            }
        }

        // Backward Coalescing
        if (blockOffset > mArenaStart) {
            int prevPacked = mBuffer.getInt(blockOffset - BLOCK_FOOTER_SIZE_BYTES);
            if (unpackIsFree(prevPacked)) {
                int prevSize = unpackSize(prevPacked);
                newBlockOffset = blockOffset - prevSize;
                blockSize += prevSize;
                coalesced = true;
                removeFromFreeLists(newBlockOffset);
            }
        }

        writeBlockMetadata(newBlockOffset, blockSize, type, true);

        // Reclaim physical memory if we freed the top-most block
        if (newBlockOffset + blockSize == mArenaTop) {
            mArenaTop = newBlockOffset;
            // Note: commit() is deferred.
            return;
        }

        // If isolated node, goes to mNodeFreeList
        if (!coalesced && type == BLOCK_TYPE_NODE) {
            mNodeFreeList.add(newBlockOffset);
        } else {
            mGeneralFreeList.add(newBlockOffset);
        }
    }

    private void removeFromFreeLists(int blockOffset) {
        int idx = mNodeFreeList.indexOf(blockOffset);
        if (idx >= 0) {
            mNodeFreeList.remove(idx);
            return;
        }
        idx = mGeneralFreeList.indexOf(blockOffset);
        if (idx >= 0) {
            mGeneralFreeList.remove(idx);
        }
    }

    // ==================================================================================
    // BIT-PACKING METADATA
    // ==================================================================================

    private void writeBlockMetadata(int offset, int size, byte type, boolean isFree) {
        int packed = packMetadata(size, type, isFree);
        mBuffer.putInt(offset, packed);
        mBuffer.putInt(offset + size - BLOCK_FOOTER_SIZE_BYTES, packed);
    }

    /**
     * Aligns the given size to the next multiple of 8.
     *
     * <p>8-byte alignment is a critical optimization for platform performance on ARM64 mobile CPUs:
     * <ul>
     *   <li><b>Cache Line & Page Boundaries:</b> Unaligned memory accesses that cross cache line
     *   boundaries (64 bytes) or memory page boundaries (4 KB / 16 KB) require multiple memory
     *   accesses and CPU cycles to merge split values, leading to jank on the UI thread.</li>
     *   <li><b>SIMD & Vectorization:</b> Native bulk operations (e.g. copying payloads or strings
     *   via NEON) require aligned data. Unaligned pointers force compile/runtime fallbacks to slow,
     *   byte-by-byte loops, or trigger SIGBUS alignment faults.</li>
     *   <li><b>JVM/ART Intrinsics:</b> Direct ByteBuffer operations (like `putInt` or `getInt`)
     *   rely on hardware-aligned access to bypass runtime checking and use optimal CPU
     *   intrinsics.</li>
     *   <li><b>Atomic Operations:</b> Concurrent atomic operations and memory fences on
     *   shared memory buffers are strictly required to be aligned to their data size
     *   (4 or 8 bytes) to avoid exclusive monitor failures or hardware trap lockups.</li>
     * </ul>
     *
     * @param size The size in bytes to align.
     * @return The aligned size (a multiple of 8).
     */
    public static int alignBytes(int size) {
        return (size + 7) & ~7;
    }

    private static int packMetadata(int size, byte type, boolean isFree) {
        return (isFree ? 0x80000000 : 0) | ((type & 0x03) << 29) | (size & 0x1FFFFFFF);
    }

    private static int unpackSize(int packed) {
        return packed & 0x1FFFFFFF;
    }

    private static byte unpackType(int packed) {
        return (byte) ((packed >>> 29) & 0x03);
    }

    private static boolean unpackIsFree(int packed) {
        return (packed & 0x80000000) != 0;
    }

    // ==================================================================================
    // CONSUMER READ-ONLY APIS
    // ==================================================================================

    /**
     * Gets the starting offset for the consumer to begin scanning.
     */
    public static int getArenaStart(@NonNull ByteBuffer buffer) {
        return buffer.getInt(HEADER_ARENA_START);
    }

    /**
     * Fast-path scanner for the consumer. Skips free blocks and returns the next payload.
     *
     * @param buffer             The shared memory buffer.
     * @param currentBlockOffset Pass getArenaStart() for the first call, and
     *                           {@code (previousPayload - BLOCK_HEADER_SIZE_BYTES
     *                           + previousBlockSize)} for subsequent.
     * @return The payload offset of the next valid block, or 0 if end of arena.
     */
    public static int getNextAllocatedPayload(@NonNull ByteBuffer buffer, int currentBlockOffset) {
        int arenaTop = buffer.getInt(HEADER_ARENA_TOP);

        while (currentBlockOffset < arenaTop) {
            int packedSize = buffer.getInt(currentBlockOffset);
            int blockSize = unpackSize(packedSize);

            if (!unpackIsFree(packedSize)) {
                return currentBlockOffset + BLOCK_HEADER_SIZE_BYTES;
            }
            currentBlockOffset += blockSize;
        }
        return 0;
    }

    /** Returns the type of the block at the given payload offset. */
    public static byte getBlockType(@NonNull ByteBuffer buffer, int payloadOffset) {
        return unpackType(buffer.getInt(payloadOffset - BLOCK_HEADER_SIZE_BYTES));
    }

    /**
     * Returns the total size of the block (including header/footer) at the given payload
     * offset.
     */
    public static int getBlockSize(@NonNull ByteBuffer buffer, int payloadOffset) {
        int packedSize = buffer.getInt(payloadOffset - BLOCK_HEADER_SIZE_BYTES);
        return unpackSize(packedSize);
    }

    /**
     * Returns the exact logical byte length of a string block.
     *
     * @param buffer The shared memory buffer.
     * @param payloadOffset The offset returned by getNextAllocatedPayload().
     * @return The length of the string in bytes.
     * @throws IllegalArgumentException if the block is not a STRING type.
     */
    public static int getStringLength(@NonNull ByteBuffer buffer, int payloadOffset) {
        if (getBlockType(buffer, payloadOffset) != BLOCK_TYPE_STRING) {
            throw new IllegalArgumentException(
                    "Block at offset " + payloadOffset + " is not a string.");
        }
        // The exact length was written into the first 4 bytes of the payload
        return buffer.getInt(payloadOffset);
    }

    /** Validates physical block formatting inside the buffer. */
    @VisibleForTesting
    public static void verifyIntegrity(@NonNull ByteBuffer buffer) {
        int arenaTop = buffer.getInt(HEADER_ARENA_TOP);
        if (arenaTop > buffer.capacity()) {
            throw new AssertionError("Arena top exceeds buffer capacity");
        }

        int arenaStart = buffer.getInt(HEADER_ARENA_START);
        int currentOffset = arenaStart;

        while (currentOffset < arenaTop) {
            int packedSize = buffer.getInt(currentOffset);
            int blockSize = unpackSize(packedSize);

            if (blockSize < MIN_BLOCK_SIZE) {
                throw new AssertionError("Unaligned/invalid size at " + currentOffset);
            }
            if (currentOffset + blockSize > arenaTop) {
                throw new AssertionError(
                        "Block size exceeds arena boundaries at " + currentOffset);
            }

            int footerPackedSize = buffer.getInt(
                    currentOffset + blockSize - BLOCK_FOOTER_SIZE_BYTES);
            if (packedSize != footerPackedSize) {
                throw new AssertionError("Header/Footer mismatch at " + currentOffset);
            }

            currentOffset += blockSize;
        }

        if (currentOffset != arenaTop) {
            throw new AssertionError("Arena physical scan did not end at arenaTop");
        }
    }
}
