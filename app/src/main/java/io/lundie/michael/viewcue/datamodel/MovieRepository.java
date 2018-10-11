/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 27/09/18 22:45
 */
package io.lundie.michael.viewcue.datamodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.android.ContributesAndroidInjector;
import io.lundie.michael.viewcue.BuildConfig;
import io.lundie.michael.viewcue.datamodel.database.MoviesDao;
import io.lundie.michael.viewcue.datamodel.database.MoviesDatabase;
import io.lundie.michael.viewcue.datamodel.models.MovieItem;
import io.lundie.michael.viewcue.datamodel.models.MoviesList;
import io.lundie.michael.viewcue.utilities.AppExecutors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MovieRepository {

    private final TheMovieDbApi theMovieDbApi;
    private final Executor appExecutors;
    private final MoviesDatabase moviesDatabase;
    private final MoviesDao moviesDao;
    private Context mContext;

    /*// Declare our singleton variable of which we will get an instance
    private static MovieRepository movieRepository;*/

    // Add requirement for client in method params, and use api.getClient.
    @Inject
    public MovieRepository(MoviesDatabase moviesDatabase, MoviesDao moviesDao, Executor appExecutors) {
        this.moviesDatabase = moviesDatabase;
        this.moviesDao = moviesDao;
        this.appExecutors = appExecutors;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(TheMovieDbApi.HTTPS_THEMOVIEDB_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        theMovieDbApi = retrofit.create(TheMovieDbApi.class);
    }

    private MutableLiveData<ArrayList<MovieItem>> movieList = new MutableLiveData<>();

    public MutableLiveData<ArrayList<MovieItem>> getMovieList(String sortOrder) {

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

    private void refreshMoviesDatabase(final String sortOrder) {

        theMovieDbApi.getListOfMovies(sortOrder, BuildConfig.API_KEY).enqueue(new Callback<MoviesList>() {
            @Override
            public void onResponse(Call<MoviesList> call, Response<MoviesList> response) {

                // Now let's set out list to out MutableLiveData
                // Keep in mind this is a singleton object instance
                //movieList.setValue(response.body());

                Log.i("LOG", "TEST: Json: " + response.body());

                movieItems = parseMovieItems(response);
            }

            @Override
            public void onFailure(Call<MoviesList> call, Throwable t) {
                Log.e("LOG", "TEST: Json: Failed", t);
            }
        });

        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < movieItems.size(); i++) {
                    movieItems.get(i).getId();
                    // insert using database
                }
            }
        });
    }

    private ArrayList<MovieItem> parseMovieItems(Response<MoviesList> response) {
        MoviesList moviesList = response.body();
        return moviesList.getResults();
    }
}