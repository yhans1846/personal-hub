package com.personalhub.resource.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 创建/编辑文件分类请求 */
@Data
@Schema(description = "创建/编辑文件分类请求")
public class FileCategoryCreateDTO {
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50")
    @Schema(description = "分类名称", example = "文档")
    private String name;

    @Schema(description = "排序", example = "0")
    private Integer sortOrder;
}
