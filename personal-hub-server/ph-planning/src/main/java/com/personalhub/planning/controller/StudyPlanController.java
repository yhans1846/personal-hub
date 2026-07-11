package com.personalhub.planning.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.common.result.PageResult;
import com.personalhub.common.result.Result;
import com.personalhub.planning.dto.StudyPlanCreateDTO;
import com.personalhub.planning.dto.StudyPlanQueryDTO;
import com.personalhub.planning.service.StudyPlanService;
import com.personalhub.planning.vo.StudyPlanVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 学习计划控制器
 */
@Tag(name = "学习计划", description = "学习计划的增删改查、进度跟踪")
@RestController
@RequestMapping("/api/study-plans")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @Operation(summary = "计划列表", description = "分页查询学习计划，支持关键词搜索、状态筛选")
    @GetMapping
    public Result<PageResult<StudyPlanVO>> list(
            @Parameter(hidden = true) Authentication authentication,
            StudyPlanQueryDTO query) {
        Long userId = Long.valueOf(authentication.getName());
        IPage<StudyPlanVO> page = studyPlanService.list(userId, query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "计划详情")
    @GetMapping("/{id}")
    public Result<StudyPlanVO> getById(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(studyPlanService.getById(id, userId));
    }

    @Operation(summary = "新建计划")
    @PostMapping
    public Result<StudyPlanVO> create(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody StudyPlanCreateDTO dto) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(studyPlanService.create(userId, dto));
    }

    @Operation(summary = "编辑计划")
    @PutMapping("/{id}")
    public Result<StudyPlanVO> update(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody StudyPlanCreateDTO dto) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(studyPlanService.update(id, userId, dto));
    }

    @Operation(summary = "删除计划")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        studyPlanService.delete(id, userId);
        return Result.success();
    }
}
