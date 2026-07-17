package com.personalhub.knowledge.controller;

import com.personalhub.common.result.PageResult;
import com.personalhub.common.result.Result;
import com.personalhub.common.util.CurrentUser;
import com.personalhub.knowledge.dto.ReadingCreateDTO;
import com.personalhub.knowledge.dto.ReadingQueryDTO;
import com.personalhub.knowledge.service.ReadingRecordService;
import com.personalhub.knowledge.vo.ReadingVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Tag(name = "阅读记录", description = "阅读记录的增删改查、章节进度跟踪")
@RestController
@RequestMapping("/api/readings")
@RequiredArgsConstructor
public class ReadingRecordController {
    private final ReadingRecordService readingService;

    @Operation(summary = "阅读列表")
    @GetMapping
    public Result<PageResult<ReadingVO>> list(@Parameter(hidden = true) Authentication auth, ReadingQueryDTO q) {
        return Result.success(PageResult.of(readingService.list(CurrentUser.id(auth), q)));
    }

    @Operation(summary = "导出 XLSX", description = "scope=filtered 带当前筛选；scope=all 导出全部。内存生成，不落盘。")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @Parameter(hidden = true) Authentication auth,
            @RequestParam(defaultValue = "filtered") String scope,
            ReadingQueryDTO query) {
        byte[] xlsx = readingService.exportXlsx(CurrentUser.id(auth), query, scope);
        String filename = URLEncoder.encode("阅读记录.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .body(xlsx);
    }

    @Operation(summary = "阅读详情")
    @GetMapping("/{id}")
    public Result<ReadingVO> getById(@Parameter(hidden = true) Authentication auth, @PathVariable Long id) {
        return Result.success(readingService.getById(id, CurrentUser.id(auth)));
    }

    @Operation(summary = "新建记录")
    @PostMapping
    public Result<ReadingVO> create(@Parameter(hidden = true) Authentication auth, @Valid @RequestBody ReadingCreateDTO dto) {
        return Result.success(readingService.create(CurrentUser.id(auth), dto));
    }

    @Operation(summary = "编辑记录")
    @PutMapping("/{id}")
    public Result<ReadingVO> update(
            @Parameter(hidden = true) Authentication auth,
            @PathVariable Long id,
            @Valid @RequestBody ReadingCreateDTO dto) {
        return Result.success(readingService.update(id, CurrentUser.id(auth), dto));
    }

    @Operation(summary = "删除记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(hidden = true) Authentication auth, @PathVariable Long id) {
        readingService.delete(id, CurrentUser.id(auth));
        return Result.success();
    }
}
