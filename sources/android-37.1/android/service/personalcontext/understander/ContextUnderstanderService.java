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

package android.service.personalcontext.understander;

import android.Manifest;
import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.RequiresPermission;
import android.annotation.SystemApi;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerExecutor;
import android.os.IBinder;
import android.os.Looper;
import android.os.ParcelUuid;
import android.service.personalcontext.Flags;
import android.service.personalcontext.IOpCallback;
import android.service.personalcontext.hint.ContextHint;
import android.service.personalcontext.hint.ContextHintWrapper;
import android.service.personalcontext.hint.HintFilter;
import android.service.personalcontext.hint.PublishedContextHint;
import android.service.personalcontext.insight.ContextInsight;
import android.service.personalcontext.insight.InsightCollection;
import android.service.personalcontext.insight.PublishedContextInsight;
import android.service.personalcontext.insight.destination.CatchAllDestinationSpec;
import android.service.personalcontext.insight.destination.ContextDestination;
import android.service.personalcontext.insight.destination.DestinationSpec;
import android.service.personalcontext.insight.destination.LegacyCatchAllDestinationSpec;
import android.service.personalcontext.insight.destination.LegacyRenderTokenDestinationSpec;
import android.service.personalcontext.insight.interaction.InsightEvent;
import android.service.personalcontext.refiner.IGetFilterCallback;
import android.service.personalcontext.util.BinderRequestProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executor;

/**
 * This is the base class for understander services to implement, handling service details.
 *
 * <p>An understander takes hints provided to the Personal Context system and interprets them. This
 * usually means taking one or more hints, running them through models, and generating zero or more
 * {@link ContextInsight}s based on the hints.
 *
 * <p>The Personal Context service will manage the lifetime of this service, and this service may be
 * stopped if not utilized for some time. Services should start as rapidly as possible to minimize
 * latency in the Personal Context workflow.
 *
 * <p>You must declare the service in the AndroidManifest of the app hosting the service with the
 * {@link Manifest.permission#BIND_CONTEXT_COMPONENT_SERVICE} permission, and include an intent
 * filter with the necessary action indicating that it is a {@link ContextUnderstanderService}
 * (android.service.personalcontext.UnderstanderService). The application must have the {@link
 * Manifest.permission#PERSONAL_CONTEXT_RECEIVE_HINTS} and {@link
 * Manifest.permission#PERSONAL_CONTEXT_PUBLISH_INSIGHTS} permissions.
 *
 * <p>For example:
 *
 * <pre>
 *     &lt;uses-permission
 *         android:name="android.permission.PERSONAL_CONTEXT_RECEIVE_HINTS"/&gt;
 *     &lt;uses-permission
 *         android:name="android.permission.PERSONAL_CONTEXT_PUBLISH_INSIGHTS"/&gt;
 *
 *     &lt;service android:name=".ExampleContextUnderstanderService"
 *             android:exported="true"
 *             android:permission="android.permission.BIND_CONTEXT_COMPONENT_SERVICE"&gt;
 *         &lt;intent-filter&gt;
 *             &lt;action
 *             android:name="android.service.personalcontext.UnderstanderService"
 *             /&gt;
 *     &lt;/service&gt;
 * </pre>
 */
@SystemApi
@FlaggedApi(Flags.FLAG_ENABLE_PERSONAL_CONTEXT_SERVICE)
public abstract class ContextUnderstanderService extends Service {
    // TODO(b/516534291): Make this public.

    private static final Set<Class<? extends DestinationSpec>> sLegacyDestinationSpecs = Set.of(
            LegacyRenderTokenDestinationSpec.class,
            LegacyCatchAllDestinationSpec.class,
            CatchAllDestinationSpec.class);

    private UUID mComponentId = null;
    private Executor mBinderExecutor = null;

    private void configure(UUID componentId) {
        if (mComponentId == null) {
            mComponentId = componentId;
            onConnected();
        }
    }

    /**
     * Sets the executor to be used when methods are invoked on this service. By default, an
     * Executor running on the main looper is used. This method should be called within
     * {@link #onCreate()}.
     *
     * @param executor The {@link Executor} to run calls on.
     */
    public final void setExecutor(@androidx.annotation.NonNull Executor executor) {
        mBinderExecutor = executor;
    }

    @Override
    @Nullable
    public final IBinder onBind(@Nullable Intent intent) {
        final Executor executor = mBinderExecutor != null
                ? mBinderExecutor
                : new HandlerExecutor(new Handler(Looper.getMainLooper()));
        return new Binder(this, executor);
    }

    /**
     * Called when the understander has been configured and is ready to receive insights.
     * Any actions related to this method should complete before returning.
     */
    public void onConnected() {
        // Default implementation does nothing.
    }

    /**
     * The understander should return a {@link HintFilter} that will be used to filter the
     * hints that this understander's {@link #onUnderstand} method will be called with.
     *
     * The result of this method will be cached and re-used between service bindings. If the filter
     * returned by this method changes, the changes will be ignored.
     *
     * @return a filter that restricts the {@link ContextHint}s this understander will receive
     */
    @NonNull
    public abstract HintFilter onInitializeFilter();

    /**
     * Called when a new hint is available.
     *
     * <p>As each hint is provided to the Personal Context system it will be forwarded on to
     * understanding components. The understander can take these hints, cache them between calls,
     * and use one or more hints together to generate {@link ContextInsight}s.
     *
     * <p>If the understander does not have any results, it should return {@link
     * android.service.personalcontext.insight.NoResultInsight} to allow CUJs to stop waiting for
     * results.
     *
     * @param hints new hints that this understander has not seen before
     * @return a list of {@link ContextInsight} that this {@link ContextUnderstanderService}
     *     generated by processing the incoming hints.
     */
    // TODO(b/516534291): Give this a default implementation, remove the permission, and deprecate.
    @SystemApi
    @NonNull
    @RequiresPermission(Manifest.permission.PERSONAL_CONTEXT_PUBLISH_INSIGHTS)
    public abstract List<ContextInsight> onUnderstand(@NonNull List<PublishedContextHint> hints);

