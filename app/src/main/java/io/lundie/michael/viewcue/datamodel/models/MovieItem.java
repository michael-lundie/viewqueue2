/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 29/09/18 16:14
 */

/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 29/09/18 16:14
 */

package io.lundie.michael.viewcue.datamodel.models;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.lundie.michael.viewcue.ui.activities.MainActivity;

/**
 * A simple object to store individual movie data.
 */
@Entity(tableName = "movies")
public class MovieItem implements Parcelable {

    private static final String LOG_TAG = MainActivity.class.getName();

    private static final String POSTER_SIZE = "w185";
    private static final String BACKGROUND_SIZE = "w500";

    //TODO: REmove constants
    private static final int TYPE_POSTER = 0;
    private static final int TYPE_BACKGROUND = 1;

    @PrimaryKey
    @SerializedName("id")
    @Expose
    private int id;

    @SerializedName("popularity")
    @Expose
    private float popularity;

    @SerializedName("title")
    @Expose
    private String title;

    @ColumnInfo(name = "release_date")
    @SerializedName("release_date")
    @Expose
    private String releaseDate;

    @ColumnInfo(name = "poster_path")
    @SerializedName("poster_path")
    @Expose
    private String posterPath;

    @ColumnInfo(name = "background_path")
    @SerializedName("background_path")
    @Expose
    private String backgroundPath;

    @ColumnInfo(name = "vote_average")
    @SerializedName("vote_average")
    @Expose
    private double voteAverage;

    @SerializedName("overview")
    @Expose
    private String overview;

    @ColumnInfo(name = "high_rated")
    private int highRated;

    private int popular;

    private int favorite;

    public MovieItem(int id, float popularity, String title, String releaseDate, String posterPath,
                     String backgroundPath, double voteAverage, String overview) {
        this.id = id;
        this.popularity = popularity;
        this.title = title;
        this.releaseDate = releaseDate;
        this.posterPath = posterPath;
        this.backgroundPath = backgroundPath;
        this.voteAverage = voteAverage;
        this.overview = overview;
    }

    public MovieItem item(){
        return this;
    }

    /**
     * Constructor taking parcelable (from returned bundle on instanceSaved) as an argument.
     * @param in Parcel object data which has been Marshaled
     */
    private MovieItem(Parcel in) {
        this.id = in.readInt();
        this.title = in.readString();
        this.releaseDate = in.readString();
        this.posterPath = in.readString();
        this.backgroundPath = in.readString();
        this.voteAverage = in.readDouble();
        this.overview = in.readString();
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(id);
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

    public int getId() {
        return id;
    }

    public float getPopularity() {
        return popularity;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public String getBackgroundPath() {
        return backgroundPath;
    }

    public String getTitle() { return title; }

    public String getReleaseDate() { return releaseDate; }

    public String getPosterURL() { return buildImageURL(POSTER_SIZE, posterPath); }

    public String getBackgroundURL() {
        if(TextUtils.isEmpty(backgroundPath)) {
            return buildImageURL(BACKGROUND_SIZE, posterPath);
        } return buildImageURL(BACKGROUND_SIZE, backgroundPath); }

    public double getVoteAverage() { return voteAverage; }

    public String getOverview() { return overview; }

    public int getHighRated() { return highRated; }

    public void setHighRated(int highRated) { this.highRated = highRated; }

    public int getPopular() { return popular; }

    public void setPopular(int popular) { this.popular = popular; }

    public int getFavorite() { return favorite; }

    public void setFavorite(int favorite) { this.favorite = favorite; }

    //TODO: Remove this method.
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

    private String buildImageURL(String requestSize, String path) {
        Uri.Builder posterUrlBuilder = new Uri.Builder();
        posterUrlBuilder.scheme("http")
                .authority("image.tmdb.org")
                .appendPath("t")
                .appendPath("p")
                .appendPath(requestSize)
                .appendPath(path.substring(1));
        return posterUrlBuilder.toString();
    }
}