/*
 * Copyright (C) 2007 The Android Open Source Project
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

package android.view.animation;

import static android.view.flags.Flags.noMoreResync;

import android.annotation.AnimRes;
import android.annotation.Hide;
import android.annotation.InterpolatorRes;
import android.annotation.TestApi;
import android.compat.annotation.UnsupportedAppUsage;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.content.res.Resources.Theme;
import android.content.res.XmlResourceParser;
import android.os.SystemClock;
import android.os.Trace;
import android.ravenwood.annotation.RavenwoodIgnore;
import android.ravenwood.annotation.RavenwoodKeepWholeClass;
import android.ravenwood.annotation.RavenwoodReplace;
import android.util.AttributeSet;
import android.util.TimeUtils;
import android.util.Xml;
import android.view.InflateException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Defines common utilities for working with animations.
 *
 */
@RavenwoodKeepWholeClass
public class AnimationUtils {

    /**
     * These flags are used when parsing AnimatorSet objects
     */
    private static final int TOGETHER = 0;
    private static final int SEQUENTIALLY = 1;

    private static class AnimationState {
        boolean animationClockLocked;
        long currentVsyncTimeMillis;
        long lastReportedTimeMillis;
    };

    private static ThreadLocal<AnimationState> sAnimationState
            = new ThreadLocal<AnimationState>() {
        @Override
        protected AnimationState initialValue() {
            return new AnimationState();
        }
    };

    private static ThreadLocal<AnimationTime> sAnimationTime = new ThreadLocal<AnimationTime>() {
        @Override
        protected AnimationTime initialValue() {
            return new AnimationTime();
        }
    };

    /**
     * Locks AnimationUtils{@link #currentAnimationTimeMillis()} to a fixed value for the current
     * thread. This is used by {@link android.view.Choreographer} to ensure that all accesses
     * during a vsync update are synchronized to the timestamp of the vsync.
     *
     * It is also exposed to tests to allow for rapid, flake-free headless testing.
     *
     * Must be followed by a call to {@link #unlockAnimationClock()} to allow time to
     * progress. Failing to do this will result in stuck animations, scrolls, and flings.
     *
     * Note that time is not allowed to "rewind" and must perpetually flow forward. So the
     * lock may fail if the time is in the past from a previously returned value, however
     * time will be frozen for the duration of the lock. The clock is a thread-local, so
     * ensure that {@link #lockAnimationClock(long)}, {@link #unlockAnimationClock()}, and
     * {@link #currentAnimationTimeMillis()} are all called on the same thread.
     *
     * This is also not reference counted in any way. Any call to {@link #unlockAnimationClock()}
     * will unlock the clock for everyone on the same thread. It is therefore recommended
     * for tests to use their own thread to ensure that there is no collision with any existing
     * {@link android.view.Choreographer} instance.
     *
     * Have to add the method back because of b/307888459.
     * Remove this method once the lockAnimationClock(long, long) change
     * is landed to aosp/android14-tests-dev branch.
     */
    @TestApi
    @RavenwoodIgnore
    public static void lockAnimationClock(long vsyncNanos, long frameIntervalNanos) {
        AnimationState state = sAnimationState.get();
        state.animationClockLocked = true;
        state.currentVsyncTimeMillis = vsyncNanos / TimeUtils.NANOS_PER_MS;

        if (noMoreResync()) {
            sAnimationTime.get().tick(vsyncNanos, frameIntervalNanos);
        }
    }

    /**
     * Frees the time lock set in place by {@link #lockAnimationClock(long)}. Must be called
     * to allow the animation clock to self-update.
     */
    @TestApi
    @RavenwoodIgnore
    public static void unlockAnimationClock() {
        sAnimationState.get().animationClockLocked = false;

        if (noMoreResync()) {
            sAnimationTime.get().onCallbacksCompleted();
        }

    }

    /**
     * Gives direct access to the UI thread's animation time thread local. If using this API, be
     * sure to follow the threading contract as highlighted in {@link AnimationTime AnimationTime's}
     * methods.
     */
    @Hide
    @RavenwoodReplace
    public static AnimationTime getAnimationTime() {
        return sAnimationTime.get();
    }

