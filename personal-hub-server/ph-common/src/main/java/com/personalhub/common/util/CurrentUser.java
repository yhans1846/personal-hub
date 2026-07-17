package com.personalhub.common.util;

import org.springframework.security.core.Authentication;

/**
 * 从 Spring Security Authentication 解析当前用户 ID。
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    /**
     * @param authentication 当前认证，principal name 为用户 ID 字符串
     * @return 用户 ID
     */
    public static Long id(Authentication authentication) {
        return Long.valueOf(authentication.getName());
    }
}
