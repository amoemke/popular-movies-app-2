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
package com.moemke.android.popmovies.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

/**
 * These utilities will be used to communicate with the movie server.
 */
public final class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    //For saving local posters
    private static final String LOCAL_IMG_BASE_URL = "file://";

    // TODO: Please enter your TMDB API Key here
    private static final String TMDB_API_KEY = "enter-api-key-here";

    private static final String TMDB_APIKEY_PARAM = "api_key";

    //http://api.themoviedb.org/3/movie/popular?api_key=your-api-key-here
    private static final String TMDB_BASE_URL = "http://api.themoviedb.org/3/movie";

    //Access movie detail using append_to_response (rather than 2 separate calls to videos and reviews)
    //Refer: https://developers.themoviedb.org/3/getting-started/append-to-response
    //https://api.themoviedb.org/3/movie/328111?api_key=YOUR_API_KEY&append_to_response=videos,reviews
    private static final String TMDB_APPEND_TO_RESPONSE = "append_to_response";

    //access videos
    //To fetch trailers you will want to make a request to the /movie/{id}/videos endpoint.
    //You should use an Intent to open a youtube link in either the native app or a web browser of choice.
    //https://api.themoviedb.org/3/movie/328111/videos?api_key=YOUR_KEY_HERE
    private static final String TMDB_VIDEOS = "videos";

    //access reviews
    //To fetch reviews you will want to make a request to the /movie/{id}/reviews endpoint
    //https://api.themoviedb.org/3/movie/328111/videos?api_key=YOUR_KEY_HERE
    private static final String TMDB_REVIEWS = "reviews";

    // In order to request popular movies you will want to request data from the
    // /movie/popular and /movie/top_rated endpoints.
    private static final String TMDB_IMG_BASE_URL = "http://image.tmdb.org/t/p/";

    private static final String TMDB_DATE_FORMAT = "yyyy-MM-dd";

    //https://www.youtube.com/watch?v=bvu-zlR5A8Q
    private static final String YOUTUBE_BASE_URL = "https://www.youtube.com/watch";
    private static final String YOUTUBE_VIDEO_PARAM = "v";

    //REFER: http://stackoverflow.com/questions/8841159/how-to-make-youtube-video-thumbnails-in-android
    // ex. http://img.youtube.com/vi/bvu-zlR5A8Q/0.jpg
    // http://img.youtube.com/vi/<VideoId>/0.jpg
    private static final String YOUTUBE_IMG_BASE_URL = "http://img.youtube.com/vi";
    private static final String YOUTUBE_IMG_SUFFIX = "0.jpg";

    public static String getTMDBDateFormat() {
        return TMDB_DATE_FORMAT;
    }

    public static boolean APIKEYexists() {
        if (TMDB_API_KEY.isEmpty() || TMDB_API_KEY == "") {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Builds the URL used to talk to the tmdb server using a sort order specified.
     *
     * @param sortOrder The sort order that will be queried for,
     *                  possible values: "popular", "top_rated", "favorites"
     * @return The URL to use to query the tmdb server.
     */
    public static URL buildUrl(String sortOrder) {
        Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                .appendPath(sortOrder)
                .appendQueryParameter(TMDB_APIKEY_PARAM, TMDB_API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // Log.v(TAG, "Built URL " + url);
        return url;
    }

    /* Builds the URL to access movie details from the tmdb server including
    * reviews and details (using append_to_response)

    * @param videoId
    * @return The URL to use to query the tmdb server.
    * //https://api.themoviedb.org/3/movie/328111?api_key=YOUR_API_KEY_HERE
    *                      &append_to_response=videos,reviews
    */
    public static URL buildMovieDetailsUrl(int videoId) {
        Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                .appendPath(Integer.toString(videoId))
                .appendQueryParameter(TMDB_APIKEY_PARAM, TMDB_API_KEY)
                .appendQueryParameter(TMDB_APPEND_TO_RESPONSE, TMDB_VIDEOS + "," + TMDB_REVIEWS)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // Log.v(TAG, "Built Details URL " + url);
        return url;
    }

    /**
     * Builds the image URL used to talk to the tmdb server using a sort order specified.
     *
     * @param size The ‘size’ required with possible values: "w92", "w154", "w185", "w342", "w500",
     *             "w780", or "original". For most phones, recommended size is “w185”.
     * @return The URL to use to query the tmdb server.
     */
    public static String buildImageUrlString(String size, String poster_path) {

        if (poster_path == null || poster_path.isEmpty())
            return "";
        Uri builtUri = Uri.parse(TMDB_IMG_BASE_URL).buildUpon()
                .appendPath(size)
                .appendPath(poster_path.substring(1))
                .appendQueryParameter(TMDB_APIKEY_PARAM, TMDB_API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // Log.v(TAG, "Built Image URI " + url.toString());

        return url.toString();
    }

    public static String buildLocalImageUrlString(String poster_path) {

        if (poster_path == null || poster_path.isEmpty())
            return "";
        Uri builtUri = Uri.parse(LOCAL_IMG_BASE_URL).buildUpon()
                .appendPath(poster_path.substring(1))
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // Log.v(TAG, "Built Image URI " + url.toString());

        return url.toString();
    }

    //// ex. http://img.youtube.com/vi/bvu-zlR5A8Q/0.jpg
    // http://img.youtube.com/vi/<VideoId>/0.jpg
    public static String buildYoutubeImageUrlString(String videoId) {

        Uri builtUri = Uri.parse(YOUTUBE_IMG_BASE_URL).buildUpon()
                .appendPath(videoId)
                .appendPath(YOUTUBE_IMG_SUFFIX)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // Log.v(TAG, "Built Youtube Image URI " + url.toString());

        return url.toString();
    }

    /**
     * Builds the URL used to talk to the tmdb server.
     * https://api.themoviedb.org/3/movie/328111/videos?api_key=YOUR_API_KEY_HERE
     *
     * @param movieId The id of the movie, i.e. 328111
     * @return The URL to use to query the tmdb server.
     */
    public static URL buildVideoUrl(int movieId) {
        Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                .appendPath(Integer.toString(movieId))
                .appendPath(TMDB_VIDEOS)
                .appendQueryParameter(TMDB_APIKEY_PARAM, TMDB_API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // Log.v(TAG, "Built Video URL " + url);
        return url;
    }

    /**
     * Builds the URL used to talk to the tmdb server.
     * https://api.themoviedb.org/3/movie/328111/reviews?api_key=YOUR_API_KEY_HERE
     *
     * @param movieId The id of the movie, i.e. 328111
     * @return The URL to use to query the tmdb server.
     */
    public static URL buildReviewUrl(int movieId) {
        Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                .appendPath(Integer.toString(movieId))
                .appendPath(TMDB_REVIEWS)
                .appendQueryParameter(TMDB_APIKEY_PARAM, TMDB_API_KEY)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        //Log.v(TAG, "Built Reviews URL " + url);
        return url;
    }

    /**
     * Builds the youtube URL to play the youtube video.
     *
     * @param key the key identifier for the video
     * @return The URL to use to query the tmdb server.
     */
    public static Uri buildYoutubeUrl(String key) {

        Uri builtUri = Uri.parse(YOUTUBE_BASE_URL).buildUpon()
                .appendQueryParameter(YOUTUBE_VIDEO_PARAM, key)
                .build();

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        // Log.v(TAG, "Built Youtube uri " + url.toString());
        return builtUri;
    }

    /**
     * This method returns the entire result from the HTTP response.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The contents of the HTTP response.
     * @throws IOException Related to network and stream reading
     */
    public static String getResponseFromHttpUrl(URL url) throws IOException {
        //Log.v(TAG, "Built URI passed to getResponseFromHttpUrl" + url);
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    //Refer: http://stackoverflow.com/questions/1560788/
    // how-to-check-internet-access-on-android-inetaddress-never-times-out
    public static boolean isOnline(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}