package com.example.greatfilms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

    ReviewAdapter mReviewAdapter;

    public int mMovieId;

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

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mReviewRecycler.setLayoutManager(linearLayoutManager);
        mReviewAdapter = new ReviewAdapter(this);
        mReviewRecycler.setAdapter(mReviewAdapter);

        Intent intent = getIntent();

        if(intent != null) {
            if(intent.hasExtra(MovieDBUtils.PARAM_POSTER)) {
                int imageSize = intent.getByteArrayExtra(MovieDBUtils.PARAM_POSTER).length;

                Bitmap posterBitmap = BitmapFactory.decodeByteArray(intent.getByteArrayExtra(MovieDBUtils.PARAM_POSTER),0, imageSize);
                mPosterDisplay.setImageBitmap(posterBitmap);
            }
            if(intent.hasExtra(MovieDBUtils.PARAM_ID)) {
                mMovieId = intent.getIntExtra(MovieDBUtils.PARAM_ID, 0);
                new FetchMovieDetails().execute(mMovieId);
                new FetchMovieTrailers().execute(mMovieId);
                mButtonShowReviews.setOnClickListener(this);
            }
        }
    }

    // TODO: Read the long review in a new activity
    @Override
    public void onClick(View view, JSONObject review) {
        try {
            String urlString = review.getString(MovieDBUtils.PARAM_REVIEW_URL);
            Uri uri = Uri.parse(urlString);
            Intent openReviewUrlIntent = new Intent(Intent.ACTION_VIEW, uri);
            if(openReviewUrlIntent.resolveActivity(getPackageManager()) != null)
                startActivity(openReviewUrlIntent);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();
        if(id == mButtonShowReviews.getId()) {
            mButtonShowReviews.setVisibility(View.GONE);
            mReviewRecycler.setVisibility(View.VISIBLE);
            mReviewsLabel.setVisibility(View.VISIBLE);
            new FetchMovieReviews().execute(mMovieId);
        }
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
                setTitle(movieDetailsJson.getString(MovieDBUtils.PARAM_TITLE));
                String releaseYear = movieDetailsJson
                        .getString(MovieDBUtils.PARAM_RELEASE).substring(0,4);
                mReleaseDisplay.setText(releaseYear);
                String runtime = getString(R.string.movie_runtime,
                        movieDetailsJson.getString(MovieDBUtils.PARAM_RUNTIME));
                mRuntimeDisplay.setText(runtime);
                String vote = getString(R.string.movie_vote,
                        movieDetailsJson.getString(MovieDBUtils.PARAM_VOTES));
                mVoteDisplay.setText(vote);
                mOverviewDisplay.setText(movieDetailsJson.getString(MovieDBUtils.PARAM_OVERVIEW));
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
                public void onClick(View button) {
                    Intent playTrailerIntent = new Intent(Intent.ACTION_VIEW);
                    if(button == mButtonTrailer1)
                        playTrailerIntent.setData(movieTrailerUris.get(0));
                    else if(button == mButtonTrailer2)
                        playTrailerIntent.setData(movieTrailerUris.get(1));
                    else if(button == mButtonTrailer3)
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
