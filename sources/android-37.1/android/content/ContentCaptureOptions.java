/*
 * Copyright (C) 2018 The Android Open Source Project
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
package android.content;

import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SuppressLint;
import android.annotation.TestApi;
import android.app.ActivityThread;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.view.contentcapture.ContentCaptureManager;
import android.view.contentcapture.ContentCaptureManager.ContentCaptureClient;
import android.view.contentcapture.ContentCaptureSession;
import android.view.contentcapture.flags.Flags;

import com.android.internal.annotations.VisibleForTesting;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Content capture options for a given package.
 *
 * <p>This object is created by the Content Capture System Service and passed back to the app when
 * the application is created.
 */
@TestApi
public final class ContentCaptureOptions implements Parcelable {

    private static final String TAG = ContentCaptureOptions.class.getSimpleName();

    /**
     * Logging level for {@code logcat} statements.
     */
    public final int loggingLevel;

    /**
     * Maximum number of events that are buffered before sent to the app.
     */
    public final int maxBufferSize;

    /**
     * Frequency the buffer is flushed if idle.
     */
    public final int idleFlushingFrequencyMs;

    /**
     * Frequency the buffer is flushed if last event is a text change.
     */
    public final int textChangeFlushingFrequencyMs;

    /**
     * Size of events that are logging on {@code dump}.
     */
    public final int logHistorySize;

    /**
     * Is the content capture receiver enabled.
     */
    @Hide
    public final boolean enableReceiver;

    /**
     * The Content Capture API version enabled for this package.
     */
    @Hide
    @ContentCaptureSession.ContentCaptureVersion
    public final int contentCaptureVersion;

    /**
     * Options for the content protection flow.
     */
    @Hide
    @NonNull public final ContentProtectionOptions contentProtectionOptions;

    /**
     * List of activities explicitly allowlisted for content capture (or {@code null} if allowlisted
     * for all activities in the package).
     */
    @Nullable
    @SuppressLint("NullableCollection")
    public final ArraySet<ComponentName> whitelistedComponents;

    /**
     * Map of activities explicitly allowlisted for specific Content Capture versions.
     */
    @Hide
    public final ArrayMap<ComponentName, Integer> componentNameVersionMapping;

    /**
     * Used to enable just a small set of APIs so it can used by activities belonging to the
     * content capture service APK.
     */
    public final boolean lite;

    /**
     * Constructor for "lite" objects that are just used to enable a {@link ContentCaptureManager}
     * for contexts belonging to the content capture service app.
     */
    public ContentCaptureOptions(int loggingLevel) {
        this(
                /* lite= */ true,
                loggingLevel,
                /* maxBufferSize= */ 0,
                /* idleFlushingFrequencyMs= */ 0,
                /* textChangeFlushingFrequencyMs= */ 0,
                /* logHistorySize= */ 0,
                /* enableReceiver= */ false,
                ContentCaptureManager.DEFAULT_CONTENT_CAPTURE_VERSION,
                new ContentProtectionOptions(
                        /* enableReceiver= */ false,
                        /* bufferSize= */ 0,
                        /* requiredGroups= */ Collections.emptyList(),
                        /* optionalGroups= */ Collections.emptyList(),
                        /* optionalGroupsThreshold= */ 0),
                /* whitelistedComponents= */ null,
                /* componentNameVersionMapping= */ new ArrayMap<>());
    }

    /** Default constructor. */
    public ContentCaptureOptions(
            int loggingLevel,
            int maxBufferSize,
            int idleFlushingFrequencyMs,
            int textChangeFlushingFrequencyMs,
            int logHistorySize,
            @SuppressLint({"ConcreteCollection", "NullableCollection"}) @Nullable
                    ArraySet<ComponentName> whitelistedComponents) {
        this(
                /* lite= */ false,
                loggingLevel,
                maxBufferSize,
                idleFlushingFrequencyMs,
                textChangeFlushingFrequencyMs,
                logHistorySize,
                ContentCaptureManager.DEFAULT_ENABLE_CONTENT_CAPTURE_RECEIVER,
                ContentCaptureManager.DEFAULT_CONTENT_CAPTURE_VERSION,
                new ContentProtectionOptions(),
                whitelistedComponents,
                /* componentNameVersionMapping= */ new ArrayMap<>());
    }

