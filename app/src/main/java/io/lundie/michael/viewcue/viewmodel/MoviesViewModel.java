package io.lundie.michael.viewcue.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;

import io.lundie.michael.viewcue.datamodel.models.MovieItem;
import io.lundie.michael.viewcue.datamodel.MovieRepository;

public class MoviesViewModel extends ViewModel {

    private LiveData<ArrayList<MovieItem>> movieListObservable;

    // Getter method for fetching data
    public LiveData<ArrayList<MovieItem>> getMovies() {
        // Lets check to see if our results variable is null
        if (movieListObservable == null) {
            //movieListObservable = new LiveData<ArrayList<MovieItem>>();
            //Fetch Data Async from server.
            movieListObservable = MovieRepository.getInstance().getMovieList();
        }

        // Return our movies array list
        return movieListObservable;
    }
}
