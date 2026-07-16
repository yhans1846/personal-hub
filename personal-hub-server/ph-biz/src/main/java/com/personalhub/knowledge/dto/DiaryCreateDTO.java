package com.personalhub.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 创建/编辑日记 DTO
 */
@Data
@Schema(description = "创建/编辑日记请求")
public class DiaryCreateDTO {

    @Schema(description = "日记日期", example = "2026-07-10")
    private LocalDate date;

    @Size(max = 200, message = "标题长度不能超过200")
    @Schema(description = "日记标题", example = "忙碌的一天")
    private String title;

    @Schema(description = "日记内容（Markdown）")
    private String content;

    @Schema(description = "心情 1-很好 2-好 3-一般 4-不好 5-很差", example = "2")
    private Integer mood;

    @Schema(description = "天气", example = "晴")
    private String weather;

    @Size(max = 200, message = "地点长度不能超过200")
    @Schema(description = "地点", example = "图书馆")
    private String location;

    @Schema(description = "配图文件ID")
    private Long imageFileId;
}
