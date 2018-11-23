package io.lundie.michael.viewcue.datamodel.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MovieReviewsList {

    @SerializedName("results")
    @Expose
    private ArrayList<MovieReviewItem> results = new ArrayList<>();

    @SerializedName("total_results")
    @Expose
    private int totalResults;

    public ArrayList<MovieReviewItem> getResults() { return results; }

    public Integer getTotalResults() { return totalResults; }
}
