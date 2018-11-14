package io.lundie.michael.viewcue.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import java.util.ArrayList;

import javax.inject.Inject;

import io.lundie.michael.viewcue.datamodel.models.MovieItem;
import io.lundie.michael.viewcue.datamodel.MovieRepository;
import io.lundie.michael.viewcue.datamodel.models.MovieReviewItem;
import io.lundie.michael.viewcue.utilities.DataAcquireStatus;

public class MoviesViewModel extends ViewModel {

    private static final String LOG_TAG = MoviesViewModel.class.getName();

    private MovieRepository movieRepository;
    private MutableLiveData<ArrayList<MovieItem>> movieListObservable;
    private MutableLiveData<MovieItem> selectedMovieItem;
    private MutableLiveData<ArrayList<MovieReviewItem>> movieReviewItems;
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

    /**
     * A simple method used to select a specific movie from our list, and load it into a
     * live data object which can be accessed by our fragment via the view model
     * @param item
     */
    public void selectMovieItem(MovieItem item) {
        Log.i(LOG_TAG, "TEST Selecting Item:" + item);
        if (selectedMovieItem == null) {
            selectedMovieItem = new MutableLiveData<>();
        }
        selectedMovieItem.setValue(item);
        fetchReviewItems(item.getId());
    }

    /**
     * @return a reference to a MovieItem object which should be set through the selectMovieItem method.
     */
    public LiveData<MovieItem> getSelectedItem() {
        Log.i(LOG_TAG, "TEST Getting Item:" + selectedMovieItem.getValue() );
        return selectedMovieItem;
    }

    public LiveData<ArrayList<MovieReviewItem>> getReviewItems() {
        return movieReviewItems;
    }

    /**
     * @return The sortOrder value in our most recent repository request.
     */
    public LiveData<String> getCurrentSortOrder() {
        return mCurrentSortOrder;
    }

    private void fetchReviewItems(int id) {
        movieReviewItems = movieRepository.getReviewItems(id);
    }

    /**
     * Simple setter method to update a LiveData variable with a reference to the most recently
     * used sort order - ie: the current movie list being viewed by the user. This is managed
     * automatically.
     * @param sortOrder - sort order for our list of movies, accessed via The Movie Database API.
     */
    private void setCurrentSortOrder(String sortOrder) {
        if (mCurrentSortOrder == null) {
            mCurrentSortOrder = new MutableLiveData<>();
        }
        mCurrentSortOrder.postValue(sortOrder);
    }

    /**
     * This is a simple getter method, which is used to access a LiveData object containing the
     * status of our network/api access calls and other data comms made by the repository.
     * This is used in lieu of an interface.
     * @return a reference to our LiveData<DataAcquireStatus> object.
     */
    public LiveData<DataAcquireStatus> getDataAcquireStatus() {
        if(dataStatusObservable == null) {
            dataStatusObservable = new MutableLiveData<>();
        }
        dataStatusObservable = movieRepository.getDataAcquireStatusLiveData();
        return dataStatusObservable;
    }
}