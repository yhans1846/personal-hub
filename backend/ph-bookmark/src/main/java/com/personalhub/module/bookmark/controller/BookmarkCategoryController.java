package com.personalhub.module.bookmark.controller;

import com.personalhub.common.result.Result;
import com.personalhub.module.bookmark.dto.BookmarkCategoryDTO;
import com.personalhub.module.bookmark.service.BookmarkCategoryService;
import com.personalhub.module.bookmark.vo.BookmarkCategoryVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 收藏夹分类控制器
 */
@Tag(name = "收藏夹分类", description = "收藏夹分类管理")
@RestController
@RequestMapping("/api/bookmark-categories")
@RequiredArgsConstructor
public class BookmarkCategoryController {

    private final BookmarkCategoryService bookmarkCategoryService;

    @Operation(summary = "分类列表")
    @GetMapping
    public Result<List<BookmarkCategoryVO>> list(@Parameter(hidden = true) Authentication authentication) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(bookmarkCategoryService.list(userId));
    }

    @Operation(summary = "新建分类")
    @PostMapping
    public Result<BookmarkCategoryVO> create(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody BookmarkCategoryDTO dto) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(bookmarkCategoryService.create(userId, dto));
    }

    @Operation(summary = "编辑分类")
    @PutMapping("/{id}")
    public Result<BookmarkCategoryVO> update(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody BookmarkCategoryDTO dto) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(bookmarkCategoryService.update(id, userId, dto));
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        bookmarkCategoryService.delete(id, userId);
        return Result.success();
    }
}
