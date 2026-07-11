# 文件存储重构与 Markdown 资源管理 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 subagent-driven-development（推荐）或 executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将文件存储重构为独立 `ph-storage` 模块，实现笔记即资源包（Note Bundle）的目录结构，支持 Markdown 导入/导出/资源本地化。

**架构：** 新增 `ph-storage` 模块，包含 StorageService 接口 + LocalStorageServiceImpl + StorageProperties。`ph-resource` 和 `ph-knowledge` 依赖 `ph-storage`。通用文件走 uploads/{type}/ 目录 + file_resource 表，笔记资源走 notes/{noteId}/ 目录（不写 file_resource）。笔记正文同时存在文件系统和 DB content 字段。

**技术栈：** Spring Boot 3, MyBatis-Plus, MySQL, Spring Scheduled

---

# 文件清单

## 创建

| 文件 | 职责 |
|------|------|
| `ph-storage/pom.xml` | ph-storage 模块 POM |
| `ph-storage/src/main/java/com/personalhub/storage/StorageService.java` | 统一存储接口 |
| `ph-storage/src/main/java/com/personalhub/storage/StorageProperties.java` | 存储配置类 |
| `ph-storage/src/main/java/com/personalhub/storage/LocalStorageServiceImpl.java` | 本地文件存储实现 |
| `ph-resource/src/main/java/com/personalhub/resource/controller/NoteFileController.java` | 笔记资源（图片/附件）上传下载 |
| `ph-resource/src/main/java/com/personalhub/resource/service/NoteFileService.java` | 笔记资源服务接口 |
| `ph-resource/src/main/java/com/personalhub/resource/service/impl/NoteFileServiceImpl.java` | 笔记资源服务实现 |
| `ph-knowledge/src/main/java/com/personalhub/knowledge/service/MarkdownImportService.java` | Markdown 导入服务（资源本地化） |
| `ph-knowledge/src/main/java/com/personalhub/knowledge/service/NoteExportService.java` | 笔记导出服务 |
| `ph-knowledge/src/main/java/com/personalhub/knowledge/service/NoteResourceCleanupTask.java` | 定时清理孤立笔记资源 |

## 修改

| 文件 | 修改内容 |
|------|---------|
| `personal-hub-server/pom.xml` | 新增 ph-storage 模块声明 |
| `ph-resource/pom.xml` | 添加 ph-storage 依赖 |
| `ph-knowledge/pom.xml` | 添加 ph-storage 依赖 |
| `ph-boot/pom.xml` | 添加 ph-storage 依赖 |
| `ph-resource/src/main/java/com/personalhub/resource/entity/FileResource.java` | 新增 source、refId 字段 |
| `ph-resource/src/main/java/com/personalhub/resource/service/impl/FileResourceServiceImpl.java` | 改用 StorageService，新目录结构，物理删除 |
| `ph-resource/src/main/java/com/personalhub/resource/controller/FileController.java` | 新增预览端点，下载改用 StorageService |
| `ph-knowledge/src/main/java/com/personalhub/knowledge/entity/Note.java` | 新增 mdPath 字段 |
| `ph-knowledge/src/main/java/com/personalhub/knowledge/service/impl/NoteServiceImpl.java` | 新增 note.md 文件读写，永久删除时清理目录 |
| `ph-knowledge/src/main/java/com/personalhub/knowledge/controller/NoteController.java` | 新增导入/导出端点 |
| `ph-boot/src/main/resources/application.yml` | 添加 personal-hub.storage 配置 |

## SQL 变更（sql/migration.sql）

```sql
ALTER TABLE file_resource
    ADD COLUMN source VARCHAR(20) DEFAULT NULL COMMENT '上传来源: upload/diary/avatar',
    ADD COLUMN ref_id BIGINT DEFAULT NULL COMMENT '关联实体ID(diary_id等)';

ALTER TABLE note_note
    ADD COLUMN md_path VARCHAR(500) DEFAULT NULL COMMENT '笔记MD文件路径(notes/{id}/note.md)';
```

---

# 实现任务

## 任务 1：创建 ph-storage 模块

**文件：**
- 创建：`personal-hub-server/ph-storage/pom.xml`
- 创建：`personal-hub-server/ph-storage/src/main/java/com/personalhub/storage/package-info.java`（可选）
- 修改：`personal-hub-server/pom.xml`（父 POM 添加模块）

- [ ] **步骤 1：创建 ph-storage/pom.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>com.personalhub</groupId>
        <artifactId>personal-hub</artifactId>
        <version>0.1.0</version>
        <relativePath>../pom.xml</relativePath>
    </parent>

    <artifactId>ph-storage</artifactId>
    <name>ph-storage</name>
    <description>统一存储抽象层（本地文件/MinIO/OSS）</description>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-configuration-processor</artifactId>
            <optional>true</optional>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
    </dependencies>
