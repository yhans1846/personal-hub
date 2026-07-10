package com.personalhub.module.todo.dto;

import com.personalhub.common.result.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 待办任务查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "待办任务查询参数")
public class TodoQueryDTO extends PageParam {

    @Schema(description = "搜索关键词（标题）")
    private String keyword;

    @Schema(description = "优先级筛选 1-高 2-中 3-低")
    private Integer priority;

    @Schema(description = "完成状态筛选")
    private Boolean isDone;
}
