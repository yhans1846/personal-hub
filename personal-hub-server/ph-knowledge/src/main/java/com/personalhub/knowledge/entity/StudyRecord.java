package com.personalhub.knowledge.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习记录实体
 */
@Data
@TableName("study_record")
public class StudyRecord {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 学习主题 */
    private String subject;

    /** 学习日期 */
    private LocalDate date;

    /** 学习时长（分钟） */
    private Integer duration;

    /** 学习内容 */
    private String content;

    /** 学习心得 */
    private String reflection;

    /** 关联学习计划ID */
    private Long planId;

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
