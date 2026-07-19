package com.personalhub.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.personalhub.common.exception.BusinessException;
import com.personalhub.storage.FileAssetService;
import com.personalhub.storage.FileUploadValidator;
import com.personalhub.storage.MultipartPayloads;
import com.personalhub.storage.StoragePaths;
import com.personalhub.system.dto.UserProfileUpdateDTO;
import com.personalhub.system.entity.User;
import com.personalhub.system.mapper.UserMapper;
import com.personalhub.system.service.UserService;
import com.personalhub.system.vo.UserProfileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.UUID;

/**
 * 用户服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final long MAX_AVATAR_SIZE = 5 * 1024 * 1024;

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileAssetService fileAssetService;

    @Override
    public User getByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username);
        return userMapper.selectOne(wrapper);
    }

    @Override
    public UserProfileVO getProfile(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserProfileVO.from(user);
    }

    @Override
    public void updateProfile(Long userId, UserProfileUpdateDTO dto) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("更新资料用户不存在: userId={}", userId);
            throw new BusinessException("用户不存在");
        }
        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail());
        user.setGender(dto.getGender());
        user.setBirthday(dto.getBirthday());
        user.setPhone(dto.getPhone());
        user.setCountry(dto.getCountry());
        user.setProvince(dto.getProvince());
        user.setCity(dto.getCity());
        user.setDistrict(dto.getDistrict());
        user.setWebsite(dto.getWebsite());
        user.setGithub(dto.getGithub());
        user.setBio(dto.getBio());
        userMapper.updateById(user);
        log.info("用户更新资料: userId={}", userId);
    }

    @Override
    public void updateAvatar(Long userId, String avatarUrl) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("更新头像用户不存在: userId={}", userId);
            throw new BusinessException("用户不存在");
        }
        user.setAvatar(avatarUrl);
        userMapper.updateById(user);
        log.info("用户更新头像: userId={}", userId);
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        FileUploadValidator.requireNonEmpty(file.getSize());
        if (file.getSize() > MAX_AVATAR_SIZE) {
            throw new BusinessException("头像文件不能超过 5MB");
        }
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null) {
            int dot = originalName.lastIndexOf('.');
            if (dot > 0) {
                ext = originalName.substring(dot + 1).toLowerCase();
            }
        }
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException("仅支持 JPG/PNG/GIF/WebP 格式");
        }
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String relativePath = StoragePaths.avatar(storedName);
        fileAssetService.storeBytes(MultipartPayloads.readBytes(file), relativePath);
        String avatarUrl = "/api/files/avatar/" + storedName;
        updateAvatar(userId, avatarUrl);
        return avatarUrl;
    }

    @Override
    public void updatePassword(Long userId, String oldPassword, String newPassword) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            log.warn("修改密码用户不存在: userId={}", userId);
            throw new BusinessException("用户不存在");
        }
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            log.warn("修改密码原密码错误: userId={}", userId);
            throw new BusinessException("原密码错误");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userMapper.updateById(user);
        log.info("用户修改密码: userId={}", userId);
    }
}
