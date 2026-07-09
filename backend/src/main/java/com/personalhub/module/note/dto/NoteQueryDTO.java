package com.personalhub.module.note.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 笔记查询 DTO
 */
@Data
@Schema(description = "笔记查询参数")
public class NoteQueryDTO {

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页条数", example = "10")
    private Integer size = 10;

    @Schema(description = "搜索关键词")
    private String keyword;

    @Schema(description = "分类ID筛选")
    private Long categoryId;

    @Schema(description = "标签ID筛选")
    private Long tagId;

    @Schema(description = "收藏筛选")
    private Boolean isFavorite;

    @Schema(description = "回收站筛选")
    private Boolean isDeleted;
}
