package com.personalhub.storage;

import com.personalhub.common.exception.BusinessException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Multipart → byte[]（Web 适配，仅上传入口使用）。
 */
public final class MultipartPayloads {

    private MultipartPayloads() {
    }

    /**
     * @param file 上传文件
     * @return 字节内容
     */
    public static byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException e) {
            throw new BusinessException("读取上传文件失败", e);
        }
    }
}
