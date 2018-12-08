/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 27/09/18 22:45
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

public class MovieRepository {

    private static final String LOG_TAG = MovieRepository.class.getSimpleName();

    private final AppConstants constants;
    private final AppUtils appUtils;
    private final TheMovieDbApi theMovieDbApi;
    private final MoviesDao moviesDao;
    private final Prefs prefs;

    // Add requirement for client in method params, and use api.getClient.
    @Inject
    public MovieRepository(TheMovieDbApi theMovieDbApi,
                           MoviesDao moviesDao, Prefs prefs, AppConstants constants,
                           AppUtils appUtils) {
        this.moviesDao = moviesDao;
        this.theMovieDbApi = theMovieDbApi;
        this.prefs = prefs;
        this.constants = constants;
        this.appUtils = appUtils;
    }

    private MutableLiveData<ArrayList<MovieItem>> movieList = new MutableLiveData<>();

    private MutableLiveData<ArrayList<MovieReviewItem>> movieReviewItems;

    private MutableLiveData<ArrayList<RelatedVideos>> relatedVideosLd;

    private MutableLiveData<DataStatus> listDataStatus = new MutableLiveData<>();

    private MutableLiveData<DataStatus> detailDataStatus = new MutableLiveData<>();

    ArrayList<MovieItem> movieItems;

    ArrayList<MovieReviewItem> reviewItems;

    ArrayList<RelatedVideos> relatedVideos;

    public MutableLiveData<ArrayList<MovieItem>> getMovieList(final String sortOrder,
                                                              byte refreshCase) {
        switch (refreshCase) {
            case (MoviesViewModel.REFRESH_DATA):

                if (hasInvalidRefreshTime(sortOrder) && appUtils.hasNetworkAccess()) {

                    // Let's instantiate a new interface, which will give us access
                    // to a simple callback after retrofit has 'done its thing'.
                    RunnableInterface listRequestRunInterface = new RunnableInterface() {
                        @Override
                        public void onRunCompletion() {
                            setListDataStatus(FETCH_COMPLETE);
                            movieList.postValue(movieItems);
                            commitItemsToDatabase(sortOrder);
                        }
                    };

                    setListDataStatus(ATTEMPTING_API_FETCH);

                    // Check to see if the refresh database limit has been passed. If so, we want to fetch
                    // data from our API and update everything. NOTE: refresh time is saved in a preference.
                    // If we don't need to refresh, we'll grab everything from the database.

                    // Get an instance of AppExecutors. We will run retrofit in synchronous mode so
                    // we can micromanage some error handling / callbacks.
                    AppExecutors.getInstance().networkIO().execute(new CallbackRunnable(listRequestRunInterface) {
                        @Override
                        public void run() {
                            try {
                                //Attempt to fetch our movies list from the movies db api
                                Response<MoviesList> response =
                                        theMovieDbApi.getListOfMovies(sortOrder, BuildConfig.API_KEY).execute();

                                // Let's make sure we have a response from the MDB API (via retrofit)
                                if (response.isSuccessful()) {
                                    MoviesList moviesList = response.body();
                                    if (moviesList != null) {
                                        movieItems = moviesList.getResults();
                                        // If all has gone well, we run our callback.
                                        super.run();
                                    } else {
                                        setListDataStatus(ERROR_PARSING);
                                    }
                                } else {
                                    // Something went wrong. Let's parse the error using codes
                                    // returned by the API.
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
                                setListDataStatus(ERROR_NETWORK_FAILURE);
                            }
                            // All is well. Lets call super.run() which will trigger our runnable callback.
                        }
                    });
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

    public MutableLiveData<ArrayList<MovieReviewItem>> getReviewItems(final int id) {

        if (movieReviewItems == null) {
            movieReviewItems = new MutableLiveData<>();
        }

        // Let's instantiate a new interface, which will give us access
        // to a simple callback after retrofit has 'done its thing'.
        RunnableInterface reviewsRequestRunInterface = new RunnableInterface() {
            @Override
            public void onRunCompletion() {
                Log.v("REVIEWS", "onRunCompletion CALLBACK success.");
                // Note that we should never have a null value returned here.
                movieReviewItems.postValue(reviewItems);
            }
        };

        AppExecutors.getInstance().networkIO().execute(new CallbackRunnable(reviewsRequestRunInterface) {
            @Override
            public void run() {
                try {
                    Log.v(LOG_TAG, "REVIEWS: Attempting to get reviews from API.");
                    Response<MovieReviewsList> response =
                            theMovieDbApi.getMovieReviews(id, BuildConfig.API_KEY).execute();

                    // Let's make sure we have a response from the MDB API (via retrofit)
                    if (response.isSuccessful()) {
                        MovieReviewsList reviewsList = response.body();
                        if (reviewsList != null) {
                            Log.v(LOG_TAG, "REVIEWS: List is not null - proceed");
                            reviewItems = reviewsList.getResults();
                            Log.v(LOG_TAG, "REVIEWS: Total Results: " + reviewsList.getTotalResults());
                            // All is well. Lets call super.run() which will trigger our callback.
                            super.run();
                        } else {
                            // Parsing error.
                            Log.e(LOG_TAG, "Error parsing review items.");
                        }
                    } else {
                        // Something went wrong. Let's parse the error.
                        handleDetailRequestErrors(response.code());
                    }
                    // Catch any IO errors - likely there is no network access.
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Network failure: ", e);
                    if(!AppUtils.isInternetAvailable()) {
                        setDetailDataStatus(ERROR_UNAVAILABLE_OFFLINE);
                    }
                }

            }
        });
        return movieReviewItems;
    }

    public MutableLiveData<ArrayList<RelatedVideos>> getRelatedVideos(final int id) {
        if (relatedVideosLd == null) {
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
                            relatedVideos = null;
                        }
                        // Something went wrong. Let's parse the error.
                    } else {
                        handleDetailRequestErrors(response.code());
                    }

                    // Catch any IO errors - likely there is no network access.
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Network failure: ", e);
                    if(!AppUtils.isInternetAvailable()) {
                        setDetailDataStatus(ERROR_UNAVAILABLE_OFFLINE);
                    }
                }
            }
        });

