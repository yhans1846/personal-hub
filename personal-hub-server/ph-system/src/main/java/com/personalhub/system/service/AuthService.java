package com.personalhub.system.service;

import com.personalhub.system.vo.LoginVO;

/**
 * 认证服务接口
 */
public interface AuthService {

    /**
     * 用户登录（须先通过滑动拼图）
     *
     * @param username  用户名
     * @param password  密码
     * @param captchaId 验证码 ID
     * @param sliderX   滑块偏移
     * @return 登录结果（包含 JWT Token 和用户信息）
     */
    LoginVO login(String username, String password, String captchaId, Integer sliderX);
}
