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
        dataSource.setUrl("jdbc:mysql://localhost:3306/movie_list?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai");
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