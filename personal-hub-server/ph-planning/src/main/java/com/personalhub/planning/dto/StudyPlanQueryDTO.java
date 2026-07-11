package com.personalhub.planning.dto;

import com.personalhub.common.result.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 学习计划查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "学习计划查询参数")
public class StudyPlanQueryDTO extends PageParam {

    @Schema(description = "搜索关键词（计划名称）")
    private String keyword;

    @Schema(description = "状态筛选 0-未开始 1-进行中 2-已完成 3-已放弃")
    private Integer status;
}
