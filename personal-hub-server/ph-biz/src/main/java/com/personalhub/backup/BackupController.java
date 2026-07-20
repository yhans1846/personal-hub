package com.personalhub.backup;

import com.personalhub.common.exception.BusinessException;
import com.personalhub.common.result.Result;
import com.personalhub.common.util.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 数据备份与恢复。
 */
@Tag(name = "数据备份")
@Validated
@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
public class BackupController {

    private final DataBackupService dataBackupService;

    @Operation(summary = "立即备份", description = "写入服务端历史并下载 ZIP")
    @PostMapping("/now")
    public ResponseEntity<byte[]> backupNow(
            @Parameter(hidden = true) Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        byte[] zip = dataBackupService.createStoredBackup(userId, "MANUAL");
        String encoded = URLEncoder.encode(DataBackupServiceImpl.suggestFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(zip);
    }

    @Operation(summary = "备份历史列表")
    @GetMapping("/list")
    public Result<List<UserBackupVO>> list(
            @Parameter(hidden = true) Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(dataBackupService.listBackups(userId));
    }

    @Operation(summary = "下载历史备份")
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        byte[] zip = dataBackupService.downloadBackup(userId, id);
        String encoded = URLEncoder.encode(DataBackupServiceImpl.suggestFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(zip);
    }

    @Operation(summary = "从历史备份恢复", description = "先打当前快照再全量覆盖")
    @PostMapping("/{id}/restore")
    public Result<Void> restore(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        dataBackupService.restoreFromBackup(userId, id);
        return Result.success();
    }

    @Operation(summary = "删除历史备份")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        dataBackupService.deleteBackup(userId, id);
        return Result.success();
    }

    @Operation(summary = "获取备份设置")
    @GetMapping("/settings")
    public Result<BackupSettingsDTO> getSettings(
            @Parameter(hidden = true) Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(dataBackupService.getSettings(userId));
    }

    @Operation(summary = "更新备份设置")
    @PutMapping("/settings")
    public Result<Void> updateSettings(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody BackupSettingsDTO dto) {
        Long userId = CurrentUser.id(authentication);
        dataBackupService.updateSettings(userId, dto);
        return Result.success();
    }

    @Operation(summary = "导入备份", description = "上传 ZIP，全量覆盖当前用户业务数据")
    @PostMapping("/import")
    public Result<Void> importBackup(
            @Parameter(hidden = true) Authentication authentication,
            @RequestParam("file") MultipartFile file) throws Exception {
        Long userId = CurrentUser.id(authentication);
        if (file == null || file.isEmpty()) {
            throw new BusinessException("请选择备份文件");
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".zip")) {
            throw new BusinessException("仅支持 .zip 备份包");
        }
        dataBackupService.importZip(userId, file.getBytes());
        return Result.success();
    }
}
