/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 07/10/18 14:53
 */

package io.lundie.michael.viewcue.datamodel.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;
import android.util.Log;

import io.lundie.michael.viewcue.datamodel.models.MovieItem;

@Database(entities = {MovieItem.class}, version = 1, exportSchema = false)
public abstract class MoviesDatabase extends RoomDatabase {

    private static final String LOG_TAG = MoviesDatabase.class.getSimpleName();

    private static volatile MoviesDatabase sInstance;

    public abstract MoviesDao moviesDao();
}