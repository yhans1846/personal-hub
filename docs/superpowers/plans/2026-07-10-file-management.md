# 文件管理模块 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 实现文件上传/下载/分类管理模块，完成 MVP 第一阶段最后一个功能。

**架构：** 新建 `ph-file` Maven 子模块（后端），遵循现有 ph-todo 模块模式（Entity/DTO/VO/Mapper/Service/Controller）。前端新增文件列表页和分类管理页。文件存储在本地磁盘按日期分目录。

**技术栈：** Spring Boot 3 + MyBatis-Plus + Vue 3 + Element Plus + Lucide 图标

**前置条件：** 数据库表 `file_resource` 和 `file_category` 已在 `sql/init.sql` 中创建。

---

### 任务 1：后端 — 创建 ph-file 模块脚手架

**文件：**
- 创建：`backend/ph-file/pom.xml`
- 创建：`backend/ph-file/src/main/java/com/personalhub/module/file/entity/FileResource.java`
- 创建：`backend/ph-file/src/main/java/com/personalhub/module/file/entity/FileCategory.java`
- 创建：`backend/ph-file/src/main/java/com/personalhub/module/file/vo/FileVO.java`
- 创建：`backend/ph-file/src/main/java/com/personalhub/module/file/vo/FileCategoryVO.java`
- 创建：`backend/ph-file/src/main/java/com/personalhub/module/file/dto/FileQueryDTO.java`
- 创建：`backend/ph-file/src/main/java/com/personalhub/module/file/dto/FileCategoryCreateDTO.java`
- 创建：`backend/ph-file/src/main/java/com/personalhub/module/file/mapper/FileResourceMapper.java`
- 创建：`backend/ph-file/src/main/java/com/personalhub/module/file/mapper/FileCategoryMapper.java`

- [ ] **步骤 1：创建 ph-file/pom.xml**

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

    <artifactId>ph-file</artifactId>
    <name>ph-file</name>
    <description>文件管理模块</description>

    <dependencies>
        <dependency>
            <groupId>com.personalhub</groupId>
            <artifactId>ph-common</artifactId>
        </dependency>
    </dependencies>
</project>
```

- [ ] **步骤 2：创建 FileResource 实体**

```java
package com.personalhub.module.file.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件资源实体
 */
@Data
@TableName("file_resource")
public class FileResource {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 原始文件名 */
    private String name;

    /** 存储文件名（UUID） */
    private String storedName;

    /** 存储路径 */
    private String path;

    /** 大小（字节） */
    private Long size;

    /** 扩展名 */
    private String type;

    /** MIME 类型 */
    private String mimeType;

    /** 文件分类ID */
    private Long categoryId;

