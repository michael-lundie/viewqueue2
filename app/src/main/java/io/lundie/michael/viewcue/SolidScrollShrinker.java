package io.lundie.michael.viewcue;

import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
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
        mScrollSolidView.setScaleY((1 - ((float) displacementFraction)));
    }
}