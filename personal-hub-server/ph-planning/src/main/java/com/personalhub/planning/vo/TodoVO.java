package com.personalhub.planning.vo;

import com.personalhub.planning.entity.TodoTask;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 待办任务 VO
 */
@Data
@Schema(description = "待办任务详情")
public class TodoVO {

    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "任务标题")
    private String title;

    @Schema(description = "任务内容")
    private String content;

    @Schema(description = "是否完成 0-否 1-是")
    private Integer isDone;

    @Schema(description = "优先级 1-高 2-中 3-低")
    private Integer priority;

    @Schema(description = "优先级中文标签")
    private String priorityLabel;

    @Schema(description = "截止日期")
    private LocalDate dueDate;

    @Schema(description = "是否超期（截止日期已过且未完成）")
    private Boolean isOverdue;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public static TodoVO from(TodoTask task) {
        TodoVO vo = new TodoVO();
        vo.setId(task.getId());
        vo.setTitle(task.getTitle());
        vo.setContent(task.getContent());
        vo.setIsDone(task.getIsDone());
        vo.setPriority(task.getPriority());
        vo.setDueDate(task.getDueDate());
        vo.setCreatedAt(task.getCreatedAt());
        vo.setUpdatedAt(task.getUpdatedAt());
        // 设置优先级标签
        if (task.getPriority() != null) {
            vo.setPriorityLabel(switch (task.getPriority()) {
                case 1 -> "高";
                case 2 -> "中";
                case 3 -> "低";
                default -> "未知";
            });
        }
        // 检查是否超期
        if (task.getDueDate() != null && (task.getIsDone() == null || task.getIsDone() == 0)) {
            vo.setIsOverdue(task.getDueDate().isBefore(LocalDate.now()));
        } else {
            vo.setIsOverdue(false);
        }
        return vo;
    }
}
