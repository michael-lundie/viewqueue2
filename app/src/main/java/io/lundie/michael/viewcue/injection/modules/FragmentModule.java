/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 10/10/18 19:42
 */

package io.lundie.michael.viewcue.injection.modules;

import javax.inject.Singleton;

import dagger.Module;
import dagger.android.ContributesAndroidInjector;
import io.lundie.michael.viewcue.ui.activities.SettingsActivity;
import io.lundie.michael.viewcue.ui.fragments.MovieDetailFragment;
import io.lundie.michael.viewcue.ui.fragments.MovieListFragment;

@Module
public abstract class FragmentModule {
    @ContributesAndroidInjector
    abstract MovieListFragment contributeMovieListFragment();

    @ContributesAndroidInjector
    abstract MovieDetailFragment contributeMovieDetailFragment();

    @ContributesAndroidInjector(modules = {SharedPreferencesModule.class})
    abstract SettingsActivity.QueryPreferenceFragment contributeQueryPrefsFragment();
}
