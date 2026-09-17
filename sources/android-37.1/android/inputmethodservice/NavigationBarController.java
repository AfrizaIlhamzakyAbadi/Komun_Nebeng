/*
 * Copyright (C) 2022 The Android Open Source Project
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

package android.inputmethodservice;

import static android.app.StatusBarManager.NAVBAR_BACK_DISMISS_IME;
import static android.app.StatusBarManager.NAVBAR_IME_SWITCHER_BUTTON_VISIBLE;
import static android.app.StatusBarManager.NAVBAR_IME_VISIBLE;
import static android.view.WindowInsets.Type.captionBar;
import static android.view.WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS;

import android.animation.ValueAnimator;
import android.annotation.FloatRange;
import android.annotation.Hide;
import android.annotation.NonNull;
import android.annotation.Nullable;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.Rect;
import android.graphics.Region;
import android.inputmethodservice.navigationbar.NavigationBarFrame;
import android.inputmethodservice.navigationbar.NavigationBarView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowInsetsController.Appearance;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.inputmethod.InputMethodNavButtonFlags;

import java.util.Objects;

/**
 * The IME navigation bar, used when {@code config_imeDrawsImeNavBar} is set. This does not replace,
 * but rather complements the system navigation bar.
 */
@Hide
@VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
public final class NavigationBarController implements Window.DecorCallback,
        NavigationBarView.ButtonClickListener {

    private static final int DEFAULT_COLOR_ADAPT_TRANSITION_TIME = 1700;

    // Copied from com.android.systemui.animation.Interpolators#LEGACY_DECELERATE
    private static final Interpolator LEGACY_DECELERATE = new PathInterpolator(0f, 0f, 0.2f, 1f);

    @NonNull
    private final Delegate mDelegate;

    private boolean mDestroyed = false;

    /** Whether the IME navigation bar should be drawn. */
    private boolean mImeDrawsImeNavBar;

    @Nullable
    private NavigationBarFrame mNavigationBarFrame;
    @Nullable
    private Insets mLastInsets;

    /**
     * Whether the IME Switcher button should be visible. Depending on the current state, it may
     * be shown either by the IME Navigation Bar, or by the IME itself, as a custom button.
     */
    private boolean mShowImeSwitcherButton;

    /**
     *  Whether the IME Switcher button could be shown on the IME Navigation bar.
     */
    private boolean mImeSwitcherButtonEnabled;

    /** Whether a custom IME Switcher button should be visible. */
    private boolean mCustomImeSwitcherButtonRequestedVisible;

    @Appearance
    private int mAppearance;

    @FloatRange(from = 0.0f, to = 1.0f)
    private float mDarkIntensity;

    @Nullable
    private ValueAnimator mTintAnimator;

    private boolean mDrawLegacyNavigationBarBackground;

    private final Rect mTempRect = new Rect();
    private final int[] mTempPos = new int[2];

    @Hide
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public NavigationBarController(@NonNull Delegate delegate) {
        mDelegate = delegate;
    }

    /**
     * Interface to be implemented by the owner of the {@link NavigationBarController} to
     * provide access to the IME window and receive callbacks for navigation bar events.
     */
    @Hide
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public interface Delegate {
        /**
         * Returns the {@link Context} of the delegate.
         */
        @NonNull
        Context getContext();

        /**
         * Returns the {@link Resources} of the delegate.
         */
        @NonNull
        Resources getResources();

        /**
         * Returns the {@link Window} of the IME.
         */
        @Nullable
        Window getWindow();

        /**
         * Returns the root view of the IME input area.
         */
        @Nullable
        View getInputFrame();

        /**
         * Returns whether the IME is in fullscreen mode.
         */
        boolean isFullscreenMode();

        /**
         * Returns whether the extract view (used in landscape fullscreen) is currently shown.
         */
        boolean isExtractViewShown();

        /**
         * Callback when the IME switcher button in the navigation bar is clicked.
         */
        void onImeSwitchButtonClickFromClient();

        /**
         * Callback to notify the delegate whether a custom IME switcher button should be
         * shown inside the IME's input view.
         *
         * @param visible true if the button should be visible.
         */
        void onCustomImeSwitcherButtonRequestedVisible(boolean visible);
    }

    @Nullable
    private Insets getSystemInsets() {
        final Window window = mDelegate.getWindow();
        if (window == null) {
            return null;
        }
        final View decorView = window.getDecorView();
        if (decorView == null) {
            return null;
        }
        final WindowInsets windowInsets = decorView.getRootWindowInsets();
        if (windowInsets == null) {
            return null;
        }
        final Insets stableBarInsets =
                windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
        return Insets.min(windowInsets.getInsets(WindowInsets.Type.systemBars()
                | WindowInsets.Type.displayCutout()), stableBarInsets);
    }

    private void installNavigationBarFrameIfNecessary() {
        if (!mImeDrawsImeNavBar) {
            return;
        }
        if (mNavigationBarFrame != null) {
            return;
        }
        final Window window = mDelegate.getWindow();
        if (window == null) {
            return;
        }
        final View rawDecorView = window.getDecorView();
        if (!(rawDecorView instanceof ViewGroup)) {
            return;
        }
        final ViewGroup decorView = (ViewGroup) rawDecorView;
        mNavigationBarFrame = decorView.findViewByPredicate(
                NavigationBarFrame.class::isInstance);
        final Insets systemInsets = getSystemInsets();
        if (mNavigationBarFrame == null) {
            mNavigationBarFrame = new NavigationBarFrame(mDelegate.getContext());
            LayoutInflater.from(mDelegate.getContext()).inflate(
                    com.android.internal.R.layout.input_method_navigation_bar,
                    mNavigationBarFrame);
            if (systemInsets != null) {
                decorView.addView(mNavigationBarFrame, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        systemInsets.bottom, Gravity.BOTTOM));
                mLastInsets = systemInsets;
            } else {
                // If systemInsets are null, the DecorView is not attached to the window yet.
                // Use the final captionBar height as the initial one, otherwise it resolves to
                // match parent, and can lead to full size IME insets.
                final int height = getImeCaptionBarHeight(true /* imeDrawsImeNavBar */);
                decorView.addView(mNavigationBarFrame, new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, height, Gravity.BOTTOM));
            }
            final NavigationBarView navigationBarView = mNavigationBarFrame.findViewByPredicate(
                    NavigationBarView.class::isInstance);
            if (navigationBarView != null) {
                // TODO(b/213337792): Support InputMethodService#setBackDisposition().
                // TODO(b/213337792): Set NAVBAR_IME_VISIBLE only when necessary.
                final int flags = NAVBAR_BACK_DISMISS_IME | NAVBAR_IME_VISIBLE | (
                        showImeSwitcherButtonInNavBar() ? NAVBAR_IME_SWITCHER_BUTTON_VISIBLE : 0);
                navigationBarView.setNavbarFlags(flags);
                navigationBarView.prepareNavButtons(this);
            }
        } else {
            mNavigationBarFrame.setLayoutParams(new FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, systemInsets.bottom, Gravity.BOTTOM));
            mLastInsets = systemInsets;
        }

        if (mDrawLegacyNavigationBarBackground) {
            mNavigationBarFrame.setBackgroundColor(Color.BLACK);
        } else {
            mNavigationBarFrame.setBackground(null);
        }

        setIconTintInternal(calculateTargetDarkIntensity(mAppearance,
                mDrawLegacyNavigationBarBackground));

        mNavigationBarFrame.setOnApplyWindowInsetsListener((view, insets) -> {
            if (mNavigationBarFrame != null) {
                // The IME window receives IME-specific captionBar insets, representing the
                // IME navigation bar.
                boolean visible = insets.isVisible(captionBar());
                mNavigationBarFrame.setVisibility(visible ? View.VISIBLE : View.GONE);
                notifyCustomImeSwitcherButtonRequestedVisible();
            }
            return view.onApplyWindowInsets(insets);
        });
    }

    private void uninstallNavigationBarFrameIfNecessary() {
        if (mNavigationBarFrame == null) {
            return;
        }
        final ViewParent parent = mNavigationBarFrame.getParent();
        if (parent instanceof ViewGroup) {
            ((ViewGroup) parent).removeView(mNavigationBarFrame);
        }
        mNavigationBarFrame.setOnApplyWindowInsetsListener(null);
        mNavigationBarFrame = null;
    }

    /**
     * Update the given insets to be at least as big as the IME navigation bar, when visible.
     *
     * @param originalInsets the insets to check and modify to include the IME navigation bar.
     */
    void updateInsets(@NonNull InputMethodService.Insets originalInsets) {
        if (!mImeDrawsImeNavBar || mNavigationBarFrame == null
                || mNavigationBarFrame.getVisibility() != View.VISIBLE
                || mDelegate.isFullscreenMode()) {
            return;
        }

        final int[] loc = new int[2];
        mNavigationBarFrame.getLocationInWindow(loc);
        if (originalInsets.contentTopInsets > loc[1]) {
            originalInsets.contentTopInsets = loc[1];
        }
        if (originalInsets.visibleTopInsets > loc[1]) {
            originalInsets.visibleTopInsets = loc[1];
        }
    }

    void updateTouchableInsets(@NonNull InputMethodService.Insets originalInsets,
            @NonNull ViewTreeObserver.InternalInsetsInfo dest) {
        if (!mImeDrawsImeNavBar || mNavigationBarFrame == null) {
            return;
        }

        final Insets systemInsets = getSystemInsets();
        if (systemInsets != null) {
            final Window window = mDelegate.getWindow();
            if (window == null) {
                return;
            }
            final View decor = window.getDecorView();

            // If the extract view is shown, everything is touchable, so no need to update
            // touchable insets, but we still update normal insets below.
            if (!mDelegate.isExtractViewShown()) {
                Region touchableRegion = null;
                final View inputFrame = mDelegate.getInputFrame();
                switch (originalInsets.touchableInsets) {
                    case ViewTreeObserver.InternalInsetsInfo.TOUCHABLE_INSETS_FRAME:
                        if (inputFrame != null && inputFrame.getVisibility() == View.VISIBLE) {
                            inputFrame.getLocationInWindow(mTempPos);
                            mTempRect.set(mTempPos[0], mTempPos[1],
                                    mTempPos[0] + inputFrame.getWidth(),
                                    mTempPos[1] + inputFrame.getHeight());
                            touchableRegion = new Region(mTempRect);
                        }
                        break;
                    case ViewTreeObserver.InternalInsetsInfo.TOUCHABLE_INSETS_CONTENT:
                        if (inputFrame != null && inputFrame.getVisibility() == View.VISIBLE) {
                            inputFrame.getLocationInWindow(mTempPos);
                            mTempRect.set(mTempPos[0], originalInsets.contentTopInsets,
                                    mTempPos[0] + inputFrame.getWidth(),
                                    mTempPos[1] + inputFrame.getHeight());
                            touchableRegion = new Region(mTempRect);
                        }
                        break;
                    case ViewTreeObserver.InternalInsetsInfo.TOUCHABLE_INSETS_VISIBLE:
                        if (inputFrame != null && inputFrame.getVisibility() == View.VISIBLE) {
                            inputFrame.getLocationInWindow(mTempPos);
                            mTempRect.set(mTempPos[0], originalInsets.visibleTopInsets,
                                    mTempPos[0] + inputFrame.getWidth(),
                                    mTempPos[1] + inputFrame.getHeight());
                            touchableRegion = new Region(mTempRect);
                        }
                        break;
                    case ViewTreeObserver.InternalInsetsInfo.TOUCHABLE_INSETS_REGION:
                        touchableRegion = new Region();
                        touchableRegion.set(originalInsets.touchableRegion);
                        break;
                }
                // Hereafter "mTempRect" means a navigation bar rect.
                mTempRect.set(decor.getLeft(), decor.getBottom() - systemInsets.bottom,
                        decor.getRight(), decor.getBottom());
                if (touchableRegion == null) {
                    touchableRegion = new Region(mTempRect);
                } else {
                    touchableRegion.union(mTempRect);
                }

                dest.touchableRegion.set(touchableRegion);
                dest.setTouchableInsets(
                        ViewTreeObserver.InternalInsetsInfo.TOUCHABLE_INSETS_REGION);
            }

            // TODO(b/215443343): See if we can use View#OnLayoutChangeListener().
            // TODO(b/215443343): See if we can replace DecorView#mNavigationColorViewState.view
            boolean zOrderChanged = false;
            if (decor instanceof ViewGroup) {
                ViewGroup decorGroup = (ViewGroup) decor;
                final View navbarBackgroundView = window.getNavigationBarBackgroundView();
                zOrderChanged = navbarBackgroundView != null
                        && decorGroup.indexOfChild(navbarBackgroundView)
                        > decorGroup.indexOfChild(mNavigationBarFrame);
            }
            final boolean insetChanged = !Objects.equals(systemInsets, mLastInsets);
            if (zOrderChanged || insetChanged) {
                scheduleRelayout();
            }
        }
    }

    private void scheduleRelayout() {
        // Capture the current frame object in case the object is replaced or cleared later.
        final NavigationBarFrame frame = mNavigationBarFrame;
        frame.post(() -> {
            if (mDestroyed) {
                return;
            }
            if (!frame.isAttachedToWindow()) {
                return;
            }
            final Window window = mDelegate.getWindow();
            if (window == null) {
                return;
            }
            final View decor = window.peekDecorView();
            if (decor == null) {
                return;
            }
            if (!(decor instanceof ViewGroup)) {
                return;
            }
            final ViewGroup decorGroup = (ViewGroup) decor;
            final Insets currentSystemInsets = getSystemInsets();
            if (!Objects.equals(currentSystemInsets, mLastInsets)) {
                frame.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        currentSystemInsets.bottom, Gravity.BOTTOM));
                mLastInsets = currentSystemInsets;
            }
            final View navbarBackgroundView =
                    window.getNavigationBarBackgroundView();
            if (navbarBackgroundView != null
                    && decorGroup.indexOfChild(navbarBackgroundView)
                    > decorGroup.indexOfChild(frame)) {
                decorGroup.bringChildToFront(frame);
            }
        });
    }

    void onWindowCreated(@NonNull Window window) {
        mAppearance = window.getSystemBarAppearance();
        window.setDecorCallback(this);
    }

    void onViewInitialized() {
        if (mDestroyed) {
            return;
        }
        installNavigationBarFrameIfNecessary();
    }

    void onDestroy() {
        if (mDestroyed) {
            return;
        }
        if (mTintAnimator != null) {
            mTintAnimator.cancel();
            mTintAnimator = null;
        }
        mDestroyed = true;
    }

    void onWindowShown() {
        if (mDestroyed || !mImeDrawsImeNavBar || mNavigationBarFrame == null) {
            return;
        }
        final Insets systemInsets = getSystemInsets();
        if (systemInsets != null) {
            if (!Objects.equals(systemInsets, mLastInsets)) {
                mNavigationBarFrame.setLayoutParams(new FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        systemInsets.bottom, Gravity.BOTTOM));
                mLastInsets = systemInsets;
            }
            final Window window = mDelegate.getWindow();
            if (window == null) {
                return;
            }
            View rawDecorView = window.getDecorView();
            if (rawDecorView instanceof ViewGroup) {
                final ViewGroup decor = (ViewGroup) rawDecorView;
                final View navbarBackgroundView = window.getNavigationBarBackgroundView();
                if (navbarBackgroundView != null
                        && decor.indexOfChild(navbarBackgroundView)
                        > decor.indexOfChild(mNavigationBarFrame)) {
                    decor.bringChildToFront(mNavigationBarFrame);
                }
            }
        }
    }

    /**
     * Whether the IME Switcher button should be displayed in the IME navigation bar. This is {true}
     * only when requested visible explicitly in the navigation bar.
     */
    private boolean showImeSwitcherButtonInNavBar() {
        return mImeSwitcherButtonEnabled && mShowImeSwitcherButton;
    }

    @Hide
    public void onNavButtonFlagsChanged(@InputMethodNavButtonFlags int navButtonFlags) {
        if (mDestroyed) {
            return;
        }

        final boolean prevShowImeSwitcherButtonInNavBar = showImeSwitcherButtonInNavBar();
        mImeDrawsImeNavBar =
                (navButtonFlags & InputMethodNavButtonFlags.IME_DRAWS_IME_NAV_BAR) != 0;
        mShowImeSwitcherButton =
                (navButtonFlags & InputMethodNavButtonFlags.SHOW_IME_SWITCHER_BUTTON) != 0;
        mImeSwitcherButtonEnabled =
                (navButtonFlags & InputMethodNavButtonFlags.IME_SWITCHER_BUTTON_ENABLED) != 0;

        final Window window = mDelegate.getWindow();
        if (window != null) {
            window.getDecorView().getWindowInsetsController()
                    .setImeCaptionBarInsetsHeight(getImeCaptionBarHeight(mImeDrawsImeNavBar));
        }

        if (mImeDrawsImeNavBar) {
            installNavigationBarFrameIfNecessary();
            if (mNavigationBarFrame != null
                    && showImeSwitcherButtonInNavBar() != prevShowImeSwitcherButtonInNavBar) {
                final NavigationBarView navigationBarView = mNavigationBarFrame
                        .findViewByPredicate(NavigationBarView.class::isInstance);
                if (navigationBarView != null) {
                    // TODO(b/213337792): Support InputMethodService#setBackDisposition().
                    // TODO(b/213337792): Set NAVBAR_IME_VISIBLE only when necessary.
                    final int flags = NAVBAR_BACK_DISMISS_IME | NAVBAR_IME_VISIBLE | (
                            showImeSwitcherButtonInNavBar() ? NAVBAR_IME_SWITCHER_BUTTON_VISIBLE
                                    : 0);
                    navigationBarView.setNavbarFlags(flags);
                }
            }
        } else {
            uninstallNavigationBarFrameIfNecessary();
        }

        // Check custom IME Switcher button visibility after (un)installing nav bar frame.
        notifyCustomImeSwitcherButtonRequestedVisible();
    }

    @Override
    public void onSystemBarAppearanceChanged(@Appearance int appearance) {
        if (mDestroyed) {
            return;
        }

        final int prevAppearance = mAppearance;
        mAppearance = appearance;

        if (mNavigationBarFrame == null) {
            return;
        }

        // Cancels any running animation if a new one is gonna be started.
        if (mTintAnimator != null) {
            mTintAnimator.cancel();
        }

        final float targetDarkIntensity = calculateTargetDarkIntensity(mAppearance,
                mDrawLegacyNavigationBarBackground);
        if (prevAppearance == 0) {
            // Skip animation for the first appearance update after creating the window.
            setIconTintInternal(targetDarkIntensity);
            return;
        }

        mTintAnimator = ValueAnimator.ofFloat(mDarkIntensity, targetDarkIntensity);
        mTintAnimator.addUpdateListener(
                animation -> setIconTintInternal((Float) animation.getAnimatedValue()));
        mTintAnimator.setDuration(DEFAULT_COLOR_ADAPT_TRANSITION_TIME);
        mTintAnimator.setStartDelay(0);
        mTintAnimator.setInterpolator(LEGACY_DECELERATE);
        mTintAnimator.start();
    }

    private void setIconTintInternal(float darkIntensity) {
        mDarkIntensity = darkIntensity;
        if (mNavigationBarFrame == null) {
            return;
        }
        final NavigationBarView navigationBarView =
                mNavigationBarFrame.findViewByPredicate(NavigationBarView.class::isInstance);
        if (navigationBarView == null) {
            return;
        }
        navigationBarView.setDarkIntensity(darkIntensity);
    }

    @FloatRange(from = 0.0f, to = 1.0f)
    private static float calculateTargetDarkIntensity(@Appearance int appearance,
            boolean drawLegacyNavigationBarBackground) {
        final boolean lightNavBar = !drawLegacyNavigationBarBackground
                && (appearance & APPEARANCE_LIGHT_NAVIGATION_BARS) != 0;
        return lightNavBar ? 1.0f : 0.0f;
    }

    @Override
    public boolean onDrawLegacyNavigationBarBackgroundChanged(
            boolean drawLegacyNavigationBarBackground) {
        if (mDestroyed) {
            return false;
        }

        if (drawLegacyNavigationBarBackground != mDrawLegacyNavigationBarBackground) {
            mDrawLegacyNavigationBarBackground = drawLegacyNavigationBarBackground;
            if (mNavigationBarFrame != null) {
                if (mDrawLegacyNavigationBarBackground) {
                    mNavigationBarFrame.setBackgroundColor(Color.BLACK);
                } else {
                    mNavigationBarFrame.setBackground(null);
                }
                scheduleRelayout();
            }
            onSystemBarAppearanceChanged(mAppearance);
        }
        return drawLegacyNavigationBarBackground;
    }

    @Override
    public void onImeSwitchButtonClick(View v) {
        mDelegate.onImeSwitchButtonClickFromClient();
    }

    @Override
    public boolean onImeSwitchButtonLongClick(View v) {
        v.getContext().getSystemService(InputMethodManager.class).showInputMethodPicker();
        return true;
    }

    /**
     * Returns the height of the IME caption bar if this should be shown, or {@code 0} instead.
     *
     * @param imeDrawsImeNavBar whether the IME should show the IME navigation bar.
     */
    private int getImeCaptionBarHeight(boolean imeDrawsImeNavBar) {
        return imeDrawsImeNavBar
                ? mDelegate.getResources().getDimensionPixelSize(
                        com.android.internal.R.dimen.input_method_navigation_bar_height)
                : 0;
    }

    /**
     * Returns whether the IME navigation bar is currently shown.
     */
    boolean isShown() {
        return mNavigationBarFrame != null && mNavigationBarFrame.getVisibility() == View.VISIBLE;
    }

    /**
     * Calculates and updates the requested visibility of the custom IME switcher button inside
     * IME's input view. This ensures switcher button visibility even when it cannot be rendered
     * within the IME navigation bar due to device configurations or user settings.
     */
    private void notifyCustomImeSwitcherButtonRequestedVisible() {
        // The system nav bar will be hidden when the IME is shown and the config is set.
        final boolean navBarVisible =
                mImeDrawsImeNavBar ? isShown() : !mDelegate.getResources().getBoolean(
                        com.android.internal.R.bool.config_hideNavBarForKeyboard);

        final boolean visible =
                mShowImeSwitcherButton && (!navBarVisible || !mImeSwitcherButtonEnabled);

        if (visible != mCustomImeSwitcherButtonRequestedVisible) {
            mCustomImeSwitcherButtonRequestedVisible = visible;
            mDelegate.onCustomImeSwitcherButtonRequestedVisible(visible);
        }
    }

    /**
     * Sets the navigation bar frame.
     *
     * <p>This method should only ever be used in tests.</p>
     *
     * @param frame the navigation bar frame to set
     */
    @Hide
    @VisibleForTesting(visibility = VisibleForTesting.Visibility.PACKAGE)
    public void setNavigationBarFrameForTesting(NavigationBarFrame frame) {
        mNavigationBarFrame = frame;
    }

    String toDebugString() {
        return "{mImeDrawsImeNavBar=" + mImeDrawsImeNavBar
                + " mNavigationBarFrame=" + mNavigationBarFrame
                + " mShowImeSwitcherButton=" + mShowImeSwitcherButton
                + " mImeSwitcherButtonEnabled=" + mImeSwitcherButtonEnabled
                + " mCustomImeSwitcherButtonRequestedVisible="
                + mCustomImeSwitcherButtonRequestedVisible
                + " mAppearance=0x" + Integer.toHexString(mAppearance)
                + " mDarkIntensity=" + mDarkIntensity
                + " mDrawLegacyNavigationBarBackground=" + mDrawLegacyNavigationBarBackground
                + "}";
    }
}
