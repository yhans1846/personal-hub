package com.personalhub.resource.vo;

import com.personalhub.resource.entity.FileCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/** 文件分类 VO */
@Data
@Schema(description = "文件分类")
public class FileCategoryVO {
    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "排序")
    private Integer sortOrder;

    public static FileCategoryVO from(FileCategory category) {
        FileCategoryVO vo = new FileCategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setSortOrder(category.getSortOrder());
        return vo;
    }
}
