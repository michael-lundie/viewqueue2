package io.lundie.michael.viewcue.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import dagger.android.AndroidInjection;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.AndroidSupportInjection;
import dagger.android.support.HasSupportFragmentInjector;
import io.lundie.michael.viewcue.R;
import io.lundie.michael.viewcue.utilities.Prefs;

/**
 * Settings activity class allowing a user to alter various shared preferences.
 */
public class SettingsActivity extends AppCompatActivity implements HasSupportFragmentInjector {

    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    static String movieOrderInitialValue;

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;

    @BindView(R.id.settings_toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.configureDagger();
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        // Set up our toolbar/action bar
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }

    private void configureDagger(){
        AndroidInjection.inject(this);
    }

    public static class QueryPreferenceFragment extends PreferenceFragmentCompat
            implements Preference.OnPreferenceChangeListener {

        @Inject
        SharedPreferences sharedPrefs;

        @Inject
        Prefs prefs;

        Preference movieOrder;

        public QueryPreferenceFragment(){}

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            this.configureDagger();
            return super.onCreateView(inflater, container, savedInstanceState);

        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            addPreferencesFromResource(R.xml.settings_main);
            movieOrder = findPreference(getString(R.string.settings_orderby_key));
            bindPreferenceSummaryToValue(movieOrder);
        }

        /**
         * Links/Binds the UI description (through the use of a key value) with the current preference
         * value.
         * @param preference A preference object to bind
         */
        private void bindPreferenceSummaryToValue(Preference preference) {
            this.configureDagger();

            preference.setOnPreferenceChangeListener(this);
            getPreferenceManager().setSharedPreferencesName("vq_prefs");
            String preferenceValue = sharedPrefs.getString(preference.getKey(), "");

            if (preference == movieOrder) {
                movieOrderInitialValue = preferenceValue;
            }
            onPreferenceChange(preference, preferenceValue);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            Log.v(LOG_TAG, "Shared Pref changes");
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                Log.i(LOG_TAG, "Preference is List");
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    preference.setSummary(listPreference.getEntries()[prefIndex]);
                    sharedPrefs.edit().putString(preference.getKey(), value.toString()).apply();
                }
            } else {
                preference.setSummary(stringValue);
            }
            return true;
        }

        private void configureDagger(){
            AndroidSupportInjection.inject(this);
        }
    }
}