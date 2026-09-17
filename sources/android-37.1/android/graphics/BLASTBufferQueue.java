/*
 * Copyright (C) 2019 The Android Open Source Project
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

package android.graphics;

import android.annotation.Hide;
import android.annotation.NonNull;
import android.gui.IBLASTBufferQueue;
import android.gui.ITransactionHangCallback;
import android.gui.ITransactionReadyCallback;
import android.os.IBinder;
import android.os.Parcel;
import android.view.Surface;
import android.view.SurfaceControl;

import java.lang.ref.Reference;
import java.util.function.Consumer;

@Hide
@android.ravenwood.annotation.RavenwoodKeepWholeClass
public final class BLASTBufferQueue {
    // Note: This field is accessed by native code.
    public long mNativeObject; // BLASTBufferQueue*

    private static native long nativeCreate(String name, boolean updateDestinationFrame);
    private static native void nativeDestroy(long ptr);
    private static native Surface nativeGetSurface(long ptr, boolean includeSurfaceControlHandle);
    private static native boolean nativeSyncNextTransaction(long ptr,
            ITransactionReadyCallback callback, boolean acquireSingleBuffer);
    private static native void nativeStopContinuousSyncTransaction(long ptr);
    private static native void nativeClearSyncTransaction(long ptr);
    private static native void nativeUpdate(long ptr, long surfaceControl, long width, long height,
            int format);
    private static native void nativeMergeWithNextTransaction(long ptr, long transactionPtr,
                                                              long frameNumber);
    private static native long nativeGetLastAcquiredFrameNum(long ptr);
    private static native void nativeApplyPendingTransactions(long ptr, long frameNumber);
    private static native boolean nativeIsSameSurfaceControl(long ptr, long surfaceControlPtr);
    private static native SurfaceControl.Transaction nativeGatherPendingTransactions(long ptr,
            long frameNumber);
    private static native void nativeSetTransactionHangCallback(long ptr,
            ITransactionHangCallback callback);
    private static native void nativeSetApplyToken(long ptr, IBinder applyToken);
    private static native void nativeSetWaitForBufferReleaseCallback(long ptr,
            WaitForBufferReleaseCallback callback);

    private static native void nativeSetCornerRadiiCallback(
            long ptr, CornerRadiiCallback callback);
    private static native IBinder nativeGetIBinder(long nativeObject);

    private static native boolean nativeAidlSyncNextTransaction(long ptr,
            ITransactionReadyCallback callback, boolean acquireSingleBuffer);
    private static native void nativeAidlStopContinuousSyncTransaction(long ptr);
    private static native void nativeAidlUpdate(
            long ptr, long surfaceControl, long width, long height, int format);
    private static native void nativeAidlMergeWithNextTransaction(long ptr, long transactionPtr);
    private static native void nativeAidlSetRemoteForTest(long ptr, IBinder remoteBBQ);
    private static native Surface nativeAidlGetSurfaceForPixelCopy(long ptr);

    public interface WaitForBufferReleaseCallback {
        /**
         * Indicates that the client is waiting on buffer release
         * due to no free buffers being available to render into.
         * @param durationNanos The length of time in nanoseconds
         * that the client was blocked on buffer release.
         */
        void onWaitForBufferRelease(long durationNanos);
    }

    public interface CornerRadiiCallback {
        /**
         * Indicates that the client is waiting on buffer release
         * due to no free buffers being available to render into.
         * @param cornerRadii The length of time in nanoseconds
         * that the client was blocked on buffer release.
         */
        void onCornerRadiiChanged(float[] cornerRadii);
    }

    /** Create a new connection with the surface flinger. */
    public BLASTBufferQueue(String name, boolean updateDestinationFrame) {
        mNativeObject = nativeCreate(name, updateDestinationFrame);
    }

    public void destroy() {
        nativeDestroy(mNativeObject);
        mNativeObject = 0;
    }

    /**
     * @return a new Surface instance from the IGraphicsBufferProducer of the adapter.
     */
    public Surface createSurface() {
        return nativeGetSurface(mNativeObject, false /* includeSurfaceControlHandle */);
    }

    /**
     * @return a new Surface instance from the IGraphicsBufferProducer of the adapter and
     * the SurfaceControl handle.
     */
    public Surface createSurfaceWithHandle() {
        return nativeGetSurface(mNativeObject, true /* includeSurfaceControlHandle */);
    }

    /**
     * Send a callback that accepts a transaction to BBQ. BBQ will acquire buffers into the a
     * transaction it created and will eventually send the transaction into the callback when it is
     * ready.
     *
     * @param callback The callback invoked when the buffer has been added to the transaction. The
     *     callback will contain the transaction with the buffer.
     * @param acquireSingleBuffer If true, only acquire a single buffer when processing frames. The
     *     callback will be cleared once a single buffer has been acquired. If false, continue to
     *     acquire all buffers into the transaction until stopContinuousSyncTransaction is called.
     */
    public boolean syncNextTransaction(
            boolean acquireSingleBuffer, @NonNull ITransactionReadyCallback callback) {
        return nativeSyncNextTransaction(mNativeObject, callback, acquireSingleBuffer);
    }

    /**
     * Send a callback that accepts a transaction to BBQ. BBQ will acquire buffers into the a
     * transaction it created and will eventually send the transaction into the callback when it is
     * ready.
     *
     * @param callback The callback invoked when the buffer has been added to the transaction. The
     *     callback will contain the transaction with the buffer.
     */
    public boolean syncNextTransaction(@NonNull Consumer<SurfaceControl.Transaction> callback) {
        ITransactionReadyCallback transactionReadyCallback =
                new ITransactionReadyCallback.Stub() {
                    @Override
                    public void onTransactionReady(SurfaceControl.Transaction transaction) {
                        callback.accept(transaction);
                    }
                };
        return syncNextTransaction(true /* acquireSingleBuffer */, transactionReadyCallback);
    }

    /**
     * Tell BBQ to stop acquiring buffers into a single transaction. BBQ will send the sync
     * transaction callback after this has been called. This should only be used when
     * syncNextTransaction was called with acquireSingleBuffer set to false.
     */
    public void stopContinuousSyncTransaction() {
        nativeStopContinuousSyncTransaction(mNativeObject);
    }

    /**
     * Tell BBQ to clear the sync transaction that was previously set. The callback will not be
     * invoked when the next frame is acquired.
     */
    public void clearSyncTransaction() {
        nativeClearSyncTransaction(mNativeObject);
    }

    /**
     * Updates {@link SurfaceControl}, size, and format for a particular BLASTBufferQueue
     * @param sc The new SurfaceControl that this BLASTBufferQueue will update
     * @param width The new width for the buffer.
     * @param height The new height for the buffer.
     * @param format The new format for the buffer.
     */
    public void update(SurfaceControl sc, int width, int height, @PixelFormat.Format int format) {
        nativeUpdate(mNativeObject, sc.mNativeObject, width, height, format);
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mNativeObject != 0) {
                nativeDestroy(mNativeObject);
            }
        } finally {
            super.finalize();
        }
    }

    /**
     * Merge the transaction passed in to the next transaction in BlastBufferQueue. The next
     * transaction will be applied or merged when the next frame with specified frame number
     * is available.
     */
    public void mergeWithNextTransaction(SurfaceControl.Transaction t, long frameNumber) {
        nativeMergeWithNextTransaction(mNativeObject, t.mNativeObject, frameNumber);
    }

    /**
     * Merge the transaction passed in to the next transaction in BlastBufferQueue.
     * @param nativeTransaction native handle passed from native c/c++ code.
     */
    public void mergeWithNextTransaction(long nativeTransaction, long frameNumber) {
        nativeMergeWithNextTransaction(mNativeObject, nativeTransaction, frameNumber);
    }

    /**
     * Apply any transactions that were passed to {@link #mergeWithNextTransaction} with the
     * specified frameNumber. This is intended to ensure transactions don't get stuck as pending
     * if the specified frameNumber is never drawn.
     *
     * @param frameNumber The frameNumber used to determine which transactions to apply.
     */
    public void applyPendingTransactions(long frameNumber) {
        nativeApplyPendingTransactions(mNativeObject, frameNumber);
    }

    public long getLastAcquiredFrameNum() {
        return nativeGetLastAcquiredFrameNum(mNativeObject);
    }

    /**
     * @return True if the associated SurfaceControl has the same handle as {@code sc}.
     */
    public boolean isSameSurfaceControl(SurfaceControl sc) {
        return nativeIsSameSurfaceControl(mNativeObject, sc.mNativeObject);
    }

    /**
     * Get any transactions that were passed to {@link #mergeWithNextTransaction} with the
     * specified frameNumber. This is intended to ensure transactions don't get stuck as pending
     * if the specified frameNumber is never drawn.
     *
     * @param frameNumber The frameNumber used to determine which transactions to apply.
     * @return a Transaction that contains the merge of all the transactions that were sent to
     *         mergeWithNextTransaction
     */
    public SurfaceControl.Transaction gatherPendingTransactions(long frameNumber) {
        return nativeGatherPendingTransactions(mNativeObject, frameNumber);
    }

    public void setTransactionHangCallback(ITransactionHangCallback hangCallback) {
        nativeSetTransactionHangCallback(mNativeObject, hangCallback);
    }

    public void setApplyToken(IBinder applyToken) {
        nativeSetApplyToken(mNativeObject, applyToken);
    }

    /**
     * Propagate callback about being blocked on buffer release.
     */
    public void setWaitForBufferReleaseCallback(WaitForBufferReleaseCallback waitCallback) {
        nativeSetWaitForBufferReleaseCallback(mNativeObject, waitCallback);
    }

     /**
     * Propagate callback to receive corner radii on the Surface.
     */
    public void setCornerRadiiCallback(CornerRadiiCallback callback) {
        nativeSetCornerRadiiCallback(mNativeObject, callback);
    }

    public android.gui.IBLASTBufferQueue getIBlastBufferQueue() {
        try {
            IBinder binder = nativeGetIBinder(mNativeObject);
            if (binder == null) {
                return null;
            }
            return android.gui.IBLASTBufferQueue.Stub.asInterface(binder);
        } finally {
            // Hold a reference to prevent the finalizer from running before the native method
            // is complete (see https://errorprone.info/bugpattern/UnsafeFinalization)
            Reference.reachabilityFence(this);
        }
    }

    /**
     * Send a callback that accepts a transaction to BBQ, directly using the AIDL method.
     */
    public boolean aidlSyncNextTransaction(
            boolean acquireSingleBuffer, @NonNull ITransactionReadyCallback callback) {
        return nativeAidlSyncNextTransaction(mNativeObject, callback, acquireSingleBuffer);
    }

    /**
     * Tell BBQ to stop acquiring buffers into a single transaction, directly using the AIDL method.
     */
    public void aidlStopContinuousSyncTransaction() {
        nativeAidlStopContinuousSyncTransaction(mNativeObject);
    }

    /**
     * Updates {@link SurfaceControl}, size, and format for a particular BLASTBufferQueue, directly
     * using the AIDL method.
     */
    public void aidlUpdate(
            SurfaceControl sc, int width, int height, @PixelFormat.Format int format) {
        nativeAidlUpdate(mNativeObject, sc.mNativeObject, width, height, format);
    }

    /**
     * Merge the transaction passed in to the next transaction in BlastBufferQueue, directly using
     * the AIDL method.
     */
    public void aidlMergeWithNextTransaction(SurfaceControl.Transaction t) {
        nativeAidlMergeWithNextTransaction(mNativeObject, t.mNativeObject);
    }

    /**
     * Sets a remote IBLASTBufferQueue, directly using the AIDL method.
     */
    @Hide
    public void setRemoteForTest(IBLASTBufferQueue remoteBBQ) {
        nativeAidlSetRemoteForTest(mNativeObject, remoteBBQ.asBinder());
    }

    /**
     * @return a new Surface instance to be used for PixelCopy.
     */
    @Hide
    public Surface getSurfaceForPixelCopy() {
        try {
            return nativeAidlGetSurfaceForPixelCopy(mNativeObject);
        } finally {
            Reference.reachabilityFence(this);
        }
    }
}
