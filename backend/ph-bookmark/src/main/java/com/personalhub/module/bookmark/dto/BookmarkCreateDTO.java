package com.personalhub.module.bookmark.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建/编辑收藏 DTO
 */
@Data
@Schema(description = "创建/编辑收藏请求")
public class BookmarkCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 255, message = "标题长度不能超过255")
    @Schema(description = "标题", example = "GitHub")
    private String title;

    @NotBlank(message = "网址不能为空")
    @Size(max = 2048, message = "网址长度不能超过2048")
    @Schema(description = "网址", example = "https://github.com")
    private String url;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "图标URL")
    private String favicon;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "标签（逗号分隔）", example = "开发,工具")
    private String tags;
}
