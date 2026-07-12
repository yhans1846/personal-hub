package com.personalhub.system.controller;

import com.personalhub.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Tag(name = "数据备份")
@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
public class BackupController {

    @Operation(summary = "立即备份")
    @PostMapping("/now")
    public Result<Map<String, Object>> backupNow(Authentication auth) {
        Long userId = Long.valueOf(auth.getName());
        return Result.success(Map.of(
            "id", 0,
            "downloadUrl", ""
        ));
    }

    @Operation(summary = "备份列表")
    @GetMapping("/list")
    public Result<List<Map<String, Object>>> list(Authentication auth) {
        return Result.success(List.of());
    }

    @Operation(summary = "下载备份")
    @GetMapping("/download/{id}")
    public Result<String> download(@PathVariable Long id) {
        return Result.success("备份下载功能建设中");
    }

    @Operation(summary = "导入备份")
    @PostMapping("/import")
    public Result<Void> importBackup(Authentication auth, @RequestParam("file") MultipartFile file) {
        return Result.success();
    }
}
