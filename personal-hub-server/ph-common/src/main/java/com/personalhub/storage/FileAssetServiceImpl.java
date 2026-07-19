package com.personalhub.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

/**
 * 文件资产门面默认实现
 */
@Service
@RequiredArgsConstructor
public class FileAssetServiceImpl implements FileAssetService {

    private final StorageService storageService;

    @Override
    public void storeBytes(byte[] data, String relativePath) {
        storageService.store(data, relativePath);
    }

    @Override
    public Resource load(String relativePath) {
        return storageService.load(relativePath);
    }

    @Override
    public void delete(String relativePath) {
        storageService.delete(relativePath);
    }

    @Override
    public boolean exists(String relativePath) {
        return storageService.exists(relativePath);
    }
}
