package io.lundie.michael.viewcue.datamodel.models.videos;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class RelatedVideosList {
    @SerializedName("results")
    @Expose
    private ArrayList<RelatedVideos> results = new ArrayList<>();

    public ArrayList<RelatedVideos> getResults() { return results; }
}