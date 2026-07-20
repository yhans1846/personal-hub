package com.personalhub.backup;

import com.personalhub.system.mapper.NotificationMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 每日 02:00 按用户频率自动备份。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BackupScheduleJob {

    private final NotificationMapper notificationMapper;
    private final DataBackupService dataBackupService;

    @Scheduled(cron = "0 0 2 * * ?")
    public void scheduledBackup() {
        log.info("定时任务: 开始自动备份");
        List<Long> userIds = notificationMapper.selectAllUserIds();
        for (Long userId : userIds) {
            try {
                dataBackupService.runScheduledBackupIfDue(userId);
            } catch (Exception e) {
                log.error("自动备份失败: userId={}", userId, e);
            }
        }
        log.info("定时任务: 自动备份结束");
    }
}