    private static AnimationTime getAnimationTime$ravenwood() {
        return new AnimationTime();
    }

    /**
     * Returns the current animation time in milliseconds. This time should be used when invoking
     * {@link Animation#setStartTime(long)}. Refer to {@link android.os.SystemClock} for more
     * information about the different available clocks. The clock used by this method is
     * <em>not</em> the "wall" clock (it is not {@link System#currentTimeMillis}).
     *
     * @return the current animation time in milliseconds
     *
     * @see android.os.SystemClock
     */
    public static long currentAnimationTimeMillis() {
        if (noMoreResync()) {
            return sAnimationTime.get().getCurrent();
        }

        AnimationState state = sAnimationState.get();
        if (state.animationClockLocked) {
            // It's important that time never rewinds
            return Math.max(state.currentVsyncTimeMillis,
                    state.lastReportedTimeMillis);
        }
        state.lastReportedTimeMillis = SystemClock.uptimeMillis();
        return state.lastReportedTimeMillis;
    }

    /**
     * Loads an {@link Animation} object from a resource
     *
     * @param context Application context used to access resources
     * @param id The resource id of the animation to load
     * @return The animation object referenced by the specified id
     * @throws NotFoundException when the animation cannot be loaded
     */
    public static Animation loadAnimation(Context context, @AnimRes int id)
            throws NotFoundException {

        XmlResourceParser parser = null;
        try {
            parser = context.getResources().getAnimation(id);
            return createAnimationFromXml(context, parser);
        } catch (XmlPullParserException | IOException ex) {
            throw new NotFoundException(
                    "Can't load animation resource ID #0x" + Integer.toHexString(id), ex);
        } finally {
            if (parser != null) parser.close();
        }
    }

    private static Animation createAnimationFromXml(Context c, XmlPullParser parser)
            throws XmlPullParserException, IOException {

        return createAnimationFromXml(c, parser, null, Xml.asAttributeSet(parser));
    }

    @UnsupportedAppUsage
    private static Animation createAnimationFromXml(
            Context c, XmlPullParser parser, AnimationSet parent, AttributeSet attrs)
            throws XmlPullParserException, IOException, InflateException {

        Animation anim = null;

        // Make sure we are on a start tag.
        int type;
        int depth = parser.getDepth();

        while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
                && type != XmlPullParser.END_DOCUMENT) {

            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            String  name = parser.getName();

            if (name.equals("set")) {
                anim = new AnimationSet(c, attrs);
                createAnimationFromXml(c, parser, (AnimationSet)anim, attrs);
            } else if (name.equals("alpha")) {
                anim = new AlphaAnimation(c, attrs);
            } else if (name.equals("scale")) {
                anim = new ScaleAnimation(c, attrs);
            }  else if (name.equals("rotate")) {
                anim = new RotateAnimation(c, attrs);
            }  else if (name.equals("translate")) {
                anim = new TranslateAnimation(c, attrs);
            } else if (name.equals("cliprect")) {
                anim = new ClipRectAnimation(c, attrs);
            } else if (name.equals("extend")) {
                anim = new ExtendAnimation(c, attrs);
            } else {
                throw new InflateException("Unknown animation name: " + parser.getName());
            }

            if (parent != null) {
                parent.addAnimation(anim);
            }
        }

