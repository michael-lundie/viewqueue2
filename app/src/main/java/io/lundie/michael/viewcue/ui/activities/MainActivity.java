/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 08/10/18 09:34
 */

/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 07/10/18 11:58
 */

package io.lundie.michael.viewcue.ui.activities;

import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;
import io.lundie.michael.viewcue.R;
import io.lundie.michael.viewcue.ui.fragments.MovieListFragment;

/**
 * Main / Root activity of ViewQueue
 */
public class MainActivity extends AppCompatActivity implements HasSupportFragmentInjector {

    public static final String LOG_TAG = MainActivity.class.getName();

    @Inject
    DispatchingAndroidInjector<Fragment> dispatchingAndroidInjector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        this.configureDagger();
        if(savedInstanceState == null) {
            MovieListFragment listFragment = new MovieListFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content_frame, listFragment, listFragment.getTag())
                    .commit();
        }
    }

    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return dispatchingAndroidInjector;
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

    private void configureDagger(){
        AndroidInjection.inject(this);
    }
}