package com.personalhub.knowledge.enums;

import com.personalhub.common.util.EnumLabels;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 阅读状态：0 未读 / 1 在读 / 2 读完
 */
@Getter
@RequiredArgsConstructor
public enum ReadingStatus {
    UNREAD(0, "未读"),
    READING(1, "在读"),
    DONE(2, "读完");

    private final int code;
    private final String label;

    /**
     * @param code 状态码，可为 null
     * @return 中文标签；null → null；未知 → 「未知」
     */
    public static String labelOf(Integer code) {
        return EnumLabels.labelOf(code, values(), ReadingStatus::getCode, ReadingStatus::getLabel);
    }
}
