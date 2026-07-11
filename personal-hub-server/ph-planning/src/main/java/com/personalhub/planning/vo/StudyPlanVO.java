package com.personalhub.planning.vo;

import com.personalhub.planning.entity.StudyPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 学习计划 VO
 */
@Data
@Schema(description = "学习计划详情")
public class StudyPlanVO {

    @Schema(description = "计划ID")
    private Long id;

    @Schema(description = "计划名称")
    private String name;

    @Schema(description = "学习目标")
    private String goal;

    @Schema(description = "进度百分比 0-100")
    private Integer progress;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "状态 0-未开始 1-进行中 2-已完成 3-已放弃")
    private Integer status;

    @Schema(description = "状态中文标签")
    private String statusLabel;

    @Schema(description = "关联学习记录数")
    private Long recordCount;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    public static StudyPlanVO from(StudyPlan plan) {
        StudyPlanVO vo = new StudyPlanVO();
        vo.setId(plan.getId());
        vo.setName(plan.getName());
        vo.setGoal(plan.getGoal());
        vo.setProgress(plan.getProgress());
        vo.setStartDate(plan.getStartDate());
        vo.setEndDate(plan.getEndDate());
        vo.setStatus(plan.getStatus());
        vo.setCreatedAt(plan.getCreatedAt());
        vo.setUpdatedAt(plan.getUpdatedAt());
        // 状态标签
        if (plan.getStatus() != null) {
            vo.setStatusLabel(switch (plan.getStatus()) {
                case 0 -> "未开始";
                case 1 -> "进行中";
                case 2 -> "已完成";
                case 3 -> "已放弃";
                default -> "未知";
            });
        }
        return vo;
    }
}
