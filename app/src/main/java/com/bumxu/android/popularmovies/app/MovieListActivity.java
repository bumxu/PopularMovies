package com.bumxu.android.popularmovies.app;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumxu.android.lab.ProgressiveAdapter;
import com.bumxu.android.lab.ProgressiveAdapter.ViewHolder;
import com.bumxu.android.popularmovies.app.util.NetworkUtils;
import com.bumxu.android.popularmovies.data.Movie;
import com.bumxu.android.popularmovies.data.repository.MovieListFeed;
import com.bumxu.android.popularmovies.data.repository.MovieSyncHelper;

import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sephiroth.android.library.bottomnavigation.BottomNavigation;


public class MovieListActivity extends AppCompatActivity implements BottomNavigation.OnMenuItemSelectionListener {
    private static final String TAG = MovieListActivity.class.getSimpleName();

    private final static int FROM_MOVIE_DETAIL = 15;

    // Possible sorting criteria
    private enum Sorting {
        POPULAR, TOP_RATED, FAVORITES
    }

    // [ Activity components ]
    // RecyclerView
    @BindView(R.id.rv_popular_movies) RecyclerView mMoviesRecyclerView;
    // Empty message for favorite list
    @BindView(R.id.tv_empty_favorites) TextView mEmptyFavoritesTextView;
    // Bottom navigation bar
    @BindView(R.id.bottom_navigation) BottomNavigation mBottomNavigation;
    // Future reference to the network-issues snackbar
    Snackbar mNetworkSnackbar;

    // Current adapter for the movie list
    private ProgressiveAdapter<Movie> mMoviesAdapter;
    // Handler for the custom adapter
    private final ProgressiveAdapter.Handler<Movie> mMovieListHandler = new MovieListHandler();
    // Current sorting criteria being applied
    private Sorting mSorting;

    // · // · // · Lifecycle · // · // · //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Assign the layout to the activity
        setContentView(R.layout.activity_movies);
        // Bind the components
        ButterKnife.bind(this);

        // Setup bottom navigation
        mBottomNavigation.setOnMenuItemClickListener(this);

        // Setup recyclerview
        mMoviesRecyclerView.setHasFixedSize(true);

        // Dynamic number of columns
        // OLD: Using 185px images I'm going to take 150dp as reference column width
        // NOW: Using dimens.xml file
        final int colNumber = Math.max(2, getDisplayWidthDp() / getResources().getInteger(R.integer.ref_column_size));
        mMoviesRecyclerView.setLayoutManager(new GridLayoutManager(this, colNumber));

        // Initialize adapter or apply saved state
        Map<Integer, Movie> movies = null;

        if (savedInstanceState == null) {
            // Initialize sorting criteria
            mSorting = Sorting.POPULAR;
        } else {
            // Restore sorting criteria
            mSorting = (Sorting) savedInstanceState.getSerializable("sorting");
            // Restored adapter state
            //noinspection unchecked
            movies = (Map<Integer, Movie>) savedInstanceState.getSerializable("movies");
        }

