package com.personalhub.storage;

import com.personalhub.common.exception.BusinessException;

/**
 * 上传文件通用校验（与 Multipart 解耦，只看 size / contentType）。
 */
public final class FileUploadValidator {

    private FileUploadValidator() {
    }

    /**
     * @param size 字节数
     */
    public static void requireNonEmpty(long size) {
        if (size <= 0) {
            throw new BusinessException("上传文件不能为空");
        }
    }

    /**
     * @param size    字节数
     * @param maxSize 上限
     */
    public static void requireWithinMax(long size, long maxSize) {
        if (size > maxSize) {
            throw new BusinessException("文件大小超过限制（最大 50MB）");
        }
    }

    /**
     * @param contentType MIME，可为 null
     */
    public static void requireImageContentType(String contentType) {
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BusinessException("仅支持上传图片");
        }
    }
}
