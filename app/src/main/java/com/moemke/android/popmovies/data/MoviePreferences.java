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
package com.moemke.android.popmovies.data;

public class MoviePreferences {

    /*
     * Poster resolution for reading movie data.
     */
    private static final String DEFAULT_POSTER_RESOLUTION = "w500";

    public static final int WIFI_REQUEST_CODE = 1;

    public static final int DETAIL_REQUEST_CODE = 2;

    /*
    * Default sort order when starting up Movie app
     */
    private static final String DEFAULT_SORT_ORDER = "popular";

    private static final String DEFAULT_DISPLAY_DATE_FORMAT ="MMM d, yyyy";

    public static String getDefaultMoviePosterResolution() {
        return DEFAULT_POSTER_RESOLUTION;
    }

    public static String getDefaultSortOrder() {
        return DEFAULT_SORT_ORDER;
    }

    public static String getDefaultDisplayDateFormat() {
        return DEFAULT_DISPLAY_DATE_FORMAT;
    }
}