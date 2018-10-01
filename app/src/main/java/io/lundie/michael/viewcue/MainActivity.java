package io.lundie.michael.viewcue;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.Nullable;
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
import io.lundie.michael.viewcue.datamodel.MovieRepository;
import io.lundie.michael.viewcue.datamodel.models.MovieItem;
import io.lundie.michael.viewcue.utilities.MovieResultsViewAdapter;
import io.lundie.michael.viewcue.utilities.QueryUtils;
import io.lundie.michael.viewcue.viewmodel.MoviesViewModel;

/**
 * Main / Root activity of ViewQueue
 */
public class MainActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String LOG_TAG = MainActivity.class.getName();

    private MovieResultsViewAdapter mAdapter;
    private ArrayList<MovieItem> mList = new ArrayList<>();
    private boolean hasInternet = true;

    MoviesViewModel model;

    private String orderPreference;

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

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        // Set up Recycler view
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setEmptyView(mEmptyStateTextView);

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

        MovieRepository.getInstance();
        model = ViewModelProviders.of(this).get(MoviesViewModel.class);

        model.getMovies(getSharedPreferences()).observe(this, new Observer<ArrayList<MovieItem>>() {
            @Override
            public void onChanged(@Nullable ArrayList<MovieItem> movieItems) {
                Log.i("TEST", "TEST Observer changed" +movieItems);
                mAdapter.setMovieEntries(movieItems);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    private String getSharedPreferences() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        return  sharedPrefs.getString(
                getString(R.string.settings_orderby_key),
                getString(R.string.settings_orderby_most_popular));
    }

    @Override
    public void onResume(){
        super.onResume();
        if (!hasInternet) {
            hasInternet = true;
            resetSearch();
            executeQuery(orderPreference);
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
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.action_sort_popular:
                mList.clear();
                mAdapter.notifyDataSetChanged();
                model.getMovies(getString(R.string.settings_orderby_most_popular));
                return true;
            case R.id.action_sort_rating:
                mList.clear();
                mAdapter.notifyDataSetChanged();
                model.getMovies(getString(R.string.settings_orderby_high_rated));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * A method containing the necessary logic for executing a reliable API query, checking
     * shared user preferences, if internet access is available and passing our query to the
     * loader manager.
     */
    private void executeQuery(String listOrder) {
        // Build our Query URL
        String queryURL = QueryUtils.queryUrlBuilder(listOrder);

        // Create a new loader from class.
        // Our instance will persist across configuration changes, as the loader logic will return
        // any current instances. Not sure if this is the best way to go about it.

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
        }
    }

    private void resetSearch() {
        // upon a new search initiation, destroy previous loader.

        //clear the array list
        mList.clear();
        //notify the adapter and scroll to position 0
        mAdapter.notifyDataSetChanged();
        mRecyclerView.scrollToPosition(0);
        // Show our progress ring.
        mProgressRing.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(LOG_TAG, "TEST: Prefs changed");
        if (key.equals(getString(R.string.settings_orderby_key))) {
            mList.clear();
            mAdapter.notifyDataSetChanged();
            model.getMovies(getSharedPreferences());
        }
    }
}