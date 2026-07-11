package com.personalhub.system.service;

/**
 * 审计日志服务
 */
public interface AuditLogService {

    /**
     * 写入审计日志
     *
     * @param module     业务模块
     * @param businessId 业务ID
     * @param action     操作类型
     * @param content    操作描述
     * @param operatorId 操作用户ID
     */
    void log(String module, Long businessId, String action, String content, Long operatorId);
}
