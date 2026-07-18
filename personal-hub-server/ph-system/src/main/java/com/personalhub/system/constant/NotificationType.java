package com.personalhub.system.constant;

/**
 * 系统通知类型。
 * <p>
 * 由定时任务按规则扫描业务数据后生成，存入通知表的 type 字段。
 */
public enum NotificationType {

    /**
     * 待办超期：已超过截止日期且仍未完成的待办
     */
    TODO_OVERDUE,

    /**
     * 计划即将截止：未来 3 天内截止且尚未完成的学习计划
     */
    PLAN_DEADLINE,

    /**
     * 计划已完成：进度达到 100% 的学习计划
     */
    PLAN_COMPLETED
}
