package cn.edu.swu.xjj.movie;

import cn.edu.swu.xjj.repo.DatabaseService;
import cn.edu.swu.xjj.repo.MovieResultSetVisitor;
import cn.edu.swu.xjj.repo.ResultSetVisitor;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.Writer;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;

//@WebServlet(urlPatterns = "/x123-png/moviesService")
@WebServlet(urlPatterns = "/movies")
public class MovieService extends HttpServlet {
    public final static int DEFAULT_PAGE_SIZE = 5;
    public final static int DEFAULT_PAGE = 1;

    ObjectMapper objectMapper = new ObjectMapper();

    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.objectMapper.setDateFormat(dateFormat);
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String inputPage = request.getParameter("page");
        String inputSize = request.getParameter("size");
        int page = (inputPage != null) ? Integer.parseInt(inputPage) : DEFAULT_PAGE;
        int size = (inputSize != null) ? Integer.parseInt(inputSize) : DEFAULT_PAGE_SIZE;

        ServletContext context = request.getServletContext();
        DatabaseService service = (DatabaseService)context.getAttribute(DatabaseService.CONTEXT_KEY);
        try {
            String sql = String.format("SELECT * FROM movies ORDER BY movieId DESC LIMIT %s OFFSET %s", size, (page - 1) * size);
            List<Movie> movies = service.query(sql , new MovieResultSetVisitor());
            int pages = this.totalPages(service, size);

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
        } catch (IOException | SQLException e) {
            throw new ServletException(e);
        }
    }

    private int totalPages(DatabaseService service, int size) throws SQLException {
        String sql = String.format("SELECT CEIL(COUNT(*)/%s) AS pages FROM movies;", size);
        // 编写一个匿名的 ResultSetVisitor 接口的实现类
        List<Integer> pages = service.query(sql, new ResultSetVisitor<Integer>() {
            @Override
            public List<Integer> visit(ResultSet rs) throws SQLException {
                rs.next();
                return List.of(rs.getInt("pages"));
            }
        });
        return pages.get(0);
    }
}