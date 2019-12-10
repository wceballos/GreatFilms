package com.example.greatfilms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MovieDetailActivity extends AppCompatActivity {

    private ImageView mPosterDisplay;
    private TextView mTitleDisplay;
    private TextView mReleaseDisplay;
    private TextView mVoteDisplay;
    private TextView mOverviewDisplay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_detail);

        mPosterDisplay = (ImageView) findViewById(R.id.iv_movie_poster);
        mTitleDisplay = (TextView) findViewById(R.id.tv_movie_title);
        mReleaseDisplay = (TextView) findViewById(R.id.tv_movie_release);
        mVoteDisplay = (TextView) findViewById(R.id.tv_movie_vote);
        mOverviewDisplay = (TextView) findViewById(R.id.tv_movie_overview);

        Intent intent = getIntent();

        if(intent != null) {
            if(intent.hasExtra("TITLE")) {
                mTitleDisplay.setText(intent.getStringExtra("TITLE"));
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
        }
    }
}
