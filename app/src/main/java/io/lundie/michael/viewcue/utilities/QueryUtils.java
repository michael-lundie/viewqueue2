package io.lundie.michael.viewcue.utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

import io.lundie.michael.viewcue.MovieItem;

public class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     */
    private QueryUtils() {
    }

    /**
     * Method for building our query URL
     * @param context The current activity context.
     * @return url string
     */
    public static String queryUrlBuilder (Context context) {
        final String API_AUTHORITY = "api.themoviedb.org";
        final String API_VERSION = "3";
        final String API_DISCOVER_PATH = "discover";
        final String API_MOVIE_PATH = "movie";
        final String API_SORT_PARAM = "sort_by";
        final String API_SORT_POPULAR_VALUE = "popularity.desc";
        final String API_SORT_RATED_VALUE = "vote_average.desc";
        final String API_ADULT_PARAM = "include_adult";
        final String API_ADULT_VALUE = "false";
        final String API_VIDEO_PARAM = "include_video";
        final String API_VIDEO_VALUE = "false";


        final String API_KEY_PARAM = "api_key";
        // TODO: Replace with preference request
        final String API_KEY = "";
        final String testURL = "";

//Use URL builder to construct our URL
        Uri.Builder query = new Uri.Builder();
        query.scheme("https")
                .authority(API_AUTHORITY)
                .appendPath(API_VERSION)
                .appendPath(API_DISCOVER_PATH)
                .appendPath(API_MOVIE_PATH)
                .appendQueryParameter(API_KEY_PARAM, API_KEY)
                .appendQueryParameter(API_SORT_PARAM, API_SORT_POPULAR_VALUE)
                .appendQueryParameter(API_ADULT_PARAM, API_ADULT_VALUE)
                .appendQueryParameter(API_VIDEO_PARAM, API_VIDEO_VALUE)
                .build();
        URL returnUrl = null;

        //Attempt to return our URL, check for exception, then convert to String on return.
        try {
            returnUrl = new URL(query.toString());
        } catch (MalformedURLException e) {
            //We'll do further checking in AsyncLoader, but perhaps it's nice to check for
            //any initial errors.
            Log.e(LOG_TAG, "There is a problem with URL construction.", e);
        }
        //Handle any null pointer exception that may be thrown by .toString() method;
        if (returnUrl == null) {
            Log.i(LOG_TAG, "URL returned null.");
            return null;
        }
        Log.i(LOG_TAG, "TEST: Query url: " + returnUrl.toString());
        return returnUrl.toString();
    }

    /**
     * Query themoviedb.org API and return an {@link ArrayList<MovieItem>} object to represent a.
     * list of movies
     * @param requestUrl the URL for our API data request
     * @return parsed JSON query results (as a MovieItem object)
     */
    public static ArrayList<MovieItem> fetchQueryResults(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error closing input stream", e);
        }

        // Extract relevant fields from the JSON response and return a List<NewsItem> object
        return extractMovieResults(jsonResponse);
    }

    /**
     * Checks to make sure the smart phone has access to the internet.
     * @param context the application context
     * @return boolean
     */
    public static boolean checkNetworkAccess(Context context) {
        ConnectivityManager connMgr =
                (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Check the connectivity manager is not null first to avoid NPE.
        if (connMgr != null) {
            NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
            // Returns true or false depending on connectivity status.
            return networkInfo != null && networkInfo.isConnectedOrConnecting();
        }
        //Connectivity manager is null so returning false.
        return false;
    }

    /**
     * Returns new URL object from the given string URL.
     */
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error with creating URL ", e);
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If the URL is null, then return early.
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // If the request was successful (response code 200),
            // then read the input stream and parse the response.
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, "Error response code: " + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "Problem retrieving themoviedb.org JSON results.", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                inputStream.close();
            }
        }
        Log.i(LOG_TAG, "TEST: json: " + jsonResponse);
        return jsonResponse;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, Charset.forName("UTF-8"));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    /**
     * Return a list of {@link MovieItem} objects that has been built up from
     * parsing a JSON response.
     */
    //TODO: Complete JSON parsing method
    private static ArrayList<MovieItem> extractMovieResults(String movieQueryJSON) {

        // If the JSON string is empty or null, then return early.
        if (TextUtils.isEmpty(movieQueryJSON)) {
            return null;
        }

        // Create an empty List that we can start adding earthquakes to
        ArrayList<MovieItem> movieQueryResults = new ArrayList<>();

        try {
            // Let's assign our returned JSON string to a new JSONObject
            JSONObject jsonObj = new JSONObject(movieQueryJSON);

            // Next, let's grab our JSON array with returned results
            JSONArray movieItemsJsonA = jsonObj.getJSONArray("results");

            // Alrighty, let's loop through out results!

            for (int movieNumber = 0; movieNumber < movieItemsJsonA.length(); movieNumber++) {
                JSONObject currentMovieJsonO = movieItemsJsonA.getJSONObject(movieNumber);

                String title = currentMovieJsonO.getString("title");
                Log.i(LOG_TAG, "TEST: results: " + title);

                String posterPath =  currentMovieJsonO.getString("poster_path");
                Log.i(LOG_TAG, "TEST: results: " + posterPath);


                String overview = currentMovieJsonO.getString("overview");
                Log.i(LOG_TAG, "TEST: results: " + overview);


                String releaseDate = currentMovieJsonO.getString("release_date");
                Log.i(LOG_TAG, "TEST: results: " + releaseDate);

                // Some movies may not have a URL for background, if so, let's assign the background
                // the same URL as the poster.
                String backgroundPath = currentMovieJsonO.getString("backdrop_path");

                // Note that the string literal 'null' is returned from TMDB in some cases if a
                // field is empty. Let's check for both a literal and for a null value.
                if (backgroundPath.equals("null") || TextUtils.isEmpty(backgroundPath)) {
                    backgroundPath = posterPath;
                }

                Log.i(LOG_TAG, "TEST: results: " + backgroundPath);


                double voteAverage = currentMovieJsonO.getDouble("vote_average");
                Log.i(LOG_TAG, "TEST: results: " + voteAverage);


                movieQueryResults.add(new MovieItem
                        (title, releaseDate, posterPath, backgroundPath, voteAverage, overview));
            }

        } catch (JSONException e) {
        // Catch any errors above during execution and show the exception in the log.
        Log.e(LOG_TAG, "Problem parsing the JSON results.", e);
        }

        // Return our list of movie results
        return movieQueryResults;
    }
}
