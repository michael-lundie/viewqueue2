/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 07/10/18 11:58
 */

package io.lundie.michael.viewcue.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import io.lundie.michael.viewcue.R;

/**
 * Main / Root activity of ViewQueue
 */
public class MainActivity extends AppCompatActivity {

    public static final String LOG_TAG = MainActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

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
}