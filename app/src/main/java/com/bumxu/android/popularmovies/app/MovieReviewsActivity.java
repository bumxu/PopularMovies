package com.bumxu.android.popularmovies.app;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumxu.android.lab.ProgressiveAdapter;
import com.bumxu.android.lab.ProgressiveAdapter.Handler;
import com.bumxu.android.lab.ProgressiveAdapter.ViewHolder;
import com.bumxu.android.popularmovies.app.util.NetworkUtils;
import com.bumxu.android.popularmovies.data.Movie;
import com.bumxu.android.popularmovies.data.MovieReview;
import com.bumxu.android.popularmovies.data.repository.MovieReviewsFeed;

import org.json.JSONException;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MovieReviewsActivity extends AppCompatActivity {
    private static final String TAG = MovieReviewsActivity.class.getSimpleName();

    // [ Activity components ]
    @BindView(R.id.rv_review_list) RecyclerView mRecyclerView;
    @BindView(R.id.text_reviews_fallback) TextView mFallbackTextView;

    // ProgressiveAdapter and Handler
    private final Handler<MovieReview> mHandler = new ReviewListHandler();
    private ProgressiveAdapter<MovieReview> mReviewListAdapter;

    // Target movie
    private Movie mMovie;

    @Override
    @SuppressWarnings("ConstantConditions")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Assign the layout to the activity
        setContentView(R.layout.activity_movie_reviews);
        // Bind the elements
        ButterKnife.bind(this);

        // Display back option
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Recover movie from intent
        mMovie = getIntent().getParcelableExtra("movie");

        // Check...
        if (mMovie == null) {
            Toast.makeText(this, R.string.msg_invalid_movie_reviews, Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Setup recyclerview and apply
        mReviewListAdapter = new ProgressiveAdapter<>(20, mHandler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(mReviewListAdapter);

        // Setup title and subtitle
        getSupportActionBar().setTitle(R.string.head_reviews);
        getSupportActionBar().setSubtitle(mMovie.getTitle());

        if (savedInstanceState != null) {
            // Restore adapter
            //noinspection unchecked
            mReviewListAdapter.restoreCollection((Map<Integer, MovieReview>) savedInstanceState.getSerializable("reviews"));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Save current status
        outState.putSerializable("reviews", (Serializable) mReviewListAdapter.getCollection());

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * ProgressiveAdapter Handler for reviews.
     */
    private class ReviewListHandler extends Handler<MovieReview> {
        @Override
        public Context getContext() {
            return MovieReviewsActivity.this;
        }

        @Override
        public ViewHolder<MovieReview> createViewHolder(ViewGroup parent) {
            // Create mView for item
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View view = inflater.inflate(R.layout.item_movie_review, parent, false);

            return new MovieReviewViewHolder(view);
        }

        @Override
        public List<MovieReview> backgroundRequest(int page) {
            List<MovieReview> reviews = null;

            try {
                reviews = MovieReviewsFeed.fetchPage(mMovie.getId(), page + 1);

                // Show zero message
                if (page == 0 && reviews != null && reviews.size() == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mFallbackTextView.setVisibility(View.VISIBLE);
                            mRecyclerView.setVisibility(View.GONE);
                        }
                    });
                }
            } catch (JSONException e) {
                Log.e(TAG, "Unexpected error reading fetched chunk of reviews.", e);
            } catch (IOException e) {
                Log.e(TAG, "Failed to get a chunk of reviews from the Internet.", e);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (!NetworkUtils.isOnline(MovieReviewsActivity.this)) {
                            Toast.makeText(MovieReviewsActivity.this, R.string.msg_no_internet, Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                });
            }

            return reviews;
        }

        private class MovieReviewViewHolder extends ViewHolder<MovieReview> {
            private final TextView mAuthorTextView;
            private final TextView mContentTextView;

            MovieReviewViewHolder(View view) {
                super(view);

                mAuthorTextView = (TextView) view.findViewById(R.id.author);
                mContentTextView = (TextView) view.findViewById(R.id.content);
            }

            @Override
            public void bind(MovieReview review) {
                mAuthorTextView.setText(review.getAuthor());
                mContentTextView.setText(review.getContent());
            }

            @Override
            public void clear() {
                mAuthorTextView.setText("");
                mContentTextView.setText("");
            }
        }

    }
}
