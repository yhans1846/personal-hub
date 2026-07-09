package com.personalhub.module.auth.vo;

import com.personalhub.module.user.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 登录返回 VO
 */
@Data
@Builder
@Schema(description = "登录响应")
public class LoginVO {

    @Schema(description = "JWT Token")
    private String token;

    @Schema(description = "用户信息")
    private UserInfo user;

    @Data
    @Builder
    @Schema(description = "用户简要信息")
    public static class UserInfo {
        @Schema(description = "用户ID", example = "1")
        private Long id;
        @Schema(description = "用户名", example = "admin")
        private String username;
        @Schema(description = "昵称", example = "管理员")
        private String nickname;
        @Schema(description = "头像URL")
        private String avatar;

        public static UserInfo from(User user) {
            return UserInfo.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .nickname(user.getNickname())
                    .avatar(user.getAvatar())
                    .build();
        }
    }

    public static LoginVO of(String token, User user) {
        return LoginVO.builder()
                .token(token)
                .user(UserInfo.from(user))
                .build();
    }
}
