package com.bumxu.android.popularmovies.data;

import org.json.JSONException;
import org.json.JSONObject;

public class MovieReview {
    private final String author;
    private final String content;

    @SuppressWarnings("WeakerAccess")
    public MovieReview(String author, String content) {
        this.author = author;
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    /**
     * Creates a MovieReview from a JSONObject movie review entry provided by TMDB.
     *
     * @param entry The JSONObject provided by TMDB movie review entry.
     * @return The resulting movie review object with the entry data.
     * @throws JSONException If the format of the JSON object is not valid or unexpected.
     */
    public static MovieReview fromJSON(JSONObject entry) throws JSONException {
        // Parse JSON entry
        String author = entry.getString("author");
        String content = entry.getString("content");

        // Return a new created movie review
        return new MovieReview(author, content);
    }
}
