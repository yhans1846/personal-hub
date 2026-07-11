package com.personalhub.resource.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏夹实体
 */
@Data
@TableName("bookmark_url")
public class BookmarkUrl {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    private String url;

    private String description;

    private String favicon;

    private Long categoryId;

    /** 标签（逗号分隔） */
    private String tags;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
