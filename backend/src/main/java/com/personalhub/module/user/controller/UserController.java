package com.personalhub.module.user.controller;

import com.personalhub.common.result.Result;
import com.personalhub.module.user.dto.PasswordUpdateDTO;
import com.personalhub.module.user.dto.UserProfileUpdateDTO;
import com.personalhub.module.user.service.UserService;
import com.personalhub.module.user.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@Tag(name = "用户信息", description = "个人信息管理相关接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取个人信息")
    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile(
            @Parameter(hidden = true) Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(userService.getProfile(userId));
    }

    @Operation(summary = "修改个人信息", description = "修改昵称和邮箱")
    @PutMapping("/profile")
    public Result<Void> updateProfile(
            @Valid @RequestBody UserProfileUpdateDTO dto,
            @Parameter(hidden = true) Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        userService.updateProfile(userId, dto.getNickname(), dto.getEmail());
        return Result.success();
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(
            @Valid @RequestBody PasswordUpdateDTO dto,
            @Parameter(hidden = true) Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        userService.updatePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }
}
