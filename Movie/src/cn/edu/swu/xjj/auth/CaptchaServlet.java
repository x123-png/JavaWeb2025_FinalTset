package cn.edu.swu.xjj.auth;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

@WebServlet(urlPatterns = "/captcha")
public class CaptchaServlet extends HttpServlet {

    public final static String CAPTCHA_KEY = "CAPTCHA_KEY";

    public void doGet(HttpServletRequest request, HttpServletResponse response){
        //生成验证码
        CaptchaGenerator gen = new CaptchaGenerator();
        String captcha = gen.generateText();

        HttpSession session = request.getSession(true);
        session.setAttribute(CAPTCHA_KEY, captcha);

        try (OutputStream outputStream = response.getOutputStream()) {
            BufferedImage img = gen.generateImage(captcha);
            ImageIO.write(img,"png",outputStream);
            outputStream.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
