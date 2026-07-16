package com.personalhub.knowledge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新分类 DTO
 */
@Data
public class CategoryUpdateDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称最长50个字符")
    private String name;

    private Integer sortOrder;
}
