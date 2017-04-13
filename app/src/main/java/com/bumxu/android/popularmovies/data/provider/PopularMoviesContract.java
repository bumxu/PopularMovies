package com.bumxu.android.popularmovies.data.provider;

import android.net.Uri;
import android.provider.BaseColumns;


public class PopularMoviesContract {
    public static final String AUTHORITY = "com.bumxu.android.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_FAVORITE_MOVIES = "favorites";

    public static final class FavoriteMoviesEntry implements BaseColumns {
        /**
         * Content URI for movies stored in the favorite movies directory.
         */
        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(BASE_CONTENT_URI, PATH_FAVORITE_MOVIES);

        /**
         * The name of the SQL table of favorite movies.
         */
        public static final String TABLE_NAME = "favorite_movies";

        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_USERS_RATING = "users_rating";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_POSTER = "poster_path";

        public static final String COLUMN_UPDATED_TIME = "updated_time";
    }
}
