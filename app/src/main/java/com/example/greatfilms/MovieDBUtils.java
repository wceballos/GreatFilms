package com.example.greatfilms;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MovieDBUtils {
    final static String API_BASE_URL = "https://api.themoviedb.org/3/movie";
    final static String POSTER_BASE_URL = "https://image.tmdb.org/t/p/";
    final static String POSTER_SIZE = "w500";

    // Youtube
    final static String YT_BASE_URL = "https://www.youtube.com/watch";
    final static String QUERY_PARAM_YT_VIDEO = "v";

    // Query parameters
    final static String QUERY_PARAM_API_KEY = "api_key";

    // Sorting options
    final static String SORT_RATINGS = "top_rated";
    final static String SORT_POPULARITY = "popular";

    static String apiKey;

    public static JSONObject getMovieDataJson(String apiKey, String sortOption) {
        MovieDBUtils.apiKey = apiKey;
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

    public static ArrayList<Uri> getMovieTrailerUris(int movieID) {
        ArrayList<Uri> trailerUriList = new ArrayList<>();

        Uri apiRequestUri = Uri.parse(API_BASE_URL)
                .buildUpon()
                .appendPath(Integer.toString(movieID))
                .appendPath("videos")
                .appendQueryParameter(QUERY_PARAM_API_KEY, apiKey)
                .build();
        Log.d("URL", "api movie trailer request url: " + apiRequestUri.toString());

        URL url = null;
        try {
            url = new URL(apiRequestUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            String responseString = getHttpResponseBody(url);
            try {
                JSONObject responseJSON = new JSONObject(responseString);
                JSONArray resultsJsonArray = responseJSON.getJSONArray("results");
                for(int i = 0; i < resultsJsonArray.length(); i++) {
                    JSONObject resultJson = resultsJsonArray.getJSONObject(i);
                    String site = resultJson.getString("site");
                    String type = resultJson.getString("type");
                    String key  = resultJson.getString("key");
                    if(site.equals("YouTube") && type.equals("Trailer")) {
                        Uri trailerUri = Uri.parse(YT_BASE_URL)
                                .buildUpon()
                                .appendQueryParameter(QUERY_PARAM_YT_VIDEO, key)
                                .build();
                        Log.d("URL", "trailer url created: " + trailerUri.toString());
                        trailerUriList.add(trailerUri);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return trailerUriList;
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