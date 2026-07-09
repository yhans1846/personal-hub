package com.personalhub.module.auth.vo;

import com.personalhub.module.user.entity.User;
import lombok.Builder;
import lombok.Data;

/**
 * 登录返回 VO
 */
@Data
@Builder
public class LoginVO {

    private String token;
    private UserInfo user;

    @Data
    @Builder
    public static class UserInfo {
        private Long id;
        private String username;
        private String nickname;
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
