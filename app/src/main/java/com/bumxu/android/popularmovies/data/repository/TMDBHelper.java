package com.bumxu.android.popularmovies.data.repository;

import android.net.Uri;
import android.util.Log;

import com.bumxu.android.popularmovies.data.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;


class TMDBHelper {
    ///////////////////
    private static final String API_KEY = "PASTE_YOUR_API_KEY_HERE";
    ///////////////////

    private static final String TAG = TMDBHelper.class.getSimpleName();

    // Quick setup flags
    private static final int TIME_OUT = 4000;
    private static final boolean CACHE_RESULTS = false;

    static final Uri BASE_URL = Uri.parse("http://api.themoviedb.org/3/movie");

    public static final String PATH_POPULAR = "popular";
    public static final String PATH_TOP_RATED = "top_rated";

    /**
     * Retrieves a list of movies from the "results" field of the given TMDB API request.
     *
     * @param url A valid URL whose "results" field is a valid list of movies from TMDB.
     * @return A standard list collection of Movie objects with the movies from the request.
     * @throws JSONException If the format of the JSON object is not valid or unexpected.
     * @throws IOException   In case of network error.
     */
    public static List<Movie> fetchMovieList(Uri url) throws JSONException, IOException {
        List<Movie> movies = new ArrayList<>();

        // Parse
        JSONObject data = fetchJSON(url);
        // Iterate
        JSONArray entries = data.getJSONArray("results");
        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);
            try {
                movies.add(Movie.fromJSON(entry));
            } catch (ParseException e) {
                // Watch for the date parsing exception
                Log.w(TAG, "Unable to parse the release date of a movie.", e);
            }
        }

        return movies;
    }

    /**
     * Fetch JSON from TMDB uri, adding the required API secret key.
     *
     * @param url A valid URI from a TMDB JSON endpoint.
     * @return The appropriate JSONObject.
     */
    public static JSONObject fetchJSON(Uri url) throws JSONException, IOException {
        url = url.buildUpon().appendQueryParameter("api_key", API_KEY).build();
        return fetchJSON(new URL(url.toString()));
    }

    /**
     * Fetch JSON from TMDB url (not uri), with the required API secret key already included.
     *
     * @param url A valid URL from a TMDB JSON endpoint, with the API secret already appened.
     * @return The appropriate JSONObject.
     */
    private static JSONObject fetchJSON(URL url) throws JSONException, IOException {
        String raw;

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setUseCaches(CACHE_RESULTS);
        connection.setConnectTimeout(TIME_OUT);
        connection.setReadTimeout(TIME_OUT);
        InputStream in = connection.getInputStream();
        Scanner scanner = new Scanner(in).useDelimiter("\\A");

        // If stream is empty return empty result
        if (!scanner.hasNext()) {
            return new JSONObject();
        }

        // Read stream
        raw = scanner.next();
        // Parse & return
        return new JSONObject(raw);
    }
}
