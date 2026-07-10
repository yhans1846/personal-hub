package com.personalhub.module.diary.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 日记条目实体
 */
@Data
@TableName("diary_entry")
public class DiaryEntry {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 日记日期 */
    private LocalDate date;

    /** 日记标题 */
    private String title;

    /** 日记内容（Markdown） */
    private String content;

    /** 心情 1-很好 2-好 3-一般 4-不好 5-很差 */
    private Integer mood;

    /** 天气 */
    private String weather;

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
