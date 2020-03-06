package com.example.greatfilms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.example.greatfilms.TheMovieDB.MovieDBUtils;

public class MovieReviewActivity extends AppCompatActivity {

    private TextView mReviewAuthor;
    private TextView mReviewText;
    private TextView mClickMoreText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_review);

        mReviewAuthor  = (TextView) findViewById(R.id.tv_review_author);
        mReviewText    = (TextView) findViewById(R.id.tv_review_text);
        mClickMoreText = (TextView) findViewById(R.id.tv_click_more);

        mClickMoreText.setVisibility(View.INVISIBLE);

        Intent intent = getIntent();
        if(intent != null) {
            if(intent.hasExtra(MovieDBUtils.PARAM_TITLE)) {
                setTitle(intent.getStringExtra(MovieDBUtils.PARAM_TITLE));
            }
            if(intent.hasExtra(MovieDBUtils.PARAM_REVIEW_AUTHOR)) {
                mReviewAuthor.setText(intent.getStringExtra(MovieDBUtils.PARAM_REVIEW_AUTHOR));
            }
            if(intent.hasExtra(MovieDBUtils.PARAM_REVIEW_TEXT)) {
                mReviewText.setText(intent.getStringExtra(MovieDBUtils.PARAM_REVIEW_TEXT));
            }
        }
    }
}
