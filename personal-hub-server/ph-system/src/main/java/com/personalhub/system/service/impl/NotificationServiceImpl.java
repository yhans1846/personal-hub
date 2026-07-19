package com.personalhub.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.constant.EntityType;
import com.personalhub.common.constant.Flags;
import com.personalhub.system.constant.NotificationType;
import com.personalhub.system.dto.NotificationQueryDTO;
import com.personalhub.system.entity.Notification;
import com.personalhub.system.mapper.NotificationMapper;
import com.personalhub.system.notification.PlanningNotificationCandidate;
import com.personalhub.system.notification.PlanningNotificationSource;
import com.personalhub.system.service.NotificationService;
import com.personalhub.system.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户通知 Service 实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final PlanningNotificationSource planningNotificationSource;

    @Override
    public IPage<NotificationVO> list(Long userId, NotificationQueryDTO query) {
        LambdaQueryWrapper<Notification> w = new LambdaQueryWrapper<>();
        w.eq(Notification::getUserId, userId)
                .eq(Notification::getIsDismissed, Flags.NO)
                .orderByAsc(Notification::getIsRead)
                .orderByDesc(Notification::getCreatedAt);
        return notificationMapper
                .selectPage(new Page<>(query.getPage(), query.getSize()), w)
                .convert(NotificationVO::from);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getIsDismissed, Flags.NO)
                        .eq(Notification::getIsRead, Flags.NO));
    }

    @Override
    @Transactional
    public void markAsRead(Long userId, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        notificationMapper.update(null,
                new LambdaUpdateWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .in(Notification::getId, ids)
                        .set(Notification::getIsRead, Flags.YES));
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationMapper.markAllAsReadXml(userId);
    }

    @Override
    @Transactional
    public void clearAll(Long userId) {
        notificationMapper.clearAllXml(userId);
        log.info("清空通知: userId={}", userId);
    }

    @Override
    @Transactional
    public void create(Long userId, String type, String title, String content,
                       Long relatedId, String relatedType) {
        var n = Notification.builder()
                .userId(userId)
                .type(type)
                .title(title)
                .content(content)
                .isRead(Flags.NO)
                .isDismissed(Flags.NO)
                .relatedId(relatedId)
                .relatedType(relatedType)
                .build();
        notificationMapper.insert(n);
        log.info("生成通知: userId={}, type={}, title={}", userId, type, title);
    }

    @Override
    @Transactional
    public void generateSystemNotifications(Long userId) {
        LocalDate today = LocalDate.now();
        int created = 0;

        for (PlanningNotificationCandidate c : planningNotificationSource.findOverdueTodos(userId, today)) {
            if (hasNoExistingNotification(userId, NotificationType.TODO_OVERDUE.name(), c.getRelatedId())) {
                create(userId, NotificationType.TODO_OVERDUE.name(),
                        "待办超期: " + c.getTitle(),
                        "待办任务已超过截止日期",
                        c.getRelatedId(), EntityType.TODO);
                created++;
            }
        }

        LocalDate deadline = today.plusDays(3);
        for (PlanningNotificationCandidate c : planningNotificationSource.findDeadlinePlans(userId, today, deadline)) {
            if (hasNoExistingNotification(userId, NotificationType.PLAN_DEADLINE.name(), c.getRelatedId())) {
                create(userId, NotificationType.PLAN_DEADLINE.name(),
                        "计划即将截止: " + c.getTitle(),
                        "学习计划截止日期临近",
                        c.getRelatedId(), EntityType.STUDY_PLAN);
                created++;
            }
        }

        for (PlanningNotificationCandidate c : planningNotificationSource.findCompletedPlans(userId)) {
            if (hasNoExistingNotification(userId, NotificationType.PLAN_COMPLETED.name(), c.getRelatedId())) {
                create(userId, NotificationType.PLAN_COMPLETED.name(),
                        "计划已完成: " + c.getTitle(),
                        "恭喜！学习计划已全部完成",
                        c.getRelatedId(), EntityType.STUDY_PLAN);
                created++;
            }
        }

        log.info("系统通知生成完成: userId={}, 新增={}", userId, created);
    }

    private boolean hasNoExistingNotification(Long userId, String type, Long relatedId) {
        Long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getType, type)
                        .eq(Notification::getRelatedId, relatedId));
        return count == 0;
    }
}
