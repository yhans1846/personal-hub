package com.personalhub.module.todo.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 待办任务实体
 */
@Data
@TableName("todo_task")
public class TodoTask {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 任务标题 */
    private String title;

    /** 任务内容 */
    private String content;

    /** 是否完成 0-否 1-是 */
    private Integer isDone;

    /** 优先级 1-高 2-中 3-低 */
    private Integer priority;

    /** 截止日期 */
    private LocalDate dueDate;

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
