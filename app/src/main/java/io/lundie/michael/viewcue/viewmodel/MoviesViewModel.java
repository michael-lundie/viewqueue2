package io.lundie.michael.viewcue.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import java.util.ArrayList;

import javax.inject.Inject;

import io.lundie.michael.viewcue.datamodel.models.MovieItem;
import io.lundie.michael.viewcue.datamodel.MovieRepository;
import io.lundie.michael.viewcue.utilities.DataAcquireStatus;

public class MoviesViewModel extends ViewModel {

    private static final String LOG_TAG = MoviesViewModel.class.getName();

    private MovieRepository movieRepository;
    private MutableLiveData<ArrayList<MovieItem>> movieListObservable = new MutableLiveData<>();
    private MutableLiveData<MovieItem> selectedMovieItem = new MutableLiveData<>();
    private MutableLiveData<DataAcquireStatus> dataStatusObservable = new MutableLiveData<>();

    // Defining model access constants.
    public static final byte REFRESH_DATA = 0;
    public static final byte DO_NOT_REFRESH_DATA = 1;

    public MoviesViewModel() {}

    @Inject
    public MoviesViewModel(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // Getter method for fetching data
    public LiveData<ArrayList<MovieItem>> getMovies(String sortOrder, byte refreshCase) {
        Log.i("TEST", "ViewModel get movies called");

        movieListObservable = movieRepository.getMovieList(sortOrder, refreshCase);
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

    public LiveData<DataAcquireStatus> getDataAcquireStatus() {
        dataStatusObservable = movieRepository.getDataAcquireStatusLiveData();
        return dataStatusObservable;
    }
}