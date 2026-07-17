package com.personalhub.planning.enums;

import com.personalhub.common.util.EnumLabels;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 学习计划状态：0 未开始 / 1 学习中 / 2 已完成 / 3 已暂停
 */
@Getter
@RequiredArgsConstructor
public enum StudyPlanStatus {
    NOT_STARTED(0, "未开始"),
    LEARNING(1, "学习中"),
    COMPLETED(2, "已完成"),
    PAUSED(3, "已暂停");

    private final int code;
    private final String label;

    /**
     * @param code 状态码，可为 null
     * @return 中文标签；null → null；未知 → 「未知」
     */
    public static String labelOf(Integer code) {
        return EnumLabels.labelOf(code, values(), StudyPlanStatus::getCode, StudyPlanStatus::getLabel);
    }
}
