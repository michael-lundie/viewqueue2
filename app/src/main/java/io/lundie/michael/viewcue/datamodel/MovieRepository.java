/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 08/12/18 16:04
 */
package io.lundie.michael.viewcue.datamodel;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import io.lundie.michael.viewcue.BuildConfig;
import io.lundie.michael.viewcue.datamodel.database.MoviesDao;
import io.lundie.michael.viewcue.datamodel.models.item.MovieItem;
import io.lundie.michael.viewcue.datamodel.models.review.MovieReviewItem;
import io.lundie.michael.viewcue.datamodel.models.review.MovieReviewsList;
import io.lundie.michael.viewcue.datamodel.models.item.MoviesItemSimple;
import io.lundie.michael.viewcue.datamodel.models.item.MoviesList;
import io.lundie.michael.viewcue.datamodel.models.videos.RelatedVideos;
import io.lundie.michael.viewcue.datamodel.models.videos.RelatedVideosList;
import io.lundie.michael.viewcue.network.TheMovieDbApi;
import io.lundie.michael.viewcue.utilities.AppExecutors;
import io.lundie.michael.viewcue.utilities.AppUtils;
import io.lundie.michael.viewcue.utilities.CallbackRunnable;
import io.lundie.michael.viewcue.utilities.DataStatus;
import io.lundie.michael.viewcue.utilities.Prefs;
import io.lundie.michael.viewcue.utilities.AppConstants;
import io.lundie.michael.viewcue.utilities.RunnableInterface;
import io.lundie.michael.viewcue.viewmodel.MoviesViewModel;
import retrofit2.Response;

import static io.lundie.michael.viewcue.utilities.DataStatus.*;

/**
 * Movie Repository class responsible for managing application data. It is responsible for keeping
 * the database and api synced so that offline access can be granted to a user.
 * An improved version of this would add paging ability (perhaps beyond scope in this case).
 */
public class MovieRepository {

    private static final String LOG_TAG = MovieRepository.class.getSimpleName();

    // Setting up methods which will be injected via the constructor with Dagger 2
    private final AppConstants constants;
    private final AppUtils appUtils;
    private final TheMovieDbApi theMovieDbApi;
    private final MoviesDao moviesDao;
    private final Prefs prefs;

    // Setting up LiveData variables
    private static MutableLiveData<ArrayList<MovieItem>> movieList = new MutableLiveData<>();
    private static MutableLiveData<DataStatus> listDataStatus = new MutableLiveData<>();
    private static MutableLiveData<DataStatus> detailDataStatus = new MutableLiveData<>();
    private static MutableLiveData<ArrayList<MovieReviewItem>> movieReviewItems;
    private static MutableLiveData<ArrayList<RelatedVideos>> relatedVideosLd;

    // Setting up ArrayLists for fetched data
    ArrayList<MovieItem> movieItems;
    ArrayList<MovieReviewItem> reviewItems;
    ArrayList<RelatedVideos> relatedVideos;

    /**
     * Constructor for our repository though which class/interface injection takes place.
     * @param theMovieDbApi An interface providing API end-point access methods.
     * @param moviesDao Data Access Object providing methods for database access and manipulation
     * @param prefs A class providing shared preferences access and manipulation methods.
     * @param constants A class providing application contexts (fetched through context)
     * @param appUtils Utility class.
     */
    @Inject
    public MovieRepository(TheMovieDbApi theMovieDbApi, MoviesDao moviesDao,
                           Prefs prefs, AppConstants constants, AppUtils appUtils) {
        this.moviesDao = moviesDao;
        this.theMovieDbApi = theMovieDbApi;
        this.prefs = prefs;
        this.constants = constants;
        this.appUtils = appUtils;
    }

