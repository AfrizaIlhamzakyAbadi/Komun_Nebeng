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

package android.graphics;

import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.text.TextUtils;
import android.util.LruCache;

import com.android.graphics.flags.Flags;
import com.android.internal.annotations.VisibleForTesting;

import java.lang.ref.WeakReference;
import java.util.function.Supplier;
import java.util.Map;
import java.util.Objects;

/**
 * A framework-internal cache designed to minimize redundant ashmem allocations
 * for bitmaps during Inter-Process Communication (IPC).
 *
 * <p>When `Bitmap#asShared()` is called on a same heap bitmap multiple times, a
 * different ashmem bitmap is created and returned every time. This can be cached
 * and reused if the heap bitmap content remains the same.
 *
 * <p>When transferring large bitmaps (e.g., via {@link android.media.MediaMetadata}
 * or {@link android.app.Notification}), the system converts heap bitmaps into
 * ashmem-backed shared bitmaps. Without caching, sending the same heap bitmap
 * multiple times—or scaling it down for different consumers results in multiple
 * copies of the same bitmap pixel data in ashmem.
 *
 * <p>{@code BitmapCache} mitigates this by tracking the outbound (shared/scaled)
 * bitmaps. It holds bitmaps via {@link java.lang.ref.WeakReference}, allowing
 * normal garbage collection when the bitmaps are no longer strongly referenced
 * by the application.
 */
@Hide
@android.ravenwood.annotation.RavenwoodKeepWholeClass
public class BitmapCache {
    private static final String TAG = "BitmapCache";

    private static final int DEFAULT_CACHE_SIZE = 16;

    private static final class Key {
        private final int scaledWidth;
        private final int scaledHeight;
        private final int generationId;
        private final WeakReference<Bitmap> sourceBitmap;
        private final int hashCode;

        Key(@NonNull Bitmap bmp) {
            this(bmp, bmp.getWidth(), bmp.getHeight());
        }

        Key(@NonNull Bitmap bmp, int scaledWidth, int scaledHeight) {
            this.sourceBitmap = new WeakReference<>(bmp);
            this.scaledWidth = scaledWidth;
            this.scaledHeight = scaledHeight;
            this.generationId = bmp.getGenerationId();
            this.hashCode = Objects.hash(bmp, generationId, scaledWidth, scaledHeight);
        }

        boolean stale() {
            Bitmap source = sourceBitmap.get();
            return source == null || source.getGenerationId() != generationId;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (other == null || getClass() != other.getClass()) {
                return false;
            }
            Key o = (Key) other;
            Bitmap b = sourceBitmap.get();
            Bitmap ob = o.sourceBitmap.get();
            return b != null && b == ob
                    && scaledWidth == o.scaledWidth
                    && scaledHeight == o.scaledHeight
                    && generationId == o.generationId;
        }

        @Override
        public int hashCode() {
            return this.hashCode;
        }

        @Override
        public final String toString() {
            return TextUtils.formatSimple("Key[src=%s,w=%d,h=%d,gen=%d]",
                String.valueOf(sourceBitmap.get()), scaledWidth, scaledHeight, generationId);
        }
    }

    private static final class Value {
        private WeakReference<Bitmap> bitmap;

        Value(Bitmap bmp) {
            this.bitmap = new WeakReference<>(bmp);
        }

        Bitmap get() {
            return bitmap.get();
        }

        @Override
        public final String toString() {
            return TextUtils.formatSimple("Value[bmp=%s]", String.valueOf(bitmap.get()));
        }
    }

    private static final LruCache<Key, Value> sSharedBitmaps = new LruCache<>(DEFAULT_CACHE_SIZE);

    @Hide
    @VisibleForTesting
    public static LruCache<Key, Value> getSharedBitmapCache() {
        return sSharedBitmaps;
    }

    /**
     * Iterates over the cache and removes empty wrapper objects.
     * To avoid allocation overhead, this is only triggered when the cache reaches capacity.
     */
    private static void purgeDeadEntries() {
        LruCache<Key, Value> cache = sSharedBitmaps;
        if (cache.size() < cache.maxSize()) {
            return;
        }
        for (Map.Entry<Key, Value> entry : cache.snapshot().entrySet()) {
            if (entry.getKey().stale() || entry.getValue().get() == null) {
                cache.remove(entry.getKey());
            }
        }
    }

    @Nullable
    private static Bitmap getShared(@NonNull Key k, Supplier<Bitmap> supplier) {
        Value v = sSharedBitmaps.get(k);
        if (v != null) {
            if (v.get() != null) {
                return v.get();
            }
            sSharedBitmaps.remove(k);
        }
        purgeDeadEntries();
        Bitmap shared = supplier.get();
        sSharedBitmaps.put(k, new Value(shared));
        return shared;
    }

    /**
     * Returns a shared (ashmem-backed) version of the bitmap if it exists in the cache.
     */
    @Nullable
    public static Bitmap getShared(@NonNull Bitmap bitmap) {
        if (!Flags.cacheAshmemBitmaps()) {
            return bitmap.asShared();
        }
        return getShared(new Key(bitmap), () -> bitmap.asShared());
    }

    /**
     * Returns a scaled and shared (ashmem-backed) version of the bitmap in the cache by
     * width and height to be scaled.
     */
    @Nullable
    public static Bitmap getShared(@NonNull Bitmap bitmap, int scaledWidth, int scaledHeight) {
        if (!Flags.cacheAshmemBitmaps()) {
            return Bitmap.createScaledAshmemBitmap(bitmap, scaledWidth, scaledHeight, true);
        }
        return getShared(new Key(bitmap, scaledWidth, scaledHeight), () ->
            Bitmap.createScaledAshmemBitmap(bitmap, scaledWidth, scaledHeight, true));
    }
}
