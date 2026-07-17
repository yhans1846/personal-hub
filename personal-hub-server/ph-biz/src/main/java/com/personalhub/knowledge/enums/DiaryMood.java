package com.personalhub.knowledge.enums;

import com.personalhub.common.util.EnumLabels;
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
        return EnumLabels.labelOf(code, values(), DiaryMood::getCode, DiaryMood::getLabel);
    }
}
