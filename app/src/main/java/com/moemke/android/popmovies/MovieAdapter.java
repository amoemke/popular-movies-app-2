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
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.moemke.android.popmovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;

import static com.moemke.android.popmovies.data.MoviePreferences.getDefaultMoviePosterResolution;

/**
 * Created by aureamoemke on 02/02/2017.
 */

/**
 * {@link MovieAdapter} exposes a list of movies to a
 * {@link android.support.v7.widget.RecyclerView}
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MovieAdapterViewHolder> {

    private static final String TAG = MovieAdapter.class.getSimpleName();

    ArrayList<Movie> mMovieData;

    Context context;

    /*
     * An on-click handler that we've defined to make it easy for an Activity to interface with
     * our RecyclerView
     */
    private final MovieAdapter.MovieAdapterOnClickHandler mClickHandler;

    /**
     * The interface that receives onClick messages.
     */
    public interface MovieAdapterOnClickHandler {
        void onClick(Movie chosenMovie);
    }

    /**
     * Creates a MovieAdapter.
     *
     * @param clickHandler The on-click handler for this adapter. This single handler is called
     *                     when an item is clicked.
     */
    public MovieAdapter(MovieAdapter.MovieAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    /**
     * Cache of the children views for a movie list item.
     */
    public class MovieAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public final ImageView mPosterImageView;

        public MovieAdapterViewHolder(View view) {
            super(view);
            context = view.getContext();

            mPosterImageView = (ImageView) view.findViewById(R.id.iv_movie_thumbnail);
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
            Movie chosenMovie = mMovieData.get(adapterPosition);
            mClickHandler.onClick(chosenMovie);
        }
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param viewGroup The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout. See
     *                  {@link android.support.v7.widget.RecyclerView.Adapter#getItemViewType(int)}
     *                  for more details.
     * @return A new MovieAdapterViewHolder that holds the View for each list item
     */
    @Override
    public MovieAdapter.MovieAdapterViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately);
        return new MovieAdapter.MovieAdapterViewHolder(view);
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
    public void onBindViewHolder(MovieAdapter.MovieAdapterViewHolder movieAdapterViewHolder, int position) {

        Movie chosenMovie = mMovieData.get(position);

        // Use Picasso to return poster image
        // The base URL will look like: http://image.tmdb.org/t/p/.
        // Then you will need a ‘size’, which will be one of the following:
        // "w92", "w154", "w185", "w342", "w500", "w780", or "original". For most phones we recommend using “w185”.
        // And finally the poster path returned by the query, for ex. “/nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg”

        //From Review comment:
        //Here, to learn more, you can also try to use error and placeholder in Picasso here to
        // avoid crashing down due to empty string values or null values. Before the error
        // placeholder is shown, your request will be retried three times.
        // sample codes (from Picasso documentation):
        //Picasso.with(context).load(url).placeholder(R.drawable.user_placeholder)
        //      .error(R.drawable.user_placeholder_error).into(imageView);

        if (NetworkUtils.isOnline(context)) {
            String imgUrlString = NetworkUtils.buildImageUrlString(getDefaultMoviePosterResolution(),
                    chosenMovie.getPoster_path());
            // Original no error handling:
            // Picasso.with(movieAdapterViewHolder.mPosterImageView.getContext()).load(imgUrlString)
            //                .into(movieAdapterViewHolder.mPosterImageView);
            Picasso.with(movieAdapterViewHolder.mPosterImageView.getContext()).load(imgUrlString)
                    //.placeholder(R.drawable.ic_movie_placeholder)  not nice:shows placeholder while loading
                    .error(R.drawable.ic_movie_placeholder)
                    .into(movieAdapterViewHolder.mPosterImageView);
        } else {
            if (MainActivity.mSortOrder.equalsIgnoreCase(MainActivity.SORTORDER_FAVORITES)) {
                File imgFile = new File(context.getFilesDir(), chosenMovie.getPoster_path().substring(1));
                //Log.v(TAG, "displaying file from internal dir" + imgFile.toString() + " of length:" +
                //        imgFile.length() / 1024 + "kb");
                movieAdapterViewHolder.mPosterImageView.setImageURI(Uri.parse(imgFile.toString()));
            } else {
                movieAdapterViewHolder.mPosterImageView.setImageResource(R.drawable.ic_movie_placeholder);
            }
        }
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of items available in our movie set
     */
    @Override
    public int getItemCount() {
        if (null == mMovieData) return 0;
        return mMovieData.size();
    }

    /**
     * This method is used to set the detail movie data on a MovieAdapter if we've already
     * created one. This is handy when we get new data from the web but don't want to create a
     * new MovieAdapter to display it.
     *
     * @param movieData The new movie data to be displayed.
     */
    public void setMovieData(ArrayList<Movie> movieData) {
        mMovieData = movieData;
        this.notifyDataSetChanged();
    }
}
