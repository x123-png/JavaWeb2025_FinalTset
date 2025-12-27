package cn.edu.swu.xjj.servlet;

import cn.edu.swu.xjj.auth.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet(urlPatterns = "/x123-png/onlineUsers")
public class OnlineUserServlet extends HttpServlet {
    
    private ObjectMapper objectMapper = new ObjectMapper();
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // 获取ServletContext中的在线用户数
        ServletContext context = request.getServletContext();
        Integer onlineUserCount = (Integer) context.getAttribute("onlineUserCount");
        
        if (onlineUserCount == null) {
            onlineUserCount = 0;
        }
        
        // 检查当前用户是否已登录
        HttpSession session = request.getSession(false);
        boolean isLoggedIn = false;
        String currentUsername = null;
        if (session != null) {
            User currentUser = (User) session.getAttribute("LOGIN_TOKEN_KEY");
            if (currentUser != null) {
                isLoggedIn = true;
                currentUsername = currentUser.getName();
            }
        }
        
        // 构建响应数据
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("onlineUserCount", onlineUserCount);
        responseData.put("isLoggedIn", isLoggedIn);
        responseData.put("currentUsername", currentUsername);
        
        // 设置响应格式
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        // 发送JSON响应
        String jsonResponse = objectMapper.writeValueAsString(responseData);
        response.getWriter().write(jsonResponse);
    }
}