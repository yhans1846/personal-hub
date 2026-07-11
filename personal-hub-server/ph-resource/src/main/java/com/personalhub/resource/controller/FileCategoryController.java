package com.personalhub.resource.controller;

import com.personalhub.common.result.Result;
import com.personalhub.resource.dto.FileCategoryCreateDTO;
import com.personalhub.resource.entity.FileCategory;
import com.personalhub.resource.service.FileCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 文件分类控制器
 */
@Tag(name = "文件分类", description = "文件分类的增删改查")
@RestController
@RequestMapping("/api/file-categories")
@RequiredArgsConstructor
public class FileCategoryController {

    private final FileCategoryService categoryService;

    @Operation(summary = "分类列表")
    @GetMapping
    public Result<List<FileCategory>> list(
            @Parameter(hidden = true) Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(categoryService.list(userId));
    }

    @Operation(summary = "新建分类")
    @PostMapping
    public Result<FileCategory> create(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody FileCategoryCreateDTO dto) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(categoryService.create(userId, dto));
    }

    @Operation(summary = "编辑分类")
    @PutMapping("/{id}")
    public Result<FileCategory> update(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody FileCategoryCreateDTO dto) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(categoryService.update(id, userId, dto));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        categoryService.delete(id, userId);
        return Result.success();
    }
}
