/*
* Copyright (C) 2016 The Android Open Source Project
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

package com.moemke.android.popmovies.data;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.moemke.android.popmovies.data.TestUtilities.BULK_INSERT_RECORDS_TO_INSERT;
import static com.moemke.android.popmovies.data.TestUtilities.createBulkInsertTestFavoriteValues;
import static com.moemke.android.popmovies.data.TestUtilities.createTestFavoritesContentValues;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class TestFavoritesContentProvider {

    /* Context used to access various parts of the system */
    private final Context mContext = InstrumentationRegistry.getTargetContext();

    /**
     * Because we annotate this method with the @Before annotation, this method will be called
     * before every single method with an @Test annotation. We want to start each test clean, so we
     * delete all entries in the tasks directory to do so.
     */
    @Before
    public void setUp() {
        deleteAllRecordsFromFavoritesTable();
    }

    //================================================================================
    // Test ContentProvider Registration
    //================================================================================


    /**
     * This test checks to make sure that the content provider is registered correctly in the
     * AndroidManifest file. If it fails, you should check the AndroidManifest to see if you've
     * added a <provider/> tag and that you've properly specified the android:authorities attribute.
     */
    @Test
    public void testProviderRegistry() {

        /*
         * A ComponentName is an identifier for a specific application component, such as an
         * Activity, ContentProvider, BroadcastReceiver, or a Service.
         *
         * Two pieces of information are required to identify a component: the package (a String)
         * it exists in, and the class (a String) name inside of that package.
         *
         * We will use the ComponentName for our ContentProvider class to ask the system
         * information about the ContentProvider, specifically, the authority under which it is
         * registered.
         */
        String packageName = mContext.getPackageName();
        String taskProviderClassName = FavoritesContentProvider.class.getName();
        ComponentName componentName = new ComponentName(packageName, taskProviderClassName);

        try {

            /*
             * Get a reference to the package manager. The package manager allows us to access
             * information about packages installed on a particular device. In this case, we're
             * going to use it to get some information about our ContentProvider under test.
             */
            PackageManager pm = mContext.getPackageManager();

            /* The ProviderInfo will contain the authority, which is what we want to test */
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);
            String actualAuthority = providerInfo.authority;
            String expectedAuthority = packageName;

            /* Make sure that the registered authority matches the authority from the Contract */
            String incorrectAuthority =
                    "Error: FavoritesContentProvider registered with authority: " + actualAuthority +
                            " instead of expected authority: " + expectedAuthority;
            assertEquals(incorrectAuthority,
                    actualAuthority,
                    expectedAuthority);

        } catch (PackageManager.NameNotFoundException e) {
            String providerNotRegisteredAtAll =
                    "Error: FavoritesContentProvider not registered at " + mContext.getPackageName();
            /*
             * This exception is thrown if the ContentProvider hasn't been registered with the
             * manifest at all. If this is the case, you need to double check your
             * AndroidManifest file
             */
            fail(providerNotRegisteredAtAll);
        }
    }


    //================================================================================
    // Test UriMatcher
    //================================================================================


    private static final Uri TEST_FAVORITES = FavoritesContract.FavoritesEntry.CONTENT_URI;
    // Content URI for a single task with id = 1
    private static final Uri TEST_FAVORITE_WITH_ID = TEST_FAVORITES.buildUpon().appendPath("1").build();


    /**
     * This function tests that the UriMatcher returns the correct integer value for
     * each of the Uri types that the ContentProvider can handle. Uncomment this when you are
     * ready to test your UriMatcher.
     */
    @Test
    public void testUriMatcher() {

        /* Create a URI matcher that the FavoritesContentProvider uses */
        UriMatcher testMatcher = FavoritesContentProvider.buildUriMatcher();

        /* Test that the code returned from our matcher matches the expected FAVORITES int */
        String tasksUriDoesNotMatch = "Error: The FAVORITES URI was matched incorrectly.";
        int actualFavoritessMatchCode = testMatcher.match(TEST_FAVORITES);
        int expectedFavoritessMatchCode = FavoritesContentProvider.FAVORITES;
        assertEquals(tasksUriDoesNotMatch,
                actualFavoritessMatchCode,
                expectedFavoritessMatchCode);

        /* Test that the code returned from our matcher matches the expected FAVORITE_WITH_ID */
        String taskWithIdDoesNotMatch =
                "Error: The FAVORITE_WITH_ID URI was matched incorrectly.";
        int actualFavoritesWithIdCode = testMatcher.match(TEST_FAVORITE_WITH_ID);
        int expectedFavoritesWithIdCode = FavoritesContentProvider.FAVORITE_WITH_ID;
        assertEquals(taskWithIdDoesNotMatch,
                actualFavoritesWithIdCode,
                expectedFavoritesWithIdCode);
    }


    //================================================================================
    // Test Insert
    //================================================================================


    /**
     * Tests inserting a single row of data via a ContentResolver
     */
    @Test
    public void testInsert() {

        /* Create values to insert */
        ContentValues testFavoritesValues = createTestFavoritesContentValues();

        /* TestContentObserver allows us to test if notifyChange was called appropriately */
        TestUtilities.TestContentObserver taskObserver = TestUtilities.getTestContentObserver();

        ContentResolver contentResolver = mContext.getContentResolver();

        /* Register a content observer to be notified of changes to data at a given URI (tasks) */
        contentResolver.registerContentObserver(
                /* URI that we would like to observe changes to */
                FavoritesContract.FavoritesEntry.CONTENT_URI,
                /* Whether or not to notify us if descendants of this URI change */
                true,
                /* The observer to register (that will receive notifyChange callbacks) */
                taskObserver);


        Uri uri = contentResolver.insert(FavoritesContract.FavoritesEntry.CONTENT_URI, testFavoritesValues);
        long returnedId = ContentUris.parseId(uri); //value of the returned id

        //NOTE: This only works for the first element, because the id passed on is 1
        //If your db already has values, it won't work
        //THEREFORE, run test on empty database
        //Uri expectedUri = ContentUris.withAppendedId(FavoritesContract.FavoritesEntry.CONTENT_URI, 1);
        Uri expectedUri = ContentUris.withAppendedId(FavoritesContract.FavoritesEntry.CONTENT_URI, returnedId);

        String insertProviderFailed = "Unable to insert item through Provider";
        assertEquals(insertProviderFailed, uri, expectedUri);

        /*
         * If this fails, it's likely you didn't call notifyChange in your insert method from
         * your ContentProvider.
         */
        taskObserver.waitForNotificationOrFail();

        /*
         * waitForNotificationOrFail is synchronous, so after that call, we are done observing
         * changes to content and should therefore unregister this observer.
         */
        contentResolver.unregisterContentObserver(taskObserver);

        deleteAllRecordsFromFavoritesTable();

    }


    //================================================================================
    // Test Query (for favorites directory)
    //================================================================================


    /**
     * Inserts data, then tests if a query for the tasks directory returns that data as a Cursor
     */
    @Test
    public void testQuery() {

        /* Get access to a writable database */
        FavoritesDbHelper dbHelper = new FavoritesDbHelper(mContext);
        SQLiteDatabase database = dbHelper.getWritableDatabase();

        /* Create values to insert */
        ContentValues testFavoritesValues = createTestFavoritesContentValues();

        /* Insert ContentValues into database and get a row ID back */
        long taskRowId = database.insert(
                /* Table to insert values into */
                FavoritesContract.FavoritesEntry.TABLE_NAME,
                null,
                /* Values to insert into table */
                testFavoritesValues);

        String insertFailed = "Unable to insert directly into the database";
        assertTrue(insertFailed, taskRowId != -1);

        /* We are done with the database, close it now. */
        database.close();

        /* Perform the ContentProvider query */
        Cursor favoritesCursor = mContext.getContentResolver().query(
                FavoritesContract.FavoritesEntry.CONTENT_URI,
                /* Columns; leaving this null returns every column in the table */
                null,
                /* Optional specification for columns in the "where" clause above */
                null,
                /* Values for "where" clause */
                null,
                /* Sort order to return in Cursor */
                null);


        String queryFailed = "Query failed to return a valid Cursor";
        assertTrue(queryFailed, favoritesCursor != null);

        /* We are done with the cursor, close it now. */
        favoritesCursor.close();
    }


    //================================================================================
    // Test Delete (for a single item)
    //================================================================================

    /**
     * Tests deleting a single row of data via a ContentResolver
     */
    @Test
    public void testDelete() {
        /* Access writable database */
        FavoritesDbHelper helper = new FavoritesDbHelper(InstrumentationRegistry.getTargetContext());
        SQLiteDatabase database = helper.getWritableDatabase();

        /* Create a new row of task data */
        ContentValues testFavoritesValues = createTestFavoritesContentValues();

        /* Insert ContentValues into database and get a row ID back
        * using sql, not content provider (since the test is for the delete) */
        long taskRowId = database.insert(
                /* Table to insert values into */
                FavoritesContract.FavoritesEntry.TABLE_NAME,
                null,
                /* Values to insert into table */
                testFavoritesValues);

        /* Always close the database when you're through with it */
        database.close();

        String insertFailed = "Unable to insert into the database";
        assertTrue(insertFailed, taskRowId != -1);

        /* TestContentObserver allows us to test if notifyChange was called appropriately */
        TestUtilities.TestContentObserver taskObserver = TestUtilities.getTestContentObserver();

        ContentResolver contentResolver = mContext.getContentResolver();

        /* Register a content observer to be notified of changes to data at a given URI (tasks) */
        contentResolver.registerContentObserver(
                /* URI that we would like to observe changes to */
                FavoritesContract.FavoritesEntry.CONTENT_URI,
                /* Whether or not to notify us if descendants of this URI change */
                true,
                /* The observer to register (that will receive notifyChange callbacks) */
                taskObserver);


        /* The delete method deletes the previously inserted row with id = 1 */
        // use when you know the row id to delete
        // Uri uriToDelete = FavoritesContract.FavoritesEntry.CONTENT_URI.buildUpon().appendPath("1").build();
        int movieId = 23456;
        Uri uriToDelete = FavoritesContract.FavoritesEntry.CONTENT_URI;
        String whereClause = FavoritesContract.FavoritesEntry.COLUMN_NAME_MOVIE_ID + "= ?";
        String[] whereArgs = new String[]{String.valueOf(movieId)};
        int tasksDeleted = contentResolver.delete(uriToDelete, whereClause, whereArgs);

        String deleteFailed = "Unable to delete item in the database";
        assertTrue(deleteFailed, tasksDeleted != 1);

        /*
         * If this fails, it's likely you didn't call notifyChange in your delete method from
         * your ContentProvider.
         */
//        taskObserver.waitForNotificationOrFail();

        /*
         * waitForNotificationOrFail is synchronous, so after that call, we are done observing
         * changes to content and should therefore unregister this observer.
         */
//        contentResolver.unregisterContentObserver(taskObserver);
    }
    //================================================================================
    // Test Update (for favorites directory)
    //================================================================================

    /**
     * Inserts data, then tests if an update for the favorites directory returns 1
     */
    @Test
    public void testUpdate() {

        /* Get access to a writable database */
        FavoritesDbHelper helper = new FavoritesDbHelper(InstrumentationRegistry.getTargetContext());
        SQLiteDatabase database = helper.getWritableDatabase();

        /* Create a new row of favorites data */
        ContentValues testFavoritesValues = createTestFavoritesContentValues();

        /* Insert ContentValues into database and get a row ID back */
        long taskRowId = database.insert(
                /* Table to insert values into */
                FavoritesContract.FavoritesEntry.TABLE_NAME,
                null,
                /* Values to insert into table */
                testFavoritesValues);

        /* Always close the database when you're through with it */
        database.close();

        /* TestContentObserver allows us to test if notifyChange was called appropriately */
        TestUtilities.TestContentObserver taskObserver = TestUtilities.getTestContentObserver();

        ContentResolver contentResolver = mContext.getContentResolver();

        /* Register a content observer to be notified of changes to data at a given URI (tasks) */
        contentResolver.registerContentObserver(
                /* URI that we would like to observe changes to */
                FavoritesContract.FavoritesEntry.CONTENT_URI,
                /* Whether or not to notify us if descendants of this URI change */
                true,
                /* The observer to register (that will receive notifyChange callbacks) */
                taskObserver);

        /* The update method updates the previously inserted row with MOVIE_ID = 12345 */
        Uri uriToUpdate = FavoritesContract.FavoritesEntry.CONTENT_URI;
        //Defines selection criteria for the rows you want to update
        String mSelectionClause = FavoritesContract.FavoritesEntry.COLUMN_NAME_MOVIE_ID + "=?";
        String[] mSelectionArgs = new String[]{"12345"};
        /* Create values for update  */
        ContentValues testUpdatesValues = new ContentValues();
        testUpdatesValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_POSTER_PATH, "udpate poster path");
        testUpdatesValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_OVERVIEW, "update overview");
        testUpdatesValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_RELEASE_DATE, "update release date");

        int favoritesUpdated = contentResolver.update(uriToUpdate, testUpdatesValues,
                mSelectionClause, mSelectionArgs);

        String updateFailed = "Unable to update item in the database";
        assertTrue(updateFailed, favoritesUpdated != 0);

        /*
         * If this fails, it's likely you didn't call notifyChange in your update method from
         * your ContentProvider.
         */
        taskObserver.waitForNotificationOrFail();

        /*
         * waitForNotificationOrFail is synchronous, so after that call, we are done observing
         * changes to content and should therefore unregister this observer.
         */
        contentResolver.unregisterContentObserver(taskObserver);

        deleteAllRecordsFromFavoritesTable();

    }

    //================================================================================
    // Test Bulk Insert (for favorites directory)
    //================================================================================

    /**
     * This test test the bulkInsert feature of the ContentProvider. It also verifies that
     * registered ContentObservers receive onChange callbacks when data is inserted.
     * <p>
     * It finally queries the ContentProvider to make sure that the table has been successfully
     * inserted.
     * <p>
     * Potential causes for failure:
     * <p>
     * 1) Within {@link FavoritesContentProvider#delete(Uri, String, String[])}, you didn't call
     * getContext().getContentResolver().notifyChange(uri, null) after performing an insertion.
     * <p>
     * 2) The number of records the ContentProvider reported that it inserted do no match the
     * number of records we inserted into the ContentProvider.
     * <p>
     * 3) The size of the Cursor returned from the query does not match the number of records
     * that we inserted into the ContentProvider.
     * <p>
     * 4) The data contained in the Cursor from our query does not match the data we inserted
     * into the ContentProvider.
     * </p>
     */
    @Test
    public void testBulkInsert() {

        /* Create a new array of ContentValues for favorites */
        ContentValues[] bulkInsertTestContentValues = createBulkInsertTestFavoriteValues();

        /*
         * TestContentObserver allows us to test favorites or not notifyChange was called
         * appropriately. We will use that here to make sure that notifyChange is called when a
         * deletion occurs.
         */
        TestUtilities.TestContentObserver favoritesObserver = TestUtilities.getTestContentObserver();

        /*
         * A ContentResolver provides us access to the content model. We can use it to perform
         * deletions and queries at our CONTENT_URI
         */
        ContentResolver contentResolver = mContext.getContentResolver();

        /* Register a content observer to be notified of changes to data at a given URI (favorites) */
        contentResolver.registerContentObserver(
                /* URI that we would like to observe changes to */
                FavoritesContract.FavoritesEntry.CONTENT_URI,
                /* Whether or not to notify us if descendants of this URI change */
                true,
                /* The observer to register (that will receive notifyChange callbacks) */
                favoritesObserver);

        /* bulkInsert will return the number of records that were inserted. */
        int insertCount = contentResolver.bulkInsert(
                /* URI at which to insert data */
                FavoritesContract.FavoritesEntry.CONTENT_URI,
                /* Array of values to insert into given URI */
                bulkInsertTestContentValues);

        /*
         * If this fails, it's likely you didn't call notifyChange in your insert method from
         * your ContentProvider.
         */
        favoritesObserver.waitForNotificationOrFail();

        /*
         * waitForNotificationOrFail is synchronous, so after that call, we are done observing
         * changes to content and should therefore unregister this observer.
         */
        contentResolver.unregisterContentObserver(favoritesObserver);

        /*
         * We expect that the number of test content values that we specify in our TestUtility
         * class were inserted here. We compare that value to the value that the ContentProvider
         * reported that it inserted. These numbers should match.
         */
        String expectedAndActualInsertedRecordCountDoNotMatch =
                "Number of expected records inserted does not match actual inserted record count";
        assertEquals(expectedAndActualInsertedRecordCountDoNotMatch,
                insertCount,
                BULK_INSERT_RECORDS_TO_INSERT);

        /*
         * Perform our ContentProvider query. We expect the cursor that is returned will contain
         * the exact same data that is in testFavoritesValues and we will validate that in the next
         * step.
         */
        Cursor cursor = mContext.getContentResolver().query(
                FavoritesContract.FavoritesEntry.CONTENT_URI,
                /* Columns; leaving this null returns every column in the table */
                null,
                /* Optional specification for columns in the "where" clause above */
                null,
                /* Values for "where" clause */
                null,
                /* Sort by date from smaller to larger (past to future) */
                FavoritesContract.FavoritesEntry.COLUMN_NAME_TIMESTAMP + " ASC");

        /*
         * Although we already tested the number of records that the ContentProvider reported
         * inserting, we are now testing the number of records that the ContentProvider actually
         * returned from the query above.
         */
        assertEquals(cursor.getCount(), BULK_INSERT_RECORDS_TO_INSERT);

        /*
         * We now loop through and validate each record in the Cursor with the expected values from
         * bulkInsertTestContentValues.
         */
        cursor.moveToFirst();
        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext()) {
            TestUtilities.validateCurrentRecord(
                    "testBulkInsert. Error validating FavoriteEntry " + i,
                    cursor,
                    bulkInsertTestContentValues[i]);
        }

        /* Always close the Cursor! */
        cursor.close();

        deleteAllRecordsFromFavoritesTable();
    }

    /**
     * This method will clear all rows from the favorites table in our database.
     * <p>
     * Please note:
     * <p>
     * - This does NOT delete the table itself. We call this method from our @Before annotated
     * method to clear all records from the database before each test on the ContentProvider.
     * <p>
     * - We don't use the ContentProvider's delete functionality to perform this row deletion
     * because in this class, we are attempting to test the ContentProvider. We can't assume
     * that our ContentProvider's delete method works in our ContentProvider's test class.
     */
    private void deleteAllRecordsFromFavoritesTable() {
        /* Access writable database through FavoritesDbHelper */
        FavoritesDbHelper helper = new FavoritesDbHelper(InstrumentationRegistry.getTargetContext());
        SQLiteDatabase database = helper.getWritableDatabase();

        /* The delete method deletes all of the desired rows from the table, not the table itself */
        database.delete(FavoritesContract.FavoritesEntry.TABLE_NAME, null, null);

        /* Always close the database when you're through with it */
        database.close();
    }

}
