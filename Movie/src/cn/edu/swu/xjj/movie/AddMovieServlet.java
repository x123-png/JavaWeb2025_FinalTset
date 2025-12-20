package cn.edu.swu.xjj.movie;

import cn.edu.swu.xjj.repo.DatabaseService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.commons.fileupload2.core.DiskFileItem;
import org.apache.commons.fileupload2.core.DiskFileItemFactory;
import org.apache.commons.fileupload2.core.FileItem;
import org.apache.commons.fileupload2.jakarta.servlet5.JakartaServletDiskFileUpload;
import org.apache.commons.fileupload2.jakarta.servlet5.JakartaServletFileUpload;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

@WebServlet(urlPatterns = "/x123-png/addMovie")
public class AddMovieServlet extends HttpServlet {

    // 上传文件存储目录
    private static final String UPLOAD_DIRECTORY = "upload";

    // 上传配置
    private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 3;  // 3MB
    private static final int MAX_FILE_SIZE      = 1024 * 1024 * 40; // 40MB
    private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 50; // 50MB

    @Override
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

            // 构造上传路径，使用相对路径 "upload" 存储到应用根目录
            String uploadPath = request.getServletContext().getRealPath("/") + UPLOAD_DIRECTORY;
            System.out.println(uploadPath);
            // 如果目录不存在则创建
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }

            // 解析请求的内容提取文件数据
            String movieTitle = null, releaseYear = null, region = null,
                    language = null, genre = null, plotSummary = null, picture = null;
            double averageRating = 0;

            List<DiskFileItem> items = upload.parseRequest(request);
            for (FileItem item : items) {
                if (item.isFormField()) {
                    String fieldName = item.getFieldName();
                    if (fieldName.equals("movieTitle")) {
                        movieTitle = item.getString();
                    } else if(fieldName.equals("releaseYear")) {
                        releaseYear = item.getString(); // Get raw string input
                    } else if(fieldName.equals("region")) {
                        region = item.getString();
                    } else if(fieldName.equals("language")) {
                        language = item.getString();
                    } else if(fieldName.equals("genre")) {
                        genre = item.getString();
                    } else if(fieldName.equals("plotSummary")) {
                        plotSummary = item.getString();
                    } else if(fieldName.equals("averageRating")) {
                         averageRating = Double.parseDouble(item.getString());; // Parse Average Rating
                    }
                } else {
                    String fileName = new File(item.getName()).getName();
                    Path filePath = Path.of(uploadPath, fileName);
                    System.out.println(filePath);
                    item.write(filePath); // 保存文件到硬盘
                    picture = fileName;
                }
            }
            this.saveMovieToDb(request, movieTitle, releaseYear, region, language,
                                genre, plotSummary, averageRating, picture);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }

        response.sendRedirect("./movies");
    }

    private void saveMovieToDb(HttpServletRequest request, String movieTitle, String releaseYear,
                                String region, String language, String genre, String plotSummary,
                                double averageRating, String picture) throws SQLException {
        ServletContext context = this.getServletContext();
        DatabaseService dbService = (DatabaseService)context.getAttribute(DatabaseService.CONTEXT_KEY);

        // 限制 plotSummary 字段长度，防止超出数据库字段限制
        if (plotSummary != null && plotSummary.length() > 400) {
            plotSummary = plotSummary.substring(0, 400);
        }

        // 转义单引号，防止 SQL 注入和语法错误
        movieTitle = escapeSqlString(movieTitle);
        releaseYear = escapeSqlString(releaseYear);
        region = escapeSqlString(region);
        language = escapeSqlString(language);
        genre = escapeSqlString(genre);
        plotSummary = escapeSqlString(plotSummary);
        picture = escapeSqlString(picture);

        String insertSQL = "INSERT INTO movies (movieTitle, releaseYear, region, language, genre, plotSummary, averageRating, picture)" +
                "VALUES ('%s','%s','%s','%s','%s','%s',%s,'%s')";
        dbService.execute(String.format(insertSQL,movieTitle, releaseYear, region, language, genre, plotSummary, averageRating, picture));
    }

    // 转义 SQL 字符串中的单引号
    private String escapeSqlString(String str) {
        if (str == null) {
            return "NULL";
        }
        return str.replace("'", "''");  // 将单引号转义为两个单引号
    }
}