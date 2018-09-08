package io.lundie.michael.viewcue;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
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

public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = MainActivity.class.getName();

    /**
     * Create Loader Manager as static, to prevent NPE onDestroy
     * NOTE: I tried to find a better way to handle this - but so far unsuccessful.
     */
    private static LoaderManager.LoaderCallbacks<ArrayList<MovieItem>> movieQueryLoaderCallback;

    private RecycleViewWithSetEmpty.Adapter mAdapter;
    private ArrayList<MovieItem> mList = new ArrayList<>();
    private static final int API_REQUEST_LOADER_ID = 1;
    static boolean settingsChanged = false;

    /** A boolean value indicating whether or not this is the first time the app has been loaded. */
    static boolean firstLoad = true;

    @BindView(R.id.progressRing) ProgressBar mProgressRing;
    @BindView(R.id.list_empty) TextView mEmptyStateTextView;
    @BindView(R.id.movie_list) RecycleViewWithSetEmpty mRecyclerView;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);

        // Set up Recycler view
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setEmptyView(mEmptyStateTextView);

        // Initiate our new custom recycler adapter
        mAdapter = new MovieResultsViewAdapter(mList, this, 0);

        mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        mRecyclerView.setAdapter(mAdapter);

        executeSearch();

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
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivityForResult(settingsIntent, 1);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A method containing the necessary logic for executing a reliable API query, checking
     * shared user preferences, if internet access is available and passing our query to the
     * loader manager.
     */
    public void executeSearch() {
        mProgressRing.setVisibility(View.VISIBLE);
        // Build our Query URL
        String queryURL = QueryUtils.queryUrlBuilder(this);

        // Create a new loader from class.
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
            // Looks like we are good to go.
            mEmptyStateTextView.setVisibility(View.GONE);
            mProgressRing.setVisibility(View.GONE);
            // Let's get our loader manager hooked up and started
            getSupportLoaderManager().initLoader(API_REQUEST_LOADER_ID, null, movieQueryLoaderCallback);
        }
    }
}