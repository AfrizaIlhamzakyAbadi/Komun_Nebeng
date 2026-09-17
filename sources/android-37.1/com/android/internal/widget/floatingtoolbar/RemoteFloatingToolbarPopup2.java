/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.android.internal.widget.floatingtoolbar;

import static android.view.selectiontoolbar.ToolbarMenuItem.PRIORITY_OVERFLOW;
import static android.view.selectiontoolbar.ToolbarMenuItem.PRIORITY_PRIMARY;
import static android.view.selectiontoolbar.ToolbarMenuItem.PRIORITY_UNKNOWN;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.annotation.UiThread;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Trace;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.SurfaceControlViewHost;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewRootImpl;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.selectiontoolbar.SelectionToolbarClient;
import android.view.selectiontoolbar.SelectionToolbarProvider;
import android.view.selectiontoolbar.SelectionToolbarProviderFactory;
import android.view.selectiontoolbar.SelectionToolbarRequest;
import android.view.selectiontoolbar.SelectionToolbarSession;
import android.view.selectiontoolbar.ToolbarMenuItem;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.android.internal.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * A popup window used by the floating toolbar to render menu items in the remote system process.
 *
 * It holds 2 panels (i.e. main panel and overflow panel) and an overflow button
 * to transition between panels.
 */
