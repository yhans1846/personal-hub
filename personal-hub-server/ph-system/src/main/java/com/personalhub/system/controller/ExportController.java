package com.personalhub.system.controller;

import com.personalhub.common.result.Result;
import com.personalhub.common.util.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "数据导出")
@RestController
@RequestMapping("/api/export")
@RequiredArgsConstructor
public class ExportController {

    @Operation(summary = "导出数据")
    @PostMapping
    public Result<Map<String, String>> export(
            Authentication auth,
            @RequestBody ExportRequest request) {
        Long userId = CurrentUser.id(auth);
        // 简单返回成功，实际导出异步处理
        return Result.success(Map.of(
            "downloadUrl", "/api/export/download/" + userId + "?format=" + request.format
        ));
    }

    @Operation(summary = "下载导出文件")
    @GetMapping("/download/{userId}")
    public Result<String> download(@PathVariable Long userId, @RequestParam(defaultValue = "markdown") String format) {
        return Result.success("导出功能建设中，请查看 docs/ 了解手动备份方式");
    }

    @Operation(summary = "导出历史")
    @GetMapping("/history")
    public Result<List<Map<String, Object>>> history(Authentication auth) {
        return Result.success(List.of());
    }

    public record ExportRequest(List<String> modules, String format) {}
}
