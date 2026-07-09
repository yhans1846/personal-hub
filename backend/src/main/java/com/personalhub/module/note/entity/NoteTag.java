package com.personalhub.module.note.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记标签实体
 */
@Data
@TableName("note_tag")
public class NoteTag {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;
    private String name;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
