/*
 * Copyright (C) 2017 The Android Open Source Project
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
package com.moemke.android.popmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by aureamoemke on 02/02/2017.
 */

/**
 * {@link MovieReviewAdapter} exposes a list of movie reviews to a
 * {@link RecyclerView}
 */

class MovieReviewAdapter extends RecyclerView.Adapter<MovieReviewAdapter.MovieReviewAdapterViewHolder> {

    private static final String TAG = MovieReviewAdapter.class.getSimpleName();

    ArrayList<MovieReview> mMovieReviewData;
    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    private final MovieReviewAdapter.MovieReviewAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface MovieReviewAdapterOnClickHandler {
        void onClick(MovieReview chosenMovieReview);
    }

    /**
     * Creates a MovieAdapter.
     *
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public MovieReviewAdapter(MovieReviewAdapter.MovieReviewAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    /**
     * Cache of the children views for a movie list item.
     */
    class MovieReviewAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView mContentView;
        final TextView mReadAll;
        final TextView mAuthorView;
        final TextView mReadMore;

        MovieReviewAdapterViewHolder(View view) {
            super(view);

            //Refer: http://stackoverflow.com/questions/11311541/
            // how-can-i-implement-a-collapsible-view-like-the-one-from-google-play

            //http://stackoverflow.com/questions/31668697/
            // android-expandable-text-view-with-view-more-button-displaying-at-center-after

            mContentView = (TextView) view.findViewById(R.id.tv_detail_review);

            mReadAll = (TextView) view.findViewById(R.id.tv_detail_read_all);
            mAuthorView = (TextView) view.findViewById(R.id.tv_detail_review_author);
            mReadMore = (TextView) view.findViewById(R.id.tv_read_more);

            //TODO: Shows "read more", even if less than 3 lines in review
            mReadMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.v(TAG, "clicked Read more");
                    mReadAll.setVisibility(View.INVISIBLE);
                    mContentView.setMaxLines(Integer.MAX_VALUE);
                    mReadMore.setVisibility(View.INVISIBLE);
                }
            });

            // TODO: Alternative solution for expanding text view
            // http://stackoverflow.com/questions/31668697/
            // android-expandable-text-view-with-view-more-button-displaying-at-center-after
            view.setOnClickListener(this);
        }

        /**
         * This gets called by the child views during a click.
         *
         * @param v The View that was clicked
         */
        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            MovieReview chosenMovieReview = mMovieReviewData.get(adapterPosition);
            mClickHandler.onClick(chosenMovieReview);
        }
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new MovieAdapterViewHolder that holds the View for each list item
     */
    @Override
    public MovieReviewAdapter.MovieReviewAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movie_review_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);

        return new MovieReviewAdapter.MovieReviewAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the movie
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param movieAdapterViewHolder The ViewHolder which should be updated to represent the
     *                               contents of the item at the given position in the data set.
     * @param position               The position of the item within the adapter's data set.
     */

    @Override
    public void onBindViewHolder(MovieReviewAdapter.MovieReviewAdapterViewHolder movieAdapterViewHolder, int position) {
        MovieReview chosenMovieReview = mMovieReviewData.get(position);

        movieAdapterViewHolder.mContentView.setText(chosenMovieReview.content);
        //movieAdapterViewHolder.mReadAll.setText(chosenMovieReview.content);
        movieAdapterViewHolder.mAuthorView.setText(chosenMovieReview.author);
//        int height    = movieAdapterViewHolder.mReadAll.getHeight();
//        int scrollY   = movieAdapterViewHolder.mReadAll.getScrollY();
//        Layout layout = movieAdapterViewHolder.mReadAll.getLayout();
//
//        int linect = movieAdapterViewHolder.mReadAll.getLineCount();
//        Log.v(TAG, "linect "+linect);

//        int firstVisibleLineNumber = layout.getLineForVertical(scrollY);
//        int lastVisibleLineNumber  = layout.getLineForVertical(scrollY+height);
//        Log.v(TAG, "first visible num "+firstVisibleLineNumber);
//        Log.v(TAG, "last visible num "+lastVisibleLineNumber);
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our movie set
     */
    @Override
    public int getItemCount() {
        if (null == mMovieReviewData) return 0;
        return mMovieReviewData.size();
    }

    /**
     * This method is used to set the detail movie data on a MovieAdapter if we've already
     * created one. This is handy when we get new data from the web but don't want to create a
     * new MovieAdapter to display it.
     *
     * @param movieReviewData The new movie review data to be displayed.
     */
    public void setMovieReviewData(ArrayList<MovieReview> movieReviewData) {
        mMovieReviewData = movieReviewData;
        notifyDataSetChanged();
    }
}
