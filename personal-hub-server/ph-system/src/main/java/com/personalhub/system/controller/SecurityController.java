package com.personalhub.system.controller;

import com.personalhub.common.result.Result;
import com.personalhub.system.service.ImageCaptchaService;
import com.personalhub.system.vo.ImageCaptchaVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 危险操作验证码（需登录）
 */
@Tag(name = "安全", description = "危险操作验证码")
@RestController
@RequestMapping("/api/security")
@RequiredArgsConstructor
public class SecurityController {

    private final ImageCaptchaService imageCaptchaService;

    @Operation(summary = "获取字符图验证码")
    @GetMapping("/image-captcha")
    public Result<ImageCaptchaVO> imageCaptcha() {
        return Result.success(imageCaptchaService.create());
    }
}
