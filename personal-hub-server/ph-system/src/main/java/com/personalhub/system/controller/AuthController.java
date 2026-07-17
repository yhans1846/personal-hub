package com.personalhub.system.controller;

import com.personalhub.common.result.Result;
import com.personalhub.system.dto.CaptchaCheckDTO;
import com.personalhub.system.dto.LoginDTO;
import com.personalhub.system.service.AuthService;
import com.personalhub.system.service.CaptchaService;
import com.personalhub.system.vo.CaptchaVO;
import com.personalhub.system.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@Tag(name = "用户认证", description = "登录/退出相关接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    @Operation(summary = "获取滑动拼图验证码")
    @GetMapping("/captcha")
    public Result<CaptchaVO> captcha() {
        return Result.success(captchaService.create());
    }

    @Operation(summary = "拼图松手预检（不消费）")
    @PostMapping("/captcha/check")
    public Result<Map<String, Boolean>> checkCaptcha(@Valid @RequestBody CaptchaCheckDTO dto) {
        boolean matched = captchaService.matches(dto.getCaptchaId(), dto.getSliderX());
        return Result.success(Map.of("matched", matched));
    }

    @Operation(summary = "用户登录", description = "校验滑动拼图后使用用户名和密码登录，返回 JWT Token")
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO result = authService.login(
                dto.getUsername(),
                dto.getPassword(),
                dto.getCaptchaId(),
                dto.getSliderX());
        return Result.success("登录成功", result);
    }

    @Operation(summary = "退出登录", description = "客户端清除 Token 即可")
    @PostMapping("/logout")
    public Result<Void> logout() {
        return Result.success();
    }
}
