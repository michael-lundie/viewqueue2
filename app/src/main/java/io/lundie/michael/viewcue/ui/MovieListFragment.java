/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 01/10/18 15:37
 */

package io.lundie.michael.viewcue.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
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

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.lundie.michael.viewcue.R;
import io.lundie.michael.viewcue.datamodel.database.MoviesDatabase;
import io.lundie.michael.viewcue.ui.views.RecycleViewWithSetEmpty;
import io.lundie.michael.viewcue.SettingsActivity;
import io.lundie.michael.viewcue.datamodel.models.MovieItem;
import io.lundie.michael.viewcue.utilities.MovieResultsViewAdapter;
import io.lundie.michael.viewcue.viewmodel.MoviesViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieListFragment extends Fragment {

    public static final String LOG_TAG = MovieListFragment.class.getName();

    private MovieResultsViewAdapter mAdapter;
    private ArrayList<MovieItem> mList = new ArrayList<>();

    private MoviesViewModel moviesViewModel;

    private MoviesDatabase moviesDatabase;

    @BindView(R.id.progressRing) ProgressBar mProgressRing;
    @BindView(R.id.list_empty) TextView mEmptyStateTextView;
    @BindView(R.id.movie_list) RecycleViewWithSetEmpty mRecyclerView;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    SharedPreferences.OnSharedPreferenceChangeListener listener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            Log.i(LOG_TAG, "TEST: Prefs changed");
            if (key.equals(getString(R.string.settings_orderby_key))) {
                mList.clear();
                mAdapter.notifyDataSetChanged();
                moviesViewModel.getMovies(getSharedPreferences());
            }
        }
    };

    public MovieListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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

        moviesDatabase = MoviesDatabase.getInstance(getActivity());

        // Set up Recycler view
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setEmptyView(mEmptyStateTextView);

        // Let's get our view model instance. Note we are returning the view model instance of
        // this fragments parent activity.
        moviesViewModel = ViewModelProviders.of(getActivity()).get(MoviesViewModel.class);

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


        moviesViewModel.getMovies(getSharedPreferences()).observe(this, new Observer<ArrayList<MovieItem>>() {
            @Override
            public void onChanged(@Nullable ArrayList<MovieItem> movieItems) {
                Log.i("TEST", "TEST Observer changed" +movieItems);
                mAdapter.setMovieEntries(movieItems);
                mAdapter.notifyDataSetChanged();
            }
        });

        return listFragmentView;
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
                saveItem(moviesViewModel.getSelectedItem());
                return true;
            case R.id.action_test:
                getFragmentManager().beginTransaction()
                        .replace(R.id.content_frame,
                                new DbTestFragment(), "DbTestFragment")
                        .addToBackStack(null)
                        .commit();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String getSharedPreferences() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return  sharedPrefs.getString(
                getString(R.string.settings_orderby_key),
                getString(R.string.settings_orderby_most_popular));
    }

    private void saveItem(LiveData<MovieItem> selectedItem) {
        MovieItem item = selectedItem.getValue().item();
        moviesDatabase.moviesDao().insertMovie(item);
        Log.i(LOG_TAG, "TEST: Movie Entered to Database");
    }
}
