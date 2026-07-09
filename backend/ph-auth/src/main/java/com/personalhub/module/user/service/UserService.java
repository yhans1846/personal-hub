package com.personalhub.module.user.service;

import com.personalhub.module.user.entity.User;
import com.personalhub.module.user.vo.UserProfileVO;

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
    void updateProfile(Long userId, String nickname, String email);

    /**
     * 修改密码
     */
    void updatePassword(Long userId, String oldPassword, String newPassword);
}
