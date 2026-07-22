package com.personalhub.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/** 笔记文件夹 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("note_folder")
public class NoteFolder {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    /** 父文件夹，根为 null */
    private Long parentId;

    private String name;

    @Builder.Default
    private Integer sortOrder = 0;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
