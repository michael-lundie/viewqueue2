package io.lundie.michael.viewcue.utilities;

import android.app.Application;

import javax.inject.Inject;

import io.lundie.michael.viewcue.R;

public class AppConstants {

    Application mApplication;
    public static String SORT_ORDER_POPULAR;
    public static String SORT_ORDER_HIGHRATED ;
    public static String SORT_ORDER_FAVS;

    public static String FRAGTAG_DETAIL;
    public static String FRAGTAG_LIST;
    public static String FRAGTAG_EMPTY_FAVS;
    public static String FRAGTAG_EMPTY_DETAIL;

    @Inject
    public AppConstants(Application application) {
        mApplication = application;

        SORT_ORDER_POPULAR = mApplication.getString(R.string.settings_orderby_most_popular);
        SORT_ORDER_HIGHRATED = mApplication.getString(R.string.settings_orderby_high_rated);
        SORT_ORDER_FAVS = mApplication.getString(R.string.settings_order_favorites);

        FRAGTAG_LIST = mApplication.getString(R.string.fragtag_content);
        FRAGTAG_DETAIL = mApplication.getString(R.string.fragtag_detail);
        FRAGTAG_EMPTY_FAVS = mApplication.getString(R.string.fragtag_favs_empty);
        FRAGTAG_EMPTY_DETAIL = mApplication.getString(R.string.frag_empty_detail);
    }
}
