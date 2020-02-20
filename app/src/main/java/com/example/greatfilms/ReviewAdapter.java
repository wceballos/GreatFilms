package com.example.greatfilms;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewAdapterViewHolder> {

    public Context context;
    private JSONArray mMovieReviews;
    private final ReviewAdapterOnClickHandler mClickHandler;

    public ReviewAdapter(ReviewAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    public interface ReviewAdapterOnClickHandler {
        void onClick(View view, JSONObject review);
    }

    @NonNull
    @Override
    public ReviewAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        int layoutIdForListItem = R.layout.review_list_item;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = layoutInflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new ReviewAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewAdapterViewHolder holder, int position) {
        try {
            JSONObject review = mMovieReviews.getJSONObject(position);
            String author = review.getString(MovieDBUtils.PARAM_REVIEW_AUTHOR);
            String text = review.getString(MovieDBUtils.PARAM_REVIEW_TEXT);
            holder.mReviewAuthor.setText(author);
            holder.mReviewText.setText(text);
            holder.mReviewText.setEllipsize(TextUtils.TruncateAt.END);
            holder.mReviewText.setMaxLines(context.getResources().getInteger(R.integer.reviewTextLines));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        if(mMovieReviews == null)
            return 0;
        return mMovieReviews.length();
    }

    public void setMovieReviews(JSONArray reviews) {
        mMovieReviews = reviews;
        notifyDataSetChanged();
    }

    public class ReviewAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mReviewAuthor;
        public final TextView mReviewText;
        public final TextView mClickMoreText;

        public ReviewAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            mReviewAuthor = (TextView) itemView.findViewById(R.id.tv_review_author);
            mReviewText = (TextView) itemView.findViewById(R.id.tv_review_text);
            mClickMoreText = (TextView) itemView.findViewById(R.id.tv_click_more);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            try {
                JSONObject review = mMovieReviews.getJSONObject(adapterPosition);
                mClickHandler.onClick(view, review);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
