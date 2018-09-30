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

import com.google.gson.annotations.SerializedName;

import io.lundie.michael.viewcue.MainActivity;

/**
 * A simple object to store individual movie data.
 */

public class MovieItem implements Parcelable {

    private static final String LOG_TAG = MainActivity.class.getName();

    private static final int TYPE_POSTER = 0;
    private static final int TYPE_BACKGROUND = 1;

    @SerializedName("title")
    private String title;
    @SerializedName("release_date")
    private String releaseDate;
    @SerializedName("poster_path")
    private String posterPath;
    @SerializedName("background_path")
    private String backgroundPath;
    @SerializedName("vote_average")
    private double voteAverage;
    @SerializedName("overview")
    private String overview;

    public MovieItem(String title, String releaseDate, String posterPath,
                     String backgroundPath, double voteAverage, String overview) {
        this.title = title;
        this.releaseDate = releaseDate;
        this.posterPath = setURL(TYPE_POSTER, posterPath);
        this.backgroundPath = setURL(TYPE_BACKGROUND, backgroundPath);
        this.voteAverage = voteAverage;
        this.overview = overview;
    }

    /**
     * Constructor taking parcelable (from returned bundle on instanceSaved) as an argument.
     * @param in Parcel object data which has been Marshaled
     */
    private MovieItem(Parcel in) {
        this.title = in.readString();
        this.releaseDate = in.readString();
        this.posterPath = in.readString();
        this.backgroundPath = in.readString();
        this.voteAverage = in.readDouble();
        this.overview = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(title);
        out.writeString(releaseDate);
        out.writeString(posterPath);
        out.writeString(backgroundPath);
        out.writeDouble(voteAverage);
        out.writeString(overview);
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

    public String getReleaseDate() { return releaseDate; }

    public String getPosterPath() { return posterPath; }

    public String getBackgroundPath() { return backgroundPath; }

    public double getVoteAverage() { return voteAverage; }

    public String getOverview() { return overview; }

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