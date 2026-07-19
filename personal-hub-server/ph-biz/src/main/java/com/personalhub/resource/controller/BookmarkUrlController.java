package com.personalhub.resource.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.common.result.PageResult;
import com.personalhub.common.result.Result;
import com.personalhub.common.util.CurrentUser;
import com.personalhub.resource.dto.BookmarkCreateDTO;
import com.personalhub.resource.dto.BookmarkQueryDTO;
import com.personalhub.resource.service.BookmarkUrlService;
import com.personalhub.resource.vo.BookmarkVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 收藏夹控制器
 */
@Tag(name = "收藏夹", description = "收藏夹的增删改查、分类筛选、标签筛选、关键词搜索")
@Validated
@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkUrlController {

    private final BookmarkUrlService bookmarkUrlService;

    @Operation(summary = "首页外部快捷", description = "返回勾选「展示到首页」的收藏，按更新时间倒序")
    @GetMapping("/dashboard")
    public Result<java.util.List<BookmarkVO>> dashboardList(
            @Parameter(hidden = true) Authentication authentication,
            @RequestParam(defaultValue = "8") int limit) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(bookmarkUrlService.listForDashboard(userId, limit));
    }

    @Operation(summary = "收藏列表", description = "分页查询收藏，支持关键词搜索、分类筛选、标签筛选")
    @GetMapping
    public Result<PageResult<BookmarkVO>> list(
            @Parameter(hidden = true) Authentication authentication,
            @Valid BookmarkQueryDTO query) {
        Long userId = CurrentUser.id(authentication);
        IPage<BookmarkVO> page = bookmarkUrlService.list(userId, query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "收藏详情")
    @GetMapping("/{id}")
    public Result<BookmarkVO> getById(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(bookmarkUrlService.getById(id, userId));
    }

    @Operation(summary = "新建收藏")
    @PostMapping
    public Result<BookmarkVO> create(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody BookmarkCreateDTO dto) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(bookmarkUrlService.create(userId, dto));
    }

    @Operation(summary = "编辑收藏")
    @PutMapping("/{id}")
    public Result<BookmarkVO> update(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody BookmarkCreateDTO dto) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(bookmarkUrlService.update(id, userId, dto));
    }

    @Operation(summary = "删除收藏")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = CurrentUser.id(authentication);
        bookmarkUrlService.delete(id, userId);
        return Result.success();
    }
}
