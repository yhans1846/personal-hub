package com.personalhub.module.reading.dto;

import com.personalhub.common.result.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "阅读记录查询参数")
public class ReadingQueryDTO extends PageParam {
    @Schema(description = "搜索关键词（书名/作者）") private String keyword;
    @Schema(description = "状态筛选") private Integer status;
}
