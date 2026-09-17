/*
 * Copyright (C) 2016 The Android Open Source Project
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

package android.webkit;

import android.annotation.Hide;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.ChildZygoteProcess;
import android.os.IZygoteProcess;
import android.os.NativeZygoteProcess;
import android.os.Process;
import android.os.UserHandle;
import android.os.ZygoteProcess;
import android.text.TextUtils;
import android.util.Log;

import com.android.internal.annotations.GuardedBy;
import com.android.internal.os.Zygote;

@Hide
public class WebViewZygote {
    private static final String LOGTAG = "WebViewZygote";

    /**
     * Lock object that protects all other static members.
     */
    private static final Object sLock = new Object();

    /**
     * Instance that maintains the socket connection to the zygote. This is {@code null} if the
     * zygote is not running or is not connected.
     */
    @GuardedBy("sLock")
    private static ChildZygoteProcess sZygote;

    /**
     * Information about the selected WebView package. This is set from #onWebViewProviderChanged().
     */
    @GuardedBy("sLock")
    private static PackageInfo sPackage;

    /**
     * Returns the underlying Zygote process.
     */
    public static IZygoteProcess getProcess() {
        synchronized (sLock) {
            connectToZygoteIfNeededLocked();
            return sZygote != null ? sZygote.getZygoteProcess() : null;
        }
    }

    public static String getPackageName() {
        synchronized (sLock) {
            return sPackage.packageName;
        }
    }

    public static boolean isMultiprocessEnabled() {
        synchronized (sLock) {
            return sPackage != null;
        }
    }

    /**
     * Returns whether the Native WebView Zygote is enabled for the currently loaded WebView
     * package.
     */
    @Hide
    public static boolean isNativeWebViewZygoteEnabled() {
        synchronized (sLock) {
            return isNativeWebViewZygoteEnabledForPackage(sPackage);
        }
    }

    /**
     * Returns whether the Native WebView Zygote is enabled for the given package.
     */
    @Hide
    public static boolean isNativeWebViewZygoteEnabledForPackage(PackageInfo pkg) {
        if (!Flags.nativeWebviewZygote() || pkg == null) {
            return false;
        }

        // Native WebView Zygote is only enabled when the bitness of the WebView renderer ABI
        // matches the bitness of the system's primary ABI.
        final String abi = pkg.applicationInfo.primaryCpuAbi;
        final String systemAbi = android.os.SystemProperties.get("ro.product.cpu.abi");
        return Build.is64BitAbi(abi) == Build.is64BitAbi(systemAbi);
    }

    static void onWebViewProviderChanged(PackageInfo packageInfo) {
        synchronized (sLock) {
            sPackage = packageInfo;
            if (isNativeWebViewZygoteEnabledForPackage(packageInfo)) {
                NativeZygoteProcess.prewarmNativeZygote();
            }
            stopZygoteLocked();
        }
    }

    @GuardedBy("sLock")
    private static void stopZygoteLocked() {
        if (sZygote != null) {
            // Close the connection and kill the zygote process. This will not cause
            // child processes to be killed by itself. But if this is called in response to
            // setMultiprocessEnabled() or onWebViewProviderChanged(), the WebViewUpdater
            // will kill all processes that depend on the WebView package.
            sZygote.getZygoteProcess().close();
            Process.killProcess(sZygote.getPid());
            sZygote = null;
        }
    }

    @GuardedBy("sLock")
    private static void connectToZygoteIfNeededLocked() {
        if (sZygote != null) {
            return;
        }

        if (sPackage == null) {
            Log.e(LOGTAG, "Cannot connect to zygote, no package specified");
            return;
        }

        try {
            String abi = sPackage.applicationInfo.primaryCpuAbi;
            int runtimeFlags = Zygote.getMemorySafetyRuntimeFlagsForSecondaryZygote(
                    sPackage.applicationInfo, null);
            final int[] sharedAppGid = {
                    UserHandle.getSharedAppGid(UserHandle.getAppId(sPackage.applicationInfo.uid)) };

            final boolean useNativeWebViewZygote = isNativeWebViewZygoteEnabledForPackage(sPackage);
            final IZygoteProcess zygoteProcess = useNativeWebViewZygote
                    ? Process.NATIVE_ZYGOTE_PROCESS : Process.ZYGOTE_PROCESS;
            final String zygoteName = useNativeWebViewZygote
                    ? "native_webview_zygote" : "webview_zygote";
            final String nativePreloadLibrary =
                    WebViewFactory.getWebViewLibrary(sPackage.applicationInfo);
            if (useNativeWebViewZygote && nativePreloadLibrary == null) {
                Log.e(LOGTAG, "No library specified, cannot connect to Native WebView Zygote");
                return;
            }
            final long nativeReservedAddress = useNativeWebViewZygote
                    ? WebViewLibraryLoader.getReservedAddress() : 0;
            final long nativeReservedSize = useNativeWebViewZygote
                    ? WebViewLibraryLoader.getReservedSize() : 0;
            final String nativeLibraryRelroPath = useNativeWebViewZygote
                    ? WebViewLibraryLoader.getRelroPath() : null;

            sZygote = zygoteProcess.startChildZygote(
                    "com.android.internal.os.WebViewZygoteInit",
                    zygoteName,
                    Process.WEBVIEW_ZYGOTE_UID,
                    Process.WEBVIEW_ZYGOTE_UID,
                    sharedAppGid,  // Access to shared app GID for ART profiles
                    runtimeFlags,
                    zygoteName, // seInfo
                    abi,
                    TextUtils.join(",", Build.SUPPORTED_ABIS),
                    null, // instructionSet
                    Process.FIRST_ISOLATED_UID,
                    Integer.MAX_VALUE,  // TODO(b/123615476) deal with user-id ranges properly
                    sPackage.applicationInfo,
                    nativePreloadLibrary,
                    null, // nativePreloadInit
                    nativeReservedAddress,
                    nativeReservedSize,
                    nativeLibraryRelroPath);

            if (useNativeWebViewZygote) {
                ZygoteProcess.waitForConnectionToNativeZygote(
                        sZygote.getZygoteProcess().getPrimarySocketAddress());
            } else {
                ZygoteProcess.waitForConnectionToZygote(
                        sZygote.getZygoteProcess().getPrimarySocketAddress());
                sZygote.getZygoteProcess().preloadApp(sPackage.applicationInfo, abi);
            }
        } catch (Exception e) {
            Log.e(LOGTAG, "Error connecting to webview zygote", e);
            stopZygoteLocked();
        }
    }
}
