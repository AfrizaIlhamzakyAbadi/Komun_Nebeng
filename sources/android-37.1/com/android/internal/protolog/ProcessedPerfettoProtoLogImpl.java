/*
 * Copyright (C) 2024 The Android Open Source Project
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

package com.android.internal.protolog;

import android.annotation.NonNull;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.util.Log;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.protolog.IProtoLogConfigurationService.RegisterClientArgs;
import com.android.internal.protolog.common.ILogger;
import com.android.internal.protolog.common.IProtoLogGroup;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public class ProcessedPerfettoProtoLogImpl extends PerfettoProtoLogImpl {
    private static final String LOG_TAG = "PerfettoProtoLogImpl";

    @NonNull
    private final ProtoLogViewerConfigReader mViewerConfigReader;
    @Deprecated
    @NonNull
    private final ViewerConfigInputStreamProvider mViewerConfigInputStreamProvider;
    @NonNull
    private final String mViewerConfigFilePath;

    /**
     * Caches messages decoded via the slow path (reading from the viewer config file).
     * This prevents disk thrashing when a race condition unloads the viewer config
     * from memory while concurrent logging calls are still active (b/502616008).
     */
    @SuppressWarnings("AndroidFrameworkEfficientCollections")
    private final ConcurrentHashMap<Long, String> mFallbackDecodedMessages =
            new ConcurrentHashMap<>();

    public ProcessedPerfettoProtoLogImpl(
            @NonNull ProtoLogDataSource datasource,
            @NonNull String viewerConfigFilePath,
            @NonNull ProtoLogCacheUpdater cacheUpdater,
            @NonNull IProtoLogGroup[] groups) throws ServiceManager.ServiceNotFoundException {
        this(datasource, viewerConfigFilePath, (ViewerConfigInputStreamProvider) () -> {
            try {
                final var protoFileInputStream = new FileInputStream(viewerConfigFilePath);
                return new AutoClosableProtoInputStream(protoFileInputStream);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(
                        "Failed to load viewer config file " + viewerConfigFilePath, e);
            }
        },
                        cacheUpdater, groups);
    }

    @VisibleForTesting
    public ProcessedPerfettoProtoLogImpl(
            @NonNull ProtoLogDataSource datasource,
            @NonNull String viewerConfigFilePath,
            @NonNull ViewerConfigInputStreamProvider viewerConfigInputStreamProvider,
            @NonNull ProtoLogCacheUpdater cacheUpdater,
            @NonNull IProtoLogGroup[] groups) throws ServiceManager.ServiceNotFoundException {
        super(datasource, cacheUpdater, groups);

        this.mViewerConfigFilePath = viewerConfigFilePath;

        this.mViewerConfigInputStreamProvider = viewerConfigInputStreamProvider;
        this.mViewerConfigReader = new ProtoLogViewerConfigReader(viewerConfigInputStreamProvider);

        loadLogcatGroupsViewerConfig(groups);
    }

    @VisibleForTesting
    public ProcessedPerfettoProtoLogImpl(
            @NonNull ProtoLogDataSource datasource,
            @NonNull String viewerConfigFilePath,
            @NonNull ViewerConfigInputStreamProvider viewerConfigInputStreamProvider,
            @NonNull ProtoLogViewerConfigReader viewerConfigReader,
            @NonNull ProtoLogCacheUpdater cacheUpdater,
            @NonNull IProtoLogGroup[] groups) {
        super(datasource, cacheUpdater, groups);

        this.mViewerConfigFilePath = viewerConfigFilePath;

        this.mViewerConfigInputStreamProvider = viewerConfigInputStreamProvider;
        this.mViewerConfigReader = viewerConfigReader;

        loadLogcatGroupsViewerConfig(groups);
    }

    @VisibleForTesting
    public ProcessedPerfettoProtoLogImpl(
            @NonNull ProtoLogDataSource datasource,
            @NonNull String viewerConfigFilePath,
            @NonNull ViewerConfigInputStreamProvider viewerConfigInputStreamProvider,
            @NonNull ProtoLogViewerConfigReader viewerConfigReader,
            @NonNull ProtoLogCacheUpdater cacheUpdater,
            @NonNull IProtoLogGroup[] groups,
            @NonNull ExecutorService executorService) {
        super(datasource, cacheUpdater, groups, executorService);

        this.mViewerConfigFilePath = viewerConfigFilePath;

        this.mViewerConfigInputStreamProvider = viewerConfigInputStreamProvider;
        this.mViewerConfigReader = viewerConfigReader;

        loadLogcatGroupsViewerConfig(groups);
    }

    @NonNull
    @Override
    protected RegisterClientArgs createConfigurationServiceRegisterClientArgs() {
        var args = new RegisterClientArgs();
        args.viewerConfigFile = mViewerConfigFilePath;
        return args;
    }

    /**
     * Start text logging
     * @param groups Groups to start text logging for
     * @param logger A logger to write status updates to
     * @return status code
     */
    @Override
    public int startLoggingToLogcat(@NonNull String[] groups, @NonNull ILogger logger) {
        if (!validateGroups(logger, groups)) {
            return -1;
        }

        mViewerConfigReader.loadViewerConfig(groups, logger);
        // Clear cache since new configs are loaded into memory. Which means we might not need the
        // fallback decoded messages anymore. This avoids keeping stale data around, and also keeps
        // memory usage down.
        mFallbackDecodedMessages.clear();
        return super.startLoggingToLogcat(groups, logger);
    }

    /**
     * Stop text logging
     * @param groups Groups to start text logging for
     * @param logger A logger to write status updates to
     * @return status code
     */
    @Override
    public int stopLoggingToLogcat(@NonNull String[] groups, @NonNull ILogger logger) {
        if (!validateGroups(logger, groups)) {
            return -1;
        }

        var status = super.stopLoggingToLogcat(groups, logger);

        if (status != 0) {
            throw new RuntimeException("Failed to stop logging to logcat");
        }

        // If we successfully disabled logging, unload the viewer config.
        mViewerConfigReader.unloadViewerConfig(groups, logger);
        return status;
    }

    @Override
    protected String getViewerConfigPath() {
        return mViewerConfigFilePath;
    }

    @Deprecated
    @Override
    void dumpViewerConfig() {
        Log.d(LOG_TAG, "Dumping viewer config to trace from " + mViewerConfigFilePath);
        Utils.dumpViewerConfig(mDataSource, mViewerConfigInputStreamProvider);
        Log.d(LOG_TAG, "Successfully dumped viewer config to trace from " + mViewerConfigFilePath);
    }

    @NonNull
    @Override
    String getLogcatMessageString(@NonNull Message message) {
        String messageString;
        messageString = message.getMessage(mViewerConfigReader);

        if (messageString == null) {
            Long hash = message.getMessageHash();

            if (hash != null) {
                String cachedMessage = mFallbackDecodedMessages.get(hash);
                if (cachedMessage != null) {
                    Log.w(LOG_TAG, "Reading ProtoLog message from cache to log to logcat! "
                            + "Look at earlier logs for more details.");
                    return cachedMessage;
                }
            }

            boolean debugMode =
                    SystemProperties.getBoolean("persist.protolog.debug_unknown_messages", false);

            if (hash != null) {
                if (debugMode) {
                    try {
                        ProtoLogViewerConfigReader.MessageData messageData = mViewerConfigReader
                                .getMessageDataForHashFromFile(hash);
                        if (messageData != null) {
                            Log.w(LOG_TAG, "Message hash (" + hash + ") was available in the "
                                    + "viewerConfig file (" + mViewerConfigFilePath + ") but "
                                    + "wasn't loaded into memory before decoding! Falling back to "
                                    + "reading from file.");
                            mFallbackDecodedMessages.put(hash, messageData.message);
                            return messageData.message;
                        }
                    } catch (IOException e) {
                        Log.wtf(LOG_TAG,
                                "Failed to read viewer config file for message hash: " + hash, e);
                    }
                }
            }

            String reason;
            if (debugMode) {
                reason = getReasonForFailureToGetMessageString(message);
            } else {
                reason = "Failed to decode message for logcat logging. "
                        + "Message hash (" + (hash != null ? hash : "unknown") + ") could not be "
                        + "decoded. Enable debug mode by setting system property "
                        + "'persist.protolog.debug_unknown_messages' to 'true' to get the full "
                        + "reason.";
            }
            Log.wtf(LOG_TAG, reason);

            String hashString = hash != null ? String.valueOf(hash) : "unknown";
            String fallback = "UNKNOWN PROTOLOG MESSAGE (hash: " + hashString + ")";
            if (hash != null) {
                mFallbackDecodedMessages.put(hash, fallback);
            }
            return fallback;
        }

        return messageString;
    }

    @NonNull
    private String getReasonForFailureToGetMessageString(@NonNull Message message) {
        if (message.getMessageHash() == null) {
            return "Trying to get message from null message hash";
        }

        try {
            ProtoLogViewerConfigReader.MessageData messageData =
                    mViewerConfigReader.getMessageDataForHashFromFile(message.getMessageHash());
            if (messageData == null) {
                return "Failed to decode message for logcat logging. "
                        + "Message hash (" + message.getMessageHash() + ") is not available in "
                        + "viewerConfig file (" +  mViewerConfigFilePath + "). This might be due "
                        + "to the viewer config file and the executing code being out of sync.";
            } else {
                return "Failed to decode message for logcat. "
                        + "Message hash (" + message.getMessageHash()
                        + ") was available in the viewerConfig file (" + mViewerConfigFilePath
                        + ") but wasn't loaded into memory from file before decoding! "
                        + "This is likely a bug. Message: '" + messageData.message
                        + "', group: '" + messageData.group + "'.";
            }
        } catch (IOException e) {
            return "Failed to get string message to log but could not identify the root cause due "
                    + "to an IO error in reading the viewer config file.";
        }
    }

    private void loadLogcatGroupsViewerConfig(@NonNull IProtoLogGroup[] protoLogGroups) {
        final var groupsLoggingToLogcat = new ArrayList<String>();
        for (IProtoLogGroup protoLogGroup : protoLogGroups) {
            if (protoLogGroup.isLogToLogcat()) {
                groupsLoggingToLogcat.add(protoLogGroup.name());
            }
        }

        // Load in background to avoid delay in boot process.
        // The caveat is that any log message that is also logged to logcat will not be
        // successfully decoded until this completes.
        mSingleThreadedExecutor.execute(() -> {
            mViewerConfigReader.loadViewerConfig(groupsLoggingToLogcat.toArray(new String[0]));
            // Clear cache since new configs are loaded into memory.
            // We clear on load (and not on unload) so that the cache remains active
            // during the race condition window when configs are temporarily unloaded.
            mFallbackDecodedMessages.clear();
            readyToLogToLogcat();
        });
    }
}
