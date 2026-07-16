package com.personalhub.planning.vo;

import com.personalhub.knowledge.vo.TagVO;
import com.personalhub.planning.entity.StudyPlan;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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

    @Schema(description = "来源")
    private String source;

    @Schema(description = "作者")
    private String author;

    @Schema(description = "资源地址")
    private String url;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "进度百分比 0-100")
    private Integer progress;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "状态 0-未开始 1-学习中 2-已完成 3-已暂停")
    private Integer status;

    @Schema(description = "状态中文标签")
    private String statusLabel;

    @Schema(description = "分类标签")
    private List<TagVO> tags;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    private LocalDateTime updatedAt;

    /**
     * 从实体转换
     *
     * @param plan 实体
     * @return VO
     */
    public static StudyPlanVO from(StudyPlan plan) {
        StudyPlanVO vo = new StudyPlanVO();
        vo.setId(plan.getId());
        vo.setName(plan.getName());
        vo.setSource(plan.getSource());
        vo.setAuthor(plan.getAuthor());
        vo.setUrl(plan.getUrl());
        vo.setRemark(plan.getRemark());
        vo.setProgress(plan.getProgress() != null ? plan.getProgress() : 0);
        vo.setStartDate(plan.getStartDate());
        vo.setEndDate(plan.getEndDate());
        vo.setStatus(plan.getStatus());
        vo.setCreatedAt(plan.getCreatedAt());
        vo.setUpdatedAt(plan.getUpdatedAt());
        if (plan.getStatus() != null) {
            vo.setStatusLabel(switch (plan.getStatus()) {
                case 0 -> "未开始";
                case 1 -> "学习中";
                case 2 -> "已完成";
                case 3 -> "已暂停";
                default -> "未知";
            });
        }
        return vo;
    }
}
