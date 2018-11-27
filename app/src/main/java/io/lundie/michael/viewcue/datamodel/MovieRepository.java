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
import io.lundie.michael.viewcue.utilities.CallbackRunnable;
import io.lundie.michael.viewcue.utilities.DataAcquireStatus;
import io.lundie.michael.viewcue.utilities.Prefs;
import io.lundie.michael.viewcue.utilities.AppConstants;
import io.lundie.michael.viewcue.utilities.RunnableInterface;
import io.lundie.michael.viewcue.viewmodel.MoviesViewModel;
import retrofit2.Response;

import static io.lundie.michael.viewcue.utilities.DataAcquireStatus.ERROR_NETWORK_FAILURE;
import static io.lundie.michael.viewcue.utilities.DataAcquireStatus.ERROR_NOT_FOUND;
import static io.lundie.michael.viewcue.utilities.DataAcquireStatus.ERROR_PARSING;
import static io.lundie.michael.viewcue.utilities.DataAcquireStatus.ERROR_SERVER_BROKEN;
import static io.lundie.michael.viewcue.utilities.DataAcquireStatus.ERROR_UNKNOWN;
import static io.lundie.michael.viewcue.utilities.DataAcquireStatus.FETCHING_FROM_DATABASE;
import static io.lundie.michael.viewcue.utilities.DataAcquireStatus.FETCH_COMPLETE;

public class MovieRepository {

    private static final String LOG_TAG = MovieRepository.class.getSimpleName();

    private final AppConstants constants;
    private final TheMovieDbApi theMovieDbApi;
    private final MoviesDao moviesDao;
    private final Prefs prefs;

    // Add requirement for client in method params, and use api.getClient.
    @Inject
    public MovieRepository(TheMovieDbApi theMovieDbApi,
                           MoviesDao moviesDao, Prefs prefs, AppConstants constants) {
        this.moviesDao = moviesDao;
        this.theMovieDbApi = theMovieDbApi;
        this.prefs = prefs;
        this.constants = constants;
    }

    private MutableLiveData<ArrayList<MovieItem>> movieList = new MutableLiveData<>();

    private MutableLiveData<ArrayList<MovieReviewItem>> movieReviewItems;

    private MutableLiveData<ArrayList<RelatedVideos>> relatedVideosLd;

    private MutableLiveData<DataAcquireStatus> dataStatus = new MutableLiveData<>();

    ArrayList<MovieItem> movieItems;

    ArrayList<MovieReviewItem> reviewItems;

    ArrayList<RelatedVideos> relatedVideos;

