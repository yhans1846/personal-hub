package com.personalhub.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

/**
 * 个人信息修改 DTO
 */
@Data
@Schema(description = "个人信息修改请求")
public class UserProfileUpdateDTO {

    @NotBlank(message = "昵称不能为空")
    @Size(min = 2, max = 20, message = "昵称长度 2~20 个字符")
    @Schema(description = "昵称", example = "管理员")
    private String nickname;

    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    @Schema(description = "性别 0-保密 1-男 2-女", example = "1")
    private Integer gender;

    @Schema(description = "出生日期", example = "1990-01-01")
    private LocalDate birthday;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确")
    @Schema(description = "手机号", example = "13800138000")
    private String phone;

    @Schema(description = "国家", example = "中国")
    private String country;

    @Schema(description = "省份", example = "广东省")
    private String province;

    @Schema(description = "城市", example = "深圳市")
    private String city;

    @Schema(description = "区/县", example = "南山区")
    private String district;

    @Schema(description = "个人网站", example = "https://example.com")
    private String website;

    @Schema(description = "GitHub 地址", example = "https://github.com/username")
    private String github;

    @Size(max = 200, message = "个人简介不超过 200 字")
    @Schema(description = "个人简介", example = "热爱编程")
    private String bio;
}
