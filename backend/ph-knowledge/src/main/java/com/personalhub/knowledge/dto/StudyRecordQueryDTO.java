package com.personalhub.knowledge.dto;

import com.personalhub.common.result.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 学习记录查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学习记录查询参数")
public class StudyRecordQueryDTO extends PageParam {

    @Schema(description = "搜索关键词（匹配主题和内容）")
    private String keyword;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;
}
