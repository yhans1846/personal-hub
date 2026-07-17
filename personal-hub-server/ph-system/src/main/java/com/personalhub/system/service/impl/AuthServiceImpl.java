package com.personalhub.system.service.impl;

import com.personalhub.common.exception.BusinessException;
import com.personalhub.common.util.JwtUtil;
import com.personalhub.system.service.AuthService;
import com.personalhub.system.service.CaptchaService;
import com.personalhub.system.vo.LoginVO;
import com.personalhub.system.entity.User;
import com.personalhub.system.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final CaptchaService captchaService;

    @Override
    public LoginVO login(String username, String password, String captchaId, Integer sliderX) {
        captchaService.verifyAndConsume(captchaId, sliderX);

        User user = userService.getByUsername(username);
        if (user == null) {
            log.warn("登录失败，用户不存在: {}", username);
            throw new BusinessException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("登录失败，密码错误: userId={}", user.getId());
            throw new BusinessException("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        log.info("用户登录成功: userId={}, username={}", user.getId(), username);
        return LoginVO.of(token, user);
    }
}