    /**
     * Method for managing a request to fetch our primary movie list data. Data is initially fetched
     * from The Movie DB API and assigned to a LiveData variable (which is accessible via the
     * view model). The data is duplicated into a ROOM database in a background thread.
     * After the initial request, data requested is accessed from the database and automatically
     * refreshed regularly while the user is online.
     * @param sortOrder The requested sort order (or tab). Popular/High Rated and User Favourites
     * @param refreshCase Primarily in place to allow us to unregister observables without triggering
     *                    a refresh of the data.
     * @return MutableLiveData object containing an ArrayList of MovieItem(s)
     */
    public MutableLiveData<ArrayList<MovieItem>> getMovieList(final String sortOrder,
                                                              byte refreshCase) {
        switch (refreshCase) {
            case (MoviesViewModel.REFRESH_DATA):
                if (hasInvalidRefreshTime(sortOrder) && appUtils.hasNetworkAccess()
                        && !sortOrder.equals(constants.SORT_ORDER_FAVS)) {
                    // We have network access and the database requires updating (invalid refresh
                    // time), so we want to fetch data from our API and update everything.
                    // NOTE: refresh time is saved in a preference.
                    // If we don't need to refresh, we'll grab everything from the database by running
                    // into the next switch case 'REFRESH_FROM_DATABASE'.

                    // So... Let's go! Instantiate a new interface, which will give us access
                    // to a simple callback after retrofit has 'done its thing'.
                    RunnableInterface listRequestRunInterface = new RunnableInterface() {
                        @Override
                        public void onRunCompletion() {

                            // Notify a data observer that a fetch has been completed through Live Data.
                            setListDataStatus(FETCH_COMPLETE);

                            // We're running in a non-ui thread so let's use post value here.
                            movieList.postValue(movieItems);

                            // We'll commit the same data we just posted to the UI to our database.
                            // Note we do it in this order to deliver data to the user asap, though
                            // we are not observing our 'one true source' rule because of this.
                            commitItemsToDatabase(sortOrder);
                        }
                    };

                    // Notify any DataStatus observer that we're attempting to fetch data from API
                    setListDataStatus(ATTEMPTING_API_FETCH);

                    // Get an instance of AppExecutors. We will run retrofit in synchronous mode so
                    // we can micromanage some error handling / callbacks.
                    AppExecutors.getInstance().networkIO().execute(new CallbackRunnable(listRequestRunInterface) {
                        @Override
                        public void run() {
                            try {
                                //Attempt to fetch our movies list from the movies db api
                                Response<MoviesList> response = theMovieDbApi.getListOfMovies(
                                        sortOrder, BuildConfig.API_KEY).execute();

                                // Let's make sure we have a response from the MDB API (via retrofit)
                                if (response.isSuccessful()) {
                                    MoviesList moviesList = response.body();
                                    if (moviesList != null) {
                                        movieItems = moviesList.getResults();
                                        // If all has gone well, we run our callback.
                                        super.run();
                                    } else {
                                        // There is a problem parsing our data.
                                        setListDataStatus(ERROR_PARSING);
                                    }
                                } else {
                                    // Something went wrong while fetching data with the API.
                                    // Let's parse the returned error code and notify any data
                                    // status observers accordingly.
                                    switch (response.code()) {
                                        case 404:
                                            setListDataStatus(ERROR_NOT_FOUND);
                                            break;
                                        case 500:
                                            setListDataStatus(ERROR_SERVER_BROKEN);
                                            break;
                                        default:
                                            setListDataStatus(ERROR_UNKNOWN);
                                            break;
                                    }
                                }
                            } catch (IOException e) {
                                // Catch any IO errors - likely there is no network access.
                                Log.e(LOG_TAG, "Network failure: ", e);
                                // Notify any data status observers
                                setListDataStatus(ERROR_NETWORK_FAILURE);
                            }
                        }
                    });
                    // Everything went smoothly, so we will break out of our switch.
                    break;
                }

            // NOTE that to avoid code repetition here, as opposed to an else condition for the
            // above code, we are just running into the next case (no break statement).

            // The following case will be run upon any of these conditions being met:
            // a. Case is called directly from another class or method.
            // b. No Network access - an attempt will be made to fetch items from the database
            // c. sortOrder variable is set to SORT_ORDER_FAVS

            case (MoviesViewModel.REFRESH_FROM_DATABASE):
                setListDataStatus(FETCHING_FROM_DATABASE);
                fetchItemsFromDatabase(sortOrder);
                break;

            // The following case exists primarily to prevent getMovies from being called, when we
            // unregister any LiveData observables.
            // Hopefully, this won't be required once we are using singleton observers.

            case (MoviesViewModel.DO_NOT_REFRESH_DATA):
                break;
        }
        return movieList;
    }

