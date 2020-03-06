package com.example.greatfilms.Favorites;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;

@Entity(tableName = "movie")
@TypeConverters({Converters.class})
public class MovieEntity {

    /*
     * The local database ID should match the movie ID from The Movie Database API.
     * The movie ID is enough to query The Movie Database for the rest of the information
     * but additional data is stored in the database for offline viewing.
     */
    @PrimaryKey
    private int id;
    // Movie poster in a compressed format
    private byte[] poster;
    // The movie title, used for sorting
    private String title;
    // The year the movie was released
    private String releaseDate;
    // The movie runtime in minutes
    private String runtime;
    // The movie overview or synopsis
    private String overview;
    // Datetime (UTC)
    @ColumnInfo(name = "updated_at")
    private Date updatedAt;

    public MovieEntity(
            int id,
            byte[] poster,
            String title,
            String releaseDate,
            String runtime,
            String overview,
            Date updatedAt) {
        this.id = id;
        this.poster = poster;
        this.title = title;
        this.releaseDate = releaseDate;
        this.runtime = runtime;
        this.overview = overview;
        this.updatedAt = updatedAt == null ? Calendar.getInstance().getTime() : updatedAt;
    }

    public int getId() {
        return id;
    }

    public byte[] getPoster() {
        return poster;
    }

    public String getTitle() {
        return title;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public String getRuntime() {
        return runtime;
    }

    public String getOverview() {
        return overview;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }
}
