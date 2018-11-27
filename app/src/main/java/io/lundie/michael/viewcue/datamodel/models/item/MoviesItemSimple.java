package io.lundie.michael.viewcue.datamodel.models.item;

import android.arch.persistence.room.ColumnInfo;

public class MoviesItemSimple {

    private int id;

    @ColumnInfo(name = "high_rated")
    private int highRated;

    private int popular;

    private int favorite;

    public MoviesItemSimple(int id, int highRated, int popular, int favorite) {
        this.id = id;
        this.highRated = highRated;
        this.popular = popular;
        this.favorite = favorite;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getHighRated() {
        return highRated;
    }

    public void setHighRated(int highRated) {
        this.highRated = highRated;
    }

    public int getPopular() {
        return popular;
    }

    public void setPopular(int popular) {
        this.popular = popular;
    }

    public int getFavorite() {
        return favorite;
    }

    public void setFavorite(int favorite) {
        this.favorite = favorite;
    }
}
