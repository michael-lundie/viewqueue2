package io.lundie.michael.viewcue;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Detail Activity
 * Tutorials/code assistance used from:
 * https://www.bignerdranch.com/blog/extracting-colors-to-a-palette-with-android-lollipop/
 * https://blog.iamsuleiman.com/toolbar-animation-with-android-design-support-library/
 */
public class DetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);

        // Bind view references with butterknife library.
        ButterKnife.bind(this);

        // Set-up toolbar.
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(R.color.colorTransparent));

        // Get intent and parcelable from main activity.
        Intent activityIntent = getIntent();
        MovieItem movie = activityIntent.getParcelableExtra("movie");

        // Set Y center offset crop (uses 'PercentCropImageView')
        backdrop.setCropYCenterOffsetPct(0f);

        // Set text data to appropriate views.
        title.setText(movie.getTitle());
        releasedDateTv.setText(movie.getDate());
        voteAverageTv.setText(Double.toString(movie.getVoteAverage()));
        synopsisTv.setText(movie.getSynopsis());

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

        // Load background and poster images using glide library.
        loadImageWithGlide(movie.getBackgroundURL(), progressBar, backdrop);
        loadImageWithGlide(movie.getPosterURL(), null, mPosterView);
    }

    /**
     * Simple method for loading an image using glide.
     * @param url The URL of the image we wish to process.
     * @param progressViewId The reference ID if the progress bar.
     * @param displayView The reference ID for the image view.
     */
    private void loadImageWithGlide(String url, final ProgressBar progressViewId, ImageView displayView) {
        final ProgressBar progressView = progressViewId;
        Glide.with(this)
                .load(url)
                .listener(new RequestListener<Drawable>() {

                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        if(progressViewId != null) {
                        progressView.setVisibility(View.GONE); }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        if(progressViewId != null) {progressBar.setVisibility(View.GONE);}
                        return false;
                    }
                })
                .into(displayView);
    }
}