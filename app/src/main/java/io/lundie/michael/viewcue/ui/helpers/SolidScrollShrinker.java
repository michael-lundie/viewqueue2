/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 22/09/18 12:59
 */

package io.lundie.michael.viewcue.ui.helpers;

import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.view.View;

public class SolidScrollShrinker implements AppBarLayout.OnOffsetChangedListener{

    private static final String LOG_TAG = SolidScrollShrinker.class.getSimpleName();

    private final View mScrollSolidView;

    public SolidScrollShrinker(@NonNull View scrollSolidView) {
        this.mScrollSolidView = scrollSolidView;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {

        double displacementFraction = (-verticalOffset / (float) appBarLayout.getHeight()) * 1.7;
        mScrollSolidView.setScaleY((1 - ((float) displacementFraction)));
    }
}