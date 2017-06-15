package com.moemke.android.popmovies;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.moemke.android.popmovies.MovieAdapter.MovieAdapterOnClickHandler;
import com.moemke.android.popmovies.data.FavoritesContract;
import com.moemke.android.popmovies.data.FavoritesDbHelper;
import com.moemke.android.popmovies.data.MoviePreferences;
import com.moemke.android.popmovies.utilities.MovieJsonUtils;
import com.moemke.android.popmovies.utilities.NetworkUtils;

import java.net.URL;
import java.util.ArrayList;

import static com.moemke.android.popmovies.data.MoviePreferences.getDefaultSortOrder;

/**
 * Created by aureamoemke on 02/02/2017.
 */

//Based on S04-03-Solution-AddMapAndSharing of ud851-Sunshine exercises/
public class MainActivity extends AppCompatActivity implements MovieAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<ArrayList<Movie>>,
        AdapterView.OnItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    //constant int to identify loader
    private static final int MOVIE_LOADER = 11;

    /* A constant to save and restore the URL that is being displayed */
    private static final String MOVIE_QUERY_URL_EXTRA = "query";
    private static final String SORT_ORDER_EXTRA = "sortOrder";

    //This constant String will be used to store the chosen sort order, i.e. popular or top_rated
    private static final String SORTORDER_TEXT_KEY = "sortOrder";

    public static final String SORTORDER_POPULAR = "popular";
    public static final String SORTORDER_TOP_RATED = "top_rated";
    public static final String SORTORDER_FAVORITES = "favorites";

    private static RecyclerView mRecyclerView;

    private static MovieAdapter mMovieAdapter;

    private static TextView mErrorMessageDisplay;

    private ProgressBar mLoadingIndicator;

    public static String mSortOrder = getDefaultSortOrder();

    private Spinner spinner;
    private int mSpinnerPos = 0;

    private String mQueryUrlString;

    private AlertDialog showWifiDialog = null;

    // Add a private static boolean flag for preference updates and initialize it to false
    private static boolean PREFERENCES_HAVE_BEEN_UPDATED = false;

    Context context = this;

    //Create a local field SQLiteDatabase called mFavoritesDb
    public static SQLiteDatabase mFavoritesDb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movies);

        // Create a DB helper (this will create the DB if run for the first time)
        FavoritesDbHelper dbHelper = new FavoritesDbHelper(this);
        // Get a writable database reference using getWritableDatabase and store it in mFavoritesDb
        // Keep a reference to the mDb until paused or killed. Get a writable database
        // because you will be adding favorites
        mFavoritesDb = dbHelper.getWritableDatabase();

        /*
         * Using findViewById, we get a reference to our RecyclerView from xml. This allows us to
         * do things like set the adapter of the RecyclerView and toggle the visibility.
         */
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_movies);

        /* This TextView is used to display errors and will be hidden if there are no errors */
        mErrorMessageDisplay = (TextView) findViewById(R.id.tv_error_message_display);
        mErrorMessageDisplay.setText(R.string.error_message_unknown);

       /* GridLayoutManager
          A RecyclerView.LayoutManager implementation that lays out items in a grid.
        */
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);

        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);

        /*
         * The MovieAdapter is responsible for linking our movie data with the Views that
         * will end up displaying our movie data.
         */
        mMovieAdapter = new MovieAdapter(this);

        /* Setting the adapter attaches it to the RecyclerView in our layout. */
        mRecyclerView.setAdapter(mMovieAdapter);

        switch (mSortOrder) {
            case SORTORDER_POPULAR:
                mSpinnerPos = 0;
                break;
            case SORTORDER_TOP_RATED:
                mSpinnerPos = 1;
                break;
            case SORTORDER_FAVORITES:
                mSpinnerPos = 2;
                break;
        }

        /*
         * If savedInstanceState is not null, that means our Activity is not being started for the
         * first time. Even if the savedInstanceState is not null, it is smart to check if the
         * bundle contains the key we are looking for. In our case, we need the sort order to
         * retain the spinner position and therefore the correct choice of movies to display.
         */
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(SORTORDER_TEXT_KEY)) {
                mSortOrder = savedInstanceState.getString(SORTORDER_TEXT_KEY);
                switch (mSortOrder) {
                    case SORTORDER_POPULAR:
                        mSpinnerPos = 0;
                        break;
                    case SORTORDER_TOP_RATED:
                        mSpinnerPos = 1;
                        break;
                    case SORTORDER_FAVORITES:
                        mSpinnerPos = 2;
                }
            }
            if (savedInstanceState.containsKey(MOVIE_QUERY_URL_EXTRA)) {
                mQueryUrlString = savedInstanceState.getString(MOVIE_QUERY_URL_EXTRA);
            }
        }

        /*
         * The ProgressBar that will indicate to the user that we are loading data. It will be
         * hidden when no data is loading.
         *
         * Please note: This so called "ProgressBar" isn't a bar by default. It is more of a
         * circle. We didn't make the rules (or the names of Views), we just follow them.
         */
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        /* Once all of our views are setup, we can load the movie data. */
        loadMovieData(mSortOrder);
        /*
         * Register MainActivity as an OnPreferenceChangedListener to receive a callback when a
         * SharedPreference has changed. Please note that we must unregister MainActivity as an
         * OnSharedPreferenceChanged listener in onDestroy to avoid any memory leaks.
         */
        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    public Loader<ArrayList<Movie>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<ArrayList<Movie>>(this) {

            // Create a member variable to store the cached result in
            ArrayList<Movie> moviesJson = null;

            @Override
            public void deliverResult(ArrayList<Movie> data) {
                moviesJson = data;
                mLoadingIndicator.setVisibility(View.INVISIBLE);
                super.deliverResult(moviesJson);
            }

            // Just call deliverResult if the cache isn't null
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                if (args == null) {
                    return;
                }
                mLoadingIndicator.setVisibility(View.VISIBLE);
                if (moviesJson == null)
                    forceLoad();
                else
                    deliverResult(moviesJson);
            }

            @Override
            public ArrayList<Movie> loadInBackground() {
                ArrayList<Movie> jsonMoviesData;
                //loading movieData from tmdb
                String chosenSortOrder = args.getString(SORT_ORDER_EXTRA);
                if (chosenSortOrder.equalsIgnoreCase(SORTORDER_POPULAR) ||
                        chosenSortOrder.equalsIgnoreCase(SORTORDER_TOP_RATED)) {
                    String movieUrlString = args.getString(MOVIE_QUERY_URL_EXTRA);
                    if (movieUrlString == null || TextUtils.isEmpty(movieUrlString)) {
                        return null;
                    }
                    try {
                        URL movieURL = new URL(movieUrlString);
                        String jsonMoviesResponse = NetworkUtils.getResponseFromHttpUrl(movieURL);
                        jsonMoviesData = MovieJsonUtils.getMovieDataFromJson(jsonMoviesResponse);
                        return jsonMoviesData;
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                } else {
                    //load movieData from FavoritesContentProvider
                    try {
                        Cursor cursor = getContentResolver().query(FavoritesContract.FavoritesEntry.CONTENT_URI,
                                null,
                                null,
                                null,
                                FavoritesContract.FavoritesEntry.COLUMN_NAME_TIMESTAMP);
                        jsonMoviesData = getAllFavorites(cursor);
                        return jsonMoviesData;
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to asynchronously load data.");
                        e.printStackTrace();
                        return null;
                    }
                }
            }
        };
    }

    // Hide the loading indicator; Show the data or the error message
    @Override
    public void onLoadFinished(Loader<ArrayList<Movie>> loader, ArrayList<Movie> movieData) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if (movieData != null) {
            showMovieDataView();
            mMovieAdapter.setMovieData(movieData);
        } else {
            if (!NetworkUtils.APIKEYexists()) {
                mErrorMessageDisplay.setText(R.string.error_no_apikey);
                showErrorMessage();
            } else {
                if (NetworkUtils.isOnline(context)) {
                    if (mSortOrder.equalsIgnoreCase(SORTORDER_FAVORITES)) {
                        mErrorMessageDisplay.setText(R.string.error_no_movies);
                    } else {
                        mErrorMessageDisplay.setText(R.string.error_no_movies_found);
                    }
                } else {
                    if (mSortOrder.equalsIgnoreCase(SORTORDER_FAVORITES)) {
                        mErrorMessageDisplay.setText(R.string.error_no_movies);
                    } else {
                        mErrorMessageDisplay.setText(R.string.error_no_internet);
                    }
                }
                showErrorMessage();
            }
        }
    }

    // Override onLoaderReset as it is part of the interface we implement, but don't do anything in this method
    @Override
    public void onLoaderReset(Loader<ArrayList<Movie>> loader) {

    }

    private void loadFavorites() {
        //load favorites offline and update recycler view
        ArrayList<Movie> favoriteMoviesData;
        //load movieData from FavoritesContentProvider
        Cursor cursor = getContentResolver().query(FavoritesContract.FavoritesEntry.CONTENT_URI,
                null,
                null,
                null,
                FavoritesContract.FavoritesEntry.COLUMN_NAME_TIMESTAMP);
        favoriteMoviesData = getAllFavorites(cursor);
        showMovieDataView();
        if (!(favoriteMoviesData == null))
            mMovieAdapter.setMovieData(favoriteMoviesData);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SORTORDER_TEXT_KEY, mSortOrder);
        Bundle queryBundle = new Bundle();
        queryBundle.putString(MOVIE_QUERY_URL_EXTRA, mQueryUrlString);
    }

    /**
     * This method is overridden by our MainActivity class in order to handle RecyclerView item
     * clicks.
     *
     * @param chosenMovie The movie that was clicked
     */
    @Override
    public void onClick(Movie chosenMovie) {
        // Log.v(TAG, "Clicked Movie " + chosenMovie.getOriginal_title());
        Context context = this;
        Class destinationClass = DetailActivity.class;
        Intent intent = new Intent(context, destinationClass);
        intent.putExtra("movie", chosenMovie);
        intent.putExtra("sortOrder", mSortOrder);
        // startActivity(intent);
        // need to know when the activity returns so that it is reloaded to the correct sort order
        // on click, also need to check if offline because it shouldn't continue to detail activity
        // if so (except if Favorites -- in which case the offline data is accessed)
        if (NetworkUtils.isOnline(context)) {
            startActivityForResult(intent, MoviePreferences.DETAIL_REQUEST_CODE);
        } else {
            if (mSortOrder.equalsIgnoreCase(SORTORDER_FAVORITES)) {
                startActivityForResult(intent, MoviePreferences.DETAIL_REQUEST_CODE);
            } else {
                mErrorMessageDisplay.setText(R.string.error_no_internet);
                showErrorMessage();
                showWifiDialog();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        // need to reload to previous order (popular, toprated, favorites) on return from detail
        if (requestCode == MoviePreferences.DETAIL_REQUEST_CODE) {
            loadMovieData(mSortOrder);
        }
    }

    /**
     * This method will make the View for the movie data visible and
     * hide the error message.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    public static void showMovieDataView() {
        /* First, make sure the error is invisible */
        mErrorMessageDisplay.setVisibility(View.INVISIBLE);
        /* Then, make sure the movie data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    /**
     * This method will make the error message visible and hide the movie
     * View.
     * <p>
     * Since it is okay to redundantly set the visibility of a View, we don't
     * need to check whether each view is currently visible or invisible.
     */
    private void showErrorMessage() {
        /* First, hide the currently visible data */
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Then, show the error */
        mErrorMessageDisplay.setVisibility(View.VISIBLE);
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
                        showErrorMessage();
                    }
                });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show
        alertDialog.show();
    }

    //REFER:http://stackoverflow.com/questions/11377760/adding-spinner-to-actionbar-not-navigation
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sortorder, menu);
        MenuItem item = menu.findItem(R.id.spinner);
        spinner = (Spinner) MenuItemCompat.getActionView(item);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sort_order_array, R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter); // set the adapter to provide layout of rows and content
        spinner.setSelection(mSpinnerPos);// set spinner position to correct value
        spinner.setOnItemSelectedListener(this); // set the listener,
        // to perform actions based on item selection
        return true;
    }

    /**
     * This method will get the user's default sort order, and then tell some
     * background method to get the movie data in the background.
     */
    private void loadMovieData(String sortOrder) {
        //if online, load all from web
        if (NetworkUtils.isOnline(context)) {
            if (!NetworkUtils.APIKEYexists()) {
                mErrorMessageDisplay.setText(R.string.error_no_apikey);
                showErrorMessage();
            } else {
                mQueryUrlString = NetworkUtils.buildUrl(sortOrder).toString();
                // If no url passed, indicate that there isn't anything to search for and return
                if (TextUtils.isEmpty(mQueryUrlString)) {
                    mErrorMessageDisplay.setText(R.string.error_no_query_string);
                    return;
                }
                // Create a bundle called queryBundle
                Bundle queryBundle = new Bundle();
                // Use putString with MOVIE_QUERY_URL_EXTRA as the key and the String value of the URL as the value
                queryBundle.putString(SORT_ORDER_EXTRA, mSortOrder);
                queryBundle.putString(MOVIE_QUERY_URL_EXTRA, mQueryUrlString);

                // Call getSupportLoaderManager and store it in a LoaderManager variable
                LoaderManager loaderManager = getSupportLoaderManager();
                // Get our Loader by calling getLoader and passing the ID we specified
                Loader<ArrayList<Movie>> movieSearchLoader = loaderManager.getLoader(MOVIE_LOADER);
                // If the Loader was null, initialize it. Else, restart it.
                if (movieSearchLoader == null) {
                    loaderManager.initLoader(MOVIE_LOADER, queryBundle, this);
                } else {
                    loaderManager.restartLoader(MOVIE_LOADER, queryBundle, this);
                }
            }
        } else {//offline
            if (!(mSortOrder.equalsIgnoreCase(SORTORDER_FAVORITES))) { //not favorites, show error
                mErrorMessageDisplay.setText(R.string.error_no_internet);
                showErrorMessage();
                showWifiDialog();
            } else {//favorites, load adapter from favorites db locally
                loadFavorites();
            }
        }
    }

    /**
     * This method is used when we are resetting data, so that at one point in time during a
     * refresh of our data, you can see that there is no data showing.
     */
    private void invalidateData() {
        mMovieAdapter.setMovieData(null);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
        switch (pos) {
            case 0:
                mSortOrder = SORTORDER_POPULAR;
                mSpinnerPos = 0;
                spinner.setSelection(0);
                break;
            case 1:
                mSortOrder = SORTORDER_TOP_RATED;
                mSpinnerPos = 1;
                spinner.setSelection(1);
                break;
            case 2:
                mSortOrder = SORTORDER_FAVORITES;
                mSpinnerPos = 2;
                spinner.setSelection(2);
                break;
        }
        loadMovieData(mSortOrder);
//        mMovieAdapter.notifyDataSetChanged();
        return;
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // Another interface callback
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // In onStart, if preferences have been changed, refresh the data and set the flag to false

    /**
     * OnStart is called when the Activity is coming into view. This happens when the Activity is
     * first created, but also happens when the Activity is returned to from another Activity. We
     * are going to use the fact that onStart is called when the user returns to this Activity to
     * check if the location setting or the preferred units setting has changed. If it has changed,
     * we are going to perform a new query.
     */
    @Override
    protected void onStart() {
        super.onStart();
        /*
         * If the preferences for have changed since the user was last in
         * MainActivity, perform another query and set the flag to false.
         *
         */
        if (PREFERENCES_HAVE_BEEN_UPDATED) {
            Log.d(TAG, "onStart: preferences were updated");
            getSupportLoaderManager().restartLoader(MOVIE_LOADER, null, this);
            PREFERENCES_HAVE_BEEN_UPDATED = false;
        }
    }

    // Override onDestroy and unregister MainActivity as a SharedPreferenceChangedListener
    @Override
    protected void onDestroy() {
        super.onDestroy();
        /* Unregister MainActivity as an OnPreferenceChangedListener to avoid any memory leaks. */
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    // Override onSharedPreferenceChanged to set the preferences flag to true
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        /*
         * Set this flag to true so that when control returns to MainActivity, it can refresh the
         * data.
         *
         * This isn't the ideal solution because there really isn't a need to perform another
         * GET request just to change the units, but this is the simplest solution that gets the
         * job done for now. Later in this course, we are going to show you more elegant ways to
         * handle converting the units from celsius to fahrenheit and back without hitting the
         * network again by keeping a copy of the data in a manageable format.
         */
        PREFERENCES_HAVE_BEEN_UPDATED = true;
        if (key.equals(getString(R.string.pref_show_reviews_key))) {
            boolean show_reviews = sharedPreferences.getBoolean(key, getResources()
                    .getBoolean(R.bool.pref_show_reviews_default));
            Log.v(TAG, "Show user reviews value:" + show_reviews);
        }
        if (key.equals(getString(R.string.pref_show_trailers_key))) {
            boolean show_trailers = sharedPreferences.getBoolean(key, getResources()
                    .getBoolean(R.bool.pref_show_reviews_default));
            Log.v(TAG, "Show movie trailers value:" + show_trailers);
        }
    }
    //  Create a private method called getAllFavorites that returns a cursor

    /**
     * Query the mFavoritesDb and get all the favorites from the favorites table
     *
     * @return Cursor containing the list of favorite Movies
     */
    private ArrayList<Movie> getAllFavorites(Cursor cursor) {
        // Inside, call query on mFavoritesDb passing in the table name and projection String []
        // order by COLUMN_TIMESTAMP
        //        USED FOR SQLite DB commands
        //  Cursor cursor = mFavoritesDb.query(
        //                FavoritesContract.FavoritesEntry.TABLE_NAME,
        //                null,
        //                null,
        //                null,
        //                null,
        //                null,
        //                FavoritesContract.FavoritesEntry.COLUMN_NAME_TIMESTAMP
        //        );
        ArrayList<Movie> favorites = new ArrayList<Movie>();
        Movie movie;
        String pp;
        String ov;
        String rd;
        int id;
        String ot;
        String ol;
        String bp;
        int vc;
        float va;
        String gs;
        //int is_favorite
        if (!(cursor.moveToFirst()) || cursor.getCount() == 0) {
            //cursor is empty
            return null;
        } else {
            cursor.moveToFirst();
            try {
                do {
                    pp = cursor.getString(cursor.getColumnIndex
                            (FavoritesContract.FavoritesEntry.COLUMN_NAME_POSTER_PATH));
                    ov = cursor.getString(cursor.getColumnIndex
                            (FavoritesContract.FavoritesEntry.COLUMN_NAME_OVERVIEW));
                    rd = cursor.getString(cursor.getColumnIndex
                            (FavoritesContract.FavoritesEntry.COLUMN_NAME_RELEASE_DATE));
                    id = cursor.getInt(cursor.getColumnIndex
                            (FavoritesContract.FavoritesEntry.COLUMN_NAME_MOVIE_ID));
                    ot = cursor.getString(cursor.getColumnIndex
                            (FavoritesContract.FavoritesEntry.COLUMN_NAME_ORIGINAL_TITLE));
                    ol = cursor.getString(cursor.getColumnIndex
                            (FavoritesContract.FavoritesEntry.COLUMN_NAME_ORIGINAL_LANGUAGE));
                    bp = cursor.getString(cursor.getColumnIndex
                            (FavoritesContract.FavoritesEntry.COLUMN_NAME_BACKDROP_PATH));
                    vc = cursor.getInt(cursor.getColumnIndex
                            (FavoritesContract.FavoritesEntry.COLUMN_NAME_VOTE_COUNT));
                    va = cursor.getFloat(cursor.getColumnIndex
                            (FavoritesContract.FavoritesEntry.COLUMN_NAME_VOTE_AVERAGE));
                    gs = cursor.getString(cursor.getColumnIndex
                            (FavoritesContract.FavoritesEntry.COLUMN_NAME_GENRE_LIST));
                    movie = new Movie(pp, ov, rd, id, ot, ol,
                            bp, vc, va, 1, gs); //is_favorite - set to 1

                    favorites.add(movie);
                }
                while (cursor.moveToNext());

            } finally {
                cursor.close();
            }
            return favorites;
        }
    }

    /**
     * Update the movieData list with favorites from the favorites file
     * Adds a new favorite to the mFavoritesDb including the current timestamp
     *
     * @param movieData
     * @return id of new record added
     */
    //If the movie id is in the favorites table, then set the movie is_favorite to 1
    private ArrayList<Movie> setFavorites(ArrayList<Movie> movieData) {
        // create a ContentValues instance to pass the values onto the insert query
        Cursor c;
        int i = 0;
        String[] tableColumns;
        String whereClause;
        String[] whereArgs;
        for (Movie m : movieData) {
            tableColumns = new String[]{FavoritesContract.FavoritesEntry.COLUMN_NAME_MOVIE_ID};
            whereClause = FavoritesContract.FavoritesEntry.COLUMN_NAME_MOVIE_ID + "= ?";
            whereArgs = new String[]{String.valueOf(m.getId())};
            //Using sql
            // c = mFavoritesDb.query(FavoritesContract.FavoritesEntry.TABLE_NAME,
            //                    tableColumns,
            //                    whereClause,
            //                    whereArgs,
            //                    null, null, null, null);
            // Using contentProvider
            Cursor cursor = getContentResolver().query(FavoritesContract.FavoritesEntry.CONTENT_URI,
                    tableColumns,
                    whereClause,
                    whereArgs,
                    FavoritesContract.FavoritesEntry.COLUMN_NAME_TIMESTAMP);

            if (cursor.getCount() > 0) {
                movieData.get(i).setIs_favorite(1);
            } else {
                movieData.get(i).setIs_favorite(0);
            }
            i++;
        }
        return movieData;
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(showWifiDialog != null)
            showWifiDialog.dismiss();
    }
}
