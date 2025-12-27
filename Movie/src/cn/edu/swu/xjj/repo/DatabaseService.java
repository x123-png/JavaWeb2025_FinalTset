package cn.edu.swu.xjj.repo;

import org.apache.commons.dbcp2.BasicDataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DatabaseService {

    private BasicDataSource dataSource = null;

    private static DatabaseService instance = new DatabaseService();
    public final static String CONTEXT_KEY = "DATABASE_SERVICE";

    private DatabaseService(){
    }

    public static DatabaseService getInstance() {
        return instance;
    }

    public void init() {
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://localhost:3306/movie_list?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai");  //IPv4：10.65.206.47
//        dataSource.setUrl("jdbc:mysql://10.65.206.47:3306/movie_list?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai");
        dataSource.setUsername("root");
        dataSource.setPassword("51211mysql@");

        // 配置连接池参数
        dataSource.setInitialSize(3);           // 初始连接数
        dataSource.setMaxTotal(6);             // 最大连接数
        dataSource.setMaxIdle(4);              // 最大空闲连接数
        dataSource.setMinIdle(2);               // 最小空闲连接数
        dataSource.setMaxWaitMillis(10000);     // 获取连接最大等待时间（毫秒）

        //执行脚本部分
        initDatabase();
    }

    private Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public void closeDataSource() {
        if (dataSource != null) {
            try {
                dataSource.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public List query(String sql, ResultSetVisitor visitor) throws SQLException {
        try(Connection connection = this.dataSource.getConnection()) {
            try(Statement statement = connection.createStatement()) {
                try(ResultSet resultSet = statement.executeQuery(sql)) {
                    return visitor.visit(resultSet);
                }
            }
        }
    }

    public boolean execute(String sql) throws SQLException {
        try(Connection connection = this.dataSource.getConnection()) {
            try(Statement statement = connection.createStatement()) {
                return statement.execute(sql);
            }
        }
    }

    //脚本部分
    private void initDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // 检查初始化标志，如果已经初始化过则跳过
            boolean isInitialized = checkInitialization(stmt);
            if (isInitialized) {
                System.out.println("数据库已初始化，跳过初始化脚本执行");
                return;
            }

            String sql = loadSqlFromClasspath("db_init.sql");

            for (String s : sql.split(";")) {
                String execSql = s.trim();
                if (!execSql.isEmpty()) {
                    System.out.println("执行 SQL: " + execSql);
                    stmt.execute(execSql);
                }
            }

            System.out.println("db_init.sql 执行完成");

        } catch (Exception e) {
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    private boolean checkInitialization(Statement stmt) {
        try {
            // 检查 db_init_flag 表是否存在
            String checkTableSql = "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'movie_list' AND table_name = 'db_init_flag'";
            ResultSet rs = stmt.executeQuery(checkTableSql);
            if (rs.next() && rs.getInt(1) > 0) {
                // 表存在，检查初始化状态
                String checkInitSql = "SELECT initialized FROM db_init_flag WHERE id = 1 LIMIT 1";
                rs = stmt.executeQuery(checkInitSql);
                if (rs.next()) {
                    return rs.getBoolean("initialized");
                }
            }
        } catch (SQLException e) {
            // 如果出现异常（如表不存在），则认为未初始化
            System.out.println("检查初始化状态时出现异常，将执行初始化: " + e.getMessage());
        }
        return false; // 默认认为未初始化
    }

    private String loadSqlFromClasspath(String fileName) throws Exception {
        StringBuilder sb = new StringBuilder();

        try (var in = getClass().getClassLoader().getResourceAsStream(fileName);
             var reader = new java.io.BufferedReader(new java.io.InputStreamReader(in))) {

            if (in == null) {
                throw new RuntimeException("找不到 " + fileName);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("--")) {
                    continue;
                }
                sb.append(line).append("\n");
            }
        }

        return sb.toString();
    }

}