/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 07/10/18 20:11
 */

/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 03/10/18 21:44
 */

package io.lundie.michael.viewcue.ui.fragments;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.AndroidSupportInjection;
import io.lundie.michael.viewcue.datamodel.database.MoviesDao;
import io.lundie.michael.viewcue.datamodel.models.review.MovieReviewItem;
import io.lundie.michael.viewcue.datamodel.models.videos.RelatedVideos;
import io.lundie.michael.viewcue.ui.adapters.MovieReviewsViewAdapter;
import io.lundie.michael.viewcue.ui.adapters.RelatedVideosViewAdapter;
import io.lundie.michael.viewcue.ui.views.PercentageCropImageView;
import io.lundie.michael.viewcue.R;
import io.lundie.michael.viewcue.ui.helpers.SolidScrollShrinker;
import io.lundie.michael.viewcue.datamodel.models.item.MovieItem;
import io.lundie.michael.viewcue.ui.views.RecycleViewWithSetEmpty;
import io.lundie.michael.viewcue.utilities.AppExecutors;
import io.lundie.michael.viewcue.utilities.AppUtils;
import io.lundie.michael.viewcue.utilities.CallbackRunnable;
import io.lundie.michael.viewcue.utilities.DataStatus;
import io.lundie.michael.viewcue.utilities.Prefs;
import io.lundie.michael.viewcue.utilities.RunnableInterface;
import io.lundie.michael.viewcue.viewmodel.MoviesViewModel;

/**
 * Fragment object responsible for creating the UI for our movie detail view
 */
public class MovieDetailFragment extends Fragment {

    private static final String LOG_TAG = MovieDetailFragment.class.getName();

    // Setting up some static variables
    private static boolean IS_LANDSCAPE_TABLET;

    // Method injection with dagger 2
    @Inject
    ViewModelProvider.Factory moviesViewModelFactory;

    @Inject
    AppUtils appUtils;

    @Inject
    MoviesDao moviesDao;

    @Inject
    Prefs prefs;

    // Set-up required reference variables
    private MoviesViewModel moviesViewModel;
    private String mRequestSortOrder;
    private boolean addFavorite;

    // Coordinator layout and tool/appbar view references.
    @BindView(R.id.main_layout) ConstraintLayout mRootDetailLayout;
    @BindView(R.id.appbar) AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbar;
    @Nullable @BindView(R.id.toolbar_detail) Toolbar mToolbarDetail;

    // Image/UI misc display view references.
    @BindView(R.id.title_background) View titleBackgroundView;
    @BindView(R.id.detail_view_poster) ImageView mPosterView;
    @BindView(R.id.backdrop_iv) PercentageCropImageView backdrop;
    @BindView(R.id.progressbar) ProgressBar progressBar;
    @BindView(R.id.fab_add) FloatingActionButton favButton;

    // References to views displaying text.
    @BindView(R.id.title) TextView title;
    @BindView(R.id.released_text_tv) TextView releasedDateTv;
    @BindView(R.id.vote_average_text_tv) TextView voteAverageTv;
    @BindView(R.id.synopsis_tv) TextView synopsisTv;

    @BindView(R.id.review_list_lv) RecycleViewWithSetEmpty reviewLv;
    @BindView(R.id.related_video_lv) RecycleViewWithSetEmpty relatedVideoLv;

    private MovieItem mMovieItem;
    private ArrayList<MovieReviewItem> mReviewItems;
    private ArrayList<RelatedVideos> mRelatedVideoItems;

    private MovieReviewsViewAdapter reviewsAdapter;
    private RelatedVideosViewAdapter relatedVideosAdapter;

    @BindView(R.id.review_empty_tv) TextView emptyReviewTv;
    @BindView(R.id.videos_empty_tv) TextView emptyVideosTv;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Check what kind of device we are viewing on
        IS_LANDSCAPE_TABLET = getResources().getBoolean(R.bool.isLandscapeTablet);

        // Get bundle data.
        if(getArguments() != null) {
         mRequestSortOrder = getArguments().getString("sortOrder");
        }

