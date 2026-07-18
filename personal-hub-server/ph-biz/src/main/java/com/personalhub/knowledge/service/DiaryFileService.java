package com.personalhub.knowledge.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 日记配图资源（独立目录，不进 file_resource）
 */
public interface DiaryFileService {

    /**
     * 上传配图，写入 diaries/{id}/images/，并追加到 image_files
     *
     * @return name=文件名
     */
    Map<String, String> uploadImage(Long diaryId, Long userId, MultipartFile file);

    /** 加载配图 */
    Resource loadImage(Long diaryId, Long userId, String filename);

    /** 删除配图文件并更新 image_files */
    void deleteImage(Long diaryId, Long userId, String filename);
}
