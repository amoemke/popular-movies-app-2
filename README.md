# Popular Movies, Stage 1

[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

**Aurea Moemke, February 5, 2017**
**(Project  1, Associate Android Developer Nanodegree, Udacity)**


To be able to access the tmdb server, please fill in your API_KEY in the following file:

/app/java/com.moemke.android.popmovies1/utilities/NetworkUtils.java

There is a TODO specified where the key has to be entered.

    *// TODO:Please enter your API Key here*
    *private static final String API_KEY = " ";*


This app does the following:
* on launch, a grid of movie posters sorted according to popularity is presented. Data comes from the tmdb server (themoviedb.org) 
* an option to sort according to "Most Popular" and "Highest Rated" are provided in the Settings menu 
* on tap of a movie poster on the main screen, the app transitions to a details screen with additional information, that is,
     - original title
     - movie poster image thumbnail
     - plot synopsis
     - user rating (scaled to a 5 point scale from the original 10, for presentation purposes)
     - release date
* provides a dialog box to turn on wi-fi when a "no internet connection" state is detected.