    /** 逻辑删除 0-正常 1-删除 */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

- [ ] **步骤 3：创建 FileCategory 实体**

```java
package com.personalhub.module.file.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件分类实体
 */
@Data
@TableName("file_category")
public class FileCategory {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户ID */
    private Long userId;

    /** 分类名称 */
    private String name;

    /** 排序 */
    private Integer sortOrder;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
```

- [ ] **步骤 4：创建 FileVO**

```java
package com.personalhub.module.file.vo;

import com.personalhub.module.file.entity.FileResource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 文件资源 VO
 */
@Data
@Schema(description = "文件资源详情")
public class FileVO {

    @Schema(description = "文件ID")
    private Long id;

    @Schema(description = "原始文件名")
    private String name;

    @Schema(description = "扩展名")
    private String type;

    @Schema(description = "文件类型图标标识")
    private String typeIcon;

    @Schema(description = "文件类型中文标签")
    private String typeLabel;

    @Schema(description = "大小（字节）")
    private Long size;

    @Schema(description = "格式化后的大小")
    private String sizeFormatted;

    @Schema(description = "MIME 类型")
    private String mimeType;

    @Schema(description = "文件分类ID")
    private Long categoryId;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    public static FileVO from(FileResource resource) {
        FileVO vo = new FileVO();
        vo.setId(resource.getId());
        vo.setName(resource.getName());
        vo.setType(resource.getType());
        vo.setSize(resource.getSize());
        vo.setMimeType(resource.getMimeType());
        vo.setCategoryId(resource.getCategoryId());
        vo.setCreatedAt(resource.getCreatedAt());

        // 格式化文件大小
        vo.setSizeFormatted(formatSize(resource.getSize()));

        // 设置类型图标和标签
        String ext = resource.getType() != null ? resource.getType().toLowerCase() : "";
        vo.setTypeIcon(getTypeIcon(ext));
        vo.setTypeLabel(getTypeLabel(ext));

        return vo;
    }

    private static String formatSize(Long bytes) {
        if (bytes == null) return "0 B";
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private static String getTypeIcon(String ext) {
        return switch (ext) {
            case "jpg", "jpeg", "png", "gif", "svg", "webp", "bmp" -> "image";
            case "pdf" -> "pdf";
            case "doc", "docx" -> "word";
            case "xls", "xlsx" -> "excel";
            case "ppt", "pptx" -> "ppt";
            case "md", "txt" -> "text";
            case "zip", "rar", "7z", "tar", "gz" -> "archive";
            default -> "file";
        };
    }

    private static String getTypeLabel(String ext) {
        return switch (ext) {
            case "jpg", "jpeg", "png", "gif", "svg", "webp", "bmp" -> "图片";
            case "pdf" -> "PDF";
            case "doc", "docx" -> "Word";
            case "xls", "xlsx" -> "Excel";
            case "ppt", "pptx" -> "PPT";
            case "md" -> "Markdown";
            case "txt" -> "文本";
            case "zip", "rar", "7z" -> "压缩包";
            default -> ext.toUpperCase();
        };
    }
}
```

- [ ] **步骤 5：创建 FileCategoryVO**

```java
package com.personalhub.module.file.vo;

import com.personalhub.module.file.entity.FileCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 文件分类 VO
 */
@Data
@Schema(description = "文件分类")
public class FileCategoryVO {

    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "排序")
    private Integer sortOrder;

    public static FileCategoryVO from(FileCategory category) {
        FileCategoryVO vo = new FileCategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setSortOrder(category.getSortOrder());
        return vo;
    }
}
```

- [ ] **步骤 6：创建 FileQueryDTO**

```java
package com.personalhub.module.file.dto;

import com.personalhub.common.result.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件查询参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文件查询参数")
public class FileQueryDTO extends PageParam {

    @Schema(description = "搜索关键词（文件名）")
    private String keyword;

    @Schema(description = "文件扩展名筛选")
    private String type;

    @Schema(description = "文件分类ID")
    private Long categoryId;
}
```

- [ ] **步骤 7：创建 FileCategoryCreateDTO**

```java
package com.personalhub.module.file.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 创建/编辑文件分类请求
 */
@Data
@Schema(description = "创建/编辑文件分类请求")
public class FileCategoryCreateDTO {

    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50")
    @Schema(description = "分类名称", example = "文档")
    private String name;

    @Schema(description = "排序", example = "0")
    private Integer sortOrder;
}
```

- [ ] **步骤 8：创建 FileResourceMapper**

```java
package com.personalhub.module.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.module.file.entity.FileResource;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件资源 Mapper
 */
@Mapper
public interface FileResourceMapper extends BaseMapper<FileResource> {
}
```

- [ ] **步骤 9：创建 FileCategoryMapper**

```java
package com.personalhub.module.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.module.file.entity.FileCategory;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件分类 Mapper
 */
@Mapper
public interface FileCategoryMapper extends BaseMapper<FileCategory> {
}
```

---

### 任务 2：后端 — 文件分类 CRUD 服务与控制器

**文件：**
- 创建：`backend/ph-file/src/main/java/com/personalhub/module/file/service/FileCategoryService.java`
- 创建：`backend/ph-file/src/main/java/com/personalhub/module/file/service/impl/FileCategoryServiceImpl.java`
- 创建：`backend/ph-file/src/main/java/com/personalhub/module/file/controller/FileCategoryController.java`

- [ ] **步骤 1：创建 FileCategoryService 接口**

```java
package com.personalhub.module.file.service;

import com.personalhub.module.file.dto.FileCategoryCreateDTO;
import com.personalhub.module.file.vo.FileCategoryVO;

import java.util.List;

/**
 * 文件分类服务接口
 */
public interface FileCategoryService {

    /**
     * 获取当前用户所有分类列表
     */
    List<FileCategoryVO> list(Long userId);

    /**
     * 新建分类
     */
    FileCategoryVO create(Long userId, FileCategoryCreateDTO dto);

    /**
     * 修改分类
     */
    FileCategoryVO update(Long id, Long userId, FileCategoryCreateDTO dto);

    /**
     * 删除分类
     */
    void delete(Long id, Long userId);
}
```

- [ ] **步骤 2：创建 FileCategoryServiceImpl**

```java
package com.personalhub.module.file.service.impl;

import com.personalhub.common.exception.NotFoundException;
import com.personalhub.module.file.dto.FileCategoryCreateDTO;
import com.personalhub.module.file.entity.FileCategory;
import com.personalhub.module.file.mapper.FileCategoryMapper;
import com.personalhub.module.file.service.FileCategoryService;
import com.personalhub.module.file.vo.FileCategoryVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 文件分类服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileCategoryServiceImpl implements FileCategoryService {

    private final FileCategoryMapper fileCategoryMapper;

    @Override
    public List<FileCategoryVO> list(Long userId) {
        List<FileCategory> list = fileCategoryMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FileCategory>()
                        .eq(FileCategory::getUserId, userId)
                        .orderByAsc(FileCategory::getSortOrder)
        );
        return list.stream().map(FileCategoryVO::from).toList();
    }

    @Override
    @Transactional
    public FileCategoryVO create(Long userId, FileCategoryCreateDTO dto) {
        FileCategory category = new FileCategory();
        category.setUserId(userId);
        category.setName(dto.getName());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        fileCategoryMapper.insert(category);
        log.info("新建文件分类: id={}, userId={}, name={}", category.getId(), userId, dto.getName());
        return FileCategoryVO.from(category);
    }

    @Override
    @Transactional
    public FileCategoryVO update(Long id, Long userId, FileCategoryCreateDTO dto) {
        FileCategory category = fileCategoryMapper.selectById(id);
        if (category == null || !category.getUserId().equals(userId)) {
            log.warn("编辑分类不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("分类不存在");
        }
        category.setName(dto.getName());
        category.setSortOrder(dto.getSortOrder() != null ? dto.getSortOrder() : 0);
        fileCategoryMapper.updateById(category);
        log.info("编辑文件分类: id={}, userId={}", id, userId);
        return FileCategoryVO.from(category);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        FileCategory category = fileCategoryMapper.selectById(id);
        if (category == null || !category.getUserId().equals(userId)) {
            log.warn("删除分类不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("分类不存在");
        }
        fileCategoryMapper.deleteById(id);
        log.info("删除文件分类: id={}, userId={}", id, userId);
    }
}
```

- [ ] **步骤 3：创建 FileCategoryController**

```java
package com.personalhub.module.file.controller;

import com.personalhub.common.result.Result;
import com.personalhub.module.file.dto.FileCategoryCreateDTO;
import com.personalhub.module.file.service.FileCategoryService;
import com.personalhub.module.file.vo.FileCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件分类控制器
 */
@Tag(name = "文件分类", description = "文件分类的增删改查")
@RestController
@RequestMapping("/api/file-categories")
@RequiredArgsConstructor
public class FileCategoryController {

    private final FileCategoryService fileCategoryService;

    @Operation(summary = "分类列表")
    @GetMapping
    public Result<List<FileCategoryVO>> list(
            @Parameter(hidden = true) Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(fileCategoryService.list(userId));
    }

    @Operation(summary = "新建分类")
    @PostMapping
    public Result<FileCategoryVO> create(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody FileCategoryCreateDTO dto) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(fileCategoryService.create(userId, dto));
    }

    @Operation(summary = "编辑分类")
    @PutMapping("/{id}")
    public Result<FileCategoryVO> update(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody FileCategoryCreateDTO dto) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(fileCategoryService.update(id, userId, dto));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        fileCategoryService.delete(id, userId);
        return Result.success();
    }
}
```

---

### 任务 3：后端 — 文件上传/下载/列表服务与控制器

**文件：**
- 创建：`backend/ph-file/src/main/java/com/personalhub/module/file/service/FileService.java`
- 创建：`backend/ph-file/src/main/java/com/personalhub/module/file/service/impl/FileServiceImpl.java`
- 创建：`backend/ph-file/src/main/java/com/personalhub/module/file/controller/FileController.java`

- [ ] **步骤 1：创建 FileService 接口**

```java
package com.personalhub.module.file.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.module.file.dto.FileQueryDTO;
import com.personalhub.module.file.vo.FileVO;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件资源服务接口
 */
public interface FileService {

    /**
     * 分页查询文件列表
     */
    IPage<FileVO> list(Long userId, FileQueryDTO query);

    /**
     * 获取文件详情
     */
    FileVO getById(Long id, Long userId);

    /**
     * 上传文件
     *
     * @param userId     用户ID
     * @param file       上传的文件
     * @param categoryId 文件分类ID（可选）
     * @return 文件VO
     */
    FileVO upload(Long userId, MultipartFile file, Long categoryId);

    /**
     * 下载文件
     *
     * @param id     文件ID
     * @param userId 用户ID
     * @return 文件资源的 ResponseEntity，供浏览器直接下载
     */
    ResponseEntity<Resource> download(Long id, Long userId);

    /**
     * 删除文件（软删除）
     */
    void delete(Long id, Long userId);
}
```

- [ ] **步骤 2：创建 FileServiceImpl**

```java
package com.personalhub.module.file.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.exception.BusinessException;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.module.file.dto.FileQueryDTO;
import com.personalhub.module.file.entity.FileResource;
import com.personalhub.module.file.mapper.FileResourceMapper;
import com.personalhub.module.file.service.FileService;
import com.personalhub.module.file.vo.FileVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * 文件资源服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final FileResourceMapper fileResourceMapper;

    @Value("${file.upload-dir:upload}")
    private String uploadDir;

    /** 允许上传的扩展名白名单 */
    private static final List<String> ALLOWED_EXTENSIONS = List.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
            "md", "txt",
            "png", "jpg", "jpeg", "gif", "svg", "webp", "bmp",
            "zip", "rar", "7z", "tar", "gz"
    );

    @Override
    public IPage<FileVO> list(Long userId, FileQueryDTO query) {
        LambdaQueryWrapper<FileResource> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileResource::getUserId, userId);

        // 关键词搜索
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(FileResource::getName, query.getKeyword());
        }
        // 类型筛选
        if (StringUtils.hasText(query.getType())) {
            wrapper.eq(FileResource::getType, query.getType());
        }
        // 分类筛选
        if (query.getCategoryId() != null) {
            wrapper.eq(FileResource::getCategoryId, query.getCategoryId());
        }

        wrapper.orderByDesc(FileResource::getCreatedAt);

        Page<FileResource> page = new Page<>(query.getPage(), query.getSize());
        IPage<FileResource> resourcePage = fileResourceMapper.selectPage(page, wrapper);

        return resourcePage.convert(resource -> {
            FileVO vo = FileVO.from(resource);
            // 查询分类名称（简单实现，只查一次缓存；数据量小可直接关联查询）
            return vo;
        });
    }

    @Override
    public FileVO getById(Long id, Long userId) {
        FileResource resource = fileResourceMapper.selectById(id);
        if (resource == null || !resource.getUserId().equals(userId)) {
            log.warn("文件不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("文件不存在");
        }
        return FileVO.from(resource);
    }

    @Override
    @Transactional
    public FileVO upload(Long userId, MultipartFile file, Long categoryId) {
        if (file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        // 校验文件扩展名
        String originalName = file.getOriginalFilename();
        if (originalName == null) {
            throw new BusinessException("文件名不能为空");
        }
        String ext = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            ext = originalName.substring(dotIndex + 1).toLowerCase();
        }
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException("不支持的文件类型: " + ext);
        }

        try {
            // 生成存储目录: upload/2026/07/
            String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
            String storedName = UUID.randomUUID() + (ext.isEmpty() ? "" : "." + ext);
            String relativePath = dateDir + "/" + storedName;
            Path fullPath = Paths.get(uploadDir, relativePath);

            // 创建目录
            Files.createDirectories(fullPath.getParent());

            // 写入磁盘
            file.transferTo(fullPath.toFile());

            // 入库
            FileResource resource = new FileResource();
            resource.setUserId(userId);
            resource.setName(originalName);
            resource.setStoredName(storedName);
            resource.setPath(relativePath);
            resource.setSize(file.getSize());
            resource.setType(ext);
            resource.setMimeType(file.getContentType());
            resource.setCategoryId(categoryId);
            fileResourceMapper.insert(resource);

            log.info("文件上传成功: id={}, userId={}, name={}, size={}",
                    resource.getId(), userId, originalName, file.getSize());

            return FileVO.from(resource);
        } catch (IOException e) {
            log.error("文件上传失败: name={}, userId={}", originalName, userId, e);
            throw new BusinessException("文件上传失败: " + e.getMessage());
        }
    }

    @Override
    public ResponseEntity<Resource> download(Long id, Long userId) {
        FileResource resource = fileResourceMapper.selectById(id);
        if (resource == null || !resource.getUserId().equals(userId)) {
            log.warn("下载文件不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("文件不存在");
        }

        Path filePath = Paths.get(uploadDir, resource.getPath());
        java.io.File diskFile = filePath.toFile();
        if (!diskFile.exists()) {
            log.error("文件已被删除或移动: storedPath={}", resource.getPath());
            throw new NotFoundException("文件已被删除或移动");
        }

        FileSystemResource fileResource = new FileSystemResource(diskFile);
        String encodedName = URLEncoder.encode(resource.getName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName)
                .body(fileResource);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        FileResource resource = fileResourceMapper.selectById(id);
        if (resource == null || !resource.getUserId().equals(userId)) {
            log.warn("删除文件不存在或无权访问: id={}, userId={}", id, userId);
            throw new NotFoundException("文件不存在");
        }
        fileResourceMapper.deleteById(id); // 逻辑删除
        log.info("文件已删除（逻辑）: id={}, userId={}, name={}", id, userId, resource.getName());
    }
}
```

- [ ] **步骤 3：创建 FileController**

```java
package com.personalhub.module.file.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.common.result.PageResult;
import com.personalhub.common.result.Result;
import com.personalhub.module.file.dto.FileQueryDTO;
import com.personalhub.module.file.service.FileService;
import com.personalhub.module.file.vo.FileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件资源控制器
 */
@Tag(name = "文件管理", description = "文件上传、下载、列表查询、删除")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "文件列表", description = "分页查询文件列表，支持关键词搜索、类型筛选、分类筛选")
    @GetMapping
    public Result<PageResult<FileVO>> list(
            @Parameter(hidden = true) Authentication authentication,
            FileQueryDTO query) {
        Long userId = Long.valueOf(authentication.getName());
        IPage<FileVO> page = fileService.list(userId, query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "文件详情")
    @GetMapping("/{id}")
    public Result<FileVO> getById(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(fileService.getById(id, userId));
    }

    @Operation(summary = "上传文件", description = "multipart/form-data 上传，支持 pdf/doc/docx/xls/xlsx/ppt/pptx/md/图片/zip/rar")
    @PostMapping("/upload")
    public Result<FileVO> upload(
            @Parameter(hidden = true) Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(fileService.upload(userId, file, categoryId));
    }

    @Operation(summary = "下载文件")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        return fileService.download(id, userId);
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        fileService.delete(id, userId);
        return Result.success();
    }
}
```

---

### 任务 4：后端 — 注册 ph-file 模块到父 POM 和启动模块

**文件：**
- 修改：`backend/pom.xml`
- 修改：`backend/ph-boot/pom.xml`
- 修改：`backend/ph-boot/src/main/resources/application.yml`

- [ ] **步骤 1：父 POM 添加 module 和 dependencyManagement**

修改 `backend/pom.xml`：

在 `<modules>` 中追加 `<module>ph-file</module>`：

```xml
<module>ph-todo</module>
<module>ph-file</module>
<module>ph-boot</module>
```

在 `<dependencyManagement>` 中追加：

```xml
<dependency>
    <groupId>com.personalhub</groupId>
    <artifactId>ph-file</artifactId>
    <version>${project.version}</version>
</dependency>
```

- [ ] **步骤 2：ph-boot/pom.xml 添加 ph-file 依赖**

在已有内部模块依赖后追加：

```xml
<dependency>
    <groupId>com.personalhub</groupId>
    <artifactId>ph-file</artifactId>
</dependency>
```

- [ ] **步骤 3：application.yml 添加文件上传配置**

在 `spring:` 块内追加：

```yaml
  servlet:
    multipart:
      enabled: true
      max-file-size: 50MB
      max-request-size: 60MB

# 文件上传存储目录
file:
  upload-dir: upload
```

---

### 任务 5：前端 — 类型定义和 API 层

**文件：**
- 创建：`frontend/src/types/file.ts`
- 创建：`frontend/src/api/fileApi.ts`

- [ ] **步骤 1：创建 types/file.ts**

```ts
/** 文件资源 */
export interface FileVO {
  id: number
  name: string
  type: string
  typeIcon: string
  typeLabel: string
  size: number
  sizeFormatted: string
  mimeType: string
  categoryId: number | null
  categoryName: string | null
  createdAt: string
}

/** 查询参数 */
export interface FileQuery {
  page?: number
  size?: number
  keyword?: string
  type?: string
  categoryId?: number
}

/** 文件分类 */
export interface FileCategoryVO {
  id: number
  name: string
  sortOrder: number
}

/** 创建/编辑文件分类 */
export interface FileCategoryCreateDTO {
  name: string
  sortOrder?: number
}
```

- [ ] **步骤 2：创建 api/fileApi.ts**

```ts
import request from './request'
import type { Result, PageResult } from '@/types/common'
import type { FileVO, FileQuery, FileCategoryVO, FileCategoryCreateDTO } from '@/types/file'

export function getFileList(params: FileQuery) {
  return request.get<Result<PageResult<FileVO>>>('/files', { params })
}

export function getFileById(id: number) {
  return request.get<Result<FileVO>>(`/files/${id}`)
}

export function uploadFile(file: File, categoryId?: number) {
  const form = new FormData()
  form.append('file', file)
  if (categoryId !== undefined && categoryId !== null) {
    form.append('categoryId', String(categoryId))
  }
  return request.post<Result<FileVO>>('/files/upload', form, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function downloadFile(id: number) {
  return request.get(`/files/${id}/download`, {
    responseType: 'blob'
  }).then(res => {
    const disposition = res.headers['content-disposition'] || ''
    const match = disposition.match(/filename\*=UTF-8''(.+?)$/)
    const fileName = match
      ? decodeURIComponent(match[1])
      : `download-${id}`
    const url = URL.createObjectURL(res.data)
    const a = document.createElement('a')
    a.href = url
    a.download = fileName
    a.click()
    URL.revokeObjectURL(url)
  })
}

export function deleteFile(id: number) {
  return request.delete<Result<void>>(`/files/${id}`)
}

export function getFileCategories() {
  return request.get<Result<FileCategoryVO[]>>('/file-categories')
}

export function createFileCategory(data: FileCategoryCreateDTO) {
  return request.post<Result<FileCategoryVO>>('/file-categories', data)
}

export function updateFileCategory(id: number, data: FileCategoryCreateDTO) {
  return request.put<Result<FileCategoryVO>>(`/file-categories/${id}`, data)
}

export function deleteFileCategory(id: number) {
  return request.delete<Result<void>>(`/file-categories/${id}`)
}
```

---

### 任务 6：前端 — 文件列表页（含上传对话框）

**文件：**
- 创建：`frontend/src/views/files/FileList.vue`

- [ ] **步骤 1：创建 FileList.vue**

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getFileList, uploadFile, downloadFile, deleteFile, getFileCategories } from '@/api/fileApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Upload, Download, Trash2, Search,
  File, FileImage, FileText, FileArchive, FileSpreadsheet, FileSlides
} from 'lucide-vue-next'
import type { FileVO, FileQuery, FileCategoryVO } from '@/types/file'

