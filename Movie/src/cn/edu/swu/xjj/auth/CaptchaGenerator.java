package cn.edu.swu.xjj.auth;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;

public class CaptchaGenerator {

    private static final String CHARS = "0123456789";  // 仅数字
    private final Random random = new Random();

    private int width = 200;
    private int height = 80;
    private int length = 4;           // 验证码长度 = 4
    private int noiseLines = 8;       // 干扰线更多
    private int noiseDots = 800;      // 噪点大幅增加

    public CaptchaGenerator() {}

    public String generateText() {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    public BufferedImage generateImage(String text) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();

        g.setColor(Color.WHITE);       // 白色背景
        g.fillRect(0, 0, width, height);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        addNoiseDots(g);
        addNoiseLines(g);

        int charSpacing = width / (length + 1);
        int baseline = (int) (height * 0.7);

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);

            // 设置字体
            int fontSize = 40 + random.nextInt(10);
            g.setFont(new Font("Arial", Font.BOLD, fontSize));

            // 随机深色
            g.setColor(new Color(random.nextInt(120), random.nextInt(120), random.nextInt(120)));

            // 旋转 + 扭曲
            AffineTransform old = g.getTransform();
            AffineTransform trans = new AffineTransform();

            trans.translate(charSpacing * (i + 1), baseline);
            trans.rotate(Math.toRadians(random.nextInt(50) - 25)); // -25° ~ 25°
            trans.shear((random.nextDouble() - 0.5) * 0.5,  // X 剪切
                    (random.nextDouble() - 0.5) * 0.3); // Y 剪切

            g.setTransform(trans);
            g.drawString(String.valueOf(c), 0, 0);
            g.setTransform(old);
        }

        g.dispose();

        // 加轻微模糊，让数字更模糊
        return applyBlur(image);
    }

    // 添加大量噪点
    private void addNoiseDots(Graphics2D g) {
        for (int i = 0; i < noiseDots; i++) {
            g.setColor(new Color(random.nextInt(200), random.nextInt(200), random.nextInt(200)));
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            g.fillRect(x, y, 1, 1);
        }
    }

    // 添加干扰线
    private void addNoiseLines(Graphics2D g) {
        for (int i = 0; i < noiseLines; i++) {
            g.setColor(new Color(random.nextInt(150), random.nextInt(150), random.nextInt(150)));
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            g.setStroke(new BasicStroke(1.5f));
            g.drawLine(x1, y1, x2, y2);
        }
    }

    // 模糊滤镜增强模糊感
    private BufferedImage applyBlur(BufferedImage image) {
        float[] blurKernel = {
                1f/9f, 1f/9f, 1f/9f,
                1f/9f, 1f/9f, 1f/9f,
                1f/9f, 1f/9f, 1f/9f
        };
        ConvolveOp op = new ConvolveOp(new Kernel(3, 3, blurKernel));
        return op.filter(image, null);
    }

    // 写入文件
    public void writeToFile(BufferedImage image, String path) throws IOException {
        ImageIO.write(image, "png", new File(path));
    }

    // 输出为 Base64 字符串（给前端直接显示图片）
    public String writeToBase64(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        byte[] bytes = baos.toByteArray();
        return Base64.getEncoder().encodeToString(bytes);
    }

    // 演示：生成验证码文件
    public static void main(String[] args) throws Exception {
        CaptchaGenerator gen = new CaptchaGenerator();
        String text = gen.generateText();
        System.out.println("验证码: " + text);

        BufferedImage img = gen.generateImage(text);
        gen.writeToFile(img, "captcha.png");
        System.out.println("生成文件：captcha.png");
    }
}
