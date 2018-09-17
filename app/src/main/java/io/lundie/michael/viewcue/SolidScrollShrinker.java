package io.lundie.michael.viewcue;

import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;

public class SolidScrollShrinker implements AppBarLayout.OnOffsetChangedListener{

    private static final String LOG_TAG = SolidScrollShrinker.class.getSimpleName();

    private final View mScrollSolidView;

    SolidScrollShrinker(@NonNull View scrollSolidView) {
        this.mScrollSolidView = scrollSolidView;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

        double displacementFraction = (-verticalOffset / (float) appBarLayout.getHeight()) * 1.7;
        Log.i(LOG_TAG, "ANIM: Displacement:" + (1- displacementFraction));
        mScrollSolidView.setScaleY((1 - ((float) displacementFraction)));
        //childView.setTranslationY((float) displacementFraction);
    }
}