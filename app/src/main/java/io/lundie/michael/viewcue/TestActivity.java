/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 29/09/18 10:25
 */

package io.lundie.michael.viewcue;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;

import junit.framework.Test;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.lundie.michael.viewcue.datamodel.models.MovieItem;
import io.lundie.michael.viewcue.datamodel.MovieRepository;
import io.lundie.michael.viewcue.ui.MovieListFragment;
import io.lundie.michael.viewcue.utilities.MovieResultsViewAdapter;
import io.lundie.michael.viewcue.viewmodel.MoviesViewModel;

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
            FragmentManager manager = getSupportFragmentManager();
            MovieListFragment listFragment = new MovieListFragment();
            manager.beginTransaction().replace(R.id.content_frame, listFragment, listFragment.getTag()).commit();
        }
    }

    public interface selectionChangeListener {

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
