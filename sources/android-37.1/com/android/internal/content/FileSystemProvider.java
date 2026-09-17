/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.android.internal.content;

import static android.Manifest.permission.MANAGE_DOCUMENTS;
import static android.provider.Flags.enableDocumentsTrashApi;

import static com.android.providers.media.flags.Flags.enableTrashAndRestoreByFilePathApi;

import android.annotation.CallSuper;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import android.graphics.Point;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.FileObserver;
import android.os.FileUtils;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.provider.DocumentsProvider;
import android.provider.MediaStore;
import android.provider.MetadataReader;
import android.system.Int64Ref;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.android.internal.annotations.GuardedBy;
import com.android.internal.content.storage.flags.Flags;
import com.android.internal.util.ArrayUtils;

import libcore.io.IoUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A helper class for {@link android.provider.DocumentsProvider} to perform file operations on local
 * files.
 */
public abstract class FileSystemProvider extends DocumentsProvider {

    private static final String TAG = "FileSystemProvider";

    private static final boolean LOG_INOTIFY = false;

    protected static final String SUPPORTED_QUERY_ARGS = joinNewline(
            DocumentsContract.QUERY_ARG_DISPLAY_NAME,
            DocumentsContract.QUERY_ARG_FILE_SIZE_OVER,
            DocumentsContract.QUERY_ARG_LAST_MODIFIED_AFTER,
            DocumentsContract.QUERY_ARG_MIME_TYPES,
            ContentResolver.QUERY_ARG_LIMIT);

    private static final int DEFAULT_SEARCH_RESULT_LIMIT = 23;
    private static final int MAX_SEARCH_RESULT_LIMIT = 1000;

    /**
     * File prefix indicating that the file {@link MediaStore.MediaColumns#IS_TRASHED}.
     */
    protected static final String PREFIX_TRASHED = "trashed";

    /**
     * Default directory for trashed items
     */
    protected static final String DIRECTORY_TRASH_STORAGE = ".trash-storage";

    private static final Pattern PATTERN_EXPIRES_FILE = Pattern.compile(
            "(?i)^\\.(pending|trashed)-(\\d+)-([^/]+)$");

    private static String joinNewline(String... args) {
        return TextUtils.join("\n", args);
    }

    /**
     * Optional boolean included in a directory {@link Cursor#getExtras()} to initiate an
     * asynchronous query.
     */
    private static final String QUERY_ARG_USE_ASYNC = "android:query-arg-use-async";

    /**
     * Optional int included in a directory {@link Cursor#getExtras()} providing a sequence number
     * for re-queries.
     */
    private static final String QUERY_ARG_REQUERY_SEQUENCE = "android:query-arg-requery-sequence";

    private String[] mDefaultProjection;

    @GuardedBy("mObservers")
    private final ArrayMap<File, DirectoryObserver> mObservers = new ArrayMap<>();

    /** Stores populated cursors keyed by their sequence number. */
    private final ConcurrentHashMap<Integer, Cursor> mSequenceCache = new ConcurrentHashMap<>();

    /** Generator for unique sequence numbers. */
    private final AtomicInteger mSequenceGenerator = new AtomicInteger(0);

    protected Handler mHandler;

    /** Throttles concurrent background listings to bound system resource usage. */
    protected ExecutorService mFetchExecutor;

    /**
     * Time to wait before cleaning up an abandoned asynchronous cursor from the cache. This
     * protects against memory leaks if the caller never retrieves the result.
     */
    private static final long ASYNC_CURSOR_TIMEOUT_MS = TimeUnit.MINUTES.toMillis(1);

    /** Checks if the calling app has the MANAGE_DOCUMENTS permission. */
    protected boolean callingAppHasManageDocumentsPermissions() {
        return getContext().checkCallingOrSelfPermission(MANAGE_DOCUMENTS)
                == PackageManager.PERMISSION_GRANTED;
    }

    private int getNextAsyncSequenceNumber() {
        return mSequenceGenerator.updateAndGet(val -> val >= Integer.MAX_VALUE ? 1 : val + 1);
    }

    private boolean isAsyncLoadingAllowed() {
        return Flags.espAsyncLoadingSequence() && callingAppHasManageDocumentsPermissions();
    }

    /** Checks if a request should be handled asynchronously. */
    private boolean isAsyncLoadingRequest(@Nullable Bundle queryArgs) {
        return isAsyncLoadingAllowed()
                && queryArgs != null
                && queryArgs.getBoolean(QUERY_ARG_USE_ASYNC);
    }

    /** Retrieves and removes a cached cursor for a valid sequence number. */
    private @Nullable Cursor checkAsyncResultCache(@Nullable Bundle queryArgs) {
        if (!isAsyncLoadingAllowed()
                || queryArgs == null
                || !queryArgs.containsKey(QUERY_ARG_REQUERY_SEQUENCE)) {
            return null;
        }

        final int sequence = queryArgs.getInt(QUERY_ARG_REQUERY_SEQUENCE);
        Cursor cached = mSequenceCache.remove(sequence);
        if (cached != null) {
            // We use the cursor instance as the token to ensure strict reference equality.
            // MessageQueue relies on ==, which would fail with autoboxed integers.
            mHandler.removeCallbacksAndMessages(cached);
        } else {
            Log.e(TAG, "Cache miss for async sequence: " + sequence);
        }
        return cached;
    }

    /**
     * Adds a populated cursor to the sequence cache and schedules a background cleanup task. If the
     * cursor is not retrieved (which removes it from the cache) within {@link
     * #ASYNC_CURSOR_TIMEOUT_MS}, the cleanup task will automatically remove and close it. If the
     * Cursor is removed, subsequent requests for that sequence will fallback to performing another
     * asynchronous loading task.
     *
     * @param sequence The unique sequence number identifying the asynchronous request.
     * @param cursor The populated cursor to be cached.
     */
    private void addToCacheAndScheduleCleanup(int sequence, Cursor cursor) {
        mSequenceCache.put(sequence, cursor);
        // We use the Cursor instance as the identity token. Because MessageQueue relies on
        // reference equality (==) for cancellation, using the unique Cursor object avoids the
        // identity bugs caused by autoboxed integer sequences.
        mHandler.postDelayed(() -> executeCleanup(sequence), cursor, ASYNC_CURSOR_TIMEOUT_MS);
    }

