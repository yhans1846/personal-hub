package com.personalhub.module.todo.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.common.result.PageResult;
import com.personalhub.common.result.Result;
import com.personalhub.module.todo.dto.TodoCreateDTO;
import com.personalhub.module.todo.dto.TodoQueryDTO;
import com.personalhub.module.todo.service.TodoTaskService;
import com.personalhub.module.todo.vo.TodoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 待办任务控制器
 */
@Tag(name = "待办任务", description = "待办任务的增删改查、完成状态切换")
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoTaskController {

    private final TodoTaskService todoTaskService;

    @Operation(summary = "待办列表", description = "分页查询待办任务，支持关键词搜索、优先级筛选、完成状态筛选")
    @GetMapping
    public Result<PageResult<TodoVO>> list(
            @Parameter(hidden = true) Authentication authentication,
            TodoQueryDTO query) {
        Long userId = Long.valueOf(authentication.getName());
        IPage<TodoVO> page = todoTaskService.list(userId, query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "任务详情")
    @GetMapping("/{id}")
    public Result<TodoVO> getById(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(todoTaskService.getById(id, userId));
    }

    @Operation(summary = "新建任务")
    @PostMapping
    public Result<TodoVO> create(
            @Parameter(hidden = true) Authentication authentication,
            @Valid @RequestBody TodoCreateDTO dto) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(todoTaskService.create(userId, dto));
    }

    @Operation(summary = "编辑任务")
    @PutMapping("/{id}")
    public Result<TodoVO> update(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody TodoCreateDTO dto) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(todoTaskService.update(id, userId, dto));
    }

    @Operation(summary = "删除任务")
    @DeleteMapping("/{id}")
    public Result<Void> delete(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        todoTaskService.delete(id, userId);
        return Result.success();
    }

    @Operation(summary = "切换完成状态")
    @PatchMapping("/{id}/done")
    public Result<TodoVO> toggleDone(
            @Parameter(hidden = true) Authentication authentication,
            @PathVariable Long id) {
        Long userId = Long.valueOf(authentication.getName());
        return Result.success(todoTaskService.toggleDone(id, userId));
    }
}
