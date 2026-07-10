package com.personalhub.module.bookmark.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏夹分类 VO
 */
@Data
@Schema(description = "收藏夹分类")
public class BookmarkCategoryVO {

    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "收藏数量")
    private Long count;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;
}
