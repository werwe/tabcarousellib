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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.tabcarousel.R;

/**
 * This class represents each tab in the {@link CarouselContainer}.
 * 
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class CarouselTab extends FrameLayoutWithOverlay {

    /**
     * Used to display the main images in the tabs of the carousel
     */
    private ImageView mCarouselImage;

    /**
     * The label of each tab in the carousel
     */
    private TextView mLabel;

    /**
     * The layer placed over {@code #mCarouselImage}
     */
    private View mAlphaLayer;

    /**
     * Used to indicate which tab in selected
     */
    private View mColorstrip;

    /**
     * @param context The {@link Context} to use
     * @param attrs The attributes of the XML tag that is inflating the view
     */
    public CarouselTab(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        // Initiate the tab
        mCarouselImage = (ImageView) findViewById(R.id.carousel_tab_image);
        mLabel = (TextView) findViewById(R.id.carousel_tab_label);
        mAlphaLayer = findViewById(R.id.carousel_tab_alpha_overlay);
        mColorstrip = findViewById(R.id.carousel_tab_colorstrip);
        // Set the alpha layer
        setAlphaLayer(mAlphaLayer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            mColorstrip.setVisibility(View.VISIBLE);
            setOverlayClickable(false);
        } else {
            mColorstrip.setVisibility(View.GONE);
            setOverlayClickable(true);
        }
        setSelectedState(selected);
    }

    /**
     * Sets the label for a tab
     * 
     * @param label The string to set as the label
     */
    public void setLabel(String label) {
        mLabel.setText(label);
    }

    /**
     * Sets the selected state of the label view
     * 
     * @param state True to select the label, false otherwise
     */
    public void setSelectedState(boolean state) {
        mLabel.setSelected(state);
    }

    /**
     * Sets a drawable as the content of this ImageView
     * 
     * @param resId The resource identifier of the the drawable
     */
    public void setImageResource(int resId) {
        mCarouselImage.setImageResource(resId);
    }

    /**
     * Sets a Bitmap as the content of this ImageView
     * 
     * @param bm The {@link Bitmap} to set
     */
    public void setImageBitmap(Bitmap bm) {
        mCarouselImage.setImageBitmap(bm);
    }

    /**
     * Sets a drawable as the content of this ImageView
     * 
     * @param drawable The {@link Drawable} to set
     */
    public void setImageDrawable(Drawable drawable) {
        mCarouselImage.setImageDrawable(drawable);
    }

    public void setOnImageClickListner(OnClickListener onClickListener) {
        mCarouselImage.setOnClickListener(onClickListener);
    }

    /**
     * @return the mCarouselImage
     */
    public ImageView getImage() {
        return mCarouselImage;
    }

    /**
     * @return the mLabel
     */
    public TextView getLabel() {
        return mLabel;
    }

    /**
     * @return the mAlphaLayer
     */
    public View getAlphaLayer() {
        return mAlphaLayer;
    }

    /**
     * @return the mColorstrip
     */
    public View getColorstrip() {
        return mColorstrip;
    }

}
