package com.personalhub.module.auth.controller;

import com.personalhub.common.result.Result;
import com.personalhub.module.auth.dto.LoginDTO;
import com.personalhub.module.auth.service.AuthService;
import com.personalhub.module.auth.vo.LoginVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO result = authService.login(dto.getUsername(), dto.getPassword());
        return Result.success("登录成功", result);
    }

    /**
     * 退出登录（客户端清除 Token 即可）
     */
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