const list = ref<FileVO[]>([])
const total = ref(0)
const loading = ref(false)
const query = ref<FileQuery>({ page: 1, size: 20, keyword: '' })

const categories = ref<FileCategoryVO[]>([])

// 上传对话框
const uploadDialogVisible = ref(false)
const uploadFileRef = ref<File | null>(null)
const uploadCategoryId = ref<number | undefined>(undefined)
const uploading = ref(false)

// 类型筛选选项
const typeOptions = [
  { value: '', label: '全部类型' },
  { value: 'pdf', label: 'PDF' },
  { value: 'doc', label: 'Word' },
  { value: 'docx', label: 'Word' },
  { value: 'xls', label: 'Excel' },
  { value: 'xlsx', label: 'Excel' },
  { value: 'ppt', label: 'PPT' },
  { value: 'pptx', label: 'PPT' },
  { value: 'md', label: 'Markdown' },
  { value: 'png', label: '图片' },
  { value: 'jpg', label: '图片' },
  { value: 'jpeg', label: '图片' },
  { value: 'gif', label: '图片' },
  { value: 'zip', label: '压缩包' },
  { value: 'rar', label: '压缩包' }
]

onMounted(() => {
  fetchList()
  fetchCategories()
})

async function fetchList() {
  loading.value = true
  try {
    const res = await getFileList(query.value)
    list.value = res.data.data.records
    total.value = res.data.data.total
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  try {
    const res = await getFileCategories()
    categories.value = res.data.data
  } catch (_) { /* ignore */ }
}

function onSearch() { query.value.page = 1; fetchList() }
function onPageChange(page: number) { query.value.page = page; fetchList() }
function onFilterChange() { query.value.page = 1; fetchList() }

function openUpload() {
  uploadFileRef.value = null
  uploadCategoryId.value = undefined
  uploadDialogVisible.value = true
}

function onFileSelected(e: Event) {
  const target = e.target as HTMLInputElement
  if (target.files && target.files.length > 0) {
    uploadFileRef.value = target.files[0]
  }
}

async function handleUpload() {
  if (!uploadFileRef.value) { ElMessage.warning('请选择文件'); return }
  uploading.value = true
  try {
    await uploadFile(uploadFileRef.value, uploadCategoryId.value)
    ElMessage.success('上传成功')
    uploadDialogVisible.value = false
    fetchList()
  } catch (e: any) {
    ElMessage.error(e.response?.data?.message || '上传失败')
  } finally {
    uploading.value = false
  }
}

function handleDownload(file: FileVO) {
  downloadFile(file.id).catch(() => ElMessage.error('下载失败'))
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该文件？', '提示', { type: 'warning' })
  await deleteFile(id)
  ElMessage.success('已删除')
  fetchList()
}

function getIcon(icon: string) {
  return {
    image: FileImage,
    pdf: FileText,
    word: FileText,
    excel: FileSpreadsheet,
    ppt: FileSlides,
    text: FileText,
    archive: FileArchive,
    file: File
  }[icon] || File
}
</script>

<template>
  <div>
    <div class="page-header">
      <h2>文件管理</h2>
      <p>共 {{ total }} 个文件</p>
    </div>

    <div class="toolbar">
      <div class="toolbar-left">
        <el-input v-model="query.keyword" placeholder="搜索文件..." style="width:200px" clearable @clear="onSearch" @keyup.enter="onSearch">
          <template #prefix><Search :size="14" style="color: var(--text-tertiary)" /></template>
        </el-input>
        <el-select v-model="query.type" placeholder="文件类型" style="width:130px" clearable @change="onFilterChange">
          <el-option v-for="item in typeOptions" :key="item.value" :value="item.value || undefined" :label="item.label" />
        </el-select>
        <el-select v-model="query.categoryId" placeholder="分类" style="width:130px" clearable @change="onFilterChange">
          <el-option v-for="item in categories" :key="item.id" :value="item.id" :label="item.name" />
        </el-select>
      </div>
      <el-button type="primary" @click="openUpload">
        <Upload :size="14" /> 上传文件
      </el-button>
    </div>

    <!-- 加载骨架 -->
    <div v-if="loading" class="file-grid">
      <div v-for="i in 8" :key="i" class="file-card-skeleton" />
    </div>

    <!-- 空状态 -->
    <div v-else-if="list.length === 0" class="empty-state">
      <div class="empty-state__icon"><File :size="48" /></div>
      <div class="empty-state__text">还没有文件，上传一个吧</div>
      <el-button type="primary" @click="openUpload">上传文件</el-button>
    </div>

    <!-- 文件网格 -->
    <div v-else class="file-grid">
      <div v-for="file in list" :key="file.id" class="file-card">
        <div class="file-card__icon">
          <component :is="getIcon(file.typeIcon)" :size="32" />
        </div>
        <div class="file-card__info">
          <div class="file-card__name" :title="file.name">{{ file.name }}</div>
          <div class="file-card__meta">
            <span>{{ file.sizeFormatted }}</span>
            <span class="dot">·</span>
            <span>{{ file.typeLabel }}</span>
            <span v-if="file.categoryName" class="dot">·</span>
            <span v-if="file.categoryName">{{ file.categoryName }}</span>
          </div>
          <div class="file-card__date">{{ file.createdAt?.slice(0, 10) }}</div>
        </div>
        <div class="file-card__actions">
          <button class="icon-btn" title="下载" @click="handleDownload(file)">
            <Download :size="14" />
          </button>
          <button class="icon-btn icon-btn--danger" title="删除" @click="handleDelete(file.id)">
            <Trash2 :size="14" />
          </button>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <el-pagination
      v-if="total > query.size"
      v-model:current-page="query.page"
      :total="total" :page-size="query.size"
      layout="total, prev, pager, next"
      style="margin-top: var(--sp-6); justify-content: flex-end"
      @current-change="onPageChange"
    />

    <!-- 上传对话框 -->
    <el-dialog v-model="uploadDialogVisible" title="上传文件" width="420px" top="30vh">
      <el-upload
        ref="uploadRef"
        :auto-upload="false"
        :show-file-list="true"
        drag
        style="margin-bottom: var(--sp-4)"
        @change="onFileSelected"
      >
        <div style="padding: 20px">
          <Upload :size="40" style="color: var(--text-tertiary); margin-bottom: 8px" />
          <div style="color: var(--text-secondary); font-size: var(--text-sm)">
            拖拽文件到此处，或<em style="color: var(--accent); font-style: normal">点击选择</em>
          </div>
          <div style="color: var(--text-tertiary); font-size: var(--text-xs); margin-top: 4px">
            支持 PDF / Word / Excel / PPT / Markdown / 图片 / 压缩包，最大 50MB
          </div>
        </div>
      </el-upload>
      <el-select v-model="uploadCategoryId" placeholder="选择分类（可选）" style="width:100%" clearable>
        <el-option v-for="item in categories" :key="item.id" :value="item.id" :label="item.name" />
      </el-select>
      <template #footer>
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="uploading" @click="handleUpload">
          {{ uploading ? '上传中...' : '开始上传' }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.file-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: var(--sp-3);
}

.file-card-skeleton {
  height: 100px;
  border-radius: var(--radius-md);
  background: var(--bg-hover);
  animation: pulse 1.5s ease-in-out infinite;
}

.file-card {
  display: flex;
  align-items: center;
  gap: var(--sp-3);
  padding: var(--sp-4);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  transition: all var(--transition);
}
.file-card:hover {
  box-shadow: var(--shadow-sm);
  border-color: var(--accent-border);
}

.file-card__icon {
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: var(--accent);
  background: var(--accent-light);
  border-radius: var(--radius-sm);
}

.file-card__info {
  flex: 1;
  min-width: 0;
}
.file-card__name {
  font-size: var(--text-sm);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.file-card__meta {
  font-size: var(--text-xs);
  color: var(--text-tertiary);
  margin-top: 2px;
  display: flex;
  align-items: center;
  gap: 4px;
  flex-wrap: wrap;
}
.file-card__date {
  font-size: var(--text-xs);
  color: var(--text-placeholder);
  margin-top: 1px;
}
.dot { color: var(--text-placeholder); }

.file-card__actions {
  display: flex;
  gap: var(--sp-1);
  flex-shrink: 0;
  opacity: 0;
  transition: opacity var(--transition);
}
.file-card:hover .file-card__actions { opacity: 1; }

.icon-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 6px;
  border-radius: var(--radius-sm);
  color: var(--text-tertiary);
  transition: all var(--transition);
  display: flex;
  align-items: center;
}
.icon-btn:hover { color: var(--accent); background: var(--accent-light); }
.icon-btn--danger:hover { color: var(--danger); background: var(--danger-light); }
</style>
```

---

### 任务 7：前端 — 文件分类管理页

**文件：**
- 创建：`frontend/src/views/files/FileCategoryManage.vue`

- [ ] **步骤 1：创建 FileCategoryManage.vue**

```vue
<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { getFileCategories, createFileCategory, updateFileCategory, deleteFileCategory } from '@/api/fileApi'
import { ElMessage, ElMessageBox } from 'element-plus'
import { FolderPlus, Pencil, Trash2, Folder } from 'lucide-vue-next'
import type { FileCategoryVO, FileCategoryCreateDTO } from '@/types/file'

const list = ref<FileCategoryVO[]>([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const form = ref<FileCategoryCreateDTO & { id: number }>({ id: 0, name: '', sortOrder: 0 })

onMounted(() => fetchList())

async function fetchList() {
  const res = await getFileCategories()
  list.value = res.data.data
}

function openCreate() {
  isEdit.value = false
  form.value = { id: 0, name: '', sortOrder: 0 }
  dialogVisible.value = true
}

function openEdit(row: FileCategoryVO) {
  isEdit.value = true
  form.value = { id: row.id, name: row.name, sortOrder: row.sortOrder }
  dialogVisible.value = true
}

async function handleSave() {
  if (!form.value.name.trim()) { ElMessage.warning('请输入名称'); return }
  if (isEdit.value) {
    await updateFileCategory(form.value.id, { name: form.value.name, sortOrder: form.value.sortOrder })
    ElMessage.success('已更新')
  } else {
    await createFileCategory({ name: form.value.name, sortOrder: form.value.sortOrder })
    ElMessage.success('已创建')
  }
  dialogVisible.value = false
  fetchList()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确定删除该分类？', '提示', { type: 'warning' })
  await deleteFileCategory(id)
  ElMessage.success('已删除')
  fetchList()
}
</script>

<template>
  <div>
    <div class="page-header">
      <h2>文件分类管理</h2>
      <p>管理文件分类</p>
    </div>

    <div class="toolbar">
      <span class="text-secondary">{{ list.length }} 个分类</span>
      <el-button type="primary" size="small" @click="openCreate">
        <FolderPlus :size="14" /> 新建分类
      </el-button>
    </div>

    <div v-if="list.length === 0" class="empty-state">
      <div class="empty-state__icon"><Folder :size="48" /></div>
      <div class="empty-state__text">暂无分类，创建一个吧</div>
    </div>

    <div v-else class="manage-list">
      <div v-for="item in list" :key="item.id" class="manage-item">
        <div class="manage-item-left">
          <Folder :size="16" class="text-tertiary" />
          <span class="manage-item-name">{{ item.name }}</span>
          <span class="manage-item-meta">排序 {{ item.sortOrder }}</span>
        </div>
        <div class="manage-item-actions">
          <button class="icon-btn" @click="openEdit(item)"><Pencil :size="14" /></button>
          <button class="icon-btn icon-btn--danger" @click="handleDelete(item.id)"><Trash2 :size="14" /></button>
        </div>
      </div>
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑分类' : '新建分类'" width="400px" top="30vh">
      <el-form>
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.manage-list { display: flex; flex-direction: column; gap: var(--sp-2); }
.manage-item {
  display: flex; justify-content: space-between; align-items: center;
  padding: var(--sp-3) var(--sp-5); background: var(--bg-card);
  border: 1px solid var(--border-color); border-radius: var(--radius-md);
  transition: box-shadow var(--transition);
}
.manage-item:hover { box-shadow: var(--shadow-sm); }
.manage-item-left { display: flex; align-items: center; gap: var(--sp-3); }
.manage-item-name { font-size: var(--text-sm); font-weight: 500; }
.manage-item-meta { font-size: var(--text-xs); color: var(--text-tertiary); }
.manage-item-actions { display: flex; gap: var(--sp-1); }
.icon-btn {
  background: none; border: none; cursor: pointer; padding: 6px;
  border-radius: var(--radius-sm); color: var(--text-tertiary);
  transition: all var(--transition); display: flex; align-items: center;
}
.icon-btn:hover { color: var(--accent); background: var(--accent-light); }
.icon-btn--danger:hover { color: var(--danger); background: var(--danger-light); }
</style>
```

---

### 任务 8：前端 — 路由和侧边栏导航

**文件：**
- 修改：`frontend/src/router/index.ts`
- 修改：`frontend/src/components/AppLayout.vue`

- [ ] **步骤 1：路由表中添加文件管理路由**

在 `router/index.ts` 的 children 数组中，`/todos/:id/edit` 路由之后追加：

```ts
{ path: 'files', name: 'FileList', component: () => import('@/views/files/FileList.vue') },
{ path: 'files/categories', name: 'FileCategories', component: () => import('@/views/files/FileCategoryManage.vue') },
```

- [ ] **步骤 2：侧边栏添加导航项**

在 `AppLayout.vue` 的 `<template>` 中，`待办任务` 导航项之后、`导航分隔线` 之前添加：

```html
<router-link to="/files" class="nav-item" :class="{ active: $route.path.startsWith('/files') && !$route.path.includes('/categories') }">
  <Folder :size="18" />
  <span>文件</span>
</router-link>
```

并在 script 的 import 中添加 `Folder`：

```ts
import { ..., Folder } from 'lucide-vue-next'
```

在分隔线之后的子导航区域添加：

```html
<router-link to="/files/categories" class="nav-item nav-item--sub" :class="{ active: $route.path === '/files/categories' }">
  <Grid3X3 :size="14" />
  <span>文件分类</span>
</router-link>
```

---

### 任务 9：更新文档

**文件：**
- 修改：`docs/DEVELOPMENT_PLAN.md`
- 修改：`docs/ROADMAP.md`

- [ ] **步骤 1：更新 DEVELOPMENT_PLAN.md**

将 Step 10 的状态从 `🔲` 改为 `✅`。

- [ ] **步骤 2：更新 ROADMAP.md**

将文件管理行的 `[ ]` 改为 `[x]`。

---

### 任务 10：编译验证

- [ ] **步骤 1：后端编译**

运行：`cd backend && mvn compile -q`
预期：BUILD SUCCESS，无编译错误。

- [ ] **步骤 2：前端类型检查**

运行：`cd frontend && npx vue-tsc --noEmit`
预期：无类型错误。

- [ ] **步骤 3：启动后端验证**

运行：`cd backend && mvn spring-boot:run`
预期：启动成功，访问 `http://localhost:8080/swagger-ui.html` 可看到文件管理相关接口。
