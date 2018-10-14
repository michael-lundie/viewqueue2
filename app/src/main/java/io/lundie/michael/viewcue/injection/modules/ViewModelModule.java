/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 10/10/18 06:30
 */

package io.lundie.michael.viewcue.injection.modules;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import dagger.Binds;
import dagger.Module;
import dagger.multibindings.IntoMap;
import io.lundie.michael.viewcue.injection.ViewModelKey;
import io.lundie.michael.viewcue.viewmodel.MoviesViewModelFactory;
import io.lundie.michael.viewcue.viewmodel.MoviesViewModel;

@Module
public abstract class ViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(MoviesViewModel.class)
    abstract ViewModel bindMoviesViewModel(MoviesViewModel repoViewModel);

    @Binds
    abstract ViewModelProvider.Factory bindViewModelFactory(MoviesViewModelFactory factory);
}
