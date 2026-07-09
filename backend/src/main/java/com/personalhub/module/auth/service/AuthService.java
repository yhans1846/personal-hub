package com.personalhub.module.auth.service;

import com.personalhub.module.auth.vo.LoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录
     */
    LoginVO login(String username, String password);
}
