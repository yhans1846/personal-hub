package com.personalhub.system.service;

import com.personalhub.common.exception.BusinessException;
import com.personalhub.system.vo.ImageCaptchaVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.Duration;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 危险操作字符图验证码（与登录书架验证独立）。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ImageCaptchaService {

    public static final Duration TTL = Duration.ofSeconds(120);

    private static final String KEY_PREFIX = "image-captcha:";
    /** 去掉易混字符 */
    private static final char[] CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int CODE_LEN = 4;

    private final StringRedisTemplate stringRedisTemplate;

    public ImageCaptchaVO create() {
        ThreadLocalRandom rnd = ThreadLocalRandom.current();
        StringBuilder code = new StringBuilder(CODE_LEN);
        for (int i = 0; i < CODE_LEN; i++) {
            code.append(CHARS[rnd.nextInt(CHARS.length)]);
        }
        String id = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + id, code.toString().toLowerCase(Locale.ROOT), TTL);

        String imageBase64;
        try {
            imageBase64 = renderPngBase64(code.toString(), rnd);
        } catch (Exception e) {
            stringRedisTemplate.delete(KEY_PREFIX + id);
            throw new BusinessException("生成验证码失败", e);
        }
        return ImageCaptchaVO.builder()
                .captchaId(id)
                .imageBase64(imageBase64)
                .build();
    }

    public void verifyAndConsume(String captchaId, String captchaCode) {
        if (captchaId == null || captchaId.isBlank() || captchaCode == null || captchaCode.isBlank()) {
            throw new BusinessException("请输入验证码");
        }
        String key = KEY_PREFIX + captchaId;
        String stored = stringRedisTemplate.opsForValue().get(key);
        if (stored == null) {
            throw new BusinessException("验证码已失效，请刷新");
        }
        stringRedisTemplate.delete(key);
        if (!stored.equals(captchaCode.trim().toLowerCase(Locale.ROOT))) {
            throw new BusinessException("验证码错误");
        }
    }

    private static String renderPngBase64(String code, ThreadLocalRandom rnd) throws Exception {
        int w = 120;
        int h = 40;
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(245, 247, 250));
            g.fillRect(0, 0, w, h);
            for (int i = 0; i < 6; i++) {
                g.setColor(new Color(180 + rnd.nextInt(50), 180 + rnd.nextInt(50), 180 + rnd.nextInt(50)));
                g.drawLine(rnd.nextInt(w), rnd.nextInt(h), rnd.nextInt(w), rnd.nextInt(h));
            }
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 22));
            for (int i = 0; i < code.length(); i++) {
                g.setColor(new Color(40 + rnd.nextInt(80), 40 + rnd.nextInt(80), 80 + rnd.nextInt(80)));
                int x = 18 + i * 24 + rnd.nextInt(4);
                int y = 28 + rnd.nextInt(5);
                g.drawString(String.valueOf(code.charAt(i)), x, y);
            }
        } finally {
            g.dispose();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ImageIO.write(img, "png", out);
        return Base64.getEncoder().encodeToString(out.toByteArray());
    }
}
