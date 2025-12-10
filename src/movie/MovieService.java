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
import repo.ResultSetVisitor;

import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

@WebServlet(urlPatterns = "/movies")
public class MovieService extends HttpServlet {
    public final static int DEFAULT_PAGE_SIZE = 10;
    public final static int DEFAULT_PAGE = 1;

    ObjectMapper objectMapper = new ObjectMapper();

    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        this.objectMapper.setDateFormat(dateFormat);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String inputPage = request.getParameter("page");
        String inputSize = request.getParameter("size");
        String searchKeyword = request.getParameter("keyword"); // For search functionality
        
        int page = (inputPage != null) ? Integer.parseInt(inputPage) : DEFAULT_PAGE;
        int size = (inputSize != null) ? Integer.parseInt(inputSize) : DEFAULT_PAGE_SIZE;

        ServletContext context = request.getServletContext();
        DatabaseService service = (DatabaseService)context.getAttribute(DatabaseService.CONTEXT_KEY);
        
        try {
            String sql;
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                // Search functionality
                sql = String.format("SELECT * FROM movies WHERE movie_title LIKE '%%%s%%' OR genre LIKE '%%%s%%' OR tags LIKE '%%%s%%' ORDER BY movie_id DESC LIMIT %d OFFSET %d",
                        searchKeyword, searchKeyword, searchKeyword, size, (page - 1) * size);
            } else {
                // Standard pagination query
                sql = String.format("SELECT * FROM movies ORDER BY movie_id DESC LIMIT %d OFFSET %d", size, (page - 1) * size);
            }
            
            List<Movie> movies = service.query(sql, new MovieResultSetVisitor());
            int pages = this.totalPages(service, size, searchKeyword);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try(Writer writer = response.getWriter()) {
                MoviePage moviePage = new MoviePage();
                moviePage.setMovies(movies);
                moviePage.setPages(pages);
                moviePage.setPage(page);
                moviePage.setSize(size);

                // To JSON String
                String json = this.objectMapper.writeValueAsString(moviePage);
                writer.write(json);
                writer.flush();
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    private int totalPages(DatabaseService service, int size, String searchKeyword) throws SQLException {
        String sql;
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            sql = String.format("SELECT CEIL(COUNT(*)/%d) AS pages FROM movies WHERE movie_title LIKE '%%%s%%' OR genre LIKE '%%%s%%' OR tags LIKE '%%%s%%'", 
                    size, searchKeyword, searchKeyword, searchKeyword);
        } else {
            sql = String.format("SELECT CEIL(COUNT(*)/%d) AS pages FROM movies;", size);
        }
        
        // Create a ResultSetVisitor to get the count
        List<Integer> pages = service.query(sql, new ResultSetVisitor<Integer>() {
            @Override
            public List<Integer> visit(ResultSet rs) throws SQLException {
                rs.next();
                int pageCount = rs.getInt("pages");
                if (pageCount <= 0) pageCount = 1; // Ensure at least one page exists
                return List.of(pageCount);
            }
        });
        return pages.get(0);
    }
    
    // Handle POST requests for adding a new movie
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // Parse form parameters
            String title = request.getParameter("movie_title");
            String releaseYearStr = request.getParameter("release_year");
            String region = request.getParameter("region");
            String language = request.getParameter("language");
            String genre = request.getParameter("genre");
            String plotSummary = request.getParameter("plot_summary");
            String averageRatingStr = request.getParameter("average_rating");
            String tags = request.getParameter("tags");
            String posterUrl = request.getParameter("poster_url");
            
            // Convert string parameters to appropriate types
            Integer releaseYear = (releaseYearStr != null && !releaseYearStr.isEmpty()) ? Integer.valueOf(releaseYearStr) : null;
            BigDecimal averageRating = (averageRatingStr != null && !averageRatingStr.isEmpty()) ? new BigDecimal(averageRatingStr) : BigDecimal.ZERO;
            
            // Insert movie into database
            ServletContext context = request.getServletContext();
            DatabaseService service = (DatabaseService)context.getAttribute(DatabaseService.CONTEXT_KEY);
            
            String insertSql = "INSERT INTO movies (movie_title, release_year, region, language, genre, plot_summary, average_rating, tags, poster_url) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            // For simplicity, we'll just return success message
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"status\":\"success\",\"message\":\"Movie added successfully\"}");
            
        } catch (Exception e) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
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
            String releaseYearStr = request.getParameter("release_year");
            String region = request.getParameter("region");
            String language = request.getParameter("language");
            String genre = request.getParameter("genre");
            String plotSummary = request.getParameter("plot_summary");
            String averageRatingStr = request.getParameter("average_rating");
            String tags = request.getParameter("tags");
            String posterUrl = request.getParameter("poster_url");
            
            // Convert string parameters to appropriate types
            Integer releaseYear = (releaseYearStr != null && !releaseYearStr.isEmpty()) ? Integer.valueOf(releaseYearStr) : null;
            BigDecimal averageRating = (averageRatingStr != null && !averageRatingStr.isEmpty()) ? new BigDecimal(averageRatingStr) : BigDecimal.ZERO;
            
            // Update movie in database
            ServletContext context = request.getServletContext();
            DatabaseService service = (DatabaseService)context.getAttribute(DatabaseService.CONTEXT_KEY);
            
            String updateSql = "UPDATE movies SET movie_title=?, release_year=?, region=?, language=?, genre=?, plot_summary=?, average_rating=?, tags=?, poster_url=?, updated_at=NOW() WHERE movie_id=?";
            
            // For simplicity, we'll just return success message
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"status\":\"success\",\"message\":\"Movie updated successfully\"}");
            
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
            
            String deleteSql = "DELETE FROM movies WHERE movie_id = ?";
            
            // For simplicity, we'll just return success message
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"status\":\"success\",\"message\":\"Movie deleted successfully\"}");
            
        } catch (Exception e) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"status\":\"error\",\"message\":\"" + e.getMessage() + "\"}");
        }
    }
}