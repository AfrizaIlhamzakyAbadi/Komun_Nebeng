/*
 * Copyright (C) 2012 The Android Open Source Project
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
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.SuppressLint;
import android.annotation.SystemApi;
import android.annotation.UiThread;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.print.PrintDocumentAdapter;
import android.util.LongSparseArray;
import android.util.SparseArray;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeProvider;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.view.textclassifier.TextClassifier;
import android.view.translation.TranslationCapability;
import android.view.translation.TranslationSpec.DataFormat;
import android.view.translation.ViewTranslationRequest;
import android.view.translation.ViewTranslationResponse;
import android.webkit.WebView.HitTestResult;
import android.webkit.WebView.PictureListener;
import android.webkit.WebView.VisualStateCallback;


import java.io.BufferedWriter;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

/**
 * WebView backend provider interface: this interface is the abstract backend to a WebView
 * instance; each WebView object is bound to exactly one WebViewProvider object which implements
 * the runtime behavior of that WebView.
 *
 * All methods must behave as per their namesake in {@link WebView}, unless otherwise noted.
 */
@SystemApi
public interface WebViewProvider {
    //-------------------------------------------------------------------------
    // Main interface for backend provider of the WebView class.
    //-------------------------------------------------------------------------
    /**
     * Initialize this WebViewProvider instance. Called after the WebView has fully constructed.
     * @param javaScriptInterfaces is a Map of interface names, as keys, and
     * object implementing those interfaces, as values.
     * @param privateBrowsing If {@code true} the web view will be initialized in private /
     * incognito mode.
     */
    public void init(Map<String, Object> javaScriptInterfaces,
            boolean privateBrowsing);

    // Deprecated - should never be called
    public void setHorizontalScrollbarOverlay(boolean overlay);

    // Deprecated - should never be called
    public void setVerticalScrollbarOverlay(boolean overlay);

    // Deprecated - should never be called
    public boolean overlayHorizontalScrollbar();

    // Deprecated - should never be called
    public boolean overlayVerticalScrollbar();

    @UiThread
    public int getVisibleTitleHeight();

    @UiThread
    public SslCertificate getCertificate();

    @UiThread
    public void setCertificate(SslCertificate certificate);

    @UiThread
    public void savePassword(String host, String username, String password);

    @UiThread
    public void setHttpAuthUsernamePassword(String host, String realm,
            String username, String password);

    @UiThread
    public String[] getHttpAuthUsernamePassword(String host, String realm);

    /**
     * See {@link WebView#destroy()}.
     * As well as releasing the internal state and resources held by the implementation,
     * the provider should null all references it holds on the WebView proxy class, and ensure
     * no further method calls are made to it.
     */
    @UiThread
    public void destroy();

    @UiThread
    public void setNetworkAvailable(boolean networkUp);

    @UiThread
    public WebBackForwardList saveState(Bundle outState);

    @UiThread
    public boolean savePicture(Bundle b, final File dest);

    @UiThread
    public boolean restorePicture(Bundle b, File src);

    @UiThread
    public WebBackForwardList restoreState(Bundle inState);

    @UiThread
    public void loadUrl(String url, Map<String, String> additionalHttpHeaders);

    @UiThread
    public void loadUrl(String url);

    @UiThread
    public void postUrl(String url, byte[] postData);

    @UiThread
    public void loadData(String data, String mimeType, String encoding);

    @UiThread
    public void loadDataWithBaseURL(String baseUrl, String data,
            String mimeType, String encoding, String historyUrl);

    @UiThread
    public void evaluateJavaScript(String script, ValueCallback<String> resultCallback);

    @UiThread
    public void saveWebArchive(String filename);

    @UiThread
    public void saveWebArchive(String basename, boolean autoname, ValueCallback<String> callback);

    @UiThread
    public void stopLoading();

    @UiThread
    public void reload();

    @UiThread
    public boolean canGoBack();

    @UiThread
    public void goBack();

    @UiThread
    public boolean canGoForward();

