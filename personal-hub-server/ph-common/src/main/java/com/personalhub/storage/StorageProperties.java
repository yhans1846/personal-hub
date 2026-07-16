package com.personalhub.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件存储配置
 */
@Data
@ConfigurationProperties(prefix = "personal-hub.storage")
public class StorageProperties {

    /** 文件存储根目录，默认 ./data/uploads */
    private Path location = Paths.get("data", "uploads");

    /** 单文件最大字节，默认 50MB */
    private long maxSize = 50 * 1024 * 1024;
}
