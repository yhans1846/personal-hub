package com.personalhub.knowledge.dto;

import com.personalhub.common.result.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "阅读记录查询参数")
public class ReadingQueryDTO extends PageParam {
    @Schema(description = "搜索关键词（书名/作者）")
    private String keyword;

    @Schema(description = "状态筛选 0-未读 1-在读 2-读完")
    private Integer status;

    @Schema(description = "排序字段：updatedAt / createdAt / progress / startDate / bookTitle，默认 updatedAt")
    private String sortBy;

    @Schema(description = "排序方向：asc / desc，默认 desc")
    private String sortDir;
}
