package io.lundie.michael.viewcue.ui.activities;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import javax.inject.Inject;

import butterknife.BindView;
import dagger.android.AndroidInjection;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import io.lundie.michael.viewcue.R;
import io.lundie.michael.viewcue.ui.fragments.EmptyDetailFragment;
import io.lundie.michael.viewcue.ui.fragments.MovieDetailFragment;
import io.lundie.michael.viewcue.ui.fragments.MovieListFragment;
import io.lundie.michael.viewcue.utilities.AppConstants;

/**
 * Main / Root activity of ViewQueue
 * All begins here.
 */
public class MainActivity extends AppCompatActivity implements HasSupportFragmentInjector {

    public static final String LOG_TAG = MainActivity.class.getName();

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        //Configure Dagger 2 injection
        this.configureDagger();

        if(savedInstanceState == null) {
            setUpListFragment();
            setUpDetailEmptyView();
        } else {
            MovieDetailFragment detailFragment = fetchDetailFragment();
            if (detailFragment != null) {
                if (getResources().getBoolean(R.bool.isLandscapeTablet)) {
                    getSupportFragmentManager().popBackStack();
                    swapFragment(detailFragment, R.id.detail_frame);
                } else {
                    getSupportFragmentManager().popBackStack();
                    swapFragment(detailFragment, R.id.content_frame);
                }
            } else {
                setUpListFragment();
                setUpDetailEmptyView();
            }
        }
    }

    /**
     * Sets up the list fragment for displaying our list of movies.
     */
    private void setUpListFragment() {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, new MovieListFragment(), AppConstants.FRAGTAG_LIST)
                .commit();
    }

    /**
     * @return the current detail fragment (if it exists)
     */
    private MovieDetailFragment fetchDetailFragment() {
        return (MovieDetailFragment) getSupportFragmentManager().findFragmentByTag(AppConstants.FRAGTAG_DETAIL);
    }

    /**
     * Method for swapping a fragment between content frames.
     * @param fragment The fragment which will be swapped
     * @param frameID the frame ID for which content frame we want to move our fragment too.
     */
    private void swapFragment(Fragment fragment, int frameID) {
        getSupportFragmentManager()
                .beginTransaction()
                .remove(fragment)
                .add(frameID, recreateFragment(fragment), AppConstants.FRAGTAG_DETAIL)
                .commit();
    }

    /**
     * Method to set up an empty detail view if we are running in landscape mode on a tablet.
     */
    private void setUpDetailEmptyView() {
        if (getResources().getBoolean(R.bool.isLandscapeTablet)) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.detail_frame, new EmptyDetailFragment(), AppConstants.FRAGTAG_EMPTY_DETAIL)
                    .commitAllowingStateLoss();
        }
    }

    // Note: There must be a better way to do this - it's really slow.
    // (Or I must optimise the rate at which the fragment can be recreated.
    private Fragment recreateFragment(Fragment f)
    {
        try {
            Fragment.SavedState savedState = getSupportFragmentManager().saveFragmentInstanceState(f);

            Fragment newInstance = f.getClass().newInstance();
            newInstance.setInitialSavedState(savedState);

            return newInstance;
        }
        catch (Exception e) // InstantiationException, IllegalAccessException
        {
            throw new RuntimeException("Cannot reinstantiate fragment " + f.getClass().getName(), e);
        }
    }

    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count < 1) {
            if(!getResources().getBoolean(R.bool.isLandscapeTablet) &&
                    getSupportFragmentManager().findFragmentByTag(AppConstants.FRAGTAG_DETAIL).isVisible()) {
                Log.v(LOG_TAG, "TEST, REPORTING on back");
                setUpListFragment();
            } else {
                super.onBackPressed();
            }
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

    private void configureDagger(){
        AndroidInjection.inject(this);
    }
}