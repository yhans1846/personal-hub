package com.personalhub.module.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Dashboard 统计数据 VO
 */
@Data
@Schema(description = "Dashboard 统计数据")
public class DashboardStatsVO {

    @Schema(description = "笔记总数")
    private Long noteCount;

    @Schema(description = "学习记录总数")
    private Long studyCount;

    @Schema(description = "学习总时长（分钟）")
    private Long studyDurationTotal;

    @Schema(description = "本周学习时长")
    private Long studyDurationThisWeek;

    @Schema(description = "待办总数")
    private Long todoTotal;

    @Schema(description = "已完成待办")
    private Long todoDone;

    @Schema(description = "未完成待办")
    private Long todoPending;

    @Schema(description = "已超期待办")
    private Long todoOverdue;

    @Schema(description = "文件总数")
    private Long fileCount;

    @Schema(description = "日记总数")
    private Long diaryCount;

    @Schema(description = "本月日记数")
    private Long diaryCountThisMonth;

    @Schema(description = "收藏总数")
    private Long bookmarkCount;

    @Schema(description = "阅读记录总数")
    private Long readingCount;

    @Schema(description = "学习计划总数")
    private Long studyPlanCount;
}