        // Inflate the layout for this fragment
        View detailFragmentView =  inflater.inflate(R.layout.fragment_movie_detail, container, false);

        // Bind view references with butterknife library.
        ButterKnife.bind(this, detailFragmentView);

        if(!IS_LANDSCAPE_TABLET) {
            // Set-up toolbar.
            ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbarDetail);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            setHasOptionsMenu(true);
        }

        //Make the title invisible - we have our own TextView for that. Maybe there is a better
        // way to do this?
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(R.color.colorTransparent));
        collapsingToolbar.setCollapsedTitleTextColor(getResources().getColor(R.color.colorTransparent));

        // Set Y center offset crop (uses 'PercentCropImageView')
        backdrop.setCropYCenterOffsetPct(0f);

        // Let's get our view model instance. Note we are returning the view model instance of
        // this fragments parent activity.
        moviesViewModel = ViewModelProviders.of(getActivity()).get(MoviesViewModel.class);

        if(savedInstanceState != null) {
            mMovieItem = savedInstanceState.getParcelable("mMovieItem");
            if (mMovieItem != null) {
                // We could resume our fragment from the saved movie item state,
                // however we want to 'refetch' data from our view model.
                // Re-selecting the item through the viewmodel method allows us to do this.
                moviesViewModel.selectMovieItem(mMovieItem);
            } else {
                // else - the app will crash
                Log.e(LOG_TAG, "The movie item could not be recovered from saved instance.");
            }
        }

        setUpRelatedVideosList();
        setUpReviewList();

        // Set up our 'fake parallax' transition.
        // Solution for scaling transition from 'bottom': https://stackoverflow.com/a/22144862
        // Get height after layout is drawn solution: https://stackoverflow.com/a/24035591
        titleBackgroundView.post(new Runnable() {
            @Override
            public void run() {
                titleBackgroundView.getHeight(); //height is ready
                titleBackgroundView.setPivotY(titleBackgroundView.getHeight());
            }
        });

        appBarLayout.addOnOffsetChangedListener(new SolidScrollShrinker(titleBackgroundView));

        return detailFragmentView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.configureDagger();
        this.configureViewModel();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        if (mMovieItem != null){
            Log.i(LOG_TAG, "TEST: Saving parcelable!!!!");
            outState.putParcelable("mMovieItem", mMovieItem);
        }

        super.onSaveInstanceState(outState);
    }

    private void configureDagger(){ AndroidSupportInjection.inject(this); }

    /**
     * Configure and set up our view model. This method is responsible for registering
     * and unregistering observers.
     */
    private void configureViewModel(){

        // Get our view model provider using factory method.
        moviesViewModel = ViewModelProviders.of(getActivity(), moviesViewModelFactory).get(MoviesViewModel.class);

        // Remove any observers to prevent duplication, we can be relatively sure if one observer
        // exists, so do the others.
        if(moviesViewModel.getSelectedItem().hasObservers()) {
            moviesViewModel.getSelectedItem().removeObservers(this);
            moviesViewModel.getDetailDataAcquireStatus().removeObservers(this);
            moviesViewModel.getReviewItems().removeObservers(this);
            moviesViewModel.getRelatedVideoItems().removeObservers(this);
        }

        // Set up selected item observer.
        moviesViewModel.getSelectedItem().observe(this, new Observer<MovieItem>() {
            @Override
            public void onChanged(@Nullable MovieItem movieItem) {
                mMovieItem = movieItem;
                setUpDetailView();
            }
        });

        // Set up detail data status observer
        moviesViewModel.getDetailDataAcquireStatus().observe(this, new Observer<DataStatus>() {
            @Override
            public void onChanged(@Nullable DataStatus dataStatus) {
                if(dataStatus != null) {
                    switch (dataStatus) {
                        case ERROR_UNAVAILABLE_OFFLINE:
                            emptyReviewTv.setText(getText(R.string.unavailable_offline));
                            emptyVideosTv.setText(getText(R.string.unavailable_offline));
                        case FETCH_COMPLETE:
                            emptyReviewTv.setText(getText(R.string.reviews_empty_text));
                            emptyVideosTv.setText(getText(R.string.videos_empty_text));
                    }
                }
            }
        });

        // Set-up an observer so we can get our video item results when received.
        moviesViewModel.getRelatedVideoItems().observe(this, new Observer<ArrayList<RelatedVideos>>() {
            @Override
            public void onChanged(@Nullable ArrayList<RelatedVideos> relatedVideoItems) {
                if (relatedVideoItems != null) {
                    mRelatedVideoItems.addAll(relatedVideoItems);
                     relatedVideosAdapter.notifyDataSetChanged();
                }
            }
        });

        // Set-up an observer to view the status of our
        moviesViewModel.getReviewItems().observe(this, new Observer<ArrayList<MovieReviewItem>>() {
            @Override
            public void onChanged(@Nullable ArrayList<MovieReviewItem> movieReviewItems) {
                if (movieReviewItems != null) {
                    mReviewItems.addAll(movieReviewItems);
                    Log.v(LOG_TAG, "REVIEWS: Notifying adapter changed observer");
                    reviewsAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    /**
     * Responsible for setting data to some of our views
     */
    private void setUpDetailView(){

        title.setText(mMovieItem.getTitle());
        releasedDateTv.setText(AppUtils.formatDate(new SimpleDateFormat("yyyy-MM-dd"),
                mMovieItem.getReleaseDate(),
                getActivity().getString(R.string.date_unknown), LOG_TAG));

        voteAverageTv.setText(Double.toString(mMovieItem.getVoteAverage()));
        synopsisTv.setText(mMovieItem.getOverview());
        configureFavsButton(favButton, mMovieItem);

        // Load background and poster images using picasso library.
        loadImageWithPicasso(mMovieItem.getBackgroundURL(), progressBar, backdrop);
        loadImageWithPicasso(mMovieItem.getPosterURL(), null, mPosterView);
    }

    /**
     * Responsible for setting up the list of related videos
     */
    private void setUpRelatedVideosList() {
        if(relatedVideosAdapter == null) {
            if(mRelatedVideoItems == null) {
                mRelatedVideoItems = new ArrayList<>();
            }
            // Create an adapter to display related videos.
            // This will eventually be replaced using dagger injection.
            relatedVideosAdapter = new RelatedVideosViewAdapter(mRelatedVideoItems,
                    new RelatedVideosViewAdapter.OnItemClickListener() {
                        @Override
                        public void onItemClick(String key) {
                            //Let's generate our URI from the item values
                            //NOTE: themoviedb api uses only youtube links for their videos,
                            //hence we are only parsing a youtube url.
                            try {
                                // We will try to open the link in a the youtube app
                                getActivity().startActivity(new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("vnd.youtube:" + key)
                                ));
                            } catch (ActivityNotFoundException ex) {
                                // if there is no app we will load through a browser
                                getActivity().startActivity(new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("http://www.youtube.com/watch?v=" + key)
                                ));
                            }
                        }
                    });
        }
        relatedVideoLv.setLayoutManager(new GridLayoutManager(getContext(), 1));
        relatedVideoLv.setAdapter(relatedVideosAdapter);
        relatedVideoLv.setEmptyView(emptyVideosTv);
        relatedVideoLv.setNestedScrollingEnabled(false);
    }

    /**
     * Responsible for setting up our list of reviews.
     */
    private void setUpReviewList() {
        if(reviewsAdapter == null) {
            Log.v(LOG_TAG, "REVIEWS: Review Adapter is NULL");
            // Create a review adapter. This will eventually be replaced using dagger injection.
            // (I'm still learning the intricacies of it.)
            if(mReviewItems == null){
                mReviewItems = new ArrayList<>();
            }
            reviewsAdapter = new MovieReviewsViewAdapter(
                    mReviewItems,
                    getActivity().getString(R.string.button_txt_read_more),
                    getActivity().getString(R.string.button_txt_hide));
        }
        reviewLv.setLayoutManager(new GridLayoutManager(getContext(), 1));
        reviewLv.setAdapter(reviewsAdapter);
        reviewLv.setEmptyView(emptyReviewTv);
        reviewLv.setNestedScrollingEnabled(false);
    }

    /**
     * Simple method for loading an image using picasso.
     * @param url The URL of the image we wish to process.
     * @param progressViewId The reference ID if the progress bar.
     * @param displayView The reference ID for the image view.
     */
    private void loadImageWithPicasso(String url, final ProgressBar progressViewId, final ImageView displayView) {
        if (appUtils.hasNetworkAccess()) {
            Picasso.get().load(url)
                    .into(displayView, new Callback() {
                        @Override
                        public void onSuccess() {
                            onPicassoSuccess(progressViewId);
                        }

                        @Override
                        public void onError(Exception e) {
                            onPicassoError(progressViewId, displayView);
                        }
                    });
        } else {
            // Looks like we are offline, so picasso will load from cache where available
            Picasso.get().load(url).networkPolicy(NetworkPolicy.OFFLINE)
                    .into(displayView, new Callback() {
                        @Override
                        public void onSuccess() {
                            onPicassoSuccess(progressViewId);
                        }

                        @Override
                        public void onError(Exception e) {
                            onPicassoError(progressViewId, displayView);
                        }
                    });
        }
    }

    private void onPicassoSuccess(ProgressBar progressViewId) {
        if (progressViewId != null) {
            progressViewId.setVisibility(View.GONE);
        }
    }

    private void onPicassoError(ProgressBar progressViewId, ImageView displayView) {
        if (progressViewId != null) {
            progressViewId.setVisibility(View.GONE);
        }
        if (displayView == backdrop) {
            appBarLayout.setExpanded(false);
            appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
                // Used code from: https://stackoverflow.com/a/39424318
                @Override
                public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                    if (Math.abs(verticalOffset) == appBarLayout.getTotalScrollRange()) {
                        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams)
                                collapsingToolbar.getLayoutParams();
                        params.setScrollFlags(0);
                    }
                }
            });
        }
    }

    /**
     * Configure favourites floating action button.
     * @param favButton reference object for our FAB button
     * @param item reference object for the currently viewed item (for which we will fav/un-fav).
     */
    private void configureFavsButton(final FloatingActionButton favButton, final MovieItem item) {

        if (item.getFavorite() == MovieItem.IS_FAVOURITE) {
            favButton.setImageResource(R.drawable.ic_star_filled);
            favButton.setContentDescription(getString(R.string.CD_image_icon_is_fav));
        } else {
            favButton.setImageResource(R.drawable.ic_star);
        }
        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                if (item.getFavorite() == MovieItem.IS_NOT_FAVOURITE) {
                    item.setFavorite(MovieItem.IS_FAVOURITE);
                    addFavorite = true;
                    favButton.setImageResource(R.drawable.ic_star_filled);
                } else {
                    item.setFavorite(MovieItem.IS_NOT_FAVOURITE);
                    favButton.setImageResource(R.drawable.ic_star);
                    addFavorite = false;
                }

                AppExecutors.getInstance().diskIO().execute(new CallbackRunnable(new RunnableInterface() {
                    @Override
                    public void onRunCompletion() {
                        if (addFavorite) {
                            prefs.incrementFavoriteCount();
                        } else {
                            prefs.decrementFavoriteCount();
                        }

                        // Let's avoid janky UI and check if the user is still looking at the originating
                        // item list. We don't want to trigger an observable update if the user is
                        // not looking at that category list anymore.
                        if(IS_LANDSCAPE_TABLET && mRequestSortOrder != null) {
                            if (moviesViewModel.getCurrentSortOrder().getValue().equals(mRequestSortOrder)) {
                                moviesViewModel.getMovies(mRequestSortOrder, MoviesViewModel.REFRESH_FROM_DATABASE);
                            } else {
                            }
                        }
                    }
                }) {
                    @Override
                    public void run() {
                        moviesDao.updateMovie(item);
                        // Let's trigger our callback once the database is successfully updated.
                        super.run();
                    }
                });
            }
        });
    }
}