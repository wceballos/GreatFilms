package com.example.greatfilms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

import static android.widget.Toast.makeText;

public class MainActivity extends AppCompatActivity implements MovieAdapter.MovieAdapterOnClickHandler {

    private final String API_KEY = "";

    RecyclerView mMovieRecycler;
    ProgressBar mLoadingIndicator;
    MovieAdapter mMovieAdapter;

    String mSortSetting = MovieDBUtils.SORT_RATINGS_DESC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMovieRecycler = (RecyclerView) findViewById(R.id.rv_movies);
        mLoadingIndicator = (ProgressBar) findViewById(R.id.pb_loading_indicator);

        /* To do a grid view for the recycler, I got help from:
         * https://stackoverflow.com/questions/40587168/simple-android-grid-example-using-recyclerview-with-gridlayoutmanager-like-the
         */
        int numberOfColumns = 3;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, numberOfColumns);
        mMovieRecycler.setLayoutManager(gridLayoutManager);
        //mMovieRecycler.setLayoutManager(new GridLayoutManager(this, numberOfColumns));

        mMovieAdapter = new MovieAdapter(this);
        mMovieRecycler.setAdapter(mMovieAdapter);

        loadMovieGrid(mSortSetting);
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
                mSortSetting = MovieDBUtils.SORT_POPULARITY_DESC;
                loadMovieGrid(mSortSetting);
                break;
            case R.id.sort_ratings:
                mMovieAdapter.setMovieData(null);
                mSortSetting = MovieDBUtils.SORT_RATINGS_DESC;
                loadMovieGrid(mSortSetting);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view, JSONObject movie) {
        Context context = this;
        Class destinationClass = MovieDetailActivity.class;
        Intent intentToStartDetailActivity = new Intent(context, destinationClass);
        // TODO: instead of passing all of these movie details, pass the json object in the intent
        try {
            intentToStartDetailActivity.putExtra("TITLE", movie.getString("title"));
            intentToStartDetailActivity.putExtra("RELEASE", movie.getString("release_date"));
            intentToStartDetailActivity.putExtra("OVERVIEW", movie.getString("overview"));
            intentToStartDetailActivity.putExtra("VOTE", movie.getString("vote_average"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        view.setDrawingCacheEnabled(false);
        view.buildDrawingCache();
        Bitmap posterBitmap = view.getDrawingCache();
        view.setDrawingCacheEnabled(false);

        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        posterBitmap.compress(Bitmap.CompressFormat.JPEG, 50, bs);

        intentToStartDetailActivity.putExtra("POSTER", bs.toByteArray());

        if(intentToStartDetailActivity.resolveActivity(getPackageManager()) != null) {
            startActivity(intentToStartDetailActivity);
        }
    }

    private void loadMovieGrid(String sortMethod) {
        showMovieGridView();
        new FetchMovieData().execute(sortMethod);
    }

    private void showMovieGridView() {
        mMovieRecycler.setVisibility(View.VISIBLE);
    }

    public class FetchMovieData extends AsyncTask<String, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected JSONObject doInBackground(String... sortOption) {
            JSONObject moviesJSON =
                    MovieDBUtils.getMovieDataJson(API_KEY, sortOption[0]);

            return moviesJSON;
        }

        @Override
        protected void onPostExecute(JSONObject moviesJSON) {
            super.onPostExecute(moviesJSON);
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if(moviesJSON == null) {
                String toastMsg = "Failed to load movies";
                makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                return;
            }

            JSONArray results;
            try {
                results = moviesJSON.getJSONArray("results");
                mMovieAdapter.setMovieData(results);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
