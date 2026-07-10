package com.personalhub.module.diary.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.common.result.PageResult;
import com.personalhub.common.result.Result;
import com.personalhub.module.diary.dto.DiaryCreateDTO;
import com.personalhub.module.diary.dto.DiaryQueryDTO;
import com.personalhub.module.diary.service.DiaryEntryService;
import com.personalhub.module.diary.vo.DiaryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 日记控制器
 */
@Tag(name = "日记", description = "日记的增删改查、月视图聚合")
@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryEntryController {

    private final DiaryEntryService diaryEntryService;

    @Operation(summary = "日记列表", description = "分页查询日记，支持关键词搜索、日期范围筛选、心情筛选、按月查询")
    @GetMapping
    public Result<PageResult<DiaryVO>> list(
            @Parameter(hidden = true) Authentication authentication,
            DiaryQueryDTO query) {
        Long userId = Long.valueOf(authentication.getName());
        IPage<DiaryVO> page = diaryEntryService.list(userId, query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "月视图", description = "按月查询日记列表，返回当月所有日记")
    @GetMapping("/month")
    public Result<List<DiaryVO>> listByMonth(
            @Parameter(hidden = true) Authentication authentication,
            @RequestParam String month) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(diaryEntryService.listByMonth(userId, month));
    }

    @Operation(summary = "日记详情")
    @GetMapping("/{id}")
    public Result<DiaryVO> getById(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(diaryEntryService.getById(id, userId));
    }

    @Operation(summary = "新建日记")
    @PostMapping
    public Result<DiaryVO> create(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody DiaryCreateDTO dto) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(diaryEntryService.create(userId, dto));
    }

    @Operation(summary = "编辑日记")
    @PutMapping("/{id}")
    public Result<DiaryVO> update(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody DiaryCreateDTO dto) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(diaryEntryService.update(id, userId, dto));
    }

    @Operation(summary = "删除日记")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        diaryEntryService.delete(id, userId);
        return Result.success();
    }
}
