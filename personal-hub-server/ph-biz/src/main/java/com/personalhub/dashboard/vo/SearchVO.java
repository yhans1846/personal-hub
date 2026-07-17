package com.personalhub.dashboard.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

/**
 * 全局搜索结果 VO
 */
@Data
@Schema(description = "全局搜索结果")
public class SearchVO {

    @Schema(description = "搜索结果分组")
    private List<SearchGroup> groups;

    @Schema(description = "结果总数")
    private int total;

    @Data
    @AllArgsConstructor
    @Schema(description = "搜索结果分组")
    public static class SearchGroup {
        @Schema(description = "模块类型", example = "note")
        private String type;

        @Schema(description = "模块名称", example = "笔记")
        private String label;

        @Schema(description = "图标名", example = "FileText")
        private String icon;

        @Schema(description = "该模块结果数")
        private int count;

        @Schema(description = "结果列表")
        private List<SearchItem> items;
    }

    @Data
    @Schema(description = "搜索结果条目")
    public static class SearchItem {
        @Schema(description = "条目ID")
        private Long id;

        @Schema(description = "标题")
        private String title;

        @Schema(description = "摘要/描述")
        private String snippet;

        @Schema(description = "日期")
        private String date;

        @Schema(description = "跳转路径", example = "/notes/1/edit")
        private String url;
    }
}
