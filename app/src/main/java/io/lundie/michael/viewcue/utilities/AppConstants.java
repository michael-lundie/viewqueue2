package io.lundie.michael.viewcue.utilities;

import android.app.Application;

import javax.inject.Inject;

import io.lundie.michael.viewcue.R;

public class AppConstants {

    Application mApplication;
    public static String SORT_ORDER_POPULAR;
    public static String SORT_ORDER_HIGHRATED ;

    @Inject
    public AppConstants(Application application) {
        mApplication = application;

        SORT_ORDER_POPULAR = mApplication.getString(R.string.settings_orderby_most_popular);
        SORT_ORDER_HIGHRATED = mApplication.getString(R.string.settings_orderby_high_rated);
    }

}
