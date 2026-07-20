package com.personalhub.backup;

import java.util.List;

/**
 * 用户数据备份 / 恢复。
 */
public interface DataBackupService {

    /**
     * 导出当前用户完整备份 ZIP（不含 backups/ 目录）。
     */
    byte[] exportZip(Long userId);

    /**
     * 用备份 ZIP 全量覆盖当前用户业务数据（不触碰 user_backup / backups/）。
     */
    void importZip(Long userId, byte[] zipBytes);

    /**
     * 生成 ZIP 并写入服务端历史；成功返回 ZIP 字节。
     */
    byte[] createStoredBackup(Long userId, String triggerType);

    List<UserBackupVO> listBackups(Long userId);

    byte[] downloadBackup(Long userId, Long backupId);

    void restoreFromBackup(Long userId, Long backupId);

    void deleteBackup(Long userId, Long backupId);

    BackupSettingsDTO getSettings(Long userId);

    void updateSettings(Long userId, BackupSettingsDTO dto);

    /**
     * 按用户频率执行自动备份（供定时任务调用）。
     */
    void runScheduledBackupIfDue(Long userId);
}
