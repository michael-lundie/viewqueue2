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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.AndroidSupportInjection;
import io.lundie.michael.viewcue.datamodel.database.MoviesDao;
import io.lundie.michael.viewcue.ui.views.PercentageCropImageView;
import io.lundie.michael.viewcue.R;
import io.lundie.michael.viewcue.ui.helpers.SolidScrollShrinker;
import io.lundie.michael.viewcue.datamodel.models.MovieItem;
import io.lundie.michael.viewcue.utilities.AppExecutors;
import io.lundie.michael.viewcue.utilities.AppUtils;
import io.lundie.michael.viewcue.utilities.CallbackRunnable;
import io.lundie.michael.viewcue.utilities.RunnableInterface;
import io.lundie.michael.viewcue.viewmodel.MoviesViewModel;

public class MovieDetailFragment extends Fragment {

    private static final String LOG_TAG = MovieDetailFragment.class.getName();

    // Setting up some static variables
    private static boolean IS_LANDSCAPE_TABLET;

    @Inject
    ViewModelProvider.Factory moviesViewModelFactory;

    @Inject
    AppUtils appUtils;

    @Inject
    MoviesDao moviesDao;

    private MoviesViewModel moviesViewModel;

    private String mRequestSortOrder;

    // Coordinator layout and tool/appbar view references.
    @BindView(R.id.main_content) CoordinatorLayout mRootDetailLayout;
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Check what kind of device we are viewing on
        IS_LANDSCAPE_TABLET = getResources().getBoolean(R.bool.isLandscapeTablet);

        // Get bundle data.

        mRequestSortOrder = getArguments().getString("sortOrder");

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
        Log.i(LOG_TAG, "TEST: onActivityCreated, configureDagger");
        this.configureViewModel();
    }

    private void configureDagger(){ AndroidSupportInjection.inject(this); }

    private void configureViewModel(){
        moviesViewModel = ViewModelProviders.of(getActivity(), moviesViewModelFactory).get(MoviesViewModel.class);
        moviesViewModel.getSelectedItem().observe(this, new Observer<MovieItem>() {
            @Override
            public void onChanged(@Nullable MovieItem movieItem) {
                title.setText(movieItem.getTitle());
                releasedDateTv.setText(AppUtils.formatDate(new SimpleDateFormat("yyyy-MM-dd"),
                        movieItem.getReleaseDate(),
                        getActivity().getString(R.string.date_unknown), LOG_TAG));
                voteAverageTv.setText(Double.toString(movieItem.getVoteAverage()));
                synopsisTv.setText(movieItem.getOverview());

                configureFavsButton(favButton, movieItem);
                // Load background and poster images using glide library.
                loadImageWithPicasso(movieItem.getBackgroundURL(), progressBar, backdrop);
                loadImageWithPicasso(movieItem.getPosterURL(), null, mPosterView);

            }
        });
    }

    /**
     * Simple method for loading an image using picasso.
     * @param url The URL of the image we wish to process.
     * @param progressViewId The reference ID if the progress bar.
     * @param displayView The reference ID for the image view.
     */
    private void loadImageWithPicasso(String url, final ProgressBar progressViewId, final ImageView displayView) {
        if (appUtils.checkNetworkAccess()) {
            Log.i(LOG_TAG, "TEST: We have network access");
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
            Log.i(LOG_TAG, "TEST: No network, load from cache.");
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

    private void configureFavsButton(final FloatingActionButton favButton, final MovieItem item) {

        if (item.getFavorite() == MovieItem.IS_FAVOURITE) {
            favButton.setImageResource(R.drawable.ic_star_filled);
        } else {
            favButton.setImageResource(R.drawable.ic_star);
        }
        favButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {

                final String toastText;
                if (item.getFavorite() == MovieItem.IS_NOT_FAVOURITE) {
                    item.setFavorite(MovieItem.IS_FAVOURITE);
                    favButton.setImageResource(R.drawable.ic_star_filled);
                    toastText = "Made fav.";
                } else {
                    item.setFavorite(MovieItem.IS_NOT_FAVOURITE);
                    favButton.setImageResource(R.drawable.ic_star);
                    toastText = "Removed fav";
                }
                AppExecutors.getInstance().diskIO().execute(new CallbackRunnable(new RunnableInterface() {
                    @Override
                    public void complete() {
                        Log.i(LOG_TAG, "Running task complete. Update database.");
                        moviesViewModel.getMovies(mRequestSortOrder, MoviesViewModel.REFRESH_DATABASE);
                    }
                }) {
                    @Override
                    public void run() {
                        Log.i(LOG_TAG, "Running action" + moviesDao);
                        moviesDao.updateMovie(item);

                        // Let's trigger our callback if we are running in tablet + landscape mode.
                        // (If we are not running in this mode, these is no need to update the list UI.
                        if(IS_LANDSCAPE_TABLET) {
                            // All is well. Lets call super.run() which will trigger our callback.
                            super.run();
                        }

                    }
                });

            }
        });
    }

}