package com.personalhub.system.controller;

import com.personalhub.common.result.Result;
import com.personalhub.system.dto.UserProfileUpdateDTO;
import com.personalhub.system.service.UserService;
import com.personalhub.system.vo.UserProfileVO;
import com.personalhub.storage.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
    private final StorageService storageService;

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;

    @GetMapping("/profile")
    @Operation(summary = "获取当前用户资料")
    public Result<UserProfileVO> getProfile(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return Result.success(userService.getProfile(userId));
    }

    @PutMapping("/profile")
    @Operation(summary = "更新个人资料")
    public Result<UserProfileVO> updateProfile(Authentication auth, @Valid @RequestBody UserProfileUpdateDTO dto) {
        Long userId = Long.valueOf(auth.getName());
        userService.updateProfile(userId, dto);
        return Result.success(userService.getProfile(userId));
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "上传头像")
    public Result<Map<String, String>> uploadAvatar(Authentication auth, @RequestParam("file") MultipartFile file) {
        Long userId = Long.valueOf(auth.getName());

        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }

        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null) {
            int dot = originalName.lastIndexOf('.');
            if (dot > 0) ext = originalName.substring(dot + 1).toLowerCase();
        }
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("仅支持 JPG/PNG/GIF/WebP 格式");
        }
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new IllegalArgumentException("头像文件不能超过 5MB");
        }

        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String relativePath = "avatars/" + storedName;

        storageService.store(file, relativePath);

        String avatarUrl = "/api/files/avatar/" + storedName;

        userService.updateAvatar(userId, avatarUrl);
        log.info("用户上传头像: userId={}, path={}", userId, relativePath);

        return Result.success(Map.of("url", avatarUrl));
    }
}
