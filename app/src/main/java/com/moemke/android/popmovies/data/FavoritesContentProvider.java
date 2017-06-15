package com.moemke.android.popmovies.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by aureamoemke on 01/03/2017.
 */

public class FavoritesContentProvider extends ContentProvider {

    private FavoritesDbHelper mFavoritesDbHelper;

    // Define final integer constants for the directory of favorites and a single item.
    // It's convention to use 100, 200, 300, etc for directories,
    // and related ints (101, 102, ..) for items in that directory.
    public static final int FAVORITES = 100;
    public static final int FAVORITE_WITH_ID = 101;


    // Declare a static variable for the Uri matcher that you construct
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    // Define a static buildUriMatcher method that associates URI's with their int match

    /**
     * Initialize a new matcher object without any matches,
     * then use .addURI(String authority, String path, int match) to add matches
     */
    public static UriMatcher buildUriMatcher() {

        // Initialize a UriMatcher with no matches by passing in NO_MATCH to the constructor
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        /*
          All paths added to the UriMatcher have a corresponding int.
          For each kind of uri you may want to access, add the corresponding match with addURI.
          The two calls below add matches for the task directory and a single item by ID.
         */
        uriMatcher.addURI(FavoritesContract.AUTHORITY, FavoritesContract.PATH_FAVORITES, FAVORITES);
        uriMatcher.addURI(FavoritesContract.AUTHORITY, FavoritesContract.PATH_FAVORITES + "/#",
                FAVORITE_WITH_ID);

        return uriMatcher;
    }

    /**
     * Handles requests to insert a set of new rows. In Popular Movies, we are not inserting
     * multiple rows. There is no use case for this, but just to provide complete functionality.
     *
     * @param uri    The content:// URI of the insertion request.
     * @param values An array of sets of column_name/value pairs to add to the database.
     *               This must not be {@code null}.
     * @return The number of values that were inserted.
     */
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        final SQLiteDatabase db = mFavoritesDbHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {

            // Only perform our implementation of bulkInsert if the URI matches the FAVORITES code
            case FAVORITES:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(FavoritesContract.FavoritesEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                // Return the number of rows inserted from our implementation of bulkInsert
                return rowsInserted;

            // If the URI does match match FAVORITE, return the super implementation of bulkInsert
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // Implement insert to handle requests to insert a single new row of data
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // Get access to the task database (to write new data to)
        final SQLiteDatabase db = mFavoritesDbHelper.getWritableDatabase();

        // Write URI matching code to identify the match for the tasks directory
        int match = sUriMatcher.match(uri);
        Uri returnUri; // URI to be returned

        switch (match) {
            case FAVORITES:
                // Insert new values into the database
                // Inserting values into favorites table (long id is the new id auto-generated)
                long id = db.insert(FavoritesContract.FavoritesEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    returnUri = ContentUris.withAppendedId(FavoritesContract.FavoritesEntry.CONTENT_URI, id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            // Set the value for the returnedUri and write the default case for unknown URI's
            // Default case throws an UnsupportedOperationException
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver if the uri has been changed, and return the newly inserted URI
        getContext().getContentResolver().notifyChange(uri, null);

        // Return constructed uri (this points to the newly inserted row of data)
        return returnUri;
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mFavoritesDbHelper = new FavoritesDbHelper(context);
        return true;
    }

    // Implement query to handle requests for data by URI
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        // Get access to underlying database (read-only for query)
        final SQLiteDatabase db = mFavoritesDbHelper.getReadableDatabase();

        // Write URI match code and set a variable to return a Cursor
        int match = sUriMatcher.match(uri);
        Cursor retCursor;

        // COMPLETED (3) Query for the tasks directory and write a default case
        switch (match) {
            // Query for the tasks directory
            case FAVORITES:
                retCursor = db.query(FavoritesContract.FavoritesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case FAVORITE_WITH_ID:
                //case to query for a single row of data by ID
                // Get the favorite ID from the URI path
                //using selection and selection args
                //uri: content://<authority>/favorites/#
                // index 0 is the "/favorites" portion in uri path
                String id = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                //index 1 is the "#" portion of the uri

                //selection is the "_id=?" and the selection args is the row id from the uri
                String mSelection = "_id=?";
                String[] mSelectionArgs = new String[]{id};
                retCursor = db.query(FavoritesContract.FavoritesEntry.TABLE_NAME,
                        projection,
                        mSelection,
                        mSelectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            // Default exception
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Set a notification URI on the Cursor and return that Cursor
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);

        // Return the desired Cursor
        return retCursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    //  Implement the delete method of the ContentProvider
    /**
     * Deletes data at a given URI with optional arguments for more fine tuned deletions.
     *
     * @param uri           The full URI to query
     * @param selection     An optional restriction to apply to rows when deleting.
     * @param selectionArgs Used in conjunction with the selection statement
     * @return The number of rows deleted
     */
    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {

        // Get access to the database and write URI matching code to recognize a single item
        final SQLiteDatabase db = mFavoritesDbHelper.getWritableDatabase();

        int match = sUriMatcher.match(uri);
        // Keep track of the number of deleted tasks
        int favoritesDeleted; // starts as 0

        // Write the code to delete a single row of data
        // [Hint] Use selections to delete an item by its row ID
        switch (match) {
            case FAVORITES:
                favoritesDeleted = db.delete(FavoritesContract.FavoritesEntry.TABLE_NAME,
                        selection, selectionArgs);
                break;
            // Handle the single item case, recognized by the ID included in the URI path
            case FAVORITE_WITH_ID:
                // Get the task ID from the URI path, index 1 is the # part of the uri
                // index 0 is the /favorites part in uri path
                String id = uri.getPathSegments().get(1);
                // Use selections/selectionArgs to filter for this ID
                favoritesDeleted = db.delete(FavoritesContract.FavoritesEntry.TABLE_NAME,
                        "id=?", new String[]{String.valueOf(id)});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Notify the resolver of a change and return the number of items deleted
        if (favoritesDeleted != 0) {
            // A task was deleted, set notification
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // Return the number of tasks deleted
        return favoritesDeleted;
    }

    // Update won't be used in the final Favorites app but is implemented here for completeness
    // This updates a single item (by it's ID) in the tasks directory
    //REFER: https://developer.android.com/guide/topics/providers/content-provider-basics.html#Updating
    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {

        //Keep track of if an update occurs
        int favoritesUpdated;

        // match code
        int match = sUriMatcher.match(uri);

        switch (match) {
            case FAVORITES:
                favoritesUpdated = mFavoritesDbHelper.getWritableDatabase()
                        .update(FavoritesContract.FavoritesEntry.TABLE_NAME,
                                values,
                                selection,
                                selectionArgs);

                break;
            case FAVORITE_WITH_ID:
                //update a single task by getting the id
                String id = uri.getPathSegments().get(1);
                //using selections
                favoritesUpdated = mFavoritesDbHelper.getWritableDatabase()
                        .update(FavoritesContract.FavoritesEntry.TABLE_NAME, values, "_id=?", new String[]{id});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (favoritesUpdated != 0) {
            //set notifications if a task was updated
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // return number of favorites updated
        return favoritesUpdated;
    }

}
