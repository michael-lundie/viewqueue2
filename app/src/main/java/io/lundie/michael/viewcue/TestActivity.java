/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 29/09/18 10:25
 */

package io.lundie.michael.viewcue;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

import butterknife.ButterKnife;
import io.lundie.michael.viewcue.datamodel.models.MovieItem;
import io.lundie.michael.viewcue.ui.fragments.MovieListFragment;
import io.lundie.michael.viewcue.ui.views.RecycleViewWithSetEmpty;

public class TestActivity extends AppCompatActivity implements SharedPreferences.OnSharedPreferenceChangeListener{

    public static final String LOG_TAG = TestActivity.class.getName();

    private RecycleViewWithSetEmpty.Adapter mAdapter;
    private ArrayList<MovieItem> mList = new ArrayList<>();
    private boolean hasInternet = true;

    private String orderPreference;

    /** A boolean value indicating whether or setting shave been changed. */
    static boolean settingsChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        // Bind view references with butterknife library.
        ButterKnife.bind(this);

        if(savedInstanceState == null) {
            MovieListFragment listFragment = new MovieListFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, listFragment, listFragment.getTag())
                    .commit();
        }
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
        } else {
            getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        super.onSupportNavigateUp();
        onBackPressed();
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.i(LOG_TAG, "TEST: Prefs changed");
        if (key.equals(getString(R.string.settings_orderby_key))) {
            mList.clear();
            mAdapter.notifyDataSetChanged();
            // model.getMovies(getSharedPreferences());
        }
    }
}