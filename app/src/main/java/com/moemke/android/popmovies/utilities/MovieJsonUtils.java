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

import android.content.ContentValues;
import android.content.Context;

import com.moemke.android.popmovies.Movie;
import com.moemke.android.popmovies.MovieDetail;
import com.moemke.android.popmovies.MovieReview;
import com.moemke.android.popmovies.MovieVideo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.ArrayList;

/**
 * Utility functions to handle OpenWeatherMap JSON data.
 */
public final class MovieJsonUtils {

    private static final String TAG = MovieJsonUtils.class.getSimpleName();

    //Movie information. Each movie has the following data:
    private static final String TMDB_POSTER_PATH = "poster_path";      // "poster_path": "/WLQN5aiQG8wc9SeKwixW7pAR8K.jpg"
    //private static final String TMDB_ADULT = "adult";                //"adult": false
    private static final String TMDB_OVERVIEW = "overview";            //ex. "overview": "The quiet life of a terrier named Max ..."
    private static final String TMDB_RELEASE_DATE = "release_date";    // ex. "release_date": "2016-06-18",
    private static final String TMDB_GENRE_IDS = "genre_ids";          //"genre_ids": [12,16,35,10751]
    private static final String TMDB_ID = "id";                        // "id": 328111
    private static final String TMDB_ORIGINAL_TITLE = "original_title";//ex."original_title": "The Secret Life of Pets",
    private static final String TMDB_ORIGINAL_LANGUAGE = "original_language";//"original_language": "en"
    //private static final String TMDB_TITLE = "title";                //"title": "The Secret Life of Pets",
    private static final String TMDB_BACKDROP_PATH = "backdrop_path";  //"backdrop_path": "/lubzBMQLLmG88CLQ4F3TxZr2Q7N.jpg"
    //private static final String TMDB_POPULARITY="popularity";        //"popularity": 181.40313
    private static final String TMDB_VOTE_COUNT = "vote_count";        //"vote_count": 2332,
    //private static final String TMDB_VIDEO = "video";                //"video": false,
    private static final String TMDB_VOTE_AVERAGE = "vote_average";    //"vote_average": 5.8

    private static final String TMDB_MESSAGE_CODE = "cod";

    private static final String TMDB_GENRES_RESULT = "genres";
    private static final String TMDB_REVIEWS_RESULT = "reviews";
    private static final String TMDB_VIDEOS_RESULT = "videos";
    private static final String TMDB_RESULTS = "results";

    //Movie Video Information
    private static final String TMDB_TRAILER_ID = "id";         //"id": "571cdc239251414a8700191e",
    private static final String TMDB_ISO_639_1 = "iso_639_1";   //"iso_639_1": "en",
    private static final String TMDB_ISO_3166_1 = "iso_3166_1"; //"iso_3166_1": "US",
    private static final String TMDB_KEY = "key";               //"key": "eWI_Jsw9qUs",
    private static final String TMDB_NAME = "name";             //"name": "Official Trailer #3",
    private static final String TMDB_SITE = "site";             //"site": "YouTube",
    private static final String TMDB_SIZE = "size";             //"size": 1080,
    private static final String TMDB_TYPE = "type";             //"type": "Trailer"

    //Movie Reviews Information
    private static final String TMDB_REVIEW_ID = "id";      //"id": "579cfaac9251411b36008316",
    private static final String TMDB_AUTHOR = "author";     //"author": "Screen Zealots",
    private static final String TMDB_CONTENT = "content";   //"content": "A SCREEN ZEALOTS REVIEW www.screenzealots.com\r\n\r\nAnyone who is fortunate..."
    private static final String TMDB_REVIEW_URL = "url";    //"url": "https://www.themoviedb.org/review/579cfaac9251411b36008316"

