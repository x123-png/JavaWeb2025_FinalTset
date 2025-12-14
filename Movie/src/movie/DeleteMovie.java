package movie;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import repo.DatabaseService;
import repo.MovieResultSetVisitor;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

@WebServlet(urlPatterns = "/deleteMovie")
public class DeleteMovie extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");

        ServletContext context = this.getServletContext();
        DatabaseService dbService = (DatabaseService)context.getAttribute(DatabaseService.CONTEXT_KEY);

        // 首先获取图书信息以获取图片文件名
        String selectSQL = "select picture from movies where id=" + id;
        try {
            List<Movie> movies = dbService.query(selectSQL, new MovieResultSetVisitor());
            if (!movies.isEmpty() && movies.get(0).getPicture() != null) {
                String pictureFileName = movies.get(0).getPicture();

                // 删除数据库记录
                String deleteSQL = "delete from movies where id=" + id;
                dbService.execute(deleteSQL);

                // 删除对应的图片文件
                String uploadPath = Paths.get(request.getServletContext().getRealPath("./"), "upload").toString();
                File pictureFile = new File(uploadPath, pictureFileName);
                if (pictureFile.exists()) {
                    pictureFile.delete();
                }
            } else {
                // 如果没有图片或者图书不存在，直接删除记录
                String deleteSQL = "delete from movies where id=" + id;
                dbService.execute(deleteSQL);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        response.sendRedirect("./movies");
    }
}
