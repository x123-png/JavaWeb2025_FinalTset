package cn.edu.swu.xjj.auth;

import cn.edu.swu.xjj.repo.DatabaseService;
import cn.edu.swu.xjj.repo.ResultSetVisitor;

import java.sql.SQLException;
import java.util.List;

public class UserService {

    public boolean registerUser(String username, String password) throws SQLException {
        // Check if user already exists
        if (userExists(username)) {
            return false; // User already exists
        }

        // Insert new user with default role 'user'
        username = escapeSqlString(username);
        password = escapeSqlString(password);
        String insertSql = "INSERT INTO user (name, password, role) VALUES ('%s', MD5('%s'), 'user')";
        String sql = String.format(insertSql, username, password);

        DatabaseService dbService = DatabaseService.getInstance();
        return dbService.execute(sql);
    }

    // 转义 SQL 字符串中的单引号
    private String escapeSqlString(String str) {
        if (str == null) {
            return "NULL";
        }
        return str.replace("'", "''");  // 将单引号转义为两个单引号
    }

    public boolean userExists(String username) throws SQLException {
        String querySql = "SELECT COUNT(*) AS count FROM user WHERE name = '%s'";
        String sql = String.format(querySql, username);

        DatabaseService dbService = DatabaseService.getInstance();
        List<Integer> counts = dbService.query(sql, new CountResultSetVisitor());

        if (!counts.isEmpty()) {
            Integer count = counts.get(0);
            return count > 0;
        }

        return false;
    }

    // Inner class for counting results
    private static class CountResultSetVisitor implements ResultSetVisitor<Integer> {
        @Override
        public List<Integer> visit(java.sql.ResultSet rs) throws java.sql.SQLException {
            java.util.ArrayList<Integer> results = new java.util.ArrayList<>();

            if (rs.next()) {
                int count = rs.getInt("count");
                results.add(count);
            }

            return results;
        }
    }
}