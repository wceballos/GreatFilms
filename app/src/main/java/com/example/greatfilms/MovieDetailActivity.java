package com.example.greatfilms;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

import static android.widget.Toast.makeText;

public class MovieDetailActivity extends AppCompatActivity {

    private ImageView mPosterDisplay;
    private TextView mTitleDisplay;
    private TextView mReleaseDisplay;
    private TextView mVoteDisplay;
    private TextView mOverviewDisplay;
    private Button mButtonTrailer1;
    private Button mButtonTrailer2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);
        androidx.appcompat.widget.Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        mPosterDisplay = (ImageView) findViewById(R.id.iv_movie_poster);
        //mTitleDisplay = (TextView) findViewById(R.id.tv_movie_title);
        mReleaseDisplay = (TextView) findViewById(R.id.tv_movie_release);
        mVoteDisplay = (TextView) findViewById(R.id.tv_movie_vote);
        mOverviewDisplay = (TextView) findViewById(R.id.tv_movie_overview);
        mButtonTrailer1 = (Button) findViewById(R.id.btn_trailer1);
        mButtonTrailer2 = (Button) findViewById(R.id.btn_trailer2);

        Intent intent = getIntent();

        if(intent != null) {
            if(intent.hasExtra("TITLE")) {
                //mTitleDisplay.setText(intent.getStringExtra("TITLE"));
                setTitle(intent.getStringExtra("TITLE"));
            }
            if(intent.hasExtra("RELEASE")) {
                mReleaseDisplay.setText(intent.getStringExtra("RELEASE").substring(0,4));
            }
            if(intent.hasExtra("VOTE")) {
                String vote = intent.getStringExtra("VOTE");
                vote += "/10";
                mVoteDisplay.setText(vote);
            }
            if(intent.hasExtra("OVERVIEW")) {
                mOverviewDisplay.setText(intent.getStringExtra("OVERVIEW"));
            }
            if(intent.hasExtra("POSTER")) {
                int imageSize = intent.getByteArrayExtra("POSTER").length;
                Bitmap posterBitmap = BitmapFactory.decodeByteArray(intent.getByteArrayExtra("POSTER"),0, imageSize);
                mPosterDisplay.setImageBitmap(posterBitmap);
            }
            if(intent.hasExtra("ID")) {
                int movieID = intent.getIntExtra("ID", 0);
                new FetchMovieTrailers().execute(movieID);
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
            return MovieDBUtils.getMovieTrailerUris(movieID[0]);
        }

        @Override
        protected void onPostExecute(final ArrayList<Uri> movieTrailerUris) {
            super.onPostExecute(movieTrailerUris);
            if(movieTrailerUris == null) {
                String toastMsg = "Failed to load trailers";
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
                    else
                        return;
                    if(playTrailerIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(playTrailerIntent);
                    }
                }
            };
            if(movieTrailerUris.size() >= 1) {
                mButtonTrailer1.setVisibility(View.VISIBLE);
                mButtonTrailer1.setOnClickListener(trailerButtonClickListener);
            }
            if(movieTrailerUris.size() >= 2) {
                mButtonTrailer2.setVisibility(View.VISIBLE);
                mButtonTrailer2.setOnClickListener(trailerButtonClickListener);
            }
        }
    }
}
