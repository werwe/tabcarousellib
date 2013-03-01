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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tabcarousel.BackScrollManager;
import com.android.tabcarousel.CarouselContainer;

import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * @author Andrew Neal (andrewdneal@gmail.com)
 */
public class DummyListFragment extends ListFragment implements OnItemClickListener {

    /**
     * List content
     */
    private static final String[] MOVIES = new String[] {
            "A Separation", "Beasts of the Southern Wild", "Being John Malkovich", "Capote",
            "Cast Away", "City of God", "Cloud Atlas", "Dead Poet's Society", "Downfall",
            "Incendies", "Let Me In", "Looper", "Moneyball", "Monsieur Lazhar", "Moon",
            "My Neighbour Totoro", "Paranorman", "Boogie Nights", "Hard Eight", "Magnolia",
            "Punch Drunk Love", "The Master", "There Will Be Blood", "Sherlock Holmes",
            "Silver Linings Playbook", "Lost in Translation", "The Virgin Suicides",
            "The Bling Ring", "Spirited Away", "The Big Lebowski", "The Goods", "Anchorman",
            "Step Brothers", "Talladega Nights", "The Grey", "The Hurt Locker", "Zero Dark Thirty",
            "The Impossible", "The Lives of Others", "Troll Hunter", "United 93", "Winter's Bone",
            "This is 40", "Casa De Mi Padre", "Gone Baby Gone", "Good Will Hunting", "The Town",
            "Another Earth", "Sond of my Voice", "Alien", "Alien II", "Blad Runner", "Inception",
            "Memento", "The Prestige", "Batman", "Black Swan", "The Fountain", "Pan's Labyrinth"
    };

    /**
     * The header to bind the {@link BackScrollManager} to
     */
    private CarouselContainer mCarousel;

    /**
     * Empty constructor as per the {@link Fragment} docs
     */
    public DummyListFragment() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mCarousel = (CarouselContainer) activity.findViewById(R.id.carousel_header);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Simple ArrayAdapter
        Arrays.sort(MOVIES);
        final CarouselListAdapter adapter = new CarouselListAdapter(getActivity());
        for (final String movie : MOVIES) {
            adapter.add(movie);
        }

        // Bind the data
        setListAdapter(adapter);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final ListView listView = getListView();
        // Attach the BackScrollManager
        listView.setOnScrollListener(new BackScrollManager(mCarousel, null,
                CarouselContainer.TAB_INDEX_FIRST));
        // Register the onItemClickListener
        listView.setOnItemClickListener(this);
        // We disable the scroll bar because it would otherwise be incorrect
        // because of the hidden
        // header
        listView.setVerticalScrollBarEnabled(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // This is the header
        if (position == 0) {
            return;
        }

        // Remember to substract one from the touched position
        final String movie = (String) parent.getItemAtPosition(position - 1);
        Toast.makeText(getActivity(), movie, Toast.LENGTH_SHORT).show();
    }

    /**
     * This is a special adapater used in conjunction with
     * {@link CarouselHeader} and {@link BackScrollManager}. In order to
     * smoothly animate the {@link CarouselHeader}, a faux header in placed at
     * position == 0 in the adapter. This isn't necessary to use the widget, but
     * it is if you want the animation to appear correct.
     */
    private static final class CarouselListAdapter extends ArrayAdapter<String> {

        /**
         * The header view
         */
        private static final int ITEM_VIEW_TYPE_HEADER = 0;

        /**
         * * The data in the list.
         */
        private static final int ITEM_VIEW_TYPE_DATA = 1;

        /**
         * Number of views (TextView, CarouselHeader)
         */
        private static final int VIEW_TYPE_COUNT = 2;

        /**
         * Fake header
         */
        private final View mHeader;

        /**
         * Constructor of <code>CarouselListAdapter</code>
         * 
         * @param context The {@link Context} to use
         */
        public CarouselListAdapter(Context context) {
            super(context, 0);
            // Inflate the fake header
            mHeader = LayoutInflater.from(context).inflate(R.layout.faux_carousel, null);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Return a faux header at position 0
            if (position == 0) {
                return mHeader;
            }

            // Recycle ViewHolder's items
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(
                        android.R.layout.simple_list_item_1, parent, false);
                holder = new ViewHolder(convertView);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            // Retieve the data, but make sure to call one less than the current
            // position to avoid counting the faux header.
            final String movies = getItem(position - 1);
            holder.mLineOne.get().setText(movies);
            return convertView;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public boolean hasStableIds() {
            return true;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getCount() {
            return MOVIES.length + 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public long getItemId(int position) {
            if (position == 0) {
                return -1;
            }
            return position - 1;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getViewTypeCount() {
            return VIEW_TYPE_COUNT;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return ITEM_VIEW_TYPE_HEADER;
            }
            return ITEM_VIEW_TYPE_DATA;
        }
    }

    private static final class ViewHolder {

        public WeakReference<TextView> mLineOne;

        /* Constructor of <code>ViewHolder</code> */
        public ViewHolder(View view) {
            // Initialize mLineOne
            mLineOne = new WeakReference<TextView>((TextView) view.findViewById(android.R.id.text1));
        }

    }

}
