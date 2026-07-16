package com.personalhub.system.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户布局配置实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_layout")
public class UserLayout {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 配置类型: menu / dashboard */
    private String layoutType;

    /** 布局配置JSON内容 */
    private String layoutJson;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer isDeleted;
}
