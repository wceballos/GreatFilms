package com.example.greatfilms;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.greatfilms.Favorites.FavoritesDB;
import com.example.greatfilms.Favorites.MovieEntity;

import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private LiveData<List<MovieEntity>> movies;

    public MainViewModel(@NonNull Application application) {
        super(application);
        FavoritesDB db = FavoritesDB.getInstance(this.getApplication());
        movies = db.movieDao().loadAllFavorites();
    }

    public LiveData<List<MovieEntity>> getMovies() {
        return movies;
    }
}
