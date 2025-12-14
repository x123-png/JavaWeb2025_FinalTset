package movie;

import repo.DatabaseService;
import jakarta.servlet.ServletConfig;
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
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.Year; // Import Year for validation
import java.util.List;

@WebServlet(urlPatterns = "/addMovie")
public class AddMovieServlet extends HttpServlet {

    // 上传文件存储目录
    private static final String UPLOAD_DIRECTORY = "upload";

    // 上传配置
    private static final int MEMORY_THRESHOLD   = 1024 * 1024 * 3;  // 3MB
    private static final int MAX_FILE_SIZE      = 1024 * 1024 * 40; // 40MB
    private static final int MAX_REQUEST_SIZE   = 1024 * 1024 * 50; // 50MB

//    public void init(ServletConfig config) throws ServletException {
//        super.init(config);
//
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//    }

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
            String movieTitle = null, releaseYearStr = null, region = null,
                    language = null, genre = null, plotSummary = null, averageRatingStr = null, picture = null;

            List<DiskFileItem> items = upload.parseRequest(request);
            for (FileItem item : items) {
                if (item.isFormField()) {
                    String fieldName = item.getFieldName();
                    if (fieldName.equals("movieTitle")) {
                        movieTitle = item.getString();
                    } else if(fieldName.equals("releaseYear")) {
                        releaseYearStr = item.getString(); // Get raw string input
                    } else if(fieldName.equals("region")) {
                        region = item.getString();
                    } else if(fieldName.equals("language")) {
                        language = item.getString();
                    } else if(fieldName.equals("genre")) {
                        genre = item.getString();
                    } else if(fieldName.equals("plotSummary")) {
                        plotSummary = item.getString();
                    } else if(fieldName.equals("averageRating")) {
                         averageRatingStr = String.valueOf(Double.parseDouble(item.getString())); // Parse Average Rating
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

            // *** MODIFY THIS SECTION: Parse releaseYearStr as Date (YYYY-MM-DD) ***
            java.sql.Date releaseYearSqlDate = null; // Initialize with null
            if (releaseYearStr != null && !releaseYearStr.trim().isEmpty()) {
                 try {
                     // Parse the YYYY-MM-DD string into a java.sql.Date
                     releaseYearSqlDate = java.sql.Date.valueOf(releaseYearStr.trim());
                     // Optional: Add date range check if needed
                     // java.time.LocalDate inputDate = releaseYearSqlDate.toLocalDate();
                     // java.time.LocalDate minDate = java.time.LocalDate.of(1800, 1, 1);
                     // java.time.LocalDate maxDate = java.time.LocalDate.now().plusYears(5);
                     // if (inputDate.isBefore(minDate) || inputDate.isAfter(maxDate)) {
                     //     request.setAttribute("errorMessage", "发布日期应在 1800-01-01 到 " + maxDate + " 之间。");
                     //     request.getRequestDispatcher("/add_movie.html").forward(request, response);
                     //     return; // Terminate doPost execution
                     // }
                 } catch (IllegalArgumentException e) {
                     // If parsing fails (e.g., invalid format like "abc"), catch IllegalArgumentException
                      request.setAttribute("errorMessage", "发布日期格式不正确 (应为 YYYY-MM-DD 格式)。");
                      request.getRequestDispatcher("/add_movie.html").forward(request, response);
                      return; // Terminate doPost execution
                 }
            }
            // At this point, releaseYearSqlDate is either a valid java.sql.Date or null.

            // --- Add movieTitle validation here ---
            if (movieTitle == null || movieTitle.trim().isEmpty()) {
                request.setAttribute("errorMessage", "电影标题不能为空。");
                request.getRequestDispatcher("/add_movie.html").forward(request, response);
                return; // Terminate doPost execution
            }
            // --- movieTitle validation end ---

            this.saveMovieToDb(request, movieTitle, releaseYearSqlDate, region, language, // Pass java.sql.Date object
                                genre, plotSummary, averageRatingStr, picture);
        } catch (Exception ex) {
            throw new ServletException(ex);
        }

        response.sendRedirect("./movies");
    }

    // Modified signature to accept java.sql.Date for releaseYear
    private void saveMovieToDb(HttpServletRequest request, String movieTitle, java.sql.Date releaseYearSqlDate, // Changed type to java.sql.Date
                                String region, String language, String genre, String plotSummary,
                                String averageRatingStr, String picture) throws SQLException { // Renamed param to averageRatingStr
        ServletContext context = this.getServletContext();
        DatabaseService dbService = (DatabaseService)context.getAttribute(DatabaseService.CONTEXT_KEY);
        BasicDataSource dataSource = dbService.getDataSource();

        // 限制 plotSummary 字段长度，防止超出数据库字段限制
        if (plotSummary != null && plotSummary.length() > 100) {
            plotSummary = plotSummary.substring(0, 100);
        }

        // Use PreparedStatement
        String insertSql = "INSERT INTO movies (movieTitle, releaseYear, region, language, genre, plotSummary, averageRating, picture) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        // Get connection and prepare statement
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement(insertSql)) {

            statement.setString(1, movieTitle);
            // Handle releaseYearSqlDate (java.sql.Date)
            if (releaseYearSqlDate != null) {
                // If database column is DATE, use setDate
                statement.setDate(2, releaseYearSqlDate);
            } else {
                // Use setNull if the value is null
                statement.setNull(2, java.sql.Types.DATE); // Use Types.DATE if column is DATE
            }
            statement.setString(3, region);
            statement.setString(4, language);
            statement.setString(5, genre);
            statement.setString(6, plotSummary);
            // Handle averageRatingStr -> Double -> setDouble
            if (averageRatingStr != null && !averageRatingStr.isEmpty()) {
                 try {
                     double avgRating = Double.parseDouble(averageRatingStr);
                     statement.setDouble(7, avgRating);
                 } catch (NumberFormatException e) {
                      // This should ideally have been caught earlier, but handle just in case
                      statement.setNull(7, java.sql.Types.DOUBLE);
                 }
            } else {
                 statement.setNull(7, java.sql.Types.DOUBLE);
            }
            statement.setString(8, picture);

            statement.executeUpdate(); // Execute the prepared statement
        } catch (SQLException e) {
             throw e; // Re-throw the exception
        }

        // Original code using string formatting is removed as PreparedStatement is used now.
        // dbService.execute(String.format(insertSql, movieTitle, releaseYear, region, language, genre, plotSummary, averageRating, picture));
    }

}