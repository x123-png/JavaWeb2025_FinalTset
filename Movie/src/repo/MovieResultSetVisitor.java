package repo;

import movie.Movie;

import java.math.BigDecimal;
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
            movie.setMovieId(rs.getInt("movie_id"));
            movie.setMovieTitle(rs.getString("movie_title"));
            movie.setReleaseYear(rs.getObject("release_year", Integer.class));
            movie.setRegion(rs.getString("region"));
            movie.setLanguage(rs.getString("language"));
            movie.setGenre(rs.getString("genre"));
            movie.setPlotSummary(rs.getString("plot_summary"));
            movie.setAverageRating(rs.getObject("average_rating", BigDecimal.class));
            movie.setPosterUrl(rs.getString("poster_url"));
            movie.setCreatedAt(rs.getTimestamp("created_at"));
            movie.setUpdatedAt(rs.getTimestamp("updated_at"));
            movies.add(movie);
        }
        return movies;
    }
}