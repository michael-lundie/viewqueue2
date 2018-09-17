package io.lundie.michael.viewcue;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Tutorials/code assistance used from:
 * https://www.bignerdranch.com/blog/extracting-colors-to-a-palette-with-android-lollipop/
 * https://blog.iamsuleiman.com/toolbar-animation-with-android-design-support-library/
 */
public class DetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    @BindView(R.id.title) TextView title;
    @BindView(R.id.backdrop_iv) PercentageCropImageView backdrop;
    @BindView(R.id.progressbar) ProgressBar progressBar;
    @BindView(R.id.appbar) AppBarLayout appBarLayout;
    @BindView(R.id.collapsing_toolbar) CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.detail_view_poster) ImageView mPosterView;
    @BindView(R.id.main_content) CoordinatorLayout mRootDetailLayout;
    @BindView(R.id.title_background) View titleBackgroundView;

    Boolean appBarExpanded;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.detail_activity);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent activityIntent = getIntent();
        MovieItem movie = activityIntent.getParcelableExtra("movie");
        backdrop.setCropYCenterOffsetPct(0f);
        title.setText(movie.getTitle());
        collapsingToolbar.setExpandedTitleColor(getResources().getColor(R.color.colorTransparent));

        title.setText(movie.getTitle());

        // Solution for scaling from 'bottom': https://stackoverflow.com/a/22144862
        // Get height after layout is drawn solution: https://stackoverflow.com/a/24035591

        titleBackgroundView.post(new Runnable() {
            @Override
            public void run() {
                titleBackgroundView.getHeight(); //height is ready
                titleBackgroundView.setPivotY(titleBackgroundView.getHeight());
                Log.i(LOG_TAG, "ANIM: Pivot:" + (titleBackgroundView.getHeight()));
            }
        });

        appBarLayout.addOnOffsetChangedListener(new SolidScrollShrinker(titleBackgroundView));

        loadImageWithGlide(movie.getBackgroundURL(), progressBar, backdrop);
        loadImageWithGlide(movie.getPosterURL(), null, mPosterView);

    }

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