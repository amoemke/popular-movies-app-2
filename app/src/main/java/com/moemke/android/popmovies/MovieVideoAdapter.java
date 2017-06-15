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
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.moemke.android.popmovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by aureamoemke on 02/02/2017.
 */

/**
 * {@link MovieVideoAdapter} exposes a list of movie reviews to a
 * {@link RecyclerView}
 */

class MovieVideoAdapter extends RecyclerView.Adapter<MovieVideoAdapter.MovieVideoAdapterViewHolder> {

    private static final String TAG = MovieVideoAdapter.class.getSimpleName();

    private Context context;
    private String mVideoUriStr;

    ArrayList<MovieVideo> mMovieVideoData;

    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    private final MovieVideoAdapter.MovieVideoAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface MovieVideoAdapterOnClickHandler {
        void onClick(MovieVideo chosenMovieVideo);
    }

    /**
     * Creates a MovieAdapter.
     *
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public MovieVideoAdapter(MovieVideoAdapter.MovieVideoAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;

    }

    /**
     * Cache of the children views for a movie list item.
     */
    class MovieVideoAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        final TextView mTrailerNameTv;
        //final TextView mTrailerTypeTv;
        //final TextView mVideoUrl;
        final ImageView mTrailerImgView;
        final TextView mPlayButtonTv;
        //final VideoView mTrailerVideoView;

        MovieVideoAdapterViewHolder(View view) {
            super(view);

            mTrailerNameTv = (TextView) view.findViewById(R.id.tv_detail_video_trailer_name);
            mTrailerImgView = (ImageView) view.findViewById(R.id.iv_detail_youtube_img);
            mPlayButtonTv = (TextView) view.findViewById(R.id.tv_play_button);

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
            MovieVideo chosenMovieVideo = mMovieVideoData.get(adapterPosition);
            mClickHandler.onClick(chosenMovieVideo);
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
    public MovieVideoAdapter.MovieVideoAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movie_video_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new MovieVideoAdapter.MovieVideoAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the movie
     * details for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param movieVideoAdapterViewHolder The ViewHolder which should be updated to represent the
     *                                    contents of the item at the given position in the data set.
     * @param position                    The position of the item within the adapter's data set.
     */

    @Override
    public void onBindViewHolder(MovieVideoAdapter.MovieVideoAdapterViewHolder movieVideoAdapterViewHolder, int position) {

        MovieVideo chosenMovieVideo = mMovieVideoData.get(position);
        movieVideoAdapterViewHolder.mTrailerNameTv.setText(chosenMovieVideo.name);

        String youtubeImgUrlString = NetworkUtils.buildYoutubeImageUrlString(chosenMovieVideo.key);
        Picasso.with(movieVideoAdapterViewHolder.mTrailerImgView.getContext()).load(youtubeImgUrlString)
//                .placeholder(R.drawable.ic_movie_placeholder)  not nice:shows placeholder while loading
                .error(R.drawable.ic_movie_placeholder)
                .into(movieVideoAdapterViewHolder.mTrailerImgView);

    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our movie set
     */
    @Override
    public int getItemCount() {
        if (null == mMovieVideoData) return 0;
        return mMovieVideoData.size();
    }

    /**
     * This method is used to set the detail movie data on a MovieAdapter if we've already
     * created one. This is handy when we get new data from the web but don't want to create a
     * new MovieAdapter to display it.
     *
     * @param MovieVideoData The new movie review data to be displayed.
     */
    public void setMovieVideoData(ArrayList<MovieVideo> MovieVideoData) {
        mMovieVideoData = MovieVideoData;
        notifyDataSetChanged();
    }

}
