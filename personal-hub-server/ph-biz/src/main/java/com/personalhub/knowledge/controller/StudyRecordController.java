package com.personalhub.knowledge.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.common.result.PageResult;
import com.personalhub.common.result.Result;
import com.personalhub.common.util.CurrentUser;
import com.personalhub.knowledge.dto.StudyRecordCreateDTO;
import com.personalhub.knowledge.dto.StudyRecordQueryDTO;
import com.personalhub.knowledge.service.StudyRecordService;
import com.personalhub.knowledge.vo.StudyRecordVO;
import com.personalhub.knowledge.vo.StudyStatsVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 学习记录控制器
 */
@Tag(name = "学习记录", description = "学习记录的增删改查")
@RestController
@RequestMapping("/api/study-records")
@RequiredArgsConstructor
public class StudyRecordController {

    private final StudyRecordService service;

    @Operation(summary = "学习记录列表", description = "分页查询，支持关键词搜索和日期范围筛选")
    @GetMapping
    public Result<PageResult<StudyRecordVO>> list(
            @Parameter(hidden = true) Authentication authentication,
            StudyRecordQueryDTO query) {
        Long userId = CurrentUser.id(authentication);
        IPage<StudyRecordVO> page = service.list(userId, query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "学习记录详情")
    @GetMapping("/{id}")
    public Result<StudyRecordVO> getById(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(service.getById(id, userId));
    }

    @Operation(summary = "新建学习记录")
    @PostMapping
    public Result<StudyRecordVO> create(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody StudyRecordCreateDTO dto) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(service.create(userId, dto));
    }

    @Operation(summary = "编辑学习记录")
    @PutMapping("/{id}")
    public Result<StudyRecordVO> update(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody StudyRecordCreateDTO dto) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(service.update(id, userId, dto));
    }

    @Operation(summary = "学习统计", description = "今日学习时长、本周统计、连续学习天数")
    @GetMapping("/stats")
    public Result<StudyStatsVO> stats(@Parameter(hidden = true) Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(service.stats(userId));
    }

    @Operation(summary = "删除学习记录")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        service.delete(id, userId);
        return Result.success();
    }
}
