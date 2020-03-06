package com.example.greatfilms.Adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.greatfilms.Favorites.FavoritesDBUtils;
import com.example.greatfilms.R;
import com.example.greatfilms.TheMovieDB.MovieDBUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.MoviesAdapterViewHolder> {

    private JSONArray mMovieData;
    private final MovieAdapterOnClickHandler mClickHandler;

    public interface MovieAdapterOnClickHandler {
        void onClick(View view, JSONObject movie);
    }

    public MovieAdapter(MovieAdapterOnClickHandler clickHandler) {
        mClickHandler = clickHandler;
    }

    /**
     * Cache of the children views for a movie list item.
     */
    public class MoviesAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final ImageView mMoviePosterImageView;

        public MoviesAdapterViewHolder(View view) {
            super(view);
            mMoviePosterImageView = (ImageView) view.findViewById(R.id.iv_movie_poster);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            try {
                JSONObject movie = mMovieData.getJSONObject(adapterPosition);
                mClickHandler.onClick(view, movie);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * This gets called when each new ViewHolder is created. This happens when the RecyclerView
     * is laid out. Enough ViewHolders will be created to fill the screen and allow for scrolling.
     *
     * @param parent The ViewGroup that these ViewHolders are contained within.
     * @param viewType  If your RecyclerView has more than one type of item (which ours doesn't) you
     *                  can use this viewType integer to provide a different layout.
     * @return A new MoviesAdapterViewHolder that holds the View for each list item
     */
    @NonNull
    @Override
    public MoviesAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutIdForListItem, parent, shouldAttachToParentImmediately);
        return new MoviesAdapterViewHolder(view);
    }

    /**
     * OnBindViewHolder is called by the RecyclerView to display the data at the specified
     * position. In this method, we update the contents of the ViewHolder to display the movie
     * poster for this particular position, using the "position" argument that is conveniently
     * passed into us.
     *
     * @param holder The ViewHolder which should be updated to represent the
     *                                  contents of the item at the given position in the data set.
     * @param position                  The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(@NonNull MoviesAdapterViewHolder holder, int position) {
        try {
            byte[] posterBytes = FavoritesDBUtils.posterToBytes(
                    mMovieData.getJSONObject(position).getString(MovieDBUtils.PARAM_POSTER_BYTE));
            Bitmap posterBitmap = BitmapFactory.decodeByteArray(posterBytes, 0, posterBytes.length);
            holder.mMoviePosterImageView.setImageBitmap(posterBitmap);
        } catch (JSONException e1) {
            try {
                Uri posterUri = MovieDBUtils.getMoviePosterUri(
                        mMovieData.getJSONObject(position).getString(MovieDBUtils.PARAM_POSTER_PATH));
                Picasso.get()
                        .load(posterUri)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(holder.mMoviePosterImageView);
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
        }
    }

    /**
     * This method simply returns the number of items to display. It is used behind the scenes
     * to help layout our Views and for animations.
     *
     * @return The number of movie items available
     */
    @Override
    public int getItemCount() {
        if(mMovieData == null)
            return 0;
        else
            return mMovieData.length();
    }

    public void setMovieData(JSONArray movieData) {
        mMovieData = movieData;
        notifyDataSetChanged();
    }
}
