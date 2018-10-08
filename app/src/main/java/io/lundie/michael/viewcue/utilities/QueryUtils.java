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

import io.lundie.michael.viewcue.BuildConfig;
import io.lundie.michael.viewcue.datamodel.models.MovieItem;

public class QueryUtils {

    private static final String LOG_TAG = QueryUtils.class.getSimpleName();

    /**
     * Create a private constructor because no one should ever create a {@link QueryUtils} object.
     */
    private QueryUtils() {}

    /**
     * Method for building our query URL
     * @param context The current activity context.
     * @return url string
     */
    public static String queryUrlBuilder (String sortOrder) {
        final String API_AUTHORITY = "api.themoviedb.org";
        final String API_VERSION = "3";
        final String API_MOVIE_PATH = "movie";
        final String API_ADULT_PARAM = "adult";
        final String API_ADULT_VALUE = "false";
        final String API_KEY_PARAM = "api_key";

        //Use URI builder to construct our URL
        Uri.Builder query = new Uri.Builder();
        query.scheme("https")
                .authority(API_AUTHORITY)
                .appendPath(API_VERSION)
                .appendPath(API_MOVIE_PATH)
                .appendPath(sortOrder)
                .appendQueryParameter(API_KEY_PARAM, BuildConfig.API_KEY)
                .appendQueryParameter(API_ADULT_PARAM, API_ADULT_VALUE)
                .build();
        URL returnUrl = null;

        //Attempt to return our URL, check for exceptions, then convert to String on return.
        try {
            returnUrl = new URL(query.toString());
        } catch (MalformedURLException e) {
            //We'll do further checking in AsyncLoader, but perhaps it's nice to check for
            //any initial errors.
            Log.e(LOG_TAG, "There is a problem with URL construction.", e);
        }
        //Handle any null pointer exception that may be thrown by .toString() method;
        if (returnUrl == null) {
            return null;
        }
        Log.i(LOG_TAG, "TEST URL:" + returnUrl.toString());
        return returnUrl.toString();
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
        // Create and initialise empty variable.
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
}