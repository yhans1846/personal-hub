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

/**
 * 笔记资源控制器（图片/附件上传下载）
 */
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
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
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
