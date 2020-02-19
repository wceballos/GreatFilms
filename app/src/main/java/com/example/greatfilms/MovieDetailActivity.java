package com.example.greatfilms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static android.widget.Toast.makeText;

public class MovieDetailActivity extends AppCompatActivity {

    private ImageView mPosterDisplay;
    private TextView mReleaseDisplay;
    private TextView mRuntimeDisplay;
    private TextView mVoteDisplay;
    private TextView mOverviewDisplay;
    private TextView mNoTrailers;
    private Button mButtonTrailer1;
    private Button mButtonTrailer2;
    private Button mButtonTrailer3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mPosterDisplay   = (ImageView) findViewById(R.id.iv_movie_poster);
        mReleaseDisplay  = (TextView)  findViewById(R.id.tv_movie_release);
        mRuntimeDisplay  = (TextView)  findViewById(R.id.tv_movie_runtime);
        mVoteDisplay     = (TextView)  findViewById(R.id.tv_movie_vote);
        mOverviewDisplay = (TextView)  findViewById(R.id.tv_movie_overview);
        mNoTrailers      = (TextView)  findViewById(R.id.tv_no_trailers);
        mButtonTrailer1  = (Button)    findViewById(R.id.btn_trailer1);
        mButtonTrailer2  = (Button)    findViewById(R.id.btn_trailer2);
        mButtonTrailer3  = (Button)    findViewById(R.id.btn_trailer3);

        Intent intent = getIntent();

        if(intent != null) {
            if(intent.hasExtra(MovieDBUtils.PARAM_POSTER)) {
                int imageSize = intent.getByteArrayExtra(MovieDBUtils.PARAM_POSTER).length;

                Bitmap posterBitmap = BitmapFactory.decodeByteArray(intent.getByteArrayExtra(MovieDBUtils.PARAM_POSTER),0, imageSize);
                mPosterDisplay.setImageBitmap(posterBitmap);
            }
            if(intent.hasExtra(MovieDBUtils.PARAM_ID)) {
                int movieID = intent.getIntExtra(MovieDBUtils.PARAM_ID, 0);
                new FetchMovieDetails().execute(movieID);
                new FetchMovieTrailers().execute(movieID);
            }
        }
    }

    public class FetchMovieDetails extends AsyncTask<Integer, Void, JSONObject> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            //mLoadingIndicator.setVisibility(View.VISIBLE);
        }

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
        protected void onPreExecute() {
            super.onPreExecute();
        }

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
}