    private void executeCleanup(int sequence) {
        // Cache removal and cursor closing occur on the fetch executor to avoid blocking the main
        // thread with potential file system I/O.
        try {
            mFetchExecutor.execute(
                    () -> {
                        Cursor abandoned = mSequenceCache.remove(sequence);
                        if (abandoned != null) {
                            Log.w(
                                    TAG,
                                    "Cleaning up abandoned async cursor for sequence: " + sequence);
                            abandoned.close();
                        }
                    });
        } catch (RejectedExecutionException e) {
            Log.w(TAG, "Cleanup task rejected (provider likely shut down): " + e.getMessage());
        }
    }

    /**
     * Returns an empty loading cursor with a sequence number for re-queries. We use a {@link
     * LoadingCursor} here (which is a plain {@link MatrixCursor}) instead of a {@link
     * DirectoryCursor} to avoid unwanted notifications from the underlying file system during the
     * initial load, which could trigger redundant synchronous loads.
     */
    private @NonNull LoadingCursor createLoadingCursor(@NonNull String[] projection, int sequence) {
        final LoadingCursor result = new LoadingCursor(resolveProjection(projection));

        final Bundle extras = new Bundle();
        extras.putBoolean(DocumentsContract.EXTRA_LOADING, true);
        extras.putInt(QUERY_ARG_REQUERY_SEQUENCE, sequence);
        result.setExtras(extras);
        return result;
    }

    protected abstract File getFileForDocId(String docId, boolean visible)
            throws FileNotFoundException;

    protected abstract String getDocIdForFile(File file) throws FileNotFoundException;

    protected abstract Uri buildNotificationUri(String docId);

    /**
     * Callback indicating that the given document has been modified. This gives
     * the provider a hook to invalidate cached data, such as {@code sdcardfs}.
     */
    protected void onDocIdChanged(String docId) {
        // Default is no-op
    }

    /**
     * Callback indicating that the given document has been deleted or moved. This gives
     * the provider a hook to revoke the uri permissions.
     */
    protected void onDocIdDeleted(String docId, boolean shouldRevokeUriPermission) {
        // Default is no-op
    }

    @Override
    public boolean onCreate() {
        throw new UnsupportedOperationException(
                "Subclass should override this and call onCreate(defaultDocumentProjection)");
    }

    @CallSuper
    protected void onCreate(String[] defaultProjection) {
        mHandler = new Handler();
        mDefaultProjection = defaultProjection;
        if (Flags.espAsyncLoadingSequence() && mFetchExecutor == null) {
            final ThreadPoolExecutor executor =
                    new ThreadPoolExecutor(
                            /* corePoolSize= */ 4,
                            /* maximumPoolSize= */ 4,
                            /* keepAliveTime= */ 60L,
                            TimeUnit.SECONDS,
                            new LinkedBlockingQueue<>());
            executor.allowCoreThreadTimeOut(true);
            mFetchExecutor = executor;
        }
    }

    @Override
    public void shutdown() {
        if (Flags.espAsyncLoadingSequence()) {
            if (mHandler != null) {
                mHandler.removeCallbacksAndMessages(null);
            }
            synchronized (mObservers) {
                for (DirectoryObserver observer : mObservers.values()) {
                    observer.stopWatching();
                }
                mObservers.clear();
            }
            for (Cursor cursor : mSequenceCache.values()) {
                cursor.close();
            }
            mSequenceCache.clear();
            if (mFetchExecutor != null) {
                mFetchExecutor.shutdownNow();
            }
        }
        super.shutdown();
    }

    @Override
    public boolean isChildDocument(String parentDocId, String docId) {
        try {
            final File parent = getFileForDocId(parentDocId).getCanonicalFile();
            final File doc = getFileForDocId(docId).getCanonicalFile();
            return FileUtils.contains(parent, doc);
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Failed to determine if " + docId + " is child of " + parentDocId + ": " + e);
        }
    }

