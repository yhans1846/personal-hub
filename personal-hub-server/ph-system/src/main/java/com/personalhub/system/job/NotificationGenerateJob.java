package com.personalhub.system.job;

import com.personalhub.system.mapper.NotificationMapper;
import com.personalhub.system.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 系统通知定时生成任务：每日 8:00 为全体用户扫描并生成通知。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationGenerateJob {

    private final NotificationMapper notificationMapper;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8 * * ?")
    public void scheduledGenerate() {
        log.info("定时任务: 开始生成系统通知");
        List<Long> userIds = notificationMapper.selectAllUserIds();
        for (Long userId : userIds) {
            try {
                notificationService.generateSystemNotifications(userId);
            } catch (Exception e) {
                log.error("生成系统通知失败: userId={}", userId, e);
            }
        }
    }
}
