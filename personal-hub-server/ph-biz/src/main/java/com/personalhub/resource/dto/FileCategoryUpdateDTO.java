package com.personalhub.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 更新文件分类 */
@Data
@Schema(description = "更新文件分类")
public class FileCategoryUpdateDTO {

    @Schema(description = "分类ID，null 表示清除分类")
    private Long categoryId;
}
