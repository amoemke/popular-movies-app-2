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

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.moemke.android.popmovies.MovieReviewAdapter.MovieReviewAdapterOnClickHandler;
import com.moemke.android.popmovies.MovieVideoAdapter.MovieVideoAdapterOnClickHandler;
import com.moemke.android.popmovies.data.FavoritesContract;
import com.moemke.android.popmovies.data.MoviePreferences;
import com.moemke.android.popmovies.utilities.MovieJsonUtils;
import com.moemke.android.popmovies.utilities.MovieUtils;
import com.moemke.android.popmovies.utilities.NetworkUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<MovieDetail>, MovieReviewAdapterOnClickHandler,
        MovieVideoAdapterOnClickHandler, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = DetailActivity.class.getSimpleName();

    private static final String MOVIE_SHARE_HASHTAG = " #PopMoviesApp";

    /* A constant to save and restore the URL that is being displayed */
    private static final String MOVIE_QUERY_DETAIL_URL_EXTRA = "query_detail";

    //constant int to identify movie review and video loader
    private static final int MOVIE_QUERY_DETAIL_LOADER = 22;

    private ImageView mDetailPoster;
    private ImageView mBackdropPoster;
    private TextView mDetailTitle;
    private ImageView mOfflineIcon;
    private TextView mDetailReleaseDate;
    private TextView mDetailOverview;
    private TextView mDetailVoteAverage;

    // private RatingBar mRatingBar;
    private TextView mReviewsTitle;
    private TextView mVideosTitle;

    private ImageButton mFavorite;

    //show genres in string form
    private TextView mGenres;

    private RecyclerView mrRecyclerView; //MovieReviews
    private RecyclerView mvRecyclerView; //MovieVideos

    private MovieReviewAdapter mMovieReviewAdapter;
    private MovieVideoAdapter mMovieVideoAdapter;

    private TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    private String mQueryDetailUrlString;

    private AlertDialog showWifiDialog = null;

    //passed from Intent
    private Movie mMovie;
    private String mSortOrder;

    private MovieDetail mMovieDetailData;

    //shared preference setting
    private boolean mShowVideos;
    private boolean mShowReviews;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupSharedPreferences();

        mDetailPoster = (ImageView) findViewById(R.id.iv_detail_poster);
        mBackdropPoster = (ImageView) findViewById(R.id.iv_detail_backdrop);
        mDetailTitle = (TextView) findViewById(R.id.tv_detail_title);
        mOfflineIcon = (ImageView) findViewById(R.id.iv_detail_wifi_off);
        mDetailReleaseDate = (TextView) findViewById(R.id.tv_detail_release_date);
        mDetailOverview = (TextView) findViewById(R.id.tv_detail_overview);
        mDetailVoteAverage = (TextView) findViewById(R.id.tv_detail_vote_average);
        // mRatingBar = (RatingBar) findViewById(R.id.rating_bar);
        mReviewsTitle = (TextView) findViewById((R.id.tv_reviews_title));
        mVideosTitle = (TextView) findViewById((R.id.tv_videos_title));
        mGenres = (TextView) findViewById(R.id.tv_detail_genres);
        mFavorite = (ImageButton) findViewById(R.id.ib_favorite);
        mReviewsTitle.setVisibility(View.VISIBLE);
        mVideosTitle.setVisibility(View.VISIBLE);

        mrRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movie_reviews);

        mvRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movie_videos);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_detail_error_message_display);
        mErrorMessageDisplay.setText(R.string.error_message_unknown);

        LinearLayoutManager mrLayoutManager = new LinearLayoutManager(this);
        LinearLayoutManager mvLayoutManager = new LinearLayoutManager(this);

        mrRecyclerView.setLayoutManager(mrLayoutManager);
        mvRecyclerView.setLayoutManager(mvLayoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mrRecyclerView.setHasFixedSize(true);
        mvRecyclerView.setHasFixedSize(true);

        /*
         * The MovieReviewAdapter and MovieVideoAdapter are responsible for linking our movie review
         * data and movie video data with the Views that will end up displaying our  data.
         */
        mMovieReviewAdapter = new MovieReviewAdapter(this);
        mMovieVideoAdapter = new MovieVideoAdapter(this);

        /* Setting the adapter attaches it to the corresponding RecyclerView in our layout. */
        mrRecyclerView.setAdapter(mMovieReviewAdapter);
        mvRecyclerView.setAdapter(mMovieVideoAdapter);

        Intent intent = getIntent();

        if (intent != null) {
            if (intent.hasExtra("movie")) {
                mMovie = intent.getParcelableExtra("movie");
                //check if in favorites file, if yes, then set the favorite button on
                if (isFavorite(mMovie.getId())) {
                    mFavorite.setActivated(true);
                } else {
                    mFavorite.setActivated(false);
                }
            }
            // if favorites, then load from offline if no internet
            if (intent.hasExtra("sortOrder")) {
                mSortOrder = intent.getStringExtra("sortOrder");
            }
            /*
            * The ProgressBar that will indicate to the user that we are loading data. It will be
            * hidden when no data is loading.
            */
            mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_detail_loading_indicator);

            mFavorite.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    if (!(mFavorite.isActivated())) {
                        addFavorite(mMovie);
                        mFavorite.setActivated(true);
                        Toast.makeText(DetailActivity.this,
                                getResources().getString(R.string.save_favorite), Toast.LENGTH_SHORT).show();
                    } else {
                        int removedFave = removeFavorite(mMovie.getId());
                        mFavorite.setActivated(false);
                        Toast.makeText(DetailActivity.this,
                                getResources().getString(R.string.remove_favorite), Toast.LENGTH_SHORT).show();
                        if (mSortOrder.equalsIgnoreCase(MainActivity.SORTORDER_FAVORITES)) {
                            finish();
                        }
                    }
                }

            });
            mFavorite.setOnLongClickListener(new View.OnLongClickListener() {
                public boolean onLongClick(View v) {
                    Toast.makeText(DetailActivity.this, getResources().getString(R.string.click_to_save_favorite),
                            Toast.LENGTH_SHORT).show();
                    return true;
                }
            });

            mDetailTitle.setText(mMovie.getOriginal_title());
            mDetailReleaseDate.setText(MovieUtils.
                    formatDate(mMovie.getRelease_date(), NetworkUtils.getTMDBDateFormat(),
                            MoviePreferences.getDefaultDisplayDateFormat()));
            mDetailOverview.setText(mMovie.getOverview());
            mDetailVoteAverage.setText(Float.toString(mMovie.vote_average) +
                    getResources().getString(R.string.over_10));
            // mDetailVoteAverage.setText(Float.toString(mMovie.vote_average / 2));
            // mRatingBar.setRating(mMovie.vote_average / 2);

            //Show poster and backdrop
            if (NetworkUtils.isOnline(this)) { //online
                if (!NetworkUtils.APIKEYexists()) {
                    mErrorMessageDisplay.setText(R.string.error_no_apikey);
                    showMovieReviewErrorMessage();
                } else {
                    String imgUrlString = NetworkUtils.buildImageUrlString(
                            MoviePreferences.getDefaultMoviePosterResolution(), mMovie.getPoster_path());

                    Picasso.with(mDetailPoster.getContext()).load(imgUrlString)
                            .error(R.drawable.ic_movie_placeholder)
                            .into(mDetailPoster);

                    String imgBackdropUrlString = NetworkUtils.buildImageUrlString(
                            MoviePreferences.getDefaultMoviePosterResolution(), mMovie.getBackdrop_path());

                    Picasso.with(mBackdropPoster.getContext()).load(imgBackdropUrlString)
                            .error(R.drawable.ic_movie_placeholder)
                            .into(mBackdropPoster);
                    mOfflineIcon.setVisibility(View.INVISIBLE);
                    loadMovieDetailData(mMovie.getId());
                }
            } else { //offline -- if favorites, show, else, the error is displayed
                if (mSortOrder.equalsIgnoreCase(MainActivity.SORTORDER_FAVORITES)) {
                    mGenres.setText(mMovie.getGenre_str());

                    //load from file in internal storage
                    File pImgFile = new File(this.getFilesDir(), mMovie.getPoster_path().substring(1));
                    //Log.v(TAG, "displaying file from internal dir" + imgFile.toString() + " of length:" + imgFile.length() / 1024 + "kb");
                    mDetailPoster.setImageURI(Uri.parse(pImgFile.toString()));

                    File bImgFile = new File(this.getFilesDir(), mMovie.getBackdrop_path().substring(1));
                    mBackdropPoster.setImageURI(Uri.parse(bImgFile.toString()));
                    mOfflineIcon.setVisibility(View.VISIBLE);
                    mVideosTitle.setVisibility(View.INVISIBLE);
                    mReviewsTitle.setVisibility(View.INVISIBLE);
                } else {
                    mErrorMessageDisplay.setText(R.string.error_no_internet);
                    showWifiDialog();
                }
            }
        }
    }

    public Loader<MovieDetail> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<MovieDetail>(this) {

            // Create a member variable to store the cached result in
            MovieDetail movieDetail = null;

            @Override
            public void deliverResult(MovieDetail data) {
                movieDetail = data;
                mLoadingIndicator.setVisibility(View.INVISIBLE);
                mMovieDetailData = data;
                super.deliverResult(movieDetail);
            }

            // Just call deliverResult if the cache isn't null
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if (args == null) {
                    return;
                }
                mLoadingIndicator.setVisibility(View.VISIBLE);
                if (movieDetail == null)
                    forceLoad();
                else
                    deliverResult(movieDetail);
            }

            @Override
            public MovieDetail loadInBackground() {
                String movieDetailUrlString = args.getString(MOVIE_QUERY_DETAIL_URL_EXTRA);
                if (movieDetailUrlString == null || TextUtils.isEmpty(movieDetailUrlString)) {
                    return null;
                }
                try {
                    URL movieDetailURL = new URL(movieDetailUrlString);
                    String jsonMovieDetailResponse = NetworkUtils.getResponseFromHttpUrl(movieDetailURL);
                    mMovieDetailData = MovieJsonUtils.getMovieDetailDataFromJson(jsonMovieDetailResponse);
                    return mMovieDetailData;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    // Hide the loading indicator; Show the data or the error message
    @Override
    public void onLoadFinished(Loader<MovieDetail> loader, MovieDetail movieDetailData) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        String genreStr = "";
        mMovieDetailData = movieDetailData;
        if (movieDetailData != null) {
            if (!(movieDetailData.getGenres() == null)) {
                genreStr = genresToString(movieDetailData.getGenres());
            }
            mGenres.setText(genreStr);
            if (movieDetailData.getMovieVideos().size() == 0)   //no videos
                mVideosTitle.setText(R.string.no_videos_available);
            if (mShowVideos) {
                showMovieVideoDataView();
                mMovieVideoAdapter.setMovieVideoData(mMovieDetailData.getMovieVideos());
                mVideosTitle.setVisibility(View.VISIBLE);
            } else {
                mVideosTitle.setVisibility(View.INVISIBLE);
            }
            if (movieDetailData.getMovieReviews().size() == 0) //no videos
                mReviewsTitle.setText(R.string.no_reviews_available);
            if (mShowReviews) {
                showMovieVideoDataView();
                mMovieReviewAdapter.setMovieReviewData(mMovieDetailData.getMovieReviews());
                mReviewsTitle.setVisibility(View.VISIBLE);
            } else {
                mReviewsTitle.setVisibility(View.INVISIBLE);
            }
        } else {
            if (!NetworkUtils.APIKEYexists()) {
                mErrorMessageDisplay.setText(R.string.error_no_apikey);
                showMovieVideoErrorMessage();
            } else {
                mErrorMessageDisplay.setText(R.string.error_no_internet);
                showMovieVideoErrorMessage();
            }
        }
    }

    // Override onLoaderReset as it is part of the interface we implement, but don't do anything in this method
    @Override
    public void onLoaderReset(Loader<MovieDetail> loader) {

    }

    public static String genresToString(ArrayList<String> genreStrings) {
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i < genreStrings.size(); i++) {
            strBuilder.append(genreStrings.get(i));
            if (i < (genreStrings.size() - 1))
                strBuilder.append(" | ");
        }
        String genreStr = strBuilder.toString();
        return genreStr;
    }

    /**
     * This method will get the movieid, and then tell some
     * background method to get the movie detail data (reviews and videos) in the background.
     */
    private void loadMovieDetailData(int movieId) {
        mQueryDetailUrlString = NetworkUtils.buildMovieDetailsUrl(movieId).toString();
        // If no url passed, indicate that there isn't anything to search for and return
        if (TextUtils.isEmpty(mQueryDetailUrlString)) {
            mErrorMessageDisplay.setText(R.string.error_no_query_string);
            return;
        }
        // Create a bundle called queryBundle
        Bundle detailQueryBundle = new Bundle();
        // Use putString with MOVIE_QUERY_REVIEW_URL_EXTRA as the key and the String
        // value of the URL as the value
        detailQueryBundle.putString(MOVIE_QUERY_DETAIL_URL_EXTRA, mQueryDetailUrlString);

        // Call getSupportLoaderManager and store it in a LoaderManager variable
        LoaderManager loaderManager = getSupportLoaderManager();
        // Get our Loader by calling getLoader and passing the ID we specified
        Loader<ArrayList<MovieDetail>> MovieDetailSearchLoader =
                loaderManager.getLoader(MOVIE_QUERY_DETAIL_LOADER);
        // If the Loader was null, initialize it. Else, restart it.
        if (MovieDetailSearchLoader == null) {
            loaderManager.initLoader(MOVIE_QUERY_DETAIL_LOADER, detailQueryBundle, this);
        } else {
            loaderManager.restartLoader(MOVIE_QUERY_DETAIL_LOADER, detailQueryBundle, this);
        }
    }

    // Display the menu and implement the movie sharing functionality

    /**
     * Uses the ShareCompat Intent builder to create our Movie intent for sharing. We set the
     * type of content that we are sharing (just regular text), the text itself, and we return the
     * newly created Intent.
     *
     * @return The Intent to use to start our share.
     */
    private Intent createShareMovieIntent() {
        //allow the user to share the first trailer’s YouTube URL from the movie details screen
        Intent shareIntent;
        // no detail or no videos
        if (mMovieDetailData == null) {
            //Toast.makeText(DetailActivity.this, getResources().getString(R.string.details_loading),
            //  Toast.LENGTH_LONG).show();
            shareIntent = ShareCompat.IntentBuilder.from(this)
                    .setType("text/plain")
                    .setText(getResources().getString(R.string.check_out_movie) + "-- " +
                            mMovie.toString() + "--" + MOVIE_SHARE_HASHTAG)
                    .getIntent();
        } else {
            // Log.v(TAG, "num of videos " + String.valueOf(mMovieDetailData.getMovieVideos().size()));
            // at least one video, get first one
            Uri youtubeUri = NetworkUtils.buildYoutubeUrl(mMovieDetailData.getMovieVideos().get(0).getKey());
            URL url = null;
            try {
                url = new URL(youtubeUri.toString());
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            shareIntent = ShareCompat.IntentBuilder.from(this)
                    .setType("text/plain")
                    .setText(getResources().getString(R.string.check_out_video) + "--'" +
                            mMovie.toString() + "'-- at " +
                            url.toString() + "  " + MOVIE_SHARE_HASHTAG)
                    .getIntent();
        }
        return shareIntent;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);
        MenuItem menuItem = menu.findItem(R.id.action_share);
        menuItem.setIntent(createShareMovieIntent());
        return true;
    }

    private void showWifiDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        // set title
        alertDialogBuilder.setTitle(R.string.wifi_settings);
        // set dialog message
        alertDialogBuilder
                .setMessage(R.string.dialog_nowifi)
                .setCancelable(false)
                .setPositiveButton(R.string.yes_str, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Refer: http://stackoverflow.com/questions/11215473/
                        // how-can-i-overlay-a-back-and-next-button-on-a-pick-wifi-network-window
                        Intent intent = new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK);
                        intent.putExtra("extra_prefs_show_button_bar", true);
                        startActivityForResult(intent, MoviePreferences.WIFI_REQUEST_CODE);
                    }
                })
                .setNegativeButton(R.string.no_str, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        showMovieReviewErrorMessage();
                    }
                });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show
        alertDialog.show();
    }

    /* The next 2 methods will make the View for the movie review data
    * and movie video data visible and hide the error message.
    * Since it is okay to redundantly set the visibility of a View, we don't
    * need to check whether each view is currently visible or invisible.
    */
    private void showMovieReviewDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the movie review data is visible, if sharedPreference on */
        mrRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showMovieVideoDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the movie video data is visible, if sharedPreference on */
        mvRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * The next 2 methods will make the error message visible and hide the MovieReview
     * and/or MovieVideo View.
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showMovieReviewErrorMessage() {
        /* First, hide the currently visible data */
        mrRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    private void showMovieVideoErrorMessage() {
        /* First, hide the currently visible data */
        mvRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
    }

    /**
     * This method is overridden by our MainActivity class in order to handle
     * RecyclerView item clicks. Not used, click handler is in adapter
     *
     * @param chosenMovieReview The movie review that was clicked
     */
    @Override
    public void onClick(MovieReview chosenMovieReview) {
        // Context context = this;
        // Log.v(TAG, "clicked " + chosenMovieReview.describeContents());
    }

    /**
     * This method is overridden by our MainActivity class in order to handle
     * RecyclerView item clicks.
     *
     * @param chosenMovieVideo The movie video that was clicked
     */
    @Override
    public void onClick(MovieVideo chosenMovieVideo) {
        Context context = this;
        watchYoutubeVideo(this, chosenMovieVideo.key);
    }

    public static void watchYoutubeVideo(Context context, String key) {
        Uri youtubeUri = NetworkUtils.buildYoutubeUrl(key);
        Intent appIntent = new Intent(Intent.ACTION_VIEW, youtubeUri);
        Intent webIntent = new Intent(Intent.ACTION_VIEW, youtubeUri);
        try {
            context.startActivity(appIntent);
        } catch (Exception ex) {
            context.startActivity(webIntent);
        }
    }

    /**
     * Called when the user touches the favorite button
     * Adds a new favorite to the mFavoritesDb including the current timestamp
     *
     * @param movie
     * @return void
     */
    private long addFavorite(Movie movie) {
        // Inside, create a ContentValues instance to pass the values onto the insert query
        ContentValues cv = new ContentValues();

        //Save all needed data to favorites for display offline
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_POSTER_PATH, movie.getPoster_path());
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_OVERVIEW, movie.getOverview());
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_RELEASE_DATE, movie.getRelease_date());
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_MOVIE_ID, movie.getId());
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_ORIGINAL_TITLE, movie.getOriginal_title());
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_ORIGINAL_LANGUAGE, movie.getOriginal_language());
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_BACKDROP_PATH, movie.getBackdrop_path());
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_VOTE_COUNT, movie.getVote_count());
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_VOTE_AVERAGE, movie.getVote_average());
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_GENRE_LIST, genresToString(mMovieDetailData.getGenres()));

        //download poster path and backdrop path to internal memory
        //REFER:http://stackoverflow.com/questions/4181774/show-image-view-from-file-path
        //source is imgUrlString, write to internal storage with filename poster_path (remove /)
        String imgUrlString = NetworkUtils.buildImageUrlString(
                MoviePreferences.getDefaultMoviePosterResolution(), mMovie.getPoster_path());
        imageDownload(this, imgUrlString, movie.getPoster_path().substring(1));

        imgUrlString = NetworkUtils.buildImageUrlString(
                MoviePreferences.getDefaultMoviePosterResolution(), mMovie.getBackdrop_path());
        imageDownload(this, imgUrlString, movie.getBackdrop_path().substring(1));

        // Using regular SQL commands:
        // add insert to run an insert query on TABLE_NAME with the ContentValues created
        // return MainActivity.mFavoritesDb.insert(FavoritesContract.FavoritesEntry.TABLE_NAME, null, cv);

        // Using Content Provider:
        // Insert the content values via a ContentResolver
        Uri uri = getContentResolver().insert(FavoritesContract.FavoritesEntry.CONTENT_URI, cv);

        // Display the URI that's returned with a Toast
        // [Hint] Don't forget to call finish() to return to MainActivity after this insert is complete
        long idval = 0l;
        if (uri != null) {
            //Log.v(TAG, uri.toString()0;
        } else {
            //get the _id and save
            idval = ContentUris.parseId(uri);
        }

        // If necessary, Finish activity (this returns back to MainActivity)
        //finish();
        return idval;
    }

    // Create a new function called removeFavorite that takes long id as input and returns a boolean

    /**
     * Removes the record with the specified id
     *
     * @param movieId the movieId to be removed
     * @return True: if removed successfully, False: if failed
     */
    private int removeFavorite(int movieId) {
        // Using SQL:
        // Inside, call mDb.delete to pass in the TABLE_NAME and the condition that WaitlistEntry._ID equals id
        // return MainActivity.mFavoritesDb.delete(FavoritesContract.FavoritesEntry.TABLE_NAME,
        //                FavoritesContract.FavoritesEntry.COLUMN_NAME_MOVIE_ID + "=" + movieId, null) > 0;
        Uri uri = FavoritesContract.FavoritesEntry.CONTENT_URI;
        String whereClause = FavoritesContract.FavoritesEntry.COLUMN_NAME_MOVIE_ID + "= ?";
        String[] whereArgs = new String[]{String.valueOf(movieId)};
        int numDeleted = getContentResolver().delete(uri, whereClause, whereArgs);

        //delete poster and backdrop file in local filesystem
        DetailActivity.this.deleteFile(mMovie.getPoster_path().substring(1));
        DetailActivity.this.deleteFile(mMovie.getBackdrop_path().substring(1));

        return numDeleted;
    }

    /**
     * Removes the record with the specified id
     *
     * @param movieId the movieId to be removed
     * @return True: if removed successfully, False: if failed
     */

    private boolean isFavorite(int movieId) {
        String[] tableColumns = new String[]{FavoritesContract.FavoritesEntry.COLUMN_NAME_MOVIE_ID};
        String whereClause = FavoritesContract.FavoritesEntry.COLUMN_NAME_MOVIE_ID + "= ?";
        String[] whereArgs = new String[]{String.valueOf(movieId)};

        Cursor c = MainActivity.mFavoritesDb.query(
                FavoritesContract.FavoritesEntry.TABLE_NAME,
                tableColumns,
                whereClause, whereArgs, null, null, null, null);
        if (c.getCount() > 0)
            return true;
        else
            return false;
    }

    //Save image from Picasso
    //REFER:http://stackoverflow.com/questions/32799353/saving-image-from-url-using-picasso
    //save image
    public static void imageDownload(Context ctx, String sourceUrl, String filename) {
        Picasso.with(ctx)
                .load(sourceUrl)
                .into(getTarget(ctx, filename));
    }

    //target to save
    private static Target getTarget(final Context context, final String filename) {
        Target target = new Target() {

            @Override
            public void onBitmapLoaded(final Bitmap bitmap, Picasso.LoadedFrom from) {
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        File file = new File(context.getFilesDir(), filename);
                        try {
                            file.createNewFile();
                            FileOutputStream ostream = new FileOutputStream(file);
                            //compress quality 0-100, 100 is max quality
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, ostream);
                            ostream.flush();
                            ostream.close();
                        } catch (IOException e) {
                            Log.e("IOException", e.getLocalizedMessage());
                        }
                    }
                }).start();
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };
        return target;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupSharedPreferences() {
        //Get all of the values from sharedPreferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mShowVideos = sharedPreferences.getBoolean(getString(R.string.pref_show_trailers_key),
                getResources().getBoolean(R.bool.pref_show_trailers_default));
        mShowReviews = sharedPreferences.getBoolean(getString(R.string.pref_show_reviews_key),
                getResources().getBoolean(R.bool.pref_show_reviews_default));
        //Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_show_trailers_key))) {
            mShowVideos = sharedPreferences.getBoolean(key, getResources().getBoolean(R.bool.pref_show_trailers_default));
        }
        if (key.equals(getString(R.string.pref_show_reviews_key))) {
            mShowReviews = sharedPreferences.getBoolean(key, getResources().getBoolean(R.bool.pref_show_trailers_default));
        }
        showMovieVideosPreference();
        showMovieReviewsPreference();
    }

    private void showMovieVideosPreference() {
        if (mShowVideos) {
            mMovieVideoAdapter.notifyDataSetChanged();
            showMovieVideoDataView();
            mVideosTitle.setVisibility(View.VISIBLE);
        } else {
            mVideosTitle.setVisibility(View.INVISIBLE);
        }
    }

    private void showMovieReviewsPreference() {
        if (mShowReviews) {
            mMovieReviewAdapter.notifyDataSetChanged();
            showMovieVideoDataView();
            mReviewsTitle.setVisibility(View.VISIBLE);
        } else {
            mReviewsTitle.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (showWifiDialog != null)
            showWifiDialog.dismiss();
    }
}