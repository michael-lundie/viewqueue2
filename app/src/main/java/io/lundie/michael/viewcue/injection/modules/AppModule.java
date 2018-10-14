/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 08/10/18 15:13
 */

package io.lundie.michael.viewcue.injection.modules;

import android.app.Application;
import android.arch.persistence.room.Room;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.lundie.michael.viewcue.datamodel.MovieRepository;
import io.lundie.michael.viewcue.datamodel.TheMovieDbApi;
import io.lundie.michael.viewcue.datamodel.database.MoviesDao;
import io.lundie.michael.viewcue.datamodel.database.MoviesDatabase;
import io.lundie.michael.viewcue.utilities.Prefs;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Tutorial followed from:
 * https://blog.mindorks.com/the-new-dagger-2-android-injector-cbe7d55afa6a
 */
@Module(includes = {ViewModelModule.class,
                    SharedPreferencesModule.class})
public class AppModule {

    // Preference Injection
    @Provides
    @Singleton
    Prefs providePrefs(Application application, SharedPreferences sharedPrefs) {
        return new Prefs(application, sharedPrefs);
    }

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
    @Singleton
    MovieRepository provideMovieRepository(TheMovieDbApi theMovieDbApi, MoviesDao moviesDao) {
        return new MovieRepository(theMovieDbApi, moviesDao);
    }

    // API Injection
    @Provides
    Gson provideGson() { return new GsonBuilder().create(); }

    @Provides
    Retrofit provideRetrofit(Gson gson) {
        return new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(TheMovieDbApi.HTTPS_THEMOVIEDB_API_URL)
                .build();
    }

    @Provides
    @Singleton
    TheMovieDbApi provideApiService(Retrofit retrofit) {
        return retrofit.create(TheMovieDbApi.class);
    }
}