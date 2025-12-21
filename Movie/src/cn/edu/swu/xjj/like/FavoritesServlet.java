package cn.edu.swu.xjj.like;

import cn.edu.swu.xjj.auth.AuthFilter;
import cn.edu.swu.xjj.auth.User;
import cn.edu.swu.xjj.movie.Movie;
import cn.edu.swu.xjj.repo.DatabaseService;
import cn.edu.swu.xjj.utils.HtmlHelper;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/x123-png/toggleFavorite")
public class FavoritesServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(AuthFilter.LOGIN_TOKEN_KEY) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"success\": false, \"message\": \"未登录\"}");
            return;
        }

        // 从登录令牌中获取用户ID
        User user = (User) session.getAttribute(AuthFilter.LOGIN_TOKEN_KEY);
        int userId = user.getId(); // 使用User类的getId()方法

        String movieIdStr = request.getParameter("movieId");
        String action = request.getParameter("action"); // add or remove

        if (movieIdStr == null || action == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"message\": \"参数错误\"}");
            return;
        }

        // 验证输入为数字，防止SQL注入
        int movieId;
        try {
            movieId = Integer.parseInt(movieIdStr);
            if (movieId <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"message\": \"参数错误\"}");
            return;
        }

        if (!"add".equals(action) && !"remove".equals(action)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"success\": false, \"message\": \"无效操作\"}");
            return;
        }

        ServletContext context = request.getServletContext();
        DatabaseService service = (DatabaseService) context.getAttribute(DatabaseService.CONTEXT_KEY);

        try {
            if ("add".equals(action)) {
                // 检查是否已经收藏
                List<Favorite> existingFavorites = service.query(
                    "SELECT * FROM user_favorites WHERE userId = " + userId + " AND movieId = " + movieId,
                    new FavoriteResultSetVisitor());

                if (existingFavorites.isEmpty()) {
                    // 添加收藏
                    service.execute("INSERT INTO user_favorites (userId, movieId) VALUES (" + userId + ", " + movieId + ")");
                    out.print("{\"success\": true, \"message\": \"收藏成功\", \"action\": \"remove\"}");
                } else {
                    // 已经收藏，返回对应状态
                    out.print("{\"success\": true, \"message\": \"已收藏\", \"action\": \"remove\"}");
                }
            } else if ("remove".equals(action)) {
                // 取消收藏
                service.execute("DELETE FROM user_favorites WHERE userId = " + userId + " AND movieId = " + movieId);
                out.print("{\"success\": true, \"message\": \"取消收藏\", \"action\": \"add\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"success\": false, \"message\": \"操作失败: " + e.getMessage() + "\"}");
        }
    }

    // 查询方法用于获取收藏状态
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(AuthFilter.LOGIN_TOKEN_KEY) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            out.print("{\"error\": \"未登录\"}");
            return;
        }

        User user = (User) session.getAttribute(AuthFilter.LOGIN_TOKEN_KEY);
        int userId = user.getId(); // 使用User类的getId()方法

        String movieIdStr = request.getParameter("movieId");

        if (movieIdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"参数错误\"}");
            return;
        }

        // 验证输入为数字，防止SQL注入
        int movieId;
        try {
            movieId = Integer.parseInt(movieIdStr);
            if (movieId <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("{\"error\": \"参数错误\"}");
            return;
        }

        ServletContext context = request.getServletContext();
        DatabaseService service = (DatabaseService) context.getAttribute(DatabaseService.CONTEXT_KEY);

        try {
            List<Favorite> favorites = service.query(
                "SELECT * FROM user_favorites WHERE userId = " + userId + " AND movieId = " + movieId,
                new FavoriteResultSetVisitor());

            if (favorites.isEmpty()) {
                out.print("{\"isFavorite\": false, \"action\": \"add\"}");
            } else {
                out.print("{\"isFavorite\": true, \"action\": \"remove\"}");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            out.print("{\"error\": \"查询失败\"}");
        }
    }
}