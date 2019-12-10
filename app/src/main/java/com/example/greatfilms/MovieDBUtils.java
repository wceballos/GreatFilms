package com.example.greatfilms;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.JsonWriter;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MovieDBUtils {
    final static String API_BASE_URL = "https://api.themoviedb.org/3";
    final static String POSTER_BASE_URL = "https://image.tmdb.org/t/p/";
    final static String POSTER_SIZE = "w500";

    // Paths
    final static String DATA_PATH_DISCOVER = "discover/movie";

    // Query parameters
    final static String QUERY_PARAM_API_KEY = "api_key";
    final static String QUERY_PARAM_SORT_BY = "sort_by";

    // Sorting options
    final static String SORT_POPULARITY_DESC = "popularity.desc";
    final static String SORT_RATINGS_DESC = "vote_count.desc";

    public static JSONObject getMovieDataJson(String apiKey, String sortOption) {
        if (sortOption.equals(SORT_RATINGS_DESC)) {
            sortOption = SORT_RATINGS_DESC;
        } else { // Default
            sortOption = SORT_POPULARITY_DESC;
        }

        Uri apiRequestUri = Uri.parse(API_BASE_URL)
                .buildUpon()
                .appendEncodedPath(DATA_PATH_DISCOVER)
                .appendQueryParameter(QUERY_PARAM_API_KEY, apiKey)
                .appendQueryParameter(QUERY_PARAM_SORT_BY, sortOption)
                .build();
        Log.d("URL", "api request url: " + apiRequestUri.toString());

        URL url = null;
        try {
            url = new URL(apiRequestUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        JSONObject responseJSON = new JSONObject();

        try {
            String responseString = getHttpResponseBody(url);
            try {
                responseJSON = new JSONObject(responseString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }



        return responseJSON;
    }

    public static Uri getMoviePosterUri(String posterPath) {
        Uri moviePosterUri = Uri.parse(POSTER_BASE_URL)
                .buildUpon()
                .appendEncodedPath(POSTER_SIZE)
                .appendEncodedPath(posterPath)
                .build();
        Log.d("URL", "poster url: " + moviePosterUri.toString());
        return moviePosterUri;
    }

    /**
     * This method returns the entire HTTP response body.
     *
     * @param url The URL to fetch the HTTP response from.
     * @return The response body as a string.
     * @throws IOException Related to network.
     */
    private static String getHttpResponseBody(URL url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        Request httpRequest = new Request.Builder()
                .url(url)
                .build();

        try (Response response = client.newCall(httpRequest).execute()) {
            return response.body().string();
        }
    }
}