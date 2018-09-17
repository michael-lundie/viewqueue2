package io.lundie.michael.viewcue;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;

import java.net.URI;
import java.net.URL;

import butterknife.internal.Utils;
import io.lundie.michael.viewcue.utilities.QueryUtils;

public class MovieItem implements Parcelable {

    private static final String LOG_TAG = MainActivity.class.getName();
    private static final int TYPE_POSTER = 0;
    private static final int TYPE_BACKGROUND = 1;

    private String title;
    private String date;
    private String posterURL;
    private String backgroundURL;
    private double voteAverage;
    private String synopsis;
    private String test;

    public MovieItem(String title, String date, String posterPath,
                     String backgroundPath, double voteAverage, String synopsis) {
        this.title = title;
        this.date = date;
        this.posterURL = setURL(TYPE_POSTER, posterPath);
        this.backgroundURL = setURL(TYPE_BACKGROUND, backgroundPath);
        this.voteAverage = voteAverage;
        Log.i(LOG_TAG, "TEST: MovieItem Synopsis var: " + synopsis);
        this.synopsis = synopsis;
    }

    /**
     * Constructor taking parcelable (from returned bundle on instanceSaved) as an argument.
     * @param in Parcel object data which has been Marshaled
     */
    private MovieItem(Parcel in) {
        this.title = in.readString();
        this.date = in.readString();
        this.posterURL = in.readString();
        this.backgroundURL = in.readString();
        this.voteAverage = in.readDouble();
        this.synopsis = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(date);
        out.writeString(posterURL);
        out.writeString(backgroundURL);
        out.writeDouble(voteAverage);
        out.writeString(synopsis);
    }

    public static final Parcelable.Creator<MovieItem> CREATOR = new Parcelable.Creator<MovieItem>() {
        @Override
        public MovieItem createFromParcel(Parcel in) {
            return new MovieItem(in);
        }

        @Override
        public MovieItem[] newArray(int size) {
            return new MovieItem[size];
        }
    };

    public int describeContents() {
        return 0;
    }

    public String getTitle() { return title; }

    public String getDate() { return date; }

    public String getPosterURL() { return posterURL; }

    public String getBackgroundURL() { return backgroundURL; }

    public double getVoteAverage() { return voteAverage; }

    public String getSynopsis() {
        Log.i(LOG_TAG, "TEST: Get synopsis called: DATA: " + synopsis);
        return synopsis; }

    private String setURL(int requestType, String path) {

        String size = "w185";
        if (requestType == TYPE_BACKGROUND) {
            size = "w500";
        }

        Uri.Builder posterUrlBuilder = new Uri.Builder();
        posterUrlBuilder.scheme("http")
                .authority("image.tmdb.org")
                .appendPath("t")
                .appendPath("p")
                .appendPath(size)
                .appendPath(path.substring(1));
        Log.i(LOG_TAG, "TEST poster URL" + posterUrlBuilder.toString());
        return posterUrlBuilder.toString();
    }
}