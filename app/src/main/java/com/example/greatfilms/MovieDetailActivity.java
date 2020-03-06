package com.example.greatfilms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.greatfilms.Adapters.ReviewAdapter;
import com.example.greatfilms.Favorites.FavoritesDB;
import com.example.greatfilms.Favorites.MovieEntity;
import com.example.greatfilms.TheMovieDB.MovieDBUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.widget.Toast.makeText;

public class MovieDetailActivity extends AppCompatActivity implements ReviewAdapter.ReviewAdapterOnClickHandler, View.OnClickListener {

    private ImageView mPosterDisplay;
    private TextView mReleaseDisplay;
    private TextView mRuntimeDisplay;
    private TextView mVoteDisplay;
    private TextView mOverviewDisplay;
    private TextView mNoTrailers;
    private TextView mReviewsLabel;
    private TextView mNoReviews;
    private Button mButtonTrailer1;
    private Button mButtonTrailer2;
    private Button mButtonTrailer3;
    private Button mButtonShowReviews;
    private RecyclerView mReviewRecycler;
    private FloatingActionButton fab;

    ReviewAdapter mReviewAdapter;

    private int mMovieId = 0;
    private byte[] mPosterByteArray;
    String mMovieTitle;
    String mMovieReleaseDate;
    String mMovieRuntime;
    String mMovieOverview;
    String mMovieVote;

    boolean mMovieIsFavorite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mPosterDisplay     = (ImageView)    findViewById(R.id.iv_movie_poster);
        mReleaseDisplay    = (TextView)     findViewById(R.id.tv_movie_release);
        mRuntimeDisplay    = (TextView)     findViewById(R.id.tv_movie_runtime);
        mVoteDisplay       = (TextView)     findViewById(R.id.tv_movie_vote);
        mOverviewDisplay   = (TextView)     findViewById(R.id.tv_movie_overview);
        mNoTrailers        = (TextView)     findViewById(R.id.tv_no_trailers);
        mReviewsLabel      = (TextView)     findViewById(R.id.tv_movie_reviews_label);
        mNoReviews         = (TextView)     findViewById(R.id.tv_no_reviews);
        mButtonTrailer1    = (Button)       findViewById(R.id.btn_trailer1);
        mButtonTrailer2    = (Button)       findViewById(R.id.btn_trailer2);
        mButtonTrailer3    = (Button)       findViewById(R.id.btn_trailer3);
        mButtonShowReviews = (Button)       findViewById(R.id.btn_show_reviews);
        mReviewRecycler    = (RecyclerView) findViewById(R.id.rv_reviews);
        fab                = (FloatingActionButton) findViewById(R.id.fab_mark_favorite);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mReviewRecycler.setLayoutManager(linearLayoutManager);
        mReviewAdapter = new ReviewAdapter(this);
        mReviewRecycler.setAdapter(mReviewAdapter);
        fab.setOnClickListener(this);

        Intent intent = getIntent();

