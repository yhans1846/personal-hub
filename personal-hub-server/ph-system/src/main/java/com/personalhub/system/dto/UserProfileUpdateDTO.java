package com.personalhub.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 个人信息修改 DTO
 */
@Data
@Schema(description = "个人信息修改请求")
public class UserProfileUpdateDTO {

    @Size(max = 50, message = "昵称长度不能超过50")
    @Schema(description = "昵称", example = "管理员")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;
}