    @Hide
    public ContentCaptureOptions(
            int loggingLevel,
            int maxBufferSize,
            int idleFlushingFrequencyMs,
            int textChangeFlushingFrequencyMs,
            int logHistorySize,
            boolean enableReceiver,
            @ContentCaptureSession.ContentCaptureVersion int contentCaptureVersion,
            @NonNull ContentProtectionOptions contentProtectionOptions,
            @SuppressLint({"ConcreteCollection", "NullableCollection"}) @Nullable
                    ArraySet<ComponentName> whitelistedComponents,
            @SuppressLint("ConcreteCollection")
                    ArrayMap<ComponentName, Integer> componentNameVersionMapping) {
        this(
                /* lite= */ false,
                loggingLevel,
                maxBufferSize,
                idleFlushingFrequencyMs,
                textChangeFlushingFrequencyMs,
                logHistorySize,
                enableReceiver,
                contentCaptureVersion,
                contentProtectionOptions,
                whitelistedComponents,
                componentNameVersionMapping);
    }

    @Hide
    @VisibleForTesting
    public ContentCaptureOptions(@Nullable ArraySet<ComponentName> whitelistedComponents) {
        this(
                ContentCaptureManager.LOGGING_LEVEL_VERBOSE,
                ContentCaptureManager.DEFAULT_MAX_BUFFER_SIZE,
                ContentCaptureManager.DEFAULT_IDLE_FLUSHING_FREQUENCY_MS,
                ContentCaptureManager.DEFAULT_TEXT_CHANGE_FLUSHING_FREQUENCY_MS,
                ContentCaptureManager.DEFAULT_LOG_HISTORY_SIZE,
                ContentCaptureManager.DEFAULT_ENABLE_CONTENT_CAPTURE_RECEIVER,
                ContentCaptureManager.DEFAULT_CONTENT_CAPTURE_VERSION,
                new ContentProtectionOptions(),
                whitelistedComponents,
                /* componentNameVersionMapping= */ new ArrayMap<>());
    }

    private ContentCaptureOptions(
            boolean lite,
            int loggingLevel,
            int maxBufferSize,
            int idleFlushingFrequencyMs,
            int textChangeFlushingFrequencyMs,
            int logHistorySize,
            boolean enableReceiver,
            @ContentCaptureSession.ContentCaptureVersion int contentCaptureVersion,
            @NonNull ContentProtectionOptions contentProtectionOptions,
            @SuppressLint({"ConcreteCollection", "NullableCollection"}) @Nullable
                    ArraySet<ComponentName> whitelistedComponents,
            @SuppressLint("ConcreteCollection")
                    ArrayMap<ComponentName, Integer> componentNameVersionMapping) {
        this.lite = lite;
        this.loggingLevel = loggingLevel;
        this.maxBufferSize = maxBufferSize;
        this.idleFlushingFrequencyMs = idleFlushingFrequencyMs;
        this.textChangeFlushingFrequencyMs = textChangeFlushingFrequencyMs;
        this.logHistorySize = logHistorySize;
        this.enableReceiver = enableReceiver;
        this.contentCaptureVersion = contentCaptureVersion;
        this.contentProtectionOptions = contentProtectionOptions;
        this.whitelistedComponents = whitelistedComponents;
        this.componentNameVersionMapping = componentNameVersionMapping;
    }

