package com.personalhub.resource.dto;

import com.personalhub.common.result.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 收藏夹查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "收藏夹查询参数")
public class BookmarkQueryDTO extends PageParam {

    @Schema(description = "搜索关键词（标题/网址/描述）")
    private String keyword;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "标签ID")
    private Long tagId;
}
