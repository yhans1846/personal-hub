package com.personalhub.storage;

import org.springframework.core.io.Resource;

/**
 * 文件资产门面：统一委托 {@link StorageService}。
 */
public interface FileAssetService {

    /**
     * 写入字节到相对路径
     */
    void storeBytes(byte[] data, String relativePath);

    /**
     * 加载资源
     */
    Resource load(String relativePath);

    /**
     * 删除文件或目录
     */
    void delete(String relativePath);

    /**
     * 是否存在
     */
    boolean exists(String relativePath);
}
