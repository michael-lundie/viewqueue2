package io.lundie.michael.viewcue.network;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
//import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Simple class returning an instance of a retrofit object providing access to out API
 * Class initially used from: https://stackoverflow.com/a/45646202 to assist with debugging.
 */
public class ApiClient {
    private static Retrofit retrofit = null;

    public static Retrofit getClient(Gson gson){
        if(retrofit==null){
            retrofit = new Retrofit.Builder()
                    .baseUrl(TheMovieDbApi.HTTPS_THEMOVIEDB_API_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .client(new OkHttpClient.Builder().build())
                    .build();
        }
        return retrofit;
    }
}
