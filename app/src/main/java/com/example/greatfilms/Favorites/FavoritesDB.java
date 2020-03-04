package com.example.greatfilms.Favorites;

import android.content.Context;
import android.util.Log;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {MovieEntity.class}, version = 1, exportSchema = false)
public abstract class FavoritesDB extends RoomDatabase {

    private static final String LOG_TAG = FavoritesDB.class.getSimpleName();
    private static final Object LOCK = new Object();
    private static final String DB_NAME = "favorite_movies";
    private static FavoritesDB sInstance;

    public static FavoritesDB getInstance(Context context) {
        if(sInstance == null) {
            synchronized (LOCK) {
                Log.d(LOG_TAG, "creating new DB instance");
                sInstance = Room.databaseBuilder(context.getApplicationContext(),
                        FavoritesDB.class, FavoritesDB.DB_NAME)
                        .allowMainThreadQueries()
                        .build();
            }
        }
        Log.d(LOG_TAG, "Getting the DB instance");
        return sInstance;
    }

    public abstract MovieDao movieDao();
}



