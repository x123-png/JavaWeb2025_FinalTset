package movie;

import repo.DatabaseService;
import repo.MovieResultSetVisitor;

import java.sql.SQLException;
import java.util.List;

public class MovieTest {
    public static void main(String[] args) {
        try {
            // Initialize database service
            DatabaseService dbService = DatabaseService.getInstance();
            dbService.init();
            
            // Query all movies
            String sql = "SELECT * FROM movies ORDER BY movie_id DESC LIMIT 10";
            List<Movie> movies = dbService.query(sql, new MovieResultSetVisitor());
            
            System.out.println("Movies retrieved: " + movies.size());
            for (Movie movie : movies) {
                System.out.println(movie);
            }
            
            // Close the database connection
            dbService.closeDataSource();
            
        } catch (SQLException e) {
            System.err.println("Error connecting to database: " + e.getMessage());
            e.printStackTrace();
        }
    }
}