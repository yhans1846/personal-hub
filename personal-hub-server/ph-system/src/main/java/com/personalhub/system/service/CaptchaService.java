package com.personalhub.system.service;

import com.personalhub.common.exception.BusinessException;
import com.personalhub.system.vo.CaptchaVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 书架归位验证码：把书拖回空位，正确槽位存 Redis。
 * 登录体字段仍用 sliderX 表示选中的槽位下标。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaptchaService {

    public static final int SLOT_COUNT = 5;
    public static final Duration TTL = Duration.ofSeconds(120);

    private static final String KEY_PREFIX = "captcha:";
    private static final String[] BOOKS = {
            "📕", "📗", "📘", "📙", "📓", "📔", "📒", "📚"
    };

    private final StringRedisTemplate stringRedisTemplate;

    public CaptchaVO create() {
        Random rnd = ThreadLocalRandom.current();
        int emptyIndex = rnd.nextInt(SLOT_COUNT);
        String dragBook = BOOKS[rnd.nextInt(BOOKS.length)];

        List<String> shelf = new ArrayList<>(SLOT_COUNT);
        for (int i = 0; i < SLOT_COUNT; i++) {
            if (i == emptyIndex) {
                shelf.add("");
            } else {
                String b;
                do {
                    b = BOOKS[rnd.nextInt(BOOKS.length)];
                } while (b.equals(dragBook) && rnd.nextBoolean());
                shelf.add(b);
            }
        }

        String id = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set(KEY_PREFIX + id, String.valueOf(emptyIndex), TTL);

        return CaptchaVO.builder()
                .captchaId(id)
                .slotCount(SLOT_COUNT)
                .emptyIndex(emptyIndex)
                .shelfBooks(shelf)
                .dragBook(dragBook)
                .build();
    }

    public boolean matches(String captchaId, Integer slotIndex) {
        if (captchaId == null || captchaId.isBlank() || slotIndex == null) {
            return false;
        }
        String stored = stringRedisTemplate.opsForValue().get(KEY_PREFIX + captchaId);
        if (stored == null) {
            return false;
        }
        try {
            return Integer.parseInt(stored) == slotIndex;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public void verifyAndConsume(String captchaId, Integer slotIndex) {
        if (captchaId == null || captchaId.isBlank() || slotIndex == null) {
            throw new BusinessException("请完成书架验证");
        }
        String key = KEY_PREFIX + captchaId;
        String stored = stringRedisTemplate.opsForValue().get(key);
        if (stored == null) {
            throw new BusinessException("验证已失效，请重试");
        }
        int expected;
        try {
            expected = Integer.parseInt(stored);
        } catch (NumberFormatException e) {
            stringRedisTemplate.delete(key);
            throw new BusinessException("验证已失效，请重试");
        }
        if (expected != slotIndex) {
            throw new BusinessException("请把知识放回正确的位置");
        }
        stringRedisTemplate.delete(key);
    }

    /** 供单测：槽位是否匹配 */
    public static boolean withinTolerance(int expected, int actual) {
        return expected == actual;
    }
}
