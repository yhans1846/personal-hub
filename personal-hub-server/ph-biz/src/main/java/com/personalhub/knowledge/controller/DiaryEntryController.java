package com.personalhub.knowledge.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.common.result.PageResult;
import com.personalhub.common.result.Result;
import com.personalhub.common.util.CurrentUser;
import com.personalhub.knowledge.dto.DiaryCreateDTO;
import com.personalhub.knowledge.dto.DiaryImageFromUrlDTO;
import com.personalhub.knowledge.dto.DiaryQueryDTO;
import com.personalhub.knowledge.service.DiaryEntryService;
import com.personalhub.knowledge.service.DiaryFileService;
import com.personalhub.knowledge.vo.DiaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 日记控制器
 */
@Tag(name = "日记", description = "日记的增删改查、月视图聚合、配图资源")
@Validated
@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryEntryController {

    private final DiaryEntryService diaryEntryService;
    private final DiaryFileService diaryFileService;

    @Operation(summary = "日记列表", description = "分页查询日记，支持关键词搜索、日期范围筛选、心情筛选、按月查询")
    @GetMapping
    public Result<PageResult<DiaryVO>> list(
            @Parameter(hidden = true) Authentication authentication,
            @Valid DiaryQueryDTO query) {
        Long userId = CurrentUser.id(authentication);
        IPage<DiaryVO> page = diaryEntryService.list(userId, query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "月视图", description = "按月查询日记列表，返回当月所有日记")
    @GetMapping("/month")
    public Result<List<DiaryVO>> listByMonth(
            @Parameter(hidden = true) Authentication authentication,
            @RequestParam String month) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(diaryEntryService.listByMonth(userId, month));
    }

    @Operation(summary = "日记详情")
    @GetMapping("/{id}")
    public Result<DiaryVO> getById(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(diaryEntryService.getById(id, userId));
    }

    @Operation(summary = "新建日记")
    @PostMapping
    public Result<DiaryVO> create(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody DiaryCreateDTO dto) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(diaryEntryService.create(userId, dto));
    }

    @Operation(summary = "编辑日记")
    @PutMapping("/{id}")
    public Result<DiaryVO> update(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody DiaryCreateDTO dto) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(diaryEntryService.update(id, userId, dto));
    }

    @Operation(summary = "删除日记")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        diaryEntryService.delete(id, userId);
        return Result.success();
    }

    @Operation(summary = "上传日记配图")
    @PostMapping("/{id}/images")
    public Result<Map<String, String>> uploadImage(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(diaryFileService.uploadImage(id, userId, file));
    }

    @Operation(summary = "从链接添加日记配图", description = "服务端下载 http(s) 图片并写入配图目录")
    @PostMapping("/{id}/images/from-url")
    public Result<Map<String, String>> uploadImageFromUrl(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody DiaryImageFromUrlDTO dto) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(diaryFileService.uploadImageFromUrl(id, userId, dto.getUrl()));
    }

    @Operation(summary = "获取日记配图")
    @GetMapping("/{id}/images/{filename}")
    public ResponseEntity<Resource> getImage(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @PathVariable String filename) {
        Long userId = CurrentUser.id(authentication);
        Resource resource = diaryFileService.loadImage(id, userId, filename);
        return ResponseEntity.ok()
                .contentType(guessImageMediaType(filename))
                .body(resource);
    }

    @Operation(summary = "删除日记配图")
    @DeleteMapping("/{id}/images/{filename}")
    public Result<Void> deleteImage(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @PathVariable String filename) {
        Long userId = CurrentUser.id(authentication);
        diaryFileService.deleteImage(id, userId, filename);
        return Result.success();
    }

    private static MediaType guessImageMediaType(String filename) {
        String lower = filename == null ? "" : filename.toLowerCase();
        if (lower.endsWith(".png")) return MediaType.IMAGE_PNG;
        if (lower.endsWith(".gif")) return MediaType.IMAGE_GIF;
        if (lower.endsWith(".webp")) return MediaType.parseMediaType("image/webp");
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return MediaType.IMAGE_JPEG;
        return MediaType.APPLICATION_OCTET_STREAM;
    }
}
