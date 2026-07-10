package com.personalhub.module.todo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.personalhub.module.todo.entity.TodoTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * 待办任务 Mapper
 */
@Mapper
public interface TodoTaskMapper extends BaseMapper<TodoTask> {
}