    /**
     * A method used (via a view model object) to return review items for any given movie (by ID).
     * Data is NOT persisted in our database (out of scope)
     * @param id The id of the movie for which we want to request reviews.
     * @return A LiveData object containing an ArrayList of Movie Review Items
     */
    public MutableLiveData<ArrayList<MovieReviewItem>> getReviewItems(final int id) {

        if (movieReviewItems == null) {
            // Instantiate our MutableLiveData
            movieReviewItems = new MutableLiveData<>();
        }

        // Let's instantiate a new interface, which will give us access
        // to a simple callback after retrofit has 'done its thing'.
        RunnableInterface reviewsRequestRunInterface = new RunnableInterface() {
            @Override
            public void onRunCompletion() {
                // Note that we should never have a null value returned here.
                movieReviewItems.postValue(reviewItems);
            }
        };

        AppExecutors.getInstance().networkIO().execute(new CallbackRunnable(reviewsRequestRunInterface) {
            @Override
            public void run() {
                try {
                    // Attempt to return reviews from the API.
                    Response<MovieReviewsList> response =
                            theMovieDbApi.getMovieReviews(id, BuildConfig.API_KEY).execute();

                    // Let's make sure we have a response from the MDB API (via retrofit)
                    if (response.isSuccessful()) {
                        MovieReviewsList reviewsList = response.body();
                        if (reviewsList != null) {
                            reviewItems = reviewsList.getResults();
                            // All is well. Lets call super.run() which will trigger our callback.
                            super.run();
                        } else {
                            // No reviews available
                            setDetailDataStatus(NO_DATA_AVAILABLE);
                        }
                    } else {
                        // Something went wrong. Let's parse the error.
                        handleDetailRequestErrors(response.code());
                    }
                } catch (IOException e) {
                    // Catch any IO errors - likely there is no network access.
                    if(!appUtils.hasNetworkAccess()) {
                        setDetailDataStatus(ERROR_UNAVAILABLE_OFFLINE);
                    } else {
                        setDetailDataStatus(ERROR_NETWORK_FAILURE);
                    }
                }
            }
        });
        // Return movie review items live data (can return null)
        return movieReviewItems;
    }

    /**
     * A method used (via a view model object) to return related video items for any given movie
     * (by ID). Data is NOT persisted in our database (out of scope)
     * @param id The id of the movie for which we want to request reviews.
     * @return A LiveData object containing an ArrayList of Related Video Items
     */
    public MutableLiveData<ArrayList<RelatedVideos>> getRelatedVideos(final int id) {

        if (relatedVideosLd == null) {
            // Instantiate our MutableLiveData
            relatedVideosLd = new MutableLiveData<>();
        }

        // Let's instantiate a new interface, which will give us access
        // to a simple callback after retrofit has 'done its thing'.
        RunnableInterface relatedItemsRequestRunInterface = new RunnableInterface() {
            @Override
            public void onRunCompletion() {
                setDetailDataStatus(FETCH_COMPLETE);
                // Note that we should never have a null value returned here.
                relatedVideosLd.postValue(relatedVideos);
            }
        };

        AppExecutors.getInstance().networkIO().execute(new CallbackRunnable(relatedItemsRequestRunInterface) {
            @Override
            public void run() {
                try {
                    Response<RelatedVideosList> response =
                            theMovieDbApi.getRelatedVideos(id, BuildConfig.API_KEY).execute();

                    // Let's make sure we have a response from the MDB API (via retrofit)
                    if (response.isSuccessful()) {
                        RelatedVideosList relatedVideosList = response.body();
                        if (relatedVideosList != null) {
                            relatedVideos = relatedVideosList.getResults();
                            // All is well. Lets call super.run() which will trigger our callback.
                            super.run();
                        } else {
                            setDetailDataStatus(NO_DATA_AVAILABLE);
                        }
                    } else {
                        // Something went wrong. Let's parse the error.
                        handleDetailRequestErrors(response.code());
                    }
                } catch (IOException e) {
                    // Catch any IO errors - likely there is no network access.
                    if(!appUtils.hasNetworkAccess()) {
                        setDetailDataStatus(ERROR_UNAVAILABLE_OFFLINE);
                    } else {
                        setDetailDataStatus(ERROR_NETWORK_FAILURE);
                    }
                }
            }
        });
        //Return related video items live data (can return null)
        return relatedVideosLd;
    }

