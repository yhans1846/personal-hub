package com.personalhub.module.todo.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.personalhub.module.todo.dto.TodoCreateDTO;
import com.personalhub.module.todo.dto.TodoQueryDTO;
import com.personalhub.module.todo.vo.TodoVO;
import java.util.List;

/**
 * 待办任务服务接口
 */
public interface TodoTaskService {

    /**
     * 分页查询任务列表
     *
     * @param userId 用户ID
     * @param query  查询条件（支持关键词搜索、优先级筛选、完成状态筛选）
     * @return 任务分页数据
     */
    IPage<TodoVO> list(Long userId, TodoQueryDTO query);

    /**
     * 获取任务详情
     *
     * @param id     任务ID
     * @param userId 用户ID
     * @return 任务VO
     */
    TodoVO getById(Long id, Long userId);

    /**
     * 新建任务
     *
     * @param userId 用户ID
     * @param dto    任务创建参数
     * @return 新建的任务VO
     */
    TodoVO create(Long userId, TodoCreateDTO dto);

    /**
     * 编辑任务
     *
     * @param id     任务ID
     * @param userId 用户ID
     * @param dto    任务编辑参数
     * @return 更新后的任务VO
     */
    TodoVO update(Long id, Long userId, TodoCreateDTO dto);

    /**
     * 删除任务（软删除）
     *
     * @param id     任务ID
     * @param userId 用户ID
     */
    void delete(Long id, Long userId);

    /**
     * 切换完成状态
     *
     * @param id     任务ID
     * @param userId 用户ID
     * @return 更新后的任务VO
     */
    TodoVO toggleDone(Long id, Long userId);

    /**
     * 今日待办列表（未完成的今日任务）
     *
     * @param userId 用户ID
     * @return 今日待办列表
     */
    List<TodoVO> today(Long userId);
}
