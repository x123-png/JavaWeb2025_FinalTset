package movie;

import java.math.BigDecimal;
import java.util.Date;

public class Movie {
    private int movieId;
    private String movieTitle;
    private Integer releaseYear;
    private String region;
    private String language;
    private String genre;
    private String plotSummary;
    private BigDecimal averageRating;
    private String picture;
    private Date createdAt;
    private Date updatedAt;

    public Movie() {
    }

    public Movie(int movieId, String movieTitle, Integer releaseYear, String region,
                 String language, String genre, String plotSummary, BigDecimal averageRating,
                 String picture, Date createdAt, Date updatedAt) {
        this.movieId = movieId;
        this.movieTitle = movieTitle;
        this.releaseYear = releaseYear;
        this.region = region;
        this.language = language;
        this.genre = genre;
        this.plotSummary = plotSummary;
        this.averageRating = averageRating;
        this.picture = picture;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getPlotSummary() {
        return plotSummary;
    }

    public void setPlotSummary(String plotSummary) {
        this.plotSummary = plotSummary;
    }

    public BigDecimal getAverageRating() {
        return averageRating;
    }

    public void setAverageRating(BigDecimal averageRating) {
        this.averageRating = averageRating;
    }


    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "movieId=" + movieId +
                ", movieTitle='" + movieTitle + '\'' +
                ", releaseYear=" + releaseYear +
                ", region='" + region + '\'' +
                ", language='" + language + '\'' +
                ", genre='" + genre + '\'' +
                ", plotSummary='" + plotSummary + '\'' +
                ", averageRating=" + averageRating +
                ", picture='" + picture + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}