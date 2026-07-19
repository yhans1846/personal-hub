package com.personalhub.resource.service.impl;

import com.personalhub.common.exception.BusinessException;
import com.personalhub.common.util.EntityGuard;
import com.personalhub.common.util.FilenameGuard;
import com.personalhub.knowledge.entity.Note;
import com.personalhub.knowledge.mapper.NoteMapper;
import com.personalhub.resource.service.NoteFileService;
import com.personalhub.storage.FileAssetService;
import com.personalhub.storage.FileUploadValidator;
import com.personalhub.storage.MultipartPayloads;
import com.personalhub.storage.StoragePaths;
import com.personalhub.storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

/**
 * 笔记资源服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NoteFileServiceImpl implements NoteFileService {

    private final FileAssetService fileAssetService;
    private final NoteMapper noteMapper;
    private final StorageProperties storageProperties;

    private void validateNote(Long noteId, Long userId) {
        EntityGuard.requireOwned(
                noteMapper.selectById(noteId), userId, Note::getUserId, "笔记不存在");
    }

    private void validateFile(MultipartFile file) {
        FileUploadValidator.requireNonEmpty(file.getSize());
        FileUploadValidator.requireWithinMax(file.getSize(), storageProperties.getMaxSize());
    }

    @Override
    public Map<String, String> uploadImage(Long noteId, Long userId, MultipartFile file) {
        validateNote(noteId, userId);
        validateFile(file);
        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        fileAssetService.storeBytes(MultipartPayloads.readBytes(file), StoragePaths.noteImage(noteId, filename));
        log.info("笔记配图上传成功: noteId={}, filename={}", noteId, filename);
        return Map.of("url", "images/" + filename, "name", filename);
    }

    @Override
    public Map<String, String> uploadAttachment(Long noteId, Long userId, MultipartFile file) {
        validateNote(noteId, userId);
        validateFile(file);
        String originalName = sanitizeOriginalFilename(file.getOriginalFilename());
        fileAssetService.storeBytes(
                MultipartPayloads.readBytes(file), StoragePaths.noteAttachment(noteId, originalName));
        log.info("笔记附件上传成功: noteId={}, filename={}", noteId, originalName);
        return Map.of("url", "attachments/" + originalName, "name", originalName);
    }

    @Override
    public Resource load(Long noteId, Long userId, String resourceType, String filename) {
        validateNote(noteId, userId);
        if (!"images".equals(resourceType) && !"attachments".equals(resourceType)) {
            throw new BusinessException("非法资源类型");
        }
        FilenameGuard.requireSafe(filename);
        String path = "images".equals(resourceType)
                ? StoragePaths.noteImage(noteId, filename)
                : StoragePaths.noteAttachment(noteId, filename);
        return fileAssetService.load(path);
    }

    private String sanitizeOriginalFilename(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "未命名文件";
        }
        int slash = Math.max(originalName.lastIndexOf('/'), originalName.lastIndexOf('\\'));
        if (slash >= 0) {
            originalName = originalName.substring(slash + 1);
        }
        return FilenameGuard.requireSafe(originalName);
    }

    private String getExtension(String filename) {
        if (filename == null) {
            return "png";
        }
        int dot = filename.lastIndexOf('.');
        return (dot > 0) ? filename.substring(dot + 1).toLowerCase() : "png";
    }
}
