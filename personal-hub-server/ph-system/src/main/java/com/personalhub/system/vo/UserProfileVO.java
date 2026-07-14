package com.personalhub.system.vo;

import com.personalhub.system.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息 VO
 */
@Data
@Schema(description = "用户个人信息")
public class UserProfileVO {

    @Schema(description = "用户ID", example = "1")
    private Long id;

    @Schema(description = "用户名", example = "admin")
    private String username;

    @Schema(description = "昵称", example = "管理员")
    private String nickname;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "邮箱", example = "admin@example.com")
    private String email;

    @Schema(description = "性别 0-保密 1-男 2-女", example = "1")
    private Integer gender;

    @Schema(description = "出生日期", example = "1990-01-01")
    private java.time.LocalDate birthday;

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

    @Schema(description = "个人简介", example = "热爱编程")
    private String bio;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    public static UserProfileVO from(User user) {
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setGender(user.getGender());
        vo.setBirthday(user.getBirthday());
        vo.setPhone(user.getPhone());
        vo.setCountry(user.getCountry());
        vo.setProvince(user.getProvince());
        vo.setCity(user.getCity());
        vo.setDistrict(user.getDistrict());
        vo.setWebsite(user.getWebsite());
        vo.setGithub(user.getGithub());
        vo.setBio(user.getBio());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
