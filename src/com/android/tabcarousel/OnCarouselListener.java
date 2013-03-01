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

/**
 * Interface for callbacks invoked when the user interacts with the carousel.
 */
public interface OnCarouselListener {

    /**
     * Determines when the user is touching the carousel
     */
    public void onTouchDown();

    /**
     * Determines when the user lifts their finger up from the carousel
     */
    public void onTouchUp();

    /**
     * @param l Current horizontal scroll origin
     * @param t Current vertical scroll origin
     * @param oldl Previous horizontal scroll origin
     * @parm oldt Previous vertical scroll origin
     */
    public void onCarouselScrollChanged(int l, int t, int oldl, int oldt);

    /**
     * Called when a tab is selected
     * 
     * @param position The position of the selected tab
     */
    public void onTabSelected(int position);
}
