package com.personalhub.module.note.controller;

import com.personalhub.common.result.Result;
import com.personalhub.module.note.entity.NoteTag;
import com.personalhub.module.note.service.NoteTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 笔记标签控制器
 */
@Tag(name = "笔记标签", description = "笔记标签的增删改查")
@RestController
@RequestMapping("/api/note-tags")
@RequiredArgsConstructor
public class NoteTagController {

    private final NoteTagService service;

    @Operation(summary = "标签列表")
    @GetMapping
    public Result<List<NoteTag>> list(
            @Parameter(hidden = true) Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(service.listByUser(userId));
    }

    @Operation(summary = "新建标签")
    @PostMapping
    public Result<NoteTag> create(
            @Parameter(hidden = true) Authentication authentication,
            @RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(authentication.getName());
        String name = (String) body.get("name");
        return Result.success(service.create(userId, name));
    }

    @Operation(summary = "修改标签")
    @PutMapping("/{id}")
    public Result<NoteTag> update(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {
        Long userId = Long.valueOf(authentication.getName());
        String name = (String) body.get("name");
        return Result.success(service.update(id, userId, name));
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        service.delete(id, userId);
        return Result.success();
    }
}