        return anim;

    }

    /**
     * Loads a {@link LayoutAnimationController} object from a resource
     *
     * @param context Application context used to access resources
     * @param id The resource id of the animation to load
     * @return The animation controller object referenced by the specified id
     * @throws NotFoundException when the layout animation controller cannot be loaded
     */
    public static LayoutAnimationController loadLayoutAnimation(Context context, @AnimRes int id)
            throws NotFoundException {

        XmlResourceParser parser = null;
        try {
            parser = context.getResources().getAnimation(id);
            return createLayoutAnimationFromXml(context, parser);
        } catch (XmlPullParserException | IOException | InflateException ex) {
            throw new NotFoundException(
                    "Can't load animation resource ID #0x" + Integer.toHexString(id), ex);
        } finally {
            if (parser != null) parser.close();
        }
    }

    private static LayoutAnimationController createLayoutAnimationFromXml(
            Context c, XmlPullParser parser)
            throws XmlPullParserException, IOException, InflateException {

        return createLayoutAnimationFromXml(c, parser, Xml.asAttributeSet(parser));
    }

    private static LayoutAnimationController createLayoutAnimationFromXml(
            Context c, XmlPullParser parser, AttributeSet attrs)
            throws XmlPullParserException, IOException, InflateException {

        LayoutAnimationController controller = null;

        int type;
        int depth = parser.getDepth();

        while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
                && type != XmlPullParser.END_DOCUMENT) {

            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            String name = parser.getName();

            if ("layoutAnimation".equals(name)) {
                controller = new LayoutAnimationController(c, attrs);
            } else if ("gridLayoutAnimation".equals(name)) {
                controller = new GridLayoutAnimationController(c, attrs);
            } else {
                throw new InflateException("Unknown layout animation name: " + name);
            }
        }

        return controller;
    }

    /**
     * Make an animation for objects becoming visible. Uses a slide and fade
     * effect.
     *
     * @param c Context for loading resources
     * @param fromLeft is the object to be animated coming from the left
     * @return The new animation
     */
    public static Animation makeInAnimation(Context c, boolean fromLeft) {
        Animation a;
        if (fromLeft) {
            a = AnimationUtils.loadAnimation(c, com.android.internal.R.anim.slide_in_left);
        } else {
            a = AnimationUtils.loadAnimation(c, com.android.internal.R.anim.slide_in_right);
        }

        a.setInterpolator(new DecelerateInterpolator());
        a.setStartTime(currentAnimationTimeMillis());
        return a;
    }

    /**
     * Make an animation for objects becoming invisible. Uses a slide and fade
     * effect.
     *
     * @param c Context for loading resources
     * @param toRight is the object to be animated exiting to the right
     * @return The new animation
     */
    public static Animation makeOutAnimation(Context c, boolean toRight) {
        Animation a;
        if (toRight) {
            a = AnimationUtils.loadAnimation(c, com.android.internal.R.anim.slide_out_right);
        } else {
            a = AnimationUtils.loadAnimation(c, com.android.internal.R.anim.slide_out_left);
        }

        a.setInterpolator(new AccelerateInterpolator());
        a.setStartTime(currentAnimationTimeMillis());
        return a;
    }


    /**
     * Make an animation for objects becoming visible. Uses a slide up and fade
     * effect.
     *
     * @param c Context for loading resources
     * @return The new animation
     */
    public static Animation makeInChildBottomAnimation(Context c) {
        Animation a;
        a = AnimationUtils.loadAnimation(c, com.android.internal.R.anim.slide_in_child_bottom);
        a.setInterpolator(new AccelerateInterpolator());
        a.setStartTime(currentAnimationTimeMillis());
        return a;
    }

    /**
     * Loads an {@link Interpolator} object from a resource
     *
     * @param context Application context used to access resources
     * @param id The resource id of the animation to load
     * @return The interpolator object referenced by the specified id
     * @throws NotFoundException
     */
    public static Interpolator loadInterpolator(Context context, @AnimRes @InterpolatorRes int id)
            throws NotFoundException {
        XmlResourceParser parser = null;
        try {
            parser = context.getResources().getAnimation(id);
            return createInterpolatorFromXml(context.getResources(), context.getTheme(), parser);
        } catch (XmlPullParserException | IOException | InflateException ex) {
            throw new NotFoundException(
                    "Can't load animation resource ID #0x" + Integer.toHexString(id), ex);
        } finally {
            if (parser != null) parser.close();
        }

    }

    /**
     * Loads an {@link Interpolator} object from a resource
     *
     * @param res The resources
     * @param id The resource id of the animation to load
     * @return The interpolator object referenced by the specified id
     * @throws NotFoundException
     */
    @Hide
    public static Interpolator loadInterpolator(Resources res, Theme theme, int id)
            throws NotFoundException {
        XmlResourceParser parser = null;
        try {
            parser = res.getAnimation(id);
            return createInterpolatorFromXml(res, theme, parser);
        } catch (XmlPullParserException | IOException | InflateException ex) {
            throw new NotFoundException(
                    "Can't load animation resource ID #0x" + Integer.toHexString(id), ex);
        } finally {
            if (parser != null) {
                parser.close();
            }
        }

    }

    private static Interpolator createInterpolatorFromXml(
            Resources res, Theme theme, XmlPullParser parser)
            throws XmlPullParserException, IOException, InflateException {

        BaseInterpolator interpolator = null;

        // Make sure we are on a start tag.
        int type;
        int depth = parser.getDepth();

        while (((type = parser.next()) != XmlPullParser.END_TAG || parser.getDepth() > depth)
                && type != XmlPullParser.END_DOCUMENT) {

            if (type != XmlPullParser.START_TAG) {
                continue;
            }

            AttributeSet attrs = Xml.asAttributeSet(parser);

            String name = parser.getName();

            if (name.equals("linearInterpolator")) {
                interpolator = new LinearInterpolator();
            } else if (name.equals("accelerateInterpolator")) {
                interpolator = new AccelerateInterpolator(res, theme, attrs);
            } else if (name.equals("decelerateInterpolator")) {
                interpolator = new DecelerateInterpolator(res, theme, attrs);
            } else if (name.equals("accelerateDecelerateInterpolator")) {
                interpolator = new AccelerateDecelerateInterpolator();
            } else if (name.equals("cycleInterpolator")) {
                interpolator = new CycleInterpolator(res, theme, attrs);
            } else if (name.equals("anticipateInterpolator")) {
                interpolator = new AnticipateInterpolator(res, theme, attrs);
            } else if (name.equals("overshootInterpolator")) {
                interpolator = new OvershootInterpolator(res, theme, attrs);
            } else if (name.equals("anticipateOvershootInterpolator")) {
                interpolator = new AnticipateOvershootInterpolator(res, theme, attrs);
            } else if (name.equals("bounceInterpolator")) {
                interpolator = new BounceInterpolator();
            } else if (name.equals("pathInterpolator")) {
                interpolator = new PathInterpolator(res, theme, attrs);
            } else {
                throw new InflateException("Unknown interpolator name: " + parser.getName());
            }
        }
        return interpolator;
    }

    @Hide
    public static class AnimationTime {
        // Resync triggering threshold for a single frame, as a multiplier of the frame interval.
        private static final int EXTRA_SEVERE_JANK_FRAMES = 6;
        // Resync triggering threshold for recent frames, as a multiplier of the frame interval.
        private static final long MAX_RECENT_JANK_FRAMES = 10;

        // Animation ID counter, shared across all Choreographers.
        private static final AtomicLong sAnimationCounter = new AtomicLong(1);

        // Accessed only from Choreographer thread, or while holding Choreographer's mLock.
        private long mLastVsyncNanos;
        private long mLastFrameIntervalNanos;
        private long mVsyncTimeOfAnimationStart;
        private long mAnimationTime;
        private long mAnimationId = sAnimationCounter.incrementAndGet();
        private boolean mAnimating;
        private boolean mInCallbacks;
        private JankHistory mJankHistory = new JankHistory();

        // Accessed from any thread.
        private AtomicBoolean mVsyncScheduled = new AtomicBoolean(false);

        public AnimationTime() {
        }

        /**
         * Reset the state. Used in tests.
         */
        void reset() {
            mLastVsyncNanos = 0;
            mLastFrameIntervalNanos = 0;
            mVsyncTimeOfAnimationStart = 0;
            mAnimationTime = 0;
            mAnimationId = sAnimationCounter.incrementAndGet();
            mAnimating = false;
            mInCallbacks = false;
            mVsyncScheduled.set(false);
        }

        /**
         * Ticks the animation time forward for the given VSYNC.
         *
         * This should only be called from the Choreographer thread, or while holding the
         * Choreographer's mLock lock.
         *
         * @return the amount of resync applied in nanoseconds
         */
        @RavenwoodIgnore
        public long tick(long vsyncNanos, long frameIntervalNanos) {
            long resync = 0;
            if (mAnimating) {
                long actualInterval = vsyncNanos - mLastVsyncNanos;
                long intervalDelta = Math.abs(actualInterval - mLastFrameIntervalNanos);
                long jankThreshold = mLastFrameIntervalNanos / 2;
                if (actualInterval > EXTRA_SEVERE_JANK_FRAMES * mLastFrameIntervalNanos) {
                    // Extra severe jank. Might as well resync.
                    mJankHistory.push(0, jankThreshold);
                    mAnimationTime += actualInterval;
                    resync = intervalDelta;
                    if (Trace.isTagEnabled(Trace.TRACE_TAG_VIEW)) {
                        Trace.instant(Trace.TRACE_TAG_VIEW, "Severe jank, resyncing "
                                + (intervalDelta / TimeUtils.NANOS_PER_MS) + "ms");
                    }
                } else if (mJankHistory.push(
                        Math.abs(actualInterval - mLastFrameIntervalNanos), jankThreshold)) {
                    // Last frew frames were janky, resync.
                    mAnimationTime += actualInterval;
                    resync = intervalDelta;
                    if (Trace.isTagEnabled(Trace.TRACE_TAG_VIEW)) {
                        Trace.instant(Trace.TRACE_TAG_VIEW, "Too much recent jank, resyncing "
                                + (intervalDelta / TimeUtils.NANOS_PER_MS) + "ms");
                    }
                } else if (intervalDelta > jankThreshold
                        && mJankHistory.getTotalJankOverWindow()
                        > MAX_RECENT_JANK_FRAMES * mLastFrameIntervalNanos) {
                    // To much recent jank, resync.
                    mJankHistory.resetMostRecent();
                    mAnimationTime += actualInterval;
                    resync = intervalDelta;
                    if (Trace.isTagEnabled(Trace.TRACE_TAG_VIEW)) {
                        Trace.instant(Trace.TRACE_TAG_VIEW, "Too much overall jank, resyncing "
                                + (intervalDelta / TimeUtils.NANOS_PER_MS) + "ms");
                    }
                } else {
                    mAnimationTime += mLastFrameIntervalNanos;
                }
            } else {
                mAnimationTime = 0;
                mVsyncTimeOfAnimationStart = vsyncNanos;
            }

            mLastVsyncNanos = vsyncNanos;
            mLastFrameIntervalNanos = frameIntervalNanos;
            mVsyncScheduled.set(false);
            mInCallbacks = true;

            if (Trace.isTagEnabled(Trace.TRACE_TAG_VIEW)) {
                String thread = Thread.currentThread().getName();
                long offset = vsyncNanos - mVsyncTimeOfAnimationStart - mAnimationTime;
                Trace.traceCounter(Trace.TRACE_TAG_VIEW, thread + "#animTimeDelta",
                        (int) (offset / TimeUtils.NANOS_PER_MS));
                Trace.traceCounter(Trace.TRACE_TAG_VIEW, thread + "#totalJankWindow",
                        (int) (mJankHistory.getTotalJankOverWindow() / TimeUtils.NANOS_PER_MS));
                Trace.traceCounter(Trace.TRACE_TAG_VIEW, thread + "#animationId",
                        (int) mAnimationId);
            }
            return resync;
        }

        /**
         * Notifies that all VSYNC callbacks have been completed. Used for animation state
         * bookkeeping.
         *
         * This should only be called from the Choreographer thread, or while holding the
         * Choreographer's mLock lock.
         */
        @RavenwoodIgnore
        public void onCallbacksCompleted() {
            mInCallbacks = false;
            if (mAnimating != mVsyncScheduled.get()) {
                mAnimating = !mAnimating;
                if (!mAnimating) {
                    // Increment counter, since next frame will have a new animation.
                    mAnimationId = sAnimationCounter.incrementAndGet();
                }
                mJankHistory.clear();

                if (Trace.isTagEnabled(Trace.TRACE_TAG_VIEW)) {
                    Trace.traceCounter(Trace.TRACE_TAG_VIEW,
                            Thread.currentThread().getName() + "#animating", mAnimating ? 1 : 0);
                }
            } else if (!mAnimating) {
                // Every non-animating frame needs to have its own animation ID, since we don't
                // know whether a frame is non-animating or the first frame of an animation, when
                // we first start rendering the frame.
                mAnimationId = sAnimationCounter.incrementAndGet();
            }
        }

        /**
         * Notifies that a callback for the next VSYNC has been scheduled. Used for animation state
         * bookkeeping.
         *
         * Can be called from any thread.
         */
        @RavenwoodIgnore
        public void onVsyncScheduled() {
            mVsyncScheduled.set(true);
        }


        /**
         * {@link AnimationUtils#currentAnimationTimeMillis}.
         *
         * This should only be called from the Choreographer thread, or while holding the
         * Choreographer's mLock lock.
         */
        public long getCurrent() {
            return getCurrentNanos() / TimeUtils.NANOS_PER_MS;
        }

        /**
         * {@link AnimationUtils#currentAnimationTimeMillis}, but in nanos.
         *
         * This should only be called from the Choreographer thread, or while holding the
         * Choreographer's mLock lock.
         */
        public long getCurrentNanos() {
            if (mLastVsyncNanos == 0) {
                // We have never been initialized from Choreographer. Either we're being called from
                // a non-ui thread, or this is during startup. Fallback to the uptime nanos.
                Trace.instant(Trace.TRACE_TAG_VIEW,
                        "AnimationUtils#currentAnimationTimeMillis using uptimeNanos");
                return SystemClock.uptimeNanos();
            }

            long current = mVsyncTimeOfAnimationStart + mAnimationTime;
            if (!mInCallbacks && !mAnimating) {
                long aboutOneFrameAgo = SystemClock.uptimeNanos() - mLastFrameIntervalNanos;
                if (current < aboutOneFrameAgo) {
                    Trace.instant(Trace.TRACE_TAG_VIEW,
                            "AnimationUtils#currentAnimationTimeMillis using aboutOneFrameAgo");
                    current = aboutOneFrameAgo;
                }
            }

            return current;
        }

        /**
         * VSYNC timestamp of last animation time tick.
         *
         * This should only be called from the Choreographer thread, or while holding the
         * Choreographer's mLock lock.
         */
        public long getLastVsyncNanos() {
            return mLastVsyncNanos;
        }

        /**
         * Gets the time in nanos since the last animation start, in the animation time domain.
         *
         * This should only be called from the Choreographer thread, or while holding the
         * Choreographer's mLock lock.
         */
        public long elapsedSinceAnimationStart() {
            return mAnimationTime;
        }

        /**
         * Gets the current animation ID.
         */
        public long getAnimationCounter() {
            return mAnimationId;
        }

        @Hide
        public static class JankHistory {
            private static final int SIZE = 16;

            private final long[] mHistory = new long[SIZE];
            private long mTotalJank = 0;
            private int mLast = 0;

            /**
             * Clear the jank history.
             */
            public void clear() {
                Arrays.fill(mHistory, 0);
                mTotalJank = 0;
            }

            /**
             * Push a new jank value onto the history.
             */
            public boolean push(long jank, long jankThreshold) {
                mLast = (mLast + 1) % SIZE;
                // This and past 2 frames were janky, do a resync.
                if (jank > jankThreshold
                        && mHistory[(mLast + SIZE - 1) % SIZE] > jankThreshold
                        && mHistory[(mLast + SIZE - 2) % SIZE] > jankThreshold) {
                    mTotalJank -= mHistory[mLast];
                    mHistory[mLast] = 0;
                    return true;
                }

                mTotalJank += jank - mHistory[mLast];
                mHistory[mLast] = jank;
                return false;
            }

            /**
             * Reset the most recent jank to 0, call if after a previous push, the jank was "fixed".
             */
            public void resetMostRecent() {
                mTotalJank -= mHistory[mLast];
                mHistory[mLast] = 0;
            }

            /**
             * Get the total jank accumulated over the last 16 frames.
             */
            public long getTotalJankOverWindow() {
                return mTotalJank;
            }
        }
    }
}
