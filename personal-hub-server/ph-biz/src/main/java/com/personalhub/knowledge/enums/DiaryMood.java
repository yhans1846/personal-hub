package com.personalhub.knowledge.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 日记心情：1 很好 … 5 很差
 */
@Getter
@RequiredArgsConstructor
public enum DiaryMood {
    GREAT(1, "很好"),
    GOOD(2, "好"),
    OK(3, "一般"),
    BAD(4, "不好"),
    TERRIBLE(5, "很差");

    private final int code;
    private final String label;

    /**
     * @param code 心情码，可为 null
     * @return 中文标签；null → null；未知 → 「未知」
     */
    public static String labelOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (DiaryMood m : values()) {
            if (m.code == code) {
                return m.label;
            }
        }
        return "未知";
    }
}
