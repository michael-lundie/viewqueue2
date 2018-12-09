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
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.AndroidSupportInjection;
import io.lundie.michael.viewcue.R;
import io.lundie.michael.viewcue.ui.activities.SettingsActivity;
import io.lundie.michael.viewcue.datamodel.models.item.MovieItem;
import io.lundie.michael.viewcue.utilities.AppConstants;
import io.lundie.michael.viewcue.ui.adapters.MovieResultsViewAdapter;
import io.lundie.michael.viewcue.utilities.AppUtils;
import io.lundie.michael.viewcue.utilities.DataStatus;
import io.lundie.michael.viewcue.utilities.Prefs;
import io.lundie.michael.viewcue.viewmodel.MoviesViewModel;

/**
 * Fragment responsible for the presentation of, and UI interaction with the movie
 * list view.
 */
public class MovieListFragment extends Fragment implements View.OnClickListener{

    private static final String LOG_TAG = MovieListFragment.class.getName();

    // Setting up some static variables
    private static boolean IS_LANDSCAPE_TABLET;
    private static boolean IS_TABLET;

    // In a fragment, constructor injection is not possible. Let's use field injection.
    @Inject
    ViewModelProvider.Factory moviesViewModelFactory;

    @Inject
    Prefs prefs;

    @Inject
    AppConstants constants;

    @Inject
    AppUtils appUtils;

    // Declaring private variables.
    private MoviesViewModel moviesViewModel;
    private MovieResultsViewAdapter mAdapter;
    private static String mRequestSortOrder = null;
    private int detailContentFrameID;

    // Instantiate our ArrayList of MovieItems.
    private ArrayList<MovieItem> mList = new ArrayList<>();

    // Declaring annotated view variables for butterknife to bind references.
    @BindView(R.id.progressRing) ProgressBar mProgressRing;
    @BindView(R.id.empty_icon_iv) ImageView mErrorIcon;
    @BindView(R.id.empty_message_tv) TextView mEmptyTv;
    @BindView(R.id.movie_list) RecyclerView mRecyclerView;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    // Bind textView buttons
    @BindView(R.id.popular_btn) TextView mPopularBtn;
    @BindView(R.id.high_rated_btn) TextView mHighRatedBtn;
    @BindView(R.id.favourites_btn) TextView mFavouritesBtn;

