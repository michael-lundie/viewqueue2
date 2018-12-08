package io.lundie.michael.viewcue.datamodel.models.review;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.lundie.michael.viewcue.datamodel.models.item.MovieItem;

public class MovieReviewItem implements Parcelable {

    @SerializedName("author")
    @Expose
    private String author;
    @SerializedName("content")
    @Expose
    private String content;
    @SerializedName("id")
    @Expose
    private String id;


    private MovieReviewItem(Parcel in) {
        this.author = in.readString();
        this.content = in.readString();
        this.id = in.readString();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(author);
        out.writeString(content);
        out.writeString(id);
    }

    public static final Parcelable.Creator<MovieReviewItem> CREATOR = new Parcelable.Creator<MovieReviewItem>() {
        @Override
        public MovieReviewItem createFromParcel(Parcel in) {
            return new MovieReviewItem(in);
        }

        @Override
        public MovieReviewItem[] newArray(int size) {
            return new MovieReviewItem[size];
        }
    };

    @Override
    public int describeContents() { return 0; }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}