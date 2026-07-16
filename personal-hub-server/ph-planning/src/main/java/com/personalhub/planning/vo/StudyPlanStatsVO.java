package com.personalhub.planning.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 学习计划状态统计
 */
@Data
@Schema(description = "学习计划状态统计")
public class StudyPlanStatsVO {

    @Schema(description = "总数")
    private long total;

    @Schema(description = "未开始")
    private long pending;

    @Schema(description = "学习中")
    private long learning;

    @Schema(description = "已完成")
    private long done;

    @Schema(description = "已暂停")
    private long paused;
}
