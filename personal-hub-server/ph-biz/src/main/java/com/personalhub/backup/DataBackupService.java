package com.personalhub.backup;

/**
 * 用户数据备份 / 恢复。
 */
public interface DataBackupService {

    /**
     * 导出当前用户完整备份 ZIP。
     */
    byte[] exportZip(Long userId);

    /**
     * 用备份 ZIP 全量覆盖当前用户业务数据。
     */
    void importZip(Long userId, byte[] zipBytes);
}
