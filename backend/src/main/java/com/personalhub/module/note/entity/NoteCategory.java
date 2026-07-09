package com.personalhub.module.note.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记分类实体
 */
@Data
@TableName("note_category")
public class NoteCategory {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String name;
    private Integer sortOrder;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
