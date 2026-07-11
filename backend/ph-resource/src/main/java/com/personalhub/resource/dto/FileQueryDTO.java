package com.personalhub.resource.dto;

import com.personalhub.common.result.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/** 文件查询参数 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文件查询参数")
public class FileQueryDTO extends PageParam {

    @Schema(description = "搜索关键词（文件名）")
    private String keyword;

    @Schema(description = "文件扩展名筛选")
    private String type;

    @Schema(description = "文件分类ID")
    private Long categoryId;
}
