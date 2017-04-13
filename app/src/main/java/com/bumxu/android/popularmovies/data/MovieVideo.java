package com.bumxu.android.popularmovies.data;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings({"WeakerAccess", "unused"})
public class MovieVideo {
    private final String mName;
    private final String mSite;
    private final String mKey;
    private final int mDuration;

    public MovieVideo(String name, int duration, String site, String key) {
        mName = name;
        mDuration = duration;
        mSite = site;
        mKey = key;
    }

    public String getName() {
        return mName;
    }

    public String getSite() {
        return mSite;
    }

    public String getKey() {
        return mKey;
    }

    public int getDuration() {
        return mDuration;
    }

    /**
     * Creates a MovieVideo from a JSONObject movie video entry of type "Trailer" provided by TMDB.
     *
     * @param entry The JSONObject provided by TMDB movie video entry.
     * @return The resulting movie review object with the entry data.
     * @throws JSONException If the format of the JSON object is not valid or unexpected.
     */
    public static MovieVideo fromJSON(JSONObject entry) throws JSONException {
        // Parse JSON entry
        String name = entry.getString("name");
        int duration = entry.getInt("size");
        String site = entry.getString("site");
        String key = entry.getString("key");

        // Return a new created movie review
        return new MovieVideo(name, duration, site, key);
    }
}
