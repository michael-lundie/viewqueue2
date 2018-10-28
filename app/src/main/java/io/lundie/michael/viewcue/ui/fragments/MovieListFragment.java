/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 07/10/18 21:18
 */

/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 01/10/18 15:37
 */

package io.lundie.michael.viewcue.ui.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.AndroidSupportInjection;
import io.lundie.michael.viewcue.R;
import io.lundie.michael.viewcue.ui.views.RecycleViewWithSetEmpty;
import io.lundie.michael.viewcue.ui.activities.SettingsActivity;
import io.lundie.michael.viewcue.datamodel.models.MovieItem;
import io.lundie.michael.viewcue.utilities.AppConstants;
import io.lundie.michael.viewcue.utilities.MovieResultsViewAdapter;
import io.lundie.michael.viewcue.utilities.DataAcquireStatus;
import io.lundie.michael.viewcue.utilities.Prefs;
import io.lundie.michael.viewcue.viewmodel.MoviesViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieListFragment extends Fragment {

    private static final String LOG_TAG = MovieListFragment.class.getName();

    // In a fragment, constructor injection is not possible. Let's use field injection.
    @Inject
    ViewModelProvider.Factory moviesViewModelFactory;

    @Inject
    Prefs prefs;

    // Declaring private variables.
    private MoviesViewModel moviesViewModel;
    private MovieResultsViewAdapter mAdapter;
    private DataAcquireStatus mDataAcquireStatus;
    private static String mRequestSortOrder;

    // Instantiate our ArrayList of MovieItems.
    private ArrayList<MovieItem> mList = new ArrayList<>();

    // Declaring annotated view variables for butterknife to bind references.
    @BindView(R.id.progressRing) ProgressBar mProgressRing;
    @BindView(R.id.list_empty) TextView mEmptyStateTextView;
    @BindView(R.id.movie_list) RecycleViewWithSetEmpty mRecyclerView;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    // Setting up our shared preference listener. If any preferences change, we'll know about it.
    SharedPreferences.OnSharedPreferenceChangeListener listener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
            Log.i(LOG_TAG, "TEST: Prefs changed");
            if (key.equals(getString(R.string.settings_orderby_key))) {
                mRequestSortOrder = prefs.getOrderPref();
                mList.clear();
                mAdapter.notifyDataSetChanged();
                moviesViewModel.getMovies(mRequestSortOrder, MoviesViewModel.REFRESH_DATA);
            }
        }
    };

    public MovieListFragment() {
        // Required empty public constructor for fragment classes.
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View listFragmentView =  inflater.inflate(R.layout.fragment_movie_list, container, false);

        // Time to butter some toast... Bind view references with butterknife library.
        ButterKnife.bind(this, listFragmentView);

        // Let's register our shared preferences change listener, set up earlier.
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(listener);

        // Set up our supportActionBar.
        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        setHasOptionsMenu(true);

        // Set up Recycler view
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setEmptyView(mEmptyStateTextView);

        if (savedInstanceState != null) {
            Log.i(LOG_TAG, "TEST: retrieving parcelable");
            mList = savedInstanceState.getParcelableArrayList("mList");
            if (mList == null ) {
                mList = new ArrayList<>();
            }
        }

        // Initiate our custom recycler adapter and set layout manager.
        mAdapter = new MovieResultsViewAdapter(mList, new MovieResultsViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieItem item) {
                Log.i(LOG_TAG, "TEST: Returning item: " + item);
                moviesViewModel.selectMovieItem(item);
                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame,
                                new MovieDetailFragment(), "MovieDetailFragment")
                        .addToBackStack(null)
                        .commit();
            }
        });

        //Check for screen orientation
        int orientation = getResources().getConfiguration().orientation;

        if (orientation == 1) {
            // If portrait mode set our grid layout to 3 columns
            mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        } else {
            // If landscape mode set our grid layout to 4 columns
            mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        } mRecyclerView.setAdapter(mAdapter);

        return listFragmentView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.i(LOG_TAG, "TEST: ON activity created - configuring dagger and viewMODEL");
        super.onActivityCreated(savedInstanceState);
        this.configureDagger();
        if(savedInstanceState == null) {
            Log.i(LOG_TAG, "TEST: saved instance state is null");
            this.configureViewModel();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (!mList.isEmpty()){
            Log.i(LOG_TAG, "TEST: Saving parcelable!!!!");
            outState.putParcelableArrayList("mList", mList);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .unregisterOnSharedPreferenceChangeListener(listener);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Simple switch statement for accessing items from our AppSupportBar.
        switch(id) {
            case R.id.action_settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            case R.id.action_sort_popular:
                mRequestSortOrder = AppConstants.SORT_ORDER_POPULAR;
                moviesViewModel.getMovies(mRequestSortOrder, MoviesViewModel.REFRESH_DATA);
                if (mDataAcquireStatus != DataAcquireStatus.FETCHING_FROM_DATABASE) {
                    mProgressRing.setVisibility(View.VISIBLE);
                }

                return true;
            case R.id.action_sort_rating:
                mRequestSortOrder = AppConstants.SORT_ORDER_HIGHRATED;
                moviesViewModel.getMovies(mRequestSortOrder, MoviesViewModel.REFRESH_DATA);
                if (mDataAcquireStatus != DataAcquireStatus.FETCHING_FROM_DATABASE) {
                    mProgressRing.setVisibility(View.VISIBLE);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A simple helper method to configure our view model.
     * Let's return two observables. One, which accesses our data. The other returns network status.
     */
    private void configureViewModel(){
        Log.i(LOG_TAG, "TEST: Configure view model called.");

        if (mRequestSortOrder == null) {
            mRequestSortOrder = prefs.getOrderPref();
        }
        // Get our view model provider.
        moviesViewModel = ViewModelProviders.of(getActivity(),
                moviesViewModelFactory).get(MoviesViewModel.class);

        // We want to remove any observers that current exist.
        // (This can be done with a singleton observe class and injection, but I haven't been
        // able to get this to work successfully yet.
        moviesViewModel.getDataAcquireStatus().removeObservers(this);
        moviesViewModel.getMovies(mRequestSortOrder,
                    MoviesViewModel.DO_NOT_REFRESH_DATA).removeObservers(this);

        // Fetch our network / data status observable first.
        // This will allow us some feedback so we can handle errors in our UI better.
        moviesViewModel.getDataAcquireStatus().observe(this, new Observer<DataAcquireStatus>() {
            @Override
            public void onChanged(@Nullable DataAcquireStatus dataAcquireStatus) {
                mDataAcquireStatus = dataAcquireStatus;
            }
        });

        // Secondly, fetch our data observables.
        moviesViewModel.getMovies(mRequestSortOrder,
                MoviesViewModel.REFRESH_DATA).observe(this, new Observer<ArrayList<MovieItem>>() {
            @Override
            public void onChanged(@Nullable ArrayList<MovieItem> movieItems) {
                if((movieItems != null) && (!movieItems.isEmpty())) {
                    Log.i(LOG_TAG, "TEST Observer changed");
                    mAdapter.setMovieEntries(movieItems);
                    Log.i(LOG_TAG, "TEST Set Adapter");
                    mProgressRing.setVisibility(View.INVISIBLE);
                    mAdapter.notifyDataSetChanged();
                    Log.i(LOG_TAG, "TEST Notify Data changed.");
                }
            }
        });
    }

    /**
     * A simple helper method for setting up dagger with this fragment.
     */
    private void configureDagger(){
        AndroidSupportInjection.inject(this);
    }
}