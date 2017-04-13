package com.bumxu.android.popularmovies.app;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumxu.android.popularmovies.app.util.NetworkUtils;
import com.bumxu.android.popularmovies.data.Movie;
import com.bumxu.android.popularmovies.data.MovieVideo;
import com.bumxu.android.popularmovies.data.loaders.MovieTrailersLoader;
import com.bumxu.android.popularmovies.data.repository.MovieSyncHelper;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.internal.FastBlur;

import static com.bumxu.android.popularmovies.app.MovieTrailersAdapter.YOUTUBE_WATCH_URL;


public class MovieDetailsActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    // [ Activity components ]
    @BindView(R.id.image_poster) ImageView mPosterImageView;
    @BindView(R.id.text_title) TextView mTitleTextView;
    @BindView(R.id.text_original_title) TextView mOriginalTitleTextView;
    @BindView(R.id.text_release_date) TextView mReleaseDateTextView;
    @BindView(R.id.button_save_favorite) AppCompatButton mSaveFavoriteButton;
    @BindView(R.id.rating_bar) RatingBar mRatingBar;
    @BindView(R.id.text_rating_note) TextView mRatingTextView;
    @BindView(R.id.text_synopsis) TextView mSynopsisTextView;
    @BindView(R.id.button_read_reviews) Button mReadReviewsButton;
    // Trailers
    @BindView(R.id.rv_tailers) RecyclerView mTrailersRecyclerView;
    @BindView(R.id.layout_trailers_fallback) View mTrailersFallback;
    @BindView(R.id.text_trailers_fallback) TextView mTrailersFallbackTextView;
    @BindView(R.id.progress_trailers_loading) ProgressBar mTrailersFallbackProgressBar;
    @BindView(R.id.button_trailers_retry) View mTrailersRetryButton;
    // Data last updated
    @BindView(R.id.text_last_update) TextView mLastUpdateTextView;
    // Background decoration
    @BindView(R.id.image_background_decorator) ImageView mBackgroundDecoratorImageView;

    // Movie object target of the activity
    private Movie mMovie;
    // Position in parent recyclerview for update changes when close
    int mParentPosition;

    // Static ID for trailers async loader
    private static final int MOVIE_TRAILERS_LOADER = 775;
    // Callbacks for the movie trailers async loader
    private final TrailersLoaderCallbacks mTrailersLoaderCallbacks = new TrailersLoaderCallbacks();
    // Local copy of trailers
    private List<MovieVideo> mTrailers;

    // Reference to favorite menu option
    private MenuItem mFavoriteMenuItem;

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Assign the layout to the activity
        setContentView(R.layout.activity_movie_details);
        // Bind the elements
        ButterKnife.bind(this);

        // Setup custom action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Display back option
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Hide title
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Setup buttons click
        mReadReviewsButton.setOnClickListener(this);
        mSaveFavoriteButton.setOnClickListener(this);
        mTrailersRetryButton.setOnClickListener(this);

        // Apply the parent intent
        applyIntent();

        // Display trailers loading interface
        showUiTrailersLoading();
    }

    @Override
    public void onBackPressed() {
        // Resend position and movie
        Intent data = new Intent();
        data.putExtra("movie", mMovie);
        data.putExtra("position", mParentPosition);

        setResult(RESULT_OK, data);

        super.onBackPressed();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Create the menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_menu, menu);

        // Save reference to favorite menu item and apply state
        mFavoriteMenuItem = menu.findItem(R.id.menu_item_favorite);
        setFavoriteButton(mMovie.isFavorite());

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_item_share:
                // Initial message
                String shareText = getString(R.string.share_movie, mMovie.getTitle());

                // Look for trailer...
                if (mTrailers != null && mTrailers.size() > 0) {
                    shareText += "\n" + getString(R.string.share_trailer,
                        YOUTUBE_WATCH_URL + mTrailers.get(0).getKey());
                }

                // Share
                ShareCompat.IntentBuilder
                    .from(this)
                    .setType("text/plain")
                    .setChooserTitle("Share movie")
                    .setText(shareText)
                    .startChooser();

                return true;
            case R.id.menu_item_favorite:
                // Switch favorite
                setFavorite(!mMovie.isFavorite());

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            // Click on "read reviews" button
            case R.id.button_read_reviews:
                // Create intent for reviews
                Intent intent = new Intent(this, MovieReviewsActivity.class);
                intent.putExtra("movie", mMovie);

                startActivity(intent);

                break;
            // Click on "save/remove favorite" button
            case R.id.button_save_favorite:
                setFavorite(!mMovie.isFavorite());
                break;
            // Click on "Retry" load trailers
            case R.id.button_trailers_retry:
                retryTrailersLoad();
                break;
        }
    }

    /**
     * Saves/removes the movie from favorite list.
     *
     * @param option Saved as favorite, or not.
     */
    private void setFavorite(boolean option) {
        // Do appropriate operation
        if (option) {
            MovieSyncHelper.saveFavoriteMovie(getBaseContext(), mMovie);
        } else {
            MovieSyncHelper.removeFavoriteMovie(getBaseContext(), mMovie);
        }

        // Apply to movie object
        mMovie.setFavorite(option);
        // Apply to button
        setFavoriteButton(option);
    }

    /**
     * Change the favorite button content according to provided option.
     *
     * @param option Marked as favorite, or not.
     */
    private void setFavoriteButton(boolean option) {
        if (option) {
            // Set text
            mSaveFavoriteButton.setText(R.string.do_remove_favorite);
            // Action bar option
            if (mFavoriteMenuItem != null) {
                mFavoriteMenuItem.setIcon(R.drawable.mi_favorite_yes);
            }
            // Change color
            ViewCompat.setBackgroundTintList(
                mSaveFavoriteButton,
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorFavorite))
            );
        } else {
            // Set text
            mSaveFavoriteButton.setText(R.string.do_save_favorite);
            // Action bar option
            if (mFavoriteMenuItem != null) {
                mFavoriteMenuItem.setIcon(R.drawable.mi_favorite_no);
            }
            // Change color
            ViewCompat.setBackgroundTintList(
                mSaveFavoriteButton,
                ColorStateList.valueOf(ContextCompat.getColor(this, R.color.colorPrimary))
            );
        }
    }

    /**
     * Build the activity with the explicit intent.
     */
    private void applyIntent() {
        // Recover intent
        Intent intent = getIntent();

        // Save movie and parent position
        mMovie = intent.getParcelableExtra("movie");
        mParentPosition = intent.getIntExtra("position", -1);

        try {
            // String fields
            mTitleTextView.setText(mMovie.getTitle());
            mOriginalTitleTextView.setText(mMovie.getOriginalTitle());
            mRatingTextView.setText(getString(R.string.current_rating, mMovie.getRating()));
            mSynopsisTextView.setText(mMovie.getOverview());
            // Rating stars
            mRatingBar.setRating((float) (mMovie.getRating() / 2));

            // Poster ImageView
            Glide.with(this).load(mMovie.getPosterUrl(Movie.POSTERWIDTH_342))
                .asBitmap()
                .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                .dontAnimate()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(final Bitmap posterBmp, GlideAnimation<? super Bitmap> glideAnimation) {
                        // Set poster
                        mPosterImageView.setImageBitmap(posterBmp);

                        // Blur image, apply to background, and display
                        Bitmap blurred = FastBlur.blur(posterBmp, 20, false);
                        Animation bgAnim = AnimationUtils.loadAnimation(MovieDetailsActivity.this, R.anim.background_fadein);
                        // · · ·
                        mBackgroundDecoratorImageView.setImageBitmap(blurred);
                        mBackgroundDecoratorImageView.startAnimation(bgAnim);
                    }
                });


            // (User local) release date
            DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(this);
            String date = dateFormat.format(mMovie.getReleaseDate());
            mReleaseDateTextView.setText(date);

            // (User local) obtained date and time
            dateFormat = android.text.format.DateFormat.getLongDateFormat(this);
            DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(this);
            Date update = mMovie.getUpdateTime();
            if (update != null) {
                mLastUpdateTextView.setText(
                    getString(R.string.updated_date) + " "
                        + dateFormat.format(update) + " · "
                        + timeFormat.format(update));
            }

        } catch (ClassCastException e) {
            Log.e(TAG, "The received intent doesn't contain a valid (Parcel) Movie.", e);
            finish();
        }

        // Setup the movie trailers carousel

        final LinearLayoutManager trailersLayout = new LinearLayoutManager(this);
        trailersLayout.setOrientation(LinearLayout.HORIZONTAL);

        mTrailersRecyclerView.setLayoutManager(trailersLayout);

        // Start/restart the movie trailers loader

        final LoaderManager loaderManager = getSupportLoaderManager();
        if (loaderManager.getLoader(MOVIE_TRAILERS_LOADER) == null) {
            loaderManager.initLoader(MOVIE_TRAILERS_LOADER, null, mTrailersLoaderCallbacks);
        } else {
            loaderManager.restartLoader(MOVIE_TRAILERS_LOADER, null, mTrailersLoaderCallbacks);
        }
    }

    /**
     * Shows the UI for trailers loading (progressbar).
     */
    private void showUiTrailersLoading() {
        mTrailersRecyclerView.setVisibility(View.GONE);

        mTrailersFallback.setVisibility(View.VISIBLE);
        mTrailersRetryButton.setVisibility(View.GONE);
        mTrailersFallbackTextView.setVisibility(View.GONE);
        mTrailersFallbackProgressBar.setVisibility(View.VISIBLE);
    }

    /**
     * Shows the obtained trailers layout.
     */
    private void showUiTrailersLoaded() {
        mTrailersRecyclerView.setVisibility(View.VISIBLE);

        mTrailersFallback.setVisibility(View.GONE);
    }

    /**
     * Shows a message if load trailers operations was bad.
     *
     * @param resource The string resource id of the message to display.
     */
    private void showUiTrailersMessage(@StringRes int resource) {
        String msg = getString(resource);

        mTrailersRecyclerView.setVisibility(View.GONE);

        mTrailersFallback.setVisibility(View.VISIBLE);
        mTrailersRetryButton.setVisibility(View.VISIBLE);
        mTrailersFallbackTextView.setVisibility(View.VISIBLE);
        mTrailersFallbackProgressBar.setVisibility(View.GONE);

        mTrailersFallbackTextView.setText(msg);
    }

    /**
     * Tries to load movie trailers again.
     */
    private void retryTrailersLoad() {
        // Show loading UI
        showUiTrailersLoading();

        // Restart the movie trailers loader
        final LoaderManager loaderManager = getSupportLoaderManager();
        loaderManager.restartLoader(MOVIE_TRAILERS_LOADER, null, mTrailersLoaderCallbacks);
    }

    /**
     * LoaderCallbacks for the movie trailers custom async loader.
     */
    private class TrailersLoaderCallbacks implements LoaderManager.LoaderCallbacks<List<MovieVideo>> {
        @Override
        public Loader<List<MovieVideo>> onCreateLoader(int id, Bundle args) {
            // Create and return custom loader for current movie
            return new MovieTrailersLoader(MovieDetailsActivity.this, mMovie.getId());
        }

        @Override
        public void onLoadFinished(Loader<List<MovieVideo>> loader, List<MovieVideo> trailers) {
            // No connection or error
            if (trailers == null) {
                if (NetworkUtils.isOnline(MovieDetailsActivity.this)) {
                    showUiTrailersMessage(R.string.msg_error_trailers);
                } else {
                    showUiTrailersMessage(R.string.msg_trailers_no_internet);
                }
                return;
            }

            // No trailers for movie
            if (trailers.isEmpty()) {
                showUiTrailersMessage(R.string.msg_no_trailers);
                return;
            }

            // Save trailers and display results
            mTrailers = trailers;
            mTrailersRecyclerView.setAdapter(new MovieTrailersAdapter(MovieDetailsActivity.this, trailers));
            showUiTrailersLoaded();
        }

        @Override
        public void onLoaderReset(Loader<List<MovieVideo>> loader) {
        }
    }
}
