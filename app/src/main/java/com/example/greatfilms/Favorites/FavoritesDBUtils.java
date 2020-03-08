package com.example.greatfilms.Favorites;

import android.content.Context;
import android.content.Entity;
import android.util.Base64;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.example.greatfilms.TheMovieDB.MovieDBUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FavoritesDBUtils {

    public static JSONObject dbEntriesToJson(List<MovieEntity> movieEntities) {
        JSONObject rootJson = new JSONObject();
        JSONArray resultsArray = new JSONArray();

        if(movieEntities != null) {
            for (MovieEntity item : movieEntities) {
                try {
                    JSONObject movieJson = new JSONObject()
                            .put(MovieDBUtils.PARAM_ID, item.getId())
                            .put(MovieDBUtils.PARAM_POSTER_BYTE, posterToString(item.getPoster()))
                            .put(MovieDBUtils.PARAM_TITLE, item.getTitle())
                            .put(MovieDBUtils.PARAM_RELEASE, item.getReleaseDate())
                            .put(MovieDBUtils.PARAM_RUNTIME, item.getRuntime())
                            .put(MovieDBUtils.PARAM_OVERVIEW, item.getOverview())
                            // To help identify that a movie is in the database
                            .put(MovieDBUtils.PARAM_LOCAL_DATA, true);
                    resultsArray.put(movieJson);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }

        try {
            rootJson.put(MovieDBUtils.PARAM_TOTAL, resultsArray.length())
                    .put(MovieDBUtils.PARAM_RESULTS, resultsArray);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        return rootJson;
    }

    public static String posterToString(byte[] poster) {
        int len = poster.length;
        return Base64.encodeToString(poster, 0, len, 0);
    }

    public static byte[] posterToBytes(String poster) {
        return Base64.decode(poster, 0);
    }
}
