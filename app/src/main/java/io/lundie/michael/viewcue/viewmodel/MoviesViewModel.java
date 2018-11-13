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
    private MutableLiveData<ArrayList<MovieItem>> movieListObservable;
    private MutableLiveData<MovieItem> selectedMovieItem;
    private MutableLiveData<DataAcquireStatus> dataStatusObservable;
    private MutableLiveData<String> mCurrentSortOrder;

    // Defining model access constants.
    public static final byte REFRESH_DATA = 0;
    public static final byte DO_NOT_REFRESH_DATA = 1;
    public static final byte REFRESH_DATABASE = 2;

    public MoviesViewModel() {}

    @Inject
    public MoviesViewModel(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // Getter method for fetching data
    public LiveData<ArrayList<MovieItem>> getMovies(String sortOrder, byte refreshCase) {
        Log.i("TEST", "ViewModel get movies called");
        if (movieListObservable == null) {
            movieListObservable = new MutableLiveData<>();
        }

        setCurrentSortOrder(sortOrder);
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
        if (selectedMovieItem == null) {
            selectedMovieItem = new MutableLiveData<>();
        }
        selectedMovieItem.setValue(item);
    }

    public LiveData<DataAcquireStatus> getDataAcquireStatus() {
        if(dataStatusObservable == null) {
            dataStatusObservable = new MutableLiveData<>();
        }
        dataStatusObservable = movieRepository.getDataAcquireStatusLiveData();
        return dataStatusObservable;
    }

    public LiveData<String> getCurrentSortOrder() {
        return mCurrentSortOrder;
    }

    public void setCurrentSortOrder(String sortOrder) {
        if (mCurrentSortOrder == null) {
            mCurrentSortOrder = new MutableLiveData<>();
        }
        mCurrentSortOrder.postValue(sortOrder);
    }
}