    @Override
    public @Nullable Bundle getDocumentMetadata(String documentId)
            throws FileNotFoundException {
        File file = getFileForDocId(documentId);

        if (!file.exists()) {
            throw new FileNotFoundException("Can't find the file for documentId: " + documentId);
        }

        final String mimeType = getDocumentType(documentId);
        if (Document.MIME_TYPE_DIR.equals(mimeType)) {
            final Int64Ref treeCount = new Int64Ref(0);
            final Int64Ref treeSize = new Int64Ref(0);
            try {
                final Path path = FileSystems.getDefault().getPath(file.getAbsolutePath());
                Files.walkFileTree(path, new FileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                        treeCount.value += 1;
                        treeSize.value += attrs.size();
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult visitFileFailed(Path file, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                Log.e(TAG, "An error occurred retrieving the metadata", e);
                return null;
            }

            final Bundle res = new Bundle();
            res.putLong(DocumentsContract.METADATA_TREE_COUNT, treeCount.value);
            res.putLong(DocumentsContract.METADATA_TREE_SIZE, treeSize.value);
            return res;
        }

        if (!file.isFile()) {
            Log.w(TAG, "Can't stream non-regular file. Returning empty metadata.");
            return null;
        }
        if (!file.canRead()) {
            Log.w(TAG, "Can't stream non-readable file. Returning empty metadata.");
            return null;
        }
        if (!MetadataReader.isSupportedMimeType(mimeType)) {
            Log.w(TAG, "Unsupported type " + mimeType + ". Returning empty metadata.");
            return null;
        }

        InputStream stream = null;
        try {
            Bundle metadata = new Bundle();
            stream = new FileInputStream(file.getAbsolutePath());
            MetadataReader.getMetadata(metadata, stream, mimeType, null);
            return metadata;
        } catch (IOException e) {
            Log.e(TAG, "An error occurred retrieving the metadata", e);
            return null;
        } finally {
            IoUtils.closeQuietly(stream);
        }
    }

    protected final List<String> findDocumentPath(File parent, File doc)
            throws FileNotFoundException {

        if (!doc.exists()) {
            throw new FileNotFoundException(doc + " is not found.");
        }

        if (!FileUtils.contains(parent, doc)) {
            throw new FileNotFoundException(doc + " is not found under " + parent);
        }

        List<String> path = new ArrayList<>();
        while (doc != null && FileUtils.contains(parent, doc)) {
            path.add(0, getDocIdForFile(doc));

            doc = doc.getParentFile();
        }

        return path;
    }

    @Override
    public String createDocument(String docId, String mimeType, String displayName)
            throws FileNotFoundException {
        displayName = FileUtils.buildValidFatFilename(displayName);

        final File parent = getFileForDocId(docId);
        if (!parent.isDirectory()) {
            throw new IllegalArgumentException("Parent document isn't a directory");
        }

        final File file = FileUtils.buildUniqueFile(parent, mimeType, displayName);
        final String childId;
        if (Document.MIME_TYPE_DIR.equals(mimeType)) {
            if (!file.mkdir()) {
                throw new IllegalStateException("Failed to mkdir " + file);
            }
            childId = getDocIdForFile(file);
            onDocIdChanged(childId);
        } else {
            try {
                if (!file.createNewFile()) {
                    throw new IllegalStateException("Failed to touch " + file);
                }
                childId = getDocIdForFile(file);
                onDocIdChanged(childId);
            } catch (IOException e) {
                throw new IllegalStateException("Failed to touch " + file + ": " + e);
            }
        }
        updateMediaStore(getContext(), file);
        return childId;
    }

    @Override
    public String renameDocument(String docId, String displayName) throws FileNotFoundException {
        // Since this provider treats renames as generating a completely new
        // docId, we're okay with letting the MIME type change.
        displayName = FileUtils.buildValidFatFilename(displayName);

        final File before = getFileForDocId(docId);
        final File beforeVisibleFile = getFileForDocId(docId, true);
        final File after = FileUtils.buildUniqueFile(before.getParentFile(), displayName);
        if (!before.renameTo(after)) {
            throw new IllegalStateException("Failed to rename to " + after);
        }

        final String afterDocId = getDocIdForFile(after);
        onDocIdChanged(docId);
        onDocIdChanged(afterDocId);

        final File afterVisibleFile = getFileForDocId(afterDocId, true);

        updateMediaStore(getContext(), beforeVisibleFile);
        updateMediaStore(getContext(), afterVisibleFile);

        if (!TextUtils.equals(docId, afterDocId)) {
            // DocumentsProvider handles the revoking / granting uri permission for the docId and
            // the afterDocId in the renameDocument case. Don't need to call revokeUriPermission
            // for the docId here.
            onDocIdDeleted(docId, /* shouldRevokeUriPermission */ false);
            return afterDocId;
        } else {
            return null;
        }
    }

    @Override
    public String moveDocument(String sourceDocumentId, String sourceParentDocumentId,
            String targetParentDocumentId)
            throws FileNotFoundException {
        final File before = getFileForDocId(sourceDocumentId);
        final File after = new File(getFileForDocId(targetParentDocumentId), before.getName());
        final File visibleFileBefore = getFileForDocId(sourceDocumentId, true);

        if (after.exists()) {
            throw new IllegalStateException("Already exists " + after);
        }
        if (!before.renameTo(after)) {
            throw new IllegalStateException("Failed to move to " + after);
        }

        final String docId = getDocIdForFile(after);
        onDocIdChanged(sourceDocumentId);
        onDocIdDeleted(sourceDocumentId, /* shouldRevokeUriPermission */ true);
        onDocIdChanged(docId);
        // update the database
        updateMediaStore(getContext(), visibleFileBefore);
        updateMediaStore(getContext(), getFileForDocId(docId, true));
        return docId;
    }

    private static void updateMediaStore(@NonNull Context context, File file) {
        if (file != null) {
            final ContentResolver resolver = context.getContentResolver();
            final String noMedia = ".nomedia";
            // For file, check whether the file name is .nomedia or not.
            // If yes, scan the parent directory to update all files in the directory.
            if (!file.isDirectory() && file.getName().toLowerCase(Locale.ROOT).endsWith(noMedia)) {
                MediaStore.scanFile(resolver, file.getParentFile());
            } else {
                MediaStore.scanFile(resolver, file);
            }
        }
    }

    @Override
    public void deleteDocument(String docId) throws FileNotFoundException {
        final File file = getFileForDocId(docId);
        final File visibleFile = getFileForDocId(docId, true);
        final boolean isTrashedDocument = isTrashFile(file);

        final boolean isDirectory = file.isDirectory();
        if (isDirectory) {
            FileUtils.deleteContents(file);
        }
        // We could be deleting pending media which doesn't have any content yet, so only throw
        // if the file exists and we fail to delete it.
        if (file.exists() && !file.delete()) {
            throw new IllegalStateException("Failed to delete " + file);
        }

        onDocIdChanged(docId);
        onDocIdDeleted(docId, /* shouldRevokeUriPermission */ true);
        // Notify if deleting a trashed document.
        if (isTrashedDocument) {
            notifyTrashChange(docId);
        }

        updateMediaStore(getContext(), visibleFile);
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection)
            throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(resolveProjection(projection));
        includeFile(result, documentId, null);
        return result;
    }

    /**
     * WARNING: this method should really be {@code final}, but for the backward compatibility it's
     * not; new classes that extend {@link FileSystemProvider} should override
     * {@link #queryChildDocuments(String, String[], String, boolean)}, not this method.
     */
    @Override
    public Cursor queryChildDocuments(String documentId, String[] projection, String sortOrder)
            throws FileNotFoundException {
        return queryChildDocuments(documentId, projection, sortOrder, /* includeHidden= */ false);
    }

    /**
     * This method is similar to {@link #queryChildDocuments(String, String[], String)}, however, it
     * enables the use of {@link Bundle} arguments to initiate asynchronous loading.
     */
    @Override
    public Cursor queryChildDocuments(
            String parentDocumentId, String[] projection, Bundle queryArgs)
            throws FileNotFoundException {
        if (!Flags.espAsyncLoadingSequence()) {
            return super.queryChildDocuments(parentDocumentId, projection, queryArgs);
        }

        // If the caller is the system document manager and provides a re-query sequence number that
        // is already cached, exit early and return that value.
        final Cursor cached = checkAsyncResultCache(queryArgs);
        if (cached != null) {
            return cached;
        }

        // If no valid sequence number was supplied, check if the caller has requested an
        // asynchronous query; otherwise, fall through to the synchronous flow.
        if (!isAsyncLoadingRequest(queryArgs)) {
            return super.queryChildDocuments(parentDocumentId, projection, queryArgs);
        }

        final File parent = getFileForDocId(parentDocumentId);
        if (!parent.isDirectory()) {
            Log.w(TAG, '"' + parentDocumentId + "\" is not a directory");
            return new DirectoryCursor(resolveProjection(projection), parentDocumentId, parent);
        }

        if (shouldHideDocument(parentDocumentId)) {
            Log.w(TAG, "Queried directory \"" + parentDocumentId + "\" is hidden");
            return new DirectoryCursor(resolveProjection(projection), parentDocumentId, parent);
        }

        final int sequence = getNextAsyncSequenceNumber();
        final String[] columns = resolveProjection(projection);
        final String docId = parentDocumentId;
        final LoadingCursor loadingCursor = createLoadingCursor(columns, sequence);

        final Future<?> job =
                mFetchExecutor.submit(
                        () ->
                                startDirectoryLoader(
                                        parent,
                                        columns,
                                        /* includeHidden= */ false,
                                        sequence,
                                        loadingCursor,
                                        docId));
        loadingCursor.setJob(job);

        return loadingCursor;
    }

    /**
     * This method is similar to {@link #queryChildDocuments(String, String[], String)}, however, it
     * could return <b>all</b> content of the directory, <b>including restricted (hidden)
     * directories and files</b>.
     * <p>
     * In the scoped storage world, some directories and files (e.g. {@code Android/data/} and
     * {@code Android/obb/} on the external storage) are hidden for privacy reasons.
     * Hence, this method may reveal privacy-sensitive data, thus should be used with extra care.
     */
    @Override
    public final Cursor queryChildDocumentsForManage(String documentId, String[] projection,
            String sortOrder) throws FileNotFoundException {
        return queryChildDocuments(documentId, projection, sortOrder, /* includeHidden= */ true);
    }

    protected Cursor queryChildDocuments(String documentId, String[] projection, String sortOrder,
            boolean includeHidden) throws FileNotFoundException {
        final File parent = getFileForDocId(documentId);
        final MatrixCursor result = new DirectoryCursor(
                resolveProjection(projection), documentId, parent);

        if (!parent.isDirectory()) {
            Log.w(TAG, '"' + documentId + "\" is not a directory");
            return result;
        }

        if (!includeHidden && shouldHideDocument(documentId)) {
            Log.w(TAG, "Queried directory \"" + documentId + "\" is hidden");
            return result;
        }

        for (File file : FileUtils.listFilesOrEmpty(parent)) {
            if (!includeHidden && shouldHideDocument(file)) continue;

            includeFile(result, null, file);
        }

        return result;
    }

    /**
     * Populates a single pending cursor on a background thread to keep the caller responsive during
     * long listings.
     */
    private void startDirectoryLoader(
            File parent,
            String[] projection,
            boolean includeHidden,
            int sequence,
            LoadingCursor loadingCursor,
            String docId) {
        final DirectoryCursor result = new DirectoryCursor(projection, docId, parent);
        // Note: Files.newDirectoryStream() blocks on underlying native file system I/O,
        // which may not respond to thread interruption immediately. Cancellation relies
        // on cooperative checks (Thread.isInterrupted()) during iteration.
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(parent.toPath())) {
            for (Path entry : stream) {
                if (Thread.currentThread().isInterrupted()) {
                    result.close();
                    return;
                }
                File file = entry.toFile();
                if (!includeHidden && shouldHideDocument(file)) {
                    continue;
                }
                includeFile(result, /* docId= */ null, file);
            }
        } catch (Exception e) {
            final Bundle extras = new Bundle();
            // TODO(b/419879340): Use a user-readable error string instead of the raw message.
            extras.putString(DocumentsContract.EXTRA_ERROR, e.getMessage());
            result.setExtras(extras);
            Log.e(TAG, "Failed to load directory", e);
        }

        addToCacheAndScheduleCleanup(sequence, result);
        loadingCursor.notifyChanged();
    }

