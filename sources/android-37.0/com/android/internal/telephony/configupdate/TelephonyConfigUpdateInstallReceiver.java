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

package com.android.internal.telephony.configupdate;

import static android.telephony.CarrierConfigManager.CARRIER_ROAMING_NTN_CONNECT_AUTOMATIC;
import static android.telephony.CarrierConfigManager.CARRIER_ROAMING_NTN_CONNECT_HYBRID;
import static android.telephony.CarrierConfigManager.SATELLITE_DATA_SUPPORT_ALL;
import static android.telephony.CarrierConfigManager.SATELLITE_DATA_SUPPORT_ONLY_RESTRICTED;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.Context;
import android.content.Intent;
import android.os.FileUtils;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.URLUtil;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.telephony.TelephonyConfigData;
import com.android.internal.telephony.data.DataConfig;
import com.android.internal.telephony.data.DataConfigParser;
import com.android.internal.telephony.data.DataUtils;
import com.android.internal.telephony.satellite.SatelliteConfig;
import com.android.internal.telephony.satellite.SatelliteConfigParser;
import com.android.internal.telephony.satellite.SatelliteConstants;
import com.android.internal.telephony.satellite.metrics.ConfigUpdaterMetricsStats;
import com.android.internal.telephony.util.TelephonyUtils;
import com.android.server.updates.ConfigUpdateInstallReceiver;

