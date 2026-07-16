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

    @Schema(description = "搜索关键词（名称/来源/作者/备注）")
    private String keyword;

    @Schema(description = "状态筛选 0-未开始 1-学习中 2-已完成 3-已暂停")
    private Integer status;

    @Schema(description = "标签筛选")
    private Long tagId;

    @Schema(description = "排序字段：updatedAt / createdAt / startDate / endDate / name，默认 updatedAt")
    private String sortBy;

    @Schema(description = "排序方向：asc / desc，默认 desc")
    private String sortDir;
}