        if(intent != null) {
            if(intent.hasExtra(MovieDBUtils.PARAM_POSTER_PATH)) {
                mPosterByteArray = intent.getByteArrayExtra(MovieDBUtils.PARAM_POSTER_PATH);
                int imageSize = mPosterByteArray.length;
                Bitmap posterBitmap = BitmapFactory.decodeByteArray(intent.getByteArrayExtra(MovieDBUtils.PARAM_POSTER_PATH),0, imageSize);
                mPosterDisplay.setImageBitmap(posterBitmap);
            }
            if(intent.hasExtra(MovieDBUtils.PARAM_ID)) {
                mMovieId = intent.getIntExtra(MovieDBUtils.PARAM_ID, 0);
                checkFavorite(mMovieId);
                new FetchMovieDetails().execute(mMovieId);
                new FetchMovieTrailers().execute(mMovieId);
                mButtonShowReviews.setOnClickListener(this);
                setTitle(mMovieTitle);
            }
        }
    }

    /**
     * onClick Handler for movie review list items (Recycler View).
     *
     * @param view The View that was clicked.
     * @param review JSONObject of the movie review.
     */
    @Override
    public void onClick(View view, JSONObject review) {
        Context context = getApplicationContext();
        Class destinationClass = MovieReviewActivity.class;
        Intent reviewActivityIntent = new Intent(context, destinationClass);

        try {
            reviewActivityIntent
                    .putExtra(MovieDBUtils.PARAM_REVIEW_AUTHOR, review.getString(MovieDBUtils.PARAM_REVIEW_AUTHOR))
                    .putExtra(MovieDBUtils.PARAM_REVIEW_TEXT, review.getString(MovieDBUtils.PARAM_REVIEW_TEXT))
                    .putExtra(MovieDBUtils.PARAM_TITLE, mMovieTitle);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if(reviewActivityIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(reviewActivityIntent);
        }
    }

    /**
     * onClick Handler for buttons, except the buttons to watch trailers.
     *
     * @param button The Button View that was clicked.
     */
    @Override
    public void onClick(View button) {
        int id = button.getId();

        if(id == mButtonShowReviews.getId()) {
            mButtonShowReviews.setVisibility(View.GONE);
            mReviewRecycler.setVisibility(View.VISIBLE);
            mReviewsLabel.setVisibility(View.VISIBLE);
            new FetchMovieReviews().execute(mMovieId);
        }
        else if(id == mButtonTrailer1.getId()) {

        }
        else if(id == mButtonTrailer2.getId()) {

        }
        else if(id == mButtonTrailer3.getId()) {

        }
        else if(id == fab.getId()) {
            toggleFavorite();
        }
    }

    /**
     * Adds a movie to favorite if it is not. Removes a movie from favorites if it is.
     */
    public void toggleFavorite() {
        if(mMovieId == 0)
            return;
        final FavoritesDB db = FavoritesDB.getInstance(getApplicationContext());
        final MovieEntity movie = new MovieEntity(
                mMovieId,
                mPosterByteArray,
                mMovieTitle,
                mMovieReleaseDate,
                mMovieRuntime,
                mMovieOverview,
                null);
        final String movieId = Integer.valueOf(movie.getId()).toString();

        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... voids) {
                if(mMovieIsFavorite) {
                    db.movieDao().deleteFavorite(movie);
                    mMovieIsFavorite = false;
                    Log.d(MovieDetailActivity.class.getSimpleName(), "Unfavorite movie id: " + movieId);
                }
                else {
                    db.movieDao().addFavorite(movie);
                    mMovieIsFavorite = true;
                    Log.d(MovieDetailActivity.class.getSimpleName(), "Favorite movie id: " + movieId);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                updateFabDrawable();
            }
        }.execute();
    }

    /**
     * Checks if the movie has been marked as favorite by querying the 'favorites' database
     * @param id The Movie ID corresponding to the primary key in the database
     * @return true if the movie is in the database
     */
    public void checkFavorite(final int id) {
        final FavoritesDB db = FavoritesDB.getInstance(getApplicationContext());

        new AsyncTask<Void, Void, Boolean>() {

            @Override
            protected Boolean doInBackground(Void... voids) {
                boolean result = !db.movieDao().getFavorite(id).isEmpty();
                return result;
            }

            @Override
            protected void onPostExecute(Boolean b) {
                super.onPostExecute(b);
                mMovieIsFavorite = b;
                updateFabDrawable();
            }
        }.execute();
    }

    /**
     * The FAB drawable indicates whether a movie is marked as favorite or not. This function
     * updates the drawable to represent the current state.
     */
    public void updateFabDrawable() {
        if(mMovieIsFavorite)
            fab.setImageDrawable(getDrawable(android.R.drawable.btn_star_big_on));
        else
            fab.setImageDrawable(getDrawable(android.R.drawable.btn_star_big_off));
    }

    public class FetchMovieDetails extends AsyncTask<Integer, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Integer... movieID) {
            return MovieDBUtils.getMovieDetailsJson(movieID[0]);
        }

        @Override
        protected void onPostExecute(JSONObject movieDetailsJson) {
            super.onPostExecute(movieDetailsJson);
            //mLoadingIndicator.setVisibility(View.INVISIBLE);
            if(movieDetailsJson == null) {
                String toastMsg = getString(R.string.error_movie_details);
                makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                return;
            }

            try {
                mMovieTitle = movieDetailsJson.getString(MovieDBUtils.PARAM_TITLE);
                setTitle(mMovieTitle);
                mMovieReleaseDate = movieDetailsJson.getString(MovieDBUtils.PARAM_RELEASE);
                String releaseYear = mMovieReleaseDate.substring(0,4);
                mReleaseDisplay.setText(releaseYear);
                mMovieRuntime = getString(R.string.movie_runtime,
                        movieDetailsJson.getString(MovieDBUtils.PARAM_RUNTIME));
                mRuntimeDisplay.setText(mMovieRuntime);
                mMovieVote = getString(R.string.movie_vote,
                        movieDetailsJson.getString(MovieDBUtils.PARAM_VOTES));
                mVoteDisplay.setText(mMovieVote);
                mMovieOverview = movieDetailsJson.getString(MovieDBUtils.PARAM_OVERVIEW);
                mOverviewDisplay.setText(mMovieOverview);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public class FetchMovieTrailers extends AsyncTask<Integer, Void, ArrayList<Uri>> {

        @Override
        protected ArrayList<Uri> doInBackground(Integer... movieID) {
            return MovieDBUtils.getMovieTrailerUriList(movieID[0]);
        }

        @Override
        protected void onPostExecute(final ArrayList<Uri> movieTrailerUris) {
            super.onPostExecute(movieTrailerUris);
            if(movieTrailerUris == null) {
                String toastMsg = getString(R.string.error_movie_trailers);
                makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                return;
            }
            View.OnClickListener trailerButtonClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View trailerButton) {
                    Intent playTrailerIntent = new Intent(Intent.ACTION_VIEW);
                    if(trailerButton == mButtonTrailer1)
                        playTrailerIntent.setData(movieTrailerUris.get(0));
                    else if(trailerButton == mButtonTrailer2)
                        playTrailerIntent.setData(movieTrailerUris.get(1));
                    else if(trailerButton == mButtonTrailer3)
                        playTrailerIntent.setData(movieTrailerUris.get(2));
                    else
                        return;
                    if(playTrailerIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(playTrailerIntent);
                    }
                }
            };
            if(movieTrailerUris.size() == 0) {
                mNoTrailers.setVisibility(View.VISIBLE);
            }
            else {
                if(movieTrailerUris.size() >= 1) {
                    mButtonTrailer1.setVisibility(View.VISIBLE);
                    mButtonTrailer1.setOnClickListener(trailerButtonClickListener);
                }
                if(movieTrailerUris.size() >= 2) {
                    mButtonTrailer2.setVisibility(View.VISIBLE);
                    mButtonTrailer2.setOnClickListener(trailerButtonClickListener);
                }
                if(movieTrailerUris.size() >= 3) {
                    mButtonTrailer3.setVisibility(View.VISIBLE);
                    mButtonTrailer3.setOnClickListener(trailerButtonClickListener);
                }
            }
        }
    }

    public class FetchMovieReviews extends AsyncTask<Integer, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Integer... movieID) {
            return MovieDBUtils.getMovieReviewsJson(movieID[0]);
        }

        @Override
        protected void onPostExecute(JSONObject movieReviewsJson) {
            super.onPostExecute(movieReviewsJson);
            if(movieReviewsJson == null) {
                String toastMsg = getString(R.string.error_movie_reviews);
                makeText(getApplicationContext(), toastMsg, Toast.LENGTH_LONG).show();
                return;
            }

            JSONArray results;
            try {
                results = movieReviewsJson.getJSONArray(MovieDBUtils.PARAM_RESULTS);
                if(results.length() > 0) {
                    mNoReviews.setVisibility(View.GONE);
                    mReviewAdapter.setMovieReviews(results);
                }
                else {
                    mNoReviews.setVisibility(View.VISIBLE);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