    /**
     * Searches documents under the given folder.
     *
     * To avoid runtime explosion only returns the at most 23 items unless the caller
     * (DocumentsUI) explicitly asks for up to MAX_SEARCH_RESULT_LIMIT using a non-negative limit.
     *
     * @param folder the root folder where recursive search begins
     * @param projection projection of the returned cursor
     * @param exclusion absolute file paths to exclude from result
     * @param queryArgs the query arguments for search
     * @return cursor containing search result. Include
     *         {@link ContentResolver#EXTRA_HONORED_ARGS} in {@link Cursor}
     *         extras {@link Bundle} when any QUERY_ARG_* value was honored
     *         during the preparation of the results.
     * @throws FileNotFoundException when root folder doesn't exist or search fails
     *
     * @see ContentResolver#EXTRA_HONORED_ARGS
     */
    protected final Cursor querySearchDocuments(File folder, String[] projection,
            Set<String> exclusion, Bundle queryArgs) throws FileNotFoundException {
        if (!Flags.espAsyncLoadingSequence()) {
            return querySearchDocumentsSync(folder, projection, exclusion, queryArgs);
        }

        // If the caller is the system document manager and provides a re-query sequence number that
        // is already cached, exit early and return that value.
        final Cursor cached = checkAsyncResultCache(queryArgs);
        if (cached != null) {
            return cached;
        }

        // If no valid sequence number was supplied, check if the caller has requested an
        // asynchronous query; otherwise, fall through to the synchronous flow.
        if (!isAsyncLoadingRequest(queryArgs)) {
            return querySearchDocumentsSync(folder, projection, exclusion, queryArgs);
        }

        final int sequence = getNextAsyncSequenceNumber();
        final String[] columns = resolveProjection(projection);
        final LoadingCursor loadingCursor = createLoadingCursor(columns, sequence);

        final Future<?> job =
                mFetchExecutor.submit(
                        () ->
                                startSearchLoader(
                                        folder,
                                        exclusion,
                                        queryArgs,
                                        columns,
                                        sequence,
                                        loadingCursor));
        loadingCursor.setJob(job);

        return loadingCursor;
    }

    private Cursor querySearchDocumentsSync(
            File folder, String[] projection, Set<String> exclusion, Bundle queryArgs)
            throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(resolveProjection(projection));
        int maxResults = DEFAULT_SEARCH_RESULT_LIMIT;
        if (Flags.useFileSystemProviderSearchLimits()) {
            maxResults =
                    queryArgs.getInt(ContentResolver.QUERY_ARG_LIMIT, DEFAULT_SEARCH_RESULT_LIMIT);
            if (maxResults > MAX_SEARCH_RESULT_LIMIT) {
                maxResults = MAX_SEARCH_RESULT_LIMIT;
            } else if (maxResults < 0) {
                maxResults = DEFAULT_SEARCH_RESULT_LIMIT;
            }
        }