    public MutableLiveData<ArrayList<MovieItem>> getMovieList(final String sortOrder ,
                                                              byte refreshCase) {

        switch(refreshCase) {
            case(MoviesViewModel.REFRESH_DATA):

                // Let's instantiate a new interface, which will give us access
                // to a simple callback after retrofit has 'done its thing'.
                RunnableInterface listRequestRunInterface = new RunnableInterface() {
                    @Override
                    public void onRunCompletion() {
                        Log.i("CALLBACK", "TEST: called onRunCompletion.");
                        setDataAcquireStatus(FETCH_COMPLETE);
                        AppExecutors.getInstance().mainThread().execute(new Runnable() {
                            @Override
                            public void run() {
                                movieList.setValue(movieItems);
                            }
                        });
                        commitItemsToDatabase(sortOrder);
                    }
                };

                // If our sort order is set to favorites, we want to return directly from the database.
                if(sortOrder.equals(AppConstants.SORT_ORDER_FAVS)) {
                    //TODO: Add null value here to account for no favs
                    fetchItemsFromDatabase(sortOrder);

                } else if (hasInvalidRefreshTime(sortOrder)) {

                // Check to see if the refresh database limit has been passed. If so, we want to fetch
                // data from our API and update everything. NOTE: refresh time is saved in a preference.
                // If we don't need to refresh, we'll grab everything from the database.

                    Log.i(LOG_TAG, "TEST: Has invalid refresh time.");

                    // Get an instance of AppExecutors. We will run retrofit in synchronous mode so
                    // we can micromanage some error handling / callbacks.
                    AppExecutors.getInstance().networkIO().execute(new CallbackRunnable(listRequestRunInterface) {
                        @Override
                        public void run() {
                            try {
                                Log.i(LOG_TAG, "TEST: Attempting to get movies from API.");
                                Response<MoviesList> response =
                                        theMovieDbApi.getListOfMovies(sortOrder, BuildConfig.API_KEY).execute();

                                // Let's make sure we have a response from the MDB API (via retrofit)
                                if(response.isSuccessful()) {
                                    MoviesList moviesList = response.body();
                                    if(moviesList != null) {
                                        Log.i(LOG_TAG, "TEST: Movie list not null");
                                        movieItems = moviesList.getResults();
                                    } else {
                                        setDataAcquireStatus(ERROR_PARSING);
                                    }

                                // Something went wrong. Let's parse the error.
                                } else {
                                    handleRequestErrors(response.code());
                                }

                            // Catch any IO errors - likely there is no network access.
                            } catch (IOException e) {
                                //TODO: Check if phone is offline. Inform the user of the problem.
                                Log.e(LOG_TAG, "Network failure: ", e);
                                setDataAcquireStatus(ERROR_NETWORK_FAILURE);
                            }

                            // All is well. Lets call super.run() which will trigger our callback.
                            super.run();
                        }
                    });
                    break;
                }

                // NOTE that to avoid code repetition here, as opposed to an else condition for the
                // above code, we are just running into the next case (no break statement).

            case (MoviesViewModel.REFRESH_DATABASE):
                Log.i(LOG_TAG, "TEST: Returning movie items: ");
                setDataAcquireStatus(FETCHING_FROM_DATABASE);
                fetchItemsFromDatabase(sortOrder);
                break;

            // The following case exists primarily to prevent getMovies from being called, when we
            // unlist any LiveData observables.
            // Hopefully, this won't be required once we are using singleton observers.
            case(MoviesViewModel.DO_NOT_REFRESH_DATA):
                break;
        }
        return movieList;
    }

    public MutableLiveData<ArrayList<MovieReviewItem>> getReviewItems(final int id) {

        Log.v(LOG_TAG, "TEST: Fetching review items.");

        if(movieReviewItems == null) { movieReviewItems = new MutableLiveData<>(); }

        // Let's instantiate a new interface, which will give us access
        // to a simple callback after retrofit has 'done its thing'.
        RunnableInterface reviewsRequestRunInterface = new RunnableInterface() {
            @Override
            public void onRunCompletion() {
                Log.i("CALLBACK", "TEST: called onRunCompletion.");
                setDataAcquireStatus(FETCH_COMPLETE);
                movieReviewItems.postValue(reviewItems);
                Log.v(LOG_TAG, "REVIEWS: Review items are:");
                for (int i = 0; i < reviewItems.size(); i++) {
                    MovieReviewItem item = reviewItems.get(i);
                    Log.v(LOG_TAG, "REVIEWS... " + item.getContent());
                }
            }
        };

        AppExecutors.getInstance().networkIO().execute(new CallbackRunnable(reviewsRequestRunInterface) {
            @Override
            public void run() {
                try {
                    Log.i(LOG_TAG, "TEST: Attempting to get movies from API.");
                    Response<MovieReviewsList> response =
                            theMovieDbApi.getMovieReviews(id, BuildConfig.API_KEY).execute();

                    // Let's make sure we have a response from the MDB API (via retrofit)
                    if(response.isSuccessful()) {
                        MovieReviewsList reviewsList = response.body();
                        if(reviewsList != null) {
                            Log.i(LOG_TAG, "REVIEWS: Movie list not null");
                            reviewItems = reviewsList.getResults();
                            Log.v(LOG_TAG, "REVIEWS: Results: " + reviewsList.getTotalResults());
                        } else {
                            setDataAcquireStatus(ERROR_PARSING);
                        }
                        // Something went wrong. Let's parse the error.
                    } else {
                        handleRequestErrors(response.code());
                    }

                    // Catch any IO errors - likely there is no network access.
                } catch (IOException e) {
                    //TODO: Check if phone is offline. Inform the user of the problem.
                    Log.e(LOG_TAG, "Network failure: ", e);
                    setDataAcquireStatus(ERROR_NETWORK_FAILURE);
                }
                // All is well. Lets call super.run() which will trigger our callback.
                super.run();
            }
        });
        return movieReviewItems;
    }

