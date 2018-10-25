package io.lundie.michael.viewcue.utilities;

import android.app.Application;
import android.content.SharedPreferences;

import javax.inject.Inject;

import io.lundie.michael.viewcue.R;

public class Prefs {
    private final static String REFRESH_TIME_POPULAR = "settings_db_update_time_pop_key";
    private final static String REFRESH_TIME_HIGH_RATED = "settings_db_update_time_hr_key";

    SharedPreferences mSharedPrefs;
    Application mApplication;

    @Inject
    public Prefs(Application application, SharedPreferences sharedPrefs) {
        mApplication = application;
        mSharedPrefs = sharedPrefs;
    }

    public void updatePopularDbRefreshTime(long time) {
        mSharedPrefs.edit().putLong(REFRESH_TIME_POPULAR, time).apply();
    }

    public void updateHighRatedDbRefreshTime(long time) {
        mSharedPrefs.edit().putLong(REFRESH_TIME_HIGH_RATED, time).apply();
    }

    public boolean hasPopularRefreshTime() {
        return mSharedPrefs.contains(REFRESH_TIME_POPULAR);
    }

    public boolean hasHighRatedRefreshTime() {
        return mSharedPrefs.contains(REFRESH_TIME_HIGH_RATED);
    }

    public long getPopularRefreshTime() {
        return mSharedPrefs.getLong(REFRESH_TIME_POPULAR, 0);
    }

    public long getHighRatedRefreshTime() {
        return mSharedPrefs.getLong(REFRESH_TIME_HIGH_RATED, 0);
    }

    public String getOrderPref() {
        return mSharedPrefs.getString(mApplication.getString(R.string.settings_orderby_key),
                                     mApplication.getString(R.string.settings_orderby_most_popular));
    }
}
