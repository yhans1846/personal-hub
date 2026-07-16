package com.personalhub.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建分类 DTO
 */
@Data
public class CategoryCreateDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称最长50个字符")
    private String name;

    private Integer sortOrder;

    @NotBlank(message = "分类类型不能为空")
    private String type; // "note", "bookmark", "file"
}
