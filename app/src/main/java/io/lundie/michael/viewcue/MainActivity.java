package io.lundie.michael.viewcue;

import android.app.Activity;
import android.support.v4.app.LoaderManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
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
    private boolean hasInternet = true;

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
        mAdapter = new MovieResultsViewAdapter(mList, new MovieResultsViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieItem item) {
                // On click, create an intent and marshall necessary data using our parcelable
                // MovieItem object, and start our new activity.
                Intent openDetailIntent = new Intent(MainActivity.this, DetailActivity.class);
                openDetailIntent.putExtra("movie", item);
                startActivity(openDetailIntent);
            }
        });

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
            Log.i(LOG_TAG, "TEST - MLIST EMPTY");
            executeQuery(getOrderDefault());
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if (!hasInternet) {
            hasInternet = true;
            resetSearch();
            executeQuery(getOrderDefault());
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
        switch(id) {
            case R.id.action_settings:
                openSettings();
                return true;
            case R.id.action_sort_popular:
                resetSearch();
                executeQuery(getString(R.string.settings_orderby_most_popular));
                return true;
            case R.id.action_sort_rating:
                resetSearch();
                executeQuery(getString(R.string.settings_orderby_high_rated));
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
                settingsChanged = data.getBooleanExtra("settingsChanged", false);
                if (settingsChanged) {
                    // Reset our settingsChanged variable
                    settingsChanged = false;
                    // reset search, destroying previous loader and cache
                    resetSearch();
                    // Begin our query using AsyncLoader
                    executeQuery(getOrderDefault());
                }
            }
        }
    }

    /**
     * Simple utility method for checking the default order preference set by the user
     * @return the user order preference
     */
    private String getOrderDefault(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        return sharedPrefs.getString(
                getString(R.string.settings_orderby_key),
                getString(R.string.settings_orderby_most_popular));
    }

    /**
     * A method containing the necessary logic for executing a reliable API query, checking
     * shared user preferences, if internet access is available and passing our query to the
     * loader manager.
     */
    private void executeQuery(String queryOrder) {
        // Build our Query URL
        String queryURL = QueryUtils.queryUrlBuilder(this, queryOrder);

        // Create a new loader from class.
        // Our instance will persist across configuration changes, as the loader logic will return
        // any current instances. Not sure if this is the best way to go about it.
        movieQueryLoaderCallback = new MovieQueryCallback(this, queryURL, mList, mAdapter,
                mProgressRing, mEmptyStateTextView);

        boolean isConnected = QueryUtils.checkNetworkAccess(this);

        if (!isConnected) {
            // There is no internet connection. Let's deal with that.
            // We already checked for connection, but just in case the user resumed while the dialog
            // was open, perhaps a double check is good here.
            mProgressRing.setVisibility(View.GONE);
            mEmptyStateTextView.setText(getResources().getString(R.string.no_connection));
            mEmptyStateTextView.setVisibility(View.VISIBLE);
            hasInternet = false;
        } else {
            // Looks like we are good to go.
            mProgressRing.setVisibility(View.VISIBLE);
            mEmptyStateTextView.setVisibility(View.GONE);
            // Let's get our loader manager hooked up and started
            getSupportLoaderManager().initLoader(API_REQUEST_LOADER_ID, null, movieQueryLoaderCallback);
        }
    }

    private void resetSearch() {
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
        Intent settingsIntent = new Intent(this, SettingsActivity.class);
        startActivityForResult(settingsIntent, 1);
    }
}