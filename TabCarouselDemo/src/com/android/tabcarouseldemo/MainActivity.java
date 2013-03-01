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

package com.android.tabcarouseldemo;

import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import com.android.tabcarousel.CarouselContainer;
import com.android.tabcarousel.CarouselPagerAdapter;

/**
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class MainActivity extends FragmentActivity {

    /**
     * First tab index
     */
    private static final int FIRST_TAB = CarouselContainer.TAB_INDEX_FIRST;

    /**
     * Second tab index
     */
    private static final int SECOND_TAB = CarouselContainer.TAB_INDEX_SECOND;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout
        setContentView(R.layout.carousel_container);

        // Resources
        final Resources res = getResources();

        // Initialize the header
        final CarouselContainer carousel = (CarouselContainer) findViewById(R.id.carousel_header);
        // Indicates that the carousel should only show a fraction of the
        // secondary tab
        carousel.setUsesDualTabs(true);
        // Add some text to the labels
        carousel.setLabel(FIRST_TAB, "Lost in Translation");
        carousel.setLabel(SECOND_TAB, "The Prestige");
        // Add some images to the tabs
        carousel.setImageDrawable(FIRST_TAB, res.getDrawable(R.drawable.lost_in_translation));
        carousel.setImageDrawable(SECOND_TAB, res.getDrawable(R.drawable.the_prestige));

        // The Bundle for the color fragment
        final Bundle blue = new Bundle();
        blue.putInt("color", Color.parseColor("#ff33b5e5"));

        // Initialize the pager adatper
        final PagerAdapter pagerAdapter = new PagerAdapter(this);
        pagerAdapter.add(DummyListFragment.class, new Bundle());
        pagerAdapter.add(ColorFragment.class, blue);

        // Initialize the pager
        final ViewPager carouselPager = (ViewPager) findViewById(R.id.carousel_pager);
        // This is used to communicate between the pager and header
        carouselPager.setOnPageChangeListener(new CarouselPagerAdapter(carouselPager, carousel));
        carouselPager.setAdapter(pagerAdapter);
    }

}
