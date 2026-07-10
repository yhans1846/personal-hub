package com.personalhub.module.bookmark.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏夹分类实体
 */
@Data
@TableName("bookmark_category")
public class BookmarkCategory {

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
