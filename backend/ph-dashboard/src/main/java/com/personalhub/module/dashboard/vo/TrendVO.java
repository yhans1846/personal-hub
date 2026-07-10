package com.personalhub.module.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 趋势数据 VO
 */
@Data
@Schema(description = "趋势数据")
public class TrendVO {

    @Schema(description = "学习趋势（每日时长）")
    private List<DataPoint> studyTrend;

    @Schema(description = "笔记趋势（每日新增）")
    private List<DataPoint> noteTrend;

    @Schema(description = "待办趋势（每日新增）")
    private List<DataPoint> todoTrend;

    @Schema(description = "阅读趋势（每日新增）")
    private List<DataPoint> readingTrend;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "数据点")
    public static class DataPoint {
        @Schema(description = "日期", example = "2026-07-01")
        private String date;

        @Schema(description = "数值", example = "120")
        private long value;
    }
}
