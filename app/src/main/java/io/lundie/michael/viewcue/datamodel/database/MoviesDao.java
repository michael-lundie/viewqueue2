/*
 * Crafted by Michael R Lundie (2018)
 * Last Modified 07/10/18 14:43
 */

package io.lundie.michael.viewcue.datamodel.database;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import io.lundie.michael.viewcue.datamodel.models.item.MovieItem;
import io.lundie.michael.viewcue.datamodel.models.item.MoviesItemSimple;

import static android.arch.persistence.room.OnConflictStrategy.REPLACE;

/**
 * Simple Data Access Object interface providing various methods to assist with database operations.
 */
@Dao
public interface MoviesDao {

    @Query("SELECT * FROM movies ORDER BY popularity")
    List<MovieItem> loadAllMovies();

    @Query("SELECT * FROM movies WHERE popular IS NOT 0 ORDER BY popular")
    List<MovieItem> loadPopularMovies();

    @Query("SELECT * FROM movies WHERE high_rated IS NOT 0 ORDER BY high_rated")
    List<MovieItem> loadHighRatedMovies();

    @Query("SELECT * FROM movies WHERE favorite IS NOT 0 ORDER BY high_rated")
    List<MovieItem> loadFavoriteMovies();

    @Query("SELECT * FROM movies WHERE id = :id")
    MovieItem fetchMovie(int id);

    @Query("SELECT id, popular, high_rated, favorite FROM movies WHERE popular IS NOT NULL ORDER BY id")
    List<MoviesItemSimple> fetchSimpleListPopular();

    @Query("SELECT id, popular, high_rated, favorite FROM movies WHERE high_rated IS NOT NULL ORDER BY id")
    List<MoviesItemSimple> fetchSimpleListHighRated();

    @Insert(onConflict = REPLACE)
    void insertMovie(MovieItem movieItem);

    @Update(onConflict = REPLACE)
    void updateMovie(MovieItem movieItem);

    @Delete
    void deleteMovie(MovieItem movieItem);
}
