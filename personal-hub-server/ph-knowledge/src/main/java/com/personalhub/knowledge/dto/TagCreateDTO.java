package com.personalhub.knowledge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建标签请求
 */
@Data
@Schema(description = "创建标签请求")
public class TagCreateDTO {

    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过50")
    @Schema(description = "标签名称", example = "前端")
    private String name;

    @Schema(description = "标签颜色（十六进制）", example = "#409eff")
    private String color;
}
