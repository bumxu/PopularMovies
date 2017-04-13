package com.bumxu.android.popularmovies.data.repository;

import android.net.Uri;

import com.bumxu.android.popularmovies.data.MovieReview;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class MovieReviewsFeed {
    private static final String PATH_REVIEWS = "reviews";

    /**
     * Retrieves a page from the list of revies from the specified movie in TMDB.
     *
     * @param movieId The movie id to retrieve reviews from.
     * @param page    The required page.
     * @return The list of movie reviews on that page.
     * @throws JSONException If the format of the returned response is not valid.
     * @throws IOException   In case of network error.
     */
    public static List<MovieReview> fetchPage(long movieId, int page) throws IOException, JSONException {
        // Request URL
        String id = String.valueOf(movieId);
        Uri url = TMDBHelper.BASE_URL
            .buildUpon()
            .appendPath(id)
            .appendPath(PATH_REVIEWS)
            .appendQueryParameter("page", String.valueOf(page))
            .build();

        ///

        List<MovieReview> reviews = new ArrayList<>();

        // Parse
        JSONObject data = TMDBHelper.fetchJSON(url);
        // Iterate
        JSONArray entries = data.getJSONArray("results");
        for (int i = 0; i < entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);
            reviews.add(
                MovieReview.fromJSON(entry));
        }

        return reviews;
    }
}
