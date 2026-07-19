package com.personalhub.knowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.personalhub.common.exception.BusinessException;
import com.personalhub.common.util.EntityGuard;
import com.personalhub.common.util.FilenameGuard;
import com.personalhub.knowledge.entity.DiaryEntry;
import com.personalhub.knowledge.mapper.DiaryEntryMapper;
import com.personalhub.knowledge.service.DiaryFileService;
import com.personalhub.storage.FileAssetService;
import com.personalhub.storage.FileUploadValidator;
import com.personalhub.storage.MultipartPayloads;
import com.personalhub.storage.StoragePaths;
import com.personalhub.storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 日记配图：diaries/{id}/images/{uuid}.ext
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryFileServiceImpl implements DiaryFileService {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final FileAssetService fileAssetService;
    private final DiaryEntryMapper diaryEntryMapper;
    private final StorageProperties storageProperties;

    private DiaryEntry requireDiary(Long diaryId, Long userId) {
        return EntityGuard.requireOwned(
                diaryEntryMapper.selectById(diaryId), userId, DiaryEntry::getUserId, "日记不存在");
    }

    private void validateFile(MultipartFile file) {
        if (file == null) {
            throw new BusinessException("上传文件不能为空");
        }
        FileUploadValidator.requireNonEmpty(file.getSize());
        FileUploadValidator.requireWithinMax(file.getSize(), storageProperties.getMaxSize());
        FileUploadValidator.requireImageContentType(file.getContentType());
    }

    private String extension(String original) {
        if (original == null) {
            return "png";
        }
        int dot = original.lastIndexOf('.');
        return (dot > 0) ? original.substring(dot + 1).toLowerCase() : "png";
    }

    private void persistImageFiles(Long diaryId, List<String> names) {
        String json = null;
        if (names != null && !names.isEmpty()) {
            try {
                json = MAPPER.writeValueAsString(names);
            } catch (Exception e) {
                throw new BusinessException("保存配图列表失败");
            }
        }
        diaryEntryMapper.update(null, new LambdaUpdateWrapper<DiaryEntry>()
                .eq(DiaryEntry::getId, diaryId)
                .set(DiaryEntry::getImageFiles, json));
    }

    @Override
    @Transactional
    public Map<String, String> uploadImage(Long diaryId, Long userId, MultipartFile file) {
        DiaryEntry entry = requireDiary(diaryId, userId);
        validateFile(file);
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension(file.getOriginalFilename());
        fileAssetService.storeBytes(
                MultipartPayloads.readBytes(file), StoragePaths.diaryImage(diaryId, filename));
        List<String> names = new ArrayList<>(entry.parseImageFiles());
        names.add(filename);
        persistImageFiles(diaryId, names);
        log.info("日记配图上传成功: diaryId={}, filename={}", diaryId, filename);
        return Map.of("name", filename);
    }

    @Override
    public Resource loadImage(Long diaryId, Long userId, String filename) {
        requireDiary(diaryId, userId);
        FilenameGuard.requireSafe(filename);
        return fileAssetService.load(StoragePaths.diaryImage(diaryId, filename));
    }

    @Override
    @Transactional
    public void deleteImage(Long diaryId, Long userId, String filename) {
        DiaryEntry entry = requireDiary(diaryId, userId);
        FilenameGuard.requireSafe(filename);
        List<String> names = new ArrayList<>(entry.parseImageFiles());
        if (!names.remove(filename)) {
            throw new BusinessException("配图不存在");
        }
        try {
            fileAssetService.delete(StoragePaths.diaryImage(diaryId, filename));
        } catch (Exception e) {
            log.warn("删除日记配图文件失败: diaryId={}, filename={}", diaryId, filename, e);
        }
        persistImageFiles(diaryId, names);
        log.info("日记配图已删除: diaryId={}, filename={}", diaryId, filename);
    }
}
