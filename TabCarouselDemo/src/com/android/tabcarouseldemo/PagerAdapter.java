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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class PagerAdapter extends FragmentPagerAdapter {

    private final SparseArray<WeakReference<Fragment>> mFragmentArray = new SparseArray<WeakReference<Fragment>>();

    private final List<Holder> mHolderList = new ArrayList<Holder>();

    private final FragmentActivity mFragmentActivity;

    private int mCurrentPage;

    /**
     * Constructor of <code>PagerAdatper<code>
     * 
     * @param fragmentActivity The {@link FragmentActivity} of the
     *            {@link SherlockFragment}.
     */
    public PagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity.getSupportFragmentManager());
        mFragmentActivity = fragmentActivity;
    }

    /**
     * Method that adds a new fragment class to the viewer (the fragment is
     * internally instantiate)
     * 
     * @param className The full qualified name of fragment class.
     * @param params The instantiate params.
     */
    @SuppressWarnings("synthetic-access")
    public void add(Class<? extends Fragment> className, Bundle params) {
        final Holder holder = new Holder();
        holder.mClassName = className.getName();
        holder.mParams = params;

        final int position = mHolderList.size();
        mHolderList.add(position, holder);
        notifyDataSetChanged();
    }

    /**
     * Method that returns the {@link SherlockFragment} in the argument
     * position.
     * 
     * @param position The position of the fragment to return.
     * @return Fragment The {@link SherlockFragment} in the argument position.
     */
    public Fragment getFragment(int position) {
        final WeakReference<Fragment> weakFragment = mFragmentArray.get(position);
        if (weakFragment != null && weakFragment.get() != null) {
            return weakFragment.get();
        }
        return getItem(position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final Fragment fragment = (Fragment) super.instantiateItem(container, position);
        final WeakReference<Fragment> weakFragment = mFragmentArray.get(position);
        if (weakFragment != null) {
            weakFragment.clear();
        }
        mFragmentArray.put(position, new WeakReference<Fragment>(fragment));
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Fragment getItem(int position) {
        final Holder currentHolder = mHolderList.get(position);
        final Fragment fragment = Fragment.instantiate(mFragmentActivity, currentHolder.mClassName,
                currentHolder.mParams);
        return fragment;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        final WeakReference<Fragment> weakFragment = mFragmentArray.get(position);
        if (weakFragment != null) {
            weakFragment.clear();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return mHolderList.size();
    }

    /**
     * Method that returns the current page position.
     * 
     * @return int The current page.
     */
    public int getCurrentPage() {
        return mCurrentPage;
    }

    /**
     * Method that sets the current page position.
     * 
     * @param currentPage The current page.
     */
    protected void setCurrentPage(int currentPage) {
        mCurrentPage = currentPage;
    }

    /**
     * A private class with information about fragment initialization
     */
    private static final class Holder {
        String mClassName;
        Bundle mParams;
    }
}
