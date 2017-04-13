package com.bumxu.android.popularmovies.data.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static com.bumxu.android.popularmovies.data.provider.PopularMoviesContract.AUTHORITY;
import static com.bumxu.android.popularmovies.data.provider.PopularMoviesContract.FavoriteMoviesEntry;
import static com.bumxu.android.popularmovies.data.provider.PopularMoviesContract.PATH_FAVORITE_MOVIES;


public class PopularMoviesProvider extends ContentProvider {
    public static final int FAVORITE_MOVIES = 100;
    public static final int FAVORITE_MOVIE_WITH_ID = 101;

    private PopularMoviesDbHelper mDbHelper;

    // Uri matcher for our operations
    public static final UriMatcher sUriMatcher = buildUriMatcher();
    // · · ·
    private static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        uriMatcher.addURI(AUTHORITY, PATH_FAVORITE_MOVIES, FAVORITE_MOVIES);
        uriMatcher.addURI(AUTHORITY, PATH_FAVORITE_MOVIES + "/#", FAVORITE_MOVIE_WITH_ID);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new PopularMoviesDbHelper(getContext());

        return true;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        // Prepare
        Cursor retCursor;
        int match = sUriMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getReadableDatabase();

        // Query according operation
        switch (match) {
            case FAVORITE_MOVIES:
                retCursor = db.query(FavoriteMoviesEntry.TABLE_NAME,
                    projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case FAVORITE_MOVIE_WITH_ID:
                String id = uri.getPathSegments().get(1);

                selection = "_id=?";
                selectionArgs = new String[]{id};

                retCursor = mDbHelper.getReadableDatabase().query(FavoriteMoviesEntry.TABLE_NAME,
                    projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation QUERY with: " + uri + ".");
        }

        if (getContext() != null) {
            retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return retCursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues values) {
        // Prepare
        Uri retUri;
        int match = sUriMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Insert according operation
        switch (match) {
            case FAVORITE_MOVIES:
                long id = db.insert(FavoriteMoviesEntry.TABLE_NAME, null, values);
                if (id > 0) {
                    retUri = ContentUris.withAppendedId(FavoriteMoviesEntry.CONTENT_URI, id);
                } else {
                    throw new SQLException("Failed to insert movie " + values.getAsLong(FavoriteMoviesEntry._ID) + " in favorite movies directory.");
                }
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation INSERT with: " + uri + ".");
        }

        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return retUri;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        // Prepare
        int retCount;
        int match = sUriMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Update according operation
        switch (match) {
            case FAVORITE_MOVIE_WITH_ID:
                String id = uri.getPathSegments().get(1);

                selection = "_id=?";
                selectionArgs = new String[]{id};

                retCount = db.update(FavoriteMoviesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation UPDATE with: " + uri + ".");
        }

        if (getContext() != null && retCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return retCount;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        // Prepare
        int retCount;
        int match = sUriMatcher.match(uri);
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Delete according operation
        switch (match) {
            case FAVORITE_MOVIE_WITH_ID:
                String id = uri.getPathSegments().get(1);

                selection = "_id=?";
                selectionArgs = new String[]{id};

                retCount = db.delete(FavoriteMoviesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unsupported operation DELETE with: " + uri + ".");
        }

        if (getContext() != null && retCount > 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return retCount;
    }
}
