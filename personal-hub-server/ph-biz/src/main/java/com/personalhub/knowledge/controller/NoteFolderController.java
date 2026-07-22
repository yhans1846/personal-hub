package com.personalhub.knowledge.controller;

import com.personalhub.common.result.Result;
import com.personalhub.common.util.CurrentUser;
import com.personalhub.knowledge.dto.NoteFolderCreateDTO;
import com.personalhub.knowledge.dto.NoteFolderMoveDTO;
import com.personalhub.knowledge.dto.NoteFolderUpdateDTO;
import com.personalhub.knowledge.service.NoteFolderService;
import com.personalhub.knowledge.vo.NoteFolderTreeVO;
import com.personalhub.knowledge.vo.NoteFolderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "笔记文件夹", description = "笔记库文件夹树")
@Validated
@RestController
@RequestMapping("/api/note-folders")
@RequiredArgsConstructor
public class NoteFolderController {

    private final NoteFolderService noteFolderService;

    @Operation(summary = "文件夹树")
    @GetMapping("/tree")
    public Result<NoteFolderTreeVO> tree(@Parameter(hidden = true) Authentication authentication) {
        return Result.success(noteFolderService.tree(CurrentUser.id(authentication)));
    }

    @Operation(summary = "创建文件夹")
    @PostMapping
    public Result<NoteFolderVO> create(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody NoteFolderCreateDTO dto) {
        return Result.success(noteFolderService.create(CurrentUser.id(authentication), dto));
    }

    @Operation(summary = "重命名文件夹")
    @PutMapping("/{id}")
    public Result<NoteFolderVO> rename(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody NoteFolderUpdateDTO dto) {
        return Result.success(noteFolderService.rename(CurrentUser.id(authentication), id, dto));
    }

    @Operation(summary = "移动文件夹")
    @PatchMapping("/{id}/move")
    public Result<NoteFolderVO> move(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody NoteFolderMoveDTO dto) {
        return Result.success(noteFolderService.move(CurrentUser.id(authentication), id, dto));
    }

    @Operation(summary = "删除文件夹（子树笔记归未分类）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        noteFolderService.delete(CurrentUser.id(authentication), id);
        return Result.success();
    }
}
