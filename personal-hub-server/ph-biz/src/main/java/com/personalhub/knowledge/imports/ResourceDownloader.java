package com.personalhub.knowledge.imports;

import com.personalhub.knowledge.imports.ResourceResolver.ResolveResult;
import com.personalhub.storage.StorageService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 资源下载/复制器 — 将已解析的资源保存到笔记目录
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResourceDownloader {

    private final StorageService storageService;

    /**
     * 下载/复制单个资源到笔记目录
     *
     * @param resolveResult 已解析的资源
     * @param noteDir       笔记目录路径（如 notes/123）
     * @param type          资源类型（images / attachments）
     * @return 下载结果
     */
    public DownloadResult download(ResolveResult resolveResult, String noteDir, String type) {
        String ext = resolveResult.getExtension() != null ? resolveResult.getExtension() : "png";

        // UUID 文件名，避免重名
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String targetPath = noteDir + "/" + type + "/" + filename;

        try {
            if (resolveResult.getData() != null) {
                storageService.store(resolveResult.getData(), targetPath);
                log.debug("资源已保存: {} → {}", resolveResult.getOriginalRef(), targetPath);
                return DownloadResult.success(resolveResult.getOriginalRef(), type + "/" + filename);
            }
            return DownloadResult.fail(resolveResult.getOriginalRef(), "无数据可写入");
        } catch (Exception e) {
            log.warn("资源保存失败: {}", resolveResult.getOriginalRef(), e);
            return DownloadResult.fail(resolveResult.getOriginalRef(), "保存失败: " + e.getMessage());
        }
    }

    /** 下载结果 */
    @Data
    @Builder
    @AllArgsConstructor
    public static class DownloadResult {
        private String originalRef;
        private String targetPath;
        private boolean success;
        private String message;

        public static DownloadResult success(String ref, String targetPath) {
            return DownloadResult.builder()
                    .originalRef(ref)
                    .targetPath(targetPath)
                    .success(true)
                    .message("下载成功")
                    .build();
        }

        public static DownloadResult fail(String ref, String message) {
            return DownloadResult.builder()
                    .originalRef(ref)
                    .success(false)
                    .message(message)
                    .build();
        }
    }
}