        updateAdapter(movies);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save current sort criteria
        outState.putSerializable("sorting", mSorting);
        // Save current status
        outState.putSerializable("movies", (Serializable) mMoviesAdapter.getCollection());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If returning from movie detail view
        if (resultCode == RESULT_OK && requestCode == FROM_MOVIE_DETAIL) {
            // Get position and movie object sent
            Movie movie = data.getParcelableExtra("movie");
            int position = data.getIntExtra("position", -1);

            if (position > -1) {
                Movie movieRef = mMoviesAdapter.getCollection().get(position);
                // With same position, ID must be the same
                // If favorite values has canged
                if (movieRef.getId() == movie.getId() && movieRef.isFavorite() != movie.isFavorite()) {
                    if (movie.isFavorite()) {
                        // Set on parent
                        movieRef.setFavorite(true);
                        // Update element
                        mMoviesAdapter.notifyItemChanged(position);
                    } else {
                        // Set on parent
                        movieRef.setFavorite(false);

                        if (mSorting == Sorting.FAVORITES) {
                            // Reset the view
                            updateAdapter(null);
                        } else {
                            // Or update the element
                            mMoviesAdapter.notifyItemChanged(position);
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Call adapter's destroy to stop open threads
        if (mMoviesAdapter != null) {
            mMoviesAdapter.destroy();
        }
    }

    // · // · // · Actionbar Menu · // · // · //

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Create the menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Update adapter when refresh button tapped to refresh all results
        if (item.getItemId() == R.id.mi_refresh) {
            updateAdapter(null);
        }

        return super.onOptionsItemSelected(item);
    }

    // · // · // · Bottom Navigation · // · // · //

    @Override
    public void onMenuItemSelect(@IdRes int itemId, int i1, boolean b) {
        // Change sorting criteria
        switch (itemId) {
            case R.id.action_popular:
                mSorting = Sorting.POPULAR;
                break;
            case R.id.action_top:
                mSorting = Sorting.TOP_RATED;
                break;
            case R.id.action_favorites:
                mSorting = Sorting.FAVORITES;
                break;
        }
        // Apply
        updateAdapter(null);
    }

    @Override
    public void onMenuItemReselect(@IdRes int i, int i1, boolean b) {
        // On reselect -> scroll to top
        mMoviesRecyclerView.scrollToPosition(0);
    }

    // · // · // · Auxiliar · // · // · //

    /**
     * Setup a new ProgressiveAdapter to the movies RecyclerView for the current sort criteria
     * and the provided initial data set (nullable).
     *
     * @param from A Map{Movie position, Movie} with an initial set of cached movies for the adapter.
     */
    private void updateAdapter(@Nullable Map<Integer, Movie> from) {
        // Hide messages
        mEmptyFavoritesTextView.setVisibility(View.GONE);

        // Let's try a network refresh, so hide snackbar if it's visible
        if (mNetworkSnackbar != null) {
            mNetworkSnackbar.dismiss();
        }
        // Destroy old adapter (important cancel threads)
        if (mMoviesAdapter != null) {
            mMoviesAdapter.destroy();
        }

        // Create new adapter
        mMoviesAdapter = new ProgressiveAdapter<>(20, mMovieListHandler);
        // Appy cached data
        if (from != null) {
            mMoviesAdapter.restoreCollection(from);
        }
        // Apply
        mMoviesRecyclerView.setAdapter(mMoviesAdapter);
    }

    /**
     * Shows the connectivity issues snackbar, setting appropriately its "dismissal" handler.
     */
    private void showSnack() {
        // Create
        mNetworkSnackbar = Snackbar.make(findViewById(R.id.snackbar_goes_here), R.string.msg_no_internet, Snackbar.LENGTH_INDEFINITE);
        // Add button (listener required)
        mNetworkSnackbar.setAction(R.string.do_retry, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        // Set listener
        mNetworkSnackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                // Destroy snackbar
                mNetworkSnackbar = null;
                // Refresh data
                mMoviesAdapter.notifyDataSetChanged();
            }
        });
        // Show
        mNetworkSnackbar.show();
    }

    /**
     * Retrieves the device screen width in dp.
     * SO: https://goo.gl/NWAAnW
     *
     * @return Device screen width in dp.
     */
    private int getDisplayWidthDp() {
        Configuration configuration = this.getResources().getConfiguration();
        return configuration.screenWidthDp;
    }

    // · // · // · // · // · //

    /**
     * A ProgressiveAdapter->Handler that controls the adapter behavior.
     */
    private class MovieListHandler extends ProgressiveAdapter.Handler<Movie> {
        @Override
        public Context getContext() {
            return MovieListActivity.this;
        }

        @Override
        public ViewHolder<Movie> createViewHolder(ViewGroup parent) {
            // Inflate layout
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_movie, parent, false);
            // Return new custom holder
            return new MovieItemViewHolder(view);
        }

        @Override
        public List<Movie> backgroundRequest(final int page) {
            // Result
            List<Movie> movies = null;

            if (mSorting == Sorting.POPULAR || mSorting == Sorting.TOP_RATED) {
                // Request popular or top rated movies from appropriate methods
                try {
                    if (mSorting == Sorting.POPULAR) {
                        movies = MovieListFeed.fetchPopular(page + 1);
                    } else {
                        movies = MovieListFeed.fetchTopRated(page + 1);
                    }

                    // Mark movies saved as user favorites, and update these last
                    MovieSyncHelper.markFavoriteMovies(MovieListActivity.this, movies);

                    // UI: Hide snackbar if visible
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mNetworkSnackbar != null) {
                                mNetworkSnackbar.dismiss();
                            }
                        }
                    });
                } catch (JSONException e) {
                    Log.e(TAG, "Unexpected error reading fetched chunk of movies.", e);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to get a chunk of movies from the Internet.", e);

                    // Check connection
                    if (!NetworkUtils.isOnline(MovieListActivity.this) && mNetworkSnackbar == null) {
                        // UI: Show snackbar
                        runOnUiThread(new Runnable() {
                            public void run() {
                                showSnack();
                            }
                        });
                    }
                }
            } else {
                // Request favorite movies from content provider
                movies = MovieListFeed.fetchFavorites(MovieListActivity.this);

                if (movies.size() == 0) {
                    // UI: Show empty message
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mEmptyFavoritesTextView.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            return movies;
        }

        /**
         * ProgressiveAdapter->ViewHolder for the movie list item.
         */
        private class MovieItemViewHolder extends ViewHolder<Movie> implements View.OnClickListener {
            private Movie mMovie;

            private final View mRippleOverlay;
            private final ImageView mPosterImageView;
            private final ImageView mFavoriteMarkImageView;

            MovieItemViewHolder(View view) {
                super(view);

                // Build the item view
                mRippleOverlay = view.findViewById(R.id.view_ripple_overlay);
                mPosterImageView = (ImageView) view.findViewById(R.id.iv_item_poster);
                mFavoriteMarkImageView = (ImageView) view.findViewById(R.id.image_favorite_mark);

                // Set click listener (on top element) for tapa on movie
                mRippleOverlay.setOnClickListener(this);
            }

            @Override
            public void bind(Movie movie) {
                mMovie = movie;

                // Set poster with glide
                Glide.with(getContext()).load(movie.getPosterUrl(Movie.POSTERWIDTH_185))
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .into(mPosterImageView);

                // Show/hide favorite mark
                if (movie.isFavorite()) {
                    mFavoriteMarkImageView.setVisibility(View.VISIBLE);
                } else {
                    mFavoriteMarkImageView.setVisibility(View.GONE);
                }

                // Disable click listener / ripple effect
                mRippleOverlay.setEnabled(true);
            }

            @Override
            public void clear() {
                // Break with movie
                mMovie = null;

                // Clear animation traces, poster image
                mPosterImageView.clearAnimation();
                Glide.clear(mPosterImageView);

                // Hide favorite mark
                mFavoriteMarkImageView.setVisibility(View.GONE);

                // Disable click listener / ripple effect
                mRippleOverlay.setEnabled(false);
            }

            @Override
            public void onClick(View view) {
                if (mMovie != null) {
                    // Prepare intent with the appropriate movie object
                    Intent intent = new Intent(MovieListActivity.this, MovieDetailsActivity.class);
                    intent.putExtra("movie", mMovie);
                    intent.putExtra("position", getAdapterPosition());

                    // Start "details" activity
                    startActivityForResult(intent, FROM_MOVIE_DETAIL);
                }
            }
        }
    }
}
