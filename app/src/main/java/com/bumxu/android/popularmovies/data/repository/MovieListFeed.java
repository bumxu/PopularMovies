package com.bumxu.android.popularmovies.data.repository;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.bumxu.android.popularmovies.data.Movie;
import com.bumxu.android.popularmovies.data.provider.PopularMoviesContract;

import org.json.JSONException;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;


public class MovieListFeed {
    private static final String TAG = MovieListFeed.class.getSimpleName();

    // · // · // · From TMDB · // · // · //

    /**
     * Retrieves a page from the list of popular movies in TMDB.
     *
     * @param page The required page.
     * @return The list of movies on that page.
     * @throws JSONException If the format of the returned response is not valid.
     * @throws IOException   In case of network error.
     */
    public static List<Movie> fetchPopular(int page) throws JSONException, IOException {
        Uri url = TMDBHelper.BASE_URL
            .buildUpon()
            .appendPath(TMDBHelper.PATH_POPULAR)
            .appendQueryParameter("page", String.valueOf(page))
            .build();

        return TMDBHelper.fetchMovieList(url);
    }

    /**
     * Retrieves a page from the list of top rated movies in TMDB.
     *
     * @param page The required page.
     * @return The list of movies on that page.
     * @throws JSONException If the format of the returned response is not valid.
     * @throws IOException   In case of network error.
     */
    public static List<Movie> fetchTopRated(int page) throws JSONException, IOException {
        Uri url = TMDBHelper.BASE_URL
            .buildUpon()
            .appendPath(TMDBHelper.PATH_TOP_RATED)
            .appendQueryParameter("page", String.valueOf(page))
            .build();

        return TMDBHelper.fetchMovieList(url);
    }

    // · // · // · From Provider · // · // · //

    /**
     * Retrieves the list of favorite movies saved locally.
     *
     * @return The list of user saved favorite movies.
     */
    public static List<Movie> fetchFavorites(Context context) {
        // Fetch from DB
        Uri dirUri = PopularMoviesContract.FavoriteMoviesEntry.CONTENT_URI;
        Cursor favsCursor = context.getContentResolver().query(dirUri, null, null, null, null);

        final List<Movie> movies = new ArrayList<>();

        // Check
        if (favsCursor == null || favsCursor.getCount() == 0) {
            return movies;
        }

        // Parse
        while (favsCursor.moveToNext()) {
            try {
                Movie movie = Movie.fromCursor(favsCursor);
                // (List of favorites -> ever favorites)
                movie.setFavorite(true);
                movies.add(movie);
            } catch (ParseException e) {
                // Watch for the date parsing exception
                Log.w(TAG, "Unable to parse the release date of a movie.", e);
            }
        }

        // Terminate cursor
        favsCursor.close();

        return movies;
    }

}
