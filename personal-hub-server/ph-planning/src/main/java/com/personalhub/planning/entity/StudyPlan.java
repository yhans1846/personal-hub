package com.personalhub.planning.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习计划实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("study_plan")
public class StudyPlan {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    /** 来源（如 B站） */
    private String source;

    /** 作者 */
    private String author;

    /** 资源地址 */
    private String url;

    /** 备注 */
    private String remark;

    /** 进度百分比 0-100 */
    @Builder.Default
    private Integer progress = 0;

    private LocalDate startDate;

    private LocalDate endDate;

    /** 状态 0-未开始 1-学习中 2-已完成 3-已暂停 */
    @Builder.Default
    private Integer status = 0;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
