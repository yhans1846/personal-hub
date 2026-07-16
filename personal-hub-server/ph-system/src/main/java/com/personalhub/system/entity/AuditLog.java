package com.personalhub.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 统一审计日志
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("audit_log")
public class AuditLog {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 业务模块（NOTE/TODO/FILE...） */
    private String module;

    /** 业务ID */
    private Long businessId;

    /** 操作类型（DELETE/RESTORE/CREATE/UPDATE...） */
    private String action;

    /** 操作描述 */
    private String content;

    /** 操作用户ID */
    private Long operatorId;

    /** 操作时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
