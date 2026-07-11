package com.personalhub.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 笔记实体
 */
@Data
@TableName("note_note")
public class Note {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 笔记标题 */
    private String title;

    /** 笔记MD文件路径(notes/{id}/note.md) */
    private String mdPath;

    /** 是否收藏 0-否 1-是 */
    private Integer isFavorite;

    /** 逻辑删除 0-正常 1-删除 */
    private Integer isDeleted;

    /** 删除时间（回收站排序/展示） */
    private LocalDateTime deletedAt;

    /** 删除原因（USER_DELETE/AUTO_ARCHIVE） */
    private String deleteReason;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