        return relatedVideosLd;
    }

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

    private void commitItemsToDatabase(final String sortOrder) {
        if (movieItems != null) {
            if (sortOrder.equals(constants.SORT_ORDER_POPULAR)) {
                Log.i(LOG_TAG, "TEST: order EQUALS: " + constants.SORT_ORDER_POPULAR);

                // Create an array of current database objects to compare the newly  objects against.
                // Note we are getting a simplified object for the sake of efficiency
                List<MoviesItemSimple> oldMovieList = moviesDao.fetchSimpleListPopular();

                // Loop through all the newly retrieved movie items
                for (int i = 0; i < movieItems.size(); i++) {
                    Log.i(LOG_TAG, "TEST: Writing popular data item: " + i);

                    checkAndSetFavorites(oldMovieList, i);

                    // Set the new item as popular.
                    movieItems.get(i).setPopular((i + 1));

                    // Insert the movie to our database. See onConflictStrategy documentation (REPLACE):
                    // https://sqlite.org/lang_conflict.html
                    moviesDao.insertMovie(movieItems.get(i));
                }
                prefs.updatePopularDbRefreshTime(new Date(System.currentTimeMillis()).getTime());
            } else if (sortOrder.equals(constants.SORT_ORDER_HIGHRATED)) {
                Log.i(LOG_TAG, "TEST: order EQUALS: " + constants.SORT_ORDER_HIGHRATED);
                List<MoviesItemSimple> oldMovieList = moviesDao.fetchSimpleListHighRated();
                for (int i = 0; i < movieItems.size(); i++) {

                    Log.i(LOG_TAG, "TEST: Writing high rated data item: " + i);
                    checkAndSetFavorites(oldMovieList, i);

                    movieItems.get(i).setHighRated((i + 1));

                    moviesDao.insertMovie(movieItems.get(i));
                }
                prefs.updateHighRatedDbRefreshTime(new Date(System.currentTimeMillis()).getTime());
            }
        }
    }

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
                // Remove the object from our reference list of old entries. No point in
                // looping through the item unnecessarily.
                oldMovieList.remove(j);
                // Kill the current loop.
                break;
            }
        }
    }

    private void fetchItemsFromDatabase(final String sortOrder) {
        Log.i(LOG_TAG, "TEST: Retrieving items from database");
        AppExecutors.getInstance().diskIO().execute(new CallbackRunnable(new RunnableInterface() {
            @Override
            public void onRunCompletion() {
                if (!movieItems.isEmpty()) {
                    setListDataStatus(FETCH_COMPLETE);
                } else {
                    setListDataStatus(DATABASE_EMPTY);
                }
            }
        }) {
            @Override
            public void run() {
                if (sortOrder.equals(AppConstants.SORT_ORDER_POPULAR)) {
                    Log.i(LOG_TAG, "TEST: Retrieving items from database: POPULAR");
                    movieItems = (ArrayList<MovieItem>) moviesDao.loadPopularMovies();
                    setMovieItems();
                } else if (sortOrder.equals(AppConstants.SORT_ORDER_HIGHRATED)) {
                    Log.i(LOG_TAG, "TEST: Retrieving items from database: HIGH RATED");
                    movieItems = (ArrayList<MovieItem>) moviesDao.loadHighRatedMovies();
                    setMovieItems();
                } else {
                    movieItems = (ArrayList<MovieItem>) moviesDao.loadFavoriteMovies();
                    setMovieItems();
                } super.run();
            }
        });
    }

    private void setMovieItems() {
        AppExecutors.getInstance().mainThread().execute(new Runnable() {
            @Override
            public void run() {
                movieList.setValue(movieItems);
            }
        });
    }

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

    public MutableLiveData<DataStatus> getListDataStatus() {
        return listDataStatus;
    }

    private void setListDataStatus(final DataStatus status) {
        listDataStatus.postValue(status);
    }

    public MutableLiveData<DataStatus> getDetailDataStatus() {
        return detailDataStatus;
    }

    private void setDetailDataStatus(final DataStatus status) {
        detailDataStatus.postValue(status);
    }
}