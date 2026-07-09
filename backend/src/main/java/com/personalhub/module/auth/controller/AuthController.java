package com.personalhub.module.auth.controller;

import com.personalhub.common.result.Result;
import com.personalhub.module.auth.dto.LoginDTO;
import com.personalhub.module.auth.service.AuthService;
import com.personalhub.module.auth.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@Tag(name = "用户认证", description = "登录/退出相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户登录", description = "使用用户名和密码登录，返回 JWT Token")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO result = authService.login(dto.getUsername(), dto.getPassword());
        return Result.success("登录成功", result);
    }

    @Operation(summary = "退出登录", description = "客户端清除 Token 即可")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
