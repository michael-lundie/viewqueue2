package io.lundie.michael.viewcue.utilities;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;

import java.util.ArrayList;

import io.lundie.michael.viewcue.MovieItem;

public class MoviesAsyncLoader extends AsyncTaskLoader {

    private static final String LOG_TAG = MoviesAsyncLoader.class.getSimpleName();
    private ArrayList<MovieItem> apiQueryResults = null;
    private String queryUrl;

    /**
     * Async Loader constructor for 'Movies'
     * @param context context of current activity
     * @param url the api request URL
     */
    MoviesAsyncLoader(Context context, String url) {
        super(context);
        this.queryUrl = url;
    }

    @Override
    protected void onStartLoading() {
        // Let's first check for any cached results
        if (apiQueryResults != null) {
            // Let's use our cached data if it exists
            deliverResult(apiQueryResults);
        } else {
            // No cached data so let's get this party on the road!
            forceLoad();
        }
        super.onStartLoading();
    }

    @Nullable
    @Override
    public ArrayList<MovieItem> loadInBackground() {
        return null;
    }

}
