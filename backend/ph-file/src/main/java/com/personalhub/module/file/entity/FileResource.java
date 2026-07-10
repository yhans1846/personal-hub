package com.personalhub.module.file.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/** 文件资源实体 */
@Data
@TableName("file_resource")
public class FileResource {
    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 原始文件名 */
    private String name;

    /** 存储文件名（UUID） */
    private String storedName;

    /** 存储路径 */
    private String path;

    /** 大小（字节） */
    private Long size;

    /** 扩展名 */
    private String type;

    /** MIME 类型 */
    private String mimeType;

    /** 文件分类ID */
    private Long categoryId;

    /** 逻辑删除 0-正常 1-删除 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
