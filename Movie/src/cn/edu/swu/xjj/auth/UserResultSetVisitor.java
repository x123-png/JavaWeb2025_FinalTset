package cn.edu.swu.xjj.auth;

import cn.edu.swu.xjj.repo.ResultSetVisitor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UserResultSetVisitor implements ResultSetVisitor<User>  {

    @Override
    public List<User> visit(ResultSet rs) throws SQLException {
        List<User> users = new ArrayList<>();
        while (rs.next()){
            int id = rs.getInt("id");
            String name = rs.getString("name");
            String password = rs.getString("password");
            String role = rs.getString("role");

            User user = new User(id,name,password,role);
            users.add(user);
        }

        return users;
    }
}
