package com.moemke.android.popmovies.utilities;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import com.moemke.android.popmovies.data.FavoritesContract;

import java.util.ArrayList;
import java.util.List;

public class TestUtil {

    public static void insertFakeData(SQLiteDatabase db){
        if(db == null){
            return;
        }
        //create a list of fake guests
        List<ContentValues> list = new ArrayList<ContentValues>();

        ContentValues cv = new ContentValues();
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_MOVIE_ID, 12345);
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_ORIGINAL_TITLE,"The Secret Life of Pets");
        list.add(cv);

        cv = new ContentValues();
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_MOVIE_ID, 23456);
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_ORIGINAL_TITLE,"La la Land");
        list.add(cv);

        cv = new ContentValues();
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_MOVIE_ID, 45678);
        cv.put(FavoritesContract.FavoritesEntry.COLUMN_NAME_ORIGINAL_TITLE,"Jurassic World");
        list.add(cv);

        //insert all guests in one transaction
        try
        {
            db.beginTransaction();
            //clear the table first
            db.delete (FavoritesContract.FavoritesEntry.TABLE_NAME,null,null);
            //go through the list and add one by one
            for(ContentValues c:list){
                db.insert(FavoritesContract.FavoritesEntry.TABLE_NAME, null, c);
            }
            db.setTransactionSuccessful();
        }
        catch (SQLException e) {
            //too bad :(
        }
        finally
        {
            db.endTransaction();
        }

    }
}