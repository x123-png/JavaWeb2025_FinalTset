package cn.edu.swu.xjj.like;

import cn.edu.swu.xjj.auth.AuthFilter;
import cn.edu.swu.xjj.auth.User;
import cn.edu.swu.xjj.movie.Movie;
import cn.edu.swu.xjj.repo.DatabaseService;
import cn.edu.swu.xjj.repo.MovieResultSetVisitor;
import cn.edu.swu.xjj.utils.HtmlHelper;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.Writer;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/x123-png/myFavorites")
public class MyFavoritesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(AuthFilter.LOGIN_TOKEN_KEY) == null) {
            response.sendRedirect("../login.html");
            return;
        }

        User user = (User) session.getAttribute(AuthFilter.LOGIN_TOKEN_KEY);
        int userId = user.getId(); // 使用User类的getId()方法

        // 验证userId为正数，防止SQL注入
        if (userId <= 0) {
            response.sendRedirect("../login.html");
            return;
        }

        ServletContext context = request.getServletContext();
        DatabaseService service = (DatabaseService) context.getAttribute(DatabaseService.CONTEXT_KEY);

        try {
            // 查询用户的收藏电影
            String sql = "SELECT m.* FROM movies m INNER JOIN user_favorites f ON m.movieId = f.movieId WHERE f.userId = " + userId;
            List<Movie> favoriteMovies = service.query(sql, new MovieResultSetVisitor());

            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            try(Writer writer = response.getWriter()) {
                String table = buildFavoritesTable(favoriteMovies);
                String html = wrapFavoritesHtml(table);
                writer.write(html);
                writer.flush();
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    // 构建收藏电影表格
    private String buildFavoritesTable(List<Movie> movies) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h2>我的收藏</h2>");
        sb.append("<table class='tb-movie'>");
        sb.append("<tr>")
          .append("<th>编号</th><th>片名</th><th>发行日期</th><th>地区</th><th>语言</th><th>类型</th><th>故事梗概</th><th>平均评分</th><th>海报</th><th>操作</th>")
          .append("</tr>");

        String template = """
            <tr>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'>%s</td>
                <td align='center'>
                    <div class="actions">
                        <a href='#' onclick='toggleFavorite(%s, this)' class='favorite-btn'>取消收藏</a>
                        <a href='./updateMovie?movieId=%s' class='update'>查看</a>
                    </div>
                </td>
            </tr>
        """;

        int seqNum = 1;
        for (Movie m : movies) {
            String pictureDisplay = m.getPicture() != null ?
                    "<img src='../upload/" + m.getPicture() + "' width='50' height='50' />"  : "无图片";
            sb.append(String.format(template,
                    seqNum, m.getMovieTitle(), m.getReleaseYear(), m.getRegion(), m.getLanguage(), m.getGenre(), m.getPlotSummary(), m.getAverageRating(),
                    pictureDisplay, m.getMovieId(), m.getMovieId()
            ));
            seqNum++;
        }
        sb.append("</table>");
        return sb.toString();
    }

    // 创建收藏页面的HTML包装器
    private String wrapFavoritesHtml(String content) {
        String template = """
<html>
    <head>
        <meta charset="utf-8">
        <title>我的收藏 - 映界</title>
        <link rel="stylesheet" href="../css/movie.css" />
        <script src="../lib/jquery-3.7.1.min.js"></script>
    </head>
<body>
    <center>
        <div class="page-container">
            <h1>我的收藏</h1>
            <div class="nav-links">
                <a href="../x123-png/add_movie.html"> 添加电影 </a>
                <a href="../x123-png/movies"> 返回电影列表 </a>
                <a href="../x123-png/myFavorites"> 我的收藏 </a>
                <a href="../x123-png/logout"> 退出系统 </a>
            </div>
            %s
        </div>
    </center>
    <script>
        function toggleFavorite(movieId, element) {
            fetch('../toggleFavorite', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                },
                body: 'movieId=' + movieId + '&action=remove'
            })
            .then(response => response.json())
            .then(data => {
                if(data.success) {
                    alert(data.message);
                    // 刷新页面
                    window.location.href = './myFavorites';
                } else {
                    alert('操作失败: ' + data.message);
                }
            })
            .catch(error => {
                alert('网络错误，请重试');
            });
        }
    </script>
</body>
</html>
       """;
        return String.format(template, content);
    }
}