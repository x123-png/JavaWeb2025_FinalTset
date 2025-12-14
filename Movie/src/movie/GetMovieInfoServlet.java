package movie;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

@WebServlet("/getMovieInfo")
public class GetMovieInfoServlet extends HttpServlet {
    ObjectMapper objectMapper = new ObjectMapper();

    public void init() throws ServletException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        this.objectMapper.setDateFormat(dateFormat);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 获取电影ID参数
        String idParam = request.getParameter("movieId");

        if (idParam == null || idParam.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"缺少电影ID参数\"}");
            return;
        }

        int movieId;
        try {
            movieId = Integer.parseInt(idParam);
            if (movieId <= 0) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":\"电影ID必须是正整数\"}");
                return;
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("{\"error\":\"电影ID参数格式错误\"}");
            return;
        }

        ServletContext context = request.getServletContext();
        DatabaseService service = (DatabaseService) context.getAttribute(DatabaseService.CONTEXT_KEY);

        try {
            String sql = "SELECT * FROM movies WHERE movieId = " + movieId;
            List<Movie> movies = service.query(sql, new MovieResultSetVisitor());

            if (movies.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                response.getWriter().write("{\"error\":\"未找到指定的电影\"}");
                return;
            }

            Movie movie = movies.get(0);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            try (Writer writer = response.getWriter()) {
                String json = this.objectMapper.writeValueAsString(movie);
                writer.write(json);
                writer.flush();
            }
        } catch (SQLException e) {
            throw new ServletException("查询电影信息时发生错误", e);
        }
    }
}
