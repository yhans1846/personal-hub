package com.personalhub.module.diary.dto;

import com.personalhub.common.result.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 日记查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "日记查询参数")
public class DiaryQueryDTO extends PageParam {

    @Schema(description = "搜索关键词（标题/内容）")
    private String keyword;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "心情筛选")
    private Integer mood;

    @Schema(description = "按年+月查询（格式：2026-07）")
    private String month;
}
