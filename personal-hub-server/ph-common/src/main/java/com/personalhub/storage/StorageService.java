package com.personalhub.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 统一存储接口，支持本地文件/MinIO/OSS 等实现
 */
public interface StorageService {

    /**
     * 上传 MultipartFile 到指定相对路径
     */
    void store(MultipartFile file, String relativePath);

    /**
     * 上传字节数据到指定相对路径
     */
    void store(byte[] data, String relativePath);

    /**
     * 加载文件为 Resource
     */
    Resource load(String relativePath);

    /**
     * 删除文件或目录
     */
    void delete(String relativePath);

    /**
     * 判断文件是否存在
     */
    boolean exists(String relativePath);

    /**
     * 写入文本内容到文件
     */
    void write(String relativePath, String content);

    /**
     * 读取文本文件内容
     */
    String read(String relativePath);

    /**
     * 移动/重命名文件
     */
    void move(String sourcePath, String targetPath);

    /**
     * 复制文件
     */
    void copy(String sourcePath, String targetPath);

    /**
     * 列出目录下的所有文件名
     */
    List<String> listFiles(String dirPath);
}
