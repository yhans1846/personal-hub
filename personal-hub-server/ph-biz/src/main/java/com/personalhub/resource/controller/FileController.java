package com.personalhub.resource.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.common.result.PageResult;
import com.personalhub.common.result.Result;
import com.personalhub.common.util.CurrentUser;
import com.personalhub.common.util.FilenameGuard;
import com.personalhub.resource.dto.FileQueryDTO;
import com.personalhub.resource.dto.FileCategoryUpdateDTO;
import com.personalhub.resource.service.FileResourceService;
import com.personalhub.resource.vo.FileVO;
import com.personalhub.resource.vo.FileClearVO;
import com.personalhub.storage.FileAssetService;
import com.personalhub.storage.StoragePaths;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 文件资源控制器
 */
@Tag(name = "文件管理", description = "文件的上传、下载、列表、删除、预览")
@Validated
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Slf4j
public class FileController {

    private final FileResourceService fileResourceService;
    private final FileAssetService fileAssetService;

    @Operation(summary = "文件列表", description = "分页查询文件，支持关键词搜索、类型筛选、分类筛选")
    @GetMapping
    public Result<PageResult<FileVO>> list(
            @Parameter(hidden = true) Authentication authentication,
            @Valid FileQueryDTO query) {
        Long userId = CurrentUser.id(authentication);
        IPage<FileVO> page = fileResourceService.list(userId, query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "文件详情")
    @GetMapping("/{id}")
    public Result<FileVO> getById(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(fileResourceService.getById(id, userId));
    }

    @Operation(summary = "上传文件", description = "multipart/form-data 上传")
    @PostMapping("/upload")
    public Result<FileVO> upload(
            @Parameter(hidden = true) Authentication authentication,
            @NotNull @RequestParam("file") MultipartFile file,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(fileResourceService.upload(userId, file, categoryId));
    }

    @Operation(summary = "下载文件")
    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        var fileEntity = fileResourceService.getFileResource(id, userId);
        Resource resource = fileAssetService.load(fileEntity.getPath());

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
        Long userId = CurrentUser.id(authentication);
        var fileEntity = fileResourceService.getFileResource(id, userId);
        Resource resource = fileAssetService.load(fileEntity.getPath());

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (fileEntity.getMimeType() != null) {
            try {
                mediaType = MediaType.parseMediaType(fileEntity.getMimeType());
            } catch (InvalidMediaTypeException e) {
                log.debug("无法解析 MIME 类型，回退 octet-stream: {}", fileEntity.getMimeType());
            }
        }

        String encodedName = URLEncoder.encode(fileEntity.getName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename*=UTF-8''" + encodedName)
                .body(resource);
    }

    @Operation(summary = "获取头像文件（公开）")
    @GetMapping("/avatar/{filename}")
    public ResponseEntity<Resource> getAvatar(@PathVariable String filename) {
        try {
            FilenameGuard.requireSafe(filename);
            Resource resource = fileAssetService.load(StoragePaths.avatar(filename));
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            String contentType = getContentTypeByExt(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS))
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    private String getContentTypeByExt(String filename) {
        String ext = "";
        int dot = filename.lastIndexOf('.');
        if (dot > 0) ext = filename.substring(dot + 1).toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            default -> "application/octet-stream";
        };
    }

    @Operation(summary = "更新文件分类")
    @PatchMapping("/{id}/category")
    public Result<FileVO> updateCategory(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @RequestBody FileCategoryUpdateDTO dto) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(fileResourceService.updateCategory(id, userId, dto.getCategoryId()));
    }

    @Operation(summary = "清空全部文件", description = "删除当前用户全部文件（磁盘+库），不可恢复；返回删除条数")
    @DeleteMapping
    public Result<FileClearVO> clearAll(
            @Parameter(hidden = true) Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(fileResourceService.clearAll(userId));
    }

    @Operation(summary = "删除文件")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        fileResourceService.delete(id, userId);
        return Result.success();
    }
}
