package movie;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import repo.DatabaseService;
import repo.MovieResultSetVisitor;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

@WebServlet(urlPatterns = "/movies")
public class MovieService extends HttpServlet {

    ObjectMapper objectMapper = new ObjectMapper();

    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.objectMapper.setDateFormat(dateFormat);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String searchKeyword = request.getParameter("keyword"); // For search functionality
        String acceptHeader = request.getHeader("Accept");

        ServletContext context = request.getServletContext();
        DatabaseService service = (DatabaseService)context.getAttribute(DatabaseService.CONTEXT_KEY);

        try {
            String sql;
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                // Search functionality
                sql = String.format("SELECT * FROM movies WHERE movie_title LIKE '%%%s%%' OR genre LIKE '%%%s%%' ORDER BY movie_id DESC",
                        searchKeyword, searchKeyword);
            } else {
                // Simple query without pagination
                sql = "SELECT * FROM movies ORDER BY movie_id DESC";
            }

            List<Movie> movies = service.query(sql, new MovieResultSetVisitor());

            // Check if request is for JSON or HTML
            if (acceptHeader != null && acceptHeader.contains("application/json")) {
                // Return JSON for AJAX requests
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                try(Writer writer = response.getWriter()) {
                    // Return the list of movies directly
                    String json = this.objectMapper.writeValueAsString(movies);
                    writer.write(json);
                    writer.flush();
                }
            } else {
                // Return HTML table for direct browser access
                response.setContentType("text/html");
                response.setCharacterEncoding("UTF-8");
                try(Writer writer = response.getWriter()) {
                    String tableHtml = utils.MovieHelper.buildMoviesTable(movies);
                    writer.write(tableHtml);
                    writer.flush();
                }
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
    
    // Handle POST requests for adding a new movie
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Read JSON data from request
            StringBuilder buffer = new StringBuilder();
            String line;
            try (java.io.BufferedReader reader = request.getReader()) {
                while ((line = reader.readLine()) != null) {
                    buffer.append(line);
                }
            }

            // Parse the JSON data
            Movie movie = this.objectMapper.readValue(buffer.toString(), Movie.class);

            // Insert movie into database
            ServletContext context = request.getServletContext();
            DatabaseService service = (DatabaseService)context.getAttribute(DatabaseService.CONTEXT_KEY);

            // Build SQL with proper NULL handling
            String title = (movie.getMovieTitle() != null) ? movie.getMovieTitle().replace("'", "''") : "";
            String releaseYear = (movie.getReleaseYear() != null) ? movie.getReleaseYear().toString() : "NULL";
            String region = (movie.getRegion() != null) ? movie.getRegion().replace("'", "''") : "";
            String language = (movie.getLanguage() != null) ? movie.getLanguage().replace("'", "''") : "";
            String genre = (movie.getGenre() != null) ? movie.getGenre().replace("'", "''") : "";
            String plotSummary = (movie.getPlotSummary() != null) ? movie.getPlotSummary().replace("'", "''") : "";
            String averageRating = (movie.getAverageRating() != null) ? movie.getAverageRating().toString() : "NULL";
            String posterUrl = (movie.getPosterUrl() != null) ? movie.getPosterUrl().replace("'", "''") : "";

            // Format SQL with values to bypass prepared statement issue in current setup
            String insertSql = String.format(
                "INSERT INTO movies (movie_title, release_year, region, language, genre, plot_summary, average_rating, poster_url) VALUES ('%s', %s, '%s', '%s', '%s', '%s', %s, '%s')",
                title, releaseYear, region, language, genre, plotSummary, averageRating, posterUrl
            );

            // 将"NULL"替换为真正的NULL（在SQL中不需要引号）
            insertSql = insertSql.replace(", 'NULL',", ", NULL,").replace(", 'NULL')", ", NULL)");

            System.out.println("Insert SQL: " + insertSql); // Debugging line

            int result = service.update(insertSql);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            if(result > 0) {
                response.getWriter().write("{\"status\":\"success\",\"message\":\"Movie added successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"status\":\"error\",\"message\":\"Failed to add movie\"}");
            }

        } catch (Exception e) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
            e.printStackTrace(); // Print stack trace for debugging
        }
    }
    
    // Handle PUT requests for updating a movie
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String movieIdStr = request.getParameter("movie_id");
            int movieId = Integer.parseInt(movieIdStr);

            // Parse form parameters
            String title = request.getParameter("movie_title");
            String releaseYearParam = request.getParameter("release_year");
            String region = request.getParameter("region");
            String language = request.getParameter("language");
            String genre = request.getParameter("genre");
            String plotSummary = request.getParameter("plot_summary");
            String averageRatingParam = request.getParameter("average_rating");
            String posterUrl = request.getParameter("poster_url");

            // Convert string parameters to appropriate types
            Integer releaseYear = (releaseYearParam != null && !releaseYearParam.isEmpty()) ? Integer.valueOf(releaseYearParam) : null;
            BigDecimal averageRating = (averageRatingParam != null && !averageRatingParam.isEmpty()) ? new BigDecimal(averageRatingParam) : BigDecimal.ZERO;

            // Update movie in database
            ServletContext context = request.getServletContext();
            DatabaseService service = (DatabaseService)context.getAttribute(DatabaseService.CONTEXT_KEY);

            // Build SQL with proper NULL handling
            String titleUpdateStr = title != null ? title.replace("'", "''") : "";
            String releaseYearUpdateStr = (releaseYear != null) ? releaseYear.toString() : "NULL";
            String regionUpdateStr = region != null ? region.replace("'", "''") : "";
            String languageUpdateStr = language != null ? language.replace("'", "''") : "";
            String genreUpdateStr = genre != null ? genre.replace("'", "''") : "";
            String plotSummaryUpdateStr = plotSummary != null ? plotSummary.replace("'", "''") : "";
            String averageRatingUpdateStr = (averageRating != null) ? averageRating.toString() : "NULL";
            String posterUrlUpdateStr = posterUrl != null ? posterUrl.replace("'", "''") : "";

            // Format SQL with values to bypass prepared statement issue in current setup
            String updateSql = String.format(
                "UPDATE movies SET movie_title='%s', release_year=%s, region='%s', language='%s', genre='%s', plot_summary='%s', average_rating=%s, poster_url='%s', updated_at=NOW() WHERE movie_id=%d",
                titleUpdateStr, releaseYearUpdateStr, regionUpdateStr, languageUpdateStr, genreUpdateStr, plotSummaryUpdateStr, averageRatingUpdateStr, posterUrlUpdateStr, movieId
            );

            // 将"NULL"替换为真正的NULL（在SQL中不需要引号）
            updateSql = updateSql.replace(", 'NULL',", ", NULL,").replace(", 'NULL')", ", NULL)").replace(", 'NULL' WHERE", ", NULL WHERE");

            System.out.println("Update SQL: " + updateSql); // Debugging line

            int result = service.update(updateSql);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            if(result > 0) {
                response.getWriter().write("{\"status\":\"success\",\"message\":\"Movie updated successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"status\":\"error\",\"message\":\"Failed to update movie\"}");
            }

        } catch (Exception e) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }
    
    // Handle DELETE requests for deleting a movie
    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String movieIdStr = request.getParameter("movie_id");
            int movieId = Integer.parseInt(movieIdStr);

            // Delete movie from database
            ServletContext context = request.getServletContext();
            DatabaseService service = (DatabaseService)context.getAttribute(DatabaseService.CONTEXT_KEY);

            String deleteSql = String.format("DELETE FROM movies WHERE movie_id = %d", movieId);

            int result = service.update(deleteSql);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            if(result > 0) {
                response.getWriter().write("{\"status\":\"success\",\"message\":\"Movie deleted successfully\"}");
            } else {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("{\"status\":\"error\",\"message\":\"Failed to delete movie\"}");
            }

        } catch (Exception e) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}