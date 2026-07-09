package com.personalhub.module.user.vo;

import com.personalhub.module.user.entity.User;
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

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    public static UserProfileVO from(User user) {
        UserProfileVO vo = new UserProfileVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setEmail(user.getEmail());
        vo.setCreatedAt(user.getCreatedAt());
        return vo;
    }
}
