package com.example.greatfilms;

import android.net.Uri;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MovieDBUtils {
    final static String API_BASE_URL = "https://api.themoviedb.org/3/movie";
    final static String POSTER_BASE_URL = "https://image.tmdb.org/t/p/";
    final static String POSTER_SIZE = "w500";

    // Query parameters
    final static String QUERY_PARAM_API_KEY = "api_key";

    // Sorting options
    final static String SORT_RATINGS = "top_rated";
    final static String SORT_POPULARITY = "popular";


    public static JSONObject getMovieDataJson(String apiKey, String sortOption) {
        if (!sortOption.equals(SORT_RATINGS) && !sortOption.equals(SORT_POPULARITY)) {
            // Default sort setting
            sortOption = SORT_POPULARITY;
        }

        Uri apiRequestUri = Uri.parse(API_BASE_URL)
                .buildUpon()
                .appendEncodedPath(sortOption)
                .appendQueryParameter(QUERY_PARAM_API_KEY, apiKey)
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