import libcore.io.IoUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class TelephonyConfigUpdateInstallReceiver extends ConfigUpdateInstallReceiver implements
        ConfigProviderAdaptor {

    private static final String TAG = "TelephonyConfigUpdateInstallReceiver";
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    protected static final String UPDATE_DIR = "/data/misc/telephonyconfig";
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    protected static final String NEW_CONFIG_CONTENT_PATH = "new_telephony_config.pb";
    protected static final String VALID_CONFIG_CONTENT_PATH = "valid_telephony_config.pb";
    private static final String BACKUP_CONTENT_PATH = "backup_telephony_config.pb";

    protected static final String UPDATE_METADATA_PATH = "metadata/";
    public static final String VERSION = "version";
    public static final String BACKUP_VERSION = "backup_version";

    private ConcurrentHashMap<Executor, Callback> mCallbackHashMap = new ConcurrentHashMap<>();

    /**
     * Map of domain to ConfigParser.
     *
     * Note: Missing configs are cached using the {@link #EMPTY_PARSER} sentinel value.
     * Callers accessing this map directly must check for this sentinel.
     */
    @VisibleForTesting
    protected final Map<String, ConfigParser> mConfigParsers = new ConcurrentHashMap<>();

    /**
     * A sentinel ConfigParser used to represent a missing or invalid configuration for a specific
     * domain.
     *
     * Concept:
     * ConcurrentHashMap does not support null values. To cache the "missing config" state (which
     * would normally be represented by null) and avoid repeated expensive disk I/O and main-thread
     * starvation, we use this singleton EMPTY_PARSER object as a sentinel.
     *
     * Logic:
     * 1. When getConfigParser(domain) is called, it uses computeIfAbsent.
     * 2. If the file is missing, it caches EMPTY_PARSER.
     * 3. getConfigParser then checks if the result is EMPTY_PARSER and returns null to the caller.
     * 4. This ensures the disk is only checked once, even if the file never exists.
     */
    private static final ConfigParser EMPTY_PARSER = new ConfigParser((byte[]) null) {
        @Override
        protected void parseData(@Nullable byte[] data) {}

        @Override
        public String getDomain() {
            return "";
        }
    };
    @NonNull
    private final ConfigUpdaterMetricsStats mConfigUpdaterMetricsStats;

    private int mOriginalVersion;

    public static TelephonyConfigUpdateInstallReceiver sReceiverAdaptorInstance =
            new TelephonyConfigUpdateInstallReceiver();

    /**
     * @return The singleton instance of TelephonyConfigUpdateInstallReceiver
     */
    @NonNull
    public static TelephonyConfigUpdateInstallReceiver getInstance() {
        return sReceiverAdaptorInstance;
    }

    public TelephonyConfigUpdateInstallReceiver() {
        super(UPDATE_DIR, NEW_CONFIG_CONTENT_PATH, UPDATE_METADATA_PATH, VERSION);
        mConfigUpdaterMetricsStats = ConfigUpdaterMetricsStats.getOrCreateInstance();
    }

    /**
     * @return byte array type of config data protobuffer file
     */
    @Nullable
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public byte[] getContentFromContentPath(@NonNull File contentPath) {
        try {
            return IoUtils.readFileAsByteArray(contentPath.getCanonicalPath());
        } catch (IOException e) {
            Log.e(TAG, "Failed to read current content : " + contentPath);
            return null;
        }
    }

    /**
     * @param parser target of validation.
     * @return {@code true} if all the config data are valid {@code false} otherwise.
     */
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public boolean isValidSatelliteCarrierConfigData(@NonNull ConfigParser parser) {
        SatelliteConfig satelliteConfig = (SatelliteConfig) parser.getConfig();
        if (satelliteConfig == null) {
            Log.e(TAG, "satelliteConfig is null");
            mConfigUpdaterMetricsStats.reportOemAndCarrierConfigError(
                    SatelliteConstants.CONFIG_UPDATE_RESULT_NO_SATELLITE_DATA);
            return false;
        }

        // If no carrier config exist then it is considered as a valid config
        Set<Integer> carrierIds = satelliteConfig.getAllSatelliteCarrierIds();
        for (int carrierId : carrierIds) {
            Map<String, Set<Integer>> plmnsServices =
                    satelliteConfig.getSupportedSatelliteServices(carrierId);
            Set<String> plmns = plmnsServices.keySet();
            for (String plmn : plmns) {
                if (!TelephonyUtils.isValidPlmn(plmn)) {
                    Log.e(TAG, "found invalid plmn : " + plmn);
                    mConfigUpdaterMetricsStats.reportCarrierConfigError(
                            SatelliteConstants.CONFIG_UPDATE_RESULT_INVALID_PLMN);
                    return false;
                }
                Set<Integer> serviceSet = plmnsServices.get(plmn);
                for (int service : serviceSet) {
                    if (!TelephonyUtils.isValidService(service)) {
                        Log.e(TAG, "found invalid service : " + service);
                        mConfigUpdaterMetricsStats.reportCarrierConfigError(SatelliteConstants
                                .CONFIG_UPDATE_RESULT_CARRIER_DATA_INVALID_SUPPORTED_SERVICES);
                        return false;
                    }
                }

                SatelliteConfig.PlmnConfig plmnConfig =
                        satelliteConfig.getSatellitePlmnConfigByCarrierId(carrierId, plmn);
                if (plmnConfig != null) {
                    if (!isValidNtnConnectType(plmnConfig.getNtnConnectType())) {
                        Log.e(TAG, "found invalid ntnConnecttype : "
                                + plmnConfig.getNtnConnectType());
                        mConfigUpdaterMetricsStats.reportCarrierConfigError(SatelliteConstants
                                .CONFIG_UPDATE_RESULT_CARRIER_DATA_INVALID_CONNECT_TYPE_PER_PLMN);
                        return false;
                    }
                }
            }

            Integer ntnConnectType = satelliteConfig
                    .getSatelliteNtnConnectTypeByCarrierId(carrierId);
            if (ntnConnectType != null) {
                if (!isValidNtnConnectType(ntnConnectType)) {
                    Log.e(TAG, "found invalid ntnConnecttype : " + ntnConnectType);
                    mConfigUpdaterMetricsStats.reportCarrierConfigError(SatelliteConstants
                            .CONFIG_UPDATE_RESULT_CARRIER_DATA_INVALID_CONNECT_TYPE);
                    return false;
                }
            }

            Integer dataSupportMode = satelliteConfig
                    .getSatelliteDataSupportModeByCarrierId(carrierId);
            if (dataSupportMode != null) {
                if (!isValidDataSupportMode(dataSupportMode)) {
                    Log.e(TAG, "found invalid dataSupportMode : " + dataSupportMode);
                    mConfigUpdaterMetricsStats.reportCarrierConfigError(SatelliteConstants
                            .CONFIG_UPDATE_RESULT_CARRIER_DATA_INVALID_DATA_SUPPORT_MODE);
                    return false;
                }
            }

            String entitlementUrl = satelliteConfig
                    .getSatelliteEntitlementServerUrlByCarrierId(carrierId);
            if (!TextUtils.isEmpty(entitlementUrl)) {
                if (!isValidEntitlementServerUrl(entitlementUrl)) {
                    Log.e(TAG, "found not properly formatted entitlement url: "
                            + entitlementUrl);
                    mConfigUpdaterMetricsStats.reportCarrierConfigError(SatelliteConstants
                            .CONFIG_UPDATE_RESULT_CARRIER_DATA_INVALID_ENTITLEMENT_URL);
                    return false;
                }
            }
        }
        Log.d(TAG, "the config data is valid");
        return true;
    }

    /**
     * Validates if the max allowed datamode is valid
     *
     * @param parser target of validation.
     * @return {@code true} if max allowed datamode is valid, {@code false} otherwise.
     */
    public boolean isValidMaxAllowedDataMode(@NonNull ConfigParser parser) {
        SatelliteConfig satelliteConfig = (SatelliteConfig) parser.getConfig();
        if (satelliteConfig == null) {
            Log.e(TAG, "satelliteConfig is null");
            mConfigUpdaterMetricsStats.reportOemAndCarrierConfigError(
                    SatelliteConstants.CONFIG_UPDATE_RESULT_NO_SATELLITE_DATA);
            return false;
        }

        Integer maxAllowedDataMode = satelliteConfig.getSatelliteMaxAllowedDataMode();
        if (maxAllowedDataMode == null) {
            Log.d(TAG, "maxAllowedDataMode is not set");
            return true;
        }

        if (!DataUtils.isValidDataMode(maxAllowedDataMode)) {
            Log.e(TAG, "found invalid maxAllowedDataMode : " + maxAllowedDataMode);
            mConfigUpdaterMetricsStats.reportCarrierConfigError(
                    SatelliteConstants
                            .CONFIG_UPDATE_RESULT_CARRIER_DATA_INVALID_MAX_ALLOWED_DATA_MODE);
            return false;
        }
        Log.d(TAG, "maxAllowedDataMode is valid");
        return true;
    }


    private boolean isValidDataSupportMode(int dataSupportMode) {
        if (dataSupportMode < SATELLITE_DATA_SUPPORT_ONLY_RESTRICTED
                || dataSupportMode > SATELLITE_DATA_SUPPORT_ALL) {
            Log.e(TAG, "isValidDataSupportMode: dataSupportMode="
                    + dataSupportMode + " is not valid");
            return false;
        }
        return true;
    }

    private boolean isValidNtnConnectType(int ntnConnectType) {
        if (ntnConnectType < CARRIER_ROAMING_NTN_CONNECT_AUTOMATIC
                || ntnConnectType > CARRIER_ROAMING_NTN_CONNECT_HYBRID) {
            Log.e(TAG, "isValidNtnConnectType: ntnConnectType="
                    + ntnConnectType + " is not valid");
            return false;
        }
        return true;
    }

    private boolean isValidEntitlementServerUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            Log.e(TAG, "isValidEntitlementServerUrl: url is empty");
            return false;
        }
        // Ensure it is a secure HTTPS connection
        if (!URLUtil.isHttpsUrl(url)) {
            Log.e(TAG, "isValidEntitlementServerUrl: url is not https connection");
            return false;
        }
        return true;
    }

    /**
     * Validates if the satellite provider plmns are valid
     *
     * @param parser target of validation.
     * @return {@code true} if satellite provider plmn are valid, {@code false} otherwise.
     */
    public boolean isValidSatelliteProvider(@NonNull ConfigParser parser) {
        SatelliteConfig satelliteConfig = (SatelliteConfig) parser.getConfig();
        if (satelliteConfig == null) {
            Log.e(TAG, "isValidSatelliteProvider: satelliteConfig is null");
            mConfigUpdaterMetricsStats.reportOemAndCarrierConfigError(
                    SatelliteConstants.CONFIG_UPDATE_RESULT_NO_SATELLITE_DATA);
            return false;
        }

        List<String> satelliteProviderList = satelliteConfig.getDeviceSatelliteProviderList();
        if (satelliteProviderList == null) {
            Log.d(TAG, "isValidSatelliteProvider: satelliteProviderList is not set");
            return true;
        }

        for (String satellitePlmn : satelliteProviderList) {
            if (!TelephonyUtils.isValidPlmn(satellitePlmn)) {
                Log.e(TAG, "isValidSatelliteProvider: invalid plmn = " + satellitePlmn);
                mConfigUpdaterMetricsStats.reportOemConfigError(SatelliteConstants
                        .CONFIG_UPDATE_RESULT_INVALID_PLMN);
                return false;
            }
        }

        Log.d(TAG, "satelliteProviderList is valid");
        return true;
    }

    /**
     * @param parser target of validation.
     * @return {@code true} if all the config data are valid {@code false} otherwise.
     */
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public boolean isValidDataConfig(@NonNull ConfigParser parser) {
        Log.d(TAG, "isValidDataConfig: parser class=" + parser.getClass().getName());
        if (!(parser instanceof DataConfigParser)) {
            Log.e(TAG, "isValidDataConfig: parser is not DataConfigParser!");
            return false;
        }
        DataConfig dataConfig = (DataConfig) parser.getConfig();
        if (dataConfig == null) {
            Log.e(TAG, "dataConfig is null");
            return false;
        }

        TelephonyConfigData.DataConfigProto configData = dataConfig.getConfigData();
        if (!configData.hasConnectionCapabilityConfigs()) {
            Log.d(TAG, "connection_capability_configs is null");
            return true;
        }

        TelephonyConfigData.ConnectionCapabilityConfig connectionCapabilityConfigs =
                configData.getConnectionCapabilityConfigs();

        if (connectionCapabilityConfigs.hasDefaultConnectionCapabilityConfig()) {
            if (!isConnectionCapabilityMapValid(
                    connectionCapabilityConfigs.getDefaultConnectionCapabilityConfig())) {
                return false;
            }
        }

        if (connectionCapabilityConfigs.getCarrierConnectionCapabilityConfigsCount() > 0) {
            for (TelephonyConfigData.ConnectionCapabilityMap map :
                    connectionCapabilityConfigs.getCarrierConnectionCapabilityConfigsList()) {
                if (!isConnectionCapabilityMapValid(map)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Validates the rules within a {@link TelephonyConfigData.ConnectionCapabilityMap}.
     * Each rule string must follow the format:
     * "NetworkCapability:ConnectionCapability:isApnRequired".
     * <ul>
     *     <li>NetworkCapability: An integer representing the network capability.</li>
     *     <li>ConnectionCapability: An integer representing the connection capability.</li>
     *     <li>isApnRequired: A boolean ("true" or "false") indicating if APN is required.</li>
     * </ul>
     *
     * @param map The {@link TelephonyConfigData.ConnectionCapabilityMap} to validate.
     * @return {@code true} if all rules in the map are valid, {@code false} otherwise.
     */
    private boolean isConnectionCapabilityMapValid(
            TelephonyConfigData.ConnectionCapabilityMap map) {
        if (map.getRulesCount() == 0) return true;
        for (String rule : map.getRulesList()) {
            String[] parts = rule.split(":");
            if (parts.length != 3) {
                Log.e(TAG, "Invalid rule format: " + rule);
                return false;
            }
            try {
                // try parsing
                Integer.parseInt(parts[0]); // NetworkCapability
                Integer.parseInt(parts[1]); // ConnectionCapability
                Boolean.parseBoolean(parts[2]);  //isApnRequired
            } catch (NumberFormatException e) {
                Log.e(TAG, "Invalid number format in rule: " + rule);
                return false;
            }
        }
        return true;
    }

    @Override
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PROTECTED)
    public void postInstall(Context context, Intent intent) {
        postInstall();
    }

    @Override
    @VisibleForTesting
    protected void writeUpdate(File dir, File file, InputStream update) throws IOException {
        // Just call the super implementation.
        // This override exists to make the method visible to unit tests in this package.
        super.writeUpdate(dir, file, update);
    }

    /**
     * Posts the installed config to the valid config path and notifies the config changed for each
     * domain.
     */
    private void postInstall() {
        // Create new config parsers for all domains.
        ConfigParser newSatelliteConfigParser =
                getNewConfigParser(DOMAIN_SATELLITE, getContentFromContentPath(updateContent));
        ConfigParser newDataConfigParser =
                getNewConfigParser(DOMAIN_DATA, getContentFromContentPath(updateContent));

        // Check if the configs are valid for all domains.
        boolean isValidSatelliteConfig = isValidConfig(newSatelliteConfigParser);
        boolean isValidDataConfig = isValidConfig(newDataConfigParser);
        // allow the config to be installed if any of the configs is valid.
        if (!isValidSatelliteConfig && !isValidDataConfig) {
            Log.e(TAG, "postInstall: All configs are invalid.");
            return;
        }

        // Replace the config parsers if the new config version is upgradeable for each domain.
        boolean isSatelliteConfigUpdated = false;
        boolean isDataConfigUpdated = false;

        if (isValidSatelliteConfig) {
            getInstance().mConfigParsers.compute(newSatelliteConfigParser.getDomain(),
                    (k, oldParser) -> {
                        if (isUpgradableVersion(oldParser, newSatelliteConfigParser)) {
                            return newSatelliteConfigParser;
                        }
                        return oldParser;
                    });
            // Check if the parser was updated and set version for metrics.
            if (getInstance().mConfigParsers.get(newSatelliteConfigParser.getDomain())
                    == newSatelliteConfigParser) {
                isSatelliteConfigUpdated = true;
                mConfigUpdaterMetricsStats.setConfigVersion(newSatelliteConfigParser.getVersion());
            }
        }

        if (isValidDataConfig) {
            getInstance().mConfigParsers.compute(newDataConfigParser.getDomain(),
                    (k, oldParser) -> {
                        if (isUpgradableVersion(oldParser, newDataConfigParser)) {
                            return newDataConfigParser;
                        }
                        return oldParser;
                    });
            // Check if the parser was updated
            if (getInstance().mConfigParsers.get(newDataConfigParser.getDomain())
                    == newDataConfigParser) {
                isDataConfigUpdated = true;
                // TODO metrics for data config
                // mConfigUpdaterMetricsStats.setConfigVersion(DOMAIN_DATA,
                //         newDataConfigParser.getVersion());
            }
        }

        // Copy the new config to the valid config path if the config is updated for any domain.
        if (isSatelliteConfigUpdated || isDataConfigUpdated) {
            if (!copySourceFileToTargetFile(NEW_CONFIG_CONTENT_PATH, VALID_CONFIG_CONTENT_PATH)) {
                Log.e(TAG, "fail to copy to the valid satellite carrier config data");
                if (isSatelliteConfigUpdated) {
                    // TODO metrics for data config, currently only satellite is supported.
                    mConfigUpdaterMetricsStats.reportOemAndCarrierConfigError(
                            SatelliteConstants.CONFIG_UPDATE_RESULT_IO_ERROR);
                }
            }
        }

        // Notify the config changed for each domain.
        if (isSatelliteConfigUpdated) {
            notifyConfigChanged(newSatelliteConfigParser);
        }
        if (isDataConfigUpdated) {
            notifyConfigChanged(newDataConfigParser);
        }
    }

    /**
     * Notifies the config changed for each domain.
     *
     * @param configParser the config parser that has been changed
     */
    private void notifyConfigChanged(@NonNull ConfigParser configParser) {
        if (!getInstance().mCallbackHashMap.isEmpty()) {
            for (Map.Entry<Executor, Callback> entry : getInstance().mCallbackHashMap.entrySet()) {
                Executor executor = entry.getKey();
                Callback callback = entry.getValue();
                if (callback != null) {
                    // run callback.onChanged() on the executor thread
                    executor.execute(() -> callback.onChanged(configParser));
                }
            }
        }
    }

    /**
     * Checks if the config parser for the given domain is upgradable.
     *
     * @param oldConfigParser the old config parser
     * @param newConfigParser the new config parser
     * @return {@code true} if the config parser is upgradable, {@code false} otherwise.
     */
    private boolean isUpgradableVersion(@Nullable ConfigParser oldConfigParser,
            @NonNull ConfigParser newConfigParser) {
        String domain = newConfigParser.getDomain();
        if (oldConfigParser == null || oldConfigParser == EMPTY_PARSER) {
            Log.d(TAG, "domain_" + domain + " is upgradable: never been installed previously.");
            return true;
        }
        int updatedVersion = newConfigParser.mVersion;
        int previousVersion = oldConfigParser.mVersion;
        if (updatedVersion <= previousVersion) {
            Log.e(TAG, "domain_" + domain + " is not upgradable: updated proto Version ["
                    + updatedVersion + "] is smaller than previous proto Version ["
                    + previousVersion + "]");
            if (domain.equals(DOMAIN_SATELLITE)) {
                // TODO metrics for data config, currently only satellite is supported.
                mConfigUpdaterMetricsStats.reportOemAndCarrierConfigError(
                        SatelliteConstants.CONFIG_UPDATE_RESULT_INVALID_VERSION);
            }
            return false;
        }
        Log.d(TAG, "domain_" + domain + " is upgradable: previous proto version is "
                + previousVersion + " | updated proto version is " + updatedVersion);
        return true;
    }

    /**
     * Checks if the config parser for the given domain is valid. If the config parser is null, it
     * is not valid. If the config parser is not null and the config is not valid for the given
     * domain, it is not valid. Otherwise, it is valid.
     *
     * @param configParser the config parser that we want to check
     * @return {@code true} if the config parser is valid, {@code false} otherwise.
     */
    private boolean isValidConfig(@Nullable ConfigParser configParser) {
        // check if configParser is null
        if (configParser == null) {
            Log.e(TAG, "isValidConfig: ConfigParser is null");
            return false;
        }

        // check if configParser is valid for given domain
        String domain = configParser.getDomain();
        switch (domain) {
            case DOMAIN_SATELLITE:
                if (!isValidSatelliteCarrierConfigData(configParser)) {
                    Log.e(TAG, "received config data has invalid satellite carrier config data");
                    return false;
                }
                if (!isValidMaxAllowedDataMode(configParser)) {
                    Log.e(TAG, "received config data has invalid max allowed data mode");
                    return false;
                }
                if (!isValidSatelliteProvider(configParser)) {
                    Log.e(TAG, "received config data has invalid satellite plmn list");
                    return false;
                }
                return true;
            case DOMAIN_DATA:
                if (!isValidDataConfig(configParser)) {
                    Log.e(TAG, "received config data has invalid data config");
                    return false;
                }
                return true;
            default:
                Log.e(TAG, "Invalid domain: " + domain);
                return false;
        }
    }

    @Nullable
    @Override
    public ConfigParser getConfigParser(String domain) {
        Log.d(TAG, "getConfigParser: domain=" + domain);
        ConfigParser parser = getInstance().mConfigParsers.computeIfAbsent(domain, d -> {
            Log.d(TAG, "CreateNewConfigParser with domain " + d);
            ConfigParser newParser = getNewConfigParser(
                    d, getContentFromContentPath(new File(updateDir,
                            VALID_CONFIG_CONTENT_PATH)));

            if (newParser == null || newParser.getConfig() == null) {
                return EMPTY_PARSER;
            }
            return newParser;
        });
        return (parser == EMPTY_PARSER) ? null : parser;
    }

    /**
     * Overrides the config parser. Should be used only in tests.
     *
     * @param configParser the config parser that we have to override
     */
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public void overrideConfigParser(ConfigParser configParser) {
        if (configParser == null) {
            Log.e(TAG, "overrideConfigParser: ConfigParser is null");
            return;
        }
        Log.d(TAG, "overrideConfigParser");
        getInstance().mConfigParsers.put(configParser.getDomain(), configParser);
    }

    /**
     * Clears the config parser for the given domain. Should be used only in tests.
     *
     * @param domain the domain that we have to clear the config parser
     */
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public void clearOverriddenConfigParser(String domain) {
        if (domain == null) {
            Log.e(TAG, "clearOverriddenConfigParser: domain is null");
            return;
        }
        Log.d(TAG, "clearOverriddenConfigParser domain_" + domain);
        getInstance().mConfigParsers.computeIfPresent(domain, (k, v) -> null);
    }

    @Override
    public void registerCallback(@NonNull Executor executor, @NonNull Callback callback) {
        mCallbackHashMap.put(executor, callback);
    }

    @Override
    public void unregisterCallback(@NonNull Callback callback) {
        Iterator<Executor> iterator = mCallbackHashMap.keySet().iterator();
        while (iterator.hasNext()) {
            Executor executor = iterator.next();
            if (mCallbackHashMap.get(executor) == callback) {
                mCallbackHashMap.remove(executor);
                break;
            }
        }
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public File getUpdateDir() {
        return getInstance().updateDir;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public File getUpdateContent() {
        return getInstance().updateContent;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public ConcurrentHashMap<Executor, Callback> getCallbackMap() {
        return getInstance().mCallbackHashMap;
    }

    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public void setCallbackMap(ConcurrentHashMap<Executor, Callback> map) {
        getInstance().mCallbackHashMap = map;
    }

    /**
     * @param data byte array type of config data
     * @return when data is null, return null otherwise return ConfigParser
     */
    @Nullable
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public ConfigParser getNewConfigParser(String domain, @Nullable byte[] data) {
        if (data == null) {
            Log.d(TAG, "content data is null");
            return null;
        }
        switch (domain) {
            case DOMAIN_SATELLITE:
                return new SatelliteConfigParser(data);
            case DOMAIN_DATA:
                return new DataConfigParser(data);
            default:
                Log.e(TAG, "DOMAIN should be specified");
                mConfigUpdaterMetricsStats.reportOemAndCarrierConfigError(
                        SatelliteConstants.CONFIG_UPDATE_RESULT_INVALID_DOMAIN);
                return null;
        }
    }

    /**
     * @param sourceFileName source file name
     * @param targetFileName target file name
     * @return {@code true} if successful, {@code false} otherwise
     */
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public boolean copySourceFileToTargetFile(
            @NonNull String sourceFileName, @NonNull String targetFileName) {
        try {
            File sourceFile = new File(UPDATE_DIR, sourceFileName);
            File targetFile = new File(UPDATE_DIR, targetFileName);
            Log.d(TAG, "copy " + sourceFile.getName() + " >> " + targetFile.getName());

            if (sourceFile.exists()) {
                if (targetFile.exists()) {
                    targetFile.delete();
                }
                FileUtils.copy(sourceFile, targetFile);
                FileUtils.copyPermissions(sourceFile, targetFile);
                Log.d(TAG, "success to copy the file " + sourceFile.getName() + " to "
                        + targetFile.getName());
                return true;
            }
        } catch (Exception e) {
            Log.e(TAG, "copy error : " + e);
            return false;
        }
        Log.d(TAG, "source file is not exist, no file to copy");
        return false;
    }

    /**
     * This API should be used by only CTS/unit tests to reset the telephony configs set through
     * config updater
     */
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PRIVATE)
    public boolean cleanUpTelephonyConfigs() {
        Log.d(TAG, "cleanTelephonyConfigs: resetting the telephony configs");
        try {
            // metadata/version
            File updateMetadataDir = new File(updateDir, UPDATE_METADATA_PATH);
            writeUpdate(
                    updateMetadataDir,
                    updateVersion,
                    new ByteArrayInputStream(Integer.toString(-1).getBytes()));

            // new_telephony_config.pb
            writeUpdate(updateDir, updateContent, new ByteArrayInputStream(new byte[]{}));

            // valid_telephony_config.pb
            File validConfigContentPath = new File(updateDir, VALID_CONFIG_CONTENT_PATH);
            writeUpdate(updateDir, validConfigContentPath, new ByteArrayInputStream(new byte[]{}));
        } catch (IOException e) {
            Log.e(TAG, "Failed to clean telephony config files: " + e);
            return false;
        }

        Log.d(TAG, "cleanTelephonyConfigs: resetting the config parser");
        getInstance().mConfigParsers.clear();
        return true;
    }

    /**
     * This API is used by CTS to override the version of the config data
     *
     * @param reset   Whether to restore the original version
     * @param version The overriding version
     * @return {@code true} if successful, {@code false} otherwise
     */
    public boolean overrideVersion(boolean reset, int version) {
        Log.d(TAG, "overrideVersion: reset=" + reset + ", version=" + version);
        if (reset) {
            version = mOriginalVersion;
            if (!restoreContentData()) {
                return false;
            }
        } else {
            mOriginalVersion = version;
            if (!backupContentData()) {
                return false;
            }
        }
        return overrideVersion(version);
    }

    private boolean overrideVersion(int version) {
        try {
            writeUpdate(updateDir, updateVersion,
                    new ByteArrayInputStream(Long.toString(version).getBytes()));
            getInstance().mConfigParsers.forEach((domain, parser) -> {
                if (parser != EMPTY_PARSER) {
                    parser.overrideVersion(version);
                }
            });
        } catch (IOException e) {
            Log.e(TAG, "overrideVersion: e=" + e);
            return false;
        }
        return true;
    }

    /**
     * Checks if a specific file exists within the telephony configuration update directory.
     * This method verifies both the existence of the file and that the path represents
     * a regular file rather than a directory.
     *
     * @param fileName The name of the file to check, relative to the update directory.
     * @return {@code true} if the file exists and is a regular file; {@code false} otherwise
     *         or if the fileName is null.
     */
    @VisibleForTesting
    protected boolean isFileExists(@NonNull String fileName) {
        Log.d(TAG, "isFileExists");
        if (fileName == null) {
            Log.d(TAG, "fileName cannot be null");
            return false;
        }
        File sourceFile = new File(UPDATE_DIR, fileName);
        return sourceFile.exists() && sourceFile.isFile();
    }

    private boolean backupContentData() {
        if (!isFileExists(VALID_CONFIG_CONTENT_PATH)) {
            Log.d(TAG, VALID_CONFIG_CONTENT_PATH + " does not exist, no need to backup");
            return true;
        }
        if (!copySourceFileToTargetFile(VALID_CONFIG_CONTENT_PATH, BACKUP_CONTENT_PATH)) {
            Log.e(TAG, "backupContentData: fail to backup the config data");
            return false;
        }
        if (!copySourceFileToTargetFile(UPDATE_METADATA_PATH + VERSION,
                UPDATE_METADATA_PATH + BACKUP_VERSION)) {
            Log.e(TAG, "backupContentData: fail to backup the version");
            return false;
        }
        Log.d(TAG, "backupContentData: backup success");
        return true;
    }

    /**
     * Helper to write content to a file in the update directory.
     * Defined here to be easily mockable in unit tests.
     */
    @VisibleForTesting
    protected void writeContentToFile(File dir, File file, byte[] content) throws IOException {
        writeUpdate(dir, file, new ByteArrayInputStream(content));
    }

    /**
     * Restores the telephony configuration data and version metadata from backup files.
     * <p>
     * If a backup exists, it replaces the current active configuration and version with
     * the backed-up data. If no backup is found, it resets the configuration to an
     * empty state to ensure the system returns to a known default.
     * <p>
     * This method triggers {@link #postInstallForRestore()} upon completion to ensure
     * that all internal maps are updated and downstream listeners (e.g., DataConfigManager)
     * are notified of the change, even if the resulting configuration is empty or a version
     * downgrade.
     *
     * @return {@code true} if the restoration or reset process completed successfully;
     *         {@code false} if an I/O error occurred during file operations.
     */
    @VisibleForTesting
    protected boolean restoreContentData() {
        if (!isFileExists(BACKUP_CONTENT_PATH)) {
            Log.d(TAG, BACKUP_CONTENT_PATH + " does not exist. Restoring to empty state.");

            // Write empty content to 'new_telephony_config.pb' to simulate an empty update
            try {
                // updateContent is the File object for NEW_CONFIG_CONTENT_PATH (from superclass)
                writeContentToFile(updateDir, updateContent, new byte[0]);
            } catch (IOException e) {
                Log.e(TAG, "restoreContentData: Failed to write empty config: " + e);
                return false;
            }

            // This ensures:
            //   - Parsers are re-created with empty data.
            //   - 'valid_telephony_config.pb' is overwritten with empty data.
            //   - Listeners (DataConfigManager) are NOTIFIED of the change (crucial!).
            postInstallForRestore();
            return true;
        }
        if (!copySourceFileToTargetFile(BACKUP_CONTENT_PATH, NEW_CONFIG_CONTENT_PATH)) {
            Log.e(TAG, "restoreContentData: fail to restore the config data");
            return false;
        }
        if (!copySourceFileToTargetFile(UPDATE_METADATA_PATH + BACKUP_VERSION,
                UPDATE_METADATA_PATH + VERSION)) {
            Log.e(TAG, "restoreContentData: fail to restore the version");
            return false;
        }
        Log.d(TAG, "restoreContentData: populate the data to Controllers");
        postInstallForRestore();
        Log.d(TAG, "restoreContentData: success");
        return true;
    }

    /**
     * An alternative to postInstall used specifically for the restore/reset flow by CTS.
     * It bypasses validation (accepting null/empty configs) and version checks (allowing
     * downgrades).
     * This is needed because the config before overriding the version can be null or even on lower
     * version than the current one in the disc. After the CTS we need to force to restore to the
     * previous version.
     */
    @VisibleForTesting
    protected void postInstallForRestore() {
        Log.d(TAG, "postInstallForRestore: Force-applying restored config");

        // Note: The file exists because restoreContentData() just copied it there.
        byte[] content = getContentFromContentPath(updateContent);
        ConfigParser newSatelliteConfigParser = getNewConfigParser(DOMAIN_SATELLITE, content);
        ConfigParser newDataConfigParser = getNewConfigParser(DOMAIN_DATA, content);

        if (newSatelliteConfigParser != null) {
            // DIRECT UPDATE: Skip isValidConfig() and isUpgradableVersion()
            getInstance().mConfigParsers.put(DOMAIN_SATELLITE, newSatelliteConfigParser);

            mConfigUpdaterMetricsStats.setConfigVersion(newSatelliteConfigParser.getVersion());

            // (Even if the inner config object is null, the parser instance is valid)
            notifyConfigChanged(newSatelliteConfigParser);
        }

        // Force Update Data Domain
        if (newDataConfigParser != null) {
            // DIRECT UPDATE: Skip isValidConfig() and isUpgradableVersion()
            getInstance().mConfigParsers.put(DOMAIN_DATA, newDataConfigParser);

            // NOTIFY: This tells DataConfigManager to re-read the config (even if null)
            notifyConfigChanged(newDataConfigParser);
        }

        // Since we forced this configuration to be active, we must overwrite the
        // "valid_telephony_config.pb" on disk to match our memory state.
        if (!copySourceFileToTargetFile(NEW_CONFIG_CONTENT_PATH, VALID_CONFIG_CONTENT_PATH)) {
            Log.e(TAG, "postInstallForRestore: failed to copy to valid config path");
        }
    }
}
