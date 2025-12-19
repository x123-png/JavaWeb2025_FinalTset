package cn.edu.swu.xjj.movie;

import cn.edu.swu.xjj.repo.DatabaseService;
import cn.edu.swu.xjj.repo.MovieResultSetVisitor;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(urlPatterns = "/x123-png/deleteMovie")
public class DeleteMovie extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("movieId");

        ServletContext context = this.getServletContext();
        DatabaseService dbService = (DatabaseService)context.getAttribute(DatabaseService.CONTEXT_KEY);

        // 首先获取图片文件名
        String selectSQL = "select picture from movies where movieId=" + id;
        String pictureFileName = null;
        try {
            // 创建一个简单的 ResultSetVisitor 来获取 picture 值
            List<String> pictures = dbService.query(selectSQL, rs -> {
                List<String> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(rs.getString("picture"));
                }
                return result;
            });

            if (!pictures.isEmpty()) {
                pictureFileName = pictures.get(0);
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        // 删除数据库记录
        String deleteSQL = "delete from movies where movieId=" + id;
        try {
            dbService.execute(deleteSQL);
        } catch (SQLException e) {
            throw new ServletException(e);
        }

        // 如果有图片文件，则删除它
        if (pictureFileName != null) {
            String uploadPath = Paths.get(request.getServletContext().getRealPath("./"), "upload").toString();
            File pictureFile = new File(uploadPath, pictureFileName);
            if (pictureFile.exists()) {
                pictureFile.delete();
            }
        }

        response.sendRedirect("./movies");
    }
}
