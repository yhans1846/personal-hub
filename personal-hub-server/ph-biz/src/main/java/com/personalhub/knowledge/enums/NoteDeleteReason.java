package com.personalhub.knowledge.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 笔记移入回收站的原因。
 */
@Getter
@RequiredArgsConstructor
public enum NoteDeleteReason {
    USER_DELETE("USER_DELETE", "用户删除"),
    AUTO_ARCHIVE("AUTO_ARCHIVE", "归档");

    private final String code;
    private final String label;

    /**
     * @param code 原因码，可为 null
     * @return 展示文案；未知码原样返回
     */
    public static String labelOf(String code) {
        if (code == null || code.isBlank()) {
            return null;
        }
        for (NoteDeleteReason r : values()) {
            if (r.code.equals(code)) {
                return r.label;
            }
        }
        return code;
    }
}
