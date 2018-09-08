package io.lundie.michael.viewcue;

import android.net.Uri;
import android.util.Log;

import java.net.URI;
import java.net.URL;

public class MovieItem {

    public static final String LOG_TAG = MainActivity.class.getName();

    private String title;
    private int date;
    private String posterURL;
    private int voteAverage;
    private String synopsis;

    public MovieItem(String title, int date, String posterPath, int voteAverage, String synopsis) {
        this.title = title;
        this.date = date;
        this.posterURL = setPosterURL(posterPath);
        this.voteAverage = voteAverage;
        this.synopsis = synopsis;
    }

    public String getTitle() { return title; }

    public int getDate() { return date; }

    public String getPosterURL() { return posterURL; }

    public int getVoteAverage() { return voteAverage; }

    public String getSynopsis() { return synopsis; }

    private String setPosterURL(String posterPath) {
        Uri.Builder posterUrlBuilder = new Uri.Builder();
        posterUrlBuilder.scheme("http")
                .authority("image.tmdb.org")
                .appendPath("t")
                .appendPath("p")
                .appendPath("w185")
                .appendPath(posterPath.substring(1));
        Log.i(LOG_TAG, "Test poster URL" + posterUrlBuilder.toString());
        return posterUrlBuilder.toString();
    }
}
