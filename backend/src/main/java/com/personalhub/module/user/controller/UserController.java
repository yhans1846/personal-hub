package com.personalhub.module.user.controller;

import com.personalhub.common.result.Result;
import com.personalhub.module.user.dto.PasswordUpdateDTO;
import com.personalhub.module.user.dto.UserProfileUpdateDTO;
import com.personalhub.module.user.service.UserService;
import com.personalhub.module.user.vo.UserProfileVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * 获取个人信息
     */
    @GetMapping("/profile")
    public Result<UserProfileVO> getProfile(Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(userService.getProfile(userId));
    }

    /**
     * 修改个人信息
     */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Valid @RequestBody UserProfileUpdateDTO dto,
                                       Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        userService.updateProfile(userId, dto.getNickname(), dto.getEmail());
        return Result.success();
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    public Result<Void> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto,
                                        Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        userService.updatePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }
}
