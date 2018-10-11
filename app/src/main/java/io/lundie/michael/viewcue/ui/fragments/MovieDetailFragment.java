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
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.support.AndroidSupportInjection;
import io.lundie.michael.viewcue.ui.views.PercentageCropImageView;
import io.lundie.michael.viewcue.R;
import io.lundie.michael.viewcue.ui.helpers.SolidScrollShrinker;
import io.lundie.michael.viewcue.datamodel.models.MovieItem;
import io.lundie.michael.viewcue.viewmodel.MoviesViewModel;

public class MovieDetailFragment extends Fragment {

    private static final String LOG_TAG = MovieDetailFragment.class.getName();

    @Inject
    ViewModelProvider.Factory moviesViewModelFactory;
    private MoviesViewModel moviesViewModel;

    // Coordinator layout and tool/appbar view references.
    @BindView(R.id.main_content) CoordinatorLayout mRootDetailLayout;
    @BindView(R.id.appbar) AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.toolbar) Toolbar mToolbar;

    // Image/UI misc display view references.
    @BindView(R.id.title_background) View titleBackgroundView;
    @BindView(R.id.detail_view_poster) ImageView mPosterView;
    @BindView(R.id.backdrop_iv) PercentageCropImageView backdrop;
    @BindView(R.id.progressbar) ProgressBar progressBar;

    // References to views displaying text.
    @BindView(R.id.title) TextView title;
    @BindView(R.id.released_text_tv) TextView releasedDateTv;
    @BindView(R.id.vote_average_text_tv) TextView voteAverageTv;
    @BindView(R.id.synopsis_tv) TextView synopsisTv;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View detailFragmentView =  inflater.inflate(R.layout.fragment_movie_detail, container, false);
        // Bind view references with butterknife library.
        ButterKnife.bind(this, detailFragmentView);

        // Set-up toolbar.

        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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
                releasedDateTv.setText(formatDate(movieItem.getReleaseDate(), getActivity().getString(R.string.date_unknown)));
                voteAverageTv.setText(Double.toString(movieItem.getVoteAverage()));
                synopsisTv.setText(movieItem.getOverview());
                // Load background and poster images using glide library.
                loadImageWithGlide(movieItem.getBackgroundURL(), progressBar, backdrop);
                loadImageWithGlide(movieItem.getPosterURL(), null, mPosterView);
            }
        });
    }

    /**
     * Simple method for loading an image using glide.
     * @param url The URL of the image we wish to process.
     * @param progressViewId The reference ID if the progress bar.
     * @param displayView The reference ID for the image view.
     */
    private void loadImageWithGlide(String url, final ProgressBar progressViewId, final ImageView displayView) {
        Glide.with(this)
                .load(url)
                .listener(new RequestListener<Drawable>() {

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource)
                    {
                        if(progressViewId != null) {
                            progressViewId.setVisibility(View.GONE); }
                        if(displayView == backdrop) {
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
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(
                            Drawable resource, Object model, Target<Drawable> target,
                            DataSource dataSource, boolean isFirstResource)
                    {
                        if(progressViewId != null) {progressViewId.setVisibility(View.GONE);}
                        return false;
                    }
                })
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(displayView);
    }

    /**
     * A simple utility method to parse/format a given date to the users locale
     * @param dateString The original date string (from JSON Query)
     * @param errorMessage An error message to display if the date cannot be parsed.
     * @return Formatted date.
     */
    private String formatDate(String dateString, String errorMessage) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String parsedDate = errorMessage;
        try {
            Date date = dateFormat.parse(dateString);
            parsedDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Error parsing date.", e);
            e.printStackTrace();
        }
        return parsedDate;
    }
}