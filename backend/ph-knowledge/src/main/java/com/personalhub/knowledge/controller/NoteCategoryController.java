package com.personalhub.knowledge.controller;

import com.personalhub.common.result.Result;
import com.personalhub.knowledge.entity.NoteCategory;
import com.personalhub.knowledge.service.NoteCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 笔记分类控制器
 */
@Tag(name = "笔记分类", description = "笔记分类的增删改查")
@RestController
@RequestMapping("/api/note-categories")
@RequiredArgsConstructor
public class NoteCategoryController {

    private final NoteCategoryService service;

    @Operation(summary = "分类列表")
    @GetMapping
    public Result<List<NoteCategory>> list(
            @Parameter(hidden = true) Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(service.listByUser(userId));
    }

    @Operation(summary = "新建分类")
    @PostMapping
    public Result<NoteCategory> create(
            @Parameter(hidden = true) Authentication authentication,
            @RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(authentication.getName());
        String name = (String) body.get("name");
        Integer sortOrder = body.get("sortOrder") != null ? ((Number) body.get("sortOrder")).intValue() : 0;
        return Result.success(service.create(userId, name, sortOrder));
    }

    @Operation(summary = "修改分类")
    @PutMapping("/{id}")
    public Result<NoteCategory> update(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(authentication.getName());
        String name = (String) body.get("name");
        Integer sortOrder = body.get("sortOrder") != null ? ((Number) body.get("sortOrder")).intValue() : null;
        return Result.success(service.update(id, userId, name, sortOrder));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        service.delete(id, userId);
        return Result.success();
    }
}
