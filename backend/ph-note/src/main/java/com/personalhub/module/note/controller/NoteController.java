package com.personalhub.module.note.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.common.result.PageResult;
import com.personalhub.common.result.Result;
import com.personalhub.module.note.dto.NoteCreateDTO;
import com.personalhub.module.note.dto.NoteQueryDTO;
import com.personalhub.module.note.service.NoteService;
import com.personalhub.module.note.vo.NoteVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 笔记控制器
 */
@Tag(name = "Markdown 笔记", description = "笔记的增删改查、收藏、回收站")
@RestController
@RequestMapping("/api/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;

    @Operation(summary = "笔记列表", description = "分页查询笔记，支持关键词搜索、分类/标签/收藏/回收站筛选")
    @GetMapping
    public Result<PageResult<NoteVO>> list(
            @Parameter(hidden = true) Authentication authentication,
            NoteQueryDTO query) {
        Long userId = Long.valueOf(authentication.getName());
        IPage<NoteVO> page = noteService.listNotes(userId, query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "笔记详情")
    @GetMapping("/{id}")
    public Result<NoteVO> getById(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(noteService.getById(id, userId));
    }

    @Operation(summary = "新建笔记")
    @PostMapping
    public Result<NoteVO> create(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody NoteCreateDTO dto) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(noteService.create(userId, dto));
    }

    @Operation(summary = "编辑笔记")
    @PutMapping("/{id}")
    public Result<NoteVO> update(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody NoteCreateDTO dto) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(noteService.update(id, userId, dto));
    }

    @Operation(summary = "删除笔记（移入回收站）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        noteService.delete(id, userId);
        return Result.success();
    }

    @Operation(summary = "切换收藏")
    @PutMapping("/{id}/favorite")
    public Result<Void> toggleFavorite(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        noteService.toggleFavorite(id, userId);
        return Result.success();
    }

    @Operation(summary = "最近编辑")
    @GetMapping("/recent")
    public Result<PageResult<NoteVO>> recent(
            @Parameter(hidden = true) Authentication authentication,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = Long.valueOf(authentication.getName());
        IPage<NoteVO> result = noteService.getRecent(userId, page, size);
        return Result.success(PageResult.of(result));
    }

    @Operation(summary = "恢复笔记")
    @PatchMapping("/{id}/restore")
    public Result<Void> restore(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        noteService.restore(id, userId);
        return Result.success();
    }

    @Operation(summary = "永久删除")
    @DeleteMapping("/{id}/permanent")
    public Result<Void> permanentDelete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        noteService.permanentDelete(id, userId);
        return Result.success();
    }
}
