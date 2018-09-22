package io.lundie.michael.viewcue;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Settings activity class allowing a user to alter various shared preferences.
 */
public class SettingsActivity extends AppCompatActivity {

    private static final String LOG_TAG = SettingsActivity.class.getSimpleName();

    private static boolean settingsChanged = false;
    /** Stores the initial value (upon access of SettingActivity) of
     * {@link QueryPreferenceFragment#apiKey}.*/
    private static String apiKeyInitialValue;
    /** Stores the initial value (upon access of SettingActivity) of
     * {@link QueryPreferenceFragment#apiKey}.*/
    private static String movieOrderInitialValue;

    @BindView(R.id.toolbar) Toolbar mToolbar;

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
        Log.d("CDA", "onBackPressed Called");
        Intent returnIntent = new Intent();
        returnIntent.putExtra("settingsChanged",settingsChanged);
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    public static class QueryPreferenceFragment extends PreferenceFragment
            implements Preference.OnPreferenceChangeListener {

        Preference apiKey;
        Preference movieOrder;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings_main);
            apiKey = findPreference(getString(R.string.settings_themoviedb_apikey_key));
            bindPreferenceSummaryToValue(apiKey);
            movieOrder = findPreference(getString(R.string.settings_orderby_key));
            bindPreferenceSummaryToValue(movieOrder);
        }

        /**
         * Links/Binds the UI description (through the use of a key value) with the current preference
         * value.
         * @param preference A preference object to bind
         */
        private void bindPreferenceSummaryToValue(Preference preference) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences =
                    PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceValue = preferences.getString(preference.getKey(), "");
            if (preference == apiKey) {
                apiKeyInitialValue = preferenceValue;
            } else {
                movieOrderInitialValue = preferenceValue;
            } onPreferenceChange(preference, preferenceValue);
        }

        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue = value.toString();
            if (preference instanceof ListPreference) {
                ListPreference listPreference = (ListPreference) preference;
                int prefIndex = listPreference.findIndexOfValue(stringValue);
                if (prefIndex >= 0) {
                    CharSequence[] labels = listPreference.getEntries();
                    preference.setSummary(labels[prefIndex]);
                }
            } else {
                preference.setSummary(stringValue);
            }

            String key = preference.getKey();  // Check this returns string and not id

            if (key.equals(getString(R.string.settings_themoviedb_apikey_key))) {
                if (value.equals(apiKeyInitialValue)) {
                    settingsChanged = false;
                } else {
                    settingsChanged = true;
                }
            } else if (key.equals(getString(R.string.settings_orderby_key))) {
                if (value.equals(movieOrderInitialValue)) {
                    settingsChanged = false;
                } else {
                    settingsChanged = true;
                }
            } return true;
        }
    }
}