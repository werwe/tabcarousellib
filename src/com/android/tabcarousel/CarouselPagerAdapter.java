/*
 * Copyright (C) 2013 Andrew Neal
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

import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;

import java.lang.ref.WeakReference;

/**
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class CarouselPagerAdapter implements OnPageChangeListener, OnCarouselListener {

    /**
     * A reference the parent {@link ViewPager}
     */
    private final WeakReference<ViewPager> mReference;

    /**
     * The carousel header
     */
    private final CarouselContainer mCarousel;

    /**
     * Constructor for <code>ViewPagerAdapter</code>
     * 
     * @param ViewPager A reference the parent {@link ViewPager}
     */
    public CarouselPagerAdapter(ViewPager viewPager, CarouselContainer carouselHeader) {
        if (viewPager == null || carouselHeader == null) {
            throw new IllegalStateException("The ViewPager and CarouselHeader must not be null");
        }
        mReference = new WeakReference<ViewPager>(viewPager);
        viewPager.setOnPageChangeListener(this);
        mCarousel = carouselHeader;
        mCarousel.setListener(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPageScrollStateChanged(int state) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            mCarousel.restoreYCoordinate(75, mReference.get().getCurrentItem());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        if (mReference.get().isFakeDragging()) {
            return;
        }

        final int scrollToX = (int) ((position + positionOffset) * mCarousel
                .getAllowedHorizontalScrollLength());
        mCarousel.scrollTo(scrollToX, 0);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPageSelected(int position) {
        mCarousel.setCurrentTab(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTouchDown() {
        if (!mReference.get().isFakeDragging()) {
            mReference.get().beginFakeDrag();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTouchUp() {
        if (mReference.get().isFakeDragging()) {
            mReference.get().endFakeDrag();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTabSelected(int position) {
        mReference.get().setCurrentItem(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCarouselScrollChanged(int l, int t, int oldl, int oldt) {
        if (mReference.get().isFakeDragging()) {
            mReference.get().fakeDragBy(oldl - l);
        }
    }

}