    @UiThread
    public void goForward();

    @UiThread
    public boolean canGoBackOrForward(int steps);

    @UiThread
    public void goBackOrForward(int steps);

    @UiThread
    public boolean isPrivateBrowsingEnabled();

    @UiThread
    public boolean pageUp(boolean top);

    @UiThread
    public boolean pageDown(boolean bottom);

    @UiThread
    public void insertVisualStateCallback(long requestId, VisualStateCallback callback);

    @UiThread
    public void clearView();

    @UiThread
    public Picture capturePicture();

    @UiThread
    public PrintDocumentAdapter createPrintDocumentAdapter(String documentName);

    @UiThread
    public float getScale();

    @UiThread
    public void setInitialScale(int scaleInPercent);

    @UiThread
    public void invokeZoomPicker();

    @UiThread
    public HitTestResult getHitTestResult();

    @UiThread
    public void requestFocusNodeHref(Message hrefMsg);

    @UiThread
    public void requestImageRef(Message msg);

    @UiThread
    public String getUrl();

    @UiThread
    public String getOriginalUrl();

    @UiThread
    public String getTitle();

    @UiThread
    public Bitmap getFavicon();

    public String getTouchIconUrl();

    @UiThread
    public int getProgress();

    @UiThread
    public int getContentHeight();

    public int getContentWidth();

    @UiThread
    public void pauseTimers();

    @UiThread
    public void resumeTimers();

    @UiThread
    public void onPause();

    @UiThread
    public void onResume();

    public boolean isPaused();

    @UiThread
    public void freeMemory();

    @UiThread
    public void clearCache(boolean includeDiskFiles);

    @UiThread
    public void clearFormData();

    @UiThread
    public void clearHistory();

    @UiThread
    public void clearSslPreferences();

    @UiThread
    public WebBackForwardList copyBackForwardList();

    @UiThread
    public void setFindListener(WebView.FindListener listener);

    @UiThread
    public void findNext(boolean forward);

    @UiThread
    public int findAll(String find);

    @UiThread
    public void findAllAsync(String find);

    @UiThread
    public boolean showFindDialog(String text, boolean showIme);

    @UiThread
    public void clearMatches();

    @UiThread
    public void documentHasImages(Message response);

    @UiThread
    public void setWebViewClient(WebViewClient client);

    @UiThread
    public WebViewClient getWebViewClient();

    @UiThread
    @Nullable
    public WebViewRenderProcess getWebViewRenderProcess();

    @UiThread
    public void setWebViewRenderProcessClient(
            @Nullable Executor executor,
            @Nullable WebViewRenderProcessClient client);

    @Nullable
    public WebViewRenderProcessClient getWebViewRenderProcessClient();

    @UiThread
    public void setDownloadListener(DownloadListener listener);

    @UiThread
    public void setWebChromeClient(WebChromeClient client);

    @UiThread
    public WebChromeClient getWebChromeClient();

    @UiThread
    public void setPictureListener(PictureListener listener);

    @UiThread
    public void addJavascriptInterface(Object obj, String interfaceName);

    @UiThread
    public void removeJavascriptInterface(String interfaceName);

    @UiThread
    public WebMessagePort[] createWebMessageChannel();

    @UiThread
    public void postMessageToMainFrame(WebMessage message, Uri targetOrigin);

    @UiThread
    public WebSettings getSettings();

    @UiThread
    public void setMapTrackballToArrowKeys(boolean setMap);

    @UiThread
    public void flingScroll(int vx, int vy);

    @UiThread
    public View getZoomControls();

    @UiThread
    public boolean canZoomIn();

    @UiThread
    public boolean canZoomOut();

    @UiThread
    public boolean zoomBy(float zoomFactor);

    @UiThread
    public boolean zoomIn();

    @UiThread
    public boolean zoomOut();

    public void dumpViewHierarchyWithProperties(BufferedWriter out, int level);

    public View findHierarchyView(String className, int hashCode);

