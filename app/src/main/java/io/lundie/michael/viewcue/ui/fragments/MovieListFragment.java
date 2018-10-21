/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 07/10/18 21:18
 */

/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 01/10/18 15:37
 */

package io.lundie.michael.viewcue.ui.fragments;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.widget.Toast;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.AndroidSupportInjection;
import io.lundie.michael.viewcue.R;
import io.lundie.michael.viewcue.datamodel.database.MoviesDatabase;
import io.lundie.michael.viewcue.ui.DbTestFragment;
import io.lundie.michael.viewcue.ui.views.RecycleViewWithSetEmpty;
import io.lundie.michael.viewcue.ui.activities.SettingsActivity;
import io.lundie.michael.viewcue.datamodel.models.MovieItem;
import io.lundie.michael.viewcue.utilities.AppExecutors;
import io.lundie.michael.viewcue.utilities.MovieResultsViewAdapter;
import io.lundie.michael.viewcue.utilities.Prefs;
import io.lundie.michael.viewcue.viewmodel.MoviesViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieListFragment extends Fragment {

    @Inject
    ViewModelProvider.Factory moviesViewModelFactory;

    @Inject
    Prefs prefs;

    private MovieResultsViewAdapter mAdapter;

    private MoviesViewModel moviesViewModel;

    public static final String LOG_TAG = MovieListFragment.class.getName();



    private ArrayList<MovieItem> mList = new ArrayList<>();

    private MoviesDatabase moviesDatabase;

    @BindView(R.id.progressRing) ProgressBar mProgressRing;
    @BindView(R.id.list_empty) TextView mEmptyStateTextView;
    @BindView(R.id.movie_list) RecycleViewWithSetEmpty mRecyclerView;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    SharedPreferences.OnSharedPreferenceChangeListener listener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
            Log.i(LOG_TAG, "TEST: Prefs changed");
            if (key.equals(getString(R.string.settings_orderby_key))) {
                mList.clear();
                mAdapter.notifyDataSetChanged();
                moviesViewModel.getMovies(prefs.getOrderPref());
            }
        }
    };

    public MovieListFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View listFragmentView =  inflater.inflate(R.layout.fragment_movie_list, container, false);
        // Bind view references with butterknife library.
        ButterKnife.bind(this, listFragmentView);

        PreferenceManager.getDefaultSharedPreferences(getActivity())
                .registerOnSharedPreferenceChangeListener(listener);

        ((AppCompatActivity)getActivity()).setSupportActionBar(mToolbar);
        setHasOptionsMenu(true);

        //moviesDatabase = MoviesDatabase.getInstance(getActivity());

        // Set up Recycler view
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setEmptyView(mEmptyStateTextView);

        //TODO: Delete...
        // Let's get our view model instance. Note we are returning the view model instance of
        // this fragments parent activity.
        // moviesViewModel = ViewModelProviders.of(getActivity()).get(MoviesViewModel.class);

        // Initiate our new custom recycler adapter and set layout manager.
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
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.configureDagger();
        this.configureViewModel();
    }

    //TODO: Implement savedInstanceState?

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
        switch(id) {
            case R.id.action_settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            case R.id.action_sort_popular:
                //mList.clear();
                //mAdapter.notifyDataSetChanged();
                moviesViewModel.getMovies(getString(R.string.settings_orderby_most_popular));
                return true;
            case R.id.action_sort_rating:
                //TODO: We need a data network listener interface here somehow
                //mList.clear();
                //mAdapter.notifyDataSetChanged();
                moviesViewModel.getMovies(getString(R.string.settings_orderby_high_rated));
                return true;
            case R.id.action_save:
                //saveItem(moviesViewModel.getSelectedItem());
                prefs.updateDbRefreshTime(2222222);
                return true;
            case R.id.action_test:
                /*getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame,
                                new DbTestFragment(), "DbTestFragment")
                        .addToBackStack(null)
                        .commit();*/

                Toast.makeText(getContext(), Long.toString(prefs.getRefreshTime()), Toast.LENGTH_SHORT).show();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void saveItem(LiveData<MovieItem> selectedItem) {
        final MovieItem item = selectedItem.getValue().item();
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                moviesDatabase.moviesDao().insertMovie(item);
            }
        });
        Log.i(LOG_TAG, "TEST: Movie Entered to Database");
    }

    private void configureDagger(){
        AndroidSupportInjection.inject(this);
    }

    private void configureViewModel(){
        moviesViewModel = ViewModelProviders.of(getActivity(), moviesViewModelFactory).get(MoviesViewModel.class);
        moviesViewModel.getMovies(prefs.getOrderPref()).observe(this, new Observer<ArrayList<MovieItem>>() {
            @Override
            public void onChanged(@Nullable ArrayList<MovieItem> movieItems) {
                if((movieItems != null) && (!movieItems.isEmpty())) {
                    Log.i(LOG_TAG, "TEST Observer changed" +movieItems);
                    mAdapter.setMovieEntries(movieItems);
                    Log.i(LOG_TAG, "TEST Set Adapter to:" +movieItems);
                    mAdapter.notifyDataSetChanged();
                    Log.i(LOG_TAG, "TEST Notify Data changed.");
                }
            }
        });
    }
}