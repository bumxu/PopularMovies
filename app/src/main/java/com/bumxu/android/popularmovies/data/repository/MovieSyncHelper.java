package com.bumxu.android.popularmovies.data.repository;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.bumxu.android.popularmovies.data.Movie;
import com.bumxu.android.popularmovies.data.provider.PopularMoviesContract.FavoriteMoviesEntry;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;


@SuppressLint("SimpleDateFormat")
public class MovieSyncHelper {
    private static final String TAG = MovieSyncHelper.class.getSimpleName();

    /**
     * Take a list of movies and flag those that are in favorite movies local list.
     *
     * @param context An application context for using the Content Provider.
     * @param movies  A list of movies to be marked as favorite if applicable.
     * @return The same list of movies with favorite movies marked.
     */
    @SuppressWarnings("UnusedReturnValue")
    public static List<Movie> markFavoriteMovies(Context context, List<Movie> movies) {
        // Get favorite movies (id is enough)
        Cursor favsCursor = context.getContentResolver().query(
            FavoriteMoviesEntry.CONTENT_URI,
            new String[]{FavoriteMoviesEntry._ID},
            null, null, null);

        // Put the ids to a hashset
        HashSet<Long> favorites = new HashSet<>();
        if (favsCursor != null) {
            if (favsCursor.getCount() > 0) {
                while (favsCursor.moveToNext()) {
                    favorites.add(favsCursor.getLong(0));
                }
            }

            // Terminate cursor
            favsCursor.close();
        }

        // Mark favorite movies
        for (Movie movie : movies) {
            if (favorites.contains(movie.getId())) {
                movie.setFavorite(true);
            }
        }

        return movies;
    }

    /**
     * Takes a fresh list of movies, updating the local favorite movies that contains the first.
     * NOTE: This is finally not implemented, so the next code is not tested.
     *
     * @param context An application context for using the Content Provider.
     * @param movies  A list of fresh movies, where favorite movies have been already marked.
     */
    public static void updateFavoriteMovies(Context context, List<Movie> movies) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final long update = System.currentTimeMillis() / 1000L;

        ContentValues values;
        String plainReleaseDate;

        // For each movie marked as favorite...
        for (Movie movie : movies) {
            if (movie.isFavorite()) {
                // Build the URI for this movie
                Uri uri = ContentUris.withAppendedId(FavoriteMoviesEntry.CONTENT_URI, movie.getId());

                // Transform specific values (i.e. release date to plain text)
                plainReleaseDate = sdf.format(movie.getReleaseDate());

                // Init new values container
                values = new ContentValues();
                // Append the provider values
                values.put(FavoriteMoviesEntry.COLUMN_TITLE, movie.getTitle());
                values.put(FavoriteMoviesEntry.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
                values.put(FavoriteMoviesEntry.COLUMN_RELEASE_DATE, plainReleaseDate);
                values.put(FavoriteMoviesEntry.COLUMN_USERS_RATING, movie.getRating());
                values.put(FavoriteMoviesEntry.COLUMN_SYNOPSIS, movie.getOverview());
                values.put(FavoriteMoviesEntry.COLUMN_POSTER, movie.getPoster());
                // Assign current content time
                values.put(FavoriteMoviesEntry.COLUMN_UPDATED_TIME, update);

                // Update if exists
                context.getContentResolver().update(uri, values, null, null);
            }
        }
    }

    /**
     * Saves the provided movie instance into user's local favorite list.
     *
     * @param context A context to manage ContentProvider.
     * @param movie   The movie object snapshot to be saved into favorite list.
     */
    public static void saveFavoriteMovie(Context context, Movie movie) {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        final long update = System.currentTimeMillis() / 1000L;

        // Transform specific values (i.e. release date to plain text)
        String releaseDateStr = sdf.format(movie.getReleaseDate());

        ContentValues values = new ContentValues();
        values.put(FavoriteMoviesEntry._ID, movie.getId());
        values.put(FavoriteMoviesEntry.COLUMN_TITLE, movie.getTitle());
        values.put(FavoriteMoviesEntry.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
        values.put(FavoriteMoviesEntry.COLUMN_RELEASE_DATE, releaseDateStr);
        values.put(FavoriteMoviesEntry.COLUMN_USERS_RATING, movie.getRating());
        values.put(FavoriteMoviesEntry.COLUMN_SYNOPSIS, movie.getOverview());
        values.put(FavoriteMoviesEntry.COLUMN_POSTER, movie.getPoster());
        // Assign current content time
        values.put(FavoriteMoviesEntry.COLUMN_UPDATED_TIME, update);

        // Insert the movie to the favorite list
        try {
            context.getContentResolver().insert(FavoriteMoviesEntry.CONTENT_URI, values);
        } catch (Exception e) {
            Log.w(TAG, "Trying to save a movie that is already in favorites.");
        }
    }

    /**
     * Removes the provided movie (by id) from user's local favorite list.
     *
     * @param context A context to manage ContentProvider.
     * @param movie   The movie object snapshot to be removed from favorite list.
     */
    public static void removeFavoriteMovie(Context context, Movie movie) {
        Uri movieUri = ContentUris.withAppendedId(FavoriteMoviesEntry.CONTENT_URI, movie.getId());

        // Delete the movie from the favorite list
        try {
            if (context.getContentResolver().delete(movieUri, null, null) == 0) {
                Log.w(TAG, "Trying to remove a movie that is not in favorites.");
            }
        } catch (Exception e) {
            Log.e(TAG, "Remove movie from favorites has failed.", e);
        }
    }
}
