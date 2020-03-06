package com.example.greatfilms.Favorites;

import android.content.Context;
import android.util.Base64;

import com.example.greatfilms.TheMovieDB.MovieDBUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class FavoritesDBUtils {

    /**
     * Converts the database into a JSONObject for compatibility with the MovieAdapter class.
     *
     * @param context The application context to access the database.
     * @return JSON representation of the database.
     */
    public static JSONObject getFavorites(Context context) {
        FavoritesDB db = FavoritesDB.getInstance(context);
        List<MovieEntity> dbEntries = db.movieDao().loadAllFavorites();

        JSONObject rootJson = new JSONObject();
        JSONArray resultsArray = new JSONArray();

        for(MovieEntity item : dbEntries) {
            try {
                JSONObject movieJson = new JSONObject()
                        .put(MovieDBUtils.PARAM_ID, item.getId())
                        .put(MovieDBUtils.PARAM_POSTER_BYTE, posterToString(item.getPoster()))
                        .put(MovieDBUtils.PARAM_TITLE, item.getTitle())
                        .put(MovieDBUtils.PARAM_RELEASE, item.getReleaseDate())
                        .put(MovieDBUtils.PARAM_RUNTIME, item.getRuntime())
                        .put(MovieDBUtils.PARAM_OVERVIEW, item.getOverview());
                resultsArray.put(movieJson);
            }
            catch (JSONException e) {
                e.printStackTrace();
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
