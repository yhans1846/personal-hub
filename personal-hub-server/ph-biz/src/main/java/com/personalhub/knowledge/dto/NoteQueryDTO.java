package com.personalhub.knowledge.dto;

import com.personalhub.common.result.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 笔记查询 DTO
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "笔记查询参数")
public class NoteQueryDTO extends PageParam {

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "分类ID筛选")
    private Long categoryId;

    @Schema(description = "标签ID筛选")
    private Long tagId;

    /**
     * 文件夹筛选：缺省/空=全部；{@code none}=未分类；数字字符串=该夹直属笔记。
     */
    @Schema(description = "文件夹筛选：none=未分类；数字=夹ID；缺省=全部")
    private String folderId;

    @Schema(description = "收藏筛选")
    private Boolean isFavorite;

    @Schema(description = "回收站筛选")
    private Boolean isDeleted;
}
