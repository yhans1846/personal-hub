package com.personalhub.module.file.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.common.result.PageResult;
import com.personalhub.common.result.Result;
import com.personalhub.module.file.dto.FileQueryDTO;
import com.personalhub.module.file.service.FileResourceService;
import com.personalhub.module.file.vo.FileVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 文件资源控制器
 */
@Tag(name = "文件管理", description = "文件的上传、下载、列表、删除")
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final FileResourceService fileResourceService;

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

        try {
            Path filePath = Paths.get(fileEntity.getPath());
            java.io.File file = filePath.toFile();

            if (!file.exists()) {
                // 尝试在 uploadDir 下查找
                filePath = Paths.get("uploads", fileEntity.getPath());
                file = filePath.toFile();
            }

            if (!file.exists()) {
                throw new FileNotFoundException("文件不存在于存储中");
            }

            InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

            String encodedName = URLEncoder.encode(fileEntity.getName(), StandardCharsets.UTF_8)
                    .replace("+", "%20");

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(file.length())
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encodedName)
                    .body(resource);
        } catch (java.io.FileNotFoundException e) {
            throw new com.personalhub.common.exception.NotFoundException("文件不存在于存储中");
        } catch (Exception e) {
            throw new RuntimeException("文件下载失败", e);
        }
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