    /**
     * Called when a new hint is available.
     *
     * <p>As each hint is provided to the Personal Context system it will be forwarded on to
     * understanding components. The understander can take these hints, cache them between calls,
     * and use one or more hints together to generate {@link ContextInsight}s.
     *
     * <p>If the understander does not have any results, it should return {@link
     * android.service.personalcontext.insight.NoResultInsight} to allow CUJs to stop waiting for
     * results.
     *
     * @param hints new hints that this understander has not seen before
     * @param destinations currently valid destinations that insights can be sent to
     * @return a map of {@link ContextDestination} to {@link ContextInsight} that this
     *         {@link ContextUnderstanderService} generated by processing the incoming hints.
     */
    @Hide
    @NonNull
    public Map<ContextDestination, ContextInsight> onUnderstand(
            @NonNull Set<ContextHint> hints,
            @NonNull Set<ContextDestination> destinations) {
        // TODO(b/516534291): Make this public
        // TODO(b/517297829): Remove the code forwarding to the old implementation.

        return callLegacyUnderstand(hints, destinations);
    }

    private Map<ContextDestination, ContextInsight> callLegacyUnderstand(
            @NonNull Set<ContextHint> hints,
            @NonNull Set<ContextDestination> destinations) {
        // Convert ContextHints to PublishedContextHints for legacy implementations.
        final List<PublishedContextHint> publishedHintShims = new ArrayList<>();
        for (ContextHint hint : hints) {
            publishedHintShims.add(new PublishedContextHint(hint));
        }

        // Get the insights from the legacy implementation.
        final List<ContextInsight> insights = onUnderstand(publishedHintShims);

        // No insights? No insight.
        if (insights == null || insights.isEmpty()) return Collections.emptyMap();

        final ContextInsight resultInsight;
        if (insights.size() == 1) {
            // If we only have one insight then use that one.
            resultInsight = insights.getFirst();

        } else {
            // If we have more than one insight, wrap it in an InsightCollection.
            InsightCollection.Builder insightBuilder = new InsightCollection.Builder();
            for (ContextInsight insight : insights) {
                insightBuilder.addInsight(insight);
            }
            resultInsight = insightBuilder.build();
        }

        // Use resultInsight as the insight for every RenderToken destination.
        Map<ContextDestination, ContextInsight> result = new HashMap<>();
        for (ContextDestination destination : destinations) {
            if (sLegacyDestinationSpecs.contains(destination.getDestinationSpec().getClass())) {
                result.put(destination, resultInsight);
            }
        }

        return result;
    }

    /**
     * Override this method to receive logging events for actions taken on the insight.
     *
     * <p>Invoked when an event has been reported on a {@link ContextInsight} originally
     * published by this {@link ContextUnderstanderService}.
     *
     * @param packageName the package of the application reporting the event.
     * @param event       The reported {@link InsightEvent}.
     */
    @SystemApi
    public void onHandleEvent(@NonNull String packageName, @NonNull InsightEvent event) {
        // Do nothing by default.
    }

    /**
     * Override this method to receive user feedback on an insight.
     *
     * @param insight  {@link ContextInsight} that the user feedback is related to
     * @param feedback information about the requested feedback and user responses
     */
    @SystemApi
    public void onHandleUserFeedback(@NonNull PublishedContextInsight insight,
            @NonNull Bundle feedback) {
        // TODO(b/516534291): Deprecate this.
        // Do nothing by default.
    }

    private static final class Binder extends IUnderstander.Stub {
        private final BinderRequestProcessor<ContextUnderstanderService> mRequestProcessor;

        private Binder(ContextUnderstanderService service, Executor executor) {
            mRequestProcessor = new BinderRequestProcessor.Builder<>(service, executor)
                    .setInitializer(ContextUnderstanderService::configure)
                    .build();
        }

        @Override
        public void understand(
                ParcelUuid componentId,
                List<ContextHintWrapper> inputHints,
                List<ContextDestination> destinations,
                IUnderstandCallback callback,
                IOpCallback opCallback) {
            mRequestProcessor.execute(
                    new BinderRequestProcessor.ExecutionParams.Builder<ContextUnderstanderService>(
                            opCallback, serviceInstance -> {
                        final Map<ContextDestination, ContextInsight> insights =
                                serviceInstance.onUnderstand(
                                        ContextHintWrapper.unwrapInto(inputHints, new HashSet<>()),
                                        new HashSet<>(destinations));

                        callback.onUnderstood(new UnderstandCallbackResponse(insights));
                    }).setComponentId(componentId).build());
        }

        @Override
        public void getFilter(
                ParcelUuid componentId, IGetFilterCallback callback, IOpCallback opCallback) {
            mRequestProcessor.execute(
                    new BinderRequestProcessor.ExecutionParams.Builder<ContextUnderstanderService>(
                            opCallback, serviceInstance -> {
                        callback.updateFilter(serviceInstance.onInitializeFilter());
                    }).setComponentId(componentId).build());
        }

        @Override
        public void handleEvent(ParcelUuid componentId, String packageName, InsightEvent event,
                IOpCallback opCallback) {
            mRequestProcessor.execute(
                    new BinderRequestProcessor.ExecutionParams.Builder<ContextUnderstanderService>(
                            opCallback, serviceInstance -> serviceInstance.onHandleEvent(
                            packageName, event)
                    ).setComponentId(componentId).build());
        }
    }
}