    /**
     * A method to handle returned API error codes and set the appropriate data status, notifying
     * and data observers appropriately.
     * @param responseCode
     */
    private void handleDetailRequestErrors(int responseCode) {
        switch (responseCode) {
            case 404:
                setListDataStatus(ERROR_NOT_FOUND);
                break;
            case 500:
                setListDataStatus(ERROR_SERVER_BROKEN);
                break;
            default:
                setListDataStatus(ERROR_UNKNOWN);
                break;
        }
    }

    /**
     * A method which manages the commitment of data to our database via a background thread.
     * @param sortOrder The sort order of the data which was requested by the user.
     */
    private void commitItemsToDatabase(final String sortOrder) {

        if (movieItems != null) {

            // Our variable checks out so we are going to proceed to attempt to write our data.
            if (sortOrder.equals(constants.SORT_ORDER_POPULAR)) {

                // Create an array of current data objects to compare the new objects against.
                // Note: we are getting a simplified movieList object for the sake of efficiency
                List<MoviesItemSimple> oldMovieList = moviesDao.fetchSimpleListPopular();

                // Loop through all the newly retrieved movie items
                for (int i = 0; i < movieItems.size(); i++) {

                    // Check our new data item against items in our old list to see if it was a fav.
                    checkAndSetFavorites(oldMovieList, i);

                    // Set the new item as popular (according to sort order).
                    movieItems.get(i).setPopular((i + 1));

                    // Insert the movie to our database. See onConflictStrategy documentation (REPLACE):
                    // https://sqlite.org/lang_conflict.html
                    moviesDao.insertMovie(movieItems.get(i));
                }
                // Finally update our preferences with the most recent refresh time.
                prefs.updatePopularDbRefreshTime(new Date(System.currentTimeMillis()).getTime());

            } else if (sortOrder.equals(constants.SORT_ORDER_HIGHRATED)) {

                // Process as above. See previous notes.
                List<MoviesItemSimple> oldMovieList = moviesDao.fetchSimpleListHighRated();

                for (int i = 0; i < movieItems.size(); i++) {

                    checkAndSetFavorites(oldMovieList, i);
                    movieItems.get(i).setHighRated((i + 1));
                    moviesDao.insertMovie(movieItems.get(i));
                }

                prefs.updateHighRatedDbRefreshTime(new Date(System.currentTimeMillis()).getTime());
            }
        }
    }

