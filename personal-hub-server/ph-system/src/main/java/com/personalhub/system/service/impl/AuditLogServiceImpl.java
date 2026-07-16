package com.personalhub.system.service.impl;

import com.personalhub.system.entity.AuditLog;
import com.personalhub.system.mapper.AuditLogMapper;
import com.personalhub.system.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 审计日志服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogMapper auditLogMapper;

    @Override
    public void log(String module, Long businessId, String action, String content, Long operatorId) {
        var auditLog = AuditLog.builder()
                .module(module)
                .businessId(businessId)
                .action(action)
                .content(content)
                .operatorId(operatorId)
                .build();
        auditLogMapper.insert(auditLog);
        log.info("审计日志: module={}, businessId={}, action={}, content={}, operatorId={}",
                module, businessId, action, content, operatorId);
    }
}
