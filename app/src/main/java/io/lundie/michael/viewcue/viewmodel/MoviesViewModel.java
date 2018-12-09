package io.lundie.michael.viewcue.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;

import javax.inject.Inject;

import io.lundie.michael.viewcue.datamodel.models.item.MovieItem;
import io.lundie.michael.viewcue.datamodel.MovieRepository;
import io.lundie.michael.viewcue.datamodel.models.review.MovieReviewItem;
import io.lundie.michael.viewcue.datamodel.models.videos.RelatedVideos;
import io.lundie.michael.viewcue.utilities.DataStatus;

/**
 * The all important view model acting as the go-between, for our UI and data repository.
 */
public class MoviesViewModel extends ViewModel {

    private static final String LOG_TAG = MoviesViewModel.class.getName();

    //Preparing MutableLiveData reference variables
    private MovieRepository movieRepository;
    private static MutableLiveData<ArrayList<MovieItem>> movieListObservable;
    private static MutableLiveData<MovieItem> selectedMovieItem;
    private static MutableLiveData<ArrayList<MovieReviewItem>> movieReviewItems;
    private static MutableLiveData<ArrayList<RelatedVideos>> relatedVideoItems;
    private static MutableLiveData<DataStatus> listDataStatusLive;
    private static MutableLiveData<DataStatus> detailDataStatusLive;
    private static MutableLiveData<String> mCurrentSortOrder;

    // Defining model access constants.
    public static final byte REFRESH_DATA = 0;
    public static final byte DO_NOT_REFRESH_DATA = 1;
    public static final byte REFRESH_FROM_DATABASE = 2;

    public MoviesViewModel() { /* Required constructor. */ }

    @Inject
    public MoviesViewModel(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    // Getter method for fetching data
    public LiveData<ArrayList<MovieItem>> getMovies(String sortOrder, byte refreshCase) {

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
     * @param item MovieItem reference variable for the currently selected movie item
     */
    public void selectMovieItem(MovieItem item) {

        if (selectedMovieItem == null) {
            selectedMovieItem = new MutableLiveData<>();
        }
        selectedMovieItem.setValue(item);

        getExtras(item.getId());
    }
    
    /**
     * @return a reference to a MovieItem object which should be set through the selectMovieItem method.
     */
    public LiveData<MovieItem> getSelectedItem() {
        return selectedMovieItem;
    }
    /**
     * @return a reference to an ArrayList of  ReviewItem objects
     * which should be set through the selectMovieItem method.
     */
    public LiveData<ArrayList<MovieReviewItem>> getReviewItems() {
        return movieReviewItems;
    }

    /**
     * @return a reference to an ArrayList of MovieItem objects which should be set
     * through the selectMovieItem method.
     */
    public LiveData<ArrayList<RelatedVideos>> getRelatedVideoItems() {
        return relatedVideoItems;
    }

    /**
     * @return The sortOrder value in our most recent repository request.
     */
    public LiveData<String> getCurrentSortOrder() {
        return mCurrentSortOrder;
    }

    /**
     * Responsible for beginning the process of fetching our review and related video items.
     * @param itemID
     */
    private void getExtras(int itemID) {
        if(relatedVideoItems != null) {
            clearRelatedVideoItems();
        }
        fetchRelatedVideoItems(itemID);

        if(movieReviewItems != null) {
            clearReviewItems();
        }
        fetchReviewItems(itemID);
    }

    /**
     * Method for fetching related video items via the data repository
     * @param id ID of required movie object for which to return related videos
     */
    private void fetchRelatedVideoItems(int id) {
        relatedVideoItems = movieRepository.getRelatedVideos(id);
    }

    /**
     * Method for nulling the relatedVideoItems LiveData
     */
    private void clearRelatedVideoItems() { relatedVideoItems.setValue(null); }

    /**
     * Method for fetching review items items via the data repository
     * @param id ID of required movie object for which to return review items
     */
    private void fetchReviewItems(int id) {
        movieReviewItems = movieRepository.getReviewItems(id);
    }

    /**
     * Method for nulling the movieReviewItems LiveData
     */
    private void clearReviewItems() { movieReviewItems.setValue(null); }

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
     * This method returns status of LIST VIEW data requests.
     * @return a reference to our list LiveData<DataStatus> object.
     */
    public LiveData<DataStatus> getListDataAcquireStatus() {
        if(listDataStatusLive == null) {
            listDataStatusLive = new MutableLiveData<>();
        }
        listDataStatusLive = movieRepository.getListDataStatus();
        return listDataStatusLive;
    }

    /**
     * This is a simple getter method, which is used to access a LiveData object containing the
     * status of our network/api access calls and other data comms made by the repository.
     * This method returns status of DETAIL VIEW data requests.
     * @return a reference to our detail LiveData<DataStatus> object.
     */
    public LiveData<DataStatus> getDetailDataAcquireStatus() {
        if(detailDataStatusLive == null) {
            detailDataStatusLive = new MutableLiveData<DataStatus>();
        }
        detailDataStatusLive = movieRepository.getDetailDataStatus();
        return detailDataStatusLive;
    }
}