    public void setRendererPriorityPolicy(int rendererRequestedPriority, boolean waivedWhenNotVisible);

    public int getRendererRequestedPriority();

    public boolean getRendererPriorityWaivedWhenNotVisible();

    @SuppressWarnings("unused")
    public default void setTextClassifier(@Nullable TextClassifier textClassifier) {}

    @NonNull
    public default TextClassifier getTextClassifier() { return TextClassifier.NO_OP; }

    //-------------------------------------------------------------------------
    // Provider internal methods
    //-------------------------------------------------------------------------

    /**
     * @return the ViewDelegate implementation. This provides the functionality to back all of
     * the name-sake functions from the View and ViewGroup base classes of WebView.
     */
    /* package */ ViewDelegate getViewDelegate();

    /**
     * @return a ScrollDelegate implementation. Normally this would be same object as is
     * returned by getViewDelegate().
     */
    /* package */ ScrollDelegate getScrollDelegate();

    /**
     * Only used by FindActionModeCallback to inform providers that the find dialog has
     * been dismissed.
     */
    public void notifyFindDialogDismissed();

    //-------------------------------------------------------------------------
    // View / ViewGroup delegation methods
    //-------------------------------------------------------------------------

    /**
     * Provides mechanism for the name-sake methods declared in View and ViewGroup to be delegated
     * into the WebViewProvider instance.
     * NOTE: For many of these methods, the WebView will provide a super.Foo() call before or after
     * making the call into the provider instance. This is done for convenience in the common case
     * of maintaining backward compatibility. For remaining super class calls (e.g. where the
     * provider may need to only conditionally make the call based on some internal state) see the
     * {@link WebView.PrivateAccess} callback class.
     */
    // TODO: See if the pattern of the super-class calls can be rationalized at all, and document
    // the remainder on the methods below.
    interface ViewDelegate {
        public boolean shouldDelayChildPressedState();

        public void onProvideVirtualStructure(android.view.ViewStructure structure);

        default void onProvideAutofillVirtualStructure(
                @SuppressWarnings("unused") android.view.ViewStructure structure,
                @SuppressWarnings("unused") int flags) {
        }

        default void autofill(@SuppressWarnings("unused") SparseArray<AutofillValue> values) {
        }

        default boolean isVisibleToUserForAutofill(@SuppressWarnings("unused") int virtualId) {
            return true; // true is the default value returned by View.isVisibleToUserForAutofill()
        }

        default void onProvideContentCaptureStructure(
                @NonNull @SuppressWarnings("unused") android.view.ViewStructure structure,
                @SuppressWarnings("unused") int flags) {
        }

        @SuppressLint("NullableCollection")
        default void onCreateVirtualViewTranslationRequests(
                @NonNull @SuppressWarnings("unused") long[] virtualIds,
                @NonNull @SuppressWarnings("unused") @DataFormat int[] supportedFormats,
                @NonNull @SuppressWarnings("unused")
                        Consumer<ViewTranslationRequest> requestsCollector) {
        }

        default void onVirtualViewTranslationResponses(
                @NonNull @SuppressWarnings("unused")
                        LongSparseArray<ViewTranslationResponse> response) {
        }

        default void dispatchCreateViewTranslationRequest(
                @NonNull @SuppressWarnings("unused") Map<AutofillId, long[]> viewIds,
                @NonNull @SuppressWarnings("unused") @DataFormat int[] supportedFormats,
                @Nullable @SuppressWarnings("unused") TranslationCapability capability,
                @NonNull @SuppressWarnings("unused") List<ViewTranslationRequest> requests) {

        }

        public AccessibilityNodeProvider getAccessibilityNodeProvider();

        public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info);

        public void onInitializeAccessibilityEvent(AccessibilityEvent event);

        public boolean performAccessibilityAction(int action, Bundle arguments);

        public void setOverScrollMode(int mode);

        public void setScrollBarStyle(int style);

        public void onDrawVerticalScrollBar(Canvas canvas, Drawable scrollBar, int l, int t,
                int r, int b);

