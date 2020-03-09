package com.example.greatfilms.ViewModels;

import android.net.Uri;

import androidx.lifecycle.ViewModel;

import org.json.JSONArray;

import java.util.ArrayList;

public class MovieDetailViewModel extends ViewModel {

    private int movieId;
    private byte[] posterByteArray;
    private String movieTitle;
    private String movieReleaseDate;
    private String movieRuntime;
    private String movieOverview;
    private String movieVote;
    private boolean favoriteMovie;
    private ArrayList<Uri> movieTrailerUris;
    private JSONArray movieReviews;

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public byte[] getPosterByteArray() {
        return posterByteArray;
    }

    public void setPosterByteArray(byte[] posterByteArray) {
        this.posterByteArray = posterByteArray;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getMovieReleaseDate() {
        return movieReleaseDate;
    }

    public void setMovieReleaseDate(String movieReleaseDate) {
        this.movieReleaseDate = movieReleaseDate;
    }

    public String getMovieRuntime() {
        return movieRuntime;
    }

    public void setMovieRuntime(String movieRuntime) {
        this.movieRuntime = movieRuntime;
    }

    public String getMovieOverview() {
        return movieOverview;
    }

    public void setMovieOverview(String movieOverview) {
        this.movieOverview = movieOverview;
    }

    public String getMovieVote() {
        return movieVote;
    }

    public void setMovieVote(String movieVote) {
        this.movieVote = movieVote;
    }

    public boolean isFavoriteMovie() {
        return favoriteMovie;
    }

    public void setFavoriteMovie(boolean favoriteMovie) {
        this.favoriteMovie = favoriteMovie;
    }

    public ArrayList<Uri> getMovieTrailerUris() {
        return movieTrailerUris;
    }

    public void setMovieTrailerUris(ArrayList<Uri> movieTrailerUris) {
        this.movieTrailerUris = movieTrailerUris;
    }

    public JSONArray getMovieReviews() {
        return movieReviews;
    }

    public void setMovieReviews(JSONArray movieReviews) {
        this.movieReviews = movieReviews;
    }
}
