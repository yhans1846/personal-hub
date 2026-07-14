package com.personalhub.system.service;

import com.personalhub.system.dto.UserProfileUpdateDTO;
import com.personalhub.system.entity.User;
import com.personalhub.system.vo.UserProfileVO;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 根据用户名查询用户
     */
    User getByUsername(String username);

    /**
     * 获取当前用户信息
     */
    UserProfileVO getProfile(Long userId);

    /**
     * 修改个人信息
     */
    void updateProfile(Long userId, UserProfileUpdateDTO dto);

    /**
     * 更新头像
     */
    void updateAvatar(Long userId, String avatarUrl);

    /**
     * 修改密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);
}
