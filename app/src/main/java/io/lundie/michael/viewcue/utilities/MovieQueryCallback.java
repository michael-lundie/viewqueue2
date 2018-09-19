package io.lundie.michael.viewcue.utilities;

import android.content.Context;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import io.lundie.michael.viewcue.MovieItem;
import io.lundie.michael.viewcue.RecycleViewWithSetEmpty;

/**
 * A simple LoaderCallbacks interface for creating and managing the Movie Query Loader Manager.
 */
public class MovieQueryCallback implements LoaderManager.LoaderCallbacks<ArrayList<MovieItem>> {

    private static final String LOG_TAG = MovieQueryCallback.class.getSimpleName();

    private Context context;
    private ArrayList<MovieItem> list;
    private String connectURL;
    private ProgressBar progressRing;
    private TextView emptyStateTextView;
    private RecycleViewWithSetEmpty.Adapter adapter;
    private MoviesAsyncLoader mLoader;

    /**
     * Object constructor for this class.
     * @param context The current context.
     * @param connectURL the URL used to query the API
     * @param list The ArrayList we will populate.
     * @param adapter The RecyclerViewer Adapter to be used in conjunction with the loader.
     * @param bar id of our ProgressBar view
     * @param emptyStateView id of empty state text view in the custom RecycleView
     */
    public MovieQueryCallback(Context context, String connectURL, ArrayList<MovieItem> list,
                      RecycleViewWithSetEmpty.Adapter adapter, ProgressBar bar,
                      TextView emptyStateView) {
        this.context = context;
        this.connectURL = connectURL;
        this.list = list;
        this.adapter = adapter;
        this.progressRing = bar;
        this.emptyStateTextView = emptyStateView;
    }

    @NonNull
    @Override
    public Loader<ArrayList<MovieItem>> onCreateLoader(int id, Bundle args) {
        if (mLoader == null) {
            Log.i(LOG_TAG, "TEST: Loader is null");
            // It's the first time to request a the loader, lets create a new instance.
            return new MoviesAsyncLoader(context, connectURL);
        } else {
            // Let's prevent any NPE on configuration change. Return the current instance.
            // (We are using the same instance ID, so we don't want to cause problems here).
            return mLoader;
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<ArrayList<MovieItem>> loader, ArrayList<MovieItem> data) {
        //Loading is complete. Clear our local array list and notify the adapter of changes.
        list.clear();
        adapter.notifyDataSetChanged();
        //Load all of our fetched and parsed data into our local ArrayList. Notify adapter.
        list.addAll(data);
        adapter.notifyDataSetChanged();
        //Hide our UI progress spinner
        progressRing.setVisibility(View.GONE);
        //emptyStateTextView.setText(R.string.no_connection);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<ArrayList<MovieItem>> loader) {
        // Reset was called. Clear our local ArrayList and notify our recyclerview adapter of the
        // change.
        mLoader = null;
        list.clear();
        adapter.notifyDataSetChanged();
    }
}
