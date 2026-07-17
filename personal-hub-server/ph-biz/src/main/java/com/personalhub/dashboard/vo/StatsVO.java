package com.personalhub.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 综合统计数据 VO（统计页面专用）
 */
@Data
@Schema(description = "综合统计数据")
public class StatsVO {

    // ========== KPI ==========

    @Schema(description = "笔记总数")
    private Long noteCount;
    @Schema(description = "笔记数较上周变化百分比")
    private Long noteCountChange;

    @Schema(description = "阅读总时长（小时）")
    private Double readingHours;
    @Schema(description = "阅读时长较上周变化百分比")
    private Long readingHoursChange;

    @Schema(description = "待办完成率 0-100")
    private Double todoCompletionRate;
    @Schema(description = "完成率较上周变化（百分点）")
    private Double todoCompletionChange;

    @Schema(description = "连续学习天数")
    private Integer streakDays;
    @Schema(description = "历史最长连续天数")
    private Integer bestStreakDays;

    // ========== 趋势 ==========

    @Schema(description = "学习趋势（面积图数据）")
    private List<DataPoint> studyTrend;

    @Schema(description = "笔记新增趋势（柱状图数据）")
    private List<DataPoint> noteTrend;

    // ========== Todo 统计 ==========

    @Schema(description = "已完成待办")
    private Long todoDone;
    @Schema(description = "进行中待办")
    private Long todoPending;
    @Schema(description = "超期待办")
    private Long todoOverdue;

    // ========== 分类 / 标签 ==========

    @Schema(description = "分类统计 TopN")
    private List<NamedStat> categoryStats;

    @Schema(description = "标签排行 TopN")
    private List<NamedStat> tagStats;

    // ========== 最近活动 ==========

    @Schema(description = "最近活动列表")
    private List<ActivityItem> recentActivity;

    // ========== 洞察 ==========

    @Schema(description = "学习洞察列表")
    private List<InsightItem> insights;

    // ========== 笔记柱状图辅助数据 ==========

    @Schema(description = "日均新增笔记")
    private Double avgDailyNotes;
    @Schema(description = "单日最高新增笔记数")
    private Long maxDailyNotes;
    @Schema(description = "单日最低新增笔记数（有活动日）")
    private Long minDailyNotes;

    // ========== 内部类 ==========

    @Data
    @Schema(description = "趋势数据点")
    public static class DataPoint {
        @Schema(description = "日期 YYYY-MM-DD")
        private String date;
        @Schema(description = "数值")
        private long value;

        public DataPoint() {}

        public DataPoint(String date, long value) {
            this.date = date;
            this.value = value;
        }
    }

    @Data
    @Schema(description = "命名统计（分类/标签）")
    public static class NamedStat {
        @Schema(description = "名称")
        private String name;
        @Schema(description = "计数")
        private Long count;
        @Schema(description = "颜色（标签专用）")
        private String color;
    }

    @Data
    @Schema(description = "最近活动条目")
    public static class ActivityItem {
        @Schema(description = "活动 ID")
        private Long id;
        @Schema(description = "业务模块")
        private String module;
        @Schema(description = "操作类型")
        private String action;
        @Schema(description = "操作描述")
        private String content;
        @Schema(description = "操作时间")
        private LocalDateTime createdAt;
        @Schema(description = "操作时间（格式化）")
        private String timeLabel;
    }

    @Data
    @Schema(description = "学习洞察条目")
    public static class InsightItem {
        @Schema(description = "洞察类型：efficiency/streak/category/reading/day/todo")
        private String type;
        @Schema(description = "Emoji 图标")
        private String icon;
        @Schema(description = "标题")
        private String title;
        @Schema(description = "描述")
        private String description;
    }
}
