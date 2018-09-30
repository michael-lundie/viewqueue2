/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 29/09/18 16:14
 */

/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 29/09/18 16:14
 */

package io.lundie.michael.viewcue.datamodel.models;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.JsonAdapter;

import io.lundie.michael.viewcue.MainActivity;

/**
 * A simple object to store individual movie data.
 */

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

    public MovieItem(String title, String date, String posterPath,
                     String backgroundPath, double voteAverage, String synopsis) {
        this.title = title;
        this.date = date;
        this.posterURL = setURL(TYPE_POSTER, posterPath);
        this.backgroundURL = setURL(TYPE_BACKGROUND, backgroundPath);
        this.voteAverage = voteAverage;
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

    public String getSynopsis() { return synopsis; }

    private String setURL(int requestType, String path) {

        // Image resource URL is set accordingly.
        // TYPE_POSTER Loads a lower resolution image than TYPE_BACKGROUND
        String size = "w185"; // == TYPE_POSTER, Lower Resolution

        if (requestType == TYPE_BACKGROUND) {
            size = "w500"; // Higher resolution than TYPE_POSTER
        }

        Uri.Builder posterUrlBuilder = new Uri.Builder();
        posterUrlBuilder.scheme("http")
                .authority("image.tmdb.org")
                .appendPath("t")
                .appendPath("p")
                .appendPath(size)
                .appendPath(path.substring(1));
        return posterUrlBuilder.toString();
    }
}