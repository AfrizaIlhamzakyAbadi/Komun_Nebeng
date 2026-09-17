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

package android.companion.virtual.computercontrol;


import android.annotation.Hide;
import android.annotation.IntDef;
import android.annotation.NonNull;
import android.annotation.RequiresPermission;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

/**
 * Automation consent manager for {@link ComputerControlSession}. To be used to manage which agent
 * application can automate which target applications.
 */
@Hide
public interface ComputerControlConsentManager {

    /**
     * Consent state: The agent is allowed to automate the target app.
     */
    int ALLOWED = 1;

    /**
     * Consent state: The agent's consent to automate the target app is not set and will invoke
     * the user permission flow to request consent.
     */
    int DEFAULT = 2;

    /**
     * Consent state: The agent is not allowed to automate the target app. Any attempt to start
     * session targeting the app will fail until user explicitly changes the consent for the app
     * in Settings.
     */
    int NOT_ALLOWED = 3;

    @Hide
    @IntDef(prefix = {""}, value = {
            ALLOWED,
            DEFAULT,
            NOT_ALLOWED
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface ConsentState {}

    /**
     * Sets the consent state for the agent to automate the target app.
     *
     * @param agentUid The UID of the agent application.
     * @param agentPackageName The package name of the agent application.
     * @param targetPackageName The package name of the target application to be automated.
     * @param consentState The new consent state.
     */
    @RequiresPermission(android.Manifest.permission.MANAGE_COMPUTER_CONTROL_CONSENT)
    void setUserConsent(int agentUid, @NonNull String agentPackageName,
            @NonNull String targetPackageName, @ConsentState int consentState);

    /**
     * Returns the consent state for the agent to automate the target app.
     *
     * @param agentUid The UID of the agent application.
     * @param agentPackageName The package name of the agent application.
     * @param targetPackageName The package name of the target application being checked.
     * @return The current consent state for the given agent and target package. If no explicit
     *   consent has been set for this target package, {@link #DEFAULT} is returned.
     */
    @ConsentState
    int getUserConsent(int agentUid, @NonNull String agentPackageName,
            @NonNull String targetPackageName);

    /**
     * Clears all user consents for the agent. This will result in all target applications
     * being reset to the {@link #DEFAULT} state for this agent.
     *
     * @param agentUid The UID of the agent application.
     * @param agentPackageName The package name of the agent application.
     */
    @RequiresPermission(android.Manifest.permission.MANAGE_COMPUTER_CONTROL_CONSENT)
    void clearAllUserConsents(int agentUid, @NonNull String agentPackageName);

    /**
     * Returns a map of target packages to their consent states for the agent.
     *
     * <p>The returned map only contains target applications for which the user has explicitly
     * provided a consent (e.g., through a consent dialog). Applications that have never been
     * presented for consent will not be present in this map, even though
     * {@link #getUserConsent(int, String, String)} would return {@link #DEFAULT} for them.</p>
     *
     * @param agentUid The UID of the agent application.
     * @param agentPackageName The package name of the agent application.
     * @return A map where keys are target package names and values are their corresponding
     * {@link ConsentState}.
     */
    @NonNull
    Map<String, Integer> getUserConsents(int agentUid, @NonNull String agentPackageName);
}