</project>
```

- [ ] **步骤 2：在父 POM 注册模块**

编辑 `personal-hub-server/pom.xml`，在 `<modules>` 区域添加 `ph-storage`（按字母顺序或放在最后）：

```xml
<modules>
    <module>ph-boot</module>
    <module>ph-common</module>
    <module>ph-dashboard</module>
    <module>ph-knowledge</module>
    <module>ph-planning</module>
    <module>ph-resource</module>
    <module>ph-storage</module>   <!-- 新增 -->
    <module>ph-system</module>
</modules>
```

- [ ] **步骤 3：在父 POM 的 dependencyManagement 中添加 ph-storage**

检查 `personal-hub-server/pom.xml`，在 `<dependencyManagement>` 的 `<dependencies>` 中添加：

```xml
<dependency>
    <groupId>com.personalhub</groupId>
    <artifactId>ph-storage</artifactId>
    <version>${project.version}</version>
</dependency>
```

- [ ] **步骤 4：验证模块创建成功**

```bash
cd personal-hub-server && mvn compile -pl ph-storage -am -q
```

- [ ] **步骤 5：Commit**

```bash
git add -A
git commit -m "feat: 新增 ph-storage 模块（统一存储抽象层）"
```

---

## 任务 2：StorageService 接口 + StorageProperties + LocalStorageServiceImpl

**文件：**
- 创建：`personal-hub-server/ph-storage/src/main/java/com/personalhub/storage/StorageProperties.java`
- 创建：`personal-hub-server/ph-storage/src/main/java/com/personalhub/storage/StorageService.java`
- 创建：`personal-hub-server/ph-storage/src/main/java/com/personalhub/storage/LocalStorageServiceImpl.java`

- [ ] **步骤 1：创建 StorageProperties**

```java
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
```

- [ ] **步骤 2：创建 StorageService 接口**

```java
package com.personalhub.storage;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 统一存储接口，支持本地文件/MinIO/OSS 等实现
 */
public interface StorageService {

    /** 上传 MultipartFile 到指定相对路径 */
    String store(MultipartFile file, String relativePath);

    /** 上传字节数据到指定相对路径 */
    String store(byte[] data, String relativePath);

    /** 加载文件为 Resource */
    Resource load(String relativePath);

    /** 删除文件或目录 */
    void delete(String relativePath);

    /** 判断文件是否存在 */
    boolean exists(String relativePath);

    /** 写入文本内容到文件 */
    void write(String relativePath, String content);

    /** 读取文本文件内容 */
    String read(String relativePath);

    /** 移动/重命名文件 */
    void move(String sourcePath, String targetPath);

    /** 复制文件 */
    void copy(String sourcePath, String targetPath);