    // Setting up our shared preference listener. If any preferences change, we'll know about it.
    SharedPreferences.OnSharedPreferenceChangeListener listener
            = new SharedPreferences.OnSharedPreferenceChangeListener() {

        public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
            if (key.equals(getString(R.string.settings_orderby_key))) {

                mRequestSortOrder = prefs.getOrderPref();
                mList.clear();
                mAdapter.notifyDataSetChanged();
                moviesViewModel.getMovies(mRequestSortOrder, MoviesViewModel.REFRESH_DATA);
            }
        }
    };

    public MovieListFragment() { /* Required empty public constructor for fragment classes. */ }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Check what kind of device we are viewing on
        IS_LANDSCAPE_TABLET = getResources().getBoolean(R.bool.isLandscapeTablet);
        IS_TABLET = getResources().getBoolean(R.bool.isTablet);

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

        // Set the padding of our recycler view to compensate for the buttons list.
        mRecyclerView.setPadding(0,
                mToolbar.getLayoutParams().height +
                        (int) getActivity().getResources().getDimension(R.dimen.button_layout_height),
                0,
                0);

        if (savedInstanceState != null) {
            // Get parcelable movies list so we can populate the UI quickly while observers are
            // being refreshed
            mList = savedInstanceState.getParcelableArrayList("mList");
            if (mList == null ) {
                mList = new ArrayList<>();
            }
        }

        // Initiate our custom recycler adapter and set layout manager.
        mAdapter = new MovieResultsViewAdapter(mList, new MovieResultsViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(MovieItem item) {
                // Let's set up our detail fragment
                // Note: This was previously done by updating an observable, and only
                // creating the details fragment when required. However, due
                // to inconsistency in content, the view is required to be remeasured.
                // Replacing the fragment is so far the only way I could figure out how to do this
                // without any 'flicker;

                moviesViewModel.selectMovieItem(item);

                MovieDetailFragment detailFragment = new MovieDetailFragment();
                Bundle toDetailFragment = new Bundle();

                toDetailFragment.putString("sortOrder", mRequestSortOrder);
                detailFragment.setArguments(toDetailFragment);

                // Let's set/reset the content frame details.
                setDetailContentFrameID();
                Fragment currentDetailFragment = getFragmentManager().findFragmentByTag(AppConstants.FRAGTAG_DETAIL);
                if(currentDetailFragment ==null) {
                    getFragmentManager().beginTransaction()
                            .add(detailContentFrameID, detailFragment, AppConstants.FRAGTAG_DETAIL)
                            //.addToBackStack(null)
                            .commit();
                } else {
                    getFragmentManager()
                            .beginTransaction()
                            .remove(currentDetailFragment)
                            .add(detailContentFrameID, detailFragment, AppConstants.FRAGTAG_DETAIL)
                            .commit();
                }
            }
        });

        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), getListSpanCount()));
        mRecyclerView.setAdapter(mAdapter);

        return listFragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Configure dagger, once our activity has been created (we are relying on dispatch injector
        // in activity so we cannot configure our view model until dagger is set up.
        this.configureDagger();
        if(savedInstanceState == null) {
            prefs.setShowOfflineNotice(true);
        }

        if(moviesViewModel == null) {
            // Create view model if reference is null
            this.configureViewModel();
        }

        // Set up click listeners.
        mPopularBtn.setOnClickListener(this);
        mHighRatedBtn.setOnClickListener(this);
        mFavouritesBtn.setOnClickListener(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (!mList.isEmpty()){
            outState.putParcelableArrayList("mList", mList);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if(moviesViewModel == null || prefs.attemptRefreshOnResume()) {
            configureViewModel();
            prefs.setAttemptRefreshOnResume(false);
        } else {
            // We'll just double check here to see if the user has gone offline.
            isUserBrowsingOFfline();
        }
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
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.popular_btn:
                mRequestSortOrder = AppConstants.SORT_ORDER_POPULAR;
                setSelectedButton();
                moviesViewModel.getMovies(mRequestSortOrder, MoviesViewModel.REFRESH_DATA);

                break;
            case R.id.high_rated_btn:
                mRequestSortOrder = AppConstants.SORT_ORDER_HIGHRATED;
                setSelectedButton();
                moviesViewModel.getMovies(mRequestSortOrder, MoviesViewModel.REFRESH_DATA);

                break;
            case R.id.favourites_btn:
                mRequestSortOrder = AppConstants.SORT_ORDER_FAVS;
                if (prefs.getFavoritesCount() > 0) {
                    setSelectedButton();
                    moviesViewModel.getMovies(mRequestSortOrder, MoviesViewModel.REFRESH_FROM_DATABASE);
                } else {
                    Snackbar.make(getView(), R.string.snack_no_favs, Snackbar.LENGTH_LONG)
                            .setAction(R.string.snack_dismiss_polite, new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Required override method
                                }
                            })
                    .show();
                }
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // Simple switch statement for accessing items from our AppSupportBar.
        switch(id) {
            case R.id.action_settings:
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A simple helper method to configure our view model.
     * Let's return two observables. One, which accesses our data. The other returns network status.
     */
    private void configureViewModel(){

        if (mRequestSortOrder == null) {
            mRequestSortOrder = prefs.getOrderPref();
        }

        setSelectedButton();

        // Get our view model provider.
        moviesViewModel = ViewModelProviders.of(getActivity(),
                moviesViewModelFactory).get(MoviesViewModel.class);

        // We want to remove any observers that currently exist.
        // (This can be done with a singleton observe class and injection, but I haven't been
        // able to get this to work successfully yet.

        moviesViewModel.getListDataAcquireStatus().removeObservers(this);

        moviesViewModel.getMovies(mRequestSortOrder,
                MoviesViewModel.DO_NOT_REFRESH_DATA).removeObservers(this);

        // Fetch our network / data status observable first.
        // This will allow us some feedback so we can handle errors in our UI better.
        moviesViewModel.getListDataAcquireStatus().observe(this, new Observer<DataStatus>() {
            @Override
            public void onChanged(@Nullable DataStatus dataStatus) {
                // We can make use of our error/status reporting  from our view model / repo
                // here by using a switch case.
                if(dataStatus != null) {

                    Log.v(LOG_TAG, dataStatus.toString());

                    processDataStatus(dataStatus);
                }
            }
        });

        // Secondly, fetch our data observables.
        moviesViewModel.getMovies(mRequestSortOrder,
                MoviesViewModel.REFRESH_DATA).observe(this, new Observer<ArrayList<MovieItem>>() {
            @Override
            public void onChanged(@Nullable ArrayList<MovieItem> movieItems) {
                if((movieItems != null) && (!movieItems.isEmpty())) {

                    if(mRequestSortOrder.equals(AppConstants.SORT_ORDER_FAVS) &&
                            prefs.getFavoritesCount() == 0) {

                        mRequestSortOrder = AppConstants.SORT_ORDER_POPULAR;
                        moviesViewModel.getMovies(mRequestSortOrder, MoviesViewModel.REFRESH_DATA);
                    }

                        mAdapter.setMovieEntries(movieItems);
                        mProgressRing.setVisibility(View.INVISIBLE);
                        mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    private void processDataStatus(DataStatus status) {
        switch (status) {
            case ATTEMPTING_API_FETCH:
            case FETCHING_FROM_DATABASE:
                mProgressRing.setVisibility(View.VISIBLE);
                break;
            case FETCH_COMPLETE:

                mProgressRing.setVisibility(View.INVISIBLE);
                showErrorViews(false);
                if(mList != null) {
                    isUserBrowsingOFfline();
                }
                break;
            case DATABASE_EMPTY:
                if(mRequestSortOrder.equals(AppConstants.SORT_ORDER_FAVS)) {
                    mRequestSortOrder = AppConstants.SORT_ORDER_POPULAR;
                    setSelectedButton();
                    moviesViewModel.getMovies(mRequestSortOrder, MoviesViewModel.REFRESH_DATA);
                    break;
                }
            case ERROR_NETWORK_FAILURE:
                mProgressRing.setVisibility(View.INVISIBLE);
                mEmptyTv.setText(getText(R.string.unavailable_offline));
                showErrorViews(true);
                prefs.setAttemptRefreshOnResume(true);
                break;
            case ERROR_PARSING:
            case ERROR_SERVER_BROKEN:
            case ERROR_UNKNOWN:
            case ERROR_NOT_FOUND:
                mErrorIcon.setVisibility(View.VISIBLE);
                mProgressRing.setVisibility(View.INVISIBLE);
                mEmptyTv.setText(R.string.error_unknown);
                showErrorViews(true);
                break;
        }
    }

    private void showErrorViews(Boolean setVisibility) {
        if(setVisibility) {
            mErrorIcon.setVisibility(View.VISIBLE);
            mEmptyTv.setVisibility(View.VISIBLE);
        } else {
            mErrorIcon.setVisibility(View.INVISIBLE);
            mEmptyTv.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * A simple helper method for setting up dagger with this fragment.
     */
    private void configureDagger(){
        AndroidSupportInjection.inject(this);
    }

    private void setSelectedButton() {
        if (mRequestSortOrder.equals(AppConstants.SORT_ORDER_POPULAR)) {
            mPopularBtn.setTextColor(getResources().getColor(R.color.colorAccent));
            mHighRatedBtn.setTextColor(getResources().getColor(R.color.colorPrimaryLight));
            mFavouritesBtn.setTextColor(getResources().getColor(R.color.colorPrimaryLight));
        } else if (mRequestSortOrder.equals(AppConstants.SORT_ORDER_HIGHRATED)) {
            mPopularBtn.setTextColor(getResources().getColor(R.color.colorPrimaryLight));
            mHighRatedBtn.setTextColor(getResources().getColor(R.color.colorAccent));
            mFavouritesBtn.setTextColor(getResources().getColor(R.color.colorPrimaryLight));
        } else {
            mPopularBtn.setTextColor(getResources().getColor(R.color.colorPrimaryLight));
            mHighRatedBtn.setTextColor(getResources().getColor(R.color.colorPrimaryLight));
            mFavouritesBtn.setTextColor(getResources().getColor(R.color.colorAccent));
        }
    }

    private void setDetailContentFrameID() {
        if(IS_LANDSCAPE_TABLET) {
            detailContentFrameID = R.id.detail_frame;
        } else {
            detailContentFrameID = R.id.content_frame;
        }
    }

    private int getListSpanCount() {
        if (getResources().getConfiguration().orientation == 2 || IS_TABLET) {
            return 4;
        } else {
            return 3;
        }
    }

    public void isUserBrowsingOFfline() {
        if (!appUtils.hasNetworkAccess() && getView() != null && prefs.getShowOfflineNotice()) {
            prefs.setShowOfflineNotice(false);
            Snackbar.make(getView(), R.string.snack_offline_browsing, Snackbar.LENGTH_LONG)
                    .setAction(R.string.snack_dismiss_polite, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Required override method
                        }
                    })
                    .show();
        }
    }
}