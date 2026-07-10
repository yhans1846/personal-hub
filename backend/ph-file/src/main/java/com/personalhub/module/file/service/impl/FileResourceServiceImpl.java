package com.personalhub.module.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.module.file.dto.FileQueryDTO;
import com.personalhub.module.file.entity.FileCategory;
import com.personalhub.module.file.entity.FileResource;
import com.personalhub.module.file.mapper.FileCategoryMapper;
import com.personalhub.module.file.mapper.FileResourceMapper;
import com.personalhub.module.file.service.FileResourceService;
import com.personalhub.module.file.vo.FileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

/**
 * 文件资源服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileResourceServiceImpl implements FileResourceService {

    private final FileResourceMapper fileResourceMapper;
    private final FileCategoryMapper fileCategoryMapper;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.max-size:52428800}")
    private long maxSize;

    @Override
    public IPage<FileVO> list(Long userId, FileQueryDTO query) {
        LambdaQueryWrapper<FileResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileResource::getUserId, userId);

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(FileResource::getName, query.getKeyword());
        }
        if (StringUtils.hasText(query.getType())) {
            wrapper.eq(FileResource::getType, query.getType().toLowerCase());
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(FileResource::getCategoryId, query.getCategoryId());
        }

        wrapper.orderByDesc(FileResource::getCreatedAt);

        Page<FileResource> page = new Page<>(query.getPage(), query.getSize());
        IPage<FileResource> filePage = fileResourceMapper.selectPage(page, wrapper);

        return filePage.convert(file -> {
            FileVO vo = FileVO.from(file);
            if (file.getCategoryId() != null) {
                FileCategory cat = fileCategoryMapper.selectById(file.getCategoryId());
                if (cat != null) {
                    vo.setCategoryName(cat.getName());
                }
            }
            return vo;
        });
    }

    @Override
    public FileVO getById(Long id, Long userId) {
        FileResource file = fileResourceMapper.selectById(id);
        if (file == null || !file.getUserId().equals(userId)) {
            log.warn("文件不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("文件不存在");
        }
        FileVO vo = FileVO.from(file);
        if (file.getCategoryId() != null) {
            FileCategory cat = fileCategoryMapper.selectById(file.getCategoryId());
            if (cat != null) {
                vo.setCategoryName(cat.getName());
            }
        }
        return vo;
    }

    @Override
    @Transactional
    public FileVO upload(Long userId, MultipartFile multipartFile, Long categoryId) {
        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (multipartFile.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小超过限制（最大 50MB）");
        }

        String originalName = multipartFile.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "未命名文件";
        }

        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot > 0) {
            ext = originalName.substring(dot + 1).toLowerCase();
        }

        String storedName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
        String relativePath = userId + "/" + storedName;

        try {
            Path targetDir = Paths.get(uploadDir, String.valueOf(userId));
            Files.createDirectories(targetDir);
            Path targetPath = targetDir.resolve(storedName);
            multipartFile.transferTo(targetPath.toFile());

            FileResource file = new FileResource();
            file.setUserId(userId);
            file.setName(originalName);
            file.setStoredName(storedName);
            file.setPath(relativePath);
            file.setSize(multipartFile.getSize());
            file.setType(ext);
            file.setMimeType(multipartFile.getContentType());
            file.setCategoryId(categoryId);
            fileResourceMapper.insert(file);

            log.info("文件上传成功: id={}, userId={}, name={}, size={}",
                    file.getId(), userId, originalName, multipartFile.getSize());

            return FileVO.from(file);
        } catch (IOException e) {
            log.error("文件存储失败: name={}, userId={}", originalName, userId, e);
            throw new RuntimeException("文件存储失败", e);
        }
    }

    @Override
    public FileResource getFileResource(Long id, Long userId) {
        FileResource file = fileResourceMapper.selectById(id);
        if (file == null || !file.getUserId().equals(userId)) {
            log.warn("文件不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("文件不存在");
        }
        return file;
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        FileResource file = fileResourceMapper.selectById(id);
        if (file == null || !file.getUserId().equals(userId)) {
            log.warn("删除文件不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("文件不存在");
        }
        fileResourceMapper.deleteById(id);
        log.info("文件已删除: id={}, userId={}, name={}", id, userId, file.getName());
    }
}
