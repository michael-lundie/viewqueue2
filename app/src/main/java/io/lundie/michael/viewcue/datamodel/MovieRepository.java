/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 27/09/18 22:45
 */
package io.lundie.michael.viewcue.datamodel;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;


import javax.inject.Inject;

import io.lundie.michael.viewcue.BuildConfig;
import io.lundie.michael.viewcue.datamodel.database.MoviesDao;
import io.lundie.michael.viewcue.datamodel.models.MovieItem;
import io.lundie.michael.viewcue.datamodel.models.MoviesList;
import io.lundie.michael.viewcue.utilities.AppExecutors;
import io.lundie.michael.viewcue.utilities.CallbackRunnable;
import io.lundie.michael.viewcue.utilities.NetworkStatus;
import io.lundie.michael.viewcue.utilities.Prefs;
import io.lundie.michael.viewcue.utilities.AppConstants;
import io.lundie.michael.viewcue.utilities.RunnableInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

    private MutableLiveData<NetworkStatus> networkStatus = new MutableLiveData<>();

    ArrayList<MovieItem> movieItems;

    MoviesList moviesList;


    public MutableLiveData<ArrayList<MovieItem>> getMovieList(final String sortOrder) {

        // Let's check to see if the refresh time is over the defined limit.
        if (hasInvalidRefreshTime(sortOrder)) {

            Log.i(LOG_TAG, "TEST: Refresh is 0  over limit");
            // We are over the refresh limit so we will fetch new movie results from the API and
            // update our list of movies in the database using our MoviesDao.

            theMovieDbApi.getListOfMovies(sortOrder, BuildConfig.API_KEY).enqueue(new Callback<MoviesList>() {
                @Override
                public void onResponse(Call<MoviesList> call, Response<MoviesList> response) {

                        RunnableInterface runnableInterface = new RunnableInterface() {
                            @Override
                            public void complete() {
                                setNetworkStatus(NetworkStatus.LOAD_COMPLETE);
                                Log.i(LOG_TAG, "Interface returned complete.");

                                AppExecutors.getInstance().mainThread().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        movieList.setValue(movieItems);
                                    }
                                });

                                commitItemsToDatabase(sortOrder);
                            }
                        };
                        parseMovieListResults(response, runnableInterface);
                }

                @Override
                public void onFailure(Call<MoviesList> call, Throwable t) {
                    Log.e(LOG_TAG, "TEST: Json: Failed", t);
                    setNetworkStatus(NetworkStatus.ERROR);
                }
            });
        } else {
            // Display the offline data from our database
            fetchItemsFromDatabase(sortOrder);
        }
        Log.i(LOG_TAG, "TEST: Returning movie items: ");
        return movieList;
    }

    private void commitItemsToDatabase(final String sortOrder) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Log.i(LOG_TAG, "TEST: Getting App Executors");
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
        });
    }

    private void fetchItemsFromDatabase(final String sortOrder) {
        Log.i(LOG_TAG, "TEST: Retrieving items from database");
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (sortOrder.equals(constants.SORT_ORDER_POPULAR)) {
                    Log.i(LOG_TAG, "TEST: Retrieving items from database: POPULAR");
                    movieItems = (ArrayList<MovieItem>) moviesDao.loadPopularMovies();
                    AppExecutors.getInstance().mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(LOG_TAG, "TEST - movieItems: " + movieItems);
                            movieList.setValue(movieItems);
                        }
                    });
                } else if (sortOrder.equals(constants.SORT_ORDER_HIGHRATED)) {
                    Log.i(LOG_TAG, "TEST: Retrieving items from database: HIGH RATED");
                    movieItems = (ArrayList<MovieItem>) moviesDao.loadHighRatedMovies();
                    AppExecutors.getInstance().mainThread().execute(new Runnable() {
                        @Override
                        public void run() {
                            Log.i(LOG_TAG, "TEST - movieItems: " + movieItems);
                            movieList.setValue(movieItems);
                        }
                    });
                }
            }
        });
    }

    private void parseMovieListResults(final Response<MoviesList> response, RunnableInterface runInterface) {
        Runnable myRunnable = new Runnable() {
            @Override
            public void run() {
                Log.i(LOG_TAG, "TEST: Parsing results");
                movieItems = response.body().getResults();
            }
        };

        AppExecutors.getInstance().diskIO().execute(new CallbackRunnable(myRunnable, runInterface));
    }

    private ArrayList<MovieItem> parseMovieItems(Response<MoviesList> response) {
        Log.i(LOG_TAG, "TEST: INITIAL Response: " + response);
        MoviesList moviesList = response.body();
        Log.i(LOG_TAG, "TEST: NEXT Response: " + response.body());
        return moviesList.getResults();
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

    public MutableLiveData<NetworkStatus> getNetworkStatusLiveData() {
        return networkStatus;
    }

    private void setNetworkStatus(final NetworkStatus status) {
        AppExecutors.getInstance().mainThread().execute(new Runnable() {
            @Override
            public void run() {
                networkStatus.setValue(status);
            }
        });
    }
}