        // We'll be a running a BFS here.
        final Queue<File> pending = new ArrayDeque<>();
        for (File child : FileUtils.listFilesOrEmpty(folder)) {
            pending.offer(child);
        }

        while (!pending.isEmpty() && result.getCount() < maxResults) {
            if (Thread.currentThread().isInterrupted()) {
                return result;
            }
            final File file = pending.poll();

            // Skip hidden documents (both files and directories)
            if (shouldHideDocument(file)) continue;

            if (file.isDirectory()) {
                for (File child : FileUtils.listFilesOrEmpty(file)) {
                    pending.offer(child);
                }
            }

            if (exclusion.contains(file.getAbsolutePath())) continue;

            if (matchSearchQueryArguments(file, queryArgs)) {
                includeFile(result, null, file);
            }
        }

        final String[] handledQueryArgs = DocumentsContract.getHandledQueryArguments(queryArgs);
        if (handledQueryArgs.length > 0) {
            final Bundle extras = new Bundle();
            extras.putStringArray(ContentResolver.EXTRA_HONORED_ARGS, handledQueryArgs);
            result.setExtras(extras);
        }
        return result;
    }

    private void startSearchLoader(
            File folder,
            Set<String> exclusion,
            Bundle queryArgs,
            String[] projection,
            int sequence,
            LoadingCursor loadingCursor) {
        try {
            final Cursor result =
                    querySearchDocumentsSync(folder, projection, exclusion, queryArgs);
            if (Thread.currentThread().isInterrupted()) {
                if (result != null) {
                    result.close();
                }
                return;
            }
            addToCacheAndScheduleCleanup(sequence, result);
            loadingCursor.notifyChanged();
        } catch (Exception e) {
            final MatrixCursor errorResult = new MatrixCursor(resolveProjection(projection));
            final Bundle extras = new Bundle();
            // TODO(b/419879340): Use a user-readable error string instead of the raw message.
            extras.putString(DocumentsContract.EXTRA_ERROR, e.getMessage());
            errorResult.setExtras(extras);
            addToCacheAndScheduleCleanup(sequence, errorResult);
            Log.e(TAG, "Failed to load search results", e);
            loadingCursor.notifyChanged();
        }
    }

    @Override
    public String getDocumentType(String documentId) throws FileNotFoundException {
        return getDocumentType(documentId, getFileForDocId(documentId));
    }

    private String getDocumentType(final String documentId, final File file)
            throws FileNotFoundException {
        if (file.isDirectory()) {
            return Document.MIME_TYPE_DIR;
        } else {
            final int lastDot = documentId.lastIndexOf('.');
            if (lastDot >= 0) {
                final String extension = documentId.substring(lastDot + 1).toLowerCase();
                final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                if (mime != null) {
                    return mime;
                }
            }
            return ContentResolver.MIME_TYPE_DEFAULT;
        }
    }

    @Override
    public ParcelFileDescriptor openDocument(
            String documentId, String mode, CancellationSignal signal)
            throws FileNotFoundException {
        final File file = getFileForDocId(documentId);
        final File visibleFile = getFileForDocId(documentId, true);

        final int pfdMode = ParcelFileDescriptor.parseMode(mode);
        if (visibleFile == null) {
            return ParcelFileDescriptor.open(file, pfdMode);
        } else if (pfdMode == ParcelFileDescriptor.MODE_READ_ONLY) {
            return openFileForRead(visibleFile);
        } else {
            try {
                // When finished writing, kick off media scanner
                return ParcelFileDescriptor.open(
                        file, pfdMode, mHandler, (IOException e) -> {
                            onDocIdChanged(documentId);
                            scanFile(visibleFile);
                        });
            } catch (IOException e) {
                throw new FileNotFoundException("Failed to open for writing: " + e);
            }
        }
    }

    private ParcelFileDescriptor openFileForRead(final File target) throws FileNotFoundException {
        final Uri uri = MediaStore.scanFile(getContext().getContentResolver(), target);
        if (uri == null) {
            Log.w(TAG, "Failed to retrieve media store URI for: " + target);
            return ParcelFileDescriptor.open(target, ParcelFileDescriptor.MODE_READ_ONLY);
        }

        // Passing the calling uid via EXTRA_MEDIA_CAPABILITIES_UID, so that the decision to
        // transcode or not transcode can be made based upon the calling app's uid, and not based
        // upon the Provider's uid.
        final Bundle opts = new Bundle();
        opts.putInt(MediaStore.EXTRA_MEDIA_CAPABILITIES_UID, Binder.getCallingUid());

        final AssetFileDescriptor afd =
                getContext().getContentResolver().openTypedAssetFileDescriptor(uri, "*/*", opts);
        if (afd == null) {
            Log.w(TAG, "Failed to open with media_capabilities uid for URI: " + uri);
            return ParcelFileDescriptor.open(target, ParcelFileDescriptor.MODE_READ_ONLY);
        }

        return afd.getParcelFileDescriptor();
    }

    /**
     * Test if the file matches the query arguments.
     *
     * @param file the file to test
     * @param queryArgs the query arguments
     */
    private boolean matchSearchQueryArguments(File file, Bundle queryArgs) {
        if (file == null) {
            return false;
        }

        final String fileMimeType;
        final String fileName = file.getName();

        if (file.isDirectory()) {
            fileMimeType = DocumentsContract.Document.MIME_TYPE_DIR;
        } else {
            int dotPos = fileName.lastIndexOf('.');
            if (dotPos < 0) {
                return false;
            }
            final String extension = fileName.substring(dotPos + 1);
            fileMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return DocumentsContract.matchSearchQueryArguments(queryArgs, fileName, fileMimeType,
                file.lastModified(), file.length());
    }

    private void scanFile(File visibleFile) {
        final Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        intent.setData(Uri.fromFile(visibleFile));
        getContext().sendBroadcast(intent);
    }

    @Override
    public AssetFileDescriptor openDocumentThumbnail(
            String documentId, Point sizeHint, CancellationSignal signal)
            throws FileNotFoundException {
        final File file = getFileForDocId(documentId);
        return DocumentsContract.openImageThumbnail(file);
    }

    @Nullable
    @Override
    public String trashDocument(@NonNull String documentId)
            throws FileNotFoundException {
        if (!enableTrashAndRestoreByFilePathApi()) {
            throw new UnsupportedOperationException(
                    "MediaStore feature for trash is not supported");
        }

        File file = getFileForDocId(documentId);
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist for " + documentId);
        }

        String trashedPath = MediaStore.trashFile(getContext().getContentResolver(),
                file.getPath());
        File trashedFile = new File(trashedPath);
        final String trashedDocId = getDocIdForFile(trashedFile);
        onDocIdChanged(documentId);
        onDocIdDeleted(documentId, /* shouldRevokeUriPermission */ true);
        onDocIdChanged(trashedDocId);
        return trashedDocId;
    }

    protected final Cursor queryTrashDocuments(@NonNull File parent, @NonNull String volumeName,
            @Nullable String[] projection)
            throws FileNotFoundException {
        String docId = getDocIdForFile(parent);
        String[] trashProjections = projection;
        if (projection == null) {
            trashProjections = mDefaultProjection;
            if (!ArrayUtils.contains(trashProjections, Document.COLUMN_ORIGINAL_RELATIVE_PATH)) {
                trashProjections = ArrayUtils.appendElement(String.class, trashProjections,
                        Document.COLUMN_ORIGINAL_RELATIVE_PATH);
            }
        }
        MatrixCursor result = new DirectoryCursor(trashProjections, docId, parent);
        includeTrashFiles(result, parent);
        // include MediaStore trashed files which are not in .trash-storage location
        includeMediaStoreTrashFiles(result, volumeName);

        // Set notification URI for trash
        final Uri trashUri = buildTrashNotificationUri(docId);
        if (trashUri != null) {
            result.setNotificationUri(getContext().getContentResolver(), trashUri);
        }

        return result;
    }

    @Nullable
    @Override
    public String restoreDocumentFromTrash(@NonNull String documentId, @Nullable String targetId)
            throws FileNotFoundException {
        if (!enableTrashAndRestoreByFilePathApi()) {
            throw new UnsupportedOperationException(
                    "MediaStore feature for trash is not supported");
        }

        File file = getFileForDocId(documentId);
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist for " + documentId);
        }

        if (!isTrashFile(file)) {
            throw new IllegalArgumentException("DocumentId represents a non-trashed file");
        }

        String targetPath = null;
        if (targetId != null) {
            File targetFile = getFileForDocId(targetId);
            if (targetFile != null) {
                targetPath = targetFile.getAbsolutePath();
            }
        }
        String restoredPath = MediaStore.restoreFileFromTrash(getContext().getContentResolver(),
                file.getPath(), targetPath);

        File restoredFile = new File(restoredPath);
        final String restoredDocId = getDocIdForFile(restoredFile);
        onDocIdChanged(documentId);
        onDocIdChanged(restoredDocId);
        // Notify if restoring a trashed document.
        notifyTrashChange(documentId);

        return restoredDocId;
    }


    private boolean isTrashFile(File file) {
        final Matcher matcher = PATTERN_EXPIRES_FILE.matcher(file.getName());
        return matcher.matches() && matcher.group(1).equals(PREFIX_TRASHED);
    }

    private void includeTrashFiles(MatrixCursor result, File parent) throws FileNotFoundException  {
        for (File file : parent.listFiles()) {
            if (isTrashFile(file)) {
                includeFile(result, null, file);
                continue;
            }
            if (file.isDirectory()) {
                includeTrashFiles(result, file);
            }
        }
    }

    private void includeMediaStoreTrashFiles(@NonNull MatrixCursor result,
            @NonNull String volumeName)
            throws FileNotFoundException {
        final Uri uri = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL);
        final Bundle queryArgs = new Bundle();
        queryArgs.putInt(MediaStore.QUERY_ARG_MATCH_TRASHED, MediaStore.MATCH_ONLY);

        final String selection = MediaStore.MediaColumns.RELATIVE_PATH + " NOT LIKE ? AND "
                + MediaStore.MediaColumns.VOLUME_NAME + " = ?";
        final String[] selectionArgs = new String[]{
                DIRECTORY_TRASH_STORAGE + "/%",
                volumeName.toLowerCase(Locale.ROOT)
        };

        queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, selection);
        queryArgs.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, selectionArgs);
        String[] projection = new String[]{MediaStore.Files.FileColumns.DATA};

        try (Cursor cursor = getContext().getContentResolver().query(uri, projection,
                queryArgs, null)) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    final String data = cursor.getString(
                            cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA));
                    File file = new File(data);
                    includeFile(result, null, file);
                }
            }
        }
    }

    protected RowBuilder includeFile(final MatrixCursor result, String docId, File file)
            throws FileNotFoundException {
        final String[] columns = result.getColumnNames();
        final RowBuilder row = result.newRow();

        if (docId == null) {
            docId = getDocIdForFile(file);
        } else {
            file = getFileForDocId(docId);
        }

        final String mimeType = getDocumentType(docId, file);
        row.add(Document.COLUMN_DOCUMENT_ID, docId);
        row.add(Document.COLUMN_MIME_TYPE, mimeType);

        final int flagIndex = ArrayUtils.indexOf(columns, Document.COLUMN_FLAGS);
        if (flagIndex != -1) {
            final boolean isDir = mimeType.equals(Document.MIME_TYPE_DIR);
            boolean isTrashedFile = isTrashFile(file);
            int flags = 0;
            if (file.canWrite()) {
                flags |= Document.FLAG_SUPPORTS_DELETE;
                if (!isTrashedFile) {
                    flags |= Document.FLAG_SUPPORTS_RENAME;
                    flags |= Document.FLAG_SUPPORTS_MOVE;

                    if (isDir) {
                        flags |= Document.FLAG_DIR_SUPPORTS_CREATE;
                        if (isSubtreeSearchSupported()) {
                            flags |= Document.FLAG_SUPPORTS_SUBTREE_SEARCH;
                        }
                    } else {
                        flags |= Document.FLAG_SUPPORTS_WRITE;
                    }
                }
            }

            if (enableDocumentsTrashApi()) {
                if (isTrashFile(file)) {
                    flags |= Document.FLAG_SUPPORTS_RESTORE;
                    final int pathColumnIndex = ArrayUtils.indexOf(columns,
                            Document.COLUMN_ORIGINAL_RELATIVE_PATH);
                    if (pathColumnIndex != -1) {
                        addOriginalRelativePath(row, columns, file);
                    }
                } else if (isTrashSupported(file)) {
                    flags |= Document.FLAG_SUPPORTS_TRASH;
                }
            }

            if (isDir && shouldBlockDirectoryFromTree(docId)) {
                flags |= Document.FLAG_DIR_BLOCKS_OPEN_DOCUMENT_TREE;
            }

            if (mimeType.startsWith("image/")) {
                flags |= Document.FLAG_SUPPORTS_THUMBNAIL;
            }

            if (typeSupportsMetadata(mimeType)) {
                flags |= Document.FLAG_SUPPORTS_METADATA;
            }
            row.add(flagIndex, flags);
        }

        final int displayNameIndex = ArrayUtils.indexOf(columns, Document.COLUMN_DISPLAY_NAME);
        if (displayNameIndex != -1) {
            String name = file.getName();
            final Matcher matcher = PATTERN_EXPIRES_FILE.matcher(name);
            if (matcher.matches() && matcher.group(1).equals(PREFIX_TRASHED)) {
                // .trashed-<timestamp>-<name>
                name = matcher.group(3);
            }
            row.add(displayNameIndex, name);
        }

        final int lastModifiedIndex = ArrayUtils.indexOf(columns, Document.COLUMN_LAST_MODIFIED);
        if (lastModifiedIndex != -1) {
            final long lastModified = file.lastModified();
            // Only publish dates reasonably after epoch
            if (lastModified > 31536000000L) {
                row.add(lastModifiedIndex, lastModified);
            }
        }
        final int sizeIndex = ArrayUtils.indexOf(columns, Document.COLUMN_SIZE);
        if (sizeIndex != -1) {
            row.add(sizeIndex, file.length());
        }

        // Return the row builder just in case any subclass want to add more stuff to it.
        return row;
    }

    /**
     * Some providers may want to restrict access to certain directories and files,
     * e.g. <i>"Android/data"</i> and <i>"Android/obb"</i> on the shared storage for
     * privacy reasons.
     * Such providers should override this method.
     */
    protected boolean shouldHideDocument(@NonNull String documentId)
            throws FileNotFoundException {
        return false;
    }

    /**
     * Some providers may want to restrict access to certain directories and files,
     * e.g. <i>"Android/data"</i> and <i>"Android/obb"</i> on the shared storage for
     * privacy reasons.
     * Such providers should override this method.
     */
    protected boolean isTrashSupported(@NonNull File document)
            throws FileNotFoundException {
        return false;
    }

    /**
     * Returns true if this provider supports subtree search. Subclasses can override this to enable
     * the feature for gradual migration.
     */
    protected boolean isSubtreeSearchSupported() {
        return false;
    }

    protected String getRelativePathFromRoot(@NonNull String path) throws FileNotFoundException {
        return null;
    }

    /**
     * A variant of the {@link #shouldHideDocument(String)} that takes a {@link File} instead of
     * a {@link String} {@code documentId}.
     *
     * @see #shouldHideDocument(String)
     */
    protected final boolean shouldHideDocument(@NonNull File document)
            throws FileNotFoundException {
        return shouldHideDocument(getDocIdForFile(document));
    }

    /**
     * @return if the directory that should be blocked from being selected when the user launches
     * an {@link Intent#ACTION_OPEN_DOCUMENT_TREE} intent.
     *
     * @see Document#FLAG_DIR_BLOCKS_OPEN_DOCUMENT_TREE
     */
    protected boolean shouldBlockDirectoryFromTree(@NonNull String documentId)
            throws FileNotFoundException {
        return false;
    }

    protected boolean typeSupportsMetadata(String mimeType) {
        return MetadataReader.isSupportedMimeType(mimeType)
                || Document.MIME_TYPE_DIR.equals(mimeType);
    }

    protected final File getFileForDocId(String docId) throws FileNotFoundException {
        return getFileForDocId(docId, false);
    }

    @Nullable
    protected Uri buildTrashNotificationUri(@NonNull String docId) {
        return null;
    }

    private String[] resolveProjection(String[] projection) {
        return projection == null ? mDefaultProjection : projection;
    }

    private void startObserving(File file, Uri notifyUri, DirectoryCursor cursor) {
        synchronized (mObservers) {
            DirectoryObserver observer = mObservers.get(file);
            if (observer == null) {
                observer =
                        new DirectoryObserver(file, getContext().getContentResolver(), notifyUri);
                observer.startWatching();
                mObservers.put(file, observer);
            }
            observer.mCursors.add(cursor);

            if (LOG_INOTIFY) Log.d(TAG, "after start: " + observer);
        }
    }

    private void stopObserving(File file, DirectoryCursor cursor) {
        synchronized (mObservers) {
            DirectoryObserver observer = mObservers.get(file);
            if (observer == null) return;

            observer.mCursors.remove(cursor);
            if (observer.mCursors.size() == 0) {
                mObservers.remove(file);
                observer.stopWatching();
            }

            if (LOG_INOTIFY) Log.d(TAG, "after stop: " + observer);
        }
    }

    /**
     * Adds the original relative path of a trashed file to the given row.
     * @param row The row to add the path to.
     * @param columns The columns of the cursor.
     * @param file The trashed file.
     */
    private void addOriginalRelativePath(RowBuilder row, String[] columns, File file)
            throws FileNotFoundException {
        final int pathColumnIndex = ArrayUtils.indexOf(columns,
                Document.COLUMN_ORIGINAL_RELATIVE_PATH);
        if (pathColumnIndex == -1) {
            return;
        }

        final String originalParentPath = getOriginalParentPath(file);
        if (originalParentPath == null) {
            return;
        }

        final String relativePath = getRelativePathFromRoot(originalParentPath);
        if (!TextUtils.isEmpty(relativePath)) {
            row.add(pathColumnIndex, relativePath);
        }
    }

    /**
     * Gets the original absolute parent path for a given trashed file.
     *
     * @param file The trashed file.
     * @return The absolute path of the original parent directory.
     */
    @Nullable
    private String getOriginalParentPath(File file) {
        if (!isTrashFile(file)) {
            return null;
        }

        final String parentPath = file.getParent();
        if (parentPath == null) {
            return null;
        }

        final String trashDirSuffix = File.separator + DIRECTORY_TRASH_STORAGE;
        final String trashDir = trashDirSuffix + File.separator;
        final int trashRootEndIndex = parentPath.indexOf(trashDir);

        // e.g., /storage/emulated/0/.trash-storage/.trashed-123-Folder
        if (trashRootEndIndex == -1) {
            // Check if the parent is the .trash-storage directory itself
            if (parentPath.endsWith(trashDirSuffix)) {
                // The original parent is the volume root.
                return parentPath.substring(0, parentPath.length() - trashDirSuffix.length());
            }

            // If a trashed file doesn't exist inside .trash-storage then it's a legacy trashed
            // file. e.g., /storage/emulated/0/Download/.trashed-123-file
            return removeTrashPrefixFromPath(parentPath);
        }

        // e.g., /storage/emulated/0
        final String volumePath = parentPath.substring(0, trashRootEndIndex);

        // e.g., Download/.trashed-123-Folder
        final String pathInsideTrash = parentPath.substring(trashRootEndIndex + trashDir.length());

        // e.g., Download/Folder
        final String cleanPathInsideTrash = removeTrashPrefixFromPath(pathInsideTrash);

        return new File(volumePath, cleanPathInsideTrash).getAbsolutePath();
    }

    /**
     * Reconstructs an original path from a path that may contain trashed directory names.
     * This method iterates through each segment of the given path and removes the trashed prefix
     * (e.g., ".trashed-123-") from any segment that matches the trashed file pattern.
     *
     * @param path The path string to clean
     * @return The reconstructed path with trashed prefixes removed from its segments.
     */
    private String removeTrashPrefixFromPath(String path) {
        if (TextUtils.isEmpty(path)) {
            return "";
        }
        final String[] segments = path.split(File.separator);
        final List<String> cleanedSegments = new ArrayList<>();
        for (String segment : segments) {
            cleanedSegments.add(removeTrashPrefixFromSegment(segment));
        }
        return String.join(File.separator, cleanedSegments);
    }

    /**
     * Removes the trashed prefix from a single path segment if it exists.
     * For example, ".trashed-12345-MyFolder" becomes "MyFolder".
     *
     * @param segment The path segment to remove the trash prefix from.
     * @return The cleaned segment, or the original segment if it doesn't represent
     * a trashed item.
     */
    private String removeTrashPrefixFromSegment(String segment) {
        if (segment == null) {
            return null;
        }
        final Matcher matcher = PATTERN_EXPIRES_FILE.matcher(segment);
        if (matcher.matches() && PREFIX_TRASHED.equals(matcher.group(1))) {
            // Return the original name part of the trashed file pattern
            return matcher.group(3);
        }
        return segment;
    }

    private void notifyTrashChange(String docId) {
        if (!enableDocumentsTrashApi()) {
            return;
        }

        Uri trashUri = buildTrashNotificationUri(docId);
        if (trashUri != null) {
            getContext().getContentResolver().notifyChange(trashUri, /* observer */ null);
        }
    }

    private static class DirectoryObserver extends FileObserver {
        private static final int NOTIFY_EVENTS = ATTRIB | CLOSE_WRITE | MOVED_FROM | MOVED_TO
                | CREATE | DELETE | DELETE_SELF | MOVE_SELF;

        private final File mFile;
        private final ContentResolver mResolver;
        private final Uri mNotifyUri;
        private final CopyOnWriteArrayList<DirectoryCursor> mCursors;

        DirectoryObserver(File file, ContentResolver resolver, Uri notifyUri) {
            super(file.getAbsolutePath(), NOTIFY_EVENTS);
            mFile = file;
            mResolver = resolver;
            mNotifyUri = notifyUri;
            mCursors = new CopyOnWriteArrayList<>();
        }

        @Override
        public void onEvent(int event, String path) {
            if ((event & NOTIFY_EVENTS) != 0) {
                if (LOG_INOTIFY) Log.d(TAG, "onEvent() " + event + " at " + path);
                for (DirectoryCursor cursor : mCursors) {
                    cursor.notifyChanged();
                }
                mResolver.notifyChange(mNotifyUri, null, false);
            }
        }

        @Override
        public String toString() {
            String filePath = mFile.getAbsolutePath();
            return "DirectoryObserver{file=" + filePath + ", ref=" + mCursors.size() + "}";
        }
    }

    /**
     * A {@link MatrixCursor} that can be manually notified of changes. We use this instead of
     * {@link DirectoryCursor} for initial loading to avoid unwanted notifications from the
     * underlying file system, which could trigger redundant synchronous loads.
     */
    private static class LoadingCursor extends MatrixCursor {
        private @Nullable Future<?> mJob;

        LoadingCursor(String[] columnNames) {
            super(columnNames);
        }

        /**
         * Assigns the background job responsible for population. If the cursor is already closed,
         * the job is cancelled immediately.
         */
        public void setJob(@Nullable Future<?> job) {
            synchronized (this) {
                if (isClosed() && job != null) {
                    job.cancel(true);
                } else {
                    mJob = job;
                }
            }
        }

        /**
         * Exposes the protected {@link #onChange(boolean)} method to allow background loaders to
         * signal that the cached result is ready.
         */
        public void notifyChanged() {
            onChange(false);
        }

        @Override
        public void close() {
            synchronized (this) {
                if (mJob != null && !mJob.isDone()) {
                    Log.w(TAG, "Cancelling async loading while in progress");
                    mJob.cancel(true);
                }
            }
            super.close();
        }
    }

    private class DirectoryCursor extends MatrixCursor {
        private final File mFile;

        public DirectoryCursor(String[] columnNames, String docId, File file) {
            super(columnNames);

            final Uri notifyUri = buildNotificationUri(docId);
            boolean registerSelfObserver = false; // Our FileObserver sees all relevant changes.
            setNotificationUris(getContext().getContentResolver(), Arrays.asList(notifyUri),
                    getContext().getContentResolver().getUserId(), registerSelfObserver);

            mFile = file;
            startObserving(mFile, notifyUri, this);
        }

        public void notifyChanged() {
            onChange(false);
        }

        @Override
        public void close() {
            super.close();
            stopObserving(mFile, this);
        }
    }
}
