package movie;

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

import repo.DatabaseService;
import repo.MovieResultSetVisitor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
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
        String id = request.getParameter("id");

        ServletContext context = this.getServletContext();
        DatabaseService dbService = (DatabaseService)context.getAttribute(DatabaseService.CONTEXT_KEY);
        String selectSQL = "select * from movies where movie_id = " + id;

        try {
            List<Movie> movies = dbService.query(selectSQL, new MovieResultSetVisitor());
            Movie m = movies.get(0);
            String template = """
    <form action="./updateMovie" method="post" enctype="multipart/form-data" style="line-height:3em">
        <input type="hidden" name="movie_id" value="%s"><br>
        电影标题：<input type="text" name="movie_title" value="%s"><br>
        发布年份：<input type="text" name="release_year" value="%s"><br>
        地区：<input type="text" name="region" value="%s"><br>
        语言：<input type="text" name="language" value="%s"><br>
        类型：<input type="text" name="genre" value="%s"><br>
        剧情简介：<textarea rows="3" cols="22" name="plot_summary">%s</textarea><br>
        平均评分：<input type="text" name="average_rating" value="%s"><br>
        海报图片：<input type="file" name="poster_file"><br>
        (当前海报: %s)<br><br>
        <input type="submit" value="修改电影">
    </form>
        """;
            response.setContentType("text/html");
            response.setCharacterEncoding("UTF-8");
            try(java.io.Writer writer = response.getWriter()) {
                String form = String.format(template,
                        m.getMovieId(),
                        m.getMovieTitle() != null ? m.getMovieTitle() : "",
                        m.getReleaseYear() != null ? m.getReleaseYear().toString() : "",
                        m.getRegion() != null ? m.getRegion() : "",
                        m.getLanguage() != null ? m.getLanguage() : "",
                        m.getGenre() != null ? m.getGenre() : "",
                        m.getPlotSummary() != null ? m.getPlotSummary() : "",
                        m.getAverageRating() != null ? m.getAverageRating().toString() : "",
                        m.getPicture() != null ? m.getPicture() : "无");
                String html = "<html><head><title>修改电影</title></head><body><h1>修改电影信息</h1>" + form + "</body></html>";
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
                   language = null, genre = null, plotSummary = null, averageRating = null;
            String posterFileName = null;

            List<DiskFileItem> items = upload.parseRequest(request);
            for (FileItem item : items) {
                if (item.isFormField()) {
                    String fieldName = item.getFieldName();
                    if (fieldName.equals("movie_id")) {
                        movieId = item.getString();
                    } else if(fieldName.equals("movie_title")) {
                        movieTitle = item.getString();
                    } else if(fieldName.equals("release_year")) {
                        releaseYear = item.getString();
                    } else if(fieldName.equals("region")) {
                        region = item.getString();
                    } else if(fieldName.equals("language")) {
                        language = item.getString();
                    } else if(fieldName.equals("genre")) {
                        genre = item.getString();
                    } else if(fieldName.equals("plot_summary")) {
                        plotSummary = item.getString();
                    } else if(fieldName.equals("average_rating")) {
                        averageRating = item.getString();
                    }
                } else if (item.getFieldName().equals("poster_file")) {
                    // 上传海报文件
                    String fileName = item.getName();
                    if(fileName != null && !fileName.isEmpty()) {
                        // 获取文件名，处理可能包含路径的情况
                        fileName = new File(fileName).getName();
                        // 确保只接受图片文件
                        String contentType = item.getContentType();
                        if(contentType != null && (contentType.toLowerCase().startsWith("image/"))) {
                            // 生成唯一文件名以避免冲突
                            String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
                            Path filePath = Paths.get(uploadPath, uniqueFileName);
                            System.out.println("Saving file to: " + filePath);
                            item.write(filePath); // 保存文件到硬盘
                            posterFileName = uniqueFileName;
                        } else {
                            System.out.println("File is not an image: " + contentType);
                        }
                    }
                }
            }

            if(movieId != null) {
                this.updateMovieToDb(request, movieId, movieTitle, releaseYear, region, language,
                                    genre, plotSummary, averageRating, posterFileName);
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }

        response.sendRedirect("./movies");
    }

    private void updateMovieToDb(HttpServletRequest request, String movieId, String movieTitle, String releaseYear,
                                String region, String language, String genre, String plotSummary,
                                String averageRating, String posterFileName) throws SQLException {
        ServletContext context = request.getServletContext();
        DatabaseService service = (DatabaseService)context.getAttribute(DatabaseService.CONTEXT_KEY);

        // 转义单引号，防止 SQL 注入和语法错误
        movieTitle = escapeSqlString(movieTitle);
        region = escapeSqlString(region);
        language = escapeSqlString(language);
        genre = escapeSqlString(genre);
        plotSummary = escapeSqlString(plotSummary);

        // 构建更新SQL语句，根据是否有新海报文件决定是否更新picture字段
        String updateSql;
        if (posterFileName != null && !posterFileName.isEmpty()) {
            // 包含海报文件的更新
            String pictureValue = "upload/" + escapeSqlString(posterFileName);
            String releaseYearValue = (releaseYear != null && !releaseYear.isEmpty()) ? releaseYear : "NULL";
            String averageRatingValue = (averageRating != null && !averageRating.isEmpty()) ? averageRating : "NULL";

            updateSql = String.format(
                "UPDATE movies SET movie_title='%s', release_year=%s, region='%s', language='%s', " +
                "genre='%s', plot_summary='%s', average_rating=%s, picture='%s', updated_at=NOW() WHERE movie_id=%s",
                movieTitle, releaseYearValue, region, language, genre, plotSummary,
                averageRatingValue, pictureValue, movieId
            );
        } else {
            // 不包含海报文件的更新
            String releaseYearValue = (releaseYear != null && !releaseYear.isEmpty()) ? releaseYear : "NULL";
            String averageRatingValue = (averageRating != null && !averageRating.isEmpty()) ? averageRating : "NULL";

            updateSql = String.format(
                "UPDATE movies SET movie_title='%s', release_year=%s, region='%s', language='%s', " +
                "genre='%s', plot_summary='%s', average_rating=%s, updated_at=NOW() WHERE movie_id=%s",
                movieTitle, releaseYearValue, region, language, genre, plotSummary,
                averageRatingValue, movieId
            );
        }

        // 将字符串"NULL"替换为真正的NULL值，但要小心不要影响其他可能含有"NULL"文本的部分
        updateSql = updateSql.replace(", 'NULL',", ", NULL,")
                             .replace(", 'NULL' ", ", NULL ")
                             .replace(", 'NULL'", ", NULL")
                             .replace(" 'NULL' WHERE", " NULL WHERE")
                             .replace("'NULL' WHERE", "NULL WHERE");

        System.out.println("Update SQL: " + updateSql); // Debugging line

        service.update(updateSql);
    }

    // 转义 SQL 字符串中的单引号
    private String escapeSqlString(String str) {
        if (str == null) {
            return "NULL";
        }
        return str.replace("'", "''");  // 将单引号转义为两个单引号
    }
}