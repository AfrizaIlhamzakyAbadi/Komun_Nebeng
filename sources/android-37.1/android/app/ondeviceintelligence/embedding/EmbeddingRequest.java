/*
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

package android.app.ondeviceintelligence.embedding;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SystemApi;
import android.app.ondeviceintelligence.flags.Flags;
import android.compat.annotation.UnsupportedAppUsage;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.PersistableBundle;

import android.app.ondeviceintelligence.Content;

import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a request to generate embeddings for a specific input.
 */
@SystemApi
@FlaggedApi(Flags.FLAG_ON_DEVICE_INTELLIGENCE_26Q2)
public final class EmbeddingRequest implements Parcelable, AutoCloseable {

    /**
     * Key for the request options bundle to specify the task type. The value must be one of the
     * TASK_TYPE_* constants from {@link TaskType}, for example {@link
     * TaskType#TASK_TYPE_RETRIEVAL_QUERY}.
     *     <p>Note: This option was added internally in Android 17 (API 37) but will become a formal
     *     System API in API 37.1 minor release.
     */
    @Hide
    public static final String OPTION_TASK_TYPE =
            "android.app.ondeviceintelligence.embedding.OPTION_TASK_TYPE";

    /**
     * Key for the request options bundle to specify the target output dimension. This value must be
     * less than or equal to the model's default output dimension returned by {@link
     * EmbeddingModel#getOutputDimension()}.
     *
     * <p>If the model does not support truncation, this value may be ignored.
     *     <p>Note: This method was added internally in Android 17 (API 37) but will become a formal
     *     System API in API 37.1 minor release.
     */
    @Hide
    public static final String OPTION_OUTPUT_DIMENSION =
            "android.app.ondeviceintelligence.embedding.OPTION_OUTPUT_DIMENSION";

    @Hide
    @IntDef(
            value = {
                TASK_TYPE_RETRIEVAL_QUERY,
                TASK_TYPE_RETRIEVAL_DOCUMENT,
                TASK_TYPE_QUESTION_ANSWERING,
                TASK_TYPE_FACT_VERIFICATION,
                TASK_TYPE_CLASSIFICATION,
                TASK_TYPE_CLUSTERING,
                TASK_TYPE_SEMANTIC_SIMILARITY,
                TASK_TYPE_CODE_RETRIEVAL
            })
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE_USE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
    public @interface TaskType {}

    /**
     * Key for the request options bundle to specify the request kind. The value must be one of the
     * REQUEST_KIND_* constants from {@link RequestKind}, for example {@link
     * RequestKind#REQUEST_KIND_INTERACTIVE}.
     *
     * <p>Note: This is a best-effort override and the remote service might ignore the option
     * provided.
     */
    @Hide
    public static final String OPTION_OVERRIDE_REQUEST_KIND =
            "android.app.ondeviceintelligence.embedding.OPTION_OVERRIDE_REQUEST_KIND";

