package com.personalhub.planning.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.personalhub.common.exception.NotFoundException;
import com.personalhub.common.util.EntityGuard;
import com.personalhub.planning.dto.TodoCreateDTO;
import com.personalhub.planning.dto.TodoQueryDTO;
import com.personalhub.planning.entity.TodoTask;
import com.personalhub.planning.mapper.TodoTaskMapper;
import com.personalhub.planning.service.TodoTaskService;
import com.personalhub.planning.vo.TodoVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;

/**
 * 待办任务服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TodoTaskServiceImpl implements TodoTaskService {

    private final TodoTaskMapper todoTaskMapper;

    @Override
    public IPage<TodoVO> list(Long userId, TodoQueryDTO query) {
        LambdaQueryWrapper<TodoTask> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TodoTask::getUserId, userId);

        // 搜索关键词
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.like(TodoTask::getTitle, query.getKeyword());
        }
        // 优先级筛选
        if (query.getPriority() != null) {
            wrapper.eq(TodoTask::getPriority, query.getPriority());
        }
        // 完成状态筛选
        if (query.getIsDone() != null) {
            wrapper.eq(TodoTask::getIsDone, query.getIsDone() ? 1 : 0);
        }

        String scope = query.getDueScope();
        if (StringUtils.hasText(scope) && !"all".equalsIgnoreCase(scope)) {
            LocalDate today = LocalDate.now();
            switch (scope.toLowerCase()) {
                case "done" -> wrapper.eq(TodoTask::getIsDone, 1);
                case "overdue" -> wrapper.eq(TodoTask::getIsDone, 0)
                        .isNotNull(TodoTask::getDueDate)
                        .lt(TodoTask::getDueDate, today);
                case "today" -> wrapper.eq(TodoTask::getIsDone, 0)
                        .eq(TodoTask::getDueDate, today);
                case "week" -> wrapper.eq(TodoTask::getIsDone, 0)
                        .isNotNull(TodoTask::getDueDate)
                        .gt(TodoTask::getDueDate, today)
                        .le(TodoTask::getDueDate, today.plusDays(7));
                case "later" -> wrapper.eq(TodoTask::getIsDone, 0)
                        .and(w -> w.isNull(TodoTask::getDueDate)
                                .or()
                                .gt(TodoTask::getDueDate, today.plusDays(7)));
                default -> { /* ignore unknown */ }
            }
        }

        // 按创建时间倒序，未完成排前
        wrapper.orderByAsc(TodoTask::getIsDone)
               .orderByDesc(TodoTask::getCreatedAt);

        Page<TodoTask> page = new Page<>(query.getPage(), query.getSize());
        IPage<TodoTask> taskPage = todoTaskMapper.selectPage(page, wrapper);

        return taskPage.convert(TodoVO::from);
    }

    @Override
    public TodoVO getById(Long id, Long userId) {
        TodoTask task = EntityGuard.requireOwned(
                todoTaskMapper.selectById(id), userId, TodoTask::getUserId, "任务不存在");
        return TodoVO.from(task);
    }

    @Override
    @Transactional
    public TodoVO create(Long userId, TodoCreateDTO dto) {
        var task = TodoTask.builder()
                .userId(userId)
                .title(dto.getTitle())
                .content(dto.getContent())
                .priority(dto.getPriority() != null ? dto.getPriority() : 2)
                .dueDate(dto.getDueDate())
                .build();
        todoTaskMapper.insert(task);
        log.info("新建待办任务: id={}, userId={}, title={}", task.getId(), userId, dto.getTitle());
        return TodoVO.from(task);
    }

    @Override
    @Transactional
    public TodoVO update(Long id, Long userId, TodoCreateDTO dto) {
        TodoTask task = EntityGuard.requireOwned(
                todoTaskMapper.selectById(id), userId, TodoTask::getUserId, "任务不存在");
        task.setTitle(dto.getTitle());
        task.setContent(dto.getContent());
        task.setPriority(dto.getPriority() != null ? dto.getPriority() : 2);
        task.setDueDate(dto.getDueDate());
        todoTaskMapper.updateById(task);
        log.info("编辑待办任务: id={}, userId={}", id, userId);
        return TodoVO.from(task);
    }

    @Override
    @Transactional
    public void delete(Long id, Long userId) {
        EntityGuard.requireOwned(
                todoTaskMapper.selectById(id), userId, TodoTask::getUserId, "任务不存在");
        todoTaskMapper.deleteById(id); // MyBatis-Plus 逻辑删除
        log.info("删除待办任务: id={}, userId={}", id, userId);
    }

    @Override
    @Transactional
    public TodoVO toggleDone(Long id, Long userId) {
        TodoTask task = EntityGuard.requireOwned(
                todoTaskMapper.selectById(id), userId, TodoTask::getUserId, "任务不存在");
        task.setIsDone(task.getIsDone() == null || task.getIsDone() == 0 ? 1 : 0);
        if (task.getIsDone() == 1) {
            task.setCompletedAt(java.time.LocalDateTime.now());
        } else {
            task.setCompletedAt(null);
        }
        todoTaskMapper.updateById(task);
        log.info("切换待办任务状态: id={}, userId={}, isDone={}", id, userId, task.getIsDone());
        return TodoVO.from(task);
    }

    @Override
    public java.util.List<TodoVO> today(Long userId) {
        LambdaQueryWrapper<TodoTask> w = new LambdaQueryWrapper<>();
        w.eq(TodoTask::getUserId, userId);
        w.eq(TodoTask::getIsDone, 0);
        w.eq(TodoTask::getDueDate, java.time.LocalDate.now());
        w.orderByAsc(TodoTask::getPriority);
        return todoTaskMapper.selectList(w).stream().map(TodoVO::from).toList();
    }
}
