package io.lundie.michael.viewcue.utilities;

import android.app.Application;
import android.content.SharedPreferences;

import javax.inject.Inject;

import io.lundie.michael.viewcue.R;

public class Prefs {
    private final static String REFRESH_TIME= "settings_db_update_time_key";

    SharedPreferences mSharedPrefs;
    Application mApplication;

    @Inject
    public Prefs(Application application, SharedPreferences sharedPrefs) {
        mApplication = application;
        mSharedPrefs = sharedPrefs;
    }

    public void updateDbRefreshTime(long time) {
        mSharedPrefs.edit().putLong(REFRESH_TIME, time).apply();
    }

    public boolean hasRefreshTime() {
        return mSharedPrefs.contains(REFRESH_TIME);
    }

    public long getRefreshTime() {
        return mSharedPrefs.getLong(REFRESH_TIME, 0);
    }

    public String getOrderPref() {
        return mSharedPrefs.getString(mApplication.getString(R.string.settings_orderby_key),
                                     mApplication.getString(R.string.settings_orderby_most_popular));
    }
}
