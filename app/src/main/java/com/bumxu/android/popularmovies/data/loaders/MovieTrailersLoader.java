package com.bumxu.android.popularmovies.data.loaders;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.bumxu.android.popularmovies.data.MovieVideo;
import com.bumxu.android.popularmovies.data.repository.MovieVideosFeed;

import java.util.List;


/**
 * AsyncLoader of trailers for a movie.
 */
@SuppressWarnings("ALL")
public class MovieTrailersLoader extends AsyncTaskLoader<List<MovieVideo>> {
    private final long mMovieId;
    private List<MovieVideo> mCachedResult;
    private Exception mException;

    /**
     * @param context A context.
     * @param movieId The id of the movie for which retrieve trailers.
     */
    public MovieTrailersLoader(final Context context, final long movieId) {
        super(context);

        this.mMovieId = movieId;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();

        // Load or return cached result
        if (mCachedResult == null) {
            forceLoad();
        } else {
            deliverResult(mCachedResult);
        }
    }

    @Override
    public List<MovieVideo> loadInBackground() {
        // Cache result or save exception
        try {
            mCachedResult = MovieVideosFeed.fetchAllTrailers(mMovieId);
        } catch (Exception e) {
            mException = e;
        }

        return mCachedResult;
    }

    @SuppressWarnings("unused")
    public Exception getException() {
        return mException;
    }
}
