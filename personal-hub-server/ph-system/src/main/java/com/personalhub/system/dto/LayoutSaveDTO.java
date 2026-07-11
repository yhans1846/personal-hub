package com.personalhub.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 保存布局配置请求
 */
@Data
@Schema(description = "保存布局配置请求")
public class LayoutSaveDTO {

    @NotBlank(message = "布局类型不能为空")
    @Schema(description = "布局类型: menu / dashboard", example = "menu")
    private String layoutType;

    @NotBlank(message = "布局内容不能为空")
    @Schema(description = "布局配置 JSON", example = "{\"items\":[{\"code\":\"dashboard\",\"visible\":true,\"order\":1}]}")
    private String layoutJson;
}