    /** 列出目录下的所有文件名 */
    List<String> listFiles(String dirPath);
}
```

- [ ] **步骤 3：创建 LocalStorageServiceImpl**

```java
package com.personalhub.storage;

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
    public String store(MultipartFile file, String relativePath) {
        try {
            Path targetPath = resolve(relativePath);
            Files.createDirectories(targetPath.getParent());
            file.transferTo(targetPath.toFile());
            log.debug("文件存储成功: {}", relativePath);
            return relativePath;
        } catch (IOException e) {
            throw new RuntimeException("文件存储失败: " + relativePath, e);
        }
    }

    @Override
    public String store(byte[] data, String relativePath) {
        try {
            Path targetPath = resolve(relativePath);
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, data);
            log.debug("文件字节存储成功: {}", relativePath);
            return relativePath;
        } catch (IOException e) {
            throw new RuntimeException("文件字节存储失败: " + relativePath, e);
        }
    }

    @Override
    public Resource load(String relativePath) {
        Path targetPath = resolve(relativePath);
        if (!Files.exists(targetPath)) {
            throw new RuntimeException("文件不存在: " + relativePath);
        }
        return new FileSystemResource(targetPath);
    }

    @Override
    public void delete(String relativePath) {
        try {
            Path targetPath = resolve(relativePath);
            if (Files.isDirectory(targetPath)) {
                try (var files = Files.walk(targetPath)) {
                    files.sorted(java.util.Comparator.reverseOrder())
                            .forEach(p -> {
                                try { Files.deleteIfExists(p); } catch (IOException ignored) {}
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
            throw new RuntimeException("文本写入失败: " + relativePath, e);
        }
    }

    @Override
    public String read(String relativePath) {
        try {
            Path targetPath = resolve(relativePath);
            return Files.readString(targetPath, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("文本读取失败: " + relativePath, e);
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
            throw new RuntimeException("文件移动失败: " + sourcePath + " -> " + targetPath, e);
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
            throw new RuntimeException("文件复制失败: " + sourcePath + " -> " + targetPath, e);
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
```

- [ ] **步骤 4：启用 @EnableConfigurationProperties**

需要在 `ph-boot` 或相关配置类中启用 `StorageProperties`。在 `ph-boot` 的启动类或一个 `@Configuration` 类上添加：

```java
@EnableConfigurationProperties(StorageProperties.class)
```

可以放在现有的 `MyBatisPlusConfig.java`（或任意 `@Configuration` 类）上。但 `MyBatisPlusConfig` 在 `ph-common` 中不依赖 `ph-storage`，所以推荐在 `ph-boot` 的启动类上添加：

```java
// PersonalHubApplication.java
package com.personalhub;

import com.personalhub.storage.StorageProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
@EnableScheduling
public class PersonalHubApplication {
    public static void main(String[] args) {
        SpringApplication.run(PersonalHubApplication.class, args);
    }
}
```

- [ ] **步骤 5：验证编译**

```bash
cd personal-hub-server && mvn compile -pl ph-storage -am -q
```

- [ ] **步骤 6：Commit**

```bash
git add -A
git commit -m "feat: 实现 StorageService 接口和本地文件存储"
```

---

## 任务 3：各模块添加 ph-storage 依赖

**文件：**
- 修改：`personal-hub-server/ph-resource/pom.xml`
- 修改：`personal-hub-server/ph-knowledge/pom.xml`
- 修改：`personal-hub-server/ph-boot/pom.xml`

- [ ] **步骤 1：ph-resource/pom.xml 添加依赖**

```xml
<dependency>
    <groupId>com.personalhub</groupId>
    <artifactId>ph-storage</artifactId>
</dependency>
```

- [ ] **步骤 2：ph-knowledge/pom.xml 添加依赖**

```xml
<dependency>
    <groupId>com.personalhub</groupId>
    <artifactId>ph-storage</artifactId>
</dependency>
```

- [ ] **步骤 3：ph-boot/pom.xml 添加依赖**

```xml
<dependency>
    <groupId>com.personalhub</groupId>
    <artifactId>ph-storage</artifactId>
</dependency>
```

- [ ] **步骤 4：验证编译**

```bash
cd personal-hub-server && mvn compile -pl ph-boot -am -q
```

- [ ] **步骤 5：Commit**

```bash
git add -A
git commit -m "chore: ph-resource/ph-knowledge/ph-boot 添加 ph-storage 依赖"
```

---

## 任务 4：FileResource 实体扩展 + 应用配置更新

**文件：**
- 修改：`personal-hub-server/ph-resource/src/main/java/com/personalhub/resource/entity/FileResource.java`
- 修改：`personal-hub-server/ph-boot/src/main/resources/application.yml`
- 修改：`personal-hub-server/ph-boot/src/main/resources/application-dev.yml`

- [ ] **步骤 1：FileResource 新增 source/refId 字段**

```java
// 在 categoryId 后面，isDeleted 前面添加：

    /** 文件分类ID */
    private Long categoryId;

    /** 上传来源: upload/diary/avatar */
    private String source;

    /** 关联实体ID(diary_id等) */
    private Long refId;

    /** 逻辑删除 0-正常 1-删除 */
    @TableLogic
    private Integer isDeleted;
```

- [ ] **步骤 2：更新 application.yml**

```yaml
# 在文件末尾添加
personal-hub:
  storage:
    location: D:/PersonalHub/uploads
    max-size: 52428800
```

- [ ] **步骤 3：运行 migration.sql**

```bash
mysql -u al_user -p123456 personal_hub < sql/migration.sql
```

- [ ] **步骤 4：Commit**

```bash
git add -A
git commit -m "feat: 扩展 FileResource 字段，更新存储配置"
```

---

## 任务 5：重构 FileResourceServiceImpl

**文件：**
- 修改：`personal-hub-server/ph-resource/src/main/java/com/personalhub/resource/service/impl/FileResourceServiceImpl.java`

重构要点：
1. 注入 `StorageService` 代替直接文件操作
2. 上传路径改为 `uploads/{type}/2026/07/uuid.ext`
3. 上传时填入 source="upload"
4. 删除时同时删除物理文件

- [ ] **步骤 1：重构 FileResourceServiceImpl**

```java
package com.personalhub.resource.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.resource.dto.FileQueryDTO;
import com.personalhub.resource.entity.FileCategory;
import com.personalhub.resource.entity.FileResource;
import com.personalhub.resource.mapper.FileCategoryMapper;
import com.personalhub.resource.mapper.FileResourceMapper;
import com.personalhub.resource.service.FileResourceService;
import com.personalhub.resource.vo.FileVO;
import com.personalhub.storage.StorageService;
import com.personalhub.storage.StorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件资源服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileResourceServiceImpl implements FileResourceService {

    private final FileResourceMapper fileResourceMapper;
    private final FileCategoryMapper fileCategoryMapper;
    private final StorageService storageService;
    private final StorageProperties storageProperties;

    @Override
    public IPage<FileVO> list(Long userId, FileQueryDTO query) {
        LambdaQueryWrapper<FileResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileResource::getUserId, userId);

        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(FileResource::getName, query.getKeyword());
        }
        if (StringUtils.hasText(query.getType())) {
            wrapper.eq(FileResource::getType, query.getType().toLowerCase());
        }
        if (query.getCategoryId() != null) {
            wrapper.eq(FileResource::getCategoryId, query.getCategoryId());
        }
        wrapper.orderByDesc(FileResource::getCreatedAt);

        Page<FileResource> page = new Page<>(query.getPage(), query.getSize());
        IPage<FileResource> filePage = fileResourceMapper.selectPage(page, wrapper);

        return filePage.convert(file -> {
            FileVO vo = FileVO.from(file);
            if (file.getCategoryId() != null) {
                FileCategory cat = fileCategoryMapper.selectById(file.getCategoryId());
                if (cat != null) vo.setCategoryName(cat.getName());
            }
            return vo;
        });
    }

    @Override
    public FileVO getById(Long id, Long userId) {
        FileResource file = fileResourceMapper.selectById(id);
        if (file == null || !file.getUserId().equals(userId)) {
            throw new NotFoundException("文件不存在");
        }
        FileVO vo = FileVO.from(file);
        if (file.getCategoryId() != null) {
            FileCategory cat = fileCategoryMapper.selectById(file.getCategoryId());
            if (cat != null) vo.setCategoryName(cat.getName());
        }
        return vo;
    }

    @Override
    @Transactional
    public FileVO upload(Long userId, MultipartFile multipartFile, Long categoryId) {
        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        if (multipartFile.getSize() > storageProperties.getMaxSize()) {
            throw new IllegalArgumentException("文件大小超过限制（最大 50MB）");
        }

        String originalName = multipartFile.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "未命名文件";
        }

        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot > 0) {
            ext = originalName.substring(dot + 1).toLowerCase();
        }

        String dirPrefix = getDirectoryPrefix(ext);
        LocalDate now = LocalDate.now();
        String yearMonth = now.format(DateTimeFormatter.ofPattern("yyyy/MM"));

        String storedName = UUID.randomUUID().toString().replace("-", "")
                + (ext.isEmpty() ? "" : "." + ext);
        String relativePath = "uploads/" + dirPrefix + "/" + yearMonth + "/" + storedName;

        storageService.store(multipartFile, relativePath);

        FileResource file = new FileResource();
        file.setUserId(userId);
        file.setName(originalName);
        file.setStoredName(storedName);
        file.setPath(relativePath);
        file.setSize(multipartFile.getSize());
        file.setType(ext);
        file.setMimeType(multipartFile.getContentType());
        file.setCategoryId(categoryId);
        file.setSource("upload");
        fileResourceMapper.insert(file);

        log.info("文件上传成功: id={}, userId={}, name={}", file.getId(), userId, originalName);
        return FileVO.from(file);
    }

    @Override
    public FileResource getFileResource(Long id, Long userId) {
        FileResource file = fileResourceMapper.selectById(id);
        if (file == null || !file.getUserId().equals(userId)) {
            throw new NotFoundException("文件不存在");
        }
        return file;
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        FileResource file = fileResourceMapper.selectById(id);
        if (file == null || !file.getUserId().equals(userId)) {
            throw new NotFoundException("文件不存在");
        }
        if (file.getPath() != null) {
            storageService.delete(file.getPath());
        }
        fileResourceMapper.deleteById(id);
        log.info("文件已删除: id={}, name={}", id, file.getName());
    }

    private String getDirectoryPrefix(String ext) {
        return switch (ext) {
            case "jpg", "jpeg", "png", "gif", "svg", "webp", "bmp" -> "image";
            case "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt" -> "document";
            case "md" -> "markdown";
            default -> "other";
        };
    }
}
```

- [ ] **步骤 2：验证编译**

```bash
cd personal-hub-server && mvn compile -pl ph-resource -am -q
```

- [ ] **步骤 3：Commit**

```bash
git add -A
git commit -m "refactor: FileResourceServiceImpl 改用 StorageService"
```

---

## 任务 6：FileController 新增预览端点

**文件：**
- 修改：`personal-hub-server/ph-resource/src/main/java/com/personalhub/resource/controller/FileController.java`

- [ ] **步骤 1：注入 StorageService，新增 preview 端点，更新 download**

```java
package com.personalhub.resource.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.common.result.PageResult;
import com.personalhub.common.result.Result;
import com.personalhub.resource.dto.FileQueryDTO;
import com.personalhub.resource.service.FileResourceService;
import com.personalhub.resource.vo.FileVO;
import com.personalhub.storage.StorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Tag(name = "文件管理", description = "文件的上传、下载、列表、删除、预览")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileResourceService fileResourceService;
    private final StorageService storageService;

    @Operation(summary = "文件列表")
    @GetMapping
    public Result<PageResult<FileVO>> list(
            @Parameter(hidden = true) Authentication authentication,
            FileQueryDTO query) {
        Long userId = Long.valueOf(authentication.getName());
        IPage<FileVO> page = fileResourceService.list(userId, query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "文件详情")
    @GetMapping("/{id}")
    public Result<FileVO> getById(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(fileResourceService.getById(id, userId));
    }

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<FileVO> upload(
            @Parameter(hidden = true) Authentication authentication,
            @NotNull @RequestParam("file") MultipartFile file,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(fileResourceService.upload(userId, file, categoryId));
    }

    @Operation(summary = "下载文件")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        var fileEntity = fileResourceService.getFileResource(id, userId);
        Resource resource = storageService.load(fileEntity.getPath());
        String encodedName = URLEncoder.encode(fileEntity.getName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName)
                .body(resource);
    }

    @Operation(summary = "预览文件", description = "inline 预览（图片/PDF 等浏览器支持的类型）")
    @GetMapping("/{id}/preview")
    public ResponseEntity<Resource> preview(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        var fileEntity = fileResourceService.getFileResource(id, userId);
        Resource resource = storageService.load(fileEntity.getPath());

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (fileEntity.getMimeType() != null) {
            try {
                mediaType = MediaType.parseMediaType(fileEntity.getMimeType());
            } catch (Exception ignored) {}
        }

        String encodedName = URLEncoder.encode(fileEntity.getName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename*=UTF-8''" + encodedName)
                .body(resource);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        fileResourceService.delete(id, userId);
        return Result.success();
    }
}
```

- [ ] **步骤 2：验证编译**

```bash
cd personal-hub-server && mvn compile -pl ph-resource -am -q
```

- [ ] **步骤 3：Commit**

```bash
git add -A
git commit -m "feat: 新增文件预览端点，下载使用 StorageService"
```

---

## 任务 7：Note 实体扩展 + NoteServiceImpl 文件读写

**文件：**
- 修改：`personal-hub-server/ph-knowledge/src/main/java/com/personalhub/knowledge/entity/Note.java`
- 修改：`personal-hub-server/ph-knowledge/src/main/java/com/personalhub/knowledge/service/impl/NoteServiceImpl.java`

- [ ] **步骤 1：Note 实体新增 mdPath 字段**

```java
    /** Markdown内容 */
    private String content;

    /** 笔记MD文件路径(notes/{id}/note.md) */
    private String mdPath;

    /** 是否收藏 0-否 1-是 */
    private Integer isFavorite;
```

- [ ] **步骤 2：NoteServiceImpl 注入 StorageService，实现文件读写**

```java
// 构造函数新增 StorageService 参数
    private final NoteMapper noteMapper;
    private final NoteCategoryMapper categoryMapper;
    private final TagService tagService;
    private final JdbcTemplate jdbcTemplate;
    private final StorageService storageService;   // 新增

// create 方法：创建笔记后，写入 note.md
    noteMapper.insert(note);
    String mdPath = "notes/" + note.getId() + "/note.md";
    if (dto.getContent() != null && !dto.getContent().isBlank()) {
        storageService.write(mdPath, dto.getContent());
    }
    note.setMdPath(mdPath);
    noteMapper.updateById(note);

// update 方法：更新笔记时，同步写入 note.md
    note.setTitle(dto.getTitle());
    note.setContent(dto.getContent());
    noteMapper.updateById(note);
    if (note.getMdPath() != null && dto.getContent() != null) {
        storageService.write(note.getMdPath(), dto.getContent());
    }

// getById（返回 NoteVO）：优先从文件读取 content
    NoteVO vo = NoteVO.from(note);
    if (note.getMdPath() != null && storageService.exists(note.getMdPath())) {
        try {
            vo.setContent(storageService.read(note.getMdPath()));
        } catch (Exception e) {
            log.warn("读取 note.md 失败，回退 DB: id={}", id);
        }
    }
    vo.setCategories(getCategories(id));
    vo.setTags(getTags(id));
    return vo;

// permanentDelete：删除笔记时清理资源目录
    jdbcTemplate.update("DELETE FROM note_category_rel WHERE note_id = ?", id);
    tagService.unbindAll("note", id);
    if (note.getMdPath() != null) {
        String noteDir = note.getMdPath().substring(0, note.getMdPath().indexOf("/note.md"));
        storageService.delete(noteDir);
    }
    noteMapper.deleteById(id);
```

- [ ] **步骤 3：验证编译**

```bash
cd personal-hub-server && mvn compile -pl ph-knowledge -am -q
```

- [ ] **步骤 4：Commit**

```bash
git add -A
git commit -m "feat: 笔记支持 note.md 文件读写，永久删除清理目录"
```

---

## 任务 8：笔记资源上传下载（NoteFileController）

**文件：**
- 创建：`personal-hub-server/ph-resource/src/main/java/com/personalhub/resource/service/NoteFileService.java`
- 创建：`personal-hub-server/ph-resource/src/main/java/com/personalhub/resource/service/impl/NoteFileServiceImpl.java`
- 创建：`personal-hub-server/ph-resource/src/main/java/com/personalhub/resource/controller/NoteFileController.java`

- [ ] **步骤 1：创建 NoteFileService 接口**

```java
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
```

- [ ] **步骤 2：创建 NoteFileServiceImpl**

```java
package com.personalhub.resource.service.impl;

import com.personalhub.common.exception.NotFoundException;
import com.personalhub.knowledge.entity.Note;
import com.personalhub.knowledge.mapper.NoteMapper;
import com.personalhub.resource.service.NoteFileService;
import com.personalhub.storage.StorageProperties;
import com.personalhub.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteFileServiceImpl implements NoteFileService {

    private final StorageService storageService;
    private final NoteMapper noteMapper;
    private final StorageProperties storageProperties;

    private String getNoteDir(Long noteId) {
        return "notes/" + noteId;
    }

    private void validateNote(Long noteId, Long userId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null || !note.getUserId().equals(userId)) {
            throw new NotFoundException("笔记不存在");
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("上传文件不能为空");
        if (file.getSize() > storageProperties.getMaxSize())
            throw new IllegalArgumentException("文件大小超过限制（最大 50MB）");
    }

    @Override
    public Map<String, String> uploadImage(Long noteId, Long userId, MultipartFile file) {
        validateNote(noteId, userId);
        validateFile(file);
        String ext = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        storageService.store(file, getNoteDir(noteId) + "/images/" + filename);
        return Map.of("url", "images/" + filename, "name", filename);
    }

    @Override
    public Map<String, String> uploadAttachment(Long noteId, Long userId, MultipartFile file) {
        validateNote(noteId, userId);
        validateFile(file);
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) originalName = "未命名文件";
        storageService.store(file, getNoteDir(noteId) + "/attachments/" + originalName);
        return Map.of("url", "attachments/" + originalName, "name", originalName);
    }

    @Override
    public Resource load(Long noteId, Long userId, String resourceType, String filename) {
        validateNote(noteId, userId);
        return storageService.load(getNoteDir(noteId) + "/" + resourceType + "/" + filename);
    }

    private String getExtension(String filename) {
        if (filename == null) return "png";
        int dot = filename.lastIndexOf('.');
        return (dot > 0) ? filename.substring(dot + 1).toLowerCase() : "png";
    }
}
```

- [ ] **步骤 3：创建 NoteFileController**

```java
package com.personalhub.resource.controller;

import com.personalhub.common.result.Result;
import com.personalhub.resource.service.NoteFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Tag(name = "笔记资源", description = "笔记的图片和附件管理")
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteFileController {

    private final NoteFileService noteFileService;

    @Operation(summary = "上传笔记配图")
    @PostMapping("/{noteId}/images")
    public Result<Map<String, String>> uploadImage(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long noteId,
            @RequestParam("file") MultipartFile file) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(noteFileService.uploadImage(noteId, userId, file));
    }

    @Operation(summary = "上传笔记附件")
    @PostMapping("/{noteId}/attachments")
    public Result<Map<String, String>> uploadAttachment(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long noteId,
            @RequestParam("file") MultipartFile file) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(noteFileService.uploadAttachment(noteId, userId, file));
    }

    @Operation(summary = "获取笔记图片")
    @GetMapping("/{noteId}/images/{filename}")
    public ResponseEntity<Resource> getImage(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long noteId,
            @PathVariable String filename) {
        Long userId = Long.valueOf(authentication.getName());
        Resource resource = noteFileService.load(noteId, userId, "images", filename);
        return ResponseEntity.ok().contentType(MediaType.IMAGE_PNG).body(resource);
    }

    @Operation(summary = "获取笔记附件")
    @GetMapping("/{noteId}/attachments/{filename}")
    public ResponseEntity<Resource> getAttachment(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long noteId,
            @PathVariable String filename) {
        Long userId = Long.valueOf(authentication.getName());
        Resource resource = noteFileService.load(noteId, userId, "attachments", filename);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}
```

- [ ] **步骤 4：验证编译**

```bash
cd personal-hub-server && mvn compile -pl ph-resource -am -q
```

- [ ] **步骤 5：Commit**

```bash
git add -A
git commit -m "feat: 新增笔记图片/附件上传下载 API"
```

---

## 任务 9：Markdown 导入服务

**文件：**
- 创建：`personal-hub-server/ph-knowledge/src/main/java/com/personalhub/knowledge/service/MarkdownImportService.java`
- 修改：`personal-hub-server/ph-knowledge/src/main/java/com/personalhub/knowledge/controller/NoteController.java`

- [ ] **步骤 1：创建 MarkdownImportService**

```java
package com.personalhub.knowledge.service;

import com.personalhub.common.storage.StorageService;
import com.personalhub.knowledge.dto.NoteCreateDTO;
import com.personalhub.knowledge.entity.Note;
import com.personalhub.knowledge.mapper.NoteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown 导入服务 — 自动解析并本地化所有资源引用
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MarkdownImportService {

    private final StorageService storageService;
    private final NoteService noteService;
    private final NoteMapper noteMapper;

    private static final Pattern IMG_PATTERN = Pattern.compile("!\\[([^\\]]*)\\]\\(([^)]+)\\)");
    private static final Pattern LINK_PATTERN = Pattern.compile("(?<!!)\\[([^\\]]*)\\]\\(([^)]+)\\)");

    /**
     * 导入 Markdown 文件，自动下载/复制所有引用资源
     */
    @Transactional
    public Long importMarkdown(Long userId, String title, List<Long> categoryIds,
                                List<Long> tagIds, MultipartFile file) {
        String content;
        try (var reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            content = sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("读取 Markdown 文件失败", e);
        }

        NoteCreateDTO dto = new NoteCreateDTO();
        dto.setTitle(title != null ? title : file.getOriginalFilename());
        dto.setContent(content);
        dto.setCategoryIds(categoryIds);
        dto.setTagIds(tagIds);

        NoteVO createdNote = noteService.create(userId, dto);
        Long noteId = createdNote.getId();
        String noteDir = "notes/" + noteId;

        Map<String, String> replacements = new LinkedHashMap<>();
        int imageCounter = 0, attachCounter = 0;

        Matcher imgMatcher = IMG_PATTERN.matcher(content);
        while (imgMatcher.find()) {
            String src = imgMatcher.group(2).trim();
            if (isLocalResource(src)) continue;
            imageCounter++;
            replacements.put(src, processResource(src, noteDir, "images", imageCounter));
        }

        Matcher linkMatcher = LINK_PATTERN.matcher(content);
        while (linkMatcher.find()) {
            String href = linkMatcher.group(2).trim();
            if (isLocalResource(href) || isTextOrMarkdown(href)) continue;
            attachCounter++;
            replacements.put(href, processResource(href, noteDir, "attachments", attachCounter));
        }

        String updatedContent = content;
        for (Map.Entry<String, String> entry : replacements.entrySet()) {
            updatedContent = updatedContent.replace(entry.getKey(), entry.getValue());
        }

        String mdPath = noteDir + "/note.md";
        storageService.write(mdPath, updatedContent);

        Note note = noteMapper.selectById(noteId);
        note.setMdPath(mdPath);
        note.setContent(updatedContent);
        noteMapper.updateById(note);

        log.info("Markdown 导入完成: noteId={}, 资源数量={}", noteId, replacements.size());
        return noteId;
    }

    private String processResource(String src, String noteDir, String type, int counter) {
        String ext = guessExtension(src);
        String prefix = type.equals("images") ? "image" : "attachment";
        String filename = prefix + "-" + String.format("%03d", counter) + "." + ext;
        String targetPath = noteDir + "/" + type + "/" + filename;

        try {
            byte[] data = resolveResource(src);
            if (data != null) storageService.store(data, targetPath);
        } catch (Exception e) {
            log.warn("资源处理失败，跳过: {}", src, e);
        }
        return type + "/" + filename;
    }

    private byte[] resolveResource(String src) throws Exception {
        src = src.trim();
        if (src.startsWith("http://") || src.startsWith("https://")) {
            try { return new URI(src).toURL().openStream().readAllBytes(); }
            catch (Exception e) { log.warn("网络资源下载失败: {}", src); return null; }
        }
        if (src.startsWith("file://")) return Files.readAllBytes(Paths.get(URI.create(src)));
        if (src.matches("^[A-Za-z]:\\\\.*")) return Files.readAllBytes(Paths.get(src));
        if (src.startsWith("/")) return Files.readAllBytes(Paths.get(src));
        log.warn("相对路径资源无法定位: {}", src);
        return null;
    }

    private boolean isLocalResource(String ref) {
        return ref.startsWith("images/") || ref.startsWith("attachments/");
    }

    private boolean isTextOrMarkdown(String ref) {
        String lower = ref.toLowerCase();
        return lower.endsWith(".md") || lower.endsWith(".txt");
    }

    private String guessExtension(String src) {
        String query = src;
        int qIdx = src.indexOf('?');
        if (qIdx > 0) query = src.substring(0, qIdx);
        int dot = query.lastIndexOf('.');
        if (dot > 0) {
            String ext = query.substring(dot + 1).toLowerCase();
            if (ext.length() <= 5 && ext.matches("[a-z0-9]+")) return ext;
        }
        return "png";
    }
}
```

- [ ] **步骤 2：在 NoteController 中新增导入端点**

```java
// 注入
    private final NoteService noteService;
    private final MarkdownImportService markdownImportService;

// 新增接口
    @Operation(summary = "导入 Markdown", description = "上传 .md 文件，自动本地化资源")
    @PostMapping("/import")
    public Result<Long> importMarkdown(
            @Parameter(hidden = true) Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            @RequestParam(value = "tagIds", required = false) List<Long> tagIds) {
        Long userId = Long.valueOf(authentication.getName());
        Long noteId = markdownImportService.importMarkdown(userId, title, categoryIds, tagIds, file);
        return Result.success(noteId);
    }
```

- [ ] **步骤 3：验证编译**

```bash
cd personal-hub-server && mvn compile -pl ph-knowledge -am -q
```

- [ ] **步骤 4：Commit**

```bash
git add -A
git commit -m "feat: 新增 Markdown 导入服务（资源本地化）"
```

---

## 任务 10：笔记导出服务 + 定时清理

**文件：**
- 创建：`personal-hub-server/ph-knowledge/src/main/java/com/personalhub/knowledge/service/NoteExportService.java`
- 创建：`personal-hub-server/ph-knowledge/src/main/java/com/personalhub/knowledge/service/NoteResourceCleanupTask.java`

- [ ] **步骤 1：创建 NoteExportService**

```java
package com.personalhub.knowledge.service;

import com.personalhub.common.exception.NotFoundException;
import com.personalhub.storage.StorageService;
import com.personalhub.knowledge.entity.Note;
import com.personalhub.knowledge.mapper.NoteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteExportService {

    private final NoteMapper noteMapper;
    private final StorageService storageService;

    public byte[] exportNote(Long noteId, Long userId) {
        Note note = noteMapper.selectById(noteId);
        if (note == null || !note.getUserId().equals(userId))
            throw new NotFoundException("笔记不存在");

        String noteDir = "notes/" + noteId;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try (ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {
            String mdContent;
            String mdPath = noteDir + "/note.md";
            if (storageService.exists(mdPath)) {
                mdContent = storageService.read(mdPath);
            } else {
                mdContent = note.getContent() != null ? note.getContent() : "";
            }

            String mdFileName = sanitizeFileName(note.getTitle()) + ".md";
            zos.putNextEntry(new ZipEntry(mdFileName));
            zos.write(mdContent.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            addToZip(zos, noteDir + "/images/", "images/");
            addToZip(zos, noteDir + "/attachments/", "attachments/");

        } catch (Exception e) {
            throw new RuntimeException("笔记导出失败", e);
        }
        return baos.toByteArray();
    }

    private void addToZip(ZipOutputStream zos, String dirPath, String prefix) {
        var files = storageService.listFiles(dirPath);
        for (String name : files) {
            try {
                Resource resource = storageService.load(dirPath + "/" + name);
                byte[] data = resource.getInputStream().readAllBytes();
                zos.putNextEntry(new ZipEntry(prefix + name));
                zos.write(data);
                zos.closeEntry();
            } catch (Exception e) {
                log.warn("导出文件失败: {}/{}", dirPath, name, e);
            }
        }
    }

    private String sanitizeFileName(String name) {
        return name != null ? name.replaceAll("[\\\\/:*?\"<>|]", "_") : "untitled";
    }
}
```

- [ ] **步骤 2：创建 NoteResourceCleanupTask**

```java
package com.personalhub.knowledge.service;

import com.personalhub.knowledge.mapper.NoteMapper;
import com.personalhub.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NoteResourceCleanupTask {

    private final StorageService storageService;
    private final NoteMapper noteMapper;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanupOrphanNoteResources() {
        log.info("开始清理孤立笔记资源...");
        var noteDirs = storageService.listFiles("notes/");
        int cleaned = 0;
        for (String dirName : noteDirs) {
            try {
                Long noteId = Long.parseLong(dirName);
                var note = noteMapper.selectById(noteId);
                if (note == null || note.getIsDeleted() == 1) {
                    storageService.delete("notes/" + noteId);
                    cleaned++;
                }
            } catch (NumberFormatException ignored) {}
        }
        log.info("孤立资源清理完成，共清理 {} 个目录", cleaned);
    }
}
```

- [ ] **步骤 3：在 NoteController 新增导出端点**

```java
// 注入 NoteExportService
    private final NoteExportService noteExportService;

    @Operation(summary = "导出笔记", description = "导出笔记为 ZIP，包含 Markdown 和资源")
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> export(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) throws java.io.IOException {
        Long userId = Long.valueOf(authentication.getName());
        byte[] zipData = noteExportService.exportNote(id, userId);
        String encodedName = URLEncoder.encode("笔记导出", StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName + ".zip")
                .body(zipData);
    }
```

- [ ] **步骤 4：验证编译**

```bash
cd personal-hub-server && mvn compile -pl ph-knowledge -am -q
```

- [ ] **步骤 5：Commit**

```bash
git add -A
git commit -m "feat: 新增笔记导出和定时清理任务"
```

---

## 任务 11：更新 API 文档

**文件：**
- 修改：`docs/API.md`
- 修改：`docs/superpowers/specs/2026-07-11-file-storage-design.md`（如有需要）

- [ ] **步骤 1：在 API.md 文件管理部分新增 preview**

```markdown
| GET | /api/files/{id}/preview | 预览（inline）| 是 |
```

- [ ] **步骤 2：新增笔记资源 API 表**

```markdown
### 笔记资源 `/api/notes/{noteId}`
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/notes/{noteId}/images | 上传笔记配图 | 是 |
| POST | /api/notes/{noteId}/attachments | 上传笔记附件 | 是 |
| GET | /api/notes/{noteId}/images/{filename} | 获取笔记图片 | 是 |
| GET | /api/notes/{noteId}/attachments/{filename} | 获取笔记附件 | 是 |

**POST /api/notes/{noteId}/images** — multipart/form-data: file(File必填)
```json
{"url":"images/xxx.png","name":"xxx.png"}
```
```

- [ ] **步骤 3：新增笔记导入/导出 API**

```markdown
### 笔记导入导出
| 方法 | 路径 | 说明 | 认证 |
|------|------|------|------|
| POST | /api/notes/import | 导入 Markdown（自动本地化资源）| 是 |
| GET | /api/notes/{id}/export | 导出笔记为 ZIP | 是 |

**POST /api/notes/import** — multipart/form-data: file(File必填), title(String可选), categoryIds(String可选), tagIds(String可选)
```

- [ ] **步骤 4：Commit**

```bash
git add -A
git commit -m "docs: 更新 API 文档（预览/笔记资源/导入导出）"
```
