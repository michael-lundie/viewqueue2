/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 29/09/18 16:21
 */

package io.lundie.michael.viewcue.datamodel.models.item;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class MoviesList {

    @SerializedName("results")
    @Expose
    private ArrayList<MovieItem> results = new ArrayList<>();

    public ArrayList<MovieItem> getResults() {
        return results;
    }
}