package com.moemke.android.popmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.moemke.android.popmovies.data.FavoritesContract.FavoritesEntry;

/**
 * Created by aureamoemke on 27/02/2017.
 */

public class FavoritesDbHelper extends SQLiteOpenHelper {
    // The database name
    private static final String DATABASE_NAME = "favorites.db";

    // DATABASE_VERSION initially set to 1
    // If you change the database schema, you must increment the database version
    private static final int DATABASE_VERSION = 3;

    // Create a Constructor that takes a context and calls the parent constructor
    // Constructor
    public FavoritesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Override the onCreate method
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold favorites data
        final String SQL_CREATE_FAVORITES_TABLE = "CREATE TABLE " + FavoritesEntry.TABLE_NAME + " (" +
                FavoritesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FavoritesEntry.COLUMN_NAME_MOVIE_ID + " INTEGER NOT NULL, " +
                FavoritesEntry.COLUMN_NAME_POSTER_PATH + " TEXT, " +
                FavoritesEntry.COLUMN_NAME_OVERVIEW + " TEXT, " +
                FavoritesEntry.COLUMN_NAME_RELEASE_DATE + " TEXT, " +
                FavoritesEntry.COLUMN_NAME_ORIGINAL_TITLE + " TEXT NOT NULL, " +
                FavoritesEntry.COLUMN_NAME_ORIGINAL_LANGUAGE + " TEXT, " +
                FavoritesEntry.COLUMN_NAME_BACKDROP_PATH + " TEXT, " +
                FavoritesEntry.COLUMN_NAME_VOTE_COUNT + " INTEGER, " +
                FavoritesEntry.COLUMN_NAME_VOTE_AVERAGE + " FLOAT, " +
                FavoritesEntry.COLUMN_NAME_GENRE_LIST + " TEXT, " +
                FavoritesEntry.COLUMN_NAME_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "UNIQUE (" + FavoritesEntry.COLUMN_NAME_MOVIE_ID + ") ON CONFLICT REPLACE" +
                ");";

        // Execute the query by calling execSQL on sqLiteDatabase and pass the string query SQL_CREATE_FAVORITES_TABLE
        sqLiteDatabase.execSQL(SQL_CREATE_FAVORITES_TABLE);
    }

    // Override the onUpgrade method
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        // For now simply drop the table and create a new one. This means if you change the
        // DATABASE_VERSION the table will be dropped.
        // In a production app, this method might be modified to ALTER the table
        // instead of dropping it, so that existing data is not deleted.
        // Inside, execute a drop table query, and then call onCreate to re-create it
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + FavoritesEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
