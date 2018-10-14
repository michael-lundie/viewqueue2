package io.lundie.michael.viewcue.utilities;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GeneralUtils {
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