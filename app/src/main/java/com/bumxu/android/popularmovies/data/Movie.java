package com.bumxu.android.popularmovies.data;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.bumxu.android.popularmovies.data.provider.PopularMoviesContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressWarnings("unused")
public class Movie implements Parcelable {
    private static final String POSTER_BASE_URL = "http://image.tmdb.org/t/p/";
    public static final String POSTERWIDTH_92 = "w92";
    public static final String POSTERWIDTH_154 = "w154";
    public static final String POSTERWIDTH_185 = "w185";
    public static final String POSTERWIDTH_342 = "w342";
    public static final String POSTERWIDTH_500 = "w500";
    public static final String POSTERWIDTH_780 = "w780";
    public static final String POSTERWIDTH_ORIGINAL = "original";

    private final long id;
    private final String title;
    private final String originalTitle;
    private final String overview;
    private final String poster;
    private final double rating;
    private final Date releaseDate;

    private boolean metaFavorite;
    private Date metaUpdatedTime;

    // Basic constructor
    public Movie(long id, String title, String originalTitle, String overview, String poster, double rating, Date releaseDate) {
        this.id = id;
        this.title = title;
        this.originalTitle = originalTitle;
        this.overview = overview;
        this.poster = poster;
        this.rating = rating;
        this.releaseDate = releaseDate;

        this.metaFavorite = false;
        this.metaUpdatedTime = new Date();
    }

    // Parcelable constructor (CREATOR)
    private Movie(Parcel parcel) {
        id = parcel.readLong();
        title = parcel.readString();
        originalTitle = parcel.readString();
        overview = parcel.readString();
        poster = parcel.readString();
        rating = parcel.readDouble();
        releaseDate = (Date) parcel.readSerializable();

        metaFavorite = parcel.readByte() != 0;
        metaUpdatedTime = (Date) parcel.readSerializable();
    }

    // ATTRIBUTES

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public String getOverview() {
        return overview;
    }

    public double getRating() {
        return rating;
    }

    public Date getReleaseDate() {
        return releaseDate;
    }

    public String getPoster() {
        return this.poster;
    }

    public Uri getPosterUrl(String size) {
        return Uri.parse(POSTER_BASE_URL + size + this.poster);
    }

    public boolean isFavorite() {
        return metaFavorite;
    }

    public void setFavorite(boolean favorite) {
        this.metaFavorite = favorite;
    }

    public Date getUpdateTime() {
        return metaUpdatedTime;
    }

    @SuppressWarnings("WeakerAccess")
    public void setUpdateTime(Date time) {
        metaUpdatedTime = time;
    }


    // PARCELABLE

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(id);
        parcel.writeString(title);
        parcel.writeString(originalTitle);
        parcel.writeString(overview);
        parcel.writeString(poster);
        parcel.writeDouble(rating);
        parcel.writeSerializable(releaseDate);

        parcel.writeByte((byte) (metaFavorite ? 1 : 0));
        parcel.writeSerializable(metaUpdatedTime);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel parcel) {
            return new Movie(parcel);
        }

        @Override
        public Movie[] newArray(int i) {
            return new Movie[i];
        }
    };

    // TRANSFORMERS

    /**
     * Creates a Movie from a JSONObject movie entry provided by TMDB.
     *
     * @param entry The JSONObject provided by TMDB movie entry.
     * @return The resulting movie object with the entry data.
     * @throws ParseException If the format of a strict field is invalid (dates, times, numbers, ...)
     * @throws JSONException  If the format of the JSON object is not valid or unexpected.
     */
    @SuppressLint("SimpleDateFormat")
    public static Movie fromJSON(JSONObject entry) throws ParseException, JSONException {
        // Fields
        long id = entry.getLong("id");
        String title = entry.getString("title");
        String originalTitle = entry.getString("original_title");
        String synopsis = entry.getString("overview");
        String poster = entry.getString("poster_path");
        double rating = entry.getDouble("vote_average");

        // Special fields
        String date = entry.getString("release_date");
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
        Date releaseDate = parser.parse(date);

        // Create and return
        return new Movie(id, title, originalTitle, synopsis, poster, rating, releaseDate);
    }

    /**
     * Crates a Movie from a standard Cursor position provided by PopularMoviesProvider.
     *
     * @param cursor The Cursor provided by the inner Provider in accord with PopularMoviesContract.
     * @return The resulting movie object with the entry data.
     * @throws ParseException If the format of a strict field is invalid (dates, times, numbers, ...)
     */
    @SuppressLint("SimpleDateFormat")
    public static Movie fromCursor(Cursor cursor) throws ParseException {
        // Fields
        long id = cursor.getLong(cursor.getColumnIndex(PopularMoviesContract.FavoriteMoviesEntry._ID));
        String title = cursor.getString(cursor.getColumnIndex(PopularMoviesContract.FavoriteMoviesEntry.COLUMN_TITLE));
        String originalTitle = cursor.getString(cursor.getColumnIndex(PopularMoviesContract.FavoriteMoviesEntry.COLUMN_ORIGINAL_TITLE));
        String synopsis = cursor.getString(cursor.getColumnIndex(PopularMoviesContract.FavoriteMoviesEntry.COLUMN_SYNOPSIS));
        String poster = cursor.getString(cursor.getColumnIndex(PopularMoviesContract.FavoriteMoviesEntry.COLUMN_POSTER));
        double rating = cursor.getDouble(cursor.getColumnIndex(PopularMoviesContract.FavoriteMoviesEntry.COLUMN_USERS_RATING));

        // Special fields
        String date = cursor.getString(cursor.getColumnIndex(PopularMoviesContract.FavoriteMoviesEntry.COLUMN_RELEASE_DATE));
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
        Date releaseDate = parser.parse(date);

        // Create new
        Movie movie = new Movie(id, title, originalTitle, synopsis, poster, rating, releaseDate);

        // Apply the saved download time
        int updatedTs = cursor.getInt(cursor.getColumnIndex(PopularMoviesContract.FavoriteMoviesEntry.COLUMN_UPDATED_TIME));
        Date updated = new Date((long) updatedTs * 1000);
        movie.setUpdateTime(updated);

        return movie;
    }
}
