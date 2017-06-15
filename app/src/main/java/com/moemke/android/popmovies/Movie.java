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

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by aureamoemke on 02/02/2017.
 */

public class Movie implements Parcelable {

    private static final String TAG = Movie.class.getSimpleName();

    private String poster_path;         // "poster_path": "/WLQN5aiQG8wc9SeKwixW7pAR8K.jpg",
    private String overview;            // "overview": "The quiet life of a terrier named Max
    private String release_date;        // "release_date": "2016-06-18",
    //private ArrayList<Integer> genre_ids;           //"genre_ids": [12,16,35,10751]
    private int id;                     // "id": 328111
    private String original_title;      // "original_title": "The Secret Life of Pets",
    private String original_language;   //"original_language": "en"
    private String backdrop_path;       //"backdrop_path": "/lubzBMQLLmG88CLQ4F3TxZr2Q7N.jpg"
    int vote_count;             //"vote_count": 2332,
    float vote_average;         // "vote_average": 5.8
    int is_favorite;        // Saved as favorite: 0-off 1-on
    private String genre_str;

    public Movie(String pp, String ov, String rd, int id, String ot, String ol,
                 String bp, int vc, float va, int is_favorite, String gs) {
        this.poster_path = pp;
        this.overview = ov;
        this.release_date = rd;
        //this.genre_ids = gis;
        this.id = id;
        this.original_title = ot;
        this.original_language = ol;
        this.backdrop_path = bp;
        this.vote_count = vc;
        this.vote_average = va;
        this.is_favorite = is_favorite;
        this.genre_str = gs;
    }

    private Movie(Parcel in) {
        poster_path = in.readString();
        overview = in.readString();
        release_date = in.readString();
        //genre_ids = in.readArrayList(null);
        id = in.readInt();
        original_title = in.readString();
        original_language = in.readString();
        backdrop_path = in.readString();
        vote_count = in.readInt();
        vote_average = in.readFloat();
        is_favorite = in.readInt();
        genre_str = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String toString() {
        return original_title;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(poster_path);
        parcel.writeString(overview);
        parcel.writeString(release_date);
//        parcel.writeList(genre_ids);
        parcel.writeInt(id);
        parcel.writeString(original_title);
        parcel.writeString(original_language);
        parcel.writeString(backdrop_path);
        parcel.writeInt(vote_count);
        parcel.writeFloat(vote_average);
        parcel.writeInt(is_favorite);
        parcel.writeString(genre_str);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }
    };

    public String getPoster_path() {
        return poster_path;
    }

    public void setPoster_path(String poster_path) {
        this.poster_path = poster_path;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getRelease_date() {
        return release_date;
    }

    public void setRelease_date(String release_date) {
        this.release_date = release_date;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOriginal_title() {
        return original_title;
    }

    public void setOriginal_title(String original_title) {
        this.original_title = original_title;
    }

    public String getOriginal_language() {
        return original_language;
    }

    public void setOriginal_language(String original_language) {
        this.original_language = original_language;
    }

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public void setBackdrop_path(String backdrop_path) {
        this.backdrop_path = backdrop_path;
    }

    public int getVote_count() {
        return vote_count;
    }

    public void setVote_count(int vote_count) {
        this.vote_count = vote_count;
    }

    public float getVote_average() {
        return vote_average;
    }

    public void setVote_average(float vote_average) {
        this.vote_average = vote_average;
    }

    public int getIs_favorite() {
        return is_favorite;
    }

    public void setIs_favorite(int is_favorite) {
        this.is_favorite = is_favorite;
    }

    public String getGenre_str() {
        return genre_str;
    }

    public void setGenre_str(String genre_str) {
        this.genre_str = genre_str;
    }
}
