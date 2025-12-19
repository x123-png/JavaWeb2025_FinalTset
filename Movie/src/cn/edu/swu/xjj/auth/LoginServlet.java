package cn.edu.swu.xjj.auth;

import cn.edu.swu.xjj.repo.DatabaseService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet(urlPatterns = "/login")   //访问路径
public class LoginServlet extends HttpServlet {

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        //接收参数
        String user = request.getParameter("user");
        String password = request.getParameter("password");
        String code = request.getParameter("code"); // 获取输入的验证码

        // 验证码校验
        HttpSession session = request.getSession(false);
        String captcha = null;
        if (session != null) {
            captcha = (String) session.getAttribute(CaptchaServlet.CAPTCHA_KEY);
        }
        if (code == null || captcha == null || !code.equalsIgnoreCase(captcha)) {
            try {
                response.sendRedirect("./login.html?error=" + java.net.URLEncoder.encode("验证码错误", "UTF-8")); // 正确编码中文错误信息
                return;
            } catch (IOException e) {
                throw new ServletException(e);
            }
        }

        //构造sql语句
        String template = "select * from user where name = '%s' and password = md5('%s')";
        //从容器上下文获取数据库访问的服务类
        ServletContext context = request.getServletContext();
        DatabaseService dbService = (DatabaseService)context.getAttribute(DatabaseService.CONTEXT_KEY);
        //执行SQl,判断用户名密码是否正确
        try {
            List<User> users = dbService.query(String.format(template, user, password), new UserResultSetVisitor());

            if(!users.isEmpty()) {
                this.loginToken(request, users.get(0));

                response.sendRedirect("./x123-png/movies"); //有用户，则转入到主页
            } else {
                response.sendRedirect("./login.html?error=" + java.net.URLEncoder.encode("用户名或密码错误", "UTF-8")); //否则重新登录
            }
        } catch (SQLException | IOException e) {
            throw new ServletException(e);
        }

    }

    //标记用户是否登录，避免拦截器重复筛选
    private void loginToken(HttpServletRequest request,User user) {
        HttpSession session = request.getSession(true);
        session.setAttribute(AuthFilter.LOGIN_TOKEN_KEY,user);
    }
}
