package com.personalhub.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.constant.Flags;
import com.personalhub.system.constant.NotificationType;
import com.personalhub.system.dto.NotificationQueryDTO;
import com.personalhub.system.entity.Notification;
import com.personalhub.system.mapper.NotificationMapper;
import com.personalhub.system.service.NotificationService;
import com.personalhub.system.vo.NotificationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

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

        // TODO_OVERDUE — 已过期未完成的待办
        List<Map<String, Object>> overdueTodos = notificationMapper.selectOverdueTodos(userId, today);
        for (Map<String, Object> row : overdueTodos) {
            Long todoId = ((Number) row.get("id")).longValue();
            String title = (String) row.get("title");
            if (hasNoExistingNotification(userId, NotificationType.TODO_OVERDUE.name(), todoId)) {
                create(userId, NotificationType.TODO_OVERDUE.name(),
                        "待办超期: " + title,
                        "待办任务已超过截止日期",
                        todoId, "todo");
                created++;
            }
        }

        // PLAN_DEADLINE — 未来3天内截止且未完成的计划
        List<Map<String, Object>> deadlinePlans = notificationMapper.selectDeadlinePlans(userId, today, today.plusDays(3));
        for (Map<String, Object> row : deadlinePlans) {
            Long planId = ((Number) row.get("id")).longValue();
            String name = (String) row.get("name");
            if (hasNoExistingNotification(userId, NotificationType.PLAN_DEADLINE.name(), planId)) {
                create(userId, NotificationType.PLAN_DEADLINE.name(),
                        "计划即将截止: " + name,
                        "学习计划截止日期临近",
                        planId, "study_plan");
                created++;
            }
        }

        // PLAN_COMPLETED — 已完成且进度100%
        List<Map<String, Object>> completedPlans = notificationMapper.selectCompletedPlans(userId);
        for (Map<String, Object> row : completedPlans) {
            Long planId = ((Number) row.get("id")).longValue();
            String name = (String) row.get("name");
            if (hasNoExistingNotification(userId, NotificationType.PLAN_COMPLETED.name(), planId)) {
                create(userId, NotificationType.PLAN_COMPLETED.name(),
                        "计划已完成: " + name,
                        "恭喜！学习计划已全部完成",
                        planId, "study_plan");
                created++;
            }
        }

        log.info("系统通知生成完成: userId={}, 新增={}", userId, created);
    }

    @Scheduled(cron = "0 0 8 * * ?")
    public void scheduledGenerate() {
        log.info("定时任务: 开始生成系统通知");
        List<Long> userIds = notificationMapper.selectAllUserIds();
        for (Long userId : userIds) {
            try {
                generateSystemNotifications(userId);
            } catch (Exception e) {
                log.error("生成系统通知失败: userId={}", userId, e);
            }
        }
    }

    private boolean hasNoExistingNotification(Long userId, String type, Long relatedId) {
        // 含已读/已清空：清空或已读后不再为同一关联实体重复生成
        Long count = notificationMapper.selectCount(
                new LambdaQueryWrapper<Notification>()
                        .eq(Notification::getUserId, userId)
                        .eq(Notification::getType, type)
                        .eq(Notification::getRelatedId, relatedId));
        return count == 0;
    }
}
