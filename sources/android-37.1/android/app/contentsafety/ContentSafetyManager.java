/*
 * Copyright (C) 2025 The Android Open Source Project
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

package android.app.contentsafety;

import static android.Manifest.permission.BYPASS_ROLE_QUALIFICATION;
import static android.Manifest.permission.MANAGE_ROLE_HOLDERS;
import static android.app.contentsafety.flags.Flags.FLAG_CONTENT_RESTRICTION_API;
import static android.app.contentsafety.flags.Flags.FLAG_CONTENT_SAFETY_API_V2;
import static android.app.contentsafety.flags.Flags.FLAG_ENABLE_CONTENTSAFETY;

import android.Manifest;
import android.annotation.CallbackExecutor;
import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresPermission;
import android.annotation.SdkConstant;
import android.annotation.SystemApi;
import android.annotation.SystemService;
import android.annotation.TestApi;
import android.annotation.UserHandleAware;
import android.annotation.UserIdInt;
import android.content.Context;
import android.content.Intent;
import android.content.LocusId;
import android.os.Binder;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.IBinder;
import android.os.ICancellationSignal;
import android.os.IpcDataCache;
import android.os.OutcomeReceiver;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.util.Log;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.infra.AndroidFuture;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * The ContentSafetyManager provides access to content safety features.
 *
 * <p>It allows granted apps to manage content safety service configured on the device. Typical
 * calling pattern will be to {@link #checkContentRequest} and {@link #isFeatureEnabledRequest}
 * checkFile against sensitive content warnings.
 */
@SystemService(Context.CONTENT_SAFETY_SERVICE)
@FlaggedApi(FLAG_CONTENT_RESTRICTION_API)
public final class ContentSafetyManager {
    private static final String TAG = "ContentSafety";
    private final IContentSafetyManager mService;
    private final IContentRestrictionService mRestrictionService;
    private final Context mContext;

    // IpcDataCache Configuration
    private static final int MAX_CACHE_SIZE = 1; // We only cache one item: the list of types.
    private static final String CACHE_MODULE = "contentsafety";
    private static final String CACHE_API_NAME = "get_supported_mime_types";

    private final IpcDataCache<Void, List<String>> mMimeTypesCache;

    // QueryHandler: Called on cache miss to fetch data from the ContentSafetyService.
    private final IpcDataCache.QueryHandler<Void, List<String>> mMimeTypesQueryHandler =
            new IpcDataCache.QueryHandler<Void, List<String>>() {
                @Override
                public List<String> apply(Void query) {
                    try {
                        // Cache miss
                        return mService.getSupportedMimeTypes();
                    } catch (RemoteException e) {
                        Log.e(TAG, "Failed to fetch MIME types from service", e);
                        throw e.rethrowFromSystemServer();
                    }
                }
            };

    /**
     * Activity Action: Launch an activity showing information about restricted content.
     *
     * <p>Apps holding the {@link android.app.role.RoleManager#ROLE_CONTENT_SAFETY} must declare an
     * activity that handles this action to show specific content restriction details.
     */
    @SdkConstant(SdkConstant.SdkConstantType.ACTIVITY_INTENT_ACTION)
    public static final String ACTION_SHOW_RESTRICTED_CONTENT_DETAILS =
            "android.app.contentsafety.action.SHOW_RESTRICTED_CONTENT_DETAILS";

    /**
     * Extra for {@link #ACTION_SHOW_RESTRICTED_CONTENT_DETAILS} containing the locus ID of the
     * restricted content.
     */
    public static final String EXTRA_CONTENT_LOCUS_ID =
            "android.app.contentsafety.extra.CONTENT_LOCUS_ID";

    @Hide
    public ContentSafetyManager(
            Context context,
            IContentSafetyManager service,
            IContentRestrictionService restrictionService) {
        mContext = context;
        mService = service;
        mRestrictionService = restrictionService;
        mMimeTypesCache = new IpcDataCache<>(
                MAX_CACHE_SIZE,
                IpcDataCache.MODULE_SYSTEM,
                CACHE_MODULE,
                CACHE_API_NAME,
                mMimeTypesQueryHandler);
    }

