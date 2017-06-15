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
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v7.app.AlertDialog;

import com.moemke.android.popmovies.R;
import com.moemke.android.popmovies.data.MoviePreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class for handling date conversions for Popular Movies.
 */
public final class MovieUtils {

    private static final String TAG = MovieUtils.class.getSimpleName();

    public static String formatDate(String dateToFormat, String oldformat, String newformat) {
        //Ex. from "yyyy-MM-dd" to "dd-MM-yyyy"
        String dateString = null;
        Date date;
        if (dateToFormat == null || dateToFormat.isEmpty()) {
            return "Empty date value";
        } else {
            SimpleDateFormat fmtIn = new SimpleDateFormat(oldformat);
            SimpleDateFormat fmtOut;
            try {
                date = fmtIn.parse(dateToFormat);
                fmtOut = new SimpleDateFormat(newformat);
                return fmtOut.format(date);
            } catch (ParseException pe) {
                return "Bad date format";
            }
        }
    }

}