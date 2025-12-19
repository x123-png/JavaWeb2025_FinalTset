package cn.edu.swu.xjj.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

@WebFilter(urlPatterns = "/x123-png/*")
public class AuthFilter extends HttpFilter {
    public final static String LOGIN_TOKEN_KEY = "LOGIN_TOKEN_KEY";

    @Override
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpSession session = request.getSession(true);
        if(session.getAttribute(LOGIN_TOKEN_KEY) == null){
            response.sendRedirect("../login.html");
        }else{
            chain.doFilter(request,response);
        }
    }
}
