package io.lundie.michael.viewcue;

import android.app.Activity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.lundie.michael.viewcue.utilities.MovieQueryCallback;
import io.lundie.michael.viewcue.utilities.MovieResultsViewAdapter;
import io.lundie.michael.viewcue.utilities.QueryUtils;

/**
 * Main / Root activity of ViewQueue
 */
public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = MainActivity.class.getName();

    /**
     * Create Loader Manager as static, to prevent NPE onDestroy
     */
    static LoaderManager.LoaderCallbacks<ArrayList<MovieItem>> movieQueryLoaderCallback;

    private RecycleViewWithSetEmpty.Adapter mAdapter;
    private ArrayList<MovieItem> mList = new ArrayList<>();
    private static final int API_REQUEST_LOADER_ID = 1;

    /** A boolean value indicating whether or setting shave been changed. */
    static boolean settingsChanged = false;

    @BindView(R.id.progressRing) ProgressBar mProgressRing;
    @BindView(R.id.list_empty) TextView mEmptyStateTextView;
    @BindView(R.id.movie_list) RecycleViewWithSetEmpty mRecyclerView;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind view references with butterknife library.
        ButterKnife.bind(this);

        // Set up our toolbar/action bar
        setSupportActionBar(mToolbar);

        // Set up Recycler view
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setEmptyView(mEmptyStateTextView);

        //Check for a saved instance to handle rotation and resume
        if(savedInstanceState != null)
        {
            mList = savedInstanceState.getParcelableArrayList("mList");
            if (mList != null ) {
                getSupportLoaderManager().initLoader(API_REQUEST_LOADER_ID, null,
                        movieQueryLoaderCallback);
                mProgressRing.setVisibility(View.INVISIBLE);
            } else {
                mList = new ArrayList<>();
            }
        }

        // Initiate our new custom recycler adapter and set layout manager.
        mAdapter = new MovieResultsViewAdapter(mList, this, 0);

        //Check for screen orientation
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == 1) {
            // If portrait mode set our grid layout to 3 columns
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));

        } else {
            // If landscape mode set our grid layout to 4 columns
            mRecyclerView.setLayoutManager(new GridLayoutManager(this, 4));

        } mRecyclerView.setAdapter(mAdapter);

        //Execute our API query
        if (mList.isEmpty()) {
            Log.i(LOG_TAG, "TEST: New QUERY IS EXECUTING.");
            executeQuery();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.action_settings) {
            openSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Allows seamless transition of our app between the various activity states.
     * @param outState Contains the listArray data (if any) used to populate our RecycleViewer
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //Saving parcelable code adapted from : https://stackoverflow.com/a/12503875/9738433
        if (!mList.isEmpty()){
            outState.putParcelableArrayList("mList", mList);
        }
        super.onSaveInstanceState(outState);
    }


    /**
     * Override method allowing the passing of intent data when resuming our MainActivity,
     * after accessing the SettingsActivity.
     * @param requestCode Where is the request coming from?
     * @param resultCode Was a result available?
     * @param data Intent data includes a boolean value, representing the change status of settings.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                Log.i(LOG_TAG, "TESTSET: onActivityResult triggered");
                settingsChanged = data.getBooleanExtra("settingsChanged", false);
                if (settingsChanged) {
                    // Reset our settingsChanged variable
                    Log.i(LOG_TAG, "TESTSET: settingsChange is TRUE. RESET.");
                    settingsChanged = false;
                    // reset search, destroying previous loader and cache
                    resetSearch();
                    // Begin our query using AsyncLoader
                    executeQuery();
                }
            }
        }
    }

    /**
     * A method containing the necessary logic for executing a reliable API query, checking
     * shared user preferences, if internet access is available and passing our query to the
     * loader manager.
     */
    public void executeQuery() {
        Log.i(LOG_TAG, "TESTSET: Executing new query.");

        // Let's access SharedPrefs from here, instead of QueryUtils, to keep code more logically
        // situated.
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        String orderValue = sharedPrefs.getString(
                getString(R.string.settings_orderby_key),
                getString(R.string.settings_orderby_most_popular));

        String apiKey = sharedPrefs.getString(
                getString(R.string.settings_themoviedb_apikey_key),
                getString(R.string.settings_themoviedb_api_default));

        if (apiKey.length() < 32) {
            openSettings();
            return;
        }

        // Build our Query URL
        String queryURL = QueryUtils.queryUrlBuilder(this, apiKey, orderValue);

        // Create a new loader from class.
        // Our instance will persist across configuration changes, as the loader logic will return
        // any current instances. Not sure if this is the best way to go about it.
        movieQueryLoaderCallback = new MovieQueryCallback(this, queryURL, mList, mAdapter,
                mProgressRing, mEmptyStateTextView);

        boolean isConnected = QueryUtils.checkNetworkAccess(this);
        Log.i(LOG_TAG, "TEST: Connection is " + isConnected);
        if (!isConnected) {
            // There is no internet connection. Let's deal with that.
            // We already checked for connection, but just in case the user resumed while the dialog
            // was open, perhaps a double check is good here.
            mProgressRing.setVisibility(View.GONE);
            mEmptyStateTextView.setText(getResources().getString(R.string.no_connection));
            mEmptyStateTextView.setVisibility(View.VISIBLE);
        } else {
            mProgressRing.setVisibility(View.VISIBLE);

            // Looks like we are good to go.
            mEmptyStateTextView.setVisibility(View.GONE);
            // Let's get our loader manager hooked up and started
            getSupportLoaderManager().initLoader(API_REQUEST_LOADER_ID, null, movieQueryLoaderCallback);
        }
    }

    private void resetSearch() {
        Log.i(LOG_TAG, "TESTSET: Resetting");
        // upon a new search initiation, destroy previous loader.
        getSupportLoaderManager().destroyLoader(API_REQUEST_LOADER_ID);
        //clear the array list
        mList.clear();
        //notify the adapter and scroll to position 0
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(0);
        // Show our progress ring.
        mProgressRing.setVisibility(View.VISIBLE);
    }

    private void openSettings() {
        Log.i(LOG_TAG, "TESTSET: Starting startActivityForResult");
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivityForResult(settingsIntent, 1);
    }
}