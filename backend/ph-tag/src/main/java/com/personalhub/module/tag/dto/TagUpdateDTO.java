package com.personalhub.module.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 更新标签请求
 */
@Data
@Schema(description = "更新标签请求")
public class TagUpdateDTO {

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过50")
    @Schema(description = "标签名称", example = "前端开发")
    private String name;

    @Schema(description = "标签颜色（十六进制）", example = "#67c23a")
    private String color;
}
