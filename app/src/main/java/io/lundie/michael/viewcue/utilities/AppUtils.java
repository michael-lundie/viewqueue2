package io.lundie.michael.viewcue.utilities;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

public class AppUtils {

    Application context;
    @Inject
    public AppUtils(Application application) {
        this.context = application;
    }
    /**
     * Checks to make sure the smart phone has access to the internet.
     * @param context the application context
     * @return boolean
     */
    public boolean checkNetworkAccess() {
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
     * A simple utility method to parse/format a given date to the users locale
     * @param dateString The original date string (from JSON Query)
     * @param errorMessage An error message to display if the date cannot be parsed.
     * @return Formatted date.
     */
    public static String formatDate(DateFormat dateFormat, String dateString, String errorMessage, String logTag) {

        String parsedDate = errorMessage;
        try {
            Date date = dateFormat.parse(dateString);
            parsedDate = DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
        } catch (ParseException e) {
            Log.e(logTag, "Error parsing date.", e);
            e.printStackTrace();
        }
        return parsedDate;
    }
}