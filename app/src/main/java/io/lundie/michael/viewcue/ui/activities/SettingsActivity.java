/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 26/09/18 20:57
 */

package io.lundie.michael.viewcue.ui.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.preference.PreferenceManager;
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
import dagger.android.support.AndroidSupportInjection;
import io.lundie.michael.viewcue.R;

/**
 * Settings activity class allowing a user to alter various shared preferences.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    private static String movieOrderInitialValue;



    @BindView(R.id.settings_toolbar) Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    public static class QueryPreferenceFragment extends PreferenceFragmentCompat
            implements Preference.OnPreferenceChangeListener {

        @Inject
        SharedPreferences sharedPrefs;

        Preference movieOrder;

        public QueryPreferenceFragment(){}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.i(LOG_TAG, "Configure dagger called");
            configureDagger();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            this.configureDagger();
            return super.onCreateView(inflater, container, savedInstanceState);

        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            Log.i(LOG_TAG, "Configure dagger called");
            //configureDagger();
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
            //SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
            String preferenceValue = sharedPrefs.getString(preference.getKey(), "");

            if (preference == movieOrder) {
                Log.i(LOG_TAG, "Preference is MOVIE ORDER");
                movieOrderInitialValue = preferenceValue;
                Log.i(LOG_TAG, "Pref is now:" + preferenceValue);
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

                preference.setSummary(
                        prefIndex >= 0 ? listPreference.getEntries()[prefIndex] : null);

            } else {
                preference.setSummary(stringValue);
            }

            String key = preference.getKey();  // Check this returns string and not id

            return true;
        }

        private void configureDagger(){
            AndroidSupportInjection.inject(this);
        }
    }


}