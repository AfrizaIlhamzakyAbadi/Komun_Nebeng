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
 * limitations under the License
 */

package android.provider;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CancellationSignal;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The contract between the People Provider and applications. Defines the URIs and columns for
 * interacting with unified person and group data.
 *
 * <h3>Overview</h3>
 *
 * <p>PeopleContract provides a standardized, read-only interface for communication apps (e.g.,
 * VoIP, messaging) to expose their person and group identities to system apps like dialers and
 * contacts.
 *
 * <p>The interaction is divided into the following roles:
 *
 * <ul>
 *   <li><b>People Directories</b>: Apps that provide people identities implement a {@link
 *       ContentProvider} that supports queries with URIs defined in {@link Persons} and {@link
 *       Groups}. By doing so, they make their persons and groups accessible to other apps on the
 *       device in a standardized format. The provider should handle queries to {@link
 *       Persons#CONTENT_URI} and {@link Groups#CONTENT_URI}.
 *   <li><b>Clients</b>: System apps (e.g., dialers, contacts apps) query this contract to present
 *       people information and actions to users. Clients can look up persons (using {@link
 *       Persons#CONTENT_URI}) by various identifiers, such as lookup keys, phone numbers, or email
 *       addresses; clients can also look up groups (using {@link Groups#CONTENT_URI}) and group
 *       members (using {@link Persons#CONTENT_URI} with {@link
 *       Persons.Query#SELECTION_TYPE_GROUP_LOOKUP_KEY}).
 * </ul>
 *
 * <h3>Platform Orchestration</h3>
 *
 * <p>A platform-level <b>People Provider</b> acts as a centralized proxy. When a client queries
 * {@link #AUTHORITY}, the <b>People Provider</b> routes queries to the appropriate <b>People
 * Directories</b> and aggregates the results into a single cursor.
 *
 * <p>To maintain system health, the platform may prefetch and cache query results, enforce strict
 * timeouts and limit concurrent directory execution to prevent system thrashing.
 *
 * <h3>Query Arguments</h3>
 *
 * <p>For persons queries, clients can specify a list of queries in the {@link #QUERY_ARG_QUERIES}
 * argument. Each query is a {@code Bundle} that specifies the target directory package, the type of
 * selection, and the selection values (e.g. lookup keys, phone numbers, email addresses, group
 * lookup keys).
 *
 * <p>For groups queries, clients can specify a list of queries in the {@link #QUERY_ARG_QUERIES}
 * argument. Each query is a {@code Bundle} that specifies the target directory package and the
 * selection values (e.g. group lookup keys).
 *
 * <p>Batch queries are supported the same way as single queries.
 */
// TODO(b/461859503): Improve documentation for PeopleContract, and define flag for APIs.
@FlaggedApi(Flags.FLAG_ENABLE_PEOPLE_CONTRACT)
public final class PeopleContract {
    private PeopleContract() {}

    /** The authority for the People Provider. */
    public static final String AUTHORITY = "com.android.people";

    /** A content:// style URI to the authority for the People Provider. */
    @NonNull public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * A content:// style URI to the notification for the People Provider.
     *
     * <p>This URI is set as extra to all result cursors from querying {@link Persons#CONTENT_URI}
     * and {@link Groups#CONTENT_URI}, which can be used to observe underlying data changes if the
     * People Directory app supports it by notifying with {@link Directory#notifyChange}.
     */
    @NonNull
    public static final Uri CONTENT_NOTIFICATION_URI =
            Uri.withAppendedPath(AUTHORITY_URI, "notification");

    /**
     * Cursor extra key for notification URI which is prefixed with {@link
     * #CONTENT_NOTIFICATION_URI}.
     *
     * <p>This extra is attached to all result cursors from querying {@link Persons#CONTENT_URI} and
     * {@link Groups#CONTENT_URI}, which can be used to observe underlying data changes if the
     * People Directory app supports it by notifying with {@link Directory#notifyChange}.
     */
    public static final String EXTRA_NOTIFICATION_URI = "android.provider.extra.NOTIFICATION_URI";

    /**
     * Query argument used by clients to specify a list of queries, each targeting a specific People
     * Directory, with a selection type (e.g. phone, email, lookup key) and selection values.
     *
     * <p>The value must be an {@code ArrayList<Bundle>} where each bundle represents a request
     * built as a {@link Bundle} containing {@link #QUERY_ARG_PACKAGE}, {@link
     * #QUERY_ARG_SELECTION_TYPE}, and {@link #QUERY_ARG_SELECTION_VALUE} keys. A convenient way to
     * build this list is to use the builders {@link Persons.Query.Builder} or {@link
     * Groups.Query.Builder}.
     */
    public static final String QUERY_ARG_QUERIES = "android:query-arg-queries";

    /**
     * Query argument specifying the package name of the target directory.
     *
     * <p>Used within the query bundles in {@link #QUERY_ARG_QUERIES}.
     */
    public static final String QUERY_ARG_PACKAGE = "android:query-arg-package";

    /**
     * Query argument specifying the type of the selection.
     *
     * <p>Used within the query bundles in {@link #QUERY_ARG_QUERIES}.
     */
    public static final String QUERY_ARG_SELECTION_TYPE = "android:query-arg-selection-type";

    /**
     * Query argument specifying the values of the selection.
     *
     * <p>Used within the query bundles in {@link #QUERY_ARG_QUERIES}.
     */
    public static final String QUERY_ARG_SELECTION_VALUE = "android:query-arg-selection-value";

    @Hide public static final String QUERY_ARG_DEPENDENCY_URI = "android:query-arg-dependency-uri";

    /**
     * Creates a securely pre-authorized {@link PendingIntent} that can be used by client
     * applications to safely start a third-party People Directory interaction Activity (e.g. call a
     * person via a VoIP app).
     *
     * <p>This method is designed to be invoked directly at event handlers (e.g. under the click
     * listener thread on button press). Invoking it synchronously at event handlers eliminates
     * memory allocations caused by pre-minting unused PendingIntents.
     *
     * <p><b>Example:</b>
     *
     * <pre>
     * callButton.setOnClickListener(view -> {
     *     try {
     *         Context context = view.getContext();
     *         Bundle requestArgs = new PeopleContract.Persons.Query.Builder()
     *                 .addPhoneQuery("com.example", List.of("555-1234"))
     *                 .build();
     *
     *         PendingIntent pendingIntent = PeopleContract.createPeopleInteractionRequest(
     *                 context.getContentResolver(),
     *                 PeopleContract.Intents.PEOPLE_INTERACTION_TYPE_VOICE,
     *                 requestArgs);
     *
     *         context.startIntentSender(pendingIntent.getIntentSender(), null, 0, 0, 0);
     *     } catch (Exception e) {
     *         Log.e(TAG, "Failed to launch interaction", e);
     *     }
     * });
     * </pre>
     *
     * <p>After receiving the {@link PendingIntent}, the caller should execute it synchronously via
     * {@link android.app.Activity#startIntentSender} to ensure that the 3P app is opened smoothly
     * on top of the current task stack, retaining proper back-button navigation.
     *
     * @param resolver The {@link ContentResolver} instance used to query the platform.
     * @param interactionType The interaction type (e.g. {@link
     *     Intents#PEOPLE_INTERACTION_TYPE_VOICE}).
     * @param queryArgs The query arguments representing the target, built using {@link
     *     Persons.Query.Builder} or {@link Groups.Query.Builder}. The query arguments must contain
     *     exactly one subquery request representing the target.
     * @return A sealed, platform-signed {@link PendingIntent} ready for execution.
     */
    @SuppressLint("PackageLayering")
    @FlaggedApi(Flags.FLAG_ENABLE_PEOPLE_CONTRACT)
    @Nullable
    public static PendingIntent createPeopleInteractionRequest(
            @NonNull ContentResolver resolver,
            @NonNull String interactionType,
            @NonNull Bundle queryArgs) {
        Objects.requireNonNull(resolver, "resolver cannot be null");
        Objects.requireNonNull(interactionType, "interactionType cannot be null");
        Objects.requireNonNull(queryArgs, "queryArgs cannot be null");

        Bundle extras = new Bundle();
        extras.putString(Intents.EXTRA_PEOPLE_INTERACTION_TYPE, interactionType);
        extras.putParcelable(Intents.EXTRA_PEOPLE_QUERY_ARGS, queryArgs);

        // TODO (b/461859503): validate there is only one query in the queryArgs.

        Bundle result =
                resolver.call(
                        AUTHORITY_URI, Intents.METHOD_CREATE_INTERACTION_INTENT, null, extras);
        if (result == null) {
            return null;
        }
        PendingIntent pendingIntent =
                result.getParcelable(
                        Intents.EXTRA_PEOPLE_INTERACTION_PENDING_INTENT, PendingIntent.class);
        return pendingIntent;
    }

    /**
     * A People Directory represents an installed application that provides person and group
     * identities.
     *
     * <h3>Discovery</h3>
     *
     * <p>The platform finds all installed content providers with meta-data identifying them as
     * directory providers in their {@code AndroidManifest.xml}:
     *
     * <pre>
     * &lt;meta-data
     *     android:name="android.content.PeopleDirectory"
     *     android:value="true" /&gt;
     * </pre>
     *
     * Ensure that each package only has one People Directory. Multiple directories per package are
     * not supported and will result in empty query results from the package.
     *
     * <h3>Mandatory vs. Optional Queries</h3>
     *
     * <p>To successfully integrate as a People Directory, source providers are to implement:
     *
     * <ul>
     *   <li>{@link Persons#CONTENT_URI} with {@link Persons.Query#SELECTION_TYPE_LOOKUP_KEY}: The
     *       canonical lookup to resolve a person using their opaque lookup key.
     * </ul>
     *
     * <p>The following query selection types are <b>optional</b>, but highly recommended depending
     * on the features the source app supports:
     *
     * <ul>
     *   <li>{@link Persons#CONTENT_URI} with {@link Persons.Query#SELECTION_TYPE_PHONE}: Required
     *       if the app's person identities can be matched by phone numbers.
     *   <li>{@link Persons#CONTENT_URI} with {@link Persons.Query#SELECTION_TYPE_EMAIL}: Required
     *       if the app's person identities can be matched by email addresses.
     *   <li>{@link Persons#CONTENT_URI} with {@link Persons.Query#SELECTION_TYPE_GROUP_LOOKUP_KEY}:
     *       Required if the app supports {@link Groups#CONTENT_URI}. This supports querying persons
     *       within a specific group.
     *   <li>{@link Groups#CONTENT_URI} with {@link Groups.Query#SELECTION_TYPE_LOOKUP_KEY}:
     *       Required if the app supports group features (e.g. displaying group calls in system call
     *       logs).
     * </ul>
     *
     * <p>The platform People Provider is read-only. Attempts to insert, update, or delete will
     * throw {@link UnsupportedOperationException}. Therefore, applications should not implement
     * these operations for People Directories.
     *
     * <h3>Error Handling & Malformed Data</h3>
     *
     * <p>If a directory provider times out, throws an exception, or returns a cursor with malformed
     * or missing required columns, the platform will safely catch the error, discard the result
     * from that specific provider, and continue aggregating results from other healthy directories.
     *
     * <p>People Directories should aim to return an empty cursor (not {@code null} or an exception)
     * if no matches are found for a valid query.
     *
     * <h3>Implementation Example</h3>
     *
     * <pre>
     * &#64;Override
     * public Cursor query(
     *     Uri uri,
     *     String[] projection,
     *     Bundle queryArgs,
     *     CancellationSignal cancellationSignal
     * ) {
     *   // The platform will route queries here.
     *   // You may check calling package here to determine whether to reject this query.
     *
     *   if (uri == Persons.CONTENT_URI) {
     *     ArrayList<Bundle> queries = queryArgs.getParcelableArrayList(
     *         PeopleContract.QUERY_ARG_QUERIES,
     *         Bundle.class
     *     );
     *
     *     // Find matching persons from internal database...
     *
     *     // Add columns to the cursor in the same order as the projection.
     *     MatrixCursor cursor = new MatrixCursor(new String[]{
     *       Persons.DISPLAY_NAME,
     *       Persons.PHOTO_URI,
     *       Persons.LOOKUP_KEY,
     *       Persons.CAPABILITY,
     *       PeopleContract.QUERY_ARG_SELECTION_TYPE,
     *       PeopleContract.QUERY_ARG_SELECTION_VALUE
     *     });
     *
     *     // Example row
     *     cursor.addRow(new Object[]{
     *       "John Doe",
     *       "content://...",
     *       "id_123",
     *       Capability.VOICE_REACHABILITY | Capability.TEXT_REACHABILITY,
     *       Persons.Query.SELECTION_TYPE_PHONE,
     *       "555-1234"
     *     });
     *   }
     *   return cursor;
     * }
     * </pre>
     */
    @FlaggedApi(Flags.FLAG_ENABLE_PEOPLE_CONTRACT)
    public static final class Directory {

        /** This utility class cannot be instantiated */
        private Directory() {}

        /** The content:// style URI for listing all available directories. */
        @NonNull
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "directories");

        /** MIME type for a single directory record. */
        @NonNull
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/people_directory";

        /** MIME type for a directory of directories. */
        @NonNull
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/people_directories";

        /** The authority of the underlying 3P Directory Provider. */
        public static final String DIRECTORY_AUTHORITY = "authority";

        /** The package name of the app owning this directory. */
        public static final String PACKAGE_NAME = "package_name";

        /**
         * Manifest metadata key for a {@link ContentProvider} to identify itself as a People
         * Directory.
         */
        public static final String DIRECTORY_METADATA = "android.content.PeopleDirectory";

        /**
         * Notifies that data has changed.
         *
         * @param context The context.
         * @param uri The URI of the changed data, either {@link Persons#CONTENT_URI} or {@link
         *     Groups#CONTENT_URI}.
         * @param queryArgs The query arguments to refresh, in the same format as the arguments to
         *     {@link android.content.ContentResolver#query}, built using {@link
         *     Persons.Query.Builder} or {@link Groups.Query.Builder}.
         * @param cancellationSignal A signal to cancel the operation.
         * @return True if the refresh was successful, false otherwise.
         */
        public static boolean notifyChange(
                @NonNull Context context,
                @NonNull Uri uri,
                @Nullable Bundle queryArgs,
                @Nullable CancellationSignal cancellationSignal) {
            ArrayList<Bundle> queries =
                    queryArgs.getParcelableArrayList(QUERY_ARG_QUERIES, Bundle.class);
            if (queries == null || queries.isEmpty()) {
                android.util.Log.d(
                        "PeopleContract", "queries is null or empty, skipping notifyChange");
                return false;
            }
            Bundle refreshBundle = new Bundle();
            refreshBundle.putParcelable(QUERY_ARG_DEPENDENCY_URI, uri);
            refreshBundle.putParcelableArrayList(PeopleContract.QUERY_ARG_QUERIES, queries);
            return context.getContentResolver()
                    .refresh(AUTHORITY_URI, refreshBundle, cancellationSignal);
        }

        /**
         * Requests the system to warm up caches and prefetch directory data.
         *
         * <p>People Directory apps should call this after the app is ready to be queried for data
         * (e.g. after user sign-in), or after a major sync to inform the system it should begin
         * prefetching data for caching. Calling this API does not guarantee cache refresh in all
         * cases. To save resources, the People Provider can schedule or even reject cache rebuilds.
         * Providers should use this API very selectively, and depend on the {@link #notifyChange}
         * API to inform the system about specific updates to persons or groups data.
         *
         * @param context The context.
         * @param cancellationSignal A signal to cancel the operation.
         * @return True if the request was successful, false otherwise.
         */
        public static boolean requestRefresh(
                @NonNull Context context, @Nullable CancellationSignal cancellationSignal) {
            return context.getContentResolver().refresh(AUTHORITY_URI, null, cancellationSignal);
        }
    }

    /**
     * Constants for querying person records. Each row represents a person identity from a People
     * Directory.
     *
     * <h3>Mandatory Integration</h3>
     *
     * <p>Directories <b>must</b> implement {@link #CONTENT_URI} with {@link
     * Query#SELECTION_TYPE_LOOKUP_KEY} to resolve persons by their stable {@link #LOOKUP_KEY}.
     * Other query selection types are optional but recommended based on app features. See {@link
     * Directory} for more details.
     */
    @FlaggedApi(Flags.FLAG_ENABLE_PEOPLE_CONTRACT)
    public static final class Persons {

        /** This utility class cannot be instantiated */
        private Persons() {}

        /** MIME type for a single person record. */
        @NonNull
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/people_person";

        /** MIME type for a directory of persons. */
        @NonNull public static final String CONTENT_TYPE = "vnd.android.cursor.dir/people_persons";

        /** Base URI for person-related queries. */
        @NonNull
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "persons");

        /** URI for resolving a person by their opaque lookup key. */
        @NonNull
        public static final Uri CONTENT_LOOKUP_URI = Uri.withAppendedPath(CONTENT_URI, "lookup");

        /**
         * The display name of the person.
         *
         * <p>TYPE: TEXT (Required)
         */
        public static final String DISPLAY_NAME = "display_name";

        /**
         * A URI pointing to the person's avatar photo.
         *
         * <p>TYPE: TEXT (Required)
         */
        public static final String PHOTO_URI = "photo_uri";

        /**
         * A stable, opaque key generated by the directory to uniquely identify this person. To
         * resolve this person later, append this key to {@link #CONTENT_LOOKUP_URI}.
         *
         * <p>TYPE:TEXT (Required)
         */
        public static final String LOOKUP_KEY = "lookup_key";

        /**
         * Bitmask of {@link Capability} flags indicating supported reachability.
         *
         * <p>TYPE: INTEGER (Required)
         */
        public static final String CAPABILITY = "capability";

        /** Constants and builders for querying person records. */
        @FlaggedApi(Flags.FLAG_ENABLE_PEOPLE_CONTRACT)
        public static final class Query {
            /** This utility class cannot be instantiated */
            private Query() {}

            /** Selection type for matching persons by phone number. */
            public static final String SELECTION_TYPE_PHONE = "selection-type-phone";

            /** Selection type for matching persons by email address. */
            public static final String SELECTION_TYPE_EMAIL = "selection-type-email";

            /** Selection type for matching persons by their group lookup key. */
            public static final String SELECTION_TYPE_GROUP_LOOKUP_KEY =
                    "selection-type-group-lookup-key";

            /** Selection type for matching persons by their opaque lookup key. */
            public static final String SELECTION_TYPE_LOOKUP_KEY = "selection-type-lookup-key";

            /** Builder for creating person query requests. */
            @FlaggedApi(Flags.FLAG_ENABLE_PEOPLE_CONTRACT)
            public static final class Builder {
                private final ArrayList<Bundle> mRequests = new ArrayList<>();

                /** Creates a new Builder for person queries. */
                public Builder() {}

                /** Adds a request to query a specific package by phone numbers. */
                @NonNull
                @SuppressLint("MissingGetterMatchingBuilder")
                public Builder addPhoneQuery(
                        @NonNull String packageName, @NonNull List<String> phones) {
                    Bundle request = new Bundle();
                    request.putString(QUERY_ARG_PACKAGE, packageName);
                    request.putString(QUERY_ARG_SELECTION_TYPE, SELECTION_TYPE_PHONE);
                    request.putStringArray(
                            QUERY_ARG_SELECTION_VALUE, phones.toArray(new String[0]));
                    mRequests.add(request);
                    return this;
                }

                /** Adds a request to query a specific package by email addresses. */
                @NonNull
                @SuppressLint("MissingGetterMatchingBuilder")
                public Builder addEmailQuery(
                        @NonNull String packageName, @NonNull List<String> emailAddresses) {
                    Bundle request = new Bundle();
                    request.putString(QUERY_ARG_PACKAGE, packageName);
                    request.putString(QUERY_ARG_SELECTION_TYPE, SELECTION_TYPE_EMAIL);
                    request.putStringArray(
                            QUERY_ARG_SELECTION_VALUE, emailAddresses.toArray(new String[0]));
                    mRequests.add(request);
                    return this;
                }

                /** Adds a request to query a specific package by group lookup keys. */
                @NonNull
                @SuppressLint("MissingGetterMatchingBuilder")
                public Builder addGroupQuery(
                        @NonNull String packageName, @NonNull List<String> groupLookupKeys) {
                    Bundle request = new Bundle();
                    request.putString(QUERY_ARG_PACKAGE, packageName);
                    request.putString(QUERY_ARG_SELECTION_TYPE, SELECTION_TYPE_GROUP_LOOKUP_KEY);
                    request.putStringArray(
                            QUERY_ARG_SELECTION_VALUE, groupLookupKeys.toArray(new String[0]));
                    mRequests.add(request);
                    return this;
                }

                /** Adds a request to query a specific package by opaque lookup keys. */
                @NonNull
                @SuppressLint("MissingGetterMatchingBuilder")
                public Builder addLookupQuery(
                        @NonNull String packageName, @NonNull List<String> lookupKeys) {
                    Bundle request = new Bundle();
                    request.putString(QUERY_ARG_PACKAGE, packageName);
                    request.putString(QUERY_ARG_SELECTION_TYPE, SELECTION_TYPE_LOOKUP_KEY);
                    request.putStringArray(
                            QUERY_ARG_SELECTION_VALUE, lookupKeys.toArray(new String[0]));
                    mRequests.add(request);
                    return this;
                }

                /**
                 * Builds the final {@code Bundle} to be passed as the {@code queryArgs} parameter
                 * to {@link android.content.ContentResolver#query}.
                 */
                @NonNull
                public Bundle build() {
                    Bundle queryArgs = new Bundle();
                    queryArgs.putParcelableArrayList(QUERY_ARG_QUERIES, mRequests);
                    return queryArgs;
                }
            }
        }
    }

    /** Constants for querying group records (e.g., VoIP group chats). */
    @FlaggedApi(Flags.FLAG_ENABLE_PEOPLE_CONTRACT)
    public static final class Groups {
        /** This utility class cannot be instantiated */
        private Groups() {}

        /** MIME type for a single group record. */
        @NonNull
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/people_group";

        /** MIME type for a directory of groups. */
        @NonNull public static final String CONTENT_TYPE = "vnd.android.cursor.dir/people_groups";

        /** Base URI for group-related queries. */
        @NonNull
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "groups");

        /** URI for resolving group details by its lookup key. */
        @NonNull
        public static final Uri CONTENT_LOOKUP_URI = Uri.withAppendedPath(CONTENT_URI, "lookup");

        /**
         * The display name or title of the group.
         *
         * <p>TYPE: TEXT (Required)
         */
        public static final String DISPLAY_NAME = "display_name";

        /**
         * Opaque identifier for the group.
         *
         * <p>TYPE: TEXT (Required)
         */
        public static final String LOOKUP_KEY = "lookup_key";

        /** Constants and builders for querying group records. */
        @FlaggedApi(Flags.FLAG_ENABLE_PEOPLE_CONTRACT)
        public static final class Query {
            /** This utility class cannot be instantiated */
            private Query() {}

            /** Selection type for matching groups by their opaque lookup key. */
            public static final String SELECTION_TYPE_LOOKUP_KEY = "selection-type-lookup-key";

            /** Builder for creating group query requests. */
            @FlaggedApi(Flags.FLAG_ENABLE_PEOPLE_CONTRACT)
            public static final class Builder {
                private final ArrayList<Bundle> mRequests = new ArrayList<>();

                /** Creates a new Builder for group queries. */
                public Builder() {}

                /** Adds a request to query a specific package by opaque lookup keys. */
                @NonNull
                @SuppressLint("MissingGetterMatchingBuilder")
                public Builder addLookupQuery(
                        @NonNull String packageName, @NonNull List<String> lookupKeys) {
                    Bundle request = new Bundle();
                    request.putString(QUERY_ARG_PACKAGE, packageName);
                    request.putString(QUERY_ARG_SELECTION_TYPE, SELECTION_TYPE_LOOKUP_KEY);
                    request.putStringArray(
                            QUERY_ARG_SELECTION_VALUE, lookupKeys.toArray(new String[0]));
                    mRequests.add(request);
                    return this;
                }

                /**
                 * Builds the final {@code Bundle} to be passed as the {@code queryArgs} parameter
                 * to {@link android.content.ContentResolver#query}.
                 */
                @NonNull
                public Bundle build() {
                    Bundle queryArgs = new Bundle();
                    queryArgs.putParcelableArrayList(QUERY_ARG_QUERIES, mRequests);
                    return queryArgs;
                }
            }
        }
    }

    /** Reachability capabilities for a person identity. */
    @FlaggedApi(Flags.FLAG_ENABLE_PEOPLE_CONTRACT)
    public static final class Capability {
        /** This utility class cannot be instantiated */
        private Capability() {}

        /** Person supports audio communication. */
        public static final int VOICE_REACHABILITY = 1 << 0;

        /** Person supports video communication. */
        public static final int VIDEO_REACHABILITY = 1 << 1;

        /** Person supports text messaging. */
        public static final int TEXT_REACHABILITY = 1 << 2;
    }

    /** Standard intents for interacting with People identities. */
    @FlaggedApi(Flags.FLAG_ENABLE_PEOPLE_CONTRACT)
    public static final class Intents {
        /** This utility class cannot be instantiated */
        private Intents() {}

        /**
         * Intent action to launch a communication task for a person or group. Should be launched
         * via {@code startActivityForResult}.
         *
         * <p>The same Intent must have {@link #EXTRA_PEOPLE_INTERACTION_TYPE} to specify the
         * interaction type with the resolved person(s) and group(s).
         *
         * <p>The same Intent must have {@link #EXTRA_PEOPLE_QUERY_ARGS} to specify the resolved
         * person(s) and group(s).
         */
        public static final String ACTION_START_PEOPLE_INTERACTION =
                "android.provider.action.START_PEOPLE_INTERACTION";

        /**
         * Intent extra key for the interaction type (Message, Audio, or Video).
         *
         * <p>The value must be one of {@link #PEOPLE_INTERACTION_TYPE_MESSAGE}, {@link
         * #PEOPLE_INTERACTION_TYPE_VOICE}, or {@link #PEOPLE_INTERACTION_TYPE_VIDEO}.
         *
         * <p>The same Intent must have {@link #EXTRA_PEOPLE_QUERY_ARGS} to specify the resolved
         * person(s) and group(s).
         */
        public static final String EXTRA_PEOPLE_INTERACTION_TYPE =
                "android.provider.extra.PEOPLE_INTERACTION_TYPE";

        /**
         * Intent extra key for the query arguments used to resolve the target person or group.
         *
         * <p>The value must be a {@code Bundle} that can be passed as the {@code queryArgs}
         * parameter to {@link android.content.ContentResolver#query} for a {@link
         * Persons#CONTENT_URI} or {@link Groups#CONTENT_URI} query, which can be built using {@link
         * Persons.Query.Builder} or {@link Groups.Query.Builder}.
         *
         * <p>The same Intent must have {@link #EXTRA_PEOPLE_INTERACTION_TYPE} to specify the
         * interaction type with the resolved target.
         */
        public static final String EXTRA_PEOPLE_QUERY_ARGS =
                "android.provider.extra.PEOPLE_QUERY_ARGS";

        /**
         * Method for {@link android.content.ContentResolver#call} to securely request and mint the
         * intent sender required to start a 3P people interaction.
         */
        @Hide
        public static final String METHOD_CREATE_INTERACTION_INTENT = "create_interaction_intent";

        /**
         * Extra key returned in the {@link android.content.ContentResolver#call} response Bundle,
         * containing the securely minted {@link android.app.PendingIntent} for the interaction.
         */
        @Hide
        public static final String EXTRA_PEOPLE_INTERACTION_PENDING_INTENT =
                "android.provider.extra.PEOPLE_INTERACTION_PENDING_INTENT";

        /**
         * Intent extra value for a message interaction. Used with {@link
         * #EXTRA_PEOPLE_INTERACTION_TYPE} in an Intent extra.
         */
        @SuppressLint("IntentName")
        public static final String PEOPLE_INTERACTION_TYPE_MESSAGE =
                "com.android.people.PEOPLE_INTERACTION_TYPE_MESSAGE";

        /**
         * Intent extra value for a voice call interaction. Used with {@link
         * #EXTRA_PEOPLE_INTERACTION_TYPE} in an Intent extra.
         */
        @SuppressLint("IntentName")
        public static final String PEOPLE_INTERACTION_TYPE_VOICE =
                "com.android.people.PEOPLE_INTERACTION_TYPE_VOICE";

        /**
         * Intent extra value for a video call interaction. Used with {@link
         * #EXTRA_PEOPLE_INTERACTION_TYPE} in an Intent extra.
         */
        @SuppressLint("IntentName")
        public static final String PEOPLE_INTERACTION_TYPE_VIDEO =
                "com.android.people.PEOPLE_INTERACTION_TYPE_VIDEO";
    }
}