    /**
     * Resolves the version of Content Capture for the given component.
     */
    @Hide
    @ContentCaptureSession.ContentCaptureVersion
    public int getContentCaptureVersion(@NonNull ComponentName componentName) {
        // Resolution hierarchy:
        // 1. Check if aflag is enabled (V1 if not).
        // 2. ComponentName query.
        // 3. Package level default.
        if (!Flags.ccapiNextEnabled()) return ContentCaptureSession.VERSION_1;
        Integer version = componentNameVersionMapping.get(componentName);
        if (version != null) {
            return version;
        }
        return contentCaptureVersion;
    }

    /**
     * Returns the Content Capture version assigned for this package.
     */
    @Hide
    @ContentCaptureSession.ContentCaptureVersion
    public int getContentCaptureVersion() {
        if (!Flags.ccapiNextEnabled()) return ContentCaptureSession.VERSION_1;
        return contentCaptureVersion;
    }

    public static ContentCaptureOptions forWhitelistingItself() {
        final ActivityThread at = ActivityThread.currentActivityThread();
        if (at == null) {
            throw new IllegalStateException("No ActivityThread");
        }

        final String packageName = at.getApplication().getPackageName();

        if (!"android.contentcaptureservice.cts".equals(packageName)
                && !"android.translation.cts".equals(packageName)) {
            Log.e(TAG, "forWhitelistingItself(): called by " + packageName);
            throw new SecurityException("Thou shall not pass!");
        }

        final ContentCaptureOptions options =
                new ContentCaptureOptions(/* whitelistedComponents= */ null);
        // Always log, as it's used by test only
        Log.i(TAG, "forWhitelistingItself(" + packageName + "): " + options);

        return options;
    }

    @Hide
    @VisibleForTesting
    public boolean isWhitelisted(@NonNull Context context) {
        if (whitelistedComponents == null) return true; // whole package is allowlisted
        final ContentCaptureClient client = context.getContentCaptureClient();
        if (client == null) {
            // Shouldn't happen, but it doesn't hurt to check...
            Log.w(TAG, "isWhitelisted(): no ContentCaptureClient on " + context);
            return false;
        }
        return whitelistedComponents.contains(client.contentCaptureClientGetComponentName());
    }

    @Override
    public String toString() {
        if (lite) {
            return "ContentCaptureOptions [loggingLevel=" + loggingLevel + " (lite)]";
        }
        final StringBuilder string = new StringBuilder("ContentCaptureOptions [");
        string.append("loggingLevel=")
                .append(loggingLevel)
                .append(", maxBufferSize=")
                .append(maxBufferSize)
                .append(", idleFlushingFrequencyMs=")
                .append(idleFlushingFrequencyMs)
                .append(", textChangeFlushingFrequencyMs=")
                .append(textChangeFlushingFrequencyMs)
                .append(", logHistorySize=")
                .append(logHistorySize)
                .append(", enableReceiver=")
                .append(enableReceiver)
                .append(", contentCaptureVersion=")
                .append(contentCaptureVersion)
                .append(", contentProtectionOptions=")
                .append(contentProtectionOptions);
        if (whitelistedComponents != null) {
            string.append(", whitelisted=").append(whitelistedComponents);
        }
        if (!componentNameVersionMapping.isEmpty()) {
            string.append(", componentNameVersionMapping=").append(componentNameVersionMapping);
        }
        return string.append(']').toString();
    }