public final class RemoteFloatingToolbarPopup2
        implements FloatingToolbarPopup, SelectionToolbarClient {

    private static final boolean DEBUG =
            Log.isLoggable(FloatingToolbar.FLOATING_TOOLBAR_TAG, Log.VERBOSE);
    public static final String TRACE_TRACK_NAME = "RemoteFloatingToolbarPopup";

    private final int mTraceCookie = System.identityHashCode(this);

    @NonNull
    private final Context mContext;

    @NonNull
    private final SelectionToolbarProvider mSelectionToolbarProvider;
    // Parent for the popup window.
    @NonNull
    private final View mParent;
    // A popup window used for showing menu items rendered by the remote system process
    @NonNull
    private final PopupWindow mPopupWindow;

    private boolean mHasSessionOpened;
    private boolean mHasShowToolbarBeenTraced;

    // Tracks this toolbar popup state.
    private @ToolbarState int mState = TOOLBAR_STATE_DISMISSED;
    private int mNextSequenceNumber;

    // To be updated in onShow
    private final Rect mContentRect = new Rect();
    private final Region mTouchableRegion = new Region();
    private final ViewTreeObserver.OnComputeInternalInsetsListener mInsetsComputer =
            info -> {
                info.contentInsets.setEmpty();
                info.visibleInsets.setEmpty();
                info.touchableRegion.set(mTouchableRegion);
                info.setTouchableInsets(
                        ViewTreeObserver.InternalInsetsInfo.TOUCHABLE_INSETS_REGION);
            };
    private List<MenuItem> mMenuItems;
    private MenuItem.OnMenuItemClickListener mMenuItemClickListener;
    private SelectionToolbarSession mSession;
    // The most recent call into mSession waiting to be dispatched for when the session has been
    // returned.
    private Runnable mPendingSessionCall;

    private int mSuggestedWidth;
    private boolean mWidthChanged = true;
    private final boolean mIsLightTheme;

    private final int[] mCoordsOnScreen = new int[2];
    private final int[] mCoordsOnWindow = new int[2];

    public RemoteFloatingToolbarPopup2(Context context, View parent) {
        mContext = context;
        mParent = Objects.requireNonNull(parent);
        mPopupWindow = createPopupWindow(context);
        mSelectionToolbarProvider = SelectionToolbarProviderFactory.create(context);
        mIsLightTheme = isLightTheme(context);
        mPopupWindow.getContentView().addOnAttachStateChangeListener(
                new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {
                        v.getViewTreeObserver().removeOnComputeInternalInsetsListener(
                                mInsetsComputer);
                        v.getViewTreeObserver().addOnComputeInternalInsetsListener(mInsetsComputer);
                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {
                        v.getViewTreeObserver().removeOnComputeInternalInsetsListener(
                                mInsetsComputer);
                    }
                });
    }

    private boolean isLightTheme(Context context) {
        TypedArray a = context.obtainStyledAttributes(new int[]{R.attr.isLightTheme});
        boolean isLightTheme = a.getBoolean(0, true);
        a.recycle();
        return isLightTheme;
    }

    @UiThread
    @Override
    public void show(List<MenuItem> menuItems,
            MenuItem.OnMenuItemClickListener menuItemClickListener, Rect contentRect) {
        Objects.requireNonNull(menuItems);
        Objects.requireNonNull(menuItemClickListener);

        ViewRootImpl viewRootImpl = mParent.getViewRootImpl();
        if (viewRootImpl == null) {
            Log.e(FloatingToolbar.FLOATING_TOOLBAR_TAG,
                    "RemoteFloatingToolbarPopup.show(): viewRootImpl is null.");
            return;
        }

        boolean isDuplicateRequest = Objects.equals(contentRect, mContentRect)
                && areMenuItemsEqual(menuItems, mMenuItems);
        if (isShowing() && isDuplicateRequest) {
            if (DEBUG) {
                Log.v(FloatingToolbar.FLOATING_TOOLBAR_TAG,
                        "RemoteFloatingToolbarPopup.show(): Ignore duplicate request.");
            }
            mMenuItemClickListener = menuItemClickListener;
            return;
        }
        boolean isLayoutRequired = !isDuplicateRequest || mWidthChanged;
        mWidthChanged = false;
        mMenuItemClickListener = menuItemClickListener;
        mContentRect.set(contentRect);

        Rect screenViewPort = new Rect();
        mParent.getWindowVisibleDisplayFrame(screenViewPort);
        final int suggestedWidth = mSuggestedWidth > 0
                ? mSuggestedWidth
                : mParent.getResources().getDimensionPixelSize(
                        R.dimen.floating_toolbar_preferred_width);
        if (mSession == null) {
            SelectionToolbarRequest request = new SelectionToolbarRequest(
                    isLayoutRequired, getToolbarMenuItems(menuItems), contentRect,
                    suggestedWidth, screenViewPort,
                    mParent.getRootSurfaceControl().getInputTransferToken(), mIsLightTheme,
                    mContext.getResources().getConfiguration());

            if (!mHasSessionOpened) {
                // We only start a trace for the first show() call that this instance sends to the
                // render service to provide the most consistent results for the slowest expected
                // case.
                mHasSessionOpened = true;
                Trace.asyncTraceForTrackBegin(TRACE_TRACK_NAME, "showToolbar", mTraceCookie);
                mSelectionToolbarProvider.openSession(request, mContext.getMainExecutor(), this);
            } else {
                mPendingSessionCall = () -> mSession.update(isLayoutRequired,
                        getToolbarMenuItems(menuItems), contentRect, suggestedWidth);
            }
        } else {
            mSession.update(isLayoutRequired, getToolbarMenuItems(menuItems), contentRect,
                    suggestedWidth);
        }

        mState = TOOLBAR_STATE_SHOWN;
        mMenuItems = menuItems;
        if (DEBUG) {
            Log.v(FloatingToolbar.FLOATING_TOOLBAR_TAG,
                    "RemoteFloatingToolbarPopup2.show() completed, and the state is SHOWN.");
        }
    }

    @UiThread
    @Override
    public void dismiss() {
        if (mState == TOOLBAR_STATE_DISMISSED) {
            Log.w(FloatingToolbar.FLOATING_TOOLBAR_TAG,
                    "The floating toolbar already dismissed.");
            return;
        }
        if (DEBUG) {
            Log.v(FloatingToolbar.FLOATING_TOOLBAR_TAG,
                    "RemoteFloatingToolbarPopup.dismiss().");
        }
        if (mSession == null) {
            mPendingSessionCall = () -> mSession.close();
        } else {
            mSession.close();
        }
        mState = TOOLBAR_STATE_DISMISSED;
    }

    @UiThread
    @Override
    public void hide() {
        if (mState == TOOLBAR_STATE_HIDDEN || mState == TOOLBAR_STATE_DISMISSED) {
            if (DEBUG) {
                Log.v(FloatingToolbar.FLOATING_TOOLBAR_TAG,
                        "The floating toolbar already dismissed/hidden.");
            }
            return;
        }
        if (DEBUG) {
            Log.v(FloatingToolbar.FLOATING_TOOLBAR_TAG,
                    "RemoteFloatingToolbarPopup.hide().");
        }
        if (mSession == null) {
            mPendingSessionCall = () -> mSession.hide();
        } else {
            mSession.hide();
        }
        mState = TOOLBAR_STATE_HIDDEN;
    }

    @UiThread
    @Override
    public void setSuggestedWidth(int suggestedWidth) {
        int difference = Math.abs(suggestedWidth - mSuggestedWidth);
        mWidthChanged = difference > (mSuggestedWidth * 0.2);
        if (DEBUG) {
            Log.v(FloatingToolbar.FLOATING_TOOLBAR_TAG,
                    "setSuggestedWidth: mWidthChanged " + mWidthChanged);
        }
        mSuggestedWidth = suggestedWidth;
    }

    @Override
    public void setWidthChanged(boolean widthChanged) {
        mWidthChanged = widthChanged;
    }

    @UiThread
    @Override
    public boolean isHidden() {
        return mState == TOOLBAR_STATE_HIDDEN;
    }

    @UiThread
    @Override
    public boolean isShowing() {
        return mState == TOOLBAR_STATE_SHOWN;
    }

    @UiThread
    @Override
    public boolean setOutsideTouchable(boolean outsideTouchable,
            @Nullable PopupWindow.OnDismissListener onDismiss) {
        if (mState == TOOLBAR_STATE_DISMISSED) {
            return false;
        }
        boolean ret = false;
        if (mPopupWindow.isOutsideTouchable() ^ outsideTouchable) {
            mPopupWindow.setOutsideTouchable(outsideTouchable);
            mPopupWindow.setFocusable(!outsideTouchable);
            mPopupWindow.update();
            ret = true;
        }
        mPopupWindow.setOnDismissListener(onDismiss);
        return ret;
    }

    private void updateSurfaceView(SurfaceControlViewHost.SurfacePackage surfacePackage) {
        ViewGroup contentContainer = (ViewGroup) mPopupWindow.getContentView();
        contentContainer.removeAllViews();
        SurfaceView surfaceView = new SurfaceView(mParent.getContext());
        surfaceView.setZOrderOnTop(true);
        surfaceView.getHolder().setFormat(PixelFormat.TRANSPARENT);
        surfaceView.setChildSurfacePackage(surfacePackage);
        contentContainer.addView(surfaceView);
    }

    private void updateTouchableRegion(Rect contentRect, Region touchableRegion) {
        mTouchableRegion.set(touchableRegion);
        mTouchableRegion.translate(-contentRect.left, -contentRect.top);
        mPopupWindow.getContentView().invalidate();
    }

    private Point getCoordinatesInWindow(int x, int y) {
        // We later specify the location of PopupWindow relative to the attached window.
        // The idea here is that 1) we can get the location of a View in both window coordinates
        // and screen coordinates, where the offset between them should be equal to the window
        // origin, and 2) we can use an arbitrary for this calculation while calculating the
        // location of the rootview is supposed to be least expensive.
        // TODO: Consider to use PopupWindow.setIsLaidOutInScreen(true) so that we can avoid
        // the following calculation.
        mParent.getRootView().getLocationOnScreen(mCoordsOnScreen);
        mParent.getRootView().getLocationInWindow(mCoordsOnWindow);
        int windowLeftOnScreen = mCoordsOnScreen[0] - mCoordsOnWindow[0];
        int windowTopOnScreen = mCoordsOnScreen[1] - mCoordsOnWindow[1];
        // In some cases, app can have specific Window for Android UI components such as EditText.
        // In this case, Window bounds != App bounds. Hence, instead of ensuring non-negative
        // PopupWindow coords, app bounds should be used to limit the coords. For instance,
        //  ____  <- |
        // |   |     |W1 & App bounds
        // |___|    |
        // |W2 |    | W2 has smaller bounds and contain EditText where PopupWindow will be opened.
        // ----  <-|
        // Here, we'll open PopupWindow upwards, but as PopupWindow is anchored based on W2, it
        // will have negative Y coords. This negative Y is safe to use because it's still within app
        // bounds. However, if it gets out of app bounds, we should clamp it to 0.
        Rect appBounds = mContext
                .getResources().getConfiguration().windowConfiguration.getAppBounds();
        Point coordsInWindow = new Point(x - windowLeftOnScreen, y - windowTopOnScreen);
        if (mCoordsOnScreen[0] + coordsInWindow.x < appBounds.left) {
            coordsInWindow.x = 0;
        }
        if (mCoordsOnScreen[1] + coordsInWindow.y < appBounds.top) {
            coordsInWindow.y = 0;
        }
        return coordsInWindow;
    }

    private static List<ToolbarMenuItem> getToolbarMenuItems(List<MenuItem> menuItems) {
        final List<ToolbarMenuItem> list = new ArrayList<>(menuItems.size());
        for (int i = 0; i < menuItems.size(); i++) {
            MenuItem menuItem = menuItems.get(i);
            ToolbarMenuItem toolbarMenuItem = new ToolbarMenuItem();
            toolbarMenuItem.itemId = menuItem.getItemId();
            toolbarMenuItem.itemIndex = i;
            toolbarMenuItem.title = menuItem.getTitle();
            toolbarMenuItem.contentDescription = menuItem.getContentDescription();
            toolbarMenuItem.groupId = menuItem.getGroupId();
            toolbarMenuItem.icon = convertDrawableToIcon(menuItem.getIcon());
            toolbarMenuItem.tooltipText = menuItem.getTooltipText();
            toolbarMenuItem.priority = getPriorityFromMenuItem(menuItem);

            list.add(toolbarMenuItem);
        }
        return list;
    }

    private static Icon convertDrawableToIcon(Drawable drawable) {
        if (drawable == null) {
            return null;
        }
        if (drawable instanceof BitmapDrawable) {
            final BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return Icon.createWithBitmap(bitmapDrawable.getBitmap());
            }
        }
        final Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return Icon.createWithBitmap(bitmap);
    }

    /**
     * Returns the priority from a given {@link MenuItem}.
     */
    private static int getPriorityFromMenuItem(MenuItem menuItem) {
        if (menuItem.requiresActionButton()) {
            return PRIORITY_PRIMARY;
        } else if (menuItem.requiresOverflow()) {
            return PRIORITY_OVERFLOW;
        }
        return PRIORITY_UNKNOWN;
    }

    private static PopupWindow createPopupWindow(Context context) {
        ViewGroup popupContentHolder = new LinearLayout(context);
        PopupWindow popupWindow = new PopupWindow(popupContentHolder);
        popupWindow.setClippingEnabled(false);
        popupWindow.setWindowLayoutType(
                WindowManager.LayoutParams.TYPE_APPLICATION_ABOVE_SUB_PANEL);
        popupWindow.setAnimationStyle(0);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return popupWindow;
    }

    @UiThread
    @Override
    public void onSessionOpened(@NonNull SelectionToolbarSession session) {
        mSession = session;
        if (mPendingSessionCall != null) {
            mPendingSessionCall.run();
            mPendingSessionCall = null;
        }
    }

    @UiThread
    @Override
    public void onSessionError(@NonNull Throwable cause) {
        Log.e(FloatingToolbar.FLOATING_TOOLBAR_TAG, "onSessionError", cause);
        mSession = null;
        mPendingSessionCall = null;
    }

    @UiThread
    @Override
    public void onUpdated(@NonNull SurfaceControlViewHost.SurfacePackage surfacePackage,
            @NonNull Rect contentRect, @NonNull Region touchableRegion) {
        if (!isShowing()) {
            Log.w(FloatingToolbar.FLOATING_TOOLBAR_TAG,
                    "onUpdated callback: The widget isn't showing.");
            return;
        }
        updateTouchableRegion(contentRect, touchableRegion);
        final Point coords = getCoordinatesInWindow(contentRect.left, contentRect.top);
        if (!mPopupWindow.isShowing()) {
            updateSurfaceView(surfacePackage);

            if (!mHasShowToolbarBeenTraced) {
                Trace.asyncTraceForTrackEnd(TRACE_TRACK_NAME, mTraceCookie);
                mHasShowToolbarBeenTraced = true;
            }

            mPopupWindow.showAtLocation(mParent, Gravity.NO_GRAVITY, coords.x, coords.y);
            WindowManager.LayoutParams layoutParams = (WindowManager.LayoutParams)
                    mPopupWindow.getContentView().getRootView().getLayoutParams();
            layoutParams.setCanPlayMoveAnimation(false);
        }
        mPopupWindow.update(coords.x, coords.y, contentRect.width(), contentRect.height());
    }

    @UiThread
    @Override
    public void onVisibilityChanged(boolean visible) {
        if (!visible) {
            if (DEBUG) {
                Log.v(FloatingToolbar.FLOATING_TOOLBAR_TAG,
                        "onInvisible callback: The widget is no longer shown.");
            }
            mWidthChanged = true;
            mPopupWindow.dismiss();
        }
    }

    @UiThread
    @Override
    public void onMenuItemClicked(int itemIndex) {
        if (mMenuItems == null || mMenuItemClickListener == null) {
            return;
        }
        if (itemIndex > mMenuItems.size()) {
            Log.w(FloatingToolbar.FLOATING_TOOLBAR_TAG, "onMenuItemClicked callback: "
                    + "itemIndex=" + itemIndex + " out of bounds of known menu items. Menu may have"
                    + " recently updated.");
            return;
        }
        MenuItem item = mMenuItems.get(itemIndex);
        if (DEBUG) {
            Log.v(FloatingToolbar.FLOATING_TOOLBAR_TAG,
                    "onMenuItemClicked callback: itemIndex="
                            + itemIndex + " item=" + item);
        }
        // TODO: handle the menu item like clipboard
        if (item != null) {
            mMenuItemClickListener.onMenuItemClick(item);
        } else {
            Log.e(FloatingToolbar.FLOATING_TOOLBAR_TAG,
                    "onMenuItemClicked callback: cannot find menu item.");
        }
    }

    /**
     * Returns true if the two menu item collections consist of equal items in the same order.
     */
    public static boolean areMenuItemsEqual(
            Collection<MenuItem> menuItems1, Collection<MenuItem> menuItems2) {
        if (menuItems1 == menuItems2) {
            return true;
        }
        if (menuItems1 == null || menuItems2 == null) {
            return false;
        }
        if (menuItems1.size() != menuItems2.size()) {
            return false;
        }

        final Iterator<MenuItem> menuItems2Iter = menuItems2.iterator();
        for (MenuItem menuItem1 : menuItems1) {
            final MenuItem menuItem2 = menuItems2Iter.next();
            if (menuItem1.getItemId() != menuItem2.getItemId()
                    || menuItem1.getGroupId() != menuItem2.getGroupId()
                    || !TextUtils.equals(menuItem1.getTitle(), menuItem2.getTitle())
                    || !Objects.equals(menuItem1.getIcon(), menuItem2.getIcon())) {
                return false;
            }
        }
        return true;
    }
}
