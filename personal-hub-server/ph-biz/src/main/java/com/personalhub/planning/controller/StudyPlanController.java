package com.personalhub.planning.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.common.result.PageResult;
import com.personalhub.common.result.Result;
import com.personalhub.common.util.CurrentUser;
import com.personalhub.planning.dto.StudyPlanCreateDTO;
import com.personalhub.planning.dto.StudyPlanQueryDTO;
import com.personalhub.planning.service.StudyPlanService;
import com.personalhub.planning.vo.StudyPlanStatsVO;
import com.personalhub.planning.vo.StudyPlanVO;
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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 学习计划控制器
 */
@Tag(name = "学习计划", description = "学习计划的增删改查、进度跟踪")
@Validated
@RestController
@RequestMapping("/api/study-plans")
@RequiredArgsConstructor
public class StudyPlanController {

    private final StudyPlanService studyPlanService;

    @Operation(summary = "计划列表", description = "分页查询学习计划，支持关键词、状态、标签、排序")
    @GetMapping
    public Result<PageResult<StudyPlanVO>> list(
            @Parameter(hidden = true) Authentication authentication,
            @Valid StudyPlanQueryDTO query) {
        Long userId = CurrentUser.id(authentication);
        IPage<StudyPlanVO> page = studyPlanService.list(userId, query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "状态统计")
    @GetMapping("/stats")
    public Result<StudyPlanStatsVO> stats(
            @Parameter(hidden = true) Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(studyPlanService.stats(userId));
    }

    @Operation(summary = "导出 XLSX", description = "scope=filtered 带当前筛选；scope=all 导出全部。内存生成，不落盘。")
    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @Parameter(hidden = true) Authentication authentication,
            @RequestParam(defaultValue = "filtered") String scope,
            @Valid StudyPlanQueryDTO query) {
        Long userId = CurrentUser.id(authentication);
        byte[] xlsx = studyPlanService.exportXlsx(userId, query, scope);
        String filename = URLEncoder.encode("学习计划.xlsx", StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + filename)
                .body(xlsx);
    }

    @Operation(summary = "计划详情")
    @GetMapping("/{id}")
    public Result<StudyPlanVO> getById(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(studyPlanService.getById(id, userId));
    }

    @Operation(summary = "新建计划")
    @PostMapping
    public Result<StudyPlanVO> create(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody StudyPlanCreateDTO dto) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(studyPlanService.create(userId, dto));
    }

    @Operation(summary = "编辑计划")
    @PutMapping("/{id}")
    public Result<StudyPlanVO> update(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody StudyPlanCreateDTO dto) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(studyPlanService.update(id, userId, dto));
    }

    @Operation(summary = "删除计划")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        studyPlanService.delete(id, userId);
        return Result.success();
    }
}
