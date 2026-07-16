package com.personalhub.planning.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 待办优先级：1 高 / 2 中 / 3 低
 */
@Getter
@RequiredArgsConstructor
public enum TodoPriority {
    HIGH(1, "高"),
    MEDIUM(2, "中"),
    LOW(3, "低");

    private final int code;
    private final String label;

    /**
     * @param code 优先级码，可为 null
     * @return 中文标签；null → null；未知 → 「未知」
     */
    public static String labelOf(Integer code) {
        if (code == null) {
            return null;
        }
        for (TodoPriority p : values()) {
            if (p.code == code) {
                return p.label;
            }
        }
        return "未知";
    }
}
