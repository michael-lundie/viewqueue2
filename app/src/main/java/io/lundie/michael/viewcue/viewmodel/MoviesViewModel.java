package io.lundie.michael.viewcue.viewmodel;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import java.util.ArrayList;

import io.lundie.michael.viewcue.datamodel.models.MovieItem;
import io.lundie.michael.viewcue.datamodel.MovieRepository;

public class MoviesViewModel extends ViewModel {

    private MutableLiveData<ArrayList<MovieItem>> movieListObservable = new MutableLiveData<>();
    private MutableLiveData<MovieItem> selectedMovieItem = new MutableLiveData<>();

    // Getter method for fetching data
    public LiveData<ArrayList<MovieItem>> getMovies(String sortOrder) {
        Log.i("TEST", "ViewModel get movies called");

        //movieListObservable = new LiveData<ArrayList<MovieItem>>();
        //Fetch Data Async from server.

        movieListObservable = MovieRepository.getInstance().getMovieList(sortOrder);
        // Return our movies array list
        return movieListObservable;
    }

    public LiveData<MovieItem> getSelectedItem() {
        return selectedMovieItem;
    }

    public void selectMovieItem(MovieItem item) {
        selectedMovieItem.setValue(item);
    }
}