    @Hide
    public void dumpShort(@NonNull PrintWriter pw) {
        pw.print("logLvl="); pw.print(loggingLevel);
        if (lite) {
            pw.print(", lite");
            return;
        }
        pw.print(", bufferSize=");
        pw.print(maxBufferSize);
        pw.print(", idle=");
        pw.print(idleFlushingFrequencyMs);
        pw.print(", textIdle=");
        pw.print(textChangeFlushingFrequencyMs);
        pw.print(", logSize=");
        pw.print(logHistorySize);
        pw.print(", enableReceiver=");
        pw.print(enableReceiver);
        pw.print(", contentCaptureVersion=");
        pw.print(contentCaptureVersion);
        pw.print(", contentProtectionOptions=[");
        contentProtectionOptions.dumpShort(pw);
        pw.print("]");
        if (whitelistedComponents != null) {
            pw.print(", whitelisted="); pw.print(whitelistedComponents);
        }
        if (!componentNameVersionMapping.isEmpty()) {
            pw.print(", componentNameVersionMapping="); pw.print(componentNameVersionMapping);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeBoolean(lite);
        parcel.writeInt(loggingLevel);
        if (lite) return;

        parcel.writeInt(maxBufferSize);
        parcel.writeInt(idleFlushingFrequencyMs);
        parcel.writeInt(textChangeFlushingFrequencyMs);
        parcel.writeInt(logHistorySize);
        parcel.writeBoolean(enableReceiver);
        parcel.writeInt(contentCaptureVersion);
        contentProtectionOptions.writeToParcel(parcel);
        parcel.writeArraySet(whitelistedComponents);
        parcel.writeInt(componentNameVersionMapping.size());
        for (int i = 0; i < componentNameVersionMapping.size(); i++) {
            componentNameVersionMapping.keyAt(i).writeToParcel(parcel, flags);
            parcel.writeInt(componentNameVersionMapping.valueAt(i));
        }
    }

    public static final @android.annotation.NonNull Parcelable.Creator<ContentCaptureOptions> CREATOR =
            new Parcelable.Creator<ContentCaptureOptions>() {

                @Override
                public ContentCaptureOptions createFromParcel(Parcel parcel) {
                    final boolean lite = parcel.readBoolean();
                    final int loggingLevel = parcel.readInt();
                    if (lite) {
                        return new ContentCaptureOptions(loggingLevel);
                    }
                    final int maxBufferSize = parcel.readInt();
                    final int idleFlushingFrequencyMs = parcel.readInt();
                    final int textChangeFlushingFrequencyMs = parcel.readInt();
                    final int logHistorySize = parcel.readInt();
                    final boolean enableReceiver = parcel.readBoolean();
                    final int contentCaptureVersion = parcel.readInt();
                    final ContentProtectionOptions contentProtectionOptions =
                            ContentProtectionOptions.createFromParcel(parcel);
                    @SuppressWarnings("unchecked")
                    final ArraySet<ComponentName> whitelistedComponents =
                            (ArraySet<ComponentName>) parcel.readArraySet(null);
                    final int size = parcel.readInt();
                    final ArrayMap<ComponentName, Integer> componentNameVersionMapping =
                            new ArrayMap<>(size);
                    for (int i = 0; i < size; i++) {
                        ComponentName cn = ComponentName.CREATOR.createFromParcel(parcel);
                        int ver = parcel.readInt();
                        componentNameVersionMapping.put(cn, ver);
                    }
                    return new ContentCaptureOptions(
                            loggingLevel,
                            maxBufferSize,
                            idleFlushingFrequencyMs,
                            textChangeFlushingFrequencyMs,
                            logHistorySize,
                            enableReceiver,
                            contentCaptureVersion,
                            contentProtectionOptions,
                            whitelistedComponents,
                            componentNameVersionMapping);
                }

                @Override
                public ContentCaptureOptions[] newArray(int size) {
                    return new ContentCaptureOptions[size];
                }
    };

    /**
     * Content protection options for a given package.
     *
     * <p>Does not implement {@code Parcelable} since it is an inner class without a matching AIDL.
     */
    @Hide
    public static class ContentProtectionOptions {

        /**
         * Is the content protection receiver enabled.
         */
        @Hide
        public final boolean enableReceiver;

        /**
         * Size of the in-memory ring buffer for the content protection flow.
         */
        @Hide
        public final int bufferSize;

        /**
         * The list of required groups of strings to match.
         */
        @Hide
        @NonNull public final List<List<String>> requiredGroups;

        /**
         * The list of optional groups of strings to match.
         */
        @Hide
        @NonNull public final List<List<String>> optionalGroups;

        /**
         * The minimal number of optional groups that have to be matched. This is the threshold
         * value and comparison is done with greater than or equals.
         */
        @Hide
        public final int optionalGroupsThreshold;

        /**
         * Empty constructor with default values.
         */
        @Hide
        public ContentProtectionOptions() {
            this(
                    ContentCaptureManager.DEFAULT_ENABLE_CONTENT_PROTECTION_RECEIVER,
                    ContentCaptureManager.DEFAULT_CONTENT_PROTECTION_BUFFER_SIZE,
                    ContentCaptureManager.DEFAULT_CONTENT_PROTECTION_REQUIRED_GROUPS,
                    ContentCaptureManager.DEFAULT_CONTENT_PROTECTION_OPTIONAL_GROUPS,
                    ContentCaptureManager.DEFAULT_CONTENT_PROTECTION_OPTIONAL_GROUPS_THRESHOLD);
        }

        /**
         * Full primary constructor.
         */
        @Hide
        public ContentProtectionOptions(
                boolean enableReceiver,
                int bufferSize,
                @NonNull List<List<String>> requiredGroups,
                @NonNull List<List<String>> optionalGroups,
                int optionalGroupsThreshold) {
            this.enableReceiver = enableReceiver;
            this.bufferSize = bufferSize;
            this.requiredGroups = requiredGroups;
            this.optionalGroups = optionalGroups;
            this.optionalGroupsThreshold = optionalGroupsThreshold;
        }

        @Override
        public String toString() {
            StringBuilder stringBuilder = new StringBuilder("ContentProtectionOptions [");
            stringBuilder
                    .append("enableReceiver=")
                    .append(enableReceiver)
                    .append(", bufferSize=")
                    .append(bufferSize)
                    .append(", requiredGroupsSize=")
                    .append(requiredGroups.size())
                    .append(", optionalGroupsSize=")
                    .append(optionalGroups.size())
                    .append(", optionalGroupsThreshold=")
                    .append(optionalGroupsThreshold);

            return stringBuilder.append(']').toString();
        }

        private void dumpShort(@NonNull PrintWriter pw) {
            pw.print("enableReceiver=");
            pw.print(enableReceiver);
            pw.print(", bufferSize=");
            pw.print(bufferSize);
            pw.print(", requiredGroupsSize=");
            pw.print(requiredGroups.size());
            pw.print(", optionalGroupsSize=");
            pw.print(optionalGroups.size());
            pw.print(", optionalGroupsThreshold=");
            pw.print(optionalGroupsThreshold);
        }

        private void writeToParcel(@NonNull Parcel parcel) {
            parcel.writeBoolean(enableReceiver);
            parcel.writeInt(bufferSize);
            writeGroupsToParcel(requiredGroups, parcel);
            writeGroupsToParcel(optionalGroups, parcel);
            parcel.writeInt(optionalGroupsThreshold);
        }

        @NonNull
        private static ContentProtectionOptions createFromParcel(@NonNull Parcel parcel) {
            boolean enableReceiver = parcel.readBoolean();
            int bufferSize = parcel.readInt();
            List<List<String>> requiredGroups = createGroupsFromParcel(parcel);
            List<List<String>> optionalGroups = createGroupsFromParcel(parcel);
            int optionalGroupsThreshold = parcel.readInt();
            return new ContentProtectionOptions(
                    enableReceiver,
                    bufferSize,
                    requiredGroups,
                    optionalGroups,
                    optionalGroupsThreshold);
        }

        private static void writeGroupsToParcel(
                @NonNull List<List<String>> groups, @NonNull Parcel parcel) {
            parcel.writeInt(groups.size());
            groups.forEach(parcel::writeStringList);
        }

        @NonNull
        private static List<List<String>> createGroupsFromParcel(@NonNull Parcel parcel) {
            int size = parcel.readInt();
            return IntStream.range(0, size)
                    .mapToObj(i -> new ArrayList<String>())
                    .peek(parcel::readStringList)
                    .collect(Collectors.toUnmodifiableList());
        }
    }
}
