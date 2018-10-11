/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 08/10/18 15:13
 */

package io.lundie.michael.viewcue.injection;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.lundie.michael.viewcue.datamodel.MovieRepository;
import io.lundie.michael.viewcue.datamodel.TheMovieDbApi;
import io.lundie.michael.viewcue.datamodel.database.MoviesDao;
import io.lundie.michael.viewcue.datamodel.database.MoviesDatabase;
import io.lundie.michael.viewcue.utilities.AppExecutors;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Tutorial followed from:
 * https://blog.mindorks.com/the-new-dagger-2-android-injector-cbe7d55afa6a
 */
@Module(includes = ViewModelModule.class)
public class AppModule {

    // Database Injection
    @Provides
    @Singleton
    MoviesDatabase provideDatabase(Application application) {
        return Room.databaseBuilder(application,
                MoviesDatabase.class, "movies")
                .build();
    }

    @Provides
    @Singleton
    MoviesDao provideMoviesDao(MoviesDatabase database) { return database.moviesDao(); }

    // Repo Injection

    @Provides
    Executor provideExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    @Provides
    @Singleton
    MovieRepository provideMovieRepository(MoviesDatabase moviesDatabase, MoviesDao moviesDao, Executor executor) {
        return new MovieRepository(moviesDatabase, moviesDao, executor);
    }
}