        public void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY);

        public void onWindowVisibilityChanged(int visibility);

        public void onDraw(Canvas canvas);

        public void setLayoutParams(LayoutParams layoutParams);

        public boolean performLongClick();

        public void onConfigurationChanged(Configuration newConfig);

        public InputConnection onCreateInputConnection(EditorInfo outAttrs);

        public boolean onDragEvent(DragEvent event);

        public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event);

        public boolean onKeyDown(int keyCode, KeyEvent event);

        public boolean onKeyUp(int keyCode, KeyEvent event);

        public void onAttachedToWindow();

        public void onDetachedFromWindow();

        public default void onMovedToDisplay(int displayId, Configuration config) {}

        public void onVisibilityChanged(View changedView, int visibility);

        public void onWindowFocusChanged(boolean hasWindowFocus);

        public void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect);

        public boolean setFrame(int left, int top, int right, int bottom);

        public void onSizeChanged(int w, int h, int ow, int oh);

        public void onScrollChanged(int l, int t, int oldl, int oldt);

        public boolean dispatchKeyEvent(KeyEvent event);

        public boolean onTouchEvent(MotionEvent ev);

        public boolean onHoverEvent(MotionEvent event);

        public boolean onGenericMotionEvent(MotionEvent event);

        public boolean onTrackballEvent(MotionEvent ev);

        public boolean requestFocus(int direction, Rect previouslyFocusedRect);

        public void onMeasure(int widthMeasureSpec, int heightMeasureSpec);

        public boolean requestChildRectangleOnScreen(View child, Rect rect, boolean immediate);

        public void setBackgroundColor(int color);

        public void setLayerType(int layerType, Paint paint);

        public void preDispatchDraw(Canvas canvas);

        public void onStartTemporaryDetach();

        public void onFinishTemporaryDetach();

        public void onActivityResult(int requestCode, int resultCode, Intent data);

        public Handler getHandler(Handler originalHandler);

        public View findFocus(View originalFocusedView);

        @SuppressWarnings("unused")
        default boolean onCheckIsTextEditor() {
            return false;
        }

        /**
         * <p>This is the entry point for the WebView implementation to override. It returns
         * {@code null} when the WebView implementation hasn't implemented the WindowInsets support
         * on S yet. In this case, the {@link View#onApplyWindowInsets()} super method will be
         * called instead.
         *
         * @param insets Insets to apply
         * @return The supplied insets with any applied insets consumed.
         * @see View#onApplyWindowInsets(WindowInsets)
         */
        @SuppressWarnings("unused")
        @Nullable
        default WindowInsets onApplyWindowInsets(@Nullable WindowInsets insets) {
            return null;
        }

        /**
         * Only used by WebView.
         */
        @Hide
        @SuppressWarnings("unused")
        @Nullable
        default PointerIcon onResolvePointerIcon(@NonNull MotionEvent event, int pointerIndex) {
            return null;
        }
    }

    interface ScrollDelegate {
        // These methods are declared protected in the ViewGroup base class. This interface
        // exists to promote them to public so they may be called by the WebView proxy class.
        // TODO: Combine into ViewDelegate?
        /**
         * See {@link android.webkit.WebView#computeHorizontalScrollRange}
         */
        public int computeHorizontalScrollRange();

        /**
         * See {@link android.webkit.WebView#computeHorizontalScrollOffset}
         */
        public int computeHorizontalScrollOffset();

        /**
         * See {@link android.webkit.WebView#computeVerticalScrollRange}
         */
        public int computeVerticalScrollRange();

        /**
         * See {@link android.webkit.WebView#computeVerticalScrollOffset}
         */
        public int computeVerticalScrollOffset();

        /**
         * See {@link android.webkit.WebView#computeVerticalScrollExtent}
         */
        public int computeVerticalScrollExtent();

        /**
         * See {@link android.webkit.WebView#computeScroll}
         */
        public void computeScroll();
    }
}
