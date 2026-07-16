package com.personalhub.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 标签关联实体（多态关联）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
