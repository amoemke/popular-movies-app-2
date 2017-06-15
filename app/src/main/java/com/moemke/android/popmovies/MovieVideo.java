package com.moemke.android.popmovies;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by aureamoemke on 23/02/2017.
 */
/*
MovieVideo (part of MovieDetail json)
https://api.themoviedb.org/3/movie/328111?api_key=YOUR_API_KEY&append_to_response=videos,reviews
    ...
   "videos": {
        "results": [
            {"id": "571cdc48c3a3684e620018b8",
            "iso_639_1": "en",
            "iso_3166_1": "US",
            "key": "i-80SGWfEjM",
            "name": "Official Teaser Trailer",
            "site": "YouTube",
            "size": 1080,
            "type": "Trailer"
            },
            ...]
        },
 */
public class MovieVideo implements Parcelable {
    String trailerId;   //"id": "571cdc239251414a8700191e",
//    String iso_639_1;    //"iso_639_1": "en",
//    String iso_3166_1;  //"iso_3166_1": "US",
    String key;         //"key": "eWI_Jsw9qUs",
    String name;        //"name": "Official Trailer #3",
    String site;        //"site": "YouTube",
    int size;        //"size": 1080,
    String type;        //"type": "Trailer"

    public MovieVideo(String ti, String ke, String na, String st, int sz,
                      String ty) {
        this.trailerId = ti;
//        this.iso_639_1 = i6;
//        this.iso_3166_1 = i3;
        this.key = ke;
        this.name = na;
        this.site = st;
        this.size = sz;
        this.type = ty;
    }

    private MovieVideo(Parcel in) {
        trailerId = in.readString();
//        iso_639_1 = in.readString();
//        iso_3166_1 = in.readString();
        key = in.readString();
        name = in.readString();
        site = in.readString();
        size = in.readInt();
        type = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String toString() {
        return trailerId + "--" + trailerId;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(trailerId);
//        parcel.writeString(iso_639_1);
//        parcel.writeString(iso_3166_1);
        parcel.writeString(key);
        parcel.writeString(name);
        parcel.writeString(site);
        parcel.writeInt(size);
        parcel.writeString(type);
    }

    public static final Parcelable.Creator<MovieVideo> CREATOR = new Parcelable.Creator<MovieVideo>() {
        @Override
        public MovieVideo createFromParcel(Parcel parcel) {
            return new MovieVideo(parcel);
        }

        @Override
        public MovieVideo[] newArray(int i) {
            return new MovieVideo[i];
        }
    };

    public String getTrailerId() {
        return trailerId;
    }

    public void setTrailerId(String trailerId) {
        this.trailerId = trailerId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
