package com.bumxu.android.lab;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ProgressiveAdapter is a RecyclerView->Adapter implementation that handles in background the
 * progressive load of foreign data (Internet or provider) for a RecyclerView, its display,
 * caching, concurrency, ...
 *
 * @param <T> Data class handled by adapter.
 */
public class ProgressiveAdapter<T> extends RecyclerView.Adapter<ProgressiveAdapter.ViewHolder> {
    private static final String TAG = ProgressiveAdapter.class.getSimpleName();

    // Concurrent data fetch calls
    private final int mConcurrency = 2;

    // Handler
    private final Handler<T> mHandler;

    // Pending page numbers
    private final LinkedList<Integer> mPedingTasks;
    // Currently running tasks in pairs [Page number, AsynkTask]
    private final Map<Integer, BackgroundTask> mRunningTasks;

    // TOREV
    private final Set<Integer> mLoadedPages;

    // Number of items to be loaded every time
    private final int mChunkSize;
    // Total number of items
    private int mItemCount;
    // Cached items list
    private final Map<Integer, T> mItemCache;

    /**
     * Instance a ProgressiveAdapter.
     *
     * @param chunkSize Number of items to be loaded every time.
     * @param handler   Foreign handler of the adapter.
     */
    @SuppressLint("UseSparseArrays")
    public ProgressiveAdapter(final int chunkSize, final Handler<T> handler) {
        mHandler = handler;

        mPedingTasks = new LinkedList<>();
        mRunningTasks = new HashMap<>();
        mLoadedPages = new HashSet<>();

        mItemCount = chunkSize - 1;

        mItemCache = new HashMap<>();
        mChunkSize = chunkSize;
    }

    /**
     * Applies a new item count to the adapter.
     *
     * @param itemCount The new total items count.
     */
    @UiThread
    public void updateItemCount(int itemCount) {
        if (mItemCount != itemCount) {
            // Update
            mItemCount = itemCount;
            // Refresh list
            notifyDataSetChanged();
        }
    }

    /**
     * Cancel all running tasks and release threads referenced by this ProgressiveAdapter instance.
     * This should be called when destroying activity.
     */
    public void destroy() {
        for (BackgroundTask task : mRunningTasks.values()) {
            task.cancel(true);
        }
    }

    // · // · // · // · // · //

    /**
     * Enqueue new task for the specified chunk.
     *
     * @param chunk The chunk or page of data to be recovered by the new tag.
     */
    private void addTask(int chunk) {
        if (!mPedingTasks.contains(chunk) && !mRunningTasks.containsKey(chunk) /*&& !mLoadedPages.contains(chunk)*/) {
            mPedingTasks.addFirst(chunk);
            // Start processing if is not
            process();
            // Clean accumulated tasks
            cleanPendingTasks();
        }
    }

    private void cleanPendingTasks() {
        while (mPedingTasks.size() > mConcurrency) {
            mPedingTasks.removeLast();
        }
    }

    private void taskCompleted(int chunk, List<T> items) {
        Log.d(TAG, "Loaded page " + chunk + ".");

        if (items != null) {
            // Save movies
            for (int i = 0; i < items.size(); i++) {
                mItemCache.put(chunk * 20 + i, items.get(i));
            }
            // Invalidate recyclerview range
            notifyItemRangeChanged(chunk * 20, 20);

            mLoadedPages.add(chunk);

            if (items.size() < mChunkSize) {
                updateItemCount(chunk * mChunkSize + items.size());
            } else if (items.size() == 0) {
                updateItemCount(chunk * mChunkSize);
            } else if (items.size() == mChunkSize) {
                if (mItemCount <= (chunk + 1) * mChunkSize) {
                    updateItemCount((chunk + 1) * mChunkSize + (mChunkSize - 1));
                }
            }
        }

        // Update item count
        /*if (update.totalCount > -1 && update.totalCount != mItemCount) {
            updateItemCount(update.totalCount);
        }*/

        // Unlock task slot
        mRunningTasks.remove(chunk);
        // Run other tasks
        process();
    }

    private void process() {
        while (mRunningTasks.size() <= mConcurrency && mPedingTasks.size() > 0) {
            Integer page = mPedingTasks.removeFirst();
            BackgroundTask task = new BackgroundTask(page);

            mRunningTasks.put(page, task);
            task.execute();
        }
    }

    /**
     * Returns the current list of cached items.
     * Look out! This is the original, not a clone.
     *
     * @return Reference to the current list of cached items.
     */
    public Map<Integer, T> getCollection() {
        return mItemCache;
    }

    /**
     * Restores the list of cached items with the given one, useful for savedInstanceState recovery.
     *
     * @param from Structure to replace current cached item list.
     */
    public void restoreCollection(Map<Integer, T> from) {
        // Discard previous
        mItemCache.clear();

        int itemCount = 0;

        // Add one by one, updating the item count properly
        for (Map.Entry<Integer, T> entry : from.entrySet()) {
            int position = entry.getKey();
            T item = entry.getValue();

            mItemCache.put(position, item);

            itemCount = Math.max(itemCount, position);
        }

        // Update itemCount
        updateItemCount(itemCount + mChunkSize);
    }

    // · // · // · // · // · //

    @Override
    public ViewHolder<T> onCreateViewHolder(ViewGroup parent, int viewType) {
        // Delegate to handler
        return mHandler.createViewHolder(parent);
    }

    @Override
    public void onBindViewHolder(ViewHolder vh, int position) {
        // Clean the viewholder
        vh.clear();

        // Try to get the element for required position
        T item = mItemCache.get(position);
        if (item != null) {
            // Bind the new item
            //noinspection unchecked
            vh.bind(item);
            return;
        }

        // If element isn't in cacahe, try to queue request for that chunk
        int chunk = position / mChunkSize;

        addTask(chunk);
    }

    /**
     * Returns the current total number of items.
     *
     * @return The current total number of items.
     */
    @Override
    public int getItemCount() {
        return mItemCount;
    }

    // · // · // · // · // · //

    /**
     * The background class to accomplish for each adapter chunk.
     */
    private class BackgroundTask extends AsyncTask<Void, Void, List<T>> {
        private final int mChunk;

        BackgroundTask(final int chunk) {
            mChunk = chunk;
        }

        @Override
        protected List<T> doInBackground(Void... voids) {
            // Don't start request if cancelled
            if (isCancelled()) return null;

            // Return request result
            return mHandler.backgroundRequest(mChunk);
        }

        @Override
        protected void onPostExecute(final List<T> update) {
            // Don't process if cancelled
            if (isCancelled()) return;

            // Complete the task
            taskCompleted(mChunk, update);
        }
    }

    /**
     * The ViewHolder base class to implement to fill the item's views.
     */
    public abstract static class ViewHolder<T> extends RecyclerView.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }

        public abstract void bind(T item);

        public abstract void clear();
    }

    /**
     * The Handler base class to implement to handle the adapter.
     */
    public abstract static class Handler<T> {
        public abstract Context getContext();

        public abstract ViewHolder<T> createViewHolder(ViewGroup parent);

        public List<T> backgroundRequest(int page) {
            return null;
        }
    }
}
