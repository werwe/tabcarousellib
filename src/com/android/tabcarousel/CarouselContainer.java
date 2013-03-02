/*
 * Copyright (C) 2013 Android Open Source Project
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

package com.android.tabcarousel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.nineoldandroids.animation.ObjectAnimator;

import java.lang.ref.WeakReference;

/**
 * This is a horizontally scrolling carousel with 2 tabs.
 */
public class CarouselContainer extends HorizontalScrollView implements OnTouchListener {

    /**
     * Number of tabs
     */
    private static final int TAB_COUNT = 2;

    /**
     * First tab index
     */
    public static final int TAB_INDEX_FIRST = 0;

    /**
     * Second tab index
     */
    public static final int TAB_INDEX_SECOND = 1;

    /**
     * Y coordinate of the tab at the given index was selected
     */
    private static final float[] Y_COORDINATE = new float[TAB_COUNT];

    /**
     * Alpha layer to be set on the lable view
     */
    private static final float MAX_ALPHA = 0.6f;

    /**
     * Tab width as defined as a fraction of the screen width
     */
    private final float mTabWidthScreenFraction;

    /**
     * Tab height as defined as a fraction of the screen width
     */
    private final float mTabHeightScreenFraction;

    /**
     * Height of the tab label
     */
    private final int mTabDisplayLabelHeight;

    /**
     * Height of the shadow under the tab carousel
     */
    private final int mTabShadowHeight;

    /**
     * Used to determine is the carousel is animating
     */
    private boolean mTabCarouselIsAnimating;

    /**
     * Indicates that both tabs are to be used if true, false if only one
     */
    private boolean mDualTabs = true;

    /**
     * Interface invoked when the user interacts with the carousel
     */
    private OnCarouselListener mCarouselListener;

    /**
     * The first tab in the carousel
     */
    private CarouselTab mFirstTab;

    /**
     * The second tab in the carousel
     */
    private CarouselTab mSecondTab;

    /**
     * Allowed horizontal scroll length
     */
    private int mAllowedHorizontalScrollLength = Integer.MIN_VALUE;

    /**
     * Allowed vertical scroll length
     */
    private int mAllowedVerticalScrollLength = Integer.MIN_VALUE;

    /**
     * The last scrolled position
     */
    private int mLastScrollPosition = Integer.MIN_VALUE;

    /**
     * Current tab index
     */
    private int mCurrentTab = TAB_INDEX_FIRST;

    /**
     * Factor to scale scroll-amount sent to {@code #mCarouselListener}
     */
    private float mScrollScaleFactor = 1.0f;

    /**
     * True to scroll to the pager's current position, false otherwise
     */
    private boolean mScrollToCurrentTab = false;

    /**
     * @param context The {@link Context} to use
     * @param attrs The attributes of the XML tag that is inflating the view
     */
    public CarouselContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        // Add the onTouchListener
        setOnTouchListener(this);
        // Retrieve the carousel dimensions
        final Resources res = getResources();
        // Width of the tab
        mTabWidthScreenFraction = res.getFraction(R.fraction.tab_width_screen_percentage, 1, 1);
        // Height of the tab
        mTabHeightScreenFraction = res.getFraction(R.fraction.tab_height_screen_percentage, 1, 1);
        // Height of the label
        mTabDisplayLabelHeight = res.getDimensionPixelSize(R.dimen.carousel_label_height);
        // Height of the image shadow
        mTabShadowHeight = res.getDimensionPixelSize(R.dimen.carousel_image_shadow_height);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mFirstTab = (CarouselTab) findViewById(R.id.carousel_tab_one);
        mFirstTab.setOverlayOnClickListener(new TabClickListener(this, TAB_INDEX_FIRST));
        mSecondTab = (CarouselTab) findViewById(R.id.carousel_tab_two);
        mSecondTab.setOverlayOnClickListener(new TabClickListener(this, TAB_INDEX_SECOND));
        mSecondTab.setAlphaLayerValue(MAX_ALPHA);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int screenWidth = MeasureSpec.getSize(widthMeasureSpec);
        // Compute the width of a tab as a fraction of the screen width
        final int tabWidth = Math.round(mTabWidthScreenFraction * screenWidth);

