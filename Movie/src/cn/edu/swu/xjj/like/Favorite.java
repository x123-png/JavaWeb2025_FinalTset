package cn.edu.swu.xjj.like;

public class Favorite {
    private int favoriteId;
    private int userId;
    private int movieId;

    public Favorite() {
    }

    public Favorite(int favoriteId, int userId, int movieId) {
        this.favoriteId = favoriteId;
        this.userId = userId;
        this.movieId = movieId;
    }

    public int getFavoriteId() {
        return favoriteId;
    }

    public void setFavoriteId(int favoriteId) {
        this.favoriteId = favoriteId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    @Override
    public String toString() {
        return "Favorite{" +
                "favoriteId=" + favoriteId +
                ", userId=" + userId +
                ", movieId=" + movieId +
                '}';
    }
}