    public MutableLiveData<ArrayList<RelatedVideos>> getRelatedVideos(final int id) {

        Log.v(LOG_TAG, "TEST: Fetching review items.");

        if(relatedVideosLd == null) { relatedVideosLd = new MutableLiveData<>(); }

        // Let's instantiate a new interface, which will give us access
        // to a simple callback after retrofit has 'done its thing'.
        RunnableInterface relatedItemsRequestRunInterface = new RunnableInterface() {
            @Override
            public void onRunCompletion() {
                relatedVideosLd.postValue(relatedVideos);
                Log.v(LOG_TAG, "RELATED: Review items are:");
                for (int i = 0; i < relatedVideos.size(); i++) {
                    RelatedVideos item = relatedVideos.get(i);
                    Log.v(LOG_TAG, "RELATED... " + item.getName());
                }
            }
        };

        AppExecutors.getInstance().networkIO().execute(new CallbackRunnable(relatedItemsRequestRunInterface) {
            @Override
            public void run() {
                try {
                    Log.i(LOG_TAG, "TEST: Attempting to get movies from API.");
                    Response<RelatedVideosList> response =
                            theMovieDbApi.getRelatedVideos(id, BuildConfig.API_KEY).execute();

                    // Let's make sure we have a response from the MDB API (via retrofit)
                    if(response.isSuccessful()) {
                        RelatedVideosList relatedVideosList = response.body();
                        if(relatedVideosList != null) {
                            Log.i(LOG_TAG, "REVIEWS: Movie list not null");
                            relatedVideos = relatedVideosList.getResults();
                        } else {
                            relatedVideos = null;
                        }
                        // Something went wrong. Let's parse the error.
                    } else {
                        handleRequestErrors(response.code());
                    }

                    // Catch any IO errors - likely there is no network access.
                } catch (IOException e) {
                    //TODO: Check if phone is offline. Inform the user of the problem.
                    Log.e(LOG_TAG, "Network failure: ", e);
                }
                // All is well. Lets call super.run() which will trigger our callback.
                super.run();
            }
        });
        return relatedVideosLd;
    }

    private void handleRequestErrors(int responseCode) {
        switch (responseCode) {
            case 404:
                setDataAcquireStatus(ERROR_NOT_FOUND);
                break;
            case 500:
                setDataAcquireStatus(ERROR_SERVER_BROKEN);
                break;
            default:
                setDataAcquireStatus(ERROR_UNKNOWN);
                break;
        }
    }

    private void commitItemsToDatabase(final String sortOrder) {

        if(sortOrder.equals(constants.SORT_ORDER_POPULAR)) {
            Log.i(LOG_TAG, "TEST: order EQUALS: " + constants.SORT_ORDER_POPULAR);

            // Create an array of current database objects to compare the newly  objects against.
            // Note we are getting a simplified object for the sake of efficiency
            List<MoviesItemSimple> oldMovieList = moviesDao.fetchSimpleListPopular();

            // Loop through all the newly retrieved movie items
            for (int i = 0; i < movieItems.size(); i++) {
                Log.i(LOG_TAG, "TEST: Writing popular data item: " + i);

                checkAndSetFavorites(oldMovieList, i);

                // Set the new item as popular.
                movieItems.get(i).setPopular((i+1));

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

                movieItems.get(i).setHighRated((i+1));

                moviesDao.insertMovie(movieItems.get(i));
            }
            prefs.updateHighRatedDbRefreshTime(new Date(System.currentTimeMillis()).getTime());
        }
    }

    private void checkAndSetFavorites(List<MoviesItemSimple> oldMovieList, int i) {
        // Get the item ID for the newly retrieved movie object.
        int newItemId = movieItems.get(i).getId();

        // Loop through the old list of items to check if any of them were favourites.
        for (int j = 0; j < oldMovieList.size(); j++) {
            // Check the matching object (if it exists)
            if (newItemId == oldMovieList.get(j).getId() ) {
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
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
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
                }
            }
        });
    }

    private void setMovieItems() {
        AppExecutors.getInstance().mainThread().execute(new Runnable() {
            @Override
            public void run() {
                Log.i(LOG_TAG, "TEST - movieItems: " + movieItems);
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
        } return false;
    }

    public MutableLiveData<DataAcquireStatus> getDataAcquireStatusLiveData() {
        return dataStatus;
    }

    private void setDataAcquireStatus(final DataAcquireStatus status) {
        dataStatus.postValue(status);

    }
}