package com.moemke.android.popmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by aureamoemke on 27/02/2017.
 */

public final class FavoritesContract {
    /* Add content provider constants to the Contract
        Clients need to know how to access the task data, and it's your job to provide
        these content URI's for the path to that data:
        1) Content authority,
        2) Base content URI,
        3) Path(s) to the tasks directory
        4) Content URI for data in the TaskEntry class
     */

    // The authority, which is how your code knows which Content Provider to access
    public static final String AUTHORITY = "com.moemke.android.popmovies";

    // The base content URI = "content://" + <authority>
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    // Define the possible paths for accessing data in this contract
    // This is the path for the "favorites" directory
    public static final String PATH_FAVORITES = "favorites";

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private FavoritesContract() {
    }

    /* Inner class that defines the table contents */
    // based on Movie.java but with additional genre list in string form
    public static class FavoritesEntry implements BaseColumns {

        // TaskEntry content URI = base content URI + path
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITES).build();

        // Task table and column names
        public static final String TABLE_NAME = "favorites";

        // Since FavoritesEntry implements the interface "BaseColumns", it has an automatically produced
        // "_ID" column in addition to the two below
        public static final String COLUMN_NAME_MOVIE_ID = "movie_id";
        public static final String COLUMN_NAME_POSTER_PATH = "poster_path";
        public static final String COLUMN_NAME_OVERVIEW = "overview";
        public static final String COLUMN_NAME_RELEASE_DATE = "release_date";
        public static final String COLUMN_NAME_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_NAME_ORIGINAL_LANGUAGE = "original_language";
        public static final String COLUMN_NAME_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_NAME_VOTE_COUNT = "vote_count";
        public static final String COLUMN_NAME_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_NAME_GENRE_LIST = "genre_list";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";

    }
}
