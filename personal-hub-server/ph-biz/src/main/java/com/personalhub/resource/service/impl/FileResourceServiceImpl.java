package com.personalhub.resource.service.impl;

import com.personalhub.common.exception.BusinessException;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.util.EntityGuard;
import com.personalhub.resource.dto.FileQueryDTO;
import com.personalhub.knowledge.service.CategoryService;
import com.personalhub.resource.entity.FileResource;
import com.personalhub.resource.mapper.FileResourceMapper;
import com.personalhub.resource.service.FileResourceService;
import com.personalhub.resource.vo.FileVO;
import com.personalhub.storage.FileAssetService;
import com.personalhub.storage.FileUploadValidator;
import com.personalhub.storage.MultipartPayloads;
import com.personalhub.storage.StoragePaths;
import com.personalhub.storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 文件资源服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileResourceServiceImpl implements FileResourceService {

    private final FileResourceMapper fileResourceMapper;
    private final CategoryService categoryService;
    private final FileAssetService fileAssetService;
    private final StorageProperties storageProperties;

    @Override
    public IPage<FileVO> list(Long userId, FileQueryDTO query) {
        LambdaQueryWrapper<FileResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileResource::getUserId, userId);

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(FileResource::getName, query.getKeyword());
        }
        if (StringUtils.hasText(query.getType())) {
            String type = query.getType().toLowerCase();
            switch (type) {
                case "image" -> wrapper.in(FileResource::getType,
                        java.util.List.of("jpg", "jpeg", "png", "gif", "svg", "webp", "bmp"));
                case "pdf" -> wrapper.eq(FileResource::getType, "pdf");
                case "doc" -> wrapper.in(FileResource::getType,
                        java.util.List.of("doc", "docx", "xls", "xlsx", "ppt", "pptx", "md", "txt"));
                case "archive" -> wrapper.in(FileResource::getType,
                        java.util.List.of("zip", "rar", "7z", "tar", "gz"));
                default -> wrapper.eq(FileResource::getType, type);
            }
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(FileResource::getCategoryId, query.getCategoryId());
        }
        wrapper.orderByDesc(FileResource::getCreatedAt);

        Page<FileResource> page = new Page<>(query.getPage(), query.getSize());
        IPage<FileResource> filePage = fileResourceMapper.selectPage(page, wrapper);

        Map<Long, String> categoryNameMap = categoryService.listByType(userId, "file").stream()
                .collect(Collectors.toMap(
                        com.personalhub.knowledge.vo.CategoryVO::getId,
                        com.personalhub.knowledge.vo.CategoryVO::getName,
                        (a, b) -> a));

        return filePage.convert(file -> {
            FileVO vo = FileVO.from(file);
            if (file.getCategoryId() != null) {
                vo.setCategoryName(categoryNameMap.get(file.getCategoryId()));
            }
            return vo;
        });
    }

    @Override
    public FileVO getById(Long id, Long userId) {
        FileResource file = EntityGuard.requireOwned(
                fileResourceMapper.selectById(id), userId, FileResource::getUserId, "文件不存在");
        FileVO vo = FileVO.from(file);
        if (file.getCategoryId() != null) {
            Map<Long, String> categoryNameMap = categoryService.listByType(userId, "file").stream()
                    .collect(Collectors.toMap(
                            com.personalhub.knowledge.vo.CategoryVO::getId,
                            com.personalhub.knowledge.vo.CategoryVO::getName,
                            (a, b) -> a));
            vo.setCategoryName(categoryNameMap.get(file.getCategoryId()));
        }
        return vo;
    }

    @Override
    @Transactional
    public FileVO upload(Long userId, MultipartFile multipartFile, Long categoryId) {
        FileUploadValidator.requireNonEmpty(multipartFile.getSize());
        FileUploadValidator.requireWithinMax(multipartFile.getSize(), storageProperties.getMaxSize());

        String originalName = multipartFile.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "未命名文件";
        }

        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot > 0) {
            ext = originalName.substring(dot + 1).toLowerCase();
        }

        String dirPrefix = getDirectoryPrefix(ext);
        LocalDate now = LocalDate.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyy/MM"));
        String storedName = UUID.randomUUID().toString().replace("-", "")
                + (ext.isEmpty() ? "" : "." + ext);
        String relativePath = StoragePaths.upload(dirPrefix, yearMonth, storedName);

        fileAssetService.storeBytes(MultipartPayloads.readBytes(multipartFile), relativePath);

        // 保存DB记录
        var file = FileResource.builder()
                .userId(userId)
                .name(originalName)
                .path(relativePath)
                .size(multipartFile.getSize())
                .type(ext)
                .mimeType(multipartFile.getContentType())
                .categoryId(categoryId)
                .build();
        fileResourceMapper.insert(file);

        log.info("文件上传成功: id={}, userId={}, name={}", file.getId(), userId, originalName);
        return FileVO.from(file);
    }

    @Override
    public FileResource getFileResource(Long id, Long userId) {
        FileResource file = EntityGuard.requireOwned(
                fileResourceMapper.selectById(id), userId, FileResource::getUserId, "文件不存在");
        return file;
    }

    @Override
    @Transactional
    public FileVO updateCategory(Long id, Long userId, Long categoryId) {
        FileResource file = EntityGuard.requireOwned(
                fileResourceMapper.selectById(id), userId, FileResource::getUserId, "文件不存在");
        if (categoryId != null) {
            boolean exists = categoryService.listByType(userId, "file").stream()
                    .anyMatch(c -> categoryId.equals(c.getId()));
            if (!exists) {
                throw new BusinessException("分类不存在");
            }
        }
        fileResourceMapper.update(null, new LambdaUpdateWrapper<FileResource>()
                .eq(FileResource::getId, id)
                .eq(FileResource::getUserId, userId)
                .set(FileResource::getCategoryId, categoryId));
        log.info("更新文件分类: id={}, categoryId={}", id, categoryId);
        return getById(id, userId);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        FileResource file = EntityGuard.requireOwned(
                fileResourceMapper.selectById(id), userId, FileResource::getUserId, "文件不存在");
        // 删除物理文件
        if (file.getPath() != null) {
            fileAssetService.delete(file.getPath());
        }
        // 逻辑删除DB记录
        fileResourceMapper.deleteById(id);
        log.info("文件已删除: id={}, name={}", id, file.getName());
    }

    /**
     * 根据扩展名确定上传目录前缀
     */
    private String getDirectoryPrefix(String ext) {
        return switch (ext) {
            case "jpg", "jpeg", "png", "gif", "svg", "webp", "bmp" -> "image";
            case "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt" -> "document";
            case "md" -> "markdown";
            default -> "other";
        };
    }
}
