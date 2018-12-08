package io.lundie.michael.viewcue.utilities;

import android.app.Application;
import android.content.SharedPreferences;

import javax.inject.Inject;

import io.lundie.michael.viewcue.R;

public class Prefs {
    private final static String REFRESH_TIME_POPULAR = "settings_db_update_time_pop_key";
    private final static String REFRESH_TIME_HIGH_RATED = "settings_db_update_time_hr_key";
    private final static String FAVORITES_COUNT = "settings_favorites_count";
    private final static String ATTEMPT_REFRESH_ON_RESUME = "attempt_refresh_on_resume";
    private final static String SHOW_OFFLINE_NOTICE = "offline_notice_count";

    private SharedPreferences mSharedPrefs;
    private Application mApplication;

    @Inject
    public Prefs(Application application, SharedPreferences sharedPrefs) {
        mApplication = application;
        mSharedPrefs = sharedPrefs;
    }

    public void setAttemptRefreshOnResume(boolean b) {
        mSharedPrefs.edit().putBoolean(ATTEMPT_REFRESH_ON_RESUME, b).apply();
    }

    public boolean attemptRefreshOnResume() {
        return mSharedPrefs.getBoolean(ATTEMPT_REFRESH_ON_RESUME, false);
    }

    public void setShowOfflineNotice(boolean b) {
        mSharedPrefs.edit().putBoolean(SHOW_OFFLINE_NOTICE, b).apply();
    }

    public boolean getShowOfflineNotice() {
        return mSharedPrefs.getBoolean(SHOW_OFFLINE_NOTICE, true);
    }

    public void updatePopularDbRefreshTime(long time) {
        mSharedPrefs.edit().putLong(REFRESH_TIME_POPULAR, time).apply();
    }

    public long getPopularRefreshTime() {
        return mSharedPrefs.getLong(REFRESH_TIME_POPULAR, 0);
    }


    public void updateHighRatedDbRefreshTime(long time) {
        mSharedPrefs.edit().putLong(REFRESH_TIME_HIGH_RATED, time).apply();
    }

    public long getHighRatedRefreshTime() {
        return mSharedPrefs.getLong(REFRESH_TIME_HIGH_RATED, 0);
    }

    public void incrementFavoriteCount() {
        int count = getFavoritesCount();
        mSharedPrefs.edit().putInt(FAVORITES_COUNT, ++count).apply();
    }

    public void decrementFavoriteCount() {
        int count = getFavoritesCount();
        mSharedPrefs.edit().putInt(FAVORITES_COUNT, --count).apply();
    }

    public int getFavoritesCount() { return mSharedPrefs.getInt(FAVORITES_COUNT, 0); }

    public String getOrderPref() {
        return mSharedPrefs.getString(mApplication.getString(R.string.settings_orderby_key),
                                     mApplication.getString(R.string.settings_orderby_most_popular));
    }
}