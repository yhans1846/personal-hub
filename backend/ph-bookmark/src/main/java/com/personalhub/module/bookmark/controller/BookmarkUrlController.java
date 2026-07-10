package com.personalhub.module.bookmark.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.common.result.PageResult;
import com.personalhub.common.result.Result;
import com.personalhub.module.bookmark.dto.BookmarkCreateDTO;
import com.personalhub.module.bookmark.dto.BookmarkQueryDTO;
import com.personalhub.module.bookmark.service.BookmarkUrlService;
import com.personalhub.module.bookmark.vo.BookmarkVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏夹控制器
 */
@Tag(name = "收藏夹", description = "收藏夹的增删改查、分类筛选、标签筛选、关键词搜索")
@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkUrlController {

    private final BookmarkUrlService bookmarkUrlService;

    @Operation(summary = "收藏列表", description = "分页查询收藏，支持关键词搜索、分类筛选、标签筛选")
    @GetMapping
    public Result<PageResult<BookmarkVO>> list(
            @Parameter(hidden = true) Authentication authentication,
            BookmarkQueryDTO query) {
        Long userId = Long.valueOf(authentication.getName());
        IPage<BookmarkVO> page = bookmarkUrlService.list(userId, query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "收藏详情")
    @GetMapping("/{id}")
    public Result<BookmarkVO> getById(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(bookmarkUrlService.getById(id, userId));
    }

    @Operation(summary = "新建收藏")
    @PostMapping
    public Result<BookmarkVO> create(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody BookmarkCreateDTO dto) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(bookmarkUrlService.create(userId, dto));
    }

    @Operation(summary = "编辑收藏")
    @PutMapping("/{id}")
    public Result<BookmarkVO> update(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody BookmarkCreateDTO dto) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(bookmarkUrlService.update(id, userId, dto));
    }

    @Operation(summary = "删除收藏")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        bookmarkUrlService.delete(id, userId);
        return Result.success();
    }
}
