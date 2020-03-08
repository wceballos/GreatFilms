package com.example.greatfilms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.greatfilms.Adapters.MovieAdapter;
import com.example.greatfilms.Favorites.FavoritesDB;
import com.example.greatfilms.Favorites.FavoritesDBUtils;
import com.example.greatfilms.Favorites.MovieEntity;
import com.example.greatfilms.NetworkUtils.Network;
import com.example.greatfilms.TheMovieDB.MovieDBUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.List;

import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    private final String API_KEY = "";

    RecyclerView mMovieRecycler;
    ProgressBar mLoadingIndicator;
    MovieAdapter mMovieAdapter;

    String mSortSetting = MovieDBUtils.PATH_SORT_RATINGS;
    final String SHOW_FAVORITES = "favorites";
    JSONObject mFavoriteMoviesJson;
    final String SORT_SETTING_KEY = "sortKey";
    final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(savedInstanceState != null) {
            mSortSetting = savedInstanceState.getString(SORT_SETTING_KEY);
            Log.d(TAG, "Sort setting " + mSortSetting);
        }
        MovieDBUtils.setApiKey(API_KEY);

        mMovieRecycler = (RecyclerView) findViewById(R.id.rv_movies);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        /* To do a grid view for the recycler, I got help from:
         * https://stackoverflow.com/questions/40587168/simple-android-grid-example-using-recyclerview-with-gridlayoutmanager-like-the
         */
        int numberOfColumns = getResources().getInteger(R.integer.moviePosterColumns);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, numberOfColumns);
        mMovieRecycler.setLayoutManager(gridLayoutManager);
        //mMovieRecycler.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

        mMovieAdapter = new MovieAdapter(this);
        mMovieRecycler.setAdapter(mMovieAdapter);

        retrieveFavorites();
        if(!mSortSetting.equals(SHOW_FAVORITES))
            loadMovieGrid(mSortSetting);
    }

    public void retrieveFavorites() {
        final LiveData<List<MovieEntity>> movies = FavoritesDB
                .getInstance(getApplicationContext())
                .movieDao()
                .loadAllFavorites();
        mFavoriteMoviesJson = FavoritesDBUtils.dbEntriesToJson(movies.getValue());
        movies.observe(this, new Observer<List<MovieEntity>>() {
            @Override
            public void onChanged(List<MovieEntity> movieEntities) {
                mFavoriteMoviesJson = FavoritesDBUtils.dbEntriesToJson(movieEntities);
                if(mSortSetting.equals(SHOW_FAVORITES)) {
                    Log.d(TAG, "Receiving database update from LiveData");
                    loadMovieGrid(mSortSetting);
                }
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putString(SORT_SETTING_KEY, mSortSetting);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        switch(id) {
            case R.id.sort_popularity:
                mMovieAdapter.setMovieData(null);
                mSortSetting = MovieDBUtils.PATH_SORT_POPULARITY;
                loadMovieGrid(mSortSetting);
                break;
            case R.id.sort_ratings:
                mMovieAdapter.setMovieData(null);
                mSortSetting = MovieDBUtils.PATH_SORT_RATINGS;
                loadMovieGrid(mSortSetting);
                break;
            case R.id.show_favorites:
                mMovieAdapter.setMovieData(null);
                mSortSetting = SHOW_FAVORITES;
                loadMovieGrid(SHOW_FAVORITES);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view, JSONObject movie) {
        Context context = this;

        Class destinationClass = MovieDetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        try {
            intentToStartDetailActivity
                    .putExtra(MovieDBUtils.PARAM_ID, movie.getInt(MovieDBUtils.PARAM_ID));
            if(movie.has(MovieDBUtils.PARAM_TITLE)) {
                intentToStartDetailActivity
                        .putExtra(MovieDBUtils.PARAM_TITLE, movie.getString(MovieDBUtils.PARAM_TITLE));
            }
            if(movie.has(MovieDBUtils.PARAM_RELEASE)) {
                intentToStartDetailActivity
                        .putExtra(MovieDBUtils.PARAM_RELEASE, movie.getString(MovieDBUtils.PARAM_RELEASE));
            }
            if(movie.has(MovieDBUtils.PARAM_RUNTIME)) {
                intentToStartDetailActivity
                        .putExtra(MovieDBUtils.PARAM_RUNTIME, movie.getString(MovieDBUtils.PARAM_RUNTIME));
            }
            if(movie.has(MovieDBUtils.PARAM_OVERVIEW)) {
                intentToStartDetailActivity
                        .putExtra(MovieDBUtils.PARAM_OVERVIEW, movie.getString(MovieDBUtils.PARAM_OVERVIEW));
            }
            if(movie.has(MovieDBUtils.PARAM_LOCAL_DATA)) {
                intentToStartDetailActivity
                        .putExtra(MovieDBUtils.PARAM_LOCAL_DATA, movie.getBoolean(MovieDBUtils.PARAM_LOCAL_DATA));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        view.setDrawingCacheEnabled(false);
        view.buildDrawingCache();
        Bitmap posterBitmap = view.getDrawingCache();
        view.setDrawingCacheEnabled(false);

        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        posterBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bs);

        intentToStartDetailActivity.putExtra(MovieDBUtils.PARAM_POSTER_BYTE, bs.toByteArray());

        if(intentToStartDetailActivity.resolveActivity(getPackageManager()) != null) {
            startActivity(intentToStartDetailActivity);
        }
    }

    private void loadMovieGrid(String sortMethod) {
        showMovieGridView();
        if(sortMethod.equals(SHOW_FAVORITES) || Network.isNetworkAvailable(this)) {
            new FetchSortedMovies().execute(sortMethod);
        }
        else {
            String errorMsg = getString(R.string.error_no_network);
            makeText(getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
        }
    }

    private void showMovieGridView() {
        mMovieRecycler.setVisibility(View.VISIBLE);
    }

    public class FetchSortedMovies extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... sortOption) {
            JSONObject movies;
            if(sortOption[0].equals(SHOW_FAVORITES)) {
                movies = mFavoriteMoviesJson;
            }
            else
                movies = MovieDBUtils.getSortedMoviesJson(sortOption[0]);
            return movies;
        }

        @Override
        protected void onPostExecute(final JSONObject moviesJSON) {
            super.onPostExecute(moviesJSON);
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            String errorToastMsg = getString(R.string.error_movies);
            if(moviesJSON == null) {
                makeText(getApplicationContext(), errorToastMsg, Toast.LENGTH_LONG).show();
                return;
            }

            JSONArray results;
            try {
                results = moviesJSON.getJSONArray(MovieDBUtils.PARAM_RESULTS);
                mMovieAdapter.setMovieData(results);
            } catch (JSONException e) {
                makeText(getApplicationContext(), errorToastMsg, Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }
    }
}
