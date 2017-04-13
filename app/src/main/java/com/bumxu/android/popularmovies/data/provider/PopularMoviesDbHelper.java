package com.bumxu.android.popularmovies.data.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.bumxu.android.popularmovies.data.provider.PopularMoviesContract.FavoriteMoviesEntry;


public class PopularMoviesDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "movies";
    private static final int DATABASE_VERSION = 6;

    public PopularMoviesDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createFavoritesTableQuery = "CREATE TABLE " + FavoriteMoviesEntry.TABLE_NAME +
            "(" +
            FavoriteMoviesEntry._ID + " INTEGER PRIMARY KEY, " +
            FavoriteMoviesEntry.COLUMN_TITLE + " TEXT, " +
            FavoriteMoviesEntry.COLUMN_ORIGINAL_TITLE + " TEXT, " +
            FavoriteMoviesEntry.COLUMN_RELEASE_DATE + " TEXT, " +
            FavoriteMoviesEntry.COLUMN_USERS_RATING + " REAL, " +
            FavoriteMoviesEntry.COLUMN_SYNOPSIS + " TEXT, " +
            FavoriteMoviesEntry.COLUMN_POSTER + " TEXT, " +
            FavoriteMoviesEntry.COLUMN_UPDATED_TIME + " INTEGER" +
            ")";

        db.execSQL(createFavoritesTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //    if (oldVersion != newVersion) {
        //        // Behaviour to be determined by future app updates
        //    }
    }
}
