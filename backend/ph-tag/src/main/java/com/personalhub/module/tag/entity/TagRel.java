package com.personalhub.module.tag.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 标签关联实体（多态关联）
 */
@Data
@TableName("tag_rel")
public class TagRel {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long tagId;

    /** 关联实体类型：note / bookmark / diary / study / todo / file / reading / study_plan */
    private String entityType;

    private Long entityId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
