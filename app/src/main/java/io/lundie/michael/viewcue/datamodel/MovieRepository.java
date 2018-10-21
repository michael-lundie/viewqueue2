/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 27/09/18 22:45
 */
package io.lundie.michael.viewcue.datamodel;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;


import javax.inject.Inject;

import io.lundie.michael.viewcue.BuildConfig;
import io.lundie.michael.viewcue.datamodel.database.MoviesDao;
import io.lundie.michael.viewcue.datamodel.models.MovieItem;
import io.lundie.michael.viewcue.datamodel.models.MoviesList;
import io.lundie.michael.viewcue.utilities.AppExecutors;
import io.lundie.michael.viewcue.utilities.Prefs;
import io.lundie.michael.viewcue.utilities.AppConstants;
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

    public MutableLiveData<ArrayList<MovieItem>> temp(String sortOrder) {

        theMovieDbApi.getListOfMovies(sortOrder, BuildConfig.API_KEY).enqueue(new Callback<MoviesList>() {
            @Override
            public void onResponse(Call<MoviesList> call, Response<MoviesList> response) {

                // Now let's set out list to out MutableLiveData
                // Keep in mind this is a singleton object instance
                //movieList.setValue(response.body());

                Log.i("LOG", "TEST: Json: " + response.body());

                movieList.setValue(parseMovieItems(response));
            }

            @Override
            public void onFailure(Call<MoviesList> call, Throwable t) {
                Log.e("LOG", "TEST: Json: Failed", t);
            }
        });
        return movieList;
    }


    ArrayList<MovieItem> movieItems;

    public MutableLiveData<ArrayList<MovieItem>> getMovieList(final String sortOrder) {

        // Let's check to see if the refresh time is over the defined limit.
        if (isRefreshOverLimit()) {

            Log.i(LOG_TAG, "TEST: Refresh is 0  over limit");
            // We are over the refresh limit so we will fetch new movie results from the API and
            // update our list of movies in the database using our MoviesDao.

            theMovieDbApi.getListOfMovies(sortOrder, BuildConfig.API_KEY).enqueue(new Callback<MoviesList>() {
                @Override
                public void onResponse(Call<MoviesList> call, Response<MoviesList> response) {
                    Log.i(LOG_TAG, "TEST: getting list of movies from API");
                    // Now let's get an Array of movies to work with
                    Log.i(LOG_TAG, "TEST: Json: " + response.body());
                    movieItems = parseMovieItems(response);

                    Log.i(LOG_TAG, "TEST: Sending list of items to UI.");
                    movieList.setValue(movieItems);

                    sendItemsToDatabase(sortOrder);
                }

                @Override
                public void onFailure(Call<MoviesList> call, Throwable t) {
                    Log.e(LOG_TAG, "TEST: Json: Failed", t);
                }
            });
        } else {
            // Display the offline data from our database
            fetchItemsFromDatabase(sortOrder);
        }
        Log.i(LOG_TAG, "TEST: Returning movie items: " + movieList);
        return movieList;
    }

    private void sendItemsToDatabase(final String sortOrder) {
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                Log.i(LOG_TAG, "TEST: Getting App Executors");
                if(sortOrder.equals(constants.SORT_ORDER_POPULAR)) {
                    Log.i(LOG_TAG, "TEST: order EQUALS: " + constants.SORT_ORDER_POPULAR);
                    for (int i = 0; i < movieItems.size(); i++) {
                        Log.i(LOG_TAG, "TEST: Writing popular data item: " + i);
                        movieItems.get(i).setPopular(i);
                        moviesDao.insertMovie(movieItems.get(i));
                    }
                } else if (sortOrder.equals(constants.SORT_ORDER_HIGHRATED)) {
                    Log.i(LOG_TAG, "TEST: order EQUALS: " + constants.SORT_ORDER_HIGHRATED);
                    for (int i = 0; i < movieItems.size(); i++) {
                        Log.i(LOG_TAG, "TEST: Writing high rated data item: " + i);
                        movieItems.get(i).setHighRated(i);
                        moviesDao.insertMovie(movieItems.get(i));
                    }
                }
                //TODO: Check if Successful
                prefs.updateDbRefreshTime(new Date(System.currentTimeMillis()).getTime());
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

    private ArrayList<MovieItem> parseMovieItems(Response<MoviesList> response) {
        Log.i(LOG_TAG, "TEST: INITIAL Response: " + response);
        MoviesList moviesList = response.body();
        Log.i(LOG_TAG, "TEST: NEXT Response: " + response.body());
        return moviesList.getResults();
    }

    private boolean isRefreshOverLimit() {
        long lastRefreshTime = prefs.getRefreshTime();

        Date date = new Date(System.currentTimeMillis());
        long currentTimeMs = date.getTime();

        if ((currentTimeMs - lastRefreshTime) > 240_000 || lastRefreshTime == 0) {
            return true;
        } return false;
    }
}