    @Hide
    @IntDef(
            value = {
                REQUEST_KIND_UNSPECIFIED,
                REQUEST_KIND_INTERACTIVE,
                REQUEST_KIND_NON_INTERACTIVE,
                REQUEST_KIND_OPPORTUNISTIC
            })
    @Retention(RetentionPolicy.SOURCE)
    @Target({ElementType.TYPE_USE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
    public @interface RequestKind {}

    /**
     * Default request kind to use when request kind is not specified.
     */
    @Hide
    public static final int REQUEST_KIND_UNSPECIFIED = 0;

    /**
     * Stacked (LIFO) processing.
     *
     * <p>When processing a new request of kind REQUEST_KIND_INTERACTIVE, if remote service is
     * already busy with another request, the new request should be added at the FRONT of the
     * priority queue and scheduled immediately, and the currently running request should be
     * suspended or terminated: if the currently running request was of kind
     * REQUEST_KIND_OPPORTUNISTIC, it should be terminated, otherwise it should be suspended, to be
     * executed as soon as the new inference completes (unless another REQUEST_KIND_INTERACTIVE
     * request comes in before then).
     *
     * <p>(The remote service should use a LIFO queuing strategy for interactive requests since the
     * user is believed to be more likely to switch back to the UI screen corresponding to the most
     * recent request rather than earlier requests.)
     */
    @Hide
    public static final int REQUEST_KIND_INTERACTIVE = 1;

    /**
     * Queued (FIFO) processing.
     *
     * <p>When processing a new request of kind REQUEST_KIND_NON_INTERACTIVE, if remote service is
     * already busy with another request, the new request should be added at the BACK of the
     * priority queue, to be executed once there are no outstanding REQUEST_KIND_INTERACTIVE or
     * REQUEST_KIND_OPPORTUNISTIC requests and all prior REQUEST_KIND_NON_INTERACTIVE requests have
     * completed.
     */
    @Hide
    public static final int REQUEST_KIND_NON_INTERACTIVE = 2;

    /**
     * Immediate processing, if resources are available.
     *
     * <p>When processing a new request of kind REQUEST_KIND_OPPORTUNISTIC, if remote service is
     * already busy with another request, the new request should immediately fail. Also, if a new
     * request of kind REQUEST_KIND_INTERACTIVE comes in while the opportunistic request is
     * processing, the opportunistic request should be terminated and should immediately fail at
     * that point.
     */
    @Hide
    public static final int REQUEST_KIND_OPPORTUNISTIC = 3;

    /**
     * Used to generate embeddings that are optimized for document search or information retrieval.
     */
    @Hide
    public static final int TASK_TYPE_RETRIEVAL_QUERY = 0;

    /**
     * Used for indexing documents for storage and a subsequent retrieval.
     */
    @Hide
    public static final int TASK_TYPE_RETRIEVAL_DOCUMENT = 1;

    /**
     * Used for question answering style data where you want to match a question to an answer.
     */
    @Hide
    public static final int TASK_TYPE_QUESTION_ANSWERING = 2;

    /**
     * Used for fact verification.
     */
    @Hide
    public static final int TASK_TYPE_FACT_VERIFICATION = 3;

    /**
     * Used to generate embeddings that are optimized to classify texts according to preset labels.
     */
    @Hide
    public static final int TASK_TYPE_CLASSIFICATION = 4;

    /**
     * Used to generate embeddings that are optimized to cluster texts based on their similarities.
     */
    @Hide
    public static final int TASK_TYPE_CLUSTERING = 5;

    /**
     * Used to generate embeddings that are optimized to assess text similarity. This is not
     * intended for retrieval use cases.
     */
    @Hide
    public static final int TASK_TYPE_SEMANTIC_SIMILARITY = 6;

    /**
     * Used to retrieve a code block based on a natural language query, such as sort an array or
     * reverse a linked list. Embeddings of the code blocks are computed using retrieval_document.
     */
    @Hide
    public static final int TASK_TYPE_CODE_RETRIEVAL = 7;

    private final List<Content> mContent;
    private final @Nullable PersistableBundle mRequestOptions;

    /**
     * Constructs a new {@link EmbeddingRequest} with the given payload.
     *
     * @param content The input list of content to generate embeddings for.
     */
    public EmbeddingRequest(@NonNull List<Content> content) {
        this(content, null);
    }

    /**
     * Constructs a new {@link EmbeddingRequest} with the given payload and options.
     *
     * @param content The input list of content to generate embeddings for.
     * @param requestOptions The options to configure the model execution (e.g. taskType).
     *     <p>Note: This method was added internally in Android 17 (API 37) but will become a formal
     *     System API in API 37.1 minor release.
     */
    @Hide
    @UnsupportedAppUsage
    public EmbeddingRequest(
            @NonNull List<Content> content, @Nullable PersistableBundle requestOptions) {
        mContent = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(content)));
        mRequestOptions = requestOptions;
    }

    /** Returns the list of content for which embeddings are requested. */
    @NonNull
    public List<Content> getContent() {
        return mContent;
    }

    /**
     * Returns the options configuring the request.
     *
     * <p>Note: This method was added internally in Android 17 (API 37) but will become a formal
     * System API in API 37.1 minor release.
     */
    @Hide
    @UnsupportedAppUsage
    @Nullable
    public PersistableBundle getRequestOptions() {
        return mRequestOptions;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedList(mContent);
        dest.writeTypedObject(mRequestOptions, flags);
    }

    public static final @NonNull Creator<EmbeddingRequest> CREATOR =
            new Creator<EmbeddingRequest>() {
                @Override
                public EmbeddingRequest createFromParcel(Parcel in) {
                    return new EmbeddingRequest(in);
                }

                @Override
                public EmbeddingRequest[] newArray(int size) {
                    return new EmbeddingRequest[size];
                }
            };

    private EmbeddingRequest(Parcel in) {
        mContent = in.createTypedArrayList(Content.CREATOR);
        mRequestOptions = in.readTypedObject(PersistableBundle.CREATOR);
    }

    @Override
    public void close() throws IOException {
        for (Content content : mContent) {
            content.close();
        }
    }
}
