package io.lundie.michael.viewcue.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import java.util.ArrayList;

import javax.inject.Inject;

import io.lundie.michael.viewcue.datamodel.models.MovieItem;
import io.lundie.michael.viewcue.datamodel.MovieRepository;
import io.lundie.michael.viewcue.ui.fragments.MovieListFragment;

public class MoviesViewModel extends ViewModel {

    public static final String LOG_TAG = MoviesViewModel.class.getName();

    private MovieRepository movieRepository;

    private MutableLiveData<ArrayList<MovieItem>> movieListObservable = new MutableLiveData<>();
    private MutableLiveData<MovieItem> selectedMovieItem = new MutableLiveData<>();

    public MoviesViewModel() {}

    @Inject
    public MoviesViewModel(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // Getter method for fetching data
    public LiveData<ArrayList<MovieItem>> getMovies(String sortOrder) {
        Log.i("TEST", "ViewModel get movies called");

        /*//movieListObservable = new LiveData<ArrayList<MovieItem>>();
        //Fetch Data Async from server.

        movieListObservable = MovieRepository.getInstance().getMovieList(sortOrder);*/

        movieListObservable = movieRepository.getMovieList(sortOrder);
        // Return our movies array list
        return movieListObservable;
    }

    public LiveData<MovieItem> getSelectedItem() {
        Log.i(LOG_TAG, "TEST Getting Item:" + selectedMovieItem.getValue() );
        return selectedMovieItem;
    }

    public void selectMovieItem(MovieItem item) {
        Log.i(LOG_TAG, "TEST Selecting Item:" + item);
        selectedMovieItem.setValue(item);
    }
}