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

/**
 * 文件资源控制器
 */
@Tag(name = "文件管理", description = "文件的上传、下载、列表、删除、预览")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileResourceService fileResourceService;
    private final StorageService storageService;

    @Operation(summary = "文件列表", description = "分页查询文件，支持关键词搜索、类型筛选、分类筛选")
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

    @Operation(summary = "上传文件", description = "multipart/form-data 上传")
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
            } catch (Exception ignored) {
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
