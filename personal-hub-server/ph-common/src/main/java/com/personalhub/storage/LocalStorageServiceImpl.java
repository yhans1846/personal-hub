package com.personalhub.storage;

import com.personalhub.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 本地文件系统存储实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LocalStorageServiceImpl implements StorageService {

    private final StorageProperties storageProperties;

    private Path resolve(String relativePath) {
        return storageProperties.getLocation().resolve(relativePath).normalize();
    }

    @Override
    public void store(MultipartFile file, String relativePath) {
        try {
            Path targetPath = resolve(relativePath);
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath.toFile());
            log.debug("文件存储成功: {}", relativePath);
        } catch (IOException e) {
            throw new BusinessException("文件存储失败: " + relativePath, e);
        }
    }

    @Override
    public void store(byte[] data, String relativePath) {
        try {
            Path targetPath = resolve(relativePath);
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, data);
            log.debug("文件字节存储成功: {}", relativePath);
        } catch (IOException e) {
            throw new BusinessException("文件字节存储失败: " + relativePath, e);
        }
    }

    @Override
    public Resource load(String relativePath) {
        Path targetPath = resolve(relativePath);
        if (!Files.exists(targetPath)) {
            throw new BusinessException("文件不存在: " + relativePath);
        }
        return new FileSystemResource(targetPath);
    }

    @Override
    public void delete(String relativePath) {
        try {
            Path targetPath = resolve(relativePath);
            if (Files.isDirectory(targetPath)) {
                try (var files = Files.walk(targetPath)) {
                    files.sorted(Comparator.reverseOrder())
                            .forEach(p -> {
                                try {
                                    Files.deleteIfExists(p);
                                } catch (IOException e) {
                                    log.debug("删除路径失败: {}", p, e);
                                }
                            });
                }
            } else {
                Files.deleteIfExists(targetPath);
            }
            log.debug("文件删除成功: {}", relativePath);
        } catch (IOException e) {
            log.warn("文件删除失败: {}", relativePath, e);
        }
    }

    @Override
    public boolean exists(String relativePath) {
        return Files.exists(resolve(relativePath));
    }

    @Override
    public void write(String relativePath, String content) {
        try {
            Path targetPath = resolve(relativePath);
            Files.createDirectories(targetPath.getParent());
            Files.writeString(targetPath, content, StandardCharsets.UTF_8);
            log.debug("文本写入成功: {}", relativePath);
        } catch (IOException e) {
            throw new BusinessException("文本写入失败: " + relativePath, e);
        }
    }

    @Override
    public String read(String relativePath) {
        try {
            Path targetPath = resolve(relativePath);
            return Files.readString(targetPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new BusinessException("文本读取失败: " + relativePath, e);
        }
    }

    @Override
    public void move(String sourcePath, String targetPath) {
        try {
            Path source = resolve(sourcePath);
            Path target = resolve(targetPath);
            Files.createDirectories(target.getParent());
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            log.debug("文件移动成功: {} -> {}", sourcePath, targetPath);
        } catch (IOException e) {
            throw new BusinessException("文件移动失败: " + sourcePath + " -> " + targetPath, e);
        }
    }

    @Override
    public void copy(String sourcePath, String targetPath) {
        try {
            Path source = resolve(sourcePath);
            Path target = resolve(targetPath);
            Files.createDirectories(target.getParent());
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            log.debug("文件复制成功: {} -> {}", sourcePath, targetPath);
        } catch (IOException e) {
            throw new BusinessException("文件复制失败: " + sourcePath + " -> " + targetPath, e);
        }
    }

    @Override
    public List<String> listFiles(String dirPath) {
        try {
            Path target = resolve(dirPath);
            if (!Files.exists(target) || !Files.isDirectory(target)) {
                return Collections.emptyList();
            }
            try (var files = Files.list(target)) {
                return files.filter(Files::isRegularFile)
                        .map(p -> p.getFileName().toString())
                        .toList();
            }
        } catch (IOException e) {
            log.warn("列出目录失败: {}", dirPath, e);
            return Collections.emptyList();
        }
    }
}