    /**
     * This method parses JSON from a web response and returns an ArrayList of movies
     * describing movies (According to a specified sort order)
     * <p/>
     * Ex. https://api.themoviedb.org/3/movie/popular?api_key=YOUR_API_KEY
     * Example below:
     * {
     * "page": 1,
     * "results": [
     * {
     * "poster_path": "/WLQN5aiQG8wc9SeKwixW7pAR8K.jpg",
     * "adult": false,
     * "overview": "The quiet life of a terrier named Max is upended when his owner takes in Duke, a stray whom Max instantly dislikes.",
     * "release_date": "2016-06-18",
     * "genre_ids": [12,16,35,10751],
     * "id": 328111,
     * "original_title": "The Secret Life of Pets",
     * "original_language": "en",
     * "title": "The Secret Life of Pets",
     * "backdrop_path": "/lubzBMQLLmG88CLQ4F3TxZr2Q7N.jpg",
     * "popularity": 181.40313,
     * "vote_count": 1960,
     * "video": false,
     * "vote_average": 5.8
     * },
     * ...
     * ],
     * "total_results": 19629,
     * "total_pages": 982
     * }
     *
     * @param movieJsonStr JSON response from server
     * @return Array of Strings describing movie data
     * @throws JSONException If JSON data cannot be properly parsed
     */
    public static ArrayList<Movie> getMovieDataFromJson(String movieJsonStr)
            throws JSONException {

        ArrayList<Movie> movies = new ArrayList<Movie>();

        JSONObject movieJson = new JSONObject(movieJsonStr);

        /* Is there an error? */
        if (movieJson.has(TMDB_MESSAGE_CODE)) {
            int errorCode = movieJson.getInt(TMDB_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        JSONArray movieArray = movieJson.getJSONArray(TMDB_RESULTS);

        Movie movie;
        // handle case when empty JSON array returned
        if (movieArray != null && movieArray.length() > 0) {

            for (int i = 0; i < movieArray.length(); i++) {

                /* Get the JSON object representing each movie */
                JSONObject jsonMovie = movieArray.getJSONObject(i);

                /* Get required data for each movie */
                String pp = jsonMovie.getString(TMDB_POSTER_PATH);
                String ov = jsonMovie.getString(TMDB_OVERVIEW);
                String rd = jsonMovie.getString(TMDB_RELEASE_DATE);
                String gs = "";
                String id = jsonMovie.getString(TMDB_ID);
                String ot = jsonMovie.getString(TMDB_ORIGINAL_TITLE);
                String ol = jsonMovie.getString(TMDB_ORIGINAL_LANGUAGE);
                String bp = jsonMovie.getString(TMDB_BACKDROP_PATH);
                String vc = jsonMovie.getString(TMDB_VOTE_COUNT);
                String va = jsonMovie.getString(TMDB_VOTE_AVERAGE);

                movie = new Movie(pp, ov, rd, Integer.parseInt(id), ot, ol, bp, Integer.parseInt(vc),
                        Float.parseFloat(va), 0, gs);  // last 0 is a marker for is_favorite, initially set to 0

                movies.add(i, movie);
            }
        }

        return movies;
    }

    /**
     * Parse the JSON and convert it into ContentValues that can be inserted into our database.
     *
     * @param context      An application context, such as a service or activity context.
     * @param movieJsonStr The JSON to parse into ContentValues.
     * @return An array of ContentValues parsed from the JSON.
     */
    public static ContentValues[] getFullMovieDataFromJson(Context context, String movieJsonStr) {
        /** This will be implemented in a future lesson **/
        return null;
    }

    //https://api.themoviedb.org/3/movie/328111/videos?api_key=YOUR_KEY_HERE
    public static ArrayList<MovieReview> getMovieReviewDataFromJson(String movieReviewsJsonStr)
            throws JSONException {

        ArrayList<MovieReview> movieReviews = new ArrayList<MovieReview>();

        JSONObject movieJson = new JSONObject(movieReviewsJsonStr);

        /* Is there an error? */
        if (movieJson.has(TMDB_MESSAGE_CODE)) {
            int errorCode = movieJson.getInt(TMDB_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        JSONArray movieReviewArray = movieJson.getJSONArray(TMDB_RESULTS);

        MovieReview movieReview;
        // handle case when empty JSON array returned
        if (movieReviewArray != null && movieReviewArray.length() > 0) {

            for (int i = 0; i < movieReviewArray.length(); i++) {

            /* Get the JSON object representing each movie */
                JSONObject jsonMovieReview = movieReviewArray.getJSONObject(i);

            /* Get required data for each movieReview */
                String ri = jsonMovieReview.getString(TMDB_REVIEW_ID);
                String au = jsonMovieReview.getString(TMDB_AUTHOR);
                String co = jsonMovieReview.getString(TMDB_CONTENT);
                String ru = jsonMovieReview.getString(TMDB_REVIEW_URL);

                movieReview = new MovieReview(ri, au, co, ru);

                movieReviews.add(i, movieReview);
            }
        }
        return movieReviews;
    }

    //https://api.themoviedb.org/3/movie/328111/videos?api_key=YOUR_KEY_HERE
    public static ArrayList<MovieVideo> getMovieVideoDataFromJson(String movieVideoJsonStr)
            throws JSONException {

        ArrayList<MovieVideo> movieVideos = new ArrayList<MovieVideo>();

        JSONObject movieVideoJson = new JSONObject(movieVideoJsonStr);

        /* Is there an error? */
        if (movieVideoJson.has(TMDB_MESSAGE_CODE)) {
            int errorCode = movieVideoJson.getInt(TMDB_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        JSONArray movieVideoArray = movieVideoJson.getJSONArray(TMDB_RESULTS);

        MovieVideo movieVideo;

        // handle case when empty JSON array returned
        if (movieVideoArray != null && movieVideoArray.length() > 0) {

            for (int i = 0; i < movieVideoArray.length(); i++) {

            /* Get the JSON object representing each video */
                JSONObject jsonMovieVideo = movieVideoArray.getJSONObject(i);

            /* Get required data for each movie video */

                String ti = jsonMovieVideo.getString(TMDB_TRAILER_ID);
                String i6 = jsonMovieVideo.getString(TMDB_ISO_639_1);
                String i3 = jsonMovieVideo.getString(TMDB_ISO_3166_1);
                String ke = jsonMovieVideo.getString(TMDB_KEY);
                String na = jsonMovieVideo.getString(TMDB_NAME);
                String st = jsonMovieVideo.getString(TMDB_SITE);
                String sz = jsonMovieVideo.getString(TMDB_SIZE);
                String ty = jsonMovieVideo.getString(TMDB_TYPE);

                movieVideo = new MovieVideo(ti, ke, na, st, Integer.parseInt(sz), ty);
//                movieVideo = new MovieVideo(ti, i6, i3, ke, na, st, Integer.parseInt(sz), ty);

                movieVideos.add(i, movieVideo);
            }
        }
        return movieVideos;
    }

    //https://api.themoviedb.org/3/movie/328111?api_key=YOUR_API_KEY&append_to_response=videos,reviews
    public static MovieDetail getMovieDetailDataFromJson(String movieDetailJsonStr)
            throws JSONException {

        MovieDetail movieDetail;
        int movieId;
        ArrayList<String> movieGenresArray = new ArrayList<String>();
        ArrayList<MovieVideo> movieVideosArray = new ArrayList<MovieVideo>();
        ArrayList<MovieReview> movieReviewsArray = new ArrayList<MovieReview>();

        JSONObject movieDetailJson = new JSONObject(movieDetailJsonStr);
        /* Is there an error? */
        if (movieDetailJson.has(TMDB_MESSAGE_CODE)) {
            int errorCode = movieDetailJson.getInt(TMDB_MESSAGE_CODE);

            switch (errorCode) {
                case HttpURLConnection.HTTP_OK:
                    break;
                case HttpURLConnection.HTTP_NOT_FOUND:
                    /* Location invalid */
                    return null;
                default:
                    /* Server probably down */
                    return null;
            }
        }

        movieId = movieDetailJson.getInt(TMDB_ID);

        JSONArray movieGenreJsonArray = movieDetailJson.getJSONArray(TMDB_GENRES_RESULT);
        String genre;
        if (movieGenreJsonArray != null && movieGenreJsonArray.length() > 0) {
            for (int i = 0; i < movieGenreJsonArray.length(); i++) {

            /* Get the JSON object representing each movie */
                JSONObject jsonMovieGenre = movieGenreJsonArray.getJSONObject(i);

            /* Get required data for each movieReview */
                String gnm = jsonMovieGenre.getString(TMDB_NAME);

                genre = new String(gnm);

                movieGenresArray.add(i, genre);
            }
        }

        JSONObject movieReviews = movieDetailJson.getJSONObject(TMDB_REVIEWS_RESULT);
        JSONArray movieReviewJsonArray = movieReviews.getJSONArray(TMDB_RESULTS);

        MovieReview movieReview;
        // handle case when empty JSON array returned
        if (movieReviewJsonArray != null && movieReviewJsonArray.length() > 0) {

            for (int i = 0; i < movieReviewJsonArray.length(); i++) {

            /* Get the JSON object representing each movie */
                JSONObject jsonMovieReview = movieReviewJsonArray.getJSONObject(i);

            /* Get required data for each movieReview */
                String ri = jsonMovieReview.getString(TMDB_REVIEW_ID);
                String au = jsonMovieReview.getString(TMDB_AUTHOR);
                String co = jsonMovieReview.getString(TMDB_CONTENT);
                String ru = jsonMovieReview.getString(TMDB_REVIEW_URL);

                movieReview = new MovieReview(ri, au, co, ru);

                movieReviewsArray.add(i, movieReview);
            }
        }

        JSONObject movieVideos = movieDetailJson.getJSONObject(TMDB_VIDEOS_RESULT);
        JSONArray movieVideoJsonArray = movieVideos.getJSONArray(TMDB_RESULTS);

        MovieVideo movieVideo;

        // handle case when empty JSON array returned
        if (movieVideoJsonArray != null && movieVideoJsonArray.length() > 0) {

            for (int i = 0; i < movieVideoJsonArray.length(); i++) {

            /* Get the JSON object representing each video */
                JSONObject jsonMovieVideo = movieVideoJsonArray.getJSONObject(i);

            /* Get required data for each movie video */

                String ti = jsonMovieVideo.getString(TMDB_TRAILER_ID);
                String i6 = jsonMovieVideo.getString(TMDB_ISO_639_1);
                String i3 = jsonMovieVideo.getString(TMDB_ISO_3166_1);
                String ke = jsonMovieVideo.getString(TMDB_KEY);
                String na = jsonMovieVideo.getString(TMDB_NAME);
                String st = jsonMovieVideo.getString(TMDB_SITE);
                String sz = jsonMovieVideo.getString(TMDB_SIZE);
                String ty = jsonMovieVideo.getString(TMDB_TYPE);

                movieVideo = new MovieVideo(ti, ke, na, st, Integer.parseInt(sz), ty);
                movieVideosArray.add(i, movieVideo);
            }
        }
        movieDetail = new MovieDetail(movieId, movieGenresArray, movieVideosArray, movieReviewsArray);
        return movieDetail;
    }

}