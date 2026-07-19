package com.personalhub.backup;

import com.personalhub.common.exception.BusinessException;
import com.personalhub.common.result.Result;
import com.personalhub.common.util.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 数据备份与恢复。
 */
@Tag(name = "数据备份")
@RestController
@RequestMapping("/api/backup")
@RequiredArgsConstructor
public class BackupController {

    private final DataBackupService dataBackupService;

    @Operation(summary = "立即备份", description = "导出当前用户业务数据与文件为 ZIP")
    @PostMapping("/now")
    public ResponseEntity<byte[]> backupNow(
            @Parameter(hidden = true) Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        byte[] zip = dataBackupService.exportZip(userId);
        String encoded = URLEncoder.encode(DataBackupServiceImpl.suggestFileName(), StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(zip);
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
