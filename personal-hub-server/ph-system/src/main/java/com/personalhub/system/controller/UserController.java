package com.personalhub.system.controller;

import com.personalhub.common.result.Result;
import com.personalhub.common.util.CurrentUser;
import com.personalhub.system.dto.PasswordUpdateDTO;
import com.personalhub.system.dto.UserProfileUpdateDTO;
import com.personalhub.system.service.UserService;
import com.personalhub.system.vo.UserProfileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 用户资料管理
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户资料")
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    @Operation(summary = "获取当前用户资料")
    public Result<UserProfileVO> getProfile(Authentication auth) {
        Long userId = CurrentUser.id(auth);
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    @Operation(summary = "更新个人资料")
    public Result<UserProfileVO> updateProfile(
            @Parameter(hidden = true) Authentication auth,
            @Valid @RequestBody UserProfileUpdateDTO dto) {
        Long userId = CurrentUser.id(auth);
        userService.updateProfile(userId, dto);
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public Result<Void> updatePassword(
            @Parameter(hidden = true) Authentication auth,
            @Valid @RequestBody PasswordUpdateDTO dto) {
        Long userId = CurrentUser.id(auth);
        userService.updatePassword(userId, dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传头像")
    public Result<Map<String, String>> uploadAvatar(Authentication auth, @RequestParam("file") MultipartFile file) {
        Long userId = CurrentUser.id(auth);
        String avatarUrl = userService.uploadAvatar(userId, file);
        log.info("用户上传头像: userId={}", userId);
        return Result.success(Map.of("url", avatarUrl));
    }
}
