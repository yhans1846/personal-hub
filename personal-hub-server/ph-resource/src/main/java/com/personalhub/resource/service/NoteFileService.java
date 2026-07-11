package com.personalhub.resource.service;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/**
 * 笔记资源服务（图片/附件）
 */
public interface NoteFileService {

    /** 上传笔记配图，返回 {url, name} */
    Map<String, String> uploadImage(Long noteId, Long userId, MultipartFile file);

    /** 上传笔记附件，返回 {url, name} */
    Map<String, String> uploadAttachment(Long noteId, Long userId, MultipartFile file);

    /** 获取笔记资源文件 */
    Resource load(Long noteId, Long userId, String resourceType, String filename);
}