    /**
     * Get package name configured for providing the remote implementation for the content safety
     * service.
     * @return empty string if the remote service is not configured or the package name.
     */
    @NonNull
    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    @RequiresPermission(Manifest.permission.CHECK_CONTENT_SAFETY)
    public String getRemoteServicePackageName() {
        String result;
        try {
            result = mService.getRemoteServicePackageName();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
        return result;
    }

    /**
     * Get package name configured for providing the remote implementation for the sandboxed
     * service.
     * @return empty string if the remote service is not configured or the package name.
     */
    @NonNull
    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    @RequiresPermission(Manifest.permission.CHECK_CONTENT_SAFETY)
    public String getRemoteSandboxedServicePackageName() {
        String result;
        try {
            result = mService.getRemoteSandboxedServicePackageName();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
        return result;
    }

    /**
     * Get package name configured for providing the remote implementation for the settings service.
     * @return empty string if the remote service is not configured or the package name.
     */
    @NonNull
    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    @RequiresPermission(Manifest.permission.CHECK_CONTENT_SAFETY)
    public String getRemoteSettingsServicePackageName() {
        String result;
        try {
            result = mService.getRemoteSettingsServicePackageName();
        } catch (RemoteException ex) {
            throw ex.rethrowFromSystemServer();
        }
        return result;
    }

    /**
     * Sensitive Content Warnings for images.
     */
    @Hide
    public static final int SENSITIVE_IMAGE = 0;

    /**
     * Sensitive Content Warnings for videos.
     */
    @Hide
    public static final int SENSITIVE_VIDEO = 1;

    /**
     * List of available Safety Features.
     */
    @Hide
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(value = {SENSITIVE_IMAGE, SENSITIVE_VIDEO})
    public @interface FeatureType { }

    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    public static final int CONTENT_SAFETY_UNKNOWN = 0;

    /**
     * Indicates that the content check was successful and no sensitive content was detected.
     */
    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    public static final int CONTENT_SAFETY_SUCCESS_NONE = 1;

    /**
     * Indicates that the content check was successful and sensitive content was detected.
     */
    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    public static final int CONTENT_SAFETY_SUCCESS_SENSITIVE = 2;

    /**
     * Indicates that SANDBOXED service failed due to unspecified internal error.
     */
    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    public static final int CONTENT_SAFETY_SANDBOXED_SERVICE_ERROR_UNKNOWN = 3;

    /**
     * Indicates that sandboxed service is not available and fail to connect.
     */
    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    public static final int CONTENT_SAFETY_SANDBOXED_SERVICE_ERROR_NOT_AVAILABLE = 4;

    /**
     * Indicates that getFeature remote call is returning failure error code due to internal error.
     */
    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    public static final int CONTENT_SAFETY_GET_FEATURE_ERROR = 5;

    /**
     * Indicates that loadFeature remote call is returning failure error code due to internal error.
     */
    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    public static final int CONTENT_SAFETY_LOAD_FEATURE_ERROR = 6;

    /**
     * Indicates that downloaded feature from remote service is not opened with read-only mode.
     */
    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    public static final int CONTENT_SAFETY_FEATURE_NOT_READ_ONLY_ERROR = 7;

    /**
     * Indicates that the input file descriptors provided by the client is not opened with read-only
     * mode.
     */
    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    public static final int CONTENT_SAFETY_PAYLOAD_NOT_READ_ONLY_ERROR = 8;

    /**
     * Indicates that the remote content safety service received a cancelled signal.
     */
    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    public static final int CONTENT_SAFETY_CHECK_CONTENT_CANCELLED = 9;

    /**
     * Indicates that checkContent remote call failed due to internal error and return an error
     * code.
     */
    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    public static final int CONTENT_SAFETY_CHECK_CONTENT_ERROR = 10;

    /**
     * Indicates that trying to invoke the remote call checkContent failed.
     */
    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    public static final int CONTENT_SAFETY_CHECK_CONTENT_INVOKE_ERROR = 11;

    /**
     * Indicates that trying to invoke the remote call getFeature failed.
     */
    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    public static final int CONTENT_SAFETY_GET_FEATURE_INVOKE_ERROR = 12;

    /**
     * Indicates that to invoke the remote call loadFeature failed.
     */
    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    public static final int CONTENT_SAFETY_LOAD_FEATURE_INVOKE_ERROR = 13;

    /**
     * Indicates that the remote call getFeature failed due to internal error.
     */
    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    public static final int CONTENT_SAFETY_GET_FEATURE_INTERNAL_ERROR = 14;

    /**
     * Indicates that the remote call loadFeature failed due to internal error.
     */
    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    public static final int CONTENT_SAFETY_LOAD_FEATURE_INTERNAL_ERROR = 15;

    @Hide
    @Target({ElementType.TYPE_USE, ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    @IntDef(
            value = {
                    CONTENT_SAFETY_UNKNOWN,
                    CONTENT_SAFETY_SUCCESS_NONE,
                    CONTENT_SAFETY_SUCCESS_SENSITIVE,
                    CONTENT_SAFETY_SANDBOXED_SERVICE_ERROR_UNKNOWN,
                    CONTENT_SAFETY_SANDBOXED_SERVICE_ERROR_NOT_AVAILABLE,
                    CONTENT_SAFETY_GET_FEATURE_ERROR,
                    CONTENT_SAFETY_LOAD_FEATURE_ERROR,
                    CONTENT_SAFETY_FEATURE_NOT_READ_ONLY_ERROR,
                    CONTENT_SAFETY_PAYLOAD_NOT_READ_ONLY_ERROR,
                    CONTENT_SAFETY_CHECK_CONTENT_CANCELLED,
                    CONTENT_SAFETY_CHECK_CONTENT_ERROR,
                    CONTENT_SAFETY_CHECK_CONTENT_INVOKE_ERROR,
                    CONTENT_SAFETY_GET_FEATURE_INVOKE_ERROR,
                    CONTENT_SAFETY_LOAD_FEATURE_INVOKE_ERROR,
                    CONTENT_SAFETY_GET_FEATURE_INTERNAL_ERROR,
                    CONTENT_SAFETY_LOAD_FEATURE_INTERNAL_ERROR
            })
    public @interface CheckContentStatus{ }

    /**
     * @deprecated Use {@link #requestCheckContent(List, Executor, CheckContentCallback)} instead.
     *
     * Checks an input asynchronously based on the provided params, and populate a response in
     * a Map. Each featureType maps to a list of
     * {@link CheckContentStatus} status codes. The order of the status codes is the same as
     * the order of the input file descriptors.
     *
     * @param featureType The safety feature type to run against the file.
     * @param input A map of list of ParcelFileDescriptors. keys should be a value of Feature Type.
     *             this input MUST have been opened for read-only access and if not, the system
     *              server will throw a {@link android.os.BadParcelableException}.
     * @param cancellationSignal signal to invoke cancellation or
     * @param callbackExecutor executor to run the callback on.
     * @param checkContentConsumer to consume the returning checkContent results. Keys should be
     *                            a value of Feature Type. Values should be a list of integer
     *                            values correspond to the execution status defined by final
     *                            integers {@code CONTENT_SAFETY_CHECK_CONTENT_*},
     *                            {@code CONTENT_SAFETY_GET_FEATURE_*}, and
     *                             {@code CONTENT_SAFETY_LOAD_FEATURE_*} which correspond to the
     *                            result of each given parcelFileDescriptor.
     *                             The order should be maintained the same as it is received.
     * @throws IllegalArgumentException If the provided feature is invalid or failed to
     *     check content.
     */
    @Deprecated
    @SystemApi
    @FlaggedApi(FLAG_CONTENT_SAFETY_API_V2)
    @RequiresPermission(Manifest.permission.CHECK_CONTENT_SAFETY)
    public void requestCheckContent(
            @FeatureType int featureType,
            @NonNull @CheckContentParams Map<Integer, List<ParcelFileDescriptor>> input,
            @Nullable CancellationSignal cancellationSignal,
            @NonNull @CallbackExecutor Executor callbackExecutor,
            @NonNull
            Consumer<Map<Integer, List<@CheckContentStatus Integer>>>
                    checkContentConsumer) {
        try {
            ICheckContentCallback callback = new ICheckContentCallback.Stub() {
                @Override
                public void onResult(
                        Bundle resultBundle) {
                            Binder.withCleanCallingIdentity(() ->
                                    callbackExecutor.execute(() -> {
                                        Map<Integer, List<Integer>> resultMap =
                                                unpackMapFromBundle(resultBundle);
                                        checkContentConsumer.accept(resultMap);
                                    }));
                }

                @Override
                public void onClassification(List<ContentClassificationResult> results) {
                    // No-op as this API is deprecated.
                }

                @Override
                public void onClassificationComplete() {
                    // No-op as this API is deprecated.
                }
            };

            mService.requestCheckContent(
                    featureType,
                    packMapIntoBundle(input),
                    configureRemoteCancellationFuture(cancellationSignal, callbackExecutor),
                    callback);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Classifies the supplied content.
     *
     * @param content the list of content to be classified
     * @param executor the {@code Executor} to call the callback on
     * @param callback the callback to deliver classification results
     *
     */
    @SystemApi
    @FlaggedApi(FLAG_CONTENT_SAFETY_API_V2)
    @RequiresPermission(Manifest.permission.CHECK_CONTENT_SAFETY)
    public void requestCheckContent(
            @NonNull List<ClassifiableContent> content,
            @NonNull @CallbackExecutor Executor executor,
            @NonNull CheckContentCallback callback) {
        try {
            mService.requestClassification(
                    content,
                    new CheckContentCallbackDelegate(executor, callback));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private static class CheckContentCallbackDelegate extends ICheckContentCallback.Stub {
        private final Executor mExecutor;
        private final CheckContentCallback mCallback;

        CheckContentCallbackDelegate(@NonNull Executor executor,
                @NonNull CheckContentCallback callback) {
            mExecutor = executor;
            mCallback = callback;
        }

        @Override
        public void onClassification(List<ContentClassificationResult> results) {
            Binder.withCleanCallingIdentity(() ->
                    mExecutor.execute(() -> mCallback.onClassification(results)));
        }

        @Override
        public void onClassificationComplete() {
            Binder.withCleanCallingIdentity(() ->
                    mExecutor.execute(() -> mCallback.onClassificationComplete()));
        }

        @Override
        public void onResult(Bundle result) {
            // No-op as onResult in ICheckContentCallback is deprecated.
        }
    }

    private Map<Integer, List<Integer>> unpackMapFromBundle(Bundle input) {
        Map<Integer, List<Integer>> map = new HashMap<>();
        for (String key : input.keySet()) {
            try {
                ArrayList<Integer> statusCodes = input.getIntegerArrayList(key);
                if (statusCodes != null) {
                    map.putIfAbsent(Integer.parseInt(key),
                            new ArrayList<>(statusCodes));
                } else {
                    Log.w(TAG,
                            "Bundle retrieved from remote service contains invalid Integer "
                                    + "ArrayList");
                }
            } catch (NumberFormatException e) {
                // ignore invalid key
                Log.w(TAG, "Bundle retrieved from remote service contains invalid keys");
            }
        }
        return map;
    }

    private Bundle packMapIntoBundle(Map<Integer, List<ParcelFileDescriptor>> input) {
        Bundle bundle = new Bundle();
        for (Integer key: input.keySet()) {
            bundle.putParcelableArrayList(Integer.toString(key),
                    new ArrayList<>(input.get(key)));
        }
        return bundle;
    }

    @Nullable
    private static AndroidFuture<IBinder> configureRemoteCancellationFuture(
            @Nullable CancellationSignal cancellationSignal, @NonNull Executor callbackExecutor) {
        if (cancellationSignal == null) {
            return null;
        }
        AndroidFuture<IBinder> cancellationFuture = new AndroidFuture<>();
        return cancellationFuture.whenCompleteAsync(
                (cancellationTransport, error) -> {
                    if (error != null || cancellationTransport == null) {
                        Log.e(TAG, "Unable to receive the remote cancellation signal.", error);
                    } else {
                        cancellationSignal.setRemote(
                                ICancellationSignal.Stub.asInterface(cancellationTransport));
                    }
                },
                callbackExecutor);
    }

    /**
     * Checks if the given feature setting is enabled for the current user.
     *
     * @param featureType The safety {@link FeatureType} to check.
     * @param cancellationSignal signal to invoke cancellation or
     * @param callbackExecutor executor to run the callback on.
     * @param isFeatureEnabledOutcomeReceiver to populate either feature is enabled or failure
     *     status code .
     */
    @SystemApi
    @FlaggedApi(FLAG_ENABLE_CONTENTSAFETY)
    @RequiresPermission(Manifest.permission.CHECK_CONTENT_SAFETY)
    public void requestIsFeatureEnabled(
            @FeatureType int featureType,
            @Nullable CancellationSignal cancellationSignal,
            @NonNull @CallbackExecutor Executor callbackExecutor,
            @NonNull OutcomeReceiver<Boolean, FeatureException>
                    isFeatureEnabledOutcomeReceiver) {
        try {
            IIsFeatureEnabledCallback callback = new IIsFeatureEnabledCallback.Stub() {
                @Override
                public void onSuccess(boolean isFeatureEnabledResult) {
                    Binder.withCleanCallingIdentity(() -> callbackExecutor.execute(
                            () -> isFeatureEnabledOutcomeReceiver
                                    .onResult(isFeatureEnabledResult)));
                }

                @Override
                public void onFailure(int errorCode) {
                    Binder.withCleanCallingIdentity(() -> callbackExecutor.execute(
                            () -> isFeatureEnabledOutcomeReceiver
                                    .onError(new FeatureException(errorCode))));
                }
            };

            mService.requestIsFeatureEnabled(
                    featureType,
                    configureRemoteCancellationFuture(cancellationSignal, callbackExecutor),
                    callback);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * Returns a list of supported MIME types.
     *
     * @return the list of supported MIME types.
     */
    @SystemApi
    @FlaggedApi(FLAG_CONTENT_SAFETY_API_V2)
    @RequiresPermission(Manifest.permission.CHECK_CONTENT_SAFETY)
    @NonNull
    public List<String> getSupportedMimeTypes() {
        List<String> result = mMimeTypesCache.query(null);
        return result != null ? result : Collections.emptyList();
    }

    /**
     * Clears the in-process cache for supported MIME types.
     */
    @Hide
    @VisibleForTesting
    public static void clearCache() {
        IpcDataCache.invalidateCache(IpcDataCache.MODULE_SYSTEM, CACHE_MODULE);
        Log.d(TAG, "MIME types cache invalidated");
    }

    /**
     * Called to cancel content classification for the provided {@link LocusId}s.
     * @param locusIds list of content IDs for which classification should be cancelled
     */
    @FlaggedApi(FLAG_CONTENT_SAFETY_API_V2)
    @RequiresPermission(Manifest.permission.CHECK_CONTENT_SAFETY)
    public void cancelClassification(@NonNull List<LocusId> locusIds) {
        try {
            mService.cancelClassification(locusIds);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    /**
     * ParcelFileDescriptors annotated with this type will be validated that they are
     * in-effect read-only when passed via Binder IPC for example
     *  {@link android.os.ParcelFileDescriptor#MODE_READ_ONLY}
     *
     * The system-server might throw a {@link android.os.BadParcelableException}
     * if the validation fails.
     */
    @Hide
    @Target({ElementType.PARAMETER, ElementType.FIELD})
    public @interface CheckContentParams {}

    /**
     * Requests classification of the specified content.
     *
     * <p>When content safety is enabled, this method determines if the specified content is
     * appropriate based on the user's settings.
     *
     * <p>If the content is classified, the result provided to the callback will have a
     * classification type of {@link ContentClassificationResult#TYPE_ALLOWED}, {@link
     * ContentClassificationResult#TYPE_WARNING}, or {@link
     * ContentClassificationResult#TYPE_BLOCKED}. If the content could not be classified or if
     * content safety is disabled, the classification type will be {@link
     * ContentClassificationResult#TYPE_UNCLASSIFIED}.
     *
     * @param content the content to be classified
     * @param executor the executor on which to run the callback
     * @param callback the callback to receive the classification result
     */
    @FlaggedApi(FLAG_CONTENT_RESTRICTION_API)
    public void requestContentClassification(
            @NonNull ClassifiableContent content,
            @NonNull @CallbackExecutor Executor executor,
            @NonNull OutcomeReceiver<ContentClassificationResult, Exception> callback) {

        if (mRestrictionService == null) {
            executor.execute(
                    () ->
                            callback.onError(
                                    new IllegalStateException(
                                            "ContentRestrictionService not available")));
            return;
        }

        if (!isContentSafetyEnabled()) {
            executor.execute(
                    () ->
                            callback.onResult(
                                    new ContentClassificationResult(
                                            content.getId(),
                                            ContentClassificationResult.TYPE_UNCLASSIFIED)));
            return;
        }

        try {
            mRestrictionService.requestClassification(
                    mContext.getUserId(),
                    content,
                    new ContentSafetyCallbackDelegate(executor, callback));
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }

    private static class ContentSafetyCallbackDelegate extends IContentSafetyCallback.Stub {
        private final Executor mExecutor;
        private final OutcomeReceiver<ContentClassificationResult, Exception> mCallback;

        ContentSafetyCallbackDelegate(@NonNull Executor executor,
                @NonNull OutcomeReceiver<ContentClassificationResult, Exception> callback) {
            mExecutor = executor;
            mCallback = callback;
        }

        @Override
        public void onResult(ContentClassificationResult result) {
            Binder.withCleanCallingIdentity(() ->
                    mExecutor.execute(() -> mCallback.onResult(result)));
        }

        @Override
        public void onError(int error, String message) {
            Binder.withCleanCallingIdentity(() ->
                    mExecutor.execute(
                            () -> mCallback.onError(new IllegalStateException(message))));
        }
    }

    /**
     * Returns whether content safety is currently enabled for the user.
     *
     * @return {@code true} if content safety is enabled for the user, {@code false} otherwise
     */
    @FlaggedApi(FLAG_CONTENT_RESTRICTION_API)
    public boolean isContentSafetyEnabled() {
        if (mRestrictionService != null) {
            try {
                return mRestrictionService.isContentRestrictionEnabledForUser(mContext.getUserId());
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return false;
    }

    /**
     * Creates an {@link Intent} that can be used with {@link Context#startActivity(Intent)} to
     * display a dialog about the restricted content.
     *
     * @param locusId the {@link LocusId} of the content to be restricted
     * @return the intent to display the restricted content dialog
     */
    @FlaggedApi(FLAG_CONTENT_RESTRICTION_API)
    @Nullable
    public Intent createContentRestrictedIntent(@NonNull LocusId locusId) {
        if (mRestrictionService != null) {
            try {
                return mRestrictionService.createContentRestrictedIntent(locusId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return null;
    }

    /**
     * Returns the package name of the app that classified the content associated with the given
     * LocusId.
     */
    @Hide
    @Nullable
    @FlaggedApi(FLAG_CONTENT_RESTRICTION_API)
    public String getClassifyingPackage(
            @UserIdInt int userId, @NonNull String callerPackage, @NonNull LocusId locusId) {
        if (mRestrictionService != null) {
            try {
                return mRestrictionService.getClassifyingPackage(userId, callerPackage, locusId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return null;
    }

    /**
     * Returns whether content safety sandbox bypassing is allowed.
     */
    @TestApi
    @FlaggedApi(FLAG_CONTENT_RESTRICTION_API)
    @UserHandleAware(requiresPermissionIfNotCaller = Manifest.permission.INTERACT_ACROSS_USERS)
    public boolean shouldAllowBypassingContentSafetySandboxingForUser(@UserIdInt int userId) {
        if (mRestrictionService != null) {
            try {
                return mRestrictionService.shouldAllowBypassingContentSafetySandboxingForUser(
                        userId);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return false;
    }

    /**
     * Sets whether content safety sandbox bypassing is allowed.
     */
    @TestApi
    @FlaggedApi(FLAG_CONTENT_RESTRICTION_API)
    @UserHandleAware(requiresPermissionIfNotCaller = Manifest.permission.INTERACT_ACROSS_USERS)
    public void setShouldAllowBypassingContentSafetySandboxingForUser(
            @UserIdInt int userId, boolean enabled) {
        if (mRestrictionService != null) {
            try {
                mRestrictionService.setShouldAllowBypassingContentSafetySandboxingForUser(
                        userId, enabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }

    /**
     * Returns whether content safety role qualification bypassing is allowed.
     */
    @SystemApi
    @FlaggedApi(FLAG_CONTENT_RESTRICTION_API)
    @RequiresPermission(MANAGE_ROLE_HOLDERS)
    public boolean shouldAllowBypassingContentSafetyRoleQualification() {
        if (mRestrictionService != null) {
            try {
                return mRestrictionService.shouldAllowBypassingContentSafetyRoleQualification();
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
        return false;
    }

    /**
     * Sets whether content safety role qualification bypassing is allowed.
     */
    @TestApi
    @FlaggedApi(FLAG_CONTENT_RESTRICTION_API)
    @RequiresPermission(BYPASS_ROLE_QUALIFICATION)
    public void setShouldAllowBypassingContentSafetyRoleQualification(boolean enabled) {
        if (mRestrictionService != null) {
            try {
                mRestrictionService.setShouldAllowBypassingContentSafetyRoleQualification(enabled);
            } catch (RemoteException e) {
                throw e.rethrowFromSystemServer();
            }
        }
    }
}
