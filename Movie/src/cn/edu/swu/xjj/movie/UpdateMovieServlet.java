package cn.edu.swu.xjj.movie;

import cn.edu.swu.xjj.repo.DatabaseService;
import cn.edu.swu.xjj.repo.MovieResultSetVisitor;
import cn.edu.swu.xjj.utils.HtmlHelper;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.jakarta.servlet5.JakartaServletDiskFileUpload;
import org.apache.commons.fileupload2.jakarta.servlet5.JakartaServletFileUpload;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;

@WebServlet(urlPatterns = "/updateMovie")
public class UpdateMovieServlet extends HttpServlet {

    // 上传文件存储目录
    private static final String UPLOAD_DIRECTORY = "upload";

    // 上传配置
    private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 3;  // 3MB
    private static final int MAX_FILE_SIZE      = 1024 * 1024 * 40; // 40MB
    private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 50; // 50MB

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("movieId");

        ServletContext context = this.getServletContext();
        DatabaseService dbService = (DatabaseService)context.getAttribute(DatabaseService.CONTEXT_KEY);
        String selectSQL = String.format("select * from movies where movieId = %s", id);

        try {
            List<Movie> movies = dbService.query(selectSQL, new MovieResultSetVisitor());
            Movie m = movies.get(0);
            String template = """
    <form action="./updateMovie" method="post" enctype="multipart/form-data" style="line-height:3em">
        <input type="hidden" name="movieId" value="%s"><br>
        电影标题：<input type="text" name="movieTitle" value="%s"><br>
        发行年份：<input type="text" name="releaseYear" value="%s"><br>
        地区：<input type="text" name="region" value="%s"><br>
        语言：<input type="text" name="language" value="%s"><br>
        类型：<input type="text" name="genre" value="%s"><br>
        剧情简介：<textarea rows="3" cols="22" name="plotSummary">%s</textarea><br>
        平均评分：<input type="text" name="averageRating" value="%s"><br>
        海报图片：<input type="file" name="picture"><br>
        (当前海报: %s)<br><br>
        <input type="submit" value="修改电影">
        &nbsp;&nbsp;&nbsp;&nbsp;
        <a href="./movies"><input type="button" value="返回电影列表"></a>
    </form>
        """;
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            try(Writer writer = response.getWriter()) {
                String form = String.format(template,
                        m.getMovieId(), m.getMovieTitle(), m.getReleaseYear(), m.getRegion(), m.getLanguage(), m.getGenre(), m.getPlotSummary(), m.getAverageRating(), m.getPicture() != null ? m.getPicture() : "无");
                String html = HtmlHelper.wrapHtml(form);
                writer.write(html);
                writer.flush();
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 检测是否为多媒体上传
        if (!JakartaServletFileUpload.isMultipartContent(request)) {
            // 如果不是则停止
            PrintWriter writer = response.getWriter();
            writer.println("Error: 表单必须包含 enctype=multipart/form-data");
            writer.flush();
            return;
        }

        try {
            // 配置上传参数
            ServletContext servletContext = this.getServletConfig().getServletContext();
            File repository = (File) servletContext.getAttribute("jakarta.servlet.context.tempdir");
            DiskFileItemFactory factory = DiskFileItemFactory.builder()
                    .setFile(repository)
                    .setCharset(java.nio.charset.Charset.forName("UTF-8"))  // 设置字符编码
                    .get();
            JakartaServletDiskFileUpload upload = new JakartaServletDiskFileUpload(factory);
            // 设置最大文件上传值
            upload.setFileSizeMax(MAX_FILE_SIZE);
            // 设置最大请求值 (包含文件和表单数据)
            upload.setSizeMax(MAX_REQUEST_SIZE);

            // 构造临时路径来存储上传的文件
            // 这个路径相对当前应用的目录
            String uploadPath = Paths.get(request.getServletContext().getRealPath("./"), UPLOAD_DIRECTORY).toString();
            System.out.println(uploadPath);
            // 如果目录不存在则创建
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }

            // 解析请求的内容提取文件数据
            String movieId = null, movieTitle = null, releaseYear = null, region = null,
                   language = null, genre = null, plotSummary = null, picture = null;
            double averageRating = 0.0;

            List<DiskFileItem> items = upload.parseRequest(request);
            for (FileItem item : items) {
                if (item.isFormField()) {
                    String fieldName = item.getFieldName();
                    if (fieldName.equals("movieId")) {
                        movieId = item.getString();
                    } else if(fieldName.equals("movieTitle")) {
                        movieTitle = item.getString();
                    } else if(fieldName.equals("releaseYear")) {
                        releaseYear = item.getString();
                    } else if(fieldName.equals("region")) {
                        region = item.getString();
                    } else if(fieldName.equals("language")) {
                        language = item.getString();
                    } else if(fieldName.equals("genre")) {
                        genre = item.getString();
                    } else if(fieldName.equals("plotSummary")) {
                        plotSummary = item.getString();
                    } else if(fieldName.equals("averageRating")) {
                        averageRating = Double.parseDouble(item.getString());
                    }
                } else {
                    String fieldName = item.getFieldName();
                    if("picture".equals(fieldName) && item.getName() != null && !item.getName().isEmpty()) {
                        String fileName = new File(item.getName()).getName();
                        Path filePath = Path.of(uploadPath, fileName);
                        System.out.println(filePath);
                        item.write(filePath); // 保存文件到硬盘
                        picture = fileName;
                    }
                }
            }

            this.updateMovieToDb(request, movieId, movieTitle, releaseYear, region, language,
                    genre, plotSummary, averageRating, picture);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }

        response.sendRedirect("./movies");
    }

