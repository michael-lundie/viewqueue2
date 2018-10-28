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

import javax.inject.Inject;

import io.lundie.michael.viewcue.BuildConfig;
import io.lundie.michael.viewcue.datamodel.database.MoviesDao;
import io.lundie.michael.viewcue.datamodel.models.MovieItem;
import io.lundie.michael.viewcue.datamodel.models.MoviesList;
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

    /*// Declare our singleton variable of which we will get an instance
    private static MovieRepository movieRepository;*/

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

    private MutableLiveData<DataAcquireStatus> dataStatus = new MutableLiveData<>();

    ArrayList<MovieItem> movieItems;

    MoviesList moviesList;

    public MutableLiveData<ArrayList<MovieItem>> getMovieList(final String sortOrder , byte refreshCase) {
        switch(refreshCase) {
            case(MoviesViewModel.REFRESH_DATA):

                // Let's instantiate a new interface, which will give us access
                // to a simple callback after retrofit has 'done its thing'.
                RunnableInterface runnableInterface = new RunnableInterface() {
                    @Override
                    public void complete() {
                        Log.i("CALLBACK", "TEST: called complete.");
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

                // Check to see if the refresh database limit has been passed. If so, we want to fetch
                // data from our API and update everything. NOTE: refresh time is saved in a preference.
                // If we don't need to refresh, we'll grab everything from the database.
                if (hasInvalidRefreshTime(sortOrder)) {
                    Log.i(LOG_TAG, "TEST: Has invalid refresh time.");

                    // Get an instance of AppExecutors. We will run retrofit in synchronous mode so
                    // we can micromanage some error handling / callbacks.
                    AppExecutors.getInstance().networkIO().execute(new CallbackRunnable(runnableInterface) {
                        @Override
                        public void run() {
                            try {
                                Log.i(LOG_TAG, "TEST: Attempting to get movies from API.");
                                Response<MoviesList> response =
                                        theMovieDbApi.getListOfMovies(sortOrder, BuildConfig.API_KEY).execute();

                                // Let's make sure we have a response from the MDB API (via retrofit)
                                if(response.isSuccessful()) {
                                    moviesList = response.body();
                                    if(moviesList != null) {
                                        Log.i(LOG_TAG, "TEST: Movie list not null");
                                        movieItems = moviesList.getResults();
                                    } else {
                                        setDataAcquireStatus(ERROR_PARSING);
                                    }
                                // Something went wrong. Let's parse the error.
                                } else {
                                    switch (response.code()) {
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

                // Refresh limit hasn't passed, so we're going to return everything from our database.
                } else {
                    Log.i(LOG_TAG, "TEST: Returning movie items: ");
                    setDataAcquireStatus(FETCHING_FROM_DATABASE);
                    fetchItemsFromDatabase(sortOrder);
                }
                break;

            // This case exists primarily to prevent get movies from being called, when we unlist
            // any LiveData observables. This won't be required once we are using singleton observers.
            case(MoviesViewModel.DO_NOT_REFRESH_DATA):
                break;
        }
        return movieList;
    }

    private void commitItemsToDatabase(final String sortOrder) {

        if(sortOrder.equals(constants.SORT_ORDER_POPULAR)) {
            Log.i(LOG_TAG, "TEST: order EQUALS: " + constants.SORT_ORDER_POPULAR);
            for (int i = 0; i < movieItems.size(); i++) {
                Log.i(LOG_TAG, "TEST: Writing popular data item: " + i);
                movieItems.get(i).setPopular((i+1));
                moviesDao.insertMovie(movieItems.get(i));
            }
            prefs.updatePopularDbRefreshTime(new Date(System.currentTimeMillis()).getTime());
        } else if (sortOrder.equals(constants.SORT_ORDER_HIGHRATED)) {
            Log.i(LOG_TAG, "TEST: order EQUALS: " + constants.SORT_ORDER_HIGHRATED);
            for (int i = 0; i < movieItems.size(); i++) {
                Log.i(LOG_TAG, "TEST: Writing high rated data item: " + i);
                movieItems.get(i).setHighRated((i+1));
                moviesDao.insertMovie(movieItems.get(i));
            }
            prefs.updateHighRatedDbRefreshTime(new Date(System.currentTimeMillis()).getTime());
        }
    }

    private void fetchItemsFromDatabase(final String sortOrder) {
        Log.i(LOG_TAG, "TEST: Retrieving items from database");
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (sortOrder.equals(constants.SORT_ORDER_POPULAR)) {
                    Log.i(LOG_TAG, "TEST: Retrieving items from database: POPULAR");
                    movieItems = (ArrayList<MovieItem>) moviesDao.loadPopularMovies();
                    setMovieItems();
                } else if (sortOrder.equals(constants.SORT_ORDER_HIGHRATED)) {
                    Log.i(LOG_TAG, "TEST: Retrieving items from database: HIGH RATED");
                    movieItems = (ArrayList<MovieItem>) moviesDao.loadHighRatedMovies();
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
        AppExecutors.getInstance().mainThread().execute(new Runnable() {
            @Override
            public void run() {
                dataStatus.setValue(status);
            }
        });
    }
}