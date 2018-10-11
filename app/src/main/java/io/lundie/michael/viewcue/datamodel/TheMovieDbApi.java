package io.lundie.michael.viewcue.datamodel;

import io.lundie.michael.viewcue.datamodel.models.MoviesList;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TheMovieDbApi {

    String HTTPS_THEMOVIEDB_API_URL = "https://api.themoviedb.org/3/";

    // Let's define some end points for our base URL.
    // NOTE: This replaces the previously implemented queryUrlBuilder method in QueryUtils

    // End point for retrieving a list of movies with a variable sort_order path
    @GET("movie/{sort_order}")
    Call<MoviesList> getListOfMovies(@Path("sort_order") String sortOrder,
                                     @Query("api_key") String apiKey);


}
