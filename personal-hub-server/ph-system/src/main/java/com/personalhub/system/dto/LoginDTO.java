package com.personalhub.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 登录请求 DTO
 */
@Data
@Schema(description = "用户登录请求")
public class LoginDTO {

    @NotBlank(message = "用户名不能为空")
    @Schema(description = "用户名", example = "admin")
    private String username;

    @NotBlank(message = "密码不能为空")
    @Schema(description = "密码", example = "123456")
    private String password;

    @NotBlank(message = "请完成书架验证")
    @Schema(description = "书架验证码 ID")
    private String captchaId;

    @NotNull(message = "请完成书架验证")
    @Schema(description = "选中的书架槽位下标")
    private Integer sliderX;
}
