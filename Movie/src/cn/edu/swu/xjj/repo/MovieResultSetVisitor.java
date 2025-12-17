package cn.edu.swu.xjj.repo;

import cn.edu.swu.xjj.movie.Movie;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MovieResultSetVisitor implements ResultSetVisitor<Movie> {
    @Override
    public List<Movie> visit(ResultSet rs) throws SQLException {
        List<Movie> movies = new ArrayList<>();
        while( rs.next()) {
            Movie movie = new Movie();
            movie.setMovieId(rs.getInt("movieId"));
            movie.setMovieTitle(rs.getString("movieTitle"));
            movie.setReleaseYear(rs.getDate("releaseYear"));
            movie.setRegion(rs.getString("region"));
            movie.setLanguage(rs.getString("language"));
            movie.setGenre(rs.getString("genre"));
            movie.setPlotSummary(rs.getString("plotSummary"));
            movie.setAverageRating(rs.getDouble("averageRating"));
            movie.setPicture(rs.getString("picture"));
            movies.add(movie);
        }
        return movies;
    }
}