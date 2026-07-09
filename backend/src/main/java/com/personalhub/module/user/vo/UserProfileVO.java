package com.personalhub.module.user.vo;

import com.personalhub.module.user.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户信息 VO
 */
@Data
public class UserProfileVO {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private String email;
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