    private void updateMovieToDb(HttpServletRequest request, String movieId, String movieTitle, String releaseYear,
                                String region, String language, String genre, String plotSummary,
                                Double averageRating, String picture) throws SQLException {
        ServletContext context = this.getServletContext();
        DatabaseService service = (DatabaseService)context.getAttribute(DatabaseService.CONTEXT_KEY);

        //限制plotSummary长度
        if (plotSummary != null && plotSummary.length() > 100) {
            plotSummary = plotSummary.substring(0, 100);
        }

        // 转义单引号，防止 SQL 注入和语法错误
        movieTitle = escapeSqlString(movieTitle);
        releaseYear = escapeSqlString(releaseYear);
        region = escapeSqlString(region);
        language = escapeSqlString(language);
        genre = escapeSqlString(genre);
        plotSummary = escapeSqlString(plotSummary);
        picture = escapeSqlString(picture);

        String updateSQL;
        if(picture != null && !picture.isEmpty()) {
            // 验证 id 是有效的数字
            int Id;
            try {
               Id = Integer.parseInt(movieId);
            } catch (NumberFormatException e) {
                throw new SQLException("无效的电影ID: " + movieId);
            }
            updateSQL = "UPDATE movies SET movieTitle='%s', releaseYear='%s', region='%s', language='%s', genre='%s', plotSummary='%s', averageRating=%s, picture='%s' WHERE movieId=%s";
            service.execute(String.format(updateSQL, movieTitle, releaseYear, region, language, genre, plotSummary, averageRating, picture, movieId));
        } else {
            // 验证 id 是有效的数字
            int id;
            try {
                id = Integer.parseInt(movieId);
            } catch (NumberFormatException e) {
                throw new SQLException("无效的电影ID: " + movieId);
            }
            updateSQL = "UPDATE movies SET movieTitle='%s', releaseYear='%s', region='%s', language='%s', genre='%s', plotSummary='%s', averageRating=%s WHERE movieId=%s";
            service.execute(String.format(updateSQL, movieTitle, releaseYear, region, language, genre, plotSummary, averageRating, id));
        }
    }

    // 转义 SQL 字符串中的单引号
    private String escapeSqlString(String str) {
        if (str == null) {
            return "NULL";
        }
        return str.replace("'", "''");  // 将单引号转义为两个单引号
    }
}