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
