package io.lundie.michael.viewcue.utilities;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.content.AsyncTaskLoader;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
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
        Log.i(LOG_TAG, "TEST: Movies Async Loader: loadInBackground executed");
        //Let's check to make sure our URL isn't empty for some reason.
        //We should never have spaces before or after our URL here. Not using trim()
        if (!TextUtils.isEmpty(queryUrl)) {
            try {
                // Everything is a-okay. Continue to fetch results.
                ArrayList<MovieItem> resultItems = QueryUtils.fetchQueryResults(queryUrl);
                if (resultItems != null) {
                    // Fetch results are not null. Assign to our return variable.
                    apiQueryResults = resultItems;
                } else {
                    throw new IOException("No response received.");
                }
            } catch(Exception e) {
                Log.e("Log error", "Problem with Requested URL", e);
            }
        }
        return apiQueryResults;
    }
}