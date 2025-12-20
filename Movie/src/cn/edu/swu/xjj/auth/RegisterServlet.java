package cn.edu.swu.xjj.auth;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

@WebServlet(urlPatterns = "/register")   //访问路径
public class RegisterServlet extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 接收参数
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        System.out.println("收到注册请求 - 用户名: " + username); // 调试信息

        // 基础验证
        if (username == null || username.trim().isEmpty()) {
            response.sendRedirect("./register.html?error=" + java.net.URLEncoder.encode("用户名不能为空", "UTF-8"));
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            response.sendRedirect("./register.html?error=" + java.net.URLEncoder.encode("密码不能为空", "UTF-8"));
            return;
        }

        // 验证密码至少包含四个字符
        if (password.length() < 4) {
            response.sendRedirect("./register.html?error=" + java.net.URLEncoder.encode("密码至少需要包含四个字符", "UTF-8"));
            return;
        }

        // 创建UserService实例并尝试注册
        UserService userService = new UserService();
        try {
            boolean registered = userService.registerUser(username, password);

            if (registered) {
                System.out.println("注册成功 - 用户: " + username); // 调试信息
                // 注册成功，重定向到首页
                response.sendRedirect("./index.html");
            } else {
                System.out.println("注册失败 - 用户已存在: " + username); // 调试信息
                // 用户名已存在或其他错误
                response.sendRedirect("./register.html?error=" + java.net.URLEncoder.encode("用户名已存在或注册失败", "UTF-8"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("数据库错误: " + e.getMessage()); // 调试信息
            response.sendRedirect("./register.html?error=" + java.net.URLEncoder.encode("数据库操作失败：" + e.getMessage(), "UTF-8"));
        }
    }

}