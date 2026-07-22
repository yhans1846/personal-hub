package com.personalhub.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 粘贴 Markdown 内容导入请求
 */
@Data
@Schema(description = "粘贴 Markdown 内容导入")
public class ImportContentDTO {

    @NotBlank(message = "Markdown 内容不能为空")
    @Schema(description = "Markdown 正文内容", example = "# 笔记标题\n\n这是正文内容")
    private String content;

    @Schema(description = "笔记标题（可选，不传则从内容提取）")
    private String title;

    @Schema(description = "分类ID列表", example = "[1, 2]")
    private List<Long> categoryIds;

    @Schema(description = "标签ID列表", example = "[1, 3]")
    private List<Long> tagIds;

    @Schema(description = "文件夹ID（可选，空=未分类）")
    private Long folderId;
}
