package com.personalhub.module.note.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 创建/编辑笔记 DTO
 */
@Data
@Schema(description = "创建/编辑笔记请求")
public class NoteCreateDTO {

    @NotBlank(message = "标题不能为空")
    @Size(max = 200, message = "标题长度不能超过200")
    @Schema(description = "笔记标题", example = "Spring Boot 学习笔记")
    private String title;

    @Schema(description = "Markdown 内容")
    private String content;

    @Schema(description = "关联分类ID列表", example = "[1, 2]")
    private List<Long> categoryIds;

    @Schema(description = "关联标签ID列表", example = "[1, 3]")
    private List<Long> tagIds;
}
