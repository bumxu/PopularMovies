package com.bumxu.android.popularmovies.app;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumxu.android.popularmovies.app.util.TimeUtils;
import com.bumxu.android.popularmovies.data.MovieVideo;

import java.util.List;


@SuppressWarnings("WeakerAccess")
public class MovieTrailersAdapter extends RecyclerView.Adapter<MovieTrailersAdapter.ViewHolder> {
    static final String YOUTUBE_IMAGE_URL = "https://img.youtube.com/vi/";
    static final String YOUTUBE_WATCH_URL = "http://www.youtube.com/watch?v=";

    private final Context mContext;
    private final List<MovieVideo> mTrailers;

    public MovieTrailersAdapter(Context context, @NonNull List<MovieVideo> trailers) {
        mContext = context;
        mTrailers = trailers;
    }

    @Override
    public MovieTrailersAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Create mView for item
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_movie_trailer, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieTrailersAdapter.ViewHolder vh, int position) {
        vh.clear();
        vh.bind(mTrailers.get(position));
    }

    @Override
    public int getItemCount() {
        return mTrailers.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private MovieVideo mTrailer;
        private final View mOverlay;
        private final ImageView mThumbnailImageView;
        private final TextView mDurationTextView;

        public ViewHolder(View view) {
            super(view);

            mOverlay = view.findViewById(R.id.overlay);
            mThumbnailImageView = (ImageView) view.findViewById(R.id.image_thumbnail);
            mDurationTextView = (TextView) view.findViewById(R.id.text_duration);

            mOverlay.setOnClickListener(this);
        }

        public void bind(MovieVideo trailer) {
            mTrailer = trailer;

            Glide.with(mContext)
                .load(YOUTUBE_IMAGE_URL + trailer.getKey() + "/0.jpg")
                .into(mThumbnailImageView);

            mDurationTextView.setText(TimeUtils.humanizeSeconds(trailer.getDuration()));
            mDurationTextView.setVisibility(View.VISIBLE);
        }

        public void clear() {
            mTrailer = null;

            mThumbnailImageView.setImageDrawable(null);
            mDurationTextView.setVisibility(View.INVISIBLE);
        }

        @Override
        public void onClick(View view) {
            if (mTrailer != null) {
                Intent webIntent = new Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(YOUTUBE_WATCH_URL + mTrailer.getKey()));

                mContext.startActivity(webIntent);
            }
        }
    }
}
