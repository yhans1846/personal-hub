package com.personalhub.backup;

/**
 * 清空当前用户业务数据（保留账号/资料/layout，先备份再清）。
 */
public interface DataPurgeService {

    DataPurgeVO purge(Long userId, String captchaId, String captchaCode);
}
