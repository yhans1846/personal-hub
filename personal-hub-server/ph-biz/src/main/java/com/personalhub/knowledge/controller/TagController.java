package com.personalhub.knowledge.controller;

import com.personalhub.common.result.Result;
import com.personalhub.common.util.CurrentUser;
import com.personalhub.knowledge.dto.TagCreateDTO;
import com.personalhub.knowledge.dto.TagUpdateDTO;
import com.personalhub.knowledge.service.TagService;
import com.personalhub.knowledge.vo.TagVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Tag(name = "标签管理")
public class TagController {

    private final TagService tagService;

    @GetMapping
    @Operation(summary = "获取所有标签")
    public Result<List<TagVO>> list(Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(tagService.listByUser(userId));
    }

    @PostMapping
    @Operation(summary = "创建标签")
    public Result<TagVO> create(@Valid @RequestBody TagCreateDTO dto, Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        return Result.success(tagService.create(userId, dto.getName(), dto.getColor()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新标签")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody TagUpdateDTO dto, Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        tagService.update(id, userId, dto.getName(), dto.getColor());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除标签")
    public Result<Void> delete(@PathVariable Long id, Authentication authentication) {
        Long userId = CurrentUser.id(authentication);
        tagService.delete(id, userId);
        return Result.success();
    }

    // ========== 标签绑定 ==========

    @PostMapping("/{id}/bind")
    @Operation(summary = "绑定标签到实体")
    public Result<Void> bind(@PathVariable Long id,
                             @RequestParam String entityType,
                             @RequestParam Long entityId) {
        tagService.bindTag(id, entityType, entityId);
        return Result.success();
    }

    @DeleteMapping("/{id}/unbind")
    @Operation(summary = "解绑标签")
    public Result<Void> unbind(@PathVariable Long id,
                               @RequestParam String entityType,
                               @RequestParam Long entityId) {
        tagService.unbindTag(id, entityType, entityId);
        return Result.success();
    }

    @GetMapping("/entities")
    @Operation(summary = "获取实体的标签列表")
    public Result<List<TagVO>> getEntityTags(@RequestParam String entityType,
                                             @RequestParam Long entityId) {
        return Result.success(tagService.getTags(entityType, entityId));
    }

    @PutMapping("/entities/{entityType}/{entityId}")
    @Operation(summary = "批量设置实体标签")
    public Result<Void> setEntityTags(@PathVariable String entityType,
                                      @PathVariable Long entityId,
                                      @RequestBody List<Long> tagIds) {
        tagService.bindTags(entityId, entityType, tagIds);
        return Result.success();
    }
}
