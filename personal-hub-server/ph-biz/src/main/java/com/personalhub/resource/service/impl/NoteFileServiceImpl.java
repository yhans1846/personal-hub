package com.personalhub.resource.service.impl;

import com.personalhub.common.exception.NotFoundException;
import com.personalhub.common.util.EntityGuard;
import com.personalhub.knowledge.entity.Note;
import com.personalhub.knowledge.mapper.NoteMapper;
import com.personalhub.resource.service.NoteFileService;
import com.personalhub.storage.StorageProperties;
import com.personalhub.storage.StorageService;
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

    private final StorageService storageService;
    private final NoteMapper noteMapper;
    private final StorageProperties storageProperties;

    private String getNoteDir(Long noteId) {
        return "notes/" + noteId;
    }

    private void validateNote(Long noteId, Long userId) {
        EntityGuard.requireOwned(
                noteMapper.selectById(noteId), userId, Note::getUserId, "笔记不存在");
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (file.getSize() > storageProperties.getMaxSize()) {
            throw new IllegalArgumentException("文件大小超过限制（最大 50MB）");
        }
    }

    @Override
    public Map<String, String> uploadImage(Long noteId, Long userId, MultipartFile file) {
        validateNote(noteId, userId);
        validateFile(file);
        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        storageService.store(file, getNoteDir(noteId) + "/images/" + filename);
        log.info("笔记配图上传成功: noteId={}, filename={}", noteId, filename);
        return Map.of("url", "images/" + filename, "name", filename);
    }

    @Override
    public Map<String, String> uploadAttachment(Long noteId, Long userId, MultipartFile file) {
        validateNote(noteId, userId);
        validateFile(file);
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "未命名文件";
        }
        storageService.store(file, getNoteDir(noteId) + "/attachments/" + originalName);
        log.info("笔记附件上传成功: noteId={}, filename={}", noteId, originalName);
        return Map.of("url", "attachments/" + originalName, "name", originalName);
    }

    @Override
    public Resource load(Long noteId, Long userId, String resourceType, String filename) {
        validateNote(noteId, userId);
        return storageService.load(getNoteDir(noteId) + "/" + resourceType + "/" + filename);
    }

    private String getExtension(String filename) {
        if (filename == null) return "png";
        int dot = filename.lastIndexOf('.');
        return (dot > 0) ? filename.substring(dot + 1).toLowerCase() : "png";
    }
}