    /**
     * Method compares a data item against an ArrayList of movies previously stored in our database
     * in order to check if we need to set the new item as a favourite.
     * @param oldMovieList Takes input of a simplified MovieItem object.
     * @param i A new data item from which we wish to check against our old list of movies.
     */
    private void checkAndSetFavorites(List<MoviesItemSimple> oldMovieList, int i) {
        // Get the item ID for the newly retrieved movie object.
        int newItemId = movieItems.get(i).getId();

        // Loop through the old list of items to check if any of them were favourites.
        for (int j = 0; j < oldMovieList.size(); j++) {
            // Check the matching object (if it exists)
            if (newItemId == oldMovieList.get(j).getId()) {
                // If the movie was a favourite, update the new list accordingly.
                if (oldMovieList.get(j).getFavorite() == MovieItem.IS_FAVOURITE) {
                    movieItems.get(i).setFavorite(MovieItem.IS_FAVOURITE);
                }
                // Remove the object from our ArrayList of old entries. No point in
                // looping through the item unnecessarily.
                oldMovieList.remove(j);
                // Kill the current loop.
                break;
            }
        }
    }

    /**
     * Fetch items from our ROOM database (using our DAO)
     * @param sortOrder Sort Order of the requested items
     */
    private void fetchItemsFromDatabase(final String sortOrder) {
        Log.i(LOG_TAG, "TEST: Retrieving items from database");
        AppExecutors.getInstance().diskIO().execute(new CallbackRunnable(new RunnableInterface() {
            @Override
            public void onRunCompletion() {
                if (!movieItems.isEmpty()) {
                    setListDataStatus(FETCH_COMPLETE);
                    movieList.postValue(movieItems);
                } else {
                    setListDataStatus(DATABASE_EMPTY);
                }
            }
        }) {
            @Override
            public void run() {
                if (sortOrder.equals(AppConstants.SORT_ORDER_POPULAR)) {
                    // Retrieve popular items from database.
                    movieItems = (ArrayList<MovieItem>) moviesDao.loadPopularMovies();
                    //movieList.postValue(movieItems);
                } else if (sortOrder.equals(AppConstants.SORT_ORDER_HIGHRATED)) {
                    // Retrieve high rated items from database.
                    movieItems = (ArrayList<MovieItem>) moviesDao.loadHighRatedMovies();
                    //movieList.postValue(movieItems);
                } else {
                    // Retrieve favorite items from database.
                    movieItems = (ArrayList<MovieItem>) moviesDao.loadFavoriteMovies();
                    //movieList.postValue(movieItems);
                } super.run();
            }
        });
    }

    /**
     * Check the current refresh status of data in the database.
     * If data is 'out-of-date' returns true.
     * @param sortOrder The sort order of the current data
     * @return boolean value determined by how long it has been since last data update.
     */
    private boolean hasInvalidRefreshTime(String sortOrder) {
        long lastRefreshTime = 0;

        if (sortOrder.equals(constants.SORT_ORDER_POPULAR)) {
            lastRefreshTime = prefs.getPopularRefreshTime();
        } else if (sortOrder.equals(constants.SORT_ORDER_HIGHRATED)) {
            lastRefreshTime = prefs.getHighRatedRefreshTime();
        }

        Date date = new Date(System.currentTimeMillis());
        long currentTimeMs = date.getTime();

        if ((currentTimeMs - lastRefreshTime) > 240_000 || lastRefreshTime == 0) {
            return true;
        }
        return false;
    }

    /**
     * Returns the current listDataStatus (via view model).
     * @return Live Data status (for list items)
     */
    public MutableLiveData<DataStatus> getListDataStatus() {
        return listDataStatus;
    }

    /**
     * Set the listDataStatus variable.
     * @param status status set via enum DataStatus
     */
    private void setListDataStatus(final DataStatus status) {
        // status set from background threads, so using post method.
        listDataStatus.postValue(status);
    }

    /**
     * Returns the current detailDataStatus (via view model).
     * @return Live Data status (for list items)
     */
    public MutableLiveData<DataStatus> getDetailDataStatus() {
        return detailDataStatus;
    }

    /**
     * Set the detailDataStatus variable
     * @param status status set via enum DataStatus
     */
    private void setDetailDataStatus(final DataStatus status) {
        // status set from background threads, so using post method.
        detailDataStatus.postValue(status);
    }
}