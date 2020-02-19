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
    // The Movie DB API URL
    final static String API_BASE_URL = "https://api.themoviedb.org/3/movie";

    // JSON Params
    final static String PARAM_POSTER = "poster_path";
    final static String PARAM_RESULTS = "results";
    final static String PARAM_ID = "id";
    final static String PARAM_TITLE = "title";
    final static String PARAM_OVERVIEW = "overview";
    final static String PARAM_VOTES = "vote_average";
    final static String PARAM_RELEASE = "release_date";
    final static String PARAM_RUNTIME = "runtime";
    final static String PARAM_VIDEO_SITE = "site";
    final static String PARAM_VIDEO_TYPE = "type";
    final static String PARAM_VIDEO_KEY = "key";

    // Movie poster
    final static String POSTER_BASE_URL = "https://image.tmdb.org/t/p/";
    final static String POSTER_SIZE = "w500";

    // Youtube
    final static String YT_BASE_URL = "https://www.youtube.com/watch";
    final static String YT_VIDEO_TYPE = "Trailer";
    final static String YT_VIDEO_SITE = "YouTube";

    // Query parameters
    final static String QUERY_PARAM_API_KEY = "api_key";
    final static String QUERY_PARAM_YT_VIDEO = "v";


    // Sorting options
    final static String PATH_SORT_RATINGS = "top_rated";
    final static String PATH_SORT_POPULARITY = "popular";

    // Movie info paths
    final static String PATH_DETAILS = ""; // There is no path name for this
    final static String PATH_REVIEWS = "reviews";
    final static String PATH_VIDEOS  = "videos";

    // The Movie DB API key
    static private String apiKey;

    /**
     * This method must be called before any other method. This sets the API key to be used.
     *
     * @param apiKey The Movie DB API key.
     */
    public static void setApiKey(String apiKey) {
        MovieDBUtils.apiKey = apiKey;
    }

    /**
     * Get a list of movies sorted by a given parameter.
     *
     * @param sortOption How to sort the movies.
     * @return The Movie DB JSON response.
     */
    public static JSONObject getSortedMoviesJson(String sortOption) {
        MovieDBUtils.apiKey = apiKey;
        if (!sortOption.equals(PATH_SORT_RATINGS) && !sortOption.equals(PATH_SORT_POPULARITY)) {
            // Default sort setting
            sortOption = PATH_SORT_POPULARITY;
        }

        JSONObject sortedMoviesJson = null;

        try {
            sortedMoviesJson = getMovieInfoApiJson(0, sortOption);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return sortedMoviesJson;
    }

    /**
     * Get details on a particular movie.
     *
     * @param movieID The Movie DB movie ID.
     * @return The Move DB Json response.
     */
    public static JSONObject getMovieDetailsJson(int movieID) {
        JSONObject movieDetailsJson = null;
        try {
            movieDetailsJson = getMovieInfoApiJson(movieID, PATH_DETAILS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return movieDetailsJson;
    }

    /**
     * This method is used for getting a Uri to retrieve a movie poster.
     *
     * @param posterPath Poster path from The Movie DB.
     * @return A uri that can be used to retrieve the poster.
     */
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
     * Gets the uri of all YouTube trailers for a movie.
     *
     * @param movieID The Movie DB movie ID.
     * @return An ArrayList of movie trailer uris.
     */
    public static ArrayList<Uri> getMovieTrailerUriList(int movieID) {
        ArrayList<Uri> trailerUriList = new ArrayList<>();

        try {
            JSONObject movieTrailersJson = getMovieInfoApiJson(movieID, PATH_VIDEOS);
            JSONArray resultsJsonArray = movieTrailersJson.getJSONArray(PARAM_RESULTS);
            for(int i = 0; i < resultsJsonArray.length(); i++) {
                JSONObject resultJson = resultsJsonArray.getJSONObject(i);
                String site = resultJson.getString(PARAM_VIDEO_SITE);
                String type = resultJson.getString(PARAM_VIDEO_TYPE);
                String key  = resultJson.getString(PARAM_VIDEO_KEY);
                if(site.equals(YT_VIDEO_SITE) && type.equals(YT_VIDEO_TYPE)) {
                    Uri trailerUri = Uri.parse(YT_BASE_URL)
                            .buildUpon()
                            .appendQueryParameter(QUERY_PARAM_YT_VIDEO, key)
                            .build();
                    Log.d("URL", "Trailer url created: " + trailerUri.toString());
                    trailerUriList.add(trailerUri);
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return trailerUriList;
    }

    /**
     * Gets the reviews on a movie.
     *
     * @param movieID The Movie DB movie ID.
     * @return The Movie DB Json response containing reviews.
     */
    public static JSONObject getMovieReviewsJson(int movieID) {
        JSONObject movieReviewsJson = null;
        try {
            movieReviewsJson = getMovieInfoApiJson(movieID, PATH_REVIEWS);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return movieReviewsJson;
    }

    /**
     *  This method is used to request information about a movie.
     *
     * @param movieID The Movie DB movie ID.
     * @param path Path for the information being requested (e.g. details, videos, reviews, etc.)
     * @return A JSONObject containing the response from the GET request.
     * @throws JSONException Parsing error.
     */
    private static JSONObject getMovieInfoApiJson(int movieID, String path) throws JSONException {
        Uri.Builder apiRequestBuilder = Uri.parse(API_BASE_URL).buildUpon();
        if(movieID > 0)
            apiRequestBuilder.appendPath(Integer.toString(movieID));
        if(!path.isEmpty())
            apiRequestBuilder.appendPath(path);
        apiRequestBuilder.appendQueryParameter(QUERY_PARAM_API_KEY, apiKey);
        Uri apiRequestUri = apiRequestBuilder.build();

        Log.d("URL", "TMDB API request url: " + apiRequestUri.toString());

        URL url = null;
        try {
            url = new URL(apiRequestUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        JSONObject responseJSON = null;

        try {
            String responseString = getHttpResponseBody(url);
            responseJSON = new JSONObject(responseString);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return responseJSON;
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