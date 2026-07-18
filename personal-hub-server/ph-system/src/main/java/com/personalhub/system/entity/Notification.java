package com.personalhub.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 系统通知
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("sys_notification")
public class Notification {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String type;

    private String title;

    private String content;

    private Integer isRead;

    /**
     * 0-正常 1-已清空/忽略
     */
    private Integer isDismissed;

    private Long relatedId;

    private String relatedType;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
