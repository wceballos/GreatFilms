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
import com.example.greatfilms.NetworkUtils.Network;
import com.example.greatfilms.TheMovieDB.MovieDBUtils;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

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

    final String TAG = MovieDetailActivity.class.getSimpleName();

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
        mButtonShowReviews.setOnClickListener(this);

        Intent intent = getIntent();

        if(intent != null) {
            if(intent.hasExtra(MovieDBUtils.PARAM_ID))
                mMovieId = intent.getIntExtra(MovieDBUtils.PARAM_ID, 0);

            /*
             * For some reason toolbar title needs to be nullified before changing it to
             * the actual movie title
             */
            setTitle(null);
            new PopulateUI().execute(mMovieId);

        } // if(intent != null)
    }

    public void setViewContent() {
        if (mMovieTitle != null)
            setTitle(mMovieTitle);

        if(mPosterByteArray != null) {
            int imageSize = mPosterByteArray.length;
            Bitmap posterBitmap = BitmapFactory.decodeByteArray(mPosterByteArray, 0, imageSize);
            mPosterDisplay.setImageBitmap(posterBitmap);
        }

        if (mMovieReleaseDate != null) {
            String releaseYear = mMovieReleaseDate.substring(0, 4);
            mReleaseDisplay.setText(releaseYear);
        }

        if (mMovieRuntime != null) {
            String runtimeFormatted = getString(R.string.movie_runtime, mMovieRuntime);
            mRuntimeDisplay.setText(runtimeFormatted);
        }

        if(mMovieVote != null) {
            String voteFormatted = getString(R.string.movie_vote, mMovieVote);
            mVoteDisplay.setText(voteFormatted);
        }

        if(mMovieOverview != null)
            mOverviewDisplay.setText(mMovieOverview);
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
        final String movieIdString = Integer.valueOf(movie.getId()).toString();

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(mMovieIsFavorite) {
                    db.movieDao().deleteFavorite(movie);
                    mMovieIsFavorite = false;
                    String logMsg = String.format("Removed from favorites (movie id: %s)", movieIdString);
                    Log.d(TAG, logMsg);
                }
                else {
                    db.movieDao().addFavorite(movie);
                    mMovieIsFavorite = true;
                    String logMsg = String.format("Added to favorites (movie id: %s)", movieIdString);
                    Log.d(TAG, logMsg);
                }
                updateFabDrawable();
            }
        }).start();
    }

    private class PopulateUI extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... movieId) {
            FavoritesDB db = FavoritesDB.getInstance(getApplicationContext());
            if(!db.movieDao().getFavorite(movieId[0]).isEmpty()) {
                mMovieIsFavorite = true;
                updateFabDrawable();
                String movieIdString = Integer.toString(mMovieId);
                String logMsg = String.format("Movie %s is in favorites database", movieIdString);
                Log.d(TAG, logMsg);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent intent = getIntent();
            if(intent.hasExtra(MovieDBUtils.PARAM_POSTER_BYTE))
                mPosterByteArray = intent.getByteArrayExtra(MovieDBUtils.PARAM_POSTER_BYTE);
            /*
             * Movie details will be loaded from network when network is available.
             * If network is not available, we'll try to load local data, such as from the
             * favorite movie database.
             */
            if(Network.isNetworkAvailable(getApplicationContext())) {
                new FetchMovieDetails().execute(mMovieId);
                new FetchMovieTrailers().execute(mMovieId);
            }
            else if (mMovieIsFavorite) {
                if(intent.hasExtra(MovieDBUtils.PARAM_TITLE))
                    mMovieTitle = intent.getStringExtra(MovieDBUtils.PARAM_TITLE);
                if(intent.hasExtra(MovieDBUtils.PARAM_RELEASE))
                    mMovieReleaseDate = intent.getStringExtra(MovieDBUtils.PARAM_RELEASE);
                if(intent.hasExtra(MovieDBUtils.PARAM_RUNTIME))
                    mMovieRuntime = intent.getStringExtra(MovieDBUtils.PARAM_RUNTIME);
                if(intent.hasExtra(MovieDBUtils.PARAM_OVERVIEW))
                    mMovieOverview = intent.getStringExtra(MovieDBUtils.PARAM_OVERVIEW);
                setViewContent();
                String toastMsg = getString(R.string.error_no_network);
                makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
            }
            else {
                String toastMsg = getString(R.string.error_no_network);
                makeText(getApplicationContext(), toastMsg, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
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
                mMovieReleaseDate = movieDetailsJson.getString(MovieDBUtils.PARAM_RELEASE);
                mMovieRuntime = movieDetailsJson.getString(MovieDBUtils.PARAM_RUNTIME);
                mMovieVote = movieDetailsJson.getString(MovieDBUtils.PARAM_VOTES);
                mMovieOverview = movieDetailsJson.getString(MovieDBUtils.PARAM_OVERVIEW);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            setViewContent();
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
