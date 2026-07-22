package com.personalhub.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 日记配图：从 URL 下载
 */
@Data
@Schema(description = "从链接添加日记配图")
public class DiaryImageFromUrlDTO {

    @NotBlank(message = "图片链接不能为空")
    @Schema(description = "http(s) 图片 URL", example = "https://example.com/a.png")
    private String url;
}
