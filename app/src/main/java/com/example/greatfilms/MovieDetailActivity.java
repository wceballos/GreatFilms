package com.example.greatfilms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProviders;
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
import com.example.greatfilms.ViewModels.MovieDetailViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Objects;

import static android.widget.Toast.makeText;

public class MovieDetailActivity extends AppCompatActivity
        implements ReviewAdapter.ReviewAdapterOnClickHandler, View.OnClickListener {

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
    boolean mIsFavoriteMovie;
    private MovieDetailViewModel mViewModel;
    ArrayList<Uri> mMovieTrailerUris;
    JSONArray mMovieReviews;

    final String TAG = MovieDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        mViewModel = ViewModelProviders.of(this).get(MovieDetailViewModel.class);

        mPosterDisplay     = findViewById(R.id.iv_movie_poster);
        mReleaseDisplay    = findViewById(R.id.tv_movie_release);
        mRuntimeDisplay    = findViewById(R.id.tv_movie_runtime);
        mVoteDisplay       = findViewById(R.id.tv_movie_vote);
        mOverviewDisplay   = findViewById(R.id.tv_movie_overview);
        mNoTrailers        = findViewById(R.id.tv_no_trailers);
        mReviewsLabel      = findViewById(R.id.tv_movie_reviews_label);
        mNoReviews         = findViewById(R.id.tv_no_reviews);
        mButtonTrailer1    = findViewById(R.id.btn_trailer1);
        mButtonTrailer2    = findViewById(R.id.btn_trailer2);
        mButtonTrailer3    = findViewById(R.id.btn_trailer3);
        mButtonShowReviews = findViewById(R.id.btn_show_reviews);
        mReviewRecycler    = findViewById(R.id.rv_reviews);
        fab                = findViewById(R.id.fab_mark_favorite);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mReviewRecycler.setLayoutManager(linearLayoutManager);
        mReviewAdapter = new ReviewAdapter(this);
        mReviewRecycler.setAdapter(mReviewAdapter);
        fab.setOnClickListener(this);
        mButtonShowReviews.setOnClickListener(this);

        Intent intent = getIntent();

        if(mViewModel.getMovieId() != 0) {
            restoreFromViewModel();
        }
        else if(intent != null) {
            if(intent.hasExtra(MovieDBUtils.PARAM_ID)) {
                mMovieId = intent.getIntExtra(MovieDBUtils.PARAM_ID, 0);
                mViewModel.setMovieId(mMovieId);
                setTitle(null);
                new PopulateUI().execute(mMovieId);
            }
        } // if(intent != null)
    } // onCreate(Bundle savedInstanceState)

    /**
     * onClick Handler for all buttons.
     *
     * @param button The Button View that was clicked.
     */
    @Override
    public void onClick(View button) {
        int id = button.getId();
        if(id == mButtonShowReviews.getId())
            showReviews();
        else if(id == mButtonTrailer1.getId())
            playTrailer(mMovieTrailerUris.get(0));
        else if(id == mButtonTrailer2.getId())
            playTrailer(mMovieTrailerUris.get(1));
        else if(id == mButtonTrailer3.getId())
            playTrailer(mMovieTrailerUris.get(2));
        else if(id == fab.getId())
            toggleFavorite();
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
            if(reviewActivityIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(reviewActivityIntent);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Restores views from the ViewModel after device rotation.
     */
    private void restoreFromViewModel() {
        Log.d(TAG, "Restoring views from ViewModel");
        mMovieId = mViewModel.getMovieId();
        mMovieTitle = mViewModel.getMovieTitle();
        mPosterByteArray = mViewModel.getPosterByteArray();
        mMovieReleaseDate = mViewModel.getMovieReleaseDate();
        mMovieRuntime = mViewModel.getMovieRuntime();
        mMovieOverview = mViewModel.getMovieOverview();
        mMovieVote = mViewModel.getMovieVote();
        mIsFavoriteMovie = mViewModel.isFavoriteMovie();
        mMovieTrailerUris = mViewModel.getMovieTrailerUris();
        mMovieReviews = mViewModel.getMovieReviews();
        hideReviews();
        updateMovieDetailViews();
        updateMovieTrailerViews();
        updateMovieReviewViews();
        updateFabDrawable();
    }

    /**
     * Updates the views relating to movie details (title, runtime, overview, etc.)
     * with current data.
     */
    public void updateMovieDetailViews() {
        if(mMovieTitle != null) {
            setTitle(mMovieTitle);
        }
        if(mPosterByteArray != null) {
            int imageSize = mPosterByteArray.length;
            Bitmap posterBitmap = BitmapFactory.decodeByteArray(mPosterByteArray, 0, imageSize);
            mPosterDisplay.setImageBitmap(posterBitmap);
        }
        if(mMovieReleaseDate != null) {
            String releaseYear = mMovieReleaseDate.substring(0, 4);
            mReleaseDisplay.setText(releaseYear);
        }
        if(mMovieRuntime != null) {
            String runtimeFormatted = getString(R.string.movie_runtime, mMovieRuntime);
            mRuntimeDisplay.setText(runtimeFormatted);
        }
        if(mMovieVote != null) {
            String voteFormatted = getString(R.string.movie_vote, mMovieVote);
            mVoteDisplay.setText(voteFormatted);
        }
        if(mMovieOverview != null) {
            mOverviewDisplay.setText(mMovieOverview);
        }
    }

    /**
     * Updates views relating to movie trailers (trailer buttons)
     * with current data.
     */
    private void updateMovieTrailerViews() {
        if(mMovieTrailerUris != null) {
            if (mMovieTrailerUris.size() == 0) {
                mNoTrailers.setVisibility(View.VISIBLE);
            } else {
                mNoTrailers.setVisibility(View.INVISIBLE);
                mButtonTrailer1.setVisibility(View.VISIBLE);
                mButtonTrailer1.setOnClickListener(this);
                if (mMovieTrailerUris.size() >= 2) {
                    mButtonTrailer2.setVisibility(View.VISIBLE);
                    mButtonTrailer2.setOnClickListener(this);
                }
                if (mMovieTrailerUris.size() >= 3) {
                    mButtonTrailer3.setVisibility(View.VISIBLE);
                    mButtonTrailer3.setOnClickListener(this);
                }
            }
        }
        else {
            mNoTrailers.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Updates views relating to movie reviews (review button, adapter, etc.)
     * with current data.
     */
    private void updateMovieReviewViews() {
        if ((mMovieReviews != null) && (mMovieReviews.length() > 0)) {
            mNoReviews.setVisibility(View.GONE);
            mReviewAdapter.setMovieReviews(mMovieReviews);
        }
        else {
            mNoReviews.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Starts an intent to play the movie trailer.
     * @param trailerUri The movie trailer Uri, typically a website url.
     */
    private void playTrailer(Uri trailerUri) {
        Intent playTrailerIntent = new Intent(Intent.ACTION_VIEW);
        playTrailerIntent.setData(trailerUri);
        if(playTrailerIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(playTrailerIntent);
        }
    }

    /**
     * Shows movie reviews by updating related view visibility and triggering an AsyncTask
     * to fetch the reviews.
     */
    private void showReviews() {
        mButtonShowReviews.setVisibility(View.GONE);
        mReviewRecycler.setVisibility(View.VISIBLE);
        mReviewsLabel.setVisibility(View.VISIBLE);
        new FetchMovieReviews().execute(mMovieId);
    }

    /**
     * Hides the movie reviews by updating related view visibility.
     */
    private void hideReviews() {
        mButtonShowReviews.setVisibility(View.VISIBLE);
        mReviewRecycler.setVisibility(View.GONE);
        mReviewsLabel.setVisibility(View.GONE);
    }

    /**
     * The FAB drawable indicates whether a movie is marked as favorite or not. This function
     * updates the drawable to represent the current state.
     */
    public void updateFabDrawable() {
        if(mIsFavoriteMovie)
            fab.setImageDrawable(getDrawable(android.R.drawable.btn_star_big_on));
        else
            fab.setImageDrawable(getDrawable(android.R.drawable.btn_star_big_off));
    }

    /**
     * Adds a movie to favorites if it has not been added or removes it if it has.
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
                String logMsg;
                if(mIsFavoriteMovie) {
                    db.movieDao().deleteFavorite(movie);
                    mIsFavoriteMovie = false;
                    logMsg = String.format("Removed movie from favorites (id: %s)", movieIdString);
                }
                else {
                    db.movieDao().addFavorite(movie);
                    mIsFavoriteMovie = true;
                    logMsg = String.format("Added movie to favorites (id: %s)", movieIdString);
                }
                Log.d(TAG, logMsg);
                mViewModel.setFavoriteMovie(mIsFavoriteMovie);
                updateFabDrawable();
            }
        }).start();
    }

    /**
     * AsyncTask that uses the best source for movie get the movie information for the UI.
     * i.e. from the network if available, otherwise from the local database if available.
     */
    private class PopulateUI extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... movieId) {
            FavoritesDB db = FavoritesDB.getInstance(getApplicationContext());
            String movieIdString = Integer.toString(mMovieId);
            String logMsg;
            if(!db.movieDao().getFavorite(movieId[0]).isEmpty()) {
                mIsFavoriteMovie = true;
                logMsg = String.format("Movie %s is in favorites", movieIdString);
            }
            else {
                mIsFavoriteMovie = false;
                logMsg = String.format("Movie %s is NOT in favorites", movieIdString);
            }
            Log.d(TAG, logMsg);
            updateFabDrawable();
            mViewModel.setFavoriteMovie(mIsFavoriteMovie);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            Intent intent = getIntent();
            if(intent.hasExtra(MovieDBUtils.PARAM_POSTER_BYTE))
                mPosterByteArray = intent.getByteArrayExtra(MovieDBUtils.PARAM_POSTER_BYTE);

            mViewModel.setPosterByteArray(mPosterByteArray);
            /*
             * Movie details will be loaded from network when network is available.
             * If network is not available, we'll try to load local data, such as from the
             * favorite movie database.
             */
            if(Network.isNetworkAvailable(getApplicationContext())) {
                new FetchMovieDetails().execute(mMovieId);
                new FetchMovieTrailers().execute(mMovieId);
            }
            else if (mIsFavoriteMovie) {
                if(intent.hasExtra(MovieDBUtils.PARAM_TITLE)) {
                    mMovieTitle = intent.getStringExtra(MovieDBUtils.PARAM_TITLE);
                    mViewModel.setMovieTitle(mMovieTitle);
                }
                if(intent.hasExtra(MovieDBUtils.PARAM_RELEASE)) {
                    mMovieReleaseDate = intent.getStringExtra(MovieDBUtils.PARAM_RELEASE);
                    mViewModel.setMovieReleaseDate(mMovieReleaseDate);
                }
                if(intent.hasExtra(MovieDBUtils.PARAM_RUNTIME)) {
                    mMovieRuntime = intent.getStringExtra(MovieDBUtils.PARAM_RUNTIME);
                    mViewModel.setMovieRuntime(mMovieRuntime);
                }
                if(intent.hasExtra(MovieDBUtils.PARAM_OVERVIEW)) {
                    mMovieOverview = intent.getStringExtra(MovieDBUtils.PARAM_OVERVIEW);
                    mViewModel.setMovieOverview(mMovieOverview);
                }
                updateMovieDetailViews();
                updateMovieTrailerViews();
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
     * AsyncTask that fetches movie details over the network.
     */
    public class FetchMovieDetails extends AsyncTask<Integer, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Integer... movieID) {
            Log.d(TAG, "Fetching movie details...");
            return MovieDBUtils.getMovieDetailsJson(movieID[0]);
        }

        @Override
        protected void onPostExecute(JSONObject movieDetailsJson) {
            super.onPostExecute(movieDetailsJson);
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
                mViewModel.setMovieTitle(mMovieTitle);
                mViewModel.setMovieReleaseDate(mMovieReleaseDate);
                mViewModel.setMovieRuntime(mMovieRuntime);
                mViewModel.setMovieVote(mMovieVote);
                mViewModel.setMovieOverview(mMovieOverview);
                updateMovieDetailViews();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * AsyncTask that fetches movie trailer URIs over the network
     */
    public class FetchMovieTrailers extends AsyncTask<Integer, Void, ArrayList<Uri>> {

        @Override
        protected ArrayList<Uri> doInBackground(Integer... movieID) {
            Log.d(TAG, "Fetching movie trailers...");
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
            mMovieTrailerUris = movieTrailerUris;
            mViewModel.setMovieTrailerUris(mMovieTrailerUris);
            updateMovieTrailerViews();
        }
    }

    /**
     * AsyncTask that fetches movie reviews over the network.
     */
    public class FetchMovieReviews extends AsyncTask<Integer, Void, JSONObject> {

        @Override
        protected JSONObject doInBackground(Integer... movieID) {
            Log.d(TAG, "Fetching movie reviews...");
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

            try {
                mMovieReviews = movieReviewsJson.getJSONArray(MovieDBUtils.PARAM_RESULTS);
                mViewModel.setMovieReviews(mMovieReviews);
                updateMovieReviewViews();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
