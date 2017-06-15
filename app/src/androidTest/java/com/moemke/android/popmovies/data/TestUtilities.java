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

import android.content.ContentValues;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;

import com.moemke.android.popmovies.utils.PollingCheck;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;


class TestUtilities {

    static final int BULK_INSERT_RECORDS_TO_INSERT = 10;

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }

    /**
     * Students: The test functions for insert and delete use TestContentObserver to test
     * the ContentObserver callbacks using the PollingCheck class from the Android Compatibility
     * Test Suite tests.
     * NOTE: This only tests that the onChange function is called; it DOES NOT test that the
     * correct Uri is returned.
     */
    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHT;
        boolean mContentChanged;

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHT = ht;
        }

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        /**
         * Called when a content change occurs.
         * <p>
         * To ensure correct operation on older versions of the framework that did not provide a
         * Uri argument, applications should also implement this method whenever they implement
         * the { #onChange(boolean, Uri)} overload.
         *
         * @param selfChange True if this is a self-change notification.
         */
        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        /**
         * Called when a content change occurs. Includes the changed content Uri when available.
         *
         * @param selfChange True if this is a self-change notification.
         * @param uri        The Uri of the changed content, or null if unknown.
         */
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        /**
         * Note: The PollingCheck class is taken from the Android CTS (Compatibility Test Suite).
         * It's useful to look at the Android CTS source for ideas on how to test your Android
         * applications. The reason that PollingCheck works is that, by default, the JUnit testing
         * framework is not running on the main Android application thread.
         */
        void waitForNotificationOrFail() {

            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
            mHT.quit();
        }
    }

    /**
     * Ensures there is a non empty cursor and validates the cursor's data by checking it against
     * a set of expected values. This method will then close the cursor.
     *
     * @param error          Message when an error occurs
     * @param valueCursor    The Cursor containing the actual values received from an arbitrary query
     * @param expectedValues The values we expect to receive in valueCursor
     */
    static void validateThenCloseCursor(String error, Cursor valueCursor, ContentValues expectedValues) {
        assertNotNull(
                "This cursor is null. Did you make sure to register your ContentProvider in the manifest?",
                valueCursor);

        assertTrue("Empty cursor returned. " + error, valueCursor.moveToFirst());
        validateCurrentRecord(error, valueCursor, expectedValues);
        valueCursor.close();
    }

    /**
     * This method iterates through a set of expected values and makes various assertions that
     * will pass if our app is functioning properly.
     *
     * @param error          Message when an error occurs
     * @param valueCursor    The Cursor containing the actual values received from an arbitrary query
     * @param expectedValues The values we expect to receive in valueCursor
     */
    static void validateCurrentRecord(String error, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();

        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int index = valueCursor.getColumnIndex(columnName);

            /* Test to see if the column is contained within the cursor */
            String columnNotFoundError = "Column '" + columnName + "' not found. " + error;
            assertFalse(columnNotFoundError, index == -1);

            /* Test to see if the expected value equals the actual value (from the Cursor) */
            String expectedValue = entry.getValue().toString();
            String actualValue = valueCursor.getString(index);

            String valuesDontMatchError = "Actual value '" + actualValue
                    + "' did not match the expected value '" + expectedValue + "'. "
                    + error;

            assertEquals(valuesDontMatchError,
                    expectedValue,
                    actualValue);
        }
    }

    /**
     * Used as a convenience method to return a singleton instance of ContentValues to populate
     * our database or insert using our ContentProvider.
     *
     * @return ContentValues that can be inserted into our ContentProvider or weather.db
     */
    static ContentValues createTestFavoritesContentValues() {

        ContentValues testFavoriteValues = new ContentValues();
        testFavoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_POSTER_PATH, "test poster path");
        testFavoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_OVERVIEW, "test overview");
        testFavoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_RELEASE_DATE, "test release date");
        testFavoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_MOVIE_ID, 12345);
        testFavoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_ORIGINAL_TITLE, "test original_title");
        testFavoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_ORIGINAL_LANGUAGE, "test original language");
        testFavoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_BACKDROP_PATH, "test backdrop path");
        testFavoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_VOTE_COUNT, 100);
        testFavoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_VOTE_AVERAGE, 9.5);

        return testFavoriteValues;
    }

    /**
     * Used as a convenience method to return a singleton instance of an array of ContentValues to
     * populate our database or insert using our ContentProvider's bulk insert method.
     * <p>
     * It is handy to have utility methods that produce test values because it makes it easy to
     * compare results from ContentProviders and databases to the values you expect to receive.
     * See {@link #validateCurrentRecord(String, Cursor, ContentValues)} and
     * {@link #validateThenCloseCursor(String, Cursor, ContentValues)} for more information on how
     * this verification is performed.
     *
     * @return Array of ContentValues that can be inserted into our ContentProvider or weather.db
     */
    static ContentValues[] createBulkInsertTestFavoriteValues() {

        ContentValues[] bulkTestFavoriteValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        int movieIdVal = 12345;
        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {

            ContentValues testFavoriteValues = new ContentValues();
            testFavoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_POSTER_PATH, "test poster path");
            testFavoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_OVERVIEW, "test overview");
            testFavoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_RELEASE_DATE, "test release date");
            testFavoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_MOVIE_ID, movieIdVal);
            testFavoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_ORIGINAL_TITLE, "test original_title");
            testFavoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_ORIGINAL_LANGUAGE, "test original language");
            testFavoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_BACKDROP_PATH, "test backdrop path");
            testFavoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_VOTE_COUNT, 100);
            testFavoriteValues.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_VOTE_AVERAGE, 9.5);

            movieIdVal++;

            bulkTestFavoriteValues[i] = testFavoriteValues;
        }

        return bulkTestFavoriteValues;
    }

}
