package com.personalhub.system.notification;

import java.time.LocalDate;
import java.util.List;

/**
 * 规划域通知数据源（由 ph-biz 实现，避免 system 跨域查表）。
 */
public interface PlanningNotificationSource {

    /**
     * 已过期且未完成的待办
     */
    List<PlanningNotificationCandidate> findOverdueTodos(Long userId, LocalDate today);

    /**
     * 即将截止且未完成/未暂停的学习计划
     */
    List<PlanningNotificationCandidate> findDeadlinePlans(Long userId, LocalDate today, LocalDate deadline);

    /**
     * 已完成的学习计划
     */
    List<PlanningNotificationCandidate> findCompletedPlans(Long userId);
}
