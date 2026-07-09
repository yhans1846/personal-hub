package com.personalhub.module.auth.service.impl;

import com.personalhub.common.exception.BusinessException;
import com.personalhub.common.util.JwtUtil;
import com.personalhub.module.auth.service.AuthService;
import com.personalhub.module.auth.vo.LoginVO;
import com.personalhub.module.user.entity.User;
import com.personalhub.module.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 认证服务实现
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(String username, String password) {
        User user = userService.getByUsername(username);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return LoginVO.of(token, user);
    }
}
