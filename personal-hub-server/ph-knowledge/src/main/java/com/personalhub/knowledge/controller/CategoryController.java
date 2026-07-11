package com.personalhub.knowledge.controller;

import com.personalhub.common.result.Result;
import com.personalhub.knowledge.dto.CategoryCreateDTO;
import com.personalhub.knowledge.dto.CategoryUpdateDTO;
import com.personalhub.knowledge.service.CategoryService;
import com.personalhub.knowledge.vo.CategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统一分类控制器
 */
@Tag(name = "统一分类管理", description = "笔记/收藏夹/文件分类的统一增删改查")
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "分类列表（按类型）")
    @GetMapping
    public Result<List<CategoryVO>> list(
            @Parameter(hidden = true) Authentication authentication,
            @RequestParam String type) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(categoryService.listByType(userId, type));
    }

    @Operation(summary = "新建分类")
    @PostMapping
    public Result<CategoryVO> create(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody CategoryCreateDTO dto) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(categoryService.create(userId, dto));
    }

    @Operation(summary = "修改分类")
    @PutMapping("/{id}")
    public Result<CategoryVO> update(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody CategoryUpdateDTO dto) {
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
