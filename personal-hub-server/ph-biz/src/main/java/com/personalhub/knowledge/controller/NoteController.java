package com.personalhub.knowledge.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.common.result.PageParam;
import com.personalhub.common.result.PageResult;
import com.personalhub.common.result.Result;
import com.personalhub.common.util.CurrentUser;
import com.personalhub.knowledge.dto.ImportContentDTO;
import com.personalhub.knowledge.dto.NoteBatchExportDTO;
import com.personalhub.knowledge.dto.NoteCreateDTO;
import com.personalhub.knowledge.dto.NoteQueryDTO;
import com.personalhub.knowledge.imports.ImportReport;
import com.personalhub.knowledge.imports.ImportService;
import com.personalhub.knowledge.service.NoteExportService;
import com.personalhub.knowledge.service.NoteService;
import com.personalhub.knowledge.vo.NoteBacklinkVO;
import com.personalhub.knowledge.vo.NoteVO;
import com.personalhub.knowledge.vo.RecycleEmptyVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 笔记控制器
 */
@Tag(name = "Markdown 笔记", description = "笔记的增删改查、收藏、回收站、导入导出")
@Validated
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final ImportService importService;
    private final NoteExportService noteExportService;

    @Operation(summary = "笔记列表", description = "分页查询笔记，支持关键词搜索、分类/标签/收藏/回收站筛选")
    @GetMapping
    public Result<PageResult<NoteVO>> list(
            @Parameter(hidden = true) Authentication authentication,
            @Valid NoteQueryDTO query) {
        Long userId = CurrentUser.id(authentication);
        IPage<NoteVO> page = noteService.listNotes(userId, query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "笔记详情")
    @GetMapping("/{id}")
    public Result<NoteVO> getById(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(noteService.getById(id, userId));
    }

    @Operation(summary = "新建笔记")
    @PostMapping
    public Result<NoteVO> create(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody NoteCreateDTO dto) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(noteService.create(userId, dto));
    }

    @Operation(summary = "编辑笔记")
    @PutMapping("/{id}")
    public Result<NoteVO> update(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody NoteCreateDTO dto) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(noteService.update(id, userId, dto));
    }

    @Operation(summary = "删除笔记（移入回收站）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        noteService.delete(id, userId);
        return Result.success();
    }

    @Operation(summary = "归档笔记（移入回收站，原因 AUTO_ARCHIVE）")
    @PostMapping("/{id}/archive")
    public Result<Void> archive(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        noteService.archive(id, userId);
        return Result.success();
    }

    @Operation(summary = "切换收藏")
    @PutMapping("/{id}/favorite")
    public Result<Void> toggleFavorite(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        noteService.toggleFavorite(id, userId);
        return Result.success();
    }

    @Operation(summary = "最近编辑")
    @GetMapping("/recent")
    public Result<PageResult<NoteVO>> recent(
            @Parameter(hidden = true) Authentication authentication,
            @Valid PageParam pageParam) {
        Long userId = CurrentUser.id(authentication);
        IPage<NoteVO> result = noteService.getRecent(userId, pageParam.getPage(), pageParam.getSize());
        return Result.success(PageResult.of(result));
    }

    @Operation(summary = "恢复笔记")
    @PatchMapping("/{id}/restore")
    public Result<Void> restore(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        noteService.restore(id, userId);
        return Result.success();
    }

    @Operation(summary = "永久删除")
    @DeleteMapping("/{id}/permanent")
    public Result<Void> permanentDelete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        noteService.permanentDelete(id, userId);
        return Result.success();
    }

    @Operation(summary = "清空回收站", description = "永久删除当前用户回收站全部笔记及磁盘资源")
    @DeleteMapping("/recycle-bin")
    public Result<RecycleEmptyVO> emptyRecycleBin(
            @Parameter(hidden = true) Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(noteService.emptyRecycleBin(userId));
    }

    @Operation(summary = "导入 Markdown 文件", description = "上传 .md 文件，自动本地化资源。可选 baseDir 用于解析相对路径")
    @PostMapping("/import")
    public Result<ImportReport> importMarkdown(
            @Parameter(hidden = true) Authentication authentication,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "categoryIds", required = false) List<Long> categoryIds,
            @RequestParam(value = "tagIds", required = false) List<Long> tagIds,
            @RequestParam(value = "baseDir", required = false) String baseDir) {
        Long userId = CurrentUser.id(authentication);
        ImportReport report = importService.importFromFile(userId, title, categoryIds, tagIds, file, baseDir);
        return Result.success(report);
    }

    @Operation(summary = "粘贴 Markdown 内容导入", description = "粘贴 Markdown 文本导入笔记，自动下载网络图片和 Base64 图片")
    @PostMapping("/import-content")
    public Result<ImportReport> importContent(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody ImportContentDTO dto) {
        Long userId = CurrentUser.id(authentication);
        ImportReport report = importService.importFromContent(
                userId, dto.getTitle(), dto.getContent(),
                dto.getCategoryIds(), dto.getTagIds());
        return Result.success(report);
    }

    @Operation(summary = "回收站列表", description = "分页查询已删除笔记，按删除时间倒序")
    @GetMapping("/recycle")
    public Result<PageResult<NoteVO>> recycle(
            @Parameter(hidden = true) Authentication authentication,
            @Valid NoteQueryDTO query) {
        Long userId = CurrentUser.id(authentication);
        IPage<NoteVO> page = noteService.getRecycleList(userId, query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "笔记预览", description = "只读预览，允许查看已删除笔记")
    @GetMapping("/{id}/preview")
    public Result<NoteVO> preview(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(noteService.getPreview(id, userId));
    }

    @Operation(summary = "笔记回链", description = "扫描当前用户笔记中 [[本篇标题]] 的引用方（最多 500 篇）")
    @GetMapping("/{id}/backlinks")
    public Result<List<NoteBacklinkVO>> backlinks(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(noteService.listBacklinks(id, userId));
    }

    @Operation(summary = "导出笔记", description = "导出笔记为 ZIP，包含 Markdown 和资源")
    @GetMapping("/{id}/export")
    public ResponseEntity<byte[]> export(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        byte[] zipData = noteExportService.exportNote(id, userId);
        String encodedName = URLEncoder.encode("笔记导出", StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName + ".zip")
                .body(zipData);
    }

    @Operation(summary = "批量导出笔记", description = "勾选多篇笔记打包为 ZIP，每篇独立目录含 note.md 与资源")
    @PostMapping("/export")
    public ResponseEntity<byte[]> exportBatch(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody NoteBatchExportDTO dto) {
        Long userId = CurrentUser.id(authentication);
        byte[] zipData = noteExportService.exportNotes(dto.getIds(), userId);
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        String encodedName = URLEncoder.encode("notes-export-" + stamp, StandardCharsets.UTF_8)
                .replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName + ".zip")
                .body(zipData);
    }
}
