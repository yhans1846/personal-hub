package com.personalhub.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalhub.common.exception.BusinessException;
import com.personalhub.system.entity.User;
import com.personalhub.system.mapper.UserMapper;
import com.personalhub.system.service.UserService;
import com.personalhub.system.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public UserProfileVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserProfileVO.from(user);
    }

    @Override
    public void updateProfile(Long userId, String nickname, String email) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("更新资料用户不存在: userId={}", userId);
            throw new BusinessException("用户不存在");
        }
        user.setNickname(nickname);
        user.setEmail(email);
        userMapper.updateById(user);
        log.info("用户更新资料: userId={}", userId);
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("修改密码用户不存在: userId={}", userId);
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("修改密码原密码错误: userId={}", userId);
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        log.info("用户修改密码: userId={}", userId);
    }
}
