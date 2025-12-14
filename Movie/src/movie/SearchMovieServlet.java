package movie;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import repo.DatabaseService;
import repo.MovieResultSetVisitor;
import utils.HtmlHelper;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.List;

@WebServlet(urlPatterns = "/searchMovie")
public class SearchMovieServlet extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        String content = request.getParameter("content");
        ServletContext context = request.getServletContext();
        DatabaseService dbService = (DatabaseService)context.getAttribute(DatabaseService.CONTEXT_KEY);

        String sql = "select * from movies where " +
                "movieTitle like '%" + content + "%' or " +
                "releaseYear like '%" + content + "%' or " +
                "region like '%" + content + "%' or " +
                "language like '%" + content + "%' or " +
                "genre like '%" + content + "%'";
        try {
            List<Movie> movies = dbService.query(sql, new MovieResultSetVisitor());
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            try(Writer writer = response.getWriter()) {
                String table = HtmlHelper.buildMoviessTable(movies);
                String html = HtmlHelper.wrapHtml(table);
                writer.write(html);
                writer.flush();
            }
        } catch (IOException | SQLException e) {
            throw new ServletException(e);
        }
    }
}