        // Find the allowed scrolling length by subtracting the current visible
        // screen width
        // from the total length of the tabs.
        mAllowedHorizontalScrollLength = tabWidth * TAB_COUNT - screenWidth;

        // Scrolling by mAllowedHorizontalScrollLength causes listeners to
        // scroll by the entire screen amount; compute the scale-factor
        // necessary to make this so.
        if (mAllowedHorizontalScrollLength == 0) {
            // Guard against divide-by-zero.
            // This hard-coded value prevents a crash, but won't result in the
            // desired scrolling behavior. We rely on the framework calling
            // onMeasure()
            // again with a non-zero screen width.
            mScrollScaleFactor = 1.0f;
        } else {
            mScrollScaleFactor = screenWidth / mAllowedHorizontalScrollLength;
        }

        final int tabHeight = Math.round(screenWidth * mTabHeightScreenFraction) + mTabShadowHeight;
        // Set the child layout's to be TAB_COUNT * the computed tab
        // width so that the layout's children (which are the tabs) will evenly
        // split that width.
        if (getChildCount() > 0) {
            final View child = getChildAt(0);

            // Add 1 dip of separation between the tabs
            final int seperatorPixels = (int) (TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()) + 0.5f);

            if (mDualTabs) {
                final int size = TAB_COUNT * tabWidth + (TAB_COUNT - 1) * seperatorPixels;
                child.measure(measureExact(size), measureExact(tabHeight));
            } else {
                child.measure(measureExact(screenWidth), measureExact(tabHeight));
            }
        }

        mAllowedVerticalScrollLength = tabHeight - mTabDisplayLabelHeight - mTabShadowHeight;
        setMeasuredDimension(resolveSize(screenWidth, widthMeasureSpec),
                resolveSize(tabHeight, heightMeasureSpec));
    }

    /**
     * {@inheritDoc}
     */
    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (!mScrollToCurrentTab) {
            return;
        }
        mScrollToCurrentTab = false;
        Utils.doAfterLayout(this, new Runnable() {
            @Override
            public void run() {
                scrollTo(mCurrentTab == TAB_INDEX_FIRST ? 0 : mAllowedHorizontalScrollLength, 0);
                updateAlphaLayers();
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onScrollChanged(int x, int y, int oldX, int oldY) {
        super.onScrollChanged(x, y, oldX, oldY);

        // Guard against framework issue where onScrollChanged() is called twice
        // for each touch-move event. This wreaked havoc on the tab-carousel:
        // the
        // view-pager moved twice as fast as it should because we called
        // fakeDragBy()
        // twice with the same value.
        if (mLastScrollPosition == x) {
            return;
        }

        // Since we never completely scroll the about/updates tabs off-screen,
        // the draggable range is less than the width of the carousel. Our
        // listeners don't care about this... if we scroll 75% percent of our
        // draggable range, they want to scroll 75% of the entire carousel
        // width, not the same number of pixels that we scrolled.
        final int scaledL = (int) (x * mScrollScaleFactor);
        final int oldScaledL = (int) (oldX * mScrollScaleFactor);
        mCarouselListener.onCarouselScrollChanged(scaledL, y, oldScaledL, oldY);

        mLastScrollPosition = x;
        updateAlphaLayers();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final boolean interceptTouch = super.onInterceptTouchEvent(ev);
        if (interceptTouch) {
            mCarouselListener.onTouchDown();
        }
        return interceptTouch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mCarouselListener.onTouchDown();
                return true;
            case MotionEvent.ACTION_UP:
                mCarouselListener.onTouchUp();
                return true;
        }
        return super.onTouchEvent(event);
    }

    /**
     * @return True if the carousel is currently animating, false otherwise
     */
    public boolean isTabCarouselIsAnimating() {
        return mTabCarouselIsAnimating;
    }

    /**
     * Reset the carousel to the start position
     */
    public void reset() {
        scrollTo(0, 0);
        setCurrentTab(TAB_INDEX_FIRST);
        moveToYCoordinate(TAB_INDEX_FIRST, 0);
    }

    /**
     * Store this information as the last requested Y coordinate for the given
     * tabIndex.
     * 
     * @param tabIndex The tab index being stored
     * @param y The Y cooridinate to move to
     */
    public void storeYCoordinate(int tabIndex, float y) {
        Y_COORDINATE[tabIndex] = y;
    }

    /**
     * Restore the Y position of this view to the last manually requested value.
     * This can be done after the parent has been re-laid out again, where this
     * view's position could have been lost if the view laid outside its
     * parent's bounds.
     * 
     * @param duration The duration of the animation
     * @param tabIndex The index to restore
     */
    public void restoreYCoordinate(int duration, int tabIndex) {
        final float storedYCoordinate = getStoredYCoordinateForTab(tabIndex);

        final Interpolator interpolator = AnimationUtils.loadInterpolator(getContext(),
                android.R.anim.accelerate_decelerate_interpolator);

        final ObjectAnimator animator = ObjectAnimator.ofFloat(this, "y", storedYCoordinate);
        animator.addListener(mTabCarouselAnimatorListener);
        animator.setInterpolator(interpolator);
        animator.setDuration(duration);
        animator.start();
    }

    /**
     * Request that the view move to the given Y coordinate. Also store the Y
     * coordinate as the last requested Y coordinate for the given tabIndex.
     * 
     * @param tabIndex The tab index being stored
     * @param y The Y cooridinate to move to
     */
    public void moveToYCoordinate(int tabIndex, float y) {
        storeYCoordinate(tabIndex, y);
        restoreYCoordinate(0, tabIndex);
    }

    /**
     * Used to propely call {@code #onMeasure(int, int)}
     * 
     * @param yesOrNo Yes to indicate both tabs will be used in the carousel,
     *            false to indicate only one
     */
    public void setUsesDualTabs(boolean yesOrNo) {
        mDualTabs = yesOrNo;
    }

    /**
     * Set the given {@link OnCarouselListener} to handle carousel events
     */
    public void setListener(OnCarouselListener carouselListener) {
        mCarouselListener = carouselListener;
    }

    /**
     * Updates the tab selection
     * 
     * @param position The index to update
     */
    public void setCurrentTab(int position) {
        final CarouselTab selected, deselected;

        switch (position) {
            case TAB_INDEX_FIRST:
                selected = mFirstTab;
                deselected = mSecondTab;
                break;
            case TAB_INDEX_SECOND:
                selected = mSecondTab;
                deselected = mFirstTab;
                break;
            default:
                throw new IllegalStateException("Invalid tab position " + position);
        }
        selected.setSelected(true);
        deselected.setSelected(false);
        mCurrentTab = position;
    }

    /**
     * Sets the label for a tab
     * 
     * @param index Which label to write on
     * @param label The string to set as the label
     */
    public void setLabel(int index, String label) {
        switch (index) {
            case TAB_INDEX_FIRST:
                mFirstTab.setLabel(label);
                break;
            case TAB_INDEX_SECOND:
                mSecondTab.setLabel(label);
                break;
            default:
                throw new IllegalStateException("Invalid tab position " + index);
        }
    }

    /**
     * Sets a drawable as the content of the tab {@link ImageView}
     * 
     * @param index Which {@link ImageView}
     * @param resId The resource identifier of the the drawable
     */
    public void setImageResource(int index, int resId) {
        switch (index) {
            case TAB_INDEX_FIRST:
                mFirstTab.setImageResource(resId);
                break;
            case TAB_INDEX_SECOND:
                mSecondTab.setImageResource(resId);
                break;
            default:
                throw new IllegalStateException("Invalid tab position " + index);
        }
    }

    /**
     * Sets a drawable as the content of the tab {@link ImageView}
     * 
     * @param index Which {@link ImageView}
     * @param drawable The {@link Drawable} to set
     */
    public void setImageDrawable(int index, Drawable drawable) {
        switch (index) {
            case TAB_INDEX_FIRST:
                mFirstTab.setImageDrawable(drawable);
                break;
            case TAB_INDEX_SECOND:
                mSecondTab.setImageDrawable(drawable);
                break;
            default:
                throw new IllegalStateException("Invalid tab position " + index);
        }
    }

    /**
     * Sets a bitmap as the content of the tab {@link ImageView}
     * 
     * @param index Which {@link ImageView}
     * @param bm The {@link Bitmap} to set
     */
    public void setImageBitmap(int index, Bitmap bm) {
        switch (index) {
            case TAB_INDEX_FIRST:
                mFirstTab.setImageBitmap(bm);
                break;
            case TAB_INDEX_SECOND:
                mSecondTab.setImageBitmap(bm);
                break;
            default:
                throw new IllegalStateException("Invalid tab position " + index);
        }
    }

    /**
     * Used to return the {@link ImageView} from one of the tabs
     * 
     * @param index The index returning the {@link ImageView}
     * @return The {@link ImageView} from one of the tabs
     */
    public ImageView getImage(int index) {
        switch (index) {
            case TAB_INDEX_FIRST:
                return mFirstTab.getImage();
            case TAB_INDEX_SECOND:
                return mSecondTab.getImage();
            default:
                throw new IllegalStateException("Invalid tab position " + index);
        }
    }

    /**
     * Used to return the label from one of the tabs
     * 
     * @param index The index returning the label
     * @return The label from one of the tabs
     */
    public TextView getLabel(int index) {
        switch (index) {
            case TAB_INDEX_FIRST:
                return mFirstTab.getLabel();
            case TAB_INDEX_SECOND:
                return mSecondTab.getLabel();
            default:
                throw new IllegalStateException("Invalid tab position " + index);
        }
    }

    /**
     * Returns the stored Y coordinate of this view the last time the user was
     * on the selected tab given by tabIndex.
     * 
     * @param tabIndex The tab index use to return the Y value
     */
    public float getStoredYCoordinateForTab(int tabIndex) {
        return Y_COORDINATE[tabIndex];
    }

    /**
     * Returns the number of pixels that this view can be scrolled horizontally
     */
    public int getAllowedHorizontalScrollLength() {
        return mAllowedHorizontalScrollLength;
    }

    /**
     * Returns the number of pixels that this view can be scrolled vertically
     * while still allowing the tab labels to still show
     */
    public int getAllowedVerticalScrollLength() {
        return mAllowedVerticalScrollLength;
    }

    /**
     * @param size The size of the measure specification
     * @return The measure specifiction based on {@link MeasureSpec.#EXACTLY}
     */
    private int measureExact(int size) {
        return MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
    }

    /**
     * Sets the correct alpha layers over the tabs.
     */
    private void updateAlphaLayers() {
        float alpha = mLastScrollPosition * MAX_ALPHA / mAllowedHorizontalScrollLength;
        alpha = Utils.clamp(alpha, 0.0f, 1.0f);
        mFirstTab.setAlphaLayerValue(alpha);
        mSecondTab.setAlphaLayerValue(MAX_ALPHA - alpha);
    }

    /**
     * This listener keeps track of whether the tab carousel animation is
     * currently going on or not, in order to prevent other simultaneous changes
     * to the Y position of the tab carousel which can cause flicker.
     */
    private final AnimatorListener mTabCarouselAnimatorListener = new AnimatorListener() {

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAnimationCancel(Animator animation) {
            mTabCarouselIsAnimating = false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAnimationEnd(Animator animation) {
            mTabCarouselIsAnimating = false;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAnimationRepeat(Animator animation) {
            mTabCarouselIsAnimating = true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onAnimationStart(Animator animation) {
            mTabCarouselIsAnimating = true;
        }
    };

    /** When pressed, selects the corresponding tab */
    private static final class TabClickListener implements OnClickListener {

        /**
         * Reference to {@link CarouselContainer}
         */
        private final WeakReference<CarouselContainer> mReference;

        /**
         * The {@link CarouselTab} being pressed
         */
        private final int mTab;

        /**
         * @param tab The index of the tab pressed
         */
        public TabClickListener(CarouselContainer carouselHeader, int tab) {
            super();
            mReference = new WeakReference<CarouselContainer>(carouselHeader);
            mTab = tab;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void onClick(View v) {
            mReference.get().mCarouselListener.onTabSelected(mTab);
        }
    }

}
