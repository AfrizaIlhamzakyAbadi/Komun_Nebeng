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

package android.app;

import static android.permission.flags.Flags.FLAG_APP_INTERACTION_ACCESS_CONTROL_ENABLED;

import android.annotation.FlaggedApi;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.SdkConstant;
import android.annotation.SystemApi;
import android.annotation.SystemService;
import android.content.Context;
import android.content.Intent;
import android.os.RemoteException;

/**
 * Provides access to App Interaction access control APIs.
 */
@FlaggedApi(FLAG_APP_INTERACTION_ACCESS_CONTROL_ENABLED)
@SystemService(Context.APP_INTERACTION_SERVICE)
public final class AppInteractionManager {
    private static final String TAG = "AppInteractionManager";

    /**
     * Activity action: Launches a UI that shows list of all agents and provides management of App
     * Interaction access of those agents.
     *
     * <p>Input: Nothing.
     *
     * <p>Output: Nothing.
     */
    @SdkConstant(SdkConstant.SdkConstantType.ACTIVITY_INTENT_ACTION)
    public static final String ACTION_MANAGE_APP_INTERACTION_ACCESS =
            "android.app.action.MANAGE_APP_INTERACTION_ACCESS";

    /**
     * Activity action: Launches a UI that shows a list of all targets that the specified agent
     * package can access, and provides management of App Interaction access of those targets.
     *
     * <p>Input: {@link android.content.Intent#EXTRA_PACKAGE_NAME} specifies the package whose
     * access will be managed by the launched UI.
     *
     * <p>Output: Nothing.
     */
    @SdkConstant(SdkConstant.SdkConstantType.ACTIVITY_INTENT_ACTION)
    public static final String ACTION_MANAGE_AGENT_APP_INTERACTION_ACCESS =
            "android.app.action.MANAGE_AGENT_APP_INTERACTION_ACCESS";

    /**
     * Activity action: Launches a UI that shows list of all agents for a specific target and
     * provides management of App Interaction access by those agents.
     *
     * <p>Input: {@link android.content.Intent#EXTRA_PACKAGE_NAME} specifies the package whose
     * access will be managed by the launched UI.
     *
     * <p>Output: Nothing.
     */
    @SdkConstant(SdkConstant.SdkConstantType.ACTIVITY_INTENT_ACTION)
    public static final String ACTION_MANAGE_TARGET_APP_INTERACTION_ACCESS =
            "android.app.action.MANAGE_TARGET_APP_INTERACTION_ACCESS";

    /**
     * Activity action: Launches a UI for an agent to request App Interaction access of a target.
     *
     * <p>Input: {@link android.content.Intent#EXTRA_PACKAGE_NAME} specifies the package for which
     * the calling agent is requesting access of.
     *
     * <p>Output: Nothing.
     */
    @SystemApi
    @SdkConstant(SdkConstant.SdkConstantType.ACTIVITY_INTENT_ACTION)
    public static final String ACTION_REQUEST_APP_INTERACTION_ACCESS =
            "android.app.action.REQUEST_APP_INTERACTION_ACCESS";

    private final IAppInteractionManager mService;

    /**
     * Creates an instance.
     *
     * @param service An interface to the backing service.
     */
    @Hide
    public AppInteractionManager(IAppInteractionManager service) {
        mService = service;
    }

    /**
     * Creates an intent which can be used to request App Interaction access for the given target
     * app.
     *
     * <p>The intent MUST be used with {@link android.app.Activity#startActivityForResult}. The
     * result code of the activity will be {@link android.app.Activity#RESULT_OK} if the request was
     * granted, {@link android.app.Activity#RESULT_CANCELED} if not.</p>
     *
     * @param targetPackageName The app access is being requested for.
     * @return The created intent.
     */
    public @NonNull Intent createRequestAccessIntent(@NonNull String targetPackageName) {
        try {
            return mService.createRequestAccessIntent(targetPackageName);
        } catch (RemoteException e) {
            throw e.rethrowFromSystemServer();
        }
    }
}
