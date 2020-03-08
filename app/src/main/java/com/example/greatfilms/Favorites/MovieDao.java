package com.example.greatfilms.Favorites;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface MovieDao {

    @Query("SELECT * FROM movie ORDER BY title")
    LiveData<List<MovieEntity>> loadAllFavorites();

    @Query("SELECT * FROM movie WHERE id = :id")
    List<MovieEntity> getFavorite(int id);

    @Insert
    void addFavorite(MovieEntity movieEntity);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateFavorite(MovieEntity movieEntity);

    @Delete
    void deleteFavorite(MovieEntity movieEntity);
}
